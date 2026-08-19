package com.arcanelens.datagen;

import com.arcanelens.ArcaneLens;
import com.arcanelens.api.spell.SpellRegistry;
import com.arcanelens.block.ArcaneAssemblerBlock;
import com.arcanelens.block.ArcaneAssemblerPart;
import com.arcanelens.registry.ModBlocks;
import com.arcanelens.registry.ModEntityTypes;
import com.arcanelens.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class ModLootTableProvider extends LootTableProvider
{
    public ModLootTableProvider(PackOutput output)
    {
        super(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(ModBlockLoot::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(ModInjectLoot::new, LootContextParamSets.CHEST),
                new LootTableProvider.SubProviderEntry(ModEntityLoot::new, LootContextParamSets.ENTITY)
        ));
    }

    public static class ModEntityLoot extends EntityLootSubProvider
    {
        protected ModEntityLoot()
        {
            super(FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        public void generate()
        {
            add(ModEntityTypes.SLEEPING_GOD.get(), LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(ModItems.SLEEPING_GOD_HELMET.get())))
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(ModItems.SLEEPING_GOD_CHESTPLATE.get())))
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(ModItems.SLEEPING_GOD_LEGGINGS.get())))
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(ModItems.SLEEPING_GOD_BOOTS.get()))));

            // Guaranteed drops, not randomized - re-fighting Broken Vessel via the Summoning Charm (see
            // VesselSummoningCharmItem, step 9) is the whole point of a re-fightable boss, so every kill
            // needs to reliably fund either upgrade sink (armor smithing or the Warped Anchor).
            add(ModEntityTypes.BROKEN_VESSEL.get(), LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(ModItems.WARPED_VESSEL_SHARD.get())
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4)))))
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(ModItems.WARPED_ATTUNEMENT_TEMPLATE.get()))));
        }

        @Override
        protected Stream<EntityType<?>> getKnownEntityTypes()
        {
            return Stream.of(ModEntityTypes.SLEEPING_GOD.get(), ModEntityTypes.BROKEN_VESSEL.get());
        }
    }

    public static class ModBlockLoot extends BlockLootSubProvider
    {
        protected ModBlockLoot()
        {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate()
        {
            dropSelf(ModBlocks.INSCRIPTION_WORKBENCH.get());
            dropSelf(ModBlocks.SOUL_PEDESTAL.get());
            dropSelf(ModBlocks.LENS_PEDESTAL.get());
            dropSelf(ModBlocks.LENS_COMBINER.get());
            dropSelf(ModBlocks.SOUL_FEEDER.get());
            dropSelf(ModBlocks.ARENA_TRIGGER.get());
            dropSelf(ModBlocks.FAITH_ALTAR.get());
            // Inventory drop-on-break handled explicitly in HungerFaithAltarBlock.onRemove, same
            // convention as SoulPedestalBlock/StorageBlock.
            dropSelf(ModBlocks.HUNGER_FAITH_ALTAR.get());
            // No inventory - Fertility's altar has no slot at all, so no onRemove drop is needed.
            dropSelf(ModBlocks.FERTILITY_FAITH_ALTAR.get());
            // No inventory - War's altar has no slot at all either.
            dropSelf(ModBlocks.WAR_FAITH_ALTAR.get());
            // No inventory - the Sun's altar has no slot at all either.
            dropSelf(ModBlocks.SUN_FAITH_ALTAR.get());
            // No inventory - the Theater's altar has no slot at all either (its Written Book interaction
            // never takes/holds the book).
            dropSelf(ModBlocks.THEATER_FAITH_ALTAR.get());
            // COMMAND_TRIGGER/TRIGGER_PLATE are deliberately excluded - both use .noLootTable(), matching
            // vanilla's own Command Block exactly (unbreakable in survival, and creative removal doesn't
            // consult loot tables anyway).
            add(ModBlocks.ARCANE_ORE.get(), createOreDrop(ModBlocks.ARCANE_ORE.get(), ModItems.RAW_ARCANE_MATERIAL.get()));

            // Inventory drop-on-break is handled explicitly in StorageBlock/TerminalBlock.onRemove
            // (matching InscriptionWorkbenchBlock's established convention), not encoded here.
            dropSelf(ModBlocks.STORAGE_BLOCK.get());
            dropSelf(ModBlocks.TERMINAL.get());
            dropSelf(ModBlocks.STORAGE_CONNECTOR.get());

            // Inventory drop-on-break handled explicitly in ArcaneAssemblerBlock.onRemove, same convention.
            // Only the MAIN half drops an item - both halves share this loot table (same Block), so
            // without this condition breaking just the EXTENSION half would duplicate the item, matching
            // vanilla's own BedBlock loot table (drops only from BedPart.HEAD, not FOOT).
            add(ModBlocks.ARCANE_ASSEMBLER.get(), createSinglePropConditionTable(
                    ModBlocks.ARCANE_ASSEMBLER.get(), ArcaneAssemblerBlock.PART, ArcaneAssemblerPart.MAIN));
        }

        @Override
        protected Iterable<Block> getKnownBlocks()
        {
            return List.of(ModBlocks.INSCRIPTION_WORKBENCH.get(), ModBlocks.SOUL_PEDESTAL.get(), ModBlocks.LENS_PEDESTAL.get(),
                    ModBlocks.LENS_COMBINER.get(), ModBlocks.ARCANE_ORE.get(), ModBlocks.SOUL_FEEDER.get(), ModBlocks.ARENA_TRIGGER.get(),
                    ModBlocks.FAITH_ALTAR.get(), ModBlocks.HUNGER_FAITH_ALTAR.get(), ModBlocks.FERTILITY_FAITH_ALTAR.get(),
                    ModBlocks.WAR_FAITH_ALTAR.get(), ModBlocks.SUN_FAITH_ALTAR.get(), ModBlocks.THEATER_FAITH_ALTAR.get(),
                    ModBlocks.STORAGE_BLOCK.get(), ModBlocks.TERMINAL.get(), ModBlocks.STORAGE_CONNECTOR.get(), ModBlocks.ARCANE_ASSEMBLER.get());
        }
    }

    /**
     * Standalone loot table fragment (not tied to a block) meant to be referenced from a chest loot table
     * later (Phase 7's dungeon), containing one weighted Ancient Spell Book entry per registered spell.
     */
    public static class ModInjectLoot implements LootTableSubProvider
    {
        @Override
        public void generate(BiConsumer<ResourceLocation, LootTable.Builder> writer)
        {
            LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1));

            ResourceLocation pocketDimensionId = new ResourceLocation(ArcaneLens.MODID, "pocket_dimension");
            for (var entry : SpellRegistry.REGISTRY.get().getEntries())
            {
                // Skill-tree-only unlock (see ServerboundPurchaseSkillPacket) - never obtainable as ordinary
                // dungeon loot.
                if (entry.getKey().location().equals(pocketDimensionId))
                {
                    continue;
                }

                CompoundTag tag = new CompoundTag();
                tag.putString("SpellId", entry.getKey().location().toString());

                pool.add(LootItem.lootTableItem(ModItems.ANCIENT_SPELL_BOOK.get())
                        .apply(SetNbtFunction.setTag(tag))
                        .setWeight(1));
            }

            writer.accept(new ResourceLocation(ArcaneLens.MODID, "inject/ancient_spell_books"), LootTable.lootTable().withPool(pool));

            // Toned-down version of what the user originally hand-stocked in the castle loot room chests:
            // same item types (cobweb, soul sand, raw arcane material) but weighted/probabilistic instead of
            // guaranteed, plus a rare chance at an Ancient Spell Book.
            LootPool.Builder castleLootRoomPool = LootPool.lootPool().setRolls(UniformGenerator.between(2, 4))
                    .add(EmptyLootItem.emptyItem().setWeight(10))
                    .add(LootItem.lootTableItem(Items.COBWEB).setWeight(6))
                    .add(LootItem.lootTableItem(Items.SOUL_SAND)
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))).setWeight(5))
                    .add(LootItem.lootTableItem(ModItems.RAW_ARCANE_MATERIAL.get()).setWeight(3))
                    .add(LootTableReference.lootTableReference(new ResourceLocation(ArcaneLens.MODID, "inject/ancient_spell_books")).setWeight(1));

            writer.accept(new ResourceLocation(ArcaneLens.MODID, "chests/castle_loot_room"), LootTable.lootTable().withPool(castleLootRoomPool));

            // Library (surface castle) chests: a guaranteed starter Magic Lens for players who find the
            // castle before crafting one themselves, plus a randomized pool weighted toward spell books
            // since the library is the mod's introductory lore location.
            LootPool.Builder libraryGuaranteedPool = LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModItems.MAGIC_LENS.get()).setWeight(1));

            LootPool.Builder libraryRandomPool = LootPool.lootPool().setRolls(UniformGenerator.between(2, 3))
                    .add(EmptyLootItem.emptyItem().setWeight(4))
                    .add(LootItem.lootTableItem(ModItems.RAW_ARCANE_MATERIAL.get())
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))).setWeight(4))
                    .add(LootItem.lootTableItem(ModItems.DIAMOND_ENGRAVER.get()).setWeight(1))
                    .add(LootTableReference.lootTableReference(new ResourceLocation(ArcaneLens.MODID, "inject/ancient_spell_books")).setWeight(5));

            writer.accept(new ResourceLocation(ArcaneLens.MODID, "chests/library"),
                    LootTable.lootTable().withPool(libraryGuaranteedPool).withPool(libraryRandomPool));
        }
    }
}
