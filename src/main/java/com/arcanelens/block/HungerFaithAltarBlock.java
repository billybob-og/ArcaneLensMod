package com.arcanelens.block;

import com.arcanelens.block.entity.HungerFaithAltarBlockEntity;
import com.arcanelens.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
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

/** Right-click with an item to feed the single slot (matches SoulPedestalBlock's insert/return
 * shape, minus the fuel/flint-and-steel branches this altar has no use for - being fed at all is its
 * only "on" condition), right-click empty-handed to take back whatever's left uneaten. */
public class HungerFaithAltarBlock extends Block implements EntityBlock
{
    public HungerFaithAltarBlock(Properties properties)
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

        if (!(level.getBlockEntity(pos) instanceof HungerFaithAltarBlockEntity altar))
        {
            return InteractionResult.PASS;
        }

        if (altar.getOwner() == null)
        {
            altar.setOwner(player.getUUID());
        }

        ItemStack held = player.getItemInHand(hand);

        if (!held.isEmpty() && altar.getFaithItem().isEmpty())
        {
            altar.setFaithItem(held.split(1));
            return InteractionResult.CONSUME;
        }

        if (held.isEmpty() && !altar.getFaithItem().isEmpty())
        {
            ItemStack returned = altar.takeFaithItem();
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
        if (placer != null && level.getBlockEntity(pos) instanceof HungerFaithAltarBlockEntity altar)
        {
            altar.setOwner(placer.getUUID());
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
    {
        if (level.getBlockEntity(pos) instanceof HungerFaithAltarBlockEntity altar && !altar.getFaithItem().isEmpty() && random.nextInt(4) == 0)
        {
            level.addParticle(ParticleTypes.SOUL,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    0.0, 0.02, 0.0);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            if (level.getBlockEntity(pos) instanceof HungerFaithAltarBlockEntity altar && !altar.getFaithItem().isEmpty())
            {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), altar.getFaithItem());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new HungerFaithAltarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if (level.isClientSide || type != ModBlockEntities.HUNGER_FAITH_ALTAR.get())
        {
            return null;
        }
        return (lvl, pos, st, be) ->
        {
            if (be instanceof HungerFaithAltarBlockEntity altar)
            {
                altar.tick(lvl, pos);
            }
        };
    }
}
