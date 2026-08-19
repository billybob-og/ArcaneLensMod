package com.arcanelens.registry;

import com.arcanelens.ArcaneLens;
import com.arcanelens.block.entity.ArcaneAssemblerBlockEntity;
import com.arcanelens.block.entity.ArcaneAssemblerExtensionBlockEntity;
import com.arcanelens.block.entity.ArenaTriggerBlockEntity;
import com.arcanelens.block.entity.CommandTriggerBlockEntity;
import com.arcanelens.block.entity.FaithAltarBlockEntity;
import com.arcanelens.block.entity.FertilityFaithAltarBlockEntity;
import com.arcanelens.block.entity.HungerFaithAltarBlockEntity;
import com.arcanelens.block.entity.InscriptionWorkbenchBlockEntity;
import com.arcanelens.block.entity.LensCombinerBlockEntity;
import com.arcanelens.block.entity.LensPedestalBlockEntity;
import com.arcanelens.block.entity.OverloadCoreBlockEntity;
import com.arcanelens.block.entity.SoulFeederBlockEntity;
import com.arcanelens.block.entity.SoulPedestalBlockEntity;
import com.arcanelens.block.entity.StorageBlockEntity;
import com.arcanelens.block.entity.SunFaithAltarBlockEntity;
import com.arcanelens.block.entity.TerminalBlockEntity;
import com.arcanelens.block.entity.TheaterFaithAltarBlockEntity;
import com.arcanelens.block.entity.TriggerPlateBlockEntity;
import com.arcanelens.block.entity.WarFaithAltarBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ArcaneLens.MODID);

    public static final RegistryObject<BlockEntityType<InscriptionWorkbenchBlockEntity>> INSCRIPTION_WORKBENCH = BLOCK_ENTITIES.register(
            "inscription_workbench",
            () -> BlockEntityType.Builder.of(InscriptionWorkbenchBlockEntity::new, ModBlocks.INSCRIPTION_WORKBENCH.get()).build(null));

    public static final RegistryObject<BlockEntityType<SoulPedestalBlockEntity>> SOUL_PEDESTAL = BLOCK_ENTITIES.register(
            "soul_pedestal",
            () -> BlockEntityType.Builder.of(SoulPedestalBlockEntity::new, ModBlocks.SOUL_PEDESTAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<LensPedestalBlockEntity>> LENS_PEDESTAL = BLOCK_ENTITIES.register(
            "lens_pedestal",
            () -> BlockEntityType.Builder.of(LensPedestalBlockEntity::new, ModBlocks.LENS_PEDESTAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<LensCombinerBlockEntity>> LENS_COMBINER = BLOCK_ENTITIES.register(
            "lens_combiner",
            () -> BlockEntityType.Builder.of(LensCombinerBlockEntity::new, ModBlocks.LENS_COMBINER.get()).build(null));

    public static final RegistryObject<BlockEntityType<SoulFeederBlockEntity>> SOUL_FEEDER = BLOCK_ENTITIES.register(
            "soul_feeder",
            () -> BlockEntityType.Builder.of(SoulFeederBlockEntity::new, ModBlocks.SOUL_FEEDER.get()).build(null));

    public static final RegistryObject<BlockEntityType<ArenaTriggerBlockEntity>> ARENA_TRIGGER = BLOCK_ENTITIES.register(
            "arena_trigger",
            () -> BlockEntityType.Builder.of(ArenaTriggerBlockEntity::new, ModBlocks.ARENA_TRIGGER.get()).build(null));

    public static final RegistryObject<BlockEntityType<FaithAltarBlockEntity>> FAITH_ALTAR = BLOCK_ENTITIES.register(
            "faith_altar",
            () -> BlockEntityType.Builder.of(FaithAltarBlockEntity::new, ModBlocks.FAITH_ALTAR.get()).build(null));

    public static final RegistryObject<BlockEntityType<CommandTriggerBlockEntity>> COMMAND_TRIGGER = BLOCK_ENTITIES.register(
            "command_trigger",
            () -> BlockEntityType.Builder.of(CommandTriggerBlockEntity::new, ModBlocks.COMMAND_TRIGGER.get()).build(null));

    public static final RegistryObject<BlockEntityType<TriggerPlateBlockEntity>> TRIGGER_PLATE = BLOCK_ENTITIES.register(
            "trigger_plate",
            () -> BlockEntityType.Builder.of(TriggerPlateBlockEntity::new, ModBlocks.TRIGGER_PLATE.get()).build(null));

    public static final RegistryObject<BlockEntityType<StorageBlockEntity>> STORAGE_BLOCK = BLOCK_ENTITIES.register(
            "storage_block",
            () -> BlockEntityType.Builder.of(StorageBlockEntity::new, ModBlocks.STORAGE_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<TerminalBlockEntity>> TERMINAL = BLOCK_ENTITIES.register(
            "terminal",
            () -> BlockEntityType.Builder.of(TerminalBlockEntity::new, ModBlocks.TERMINAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<ArcaneAssemblerBlockEntity>> ARCANE_ASSEMBLER = BLOCK_ENTITIES.register(
            "arcane_assembler",
            () -> BlockEntityType.Builder.of(ArcaneAssemblerBlockEntity::new, ModBlocks.ARCANE_ASSEMBLER.get()).build(null));

    public static final RegistryObject<BlockEntityType<ArcaneAssemblerExtensionBlockEntity>> ARCANE_ASSEMBLER_EXTENSION = BLOCK_ENTITIES.register(
            "arcane_assembler_extension",
            () -> BlockEntityType.Builder.of(ArcaneAssemblerExtensionBlockEntity::new, ModBlocks.ARCANE_ASSEMBLER.get()).build(null));

    public static final RegistryObject<BlockEntityType<OverloadCoreBlockEntity>> OVERLOAD_CORE = BLOCK_ENTITIES.register(
            "overload_core",
            () -> BlockEntityType.Builder.of(OverloadCoreBlockEntity::new, ModBlocks.OVERLOAD_CORE.get()).build(null));

    public static final RegistryObject<BlockEntityType<HungerFaithAltarBlockEntity>> HUNGER_FAITH_ALTAR = BLOCK_ENTITIES.register(
            "hunger_faith_altar",
            () -> BlockEntityType.Builder.of(HungerFaithAltarBlockEntity::new, ModBlocks.HUNGER_FAITH_ALTAR.get()).build(null));

    public static final RegistryObject<BlockEntityType<FertilityFaithAltarBlockEntity>> FERTILITY_FAITH_ALTAR = BLOCK_ENTITIES.register(
            "fertility_faith_altar",
            () -> BlockEntityType.Builder.of(FertilityFaithAltarBlockEntity::new, ModBlocks.FERTILITY_FAITH_ALTAR.get()).build(null));

    public static final RegistryObject<BlockEntityType<WarFaithAltarBlockEntity>> WAR_FAITH_ALTAR = BLOCK_ENTITIES.register(
            "war_faith_altar",
            () -> BlockEntityType.Builder.of(WarFaithAltarBlockEntity::new, ModBlocks.WAR_FAITH_ALTAR.get()).build(null));

    public static final RegistryObject<BlockEntityType<SunFaithAltarBlockEntity>> SUN_FAITH_ALTAR = BLOCK_ENTITIES.register(
            "sun_faith_altar",
            () -> BlockEntityType.Builder.of(SunFaithAltarBlockEntity::new, ModBlocks.SUN_FAITH_ALTAR.get()).build(null));

    public static final RegistryObject<BlockEntityType<TheaterFaithAltarBlockEntity>> THEATER_FAITH_ALTAR = BLOCK_ENTITIES.register(
            "theater_faith_altar",
            () -> BlockEntityType.Builder.of(TheaterFaithAltarBlockEntity::new, ModBlocks.THEATER_FAITH_ALTAR.get()).build(null));
}
