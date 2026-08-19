package com.arcanelens.worldgen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

/** Tracks whether the Hunger Tower has already been placed at (0, ground, 0) - a single world-wide
 * landmark, not a per-player thing, so this only ever needs to happen once regardless of how many players
 * cross the reveal threshold. */
public class HungerTowerSavedData extends SavedData
{
    private static final String DATA_NAME = "arcanelens_hunger_tower";

    private boolean placed;

    public static HungerTowerSavedData load(CompoundTag tag)
    {
        HungerTowerSavedData data = new HungerTowerSavedData();
        data.placed = tag.getBoolean("Placed");
        return data;
    }

    public boolean isPlaced()
    {
        return placed;
    }

    public void markPlaced()
    {
        placed = true;
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag)
    {
        tag.putBoolean("Placed", placed);
        return tag;
    }

    public static String dataName()
    {
        return DATA_NAME;
    }
}
