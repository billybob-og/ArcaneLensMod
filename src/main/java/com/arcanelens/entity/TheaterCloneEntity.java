package com.arcanelens.entity;

import com.arcanelens.Config;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** Stage Clone's decoy - a stationary Mob (no goals, zero movement speed) that hostile mobs are
 * directly retargeted onto when summoned (see TheaterAbilityHandler; ordinary mob AI never targets
 * a type it doesn't recognize on its own, so retargeting is done explicitly rather than relying on
 * vanilla target-selector goals). Expires after Config.theaterCloneLifetimeTicks either way, bursting
 * into confetti and briefly blinding nearby enemies whether it's killed or simply times out. */
public class TheaterCloneEntity extends Mob
{
    private int lifetimeTicks;

    public TheaterCloneEntity(EntityType<? extends Mob> type, Level level)
    {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.ARMOR, 0.0);
    }

    @Override
    protected void registerGoals()
    {
        // Deliberately no goals at all - a stationary decoy has nothing to decide.
    }

    @Override
    public void tick()
    {
        super.tick();
        if (level().isClientSide)
        {
            return;
        }

        lifetimeTicks++;
        if (lifetimeTicks >= Config.theaterCloneLifetimeTicks)
        {
            burstIntoConfetti();
            this.discard();
        }
    }

    @Override
    public void die(DamageSource damageSource)
    {
        burstIntoConfetti();
        super.die(damageSource);
    }

    private void burstIntoConfetti()
    {
        if (!(level() instanceof ServerLevel serverLevel))
        {
            return;
        }

        serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, getX(), getY() + 1.0, getZ(), 40, 0.4, 0.6, 0.4, 0.15);

        AABB blindRange = getBoundingBox().inflate(4.0);
        for (LivingEntity nearby : serverLevel.getEntitiesOfClass(LivingEntity.class, blindRange, e -> e instanceof Mob))
        {
            nearby.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer)
    {
        return false;
    }
}
