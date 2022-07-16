/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.structure;

import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.StairShape;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructurePieceWithDimensions;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class SwampHutGenerator
extends StructurePieceWithDimensions {
    private boolean hasWitch;
    private boolean hasCat;

    public SwampHutGenerator(Random random, int x, int z) {
        super(StructurePieceType.SWAMP_HUT, x, 64, z, 7, 7, 9, SwampHutGenerator.getRandomHorizontalDirection(random));
    }

    public SwampHutGenerator(ServerWorld world, NbtCompound nbt) {
        super(StructurePieceType.SWAMP_HUT, nbt);
        this.hasWitch = nbt.getBoolean("Witch");
        this.hasCat = nbt.getBoolean("Cat");
    }

    @Override
    protected void writeNbt(ServerWorld world, NbtCompound nbt) {
        super.writeNbt(world, nbt);
        nbt.putBoolean("Witch", this.hasWitch);
        nbt.putBoolean("Cat", this.hasCat);
    }

    @Override
    public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
        BlockPos.Mutable i;
        if (!this.adjustToAverageHeight(world, boundingBox, 0)) {
            return false;
        }
        this.fillWithOutline(world, boundingBox, 1, 1, 1, 5, 1, 7, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
        this.fillWithOutline(world, boundingBox, 1, 4, 2, 5, 4, 7, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
        this.fillWithOutline(world, boundingBox, 2, 1, 0, 4, 1, 0, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
        this.fillWithOutline(world, boundingBox, 2, 2, 2, 3, 3, 2, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
        this.fillWithOutline(world, boundingBox, 1, 2, 3, 1, 3, 6, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
        this.fillWithOutline(world, boundingBox, 5, 2, 3, 5, 3, 6, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
        this.fillWithOutline(world, boundingBox, 2, 2, 7, 4, 3, 7, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
        this.fillWithOutline(world, boundingBox, 1, 0, 2, 1, 3, 2, Blocks.OAK_LOG.getDefaultState(), Blocks.OAK_LOG.getDefaultState(), false);
        this.fillWithOutline(world, boundingBox, 5, 0, 2, 5, 3, 2, Blocks.OAK_LOG.getDefaultState(), Blocks.OAK_LOG.getDefaultState(), false);
        this.fillWithOutline(world, boundingBox, 1, 0, 7, 1, 3, 7, Blocks.OAK_LOG.getDefaultState(), Blocks.OAK_LOG.getDefaultState(), false);
        this.fillWithOutline(world, boundingBox, 5, 0, 7, 5, 3, 7, Blocks.OAK_LOG.getDefaultState(), Blocks.OAK_LOG.getDefaultState(), false);
        this.addBlock(world, Blocks.OAK_FENCE.getDefaultState(), 2, 3, 2, boundingBox);
        this.addBlock(world, Blocks.OAK_FENCE.getDefaultState(), 3, 3, 7, boundingBox);
        this.addBlock(world, Blocks.AIR.getDefaultState(), 1, 3, 4, boundingBox);
        this.addBlock(world, Blocks.AIR.getDefaultState(), 5, 3, 4, boundingBox);
        this.addBlock(world, Blocks.AIR.getDefaultState(), 5, 3, 5, boundingBox);
        this.addBlock(world, Blocks.POTTED_RED_MUSHROOM.getDefaultState(), 1, 3, 5, boundingBox);
        this.addBlock(world, Blocks.CRAFTING_TABLE.getDefaultState(), 3, 2, 6, boundingBox);
        this.addBlock(world, Blocks.CAULDRON.getDefaultState(), 4, 2, 6, boundingBox);
        this.addBlock(world, Blocks.OAK_FENCE.getDefaultState(), 1, 2, 1, boundingBox);
        this.addBlock(world, Blocks.OAK_FENCE.getDefaultState(), 5, 2, 1, boundingBox);
        BlockState blockState = (BlockState)Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH);
        BlockState blockState2 = (BlockState)Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST);
        BlockState blockState3 = (BlockState)Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST);
        BlockState blockState4 = (BlockState)Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH);
        this.fillWithOutline(world, boundingBox, 0, 4, 1, 6, 4, 1, blockState, blockState, false);
        this.fillWithOutline(world, boundingBox, 0, 4, 2, 0, 4, 7, blockState2, blockState2, false);
        this.fillWithOutline(world, boundingBox, 6, 4, 2, 6, 4, 7, blockState3, blockState3, false);
        this.fillWithOutline(world, boundingBox, 0, 4, 8, 6, 4, 8, blockState4, blockState4, false);
        this.addBlock(world, (BlockState)blockState.with(StairsBlock.SHAPE, StairShape.OUTER_RIGHT), 0, 4, 1, boundingBox);
        this.addBlock(world, (BlockState)blockState.with(StairsBlock.SHAPE, StairShape.OUTER_LEFT), 6, 4, 1, boundingBox);
        this.addBlock(world, (BlockState)blockState4.with(StairsBlock.SHAPE, StairShape.OUTER_LEFT), 0, 4, 8, boundingBox);
        this.addBlock(world, (BlockState)blockState4.with(StairsBlock.SHAPE, StairShape.OUTER_RIGHT), 6, 4, 8, boundingBox);
        for (int i2 = 2; i2 <= 7; i2 += 5) {
            for (int j = 1; j <= 5; j += 4) {
                this.fillDownwards(world, Blocks.OAK_LOG.getDefaultState(), j, -1, i2, boundingBox);
            }
        }
        if (!this.hasWitch && boundingBox.contains(i = this.offsetPos(2, 2, 5))) {
            this.hasWitch = true;
            WitchEntity j = EntityType.WITCH.create(world.toServerWorld());
            j.setPersistent();
            j.refreshPositionAndAngles((double)i.getX() + 0.5, i.getY(), (double)i.getZ() + 0.5, 0.0f, 0.0f);
            j.initialize(world, world.getLocalDifficulty(i), SpawnReason.STRUCTURE, null, null);
            world.spawnEntityAndPassengers(j);
        }
        this.spawnCat(world, boundingBox);
        return true;
    }

    private void spawnCat(ServerWorldAccess world, BlockBox box) {
        BlockPos.Mutable blockPos;
        if (!this.hasCat && box.contains(blockPos = this.offsetPos(2, 2, 5))) {
            this.hasCat = true;
            CatEntity catEntity = EntityType.CAT.create(world.toServerWorld());
            catEntity.setPersistent();
            catEntity.refreshPositionAndAngles((double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5, 0.0f, 0.0f);
            catEntity.initialize(world, world.getLocalDifficulty(blockPos), SpawnReason.STRUCTURE, null, null);
            world.spawnEntityAndPassengers(catEntity);
        }
    }
}

