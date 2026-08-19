package com.arcanelens.item;

import com.arcanelens.Config;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Worn in a Curios charm slot - curioTick() only runs while equipped, so no separate "is it worn" check
 * or event-bus tick handler is needed the way armor-gated features (see SleepingGodFlightHandler) require. */
public class MagnetCharmItem extends Item implements ICurioItem
{
    public MagnetCharmItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack)
    {
        if (!(slotContext.entity() instanceof ServerPlayer player))
        {
            return;
        }

        AABB box = player.getBoundingBox().inflate(Config.magnetCharmRadius);
        for (ItemEntity item : player.level().getEntitiesOfClass(ItemEntity.class, box))
        {
            if (!item.isAlive() || item.hasPickUpDelay())
            {
                continue;
            }

            Vec3 toPlayer = player.position().subtract(item.position());
            double distance = toPlayer.length();
            if (distance < 0.5)
            {
                continue;
            }

            item.setDeltaMovement(item.getDeltaMovement().add(toPlayer.normalize().scale(Config.magnetCharmPullStrength)));
        }
    }
}
