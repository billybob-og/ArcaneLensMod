package com.arcanelens.spell.spells;

import com.arcanelens.api.spell.Spell;
import com.arcanelens.spell.SpellScaling;
import com.arcanelens.spell.SpellTargeting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class LightningStrikeSpell implements Spell
{
    private static final double RANGE = 25.0;

    @Override
    public ItemStack getIcon()
    {
        return Items.TRIDENT.getDefaultInstance();
    }

    @Override
    public Component getDisplayName()
    {
        return Component.literal("Lightning Strike");
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
        double range = RANGE * SpellScaling.effectMultiplier(stackCount);
        LivingEntity target = SpellTargeting.getTargetEntity(caster, range);
        Vec3 pos = target != null ? target.position() : SpellTargeting.getImpactPoint(caster, range);

        int boltCount = SpellScaling.scaledCastCount(stackCount, 3, 8);
        for (int i = 0; i < boltCount; i++)
        {
            double offsetX = i == 0 ? 0.0 : (level.random.nextDouble() - 0.5) * 3.0;
            double offsetZ = i == 0 ? 0.0 : (level.random.nextDouble() - 0.5) * 3.0;

            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null)
            {
                bolt.moveTo(pos.x + offsetX, pos.y, pos.z + offsetZ);
                bolt.setCause(caster);
                level.addFreshEntity(bolt);
            }
        }
    }
}
