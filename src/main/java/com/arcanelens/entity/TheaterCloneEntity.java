package com.arcanelens.entity;

import com.arcanelens.Config;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/** Stage Clone's decoy - a stationary Mob (no goals, zero movement speed) that hostile mobs are
 * directly retargeted onto when summoned (see TheaterAbilityHandler; ordinary mob AI never targets
 * a type it doesn't recognize on its own, so retargeting is done explicitly rather than relying on
 * vanilla target-selector goals). Expires after Config.theaterCloneLifetimeTicks either way, bursting
 * into confetti and briefly blinding nearby enemies whether it's killed or simply times out.
 *
 * <p>Looks like the player who summoned it: their UUID is synced so the renderer can fetch their skin,
 * and their equipment is copied into synced DATA fields rather than the real equipment slots - purely
 * for display. Real slots stay empty, so the copied gear grants the decoy no stats or enchantment
 * effects and there is nothing on it to drop (see dropCustomDeathLoot).</p> */
public class TheaterCloneEntity extends Mob
{
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER =
            SynchedEntityData.defineId(TheaterCloneEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<ItemStack> DATA_HEAD =
            SynchedEntityData.defineId(TheaterCloneEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> DATA_CHEST =
            SynchedEntityData.defineId(TheaterCloneEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> DATA_LEGS =
            SynchedEntityData.defineId(TheaterCloneEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> DATA_FEET =
            SynchedEntityData.defineId(TheaterCloneEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> DATA_MAINHAND =
            SynchedEntityData.defineId(TheaterCloneEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> DATA_OFFHAND =
            SynchedEntityData.defineId(TheaterCloneEntity.class, EntityDataSerializers.ITEM_STACK);

    private int lifetimeTicks;

    public TheaterCloneEntity(EntityType<? extends Mob> type, Level level)
    {
        super(type, level);
        this.setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(DATA_OWNER, Optional.empty());
        this.entityData.define(DATA_HEAD, ItemStack.EMPTY);
        this.entityData.define(DATA_CHEST, ItemStack.EMPTY);
        this.entityData.define(DATA_LEGS, ItemStack.EMPTY);
        this.entityData.define(DATA_FEET, ItemStack.EMPTY);
        this.entityData.define(DATA_MAINHAND, ItemStack.EMPTY);
        this.entityData.define(DATA_OFFHAND, ItemStack.EMPTY);
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.ARMOR, 0.0);
    }

    /** Makes this decoy look like the given player - their skin (via UUID) and worn/held gear. */
    public void copyAppearanceFrom(Player player)
    {
        this.entityData.set(DATA_OWNER, Optional.of(player.getUUID()));
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            this.entityData.set(accessorFor(slot), player.getItemBySlot(slot).copy());
        }
    }

    @Nullable
    public UUID getOwnerSkinId()
    {
        return this.entityData.get(DATA_OWNER).orElse(null);
    }

    /** On the client, reports the copied display gear so the armor/held-item layers draw it; on the server
     * this stays the real (empty) slots, which is what keeps the gear from affecting stats or dropping. */
    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot)
    {
        if (this.level().isClientSide)
        {
            return this.entityData.get(accessorFor(slot));
        }
        return super.getItemBySlot(slot);
    }

    private static EntityDataAccessor<ItemStack> accessorFor(EquipmentSlot slot)
    {
        return switch (slot)
        {
            case HEAD -> DATA_HEAD;
            case CHEST -> DATA_CHEST;
            case LEGS -> DATA_LEGS;
            case FEET -> DATA_FEET;
            case MAINHAND -> DATA_MAINHAND;
            case OFFHAND -> DATA_OFFHAND;
        };
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

    /** A decoy leaves nothing behind - no gear, no loot. */
    @Override
    protected void dropCustomDeathLoot(DamageSource damageSource, int looting, boolean recentlyHit)
    {
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
