package com.arcanelens.datagen;

import com.arcanelens.Config;
import com.arcanelens.registry.ModBlocks;
import com.arcanelens.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider
{
    public ModRecipeProvider(PackOutput output)
    {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> writer)
    {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.MAGIC_LENS.get())
                .requires(Items.AMETHYST_SHARD)
                .requires(Items.GLASS_PANE)
                .requires(Items.GOLD_INGOT)
                .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.INSCRIPTION_WORKBENCH.get())
                .requires(Items.LECTERN)
                .requires(Items.AMETHYST_SHARD)
                .requires(Items.AMETHYST_SHARD)
                .unlockedBy("has_lectern", has(Items.LECTERN))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.SOUL_PEDESTAL.get())
                .requires(Items.SOUL_SAND)
                .requires(Items.STONE_BRICKS)
                .requires(Items.STONE_BRICKS)
                .requires(Items.GOLD_INGOT)
                .unlockedBy("has_soul_sand", has(Items.SOUL_SAND))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.LENS_PEDESTAL.get())
                .requires(Items.AMETHYST_SHARD)
                .requires(Items.STONE_BRICKS)
                .requires(Items.STONE_BRICKS)
                .requires(Items.GOLD_INGOT)
                .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.LENS_COMBINER.get())
                .requires(ModItems.RAW_ARCANE_MATERIAL.get())
                .requires(ModItems.RAW_ARCANE_MATERIAL.get())
                .requires(Items.STONE_BRICKS)
                .requires(Items.STONE_BRICKS)
                .requires(Items.GOLD_INGOT)
                .unlockedBy("has_raw_arcane_material", has(ModItems.RAW_ARCANE_MATERIAL.get()))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.DIAMOND_ENGRAVER.get())
                .requires(Items.DIAMOND)
                .requires(Items.AMETHYST_SHARD)
                .requires(Items.STICK)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.SOUL_FEEDER.get())
                .requires(Items.HOPPER)
                .requires(Items.SOUL_SAND)
                .requires(ModItems.RAW_ARCANE_MATERIAL.get())
                .unlockedBy("has_hopper", has(Items.HOPPER))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.WEIRD_AMULET.get())
                .requires(Items.CHAIN)
                .requires(Items.CHAIN)
                .requires(Items.SOUL_SAND)
                .requires(ModItems.RAW_ARCANE_MATERIAL.get())
                .unlockedBy("has_soul_sand", has(Items.SOUL_SAND))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SUN_AND_MOON_WARD.get())
                .requires(Items.COBBLESTONE)
                .requires(Items.COAL)
                .unlockedBy("has_coal", has(Items.COAL))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.MAGNET_CHARM.get())
                .requires(Items.IRON_INGOT)
                .requires(Items.IRON_INGOT)
                .requires(Items.REDSTONE)
                .requires(ModItems.RAW_ARCANE_MATERIAL.get())
                .unlockedBy("has_raw_arcane_material", has(ModItems.RAW_ARCANE_MATERIAL.get()))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.FAITH_CHARM.get())
                .requires(Items.SOUL_SAND)
                .requires(Items.AMETHYST_SHARD)
                .requires(ModItems.RAW_ARCANE_MATERIAL.get())
                .unlockedBy("has_raw_arcane_material", has(ModItems.RAW_ARCANE_MATERIAL.get()))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.MANA_ORB_CHARM.get())
                .requires(Items.AMETHYST_SHARD)
                .requires(Items.GOLD_INGOT)
                .requires(ModItems.RAW_ARCANE_MATERIAL.get())
                .unlockedBy("has_raw_arcane_material", has(ModItems.RAW_ARCANE_MATERIAL.get()))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BURNING_CHARCOAL_CHARM.get())
                .requires(Items.MAGMA_CREAM)
                .requires(Items.BLAZE_POWDER)
                .requires(ModItems.RAW_ARCANE_MATERIAL.get())
                .unlockedBy("has_raw_arcane_material", has(ModItems.RAW_ARCANE_MATERIAL.get()))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ANTIVENOM_CHARM.get())
                .requires(Items.FERMENTED_SPIDER_EYE)
                .requires(Items.POISONOUS_POTATO)
                .requires(ModItems.RAW_ARCANE_MATERIAL.get())
                .unlockedBy("has_raw_arcane_material", has(ModItems.RAW_ARCANE_MATERIAL.get()))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.PRESERVED_WITHER_ROSE_CHARM.get())
                .requires(Items.WITHER_ROSE)
                .requires(Items.COAL)
                .requires(Items.GLASS_PANE)
                .requires(ModItems.RAW_ARCANE_MATERIAL.get())
                .unlockedBy("has_raw_arcane_material", has(ModItems.RAW_ARCANE_MATERIAL.get()))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.IRON_BRACELET.get())
                .requires(Items.IRON_INGOT)
                .requires(Items.IRON_INGOT)
                .requires(Items.LEATHER)
                .requires(Items.STRING)
                .requires(ModItems.RAW_ARCANE_MATERIAL.get())
                .unlockedBy("has_raw_arcane_material", has(ModItems.RAW_ARCANE_MATERIAL.get()))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.GOLD_BRACELET.get())
                .requires(ModItems.IRON_BRACELET.get())
                .requires(Items.GOLD_INGOT)
                .requires(Items.GOLD_INGOT)
                .requires(Items.STRING)
                .requires(ModItems.RAW_ARCANE_MATERIAL.get())
                .unlockedBy("has_iron_bracelet", has(ModItems.IRON_BRACELET.get()))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.NETHERITE_BRACELET.get())
                .requires(ModItems.GOLD_BRACELET.get())
                .requires(Items.NETHERITE_INGOT)
                .requires(Items.LEATHER)
                .requires(ModItems.RAW_ARCANE_MATERIAL.get())
                .unlockedBy("has_gold_bracelet", has(ModItems.GOLD_BRACELET.get()))
                .save(writer);

        // Arcane Ink is meant to be rare - dungeon loot is the primary source; this recipe is a deliberately
        // punishing backup path so a Nether Star (a genuine boss-kill investment) buys just one unit of ink.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ARCANE_INK.get())
                .requires(Items.NETHER_STAR)
                .requires(Items.GLASS_BOTTLE)
                .requires(ModItems.RAW_ARCANE_MATERIAL.get())
                .requires(ModItems.RAW_ARCANE_MATERIAL.get())
                .unlockedBy("has_nether_star", has(Items.NETHER_STAR))
                .save(writer);

        // Used to be a CustomRecipe (needed to bake dynamic NBT into the result) - no longer necessary now
        // that ArcaneGuideBookItem hardcodes which book to open instead of reading a tag, and a CustomRecipe
        // has no fixed ingredient slots for the vanilla Recipe Book or JEI to generate a display from, which
        // was silently hiding this recipe from both. A plain recipe fixes that for free.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ARCANE_GUIDE_BOOK.get())
                .requires(Items.BOOK)
                .requires(ModItems.RAW_ARCANE_MATERIAL.get())
                .unlockedBy("has_raw_arcane_material", has(ModItems.RAW_ARCANE_MATERIAL.get()))
                .save(writer);

        // Craftable regardless of the Storage System skill unlock - StorageSystemUnlockHandler vetoes the
        // result at craft time if the player hasn't purchased it, rather than hiding the recipe itself.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.STORAGE_BLOCK.get())
                .pattern("WWW")
                .pattern("WCW")
                .pattern("WFW")
                .define('W', Items.WARPED_STEM)
                .define('C', Items.CHEST)
                .define('F', Items.WARPED_FUNGUS)
                .unlockedBy("has_warped_fungus", has(Items.WARPED_FUNGUS))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.TERMINAL.get())
                .requires(ModBlocks.STORAGE_BLOCK.get())
                .requires(Items.REDSTONE)
                .requires(Items.GLASS_PANE)
                .requires(ModItems.RAW_ARCANE_MATERIAL.get())
                .unlockedBy("has_storage_block", has(ModBlocks.STORAGE_BLOCK.get()))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.STORAGE_CONNECTOR.get())
                .requires(Items.CHAIN)
                .requires(Items.CHAIN)
                .requires(ModItems.RAW_ARCANE_MATERIAL.get())
                .unlockedBy("has_raw_arcane_material", has(ModItems.RAW_ARCANE_MATERIAL.get()))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.STACK_UPGRADE.get())
                .requires(Items.IRON_INGOT)
                .requires(Items.IRON_INGOT)
                .requires(ModItems.RAW_ARCANE_MATERIAL.get())
                .unlockedBy("has_raw_arcane_material", has(ModItems.RAW_ARCANE_MATERIAL.get()))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SLOT_UPGRADE.get())
                .requires(Items.CHEST)
                .requires(ModItems.RAW_ARCANE_MATERIAL.get())
                .unlockedBy("has_raw_arcane_material", has(ModItems.RAW_ARCANE_MATERIAL.get()))
                .save(writer);

        // Craftable regardless of the Arcane Assembler skill unlock - AssemblerUnlockHandler vetoes the
        // result at craft time if the player hasn't purchased it, matching StorageSystemUnlockHandler.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ARCANE_ASSEMBLER.get())
                .pattern("CIC")
                .pattern("IHI")
                .pattern("CAC")
                .define('C', Items.CRAFTING_TABLE)
                .define('I', Items.IRON_INGOT)
                .define('H', Items.HOPPER)
                .define('A', ModItems.RAW_ARCANE_MATERIAL.get())
                .unlockedBy("has_raw_arcane_material", has(ModItems.RAW_ARCANE_MATERIAL.get()))
                .save(writer);

        // Craftable regardless of the Overload Ritual skill unlock - OverloadRitualUnlockHandler vetoes
        // the result at craft time if the player hasn't purchased it, matching the Storage/Assembler
        // precedent.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.OVERLOAD_CORE.get())
                .pattern("OCO")
                .pattern("CAC")
                .pattern("OCO")
                .define('O', Items.OBSIDIAN)
                .define('C', Items.CHAIN)
                .define('A', ModItems.RAW_ARCANE_MATERIAL.get())
                .unlockedBy("has_raw_arcane_material", has(ModItems.RAW_ARCANE_MATERIAL.get()))
                .save(writer);

        // Bridges both endgame currencies - even crafting one Catalyst requires having already engaged
        // with the first boss's post-game loop (Tokens of the Hunger only come from that), a soft
        // narrative gate layered on top of the hard Skill Tree gate above.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.WARPED_CATALYST.get())
                .requires(ModItems.ARCANE_INK.get())
                .requires(ModItems.TOKEN_OF_THE_HUNGER.get())
                .requires(Items.WARPED_FUNGUS)
                .unlockedBy("has_token_of_the_hunger", has(ModItems.TOKEN_OF_THE_HUNGER.get()))
                .save(writer);

        // A Warped Vessel Shard ringed by gold - gated by needing at least one Broken Vessel kill.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GOD_HUB_MEDALLION.get())
                .pattern("GGG")
                .pattern("GSG")
                .pattern("GGG")
                .define('G', Items.GOLD_INGOT)
                .define('S', ModItems.WARPED_VESSEL_SHARD.get())
                .unlockedBy("has_warped_vessel_shard", has(ModItems.WARPED_VESSEL_SHARD.get()))
                .save(writer);

        // Duplication, not creation - same "spend a Shard to copy a Template" shape as vanilla's own
        // smithing-template duplication recipes, just a plain shapeless recipe since this codebase has no
        // reason to reuse vanilla's special per-template duplication recipe class for a single mod item.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.WARPED_ATTUNEMENT_TEMPLATE.get(), 2)
                .requires(ModItems.WARPED_ATTUNEMENT_TEMPLATE.get())
                .requires(ModItems.WARPED_VESSEL_SHARD.get())
                .requires(Items.DIAMOND)
                .unlockedBy("has_warped_attunement_template", has(ModItems.WARPED_ATTUNEMENT_TEMPLATE.get()))
                .save(writer, "arcanelens:warped_attunement_template_duplicate");

        // Craftable regardless of the Warped Attunement skill unlock - OverloadRitualUnlockHandler vetoes
        // the result at craft time if the player hasn't purchased it, matching every other unlock-gated
        // recipe in this file.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.WARPED_ANCHOR.get())
                .requires(Items.BEACON)
                .requires(ModItems.WARPED_VESSEL_SHARD.get())
                .requires(ModItems.WARPED_VESSEL_SHARD.get())
                .unlockedBy("has_warped_vessel_shard", has(ModItems.WARPED_VESSEL_SHARD.get()))
                .save(writer);

        // Deliberately doesn't cost a Warped Vessel Shard, unlike the Anchor above - the Charm is the only
        // way back to more Shards once the lair's one guaranteed boss is dead, so if crafting it also
        // consumed Shards, a player who spent every Shard from a kill on armor upgrades would have no way
        // to ever earn another one: 0 Shards -> can't craft a Charm -> can't re-fight Broken Vessel -> still
        // 0 Shards, permanently. Arcane Ink (a real cost, but from an entirely separate economy) keeps
        // re-summoning a deliberate choice without that circular dependency.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.VESSEL_SUMMONING_CHARM.get())
                .requires(Items.WARPED_FUNGUS)
                .requires(Items.WARPED_FUNGUS)
                .requires(ModItems.ARCANE_INK.get())
                .unlockedBy("has_arcane_ink", has(ModItems.ARCANE_INK.get()))
                .save(writer);

        // Fixed 1 Arcane Ink, same as WARPED_CATALYST/VESSEL_SUMMONING_CHARM above - the real gate on
        // this recipe is PledgeAltarUnlockHandler (must be pledged to the Hunger), not Arcane Ink cost.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.HUNGER_FAITH_ALTAR.get())
                .requires(Items.SOUL_SAND)
                .requires(Items.AMETHYST_SHARD)
                .requires(ModItems.ARCANE_INK.get())
                .unlockedBy("has_soul_sand", has(Items.SOUL_SAND))
                .save(writer);

        // Config.packOfTheGodsArcaneInkCost drives the Arcane Ink count here specifically.
        ShapelessRecipeBuilder packOfTheGods = ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.PACK_OF_THE_GODS.get())
                .requires(Items.PAPER)
                .requires(Items.FEATHER)
                .unlockedBy("has_paper", has(Items.PAPER));
        for (int i = 0; i < Config.packOfTheGodsArcaneInkCost; i++)
        {
            packOfTheGods.requires(ModItems.ARCANE_INK.get());
        }
        packOfTheGods.save(writer);

        // Fixed 1 Arcane Ink, same reasoning as the Hunger's altar - the real gate is
        // PledgeAltarUnlockHandler (must be pledged to Fertility), not the Arcane Ink cost.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.FERTILITY_FAITH_ALTAR.get())
                .requires(Items.BONE_MEAL)
                .requires(Items.OAK_SAPLING)
                .requires(ModItems.ARCANE_INK.get())
                .unlockedBy("has_bone_meal", has(Items.BONE_MEAL))
                .save(writer);

        // Fixed 1 Arcane Ink, same reasoning as the other dedicated altars - the real gate is
        // PledgeAltarUnlockHandler (must be pledged to War), not the Arcane Ink cost.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.WAR_FAITH_ALTAR.get())
                .requires(Items.IRON_INGOT)
                .requires(Items.ARROW)
                .requires(ModItems.ARCANE_INK.get())
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(writer);

        // Fixed 1 Arcane Ink, same reasoning as the other dedicated altars - the real gate is
        // PledgeAltarUnlockHandler (must be pledged to the Sun), not the Arcane Ink cost.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.SUN_FAITH_ALTAR.get())
                .requires(Items.GLOWSTONE_DUST)
                .requires(Items.GOLD_INGOT)
                .requires(ModItems.ARCANE_INK.get())
                .unlockedBy("has_glowstone_dust", has(Items.GLOWSTONE_DUST))
                .save(writer);

        // Fixed 1 Arcane Ink, same reasoning as the other dedicated altars - the real gate is
        // PledgeAltarUnlockHandler (must be pledged to the Theater), not the Arcane Ink cost.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.THEATER_FAITH_ALTAR.get())
                .requires(Items.WHITE_DYE)
                .requires(Items.BLACK_DYE)
                .requires(ModItems.ARCANE_INK.get())
                .unlockedBy("has_white_dye", has(Items.WHITE_DYE))
                .save(writer);

        // Fixed 1 Arcane Ink - the real gate here is TheaterHelmetUnlockHandler (must be pledged to
        // the Theater), not the Arcane Ink cost.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.THEATER_HELMET.get())
                .requires(Items.LEATHER)
                .requires(Items.STRING)
                .requires(ModItems.ARCANE_INK.get())
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(writer);
    }
}
