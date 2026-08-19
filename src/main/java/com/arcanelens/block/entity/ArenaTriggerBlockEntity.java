package com.arcanelens.block.entity;

import com.arcanelens.ArcaneLens;
import com.arcanelens.registry.ModBlockEntities;
import com.arcanelens.worldgen.BossArenaSavedData;
import com.arcanelens.worldgen.IncrementalStructurePlacer;
import com.arcanelens.worldgen.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;

/**
 * Pastes the boss arena template directly below this block, the first time its chunk loads for real, then
 * replaces itself with air (this sits in the middle of the pitfall shaft, so it needs to become an open
 * hole, not a solid floor cap) so it never fires again. See ArenaTriggerBlock for the full "why" - this
 * exists because the arena is far too large to be a normal jigsaw pool candidate, and a custom
 * StructureProcessorType can't be registered by mods in this version.
 */
public class ArenaTriggerBlockEntity extends BlockEntity
{
    private static final ResourceLocation ARENA_ID = new ResourceLocation(ArcaneLens.MODID, "castle_dungeon_boss_arena");

    // The arena is 51 blocks tall (castle_dungeon_boss_arena.nbt); dropping by (height - 1) from this
    // block's own position puts the arena's ceiling flush with the trigger block, so the player falls
    // straight from the pitfall hole into open arena space below.
    private static final int ARENA_HEIGHT = 51;

    // Verified directly from castle_dungeon_boss_arena.nbt: the ceiling has a narrow ~9x9 shaft opening
    // (before it widens into the full room a few layers down) spanning local X=[19,27], Z=[22,30] - center
    // (23, 26). The earlier value (14, 26) was a guess based on the original combined room's jigsaw block
    // position, which turned out not to actually match the arena's real ceiling opening.
    private static final int ARENA_SHAFT_LOCAL_X = 23;
    private static final int ARENA_SHAFT_LOCAL_Z = 26;

    public ArenaTriggerBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.ARENA_TRIGGER.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos)
    {
        if (!(level instanceof ServerLevel serverLevel))
        {
            return;
        }

        StructureTemplateManager manager = serverLevel.getStructureManager();
        Optional<StructureTemplate> arena = manager.get(ARENA_ID);
        if (arena.isEmpty())
        {
            return;
        }

        // At most one real arena per castle_dungeon instance - a dungeon that rolls the boss entrance room
        // more than once would otherwise paste multiple giant arenas on top of each other. If this block
        // isn't actually part of a generated dungeon (e.g. hand-placed in a WorldEdit build/test world, with
        // no structure start on record), leave it alone entirely rather than firing - a "paste unconditionally"
        // fallback used to live here, but it meant the block self-destructed the instant it was placed by
        // hand, making it impossible to ever place this block in a build and //copy it into a structure
        // export (it would already be air by the time you tried). Only real, generated dungeon instances
        // should ever cause this to fire.
        Structure castleDungeon = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ModStructures.CASTLE_DUNGEON);
        StructureStart dungeonStart = castleDungeon != null ? serverLevel.structureManager().getStructureAt(pos, castleDungeon) : null;
        if (dungeonStart == null || !dungeonStart.isValid())
        {
            return;
        }

        Rotation entranceRotation = Rotation.NONE;
        ChunkPos dungeonOrigin = dungeonStart.getChunkPos();
        if (!BossArenaSavedData.get(serverLevel).tryClaim(dungeonOrigin))
        {
            // Another boss entrance in this same dungeon already claimed the arena - cap the hole with a
            // solid block instead of pasting a second arena on top of it (leaving this as open air would
            // just be a pointless pitfall into nothing).
            serverLevel.setBlockAndUpdate(pos, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
            return;
        }

        // The entrance room (like any jigsaw piece with a "rollable" joint) can generate in any of the 4
        // rotations - find its actual placed rotation so both the shaft offset and the arena's own
        // orientation match how this specific instance came out, instead of always assuming unrotated.
        for (StructurePiece piece : dungeonStart.getPieces())
        {
            if (piece instanceof PoolElementStructurePiece poolPiece && piece.getBoundingBox().isInside(pos))
            {
                entranceRotation = poolPiece.getRotation();
                break;
            }
        }

        BlockPos shaftOffset = StructureTemplate.transform(
                new BlockPos(ARENA_SHAFT_LOCAL_X, 0, ARENA_SHAFT_LOCAL_Z), Mirror.NONE, entranceRotation, BlockPos.ZERO);
        BlockPos arenaPos = pos.offset(-shaftOffset.getX(), -(ARENA_HEIGHT - 1), -shaftOffset.getZ());
        StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(entranceRotation);

        // Spread across many ticks instead of one giant placeInWorld() call - the arena is ~160,000 blocks,
        // and pasting all of it in a single tick causes a serious lag spike (and, on memory-constrained
        // systems, can crash the JVM outright - see IncrementalStructurePlacer's own javadoc).
        IncrementalStructurePlacer.enqueue(serverLevel, arena.get(), arenaPos, settings, serverLevel.getRandom());

        // Record the arena's real world bounds so ArenaSpawnHandler can block natural monster spawns there -
        // this piece is invisible to the dungeon structure's own tracked bounding box (it's pasted directly,
        // not through the jigsaw pool system), so the structure-wide spawnOverrides in ModStructures never
        // covers it on its own.
        BossArenaSavedData.get(serverLevel).recordArenaBounds(arena.get().getBoundingBox(arenaPos, entranceRotation, BlockPos.ZERO, Mirror.NONE));

        serverLevel.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }
}
