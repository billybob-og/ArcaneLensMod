package com.arcanelens.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

/** Aurelia's boss drop - a sword with extra melee reach, so it earns the name "spear". */
public class AureliasSpearItem extends SwordItem
{
    private static final UUID REACH_MODIFIER_ID = UUID.fromString("5c1f7a3e-2b64-4d0a-9a51-3e8d7c6b1f42");
    private static final double REACH_BONUS = 2.0;

    public AureliasSpearItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties)
    {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack)
    {
        Multimap<Attribute, AttributeModifier> base = super.getAttributeModifiers(slot, stack);
        if (slot != EquipmentSlot.MAINHAND)
        {
            return base;
        }

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(base);
        builder.put(ForgeMod.ENTITY_REACH.get(),
                new AttributeModifier(REACH_MODIFIER_ID, "Spear reach", REACH_BONUS, AttributeModifier.Operation.ADDITION));
        return builder.build();
    }
}
