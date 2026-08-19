package com.arcanelens.worldgen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Tracks whether the Warped Hollow's one and only boss lair has already been pasted - unlike
 * BossArenaSavedData (one arena per castle_dungeon instance), there's only ever one Broken Vessel lair in
 * the whole shared dimension, so this is just a single boolean rather than a per-instance set.
 */
public class VesselLairSavedData extends SavedData
{
    private static final String DATA_NAME = "arcanelens_vessel_lair";

    private boolean lairPlaced;

    public static VesselLairSavedData load(CompoundTag tag)
    {
        VesselLairSavedData data = new VesselLairSavedData();
        data.lairPlaced = tag.getBoolean("LairPlaced");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag)
    {
        tag.putBoolean("LairPlaced", lairPlaced);
        return tag;
    }

    public static VesselLairSavedData get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(VesselLairSavedData::load, VesselLairSavedData::new, DATA_NAME);
    }

    /** True only the first time this is called (and records it as placed from then on). */
    public boolean tryClaim()
    {
        if (lairPlaced)
        {
            return false;
        }
        lairPlaced = true;
        setDirty();
        return true;
    }
}
