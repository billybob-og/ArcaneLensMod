package com.arcanelens.worldgen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Tracks whether the God Challenge Hub's fixed layout (the hub itself plus every built arena) has
 * already been pasted - a single world-wide layout, not per-player, same shape as VesselLairSavedData.
 */
public class GodHubSavedData extends SavedData
{
    private static final String DATA_NAME = "arcanelens_god_hub";

    private boolean placed;

    public static GodHubSavedData load(CompoundTag tag)
    {
        GodHubSavedData data = new GodHubSavedData();
        data.placed = tag.getBoolean("Placed");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag)
    {
        tag.putBoolean("Placed", placed);
        return tag;
    }

    public static GodHubSavedData get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(GodHubSavedData::load, GodHubSavedData::new, DATA_NAME);
    }

    /** True only the first time this is called (and records it as placed from then on). */
    public boolean tryClaim()
    {
        if (placed)
        {
            return false;
        }
        placed = true;
        setDirty();
        return true;
    }
}
