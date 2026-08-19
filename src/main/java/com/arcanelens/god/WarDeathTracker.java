package com.arcanelens.god;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.ArrayList;
import java.util.List;

/** War's altar mechanic needs to react to deaths, which are transient events, not persistent world
 * state the way Fertility's nature blocks are - so instead of each altar scanning the world directly,
 * one global LivingDeathEvent listener records every death here, and each WarFaithAltarBlockEntity's
 * own periodic tick queries this list for anything nearby since its own last check. Old entries are
 * pruned on every record so this never grows unbounded. */
public class WarDeathTracker
{
    private static final long MAX_AGE_TICKS = 6000;

    private static final List<DeathRecord> RECENT_DEATHS = new ArrayList<>();

    public static void onLivingDeath(LivingDeathEvent event)
    {
        if (!(event.getEntity().level() instanceof ServerLevel level))
        {
            return;
        }

        long now = level.getGameTime();
        RECENT_DEATHS.add(new DeathRecord(level.dimension(), event.getEntity().blockPosition(), now));
        RECENT_DEATHS.removeIf(record -> now - record.tick() > MAX_AGE_TICKS);
    }

    /** Counts deaths in `level` within `radius` of `pos`, recorded strictly after `sinceTick` - callers
     * should pass their own last-checked tick and store the level's current game time for next time. */
    public static int countNearbyDeathsSince(ServerLevel level, BlockPos pos, int radius, long sinceTick)
    {
        double radiusSq = (double) radius * radius;
        int count = 0;
        for (DeathRecord record : RECENT_DEATHS)
        {
            if (record.tick() <= sinceTick || !record.dimension().equals(level.dimension()))
            {
                continue;
            }
            if (record.pos().distSqr(pos) <= radiusSq)
            {
                count++;
            }
        }
        return count;
    }

    private record DeathRecord(ResourceKey<Level> dimension, BlockPos pos, long tick)
    {
    }
}
