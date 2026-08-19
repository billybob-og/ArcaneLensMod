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
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChainLightningSpell implements Spell
{
    private static final double RANGE = 20.0;
    private static final double JUMP_RANGE = 6.0;
    private static final float BASE_DAMAGE = 5.0f;
    private static final float FALLOFF = 0.75f;

    @Override
    public ItemStack getIcon()
    {
        return Items.CHAIN.getDefaultInstance();
    }

    @Override
    public Component getDisplayName()
    {
        return Component.literal("Chain Lightning");
    }

    @Override
    public int getManaCost()
    {
        return 55;
    }

    @Override
    public int getBaseCooldownTicks()
    {
        return 90;
    }

    @Override
    public void cast(ServerPlayer caster, ItemStack lensStack, ServerLevel level, int stackCount)
    {
        double range = RANGE * SpellScaling.effectMultiplier(stackCount);
        LivingEntity primary = SpellTargeting.getTargetEntity(caster, range);
        if (primary == null)
        {
            caster.displayClientMessage(Component.literal("No target in range."), true);
            return;
        }

        int extraJumps = SpellScaling.scaledCastCount(stackCount, 2, 4);
        float damage = BASE_DAMAGE * SpellScaling.effectMultiplier(stackCount);

        List<LivingEntity> alreadyHit = new ArrayList<>();
        LivingEntity current = primary;
        for (int i = 0; i <= extraJumps && current != null; i++)
        {
            float dmg = damage * (float) Math.pow(FALLOFF, i);
            current.hurt(level.damageSources().indirectMagic(caster, caster), dmg);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, current.getX(), current.getY() + 1, current.getZ(), 12, 0.3, 0.3, 0.3, 0.0);
            alreadyHit.add(current);

            LivingEntity from = current;
            AABB search = current.getBoundingBox().inflate(JUMP_RANGE);
            current = level.getEntitiesOfClass(LivingEntity.class, search, e -> e != caster && !alreadyHit.contains(e))
                    .stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(from))).orElse(null);
        }

        level.playSound(null, primary.blockPosition(), SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 1.0f, 1.4f);
    }
}
