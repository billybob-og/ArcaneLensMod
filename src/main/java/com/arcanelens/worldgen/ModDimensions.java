package com.arcanelens.worldgen;

import com.arcanelens.ArcaneLens;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Dimensions are purely datapack-driven (data/arcanelens/dimension(_type)/*.json) - these keys just need to
 * match their location so runtime code can look the level up via server.getLevel(...).
 */
public class ModDimensions
{
    public static final ResourceKey<Level> POCKET_DIMENSION_KEY =
            ResourceKey.create(Registries.DIMENSION, new ResourceLocation(ArcaneLens.MODID, "pocket_dimension"));

    public static final ResourceKey<Level> BROKEN_VESSEL_KEY =
            ResourceKey.create(Registries.DIMENSION, new ResourceLocation(ArcaneLens.MODID, "broken_vessel"));

    public static final ResourceKey<Level> GOD_CHALLENGE_HUB_KEY =
            ResourceKey.create(Registries.DIMENSION, new ResourceLocation(ArcaneLens.MODID, "god_challenge_hub"));
}
