package com.arcanelens.god;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.ArrayList;
import java.util.List;

/** Deception (one of the Theater altar's four combined mechanics) needs to react to sneak-attacks or
 * hitting a mob that isn't targeting the attacker - transient combat events, not persistent world
 * state, so this mirrors WarDeathTracker's shape exactly: one global LivingHurtEvent listener records
 * qualifying hits here, and TheaterFaithAltarBlockEntity's own periodic tick queries this list for
 * anything nearby since its last check. */
public class TheaterEventTracker
{
    private static final long MAX_AGE_TICKS = 6000;

    private static final List<DeceptionRecord> RECENT_DECEPTIONS = new ArrayList<>();

    public static void onLivingHurt(LivingHurtEvent event)
    {
        if (!(event.getSource().getEntity() instanceof Player attacker) || !(attacker.level() instanceof ServerLevel level))
        {
            return;
        }

        boolean sneakAttack = attacker.isShiftKeyDown();
        boolean hitUnawareMob = event.getEntity() instanceof Mob mob && mob.getTarget() != attacker;
        if (!sneakAttack && !hitUnawareMob)
        {
            return;
        }

        long now = level.getGameTime();
        RECENT_DECEPTIONS.add(new DeceptionRecord(level.dimension(), attacker.blockPosition(), now));
        RECENT_DECEPTIONS.removeIf(record -> now - record.tick() > MAX_AGE_TICKS);
    }

    public static int countNearbyDeceptionsSince(ServerLevel level, BlockPos pos, int radius, long sinceTick)
    {
        double radiusSq = (double) radius * radius;
        int count = 0;
        for (DeceptionRecord record : RECENT_DECEPTIONS)
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

    private record DeceptionRecord(ResourceKey<Level> dimension, BlockPos pos, long tick)
    {
    }
}
