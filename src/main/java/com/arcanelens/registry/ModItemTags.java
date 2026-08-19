package com.arcanelens.registry;

import com.arcanelens.ArcaneLens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags
{
    // Not FoodProperties/edibility-based - sugar cane (a canonical example of "surplus crop with nothing
    // to do") isn't actually edible in vanilla. Populated in ModItemTagsProvider, datapack-overridable.
    public static final TagKey<Item> ASSEMBLER_FUEL = ItemTags.create(new ResourceLocation(ArcaneLens.MODID, "assembler_fuel"));
}
