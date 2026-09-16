package com.arcanelens.endlessdungeon.dungeon;

import com.arcanelens.endlessdungeon.EndlessDungeonMod;
import com.arcanelens.endlessdungeon.worldgen.ModDimensions;
import com.arcanelens.worldgen.IncrementalStructurePlacer;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.TickEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Periodically checks players in the dungeon dimension against each floor's open-connector frontier; on
 * proximity, claims the connector, picks a room from the candidate pool that can attach to it (see
 * RoomAligner), and hands off to IncrementalStructurePlacer. Checked once a second, not every tick - matches
 * GodDropVoidRescueHandler's own cadence, for the same reason (nothing here needs sub-second responsiveness).
 *
 * <p>The connector is flipped to CLAIMED_PENDING synchronously, in the same tick the trigger fires, before
 * any candidate search or placement work starts - this is the whole reason ConnectorStatus is tri-state (see
 * DungeonGraphSavedData) rather than a boolean: two players approaching the same connector in the same
 * scan can't both pass the OPEN check and then both place a room there, since the first one to claim it
 * flips it out of OPEN before the second is ever evaluated (both run in the same single-threaded level
 * tick).</p>
 */
public class DungeonFrontierTrigger
{
    private static final Logger LOGGER = LogUtils.getLogger();
    // Originally 20 (once/second), matching GodDropVoidRescueHandler's own cadence on the assumption that
    // "nothing here needs sub-second responsiveness" - wrong in practice: a player moving faster than
    // TRIGGER_RADIUS blocks/second (easily reached by sprinting, let alone creative flight) can cross an
    // entire connector's trigger zone between two polls and never get caught. Confirmed in playtesting: a
    // 16-block bighallway chain's SIDE connectors (partway down the long wall, not directly on the walking
    // path toward the next room) were almost always skipped this way, while the front connector - directly
    // ahead, naturally lingered near while the next room generates - kept triggering reliably. 5 ticks
    // (4/second) shrinks that skip window to a much smaller, much less speed-sensitive distance.
    private static final int CHECK_INTERVAL_TICKS = 5;
    // Bumped from 6 - a larger radius means a room's multi-tick paste job has more time to finish before the
    // player actually reaches it, hiding pop-in further out. No real downside, just triggers a little earlier
    // than strictly necessary.
    private static final double TRIGGER_RADIUS = 15.0;
    private static final double TRIGGER_RADIUS_SQR = TRIGGER_RADIUS * TRIGGER_RADIUS;

    // Floor 1 ("lv2") gets a mossy stone_bricks variant on its structural/filler pieces, so it reads as a
    // visibly older, deeper level than floor 0 rather than looking identical. Only the shared UTILITY pieces
    // qualify - the corrector and the doorway blocker, which are genuinely floor-agnostic infrastructure, not
    // "real" room content. Every actual room (hallway, bighallway, room, etc.) is lv1-exclusive content and
    // never spawns on any other floor at all (see DungeonRoomPool.FLOOR_CANDIDATE_POOLS), so it never needs a
    // recolor - each floor gets its own purpose-built rooms instead of a reskinned reuse of floor 0's.
    private static final int MOSSY_VARIANT_FLOOR = 1;
    private static final Set<ResourceLocation> MOSSY_ELIGIBLE_TEMPLATES = buildMossyEligibleTemplates();

    private static Set<ResourceLocation> buildMossyEligibleTemplates()
    {
        Set<ResourceLocation> ids = new HashSet<>(DungeonRoomPool.RESIDUE_CORRECTORS.values());
        ids.add(DungeonRoomPool.DOORWAY_BLOCKER);
        return Set.copyOf(ids);
    }

    /** Wraps `onComplete` to additionally swap every stone_bricks block in the just-placed structure over to
     * mossy_stone_bricks, if `floorIndex`/`templateId` qualify (see MOSSY_VARIANT_FLOOR/MOSSY_ELIGIBLE_
     * TEMPLATES) - a plain post-placement scan-and-replace rather than a custom StructureProcessor, since a
     * custom StructureProcessorType can't be registered by mods in this version (BuiltInRegistries.STRUCTURE_
     * PROCESSOR freezes before Forge constructs mod instances - see the base mod's ArenaTriggerBlock for the
     * same limitation hit there). Has to run AFTER the room's own placement job fully finishes (chained onto
     * the SAME onComplete, matching every other post-placement step here) - swapping blocks that don't exist
     * yet would do nothing. */
    private static Runnable withMossyVariant(Runnable onComplete, ServerLevel level, int floorIndex,
                                              ResourceLocation templateId, StructureTemplate template, BlockPos origin, Rotation rotation)
    {
        if (floorIndex != MOSSY_VARIANT_FLOOR || !MOSSY_ELIGIBLE_TEMPLATES.contains(templateId))
        {
            return onComplete;
        }
        return () ->
        {
            onComplete.run();
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation);
            for (StructureTemplate.StructureBlockInfo info : template.filterBlocks(origin, settings, Blocks.STONE_BRICKS))
            {
                level.setBlockAndUpdate(info.pos(), Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
            }
        };
    }

    public static void onLevelTick(TickEvent.LevelTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || event.side.isClient()
                || !(event.level instanceof ServerLevel level) || level.dimension() != ModDimensions.DUNGEON_KEY)
        {
            return;
        }
        if (level.getGameTime() % CHECK_INTERVAL_TICKS != 0 || DungeonRoomPool.BASE_FLOOR_ROOMS.isEmpty())
        {
            return;
        }

        DungeonGraphSavedData graph = DungeonGraphSavedData.get(level);
        for (ServerPlayer player : level.players())
        {
            Integer floorIndex = nearestFloor(graph, player.getY());
            if (floorIndex == null)
            {
                continue;
            }

            List<DungeonGraphSavedData.ConnectorState> frontier = graph.getFrontierSnapshot(floorIndex);
            double nearestDistSqr = Double.MAX_VALUE;
            for (DungeonGraphSavedData.ConnectorState connector : frontier)
            {
                if (connector.status() != DungeonGraphSavedData.ConnectorStatus.OPEN)
                {
                    continue;
                }
                double dx = player.getX() - (connector.worldPos.getX() + 0.5);
                double dy = player.getY() - (connector.worldPos.getY() + 0.5);
                double dz = player.getZ() - (connector.worldPos.getZ() + 0.5);
                double distSqr = dx * dx + dy * dy + dz * dz;
                nearestDistSqr = Math.min(nearestDistSqr, distSqr);
                if (distSqr <= TRIGGER_RADIUS_SQR)
                {
                    if (connector.isDescend)
                    {
                        tryDescend(level, graph, floorIndex, connector);
                    }
                    else
                    {
                        tryExpand(level, graph, floorIndex, connector);
                    }
                }
            }
            LOGGER.info("Scan: player {} on floor {}, {} frontier connectors, nearest {} blocks away",
                    player.getGameProfile().getName(), floorIndex, frontier.size(),
                    nearestDistSqr == Double.MAX_VALUE ? "n/a" : String.valueOf(Math.sqrt(nearestDistSqr)));
        }
    }

    private static Integer nearestFloor(DungeonGraphSavedData graph, double playerY)
    {
        Integer best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (int floorIndex : graph.getFloorIndices())
        {
            int yOffset = graph.getFloorYOffset(floorIndex);
            int distance = Math.abs((int) playerY - yOffset);
            if (distance < bestDistance)
            {
                bestDistance = distance;
                best = floorIndex;
            }
        }
        return best;
    }

    private static void tryExpand(ServerLevel level, DungeonGraphSavedData graph, int floorIndex, DungeonGraphSavedData.ConnectorState connector)
    {
        LOGGER.info("Frontier trigger fired at {} (front={}, top={}, joint={}) - claiming and searching {} candidate rooms",
                connector.worldPos, connector.frontFacing, connector.topFacing, connector.jointType, DungeonRoomPool.BASE_FLOOR_ROOMS.size());

        // Claimed immediately, before any candidate search - see class javadoc.
        graph.setConnectorStatus(floorIndex, connector.worldPos, DungeonGraphSavedData.ConnectorStatus.CLAIMED_PENDING);

        if (tryLoopClosure(graph, floorIndex, connector))
        {
            return;
        }

        // Checked BEFORE any candidate is even considered, unconditionally - see RESIDUE_CORRECTORS' own
        // javadoc for the full derivation. This connector's own perpendicular-axis coordinate needs to be a
        // multiple of GRID_UNIT for ANY future multi-axis room (bighallway, tjunction) attached here to have
        // properly aligned side connectors - simple single-axis rooms (hallway, room, turn) don't care about
        // this axis at all, but since we don't know in advance which candidate will end up winning, this
        // check applies regardless of what eventually attaches. Cheap and harmless when unnecessary (just an
        // extra small segment), but the only way to actually fix the root cause rather than reacting to it
        // after a multi-axis room's side connectors are already doomed.
        int residue = computePerpendicularResidue(connector);
        if (residue != 0)
        {
            LOGGER.info("Connector at {} has perpendicular-axis residue {} - inserting a corrector before searching candidates",
                    connector.worldPos, residue);
            placeCorrector(level, graph, floorIndex, connector, residue);
            return;
        }

        // No fallback to WEIGHTED_CANDIDATES here - every real room in that pool is lv1-exclusive content
        // (see DungeonRoomPool.FLOOR_CANDIDATE_POOLS' own javadoc), so a floor with no explicit pool entry of
        // its own gets an EMPTY candidate list, not a silent reuse of floor 0's rooms. Every connector on such
        // a floor just seals with the doorway blocker (still floor-agnostic utility content) until this floor
        // gets its own real pool entry.
        List<ResourceLocation> floorCandidates = DungeonRoomPool.FLOOR_CANDIDATE_POOLS.getOrDefault(floorIndex, List.of());
        List<ResourceLocation> shuffled = new ArrayList<>(floorCandidates);
        RandomSource random = level.getRandom();
        for (int i = shuffled.size() - 1; i > 0; i--)
        {
            int j = random.nextInt(i + 1);
            ResourceLocation tmp = shuffled.get(i);
            shuffled.set(i, shuffled.get(j));
            shuffled.set(j, tmp);
        }

        // WEIGHTED_CANDIDATES repeats bighallway to bias the shuffle in its favor (see its own javadoc) -
        // skip a template once it's already been fully tried this call, so that repetition changes which
        // template tends to come out on top rather than wastefully re-scanning the same one.
        Set<ResourceLocation> alreadyTried = new HashSet<>();
        for (ResourceLocation templateId : shuffled)
        {
            if (!alreadyTried.add(templateId))
            {
                continue;
            }
            StructureTemplate template = level.getStructureManager().getOrCreate(templateId);
            ConnectorScanner.TemplateConnectors scanned = ConnectorScanner.scan(level, templateId);

            // A candidate's OWN descend-marked connector (e.g. lv1drop's) must never be used as the attach
            // point for a normal room-to-room join - it's reserved exclusively for tryDescend, fired later
            // once the room is already placed and that connector becomes an OPEN connector in its own right.
            // Without this filter, RoomAligner would occasionally pick lv1drop's descend connector (local
            // Y=0, at the BOTTOM of its own footprint) as the attach point instead of one of its 4 normal
            // ring connectors (local Y=10, at the TOP) - since attaching flips whichever local connector was
            // used to match the existing open connector's position, this silently inverted the whole room,
            // producing the "spawns going up instead of down" bug (confirmed root cause, not a rare
            // unavoidable quirk).
            List<ConnectorScanner.ConnectorInfo> candidateConnectors = new ArrayList<>();
            for (ConnectorScanner.ConnectorInfo info : scanned.connectors())
            {
                if (!info.name().equals(DungeonGraphSavedData.DESCEND_MARKER_NAME))
                {
                    candidateConnectors.add(info);
                }
            }
            for (int i = candidateConnectors.size() - 1; i > 0; i--)
            {
                int j = random.nextInt(i + 1);
                ConnectorScanner.ConnectorInfo tmp = candidateConnectors.get(i);
                candidateConnectors.set(i, candidateConnectors.get(j));
                candidateConnectors.set(j, tmp);
            }

            for (ConnectorScanner.ConnectorInfo localConnector : candidateConnectors)
            {
                Optional<RoomAligner.AlignedPlacement> aligned =
                        RoomAligner.tryAlign(level, template, templateId, connector, localConnector, floorIndex, graph);
                LOGGER.info("  candidate {} (local {}, front={}, top={}, joint={}) -> {}",
                        templateId, localConnector.localPos(), localConnector.front(), localConnector.top(),
                        localConnector.joint(), aligned.isPresent() ? "FIT" : "no fit");
                if (aligned.isPresent())
                {
                    commit(level, graph, floorIndex, templateId, template, connector, aligned.get());
                    return;
                }
            }
        }

        LOGGER.info("No candidate fit at {} - sealing with the doorway blocker", connector.worldPos);
        capDeadEnd(level, graph, floorIndex, connector);
    }

    /** True loop closure: if a DIFFERENT room's own open connector already sits exactly one block ahead of
     * this one, facing back toward it, the two branches have grown to meet each other - joining them needs
     * no new room and no blocker, since each side's own doorway is already open (final_state already
     * applied when each room was placed). Checked before any candidate room search specifically so two
     * branches meeting is never mistaken for a dead end - that's the whole reason the grid system exists
     * (see the plan's Context section on loop-closure alignment). Returns true if a join happened. */
    private static boolean tryLoopClosure(DungeonGraphSavedData graph, int floorIndex, DungeonGraphSavedData.ConnectorState connector)
    {
        BlockPos expectedPartnerPos = connector.worldPos.relative(connector.frontFacing);
        DungeonGraphSavedData.ConnectorState partner = graph.getConnector(floorIndex, expectedPartnerPos);
        if (partner == null || partner.status() != DungeonGraphSavedData.ConnectorStatus.OPEN
                || partner.ownerRoomId == connector.ownerRoomId
                || partner.frontFacing != connector.frontFacing.getOpposite())
        {
            return false;
        }

        graph.setConnectorStatus(floorIndex, connector.worldPos, DungeonGraphSavedData.ConnectorStatus.EXPANDED);
        graph.setConnectorStatus(floorIndex, partner.worldPos, DungeonGraphSavedData.ConnectorStatus.EXPANDED);
        LOGGER.info("Loop closure: joined {} directly to {} - two branches met, no new room or blocker needed",
                connector.worldPos, partner.worldPos);
        return true;
    }

    /** Handles a connector tagged DESCEND_MARKER_NAME - instead of the normal same-floor candidate search,
     * seeds (or joins) floor `floorIndex + 1`'s own shared maze. Mirrors the overworld portal system's own
     * "multiple entries into one shared pool" pattern exactly: every descend connector discovered across
     * floor `floorIndex`, no matter which room it's attached to, leads into the SAME floor `floorIndex + 1`
     * graph - each one just becomes another seed room in that one shared maze, not an independent floor of
     * its own (consistent with the project's core "one shared persistent maze" principle). Reuses the same
     * entrance.nbt template DungeonEntrancePlacer already uses for floor 0 - it's already the generic
     * "grid-conforming room with connectors on all 4 sides" seed piece, no reason to need a dedicated one
     * per floor.
     *
     * <p>This is a genuinely PHYSICAL transition, not a teleport: the descend room (e.g. lv1drop) is built
     * tall specifically so a player can walk in near its top (where floor `floorIndex`'s own maze connects)
     * and drop down an open shaft to its bottom, where the descend connector itself sits - so the new floor's
     * entrance has to land at THAT connector's own world Y, not some separately-chosen offset. The room's
     * height alone is what provides the vertical separation between floors; there's no FLOOR_HEIGHT constant
     * or similar involved anywhere in this method.</p> */
    /** Seeds `targetFloor` with a normal room from ITS OWN candidate pool when a descend connector fires -
     * NOT a special "entrance" room. Only floor 0 needs a real entrance-with-return-portal (the only floor
     * actually reachable from the overworld); a floor reached by falling through a drop has no need for one,
     * since the only way back is the same physical shaft, not a teleport - so this mirrors tryExpand's own
     * candidate-search-then-commit shape (shuffle the pool, try every connector of every candidate, footprint
     * + collision check, commit on first fit) rather than DungeonEntrancePlacer.placeEntranceRoom. */
    private static void tryDescend(ServerLevel level, DungeonGraphSavedData graph, int floorIndex, DungeonGraphSavedData.ConnectorState connector)
    {
        int targetFloor = floorIndex + 1;
        LOGGER.info("Descend connector fired at {} (front={}, top={}) - seeding floor {}",
                connector.worldPos, connector.frontFacing, connector.topFacing, targetFloor);

        // Claimed immediately, before any placement work - see class javadoc on why (tri-state status).
        graph.setConnectorStatus(floorIndex, connector.worldPos, DungeonGraphSavedData.ConnectorStatus.CLAIMED_PENDING);

        Direction requiredFront = connector.frontFacing.getOpposite();
        boolean requireTopMatch = "aligned".equals(connector.jointType);

        // Same "no fallback to floor 0's lv1-exclusive rooms" rule tryExpand follows - if targetFloor has no
        // real pool of its own yet, this stays open with a warning below rather than seeding it with
        // borrowed floor-0 content.
        List<ResourceLocation> floorCandidates = DungeonRoomPool.FLOOR_CANDIDATE_POOLS.getOrDefault(targetFloor, List.of());
        List<ResourceLocation> shuffled = new ArrayList<>(floorCandidates);
        RandomSource random = level.getRandom();
        for (int i = shuffled.size() - 1; i > 0; i--)
        {
            int j = random.nextInt(i + 1);
            ResourceLocation tmp = shuffled.get(i);
            shuffled.set(i, shuffled.get(j));
            shuffled.set(j, tmp);
        }

        Set<ResourceLocation> alreadyTried = new HashSet<>();
        for (ResourceLocation templateId : shuffled)
        {
            if (!alreadyTried.add(templateId))
            {
                continue;
            }
            StructureTemplate template = level.getStructureManager().getOrCreate(templateId);
            ConnectorScanner.TemplateConnectors scanned = ConnectorScanner.scan(level, templateId);

            for (ConnectorScanner.ConnectorInfo attachLocal : scanned.connectors())
            {
                // Same rule tryExpand's own candidate search follows (see its own comment) - a candidate's
                // OWN descend-marked connector (e.g. a future lv2-to-lv3 drop room) must never be used as the
                // attach point here either, for the same reason: it would silently invert the room's
                // orientation.
                if (attachLocal.name().equals(DungeonGraphSavedData.DESCEND_MARKER_NAME))
                {
                    continue;
                }
                Rotation chosenRotation = null;
                for (Rotation candidate : Rotation.values())
                {
                    if (candidate.rotate(attachLocal.front()) != requiredFront)
                    {
                        continue;
                    }
                    if (!requireTopMatch || candidate.rotate(attachLocal.top()) == connector.topFacing)
                    {
                        chosenRotation = candidate;
                        break;
                    }
                }
                if (chosenRotation == null)
                {
                    continue;
                }

                // Same formula every other room attachment in this system uses (see RoomAligner/
                // placeCorrector), Y included - the descend connector's own world position already IS where
                // the new floor's room needs to be, exactly like any other connector-to-connector join.
                BlockPos rotatedAttachPos = StructureTemplate.transform(attachLocal.localPos(), Mirror.NONE, chosenRotation, BlockPos.ZERO);
                BlockPos worldOrigin = connector.worldPos.subtract(rotatedAttachPos);

                // Same footprint-corner along-wall check placeCorrector uses.
                Direction attachWorldFront = chosenRotation.rotate(attachLocal.front());
                boolean alongWallIsX = attachWorldFront == Direction.NORTH || attachWorldFront == Direction.SOUTH;
                Vec3i size = template.getSize();
                int maxLocalX = size.getX() - 1;
                int maxLocalZ = size.getZ() - 1;
                int minFootprintWorldX = Integer.MAX_VALUE, minFootprintWorldZ = Integer.MAX_VALUE;
                for (int[] corner : new int[][]{{0, 0}, {maxLocalX, 0}, {0, maxLocalZ}, {maxLocalX, maxLocalZ}})
                {
                    BlockPos rotatedCorner = StructureTemplate.transform(new BlockPos(corner[0], 0, corner[1]), Mirror.NONE, chosenRotation, BlockPos.ZERO);
                    minFootprintWorldX = Math.min(minFootprintWorldX, worldOrigin.getX() + rotatedCorner.getX());
                    minFootprintWorldZ = Math.min(minFootprintWorldZ, worldOrigin.getZ() + rotatedCorner.getZ());
                }
                int alongWallMin = alongWallIsX ? minFootprintWorldX : minFootprintWorldZ;
                if (Math.floorMod(alongWallMin, DungeonGraphSavedData.GRID_UNIT) != 0)
                {
                    LOGGER.info("    descend: rejected {} attach (rotation={}) - footprint's minimum {} coordinate ({}) isn't grid-aligned, trying another connector",
                            templateId, chosenRotation, alongWallIsX ? "X" : "Z", alongWallMin);
                    continue;
                }

                // Defensive collision check - the first-ever descend to a given target floor can never
                // collide (nothing placed there yet), but a later one (a different floor-N room's own
                // descend connector, discovered after floor N+1 has already grown organically) could land
                // somewhere already occupied - including a DIFFERENT drop's own reserved-but-not-yet-visited
                // footprint (see commit()'s reserveForeignCells call). Just leave it open on collision,
                // matching the project's existing tolerance for rare unrecoverable edge cases.
                Set<Long> footprintCells = RoomAligner.computeFootprintCells(template, worldOrigin, chosenRotation);
                if (graph.anyCellOccupiedByOtherRoom(targetFloor, footprintCells, -1))
                {
                    LOGGER.info("    descend: rejected {} attach (rotation={}) - footprint collides with an already-placed room on floor {}",
                            templateId, chosenRotation, targetFloor);
                    continue;
                }

                // Same worldConnectors + shared-boundary-cell filtering commit() does for a normal room.
                List<DungeonGraphSavedData.WorldConnector> allConnectors = new ArrayList<>(scanned.connectors().size());
                for (ConnectorScanner.ConnectorInfo info : scanned.connectors())
                {
                    BlockPos rotatedPos = StructureTemplate.transform(info.localPos(), Mirror.NONE, chosenRotation, BlockPos.ZERO);
                    BlockPos worldPos = worldOrigin.offset(rotatedPos);
                    Direction worldFront = chosenRotation.rotate(info.front());
                    Direction worldTop = chosenRotation.rotate(info.top());
                    boolean isDescend = info.name().equals(DungeonGraphSavedData.DESCEND_MARKER_NAME);
                    allConnectors.add(new DungeonGraphSavedData.WorldConnector(worldPos, worldFront, worldTop, info.joint(), isDescend));
                }
                List<DungeonGraphSavedData.WorldConnector> otherConnectors = allConnectors.stream()
                        .filter(wc -> !wc.worldPos().equals(connector.worldPos))
                        .toList();

                forceLoadFootprint(level, footprintCells);
                graph.placeRoom(targetFloor, worldOrigin.getY(), templateId, worldOrigin, chosenRotation, footprintCells, otherConnectors);
                graph.setConnectorStatus(floorIndex, connector.worldPos, DungeonGraphSavedData.ConnectorStatus.EXPANDED);

                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(chosenRotation);
                Runnable onComplete = withMossyVariant(buildFinalStateCallback(level, templateId, worldOrigin, chosenRotation),
                        level, targetFloor, templateId, template, worldOrigin, chosenRotation);
                IncrementalStructurePlacer.enqueue(level, template, worldOrigin, settings, RandomSource.create(), onComplete);
                return;
            }
        }

        LOGGER.warn("Descend connector at {} couldn't find a valid room placement on floor {} - leaving it open",
                connector.worldPos, targetFloor);
        graph.setConnectorStatus(floorIndex, connector.worldPos, DungeonGraphSavedData.ConnectorStatus.OPEN);
    }

    /** The world coordinate on the axis PERPENDICULAR to this connector's own front-facing (i.e. the "depth"
     * direction a room attached here would extend along), mod GRID_UNIT. See RESIDUE_CORRECTORS' own javadoc
     * for why this needs to be 0 for a future multi-axis room's side connectors to align properly. */
    private static int computePerpendicularResidue(DungeonGraphSavedData.ConnectorState connector)
    {
        boolean alongWallIsX = connector.frontFacing == Direction.NORTH || connector.frontFacing == Direction.SOUTH;
        int depthCoord = alongWallIsX ? connector.worldPos.getZ() : connector.worldPos.getX();
        return Math.floorMod(depthCoord, DungeonGraphSavedData.GRID_UNIT);
    }

    /** Inserts the corrector piece that shifts `connector`'s perpendicular-axis residue back to a multiple of
     * GRID_UNIT (see RESIDUE_CORRECTORS' own javadoc). Structurally similar to capDeadEnd (bypasses
     * RoomAligner entirely - correctors aren't grid-conforming, so none of the footprint/collision machinery
     * real rooms need applies here) but placed rather than sealed: registers its own back connector as a
     * fresh OPEN connector rather than leaving nothing behind, so the frontier keeps growing normally from
     * there. Reserves no grid cell, same as the blocker. */
    private static void placeCorrector(ServerLevel level, DungeonGraphSavedData graph, int floorIndex,
                                        DungeonGraphSavedData.ConnectorState connector, int residue)
    {
        int neededLength = DungeonGraphSavedData.GRID_UNIT - residue;
        Map<Integer, ResourceLocation> correctorSet = DungeonRoomPool.RESIDUE_CORRECTORS_BY_FLOOR.getOrDefault(floorIndex, DungeonRoomPool.RESIDUE_CORRECTORS);
        ResourceLocation correctorId = correctorSet.get(neededLength);
        StructureTemplate template = level.getStructureManager().getOrCreate(correctorId);
        ConnectorScanner.TemplateConnectors scanned = ConnectorScanner.scan(level, correctorId);
        if (scanned.connectors().size() != 2)
        {
            LOGGER.warn("Corrector {} doesn't have exactly 2 connectors - leaving {} open", correctorId, connector.worldPos);
            graph.setConnectorStatus(floorIndex, connector.worldPos, DungeonGraphSavedData.ConnectorStatus.OPEN);
            return;
        }

        Direction requiredFront = connector.frontFacing.getOpposite();
        boolean requireTopMatch = "aligned".equals(connector.jointType);

        for (ConnectorScanner.ConnectorInfo attachLocal : scanned.connectors())
        {
            Rotation chosenRotation = null;
            for (Rotation candidate : Rotation.values())
            {
                if (candidate.rotate(attachLocal.front()) != requiredFront)
                {
                    continue;
                }
                if (!requireTopMatch || candidate.rotate(attachLocal.top()) == connector.topFacing)
                {
                    chosenRotation = candidate;
                    break;
                }
            }
            if (chosenRotation == null)
            {
                continue;
            }

            BlockPos rotatedAttachPos = StructureTemplate.transform(attachLocal.localPos(), Mirror.NONE, chosenRotation, BlockPos.ZERO);
            BlockPos worldOrigin = connector.worldPos.subtract(rotatedAttachPos);

            // Same invariant RoomAligner enforces for every normal room (see its own javadoc): worldOrigin
            // is where LOCAL (0,0,0) lands, but which CORNER of the footprint that actually is depends on
            // rotation direction - checking the connector's own along-wall coordinate alone (as this method
            // used to) isn't enough, since a room can have a perfectly-aligned attach point while the
            // footprint's OTHER corner (computed via all 4 corners, not just the attach point) lands off the
            // grid - confirmed in playtesting: a CLOCKWISE_180-rotated corrector_6 did exactly this. If this
            // specific (jigsaw, rotation) pairing fails, `continue` tries the OTHER jigsaw as the attach point
            // instead - front vs back differ by a 180-degree rotation for the same requiredFront, which
            // flips the direction local coordinates map to world ones, so the other option often succeeds
            // where this one didn't.
            Direction attachWorldFront = chosenRotation.rotate(attachLocal.front());
            boolean alongWallIsX = attachWorldFront == Direction.NORTH || attachWorldFront == Direction.SOUTH;
            Vec3i size = template.getSize();
            int maxLocalX = size.getX() - 1;
            int maxLocalZ = size.getZ() - 1;
            int minFootprintWorldX = Integer.MAX_VALUE, minFootprintWorldZ = Integer.MAX_VALUE;
            for (int[] corner : new int[][]{{0, 0}, {maxLocalX, 0}, {0, maxLocalZ}, {maxLocalX, maxLocalZ}})
            {
                BlockPos rotatedCorner = StructureTemplate.transform(new BlockPos(corner[0], 0, corner[1]), Mirror.NONE, chosenRotation, BlockPos.ZERO);
                minFootprintWorldX = Math.min(minFootprintWorldX, worldOrigin.getX() + rotatedCorner.getX());
                minFootprintWorldZ = Math.min(minFootprintWorldZ, worldOrigin.getZ() + rotatedCorner.getZ());
            }
            int alongWallMin = alongWallIsX ? minFootprintWorldX : minFootprintWorldZ;
            if (Math.floorMod(alongWallMin, DungeonGraphSavedData.GRID_UNIT) != 0)
            {
                LOGGER.info("    corrector: rejected {} attach (rotation={}) - footprint's minimum {} coordinate ({}) isn't grid-aligned, trying the other jigsaw",
                        correctorId, chosenRotation, alongWallIsX ? "X" : "Z", alongWallMin);
                continue;
            }

            // Correctors reserve no cell of their own (see RESIDUE_CORRECTORS' javadoc - deliberate, so they
            // never block a future room from attaching past them), but that's a statement about what they
            // reserve GOING FORWARD, not a license to ignore what's already there: without this check a
            // corrector can be pasted straight through an already-placed room's footprint (confirmed against
            // lv1drop specifically - its footprint spans more than one grid cell when attached via a
            // maxLocal-depth connector, and a corrector routed nearby cut right through it, since nothing here
            // was consulting the occupied-cell set at all before this fix).
            Set<Long> correctorFootprintCells = RoomAligner.computeFootprintCells(template, worldOrigin, chosenRotation);
            if (graph.anyCellOccupiedByOtherRoom(floorIndex, correctorFootprintCells, connector.ownerRoomId))
            {
                LOGGER.info("    corrector: rejected {} attach (rotation={}) - footprint overlaps an already-placed room, trying the other jigsaw",
                        correctorId, chosenRotation);
                continue;
            }

            ConnectorScanner.ConnectorInfo outgoingLocal = scanned.connectors().get(0) == attachLocal
                    ? scanned.connectors().get(1) : scanned.connectors().get(0);
            BlockPos rotatedOutgoingPos = StructureTemplate.transform(outgoingLocal.localPos(), Mirror.NONE, chosenRotation, BlockPos.ZERO);
            BlockPos outgoingWorldPos = worldOrigin.offset(rotatedOutgoingPos);
            Direction outgoingWorldFront = chosenRotation.rotate(outgoingLocal.front());
            Direction outgoingWorldTop = chosenRotation.rotate(outgoingLocal.top());

            LOGGER.info("    corrector: id={} length={} connectorWorldPos={} rotation={} worldOrigin={} newOutgoing={}",
                    correctorId, neededLength, connector.worldPos, chosenRotation, worldOrigin, outgoingWorldPos);

            Integer yOffset = graph.getFloorYOffset(floorIndex);
            List<DungeonGraphSavedData.WorldConnector> outgoingList = List.of(new DungeonGraphSavedData.WorldConnector(
                    outgoingWorldPos, outgoingWorldFront, outgoingWorldTop, outgoingLocal.joint(), false));
            graph.placeRoom(floorIndex, yOffset == null ? worldOrigin.getY() : yOffset, correctorId, worldOrigin,
                    chosenRotation, Set.of(), outgoingList);
            graph.setConnectorStatus(floorIndex, connector.worldPos, DungeonGraphSavedData.ConnectorStatus.EXPANDED);

            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(chosenRotation);
            Runnable onComplete = withMossyVariant(buildFinalStateCallback(level, correctorId, worldOrigin, chosenRotation),
                    level, floorIndex, correctorId, template, worldOrigin, chosenRotation);
            IncrementalStructurePlacer.enqueue(level, template, worldOrigin, settings, RandomSource.create(), onComplete);
            return;
        }

        // Sealed, not left OPEN: tryExpand's residue check (the only caller of this method) runs BEFORE the
        // real-candidate search and unconditionally returns after calling this, so an OPEN connector left here
        // would just re-enter tryExpand next tick, hit the same residue check, retry the same corrector, and
        // fail the same way again - forever (confirmed in playtesting: this produced permanently uncapped
        // 1xGRID_UNIT holes in the maze, logged as endless repeated "rejected...trying the other jigsaw"
        // spam). Neither the residue nor the collision this failed against changes between retries, so capping
        // immediately is strictly better than an infinite loop that never resolves.
        LOGGER.warn("Corrector {} couldn't attach to {} (front={}, joint={}) - sealing as a dead end",
                correctorId, connector.worldPos, connector.frontFacing, connector.jointType);
        capDeadEnd(level, graph, floorIndex, connector);
    }

    /** Last resort when no real room fits a connector: plugs the opening with DungeonRoomPool.DOORWAY_BLOCKER
     * instead of leaving a hole into the void forever. Deliberately bypasses RoomAligner entirely - the
     * blocker isn't grid-sized, reserves no cell, and is never itself attachable, so none of the footprint/
     * collision machinery real rooms need applies here; it's just a rotation + translation match on the
     * blocker's own single connector, then a direct paste. */
    private static void capDeadEnd(ServerLevel level, DungeonGraphSavedData graph, int floorIndex, DungeonGraphSavedData.ConnectorState connector)
    {
        ResourceLocation blockerId = DungeonRoomPool.DOORWAY_BLOCKER;
        StructureTemplate template = level.getStructureManager().getOrCreate(blockerId);
        ConnectorScanner.TemplateConnectors scanned = ConnectorScanner.scan(level, blockerId);
        if (scanned.connectors().isEmpty())
        {
            LOGGER.warn("DOORWAY_BLOCKER ({}) has no jigsaw block - leaving {} open", blockerId, connector.worldPos);
            graph.setConnectorStatus(floorIndex, connector.worldPos, DungeonGraphSavedData.ConnectorStatus.OPEN);
            return;
        }
        ConnectorScanner.ConnectorInfo blockerConnector = scanned.connectors().get(0);

        Direction requiredFront = connector.frontFacing.getOpposite();
        boolean requireTopMatch = "aligned".equals(connector.jointType);
        Rotation chosenRotation = null;
        for (Rotation candidate : Rotation.values())
        {
            if (candidate.rotate(blockerConnector.front()) != requiredFront)
            {
                continue;
            }
            if (!requireTopMatch || candidate.rotate(blockerConnector.top()) == connector.topFacing)
            {
                chosenRotation = candidate;
                break;
            }
        }
        if (chosenRotation == null)
        {
            LOGGER.warn("DOORWAY_BLOCKER's connector can't match {} (front={}, joint={}) - leaving it open",
                    connector.worldPos, connector.frontFacing, connector.jointType);
            graph.setConnectorStatus(floorIndex, connector.worldPos, DungeonGraphSavedData.ConnectorStatus.OPEN);
            return;
        }

        BlockPos rotatedLocalConnectorPos = StructureTemplate.transform(blockerConnector.localPos(), Mirror.NONE, chosenRotation, BlockPos.ZERO);
        BlockPos worldOrigin = connector.worldPos.subtract(rotatedLocalConnectorPos);
        LOGGER.info("    blocker: connectorWorldPos={} blockerLocalConnector={} rotation={} worldOrigin={}",
                connector.worldPos, blockerConnector.localPos(), chosenRotation, worldOrigin);

        StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(chosenRotation);
        Runnable onComplete = withMossyVariant(buildFinalStateCallback(level, blockerId, worldOrigin, chosenRotation),
                level, floorIndex, blockerId, template, worldOrigin, chosenRotation);
        IncrementalStructurePlacer.enqueue(level, template, worldOrigin, settings, RandomSource.create(), onComplete);

        graph.setConnectorStatus(floorIndex, connector.worldPos, DungeonGraphSavedData.ConnectorStatus.EXPANDED);
    }

    private static void commit(ServerLevel level, DungeonGraphSavedData graph, int floorIndex, ResourceLocation templateId,
                                StructureTemplate template, DungeonGraphSavedData.ConnectorState connector,
                                RoomAligner.AlignedPlacement placement)
    {
        // The connector we attached with lands at the exact same world position as `connector` itself (see
        // RoomAligner) - drop it from the new room's own connector list so placeRoom doesn't clobber the
        // entry we're about to mark EXPANDED below with a fresh OPEN one under the new room's ownership.
        List<DungeonGraphSavedData.WorldConnector> otherConnectors = placement.worldConnectors().stream()
                .filter(wc -> !wc.worldPos().equals(connector.worldPos))
                .toList();

        forceLoadFootprint(level, placement.footprintCells());

        Integer yOffset = graph.getFloorYOffset(floorIndex);
        long newRoomId = graph.placeRoom(floorIndex, yOffset == null ? placement.worldOrigin().getY() : yOffset, templateId,
                placement.worldOrigin(), placement.rotation(), placement.footprintCells(), otherConnectors);
        graph.setConnectorStatus(floorIndex, connector.worldPos, DungeonGraphSavedData.ConnectorStatus.EXPANDED);

        // A descend room's blocks (its full footprint, top AND bottom) get pasted right now, the moment it's
        // placed - NOT later when a player actually walks up to its descend connector and tryDescend fires.
        // The floor below doesn't necessarily exist yet at this point, but its own organic growth can start
        // well before any player ever triggers this room's descend connector, so reserving this footprint on
        // floorIndex+1 has to happen here, at placement time, not inside tryDescend - reserving it there was
        // too late (confirmed in playtesting: a floor-below room still pasted through a not-yet-triggered
        // drop's bottom, because tryDescend's own reservation only ran once the connector actually fired).
        if (otherConnectors.stream().anyMatch(DungeonGraphSavedData.WorldConnector::isDescend))
        {
            graph.reserveForeignCells(floorIndex + 1, placement.worldOrigin().getY(), placement.footprintCells());
        }

        StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(placement.rotation());
        Runnable applyFinalStates = buildFinalStateCallback(level, templateId, placement.worldOrigin(), placement.rotation());

        // Any connector RoomAligner flagged as unable to ever find a valid match (see its own javadoc) gets
        // sealed right now, rather than left OPEN to fail every candidate (and lag-spike doing so) whenever a
        // player eventually wanders up to it. This has to run AFTER the room's own placement job has fully
        // finished (chained onto the SAME onComplete, not enqueued separately beforehand) - IncrementalStruct-
        // urePlacer processes queued jobs in order, so a blocker job queued before the room's own would paste
        // into empty space and then get overwritten moments later when the room's own (still-raw, un-
        // converted) jigsaw block gets pasted on top of it.
        Runnable onComplete = () ->
        {
            applyFinalStates.run();
            for (BlockPos misalignedPos : placement.misalignedConnectorPositions())
            {
                DungeonGraphSavedData.ConnectorState misaligned = graph.getConnector(floorIndex, misalignedPos);
                // Only seal it if it's genuinely the new room's own connector, still OPEN. The grid being
                // discrete and finite means an unrelated room's own connector geometry can coincidentally
                // land at the exact same world position as one already tracked here - if this position
                // turns out to belong to a DIFFERENT room (or was placeRoom's own put-if-absent above
                // correctly leaving an existing entry untouched), sealing it would paste a blocker directly
                // over what's possibly an already-working doorway to a completely unrelated part of the
                // maze. Skipping it here is always safe: if it's a genuine problem, it's that OTHER room's
                // own problem to have already dealt with when IT was placed.
                if (misaligned != null && misaligned.ownerRoomId == newRoomId && misaligned.status() == DungeonGraphSavedData.ConnectorStatus.OPEN)
                {
                    capDeadEnd(level, graph, floorIndex, misaligned);
                }
            }
        };
        onComplete = withMossyVariant(onComplete, level, floorIndex, templateId, template, placement.worldOrigin(), placement.rotation());
        IncrementalStructurePlacer.enqueue(level, template, placement.worldOrigin(), settings, RandomSource.create(), onComplete);
    }

    /** Force-loads every chunk a room's footprint touches before its blocks get pasted - same idiom the base
     * mod's VesselLairPlacer uses for its own single-piece placements, just generalized to an arbitrary set
     * of grid cells instead of one fixed origin. Cells are 8 blocks, chunks are 16, so this may force-load
     * the same chunk more than once for a multi-cell footprint - harmless, getChunk is idempotent. */
    static void forceLoadFootprint(ServerLevel level, Set<Long> footprintCells)
    {
        for (long packedCell : footprintCells)
        {
            int cellX = (int) packedCell;
            int cellZ = (int) (packedCell >> 32);
            level.getChunk((cellX * DungeonGraphSavedData.GRID_UNIT) >> 4, (cellZ * DungeonGraphSavedData.GRID_UNIT) >> 4);
        }
    }

    /** Once a room's blocks are all down, every jigsaw block in it needs to become its own final_state
     * (normally minecraft:air) - IncrementalStructurePlacer pastes the raw saved blocks as-is and has no
     * concept of jigsaw final-state conversion (that's vanilla jigsaw generation's own job, which this
     * system deliberately doesn't use), so this addon has to apply it itself, once placement has actually
     * finished rather than immediately after enqueue (which only queues the job, it may run over many
     * ticks). Re-reads ConnectorScanner's cached local connector data rather than storing final_state on
     * DungeonGraphSavedData - it's only ever needed in this one moment, not worth persisting long-term. */
    static Runnable buildFinalStateCallback(ServerLevel level, ResourceLocation templateId, BlockPos worldOrigin, Rotation rotation)
    {
        return () ->
        {
            ConnectorScanner.TemplateConnectors scanned = ConnectorScanner.scan(level, templateId);
            HolderLookup<Block> blockLookup = level.registryAccess().lookupOrThrow(Registries.BLOCK);

            for (ConnectorScanner.ConnectorInfo info : scanned.connectors())
            {
                BlockPos rotatedPos = StructureTemplate.transform(info.localPos(), Mirror.NONE, rotation, BlockPos.ZERO);
                BlockPos worldPos = worldOrigin.offset(rotatedPos);
                try
                {
                    BlockStateParser.BlockResult result = BlockStateParser.parseForBlock(blockLookup, info.finalState(), true);
                    level.setBlock(worldPos, result.blockState(), Block.UPDATE_CLIENTS);
                }
                catch (CommandSyntaxException e)
                {
                    LOGGER.warn("Room {} has a jigsaw with an unparsable final_state '{}' at {} - leaving the raw jigsaw block in place",
                            templateId, info.finalState(), worldPos, e);
                }
            }
        };
    }
}
