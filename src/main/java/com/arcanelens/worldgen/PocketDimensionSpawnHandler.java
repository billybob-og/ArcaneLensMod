package com.arcanelens.worldgen;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.living.MobSpawnEvent;

/**
 * The Pocket Dimension is a personal safe room - unlike ArenaSpawnHandler's bounding-box check, the whole
 * dimension counts as safe, so this just cancels every natural monster spawn in it unconditionally.
 */
public class PocketDimensionSpawnHandler
{
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event)
    {
        if (event.getLevel() instanceof ServerLevel serverLevel
                && serverLevel.dimension() == ModDimensions.POCKET_DIMENSION_KEY)
        {
            event.setSpawnCancelled(true);
        }
    }
}
