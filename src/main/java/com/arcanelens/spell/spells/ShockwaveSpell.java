package com.arcanelens.spell.spells;

import com.arcanelens.api.spell.Spell;
import com.arcanelens.spell.SpellScaling;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ShockwaveSpell implements Spell
{
    private static final double RADIUS = 6.0;
    private static final double BASE_PUSH_STRENGTH = 0.6;
    private static final double VERTICAL_LIFT = 0.25;

    @Override
    public ItemStack getIcon()
    {
        return Items.FEATHER.getDefaultInstance();
    }

    @Override
    public Component getDisplayName()
    {
        return Component.literal("Shockwave");
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
        double radius = RADIUS * SpellScaling.effectMultiplier(stackCount);
        double pushStrength = BASE_PUSH_STRENGTH * SpellScaling.effectMultiplier(stackCount);
        Vec3 center = caster.position();

        level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 1, center.z, 4, 0.3, 0.2, 0.3, 0.0);
        level.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 0.5, center.z, 40, 1.0, 0.2, 1.0, 0.1);
        level.playSound(null, caster.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 1.6f);

        AABB area = new AABB(center, center).inflate(radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != caster))
        {
            Vec3 away = target.position().subtract(center);
            double dist = Math.max(away.length(), 0.5);
            Vec3 push = away.scale(1.0 / dist).scale(pushStrength).add(0, VERTICAL_LIFT, 0);
            target.push(push.x, push.y, push.z);
            target.hurtMarked = true;

            if (target instanceof ServerPlayer sp)
            {
                sp.connection.send(new ClientboundSetEntityMotionPacket(sp));
            }
        }
    }
}
