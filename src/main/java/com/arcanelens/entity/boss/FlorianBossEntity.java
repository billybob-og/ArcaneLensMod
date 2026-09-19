package com.arcanelens.entity.boss;

import com.arcanelens.entity.boss.ai.FlorianChargeGoal;
import com.arcanelens.entity.boss.ai.FlorianGoreGoal;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Florian's God Challenge Hub boss fight - a stag that gores (FlorianGoreGoal) and charges
 * (FlorianChargeGoal), escalating across 3 HP-driven phases like AureliaBossEntity. The numbers below are a
 * first pass, meant to be retuned by feel. His Antler drop summons a debuffed copy of this same moveset
 * (FlorianCompanionEntity). */
public class FlorianBossEntity extends AbstractGodBossEntity implements FlorianFighter
{
    public enum Phase
    {
        STANDING(0.6F, 7.0F, 30, 70, 0.24F),
        WOUNDED(0.25F, 8.0F, 24, 50, 0.28F),
        DESPERATE(0.0F, 9.0F, 18, 34, 0.33F);

        private final float minHealthPercent;
        private final float goreDamage;
        private final int goreCooldownTicks;
        private final int chargeCooldownTicks;
        private final float moveSpeed;

        Phase(float minHealthPercent, float goreDamage, int goreCooldownTicks, int chargeCooldownTicks, float moveSpeed)
        {
            this.minHealthPercent = minHealthPercent;
            this.goreDamage = goreDamage;
            this.goreCooldownTicks = goreCooldownTicks;
            this.chargeCooldownTicks = chargeCooldownTicks;
            this.moveSpeed = moveSpeed;
        }

        public static Phase fromHealthPercent(float pct)
        {
            for (Phase phase : values())
            {
                if (pct > phase.minHealthPercent)
                {
                    return phase;
                }
            }
            return DESPERATE;
        }
    }

    public FlorianBossEntity(EntityType<? extends Monster> type, Level level)
    {
        super(type, level, Component.literal("Florian"), BossEvent.BossBarColor.GREEN);
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 220.0)
                .add(Attributes.ATTACK_DAMAGE, Phase.STANDING.goreDamage)
                .add(Attributes.MOVEMENT_SPEED, Phase.STANDING.moveSpeed)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    protected void registerGoals()
    {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FlorianGoreGoal<>(this));
        this.goalSelector.addGoal(1, new FlorianChargeGoal<>(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected String getGodId()
    {
        return "fertility";
    }

    public Phase getPhase()
    {
        return Phase.fromHealthPercent(getHealth() / getMaxHealth());
    }

    @Override
    public Action getFlorianAction()
    {
        return Action.values()[getActionOrdinal()];
    }

    @Override
    public void setFlorianAction(Action action, int durationTicks)
    {
        super.setAction(action.ordinal(), durationTicks);
    }

    @Override
    public int goreCooldownTicks()
    {
        return getPhase().goreCooldownTicks;
    }

    @Override
    public int chargeCooldownTicks()
    {
        return getPhase().chargeCooldownTicks;
    }

    // A multiplier on the MOVEMENT_SPEED attribute (which customServerAiStep already ramps per phase), not a
    // speed of its own - passing the phase's 0.24-0.33 here too multiplied it into a crawl.
    @Override
    public double approachSpeed()
    {
        return 1.0;
    }

    @Override
    public void customServerAiStep()
    {
        super.customServerAiStep();
        Phase phase = getPhase();
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(phase.goreDamage);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(phase.moveSpeed);
    }
}
