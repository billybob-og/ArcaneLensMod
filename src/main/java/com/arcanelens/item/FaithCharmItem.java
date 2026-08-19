package com.arcanelens.item;

import com.arcanelens.Config;
import com.arcanelens.capability.FaithProvider;
import com.arcanelens.capability.FaithSync;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Worn in a Curios charm slot - a wearable mini Faith Altar, trickling a small amount of Faith over time. */
public class FaithCharmItem extends Item implements ICurioItem
{
    public FaithCharmItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack)
    {
        if (!(slotContext.entity() instanceof ServerPlayer player) || player.tickCount % Config.faithCharmIntervalTicks != 0)
        {
            return;
        }

        player.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> cap.addFaith(Config.faithCharmAmountPerInterval));
        FaithSync.syncToClient(player);
    }
}
