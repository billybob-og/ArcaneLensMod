package com.arcanelens.item;

import com.arcanelens.Config;
import com.arcanelens.capability.SkillTreeProvider;
import com.arcanelens.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Mana Boost bumps max mana directly on every currently-held Magic Lens at purchase time (see
 * ServerboundPurchaseSkillPacket) - this listener covers the other half, baking the player's current bonus
 * into any lens crafted afterward, so newly-made lenses don't start under-leveled compared to older ones.
 */
public class ManaBoostHandler
{
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event)
    {
        ItemStack stack = event.getCrafting();
        if (!stack.is(ModItems.MAGIC_LENS.get()) || !(event.getEntity() instanceof ServerPlayer player))
        {
            return;
        }

        player.getCapability(SkillTreeProvider.CAPABILITY).ifPresent(cap -> {
            int bonus = cap.getManaBoostLevel() * Config.manaBoostAmountPerLevel;
            if (bonus > 0)
            {
                MagicLensItem.setMaxMana(stack, MagicLensItem.getMaxMana(stack) + bonus);
            }
        });
    }
}
