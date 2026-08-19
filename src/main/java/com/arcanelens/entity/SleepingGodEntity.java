package com.arcanelens.entity;

import com.arcanelens.capability.FaithProvider;
import com.arcanelens.capability.FaithSync;
import com.arcanelens.entity.ai.SleepingGodMeleeAttackGoal;
import com.arcanelens.entity.ai.SleepingGodRangedAttackGoal;
import com.arcanelens.spell.DelayedImpactScheduler;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * "The One Who Sleeps" — dungeon boss. Fight has 4 HP-driven phases (see {@link Phase}); no other state machine.
 * Rendering is a temporary placeholder (see SleepingGodRenderer) until the user's real model exists.
 */
public class SleepingGodEntity extends Monster
{
    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(SleepingGodEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTION =
            SynchedEntityData.defineId(SleepingGodEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTION_END_TICK =
            SynchedEntityData.defineId(SleepingGodEntity.class, EntityDataSerializers.INT);

    private static final double HOVER_RADIUS = 7.0;
    private static final double HOVER_HEIGHT = 4.0;
    // Radians per tick the orbit angle advances - a full revolution takes roughly 15-16 seconds at 20 TPS,
    // a slow, deliberate circle rather than a dizzying spin.
    private static final double HOVER_ORBIT_SPEED = 0.02;

    // Animation lengths, matching the MaleSwing/SpellCast1/SpellCast2 .bbmodel keyframe data (in ticks, 20/sec).
    private static final int MELEE_ANIMATION_TICKS = 15;
    // SpellCast1 plays at 1.75x speed in SleepingGodModel (0.5s / 1.75 =~ 0.286s) - keep in sync.
    private static final int RANGED_CAST_1_ANIMATION_TICKS = 6;
    private static final int RANGED_CAST_2_ANIMATION_TICKS = 18;

    private double hoverOrbitAngle;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.literal("The One Who Sleeps"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    public final AnimationState meleeAnimationState = new AnimationState();
    public final AnimationState rangedCastOneAnimationState = new AnimationState();
    public final AnimationState rangedCastTwoAnimationState = new AnimationState();
    public final AnimationState flyStartAnimationState = new AnimationState();

    public enum Action
    {
        NONE, MELEE, RANGED_CAST_1, RANGED_CAST_2
    }

    public SleepingGodEntity(EntityType<? extends Monster> type, Level level)
    {
        super(type, level);
        // Flying-capable move control/navigation from the start (works fine for ground movement too) - the
        // AWAKE phase onward toggles no-gravity + a dedicated hover goal to actually take off, see setPhase().
        this.moveControl = new FlyingMoveControl(this, 20, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level)
    {
        return new FlyingPathNavigation(this, level);
    }

    public boolean isFlyingPhase()
    {
        return getPhase().ordinal() >= Phase.AWAKE.ordinal();
    }

    public enum Phase
    {
        // "Just woken up" - slow, mixed melee/ranged.
        // Numbers toned down significantly after first playtest (full diamond + bow + maxed lens died
        // in barely a phase) - both melee and ranged goals can now fire independently (no shared flag),
        // so their damage had to come down to avoid the two stacking into an inescapable burst.
        AWAKENING(0.75f, 5.0f, 40, 0.20f, 1, 70, 3.0f, 0.25f),
        STIRRING(0.50f, 6.0f, 32, 0.22f, 2, 60, 3.5f, 0.45f),
        // "Fully awake" - mostly ranged.
        AWAKE(0.25f, 7.0f, 26, 0.24f, 2, 50, 4.0f, 0.65f),
        // Not rage - he's recognized a worthy successor and wants to see how far they can be pushed.
        // Attacks get faster/harder across the board rather than purely more ranged.
        TESTING(0.0f, 9.0f, 20, 0.27f, 3, 45, 4.5f, 0.55f);

        private final float minHealthPercent;
        private final float meleeDamage;
        private final int meleeCooldownTicks;
        private final float moveSpeed;
        private final int rangedImpactCount;
        private final int rangedCooldownTicks;
        private final float rangedDamage;
        private final float rangedPreferenceChance;

        Phase(float minHealthPercent, float meleeDamage, int meleeCooldownTicks, float moveSpeed,
              int rangedImpactCount, int rangedCooldownTicks, float rangedDamage, float rangedPreferenceChance)
        {
            this.minHealthPercent = minHealthPercent;
            this.meleeDamage = meleeDamage;
            this.meleeCooldownTicks = meleeCooldownTicks;
            this.moveSpeed = moveSpeed;
            this.rangedImpactCount = rangedImpactCount;
            this.rangedCooldownTicks = rangedCooldownTicks;
            this.rangedDamage = rangedDamage;
            this.rangedPreferenceChance = rangedPreferenceChance;
        }

        public float meleeDamage() { return meleeDamage; }
        public int meleeCooldownTicks() { return meleeCooldownTicks; }
        public float moveSpeed() { return moveSpeed; }
        public int rangedImpactCount() { return rangedImpactCount; }
        public int rangedCooldownTicks() { return rangedCooldownTicks; }
        public float rangedDamage() { return rangedDamage; }
        public float rangedPreferenceChance() { return rangedPreferenceChance; }

        public static Phase fromHealthPercent(float pct)
        {
            for (Phase phase : values())
            {
                if (pct > phase.minHealthPercent)
                {
                    return phase;
                }
            }
            return TESTING;
        }
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.ATTACK_DAMAGE, Phase.AWAKENING.meleeDamage())
                .add(Attributes.MOVEMENT_SPEED, Phase.AWAKENING.moveSpeed())
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
                // Required by FlyingMoveControl (used from the AWAKE phase onward) - without this attribute
                // registered, the entity crashes the moment it ticks.
                .add(Attributes.FLYING_SPEED, 0.6);
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(DATA_PHASE, Phase.AWAKENING.ordinal());
        this.entityData.define(DATA_ACTION, Action.NONE.ordinal());
        this.entityData.define(DATA_ACTION_END_TICK, 0);
    }

    public Action getAction()
    {
        return Action.values()[this.entityData.get(DATA_ACTION)];
    }

    public void playMeleeAnimation()
    {
        this.entityData.set(DATA_ACTION, Action.MELEE.ordinal());
        this.entityData.set(DATA_ACTION_END_TICK, this.tickCount + MELEE_ANIMATION_TICKS);
        System.out.println("[SleepingGodDebug] SERVER start MELEE at tick=" + this.tickCount + " end=" + (this.tickCount + MELEE_ANIMATION_TICKS));
    }

    /** Randomly picks one of the two ranged-cast animations and marks it active; returns the chosen action
     * so the calling goal knows which animation length to time its actual attack effect against. */
    public Action playRandomRangedAnimation()
    {
        Action chosen = this.random.nextBoolean() ? Action.RANGED_CAST_1 : Action.RANGED_CAST_2;
        int lengthTicks = chosen == Action.RANGED_CAST_1 ? RANGED_CAST_1_ANIMATION_TICKS : RANGED_CAST_2_ANIMATION_TICKS;
        this.entityData.set(DATA_ACTION, chosen.ordinal());
        this.entityData.set(DATA_ACTION_END_TICK, this.tickCount + lengthTicks);
        System.out.println("[SleepingGodDebug] SERVER start " + chosen + " at tick=" + this.tickCount + " end=" + (this.tickCount + lengthTicks));
        return chosen;
    }

    private Action lastLoggedAction = Action.NONE;

    @Override
    public void tick()
    {
        super.tick();

        if (!this.level().isClientSide() && getAction() != Action.NONE && this.tickCount >= this.entityData.get(DATA_ACTION_END_TICK))
        {
            System.out.println("[SleepingGodDebug] SERVER expire " + getAction() + " at tick=" + this.tickCount);
            this.entityData.set(DATA_ACTION, Action.NONE.ordinal());
        }

        Action action = getAction();
        if (this.level().isClientSide() && action != lastLoggedAction)
        {
            System.out.println("[SleepingGodDebug] CLIENT action changed to " + action + " at tick=" + this.tickCount
                    + " meleeStarted=" + meleeAnimationState.isStarted()
                    + " c1Started=" + rangedCastOneAnimationState.isStarted()
                    + " c2Started=" + rangedCastTwoAnimationState.isStarted());
            lastLoggedAction = action;
        }
        meleeAnimationState.animateWhen(action == Action.MELEE, this.tickCount);
        rangedCastOneAnimationState.animateWhen(action == Action.RANGED_CAST_1, this.tickCount);
        rangedCastTwoAnimationState.animateWhen(action == Action.RANGED_CAST_2, this.tickCount);
        // Non-looping ("hold") animation - plays once when flight begins and holds its final pose for as
        // long as the boss stays in a flying phase.
        flyStartAnimationState.animateWhen(isFlyingPhase(), this.tickCount);
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
        this.setNoGravity(phase.ordinal() >= Phase.AWAKE.ordinal());
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
                cap.incrementSleepingGodDefeats();
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

        // Driven directly here (not via a Goal) so it's completely unaffected by Goal Selector flag
        // arbitration - a Goal-based hover kept losing MOVE/LOOK to the near-constant melee goal, leaving
        // the boss just drifting in place under no-gravity with nothing correcting it.
        if (isFlyingPhase())
        {
            LivingEntity target = getTarget();
            if (target != null)
            {
                // The orbit angle advances every tick (not just when picking a new spot), so the target
                // point continuously sweeps around the player - an actual circling flight instead of
                // hovering near one nearby point.
                hoverOrbitAngle += HOVER_ORBIT_SPEED;
                double x = target.getX() + Math.cos(hoverOrbitAngle) * HOVER_RADIUS;
                double z = target.getZ() + Math.sin(hoverOrbitAngle) * HOVER_RADIUS;
                double y = target.getY() + HOVER_HEIGHT;
                getMoveControl().setWantedPosition(x, y, z, getPhase().moveSpeed() * 2.0);
            }
        }
    }

    @Override
    protected void registerGoals()
    {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SleepingGodMeleeAttackGoal(this));
        this.goalSelector.addGoal(1, new SleepingGodRangedAttackGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Captures the target's position once, then schedules a phase-scaled barrage there, mirroring MeteorShowerSpell. */
    public void performRangedBarrage(LivingEntity target)
    {
        if (!(this.level() instanceof ServerLevel serverLevel))
        {
            return;
        }

        Phase phase = getPhase();
        Vec3 impact = target.position();
        long castTick = serverLevel.getGameTime();
        // Each impact lands a full second apart (and the first isn't instant either) so the barrage is a
        // dodgeable, telegraphed threat rather than a simultaneous burst - move off the marked spot and most
        // of it whiffs.
        for (int i = 0; i < phase.rangedImpactCount(); i++)
        {
            DelayedImpactScheduler.schedule(serverLevel, impact, this, phase.rangedDamage(), 2.5, castTick + (long) (i + 1) * 20L);
        }
    }

    /** Several arrows dropped from directly above the target's current position, scattered within a small
     * radius so they don't all land on the exact same block. */
    public void performArrowRain(LivingEntity target)
    {
        if (!(this.level() instanceof ServerLevel serverLevel))
        {
            return;
        }

        Phase phase = getPhase();
        for (int i = 0; i < phase.rangedImpactCount(); i++)
        {
            double dx = (getRandom().nextDouble() - 0.5) * 4.0;
            double dz = (getRandom().nextDouble() - 0.5) * 4.0;
            Arrow arrow = new Arrow(serverLevel, target.getX() + dx, target.getY() + 10.0, target.getZ() + dz);
            arrow.setOwner(this);
            arrow.setDeltaMovement(0.0, -1.2, 0.0);
            arrow.setBaseDamage(phase.rangedDamage());
            serverLevel.addFreshEntity(arrow);
        }
    }

    /** A single instant lightning bolt on the target, mirroring LightningStrikeSpell's summon pattern. */
    public void performLightningStrike(LivingEntity target)
    {
        if (!(this.level() instanceof ServerLevel serverLevel))
        {
            return;
        }

        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (bolt != null)
        {
            bolt.moveTo(target.getX(), target.getY(), target.getZ());
            serverLevel.addFreshEntity(bolt);
        }
    }

    @Override
    public boolean isPersistenceRequired()
    {
        return true;
    }

    // TEMPORARY placeholder sounds until custom audio exists.
    @Override
    protected SoundEvent getAmbientSound()
    {
        return SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource damageSource)
    {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return SoundEvents.WITHER_DEATH;
    }
}
