package com.arcanelens.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Worn in a Curios charm slot - cures Wither the instant it lands, same immediate-cure approach as
 * AntivenomCharmItem (vanilla has no applicable "wither resistance" effect to refresh instead). */
public class PreservedWitherRoseCharmItem extends Item implements ICurioItem
{
    public PreservedWitherRoseCharmItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack)
    {
        if (slotContext.entity() instanceof ServerPlayer player && player.hasEffect(MobEffects.WITHER))
        {
            player.removeEffect(MobEffects.WITHER);
        }
    }
}
