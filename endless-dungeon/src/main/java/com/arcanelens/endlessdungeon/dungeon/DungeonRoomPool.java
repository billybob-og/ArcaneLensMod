package com.arcanelens.endlessdungeon.dungeon;

import com.arcanelens.endlessdungeon.EndlessDungeonMod;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

/**
 * The base floor's candidate room pool - every grid-conforming room the frontier trigger is allowed to pick
 * from when extending an OPEN connector. Phase A's set: room (8x5x8, 1 connector), hallway (8x5x8, 2),
 * bighallway (8x5x16, a 2-cell section room, 4), turn (8x5x8, 2), tjunction (8x5x8, 3), abandonedcamp
 * (8x5x8, 1), watchtower (8x10x8, 1) - see the plan's Context section for why each must have an X/Z
 * footprint that's an exact multiple of DungeonGraphSavedData.GRID_UNIT (confirmed for all rooms via their
 * NBT size tags before being added here) - Y height doesn't need to follow the grid, hence watchtower's 10.
 *
 * <p>entrance is deliberately NOT in this pool - it's the one-time floor-0 seed room DungeonEntrancePlacer
 * places directly, never picked mid-maze by the frontier trigger.</p>
 */
public class DungeonRoomPool
{
    public static final List<ResourceLocation> BASE_FLOOR_ROOMS = List.of(
            new ResourceLocation(EndlessDungeonMod.MODID, "room"),
            new ResourceLocation(EndlessDungeonMod.MODID, "hallway"),
            new ResourceLocation(EndlessDungeonMod.MODID, "bighallway"),
            new ResourceLocation(EndlessDungeonMod.MODID, "turn"),
            new ResourceLocation(EndlessDungeonMod.MODID, "tjunction"),
            new ResourceLocation(EndlessDungeonMod.MODID, "abandonedcamp"),
            new ResourceLocation(EndlessDungeonMod.MODID, "watchtower"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv1drop"));

    /** bighallway and tjunction repeated extra times relative to the other room types - they're the only
     * rooms in the pool with more than 2 connectors, so they're the only ones that ever grow the maze's
     * open-connector frontier net-positive (the rest just replace themselves or shrink it - see
     * DungeonFrontierTrigger's own analysis of why the maze can run dry after a few dozen rooms).
     * DungeonFrontierTrigger already tries every distinct template at every connector regardless of
     * weighting - what this changes is which template WINS when more than one of them could validly fit the
     * same connector, since shuffling this list (with bighallway and tjunction repeated) makes them come
     * first in that tie far more often than an even chance would. bighallway gets more copies than tjunction
     * since it has more connectors (4 vs 3), proportionally more net growth per successful placement.
     *
     * <p>room and abandonedcamp (the only two loot-bearing templates - see their own chest LootTable NBT)
     * are ALSO given extra copies, not just left at one - a single-copy, single-connector room compounds
     * against ever winning a tie: bighallway/tjunction get 3-4 tries per candidate-try (one per connector),
     * while a 1-connector room only gets one, so their real win rate is far lower than the raw copy-count
     * weighting alone suggests (confirmed in playtesting: zero loot rooms across a "massive" fully-grown
     * dungeon). Bumped to 2 copies each - enough to make loot meaningfully findable without meaningfully
     * competing with bighallway/tjunction's own net-growth-critical weighting.</p>
     *
     * <p>lv1drop (the floor-descent room, 8x15x8 - tall to fit its extra upward DESCEND_MARKER_NAME-tagged
     * connector alongside its 4 normal ones) gets only a single copy, deliberately kept rare - per the plan's
     * own framing, descending a floor is meant to be a special, occasional find, not a routine structural
     * piece like bighallway/tjunction. Its normal 4 connectors compete like any other single-cell room; the
     * descend connector itself is invisible to this weighting entirely (DungeonFrontierTrigger routes it to
     * tryDescend, never the normal candidate search).</p> */
    public static final List<ResourceLocation> WEIGHTED_CANDIDATES = List.of(
            new ResourceLocation(EndlessDungeonMod.MODID, "room"),
            new ResourceLocation(EndlessDungeonMod.MODID, "room"),
            new ResourceLocation(EndlessDungeonMod.MODID, "hallway"),
            new ResourceLocation(EndlessDungeonMod.MODID, "bighallway"),
            new ResourceLocation(EndlessDungeonMod.MODID, "bighallway"),
            new ResourceLocation(EndlessDungeonMod.MODID, "bighallway"),
            new ResourceLocation(EndlessDungeonMod.MODID, "turn"),
            new ResourceLocation(EndlessDungeonMod.MODID, "tjunction"),
            new ResourceLocation(EndlessDungeonMod.MODID, "tjunction"),
            new ResourceLocation(EndlessDungeonMod.MODID, "abandonedcamp"),
            new ResourceLocation(EndlessDungeonMod.MODID, "abandonedcamp"),
            new ResourceLocation(EndlessDungeonMod.MODID, "watchtower"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv1drop"),
            // Moved here from the now-retired FLOOR_1_WEIGHTED_CANDIDATES - floor 1 ("lv2") is getting its
            // own purpose-built content instead, so these rejoin floor 0's general variety pool rather than
            // sitting in a slot about to be repurposed. voidroom keeps its old weight (a plain connective
            // piece, same role hallway plays) - fountain is deliberately cut down to a single copy, same
            // rare-find treatment as watchtower/lv1drop, rather than the 3 copies it had as one of only two
            // rooms in its own small floor-1 pool.
            new ResourceLocation(EndlessDungeonMod.MODID, "fountain"),
            new ResourceLocation(EndlessDungeonMod.MODID, "voidroom"),
            // lv1-exclusive content, despite the lack of an "lv2"-style prefix on its name - uses floor 0's
            // own plain (non-mossy) stone_bricks palette, not floor 1's mossy theme.
            new ResourceLocation(EndlessDungeonMod.MODID, "prison"),
            new ResourceLocation(EndlessDungeonMod.MODID, "prison"),
            // Same lv1-exclusive plain-stone_bricks pattern as prison - library and hidout are both single-
            // connector dead-end specials, same weighting boost.
            new ResourceLocation(EndlessDungeonMod.MODID, "library"),
            new ResourceLocation(EndlessDungeonMod.MODID, "library"),
            new ResourceLocation(EndlessDungeonMod.MODID, "hidout"),
            new ResourceLocation(EndlessDungeonMod.MODID, "hidout"),
            // Lore piece, not filler - floor 0 is the dungeon's newest floor, still visibly under
            // construction, and this room represents that (deliberately named, not a placeholder). Despite
            // having 4 connectors like bighallway/tjunction, kept to a single copy - a rare, distinctive find
            // rather than routine structural tissue.
            new ResourceLocation(EndlessDungeonMod.MODID, "unfinnishedroom"));

    /** Sized to exactly plug a doorway opening, not a grid cell - placed as a last resort (see
     * DungeonFrontierTrigger) when no real room from BASE_FLOOR_ROOMS fits a connector, so a dead end seals
     * shut instead of leaving a hole into the void forever. Never reserves a grid cell and is never itself a
     * candidate other rooms can attach to. */
    public static final ResourceLocation DOORWAY_BLOCKER = new ResourceLocation(EndlessDungeonMod.MODID, "wallblock");

    /** Fixes the root cause behind bighallway/tjunction's side connectors ending up sealed far more often
     * than expected: a room with connectors on BOTH axes needs the axis PERPENDICULAR to whichever connector
     * it attached through to also land on a multiple of DungeonGraphSavedData.GRID_UNIT - but that axis is
     * the room's own "depth" direction, which is deliberately left free to drift (that's what lets rooms sit
     * flush regardless of corridor length). Each hop's drift contribution is always the outgoing connector's
     * local depth offset (0, front face) minus the incoming candidate's own local depth offset (also 0 or
     * size-1) - i.e. always a multiple of GRID_UNIT, or +/-(GRID_UNIT-1) - so across a long chain of hops the
     * accumulated residue (mod GRID_UNIT) can land on ANY value 1 through 7, not just 0 or 7, and once it
     * does it stays there for the rest of that branch until something corrects it.
     *
     * <p>A corrector is a small 2-connector straight segment - front jigsaw at local depth 0, back jigsaw at
     * local depth L - used ONLY at the connector being expanded FROM, before any candidate is even
     * considered: if that connector's own perpendicular-axis coordinate isn't a multiple of GRID_UNIT
     * (residue R, 1-7), inserting the corrector for L = GRID_UNIT - R there shifts the NEXT connector
     * discovered from it back onto a multiple of GRID_UNIT, so whatever real room eventually attaches there
     * - bighallway, tjunction, or anything else - has both axes properly aligned. Keyed by L (the corrector's
     * own back-connector local depth, i.e. its physical length minus one), not by the residue it fixes, to
     * match how the structures are actually built/sized. Deliberately NOT grid-conforming (not a multiple of
     * GRID_UNIT) and NOT in BASE_FLOOR_ROOMS/WEIGHTED_CANDIDATES - like DOORWAY_BLOCKER, it's placed directly
     * by DungeonFrontierTrigger, bypassing RoomAligner's grid-cell machinery entirely (reserves no cell), but
     * unlike the blocker its own back connector stays OPEN for normal future expansion. */
    public static final Map<Integer, ResourceLocation> RESIDUE_CORRECTORS = Map.of(
            1, new ResourceLocation(EndlessDungeonMod.MODID, "corrector_1"),
            2, new ResourceLocation(EndlessDungeonMod.MODID, "corrector_2"),
            3, new ResourceLocation(EndlessDungeonMod.MODID, "corrector_3"),
            4, new ResourceLocation(EndlessDungeonMod.MODID, "corrector_4"),
            5, new ResourceLocation(EndlessDungeonMod.MODID, "corrector_5"),
            6, new ResourceLocation(EndlessDungeonMod.MODID, "corrector_6"),
            7, new ResourceLocation(EndlessDungeonMod.MODID, "corrector_7"));

    /** Floor 1's ("lv2") own natively-mossy corrector set, replacing the auto-recolored plain correctors
     * there (see DungeonFrontierTrigger's own MOSSY_ELIGIBLE_TEMPLATES, which no longer needs to cover
     * correctors once a floor has its own real set here) - same length convention as RESIDUE_CORRECTORS
     * (keyed by the back connector's own local depth), just hand-built rather than reskinned. lv2corrector
     * (unsuffixed) is length 1, matching corrector_1's role. */
    public static final Map<Integer, ResourceLocation> FLOOR_1_RESIDUE_CORRECTORS = Map.of(
            1, new ResourceLocation(EndlessDungeonMod.MODID, "lv2corrector"),
            2, new ResourceLocation(EndlessDungeonMod.MODID, "lv2corrector2"),
            3, new ResourceLocation(EndlessDungeonMod.MODID, "lv2corrector3"),
            4, new ResourceLocation(EndlessDungeonMod.MODID, "lv2corrector4"),
            5, new ResourceLocation(EndlessDungeonMod.MODID, "lv2corrector5"),
            6, new ResourceLocation(EndlessDungeonMod.MODID, "lv2corrector6"),
            7, new ResourceLocation(EndlessDungeonMod.MODID, "lv2corrector7"));

    /** Floor 2's ("lv3"/"the garden") own natively-mossy corrector set - same shape and length convention
     * as FLOOR_1_RESIDUE_CORRECTORS, just its own hand-built set rather than reusing floor 1's or falling
     * back to the auto-recolored plain ones. */
    public static final Map<Integer, ResourceLocation> FLOOR_2_RESIDUE_CORRECTORS = Map.of(
            1, new ResourceLocation(EndlessDungeonMod.MODID, "lv3corrector"),
            2, new ResourceLocation(EndlessDungeonMod.MODID, "lv3corrector2"),
            3, new ResourceLocation(EndlessDungeonMod.MODID, "lv3corrector3"),
            4, new ResourceLocation(EndlessDungeonMod.MODID, "lv3corrector4"),
            5, new ResourceLocation(EndlessDungeonMod.MODID, "lv3corrector5"),
            6, new ResourceLocation(EndlessDungeonMod.MODID, "lv3corrector6"),
            7, new ResourceLocation(EndlessDungeonMod.MODID, "lv3corrector7"));

    /** Keyed by floorIndex, looked up by DungeonFrontierTrigger.placeCorrector instead of always using
     * RESIDUE_CORRECTORS directly - unlike FLOOR_CANDIDATE_POOLS, an unmapped floor falls back to
     * RESIDUE_CORRECTORS (the plain set) rather than an empty map, since correctors are floor-agnostic
     * utility infrastructure, not floor-exclusive room content (see MOSSY_ELIGIBLE_TEMPLATES in
     * DungeonFrontierTrigger for the auto-recolor that covers a floor with no dedicated set of its own). */
    public static final Map<Integer, Map<Integer, ResourceLocation>> RESIDUE_CORRECTORS_BY_FLOOR = Map.of(
            1, FLOOR_1_RESIDUE_CORRECTORS,
            2, FLOOR_2_RESIDUE_CORRECTORS);

    /** Floor 1's ("lv2") own candidate pool - entirely separate content from WEIGHTED_CANDIDATES (floor 0's),
     * every room here is lv1drop-reachable-only, never reused on floor 0. lv2bighallway (6 connectors - even
     * more than floor 0's own bighallway) and lv2tjunction (3) are the net-positive growth pieces, weighted
     * up the same way bighallway/tjunction are on floor 0. lv2lootroom and lv2spawner are both single-
     * connector, so both get the same "compounds against ever winning a tie" weighting boost room/
     * abandonedcamp get on floor 0 (see WEIGHTED_CANDIDATES' own javadoc). lv2spire (1 connector, 10 tall)
     * is the rare special piece, matching watchtower's single-copy treatment. lv2turn, lv2hallway,
     * lv2floodedhallway, and lv2bridge (all 2 connectors) are the plain connective pieces, same role hallway/
     * turn play on floor 0 - the pool was thin on these before, so they get more relative weight than the
     * single copies turn/hallway have. lv2collapsedtunnel and lv2floodedtunnel (both 2 connectors) are more
     * connective pieces, same weighting. lv2storageroom and lv2fightroom (both 1 connector) are more dead-end
     * specials, same weighting boost as lv2lootroom/lv2spawner. */
    public static final List<ResourceLocation> FLOOR_1_WEIGHTED_CANDIDATES = List.of(
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2bighallway"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2bighallway"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2bighallway"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2tjunction"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2tjunction"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2turn"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2hallway"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2floodedhallway"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2bridge"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2collapsedtunnel"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2floodedtunnel"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2lootroom"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2lootroom"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2spawner"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2spawner"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2storageroom"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2storageroom"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2fightroom"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2fightroom"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2spire"),
            // lv2drop (floor 1's own descend room, 8x15x8) - single copy, same deliberately-rare treatment
            // as lv1drop. Unlike lv1drop it has 4 descend-marked connectors clustered at the bottom feeding
            // floor 2 from one hub instead of 1, and only a single normal connector (top) attaching it into
            // floor 1's own maze - each descend connector is handled independently by the frontier scanner
            // (DungeonFrontierTrigger routes per-connector on ConnectorState.isDescend, nothing assumes a
            // room has only one), so this is a supported shape, not a special case.
            new ResourceLocation(EndlessDungeonMod.MODID, "lv2drop"));

    /** Floor 2's ("lv3"/"the garden") own candidate pool. lv3lturn (16x16, a 2-cell section room, 6
     * connectors) is the net-positive growth piece, weighted up the same way lv2bighallway is on floor 1.
     * lv3_4way (8x8, 4 connectors) is also net-positive, just less so - one copy fewer than lv3lturn.
     * lv3hallway and lv3turn (both 2 connectors) are the plain connective pieces, same role hallway/turn play
     * on floor 0 and lv2hallway/lv2turn play on floor 1 - one copy each, same precedent. lv3logbridge (2
     * connectors) is another connective piece, same role lv2bridge plays - one copy. lv3waterroom,
     * lv3overgrownroom, lv3abandondcamp, lv3sunkenpool, and lv3spiderden (all single-connector dead-end
     * specials) get the same "compounds against ever winning a tie" weighting boost as
     * lv2lootroom/lv2storageroom/lv2fightroom on floor 1 - a 1-connector room only gets one try per candidate
     * search versus lv3lturn/lv3_4way's 4-6, so a single copy would barely ever win. lv3tower (1 connector,
     * 10 tall) and lv3floweringshrine (1 connector) are the rare special/lore pieces, matching
     * watchtower/fountain/lv2spire's deliberate single-copy treatment. */
    public static final List<ResourceLocation> FLOOR_2_WEIGHTED_CANDIDATES = List.of(
            new ResourceLocation(EndlessDungeonMod.MODID, "lv3lturn"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv3lturn"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv3lturn"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv3_4way"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv3_4way"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv3hallway"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv3turn"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv3logbridge"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv3waterroom"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv3waterroom"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv3overgrownroom"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv3overgrownroom"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv3abandondcamp"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv3abandondcamp"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv3sunkenpool"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv3sunkenpool"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv3spiderden"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv3spiderden"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv3tower"),
            new ResourceLocation(EndlessDungeonMod.MODID, "lv3floweringshrine"));

    /** Keyed by floorIndex, looked up by DungeonFrontierTrigger.tryExpand (and tryDescend, for the room that
     * seeds a new floor) instead of always using WEIGHTED_CANDIDATES directly - a floor with no explicit
     * entry here gets an EMPTY candidate list, not a fallback to WEIGHTED_CANDIDATES, since every room in
     * that pool is lv1-exclusive content (see WEIGHTED_CANDIDATES' own javadoc) - an unmapped floor should
     * seal every connector with the doorway blocker until it gets a real pool of its own, not silently reuse
     * floor 0's rooms. */
    public static final Map<Integer, List<ResourceLocation>> FLOOR_CANDIDATE_POOLS = Map.of(
            0, WEIGHTED_CANDIDATES,
            1, FLOOR_1_WEIGHTED_CANDIDATES,
            2, FLOOR_2_WEIGHTED_CANDIDATES);
}
