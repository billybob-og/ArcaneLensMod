package com.arcanelens.worldgen;

import com.arcanelens.ArcaneLens;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

public class ModPlacedFeatures
{
    public static final ResourceKey<PlacedFeature> ARCANE_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(ArcaneLens.MODID, "arcane_ore_placed"));

    public static void bootstrap(BootstapContext<PlacedFeature> context)
    {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        context.register(ARCANE_ORE_PLACED, new PlacedFeature(
                configuredFeatures.getOrThrow(ModConfiguredFeatures.ARCANE_ORE),
                List.of(
                        CountPlacement.of(4),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(-32), VerticalAnchor.absolute(64)),
                        BiomeFilter.biome())));
    }
}
