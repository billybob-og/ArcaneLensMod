package com.arcanelens;

import com.arcanelens.advancement.ModCriteriaTriggers;
import com.arcanelens.api.spell.SpellRegistry;
import com.arcanelens.block.OverloadPortalBlock;
import com.arcanelens.block.StorageNetworkPlacementHandler;
import com.arcanelens.capability.CapabilityHandler;
import com.arcanelens.command.ArcaneLensCommand;
import com.arcanelens.god.PledgeChoiceHandler;
import com.arcanelens.god.TheaterEventTracker;
import com.arcanelens.god.WarDeathTracker;
import com.arcanelens.network.NetworkHandler;
import com.arcanelens.registry.ModBlockEntities;
import com.arcanelens.registry.ModBlocks;
import com.arcanelens.registry.ModCreativeTabs;
import com.arcanelens.registry.ModEntityTypes;
import com.arcanelens.registry.ModItems;
import com.arcanelens.registry.ModMenuTypes;
import com.arcanelens.item.AssemblerUnlockHandler;
import com.arcanelens.item.FertilityPerkHandler;
import com.arcanelens.item.HungerPerkHandler;
import com.arcanelens.item.HungersBindingsHandler;
import com.arcanelens.item.ManaBoostHandler;
import com.arcanelens.item.OverloadRitualUnlockHandler;
import com.arcanelens.item.PledgeAltarUnlockHandler;
import com.arcanelens.item.SleepingGodFlightHandler;
import com.arcanelens.item.StorageSystemUnlockHandler;
import com.arcanelens.item.SunPerkHandler;
import com.arcanelens.item.AureliaSummonHandler;
import com.arcanelens.item.TheaterAbilityHandler;
import com.arcanelens.item.TheaterHelmetPerkHandler;
import com.arcanelens.item.TheaterHelmetUnlockHandler;
import com.arcanelens.item.TheHungerHandler;
import com.arcanelens.item.VesselBoundFlightHandler;
import com.arcanelens.item.WarPerkHandler;
import com.arcanelens.spell.ArrowVolleyHandler;
import com.arcanelens.spell.DelayedImpactScheduler;
import com.arcanelens.spell.ModSpells;
import com.arcanelens.spell.SummonExpiryHandler;
import com.arcanelens.worldgen.ArenaSpawnHandler;
import com.arcanelens.worldgen.BrokenVesselSpawnHandler;
import com.arcanelens.worldgen.IncrementalStructurePlacer;
import com.arcanelens.worldgen.PocketDimensionSpawnHandler;
import com.arcanelens.worldgen.RelicShrinePlacer;
import com.arcanelens.worldgen.VesselLairPlacer;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ArcaneLens.MODID)
public class ArcaneLens
{
    public static final String MODID = "arcanelens";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ArcaneLens(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        SpellRegistry.SPELLS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        ModSpells.register();
        NetworkHandler.register();
        ModCriteriaTriggers.register();

        modEventBus.addListener(CapabilityHandler::registerCapabilities);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, CapabilityHandler::onAttachCapabilities);
        MinecraftForge.EVENT_BUS.addListener(CapabilityHandler::onPlayerDeath);
        MinecraftForge.EVENT_BUS.addListener(CapabilityHandler::onPlayerClone);
        MinecraftForge.EVENT_BUS.addListener(CapabilityHandler::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(CapabilityHandler::onPlayerRespawn);
        MinecraftForge.EVENT_BUS.addListener(CapabilityHandler::onPlayerChangedDimension);
        MinecraftForge.EVENT_BUS.addListener(ArcaneLensCommand::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(SummonExpiryHandler::onLivingTick);
        MinecraftForge.EVENT_BUS.addListener(DelayedImpactScheduler::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(ArenaSpawnHandler::onFinalizeSpawn);
        MinecraftForge.EVENT_BUS.addListener(PocketDimensionSpawnHandler::onFinalizeSpawn);
        MinecraftForge.EVENT_BUS.addListener(BrokenVesselSpawnHandler::onFinalizeSpawn);
        MinecraftForge.EVENT_BUS.addListener(ArrowVolleyHandler::onEntityJoinLevel);
        MinecraftForge.EVENT_BUS.addListener(SleepingGodFlightHandler::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(VesselBoundFlightHandler::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(TheHungerHandler::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(IncrementalStructurePlacer::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(HungersBindingsHandler::onLevelTick);
        MinecraftForge.EVENT_BUS.addListener(ManaBoostHandler::onItemCrafted);
        MinecraftForge.EVENT_BUS.addListener(StorageNetworkPlacementHandler::onBlockPlace);
        MinecraftForge.EVENT_BUS.addListener(StorageSystemUnlockHandler::onItemCrafted);
        MinecraftForge.EVENT_BUS.addListener(AssemblerUnlockHandler::onItemCrafted);
        MinecraftForge.EVENT_BUS.addListener(OverloadRitualUnlockHandler::onItemCrafted);
        MinecraftForge.EVENT_BUS.addListener(VesselLairPlacer::onPlayerChangedDimension);
        MinecraftForge.EVENT_BUS.addListener(VesselLairPlacer::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(RelicShrinePlacer::onPlayerChangedDimension);
        MinecraftForge.EVENT_BUS.addListener(RelicShrinePlacer::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(OverloadPortalBlock::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(PledgeChoiceHandler::onPlayerChangedDimension);
        MinecraftForge.EVENT_BUS.addListener(PledgeChoiceHandler::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(PledgeChoiceHandler::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(PledgeAltarUnlockHandler::onItemCrafted);
        MinecraftForge.EVENT_BUS.addListener(HungerPerkHandler::onUseItemStart);
        MinecraftForge.EVENT_BUS.addListener(HungerPerkHandler::onUseItemFinish);
        MinecraftForge.EVENT_BUS.addListener(HungerPerkHandler::onUseItemStop);
        MinecraftForge.EVENT_BUS.addListener(HungerPerkHandler::onMobEffectApplicable);
        MinecraftForge.EVENT_BUS.addListener(HungerPerkHandler::onLivingHurt);
        MinecraftForge.EVENT_BUS.addListener(FertilityPerkHandler::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(WarDeathTracker::onLivingDeath);
        MinecraftForge.EVENT_BUS.addListener(WarPerkHandler::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(WarPerkHandler::onLivingDeath);
        MinecraftForge.EVENT_BUS.addListener(SunPerkHandler::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(TheaterEventTracker::onLivingHurt);
        MinecraftForge.EVENT_BUS.addListener(TheaterHelmetUnlockHandler::onItemCrafted);
        MinecraftForge.EVENT_BUS.addListener(TheaterHelmetPerkHandler::onLivingFall);
        MinecraftForge.EVENT_BUS.addListener(TheaterHelmetPerkHandler::onLivingAttack);
        MinecraftForge.EVENT_BUS.addListener(TheaterHelmetPerkHandler::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(TheaterAbilityHandler::onRightClickEmpty);
        MinecraftForge.EVENT_BUS.addListener(TheaterAbilityHandler::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(TheaterAbilityHandler::onAttackEntity);
        MinecraftForge.EVENT_BUS.addListener(AureliaSummonHandler::onRightClickItem);
        MinecraftForge.EVENT_BUS.addListener(AureliaSummonHandler::onRightClickBlock);

        MinecraftForge.EVENT_BUS.register(this);

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // Required by ForgeChunkManager for any modid that requests forced-chunk tickets (see
        // WarpedAnchorItem) - without a registered callback Forge logs a warning and won't reinstate
        // previously-saved tickets after a world reload. A no-op body keeps every reinstated ticket as-is,
        // which is exactly what's wanted here: a bound Warped Anchor's force-load should survive restarts
        // on its own, with cleanup only ever happening through the item's own explicit unbind/stale-check
        // paths, never a blanket wipe on reload.
        ForgeChunkManager.setForcedChunkLoadingCallback(MODID, (level, ticketHelper) -> {});
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("Arcane Lens starting up");
    }
}
