/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

public class FenceGateBlock
extends HorizontalFacingBlock {
    public static final BooleanProperty OPEN = Properties.OPEN;
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final BooleanProperty IN_WALL = Properties.IN_WALL;
    protected static final VoxelShape Z_AXIS_SHAPE = Block.createCuboidShape(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    protected static final VoxelShape X_AXIS_SHAPE = Block.createCuboidShape(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);
    protected static final VoxelShape IN_WALL_Z_AXIS_SHAPE = Block.createCuboidShape(0.0, 0.0, 6.0, 16.0, 13.0, 10.0);
    protected static final VoxelShape IN_WALL_X_AXIS_SHAPE = Block.createCuboidShape(6.0, 0.0, 0.0, 10.0, 13.0, 16.0);
    protected static final VoxelShape Z_AXIS_COLLISION_SHAPE = Block.createCuboidShape(0.0, 0.0, 6.0, 16.0, 24.0, 10.0);
    protected static final VoxelShape X_AXIS_COLLISION_SHAPE = Block.createCuboidShape(6.0, 0.0, 0.0, 10.0, 24.0, 16.0);
    protected static final VoxelShape Z_AXIS_CULL_SHAPE = VoxelShapes.union(Block.createCuboidShape(0.0, 5.0, 7.0, 2.0, 16.0, 9.0), Block.createCuboidShape(14.0, 5.0, 7.0, 16.0, 16.0, 9.0));
    protected static final VoxelShape X_AXIS_CULL_SHAPE = VoxelShapes.union(Block.createCuboidShape(7.0, 5.0, 0.0, 9.0, 16.0, 2.0), Block.createCuboidShape(7.0, 5.0, 14.0, 9.0, 16.0, 16.0));
    protected static final VoxelShape IN_WALL_Z_AXIS_CULL_SHAPE = VoxelShapes.union(Block.createCuboidShape(0.0, 2.0, 7.0, 2.0, 13.0, 9.0), Block.createCuboidShape(14.0, 2.0, 7.0, 16.0, 13.0, 9.0));
    protected static final VoxelShape IN_WALL_X_AXIS_CULL_SHAPE = VoxelShapes.union(Block.createCuboidShape(7.0, 2.0, 0.0, 9.0, 13.0, 2.0), Block.createCuboidShape(7.0, 2.0, 14.0, 9.0, 13.0, 16.0));

    public FenceGateBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(OPEN, false)).with(POWERED, false)).with(IN_WALL, false));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(IN_WALL).booleanValue()) {
            return state.get(FACING).getAxis() == Direction.Axis.X ? IN_WALL_X_AXIS_SHAPE : IN_WALL_Z_AXIS_SHAPE;
        }
        return state.get(FACING).getAxis() == Direction.Axis.X ? X_AXIS_SHAPE : Z_AXIS_SHAPE;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        Direction.Axis axis = direction.getAxis();
        if (state.get(FACING).rotateYClockwise().getAxis() == axis) {
            boolean bl = this.isWall(neighborState) || this.isWall(world.getBlockState(pos.offset(direction.getOpposite())));
            return (BlockState)state.with(IN_WALL, bl);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(OPEN).booleanValue()) {
            return VoxelShapes.empty();
        }
        return state.get(FACING).getAxis() == Direction.Axis.Z ? Z_AXIS_COLLISION_SHAPE : X_AXIS_COLLISION_SHAPE;
    }

    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        if (state.get(IN_WALL).booleanValue()) {
            return state.get(FACING).getAxis() == Direction.Axis.X ? IN_WALL_X_AXIS_CULL_SHAPE : IN_WALL_Z_AXIS_CULL_SHAPE;
        }
        return state.get(FACING).getAxis() == Direction.Axis.X ? X_AXIS_CULL_SHAPE : Z_AXIS_CULL_SHAPE;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        switch (type) {
            case LAND: {
                return state.get(OPEN);
            }
            case WATER: {
                return false;
            }
            case AIR: {
                return state.get(OPEN);
            }
        }
        return false;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        boolean bl = world.isReceivingRedstonePower(blockPos);
        Direction direction = ctx.getPlayerFacing();
        Direction.Axis axis = direction.getAxis();
        boolean bl2 = axis == Direction.Axis.Z && (this.isWall(world.getBlockState(blockPos.west())) || this.isWall(world.getBlockState(blockPos.east()))) || axis == Direction.Axis.X && (this.isWall(world.getBlockState(blockPos.north())) || this.isWall(world.getBlockState(blockPos.south())));
        return (BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(FACING, direction)).with(OPEN, bl)).with(POWERED, bl)).with(IN_WALL, bl2);
    }

    private boolean isWall(BlockState state) {
        return state.isIn(BlockTags.WALLS);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (state.get(OPEN).booleanValue()) {
            state = (BlockState)state.with(OPEN, false);
            world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
        } else {
            Direction direction = player.getHorizontalFacing();
            if (state.get(FACING) == direction.getOpposite()) {
                state = (BlockState)state.with(FACING, direction);
            }
            state = (BlockState)state.with(OPEN, true);
            world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
        }
        boolean direction = state.get(OPEN);
        world.syncWorldEvent(player, direction ? WorldEvents.FENCE_GATE_OPENS : WorldEvents.FENCE_GATE_CLOSES, pos, 0);
        world.emitGameEvent((Entity)player, direction ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
        return ActionResult.success(world.isClient);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (world.isClient) {
            return;
        }
        boolean bl = world.isReceivingRedstonePower(pos);
        if (state.get(POWERED) != bl) {
            world.setBlockState(pos, (BlockState)((BlockState)state.with(POWERED, bl)).with(OPEN, bl), Block.NOTIFY_LISTENERS);
            if (state.get(OPEN) != bl) {
                world.syncWorldEvent(null, bl ? WorldEvents.FENCE_GATE_OPENS : WorldEvents.FENCE_GATE_CLOSES, pos, 0);
                world.emitGameEvent(bl ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
            }
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, POWERED, IN_WALL);
    }

    public static boolean canWallConnect(BlockState state, Direction side) {
        return state.get(FACING).getAxis() == side.rotateYClockwise().getAxis();
    }
}

