package com.arcanelens.worldgen;

import com.arcanelens.ArcaneLens;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Generates Broken Vessel's lair once, the first time anyone ever reaches the Warped Hollow - a single
 * fixed-position jigsaw tower shared by the whole dimension (see VesselLairSavedData), not a per-player
 * thing like PocketDimensionPlacer's per-player cells, and not registered as a normal scattered Structure
 * either (there must only ever be one, at this one known spot) - JigsawPlacement.generateJigsaw is called
 * directly instead, the same lower-level entry point vanilla's own jigsaw block "Generate" button and
 * /place jigsaw use, bypassing the whole biome-tag/StructureSet placement pipeline entirely.
 *
 * <p>Entrance -> Room 1/Room 2/Boss Room pool (hand-built with a structure block, see
 * ModTemplatePools.VESSEL_LAIR_ROOMS) -> the boss room has no outgoing jigsaw of its own, so whichever hop
 * picks it ends the tower there. The boss room's own Command Trigger block (built into that NBT piece)
 * handles summoning Broken Vessel the first time a player reaches it - not code here, matching how the
 * castle dungeon's own boss arena already works.</p>
 */
public class VesselLairPlacer
{
    // Fixed dimension-origin offset - deliberately away from (0,0) so it doesn't overlap the arrival point
    // a ritual-opened portal will later land players at (see OverloadPortalSavedData).
    // Y=49 (not 50) - a structure template's local (0,0,0) is its floor level, and the flat world's top
    // solid layer sits at y=49 (bedrock=0, blackstone=1-48, warped_nylium=49) - placing the floor at y=50
    // (the first walkable air block) would leave it floating one block above the ground, same off-by-one
    // this constant was originally tuned against when the world was only 8 blocks deep (bedrock=0,
    // blackstone=1-6, nylium=7) - deepened for the buried Vessel Hollow exploration content and the
    // Sleeping God Relic Shrine (see RelicShrinePlacer), which both need real vertical volume to sit in.
    private static final BlockPos LAIR_ORIGIN = new BlockPos(300, 49, 300);

    // Generous safety backstop, not the expected typical height - ModTemplatePools.VESSEL_LAIR_ROOMS
    // weights the boss room heavily enough that most runs end within 1-3 hops. This just guards against
    // the unlikely tail case of a long unlucky streak of Room 1/Room 2 picks, since the tower only ever
    // generates once per world - a run that dead-ends with no boss room at the top has no in-game recovery.
    private static final int MAX_DEPTH = 10;

    // The jigsaw Name/Target baked into every piece (see ModTemplatePools.VESSEL_LAIR_ENTRANCE/_ROOMS) -
    // tells generateJigsaw which of the entrance piece's own jigsaws to start expanding from.
    private static final ResourceLocation JIGSAW_TARGET = new ResourceLocation(ArcaneLens.MODID, "lair");

    // JigsawPlacement.generateJigsaw's own pos parameter is NOT the entrance piece's local origin - it's
    // the exact world position the *named starting jigsaw itself* ends up at (confirmed by decompiling
    // JigsawPlacement.addPieces: with no Heightmap.Types override, the piece is placed so its found jigsaw
    // block lands exactly on the given pos, and the piece's own floor is computed backward from there).
    // vessel_lair_entrance.nbt's own jigsaw sits at local (7, 6, 7) within its 15x7x15 footprint (confirmed
    // via direct NBT inspection). +1 on Y beyond that raw offset: confirmed in testing - the whole tower
    // (every piece stacks relative to the entrance, so this one constant governs all of them) landed
    // exactly 1 block too low using the raw jigsaw-local Y alone.
    private static final BlockPos ENTRANCE_JIGSAW_LOCAL_POS = new BlockPos(7, 7, 7);

    // Generous fixed padding around the origin, not a measured footprint - unlike the old single-template
    // placeholder, the tower's final height/rotation varies by which rooms got picked (jigsaw joint type on
    // every piece is "rollable", so each hop can rotate 90 degrees around the vertical connecting axis, but
    // still stacks straight up). Wide/tall enough to comfortably contain any realistic run at MAX_DEPTH=10
    // (each room is 15x7x15, the boss room 15x10x15) with room to spare - only used for the Summoning
    // Charm's "must stand inside the lair" gate, so erring generous over exact is the safe direction.
    private static final int HORIZONTAL_PAD = 20;
    private static final int VERTICAL_PAD = 110;

    public static AABB getLairBoundingBox()
    {
        return new AABB(
                LAIR_ORIGIN.getX() - HORIZONTAL_PAD, LAIR_ORIGIN.getY() - 8, LAIR_ORIGIN.getZ() - HORIZONTAL_PAD,
                LAIR_ORIGIN.getX() + HORIZONTAL_PAD, LAIR_ORIGIN.getY() + VERTICAL_PAD, LAIR_ORIGIN.getZ() + HORIZONTAL_PAD);
    }

    // Well clear of the tower's own 15x15 footprint regardless of which way it ends up rotated (rollable
    // joints), sitting right on the flat world's walkable surface (Y=50, one above the solid warped_nylium
    // top layer at Y=49). A Lodestone placed here is what a Lodestone Compass (handed to the player on
    // ritual completion, see OverloadCoreBlockEntity) tracks - otherwise there's no in-game way to find this
    // one fixed tower from wherever the ritual's arrival cell happens to land a given player.
    public static final BlockPos LODESTONE_POS = new BlockPos(LAIR_ORIGIN.getX() - 10, 50, LAIR_ORIGIN.getZ() - 10);

    /** Idempotent - a no-op every call after the first. Safe to call unconditionally on every dimension entry. */
    public static void ensureLairPlaced(ServerLevel level)
    {
        if (!VesselLairSavedData.get(level).tryClaim())
        {
            return;
        }

        // Force the origin chunk to actually exist before generating into it - same idiom
        // PocketDimensionPlacer.ensureRoomPlaced uses.
        level.getChunk(LAIR_ORIGIN.getX() >> 4, LAIR_ORIGIN.getZ() >> 4);

        Holder<StructureTemplatePool> entrancePool = level.registryAccess()
                .lookupOrThrow(Registries.TEMPLATE_POOL)
                .getOrThrow(ModTemplatePools.VESSEL_LAIR_ENTRANCE);

        BlockPos jigsawTargetPos = LAIR_ORIGIN.offset(ENTRANCE_JIGSAW_LOCAL_POS);

        // Small pieces (15x7x15, the boss room 15x10x15) at a shallow depth - unlike the castle dungeon's
        // boss arena, this is well within what a single synchronous call can place without a lag spike, so
        // no IncrementalStructurePlacer staging is needed here.
        JigsawPlacement.generateJigsaw(level, entrancePool, JIGSAW_TARGET, MAX_DEPTH, jigsawTargetPos, false);

        level.setBlockAndUpdate(LODESTONE_POS, Blocks.LODESTONE.defaultBlockState());
    }

    /** Safety-net trigger: guarantees the lair exists the moment anyone first reaches the dimension, by
     * whatever means (a ritual portal today, or anything else later) - also doubles as the step-3 test
     * trigger before the real Overload Ritual exists (just teleport in). */
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer player && event.getTo() == ModDimensions.BROKEN_VESSEL_KEY)
        {
            ensureLairPlaced(player.serverLevel());
        }
    }

    /** Second safety-net trigger, for the gap onPlayerChangedDimension can't cover on its own: a player who
     * logs back in already positioned inside the Warped Hollow (server restart, reconnect after a
     * disconnect, or - as happened while testing this - simply quitting while standing in the dimension and
     * relaunching, since Minecraft respawns at the exact last logout position/dimension) never actually
     * crosses into the dimension, so no PlayerChangedDimensionEvent ever fires for them. */
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer player && player.level().dimension() == ModDimensions.BROKEN_VESSEL_KEY)
        {
            ensureLairPlaced(player.serverLevel());
        }
    }
}
