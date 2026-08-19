package com.arcanelens.worldgen;

import com.arcanelens.ArcaneLens;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.UUID;

/**
 * Places (or confirms already-placed) a player's personal room in the shared Pocket Dimension level, at a
 * fixed per-player cell (see PocketDimensionSavedData). Rooms only ever grow (Pocket Dimension Expansion is
 * a one-way skill purchase), so a bigger schematic pasted at the same origin naturally overwrites the
 * smaller room's walls with its own interior air/walls - no separate "clear old room" step is needed.
 */
public class PocketDimensionPlacer
{
    // Comfortably exceeds even a 32-chunk (512-block) view distance doubled, so no two players' loaded
    // chunks can ever overlap - a correctness margin, not a tuning knob.
    private static final int CELL_SPACING = 2048;
    private static final int CELL_Y = 64;

    private static final ResourceLocation[] ROOM_STRUCTURES = {
            new ResourceLocation(ArcaneLens.MODID, "pocket_dimension_9x9"),
            new ResourceLocation(ArcaneLens.MODID, "pocket_dimension_12x12"),
            new ResourceLocation(ArcaneLens.MODID, "pocket_dimension_15x15"),
    };

    /** @return the world position to teleport the player into - center of the room, one block above the floor. */
    public static BlockPos ensureRoomPlaced(ServerLevel pocketLevel, UUID uuid, int expansionLevel)
    {
        PocketDimensionSavedData data = PocketDimensionSavedData.get(pocketLevel);
        int cellIndex = data.getOrAssignCell(uuid);
        BlockPos origin = new BlockPos(cellIndex * CELL_SPACING, CELL_Y, 0);

        int level = Math.max(0, Math.min(expansionLevel, ROOM_STRUCTURES.length - 1));
        StructureTemplateManager structureManager = pocketLevel.getStructureManager();
        StructureTemplate template = structureManager.getOrCreate(ROOM_STRUCTURES[level]);

        if (data.getPlacedRoomSize(uuid) != level)
        {
            // Force the origin chunk to actually exist before pasting into it.
            pocketLevel.getChunk(origin.getX() >> 4, origin.getZ() >> 4);
            StructurePlaceSettings settings = new StructurePlaceSettings().addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
            template.placeInWorld(pocketLevel, origin, origin, settings, RandomSource.create(), 2);
            data.setPlacedRoomSize(uuid, level);
        }

        Vec3i size = template.getSize();
        return origin.offset(size.getX() / 2, 1, size.getZ() / 2);
    }
}
