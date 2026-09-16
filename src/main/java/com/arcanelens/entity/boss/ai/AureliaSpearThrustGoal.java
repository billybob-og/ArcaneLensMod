package com.arcanelens.entity.boss.ai;

import com.arcanelens.entity.boss.AureliaBossEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Her main melee attack - a longer-reach, longer-windup thrust than a normal punch (the spear gives
 * her reach). canUse()/canContinueToUse() just require a live target - all the actual state machine
 * (windup, busy-waiting, cooldown) lives in tick(), same shape as SleepingGodMeleeAttackGoal's own
 * (read-only precedent, not shared code). Shares AureliaBossEntity.Action with AureliaChargeGoal at the
 * same goal priority so the two mutually exclude via that synced field, not via Goal flag arbitration -
 * exact reasoning as Sleeping God's melee+ranged goals. */
public class AureliaSpearThrustGoal extends Goal
{
    private static final double REACH = 5.0;
    public static final int WINDUP_TICKS = 8;

    private final AureliaBossEntity mob;
    private int cooldown;
    private int windupTicksRemaining = -1;

    public AureliaSpearThrustGoal(AureliaBossEntity mob)
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

        // Windup completion is tracked purely by this local counter, NOT by re-checking
        // mob.getAction() == THRUST - AbstractGodBossEntity's own auto-clear resets the shared Action
        // field back to NONE once its timer expires, which could easily race ahead of this check on the
        // exact tick the windup finishes (depending on tick-order between customServerAiStep and goal
        // ticking), silently skipping the hit. windupTicksRemaining alone is authoritative for whether
        // THIS goal still considers itself mid-windup.
        if (windupTicksRemaining > 0)
        {
            mob.getNavigation().stop();
            windupTicksRemaining--;
            if (windupTicksRemaining == 0 && mob.distanceTo(target) <= REACH)
            {
                mob.doHurtTarget(target);
            }
            return;
        }

        if (mob.getAction() != AureliaBossEntity.Action.NONE)
        {
            // Some other action (currently: a charge) is busy - don't start a new thrust over it.
            mob.getNavigation().stop();
            return;
        }

        double distance = mob.distanceTo(target);
        if (distance > REACH)
        {
            mob.getNavigation().moveTo(target, mob.getPhase().moveSpeed());
            return;
        }

        if (cooldown > 0)
        {
            cooldown--;
            return;
        }

        mob.getNavigation().stop();
        mob.setAction(AureliaBossEntity.Action.THRUST, WINDUP_TICKS);
        windupTicksRemaining = WINDUP_TICKS;
        cooldown = mob.getPhase().thrustCooldownTicks();
    }
}
