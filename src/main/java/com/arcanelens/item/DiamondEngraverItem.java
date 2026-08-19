package com.arcanelens.item;

import net.minecraft.world.item.Item;

public class DiamondEngraverItem extends Item
{
    public static final int MAX_USES = 5;

    public DiamondEngraverItem(Properties properties)
    {
        super(properties.durability(MAX_USES));
    }
}
