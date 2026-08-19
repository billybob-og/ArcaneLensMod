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

public class FrostBoltSpell implements Spell
{
    private static final double RANGE = 20.0;
    private static final float BASE_DAMAGE = 4.0f;
    private static final int SLOW_DURATION_TICKS = 100;
    private static final int SLOW_AMPLIFIER = 2;

    @Override
    public ItemStack getIcon()
    {
        return Items.SNOWBALL.getDefaultInstance();
    }

    @Override
    public Component getDisplayName()
    {
        return Component.literal("Frost Bolt");
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
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_DURATION_TICKS, SLOW_AMPLIFIER));

        level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1, target.getZ(), 20, 0.3, 0.5, 0.3, 0.02);
        level.playSound(null, target.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 1.4f);
    }
}
