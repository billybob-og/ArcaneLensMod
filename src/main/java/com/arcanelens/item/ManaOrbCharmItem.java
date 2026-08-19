package com.arcanelens.item;

import com.arcanelens.Config;
import com.arcanelens.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Worn in a Curios charm slot - passively tops up mana in any Magic Lens carried in the wearer's
 * inventory, using the same NBT-backed mana API a Lens Pedestal charges a held lens with (see
 * MagicLensItem.getMana/setMana and LensPedestalBlockEntity.tick()), without needing a pedestal setup. */
public class ManaOrbCharmItem extends Item implements ICurioItem
{
    public ManaOrbCharmItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack)
    {
        if (!(slotContext.entity() instanceof ServerPlayer player) || player.tickCount % Config.manaCharmIntervalTicks != 0)
        {
            return;
        }

        for (ItemStack invStack : player.getInventory().items)
        {
            if (!invStack.is(ModItems.MAGIC_LENS.get()))
            {
                continue;
            }

            int current = MagicLensItem.getMana(invStack);
            int max = MagicLensItem.getMaxMana(invStack);
            int room = max - current;
            if (room > 0)
            {
                MagicLensItem.setMana(invStack, current + Math.min(Config.manaCharmAmountPerInterval, room));
            }
        }
    }
}
