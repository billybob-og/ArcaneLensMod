package com.arcanelens.spell.spells;

import com.arcanelens.api.spell.Spell;
import com.arcanelens.spell.SpellScaling;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class HealSpell implements Spell
{
    private static final float BASE_HEAL = 8.0f;

    @Override
    public ItemStack getIcon()
    {
        return Items.GOLDEN_APPLE.getDefaultInstance();
    }

    @Override
    public Component getDisplayName()
    {
        return Component.literal("Heal");
    }

    @Override
    public int getManaCost()
    {
        return 40;
    }

    @Override
    public int getBaseCooldownTicks()
    {
        return 60;
    }

    @Override
    public void cast(ServerPlayer caster, ItemStack lensStack, ServerLevel level, int stackCount)
    {
        float amount = BASE_HEAL * SpellScaling.effectMultiplier(stackCount);
        caster.heal(amount);
        level.sendParticles(ParticleTypes.HEART, caster.getX(), caster.getY() + 1.5, caster.getZ(), 6, 0.3, 0.3, 0.3, 0.0);
        level.playSound(null, caster.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6f, 1.5f);
    }
}
