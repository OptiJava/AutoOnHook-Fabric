/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.StructureWeightType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

public class StrongholdGenerator {
    private static final int field_31624 = 3;
    private static final int field_31625 = 3;
    private static final int field_31626 = 50;
    private static final int field_31627 = 10;
    private static final boolean field_31628 = true;
    private static final PieceData[] ALL_PIECES = new PieceData[]{new PieceData(Corridor.class, 40, 0), new PieceData(PrisonHall.class, 5, 5), new PieceData(LeftTurn.class, 20, 0), new PieceData(RightTurn.class, 20, 0), new PieceData(SquareRoom.class, 10, 6), new PieceData(Stairs.class, 5, 5), new PieceData(SpiralStaircase.class, 5, 5), new PieceData(FiveWayCrossing.class, 5, 4), new PieceData(ChestCorridor.class, 5, 4), new PieceData(Library.class, 10, 2){

        @Override
        public boolean canGenerate(int chainLength) {
            return super.canGenerate(chainLength) && chainLength > 4;
        }
    }, new PieceData(PortalRoom.class, 20, 1){

        @Override
        public boolean canGenerate(int chainLength) {
            return super.canGenerate(chainLength) && chainLength > 5;
        }
    }};
    private static List<PieceData> possiblePieces;
    static Class<? extends Piece> activePieceType;
    private static int totalWeight;
    static final StoneBrickRandomizer STONE_BRICK_RANDOMIZER;

    public static void init() {
        possiblePieces = Lists.newArrayList();
        for (PieceData pieceData : ALL_PIECES) {
            pieceData.generatedCount = 0;
            possiblePieces.add(pieceData);
        }
        activePieceType = null;
    }

    private static boolean checkRemainingPieces() {
        boolean bl = false;
        totalWeight = 0;
        for (PieceData pieceData : possiblePieces) {
            if (pieceData.limit > 0 && pieceData.generatedCount < pieceData.limit) {
                bl = true;
            }
            totalWeight += pieceData.weight;
        }
        return bl;
    }

    private static Piece createPiece(Class<? extends Piece> pieceType, StructurePiecesHolder structurePiecesHolder, Random random, int x, int y, int z, @Nullable Direction orientation, int chainLength) {
        Piece piece = null;
        if (pieceType == Corridor.class) {
            piece = Corridor.create(structurePiecesHolder, random, x, y, z, orientation, chainLength);
        } else if (pieceType == PrisonHall.class) {
            piece = PrisonHall.create(structurePiecesHolder, random, x, y, z, orientation, chainLength);
        } else if (pieceType == LeftTurn.class) {
            piece = LeftTurn.create(structurePiecesHolder, random, x, y, z, orientation, chainLength);
        } else if (pieceType == RightTurn.class) {
            piece = RightTurn.create(structurePiecesHolder, random, x, y, z, orientation, chainLength);
        } else if (pieceType == SquareRoom.class) {
            piece = SquareRoom.create(structurePiecesHolder, random, x, y, z, orientation, chainLength);
        } else if (pieceType == Stairs.class) {
            piece = Stairs.create(structurePiecesHolder, random, x, y, z, orientation, chainLength);
        } else if (pieceType == SpiralStaircase.class) {
            piece = SpiralStaircase.create(structurePiecesHolder, random, x, y, z, orientation, chainLength);
        } else if (pieceType == FiveWayCrossing.class) {
            piece = FiveWayCrossing.create(structurePiecesHolder, random, x, y, z, orientation, chainLength);
        } else if (pieceType == ChestCorridor.class) {
            piece = ChestCorridor.create(structurePiecesHolder, random, x, y, z, orientation, chainLength);
        } else if (pieceType == Library.class) {
            piece = Library.create(structurePiecesHolder, random, x, y, z, orientation, chainLength);
        } else if (pieceType == PortalRoom.class) {
            piece = PortalRoom.create(structurePiecesHolder, x, y, z, orientation, chainLength);
        }
        return piece;
    }

    private static Piece pickPiece(Start start, StructurePiecesHolder structurePiecesHolder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
        if (!StrongholdGenerator.checkRemainingPieces()) {
            return null;
        }
        if (activePieceType != null) {
            Piece piece = StrongholdGenerator.createPiece(activePieceType, structurePiecesHolder, random, x, y, z, orientation, chainLength);
            activePieceType = null;
            if (piece != null) {
                return piece;
            }
        }
        int piece = 0;
        block0: while (piece < 5) {
            ++piece;
            int i = random.nextInt(totalWeight);
            for (PieceData pieceData : possiblePieces) {
                if ((i -= pieceData.weight) >= 0) continue;
                if (!pieceData.canGenerate(chainLength) || pieceData == start.lastPiece) continue block0;
                Piece piece2 = StrongholdGenerator.createPiece(pieceData.pieceType, structurePiecesHolder, random, x, y, z, orientation, chainLength);
                if (piece2 == null) continue;
                ++pieceData.generatedCount;
                start.lastPiece = pieceData;
                if (!pieceData.canGenerate()) {
                    possiblePieces.remove(pieceData);
                }
                return piece2;
            }
        }
        BlockBox i = SmallCorridor.create(structurePiecesHolder, random, x, y, z, orientation);
        if (i != null && i.getMinY() > 1) {
            return new SmallCorridor(chainLength, i, orientation);
        }
        return null;
    }

    static StructurePiece pieceGenerator(Start start, StructurePiecesHolder structurePiecesHolder, Random random, int x, int y, int z, @Nullable Direction orientation, int chainLength) {
        if (chainLength > 50) {
            return null;
        }
        if (Math.abs(x - start.getBoundingBox().getMinX()) > 112 || Math.abs(z - start.getBoundingBox().getMinZ()) > 112) {
            return null;
        }
        Piece structurePiece = StrongholdGenerator.pickPiece(start, structurePiecesHolder, random, x, y, z, orientation, chainLength + 1);
        if (structurePiece != null) {
            structurePiecesHolder.addPiece(structurePiece);
            start.pieces.add(structurePiece);
        }
        return structurePiece;
    }

    static {
        STONE_BRICK_RANDOMIZER = new StoneBrickRandomizer();
    }

    static class PieceData {
        public final Class<? extends Piece> pieceType;
        public final int weight;
        public int generatedCount;
        public final int limit;

        public PieceData(Class<? extends Piece> pieceType, int weight, int limit) {
            this.pieceType = pieceType;
            this.weight = weight;
            this.limit = limit;
        }

        public boolean canGenerate(int chainLength) {
            return this.limit == 0 || this.generatedCount < this.limit;
        }

        public boolean canGenerate() {
            return this.limit == 0 || this.generatedCount < this.limit;
        }
    }

    public static class Corridor
    extends Piece {
        private static final int SIZE_X = 5;
        private static final int SIZE_Y = 5;
        private static final int SIZE_Z = 7;
        private final boolean leftExitExists;
        private final boolean rightExitExists;

        public Corridor(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.STRONGHOLD_CORRIDOR, chainLength, boundingBox);
            this.setOrientation(orientation);
            this.entryDoor = this.getRandomEntrance(random);
            this.leftExitExists = random.nextInt(2) == 0;
            this.rightExitExists = random.nextInt(2) == 0;
        }

        public Corridor(ServerWorld world, NbtCompound nbt) {
            super(StructurePieceType.STRONGHOLD_CORRIDOR, nbt);
            this.leftExitExists = nbt.getBoolean("Left");
            this.rightExitExists = nbt.getBoolean("Right");
        }

        @Override
        protected void writeNbt(ServerWorld world, NbtCompound nbt) {
            super.writeNbt(world, nbt);
            nbt.putBoolean("Left", this.leftExitExists);
            nbt.putBoolean("Right", this.rightExitExists);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder structurePiecesHolder, Random random) {
            this.fillForwardOpening((Start)start, structurePiecesHolder, random, 1, 1);
            if (this.leftExitExists) {
                this.fillNWOpening((Start)start, structurePiecesHolder, random, 1, 2);
            }
            if (this.rightExitExists) {
                this.fillSEOpening((Start)start, structurePiecesHolder, random, 1, 2);
            }
        }

        public static Corridor create(StructurePiecesHolder structurePiecesHolder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox blockBox = BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, 7, orientation);
            if (!Corridor.isInBounds(blockBox) || structurePiecesHolder.getIntersecting(blockBox) != null) {
                return null;
            }
            return new Corridor(chainLength, random, blockBox, orientation);
        }

        @Override
        public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
            this.fillWithOutline(world, boundingBox, 0, 0, 0, 4, 4, 6, true, random, STONE_BRICK_RANDOMIZER);
            this.generateEntrance(world, random, boundingBox, this.entryDoor, 1, 1, 0);
            this.generateEntrance(world, random, boundingBox, Piece.EntranceType.OPENING, 1, 1, 6);
            BlockState blockState = (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.EAST);
            BlockState blockState2 = (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.WEST);
            this.addBlockWithRandomThreshold(world, boundingBox, random, 0.1f, 1, 2, 1, blockState);
            this.addBlockWithRandomThreshold(world, boundingBox, random, 0.1f, 3, 2, 1, blockState2);
            this.addBlockWithRandomThreshold(world, boundingBox, random, 0.1f, 1, 2, 5, blockState);
            this.addBlockWithRandomThreshold(world, boundingBox, random, 0.1f, 3, 2, 5, blockState2);
            if (this.leftExitExists) {
                this.fillWithOutline(world, boundingBox, 0, 1, 2, 0, 3, 4, AIR, AIR, false);
            }
            if (this.rightExitExists) {
                this.fillWithOutline(world, boundingBox, 4, 1, 2, 4, 3, 4, AIR, AIR, false);
            }
            return true;
        }
    }

    public static class PrisonHall
    extends Piece {
        protected static final int SIZE_X = 9;
        protected static final int SIZE_Y = 5;
        protected static final int SIZE_Z = 11;

        public PrisonHall(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.STRONGHOLD_PRISON_HALL, chainLength, boundingBox);
            this.setOrientation(orientation);
            this.entryDoor = this.getRandomEntrance(random);
        }

        public PrisonHall(ServerWorld world, NbtCompound nbt) {
            super(StructurePieceType.STRONGHOLD_PRISON_HALL, nbt);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder structurePiecesHolder, Random random) {
            this.fillForwardOpening((Start)start, structurePiecesHolder, random, 1, 1);
        }

        public static PrisonHall create(StructurePiecesHolder structurePiecesHolder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox blockBox = BlockBox.rotated(x, y, z, -1, -1, 0, 9, 5, 11, orientation);
            if (!PrisonHall.isInBounds(blockBox) || structurePiecesHolder.getIntersecting(blockBox) != null) {
                return null;
            }
            return new PrisonHall(chainLength, random, blockBox, orientation);
        }

        @Override
        public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
            this.fillWithOutline(world, boundingBox, 0, 0, 0, 8, 4, 10, true, random, STONE_BRICK_RANDOMIZER);
            this.generateEntrance(world, random, boundingBox, this.entryDoor, 1, 1, 0);
            this.fillWithOutline(world, boundingBox, 1, 1, 10, 3, 3, 10, AIR, AIR, false);
            this.fillWithOutline(world, boundingBox, 4, 1, 1, 4, 3, 1, false, random, STONE_BRICK_RANDOMIZER);
            this.fillWithOutline(world, boundingBox, 4, 1, 3, 4, 3, 3, false, random, STONE_BRICK_RANDOMIZER);
            this.fillWithOutline(world, boundingBox, 4, 1, 7, 4, 3, 7, false, random, STONE_BRICK_RANDOMIZER);
            this.fillWithOutline(world, boundingBox, 4, 1, 9, 4, 3, 9, false, random, STONE_BRICK_RANDOMIZER);
            for (int i = 1; i <= 3; ++i) {
                this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true), 4, i, 4, boundingBox);
                this.addBlock(world, (BlockState)((BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true)).with(PaneBlock.EAST, true), 4, i, 5, boundingBox);
                this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true), 4, i, 6, boundingBox);
                this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true)).with(PaneBlock.EAST, true), 5, i, 5, boundingBox);
                this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true)).with(PaneBlock.EAST, true), 6, i, 5, boundingBox);
                this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true)).with(PaneBlock.EAST, true), 7, i, 5, boundingBox);
            }
            this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true), 4, 3, 2, boundingBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true), 4, 3, 8, boundingBox);
            BlockState i = (BlockState)Blocks.IRON_DOOR.getDefaultState().with(DoorBlock.FACING, Direction.WEST);
            BlockState blockState = (BlockState)((BlockState)Blocks.IRON_DOOR.getDefaultState().with(DoorBlock.FACING, Direction.WEST)).with(DoorBlock.HALF, DoubleBlockHalf.UPPER);
            this.addBlock(world, i, 4, 1, 2, boundingBox);
            this.addBlock(world, blockState, 4, 2, 2, boundingBox);
            this.addBlock(world, i, 4, 1, 8, boundingBox);
            this.addBlock(world, blockState, 4, 2, 8, boundingBox);
            return true;
        }
    }

    public static class LeftTurn
    extends Turn {
        public LeftTurn(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.STRONGHOLD_LEFT_TURN, chainLength, boundingBox);
            this.setOrientation(orientation);
            this.entryDoor = this.getRandomEntrance(random);
        }

        public LeftTurn(ServerWorld world, NbtCompound nbt) {
            super(StructurePieceType.STRONGHOLD_LEFT_TURN, nbt);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder structurePiecesHolder, Random random) {
            Direction direction = this.getFacing();
            if (direction == Direction.NORTH || direction == Direction.EAST) {
                this.fillNWOpening((Start)start, structurePiecesHolder, random, 1, 1);
            } else {
                this.fillSEOpening((Start)start, structurePiecesHolder, random, 1, 1);
            }
        }

        public static LeftTurn create(StructurePiecesHolder structurePiecesHolder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox blockBox = BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, 5, orientation);
            if (!LeftTurn.isInBounds(blockBox) || structurePiecesHolder.getIntersecting(blockBox) != null) {
                return null;
            }
            return new LeftTurn(chainLength, random, blockBox, orientation);
        }

        @Override
        public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
            this.fillWithOutline(world, boundingBox, 0, 0, 0, 4, 4, 4, true, random, STONE_BRICK_RANDOMIZER);
            this.generateEntrance(world, random, boundingBox, this.entryDoor, 1, 1, 0);
            Direction direction = this.getFacing();
            if (direction == Direction.NORTH || direction == Direction.EAST) {
                this.fillWithOutline(world, boundingBox, 0, 1, 1, 0, 3, 3, AIR, AIR, false);
            } else {
                this.fillWithOutline(world, boundingBox, 4, 1, 1, 4, 3, 3, AIR, AIR, false);
            }
            return true;
        }
    }

    public static class RightTurn
    extends Turn {
        public RightTurn(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.STRONGHOLD_RIGHT_TURN, chainLength, boundingBox);
            this.setOrientation(orientation);
            this.entryDoor = this.getRandomEntrance(random);
        }

        public RightTurn(ServerWorld world, NbtCompound nbt) {
            super(StructurePieceType.STRONGHOLD_RIGHT_TURN, nbt);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder structurePiecesHolder, Random random) {
            Direction direction = this.getFacing();
            if (direction == Direction.NORTH || direction == Direction.EAST) {
                this.fillSEOpening((Start)start, structurePiecesHolder, random, 1, 1);
            } else {
                this.fillNWOpening((Start)start, structurePiecesHolder, random, 1, 1);
            }
        }

        public static RightTurn create(StructurePiecesHolder structurePiecesHolder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox blockBox = BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, 5, orientation);
            if (!RightTurn.isInBounds(blockBox) || structurePiecesHolder.getIntersecting(blockBox) != null) {
                return null;
            }
            return new RightTurn(chainLength, random, blockBox, orientation);
        }

        @Override
        public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
            this.fillWithOutline(world, boundingBox, 0, 0, 0, 4, 4, 4, true, random, STONE_BRICK_RANDOMIZER);
            this.generateEntrance(world, random, boundingBox, this.entryDoor, 1, 1, 0);
            Direction direction = this.getFacing();
            if (direction == Direction.NORTH || direction == Direction.EAST) {
                this.fillWithOutline(world, boundingBox, 4, 1, 1, 4, 3, 3, AIR, AIR, false);
            } else {
                this.fillWithOutline(world, boundingBox, 0, 1, 1, 0, 3, 3, AIR, AIR, false);
            }
            return true;
        }
    }

    public static class SquareRoom
    extends Piece {
        protected static final int SIZE_X = 11;
        protected static final int SIZE_Y = 7;
        protected static final int SIZE_Z = 11;
        protected final int roomType;

        public SquareRoom(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.STRONGHOLD_SQUARE_ROOM, chainLength, boundingBox);
            this.setOrientation(orientation);
            this.entryDoor = this.getRandomEntrance(random);
            this.roomType = random.nextInt(5);
        }

        public SquareRoom(ServerWorld world, NbtCompound nbt) {
            super(StructurePieceType.STRONGHOLD_SQUARE_ROOM, nbt);
            this.roomType = nbt.getInt("Type");
        }

        @Override
        protected void writeNbt(ServerWorld world, NbtCompound nbt) {
            super.writeNbt(world, nbt);
            nbt.putInt("Type", this.roomType);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder structurePiecesHolder, Random random) {
            this.fillForwardOpening((Start)start, structurePiecesHolder, random, 4, 1);
            this.fillNWOpening((Start)start, structurePiecesHolder, random, 1, 4);
            this.fillSEOpening((Start)start, structurePiecesHolder, random, 1, 4);
        }

        public static SquareRoom create(StructurePiecesHolder structurePiecesHolder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox blockBox = BlockBox.rotated(x, y, z, -4, -1, 0, 11, 7, 11, orientation);
            if (!SquareRoom.isInBounds(blockBox) || structurePiecesHolder.getIntersecting(blockBox) != null) {
                return null;
            }
            return new SquareRoom(chainLength, random, blockBox, orientation);
        }

        @Override
        public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
            this.fillWithOutline(world, boundingBox, 0, 0, 0, 10, 6, 10, true, random, STONE_BRICK_RANDOMIZER);
            this.generateEntrance(world, random, boundingBox, this.entryDoor, 4, 1, 0);
            this.fillWithOutline(world, boundingBox, 4, 1, 10, 6, 3, 10, AIR, AIR, false);
            this.fillWithOutline(world, boundingBox, 0, 1, 4, 0, 3, 6, AIR, AIR, false);
            this.fillWithOutline(world, boundingBox, 10, 1, 4, 10, 3, 6, AIR, AIR, false);
            switch (this.roomType) {
                default: {
                    break;
                }
                case 0: {
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 1, 5, boundingBox);
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 2, 5, boundingBox);
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 3, 5, boundingBox);
                    this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.WEST), 4, 3, 5, boundingBox);
                    this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.EAST), 6, 3, 5, boundingBox);
                    this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.SOUTH), 5, 3, 4, boundingBox);
                    this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.NORTH), 5, 3, 6, boundingBox);
                    this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 4, 1, 4, boundingBox);
                    this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 4, 1, 5, boundingBox);
                    this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 4, 1, 6, boundingBox);
                    this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 6, 1, 4, boundingBox);
                    this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 6, 1, 5, boundingBox);
                    this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 6, 1, 6, boundingBox);
                    this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 5, 1, 4, boundingBox);
                    this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 5, 1, 6, boundingBox);
                    break;
                }
                case 1: {
                    for (int i = 0; i < 5; ++i) {
                        this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 1, 3 + i, boundingBox);
                        this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 7, 1, 3 + i, boundingBox);
                        this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3 + i, 1, 3, boundingBox);
                        this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3 + i, 1, 7, boundingBox);
                    }
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 1, 5, boundingBox);
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 2, 5, boundingBox);
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 3, 5, boundingBox);
                    this.addBlock(world, Blocks.WATER.getDefaultState(), 5, 4, 5, boundingBox);
                    break;
                }
                case 2: {
                    int i;
                    for (i = 1; i <= 9; ++i) {
                        this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 1, 3, i, boundingBox);
                        this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 9, 3, i, boundingBox);
                    }
                    for (i = 1; i <= 9; ++i) {
                        this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), i, 3, 1, boundingBox);
                        this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), i, 3, 9, boundingBox);
                    }
                    this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 5, 1, 4, boundingBox);
                    this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 5, 1, 6, boundingBox);
                    this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 5, 3, 4, boundingBox);
                    this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 5, 3, 6, boundingBox);
                    this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 4, 1, 5, boundingBox);
                    this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 6, 1, 5, boundingBox);
                    this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 4, 3, 5, boundingBox);
                    this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 6, 3, 5, boundingBox);
                    for (i = 1; i <= 3; ++i) {
                        this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 4, i, 4, boundingBox);
                        this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 6, i, 4, boundingBox);
                        this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 4, i, 6, boundingBox);
                        this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 6, i, 6, boundingBox);
                    }
                    this.addBlock(world, Blocks.TORCH.getDefaultState(), 5, 3, 5, boundingBox);
                    for (i = 2; i <= 8; ++i) {
                        this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 2, 3, i, boundingBox);
                        this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 3, 3, i, boundingBox);
                        if (i <= 3 || i >= 7) {
                            this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 4, 3, i, boundingBox);
                            this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 5, 3, i, boundingBox);
                            this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 6, 3, i, boundingBox);
                        }
                        this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 7, 3, i, boundingBox);
                        this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 8, 3, i, boundingBox);
                    }
                    BlockState i2 = (BlockState)Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.WEST);
                    this.addBlock(world, i2, 9, 1, 3, boundingBox);
                    this.addBlock(world, i2, 9, 2, 3, boundingBox);
                    this.addBlock(world, i2, 9, 3, 3, boundingBox);
                    this.addChest(world, boundingBox, random, 3, 4, 8, LootTables.STRONGHOLD_CROSSING_CHEST);
                }
            }
            return true;
        }
    }

    public static class Stairs
    extends Piece {
        private static final int SIZE_X = 5;
        private static final int SIZE_Y = 11;
        private static final int SIZE_Z = 8;

        public Stairs(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.STRONGHOLD_STAIRS, chainLength, boundingBox);
            this.setOrientation(orientation);
            this.entryDoor = this.getRandomEntrance(random);
        }

        public Stairs(ServerWorld world, NbtCompound nbt) {
            super(StructurePieceType.STRONGHOLD_STAIRS, nbt);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder structurePiecesHolder, Random random) {
            this.fillForwardOpening((Start)start, structurePiecesHolder, random, 1, 1);
        }

        public static Stairs create(StructurePiecesHolder structurePiecesHolder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox blockBox = BlockBox.rotated(x, y, z, -1, -7, 0, 5, 11, 8, orientation);
            if (!Stairs.isInBounds(blockBox) || structurePiecesHolder.getIntersecting(blockBox) != null) {
                return null;
            }
            return new Stairs(chainLength, random, blockBox, orientation);
        }

        @Override
        public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
            this.fillWithOutline(world, boundingBox, 0, 0, 0, 4, 10, 7, true, random, STONE_BRICK_RANDOMIZER);
            this.generateEntrance(world, random, boundingBox, this.entryDoor, 1, 7, 0);
            this.generateEntrance(world, random, boundingBox, Piece.EntranceType.OPENING, 1, 1, 7);
            BlockState blockState = (BlockState)Blocks.COBBLESTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH);
            for (int i = 0; i < 6; ++i) {
                this.addBlock(world, blockState, 1, 6 - i, 1 + i, boundingBox);
                this.addBlock(world, blockState, 2, 6 - i, 1 + i, boundingBox);
                this.addBlock(world, blockState, 3, 6 - i, 1 + i, boundingBox);
                if (i >= 5) continue;
                this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 5 - i, 1 + i, boundingBox);
                this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 5 - i, 1 + i, boundingBox);
                this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 5 - i, 1 + i, boundingBox);
            }
            return true;
        }
    }

    public static class SpiralStaircase
    extends Piece {
        private static final int SIZE_X = 5;
        private static final int SIZE_Y = 11;
        private static final int SIZE_Z = 5;
        private final boolean isStructureStart;

        public SpiralStaircase(StructurePieceType structurePieceType, int chainLength, int x, int z, Direction orientation) {
            super(structurePieceType, chainLength, SpiralStaircase.createBox(x, 64, z, orientation, 5, 11, 5));
            this.isStructureStart = true;
            this.setOrientation(orientation);
            this.entryDoor = Piece.EntranceType.OPENING;
        }

        public SpiralStaircase(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.STRONGHOLD_SPIRAL_STAIRCASE, chainLength, boundingBox);
            this.isStructureStart = false;
            this.setOrientation(orientation);
            this.entryDoor = this.getRandomEntrance(random);
        }

        public SpiralStaircase(StructurePieceType structurePieceType, NbtCompound nbtCompound) {
            super(structurePieceType, nbtCompound);
            this.isStructureStart = nbtCompound.getBoolean("Source");
        }

        public SpiralStaircase(ServerWorld world, NbtCompound nbt) {
            this(StructurePieceType.STRONGHOLD_SPIRAL_STAIRCASE, nbt);
        }

        @Override
        protected void writeNbt(ServerWorld world, NbtCompound nbt) {
            super.writeNbt(world, nbt);
            nbt.putBoolean("Source", this.isStructureStart);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder structurePiecesHolder, Random random) {
            if (this.isStructureStart) {
                activePieceType = FiveWayCrossing.class;
            }
            this.fillForwardOpening((Start)start, structurePiecesHolder, random, 1, 1);
        }

        public static SpiralStaircase create(StructurePiecesHolder structurePiecesHolder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox blockBox = BlockBox.rotated(x, y, z, -1, -7, 0, 5, 11, 5, orientation);
            if (!SpiralStaircase.isInBounds(blockBox) || structurePiecesHolder.getIntersecting(blockBox) != null) {
                return null;
            }
            return new SpiralStaircase(chainLength, random, blockBox, orientation);
        }

        @Override
        public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
            this.fillWithOutline(world, boundingBox, 0, 0, 0, 4, 10, 4, true, random, STONE_BRICK_RANDOMIZER);
            this.generateEntrance(world, random, boundingBox, this.entryDoor, 1, 7, 0);
            this.generateEntrance(world, random, boundingBox, Piece.EntranceType.OPENING, 1, 1, 4);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 6, 1, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 5, 1, boundingBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 1, 6, 1, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 5, 2, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 4, 3, boundingBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 1, 5, 3, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 4, 3, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 3, 3, boundingBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 3, 4, 3, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 3, 2, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 2, 1, boundingBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 3, 3, 1, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 2, 1, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 1, 1, boundingBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 1, 2, 1, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 1, 2, boundingBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 1, 1, 3, boundingBox);
            return true;
        }
    }

    public static class FiveWayCrossing
    extends Piece {
        protected static final int SIZE_X = 10;
        protected static final int SIZE_Y = 9;
        protected static final int SIZE_Z = 11;
        private final boolean lowerLeftExists;
        private final boolean upperLeftExists;
        private final boolean lowerRightExists;
        private final boolean upperRightExists;

        public FiveWayCrossing(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.STRONGHOLD_FIVE_WAY_CROSSING, chainLength, boundingBox);
            this.setOrientation(orientation);
            this.entryDoor = this.getRandomEntrance(random);
            this.lowerLeftExists = random.nextBoolean();
            this.upperLeftExists = random.nextBoolean();
            this.lowerRightExists = random.nextBoolean();
            this.upperRightExists = random.nextInt(3) > 0;
        }

        public FiveWayCrossing(ServerWorld world, NbtCompound nbt) {
            super(StructurePieceType.STRONGHOLD_FIVE_WAY_CROSSING, nbt);
            this.lowerLeftExists = nbt.getBoolean("leftLow");
            this.upperLeftExists = nbt.getBoolean("leftHigh");
            this.lowerRightExists = nbt.getBoolean("rightLow");
            this.upperRightExists = nbt.getBoolean("rightHigh");
        }

        @Override
        protected void writeNbt(ServerWorld world, NbtCompound nbt) {
            super.writeNbt(world, nbt);
            nbt.putBoolean("leftLow", this.lowerLeftExists);
            nbt.putBoolean("leftHigh", this.upperLeftExists);
            nbt.putBoolean("rightLow", this.lowerRightExists);
            nbt.putBoolean("rightHigh", this.upperRightExists);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder structurePiecesHolder, Random random) {
            int i = 3;
            int j = 5;
            Direction direction = this.getFacing();
            if (direction == Direction.WEST || direction == Direction.NORTH) {
                i = 8 - i;
                j = 8 - j;
            }
            this.fillForwardOpening((Start)start, structurePiecesHolder, random, 5, 1);
            if (this.lowerLeftExists) {
                this.fillNWOpening((Start)start, structurePiecesHolder, random, i, 1);
            }
            if (this.upperLeftExists) {
                this.fillNWOpening((Start)start, structurePiecesHolder, random, j, 7);
            }
            if (this.lowerRightExists) {
                this.fillSEOpening((Start)start, structurePiecesHolder, random, i, 1);
            }
            if (this.upperRightExists) {
                this.fillSEOpening((Start)start, structurePiecesHolder, random, j, 7);
            }
        }

        public static FiveWayCrossing create(StructurePiecesHolder structurePiecesHolder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox blockBox = BlockBox.rotated(x, y, z, -4, -3, 0, 10, 9, 11, orientation);
            if (!FiveWayCrossing.isInBounds(blockBox) || structurePiecesHolder.getIntersecting(blockBox) != null) {
                return null;
            }
            return new FiveWayCrossing(chainLength, random, blockBox, orientation);
        }

        @Override
        public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
            this.fillWithOutline(world, boundingBox, 0, 0, 0, 9, 8, 10, true, random, STONE_BRICK_RANDOMIZER);
            this.generateEntrance(world, random, boundingBox, this.entryDoor, 4, 3, 0);
            if (this.lowerLeftExists) {
                this.fillWithOutline(world, boundingBox, 0, 3, 1, 0, 5, 3, AIR, AIR, false);
            }
            if (this.lowerRightExists) {
                this.fillWithOutline(world, boundingBox, 9, 3, 1, 9, 5, 3, AIR, AIR, false);
            }
            if (this.upperLeftExists) {
                this.fillWithOutline(world, boundingBox, 0, 5, 7, 0, 7, 9, AIR, AIR, false);
            }
            if (this.upperRightExists) {
                this.fillWithOutline(world, boundingBox, 9, 5, 7, 9, 7, 9, AIR, AIR, false);
            }
            this.fillWithOutline(world, boundingBox, 5, 1, 10, 7, 3, 10, AIR, AIR, false);
            this.fillWithOutline(world, boundingBox, 1, 2, 1, 8, 2, 6, false, random, STONE_BRICK_RANDOMIZER);
            this.fillWithOutline(world, boundingBox, 4, 1, 5, 4, 4, 9, false, random, STONE_BRICK_RANDOMIZER);
            this.fillWithOutline(world, boundingBox, 8, 1, 5, 8, 4, 9, false, random, STONE_BRICK_RANDOMIZER);
            this.fillWithOutline(world, boundingBox, 1, 4, 7, 3, 4, 9, false, random, STONE_BRICK_RANDOMIZER);
            this.fillWithOutline(world, boundingBox, 1, 3, 5, 3, 3, 6, false, random, STONE_BRICK_RANDOMIZER);
            this.fillWithOutline(world, boundingBox, 1, 3, 4, 3, 3, 4, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
            this.fillWithOutline(world, boundingBox, 1, 4, 6, 3, 4, 6, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
            this.fillWithOutline(world, boundingBox, 5, 1, 7, 7, 1, 8, false, random, STONE_BRICK_RANDOMIZER);
            this.fillWithOutline(world, boundingBox, 5, 1, 9, 7, 1, 9, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
            this.fillWithOutline(world, boundingBox, 5, 2, 7, 7, 2, 7, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
            this.fillWithOutline(world, boundingBox, 4, 5, 7, 4, 5, 9, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
            this.fillWithOutline(world, boundingBox, 8, 5, 7, 8, 5, 9, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
            this.fillWithOutline(world, boundingBox, 5, 5, 7, 7, 5, 9, (BlockState)Blocks.SMOOTH_STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE), (BlockState)Blocks.SMOOTH_STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE), false);
            this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.SOUTH), 6, 5, 6, boundingBox);
            return true;
        }
    }

    public static class ChestCorridor
    extends Piece {
        private static final int SIZE_X = 5;
        private static final int SIZE_Y = 5;
        private static final int SIZE_Z = 7;
        private boolean chestGenerated;

        public ChestCorridor(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.STRONGHOLD_CHEST_CORRIDOR, chainLength, boundingBox);
            this.setOrientation(orientation);
            this.entryDoor = this.getRandomEntrance(random);
        }

        public ChestCorridor(ServerWorld world, NbtCompound nbt) {
            super(StructurePieceType.STRONGHOLD_CHEST_CORRIDOR, nbt);
            this.chestGenerated = nbt.getBoolean("Chest");
        }

        @Override
        protected void writeNbt(ServerWorld world, NbtCompound nbt) {
            super.writeNbt(world, nbt);
            nbt.putBoolean("Chest", this.chestGenerated);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder structurePiecesHolder, Random random) {
            this.fillForwardOpening((Start)start, structurePiecesHolder, random, 1, 1);
        }

        public static ChestCorridor create(StructurePiecesHolder structurePiecesHolder, Random random, int x, int y, int z, Direction orientation, int chainlength) {
            BlockBox blockBox = BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, 7, orientation);
            if (!ChestCorridor.isInBounds(blockBox) || structurePiecesHolder.getIntersecting(blockBox) != null) {
                return null;
            }
            return new ChestCorridor(chainlength, random, blockBox, orientation);
        }

        @Override
        public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
            this.fillWithOutline(world, boundingBox, 0, 0, 0, 4, 4, 6, true, random, STONE_BRICK_RANDOMIZER);
            this.generateEntrance(world, random, boundingBox, this.entryDoor, 1, 1, 0);
            this.generateEntrance(world, random, boundingBox, Piece.EntranceType.OPENING, 1, 1, 6);
            this.fillWithOutline(world, boundingBox, 3, 1, 2, 3, 1, 4, Blocks.STONE_BRICKS.getDefaultState(), Blocks.STONE_BRICKS.getDefaultState(), false);
            this.addBlock(world, Blocks.STONE_BRICK_SLAB.getDefaultState(), 3, 1, 1, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICK_SLAB.getDefaultState(), 3, 1, 5, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICK_SLAB.getDefaultState(), 3, 2, 2, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICK_SLAB.getDefaultState(), 3, 2, 4, boundingBox);
            for (int i = 2; i <= 4; ++i) {
                this.addBlock(world, Blocks.STONE_BRICK_SLAB.getDefaultState(), 2, 1, i, boundingBox);
            }
            if (!this.chestGenerated && boundingBox.contains(this.offsetPos(3, 2, 3))) {
                this.chestGenerated = true;
                this.addChest(world, boundingBox, random, 3, 2, 3, LootTables.STRONGHOLD_CORRIDOR_CHEST);
            }
            return true;
        }
    }

    public static class Library
    extends Piece {
        protected static final int SIZE_X = 14;
        protected static final int field_31636 = 6;
        protected static final int SIZE_Y = 11;
        protected static final int SIZE_Z = 15;
        private final boolean tall;

        public Library(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.STRONGHOLD_LIBRARY, chainLength, boundingBox);
            this.setOrientation(orientation);
            this.entryDoor = this.getRandomEntrance(random);
            this.tall = boundingBox.getBlockCountY() > 6;
        }

        public Library(ServerWorld world, NbtCompound nbt) {
            super(StructurePieceType.STRONGHOLD_LIBRARY, nbt);
            this.tall = nbt.getBoolean("Tall");
        }

        @Override
        protected void writeNbt(ServerWorld world, NbtCompound nbt) {
            super.writeNbt(world, nbt);
            nbt.putBoolean("Tall", this.tall);
        }

        public static Library create(StructurePiecesHolder structurePiecesHolder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox blockBox = BlockBox.rotated(x, y, z, -4, -1, 0, 14, 11, 15, orientation);
            if (!(Library.isInBounds(blockBox) && structurePiecesHolder.getIntersecting(blockBox) == null || Library.isInBounds(blockBox = BlockBox.rotated(x, y, z, -4, -1, 0, 14, 6, 15, orientation)) && structurePiecesHolder.getIntersecting(blockBox) == null)) {
                return null;
            }
            return new Library(chainLength, random, blockBox, orientation);
        }

        @Override
        public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
            int l;
            int i = 11;
            if (!this.tall) {
                i = 6;
            }
            this.fillWithOutline(world, boundingBox, 0, 0, 0, 13, i - 1, 14, true, random, STONE_BRICK_RANDOMIZER);
            this.generateEntrance(world, random, boundingBox, this.entryDoor, 4, 1, 0);
            this.fillWithOutlineUnderSeaLevel(world, boundingBox, random, 0.07f, 2, 1, 1, 11, 4, 13, Blocks.COBWEB.getDefaultState(), Blocks.COBWEB.getDefaultState(), false, false);
            boolean j = true;
            int k = 12;
            for (l = 1; l <= 13; ++l) {
                if ((l - 1) % 4 == 0) {
                    this.fillWithOutline(world, boundingBox, 1, 1, l, 1, 4, l, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
                    this.fillWithOutline(world, boundingBox, 12, 1, l, 12, 4, l, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
                    this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.EAST), 2, 3, l, boundingBox);
                    this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.WEST), 11, 3, l, boundingBox);
                    if (!this.tall) continue;
                    this.fillWithOutline(world, boundingBox, 1, 6, l, 1, 9, l, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
                    this.fillWithOutline(world, boundingBox, 12, 6, l, 12, 9, l, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
                    continue;
                }
                this.fillWithOutline(world, boundingBox, 1, 1, l, 1, 4, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
                this.fillWithOutline(world, boundingBox, 12, 1, l, 12, 4, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
                if (!this.tall) continue;
                this.fillWithOutline(world, boundingBox, 1, 6, l, 1, 9, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
                this.fillWithOutline(world, boundingBox, 12, 6, l, 12, 9, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
            }
            for (l = 3; l < 12; l += 2) {
                this.fillWithOutline(world, boundingBox, 3, 1, l, 4, 3, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
                this.fillWithOutline(world, boundingBox, 6, 1, l, 7, 3, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
                this.fillWithOutline(world, boundingBox, 9, 1, l, 10, 3, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
            }
            if (this.tall) {
                this.fillWithOutline(world, boundingBox, 1, 5, 1, 3, 5, 13, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
                this.fillWithOutline(world, boundingBox, 10, 5, 1, 12, 5, 13, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
                this.fillWithOutline(world, boundingBox, 4, 5, 1, 9, 5, 2, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
                this.fillWithOutline(world, boundingBox, 4, 5, 12, 9, 5, 13, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
                this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 9, 5, 11, boundingBox);
                this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 8, 5, 11, boundingBox);
                this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 9, 5, 10, boundingBox);
                BlockState l2 = (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
                BlockState blockState = (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
                this.fillWithOutline(world, boundingBox, 3, 6, 3, 3, 6, 11, blockState, blockState, false);
                this.fillWithOutline(world, boundingBox, 10, 6, 3, 10, 6, 9, blockState, blockState, false);
                this.fillWithOutline(world, boundingBox, 4, 6, 2, 9, 6, 2, l2, l2, false);
                this.fillWithOutline(world, boundingBox, 4, 6, 12, 7, 6, 12, l2, l2, false);
                this.addBlock(world, (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.EAST, true), 3, 6, 2, boundingBox);
                this.addBlock(world, (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.SOUTH, true)).with(FenceBlock.EAST, true), 3, 6, 12, boundingBox);
                this.addBlock(world, (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.WEST, true), 10, 6, 2, boundingBox);
                for (int m = 0; m <= 2; ++m) {
                    this.addBlock(world, (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.SOUTH, true)).with(FenceBlock.WEST, true), 8 + m, 6, 12 - m, boundingBox);
                    if (m == 2) continue;
                    this.addBlock(world, (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.EAST, true), 8 + m, 6, 11 - m, boundingBox);
                }
                BlockState m = (BlockState)Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.SOUTH);
                this.addBlock(world, m, 10, 1, 13, boundingBox);
                this.addBlock(world, m, 10, 2, 13, boundingBox);
                this.addBlock(world, m, 10, 3, 13, boundingBox);
                this.addBlock(world, m, 10, 4, 13, boundingBox);
                this.addBlock(world, m, 10, 5, 13, boundingBox);
                this.addBlock(world, m, 10, 6, 13, boundingBox);
                this.addBlock(world, m, 10, 7, 13, boundingBox);
                int n = 7;
                int o = 7;
                BlockState blockState2 = (BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.EAST, true);
                this.addBlock(world, blockState2, 6, 9, 7, boundingBox);
                BlockState blockState3 = (BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.WEST, true);
                this.addBlock(world, blockState3, 7, 9, 7, boundingBox);
                this.addBlock(world, blockState2, 6, 8, 7, boundingBox);
                this.addBlock(world, blockState3, 7, 8, 7, boundingBox);
                BlockState blockState4 = (BlockState)((BlockState)blockState.with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
                this.addBlock(world, blockState4, 6, 7, 7, boundingBox);
                this.addBlock(world, blockState4, 7, 7, 7, boundingBox);
                this.addBlock(world, blockState2, 5, 7, 7, boundingBox);
                this.addBlock(world, blockState3, 8, 7, 7, boundingBox);
                this.addBlock(world, (BlockState)blockState2.with(FenceBlock.NORTH, true), 6, 7, 6, boundingBox);
                this.addBlock(world, (BlockState)blockState2.with(FenceBlock.SOUTH, true), 6, 7, 8, boundingBox);
                this.addBlock(world, (BlockState)blockState3.with(FenceBlock.NORTH, true), 7, 7, 6, boundingBox);
                this.addBlock(world, (BlockState)blockState3.with(FenceBlock.SOUTH, true), 7, 7, 8, boundingBox);
                BlockState blockState5 = Blocks.TORCH.getDefaultState();
                this.addBlock(world, blockState5, 5, 8, 7, boundingBox);
                this.addBlock(world, blockState5, 8, 8, 7, boundingBox);
                this.addBlock(world, blockState5, 6, 8, 6, boundingBox);
                this.addBlock(world, blockState5, 6, 8, 8, boundingBox);
                this.addBlock(world, blockState5, 7, 8, 6, boundingBox);
                this.addBlock(world, blockState5, 7, 8, 8, boundingBox);
            }
            this.addChest(world, boundingBox, random, 3, 3, 5, LootTables.STRONGHOLD_LIBRARY_CHEST);
            if (this.tall) {
                this.addBlock(world, AIR, 12, 9, 1, boundingBox);
                this.addChest(world, boundingBox, random, 12, 8, 1, LootTables.STRONGHOLD_LIBRARY_CHEST);
            }
            return true;
        }
    }

    public static class PortalRoom
    extends Piece {
        protected static final int SIZE_X = 11;
        protected static final int SIZE_Y = 8;
        protected static final int SIZE_Z = 16;
        private boolean spawnerPlaced;

        public PortalRoom(int chainLength, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.STRONGHOLD_PORTAL_ROOM, chainLength, boundingBox);
            this.setOrientation(orientation);
        }

        public PortalRoom(ServerWorld world, NbtCompound nbt) {
            super(StructurePieceType.STRONGHOLD_PORTAL_ROOM, nbt);
            this.spawnerPlaced = nbt.getBoolean("Mob");
        }

        @Override
        protected void writeNbt(ServerWorld world, NbtCompound nbt) {
            super.writeNbt(world, nbt);
            nbt.putBoolean("Mob", this.spawnerPlaced);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder structurePiecesHolder, Random random) {
            if (start != null) {
                ((Start)start).portalRoom = this;
            }
        }

        public static PortalRoom create(StructurePiecesHolder structurePiecesHolder, int x, int y, int z, Direction orientation, int chainLength) {
            BlockBox blockBox = BlockBox.rotated(x, y, z, -4, -1, 0, 11, 8, 16, orientation);
            if (!PortalRoom.isInBounds(blockBox) || structurePiecesHolder.getIntersecting(blockBox) != null) {
                return null;
            }
            return new PortalRoom(chainLength, blockBox, orientation);
        }

        @Override
        public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
            BlockPos.Mutable l;
            int j;
            this.fillWithOutline(world, boundingBox, 0, 0, 0, 10, 7, 15, false, random, STONE_BRICK_RANDOMIZER);
            this.generateEntrance(world, random, boundingBox, Piece.EntranceType.GRATES, 4, 1, 0);
            int i = 6;
            this.fillWithOutline(world, boundingBox, 1, i, 1, 1, i, 14, false, random, STONE_BRICK_RANDOMIZER);
            this.fillWithOutline(world, boundingBox, 9, i, 1, 9, i, 14, false, random, STONE_BRICK_RANDOMIZER);
            this.fillWithOutline(world, boundingBox, 2, i, 1, 8, i, 2, false, random, STONE_BRICK_RANDOMIZER);
            this.fillWithOutline(world, boundingBox, 2, i, 14, 8, i, 14, false, random, STONE_BRICK_RANDOMIZER);
            this.fillWithOutline(world, boundingBox, 1, 1, 1, 2, 1, 4, false, random, STONE_BRICK_RANDOMIZER);
            this.fillWithOutline(world, boundingBox, 8, 1, 1, 9, 1, 4, false, random, STONE_BRICK_RANDOMIZER);
            this.fillWithOutline(world, boundingBox, 1, 1, 1, 1, 1, 3, Blocks.LAVA.getDefaultState(), Blocks.LAVA.getDefaultState(), false);
            this.fillWithOutline(world, boundingBox, 9, 1, 1, 9, 1, 3, Blocks.LAVA.getDefaultState(), Blocks.LAVA.getDefaultState(), false);
            this.fillWithOutline(world, boundingBox, 3, 1, 8, 7, 1, 12, false, random, STONE_BRICK_RANDOMIZER);
            this.fillWithOutline(world, boundingBox, 4, 1, 9, 6, 1, 11, Blocks.LAVA.getDefaultState(), Blocks.LAVA.getDefaultState(), false);
            BlockState blockState = (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true);
            BlockState blockState2 = (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true)).with(PaneBlock.EAST, true);
            for (j = 3; j < 14; j += 2) {
                this.fillWithOutline(world, boundingBox, 0, 3, j, 0, 4, j, blockState, blockState, false);
                this.fillWithOutline(world, boundingBox, 10, 3, j, 10, 4, j, blockState, blockState, false);
            }
            for (j = 2; j < 9; j += 2) {
                this.fillWithOutline(world, boundingBox, j, 3, 15, j, 4, 15, blockState2, blockState2, false);
            }
            BlockState j2 = (BlockState)Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH);
            this.fillWithOutline(world, boundingBox, 4, 1, 5, 6, 1, 7, false, random, STONE_BRICK_RANDOMIZER);
            this.fillWithOutline(world, boundingBox, 4, 2, 6, 6, 2, 7, false, random, STONE_BRICK_RANDOMIZER);
            this.fillWithOutline(world, boundingBox, 4, 3, 7, 6, 3, 7, false, random, STONE_BRICK_RANDOMIZER);
            for (int k = 4; k <= 6; ++k) {
                this.addBlock(world, j2, k, 1, 4, boundingBox);
                this.addBlock(world, j2, k, 2, 5, boundingBox);
                this.addBlock(world, j2, k, 3, 6, boundingBox);
            }
            BlockState k = (BlockState)Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.NORTH);
            BlockState blockState3 = (BlockState)Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.SOUTH);
            BlockState blockState4 = (BlockState)Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.EAST);
            BlockState blockState5 = (BlockState)Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.WEST);
            boolean bl = true;
            boolean[] bls = new boolean[12];
            for (int l2 = 0; l2 < bls.length; ++l2) {
                bls[l2] = random.nextFloat() > 0.9f;
                bl &= bls[l2];
            }
            this.addBlock(world, (BlockState)k.with(EndPortalFrameBlock.EYE, bls[0]), 4, 3, 8, boundingBox);
            this.addBlock(world, (BlockState)k.with(EndPortalFrameBlock.EYE, bls[1]), 5, 3, 8, boundingBox);
            this.addBlock(world, (BlockState)k.with(EndPortalFrameBlock.EYE, bls[2]), 6, 3, 8, boundingBox);
            this.addBlock(world, (BlockState)blockState3.with(EndPortalFrameBlock.EYE, bls[3]), 4, 3, 12, boundingBox);
            this.addBlock(world, (BlockState)blockState3.with(EndPortalFrameBlock.EYE, bls[4]), 5, 3, 12, boundingBox);
            this.addBlock(world, (BlockState)blockState3.with(EndPortalFrameBlock.EYE, bls[5]), 6, 3, 12, boundingBox);
            this.addBlock(world, (BlockState)blockState4.with(EndPortalFrameBlock.EYE, bls[6]), 3, 3, 9, boundingBox);
            this.addBlock(world, (BlockState)blockState4.with(EndPortalFrameBlock.EYE, bls[7]), 3, 3, 10, boundingBox);
            this.addBlock(world, (BlockState)blockState4.with(EndPortalFrameBlock.EYE, bls[8]), 3, 3, 11, boundingBox);
            this.addBlock(world, (BlockState)blockState5.with(EndPortalFrameBlock.EYE, bls[9]), 7, 3, 9, boundingBox);
            this.addBlock(world, (BlockState)blockState5.with(EndPortalFrameBlock.EYE, bls[10]), 7, 3, 10, boundingBox);
            this.addBlock(world, (BlockState)blockState5.with(EndPortalFrameBlock.EYE, bls[11]), 7, 3, 11, boundingBox);
            if (bl) {
                BlockState l3 = Blocks.END_PORTAL.getDefaultState();
                this.addBlock(world, l3, 4, 3, 9, boundingBox);
                this.addBlock(world, l3, 5, 3, 9, boundingBox);
                this.addBlock(world, l3, 6, 3, 9, boundingBox);
                this.addBlock(world, l3, 4, 3, 10, boundingBox);
                this.addBlock(world, l3, 5, 3, 10, boundingBox);
                this.addBlock(world, l3, 6, 3, 10, boundingBox);
                this.addBlock(world, l3, 4, 3, 11, boundingBox);
                this.addBlock(world, l3, 5, 3, 11, boundingBox);
                this.addBlock(world, l3, 6, 3, 11, boundingBox);
            }
            if (!this.spawnerPlaced && boundingBox.contains(l = this.offsetPos(5, 3, 6))) {
                this.spawnerPlaced = true;
                world.setBlockState(l, Blocks.SPAWNER.getDefaultState(), Block.NOTIFY_LISTENERS);
                BlockEntity blockEntity = world.getBlockEntity(l);
                if (blockEntity instanceof MobSpawnerBlockEntity) {
                    ((MobSpawnerBlockEntity)blockEntity).getLogic().setEntityId(EntityType.SILVERFISH);
                }
            }
            return true;
        }
    }

    static abstract class Piece
    extends StructurePiece {
        protected EntranceType entryDoor = EntranceType.OPENING;

        protected Piece(StructurePieceType structurePieceType, int i, BlockBox blockBox) {
            super(structurePieceType, i, blockBox);
        }

        public Piece(StructurePieceType structurePieceType, NbtCompound nbtCompound) {
            super(structurePieceType, nbtCompound);
            this.entryDoor = EntranceType.valueOf(nbtCompound.getString("EntryDoor"));
        }

        @Override
        public StructureWeightType method_33882() {
            return StructureWeightType.BURY;
        }

        @Override
        protected void writeNbt(ServerWorld world, NbtCompound nbt) {
            nbt.putString("EntryDoor", this.entryDoor.name());
        }

        protected void generateEntrance(StructureWorldAccess world, Random random, BlockBox boundingBox, EntranceType type, int x, int y, int z) {
            switch (type) {
                case OPENING: {
                    this.fillWithOutline(world, boundingBox, x, y, z, x + 3 - 1, y + 3 - 1, z, AIR, AIR, false);
                    break;
                }
                case WOOD_DOOR: {
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x, y, z, boundingBox);
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x, y + 1, z, boundingBox);
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x, y + 2, z, boundingBox);
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 1, y + 2, z, boundingBox);
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y + 2, z, boundingBox);
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y + 1, z, boundingBox);
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y, z, boundingBox);
                    this.addBlock(world, Blocks.OAK_DOOR.getDefaultState(), x + 1, y, z, boundingBox);
                    this.addBlock(world, (BlockState)Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER), x + 1, y + 1, z, boundingBox);
                    break;
                }
                case GRATES: {
                    this.addBlock(world, Blocks.CAVE_AIR.getDefaultState(), x + 1, y, z, boundingBox);
                    this.addBlock(world, Blocks.CAVE_AIR.getDefaultState(), x + 1, y + 1, z, boundingBox);
                    this.addBlock(world, (BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true), x, y, z, boundingBox);
                    this.addBlock(world, (BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true), x, y + 1, z, boundingBox);
                    this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.EAST, true)).with(PaneBlock.WEST, true), x, y + 2, z, boundingBox);
                    this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.EAST, true)).with(PaneBlock.WEST, true), x + 1, y + 2, z, boundingBox);
                    this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.EAST, true)).with(PaneBlock.WEST, true), x + 2, y + 2, z, boundingBox);
                    this.addBlock(world, (BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.EAST, true), x + 2, y + 1, z, boundingBox);
                    this.addBlock(world, (BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.EAST, true), x + 2, y, z, boundingBox);
                    break;
                }
                case IRON_DOOR: {
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x, y, z, boundingBox);
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x, y + 1, z, boundingBox);
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x, y + 2, z, boundingBox);
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 1, y + 2, z, boundingBox);
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y + 2, z, boundingBox);
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y + 1, z, boundingBox);
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y, z, boundingBox);
                    this.addBlock(world, Blocks.IRON_DOOR.getDefaultState(), x + 1, y, z, boundingBox);
                    this.addBlock(world, (BlockState)Blocks.IRON_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER), x + 1, y + 1, z, boundingBox);
                    this.addBlock(world, (BlockState)Blocks.STONE_BUTTON.getDefaultState().with(AbstractButtonBlock.FACING, Direction.NORTH), x + 2, y + 1, z + 1, boundingBox);
                    this.addBlock(world, (BlockState)Blocks.STONE_BUTTON.getDefaultState().with(AbstractButtonBlock.FACING, Direction.SOUTH), x + 2, y + 1, z - 1, boundingBox);
                }
            }
        }

        protected EntranceType getRandomEntrance(Random random) {
            int i = random.nextInt(5);
            switch (i) {
                default: {
                    return EntranceType.OPENING;
                }
                case 2: {
                    return EntranceType.WOOD_DOOR;
                }
                case 3: {
                    return EntranceType.GRATES;
                }
                case 4: 
            }
            return EntranceType.IRON_DOOR;
        }

        @Nullable
        protected StructurePiece fillForwardOpening(Start start, StructurePiecesHolder structurePiecesHolder, Random random, int leftRightOffset, int heightOffset) {
            Direction direction = this.getFacing();
            if (direction != null) {
                switch (direction) {
                    case NORTH: {
                        return StrongholdGenerator.pieceGenerator(start, structurePiecesHolder, random, this.boundingBox.getMinX() + leftRightOffset, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() - 1, direction, this.getChainLength());
                    }
                    case SOUTH: {
                        return StrongholdGenerator.pieceGenerator(start, structurePiecesHolder, random, this.boundingBox.getMinX() + leftRightOffset, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMaxZ() + 1, direction, this.getChainLength());
                    }
                    case WEST: {
                        return StrongholdGenerator.pieceGenerator(start, structurePiecesHolder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() + leftRightOffset, direction, this.getChainLength());
                    }
                    case EAST: {
                        return StrongholdGenerator.pieceGenerator(start, structurePiecesHolder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() + leftRightOffset, direction, this.getChainLength());
                    }
                }
            }
            return null;
        }

        @Nullable
        protected StructurePiece fillNWOpening(Start start, StructurePiecesHolder structurePiecesHolder, Random random, int heightOffset, int leftRightOffset) {
            Direction direction = this.getFacing();
            if (direction != null) {
                switch (direction) {
                    case NORTH: {
                        return StrongholdGenerator.pieceGenerator(start, structurePiecesHolder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() + leftRightOffset, Direction.WEST, this.getChainLength());
                    }
                    case SOUTH: {
                        return StrongholdGenerator.pieceGenerator(start, structurePiecesHolder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() + leftRightOffset, Direction.WEST, this.getChainLength());
                    }
                    case WEST: {
                        return StrongholdGenerator.pieceGenerator(start, structurePiecesHolder, random, this.boundingBox.getMinX() + leftRightOffset, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() - 1, Direction.NORTH, this.getChainLength());
                    }
                    case EAST: {
                        return StrongholdGenerator.pieceGenerator(start, structurePiecesHolder, random, this.boundingBox.getMinX() + leftRightOffset, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() - 1, Direction.NORTH, this.getChainLength());
                    }
                }
            }
            return null;
        }

        @Nullable
        protected StructurePiece fillSEOpening(Start start, StructurePiecesHolder structurePiecesHolder, Random random, int heightOffset, int leftRightOffset) {
            Direction direction = this.getFacing();
            if (direction != null) {
                switch (direction) {
                    case NORTH: {
                        return StrongholdGenerator.pieceGenerator(start, structurePiecesHolder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() + leftRightOffset, Direction.EAST, this.getChainLength());
                    }
                    case SOUTH: {
                        return StrongholdGenerator.pieceGenerator(start, structurePiecesHolder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() + leftRightOffset, Direction.EAST, this.getChainLength());
                    }
                    case WEST: {
                        return StrongholdGenerator.pieceGenerator(start, structurePiecesHolder, random, this.boundingBox.getMinX() + leftRightOffset, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMaxZ() + 1, Direction.SOUTH, this.getChainLength());
                    }
                    case EAST: {
                        return StrongholdGenerator.pieceGenerator(start, structurePiecesHolder, random, this.boundingBox.getMinX() + leftRightOffset, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMaxZ() + 1, Direction.SOUTH, this.getChainLength());
                    }
                }
            }
            return null;
        }

        protected static boolean isInBounds(BlockBox boundingBox) {
            return boundingBox != null && boundingBox.getMinY() > 10;
        }

        protected static enum Piece.EntranceType {
            OPENING,
            WOOD_DOOR,
            GRATES,
            IRON_DOOR;

        }
    }

    public static class Start
    extends SpiralStaircase {
        public PieceData lastPiece;
        @Nullable
        public PortalRoom portalRoom;
        public final List<StructurePiece> pieces = Lists.newArrayList();

        public Start(Random random, int i, int j) {
            super(StructurePieceType.STRONGHOLD_START, 0, i, j, Start.getRandomHorizontalDirection(random));
        }

        public Start(ServerWorld serverWorld, NbtCompound nbtCompound) {
            super(StructurePieceType.STRONGHOLD_START, nbtCompound);
        }

        @Override
        public BlockPos getCenter() {
            if (this.portalRoom != null) {
                return this.portalRoom.getCenter();
            }
            return super.getCenter();
        }
    }

    public static class SmallCorridor
    extends Piece {
        private final int length;

        public SmallCorridor(int chainLength, BlockBox boundingBox, Direction orientation) {
            super(StructurePieceType.STRONGHOLD_SMALL_CORRIDOR, chainLength, boundingBox);
            this.setOrientation(orientation);
            this.length = orientation == Direction.NORTH || orientation == Direction.SOUTH ? boundingBox.getBlockCountZ() : boundingBox.getBlockCountX();
        }

        public SmallCorridor(ServerWorld serverWorld, NbtCompound nbt) {
            super(StructurePieceType.STRONGHOLD_SMALL_CORRIDOR, nbt);
            this.length = nbt.getInt("Steps");
        }

        @Override
        protected void writeNbt(ServerWorld world, NbtCompound nbt) {
            super.writeNbt(world, nbt);
            nbt.putInt("Steps", this.length);
        }

        public static BlockBox create(StructurePiecesHolder structurePiecesHolder, Random random, int x, int y, int z, Direction orientation) {
            int i = 3;
            BlockBox blockBox = BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, 4, orientation);
            StructurePiece structurePiece = structurePiecesHolder.getIntersecting(blockBox);
            if (structurePiece == null) {
                return null;
            }
            if (structurePiece.getBoundingBox().getMinY() == blockBox.getMinY()) {
                for (int j = 2; j >= 1; --j) {
                    blockBox = BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, j, orientation);
                    if (structurePiece.getBoundingBox().intersects(blockBox)) continue;
                    return BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, j + 1, orientation);
                }
            }
            return null;
        }

        @Override
        public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
            for (int i = 0; i < this.length; ++i) {
                this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 0, 0, i, boundingBox);
                this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 0, i, boundingBox);
                this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 0, i, boundingBox);
                this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 0, i, boundingBox);
                this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 4, 0, i, boundingBox);
                for (int j = 1; j <= 3; ++j) {
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 0, j, i, boundingBox);
                    this.addBlock(world, Blocks.CAVE_AIR.getDefaultState(), 1, j, i, boundingBox);
                    this.addBlock(world, Blocks.CAVE_AIR.getDefaultState(), 2, j, i, boundingBox);
                    this.addBlock(world, Blocks.CAVE_AIR.getDefaultState(), 3, j, i, boundingBox);
                    this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 4, j, i, boundingBox);
                }
                this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 0, 4, i, boundingBox);
                this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 4, i, boundingBox);
                this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 4, i, boundingBox);
                this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 4, i, boundingBox);
                this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 4, 4, i, boundingBox);
            }
            return true;
        }
    }

    static class StoneBrickRandomizer
    extends StructurePiece.BlockRandomizer {
        StoneBrickRandomizer() {
        }

        @Override
        public void setBlock(Random random, int x, int y, int z, boolean placeBlock) {
            float f;
            this.block = placeBlock ? ((f = random.nextFloat()) < 0.2f ? Blocks.CRACKED_STONE_BRICKS.getDefaultState() : (f < 0.5f ? Blocks.MOSSY_STONE_BRICKS.getDefaultState() : (f < 0.55f ? Blocks.INFESTED_STONE_BRICKS.getDefaultState() : Blocks.STONE_BRICKS.getDefaultState()))) : Blocks.CAVE_AIR.getDefaultState();
        }
    }

    public static abstract class Turn
    extends Piece {
        protected static final int SIZE_X = 5;
        protected static final int SIZE_Y = 5;
        protected static final int SIZE_Z = 5;

        protected Turn(StructurePieceType structurePieceType, int i, BlockBox blockBox) {
            super(structurePieceType, i, blockBox);
        }

        public Turn(StructurePieceType structurePieceType, NbtCompound nbtCompound) {
            super(structurePieceType, nbtCompound);
        }
    }
}

