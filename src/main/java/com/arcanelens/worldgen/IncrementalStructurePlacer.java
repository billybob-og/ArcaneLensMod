package com.arcanelens.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * Places a StructureTemplate's blocks a batch at a time across many server ticks instead of all in one tick,
 * for structures large enough that a single-shot StructureTemplate.placeInWorld() call causes a serious lag
 * spike (and, on memory-constrained systems, can cause a native allocation failure) - see the boss arena
 * (~57x50x57, 160,000+ blocks). Reimplements placeInWorld's own pipeline using only its public building
 * blocks (StructureTemplate.processBlockInfos/updateShapeAtEdge, Block.updateFromNeighbourShapes) so the end
 * result matches a normal placement - the block-set pass is just spread out, and shape/neighbor updates are
 * deferred to a single final pass once every block is down (that recomputation needs the whole structure to
 * already exist to be correct, so it can't itself be incremental).
 */
public class IncrementalStructurePlacer
{
    private static final int BLOCKS_PER_TICK = 1000;

    private static final List<PlacementJob> ACTIVE_JOBS = new ArrayList<>();

    public static void enqueue(ServerLevel level, StructureTemplate template, BlockPos origin, StructurePlaceSettings settings, RandomSource random)
    {
        List<StructureTemplate.StructureBlockInfo> rawBlocks = readRawBlocks(level, template);
        List<StructureTemplate.StructureBlockInfo> positioned = StructureTemplate.processBlockInfos(level, origin, origin, settings, rawBlocks);

        Deque<StructureTemplate.StructureBlockInfo> pending = new ArrayDeque<>(positioned.size());
        for (StructureTemplate.StructureBlockInfo info : positioned)
        {
            BlockState state = info.state().mirror(settings.getMirror()).rotate(settings.getRotation());
            pending.add(new StructureTemplate.StructureBlockInfo(info.pos(), state, info.nbt()));
        }

        ACTIVE_JOBS.add(new PlacementJob(level, pending, random));
    }

    public static void onServerTick(TickEvent.ServerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || ACTIVE_JOBS.isEmpty())
        {
            return;
        }

        Iterator<PlacementJob> iterator = ACTIVE_JOBS.iterator();
        while (iterator.hasNext())
        {
            PlacementJob job = iterator.next();
            job.placeBatch(BLOCKS_PER_TICK);
            if (job.isDone())
            {
                job.finish();
                iterator.remove();
            }
        }
    }

    /** Reads the template's raw (untransformed, local-space) block list straight from its own save() output -
     * this is the only way to get the full per-block list without a Block filter, since
     * StructureTemplate.filterBlocks(pos, settings, block) requires a specific Block to match against and has
     * no "match everything" option (passing null matches nothing, since it filters via state.is(null)). */
    private static List<StructureTemplate.StructureBlockInfo> readRawBlocks(ServerLevel level, StructureTemplate template)
    {
        CompoundTag tag = template.save(new CompoundTag());
        HolderGetter<Block> blockLookup = level.registryAccess().lookupOrThrow(Registries.BLOCK);

        ListTag paletteTag = tag.getList(StructureTemplate.PALETTE_TAG, Tag.TAG_COMPOUND);
        List<BlockState> palette = new ArrayList<>(paletteTag.size());
        for (int i = 0; i < paletteTag.size(); i++)
        {
            palette.add(NbtUtils.readBlockState(blockLookup, paletteTag.getCompound(i)));
        }

        ListTag blocksTag = tag.getList(StructureTemplate.BLOCKS_TAG, Tag.TAG_COMPOUND);
        List<StructureTemplate.StructureBlockInfo> result = new ArrayList<>(blocksTag.size());
        for (int i = 0; i < blocksTag.size(); i++)
        {
            CompoundTag entry = blocksTag.getCompound(i);
            ListTag posTag = entry.getList(StructureTemplate.BLOCK_TAG_POS, Tag.TAG_INT);
            BlockPos pos = new BlockPos(posTag.getInt(0), posTag.getInt(1), posTag.getInt(2));
            BlockState state = palette.get(entry.getInt(StructureTemplate.BLOCK_TAG_STATE));
            CompoundTag nbt = entry.contains(StructureTemplate.BLOCK_TAG_NBT) ? entry.getCompound(StructureTemplate.BLOCK_TAG_NBT) : null;
            result.add(new StructureTemplate.StructureBlockInfo(pos, state, nbt));
        }
        return result;
    }

    private static final class PlacementJob
    {
        private final ServerLevel level;
        private final Deque<StructureTemplate.StructureBlockInfo> pending;
        private final List<BlockPos> placedPositions = new ArrayList<>();
        private final RandomSource random;

        PlacementJob(ServerLevel level, Deque<StructureTemplate.StructureBlockInfo> pending, RandomSource random)
        {
            this.level = level;
            this.pending = pending;
            this.random = random;
        }

        void placeBatch(int count)
        {
            for (int i = 0; i < count && !pending.isEmpty(); i++)
            {
                placeOne(pending.poll());
            }
        }

        private void placeOne(StructureTemplate.StructureBlockInfo info)
        {
            BlockPos pos = info.pos();
            if (info.nbt() != null)
            {
                // Matches StructureTemplate.placeInWorld's own approach: force any pre-existing block entity
                // at this position to clear/discard before the new block (and its own block entity) go in.
                Clearable.tryClear(level.getBlockEntity(pos));
                level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 20);
            }

            if (!level.setBlock(pos, info.state(), Block.UPDATE_CLIENTS))
            {
                return;
            }
            placedPositions.add(pos);

            if (info.nbt() != null)
            {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity != null)
                {
                    CompoundTag nbt = info.nbt();
                    if (blockEntity instanceof RandomizableContainerBlockEntity)
                    {
                        nbt = nbt.copy();
                        nbt.putLong("LootTableSeed", random.nextLong());
                    }
                    blockEntity.load(nbt);
                }
            }
        }

        boolean isDone()
        {
            return pending.isEmpty();
        }

        /** Runs once, after every block in the structure is already placed - mirrors the tail of
         * StructureTemplate.placeInWorld (recompute connectable-block shapes at the structure's edges, then
         * let each placed block react to its now-complete neighborhood). Needs the whole structure to already
         * exist to give correct results, so unlike the block-set pass this can't be spread across ticks. */
        void finish()
        {
            if (placedPositions.isEmpty())
            {
                return;
            }

            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
            for (BlockPos pos : placedPositions)
            {
                minX = Math.min(minX, pos.getX());
                minY = Math.min(minY, pos.getY());
                minZ = Math.min(minZ, pos.getZ());
                maxX = Math.max(maxX, pos.getX());
                maxY = Math.max(maxY, pos.getY());
                maxZ = Math.max(maxZ, pos.getZ());
            }

            BitSetDiscreteVoxelShape shape = new BitSetDiscreteVoxelShape(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
            for (BlockPos pos : placedPositions)
            {
                shape.fill(pos.getX() - minX, pos.getY() - minY, pos.getZ() - minZ);
            }
            StructureTemplate.updateShapeAtEdge(level, Block.UPDATE_CLIENTS, shape, minX, minY, minZ);

            for (BlockPos pos : placedPositions)
            {
                BlockState current = level.getBlockState(pos);
                BlockState updated = Block.updateFromNeighbourShapes(current, level, pos);
                if (current != updated)
                {
                    level.setBlock(pos, updated, (Block.UPDATE_CLIENTS & -2) | Block.UPDATE_KNOWN_SHAPE);
                }
                level.blockUpdated(pos, updated.getBlock());
            }
        }
    }
}
