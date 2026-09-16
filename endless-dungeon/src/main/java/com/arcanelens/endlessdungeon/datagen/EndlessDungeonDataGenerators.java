package com.arcanelens.endlessdungeon.datagen;

import com.arcanelens.endlessdungeon.EndlessDungeonMod;
import com.arcanelens.endlessdungeon.worldgen.EndlessDungeonStructureSets;
import com.arcanelens.endlessdungeon.worldgen.EndlessDungeonStructures;
import com.arcanelens.endlessdungeon.worldgen.EndlessDungeonTemplatePools;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

/** The addon's datagen entry point - the vanilla worldgen registry set the overworld portal structure needs
 * (template pool/structure/structure set) plus DungeonPortalBlock's blockstate/model, mirroring the base
 * mod's own DataGenerators class. */
@Mod.EventBusSubscriber(modid = EndlessDungeonMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EndlessDungeonDataGenerators
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, existingFileHelper));

        RegistrySetBuilder worldgenBuilder = new RegistrySetBuilder()
                .add(Registries.TEMPLATE_POOL, EndlessDungeonTemplatePools::bootstrap)
                .add(Registries.STRUCTURE, EndlessDungeonStructures::bootstrap)
                .add(Registries.STRUCTURE_SET, EndlessDungeonStructureSets::bootstrap);

        generator.addProvider(event.includeServer(),
                new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), worldgenBuilder, Set.of(EndlessDungeonMod.MODID)));
    }
}
