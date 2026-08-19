package com.arcanelens.spell.spells;

import com.arcanelens.api.spell.Spell;
import com.arcanelens.spell.DelayedImpactScheduler;
import com.arcanelens.spell.SpellScaling;
import com.arcanelens.spell.SpellTargeting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class MeteorShowerSpell implements Spell
{
    private static final double RANGE = 20.0;
    private static final double RADIUS = 2.5;
    private static final float BASE_DAMAGE = 5.0f;
    private static final int IMPACT_INTERVAL_TICKS = 15;

    @Override
    public ItemStack getIcon()
    {
        return Items.MAGMA_CREAM.getDefaultInstance();
    }

    @Override
    public Component getDisplayName()
    {
        return Component.literal("Meteor Shower");
    }

    @Override
    public int getManaCost()
    {
        return 60;
    }

    @Override
    public int getBaseCooldownTicks()
    {
        return 100;
    }

    @Override
    public void cast(ServerPlayer caster, ItemStack lensStack, ServerLevel level, int stackCount)
    {
        Vec3 impact = SpellTargeting.getImpactPoint(caster, RANGE * SpellScaling.effectMultiplier(stackCount));
        int meteorCount = SpellScaling.scaledCastCount(stackCount, 2, 5);
        float damage = BASE_DAMAGE * SpellScaling.effectMultiplier(stackCount);

        long castTick = level.getGameTime();
        for (int i = 0; i < meteorCount; i++)
        {
            DelayedImpactScheduler.schedule(level, impact, caster, damage, RADIUS, castTick + (long) i * IMPACT_INTERVAL_TICKS);
        }
    }
}
