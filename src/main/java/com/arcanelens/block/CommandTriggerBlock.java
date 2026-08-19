package com.arcanelens.block;

import com.arcanelens.block.entity.CommandTriggerBlockEntity;
import com.arcanelens.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/**
 * An invisible stand-in for a Command Block that fires from player touch instead of redstone. Implementing
 * GameMasterBlock reuses vanilla's own permission gate (ServerPlayerGameMode checks this marker interface
 * and blocks non-ops from ever reaching use()) - no manual permission check needed here. Walk-through (no
 * collision) like tripwire, but keeps its full outline/interaction shape so an op can still aim at and
 * right-click it to configure.
 */
public class CommandTriggerBlock extends Block implements EntityBlock, GameMasterBlock
{
    public CommandTriggerBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return Shapes.empty();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (!level.isClientSide)
        {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CommandTriggerBlockEntity trigger && player instanceof ServerPlayer serverPlayer)
            {
                NetworkHooks.openScreen(serverPlayer, trigger, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new CommandTriggerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if (level.isClientSide || type != ModBlockEntities.COMMAND_TRIGGER.get())
        {
            return null;
        }
        return (lvl, pos, st, be) ->
        {
            if (be instanceof CommandTriggerBlockEntity trigger)
            {
                trigger.tick(lvl, pos);
            }
        };
    }
}
