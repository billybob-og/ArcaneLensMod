package com.arcanelens.block.entity;

import com.arcanelens.block.ArcaneAssemblerBlock;
import com.arcanelens.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The non-inventory-holding half of a 2-wide Arcane Assembler (see ArcaneAssemblerBlock.PART). Holds no
 * state and never ticks - it exists purely so a hopper/pipe touching the EXTENSION half can still reach
 * the MAIN half's real inventory, by forwarding every capability request there. Without this, the
 * EXTENSION position would have no block entity at all, and Forge's capability lookup in this version has
 * no block-level fallback for entity-less positions - a hopper on that side would see nothing to insert
 * into, not even the fuel slot. Mirrors how ArcaneAssemblerBlock.use() already redirects either half's
 * right-click to the same menu.
 */
public class ArcaneAssemblerExtensionBlockEntity extends BlockEntity
{
    public ArcaneAssemblerExtensionBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.ARCANE_ASSEMBLER_EXTENSION.get(), pos, state);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        BlockPos mainPos = ArcaneAssemblerBlock.getMainPos(worldPosition, getBlockState());
        if (level != null && level.getBlockEntity(mainPos) instanceof ArcaneAssemblerBlockEntity main)
        {
            return main.getCapability(cap, side);
        }
        return super.getCapability(cap, side);
    }
}
