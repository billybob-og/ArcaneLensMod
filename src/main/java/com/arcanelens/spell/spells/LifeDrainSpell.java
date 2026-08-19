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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class LifeDrainSpell implements Spell
{
    private static final double RANGE = 18.0;
    private static final float BASE_DAMAGE = 6.0f;
    private static final float DRAIN_RATIO = 0.5f;

    @Override
    public ItemStack getIcon()
    {
        return Items.WITHER_ROSE.getDefaultInstance();
    }

    @Override
    public Component getDisplayName()
    {
        return Component.literal("Life Drain");
    }

    @Override
    public int getManaCost()
    {
        return 45;
    }

    @Override
    public int getBaseCooldownTicks()
    {
        return 50;
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
        caster.heal(damage * DRAIN_RATIO);

        level.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY() + 1, target.getZ(), 15, 0.3, 0.5, 0.3, 0.02);
        level.sendParticles(ParticleTypes.HEART, caster.getX(), caster.getY() + 1.5, caster.getZ(), 4, 0.3, 0.3, 0.3, 0.0);
        level.playSound(null, target.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0f, 0.7f);
    }
}
