/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.structure;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.SimpleStructurePiece;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class IglooGenerator {
    public static final int field_31550 = 90;
    static final Identifier TOP_TEMPLATE = new Identifier("igloo/top");
    private static final Identifier MIDDLE_TEMPLATE = new Identifier("igloo/middle");
    private static final Identifier BOTTOM_TEMPLATE = new Identifier("igloo/bottom");
    static final Map<Identifier, BlockPos> OFFSETS = ImmutableMap.of(TOP_TEMPLATE, new BlockPos(3, 5, 5), MIDDLE_TEMPLATE, new BlockPos(1, 3, 1), BOTTOM_TEMPLATE, new BlockPos(3, 6, 7));
    static final Map<Identifier, BlockPos> OFFSETS_FROM_TOP = ImmutableMap.of(TOP_TEMPLATE, BlockPos.ORIGIN, MIDDLE_TEMPLATE, new BlockPos(2, -3, 4), BOTTOM_TEMPLATE, new BlockPos(0, -3, -2));

    public static void addPieces(StructureManager manager, BlockPos pos, BlockRotation rotation, StructurePiecesHolder structurePiecesHolder, Random random) {
        if (random.nextDouble() < 0.5) {
            int i = random.nextInt(8) + 4;
            structurePiecesHolder.addPiece(new Piece(manager, BOTTOM_TEMPLATE, pos, rotation, i * 3));
            for (int j = 0; j < i - 1; ++j) {
                structurePiecesHolder.addPiece(new Piece(manager, MIDDLE_TEMPLATE, pos, rotation, j * 3));
            }
        }
        structurePiecesHolder.addPiece(new Piece(manager, TOP_TEMPLATE, pos, rotation, 0));
    }

    public static class Piece
    extends SimpleStructurePiece {
        public Piece(StructureManager manager, Identifier identifier, BlockPos pos, BlockRotation rotation, int yOffset) {
            super(StructurePieceType.IGLOO, 0, manager, identifier, identifier.toString(), Piece.createPlacementData(rotation, identifier), Piece.getPosOffset(identifier, pos, yOffset));
        }

        public Piece(ServerWorld world, NbtCompound nbt) {
            super(StructurePieceType.IGLOO, nbt, world, identifier -> Piece.createPlacementData(BlockRotation.valueOf(nbt.getString("Rot")), identifier));
        }

        private static StructurePlacementData createPlacementData(BlockRotation rotation, Identifier identifier) {
            return new StructurePlacementData().setRotation(rotation).setMirror(BlockMirror.NONE).setPosition(OFFSETS.get(identifier)).addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS);
        }

        private static BlockPos getPosOffset(Identifier identifier, BlockPos pos, int yOffset) {
            return pos.add(OFFSETS_FROM_TOP.get(identifier)).down(yOffset);
        }

        @Override
        protected void writeNbt(ServerWorld world, NbtCompound nbt) {
            super.writeNbt(world, nbt);
            nbt.putString("Rot", this.placementData.getRotation().name());
        }

        @Override
        protected void handleMetadata(String metadata, BlockPos pos, ServerWorldAccess world, Random random, BlockBox boundingBox) {
            if (!"chest".equals(metadata)) {
                return;
            }
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            BlockEntity blockEntity = world.getBlockEntity(pos.down());
            if (blockEntity instanceof ChestBlockEntity) {
                ((ChestBlockEntity)blockEntity).setLootTable(LootTables.IGLOO_CHEST_CHEST, random.nextLong());
            }
        }

        @Override
        public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
            BlockPos blockPos4;
            BlockState blockState;
            Identifier identifier = new Identifier(this.identifier);
            StructurePlacementData structurePlacementData = Piece.createPlacementData(this.placementData.getRotation(), identifier);
            BlockPos blockPos = OFFSETS_FROM_TOP.get(identifier);
            BlockPos blockPos2 = this.pos.add(Structure.transform(structurePlacementData, new BlockPos(3 - blockPos.getX(), 0, -blockPos.getZ())));
            int i = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, blockPos2.getX(), blockPos2.getZ());
            BlockPos blockPos3 = this.pos;
            this.pos = this.pos.add(0, i - 90 - 1, 0);
            boolean bl = super.generate(world, structureAccessor, chunkGenerator, random, boundingBox, chunkPos, pos);
            if (identifier.equals(TOP_TEMPLATE) && !(blockState = world.getBlockState((blockPos4 = this.pos.add(Structure.transform(structurePlacementData, new BlockPos(3, 0, 5)))).down())).isAir() && !blockState.isOf(Blocks.LADDER)) {
                world.setBlockState(blockPos4, Blocks.SNOW_BLOCK.getDefaultState(), Block.NOTIFY_ALL);
            }
            this.pos = blockPos3;
            return bl;
        }
    }
}

