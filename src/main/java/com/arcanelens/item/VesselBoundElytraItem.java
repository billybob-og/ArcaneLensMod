package com.arcanelens.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * The Vessel-Bound chestplate with an elytra fused in (see the smithing recipe). Everything armor-related is
 * inherited from VesselBoundArmorItem - same stats, same body texture, and it still counts as the set's
 * chestplate for VesselBoundFlightHandler, so the Faith-scaled flight passive stacks with real gliding. The
 * wings themselves are drawn by VesselBoundElytraLayer; this class only makes the worn item glide-capable.
 * Gliding wears the chestplate down like a vanilla elytra does, and it can't glide once it's one hit from breaking.
 */
public class VesselBoundElytraItem extends VesselBoundArmorItem
{
    public VesselBoundElytraItem(Properties properties)
    {
        super(Type.CHESTPLATE, properties);
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity)
    {
        return stack.getDamageValue() < stack.getMaxDamage() - 1;
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks)
    {
        if (!entity.level().isClientSide)
        {
            int nextFlightTick = flightTicks + 1;
            if (nextFlightTick % 10 == 0)
            {
                if (nextFlightTick % 20 == 0)
                {
                    stack.hurtAndBreak(1, entity, e -> e.broadcastBreakEvent(EquipmentSlot.CHEST));
                }
                entity.gameEvent(GameEvent.ELYTRA_GLIDE);
            }
        }
        return true;
    }
}
