package com.arcanelens.client;

import com.arcanelens.ArcaneLens;
import com.arcanelens.client.gui.FlightHudOverlay;
import com.arcanelens.client.gui.SpellHudOverlay;
import com.arcanelens.client.model.AureliaModel;
import com.arcanelens.client.model.SleepingGodModel;
import com.arcanelens.client.model.TheHungerModel;
import com.arcanelens.client.renderer.AureliaCompanionRenderer;
import com.arcanelens.client.renderer.BrokenVesselRenderer;
import com.arcanelens.client.renderer.InvisibleTriggerRenderer;
import com.arcanelens.client.renderer.LensPedestalRenderer;
import com.arcanelens.client.renderer.SleepingGodRenderer;
import com.arcanelens.client.renderer.SoulPedestalRenderer;
import com.arcanelens.client.renderer.TheHungerRenderer;
import com.arcanelens.client.renderer.TheHungerSkyRenderer;
import com.arcanelens.client.renderer.TheaterCloneRenderer;
import com.arcanelens.client.screen.ArcaneAssemblerScreen;
import com.arcanelens.client.screen.CommandTriggerScreen;
import com.arcanelens.client.screen.GodPledgeScreen;
import com.arcanelens.client.screen.InscriptionWorkbenchScreen;
import com.arcanelens.client.screen.LensCombinerScreen;
import com.arcanelens.client.screen.SkillTreeScreen;
import com.arcanelens.client.screen.TerminalScreen;
import com.arcanelens.client.screen.TokenOfTheHungerScreen;
import com.arcanelens.registry.ModBlockEntities;
import com.arcanelens.registry.ModBlocks;
import com.arcanelens.registry.ModEntityTypes;
import com.arcanelens.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ArcaneLens.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents
{
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(SleepingGodModel.LAYER_LOCATION, SleepingGodModel::createBodyLayer);
        event.registerLayerDefinition(TheHungerModel.LAYER_LOCATION, TheHungerModel::createBodyLayer);
        // Stock vanilla humanoid mesh, unmodified - see BrokenVesselRenderer's javadoc for why a 128x128
        // "2x resolution" texture needs no UV remapping to use it.
        event.registerLayerDefinition(BrokenVesselRenderer.LAYER_LOCATION,
                () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64));
        // Same stock-humanoid-mesh trick, see TheaterCloneRenderer's javadoc.
        event.registerLayerDefinition(TheaterCloneRenderer.LAYER_LOCATION,
                () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64));
        event.registerLayerDefinition(AureliaModel.LAYER_LOCATION, AureliaModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.INSCRIPTION_WORKBENCH.get(), InscriptionWorkbenchScreen::new);
            MenuScreens.register(ModMenuTypes.LENS_COMBINER.get(), LensCombinerScreen::new);
            MenuScreens.register(ModMenuTypes.COMMAND_TRIGGER.get(), CommandTriggerScreen::new);
            MenuScreens.register(ModMenuTypes.TOKEN_OF_THE_HUNGER.get(), TokenOfTheHungerScreen::new);
            MenuScreens.register(ModMenuTypes.SKILL_TREE.get(), SkillTreeScreen::new);
            MenuScreens.register(ModMenuTypes.TERMINAL.get(), TerminalScreen::new);
            MenuScreens.register(ModMenuTypes.ARCANE_ASSEMBLER.get(), ArcaneAssemblerScreen::new);
            MenuScreens.register(ModMenuTypes.GOD_PLEDGE.get(), GodPledgeScreen::new);
            // The author's portal art has a transparent background (a swirl, not a solid tile) - the
            // default opaque/solid render layer would draw transparent pixels as solid black instead.
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.OVERLOAD_PORTAL.get(), RenderType.translucent());
        });
        MinecraftForge.EVENT_BUS.addListener(TheHungerSkyRenderer::onRenderLevelStage);
        MinecraftForge.EVENT_BUS.addListener(TheaterNametagHandler::onRenderNameTag);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event)
    {
        event.register(KeyBindings.CAST_SPELL);
        event.register(KeyBindings.CYCLE_SPELL);
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event)
    {
        event.registerAboveAll("arcane_lens_hud", new SpellHudOverlay());
        event.registerAboveAll("faith_flight_hud", new FlightHudOverlay());
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(ModBlockEntities.SOUL_PEDESTAL.get(), SoulPedestalRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LENS_PEDESTAL.get(), LensPedestalRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SLEEPING_GOD.get(), SleepingGodRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BROKEN_VESSEL.get(), BrokenVesselRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.THE_HUNGER.get(), TheHungerRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.HUNGER_IDOL.get(), TheHungerRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.THEATER_CLONE.get(), TheaterCloneRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.AURELIA_COMPANION.get(), AureliaCompanionRenderer::new);
        // Creative-only wireframe markers so these invisible admin fixtures are still findable while
        // building - never rendered in survival/adventure. Different colors so the two are distinguishable.
        event.registerBlockEntityRenderer(ModBlockEntities.COMMAND_TRIGGER.get(),
                context -> new InvisibleTriggerRenderer<>(context, 1.0F, 0.2F, 0.2F));
        event.registerBlockEntityRenderer(ModBlockEntities.TRIGGER_PLATE.get(),
                context -> new InvisibleTriggerRenderer<>(context, 1.0F, 0.85F, 0.2F));
    }
}
