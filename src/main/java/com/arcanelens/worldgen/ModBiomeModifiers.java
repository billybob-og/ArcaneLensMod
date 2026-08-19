package com.arcanelens.worldgen;

import com.arcanelens.ArcaneLens;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBiomeModifiers
{
    public static final ResourceKey<BiomeModifier> ADD_ARCANE_ORE =
            ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(ArcaneLens.MODID, "add_arcane_ore"));

    public static void bootstrap(BootstapContext<BiomeModifier> context)
    {
        HolderGetter<Biome> biomes = context.lookup(net.minecraft.core.registries.Registries.BIOME);
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(net.minecraft.core.registries.Registries.PLACED_FEATURE);

        context.register(ADD_ARCANE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.ARCANE_ORE_PLACED)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
    }
}
