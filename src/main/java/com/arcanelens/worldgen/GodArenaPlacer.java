package com.arcanelens.worldgen;

import com.arcanelens.ArcaneLens;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Pastes the God Challenge Hub's fixed layout once, the first time anyone reaches the dimension: the hub
 * itself at the origin every entry point already assumes (see GodHubMedallionItem's HUB_SPAWN_X/Y/Z,
 * (0.5, 9, 0.5)), plus each god's arena a fixed distance out from it. All hand-built with a Structure
 * Block, all 14x14 footprints; each arena sits clear of the hub's own footprint (and of each other) so
 * more can be added later without overlap - adding a god here is just a new ResourceLocation + BlockPos
 * pair plus a call in placeIfNeeded, nothing structural to redo.
 *
 * <p>Only war (Aurelia) and fertility (Florian) have real arena structures so far. Florian's arena is
 * pasted here too since the build exists, but GodRegistry.GODS still leaves his boss-content fields null -
 * nothing reads FERTILITY_ARENA_SPAWN yet until a Florian boss entity exists to go with it.</p>
 */
public class GodArenaPlacer
{
    private static final ResourceLocation HUB_ID = new ResourceLocation(ArcaneLens.MODID, "god_challenge_hub");
    private static final ResourceLocation WAR_ARENA_ID = new ResourceLocation(ArcaneLens.MODID, "god_arena_war");
    private static final ResourceLocation FERTILITY_ARENA_ID = new ResourceLocation(ArcaneLens.MODID, "god_arena_fertility");

    // Matches GodHubMedallionItem's HUB_SPAWN_X/Y/Z (0.5, 9, 0.5) - the hub structure's own floor-level
    // corner sits directly under that fixed arrival point. Y=9 is the flat dimension's walkable surface
    // (bedrock=0, black_concrete=1-8 per god_challenge_hub.json), same floor AURELIA_ARENA_ORIGIN's old
    // placeholder was already tuned against.
    // Pasted 2 below the concrete surface: the hand-built structures carry a couple of layers of ground
    // under their floor, so their walking surface lands at the same Y=9 the hub spawn already assumes.
    private static final BlockPos HUB_ORIGIN = new BlockPos(0, 7, 0);

    private static final int ARENA_SIZE = 14;
    // Both arenas are 14x14 like the hub; a 16-block gap keeps every footprint clear of the hub's own
    // without needing exact per-structure sizes - south for war, north for fertility, leaving east/west
    // open for whichever god gets a real arena next.
    private static final int GAP = 16;
    private static final BlockPos WAR_ARENA_ORIGIN = HUB_ORIGIN.offset(0, 0, ARENA_SIZE + GAP);
    private static final BlockPos FERTILITY_ARENA_ORIGIN = HUB_ORIGIN.offset(0, 0, -(ARENA_SIZE + GAP));

    // Roughly the center of each 14x14 arena at the same absolute height as before the 2-block drop (Y=10,
    // one above the Y=9 walking surface) - where the challenge altar teleports the player and where that
    // god's boss is summoned (see GodChallengeService, GodRegistry).
    public static final BlockPos WAR_ARENA_SPAWN = WAR_ARENA_ORIGIN.offset(7, 3, 7);
    public static final BlockPos FERTILITY_ARENA_SPAWN = FERTILITY_ARENA_ORIGIN.offset(7, 3, 7);

    /** Idempotent - a no-op every call after the first. Safe to call unconditionally on every dimension entry. */
    public static void placeIfNeeded(ServerLevel level)
    {
        if (!GodHubSavedData.get(level).tryClaim())
        {
            return;
        }

        StructureTemplateManager structureManager = level.getStructureManager();
        StructurePlaceSettings settings = new StructurePlaceSettings().addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        RandomSource random = RandomSource.create();

        place(level, structureManager, HUB_ID, HUB_ORIGIN, settings, random);
        place(level, structureManager, WAR_ARENA_ID, WAR_ARENA_ORIGIN, settings, random);
        place(level, structureManager, FERTILITY_ARENA_ID, FERTILITY_ARENA_ORIGIN, settings, random);
    }

    private static void place(ServerLevel level, StructureTemplateManager structureManager, ResourceLocation id,
                               BlockPos origin, StructurePlaceSettings settings, RandomSource random)
    {
        // Force every chunk the footprint touches to exist first - the player triggering this has only
        // just arrived, so chunks tens of blocks out (the arenas) aren't guaranteed loaded yet, same
        // reasoning as HungerTowerPlacer/VesselLairPlacer force-loading their own origin chunk.
        int minChunkX = origin.getX() >> 4;
        int maxChunkX = (origin.getX() + ARENA_SIZE - 1) >> 4;
        int minChunkZ = origin.getZ() >> 4;
        int maxChunkZ = (origin.getZ() + ARENA_SIZE - 1) >> 4;
        for (int cx = minChunkX; cx <= maxChunkX; cx++)
        {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++)
            {
                level.getChunk(cx, cz);
            }
        }

        StructureTemplate template = structureManager.getOrCreate(id);
        template.placeInWorld(level, origin, origin, settings, random, 2);
    }

    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer player && event.getTo() == ModDimensions.GOD_CHALLENGE_HUB_KEY)
        {
            placeIfNeeded(player.serverLevel());
        }
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer player && player.level().dimension() == ModDimensions.GOD_CHALLENGE_HUB_KEY)
        {
            placeIfNeeded(player.serverLevel());
        }
    }
}
