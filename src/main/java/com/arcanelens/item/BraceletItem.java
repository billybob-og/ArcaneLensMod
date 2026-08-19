package com.arcanelens.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.UUID;

/** Worn in its own Curios "bracelet" slot - adds a flat bonus to the wearer's max "charm" slot count via
 * Curios' own SlotAttribute mechanism (the same live attribute-modifier system CurioStacksHandler uses to
 * size every slot type), so equipping/unequipping a tier immediately grows/shrinks available charm slots -
 * no separate handler or tick logic needed. */
public class BraceletItem extends Item implements ICurioItem
{
    private final int bonusCharmSlots;

    public BraceletItem(Properties properties, int bonusCharmSlots)
    {
        super(properties);
        this.bonusCharmSlots = bonusCharmSlots;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack)
    {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(SlotAttribute.getOrCreate("charm"),
                new AttributeModifier(uuid, "Bracelet charm bonus", bonusCharmSlots, AttributeModifier.Operation.ADDITION));
        return modifiers;
    }
}
