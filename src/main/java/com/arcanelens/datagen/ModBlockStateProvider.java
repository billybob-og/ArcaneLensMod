package com.arcanelens.datagen;

import com.arcanelens.ArcaneLens;
import com.arcanelens.block.ArcaneAssemblerBlock;
import com.arcanelens.block.ArcaneAssemblerPart;
import com.arcanelens.block.FaithAltarBlock;
import com.arcanelens.block.OverloadPortalBlock;
import com.arcanelens.block.StorageConnectorBlock;
import com.arcanelens.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider
{
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper)
    {
        super(output, ArcaneLens.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels()
    {
        ResourceLocation top = new ResourceLocation(ArcaneLens.MODID, "block/inscription_workbench_top");
        ResourceLocation side = new ResourceLocation(ArcaneLens.MODID, "block/inscription_workbench_side");
        // Bottom face reuses the (fully opaque) top texture, since the side texture's
        // crenellation cutouts would otherwise punch transparent holes in the block's underside.
        ModelFile model = models().cubeBottomTop("inscription_workbench", side, top, top);
        simpleBlock(ModBlocks.INSCRIPTION_WORKBENCH.get(), model);
        simpleBlockItem(ModBlocks.INSCRIPTION_WORKBENCH.get(), model);

        // Custom Blockbench-authored models, hand-placed under assets/arcanelens/models/block/.
        // Soul Pedestal has FUEL/LIT blockstate properties, so every combination needs its own
        // variant entry (all pointing at the same model - only the BE's behavior differs by state).
        ModelFile soulPedestalModel = models().getExistingFile(modLoc("block/soul_pedestal"));
        getVariantBuilder(ModBlocks.SOUL_PEDESTAL.get()).forAllStates(state -> new ConfiguredModel[] { new ConfiguredModel(soulPedestalModel) });
        simpleBlockItem(ModBlocks.SOUL_PEDESTAL.get(), soulPedestalModel);

        ModelFile lensPedestalModel = models().getExistingFile(modLoc("block/lens_pedestal"));
        simpleBlock(ModBlocks.LENS_PEDESTAL.get(), lensPedestalModel);
        simpleBlockItem(ModBlocks.LENS_PEDESTAL.get(), lensPedestalModel);

        ModelFile arcaneOreModel = models().cubeAll("arcane_ore", new ResourceLocation(ArcaneLens.MODID, "block/arcane_ore"));
        simpleBlock(ModBlocks.ARCANE_ORE.get(), arcaneOreModel);
        simpleBlockItem(ModBlocks.ARCANE_ORE.get(), arcaneOreModel);

        ResourceLocation combinerTop = new ResourceLocation(ArcaneLens.MODID, "block/lens_combiner_top");
        ResourceLocation combinerSide = new ResourceLocation(ArcaneLens.MODID, "block/lens_combiner_side");
        ResourceLocation combinerBottom = new ResourceLocation(ArcaneLens.MODID, "block/lens_combiner_bottom");
        ModelFile lensCombinerModel = models().cubeBottomTop("lens_combiner", combinerSide, combinerBottom, combinerTop);
        simpleBlock(ModBlocks.LENS_COMBINER.get(), lensCombinerModel);
        simpleBlockItem(ModBlocks.LENS_COMBINER.get(), lensCombinerModel);

        ModelFile soulFeederModel = models().cubeAll("soul_feeder", new ResourceLocation(ArcaneLens.MODID, "block/soul_feeder"));
        simpleBlock(ModBlocks.SOUL_FEEDER.get(), soulFeederModel);
        simpleBlockItem(ModBlocks.SOUL_FEEDER.get(), soulFeederModel);

        // Reuses vanilla's polished blackstone texture to blend in with the boss room floor - not meant to
        // be seen for more than a tick in normal play anyway (see ArenaTriggerBlockEntity).
        ModelFile arenaTriggerModel = models().cubeAll("arena_trigger", mcLoc("block/polished_blackstone"));
        simpleBlock(ModBlocks.ARENA_TRIGGER.get(), arenaTriggerModel);
        simpleBlockItem(ModBlocks.ARENA_TRIGGER.get(), arenaTriggerModel);

        // Random per-placement ROTATION state (see FaithAltarBlock) - every quarter-turn points at the
        // same model, just yaw-rotated, so the block doesn't look identically oriented every time it's placed.
        ModelFile faithAltarModel = models().cubeAll("faith_altar", new ResourceLocation(ArcaneLens.MODID, "block/faith_altar"));
        getVariantBuilder(ModBlocks.FAITH_ALTAR.get()).forAllStates(state ->
                new ConfiguredModel[] { new ConfiguredModel(faithAltarModel, 0, state.getValue(FaithAltarBlock.ROTATION) * 90, false) });
        simpleBlockItem(ModBlocks.FAITH_ALTAR.get(), faithAltarModel);

        // Placeholder art: vanilla crying obsidian, closest existing texture to a mysterious/arcane
        // challenge-altar theme (also fits its glow - see GodChallengeAltarBlock's lightLevel).
        ModelFile godChallengeAltarModel = models().cubeAll("god_challenge_altar", mcLoc("block/crying_obsidian"));
        simpleBlock(ModBlocks.GOD_CHALLENGE_ALTAR.get(), godChallengeAltarModel);
        simpleBlockItem(ModBlocks.GOD_CHALLENGE_ALTAR.get(), godChallengeAltarModel);

        // Genuinely invisible admin fixtures - no elements at all, so nothing renders in-world or in
        // inventory/hand. See CommandTriggerBlock/TriggerPlateBlock.
        ModelFile commandTriggerModel = models().getBuilder("command_trigger");
        simpleBlock(ModBlocks.COMMAND_TRIGGER.get(), commandTriggerModel);
        simpleBlockItem(ModBlocks.COMMAND_TRIGGER.get(), commandTriggerModel);

        ModelFile triggerPlateModel = models().getBuilder("trigger_plate");
        simpleBlock(ModBlocks.TRIGGER_PLATE.get(), triggerPlateModel);
        simpleBlockItem(ModBlocks.TRIGGER_PLATE.get(), triggerPlateModel);

        ModelFile storageBlockModel = models().cubeAll("storage_block", new ResourceLocation(ArcaneLens.MODID, "block/storage_block"));
        simpleBlock(ModBlocks.STORAGE_BLOCK.get(), storageBlockModel);
        simpleBlockItem(ModBlocks.STORAGE_BLOCK.get(), storageBlockModel);

        // Custom Blockbench-authored model, hand-placed under assets/arcanelens/models/block/ (matching
        // the Soul Pedestal/Lens Pedestal convention). The unrotated model faces north, so horizontalBlock()
        // (the same helper vanilla's furnace-style FACING blocks use) generates the correct per-facing
        // y-rotation variants to match TerminalBlock.getStateForPlacement().
        ModelFile terminalModel = models().getExistingFile(modLoc("block/terminal"));
        horizontalBlock(ModBlocks.TERMINAL.get(), terminalModel);
        simpleBlockItem(ModBlocks.TERMINAL.get(), terminalModel);

        // Multipart: the core hub always renders, and the single arm model is reused (rotated, never
        // re-modeled) for whichever of the 6 directions StorageConnectorBlock's boolean properties say
        // are actually connected - see StorageConnectorBlock for the shape/connection logic this must
        // stay in sync with. North/east/south/west use the same y-rotation convention as vanilla's
        // furnace FACING states; up/down's x-rotation direction is an unverified guess (no way to
        // preview it here) - flip 90/270 below if the vertical arm ends up pointing the wrong way.
        ModelFile connectorCore = models().getExistingFile(modLoc("block/storage_connector_core"));
        ModelFile connectorArm = models().getExistingFile(modLoc("block/storage_connector_arm"));
        getMultipartBuilder(ModBlocks.STORAGE_CONNECTOR.get())
                .part().modelFile(connectorCore).addModel().end()
                .part().modelFile(connectorArm).addModel().condition(StorageConnectorBlock.NORTH, true).end()
                .part().modelFile(connectorArm).rotationY(90).addModel().condition(StorageConnectorBlock.EAST, true).end()
                .part().modelFile(connectorArm).rotationY(180).addModel().condition(StorageConnectorBlock.SOUTH, true).end()
                .part().modelFile(connectorArm).rotationY(270).addModel().condition(StorageConnectorBlock.WEST, true).end()
                .part().modelFile(connectorArm).rotationX(270).addModel().condition(StorageConnectorBlock.UP, true).end()
                .part().modelFile(connectorArm).rotationX(90).addModel().condition(StorageConnectorBlock.DOWN, true).end();
        simpleBlockItem(ModBlocks.STORAGE_CONNECTOR.get(), connectorCore);

        // 2-wide custom Blockbench model split into two per-block halves (the source model spanned two
        // block-widths in one file - MAIN is the anchor half with the real block entity, EXTENSION is
        // its companion, auto-placed one block over - see ArcaneAssemblerBlock). Both halves rotate
        // together with FACING using the same y-rotation convention as Terminal/Storage Connector.
        ModelFile arcaneAssemblerMainModel = models().getExistingFile(modLoc("block/arcane_assembler_main"));
        ModelFile arcaneAssemblerExtensionModel = models().getExistingFile(modLoc("block/arcane_assembler_extension"));
        getVariantBuilder(ModBlocks.ARCANE_ASSEMBLER.get()).forAllStates(state -> {
            ModelFile selectedModel = state.getValue(ArcaneAssemblerBlock.PART) == ArcaneAssemblerPart.MAIN
                    ? arcaneAssemblerMainModel : arcaneAssemblerExtensionModel;
            return new ConfiguredModel[] { new ConfiguredModel(selectedModel, 0, yRotFor(state.getValue(ArcaneAssemblerBlock.FACING)), false) };
        });
        simpleBlockItem(ModBlocks.ARCANE_ASSEMBLER.get(), arcaneAssemblerMainModel);

        ModelFile overloadCoreModel = models().cubeAll("overload_core", new ResourceLocation(ArcaneLens.MODID, "block/overload_core"));
        simpleBlock(ModBlocks.OVERLOAD_CORE.get(), overloadCoreModel);
        simpleBlockItem(ModBlocks.OVERLOAD_CORE.get(), overloadCoreModel);

        // The frame's interior is 2 blocks tall (see OverloadCoreBlockEntity.buildPortalFrame), and the
        // author's animated portal art is a single asymmetric image per frame (top half distinct from
        // bottom half, not a repeating tile) - each half needs its own cube_all model/texture, selected by
        // the UPPER blockstate property.
        ModelFile overloadPortalTopModel = models().cubeAll("overload_portal_top", new ResourceLocation(ArcaneLens.MODID, "block/overload_portal_top"));
        ModelFile overloadPortalBottomModel = models().cubeAll("overload_portal_bottom", new ResourceLocation(ArcaneLens.MODID, "block/overload_portal_bottom"));
        getVariantBuilder(ModBlocks.OVERLOAD_PORTAL.get()).forAllStates(state -> new ConfiguredModel[] {
                new ConfiguredModel(state.getValue(OverloadPortalBlock.UPPER) ? overloadPortalTopModel : overloadPortalBottomModel)
        });

        ModelFile sleepingGodRelicAltarModel = models().cubeAll("sleeping_god_relic_altar", modLoc("block/sleeping_god_relic_altar"));
        simpleBlock(ModBlocks.SLEEPING_GOD_RELIC_ALTAR.get(), sleepingGodRelicAltarModel);
        simpleBlockItem(ModBlocks.SLEEPING_GOD_RELIC_ALTAR.get(), sleepingGodRelicAltarModel);

        // Custom Blockbench-authored model, hand-placed under assets/arcanelens/models/block/ (same
        // convention as Soul/Lens Pedestal above).
        ModelFile hungerFaithAltarModel = models().getExistingFile(modLoc("block/hunger_faith_altar"));
        simpleBlock(ModBlocks.HUNGER_FAITH_ALTAR.get(), hungerFaithAltarModel);
        simpleBlockItem(ModBlocks.HUNGER_FAITH_ALTAR.get(), hungerFaithAltarModel);

        ModelFile fertilityFaithAltarModel = models().cubeAll("fertility_faith_altar", modLoc("block/fertility_faith_altar"));
        simpleBlock(ModBlocks.FERTILITY_FAITH_ALTAR.get(), fertilityFaithAltarModel);
        simpleBlockItem(ModBlocks.FERTILITY_FAITH_ALTAR.get(), fertilityFaithAltarModel);

        // Placeholder art: vanilla chiseled deepslate, closest existing texture to a war-monument theme.
        ModelFile warFaithAltarModel = models().cubeAll("war_faith_altar", mcLoc("block/chiseled_deepslate"));
        simpleBlock(ModBlocks.WAR_FAITH_ALTAR.get(), warFaithAltarModel);
        simpleBlockItem(ModBlocks.WAR_FAITH_ALTAR.get(), warFaithAltarModel);

        // Placeholder art: vanilla gold block, closest existing texture to a sun theme.
        ModelFile sunFaithAltarModel = models().cubeAll("sun_faith_altar", mcLoc("block/gold_block"));
        simpleBlock(ModBlocks.SUN_FAITH_ALTAR.get(), sunFaithAltarModel);
        simpleBlockItem(ModBlocks.SUN_FAITH_ALTAR.get(), sunFaithAltarModel);

        ModelFile theaterFaithAltarModel = models().cubeAll("theater_faith_altar", modLoc("block/theater_faith_altar"));
        simpleBlock(ModBlocks.THEATER_FAITH_ALTAR.get(), theaterFaithAltarModel);
        simpleBlockItem(ModBlocks.THEATER_FAITH_ALTAR.get(), theaterFaithAltarModel);
    }

    /** Matches the y-rotation convention horizontalBlock() itself produces for FACING (north=0), used
     * here since a single horizontalBlock() call can't also branch on the PART property. */
    private static int yRotFor(Direction facing)
    {
        return switch (facing)
        {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
    }
}
