package com.arcanelens.entity.ai;

import com.arcanelens.entity.BrokenVesselEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class BrokenVesselMeleeAttackGoal extends Goal
{
    private static final double ATTACK_REACH = 3.5;
    private static final int HIT_DELAY_TICKS = 8;

    private final BrokenVesselEntity mob;
    private int cooldown;
    private int windupTicks = -1;

    public BrokenVesselMeleeAttackGoal(BrokenVesselEntity mob)
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

        // Same reasoning as SleepingGodMeleeAttackGoal: any attack animation (this swing's windup, a ranged
        // cast, or the eruption telegraph) should plant the mob's feet rather than let navigation keep
        // driving it toward the target mid-pose.
        boolean busy = mob.getAction() != BrokenVesselEntity.Action.NONE;
        if (busy)
        {
            mob.getNavigation().stop();
        }
        else
        {
            mob.getNavigation().moveTo(target, mob.getPhase().moveSpeed());
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
