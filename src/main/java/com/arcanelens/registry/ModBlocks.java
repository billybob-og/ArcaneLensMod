package com.arcanelens.registry;

import com.arcanelens.ArcaneLens;
import com.arcanelens.block.ArcaneAssemblerBlock;
import com.arcanelens.block.ArcaneOreBlock;
import com.arcanelens.block.ArenaTriggerBlock;
import com.arcanelens.block.CommandTriggerBlock;
import com.arcanelens.block.FaithAltarBlock;
import com.arcanelens.block.FertilityFaithAltarBlock;
import com.arcanelens.block.HungerFaithAltarBlock;
import com.arcanelens.block.InscriptionWorkbenchBlock;
import com.arcanelens.block.LensCombinerBlock;
import com.arcanelens.block.LensPedestalBlock;
import com.arcanelens.block.OverloadCoreBlock;
import com.arcanelens.block.OverloadPortalBlock;
import com.arcanelens.block.SleepingGodRelicBlock;
import com.arcanelens.block.SoulFeederBlock;
import com.arcanelens.block.SoulPedestalBlock;
import com.arcanelens.block.StorageBlock;
import com.arcanelens.block.StorageConnectorBlock;
import com.arcanelens.block.SunFaithAltarBlock;
import com.arcanelens.block.TerminalBlock;
import com.arcanelens.block.TheaterFaithAltarBlock;
import com.arcanelens.block.TriggerPlateBlock;
import com.arcanelens.block.WarFaithAltarBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ArcaneLens.MODID);

    public static final RegistryObject<Block> INSCRIPTION_WORKBENCH = BLOCKS.register("inscription_workbench",
            () -> new InscriptionWorkbenchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5f)));

    public static final RegistryObject<Block> SOUL_PEDESTAL = BLOCKS.register("soul_pedestal",
            () -> new SoulPedestalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(2.0f)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(SoulPedestalBlock.LIT) ? 15 : 0)));

    public static final RegistryObject<Block> LENS_PEDESTAL = BLOCKS.register("lens_pedestal",
            () -> new LensPedestalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).noOcclusion()));

    public static final RegistryObject<Block> ARCANE_ORE = BLOCKS.register("arcane_ore",
            () -> new ArcaneOreBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f, 3.0f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> LENS_COMBINER = BLOCKS.register("lens_combiner",
            () -> new LensCombinerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.5f)));

    public static final RegistryObject<Block> SOUL_FEEDER = BLOCKS.register("soul_feeder",
            () -> new SoulFeederBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(2.0f)));

    // Not meant to be crafted/obtained normally - placed only in the boss entrance room's build, and
    // self-destructs (replaces itself with polished blackstone) the first time its chunk loads for real.
    public static final RegistryObject<Block> ARENA_TRIGGER = BLOCKS.register("arena_trigger",
            () -> new ArenaTriggerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f)));

    public static final RegistryObject<Block> FAITH_ALTAR = BLOCKS.register("faith_altar",
            () -> new FaithAltarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(3.5f).lightLevel(state -> 6)));

    // Invisible admin fixtures, not meant to be crafted/found normally - obtainable via /give only, matching
    // vanilla's own Command Block being hidden from the creative inventory. See CommandTriggerBlock/
    // TriggerPlateBlock for why they're unbreakable/blast-immune like a real Command Block.
    public static final RegistryObject<Block> COMMAND_TRIGGER = BLOCKS.register("command_trigger",
            () -> new CommandTriggerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE)
                    .noCollission().noOcclusion().strength(-1.0f, 3600000.0f).noLootTable()));

    public static final RegistryObject<Block> TRIGGER_PLATE = BLOCKS.register("trigger_plate",
            () -> new TriggerPlateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE)
                    .noCollission().noOcclusion().strength(-1.0f, 3600000.0f).noLootTable()));

    public static final RegistryObject<Block> STORAGE_BLOCK = BLOCKS.register("storage_block",
            () -> new StorageBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(2.5f)));

    public static final RegistryObject<Block> STORAGE_CONNECTOR = BLOCKS.register("storage_connector",
            () -> new StorageConnectorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(2.0f).noOcclusion()));

    public static final RegistryObject<Block> TERMINAL = BLOCKS.register("terminal",
            () -> new TerminalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(3.0f).noOcclusion()));

    public static final RegistryObject<Block> ARCANE_ASSEMBLER = BLOCKS.register("arcane_assembler",
            () -> new ArcaneAssemblerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.5f).noOcclusion()));

    public static final RegistryObject<Block> OVERLOAD_CORE = BLOCKS.register("overload_core",
            () -> new OverloadCoreBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(3.5f).noOcclusion()));

    // Walkable, unbreakable by normal means - same "invulnerable admin fixture" properties as
    // CommandTriggerBlock/TriggerPlateBlock, so a portal pair can't be half-destroyed by accident. No
    // BlockItem registered (see ModItems.java) - matches vanilla portal blocks not being directly obtainable.
    public static final RegistryObject<Block> OVERLOAD_PORTAL = BLOCKS.register("overload_portal",
            () -> new OverloadPortalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                    .noCollission().noOcclusion().strength(-1.0f, 3600000.0f).noLootTable()));

    // Not meant to be crafted/obtained normally - hand-placed only inside the Sleeping God Relic Shrine
    // (see RelicShrinePlacer). Has a BlockItem (ModItems.SLEEPING_GOD_RELIC_ALTAR) for /give access while
    // building, same "obtainable but not in the creative tab" precedent as ARENA_TRIGGER.
    public static final RegistryObject<Block> SLEEPING_GOD_RELIC_ALTAR = BLOCKS.register("sleeping_god_relic_altar",
            () -> new SleepingGodRelicBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(3.5f).noOcclusion().lightLevel(state -> 6)));

    // Unlocked by pledging to the Hunger (see PledgeAltarUnlockHandler) - the base FAITH_ALTAR stays
    // craftable/usable by everyone regardless of pledge.
    public static final RegistryObject<Block> HUNGER_FAITH_ALTAR = BLOCKS.register("hunger_faith_altar",
            () -> new HungerFaithAltarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(3.5f).noOcclusion().lightLevel(state -> 6)));

    // Unlocked by pledging to Fertility (see PledgeAltarUnlockHandler).
    public static final RegistryObject<Block> FERTILITY_FAITH_ALTAR = BLOCKS.register("fertility_faith_altar",
            () -> new FertilityFaithAltarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(3.5f).noOcclusion()));

    // Unlocked by pledging to War (see PledgeAltarUnlockHandler).
    public static final RegistryObject<Block> WAR_FAITH_ALTAR = BLOCKS.register("war_faith_altar",
            () -> new WarFaithAltarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(3.5f).noOcclusion()));

    // Unlocked by pledging to the Sun (see PledgeAltarUnlockHandler). Deliberately NOT self-illuminating
    // (unlike the other altars) - it reads its own light level to generate Faith, so a self-emitting
    // light source would trivially inflate its own reading regardless of actual sun/torch exposure.
    public static final RegistryObject<Block> SUN_FAITH_ALTAR = BLOCKS.register("sun_faith_altar",
            () -> new SunFaithAltarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(3.5f).noOcclusion()));

    // Unlocked by pledging to the Theater (see PledgeAltarUnlockHandler).
    public static final RegistryObject<Block> THEATER_FAITH_ALTAR = BLOCKS.register("theater_faith_altar",
            () -> new TheaterFaithAltarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).strength(3.5f).noOcclusion()));
}
