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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BlinkSpell implements Spell
{
    private static final double MAX_RANGE = 8.0;

    @Override
    public ItemStack getIcon()
    {
        return Items.ENDER_PEARL.getDefaultInstance();
    }

    @Override
    public Component getDisplayName()
    {
        return Component.literal("Blink");
    }

    @Override
    public int getManaCost()
    {
        return 30;
    }

    @Override
    public int getBaseCooldownTicks()
    {
        return 40;
    }

    @Override
    public void cast(ServerPlayer caster, ItemStack lensStack, ServerLevel level, int stackCount)
    {
        double range = MAX_RANGE * SpellScaling.effectMultiplier(stackCount);
        Vec3 eye = caster.getEyePosition(1.0f);
        Vec3 look = caster.getViewVector(1.0f);
        Vec3 desiredEnd = eye.add(look.scale(range));

        HitResult hit = level.clip(new ClipContext(eye, desiredEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        Vec3 dest = hit.getType() != HitResult.Type.MISS ? hit.getLocation().subtract(look.scale(0.5)) : desiredEnd;

        level.sendParticles(ParticleTypes.PORTAL, caster.getX(), caster.getY() + 1, caster.getZ(), 30, 0.3, 0.5, 0.3, 0.1);
        caster.teleportTo(dest.x, dest.y, dest.z);
        level.sendParticles(ParticleTypes.PORTAL, dest.x, dest.y + 1, dest.z, 30, 0.3, 0.5, 0.3, 0.1);
        level.playSound(null, caster.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
