package com.arcanelens.spell.spells;

import com.arcanelens.api.spell.Spell;
import com.arcanelens.spell.SpellScaling;
import com.arcanelens.spell.SummonExpiryHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SummonAllySpell implements Spell
{
    private static final int BASE_DURATION_TICKS = 1200;

    @Override
    public ItemStack getIcon()
    {
        return Items.BONE.getDefaultInstance();
    }

    @Override
    public Component getDisplayName()
    {
        return Component.literal("Summon Ally");
    }

    @Override
    public int getManaCost()
    {
        return 55;
    }

    @Override
    public int getBaseCooldownTicks()
    {
        return 240;
    }

    @Override
    public void cast(ServerPlayer caster, ItemStack lensStack, ServerLevel level, int stackCount)
    {
        int wolfCount = SpellScaling.scaledCastCount(stackCount, 2, 3);
        int duration = (int) (BASE_DURATION_TICKS * SpellScaling.effectMultiplier(stackCount));
        long expiry = level.getGameTime() + duration;

        for (int i = 0; i < wolfCount; i++)
        {
            Wolf wolf = EntityType.WOLF.create(level);
            if (wolf == null)
            {
                continue;
            }

            double angle = i * (2 * Math.PI / wolfCount);
            double sx = caster.getX() + Math.cos(angle) * 2.0;
            double sz = caster.getZ() + Math.sin(angle) * 2.0;
            wolf.moveTo(sx, caster.getY(), sz, caster.getYRot(), 0);

            wolf.tame(caster);
            wolf.setOrderedToSit(false);
            wolf.setCollarColor(DyeColor.PURPLE);
            wolf.getPersistentData().putLong(SummonExpiryHandler.EXPIRY_TAG, expiry);

            level.addFreshEntity(wolf);
        }

        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, caster.getX(), caster.getY() + 1, caster.getZ(), 20, 0.5, 0.5, 0.5, 0.0);
        level.playSound(null, caster.blockPosition(), SoundEvents.WOLF_HOWL, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
