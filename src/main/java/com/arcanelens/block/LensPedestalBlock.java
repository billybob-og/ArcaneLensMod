package com.arcanelens.block;

import com.arcanelens.block.entity.LensPedestalBlockEntity;
import com.arcanelens.item.MagicLensItem;
import com.arcanelens.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
import org.jetbrains.annotations.Nullable;

public class LensPedestalBlock extends Block implements EntityBlock
{
    public LensPedestalBlock(Properties properties)
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

        if (!(level.getBlockEntity(pos) instanceof LensPedestalBlockEntity pedestal))
        {
            return InteractionResult.PASS;
        }

        if (pedestal.getOwner() == null)
        {
            pedestal.setOwner(player.getUUID());
        }

        ItemStack held = player.getItemInHand(hand);

        if (pedestal.getLens().isEmpty() && held.getItem() instanceof MagicLensItem)
        {
            pedestal.setLens(held.split(1));
            return InteractionResult.CONSUME;
        }

        if (!pedestal.getLens().isEmpty() && held.isEmpty())
        {
            ItemStack returned = pedestal.takeLens();
            if (!player.getInventory().add(returned))
            {
                player.drop(returned, false);
            }
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer != null && level.getBlockEntity(pos) instanceof LensPedestalBlockEntity pedestal)
        {
            pedestal.setOwner(placer.getUUID());
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            if (level.getBlockEntity(pos) instanceof LensPedestalBlockEntity pedestal && !pedestal.getLens().isEmpty())
            {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), pedestal.getLens());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new LensPedestalBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if (level.isClientSide || type != ModBlockEntities.LENS_PEDESTAL.get())
        {
            return null;
        }
        return (lvl, pos, st, be) ->
        {
            if (be instanceof LensPedestalBlockEntity pedestal)
            {
                pedestal.tick(lvl, pos, st);
            }
        };
    }
}
