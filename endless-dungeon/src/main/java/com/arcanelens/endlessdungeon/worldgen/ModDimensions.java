package com.arcanelens.endlessdungeon.worldgen;

import com.arcanelens.endlessdungeon.EndlessDungeonMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Datapack-driven (data/endless_dungeon/dimension(_type)/dungeon.json) - this key just needs to match
 * that location so runtime code can look the level up via server.getLevel(...).
 */
public class ModDimensions
{
    public static final ResourceKey<Level> DUNGEON_KEY =
            ResourceKey.create(Registries.DIMENSION, new ResourceLocation(EndlessDungeonMod.MODID, "dungeon"));
}
