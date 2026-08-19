package com.arcanelens.datagen;

import com.arcanelens.ArcaneLens;
import com.arcanelens.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider
{
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, ArcaneLens.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
        this.tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.INSCRIPTION_WORKBENCH.get());
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.SOUL_PEDESTAL.get(), ModBlocks.LENS_PEDESTAL.get(),
                ModBlocks.ARCANE_ORE.get(), ModBlocks.LENS_COMBINER.get(), ModBlocks.SOUL_FEEDER.get(),
                ModBlocks.STORAGE_BLOCK.get(), ModBlocks.TERMINAL.get(), ModBlocks.STORAGE_CONNECTOR.get(),
                ModBlocks.ARCANE_ASSEMBLER.get(), ModBlocks.OVERLOAD_CORE.get());
        this.tag(BlockTags.NEEDS_DIAMOND_TOOL).add(ModBlocks.ARCANE_ORE.get());
    }
}
