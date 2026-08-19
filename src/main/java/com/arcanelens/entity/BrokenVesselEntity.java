package com.arcanelens.entity;

import com.arcanelens.capability.FaithProvider;
import com.arcanelens.capability.FaithSync;
import com.arcanelens.entity.ai.BrokenVesselMeleeAttackGoal;
import com.arcanelens.entity.ai.BrokenVesselRangedAttackGoal;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

/**
 * "Broken Vessel" - the Warped Hollow's boss. Fight has 4 HP-driven phases (see {@link Phase}); no other
 * state machine, directly copying SleepingGodEntity's own precedent. Where Sleeping God's phases track
 * "waking up" (slow/melee -> fast/ranged), this boss's phases run the opposite narrative direction - vessel
 * intact -> fungus dominant - but the numeric phase-selection pattern is copied exactly regardless, since
 * that's what makes transitions fire at the right health thresholds.
 */
public class BrokenVesselEntity extends Monster
{
    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(BrokenVesselEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTION =
            SynchedEntityData.defineId(BrokenVesselEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTION_END_TICK =
            SynchedEntityData.defineId(BrokenVesselEntity.class, EntityDataSerializers.INT);

    private static final int MELEE_ANIMATION_TICKS = 15;
    private static final int RANGED_ANIMATION_TICKS = 12;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.literal("Broken Vessel"), BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);

    public final AnimationState meleeAnimationState = new AnimationState();
    public final AnimationState rangedAnimationState = new AnimationState();

    // Periodic spore-cloud AoE, driven directly in customServerAiStep (not a Goal) - same reasoning
    // SleepingGodEntity's hover-flight uses: this is passive/ambient, not target-reactive combat, so it
    // shouldn't have to fight the melee/ranged goals over MOVE/LOOK flags.
    private int aoeCooldownRemaining;
    private int aoeWindupRemaining = -1;
    private int teleportCooldownRemaining;

    public enum Action
    {
        NONE, MELEE, RANGED, ERUPTION_WINDUP
    }

    public BrokenVesselEntity(EntityType<? extends Monster> type, Level level)
    {
        super(type, level);
    }

    public enum Phase
    {
        // The cracked vessel/mask itself acting - slow, deliberate, no fungal effects yet.
        DORMANT(0.70f, 4.0f, 45, 0.20f, 80, 3.0f, 1, false, 0, 0.0f, 0, 0, 0),
        // Fungus starts asserting itself - periodic spore-cloud AoE bursts, ranged shots now carry Poison.
        CRACKING(0.40f, 5.0f, 38, 0.22f, 70, 3.5f, 1, true, 140, 3.0f, 0, 100, 0),
        // Fungus dominant - faster, ranged volleys instead of single shots, AoE now telegraphs first (a
        // punishable windup) rather than firing instantly.
        OVERGROWN(0.15f, 6.0f, 30, 0.28f, 55, 4.0f, 3, true, 110, 3.5f, 1, 120, 20),
        // The mask finally gives - fastest of any phase, plus a short teleport reposition as a signature
        // ability distinct from Sleeping God's flight.
        VESSEL_BREAKING(0.0f, 8.0f, 20, 0.32f, 40, 4.5f, 4, true, 90, 4.0f, 1, 140, 30, 100);

        private final float minHealthPercent;
        private final float meleeDamage;
        private final int meleeCooldownTicks;
        private final float moveSpeed;
        private final int rangedCooldownTicks;
        private final float rangedDamage;
        private final int rangedProjectileCount;
        private final boolean rangedPoisoned;
        private final int aoeCooldownTicks;
        private final float aoeRadius;
        private final int poisonAmplifier;
        private final int poisonDurationTicks;
        private final int aoeWindupTicks;
        private final int teleportCooldownTicks;

        Phase(float minHealthPercent, float meleeDamage, int meleeCooldownTicks, float moveSpeed,
              int rangedCooldownTicks, float rangedDamage, int rangedProjectileCount, boolean rangedPoisoned,
              int aoeCooldownTicks, float aoeRadius, int poisonAmplifier, int poisonDurationTicks, int aoeWindupTicks)
        {
            this(minHealthPercent, meleeDamage, meleeCooldownTicks, moveSpeed, rangedCooldownTicks, rangedDamage,
                    rangedProjectileCount, rangedPoisoned, aoeCooldownTicks, aoeRadius, poisonAmplifier,
                    poisonDurationTicks, aoeWindupTicks, 0);
        }

        Phase(float minHealthPercent, float meleeDamage, int meleeCooldownTicks, float moveSpeed,
              int rangedCooldownTicks, float rangedDamage, int rangedProjectileCount, boolean rangedPoisoned,
              int aoeCooldownTicks, float aoeRadius, int poisonAmplifier, int poisonDurationTicks, int aoeWindupTicks,
              int teleportCooldownTicks)
        {
            this.minHealthPercent = minHealthPercent;
            this.meleeDamage = meleeDamage;
            this.meleeCooldownTicks = meleeCooldownTicks;
            this.moveSpeed = moveSpeed;
            this.rangedCooldownTicks = rangedCooldownTicks;
            this.rangedDamage = rangedDamage;
            this.rangedProjectileCount = rangedProjectileCount;
            this.rangedPoisoned = rangedPoisoned;
            this.aoeCooldownTicks = aoeCooldownTicks;
            this.aoeRadius = aoeRadius;
            this.poisonAmplifier = poisonAmplifier;
            this.poisonDurationTicks = poisonDurationTicks;
            this.aoeWindupTicks = aoeWindupTicks;
            this.teleportCooldownTicks = teleportCooldownTicks;
        }

        public float meleeDamage() { return meleeDamage; }
        public int meleeCooldownTicks() { return meleeCooldownTicks; }
        public float moveSpeed() { return moveSpeed; }
        public int rangedCooldownTicks() { return rangedCooldownTicks; }
        public float rangedDamage() { return rangedDamage; }
        public int rangedProjectileCount() { return rangedProjectileCount; }
        public boolean rangedPoisoned() { return rangedPoisoned; }
        public int aoeCooldownTicks() { return aoeCooldownTicks; }
        public float aoeRadius() { return aoeRadius; }
        public int poisonAmplifier() { return poisonAmplifier; }
        public int poisonDurationTicks() { return poisonDurationTicks; }
        public int aoeWindupTicks() { return aoeWindupTicks; }
        public int teleportCooldownTicks() { return teleportCooldownTicks; }

        public static Phase fromHealthPercent(float pct)
        {
            for (Phase phase : values())
            {
                if (pct > phase.minHealthPercent)
                {
                    return phase;
                }
            }
            return VESSEL_BREAKING;
        }
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 260.0)
                .add(Attributes.ATTACK_DAMAGE, Phase.DORMANT.meleeDamage())
                .add(Attributes.MOVEMENT_SPEED, Phase.DORMANT.moveSpeed())
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4);
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(DATA_PHASE, Phase.DORMANT.ordinal());
        this.entityData.define(DATA_ACTION, Action.NONE.ordinal());
        this.entityData.define(DATA_ACTION_END_TICK, 0);
    }

    public Action getAction()
    {
        return Action.values()[this.entityData.get(DATA_ACTION)];
    }

    private void setAction(Action action, int lengthTicks)
    {
        this.entityData.set(DATA_ACTION, action.ordinal());
        this.entityData.set(DATA_ACTION_END_TICK, this.tickCount + lengthTicks);
    }

    public void playMeleeAnimation()
    {
        setAction(Action.MELEE, MELEE_ANIMATION_TICKS);
    }

    public void playRangedAnimation()
    {
        setAction(Action.RANGED, RANGED_ANIMATION_TICKS);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (!this.level().isClientSide() && getAction() != Action.NONE && this.tickCount >= this.entityData.get(DATA_ACTION_END_TICK))
        {
            this.entityData.set(DATA_ACTION, Action.NONE.ordinal());
        }

        Action action = getAction();
        meleeAnimationState.animateWhen(action == Action.MELEE, this.tickCount);
        rangedAnimationState.animateWhen(action == Action.RANGED, this.tickCount);
    }

    public Phase getPhase()
    {
        return Phase.values()[this.entityData.get(DATA_PHASE)];
    }

    private void setPhase(Phase phase)
    {
        this.entityData.set(DATA_PHASE, phase.ordinal());
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(phase.meleeDamage());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(phase.moveSpeed());
        // One-time telegraphed burst marking the shift itself - bigger/slower than anything the new phase
        // does on its own ongoing cooldown, so a transition always reads as its own moment. Never fires on
        // the very first phase assignment: defineSynchedData already defaults DATA_PHASE to DORMANT, and
        // 100% health maps to DORMANT too, so customServerAiStep's "current != getPhase()" check is false
        // on the very first tick and setPhase is never called for the starting phase at all.
        performTransitionBurst();
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player)
    {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player)
    {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void die(DamageSource damageSource)
    {
        super.die(damageSource);
        bossEvent.removeAllPlayers();
        if (damageSource.getEntity() instanceof ServerPlayer player)
        {
            player.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> {
                cap.incrementBrokenVesselDefeats();
                FaithSync.syncToClient(player);
            });
        }
    }

    @Override
    protected void customServerAiStep()
    {
        super.customServerAiStep();
        bossEvent.setProgress(Mth.clamp(this.getHealth() / this.getMaxHealth(), 0.0f, 1.0f));

        Phase current = Phase.fromHealthPercent(this.getHealth() / this.getMaxHealth());
        if (current != getPhase())
        {
            setPhase(current);
        }

        tickSporeAoe();
        tickTeleportReposition();
    }

    /** Periodic spore-cloud AoE, independent of the melee/ranged goals - disabled entirely in DORMANT
     * (aoeCooldownTicks == 0), and telegraphed with a punishable windup from OVERGROWN onward. */
    private void tickSporeAoe()
    {
        Phase phase = getPhase();
        if (phase.aoeCooldownTicks() <= 0)
        {
            return;
        }

        if (aoeWindupRemaining >= 0)
        {
            aoeWindupRemaining--;
            if (aoeWindupRemaining < 0)
            {
                performSporeBurst(phase, phase.aoeRadius());
                aoeCooldownRemaining = phase.aoeCooldownTicks();
            }
            return;
        }

        if (aoeCooldownRemaining > 0)
        {
            aoeCooldownRemaining--;
            return;
        }

        if (getTarget() == null)
        {
            return;
        }

        if (phase.aoeWindupTicks() > 0)
        {
            aoeWindupRemaining = phase.aoeWindupTicks();
            setAction(Action.ERUPTION_WINDUP, phase.aoeWindupTicks());
        }
        else
        {
            performSporeBurst(phase, phase.aoeRadius());
            aoeCooldownRemaining = phase.aoeCooldownTicks();
        }
    }

    /** VESSEL_BREAKING-only signature ability - a short reposition near the current target, distinct from
     * Sleeping God's flight. Uses the same vanilla-safe Entity.randomTeleport already relied on by things
     * like Endermen/Shulkers - no custom teleport math needed. */
    private void tickTeleportReposition()
    {
        Phase phase = getPhase();
        if (phase.teleportCooldownTicks() <= 0)
        {
            return;
        }

        if (teleportCooldownRemaining > 0)
        {
            teleportCooldownRemaining--;
            return;
        }

        LivingEntity target = getTarget();
        if (target == null)
        {
            return;
        }

        double angle = this.random.nextDouble() * Math.PI * 2.0;
        double x = target.getX() + Math.cos(angle) * 4.0;
        double z = target.getZ() + Math.sin(angle) * 4.0;
        if (this.randomTeleport(x, target.getY(), z, true))
        {
            teleportCooldownRemaining = phase.teleportCooldownTicks();
        }
    }

    private void performSporeBurst(Phase phase, float radius)
    {
        if (!(this.level() instanceof ServerLevel serverLevel))
        {
            return;
        }

        AreaEffectCloud cloud = new AreaEffectCloud(serverLevel, this.getX(), this.getY(), this.getZ());
        cloud.setOwner(this);
        cloud.setRadius(radius);
        cloud.setDuration(60);
        cloud.setWaitTime(5);
        cloud.setRadiusPerTick((-radius) / 60.0f);
        if (phase.poisonDurationTicks() > 0)
        {
            cloud.addEffect(new MobEffectInstance(MobEffects.POISON, phase.poisonDurationTicks(), phase.poisonAmplifier()));
        }
        serverLevel.addFreshEntity(cloud);
    }

    /** Bigger, slower, more visually obvious than the phase's own ongoing spore bursts - a telegraphed
     * one-shot marking the transition itself, not meant to blend into the new phase's regular pattern. */
    private void performTransitionBurst()
    {
        performSporeBurst(getPhase(), getPhase().aoeRadius() > 0 ? getPhase().aoeRadius() * 1.5f : 2.5f);
    }

    @Override
    protected void registerGoals()
    {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BrokenVesselMeleeAttackGoal(this));
        this.goalSelector.addGoal(1, new BrokenVesselRangedAttackGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** DORMANT: a single brittle shard, no fungal effect. CRACKING onward: a volley of spore-touched shards
     * that poison on hit, scaling in count with the phase - mirrors SleepingGodEntity.performArrowRain's
     * "several projectiles from above" pattern. */
    public void performRangedAttack(LivingEntity target)
    {
        if (!(this.level() instanceof ServerLevel serverLevel))
        {
            return;
        }

        Phase phase = getPhase();
        for (int i = 0; i < phase.rangedProjectileCount(); i++)
        {
            double dx = (getRandom().nextDouble() - 0.5) * 3.0;
            double dz = (getRandom().nextDouble() - 0.5) * 3.0;
            Arrow shard = new Arrow(serverLevel, target.getX() + dx, target.getY() + 8.0, target.getZ() + dz);
            shard.setOwner(this);
            shard.setDeltaMovement(0.0, -1.0, 0.0);
            shard.setBaseDamage(phase.rangedDamage());
            if (phase.rangedPoisoned() && phase.poisonDurationTicks() > 0)
            {
                shard.addEffect(new MobEffectInstance(MobEffects.POISON, phase.poisonDurationTicks(), phase.poisonAmplifier()));
            }
            serverLevel.addFreshEntity(shard);
        }
    }

    @Override
    public boolean isPersistenceRequired()
    {
        return true;
    }

    /** The boss's own spore-cloud AoE (performSporeBurst) spawns centered on itself, and AreaEffectCloud
     * applies to every LivingEntity in range including its owner - without this override it was poisoning
     * itself every time it used its own attack. Also immune to Wither for the same reason the Vessel-Bound
     * armor grants the player immunity to both: it's made of the same warped fungus that's already immune
     * to what it carries. */
    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance)
    {
        if (effectInstance.getEffect() == MobEffects.POISON || effectInstance.getEffect() == MobEffects.WITHER)
        {
            return false;
        }
        return super.canBeAffected(effectInstance);
    }

    /** Monster.shouldDespawnInPeaceful() defaults to true, and Mob.checkDespawn() fires that check before
     * (and independent of) isPersistenceRequired() above - so without this override, switching a world to
     * Peaceful would instantly discard Broken Vessel the moment nobody's fought it yet. Since the only way
     * to get a Vessel Summoning Charm is a Shard dropped by killing it, losing the one auto-spawned boss
     * before its first kill would be a permanent soft-lock with no in-game recovery. Same exemption vanilla
     * bosses (Wither, Ender Dragon) get, just applied by hand since Monster doesn't grant it automatically. */
    @Override
    protected boolean shouldDespawnInPeaceful()
    {
        return false;
    }

    // TEMPORARY placeholder sounds until custom audio exists - same approach SleepingGodEntity takes.
    @Override
    protected SoundEvent getAmbientSound()
    {
        return SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource)
    {
        return SoundEvents.SPIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return SoundEvents.SPIDER_DEATH;
    }
}
