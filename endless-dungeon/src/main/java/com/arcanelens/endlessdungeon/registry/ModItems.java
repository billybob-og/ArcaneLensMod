package com.arcanelens.endlessdungeon.registry;

import com.arcanelens.endlessdungeon.EndlessDungeonMod;
import com.arcanelens.endlessdungeon.item.DungeonTeleporterItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Mirrors the base mod's own ModItems.java registration pattern (DeferredRegister<Item>). */
public class ModItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, EndlessDungeonMod.MODID);

    /** Plain crafting material - godly-chest-exclusive, no behavior of its own. */
    public static final RegistryObject<Item> PORTAL_SHARD = ITEMS.register("portal_shard",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> DUNGEON_TELEPORTER = ITEMS.register("dungeon_teleporter",
            () -> new DungeonTeleporterItem(new Item.Properties().stacksTo(1)));
}
