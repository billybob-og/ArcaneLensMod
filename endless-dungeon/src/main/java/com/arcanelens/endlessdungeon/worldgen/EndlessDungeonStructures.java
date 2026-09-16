package com.arcanelens.endlessdungeon.worldgen;

import com.arcanelens.endlessdungeon.EndlessDungeonMod;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

import java.util.Map;
import java.util.Optional;

public class EndlessDungeonStructures
{
    public static final ResourceKey<Structure> DUNGEON_PORTAL =
            ResourceKey.create(Registries.STRUCTURE, new ResourceLocation(EndlessDungeonMod.MODID, "dungeon_portal"));

    // Overworld minus rivers/oceans - mirrors the base mod's own HAS_FAITH_SHRINE tag exactly.
    private static final TagKey<Biome> HAS_DUNGEON_PORTAL =
            TagKey.create(Registries.BIOME, new ResourceLocation(EndlessDungeonMod.MODID, "has_structure_dungeon_portal"));

    public static void bootstrap(BootstapContext<Structure> context)
    {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);

        HolderSet<Biome> validBiomes = biomes.getOrThrow(HAS_DUNGEON_PORTAL);
        Holder<StructureTemplatePool> startPool = pools.getOrThrow(EndlessDungeonTemplatePools.DUNGEON_PORTAL_START);

        // Single static piece, no jigsaw connectors inside it - same shape as the base mod's FAITH_SHRINE/
        // VESSEL_HOLLOW (depth 1 is enough to place it and stop).
        context.register(DUNGEON_PORTAL, new JigsawStructure(
                new Structure.StructureSettings(validBiomes, Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE),
                startPool,
                Optional.empty(),
                1,
                ConstantHeight.of(VerticalAnchor.absolute(-1)),
                false,
                Optional.of(Heightmap.Types.WORLD_SURFACE_WG),
                80));
    }
}
