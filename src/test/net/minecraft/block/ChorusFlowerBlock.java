/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChorusPlantBlock;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class ChorusFlowerBlock
extends Block {
    public static final int MAX_AGE = 5;
    public static final IntProperty AGE = Properties.AGE_5;
    private final ChorusPlantBlock plantBlock;

    protected ChorusFlowerBlock(ChorusPlantBlock plantBlock, AbstractBlock.Settings settings) {
        super(settings);
        this.plantBlock = plantBlock;
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0));
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return state.get(AGE) < 5;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int k;
        int j;
        BlockPos blockPos = pos.up();
        if (!world.isAir(blockPos) || blockPos.getY() >= world.getTopY()) {
            return;
        }
        int i = state.get(AGE);
        if (i >= 5) {
            return;
        }
        boolean bl = false;
        boolean bl2 = false;
        BlockState blockState = world.getBlockState(pos.down());
        if (blockState.isOf(Blocks.END_STONE)) {
            bl = true;
        } else if (blockState.isOf(this.plantBlock)) {
            j = 1;
            for (k = 0; k < 4; ++k) {
                BlockState blockState2 = world.getBlockState(pos.down(j + 1));
                if (blockState2.isOf(this.plantBlock)) {
                    ++j;
                    continue;
                }
                if (!blockState2.isOf(Blocks.END_STONE)) break;
                bl2 = true;
                break;
            }
            if (j < 2 || j <= random.nextInt(bl2 ? 5 : 4)) {
                bl = true;
            }
        } else if (blockState.isAir()) {
            bl = true;
        }
        if (bl && ChorusFlowerBlock.isSurroundedByAir(world, blockPos, null) && world.isAir(pos.up(2))) {
            world.setBlockState(pos, this.plantBlock.withConnectionProperties(world, pos), Block.NOTIFY_LISTENERS);
            this.grow(world, blockPos, i);
        } else if (i < 4) {
            j = random.nextInt(4);
            if (bl2) {
                ++j;
            }
            k = 0;
            for (int blockState2 = 0; blockState2 < j; ++blockState2) {
                Direction direction = Direction.Type.HORIZONTAL.random(random);
                BlockPos blockPos2 = pos.offset(direction);
                if (!world.isAir(blockPos2) || !world.isAir(blockPos2.down()) || !ChorusFlowerBlock.isSurroundedByAir(world, blockPos2, direction.getOpposite())) continue;
                this.grow(world, blockPos2, i + 1);
                k = 1;
            }
            if (k != 0) {
                world.setBlockState(pos, this.plantBlock.withConnectionProperties(world, pos), Block.NOTIFY_LISTENERS);
            } else {
                this.die(world, pos);
            }
        } else {
            this.die(world, pos);
        }
    }

    private void grow(World world, BlockPos pos, int age) {
        world.setBlockState(pos, (BlockState)this.getDefaultState().with(AGE, age), Block.NOTIFY_LISTENERS);
        world.syncWorldEvent(WorldEvents.CHORUS_FLOWER_GROWS, pos, 0);
    }

    private void die(World world, BlockPos pos) {
        world.setBlockState(pos, (BlockState)this.getDefaultState().with(AGE, 5), Block.NOTIFY_LISTENERS);
        world.syncWorldEvent(WorldEvents.CHORUS_FLOWER_DIES, pos, 0);
    }

    private static boolean isSurroundedByAir(WorldView world, BlockPos pos, @Nullable Direction exceptDirection) {
        for (Direction direction : Direction.Type.HORIZONTAL) {
            if (direction == exceptDirection || world.isAir(pos.offset(direction))) continue;
            return false;
        }
        return true;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction != Direction.UP && !state.canPlaceAt(world, pos)) {
            world.getBlockTickScheduler().schedule(pos, this, 1);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos.down());
        if (blockState.isOf(this.plantBlock) || blockState.isOf(Blocks.END_STONE)) {
            return true;
        }
        if (!blockState.isAir()) {
            return false;
        }
        boolean bl = false;
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockState blockState2 = world.getBlockState(pos.offset(direction));
            if (blockState2.isOf(this.plantBlock)) {
                if (bl) {
                    return false;
                }
                bl = true;
                continue;
            }
            if (blockState2.isAir()) continue;
            return false;
        }
        return bl;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    public static void generate(WorldAccess world, BlockPos pos, Random random, int size) {
        world.setBlockState(pos, ((ChorusPlantBlock)Blocks.CHORUS_PLANT).withConnectionProperties(world, pos), Block.NOTIFY_LISTENERS);
        ChorusFlowerBlock.generate(world, pos, random, pos, size, 0);
    }

    private static void generate(WorldAccess world, BlockPos pos, Random random, BlockPos rootPos, int size, int layer) {
        int j;
        ChorusPlantBlock chorusPlantBlock = (ChorusPlantBlock)Blocks.CHORUS_PLANT;
        int i = random.nextInt(4) + 1;
        if (layer == 0) {
            ++i;
        }
        for (j = 0; j < i; ++j) {
            BlockPos blockPos = pos.up(j + 1);
            if (!ChorusFlowerBlock.isSurroundedByAir(world, blockPos, null)) {
                return;
            }
            world.setBlockState(blockPos, chorusPlantBlock.withConnectionProperties(world, blockPos), Block.NOTIFY_LISTENERS);
            world.setBlockState(blockPos.down(), chorusPlantBlock.withConnectionProperties(world, blockPos.down()), Block.NOTIFY_LISTENERS);
        }
        j = 0;
        if (layer < 4) {
            int blockPos = random.nextInt(4);
            if (layer == 0) {
                ++blockPos;
            }
            for (int k = 0; k < blockPos; ++k) {
                Direction direction = Direction.Type.HORIZONTAL.random(random);
                BlockPos blockPos2 = pos.up(i).offset(direction);
                if (Math.abs(blockPos2.getX() - rootPos.getX()) >= size || Math.abs(blockPos2.getZ() - rootPos.getZ()) >= size || !world.isAir(blockPos2) || !world.isAir(blockPos2.down()) || !ChorusFlowerBlock.isSurroundedByAir(world, blockPos2, direction.getOpposite())) continue;
                j = 1;
                world.setBlockState(blockPos2, chorusPlantBlock.withConnectionProperties(world, blockPos2), Block.NOTIFY_LISTENERS);
                world.setBlockState(blockPos2.offset(direction.getOpposite()), chorusPlantBlock.withConnectionProperties(world, blockPos2.offset(direction.getOpposite())), Block.NOTIFY_LISTENERS);
                ChorusFlowerBlock.generate(world, blockPos2, random, rootPos, size, layer + 1);
            }
        }
        if (j == 0) {
            world.setBlockState(pos.up(i), (BlockState)Blocks.CHORUS_FLOWER.getDefaultState().with(AGE, 5), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        BlockPos blockPos = hit.getBlockPos();
        if (!world.isClient && projectile.canModifyAt(world, blockPos) && projectile.getType().isIn(EntityTypeTags.IMPACT_PROJECTILES)) {
            world.breakBlock(blockPos, true, projectile);
        }
    }
}

