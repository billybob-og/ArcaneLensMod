package com.arcanelens.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Worn in a Curios charm slot - cures Poison the instant it lands, before its own damage tick can fire.
 * Vanilla has no applicable "poison resistance" effect to refresh (unlike fire resistance), so immunity is
 * implemented by immediately stripping the effect instead. */
public class AntivenomCharmItem extends Item implements ICurioItem
{
    public AntivenomCharmItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack)
    {
        if (slotContext.entity() instanceof ServerPlayer player && player.hasEffect(MobEffects.POISON))
        {
            player.removeEffect(MobEffects.POISON);
        }
    }
}
