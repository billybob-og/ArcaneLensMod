package com.arcanelens.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraftforge.event.TickEvent;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

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
        enqueue(level, template, origin, settings, random, null);
    }

    /** Same as the four-arg enqueue, but runs onComplete once every block in the job has actually been
     * placed (after the final shape/neighbor-update pass, not merely once the last block is set) - for
     * callers that need to react to a specific placement finishing, e.g. the Endless Dungeon addon replacing
     * jigsaw blocks with their final_state once a room is fully down. Safe to call from another subproject:
     * a plain Runnable, no new public API surface on PlacementJob itself. */
    public static void enqueue(ServerLevel level, StructureTemplate template, BlockPos origin, StructurePlaceSettings settings,
                                RandomSource random, @Nullable Runnable onComplete)
    {
        List<StructureTemplate.StructureBlockInfo> rawBlocks = readRawBlocks(level, template);
        List<StructureTemplate.StructureBlockInfo> positioned = StructureTemplate.processBlockInfos(level, origin, origin, settings, rawBlocks);

        Deque<StructureTemplate.StructureBlockInfo> pending = new ArrayDeque<>(positioned.size());
        for (StructureTemplate.StructureBlockInfo info : positioned)
        {
            BlockState state = info.state().mirror(settings.getMirror()).rotate(settings.getRotation());
            pending.add(new StructureTemplate.StructureBlockInfo(info.pos(), state, info.nbt()));
        }

        // Entities (item frames, paintings, armor stands, minecarts, etc.) live in the template's own
        // separate ENTITIES_TAG, never in BLOCKS_TAG/PALETTE_TAG - readRawBlocks above never touched them, so
        // without this every entity in every structure this placer has ever placed was silently dropped.
        // processEntityInfos does the same world-space transform (mirror/rotation/origin) processBlockInfos
        // does for blocks, just for entity positions - safe to resolve up front like the blocks are, since
        // nothing about it depends on the incremental placement actually happening yet.
        List<StructureTemplate.StructureEntityInfo> rawEntities = readRawEntities(template);
        List<StructureTemplate.StructureEntityInfo> positionedEntities =
                StructureTemplate.processEntityInfos(template, level, origin, settings, rawEntities);

        ACTIVE_JOBS.add(new PlacementJob(level, pending, positionedEntities, settings, random, onComplete));
    }

    public static void onServerTick(TickEvent.ServerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || ACTIVE_JOBS.isEmpty())
        {
            return;
        }

        // A job's onComplete callback can itself call enqueue() (e.g. a callback that seals a connector with
        // another structure once this one's done) - that appends to this SAME list while it's still being
        // processed. Iterating by index up to a count captured before the loop starts (rather than an
        // Iterator) means a job enqueued mid-loop is simply left for a later tick instead of triggering a
        // ConcurrentModificationException; finished jobs are removed in one batch after the loop, not during
        // it, for the same reason.
        int count = ACTIVE_JOBS.size();
        List<PlacementJob> finished = new ArrayList<>();
        for (int i = 0; i < count; i++)
        {
            PlacementJob job = ACTIVE_JOBS.get(i);
            job.placeBatch(BLOCKS_PER_TICK);
            if (job.isDone())
            {
                job.finish();
                finished.add(job);
            }
        }
        ACTIVE_JOBS.removeAll(finished);
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

    /** Same idea as readRawBlocks, for the template's ENTITIES_TAG - StructureTemplate.entityInfoList itself
     * is private with no getter, so this re-parses the raw saved NBT directly, matching StructureTemplate's
     * own load() format exactly (including its behavior of silently skipping any entry with no "nbt" tag). */
    private static List<StructureTemplate.StructureEntityInfo> readRawEntities(StructureTemplate template)
    {
        CompoundTag tag = template.save(new CompoundTag());
        ListTag entitiesTag = tag.getList(StructureTemplate.ENTITIES_TAG, Tag.TAG_COMPOUND);
        List<StructureTemplate.StructureEntityInfo> result = new ArrayList<>(entitiesTag.size());
        for (int i = 0; i < entitiesTag.size(); i++)
        {
            CompoundTag entry = entitiesTag.getCompound(i);
            if (!entry.contains(StructureTemplate.ENTITY_TAG_NBT))
            {
                continue;
            }
            ListTag posTag = entry.getList(StructureTemplate.ENTITY_TAG_POS, Tag.TAG_DOUBLE);
            Vec3 pos = new Vec3(posTag.getDouble(0), posTag.getDouble(1), posTag.getDouble(2));
            ListTag blockPosTag = entry.getList(StructureTemplate.ENTITY_TAG_BLOCKPOS, Tag.TAG_INT);
            BlockPos blockPos = new BlockPos(blockPosTag.getInt(0), blockPosTag.getInt(1), blockPosTag.getInt(2));
            CompoundTag nbt = entry.getCompound(StructureTemplate.ENTITY_TAG_NBT);
            result.add(new StructureTemplate.StructureEntityInfo(pos, blockPos, nbt));
        }
        return result;
    }

    private static final class PlacementJob
    {
        private final ServerLevel level;
        private final Deque<StructureTemplate.StructureBlockInfo> pending;
        private final List<StructureTemplate.StructureEntityInfo> entities;
        private final StructurePlaceSettings settings;
        private final List<BlockPos> placedPositions = new ArrayList<>();
        private final RandomSource random;
        @Nullable
        private final Runnable onComplete;

        PlacementJob(ServerLevel level, Deque<StructureTemplate.StructureBlockInfo> pending,
                     List<StructureTemplate.StructureEntityInfo> entities, StructurePlaceSettings settings,
                     RandomSource random, @Nullable Runnable onComplete)
        {
            this.level = level;
            this.pending = pending;
            this.entities = entities;
            this.settings = settings;
            this.random = random;
            this.onComplete = onComplete;
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
                spawnEntities();
                if (onComplete != null)
                {
                    onComplete.run();
                }
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

            spawnEntities();
            if (onComplete != null)
            {
                onComplete.run();
            }
        }

        /** Reimplements StructureTemplate.addEntitiesToWorld (private, can't be called directly) - `entities`
         * was already transformed into world-space by processEntityInfos back in enqueue(), so this just
         * constructs and drops each one in, matching vanilla's own placeInWorld behavior exactly: strip the
         * template-baked UUID (two placements of the same room must never produce colliding entity UUIDs),
         * rotate/mirror the entity's own facing to match the room's placement settings, and finalizeSpawn
         * mobs the same way a freshly-generated structure would if the settings ask for it. */
        private void spawnEntities()
        {
            for (StructureTemplate.StructureEntityInfo info : entities)
            {
                CompoundTag nbt = info.nbt.copy();
                ListTag posTag = new ListTag();
                posTag.add(DoubleTag.valueOf(info.pos.x));
                posTag.add(DoubleTag.valueOf(info.pos.y));
                posTag.add(DoubleTag.valueOf(info.pos.z));
                nbt.put("Pos", posTag);
                nbt.remove("UUID");

                Optional<Entity> created;
                try
                {
                    created = EntityType.create(nbt, level);
                }
                catch (Exception exception)
                {
                    created = Optional.empty();
                }

                created.ifPresent(entity ->
                {
                    float yRot = entity.rotate(settings.getRotation());
                    yRot += entity.mirror(settings.getMirror()) - entity.getYRot();
                    entity.moveTo(info.pos.x, info.pos.y, info.pos.z, yRot, entity.getXRot());
                    if (settings.shouldFinalizeEntities() && entity instanceof Mob mob)
                    {
                        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(info.pos)),
                                MobSpawnType.STRUCTURE, (SpawnGroupData) null, nbt);
                    }
                    level.addFreshEntityWithPassengers(entity);
                });
            }
        }
    }
}
