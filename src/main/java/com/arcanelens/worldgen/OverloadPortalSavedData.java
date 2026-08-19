package com.arcanelens.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

/**
 * Lives on the Broken Vessel level specifically (see OverloadPortalSavedData.get - always called with the
 * Warped Hollow's own ServerLevel, regardless of which dimension a given Overload Core sits in), so every
 * ritual anywhere in the world shares one cell-assignment counter and one link table. Cell assignment
 * mirrors PocketDimensionSavedData.getOrAssignCell exactly (same 2048-block spacing/reasoning - no two
 * rituals' destination cells can ever have overlapping loaded chunks), except keyed by the ritual's origin
 * GlobalPos instead of a player UUID, since it's "one cell per Overload Core," not "one cell per player" -
 * many portals, one shared dimension, like the vanilla Nether, not many private copies like Pocket Dimension.
 */
public class OverloadPortalSavedData extends SavedData
{
    private static final String DATA_NAME = "arcanelens_overload_portals";

    // Same margin/reasoning as PocketDimensionPlacer.CELL_SPACING.
    private static final int CELL_SPACING = 2048;
    // Matches the Warped Hollow flat generator's own walkable surface height (bedrock=0, blackstone=1-48,
    // warped_nylium=49, first walkable air=50) - deepened alongside VesselLairPlacer's LAIR_ORIGIN for the
    // buried Vessel Hollow exploration content and the Sleeping God Relic Shrine.
    private static final int CELL_Y = 50;

    private final Map<GlobalPos, Integer> originCellIndex = new HashMap<>();
    private int nextCellIndex = 0;

    // Each portal tile position (either side, either dimension) maps to the anchor tile of its paired
    // frame - not necessarily a perfectly symmetric 1:1 tile mapping (a frame has 2 walkable tiles), just
    // "which single anchor point should I send this entity to." Populated with one entry per real tile.
    private final Map<GlobalPos, GlobalPos> portalLinks = new HashMap<>();

    public static OverloadPortalSavedData load(CompoundTag tag)
    {
        OverloadPortalSavedData data = new OverloadPortalSavedData();
        data.nextCellIndex = tag.getInt("NextCellIndex");
        for (Tag t : tag.getList("OriginCells", Tag.TAG_COMPOUND))
        {
            CompoundTag entry = (CompoundTag) t;
            data.originCellIndex.put(readGlobalPos(entry.getCompound("Origin")), entry.getInt("Cell"));
        }
        for (Tag t : tag.getList("Links", Tag.TAG_COMPOUND))
        {
            CompoundTag entry = (CompoundTag) t;
            data.portalLinks.put(readGlobalPos(entry.getCompound("From")), readGlobalPos(entry.getCompound("To")));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag)
    {
        tag.putInt("NextCellIndex", nextCellIndex);

        ListTag cellList = new ListTag();
        for (Map.Entry<GlobalPos, Integer> entry : originCellIndex.entrySet())
        {
            CompoundTag entryTag = new CompoundTag();
            entryTag.put("Origin", writeGlobalPos(entry.getKey()));
            entryTag.putInt("Cell", entry.getValue());
            cellList.add(entryTag);
        }
        tag.put("OriginCells", cellList);

        ListTag linkList = new ListTag();
        for (Map.Entry<GlobalPos, GlobalPos> entry : portalLinks.entrySet())
        {
            CompoundTag entryTag = new CompoundTag();
            entryTag.put("From", writeGlobalPos(entry.getKey()));
            entryTag.put("To", writeGlobalPos(entry.getValue()));
            linkList.add(entryTag);
        }
        tag.put("Links", linkList);
        return tag;
    }

    public static OverloadPortalSavedData get(ServerLevel brokenVesselLevel)
    {
        return brokenVesselLevel.getDataStorage().computeIfAbsent(OverloadPortalSavedData::load, OverloadPortalSavedData::new, DATA_NAME);
    }

    public int getOrAssignCell(GlobalPos origin)
    {
        Integer existing = originCellIndex.get(origin);
        if (existing != null)
        {
            return existing;
        }
        int assigned = nextCellIndex++;
        originCellIndex.put(origin, assigned);
        setDirty();
        return assigned;
    }

    /** Bottom-interior-tile world position of the given cell's Arrival Chamber frame - see
     * OverloadCoreBlockEntity's frame-building helper for the exact frame shape this anchors. */
    public BlockPos cellOrigin(int cellIndex)
    {
        return new BlockPos(cellIndex * CELL_SPACING, CELL_Y, 0);
    }

    public void linkTile(GlobalPos tilePos, GlobalPos destinationAnchor)
    {
        portalLinks.put(tilePos, destinationAnchor);
        setDirty();
    }

    @Nullable
    public GlobalPos getPairedDestination(GlobalPos tilePos)
    {
        return portalLinks.get(tilePos);
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
