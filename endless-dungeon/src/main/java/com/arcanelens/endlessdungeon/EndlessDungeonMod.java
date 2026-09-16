package com.arcanelens.endlessdungeon;

import com.arcanelens.endlessdungeon.block.DungeonPortalBlock;
import com.arcanelens.endlessdungeon.capability.EndlessDungeonCapabilityHandler;
import com.arcanelens.endlessdungeon.dungeon.DungeonEntrancePlacer;
import com.arcanelens.endlessdungeon.dungeon.DungeonFrontierTrigger;
import com.arcanelens.endlessdungeon.registry.ModBlocks;
import com.arcanelens.endlessdungeon.registry.ModItems;
import com.arcanelens.endlessdungeon.worldgen.DungeonSpawnHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/** The Endless Dungeon DLC's own mod entry point - a separate mod id/jar depending on arcanelens (see
 * mods.toml), not the base mod itself. The multi-project Gradle setup's direct project dependency on the
 * base mod is what lets DungeonFrontierTrigger call arcanelens's own IncrementalStructurePlacer directly -
 * see the dungeon package for the actual generation system (ConnectorScanner, DungeonGraphSavedData,
 * RoomAligner, DungeonFrontierTrigger). */
@Mod(EndlessDungeonMod.MODID)
public class EndlessDungeonMod
{
    public static final String MODID = "endless_dungeon";
    private static final Logger LOGGER = LogUtils.getLogger();

    public EndlessDungeonMod(FMLJavaModLoadingContext context)
    {
        LOGGER.info("Endless Dungeon addon loaded");

        IEventBus modEventBus = context.getModEventBus();
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        modEventBus.addListener(EndlessDungeonCapabilityHandler::registerCapabilities);

        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, EndlessDungeonCapabilityHandler::onAttachCapabilities);
        MinecraftForge.EVENT_BUS.addListener(DungeonFrontierTrigger::onLevelTick);
        MinecraftForge.EVENT_BUS.addListener(DungeonEntrancePlacer::onPlayerChangedDimension);
        MinecraftForge.EVENT_BUS.addListener(DungeonEntrancePlacer::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(DungeonPortalBlock::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(DungeonSpawnHandler::onLevelTick);
    }
}
