package com.arcanelens.block;

import com.arcanelens.block.entity.FaithAltarBlockEntity;
import com.arcanelens.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

/** Centerpiece of the (still-unbuilt) faith shrine structure - watches an area around itself for items
 * burned in player-placed soul fire and converts them into Faith on the burning item's owner. */
public class FaithAltarBlock extends Block implements EntityBlock
{
    // Purely cosmetic - each placement gets a random quarter-turn so a room full of altars doesn't look
    // like it was stamped out with a single texture facing the same way every time.
    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 3);

    public FaithAltarBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(ROTATION);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState().setValue(ROTATION, context.getLevel().getRandom().nextInt(4));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
    {
        if (random.nextInt(4) == 0)
        {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    0.0, 0.02, 0.0);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new FaithAltarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if (level.isClientSide || type != ModBlockEntities.FAITH_ALTAR.get())
        {
            return null;
        }
        return (lvl, pos, st, be) ->
        {
            if (be instanceof FaithAltarBlockEntity altar)
            {
                altar.tick(lvl, pos);
            }
        };
    }
}
