package com.arcanelens.block;

import com.arcanelens.block.entity.FertilityFaithAltarBlockEntity;
import com.arcanelens.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** No interaction needed at all - see FertilityFaithAltarBlockEntity, being placed somewhere lush is
 * the whole mechanic. setPlacedBy just records the owner, same as every other dedicated altar. */
public class FertilityFaithAltarBlock extends Block implements EntityBlock
{
    public FertilityFaithAltarBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer != null && level.getBlockEntity(pos) instanceof FertilityFaithAltarBlockEntity altar)
        {
            altar.setOwner(placer.getUUID());
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
    {
        if (random.nextInt(6) == 0)
        {
            level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    0.0, 0.02, 0.0);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new FertilityFaithAltarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if (level.isClientSide || type != ModBlockEntities.FERTILITY_FAITH_ALTAR.get())
        {
            return null;
        }
        return (lvl, pos, st, be) ->
        {
            if (be instanceof FertilityFaithAltarBlockEntity altar)
            {
                altar.tick(lvl, pos);
            }
        };
    }
}
