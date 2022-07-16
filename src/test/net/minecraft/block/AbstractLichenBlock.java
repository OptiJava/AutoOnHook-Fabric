/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class AbstractLichenBlock
extends Block {
    private static final float field_31194 = 1.0f;
    private static final VoxelShape UP_SHAPE = Block.createCuboidShape(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape DOWN_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
    private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
    private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
    private static final Map<Direction, BooleanProperty> FACING_PROPERTIES = ConnectingBlock.FACING_PROPERTIES;
    private static final Map<Direction, VoxelShape> SHAPES_FOR_DIRECTIONS = Util.make(Maps.newEnumMap(Direction.class), shapes -> {
        shapes.put(Direction.NORTH, SOUTH_SHAPE);
        shapes.put(Direction.EAST, WEST_SHAPE);
        shapes.put(Direction.SOUTH, NORTH_SHAPE);
        shapes.put(Direction.WEST, EAST_SHAPE);
        shapes.put(Direction.UP, UP_SHAPE);
        shapes.put(Direction.DOWN, DOWN_SHAPE);
    });
    protected static final Direction[] DIRECTIONS = Direction.values();
    private final ImmutableMap<BlockState, VoxelShape> SHAPES;
    private final boolean hasAllHorizontalDirections;
    private final boolean canMirrorX;
    private final boolean canMirrorZ;

    public AbstractLichenBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(AbstractLichenBlock.withAllDirections(this.stateManager));
        this.SHAPES = this.getShapesForStates(AbstractLichenBlock::getShapeForState);
        this.hasAllHorizontalDirections = Direction.Type.HORIZONTAL.stream().allMatch(this::canHaveDirection);
        this.canMirrorX = Direction.Type.HORIZONTAL.stream().filter(Direction.Axis.X).filter(this::canHaveDirection).count() % 2L == 0L;
        this.canMirrorZ = Direction.Type.HORIZONTAL.stream().filter(Direction.Axis.Z).filter(this::canHaveDirection).count() % 2L == 0L;
    }

    protected boolean canHaveDirection(Direction direction) {
        return true;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        for (Direction direction : DIRECTIONS) {
            if (!this.canHaveDirection(direction)) continue;
            builder.add(AbstractLichenBlock.getProperty(direction));
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!AbstractLichenBlock.hasAnyDirection(state)) {
            return Blocks.AIR.getDefaultState();
        }
        if (!AbstractLichenBlock.hasDirection(state, direction) || AbstractLichenBlock.canGrowOn(world, direction, neighborPos, neighborState)) {
            return state;
        }
        return AbstractLichenBlock.disableDirection(state, AbstractLichenBlock.getProperty(direction));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.SHAPES.get(state);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        boolean bl = false;
        for (Direction direction : DIRECTIONS) {
            if (!AbstractLichenBlock.hasDirection(state, direction)) continue;
            BlockPos blockPos = pos.offset(direction);
            if (!AbstractLichenBlock.canGrowOn(world, direction, blockPos, world.getBlockState(blockPos))) {
                return false;
            }
            bl = true;
        }
        return bl;
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        return AbstractLichenBlock.isNotFullBlock(state);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        return Arrays.stream(ctx.getPlacementDirections()).map(direction -> this.withDirection(blockState, world, blockPos, (Direction)direction)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    @Nullable
    public BlockState withDirection(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        BlockState blockState;
        if (!this.canHaveDirection(direction)) {
            return null;
        }
        if (state.isOf(this)) {
            if (AbstractLichenBlock.hasDirection(state, direction)) {
                return null;
            }
            blockState = state;
        } else {
            blockState = this.isWaterlogged() && state.getFluidState().isEqualAndStill(Fluids.WATER) ? (BlockState)this.getDefaultState().with(Properties.WATERLOGGED, true) : this.getDefaultState();
        }
        BlockPos blockPos = pos.offset(direction);
        if (AbstractLichenBlock.canGrowOn(world, direction, blockPos, world.getBlockState(blockPos))) {
            return (BlockState)blockState.with(AbstractLichenBlock.getProperty(direction), true);
        }
        return null;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        if (!this.hasAllHorizontalDirections) {
            return state;
        }
        return this.mirror(state, rotation::rotate);
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        if (mirror == BlockMirror.FRONT_BACK && !this.canMirrorX) {
            return state;
        }
        if (mirror == BlockMirror.LEFT_RIGHT && !this.canMirrorZ) {
            return state;
        }
        return this.mirror(state, mirror::apply);
    }

    private BlockState mirror(BlockState state, Function<Direction, Direction> mirror) {
        BlockState blockState = state;
        for (Direction direction : DIRECTIONS) {
            if (!this.canHaveDirection(direction)) continue;
            blockState = (BlockState)blockState.with(AbstractLichenBlock.getProperty(mirror.apply(direction)), state.get(AbstractLichenBlock.getProperty(direction)));
        }
        return blockState;
    }

    public boolean trySpreadRandomly(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        ArrayList<Direction> list = Lists.newArrayList(DIRECTIONS);
        Collections.shuffle(list);
        return list.stream().filter(from -> AbstractLichenBlock.hasDirection(state, from)).anyMatch(to -> this.trySpreadRandomly(state, world, pos, (Direction)to, random, false));
    }

    public boolean trySpreadRandomly(BlockState state, WorldAccess world, BlockPos pos, Direction from, Random random, boolean postProcess) {
        List<Direction> list = Arrays.asList(DIRECTIONS);
        Collections.shuffle(list, random);
        return list.stream().anyMatch(direction2 -> this.trySpreadTo(state, world, pos, from, (Direction)direction2, postProcess));
    }

    public boolean trySpreadTo(BlockState state, WorldAccess world, BlockPos pos, Direction from, Direction to, boolean postProcess) {
        Optional<Pair<BlockPos, Direction>> optional = this.getSpreadLocation(state, world, pos, from, to);
        if (optional.isPresent()) {
            Pair<BlockPos, Direction> pair = optional.get();
            return this.addDirection(world, pair.getFirst(), pair.getSecond(), postProcess);
        }
        return false;
    }

    protected boolean canSpread(BlockState state, BlockView world, BlockPos pos, Direction from) {
        return Stream.of(DIRECTIONS).anyMatch(to -> this.getSpreadLocation(state, world, pos, from, (Direction)to).isPresent());
    }

    private Optional<Pair<BlockPos, Direction>> getSpreadLocation(BlockState state, BlockView world, BlockPos pos, Direction from, Direction to) {
        Direction direction;
        if (to.getAxis() == from.getAxis() || !AbstractLichenBlock.hasDirection(state, from) || AbstractLichenBlock.hasDirection(state, to)) {
            return Optional.empty();
        }
        if (this.canSpreadTo(world, pos, to)) {
            return Optional.of(Pair.of(pos, to));
        }
        BlockPos blockPos = pos.offset(to);
        if (this.canSpreadTo(world, blockPos, from)) {
            return Optional.of(Pair.of(blockPos, from));
        }
        BlockPos blockPos2 = blockPos.offset(from);
        if (this.canSpreadTo(world, blockPos2, direction = to.getOpposite())) {
            return Optional.of(Pair.of(blockPos2, direction));
        }
        return Optional.empty();
    }

    private boolean canSpreadTo(BlockView world, BlockPos pos, Direction direction) {
        BlockState blockState = world.getBlockState(pos);
        if (!this.canGrowIn(blockState)) {
            return false;
        }
        BlockState blockState2 = this.withDirection(blockState, world, pos, direction);
        return blockState2 != null;
    }

    private boolean addDirection(WorldAccess world, BlockPos pos, Direction direction, boolean postProcess) {
        BlockState blockState = world.getBlockState(pos);
        BlockState blockState2 = this.withDirection(blockState, world, pos, direction);
        if (blockState2 != null) {
            if (postProcess) {
                world.getChunk(pos).markBlockForPostProcessing(pos);
            }
            return world.setBlockState(pos, blockState2, Block.NOTIFY_LISTENERS);
        }
        return false;
    }

    private boolean canGrowIn(BlockState state) {
        return state.isAir() || state.isOf(this) || state.isOf(Blocks.WATER) && state.getFluidState().isStill();
    }

    private static boolean hasDirection(BlockState state, Direction direction) {
        BooleanProperty booleanProperty = AbstractLichenBlock.getProperty(direction);
        return state.contains(booleanProperty) && state.get(booleanProperty) != false;
    }

    private static boolean canGrowOn(BlockView world, Direction direction, BlockPos pos, BlockState state) {
        return Block.isFaceFullSquare(state.getCollisionShape(world, pos), direction.getOpposite());
    }

    private boolean isWaterlogged() {
        return this.stateManager.getProperties().contains(Properties.WATERLOGGED);
    }

    private static BlockState disableDirection(BlockState state, BooleanProperty direction) {
        BlockState blockState = (BlockState)state.with(direction, false);
        if (AbstractLichenBlock.hasAnyDirection(blockState)) {
            return blockState;
        }
        return Blocks.AIR.getDefaultState();
    }

    public static BooleanProperty getProperty(Direction direction) {
        return FACING_PROPERTIES.get(direction);
    }

    private static BlockState withAllDirections(StateManager<Block, BlockState> stateManager) {
        BlockState blockState = stateManager.getDefaultState();
        for (BooleanProperty booleanProperty : FACING_PROPERTIES.values()) {
            if (!blockState.contains(booleanProperty)) continue;
            blockState = (BlockState)blockState.with(booleanProperty, false);
        }
        return blockState;
    }

    private static VoxelShape getShapeForState(BlockState state) {
        VoxelShape voxelShape = VoxelShapes.empty();
        for (Direction direction : DIRECTIONS) {
            if (!AbstractLichenBlock.hasDirection(state, direction)) continue;
            voxelShape = VoxelShapes.union(voxelShape, SHAPES_FOR_DIRECTIONS.get(direction));
        }
        return voxelShape.isEmpty() ? VoxelShapes.fullCube() : voxelShape;
    }

    protected static boolean hasAnyDirection(BlockState state) {
        return Arrays.stream(DIRECTIONS).anyMatch(direction -> AbstractLichenBlock.hasDirection(state, direction));
    }

    private static boolean isNotFullBlock(BlockState state) {
        return Arrays.stream(DIRECTIONS).anyMatch(direction -> !AbstractLichenBlock.hasDirection(state, direction));
    }
}

