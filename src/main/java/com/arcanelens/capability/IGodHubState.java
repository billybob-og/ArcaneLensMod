package com.arcanelens.capability;

/**
 * Server-only bookkeeping for where to send a player back to when they leave the God Challenge Hub via
 * the medallion - same shape and same reasoning as IPocketDimensionState (no UI, no sync packet, not
 * snapshotted across death/respawn), kept as its own capability rather than reusing
 * IPocketDimensionState's slot so a player who's inside their Pocket Dimension when they use the
 * medallion doesn't have that spell's own return position clobbered by the hub trip.
 */
public interface IGodHubState
{
    String getReturnDimension();

    double getReturnX();

    double getReturnY();

    double getReturnZ();

    float getReturnYaw();

    float getReturnPitch();

    void setReturnPosition(String dimension, double x, double y, double z, float yaw, float pitch);
}
