/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class ConnectingBlock
extends Block {
    private static final Direction[] FACINGS = Direction.values();
    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty EAST = Properties.EAST;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty WEST = Properties.WEST;
    public static final BooleanProperty UP = Properties.UP;
    public static final BooleanProperty DOWN = Properties.DOWN;
    public static final Map<Direction, BooleanProperty> FACING_PROPERTIES = ImmutableMap.copyOf(Util.make(Maps.newEnumMap(Direction.class), directions -> {
        directions.put(Direction.NORTH, NORTH);
        directions.put(Direction.EAST, EAST);
        directions.put(Direction.SOUTH, SOUTH);
        directions.put(Direction.WEST, WEST);
        directions.put(Direction.UP, UP);
        directions.put(Direction.DOWN, DOWN);
    }));
    protected final VoxelShape[] CONNECTIONS_TO_SHAPE;

    protected ConnectingBlock(float radius, AbstractBlock.Settings settings) {
        super(settings);
        this.CONNECTIONS_TO_SHAPE = this.generateFacingsToShapeMap(radius);
    }

    private VoxelShape[] generateFacingsToShapeMap(float radius) {
        float f = 0.5f - radius;
        float g = 0.5f + radius;
        VoxelShape voxelShape = Block.createCuboidShape(f * 16.0f, f * 16.0f, f * 16.0f, g * 16.0f, g * 16.0f, g * 16.0f);
        VoxelShape[] voxelShapes = new VoxelShape[FACINGS.length];
        for (int i = 0; i < FACINGS.length; ++i) {
            Direction direction = FACINGS[i];
            voxelShapes[i] = VoxelShapes.cuboid(0.5 + Math.min((double)(-radius), (double)direction.getOffsetX() * 0.5), 0.5 + Math.min((double)(-radius), (double)direction.getOffsetY() * 0.5), 0.5 + Math.min((double)(-radius), (double)direction.getOffsetZ() * 0.5), 0.5 + Math.max((double)radius, (double)direction.getOffsetX() * 0.5), 0.5 + Math.max((double)radius, (double)direction.getOffsetY() * 0.5), 0.5 + Math.max((double)radius, (double)direction.getOffsetZ() * 0.5));
        }
        VoxelShape[] i = new VoxelShape[64];
        for (int direction = 0; direction < 64; ++direction) {
            VoxelShape voxelShape2 = voxelShape;
            for (int j = 0; j < FACINGS.length; ++j) {
                if ((direction & 1 << j) == 0) continue;
                voxelShape2 = VoxelShapes.union(voxelShape2, voxelShapes[j]);
            }
            i[direction] = voxelShape2;
        }
        return i;
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.CONNECTIONS_TO_SHAPE[this.getConnectionMask(state)];
    }

    protected int getConnectionMask(BlockState state) {
        int i = 0;
        for (int j = 0; j < FACINGS.length; ++j) {
            if (!((Boolean)state.get(FACING_PROPERTIES.get(FACINGS[j]))).booleanValue()) continue;
            i |= 1 << j;
        }
        return i;
    }
}

