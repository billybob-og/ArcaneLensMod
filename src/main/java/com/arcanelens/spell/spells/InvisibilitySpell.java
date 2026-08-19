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

public class InvisibilitySpell implements Spell
{
    private static final int BASE_DURATION_TICKS = 200;

    @Override
    public ItemStack getIcon()
    {
        return Items.PHANTOM_MEMBRANE.getDefaultInstance();
    }

    @Override
    public Component getDisplayName()
    {
        return Component.literal("Invisibility");
    }

    @Override
    public int getManaCost()
    {
        return 50;
    }

    @Override
    public int getBaseCooldownTicks()
    {
        return 300;
    }

    @Override
    public void cast(ServerPlayer caster, ItemStack lensStack, ServerLevel level, int stackCount)
    {
        int duration = (int) (BASE_DURATION_TICKS * SpellScaling.effectMultiplier(stackCount));
        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, duration, 0));

        level.sendParticles(ParticleTypes.SMOKE, caster.getX(), caster.getY() + 1, caster.getZ(), 20, 0.3, 0.5, 0.3, 0.02);
        level.playSound(null, caster.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7f, 1.4f);
    }
}
