package com.arcanelens.entity;

import com.arcanelens.Config;
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
    public AureliaCompanionEntity(EntityType<? extends TamableAnimal> type, Level level)
    {
        super(type, level);
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
