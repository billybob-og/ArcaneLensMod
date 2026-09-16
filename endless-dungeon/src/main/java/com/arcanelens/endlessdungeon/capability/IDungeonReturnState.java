package com.arcanelens.endlessdungeon.capability;

/**
 * Server-only bookkeeping for where to send a player back to when they leave the dungeon dimension through a
 * return portal - mirrors the base mod's IPocketDimensionState field-for-field. Set on the FORWARD trip
 * (whatever overworld portal the player walked into, regardless of which dungeon entrance they land at), read
 * on the RETURN trip - never displayed in any UI, no sync packet, no death/respawn snapshot needed (a player
 * who dies in the dungeon respawns at world spawn/bed per vanilla logic, so stale return data is harmless and
 * gets overwritten on next entry).
 */
public interface IDungeonReturnState
{
    String getReturnDimension();

    double getReturnX();

    double getReturnY();

    double getReturnZ();

    float getReturnYaw();

    float getReturnPitch();

    void setReturnPosition(String dimension, double x, double y, double z, float yaw, float pitch);
}
