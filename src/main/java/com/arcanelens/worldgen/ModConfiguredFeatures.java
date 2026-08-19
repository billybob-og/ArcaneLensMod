package com.arcanelens.worldgen;

import com.arcanelens.ArcaneLens;
import com.arcanelens.registry.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

public class ModConfiguredFeatures
{
    public static final ResourceKey<ConfiguredFeature<?, ?>> ARCANE_ORE =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(ArcaneLens.MODID, "arcane_ore"));

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context)
    {
        List<OreConfiguration.TargetBlockState> targets = List.of(
                OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), ModBlocks.ARCANE_ORE.get().defaultBlockState()));

        context.register(ARCANE_ORE, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(targets, 6)));
    }
}
