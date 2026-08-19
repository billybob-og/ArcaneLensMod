package com.arcanelens.entity.ai;

import com.arcanelens.entity.BrokenVesselEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class BrokenVesselRangedAttackGoal extends Goal
{
    private static final double MELEE_REACH = 3.5;
    private static final float RANGED_PREFERENCE_CHANCE = 0.5f;
    private static final int CAST_EFFECT_DELAY_TICKS = 8;

    private final BrokenVesselEntity mob;
    private int cooldown;
    private int castTicksRemaining = -1;
    private LivingEntity castTarget;

    public BrokenVesselRangedAttackGoal(BrokenVesselEntity mob)
    {
        this.mob = mob;
        // No flags - same reasoning as SleepingGodRangedAttackGoal: this only needs to fire an instantaneous
        // volley and look at the target once, not hold a persistent MOVE/LOOK claim that would permanently
        // lose out to the melee goal's near-constant eligibility.
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse()
    {
        if (cooldown > 0)
        {
            cooldown--;
            return false;
        }

        if (mob.getAction() != BrokenVesselEntity.Action.NONE)
        {
            return false;
        }

        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive())
        {
            return false;
        }

        boolean outOfMeleeRange = mob.distanceToSqr(target) > MELEE_REACH * MELEE_REACH;
        return outOfMeleeRange || mob.getRandom().nextFloat() < RANGED_PREFERENCE_CHANCE;
    }

    @Override
    public boolean canContinueToUse()
    {
        return castTicksRemaining >= 0;
    }

    @Override
    public void start()
    {
        LivingEntity target = mob.getTarget();
        if (target != null)
        {
            mob.getLookControl().setLookAt(target);
            castTarget = target;
            mob.playRangedAnimation();
            castTicksRemaining = CAST_EFFECT_DELAY_TICKS;
        }
        cooldown = mob.getPhase().rangedCooldownTicks();
    }

    @Override
    public void tick()
    {
        if (castTarget != null)
        {
            mob.getLookControl().setLookAt(castTarget);
        }

        if (castTicksRemaining > 0)
        {
            castTicksRemaining--;
            return;
        }

        if (castTicksRemaining == 0 && castTarget != null && castTarget.isAlive())
        {
            mob.performRangedAttack(castTarget);
        }
        castTicksRemaining = -1;
        castTarget = null;
    }
}
