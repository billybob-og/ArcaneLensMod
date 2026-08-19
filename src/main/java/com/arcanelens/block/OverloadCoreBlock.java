package com.arcanelens.block;

import com.arcanelens.block.entity.OverloadCoreBlockEntity;
import com.arcanelens.registry.ModBlockEntities;
import com.arcanelens.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
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
import org.jetbrains.annotations.Nullable;

/**
 * Placed adjacent to a Living Chest to anchor the Overload Ritual to that chest's cluster - see
 * OverloadCoreBlockEntity for the actual ritual state machine. Deliberately not itself a BFS network node
 * (never joins the cluster, never claims it), same "read-only" relationship the Arcane Assembler already
 * has with an adjacent cluster.
 */
public class OverloadCoreBlock extends Block implements EntityBlock
{
    public OverloadCoreBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(ModItems.WARPED_CATALYST.get()))
        {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide && level.getBlockEntity(pos) instanceof OverloadCoreBlockEntity core
                && player instanceof ServerPlayer serverPlayer)
        {
            core.tryStartRitual(serverPlayer, held);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new OverloadCoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if (level.isClientSide || type != ModBlockEntities.OVERLOAD_CORE.get())
        {
            return null;
        }
        return (lvl, pos, st, be) ->
        {
            if (be instanceof OverloadCoreBlockEntity core)
            {
                core.tick(lvl, pos, st);
            }
        };
    }
}
