package com.arcanelens.block;

import com.arcanelens.block.entity.StorageBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import org.jetbrains.annotations.Nullable;

/**
 * A "dumb" storage unit - any two touching Storage Blocks (or Storage Blocks bridged by a Storage
 * Connector) merge into one shared pool, browsable only through a Terminal placed adjacent to any
 * member (see StorageNetworkManager). Has no UI of its own.
 */
public class StorageBlock extends Block implements EntityBlock
{
    public StorageBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (!level.isClientSide)
        {
            player.displayClientMessage(Component.literal("Place a Terminal next to this to access its storage.")
                    .withStyle(ChatFormatting.GRAY), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            if (level.getBlockEntity(pos) instanceof StorageBlockEntity storage)
            {
                for (int slot = 0; slot < storage.getItemHandler().getSlots(); slot++)
                {
                    ItemStack stack = storage.getItemHandler().getStackInSlot(slot);
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
        return new StorageBlockEntity(pos, state);
    }
}
