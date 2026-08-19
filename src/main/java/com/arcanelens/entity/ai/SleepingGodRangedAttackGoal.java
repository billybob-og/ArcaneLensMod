package com.arcanelens.entity.ai;

import com.arcanelens.entity.SleepingGodEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class SleepingGodRangedAttackGoal extends Goal
{
    private static final double MELEE_REACH = 4.0;
    // Match SpellCast1's t=0.16667s pose (hands-raised point, scaled for its 1.75x playback speed in the
    // model) and SpellCast2's t=0.75s release point, out of their respective 0.5s / 0.91667s lengths.
    private static final int CAST_1_EFFECT_DELAY_TICKS = 2;
    private static final int CAST_2_EFFECT_DELAY_TICKS = 15;

    private final SleepingGodEntity mob;
    private int cooldown;
    private int castTicksRemaining = -1;
    private LivingEntity castTarget;
    private int chosenEffect;

    public SleepingGodRangedAttackGoal(SleepingGodEntity mob)
    {
        this.mob = mob;
        // No flags: this goal only needs to fire an instantaneous barrage and look at the target once, not hold
        // a persistent claim on MOVE/LOOK. The melee goal (priority 1, {MOVE, LOOK}) is continuously eligible
        // any time a target exists, so if this goal claimed LOOK too, the flag conflict would permanently block
        // it from ever starting.
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

        // Don't hijack an in-progress melee swing (or another cast) with a new one - each attack animation
        // should be allowed to finish before the next one starts.
        if (mob.getAction() != SleepingGodEntity.Action.NONE)
        {
            return false;
        }

        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive())
        {
            return false;
        }

        boolean outOfMeleeRange = mob.distanceToSqr(target) > MELEE_REACH * MELEE_REACH;
        return outOfMeleeRange || mob.getRandom().nextFloat() < mob.getPhase().rangedPreferenceChance();
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
            chosenEffect = mob.getRandom().nextInt(3);
            SleepingGodEntity.Action anim = mob.playRandomRangedAnimation();
            castTicksRemaining = anim == SleepingGodEntity.Action.RANGED_CAST_1 ? CAST_1_EFFECT_DELAY_TICKS : CAST_2_EFFECT_DELAY_TICKS;
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
            switch (chosenEffect)
            {
                case 0 -> mob.performRangedBarrage(castTarget);
                case 1 -> mob.performLightningStrike(castTarget);
                default -> mob.performArrowRain(castTarget);
            }
        }
        castTicksRemaining = -1;
        castTarget = null;
    }
}
