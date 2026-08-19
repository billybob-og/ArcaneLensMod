package com.arcanelens.block;

import com.arcanelens.block.entity.SoulPedestalBlockEntity;
import com.arcanelens.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public class SoulPedestalBlock extends Block implements EntityBlock
{
    public static final IntegerProperty FUEL = IntegerProperty.create("fuel", 0, SoulPedestalBlockEntity.MAX_FUEL);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public SoulPedestalBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FUEL, 0).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FUEL, LIT);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (level.isClientSide)
        {
            return InteractionResult.sidedSuccess(true);
        }

        if (!(level.getBlockEntity(pos) instanceof SoulPedestalBlockEntity pedestal))
        {
            return InteractionResult.PASS;
        }

        if (pedestal.getOwner() == null)
        {
            pedestal.setOwner(player.getUUID());
        }

        ItemStack held = player.getItemInHand(hand);

        if (held.is(Items.SOUL_SAND) && state.getValue(FUEL) < SoulPedestalBlockEntity.MAX_FUEL)
        {
            level.setBlock(pos, state.setValue(FUEL, state.getValue(FUEL) + 1), 3);
            held.shrink(1);
            level.playSound(null, pos, SoundType.SOUL_SAND.getPlaceSound(), SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.CONSUME;
        }

        if (held.is(Items.FLINT_AND_STEEL) && !state.getValue(LIT) && state.getValue(FUEL) >= 1)
        {
            level.setBlock(pos, state.setValue(LIT, true), 3);
            held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.CONSUME;
        }

        if (state.getValue(LIT) && !held.isEmpty() && !held.is(Items.SOUL_SAND) && pedestal.getConversionItem().isEmpty())
        {
            pedestal.setConversionItem(held.split(1));
            return InteractionResult.CONSUME;
        }

        if (held.isEmpty() && !pedestal.getConversionItem().isEmpty())
        {
            ItemStack returned = pedestal.takeConversionItem();
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
        if (placer != null && level.getBlockEntity(pos) instanceof SoulPedestalBlockEntity pedestal)
        {
            pedestal.setOwner(placer.getUUID());
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
    {
        if (state.getValue(LIT) && random.nextInt(2) == 0)
        {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    pos.getX() + 0.5, pos.getY() + 1.3, pos.getZ() + 0.5,
                    0.0, 0.02, 0.0);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            if (level.getBlockEntity(pos) instanceof SoulPedestalBlockEntity pedestal && !pedestal.getConversionItem().isEmpty())
            {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), pedestal.getConversionItem());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new SoulPedestalBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if (level.isClientSide || type != ModBlockEntities.SOUL_PEDESTAL.get())
        {
            return null;
        }
        return (lvl, pos, st, be) ->
        {
            if (be instanceof SoulPedestalBlockEntity pedestal)
            {
                pedestal.tick(lvl, pos, st);
            }
        };
    }
}
