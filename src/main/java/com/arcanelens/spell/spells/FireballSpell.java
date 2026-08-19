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
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class FireballSpell implements Spell
{
    private static final double RANGE = 20.0;
    private static final double RADIUS = 3.0;
    private static final float BASE_DAMAGE = 6.0f;

    @Override
    public ItemStack getIcon()
    {
        return Items.FIRE_CHARGE.getDefaultInstance();
    }

    @Override
    public Component getDisplayName()
    {
        return Component.literal("Fireball");
    }

    @Override
    public int getManaCost()
    {
        return 50;
    }

    @Override
    public int getBaseCooldownTicks()
    {
        return 40;
    }

    @Override
    public void cast(ServerPlayer caster, ItemStack lensStack, ServerLevel level, int stackCount)
    {
        float damage = BASE_DAMAGE * SpellScaling.effectMultiplier(stackCount);
        Vec3 impact = SpellTargeting.getImpactPoint(caster, RANGE * SpellScaling.effectMultiplier(stackCount));

        level.sendParticles(ParticleTypes.FLAME, impact.x, impact.y, impact.z, 40, 0.6, 0.6, 0.6, 0.02);
        level.sendParticles(ParticleTypes.LAVA, impact.x, impact.y, impact.z, 10, 0.4, 0.4, 0.4, 0.0);
        level.playSound(null, impact.x, impact.y, impact.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 1.2f);

        AABB area = new AABB(impact, impact).inflate(RADIUS);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != caster);
        for (LivingEntity target : targets)
        {
            target.hurt(level.damageSources().indirectMagic(caster, caster), damage);
            target.setSecondsOnFire(4);
        }
    }
}
