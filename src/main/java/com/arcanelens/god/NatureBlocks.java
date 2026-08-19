package com.arcanelens.god;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Shared "is this grown vegetation" check for Fertility content - used by both
 * FertilityFaithAltarBlockEntity's scan and FertilityPerkHandler's nature-healing check, so the two
 * can't silently drift apart on what counts. */
public class NatureBlocks
{
    public static boolean isNatureBlock(BlockState state)
    {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.GRASS) || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN)
                || state.is(BlockTags.FLOWERS) || state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES);
    }
}
