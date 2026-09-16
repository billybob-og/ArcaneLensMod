package com.arcanelens.entity;

import com.arcanelens.Config;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

/** Aurelia's "My Love" summon (see item/AureliaSummonHandler) - a temporary combat companion who
 * proactively fights nearby hostiles for the summoning player. Extends TamableAnimal purely for its
 * owner-tracking/FollowOwnerGoal machinery, not for actual bone-taming: she's spawned already owned
 * via tame(), never interacted with to tame in the normal sense, so mobInteract() is a no-op. Expiry
 * is handled generically by SummonExpiryHandler (the same persistent-data-tag mechanism
 * SummonAllySpell's wolves already use), not tracked on this class at all. */
public class AureliaCompanionEntity extends TamableAnimal
{
    /** The tickCount value at her last successful attack, synced to the client so AureliaModel can
     * derive a swing animation from "ticks since this happened" - NOT LivingEntity's own swing()/
     * getAttackAnim(), which turned out not to reliably drive an AI-melee mob's attack animation
     * (Mob#doHurtTarget doesn't call swing() itself, and even after adding an explicit swing() call it
     * still didn't visibly animate). Same idea as SleepingGodEntity's own DATA_ACTION_END_TICK - a
     * synced "when does this animation end" tick the model derives progress from - though Sleeping
     * God's is a real custom swing animation while Broken Vessel currently has no attack animation at
     * all, so this isn't literally reusing either one's code, just the same known-working shape of
     * mechanism. Defaults far in the past so "ticks since attack" starts safely out of animation range
     * at spawn. */
    private static final EntityDataAccessor<Integer> DATA_ATTACK_START_TICK =
            SynchedEntityData.defineId(AureliaCompanionEntity.class, EntityDataSerializers.INT);

    public AureliaCompanionEntity(EntityType<? extends TamableAnimal> type, Level level)
    {
        super(type, level);
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACK_START_TICK, -1000);
    }

    public int getAttackStartTick()
    {
        return this.entityData.get(DATA_ATTACK_START_TICK);
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Config.aureliaCompanionMaxHealth)
                .add(Attributes.MOVEMENT_SPEED, Config.aureliaCompanionMovementSpeed)
                .add(Attributes.ATTACK_DAMAGE, Config.aureliaCompanionAttackDamage)
                .add(Attributes.FOLLOW_RANGE, Config.aureliaCompanionTargetSearchRadius);
    }

    @Override
    protected void registerGoals()
    {
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0, 4.0f, 2.0f, false));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // Proactively targets anything marked Enemy nearby, not just things that attack her/her owner
        // first - "fights mobs for you" reads as an active defender, not a purely reactive one.
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                entity -> entity instanceof Enemy));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer)
    {
        return false;
    }

    /** Records when she attacks so AureliaModel can animate the swing from it (see
     * DATA_ATTACK_START_TICK's own javadoc for why this doesn't just use LivingEntity#swing()). */
    @Override
    public boolean doHurtTarget(Entity target)
    {
        boolean result = super.doHurtTarget(target);
        this.entityData.set(DATA_ATTACK_START_TICK, this.tickCount);
        return result;
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
