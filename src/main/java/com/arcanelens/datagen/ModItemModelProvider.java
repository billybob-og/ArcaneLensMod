package com.arcanelens.datagen;

import com.arcanelens.ArcaneLens;
import com.arcanelens.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider
{
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper)
    {
        super(output, ArcaneLens.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels()
    {
        basicItem(ModItems.MAGIC_LENS.get());
        basicItem(ModItems.RAW_ARCANE_MATERIAL.get());
        basicItem(ModItems.DIAMOND_ENGRAVER.get());
        basicItem(ModItems.WEIRD_AMULET.get());
        basicItem(ModItems.TOKEN_OF_THE_HUNGER.get());
        basicItem(ModItems.HUNGERS_BOON.get());
        basicItem(ModItems.HUNGERS_BINDINGS.get());
        basicItem(ModItems.SUN_AND_MOON_WARD.get());
        basicItem(ModItems.SLEEPING_GOD_HELMET.get());
        basicItem(ModItems.SLEEPING_GOD_CHESTPLATE.get());
        basicItem(ModItems.SLEEPING_GOD_LEGGINGS.get());
        basicItem(ModItems.SLEEPING_GOD_BOOTS.get());

        // Placeholder art: reuses vanilla's enchanted book texture until custom art exists.
        withExistingParent("ancient_spell_book", mcLoc("item/generated"))
                .texture("layer0", mcLoc("item/enchanted_book"));

        basicItem(ModItems.MAGNET_CHARM.get());
        basicItem(ModItems.FAITH_CHARM.get());
        basicItem(ModItems.MANA_ORB_CHARM.get());
        basicItem(ModItems.BURNING_CHARCOAL_CHARM.get());
        basicItem(ModItems.ANTIVENOM_CHARM.get());
        basicItem(ModItems.PRESERVED_WITHER_ROSE_CHARM.get());
        basicItem(ModItems.IRON_BRACELET.get());
        basicItem(ModItems.GOLD_BRACELET.get());
        basicItem(ModItems.NETHERITE_BRACELET.get());
        basicItem(ModItems.ARCANE_INK.get());
        basicItem(ModItems.HUNGERS_PACT.get());

        basicItem(ModItems.STACK_UPGRADE.get());
        basicItem(ModItems.SLOT_UPGRADE.get());

        basicItem(ModItems.WARPED_CATALYST.get());
        basicItem(ModItems.WARPED_VESSEL_SHARD.get());
        basicItem(ModItems.WARPED_ATTUNEMENT_TEMPLATE.get());
        basicItem(ModItems.VESSEL_BOUND_SLEEPING_GOD_HELMET.get());
        basicItem(ModItems.VESSEL_BOUND_SLEEPING_GOD_CHESTPLATE.get());
        basicItem(ModItems.VESSEL_BOUND_SLEEPING_GOD_LEGGINGS.get());
        basicItem(ModItems.VESSEL_BOUND_SLEEPING_GOD_BOOTS.get());
        basicItem(ModItems.WARPED_ANCHOR.get());
        basicItem(ModItems.VESSEL_SUMMONING_CHARM.get());

        // Real art now - "A Godly Contract" (item.arcanelens.pack_of_the_gods), internal id unchanged.
        basicItem(ModItems.PACK_OF_THE_GODS.get());

        // Both the inventory icon and the worn-armor layer texture (textures/models/armor/
        // theater_layer_1.png, not touched by datagen at all - resolved by ArmorMaterial naming
        // convention, not a model file) are real art now.
        basicItem(ModItems.THEATER_HELMET.get());

        // Placeholder art: a local copy of one of Patchouli's own bundled book textures (ExistingFileHelper
        // can't see into another mod's jar directly, so the file has to actually live in our own resources,
        // not just be referenced by location) - same "placeholder reuse" precedent as ancient_spell_book.
        basicItem(ModItems.ARCANE_GUIDE_BOOK.get());
    }
}
