package com.arcanelens.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Worn in a Curios charm slot - refreshes Fire Resistance every tick, keeping it permanently topped up
 * for as long as the charm stays equipped. */
public class BurningCharcoalCharmItem extends Item implements ICurioItem
{
    private static final int REFRESH_DURATION_TICKS = 210;

    public BurningCharcoalCharmItem(Properties properties)
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

        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, REFRESH_DURATION_TICKS, 0, false, false));
    }
}
