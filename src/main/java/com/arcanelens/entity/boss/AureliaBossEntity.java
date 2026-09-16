package com.arcanelens.entity.boss;

import com.arcanelens.entity.boss.ai.AureliaChargeGoal;
import com.arcanelens.entity.boss.ai.AureliaSpearThrustGoal;
import com.arcanelens.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Aurelia's God Challenge Hub boss fight - a spear-thrust melee fighter (see AureliaSpearThrustGoal)
 * with an occasional forward charge to close distance (AureliaChargeGoal), escalating across 3 HP-driven
 * phases the same way SleepingGodEntity's fight does (read-only precedent, not shared code - see
 * AbstractGodBossEntity's own javadoc). Fresh rig/animation from the companion's (see AureliaBossModel)
 * since this is a real boss moveset, not the companion's simple punch. */
public class AureliaBossEntity extends AbstractGodBossEntity
{
    public enum Action
    {
        NONE, THRUST, CHARGE
    }

    public enum Phase
    {
        STANDING(0.6F, 6.0F, 30, 60, 0.25F),
        WOUNDED(0.25F, 7.0F, 24, 45, 0.30F),
        DESPERATE(0.0F, 8.0F, 18, 30, 0.35F);

        private final float minHealthPercent;
        private final float thrustDamage;
        private final int thrustCooldownTicks;
        private final int chargeCooldownTicks;
        private final float moveSpeed;

        Phase(float minHealthPercent, float thrustDamage, int thrustCooldownTicks, int chargeCooldownTicks, float moveSpeed)
        {
            this.minHealthPercent = minHealthPercent;
            this.thrustDamage = thrustDamage;
            this.thrustCooldownTicks = thrustCooldownTicks;
            this.chargeCooldownTicks = chargeCooldownTicks;
            this.moveSpeed = moveSpeed;
        }

        public float thrustDamage()
        {
            return thrustDamage;
        }

        public int thrustCooldownTicks()
        {
            return thrustCooldownTicks;
        }

        public int chargeCooldownTicks()
        {
            return chargeCooldownTicks;
        }

        public float moveSpeed()
        {
            return moveSpeed;
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

    public AureliaBossEntity(EntityType<? extends Monster> type, Level level)
    {
        super(type, level, Component.literal("Aurelia"), BossEvent.BossBarColor.RED);
        // Visibly carries her spear into the fight (see AureliaBossItemInHandLayer) - drop chance forced
        // to 0 so this equipped copy never ALSO drops on death alongside AbstractGodBossEntity's own
        // granted/broken-gated drop logic, which is the only source of a real, keepable spear.
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.SPEAR.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.ATTACK_DAMAGE, Phase.STANDING.thrustDamage())
                .add(Attributes.MOVEMENT_SPEED, Phase.STANDING.moveSpeed())
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4);
    }

    @Override
    protected void registerGoals()
    {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AureliaSpearThrustGoal(this));
        this.goalSelector.addGoal(1, new AureliaChargeGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected String getGodId()
    {
        return "war";
    }

    public Action getAction()
    {
        return Action.values()[getActionOrdinal()];
    }

    public void setAction(Action action, int durationTicks)
    {
        super.setAction(action.ordinal(), durationTicks);
    }

    public Phase getPhase()
    {
        return Phase.fromHealthPercent(getHealth() / getMaxHealth());
    }

    @Override
    public void customServerAiStep()
    {
        super.customServerAiStep();
        Phase phase = getPhase();
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(phase.thrustDamage());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(phase.moveSpeed());
    }
}
