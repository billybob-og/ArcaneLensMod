package com.arcanelens.capability;

/**
 * Server-only bookkeeping for where to send a player back to when they leave the Pocket Dimension - never
 * displayed in any UI, so (unlike IFaith/ISkillTree/IKnownSpells) this capability has no sync packet and
 * isn't snapshotted across death/respawn: if a player dies inside the Pocket Dimension they respawn at world
 * spawn/bed per vanilla logic anyway, so stale return data is harmless and gets overwritten on next entry.
 */
public interface IPocketDimensionState
{
    String getReturnDimension();

    double getReturnX();

    double getReturnY();

    double getReturnZ();

    float getReturnYaw();

    float getReturnPitch();

    void setReturnPosition(String dimension, double x, double y, double z, float yaw, float pitch);
}
