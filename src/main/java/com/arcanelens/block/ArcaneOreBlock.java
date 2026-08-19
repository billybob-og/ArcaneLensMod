package com.arcanelens.block;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.DropExperienceBlock;

public class ArcaneOreBlock extends DropExperienceBlock
{
    public ArcaneOreBlock(Properties properties)
    {
        super(properties, UniformInt.of(2, 5));
    }
}
