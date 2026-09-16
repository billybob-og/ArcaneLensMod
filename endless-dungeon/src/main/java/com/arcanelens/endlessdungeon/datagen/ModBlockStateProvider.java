package com.arcanelens.endlessdungeon.datagen;

import com.arcanelens.endlessdungeon.EndlessDungeonMod;
import com.arcanelens.endlessdungeon.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider
{
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper)
    {
        super(output, EndlessDungeonMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels()
    {
        // A single plain square texture (not a lens/translucent shape like the base mod's
        // OverloadPortalBlock) - both UPPER states share the same cube_all model, so simpleBlock (not
        // forAllStates) is enough; it already covers every actual blockstate combination.
        ModelFile dungeonPortalModel = models().cubeAll("dungeon_portal",
                new ResourceLocation(EndlessDungeonMod.MODID, "block/dungeon_portal"));
        simpleBlock(ModBlocks.DUNGEON_PORTAL.get(), dungeonPortalModel);
    }
}
