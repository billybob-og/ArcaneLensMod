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
 * Places the Sleeping God Relic Shrine once, the first time anyone ever reaches the Warped Hollow - a
 * single fixed-position landmark shared by the whole dimension (see RelicShrineSavedData), structurally
 * parallel to VesselLairPlacer but without any jigsaw machinery: this is one guaranteed, hand-built piece
 * with nothing to randomize, so a direct StructureTemplate.placeInWorld call (same pattern
 * PocketDimensionPlacer already uses for its own single-piece rooms) is all that's needed.
 *
 * <p>The player learns this fixed position through the Hunger Idol (see HungerIdolEntity), not a compass -
 * SHRINE_ORIGIN is exactly what that guide book hint page states.</p>
 */
public class RelicShrinePlacer
{
    private static final ResourceLocation SHRINE_ID = new ResourceLocation(ArcaneLens.MODID, "vessel_hollow_sleeping_god_relic");

    // Clearly outside the Lair Tower's own padded bounding box (VesselLairPlacer.getLairBoundingBox is
    // LAIR_ORIGIN +/-20 horizontally) - offset along Z instead of X so the two landmarks read as distinct
    // places, not stacked on the same axis.
    //
    // placeInWorld's pos param is where the template's LOCAL (0,0,0) - the bottom of its saved bounding
    // box - lands, not its top. The piece is built as a partially-buried cave (10x7x10, confirmed via the
    // exported NBT's size tag) with its entrance meant to sit at ground level and the rest dug down into
    // the blackstone below. Placing local Y=0 at Y=49 (like VesselLairPlacer's LAIR_ORIGIN, whose entrance
    // piece is NOT buried) put the whole piece floor-first at the surface instead - the entrance ended up
    // buried, not flush with it. Y=43 = 49 - (sizeY - 1) = 49 - 6 instead puts the piece's TOP at Y=49 (the
    // Warped Hollow's top solid layer), so the entrance sits at the surface and everything below it digs
    // into the deepened blackstone as intended.
    public static final BlockPos SHRINE_ORIGIN = new BlockPos(300, 43, -200);

    /** Idempotent - a no-op every call after the first. Safe to call unconditionally on every dimension entry. */
    public static void ensureShrinePlaced(ServerLevel level)
    {
        if (!RelicShrineSavedData.get(level).tryClaim())
        {
            return;
        }

        StructureTemplateManager manager = level.getStructureManager();
        StructureTemplate template = manager.getOrCreate(SHRINE_ID);

        // Force the origin chunk to actually exist before pasting into it - same idiom
        // PocketDimensionPlacer/VesselLairPlacer both already use.
        level.getChunk(SHRINE_ORIGIN.getX() >> 4, SHRINE_ORIGIN.getZ() >> 4);

        StructurePlaceSettings settings = new StructurePlaceSettings().addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        template.placeInWorld(level, SHRINE_ORIGIN, SHRINE_ORIGIN, settings, RandomSource.create(), 2);
    }

    /** Safety-net trigger: guarantees the shrine exists the moment anyone first reaches the dimension,
     * mirroring VesselLairPlacer's own two-hook approach exactly (see that class for why both are needed -
     * ordinary dimension entry, and same-dimension relogin). */
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer player && event.getTo() == ModDimensions.BROKEN_VESSEL_KEY)
        {
            ensureShrinePlaced(player.serverLevel());
        }
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer player && player.level().dimension() == ModDimensions.BROKEN_VESSEL_KEY)
        {
            ensureShrinePlaced(player.serverLevel());
        }
    }
}
