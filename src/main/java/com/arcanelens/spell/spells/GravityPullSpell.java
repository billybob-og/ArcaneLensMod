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

public class GravityPullSpell implements Spell
{
    private static final double RADIUS = 6.0;
    private static final double BASE_PULL_STRENGTH = 0.35;

    @Override
    public ItemStack getIcon()
    {
        return Items.SLIME_BALL.getDefaultInstance();
    }

    @Override
    public Component getDisplayName()
    {
        return Component.literal("Gravity Pull");
    }

    @Override
    public int getManaCost()
    {
        return 45;
    }

    @Override
    public int getBaseCooldownTicks()
    {
        return 70;
    }

    @Override
    public void cast(ServerPlayer caster, ItemStack lensStack, ServerLevel level, int stackCount)
    {
        double radius = RADIUS * SpellScaling.effectMultiplier(stackCount);
        double pullStrength = BASE_PULL_STRENGTH * SpellScaling.effectMultiplier(stackCount);
        Vec3 center = caster.position();

        level.sendParticles(ParticleTypes.PORTAL, center.x, center.y + 1, center.z, 40, 1.0, 1.0, 1.0, 0.15);
        level.playSound(null, caster.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0f, 1.0f);

        AABB area = new AABB(center, center).inflate(radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != caster))
        {
            Vec3 toCenter = center.subtract(target.position());
            if (toCenter.lengthSqr() < 1.0e-4)
            {
                continue;
            }
            Vec3 pull = toCenter.normalize().scale(pullStrength);
            target.push(pull.x, pull.y + 0.05, pull.z);
            target.hurtMarked = true;

            if (target instanceof ServerPlayer sp)
            {
                sp.connection.send(new ClientboundSetEntityMotionPacket(sp));
            }
        }
    }
}
