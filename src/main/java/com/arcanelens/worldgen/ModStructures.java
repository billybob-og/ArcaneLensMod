package com.arcanelens.worldgen;

import com.arcanelens.ArcaneLens;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.util.random.WeightedRandomList;

import java.util.Map;
import java.util.Optional;

public class ModStructures
{
    public static final ResourceKey<Structure> CASTLE_DUNGEON =
            ResourceKey.create(Registries.STRUCTURE, new ResourceLocation(ArcaneLens.MODID, "castle_dungeon"));
    public static final ResourceKey<Structure> FAITH_SHRINE =
            ResourceKey.create(Registries.STRUCTURE, new ResourceLocation(ArcaneLens.MODID, "faith_shrine"));
    public static final ResourceKey<Structure> VESSEL_HOLLOW =
            ResourceKey.create(Registries.STRUCTURE, new ResourceLocation(ArcaneLens.MODID, "vessel_hollow"));

    // Overworld minus rivers/oceans - the entrance/library are anchored to solid ground beneath water via
    // OCEAN_FLOOR_WG (see below), but we still don't want the library itself to generate in the middle of one.
    private static final TagKey<Biome> HAS_CASTLE_DUNGEON =
            TagKey.create(Registries.BIOME, new ResourceLocation(ArcaneLens.MODID, "has_structure_castle_dungeon"));
    private static final TagKey<Biome> HAS_FAITH_SHRINE =
            TagKey.create(Registries.BIOME, new ResourceLocation(ArcaneLens.MODID, "has_structure_faith_shrine"));
    // Applies to the Warped Hollow dimension's one and only biome - see
    // data/arcanelens/tags/worldgen/biome/has_structure_vessel_hollow.json.
    private static final TagKey<Biome> HAS_VESSEL_HOLLOW =
            TagKey.create(Registries.BIOME, new ResourceLocation(ArcaneLens.MODID, "has_structure_vessel_hollow"));

    public static void bootstrap(BootstapContext<Structure> context)
    {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);

        HolderSet<Biome> validBiomes = biomes.getOrThrow(HAS_CASTLE_DUNGEON);
        Holder<StructureTemplatePool> startPool = pools.getOrThrow(ModTemplatePools.ENTRANCE);

        // Empty override = no natural monster spawns anywhere in the dungeon (same mechanism vanilla uses so
        // Ocean Monuments only spawn guardians, not random zombies) - keeps the boss arena dark without
        // random vanilla hostiles cluttering a curated encounter. The boss itself is summoned separately via
        // a command block in the arena build, not through natural spawning, so it's unaffected by this.
        Map<MobCategory, StructureSpawnOverride> noMonsterSpawns = Map.of(MobCategory.MONSTER,
                new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedRandomList.create()));

        context.register(CASTLE_DUNGEON, new JigsawStructure(
                new Structure.StructureSettings(validBiomes, noMonsterSpawns, GenerationStep.Decoration.UNDERGROUND_STRUCTURES, TerrainAdjustment.BURY),
                startPool,
                Optional.empty(),
                7,
                // Offset is relative to the projected heightmap, not an absolute Y. OCEAN_FLOOR_WG projection
                // is resolved via getFirstFreeHeight, which is one block ABOVE the solid ground (the walkable
                // air position), not the ground block itself. The entrance piece is 13 blocks tall with its
                // reserved roof jigsaw at the top (y=12), and the library's connector jigsaw sits at its own
                // floor (y=0): -13 (one more than the raw 12-block offset, to cancel out that +1) lands the
                // library's floor flush with natural ground level, with the entrance's floor 12 blocks below it.
                ConstantHeight.of(VerticalAnchor.absolute(-13)),
                false,
                Optional.of(Heightmap.Types.OCEAN_FLOOR_WG),
                // Raised from the default 80: the boss room (53x62x45) is far larger than anything else in the
                // pool and needs more distance-from-center budget to have a realistic chance of fitting. 116 is
                // the maximum allowed alongside TerrainAdjustment.BURY (which reserves 12 of the hard 128 cap).
                116));

        HolderSet<Biome> faithShrineBiomes = biomes.getOrThrow(HAS_FAITH_SHRINE);
        Holder<StructureTemplatePool> faithShrineStartPool = pools.getOrThrow(ModTemplatePools.FAITH_SHRINE_START);

        context.register(FAITH_SHRINE, new JigsawStructure(
                new Structure.StructureSettings(faithShrineBiomes, Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE),
                faithShrineStartPool,
                Optional.empty(),
                // Single static piece, no jigsaw connectors inside it - depth 1 is enough to place it and stop.
                1,
                ConstantHeight.of(VerticalAnchor.absolute(-1)),
                false,
                Optional.of(Heightmap.Types.WORLD_SURFACE_WG),
                80));

        HolderSet<Biome> vesselHollowBiomes = biomes.getOrThrow(HAS_VESSEL_HOLLOW);
        Holder<StructureTemplatePool> vesselHollowStartPool = pools.getOrThrow(ModTemplatePools.VESSEL_HOLLOW_ROOMS);

        // Same single-static-piece shape as FAITH_SHRINE (depth 1, no jigsaw connectors) - see
        // ModTemplatePools.VESSEL_HOLLOW_ROOMS. Surface-placed, not buried - both vessel_hollow_ruin_1/_2
        // were hand-built as above-ground sites, same as the old placeholder this replaces, just with real
        // art and two weighted variants now. TerrainAdjustment.NONE/SURFACE_STRUCTURES/-1 offset matches
        // FAITH_SHRINE's own choice for the same reason.
        context.register(VESSEL_HOLLOW, new JigsawStructure(
                new Structure.StructureSettings(vesselHollowBiomes, Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE),
                vesselHollowStartPool,
                Optional.empty(),
                1,
                ConstantHeight.of(VerticalAnchor.absolute(-1)),
                false,
                Optional.of(Heightmap.Types.WORLD_SURFACE_WG),
                80));
    }
}
