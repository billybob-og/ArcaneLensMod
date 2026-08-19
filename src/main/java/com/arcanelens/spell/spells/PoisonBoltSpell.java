package com.arcanelens.spell.spells;

import com.arcanelens.api.spell.Spell;
import com.arcanelens.spell.SpellScaling;
import com.arcanelens.spell.SpellTargeting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PoisonBoltSpell implements Spell
{
    private static final double RANGE = 20.0;
    private static final float BASE_DAMAGE = 3.0f;
    private static final int POISON_DURATION_TICKS = 100;
    private static final int POISON_AMPLIFIER = 1;

    @Override
    public ItemStack getIcon()
    {
        return Items.SPIDER_EYE.getDefaultInstance();
    }

    @Override
    public Component getDisplayName()
    {
        return Component.literal("Poison Bolt");
    }

    @Override
    public int getManaCost()
    {
        return 35;
    }

    @Override
    public int getBaseCooldownTicks()
    {
        return 30;
    }

    @Override
    public void cast(ServerPlayer caster, ItemStack lensStack, ServerLevel level, int stackCount)
    {
        LivingEntity target = SpellTargeting.getTargetEntity(caster, RANGE * SpellScaling.effectMultiplier(stackCount));
        if (target == null)
        {
            caster.displayClientMessage(Component.literal("No target in range."), true);
            return;
        }

        float damage = BASE_DAMAGE * SpellScaling.effectMultiplier(stackCount);
        target.hurt(level.damageSources().indirectMagic(caster, caster), damage);
        target.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_DURATION_TICKS, POISON_AMPLIFIER));

        level.sendParticles(ParticleTypes.WITCH, target.getX(), target.getY() + 1, target.getZ(), 20, 0.3, 0.5, 0.3, 0.02);
        level.playSound(null, target.blockPosition(), SoundEvents.WITCH_THROW, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
