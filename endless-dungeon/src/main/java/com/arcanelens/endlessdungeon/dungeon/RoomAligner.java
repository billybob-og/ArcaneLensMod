package com.arcanelens.endlessdungeon.dungeon;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Finds a Y-axis rotation that attaches a candidate room's own connector to an already-placed room's open
 * connector, computes the resulting world origin, and rejects the placement if its footprint would overlap
 * an already-occupied grid cell. Mirror is never used (Mirror.NONE throughout) - only Y-axis rotation, since
 * nothing in this system tilts or flips a room.
 *
 * <p>All position math routes through {@link StructureTemplate#transform(BlockPos, Mirror, Rotation, BlockPos)}
 * (pivot ZERO) rather than hand-derived rotation formulas - the exact convention IncrementalStructurePlacer's
 * callers already rely on (same offset passed as both the placement origin and rotation pivot), and safer
 * than re-deriving rotation sign conventions by hand (see this session's own spear-position debugging: hand-
 * derived rotation math produced the wrong sign more than once before switching to letting the game's own
 * transform do the work).</p>
 */
public class RoomAligner
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public record AlignedPlacement(BlockPos worldOrigin, Rotation rotation, Set<Long> footprintCells,
                                    List<DungeonGraphSavedData.WorldConnector> worldConnectors,
                                    Set<BlockPos> misalignedConnectorPositions)
    {
    }

    /** Attempts to attach `template`'s connector `localConnector` to `openConnector` (already in the graph,
     * status OPEN). Returns empty if no rotation satisfies the open connector's joint requirements, or if the
     * resulting footprint would overlap an already-occupied grid cell on this floor. */
    public static Optional<AlignedPlacement> tryAlign(ServerLevel level, StructureTemplate template, ResourceLocation templateId,
                                                        DungeonGraphSavedData.ConnectorState openConnector,
                                                        ConnectorScanner.ConnectorInfo localConnector,
                                                        int floorIndex, DungeonGraphSavedData graph)
    {
        Direction requiredFront = openConnector.frontFacing.getOpposite();
        boolean requireTopMatch = "aligned".equals(openConnector.jointType);

        Rotation chosenRotation = null;
        for (Rotation candidate : Rotation.values())
        {
            if (candidate.rotate(localConnector.front()) != requiredFront)
            {
                continue;
            }
            if (!requireTopMatch || candidate.rotate(localConnector.top()) == openConnector.topFacing)
            {
                chosenRotation = candidate;
                break;
            }
        }
        if (chosenRotation == null)
        {
            return Optional.empty();
        }

        BlockPos rotatedLocalConnectorPos = StructureTemplate.transform(localConnector.localPos(), Mirror.NONE, chosenRotation, BlockPos.ZERO);
        BlockPos worldOrigin = openConnector.worldPos.subtract(rotatedLocalConnectorPos);

        // worldOrigin is where LOCAL (0,0,0) lands in world space - but which CORNER of the footprint that
        // actually is depends on rotation. Under CLOCKWISE_180 (and on one axis for the two 90-degree
        // rotations), local (0,0,0) maps to the footprint's MAXIMUM corner on a given axis, not its minimum -
        // the rest of the room extends in the NEGATIVE direction from there. A check that reads
        // worldOrigin.getX()/getZ() directly and calls it aligned whenever that's a multiple of GRID_UNIT is
        // therefore wrong half the time: a room can have a perfectly-mod-8 worldOrigin and still have its
        // footprint straddle two cells, because the OTHER end of that same axis (worldOrigin - 7) lands in
        // the cell one below. This is exactly what happened to a watchtower placed via CLOCKWISE_180 even
        // after entrance.nbt's own jigsaw-offset bug (three connectors at local 3 instead of 4) was fixed -
        // the two bugs looked identical in-game but were different: one was bad input data, this one is a
        // sign error in how the alignment check reads a rotated footprint. The fix: always check the
        // footprint's MINIMUM world coordinate on the along-wall axis (found the same way
        // computeFootprintCells finds it - by transforming all four corners, not trusting worldOrigin to be
        // the min one), since a size-GRID_UNIT room with its minimum on a grid line always has its maximum
        // (min + GRID_UNIT - 1) still inside that same cell.
        int maxLocalX = template.getSize().getX() - 1;
        int maxLocalZ = template.getSize().getZ() - 1;
        int minFootprintWorldX = Integer.MAX_VALUE, minFootprintWorldZ = Integer.MAX_VALUE;
        for (int[] corner : new int[][]{{0, 0}, {maxLocalX, 0}, {0, maxLocalZ}, {maxLocalX, maxLocalZ}})
        {
            BlockPos rotatedCorner = StructureTemplate.transform(new BlockPos(corner[0], 0, corner[1]), Mirror.NONE, chosenRotation, BlockPos.ZERO);
            minFootprintWorldX = Math.min(minFootprintWorldX, worldOrigin.getX() + rotatedCorner.getX());
            minFootprintWorldZ = Math.min(minFootprintWorldZ, worldOrigin.getZ() + rotatedCorner.getZ());
        }

        // Unlike the "note and seal" loop below (which covers the candidate's OTHER connectors), this checks
        // the one actually being used for THIS attachment right now. That connector's status flips straight
        // to EXPANDED the moment this placement commits, so it never sits OPEN for the later sealing pass to
        // catch - if it's misaligned, nothing downstream ever notices or corrects it, and the room silently
        // pastes with its along-wall axis off the grid. Rejecting here just means "try a different
        // candidate/rotation for this connector", the same outcome as any other rejection below - it does NOT
        // reintroduce the earlier over-strict regression, since that was about rejecting a room over its
        // OTHER connectors' alignment, not the one actually in use here.
        boolean activeAlongWallIsX = requiredFront == Direction.NORTH || requiredFront == Direction.SOUTH;
        int activeCoord = activeAlongWallIsX ? minFootprintWorldX : minFootprintWorldZ;
        if (Math.floorMod(activeCoord, DungeonGraphSavedData.GRID_UNIT) != 0)
        {
            LOGGER.info("    rejected: footprint's minimum {} coordinate ({}) isn't grid-aligned along the wall axis for the connector being attached through",
                    activeAlongWallIsX ? "X" : "Z", activeCoord);
            return Optional.empty();
        }

        // The precise alignment invariant (not a heuristic): a size-GRID_UNIT room's footprint sits flush
        // against its neighbor only if the footprint's MINIMUM world coordinate on the axis running ALONG the
        // shared wall (not through it) is an exact multiple of GRID_UNIT - otherwise it isn't landing on a
        // cell boundary at all, and no footprint-cell-count heuristic can reliably catch every way that
        // produces (a straddling room can still coincidentally touch the "expected" number of cells - see
        // this session's bighallway long-wall debugging). The front-facing axis is expected to straddle by
        // exactly one column (the shared connector seam) and is deliberately not checked here.
        //
        // This has to be checked for EVERY connector the candidate has, not just the one being attached
        // through right now: a room's front-facing axis for THIS attachment drifts freely as a chain grows
        // (that's correct - it's what lets rooms sit flush regardless of how far along a corridor they are),
        // but for a room with connectors on BOTH axes (e.g. bighallway's north/south AND east/west), that
        // same drifting axis becomes the ALONG-WALL axis for its OTHER connectors. A placement that's valid
        // for the connector being used right now can still leave the room's other connectors unable to ever
        // find a valid future match, since that drifted axis won't land on a multiple of GRID_UNIT.
        //
        // Rejecting the WHOLE placement over this (an earlier version of this check did) turned out far too
        // costly: for a room like bighallway, requiring BOTH axes to independently align at once is a much
        // rarer coincidence than aligning just the one axis in use, and bighallway is the only room in the
        // pool with more than 2 connectors - reject it too often and the maze's frontier shrinks faster than
        // it grows, running dry after a couple dozen rooms instead of staying endless. Instead, still commit
        // to this placement (it's valid for the connector actually being used) and separately track which of
        // the OTHER connectors would be unusable, so the caller can seal just those specific ones immediately
        // with the doorway blocker rather than leaving them open to fail (and lag-spike retrying every
        // candidate) whenever a player eventually wanders up to them.
        Set<BlockPos> misalignedConnectorPositions = new HashSet<>();
        ConnectorScanner.TemplateConnectors allConnectors = ConnectorScanner.scan(level, templateId);
        for (ConnectorScanner.ConnectorInfo other : allConnectors.connectors())
        {
            Direction otherWorldFront = chosenRotation.rotate(other.front());
            boolean otherAlongWallIsX = otherWorldFront == Direction.NORTH || otherWorldFront == Direction.SOUTH;
            int otherCoord = otherAlongWallIsX ? minFootprintWorldX : minFootprintWorldZ;
            if (Math.floorMod(otherCoord, DungeonGraphSavedData.GRID_UNIT) != 0)
            {
                BlockPos otherRotatedPos = StructureTemplate.transform(other.localPos(), Mirror.NONE, chosenRotation, BlockPos.ZERO);
                BlockPos otherWorldPos = worldOrigin.offset(otherRotatedPos);
                LOGGER.info("    note: connector at {} (local {}, front={}) will be grid-misaligned ({}) - sealing it immediately",
                        otherWorldPos, other.localPos(), otherWorldFront, otherAlongWallIsX ? "X" : "Z");
                misalignedConnectorPositions.add(otherWorldPos);
            }
        }

        Set<Long> footprintCells = computeFootprintCells(template, worldOrigin, chosenRotation);
        LOGGER.info("    align: rotation={}, worldOrigin={}, footprintCells={}", chosenRotation, worldOrigin,
                footprintCells.stream().map(c -> "(" + (int) (long) c + "," + (int) (c >> 32) + ")").collect(Collectors.joining(",")));

        // Sanity check (see the plan's open risks): a correctly grid-aligned room can only ever legitimately
        // claim its own cell count plus exactly one shared seam cell (the connector it attached through) - if
        // the computed footprint needs more cells than that, some connector along this specific attachment
        // path isn't sitting at a grid-consistent offset (see DungeonGraphSavedData.GRID_UNIT's javadoc), and
        // the room would paste with its walls straddling a cell boundary instead of lining up cleanly.
        // Rejecting this outright turns a silent visual seam/gap into a clean "try a different candidate"
        // instead of corrupting the maze with a misaligned room.
        Vec3i size = template.getSize();
        int expectedOwnCells = ((size.getX() + DungeonGraphSavedData.GRID_UNIT - 1) / DungeonGraphSavedData.GRID_UNIT)
                * ((size.getZ() + DungeonGraphSavedData.GRID_UNIT - 1) / DungeonGraphSavedData.GRID_UNIT);
        if (footprintCells.size() > expectedOwnCells + 1)
        {
            LOGGER.info("    rejected: footprint claims {} cells but a well-aligned room here should need at most {}",
                    footprintCells.size(), expectedOwnCells + 1);
            return Optional.empty();
        }

        // A count check alone still lets through a diagonal pair of cells (e.g. (-1,0) and (0,-1)) that
        // happens to total the right number but doesn't form a flush rectangle - that's exactly the kind of
        // placement that "fits" by the count check yet pastes with a visible stair-step seam, since the room
        // isn't actually sharing a full face with its neighbor. A well-aligned footprint's cells always fill
        // their own bounding box completely; a diagonal or otherwise scattered set never does.
        int minCellX = Integer.MAX_VALUE, maxCellX = Integer.MIN_VALUE, minCellZ = Integer.MAX_VALUE, maxCellZ = Integer.MIN_VALUE;
        for (long cell : footprintCells)
        {
            int cx = (int) cell;
            int cz = (int) (cell >> 32);
            minCellX = Math.min(minCellX, cx);
            maxCellX = Math.max(maxCellX, cx);
            minCellZ = Math.min(minCellZ, cz);
            maxCellZ = Math.max(maxCellZ, cz);
        }
        long boundingBoxArea = (long) (maxCellX - minCellX + 1) * (maxCellZ - minCellZ + 1);
        if (boundingBoxArea != footprintCells.size())
        {
            LOGGER.info("    rejected: footprint's {} cells don't form a flush rectangle (bounding box needs {})",
                    footprintCells.size(), boundingBoxArea);
            return Optional.empty();
        }

        if (graph.anyCellOccupiedByOtherRoom(floorIndex, footprintCells, openConnector.ownerRoomId))
        {
            return Optional.empty();
        }

        ConnectorScanner.TemplateConnectors scanned = ConnectorScanner.scan(level, templateId);
        List<DungeonGraphSavedData.WorldConnector> worldConnectors = new ArrayList<>(scanned.connectors().size());
        for (ConnectorScanner.ConnectorInfo info : scanned.connectors())
        {
            BlockPos rotatedPos = StructureTemplate.transform(info.localPos(), Mirror.NONE, chosenRotation, BlockPos.ZERO);
            BlockPos worldPos = worldOrigin.offset(rotatedPos);
            Direction worldFront = chosenRotation.rotate(info.front());
            Direction worldTop = chosenRotation.rotate(info.top());
            boolean isDescend = info.name().equals(DungeonGraphSavedData.DESCEND_MARKER_NAME);
            worldConnectors.add(new DungeonGraphSavedData.WorldConnector(worldPos, worldFront, worldTop, info.joint(), isDescend));
        }

        return Optional.of(new AlignedPlacement(worldOrigin, chosenRotation, footprintCells, worldConnectors, misalignedConnectorPositions));
    }

    /** Every grid cell the room's rotated X/Z footprint covers, computed by transforming all four footprint
     * corners (not just the origin) - this stays correct regardless of which way rotation happens to push
     * local coordinates, rather than assuming a particular corner stays the "min" one. */
    static Set<Long> computeFootprintCells(StructureTemplate template, BlockPos worldOrigin, Rotation rotation)
    {
        Vec3i size = template.getSize();
        int maxLocalX = size.getX() - 1;
        int maxLocalZ = size.getZ() - 1;

        int minWorldX = Integer.MAX_VALUE, maxWorldX = Integer.MIN_VALUE;
        int minWorldZ = Integer.MAX_VALUE, maxWorldZ = Integer.MIN_VALUE;
        int[][] corners = {{0, 0}, {maxLocalX, 0}, {0, maxLocalZ}, {maxLocalX, maxLocalZ}};
        for (int[] corner : corners)
        {
            BlockPos localCorner = new BlockPos(corner[0], 0, corner[1]);
            BlockPos rotatedCorner = StructureTemplate.transform(localCorner, Mirror.NONE, rotation, BlockPos.ZERO);
            int worldX = worldOrigin.getX() + rotatedCorner.getX();
            int worldZ = worldOrigin.getZ() + rotatedCorner.getZ();
            minWorldX = Math.min(minWorldX, worldX);
            maxWorldX = Math.max(maxWorldX, worldX);
            minWorldZ = Math.min(minWorldZ, worldZ);
            maxWorldZ = Math.max(maxWorldZ, worldZ);
        }

        int minCellX = DungeonGraphSavedData.cellOf(minWorldX);
        int maxCellX = DungeonGraphSavedData.cellOf(maxWorldX);
        int minCellZ = DungeonGraphSavedData.cellOf(minWorldZ);
        int maxCellZ = DungeonGraphSavedData.cellOf(maxWorldZ);

        Set<Long> cells = new HashSet<>();
        for (int cx = minCellX; cx <= maxCellX; cx++)
        {
            for (int cz = minCellZ; cz <= maxCellZ; cz++)
            {
                cells.add(DungeonGraphSavedData.packCell(cx, cz));
            }
        }
        return cells;
    }
}
