package com.arcanelens.god;

import com.arcanelens.entity.boss.AbstractGodBossEntity;
import com.arcanelens.worldgen.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

/** If a challenger dies in the God Challenge Hub, their fight is over - clears the arena's boss so it
 * doesn't sit there at whatever health it had (and so the next challenge spawns a fresh one instead of
 * stacking a second boss on top of it). Scoped to the bosses near the dead player, not every boss in
 * the dimension, so someone else's fight in a different arena isn't cut short. */
public class GodBossResetHandler
{
    private static final double ARENA_RADIUS = 24.0;

    public static void onLivingDeath(LivingDeathEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)
                || level.dimension() != ModDimensions.GOD_CHALLENGE_HUB_KEY)
        {
            return;
        }

        level.getEntitiesOfClass(AbstractGodBossEntity.class, player.getBoundingBox().inflate(ARENA_RADIUS))
                .forEach(AbstractGodBossEntity::discard);
    }
}
