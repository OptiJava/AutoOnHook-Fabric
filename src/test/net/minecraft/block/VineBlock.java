/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class VineBlock
extends Block {
    public static final BooleanProperty UP = ConnectingBlock.UP;
    public static final BooleanProperty NORTH = ConnectingBlock.NORTH;
    public static final BooleanProperty EAST = ConnectingBlock.EAST;
    public static final BooleanProperty SOUTH = ConnectingBlock.SOUTH;
    public static final BooleanProperty WEST = ConnectingBlock.WEST;
    public static final Map<Direction, BooleanProperty> FACING_PROPERTIES = ConnectingBlock.FACING_PROPERTIES.entrySet().stream().filter(entry -> entry.getKey() != Direction.DOWN).collect(Util.toMap());
    protected static final float field_31275 = 1.0f;
    private static final VoxelShape UP_SHAPE = Block.createCuboidShape(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
    private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
    private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
    private final Map<BlockState, VoxelShape> shapesByState;

    public VineBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(UP, false)).with(NORTH, false)).with(EAST, false)).with(SOUTH, false)).with(WEST, false));
        this.shapesByState = ImmutableMap.copyOf(this.stateManager.getStates().stream().collect(Collectors.toMap(Function.identity(), VineBlock::getShapeForState)));
    }

    private static VoxelShape getShapeForState(BlockState state) {
        VoxelShape voxelShape = VoxelShapes.empty();
        if (state.get(UP).booleanValue()) {
            voxelShape = UP_SHAPE;
        }
        if (state.get(NORTH).booleanValue()) {
            voxelShape = VoxelShapes.union(voxelShape, SOUTH_SHAPE);
        }
        if (state.get(SOUTH).booleanValue()) {
            voxelShape = VoxelShapes.union(voxelShape, NORTH_SHAPE);
        }
        if (state.get(EAST).booleanValue()) {
            voxelShape = VoxelShapes.union(voxelShape, WEST_SHAPE);
        }
        if (state.get(WEST).booleanValue()) {
            voxelShape = VoxelShapes.union(voxelShape, EAST_SHAPE);
        }
        return voxelShape.isEmpty() ? VoxelShapes.fullCube() : voxelShape;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapesByState.get(state);
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return this.hasAdjacentBlocks(this.getPlacementShape(state, world, pos));
    }

    private boolean hasAdjacentBlocks(BlockState state) {
        return this.getAdjacentBlockCount(state) > 0;
    }

    private int getAdjacentBlockCount(BlockState state) {
        int i = 0;
        for (BooleanProperty booleanProperty : FACING_PROPERTIES.values()) {
            if (!state.get(booleanProperty).booleanValue()) continue;
            ++i;
        }
        return i;
    }

    private boolean shouldHaveSide(BlockView world, BlockPos pos, Direction side) {
        if (side == Direction.DOWN) {
            return false;
        }
        BlockPos blockPos = pos.offset(side);
        if (VineBlock.shouldConnectTo(world, blockPos, side)) {
            return true;
        }
        if (side.getAxis() != Direction.Axis.Y) {
            BooleanProperty booleanProperty = FACING_PROPERTIES.get(side);
            BlockState blockState = world.getBlockState(pos.up());
            return blockState.isOf(this) && blockState.get(booleanProperty) != false;
        }
        return false;
    }

    public static boolean shouldConnectTo(BlockView world, BlockPos pos, Direction direction) {
        BlockState blockState = world.getBlockState(pos);
        return Block.isFaceFullSquare(blockState.getCollisionShape(world, pos), direction.getOpposite());
    }

    private BlockState getPlacementShape(BlockState state, BlockView world, BlockPos pos) {
        BlockPos blockPos = pos.up();
        if (state.get(UP).booleanValue()) {
            state = (BlockState)state.with(UP, VineBlock.shouldConnectTo(world, blockPos, Direction.DOWN));
        }
        AbstractBlock.AbstractBlockState blockState = null;
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BooleanProperty booleanProperty = VineBlock.getFacingProperty(direction);
            if (!state.get(booleanProperty).booleanValue()) continue;
            boolean bl = this.shouldHaveSide(world, pos, direction);
            if (!bl) {
                if (blockState == null) {
                    blockState = world.getBlockState(blockPos);
                }
                bl = blockState.isOf(this) && blockState.get(booleanProperty) != false;
            }
            state = (BlockState)state.with(booleanProperty, bl);
        }
        return state;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN) {
            return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        }
        BlockState blockState = this.getPlacementShape(state, world, pos);
        if (!this.hasAdjacentBlocks(blockState)) {
            return Blocks.AIR.getDefaultState();
        }
        return blockState;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockState direction3;
        BlockState direction2;
        BlockPos blockPos2;
        BlockState blockState;
        if (random.nextInt(4) != 0) {
            return;
        }
        Direction direction = Direction.random(random);
        BlockPos blockPos = pos.up();
        if (direction.getAxis().isHorizontal() && !state.get(VineBlock.getFacingProperty(direction)).booleanValue()) {
            if (!this.canGrowAt(world, pos)) {
                return;
            }
            BlockPos blockPos22 = pos.offset(direction);
            BlockState blockState2 = world.getBlockState(blockPos22);
            if (blockState2.isAir()) {
                Direction direction22 = direction.rotateYClockwise();
                Direction direction32 = direction.rotateYCounterclockwise();
                boolean bl = state.get(VineBlock.getFacingProperty(direction22));
                boolean bl2 = state.get(VineBlock.getFacingProperty(direction32));
                BlockPos blockPos3 = blockPos22.offset(direction22);
                BlockPos blockPos4 = blockPos22.offset(direction32);
                if (bl && VineBlock.shouldConnectTo(world, blockPos3, direction22)) {
                    world.setBlockState(blockPos22, (BlockState)this.getDefaultState().with(VineBlock.getFacingProperty(direction22), true), Block.NOTIFY_LISTENERS);
                } else if (bl2 && VineBlock.shouldConnectTo(world, blockPos4, direction32)) {
                    world.setBlockState(blockPos22, (BlockState)this.getDefaultState().with(VineBlock.getFacingProperty(direction32), true), Block.NOTIFY_LISTENERS);
                } else {
                    Direction direction4 = direction.getOpposite();
                    if (bl && world.isAir(blockPos3) && VineBlock.shouldConnectTo(world, pos.offset(direction22), direction4)) {
                        world.setBlockState(blockPos3, (BlockState)this.getDefaultState().with(VineBlock.getFacingProperty(direction4), true), Block.NOTIFY_LISTENERS);
                    } else if (bl2 && world.isAir(blockPos4) && VineBlock.shouldConnectTo(world, pos.offset(direction32), direction4)) {
                        world.setBlockState(blockPos4, (BlockState)this.getDefaultState().with(VineBlock.getFacingProperty(direction4), true), Block.NOTIFY_LISTENERS);
                    } else if ((double)random.nextFloat() < 0.05 && VineBlock.shouldConnectTo(world, blockPos22.up(), Direction.UP)) {
                        world.setBlockState(blockPos22, (BlockState)this.getDefaultState().with(UP, true), Block.NOTIFY_LISTENERS);
                    }
                }
            } else if (VineBlock.shouldConnectTo(world, blockPos22, direction)) {
                world.setBlockState(pos, (BlockState)state.with(VineBlock.getFacingProperty(direction), true), Block.NOTIFY_LISTENERS);
            }
            return;
        }
        if (direction == Direction.UP && pos.getY() < world.getTopY() - 1) {
            if (this.shouldHaveSide(world, pos, direction)) {
                world.setBlockState(pos, (BlockState)state.with(UP, true), Block.NOTIFY_LISTENERS);
                return;
            }
            if (world.isAir(blockPos)) {
                if (!this.canGrowAt(world, pos)) {
                    return;
                }
                BlockState blockPos23 = state;
                for (Direction direction23 : Direction.Type.HORIZONTAL) {
                    if (!random.nextBoolean() && VineBlock.shouldConnectTo(world, blockPos.offset(direction23), direction23)) continue;
                    blockPos23 = (BlockState)blockPos23.with(VineBlock.getFacingProperty(direction23), false);
                }
                if (this.hasHorizontalSide(blockPos23)) {
                    world.setBlockState(blockPos, blockPos23, Block.NOTIFY_LISTENERS);
                }
                return;
            }
        }
        if (pos.getY() > world.getBottomY() && ((blockState = world.getBlockState(blockPos2 = pos.down())).isAir() || blockState.isOf(this)) && (direction2 = blockState.isAir() ? this.getDefaultState() : blockState) != (direction3 = this.getGrownState(state, direction2, random)) && this.hasHorizontalSide(direction3)) {
            world.setBlockState(blockPos2, direction3, Block.NOTIFY_LISTENERS);
        }
    }

    private BlockState getGrownState(BlockState above, BlockState state, Random random) {
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BooleanProperty booleanProperty;
            if (!random.nextBoolean() || !above.get(booleanProperty = VineBlock.getFacingProperty(direction)).booleanValue()) continue;
            state = (BlockState)state.with(booleanProperty, true);
        }
        return state;
    }

    private boolean hasHorizontalSide(BlockState state) {
        return state.get(NORTH) != false || state.get(EAST) != false || state.get(SOUTH) != false || state.get(WEST) != false;
    }

    private boolean canGrowAt(BlockView world, BlockPos pos) {
        int i = 4;
        Iterable<BlockPos> iterable = BlockPos.iterate(pos.getX() - 4, pos.getY() - 1, pos.getZ() - 4, pos.getX() + 4, pos.getY() + 1, pos.getZ() + 4);
        int j = 5;
        for (BlockPos blockPos : iterable) {
            if (!world.getBlockState(blockPos).isOf(this) || --j > 0) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        BlockState blockState = context.getWorld().getBlockState(context.getBlockPos());
        if (blockState.isOf(this)) {
            return this.getAdjacentBlockCount(blockState) < FACING_PROPERTIES.size();
        }
        return super.canReplace(state, context);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos());
        boolean bl = blockState.isOf(this);
        BlockState blockState2 = bl ? blockState : this.getDefaultState();
        for (Direction direction : ctx.getPlacementDirections()) {
            boolean bl2;
            if (direction == Direction.DOWN) continue;
            BooleanProperty booleanProperty = VineBlock.getFacingProperty(direction);
            boolean bl3 = bl2 = bl && blockState.get(booleanProperty) != false;
            if (bl2 || !this.shouldHaveSide(ctx.getWorld(), ctx.getBlockPos(), direction)) continue;
            return (BlockState)blockState2.with(booleanProperty, true);
        }
        return bl ? blockState2 : null;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(UP, NORTH, EAST, SOUTH, WEST);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180: {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, state.get(SOUTH))).with(EAST, state.get(WEST))).with(SOUTH, state.get(NORTH))).with(WEST, state.get(EAST));
            }
            case COUNTERCLOCKWISE_90: {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, state.get(EAST))).with(EAST, state.get(SOUTH))).with(SOUTH, state.get(WEST))).with(WEST, state.get(NORTH));
            }
            case CLOCKWISE_90: {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, state.get(WEST))).with(EAST, state.get(NORTH))).with(SOUTH, state.get(EAST))).with(WEST, state.get(SOUTH));
            }
        }
        return state;
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT: {
                return (BlockState)((BlockState)state.with(NORTH, state.get(SOUTH))).with(SOUTH, state.get(NORTH));
            }
            case FRONT_BACK: {
                return (BlockState)((BlockState)state.with(EAST, state.get(WEST))).with(WEST, state.get(EAST));
            }
        }
        return super.mirror(state, mirror);
    }

    public static BooleanProperty getFacingProperty(Direction direction) {
        return FACING_PROPERTIES.get(direction);
    }
}

