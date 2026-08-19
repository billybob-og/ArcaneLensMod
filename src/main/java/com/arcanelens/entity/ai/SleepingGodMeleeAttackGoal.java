package com.arcanelens.entity.ai;

import com.arcanelens.entity.SleepingGodEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class SleepingGodMeleeAttackGoal extends Goal
{
    private static final double ATTACK_REACH = 4.0;
    // Matches MaleSwing's t=0.5s pose (the arm's fully-raised strike point, out of a 0.75s swing).
    private static final int HIT_DELAY_TICKS = 10;

    private final SleepingGodEntity mob;
    private int cooldown;
    private int windupTicks = -1;

    public SleepingGodMeleeAttackGoal(SleepingGodEntity mob)
    {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse()
    {
        return mob.getTarget() != null && mob.getTarget().isAlive();
    }

    @Override
    public boolean canContinueToUse()
    {
        return canUse();
    }

    @Override
    public void tick()
    {
        LivingEntity target = mob.getTarget();
        if (target == null)
        {
            return;
        }

        mob.getLookControl().setLookAt(target);

        // Any attack animation (this swing's own windup, OR a ranged cast) should plant the mob's feet -
        // otherwise the navigation keeps driving it toward the target while the arms/legs play a pose that
        // was authored assuming it stands still, which looks like it's sliding/moonwalking mid-animation.
        boolean busy = mob.getAction() != SleepingGodEntity.Action.NONE;
        if (!mob.isFlyingPhase())
        {
            if (busy)
            {
                mob.getNavigation().stop();
            }
            else
            {
                mob.getNavigation().moveTo(target, mob.getPhase().moveSpeed());
            }
        }

        if (windupTicks >= 0)
        {
            windupTicks--;
            if (windupTicks == 0)
            {
                if (mob.distanceToSqr(target) <= ATTACK_REACH * ATTACK_REACH)
                {
                    mob.doHurtTarget(target);
                }
                windupTicks = -1;
                cooldown = mob.getPhase().meleeCooldownTicks();
            }
            return;
        }

        if (cooldown > 0)
        {
            cooldown--;
            return;
        }

        double distSqr = mob.distanceToSqr(target);
        if (distSqr <= ATTACK_REACH * ATTACK_REACH && !busy)
        {
            mob.playMeleeAnimation();
            windupTicks = HIT_DELAY_TICKS;
        }
    }
}
