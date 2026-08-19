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

public class HasteSurgeSpell implements Spell
{
    private static final int BASE_DURATION_TICKS = 400;
    private static final int SPEED_AMPLIFIER = 1;
    private static final int HASTE_AMPLIFIER = 1;

    @Override
    public ItemStack getIcon()
    {
        return Items.SUGAR.getDefaultInstance();
    }

    @Override
    public Component getDisplayName()
    {
        return Component.literal("Haste Surge");
    }

    @Override
    public int getManaCost()
    {
        return 40;
    }

    @Override
    public int getBaseCooldownTicks()
    {
        return 80;
    }

    @Override
    public void cast(ServerPlayer caster, ItemStack lensStack, ServerLevel level, int stackCount)
    {
        int duration = (int) (BASE_DURATION_TICKS * SpellScaling.effectMultiplier(stackCount));
        caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, SPEED_AMPLIFIER));
        caster.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, duration, HASTE_AMPLIFIER));

        level.sendParticles(ParticleTypes.CRIT, caster.getX(), caster.getY() + 1, caster.getZ(), 20, 0.3, 0.5, 0.3, 0.1);
        level.playSound(null, caster.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.5f);
    }
}
