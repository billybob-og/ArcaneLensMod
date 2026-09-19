package com.arcanelens.entity.boss.ai;

import com.arcanelens.entity.boss.FlorianFighter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** Closes distance with a stag's charge: head-down windup, then a straight-line burst toward where the
 * target stood when it began (not homing, so it's dodgeable). Same shape as AureliaChargeGoal - claims no
 * Goal flags so it can coexist with FlorianGoreGoal, and the two exclude each other through the shared
 * synced action instead. */
public class FlorianChargeGoal<T extends Mob & FlorianFighter> extends Goal
{
    private static final double TRIGGER_MIN_DISTANCE = 6.0;
    private static final double TRIGGER_MAX_DISTANCE = 16.0;
    public static final int WINDUP_TICKS = 14;
    public static final int CHARGE_DURATION_TICKS = 12;
    private static final double CHARGE_SPEED = 1.3;
    private static final double HIT_RANGE = 3.2;

    private final T mob;
    private int cooldown;
    private int windupTicksRemaining = -1;
    private int chargeTicksRemaining = -1;
    private Vec3 chargeDirection = Vec3.ZERO;
    private boolean hasDealtDamage;

    public FlorianChargeGoal(T mob)
    {
        this.mob = mob;
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
        if (target == null || mob.getFlorianAction() != FlorianFighter.Action.NONE)
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
        mob.setFlorianAction(FlorianFighter.Action.CHARGE, WINDUP_TICKS + CHARGE_DURATION_TICKS);
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
        cooldown = mob.chargeCooldownTicks();
    }
}
