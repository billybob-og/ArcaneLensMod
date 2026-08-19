package com.arcanelens.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

/**
 * Plain ArmorItem, but immune to burning up as a dropped ItemEntity - needed since the boss room's
 * lightning/fire attacks (from both the boss and Chain Lightning) can otherwise destroy its own loot
 * before the player collects it.
 */
public class FireResistantArmorItem extends ArmorItem
{
    public FireResistantArmorItem(ArmorMaterial material, Type type, Properties properties)
    {
        super(material, type, properties);
    }

    @Override
    public boolean isFireResistant()
    {
        return true;
    }
}
