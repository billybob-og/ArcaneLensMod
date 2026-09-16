package com.arcanelens.endlessdungeon.dungeon;

import com.arcanelens.endlessdungeon.EndlessDungeonMod;
import com.arcanelens.endlessdungeon.worldgen.ModDimensions;
import com.arcanelens.worldgen.IncrementalStructurePlacer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Places dungeon entrance rooms - the seed rooms DungeonFrontierTrigger needs to have anything to grow from.
 * Two ways an entrance gets placed:
 *
 * <p><b>The original fixed entrance</b> at (0,64,0), placed once per world via {@link
 * #ensureAtLeastOneEntranceExists} - a safety net guaranteeing floor 0 always has a seed even for a fresh
 * world reached via debug teleport rather than any portal, mirroring RelicShrinePlacer's own two-hook
 * approach (ordinary dimension entry, and same-dimension relogin). Still gated on {@code floorExists(0)},
 * exactly as before Phase C - not on "the entrance pool is empty," since a pre-Phase-C save can have
 * floorExists(0)==true with no portal markers in its (not yet re-exported) entrance.nbt, and re-triggering
 * placement every single relevant event on such a save while its landing pool stays permanently empty would
 * otherwise re-enqueue the same placement job forever (see the plan's "old-save migration" risk note - this
 * is deliberately accepted as a gap, not silently made worse by a thrashing loop).</p>
 *
 * <p><b>Portal-spawned entrances</b>, via {@link #ensureEntranceSpawnedFor} - called from DungeonPortalBlock
 * the first time a specific overworld portal instance is used, placing a brand-new entrance at a fresh
 * grid-aligned slot far outside the organically-grown maze's reach.</p>
 *
 * <p>Both paths funnel through {@link #placeEntranceRoom}, which filters portal-marker jigsaws (see {@link
 * #PORTAL_MARKER_NAME}) out of the room's registered connectors and locates the return portal's own landing
 * position from them - the entrance template is otherwise placed exactly like every other room (see
 * DungeonFrontierTrigger.commit's own force-load/placeRoom/enqueue sequence, mirrored here).</p>
 */
public class DungeonEntrancePlacer
{
    // Package-visible (not private) - DungeonFrontierTrigger.tryDescend reuses this same template as the
    // generic per-floor seed room when a descend connector spawns a new floor.
    static final ResourceLocation ENTRANCE_ID = new ResourceLocation(EndlessDungeonMod.MODID, "entrance");
    private static final BlockPos ENTRANCE_ORIGIN = new BlockPos(0, 64, 0);
    private static final int FLOOR_0_INDEX = 0;

    /** Jigsaw `name` value marking a portal-return marker rather than a real room-to-room connector - see
     * the plan's Phase C. The entrance template needs two (upper=false/true, matching DungeonPortalBlock's
     * 2-tall convention), each with `final_state` set to endless_dungeon:dungeon_portal in the structure
     * block UI - the same final_state-conversion mechanism DungeonFrontierTrigger.buildFinalStateCallback
     * already applies to every other room's jigsaws, just aimed at a new block instead of a vanilla one.
     * Matches the `name` value already baked into the hand-built portalframe.nbt piece (verified via direct
     * NBT inspection) - named after the block it becomes, not an arbitrary marker string. */
    public static final ResourceLocation PORTAL_MARKER_NAME = new ResourceLocation(EndlessDungeonMod.MODID, "dungeon_portal");

    // A straight lane far outside the organically-grown maze's reach (which grows by adjacency from (0,0)) -
    // spaced ENTRANCE_SLOT_SPACING_CELLS cells apart so each entrance's own footprint plus a few hops of
    // connector growth comfortably fits without touching its neighbor. See the plan's Phase C for why this
    // needs no real collision search (anyCellOccupiedByOtherRoom is still checked defensively below). 6
    // cells * GRID_UNIT(8) = 48 blocks = 3 chunks - every slot on the shared lane is spaced this far from
    // its neighbors regardless of which portal claimed it, so any two entrances (from the same portal's own
    // batch of ENTRANCES_PER_PORTAL, or from two different portals entirely) always end up >= 3 chunks apart.
    private static final int ENTRANCE_LANE_START_CELL = 10000;
    private static final int ENTRANCE_SLOT_SPACING_CELLS = 6;

    // How many entrances a single overworld portal's first-ever use spawns at once, each drawn from its own
    // fresh lane slot (see ENTRANCE_SLOT_SPACING_CELLS) - more seed points feeding the same shared floor-0
    // maze from one portal, rather than just one.
    private static final int ENTRANCES_PER_PORTAL = 3;

    // Not persisted - a portal mid-spawn only matters within the handful of ticks IncrementalStructurePlacer
    // takes to finish its job, so an empty map on server restart is always correct (nothing was "in flight"
    // across a restart in any meaningful sense).
    private static final Map<GlobalPos, List<Consumer<BlockPos>>> pendingSpawns = new HashMap<>();

    public static void ensureAtLeastOneEntranceExists(ServerLevel level)
    {
        DungeonGraphSavedData graph = DungeonGraphSavedData.get(level);
        if (graph.floorExists(FLOOR_0_INDEX))
        {
            return;
        }

        placeEntranceRoom(level, graph, FLOOR_0_INDEX, ENTRANCE_ORIGIN, Rotation.NONE, landingPos ->
        {
            if (landingPos != null)
            {
                graph.registerEntranceLanding(landingPos);
            }
        });
    }

    /** Like {@link #ensureEntranceSpawnedFor}, but for a caller with no specific portal instance to pair
     * against (e.g. a standalone teleporter item) - just resolves `onReady` with any existing landing from
     * the pool, picked at random, or places the original fixed entrance first if the pool is still entirely
     * empty (a cold-start world nobody has ever entered the dungeon in yet). */
    public static void ensureAnyEntranceExists(ServerLevel dungeonLevel, Consumer<BlockPos> onReady)
    {
        DungeonGraphSavedData graph = DungeonGraphSavedData.get(dungeonLevel);
        BlockPos landing = graph.pickRandomEntranceLanding(dungeonLevel.getRandom());
        if (landing != null)
        {
            onReady.accept(landing);
            return;
        }

        placeEntranceRoom(dungeonLevel, graph, FLOOR_0_INDEX, ENTRANCE_ORIGIN, Rotation.NONE, landingPos ->
        {
            if (landingPos != null)
            {
                graph.registerEntranceLanding(landingPos);
                onReady.accept(landingPos);
            }
        });
    }

    /** Ensures `portalPos` (a specific overworld portal instance's own world position) has a paired dungeon
     * entrance somewhere in the pool, then resolves `onReady` with a landing BlockPos - synchronously (no
     * race between two players hitting the same portal in the same server tick) if this portal already
     * spawned one, or once a freshly-placed one finishes pasting. Resolves nothing (silently) if the
     * template has no portal markers yet - see PORTAL_MARKER_NAME's own javadoc; this is a content
     * prerequisite, not a code bug. */
    public static void ensureEntranceSpawnedFor(ServerLevel dungeonLevel, GlobalPos portalPos, Consumer<BlockPos> onReady)
    {
        DungeonGraphSavedData graph = DungeonGraphSavedData.get(dungeonLevel);
        if (graph.hasSpawnedEntranceFor(portalPos))
        {
            BlockPos landing = graph.pickRandomEntranceLanding(dungeonLevel.getRandom());
            if (landing != null)
            {
                onReady.accept(landing);
            }
            return;
        }

        List<Consumer<BlockPos>> waiters = pendingSpawns.get(portalPos);
        if (waiters != null)
        {
            // Already spawning from an earlier trigger this tick - piggyback on its own completion rather
            // than double-placing a second batch of entrances for the same portal.
            waiters.add(onReady);
            return;
        }
        pendingSpawns.put(portalPos, new ArrayList<>(List.of(onReady)));

        // Claimed immediately (not inside the first entrance's own onComplete, as before) - a second
        // player hitting the same portal in the same tick, before any of this batch's placement jobs have
        // actually finished, must see hasSpawnedEntranceFor already true and fall into the pendingSpawns
        // piggyback branch above, not start a SECOND batch of ENTRANCES_PER_PORTAL entrances for one portal.
        graph.markSpawnedFor(portalPos);

        // Places all ENTRANCES_PER_PORTAL entrances up front rather than one-at-a-time - each is independent
        // (its own lane slot, its own placement job), so there's no reason to serialize them. Only the FIRST
        // one's landing position resolves the waiting player(s); the other two just register their own
        // landing into the shared pool once they finish, same as any entrance placed after this portal's
        // first-ever trigger would.
        for (int i = 0; i < ENTRANCES_PER_PORTAL; i++)
        {
            boolean isFirst = i == 0;
            BlockPos origin = claimEntranceOrigin(graph, dungeonLevel);
            placeEntranceRoom(dungeonLevel, graph, FLOOR_0_INDEX, origin, Rotation.NONE, landingPos ->
            {
                if (landingPos != null)
                {
                    graph.registerEntranceLanding(landingPos);
                }
                if (isFirst)
                {
                    List<Consumer<BlockPos>> resolved = pendingSpawns.remove(portalPos);
                    if (resolved != null && landingPos != null)
                    {
                        for (Consumer<BlockPos> waiter : resolved)
                        {
                            waiter.accept(landingPos);
                        }
                    }
                }
            });
        }
    }

    /** Claims the next grid-aligned slot on the entrance lane. A collision here would mean the lane itself
     * (80,000+ blocks out) somehow got reached by the organic maze or a prior slot's own growth - vanishingly
     * unlikely, but anyCellOccupiedByOtherRoom is still checked and the next slot claimed instead of ever
     * silently overlapping. */
    private static BlockPos claimEntranceOrigin(DungeonGraphSavedData graph, ServerLevel level)
    {
        while (true)
        {
            int slot = graph.claimNextEntranceSlot();
            BlockPos origin = new BlockPos((ENTRANCE_LANE_START_CELL + slot * ENTRANCE_SLOT_SPACING_CELLS) * DungeonGraphSavedData.GRID_UNIT, 64, 0);
            StructureTemplate template = level.getStructureManager().getOrCreate(ENTRANCE_ID);
            Set<Long> footprintCells = RoomAligner.computeFootprintCells(template, origin, Rotation.NONE);
            if (!graph.anyCellOccupiedByOtherRoom(FLOOR_0_INDEX, footprintCells, -1))
            {
                return origin;
            }
        }
    }

    /** Places one entrance room at `origin` on `floorIndex`, filters portal-marker jigsaws out of the room's
     * registered connectors, and once placement fully finishes, calls `onLanded` with the lowest portal
     * marker's world position (or null if this template has none yet). Package-visible (not private) so
     * DungeonFrontierTrigger's descend handling can reuse it directly for seeding a new floor, with a
     * non-identity `rotation` (computed to match whatever direction the descend connector required) instead
     * of the fixed Rotation.NONE the portal-driven callers below always use. */
    static void placeEntranceRoom(ServerLevel level, DungeonGraphSavedData graph, int floorIndex, BlockPos origin,
                                   Rotation rotation, Consumer<BlockPos> onLanded)
    {
        StructureTemplateManager manager = level.getStructureManager();
        StructureTemplate template = manager.getOrCreate(ENTRANCE_ID);

        Set<Long> footprintCells = RoomAligner.computeFootprintCells(template, origin, rotation);
        DungeonFrontierTrigger.forceLoadFootprint(level, footprintCells);

        ConnectorScanner.TemplateConnectors scanned = ConnectorScanner.scan(level, ENTRANCE_ID);
        List<DungeonGraphSavedData.WorldConnector> worldConnectors = new ArrayList<>(scanned.connectors().size());
        BlockPos landingPos = null;
        int landingWorldY = Integer.MAX_VALUE;
        for (ConnectorScanner.ConnectorInfo info : scanned.connectors())
        {
            BlockPos rotatedLocalPos = StructureTemplate.transform(info.localPos(), Mirror.NONE, rotation, BlockPos.ZERO);
            BlockPos worldPos = origin.offset(rotatedLocalPos);
            if (info.name().equals(PORTAL_MARKER_NAME))
            {
                // The lower of the two markers (upper=false) is the one worth teleporting a player onto -
                // no blockstate distinction is available here (these are still raw jigsaw blocks at scan
                // time), so the lower world Y unambiguously picks the same tile regardless of rotation.
                if (worldPos.getY() < landingWorldY)
                {
                    landingWorldY = worldPos.getY();
                    landingPos = worldPos;
                }
                continue;
            }
            Direction worldFront = rotation.rotate(info.front());
            Direction worldTop = rotation.rotate(info.top());
            boolean isDescend = info.name().equals(DungeonGraphSavedData.DESCEND_MARKER_NAME);
            worldConnectors.add(new DungeonGraphSavedData.WorldConnector(worldPos, worldFront, worldTop, info.joint(), isDescend));
        }

        graph.placeRoom(floorIndex, origin.getY(), ENTRANCE_ID, origin, rotation, footprintCells, worldConnectors);

        BlockPos finalLandingPos = landingPos;
        StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation);
        Runnable applyFinalStates = DungeonFrontierTrigger.buildFinalStateCallback(level, ENTRANCE_ID, origin, rotation);
        Runnable onComplete = () ->
        {
            applyFinalStates.run();
            onLanded.accept(finalLandingPos);
        };
        IncrementalStructurePlacer.enqueue(level, template, origin, settings, RandomSource.create(), onComplete);
    }

    /** Safety-net trigger: guarantees the entrance exists the moment anyone first reaches the dimension,
     * mirroring RelicShrinePlacer/VesselLairPlacer's own two-hook approach exactly (ordinary dimension entry,
     * and same-dimension relogin). */
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer player && event.getTo() == ModDimensions.DUNGEON_KEY)
        {
            ensureAtLeastOneEntranceExists(player.serverLevel());
        }
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer player && player.level().dimension() == ModDimensions.DUNGEON_KEY)
        {
            ensureAtLeastOneEntranceExists(player.serverLevel());
        }
    }
}
