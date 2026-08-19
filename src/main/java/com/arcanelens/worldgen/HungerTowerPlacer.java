package com.arcanelens.worldgen;

import com.arcanelens.ArcaneLens;
import com.arcanelens.entity.HungerIdolEntity;
import com.arcanelens.registry.ModEntityTypes;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

/**
 * The Hunger Tower: a single world-wide landmark (not per-player) that appears at (0, ground, 0) the first
 * time any player's watching chance crosses the eyes-reveal threshold (see TheHungerHandler). Built from a
 * base piece plus the top piece repeated TOP_REPEAT_COUNT times (a Structure Block export split up to get
 * under the size limit) - each piece's own height is used to position the next directly on top, so they
 * read as one continuous tower regardless of any piece's exact height.
 */
public class HungerTowerPlacer
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation BASE_ID = new ResourceLocation(ArcaneLens.MODID, "hunger_tower_base");
    private static final ResourceLocation TOP_ID = new ResourceLocation(ArcaneLens.MODID, "hunger_tower_top");
    private static final int TOP_REPEAT_COUNT = 3;

    // Verified directly from hunger_tower_top.nbt: the topmost repeat's own local y=26 layer is a mostly
    // solid platform floor with a diamond-shaped opening around its exact center (6, 6) - the vertical
    // shaft/stairwell that runs through the whole tower's core, so the idol can't stand at dead-center. Local
    // (6, 27, 3) sits on solid floor, a few blocks off that shaft, standing at y=27 (one above the floor).
    private static final int IDOL_LOCAL_X = 6;
    private static final int IDOL_LOCAL_Y = 27;
    private static final int IDOL_LOCAL_Z = 3;

    public static void placeIfNeeded(ServerLevel level)
    {
        HungerTowerSavedData data = level.getDataStorage().computeIfAbsent(
                HungerTowerSavedData::load, HungerTowerSavedData::new, HungerTowerSavedData.dataName());
        if (data.isPlaced())
        {
            return;
        }

        place(level, data);
    }

    /** Debug-only: forces a fresh (re)placement regardless of whether the tower was already marked placed -
     * see the "hungertower place" command. Re-pasting the structure blocks on top of themselves is harmless,
     * just redundant; spawnIdol() guards against piling up duplicate idols if this is invoked repeatedly. */
    public static void forcePlace(ServerLevel level)
    {
        HungerTowerSavedData data = level.getDataStorage().computeIfAbsent(
                HungerTowerSavedData::load, HungerTowerSavedData::new, HungerTowerSavedData.dataName());
        place(level, data);
    }

    private static void place(ServerLevel level, HungerTowerSavedData data)
    {
        StructureTemplateManager structureManager = level.getStructureManager();
        StructureTemplate base = structureManager.getOrCreate(BASE_ID);
        StructureTemplate top = structureManager.getOrCreate(TOP_ID);

        // Force the origin chunk to actually be generated before reading its heightmap - if it isn't loaded
        // yet (the player triggering this can be anywhere in the world), the heightmap reads as empty and
        // returns the world's bottom instead of the real surface.
        level.getChunk(0, 0);
        BlockPos groundPos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(0, 0, 0));
        // Strip the structure block(s) left over from exporting these pieces via Structure Block - only the
        // structure block itself, not air, since ignoring air too would skip carving the interior spaces
        // (staircase, platform) and leave them filled with solid terrain instead.
        StructurePlaceSettings settings = new StructurePlaceSettings().addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        RandomSource random = RandomSource.create();

        base.placeInWorld(level, groundPos, groundPos, settings, random, 2);
        BlockPos nextPos = groundPos.above(base.getSize().getY());
        // The top piece's platform has lanterns sticking up into what would otherwise be the next repeat's
        // floor, so each repeat overlaps by one layer instead of stacking at the piece's full height -
        // without this you'd see an extra gap/layer per repeat from the lanterns' headroom.
        int topStep = top.getSize().getY() - 1;
        BlockPos lastTopOrigin = nextPos;
        for (int i = 0; i < TOP_REPEAT_COUNT; i++)
        {
            lastTopOrigin = nextPos;
            top.placeInWorld(level, nextPos, nextPos, settings, random, 2);
            nextPos = nextPos.above(topStep);
        }

        spawnIdol(level, lastTopOrigin);

        data.markPlaced();
        LOGGER.info("The Hunger Tower has risen at {}", groundPos);
    }

    /** Spawns the permanent Hunger Idol standing on the topmost repeat's platform - see the IDOL_LOCAL_*
     * comment above for why it isn't at dead-center. Pure atmosphere: no reward, just a repeatable flavor-text
     * interaction (see HungerIdolEntity). Discards any idol already standing there first, so forcePlace() (the
     * debug "hungertower place" command) can be run repeatedly without piling up duplicates. */
    private static void spawnIdol(ServerLevel level, BlockPos lastTopOrigin)
    {
        BlockPos idolPos = lastTopOrigin.offset(IDOL_LOCAL_X, IDOL_LOCAL_Y, IDOL_LOCAL_Z);
        AABB nearbyBox = new AABB(idolPos).inflate(2.0);
        level.getEntitiesOfClass(HungerIdolEntity.class, nearbyBox).forEach(Entity::discard);

        HungerIdolEntity idol = new HungerIdolEntity(ModEntityTypes.HUNGER_IDOL.get(), level);
        idol.moveTo(idolPos.getX() + 0.5, idolPos.getY(), idolPos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(idol);
    }
}
