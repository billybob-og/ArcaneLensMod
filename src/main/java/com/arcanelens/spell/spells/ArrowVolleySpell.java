package com.arcanelens.spell.spells;

import com.arcanelens.api.spell.Spell;
import com.arcanelens.spell.ArrowVolleyHandler;
import com.arcanelens.spell.SpellScaling;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Doesn't fire anything itself - marks the caster so their very next shot (from a bow, crossbow, whatever)
 * duplicates into extra arrows with a spread trajectory, like a one-time-use Multishot. See ArrowVolleyHandler.
 */
public class ArrowVolleySpell implements Spell
{
    private static final int EXPIRY_TICKS = 200;

    @Override
    public ItemStack getIcon()
    {
        return Items.ARROW.getDefaultInstance();
    }

    @Override
    public Component getDisplayName()
    {
        return Component.literal("Arrow Volley");
    }

    @Override
    public int getManaCost()
    {
        return 35;
    }

    @Override
    public int getBaseCooldownTicks()
    {
        return 100;
    }

    // Base case is 2 extra arrows (3 total, like vanilla Multishot); duplicate stacks add more on top,
    // capped at 9 extra (10 total) regardless of how many stacks beyond that.
    private static final int MAX_SCALED_CASTS = 8;

    @Override
    public void cast(ServerPlayer caster, ItemStack lensStack, ServerLevel level, int stackCount)
    {
        int extraArrows = 1 + SpellScaling.scaledCastCount(stackCount, 2, MAX_SCALED_CASTS);
        ArrowVolleyHandler.markPending(caster, extraArrows, level.getGameTime() + EXPIRY_TICKS);

        level.sendParticles(ParticleTypes.ENCHANT, caster.getX(), caster.getY() + 1, caster.getZ(), 20, 0.3, 0.5, 0.3, 0.05);
        level.playSound(null, caster.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.2f);
    }
}
