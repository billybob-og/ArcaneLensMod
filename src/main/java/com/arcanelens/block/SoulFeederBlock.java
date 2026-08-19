package com.arcanelens.block;

import com.arcanelens.block.entity.SoulFeederBlockEntity;
import com.arcanelens.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

public class SoulFeederBlock extends Block implements EntityBlock
{
    public SoulFeederBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (level.isClientSide)
        {
            return InteractionResult.sidedSuccess(true);
        }

        if (!(level.getBlockEntity(pos) instanceof SoulFeederBlockEntity feeder))
        {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty())
        {
            return InteractionResult.PASS;
        }

        ItemStack remainder = ItemHandlerHelper.insertItemStacked(feeder.getItemHandler(), held.copy(), false);
        player.setItemInHand(hand, remainder);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            if (level.getBlockEntity(pos) instanceof SoulFeederBlockEntity feeder)
            {
                for (int slot = 0; slot < feeder.getItemHandler().getSlots(); slot++)
                {
                    ItemStack stack = feeder.getItemHandler().getStackInSlot(slot);
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
        return new SoulFeederBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if (level.isClientSide || type != ModBlockEntities.SOUL_FEEDER.get())
        {
            return null;
        }
        return (lvl, pos, st, be) ->
        {
            if (be instanceof SoulFeederBlockEntity feeder)
            {
                feeder.tick(lvl, pos, st);
            }
        };
    }
}
