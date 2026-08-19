package com.arcanelens.block;

import com.arcanelens.block.entity.TriggerPlateBlockEntity;
import com.arcanelens.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * An invisible companion to CommandTriggerBlock - a walk-through, no-configuration trigger that relays
 * player touch to whichever CommandTriggerBlockEntity is placed directly adjacent to it, like an invisible
 * tripwire. See TriggerPlateBlockEntity for the relay logic.
 */
public class TriggerPlateBlock extends Block implements EntityBlock
{
    public TriggerPlateBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return Shapes.empty();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new TriggerPlateBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if (level.isClientSide || type != ModBlockEntities.TRIGGER_PLATE.get())
        {
            return null;
        }
        return (lvl, pos, st, be) ->
        {
            if (be instanceof TriggerPlateBlockEntity plate)
            {
                plate.tick(lvl, pos);
            }
        };
    }
}
