package com.arcanelens.entity.boss.ai;

import com.arcanelens.entity.boss.AureliaBossEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** Closes distance fast when she's not already in melee range - windup (a brief telegraph), then a
 * straight-line burst of velocity toward wherever the target was when the charge started (not
 * homing - a real dodgeable line, matching a "charge" reading as committal). No LOOK flag claimed (she
 * still turns to face the target every tick via direct look-control calls) so this can run alongside
 * AureliaSpearThrustGoal without fighting it for the LOOK flag - same reasoning as
 * SleepingGodRangedAttackGoal not claiming LOOK either. */
public class AureliaChargeGoal extends Goal
{
    private static final double TRIGGER_MIN_DISTANCE = 6.0;
    private static final double TRIGGER_MAX_DISTANCE = 16.0;
    // Public - AureliaBossModel reads these to ease its lean-forward pose in over the same windup
    // window this goal actually uses, so the animation timing can't drift out of sync with the AI.
    public static final int WINDUP_TICKS = 12;
    public static final int CHARGE_DURATION_TICKS = 12;
    private static final double CHARGE_SPEED = 1.4;
    private static final double HIT_RANGE = 3.0;

    private final AureliaBossEntity mob;
    private int cooldown;
    private int windupTicksRemaining = -1;
    private int chargeTicksRemaining = -1;
    private Vec3 chargeDirection = Vec3.ZERO;
    private boolean hasDealtDamage;

    public AureliaChargeGoal(AureliaBossEntity mob)
    {
        this.mob = mob;
        // No flags claimed at all (not even MOVE) - she bypasses navigation entirely during a charge
        // (direct setDeltaMovement below), so she doesn't need MOVE reserved for herself, and claiming
        // it would conflict with AureliaSpearThrustGoal's own MOVE+LOOK claim (same priority), which
        // stopped the goal selector from ever letting this one start at all - same reasoning
        // SleepingGodRangedAttackGoal claims no flags to coexist with its own melee goal.
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
        LivingEntity target = mob.getTarget();
        if (target == null || mob.getAction() != AureliaBossEntity.Action.NONE)
        {
            return false;
        }
        double distance = mob.distanceTo(target);
        return distance >= TRIGGER_MIN_DISTANCE && distance <= TRIGGER_MAX_DISTANCE;
    }

    @Override
    public boolean canContinueToUse()
    {
        return mob.getTarget() != null && (windupTicksRemaining >= 0 || chargeTicksRemaining >= 0);
    }

    @Override
    public void start()
    {
        windupTicksRemaining = WINDUP_TICKS;
        chargeTicksRemaining = -1;
        hasDealtDamage = false;
        mob.setAction(AureliaBossEntity.Action.CHARGE, WINDUP_TICKS + CHARGE_DURATION_TICKS);
        mob.getNavigation().stop();
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
            windupTicksRemaining--;
            if (windupTicksRemaining == 0)
            {
                chargeDirection = target.position().subtract(mob.position()).normalize();
                chargeTicksRemaining = CHARGE_DURATION_TICKS;
            }
            return;
        }

        if (chargeTicksRemaining > 0)
        {
            mob.setDeltaMovement(chargeDirection.x * CHARGE_SPEED, mob.getDeltaMovement().y, chargeDirection.z * CHARGE_SPEED);
            if (!hasDealtDamage && mob.distanceTo(target) <= HIT_RANGE)
            {
                mob.doHurtTarget(target);
                hasDealtDamage = true;
            }
            chargeTicksRemaining--;
        }
    }

    @Override
    public void stop()
    {
        windupTicksRemaining = -1;
        chargeTicksRemaining = -1;
        cooldown = mob.getPhase().chargeCooldownTicks();
    }
}
