package com.arcanelens.endlessdungeon.registry;

import com.arcanelens.endlessdungeon.EndlessDungeonMod;
import com.arcanelens.endlessdungeon.block.DungeonPortalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, EndlessDungeonMod.MODID);

    // Walkable, unbreakable by normal means - same "invulnerable admin fixture" shape as the base mod's
    // OverloadPortalBlock/CommandTriggerBlock, so a portal can't be half-destroyed by accident. No BlockItem
    // registered - matches vanilla portal blocks not being directly obtainable.
    public static final RegistryObject<Block> DUNGEON_PORTAL = BLOCKS.register("dungeon_portal",
            () -> new DungeonPortalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                    .noCollission().noOcclusion().strength(-1.0f, 3600000.0f).noLootTable()));
}
