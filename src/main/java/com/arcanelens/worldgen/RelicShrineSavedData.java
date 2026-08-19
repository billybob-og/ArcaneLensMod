package com.arcanelens.worldgen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Tracks whether the Warped Hollow's Sleeping God Relic Shrine has already been placed - exactly one
 * shrine in the whole shared dimension, same "single boolean, not per-instance" shape as
 * VesselLairSavedData.
 */
public class RelicShrineSavedData extends SavedData
{
    private static final String DATA_NAME = "arcanelens_relic_shrine";

    private boolean shrinePlaced;

    public static RelicShrineSavedData load(CompoundTag tag)
    {
        RelicShrineSavedData data = new RelicShrineSavedData();
        data.shrinePlaced = tag.getBoolean("ShrinePlaced");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag)
    {
        tag.putBoolean("ShrinePlaced", shrinePlaced);
        return tag;
    }

    public static RelicShrineSavedData get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(RelicShrineSavedData::load, RelicShrineSavedData::new, DATA_NAME);
    }

    /** True only the first time this is called (and records it as placed from then on). */
    public boolean tryClaim()
    {
        if (shrinePlaced)
        {
            return false;
        }
        shrinePlaced = true;
        setDirty();
        return true;
    }
}
