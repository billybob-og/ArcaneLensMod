package com.arcanelens.entity;

import com.arcanelens.entity.boss.FlorianFighter;
import com.arcanelens.entity.boss.ai.FlorianChargeGoal;
import com.arcanelens.entity.boss.ai.FlorianGoreGoal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

/** The temporary Florian a player summons with Florian's Antler (see FloriansAntlerItem) - the boss's own
 * gore/charge moveset via FlorianFighter, but with a fraction of his health and damage and slower
 * cooldowns. Owner-tracking comes from TamableAnimal exactly like AureliaCompanionEntity; expiry is the
 * shared SummonExpiryHandler tag, not tracked here. Carries its own synced action state because it can't
 * inherit AbstractGodBossEntity's (that's a Monster with a boss bar). */
public class FlorianCompanionEntity extends TamableAnimal implements FlorianFighter
{
    private static final EntityDataAccessor<Integer> DATA_ACTION =
            SynchedEntityData.defineId(FlorianCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTION_END_TICK =
            SynchedEntityData.defineId(FlorianCompanionEntity.class, EntityDataSerializers.INT);

    public FlorianCompanionEntity(EntityType<? extends TamableAnimal> type, Level level)
    {
        super(type, level);
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(DATA_ACTION, 0);
        this.entityData.define(DATA_ACTION_END_TICK, 0);
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3);
    }

    @Override
    protected void registerGoals()
    {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FlorianGoreGoal<>(this));
        this.goalSelector.addGoal(1, new FlorianChargeGoal<>(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0, 5.0f, 2.0f, false));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                entity -> entity instanceof Enemy));
    }

    @Override
    public void customServerAiStep()
    {
        super.customServerAiStep();
        if (getActionOrdinal() != 0 && this.tickCount >= getActionEndTick())
        {
            this.entityData.set(DATA_ACTION, 0);
        }
    }

    private int getActionOrdinal()
    {
        return this.entityData.get(DATA_ACTION);
    }

    @Override
    public int getActionEndTick()
    {
        return this.entityData.get(DATA_ACTION_END_TICK);
    }

    @Override
    public Action getFlorianAction()
    {
        return Action.values()[getActionOrdinal()];
    }

    @Override
    public void setFlorianAction(Action action, int durationTicks)
    {
        this.entityData.set(DATA_ACTION, action.ordinal());
        this.entityData.set(DATA_ACTION_END_TICK, this.tickCount + durationTicks);
    }

    @Override
    public int goreCooldownTicks()
    {
        return 45;
    }

    @Override
    public int chargeCooldownTicks()
    {
        return 120;
    }

    @Override
    public double approachSpeed()
    {
        return 1.0;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer)
    {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent)
    {
        return null;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand)
    {
        return InteractionResult.PASS;
    }

    @Override
    public boolean isFood(ItemStack stack)
    {
        return false;
    }
}
