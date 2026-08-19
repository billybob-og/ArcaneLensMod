package com.arcanelens.block;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A thin "wire" block that bridges two otherwise-disconnected Storage Block clusters into one network.
 * Contributes zero slots and has no block entity - it exists purely as a pass-through node for
 * StorageNetworkManager's BFS traversal. Visually it's a small hub (see storage_connector_core model)
 * with a small arm poking toward each neighbor it's actually connected to (storage_connector_arm,
 * reused via blockstate rotation rather than one model per direction - see the 6 boolean connection
 * properties below), matching the multipart technique vanilla uses for fences/walls/iron bars.
 */
public class StorageConnectorBlock extends Block
{
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = new EnumMap<>(Direction.class);
    static
    {
        PROPERTY_BY_DIRECTION.put(Direction.NORTH, NORTH);
        PROPERTY_BY_DIRECTION.put(Direction.SOUTH, SOUTH);
        PROPERTY_BY_DIRECTION.put(Direction.EAST, EAST);
        PROPERTY_BY_DIRECTION.put(Direction.WEST, WEST);
        PROPERTY_BY_DIRECTION.put(Direction.UP, UP);
        PROPERTY_BY_DIRECTION.put(Direction.DOWN, DOWN);
    }

    // Matches the core/arm models once recentered on Y (6-10, same as their X/Z range) - see chat notes.
    // Kept as named constants so the exact numbers are a one-line change if the final art differs.
    private static final VoxelShape CORE_SHAPE = Block.box(6, 6, 6, 10, 10, 10);
    private static final VoxelShape NORTH_ARM = Block.box(6, 6, 0, 10, 10, 8);
    private static final VoxelShape SOUTH_ARM = Block.box(6, 6, 8, 10, 10, 16);
    private static final VoxelShape EAST_ARM = Block.box(8, 6, 6, 16, 10, 10);
    private static final VoxelShape WEST_ARM = Block.box(0, 6, 6, 8, 10, 10);
    private static final VoxelShape UP_ARM = Block.box(6, 8, 6, 10, 16, 10);
    private static final VoxelShape DOWN_ARM = Block.box(6, 0, 6, 10, 8, 10);

    private final Map<BlockState, VoxelShape> shapesCache;

    public StorageConnectorBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST, false).setValue(WEST, false)
                .setValue(UP, false).setValue(DOWN, false));
        this.shapesCache = this.getStateDefinition().getPossibleStates().stream()
                .collect(Collectors.toMap(Function.identity(), StorageConnectorBlock::makeShape));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    /** A connector visually/functionally links to any adjacent Storage Block or another Storage
     * Connector - the same two block types StorageNetworkManager's BFS already treats as network nodes. */
    private static boolean connectsTo(BlockState neighborState)
    {
        return neighborState.getBlock() instanceof StorageBlock || neighborState.getBlock() instanceof StorageConnectorBlock;
    }

    private static BlockState computeConnections(BlockState state, LevelAccessor level, BlockPos pos)
    {
        for (Direction direction : Direction.values())
        {
            boolean connected = connectsTo(level.getBlockState(pos.relative(direction)));
            state = state.setValue(PROPERTY_BY_DIRECTION.get(direction), connected);
        }
        return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return computeConnections(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @NotNull
    @Override
    public BlockState updateShape(BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState,
                                   @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos)
    {
        return state.setValue(PROPERTY_BY_DIRECTION.get(direction), connectsTo(neighborState));
    }

    @NotNull
    @Override
    public VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context)
    {
        return shapesCache.get(state);
    }

    private static VoxelShape makeShape(BlockState state)
    {
        VoxelShape shape = CORE_SHAPE;
        if (state.getValue(NORTH)) shape = Shapes.or(shape, NORTH_ARM);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, SOUTH_ARM);
        if (state.getValue(EAST)) shape = Shapes.or(shape, EAST_ARM);
        if (state.getValue(WEST)) shape = Shapes.or(shape, WEST_ARM);
        if (state.getValue(UP)) shape = Shapes.or(shape, UP_ARM);
        if (state.getValue(DOWN)) shape = Shapes.or(shape, DOWN_ARM);
        return shape;
    }
}
