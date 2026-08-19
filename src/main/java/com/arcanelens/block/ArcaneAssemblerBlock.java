package com.arcanelens.block;

import com.arcanelens.block.entity.ArcaneAssemblerBlockEntity;
import com.arcanelens.block.entity.ArcaneAssemblerExtensionBlockEntity;
import com.arcanelens.capability.SkillTreeProvider;
import com.arcanelens.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Auto-crafting machine, powered by crop/meat fuel instead of redstone - see ArcaneAssemblerBlockEntity.
 * Deliberately separate from the storage network's own block types (StorageBlock/StorageConnectorBlock) -
 * it never participates in StorageNetworkManager's BFS, only optionally reads from an adjacent cluster.
 *
 * <p>Physically 2 blocks wide, 1 deep: this MAIN half (the one holding the real block entity/inventory)
 * plus a companion EXTENSION half with no block entity of its own, placed automatically one block over.
 * The unrotated model's extension goes west, so - matching the same y-rotation convention used for
 * TerminalBlock/StorageConnectorBlock - the actual world direction of the extension rotates together
 * with FACING (see {@link #getExtensionDirection}). Modeled on vanilla's BedBlock: placement fails
 * outright if the companion position isn't free, and breaking either half removes and drops both.</p>
 */
public class ArcaneAssemblerBlock extends HorizontalDirectionalBlock implements EntityBlock
{
    public static final EnumProperty<ArcaneAssemblerPart> PART = EnumProperty.create("part", ArcaneAssemblerPart.class);

    public ArcaneAssemblerBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, ArcaneAssemblerPart.MAIN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, PART);
    }

    /** The unrotated model's companion half sits to the WEST - this rotates that offset the same amount
     * FACING is rotated from NORTH, matching the y-rotation convention used everywhere else in this mod
     * (0/90/180/270 for north/east/south/west). */
    private static Direction getExtensionDirection(Direction facing)
    {
        Direction extension = Direction.WEST;
        switch (facing)
        {
            case EAST -> extension = extension.getClockWise();
            case SOUTH -> extension = extension.getClockWise().getClockWise();
            case WEST -> extension = extension.getClockWise().getClockWise().getClockWise();
            default -> { }
        }
        return extension;
    }

    /** Public so ArcaneAssemblerExtensionBlockEntity (a different package) can resolve which position
     * holds the real block entity to forward capability requests to. */
    public static BlockPos getMainPos(BlockPos pos, BlockState state)
    {
        if (state.getValue(PART) == ArcaneAssemblerPart.MAIN)
        {
            return pos;
        }
        return pos.relative(getExtensionDirection(state.getValue(FACING)).getOpposite());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos extensionPos = context.getClickedPos().relative(getExtensionDirection(facing));
        if (!context.getLevel().getBlockState(extensionPos).canBeReplaced(context))
        {
            return null;
        }
        return this.defaultBlockState().setValue(FACING, facing).setValue(PART, ArcaneAssemblerPart.MAIN);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide)
        {
            Direction facing = state.getValue(FACING);
            BlockPos extensionPos = pos.relative(getExtensionDirection(facing));
            level.setBlock(extensionPos, this.defaultBlockState().setValue(FACING, facing).setValue(PART, ArcaneAssemblerPart.EXTENSION), 3);

            // Baked in once at placement - see ArcaneAssemblerBlockEntity's speedLevel/fuelEfficiencyLevel
            // javadoc for why this isn't looked up live from a player each tick.
            if (level.getBlockEntity(pos) instanceof ArcaneAssemblerBlockEntity assembler && placer != null)
            {
                placer.getCapability(SkillTreeProvider.CAPABILITY).ifPresent(cap ->
                        assembler.setSkillLevels(cap.getAssemblerSpeedLevel(), cap.getAssemblerFuelEfficiencyLevel()));
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (!level.isClientSide)
        {
            BlockPos mainPos = getMainPos(pos, state);
            if (level.getBlockEntity(mainPos) instanceof ArcaneAssemblerBlockEntity assembler && player instanceof ServerPlayer serverPlayer)
            {
                NetworkHooks.openScreen(serverPlayer, assembler, mainPos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            // Only drop from the MAIN half's own block entity when we're the one processing MAIN -
            // this fires exactly once regardless of which half the player broke first (see the
            // companion-removal guard below), never duplicating the inventory drop.
            if (state.getValue(PART) == ArcaneAssemblerPart.MAIN && level.getBlockEntity(pos) instanceof ArcaneAssemblerBlockEntity assembler)
            {
                for (int slot = 0; slot < assembler.getItemHandler().getSlots(); slot++)
                {
                    ItemStack stack = assembler.getItemHandler().getStackInSlot(slot);
                    if (!stack.isEmpty())
                    {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                    }
                }
            }

            Direction facing = state.getValue(FACING);
            BlockPos otherPos = state.getValue(PART) == ArcaneAssemblerPart.MAIN
                    ? pos.relative(getExtensionDirection(facing))
                    : pos.relative(getExtensionDirection(facing).getOpposite());
            // Guards against infinite recursion - by the time removeBlock's own onRemove call for
            // otherPos runs, this position has already been cleared, so its own companion-removal
            // check correctly sees nothing left to recurse into.
            if (level.getBlockState(otherPos).is(state.getBlock()))
            {
                level.removeBlock(otherPos, isMoving);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        // EXTENSION still gets a real (but non-ticking, inventory-less) block entity - purely so hoppers/
        // pipes touching that half can reach the MAIN half's inventory via capability forwarding (see
        // ArcaneAssemblerExtensionBlockEntity). Without one, a hopper on the EXTENSION side would see no
        // capability at all, since Forge's capability lookup goes through the block entity at that exact
        // position with no block-level fallback in this Forge version.
        return state.getValue(PART) == ArcaneAssemblerPart.MAIN
                ? new ArcaneAssemblerBlockEntity(pos, state)
                : new ArcaneAssemblerExtensionBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if (level.isClientSide || type != ModBlockEntities.ARCANE_ASSEMBLER.get())
        {
            return null;
        }
        return (lvl, pos, st, be) ->
        {
            if (be instanceof ArcaneAssemblerBlockEntity assembler)
            {
                assembler.tick(lvl, pos, st);
            }
        };
    }
}
