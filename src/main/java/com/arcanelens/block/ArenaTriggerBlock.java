package com.arcanelens.block;

import com.arcanelens.block.entity.ArenaTriggerBlockEntity;
import com.arcanelens.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Placed in the boss entrance room build at the pitfall hole. Its block entity pastes the much larger boss
 * arena template directly below it the first time this chunk loads for real, then replaces itself with
 * polished blackstone so it never fires twice. See ArenaTriggerBlockEntity for why this exists instead of a
 * jigsaw-pool-based approach: the arena is far too large to attach as a normal jigsaw piece (see the
 * jigsaw-structure-worldgen-gotchas notes), and a custom StructureProcessorType can't be registered by mods
 * (BuiltInRegistries.STRUCTURE_PROCESSOR freezes before Forge constructs mod instances).
 */
public class ArenaTriggerBlock extends Block implements EntityBlock
{
    public ArenaTriggerBlock(Properties properties)
    {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new ArenaTriggerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if (level.isClientSide || type != ModBlockEntities.ARENA_TRIGGER.get())
        {
            return null;
        }
        return (lvl, pos, st, be) ->
        {
            if (be instanceof ArenaTriggerBlockEntity trigger)
            {
                trigger.tick(lvl, pos);
            }
        };
    }
}
