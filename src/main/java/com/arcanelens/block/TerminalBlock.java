package com.arcanelens.block;

import com.arcanelens.block.entity.TerminalBlockEntity;
import com.arcanelens.util.StorageNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/**
 * Placed adjacent to a Storage Block to browse that block's entire connected cluster as one merged
 * inventory (see StorageNetworkManager/TerminalBlockEntity). The first Terminal built on a cluster claims
 * it (setPlacedBy); the browsing UI itself is wired up in a later step (TerminalMenu/TerminalScreen) -
 * use() is a placeholder until then. The unrotated model faces north (see terminal.json), so FACING follows
 * the same convention as vanilla's furnace/etc: it points back at the player who placed it.
 */
public class TerminalBlock extends HorizontalDirectionalBlock implements EntityBlock
{
    public TerminalBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (!level.isClientSide)
        {
            if (level.getBlockEntity(pos) instanceof TerminalBlockEntity terminal && player instanceof ServerPlayer serverPlayer)
            {
                // The buffer-writing overload (not the simple 3-arg one) so the client receives the
                // server's exact ordered cluster-member list rather than independently re-deriving it -
                // closes off a client/server member-list race if a member block is broken mid-open.
                NetworkHooks.openScreen(serverPlayer, terminal, buf -> {
                    buf.writeBlockPos(pos);
                    var members = terminal.getHomeClusterMembers(level);
                    buf.writeVarInt(members.size());
                    for (BlockPos memberPos : members)
                    {
                        buf.writeBlockPos(memberPos);
                    }
                });
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel serverLevel)
        {
            StorageNetworkManager.tryClaim(serverLevel, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            if (level.getBlockEntity(pos) instanceof TerminalBlockEntity terminal)
            {
                dropHandler(level, pos, terminal.getUpgradeHandler());
                dropHandler(level, pos, terminal.getBonusSlotHandler());
            }
            if (level instanceof ServerLevel serverLevel)
            {
                StorageNetworkManager.releaseClaim(serverLevel, pos);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void dropHandler(Level level, BlockPos pos, ItemStackHandler handler)
    {
        for (int slot = 0; slot < handler.getSlots(); slot++)
        {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty())
            {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new TerminalBlockEntity(pos, state);
    }
}
