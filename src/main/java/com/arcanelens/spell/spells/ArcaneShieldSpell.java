package com.arcanelens.spell.spells;

import com.arcanelens.api.spell.Spell;
import com.arcanelens.spell.SpellScaling;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ArcaneShieldSpell implements Spell
{
    private static final int BASE_DURATION_TICKS = 200;
    private static final int ABSORPTION_AMPLIFIER = 1;

    @Override
    public ItemStack getIcon()
    {
        return Items.SHIELD.getDefaultInstance();
    }

    @Override
    public Component getDisplayName()
    {
        return Component.literal("Arcane Shield");
    }

    @Override
    public int getManaCost()
    {
        return 45;
    }

    @Override
    public int getBaseCooldownTicks()
    {
        return 90;
    }

    @Override
    public void cast(ServerPlayer caster, ItemStack lensStack, ServerLevel level, int stackCount)
    {
        int duration = (int) (BASE_DURATION_TICKS * SpellScaling.effectMultiplier(stackCount));
        caster.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, ABSORPTION_AMPLIFIER));

        level.sendParticles(ParticleTypes.ENCHANT, caster.getX(), caster.getY() + 1, caster.getZ(), 30, 0.4, 0.6, 0.4, 0.05);
        level.playSound(null, caster.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0f, 1.2f);
    }
}
