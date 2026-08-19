package com.arcanelens.block;

import com.arcanelens.block.entity.LensCombinerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class LensCombinerBlock extends Block implements EntityBlock
{
    public LensCombinerBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (!level.isClientSide)
        {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LensCombinerBlockEntity combiner && player instanceof ServerPlayer serverPlayer)
            {
                NetworkHooks.openScreen(serverPlayer, combiner, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            if (level.getBlockEntity(pos) instanceof LensCombinerBlockEntity combiner)
            {
                for (int slot = 0; slot < combiner.getItemHandler().getSlots(); slot++)
                {
                    ItemStack stack = combiner.getItemHandler().getStackInSlot(slot);
                    if (!stack.isEmpty())
                    {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new LensCombinerBlockEntity(pos, state);
    }
}
