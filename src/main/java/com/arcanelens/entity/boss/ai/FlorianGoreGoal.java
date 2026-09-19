package com.arcanelens.entity.boss.ai;

import com.arcanelens.entity.boss.FlorianFighter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** His main melee attack - lowers the antlers through a short windup, then tosses the target upward on
 * release. Same state-machine shape as AureliaSpearThrustGoal (windup tracked by a local counter, not by
 * re-reading the synced action), shared by the boss and his Antler summon via FlorianFighter. */
public class FlorianGoreGoal<T extends Mob & FlorianFighter> extends Goal
{
    private static final double REACH = 4.0;
    // Public - FlorianBossModel eases the head-lowering pose over this same window, so the animation
    // can't drift out of sync with the AI.
    public static final int WINDUP_TICKS = 12;
    private static final double TOSS_VELOCITY = 0.6;

    private final T mob;
    private int cooldown;
    private int windupTicksRemaining = -1;

    public FlorianGoreGoal(T mob)
    {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse()
    {
        return mob.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse()
    {
        return mob.getTarget() != null;
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

        if (windupTicksRemaining > 0)
        {
            mob.getNavigation().stop();
            windupTicksRemaining--;
            if (windupTicksRemaining == 0 && mob.distanceTo(target) <= REACH)
            {
                mob.doHurtTarget(target);
                target.setDeltaMovement(target.getDeltaMovement().add(0.0, TOSS_VELOCITY, 0.0));
                target.hurtMarked = true;
            }
            return;
        }

        if (mob.getFlorianAction() != FlorianFighter.Action.NONE)
        {
            mob.getNavigation().stop();
            return;
        }

        if (mob.distanceTo(target) > REACH)
        {
            mob.getNavigation().moveTo(target, mob.approachSpeed());
            return;
        }

        if (cooldown > 0)
        {
            cooldown--;
            return;
        }

        mob.getNavigation().stop();
        mob.setFlorianAction(FlorianFighter.Action.GORE, WINDUP_TICKS);
        windupTicksRemaining = WINDUP_TICKS;
        cooldown = mob.goreCooldownTicks();
    }
}
