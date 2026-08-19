package com.arcanelens.datagen;

import com.arcanelens.ArcaneLens;
import com.arcanelens.registry.ModItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider
{
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                CompletableFuture<TagsProvider.TagLookup<Block>> blockTags, ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, blockTags, ArcaneLens.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
        // Not FoodProperties/edibility-based - sugar cane isn't actually edible in vanilla, but is exactly
        // the kind of "surplus crop with nothing to do" this fuel tag exists for. Datapack-overridable.
        this.tag(ModItemTags.ASSEMBLER_FUEL).add(
                Items.WHEAT, Items.CARROT, Items.POTATO, Items.BEETROOT, Items.SUGAR_CANE,
                Items.MELON_SLICE, Items.PUMPKIN, Items.NETHER_WART, Items.COCOA_BEANS,
                Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.MUTTON, Items.RABBIT,
                Items.COD, Items.SALMON);
    }
}
