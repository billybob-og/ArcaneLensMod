package com.arcanelens.endlessdungeon.dungeon;

import com.arcanelens.endlessdungeon.EndlessDungeonMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * One shared, world-scoped graph of every placed room and connector across every floor - never raw blocks,
 * topology only (see the plan's Context section on why this is a single shared maze, not per-player state).
 * Each floor is an independent {@link FloorData}: its own rooms, connectors, and occupied-cell set, since a
 * "descend" connector on floor N starts an entirely separate graph on floor N+1, not a continuation of the
 * same one.
 *
 * <p>Connector {@link ConnectorStatus} is tri-state, not a boolean, specifically so a trigger can flip a
 * connector to CLAIMED_PENDING synchronously the instant it fires, before any (possibly multi-tick, see
 * IncrementalStructurePlacer) placement work starts - a boolean OPEN/not-OPEN can't represent "already
 * claimed but not finished yet," which is exactly the window a second nearby player could otherwise double-
 * trigger the same connector in.</p>
 *
 * <p>The frontier (OPEN connectors) is kept as a live in-memory index per floor for fast proximity checks,
 * rebuilt from the persisted connector list's OPEN entries on load - see {@link #load(CompoundTag)} - rather
 * than trusted to survive as its own serialized list.</p>
 */
public class DungeonGraphSavedData extends SavedData
{
    private static final String DATA_NAME = EndlessDungeonMod.MODID + "_dungeon_graph";

    /** Grid unit every room footprint's X/Z size (and every connector's wall offset) must be an exact
     * multiple of - see the plan's Context section for why (loop-closure alignment). */
    public static final int GRID_UNIT = 8;

    /** Jigsaw `name` value marking a floor-descent connector rather than a real room-to-room connector on the
     * SAME floor - mirrors DungeonEntrancePlacer.PORTAL_MARKER_NAME's own convention exactly (a reserved name
     * string a room's jigsaw can use to opt into special handling instead of normal frontier expansion). A
     * connector tagged this way still gets registered normally via placeRoom (so it shows up in the graph,
     * gets saved, etc.) but DungeonFrontierTrigger's frontier scan checks ConnectorState.isDescend before
     * ever calling the normal tryExpand candidate search, routing to tryDescend instead. */
    public static final ResourceLocation DESCEND_MARKER_NAME = new ResourceLocation(EndlessDungeonMod.MODID, "descend");

    public enum ConnectorStatus
    {
        OPEN, CLAIMED_PENDING, EXPANDED
    }

    public record PlacedRoom(long id, ResourceLocation templateId, BlockPos worldOrigin, Rotation rotation, int floorIndex)
    {
    }

    /** A connector already transformed into world space by the alignment/placement step - as opposed to
     * {@link ConnectorScanner.ConnectorInfo}, which is template-local and has no rotation applied. topFacing
     * is carried alongside frontFacing (rather than re-derived later from the owner room's stored rotation)
     * specifically so ALIGNED-joint matching never needs a reverse local-connector lookup. isDescend mirrors
     * whether the source jigsaw's own `name` NBT field matched DESCEND_MARKER_NAME - see that constant's own
     * javadoc. */
    public record WorldConnector(BlockPos worldPos, Direction frontFacing, Direction topFacing, String jointType, boolean isDescend)
    {
    }

    public static final class ConnectorState
    {
        public final long ownerRoomId;
        public final BlockPos worldPos;
        public final Direction frontFacing;
        public final Direction topFacing;
        public final String jointType;
        public final boolean isDescend;
        private ConnectorStatus status;

        ConnectorState(long ownerRoomId, BlockPos worldPos, Direction frontFacing, Direction topFacing, String jointType, boolean isDescend, ConnectorStatus status)
        {
            this.ownerRoomId = ownerRoomId;
            this.worldPos = worldPos;
            this.frontFacing = frontFacing;
            this.topFacing = topFacing;
            this.jointType = jointType;
            this.isDescend = isDescend;
            this.status = status;
        }

        public ConnectorStatus status()
        {
            return status;
        }
    }

    private static final class FloorData
    {
        final int floorIndex;
        final int yOffset;
        long nextRoomId = 0;
        final Map<Long, PlacedRoom> rooms = new HashMap<>();
        final Map<BlockPos, ConnectorState> connectors = new HashMap<>();
        // packed grid cell -> the room id that reserved it. A Map, not a Set, specifically so collision
        // checks can exclude one particular room's own cells (see anyCellOccupiedByOtherRoom) - two attached
        // rooms' footprints are expected to share exactly one boundary column/row at their connector (their
        // jigsaw blocks coincide at the same world position, same as vanilla jigsaw), so that shared cell
        // must not itself count as a collision when the room on the OTHER side of that exact connector is
        // the one being placed.
        final Map<Long, Long> occupiedCells = new HashMap<>();
        final Map<BlockPos, ConnectorState> frontier = new HashMap<>();

        FloorData(int floorIndex, int yOffset)
        {
            this.floorIndex = floorIndex;
            this.yOffset = yOffset;
        }
    }

    private final Map<Integer, FloorData> floors = new HashMap<>();

    // Entrance-pool tracking for the overworld portal system (see Phase C of the plan) - global, not
    // per-floor, since every portal-spawned entrance always seeds floor 0 specifically.
    private final Set<GlobalPos> spawnedFromPortals = new HashSet<>();
    private final List<BlockPos> entrancePortalLandingPositions = new ArrayList<>();
    private int nextEntranceSlot = 0;

    public static DungeonGraphSavedData load(CompoundTag tag)
    {
        DungeonGraphSavedData data = new DungeonGraphSavedData();
        data.nextEntranceSlot = tag.getInt("NextEntranceSlot");
        for (Tag portalTagRaw : tag.getList("SpawnedFromPortals", Tag.TAG_COMPOUND))
        {
            data.spawnedFromPortals.add(readGlobalPos((CompoundTag) portalTagRaw));
        }
        for (Tag landingTagRaw : tag.getList("EntrancePortalLandings", Tag.TAG_COMPOUND))
        {
            CompoundTag landingTag = (CompoundTag) landingTagRaw;
            data.entrancePortalLandingPositions.add(
                    new BlockPos(landingTag.getInt("X"), landingTag.getInt("Y"), landingTag.getInt("Z")));
        }

        for (Tag floorTagRaw : tag.getList("Floors", Tag.TAG_COMPOUND))
        {
            CompoundTag floorTag = (CompoundTag) floorTagRaw;
            int floorIndex = floorTag.getInt("FloorIndex");
            int yOffset = floorTag.getInt("YOffset");
            FloorData floor = new FloorData(floorIndex, yOffset);
            floor.nextRoomId = floorTag.getLong("NextRoomId");

            for (Tag roomTagRaw : floorTag.getList("Rooms", Tag.TAG_COMPOUND))
            {
                CompoundTag roomTag = (CompoundTag) roomTagRaw;
                long id = roomTag.getLong("Id");
                ResourceLocation templateId = new ResourceLocation(roomTag.getString("TemplateId"));
                BlockPos origin = new BlockPos(roomTag.getInt("X"), roomTag.getInt("Y"), roomTag.getInt("Z"));
                Rotation rotation = Rotation.valueOf(roomTag.getString("Rotation"));
                floor.rooms.put(id, new PlacedRoom(id, templateId, origin, rotation, floorIndex));
            }

            for (Tag connectorTagRaw : floorTag.getList("Connectors", Tag.TAG_COMPOUND))
            {
                CompoundTag connectorTag = (CompoundTag) connectorTagRaw;
                long ownerRoomId = connectorTag.getLong("OwnerRoomId");
                BlockPos worldPos = new BlockPos(connectorTag.getInt("X"), connectorTag.getInt("Y"), connectorTag.getInt("Z"));
                Direction front = Direction.byName(connectorTag.getString("Front"));
                Direction top = Direction.byName(connectorTag.getString("Top"));
                String jointType = connectorTag.getString("Joint");
                boolean isDescend = connectorTag.getBoolean("IsDescend");
                ConnectorStatus status = ConnectorStatus.valueOf(connectorTag.getString("Status"));
                ConnectorState state = new ConnectorState(ownerRoomId, worldPos, front, top, jointType, isDescend, status);
                floor.connectors.put(worldPos, state);
                if (status == ConnectorStatus.OPEN)
                {
                    floor.frontier.put(worldPos, state);
                }
            }

            for (Tag cellTagRaw : floorTag.getList("OccupiedCells", Tag.TAG_COMPOUND))
            {
                CompoundTag cellTag = (CompoundTag) cellTagRaw;
                floor.occupiedCells.put(cellTag.getLong("Cell"), cellTag.getLong("RoomId"));
            }

            data.floors.put(floorIndex, floor);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag)
    {
        tag.putInt("NextEntranceSlot", nextEntranceSlot);

        ListTag portalList = new ListTag();
        for (GlobalPos portalPos : spawnedFromPortals)
        {
            portalList.add(writeGlobalPos(portalPos));
        }
        tag.put("SpawnedFromPortals", portalList);

        ListTag landingList = new ListTag();
        for (BlockPos landing : entrancePortalLandingPositions)
        {
            CompoundTag landingTag = new CompoundTag();
            landingTag.putInt("X", landing.getX());
            landingTag.putInt("Y", landing.getY());
            landingTag.putInt("Z", landing.getZ());
            landingList.add(landingTag);
        }
        tag.put("EntrancePortalLandings", landingList);

        ListTag floorList = new ListTag();
        for (FloorData floor : floors.values())
        {
            CompoundTag floorTag = new CompoundTag();
            floorTag.putInt("FloorIndex", floor.floorIndex);
            floorTag.putInt("YOffset", floor.yOffset);
            floorTag.putLong("NextRoomId", floor.nextRoomId);

            ListTag roomList = new ListTag();
            for (PlacedRoom room : floor.rooms.values())
            {
                CompoundTag roomTag = new CompoundTag();
                roomTag.putLong("Id", room.id());
                roomTag.putString("TemplateId", room.templateId().toString());
                roomTag.putInt("X", room.worldOrigin().getX());
                roomTag.putInt("Y", room.worldOrigin().getY());
                roomTag.putInt("Z", room.worldOrigin().getZ());
                roomTag.putString("Rotation", room.rotation().name());
                roomList.add(roomTag);
            }
            floorTag.put("Rooms", roomList);

            ListTag connectorList = new ListTag();
            for (ConnectorState connector : floor.connectors.values())
            {
                CompoundTag connectorTag = new CompoundTag();
                connectorTag.putLong("OwnerRoomId", connector.ownerRoomId);
                connectorTag.putInt("X", connector.worldPos.getX());
                connectorTag.putInt("Y", connector.worldPos.getY());
                connectorTag.putInt("Z", connector.worldPos.getZ());
                connectorTag.putString("Front", connector.frontFacing.getSerializedName());
                connectorTag.putString("Top", connector.topFacing.getSerializedName());
                connectorTag.putString("Joint", connector.jointType);
                connectorTag.putBoolean("IsDescend", connector.isDescend);
                connectorTag.putString("Status", connector.status.name());
                connectorList.add(connectorTag);
            }
            floorTag.put("Connectors", connectorList);

            ListTag cellList = new ListTag();
            for (Map.Entry<Long, Long> entry : floor.occupiedCells.entrySet())
            {
                CompoundTag cellTag = new CompoundTag();
                cellTag.putLong("Cell", entry.getKey());
                cellTag.putLong("RoomId", entry.getValue());
                cellList.add(cellTag);
            }
            floorTag.put("OccupiedCells", cellList);

            floorList.add(floorTag);
        }
        tag.put("Floors", floorList);
        return tag;
    }

    public static DungeonGraphSavedData get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(DungeonGraphSavedData::load, DungeonGraphSavedData::new, DATA_NAME);
    }

    /** Packs a grid-cell coordinate pair into one long key for the occupied-cell set. */
    public static long packCell(int cellX, int cellZ)
    {
        return ((long) cellX & 0xFFFFFFFFL) | ((long) cellZ << 32);
    }

    public static int cellOf(int worldCoord)
    {
        return Math.floorDiv(worldCoord, GRID_UNIT);
    }

    public boolean floorExists(int floorIndex)
    {
        return floors.containsKey(floorIndex);
    }

    public void ensureFloor(int floorIndex, int yOffset)
    {
        floors.computeIfAbsent(floorIndex, i -> new FloorData(i, yOffset));
    }

    /** Reserves `cells` on `floorIndex` as occupied by a room that doesn't actually live on this floor - used
     * for a floor-descent room (e.g. lv1drop): it's registered as a floor-N room, but physically extends down
     * into floor N+1's own Y-band at the SAME (X,Z), and occupiedCells is tracked independently per floor, so
     * without this floor N+1 has no idea that cell is already filled by the shaft's own lower half - its own
     * organic growth can place a room straight through it (confirmed in playtesting: floor-N+1 rooms pasting
     * through the descent room's bottom). Uses the sentinel owner id -1 rather than a real room id, since room
     * ids are numbered independently per floor and a real id here could otherwise collide with an unrelated
     * room that happens to get the same id on THIS floor - -1 already means "no real room" everywhere
     * anyCellOccupiedByOtherRoom is called with nothing of its own to exclude, so it can never match a real
     * exclude id and will always correctly read as "occupied by something else". */
    public void reserveForeignCells(int floorIndex, int yOffset, Collection<Long> cells)
    {
        ensureFloor(floorIndex, yOffset);
        FloorData floor = floors.get(floorIndex);
        for (long cell : cells)
        {
            floor.occupiedCells.putIfAbsent(cell, -1L);
        }
        setDirty();
    }

    @Nullable
    public Integer getFloorYOffset(int floorIndex)
    {
        FloorData floor = floors.get(floorIndex);
        return floor == null ? null : floor.yOffset;
    }

    public Set<Integer> getFloorIndices()
    {
        return Set.copyOf(floors.keySet());
    }

    public boolean isCellOccupied(int floorIndex, int cellX, int cellZ)
    {
        FloorData floor = floors.get(floorIndex);
        return floor != null && floor.occupiedCells.containsKey(packCell(cellX, cellZ));
    }

    /** True if any of packedCells is already reserved by a room OTHER than excludeRoomId. excludeRoomId is
     * always the connector's owner room being attached to - two connected rooms are expected to share
     * exactly the one boundary cell their jigsaw blocks coincide at (see the Map<Long,Long> javadoc on
     * occupiedCells above), so that shared cell must not itself register as a collision. Genuine collisions
     * (a totally unrelated room already sitting where this candidate would land, e.g. a loop closing back on
     * itself) still correctly reject the placement. */
    public boolean anyCellOccupiedByOtherRoom(int floorIndex, Collection<Long> packedCells, long excludeRoomId)
    {
        FloorData floor = floors.get(floorIndex);
        if (floor == null)
        {
            return false;
        }
        for (long cell : packedCells)
        {
            Long owner = floor.occupiedCells.get(cell);
            com.mojang.logging.LogUtils.getLogger().info("      cell ({},{}) packed={} owner={} exclude={}",
                    (int) cell, (int) (cell >> 32), cell, owner, excludeRoomId);
            if (owner != null && owner != excludeRoomId)
            {
                return true;
            }
        }
        return false;
    }

    /** Places a room's topology record, reserves its footprint's grid cells, and registers its connectors -
     * one atomic call so a room's cells/connectors can never exist half-committed. Returns the new room's id. */
    public long placeRoom(int floorIndex, int yOffset, ResourceLocation templateId, BlockPos worldOrigin, Rotation rotation,
                           Collection<Long> footprintCells, List<WorldConnector> worldConnectors)
    {
        ensureFloor(floorIndex, yOffset);
        FloorData floor = floors.get(floorIndex);

        long roomId = floor.nextRoomId++;
        floor.rooms.put(roomId, new PlacedRoom(roomId, templateId, worldOrigin, rotation, floorIndex));
        // putIfAbsent, not put: a footprint cell shared with the room being attached to (see
        // anyCellOccupiedByOtherRoom) stays owned by whichever room reserved it first, rather than being
        // silently reassigned to this new room.
        com.mojang.logging.LogUtils.getLogger().info("placeRoom: id={} template={} origin={} rotation={} cells={}",
                roomId, templateId, worldOrigin, rotation, footprintCells);
        for (long cell : footprintCells)
        {
            floor.occupiedCells.putIfAbsent(cell, roomId);
        }

        for (WorldConnector info : worldConnectors)
        {
            // Never overwrite an existing entry here: with the grid being discrete and finite, an unrelated
            // room placed elsewhere in a dense maze can coincidentally compute one of ITS OWN connectors to
            // land at the exact same world position as an already-placed (possibly already EXPANDED, i.e.
            // genuinely connected) connector belonging to a completely different room. Overwriting that
            // entry - which an unconditional put() used to do here - silently corrupts or re-opens an
            // already-working connection; skipping registration when the position is already taken leaves
            // whatever's already there untouched, which is always the correct call since a shared world
            // position by definition already has SOME real connector tracked at it.
            if (floor.connectors.containsKey(info.worldPos()))
            {
                continue;
            }
            ConnectorState state = new ConnectorState(roomId, info.worldPos(), info.frontFacing(), info.topFacing(), info.jointType(), info.isDescend(), ConnectorStatus.OPEN);
            floor.connectors.put(info.worldPos(), state);
            floor.frontier.put(info.worldPos(), state);
        }

        setDirty();
        return roomId;
    }

    /** Registers a single connector directly (used for the connector on the SIDE the new room was attached
     * from - that one starts EXPANDED, not OPEN, since it's already satisfied by the room that just claimed
     * it). Split out from placeRoom so callers can mark exactly one connector differently from the rest. */
    public void setConnectorStatus(int floorIndex, BlockPos worldPos, ConnectorStatus newStatus)
    {
        FloorData floor = floors.get(floorIndex);
        if (floor == null)
        {
            return;
        }
        ConnectorState state = floor.connectors.get(worldPos);
        if (state == null)
        {
            return;
        }
        state.status = newStatus;
        if (newStatus == ConnectorStatus.OPEN)
        {
            floor.frontier.put(worldPos, state);
        }
        else
        {
            floor.frontier.remove(worldPos);
        }
        setDirty();
    }

    @Nullable
    public ConnectorState getConnector(int floorIndex, BlockPos worldPos)
    {
        FloorData floor = floors.get(floorIndex);
        return floor == null ? null : floor.connectors.get(worldPos);
    }

    @Nullable
    public PlacedRoom getRoom(int floorIndex, long roomId)
    {
        FloorData floor = floors.get(floorIndex);
        return floor == null ? null : floor.rooms.get(roomId);
    }

    /** Read-only snapshot of the currently-open connectors on a floor, for the frontier trigger's proximity
     * scan. A copy, not a live view - the trigger may mutate statuses (via setConnectorStatus) while
     * iterating a scan result from the same tick. */
    public List<ConnectorState> getFrontierSnapshot(int floorIndex)
    {
        FloorData floor = floors.get(floorIndex);
        return floor == null ? Collections.emptyList() : new ArrayList<>(floor.frontier.values());
    }

    // --- Entrance-pool tracking (Phase C: overworld portal entry) ---

    public boolean hasSpawnedEntranceFor(GlobalPos portalPos)
    {
        return spawnedFromPortals.contains(portalPos);
    }

    public void markSpawnedFor(GlobalPos portalPos)
    {
        spawnedFromPortals.add(portalPos);
        setDirty();
    }

    public void registerEntranceLanding(BlockPos landingPos)
    {
        entrancePortalLandingPositions.add(landingPos);
        setDirty();
    }

    /** Null only if the pool is completely empty - callers must have registered at least one landing (e.g.
     * via the safety-net DungeonEntrancePlacer.ensureAtLeastOneEntranceExists) before this is ever called. */
    @Nullable
    public BlockPos pickRandomEntranceLanding(RandomSource random)
    {
        if (entrancePortalLandingPositions.isEmpty())
        {
            return null;
        }
        return entrancePortalLandingPositions.get(random.nextInt(entrancePortalLandingPositions.size()));
    }

    public boolean hasAnyEntranceLanding()
    {
        return !entrancePortalLandingPositions.isEmpty();
    }

    /** Monotonic slot index driving new-entrance placement - see DungeonEntrancePlacer for how this becomes
     * a grid-aligned origin far outside the organically-grown maze's reach. */
    public int claimNextEntranceSlot()
    {
        int slot = nextEntranceSlot++;
        setDirty();
        return slot;
    }

    private static CompoundTag writeGlobalPos(GlobalPos pos)
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("Dim", pos.dimension().location().toString());
        tag.putInt("X", pos.pos().getX());
        tag.putInt("Y", pos.pos().getY());
        tag.putInt("Z", pos.pos().getZ());
        return tag;
    }

    private static GlobalPos readGlobalPos(CompoundTag tag)
    {
        ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString("Dim")));
        BlockPos pos = new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
        return GlobalPos.of(dim, pos);
    }
}
