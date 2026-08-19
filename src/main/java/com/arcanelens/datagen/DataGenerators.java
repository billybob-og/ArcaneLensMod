package com.arcanelens.datagen;

import com.arcanelens.ArcaneLens;
import com.arcanelens.worldgen.ModBiomeModifiers;
import com.arcanelens.worldgen.ModConfiguredFeatures;
import com.arcanelens.worldgen.ModPlacedFeatures;
import com.arcanelens.worldgen.ModStructureSets;
import com.arcanelens.worldgen.ModStructures;
import com.arcanelens.worldgen.ModTemplatePools;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

@Mod.EventBusSubscriber(modid = ArcaneLens.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new ModLanguageProvider(output));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(output));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, existingFileHelper));
        ModBlockTagsProvider blockTags = new ModBlockTagsProvider(output, event.getLookupProvider(), existingFileHelper);
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(),
                new ModItemTagsProvider(output, event.getLookupProvider(), blockTags.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(), new ModLootTableProvider(output));

        RegistrySetBuilder worldgenBuilder = new RegistrySetBuilder()
                .add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap)
                .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
                .add(ForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap)
                .add(Registries.TEMPLATE_POOL, ModTemplatePools::bootstrap)
                .add(Registries.STRUCTURE, ModStructures::bootstrap)
                .add(Registries.STRUCTURE_SET, ModStructureSets::bootstrap);

        generator.addProvider(event.includeServer(),
                new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), worldgenBuilder, Set.of(ArcaneLens.MODID)));
    }
}
