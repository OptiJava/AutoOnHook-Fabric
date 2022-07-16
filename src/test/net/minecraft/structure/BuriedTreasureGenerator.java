/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.structure;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class BuriedTreasureGenerator {

    public static class Piece
    extends StructurePiece {
        public Piece(BlockPos pos) {
            super(StructurePieceType.BURIED_TREASURE, 0, new BlockBox(pos));
        }

        public Piece(ServerWorld world, NbtCompound nbt) {
            super(StructurePieceType.BURIED_TREASURE, nbt);
        }

        @Override
        protected void writeNbt(ServerWorld world, NbtCompound nbt) {
        }

        @Override
        public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
            int i = world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, this.boundingBox.getMinX(), this.boundingBox.getMinZ());
            BlockPos.Mutable mutable = new BlockPos.Mutable(this.boundingBox.getMinX(), i, this.boundingBox.getMinZ());
            while (mutable.getY() > world.getBottomY()) {
                BlockState blockState = world.getBlockState(mutable);
                BlockState blockState2 = world.getBlockState((BlockPos)mutable.down());
                if (blockState2 == Blocks.SANDSTONE.getDefaultState() || blockState2 == Blocks.STONE.getDefaultState() || blockState2 == Blocks.ANDESITE.getDefaultState() || blockState2 == Blocks.GRANITE.getDefaultState() || blockState2 == Blocks.DIORITE.getDefaultState()) {
                    BlockState blockState3 = blockState.isAir() || this.isLiquid(blockState) ? Blocks.SAND.getDefaultState() : blockState;
                    for (Direction direction : Direction.values()) {
                        Vec3i blockPos = mutable.offset(direction);
                        BlockState blockState4 = world.getBlockState((BlockPos)blockPos);
                        if (!blockState4.isAir() && !this.isLiquid(blockState4)) continue;
                        BlockPos blockPos2 = ((BlockPos)blockPos).down();
                        BlockState blockState5 = world.getBlockState(blockPos2);
                        if ((blockState5.isAir() || this.isLiquid(blockState5)) && direction != Direction.UP) {
                            world.setBlockState((BlockPos)blockPos, blockState2, Block.NOTIFY_ALL);
                            continue;
                        }
                        world.setBlockState((BlockPos)blockPos, blockState3, Block.NOTIFY_ALL);
                    }
                    this.boundingBox = new BlockBox(mutable);
                    return this.addChest(world, boundingBox, random, mutable, LootTables.BURIED_TREASURE_CHEST, null);
                }
                mutable.move(0, -1, 0);
            }
            return false;
        }

        private boolean isLiquid(BlockState state) {
            return state == Blocks.WATER.getDefaultState() || state == Blocks.LAVA.getDefaultState();
        }
    }
}

