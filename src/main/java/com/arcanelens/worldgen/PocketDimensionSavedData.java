package com.arcanelens.worldgen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Assigns each player a permanent, fixed cell (a coordinate offset) within the shared Pocket Dimension level,
 * and tracks which room size has actually been pasted there so PocketDimensionPlacer only repastes when the
 * player's Pocket Dimension Expansion skill level has actually increased.
 */
public class PocketDimensionSavedData extends SavedData
{
    private static final String DATA_NAME = "arcanelens_pocket_dimension";

    private final Map<UUID, Integer> playerCellIndex = new HashMap<>();
    private final Map<UUID, Integer> placedRoomSize = new HashMap<>();
    private int nextCellIndex = 0;

    public static PocketDimensionSavedData load(CompoundTag tag)
    {
        PocketDimensionSavedData data = new PocketDimensionSavedData();
        data.nextCellIndex = tag.getInt("NextCellIndex");
        for (Tag entryTag : tag.getList("Players", Tag.TAG_COMPOUND))
        {
            CompoundTag entry = (CompoundTag) entryTag;
            UUID uuid = entry.getUUID("Uuid");
            data.playerCellIndex.put(uuid, entry.getInt("Cell"));
            data.placedRoomSize.put(uuid, entry.getInt("Size"));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag)
    {
        tag.putInt("NextCellIndex", nextCellIndex);
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Integer> entry : playerCellIndex.entrySet())
        {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("Uuid", entry.getKey());
            entryTag.putInt("Cell", entry.getValue());
            entryTag.putInt("Size", placedRoomSize.getOrDefault(entry.getKey(), -1));
            list.add(entryTag);
        }
        tag.put("Players", list);
        return tag;
    }

    public static PocketDimensionSavedData get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(PocketDimensionSavedData::load, PocketDimensionSavedData::new, DATA_NAME);
    }

    public int getOrAssignCell(UUID uuid)
    {
        Integer existing = playerCellIndex.get(uuid);
        if (existing != null)
        {
            return existing;
        }
        int assigned = nextCellIndex++;
        playerCellIndex.put(uuid, assigned);
        setDirty();
        return assigned;
    }

    public int getPlacedRoomSize(UUID uuid)
    {
        return placedRoomSize.getOrDefault(uuid, -1);
    }

    public void setPlacedRoomSize(UUID uuid, int size)
    {
        placedRoomSize.put(uuid, size);
        setDirty();
    }
}
