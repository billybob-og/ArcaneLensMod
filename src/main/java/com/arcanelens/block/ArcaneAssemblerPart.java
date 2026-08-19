package com.arcanelens.block;

import net.minecraft.util.StringRepresentable;

/** Which half of the 2-wide Arcane Assembler a given block position represents - MAIN is the anchor
 * (holds the real ArcaneAssemblerBlockEntity), EXTENSION is the companion half placed automatically to
 * its west (rotated with FACING) with no block entity of its own. See ArcaneAssemblerBlock. */
public enum ArcaneAssemblerPart implements StringRepresentable
{
    MAIN("main"),
    EXTENSION("extension");

    private final String name;

    ArcaneAssemblerPart(String name)
    {
        this.name = name;
    }

    @Override
    public String getSerializedName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
