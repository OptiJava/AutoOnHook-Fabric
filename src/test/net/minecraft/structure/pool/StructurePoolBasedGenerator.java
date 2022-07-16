/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.structure.pool;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import net.minecraft.block.JigsawBlock;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.structure.pool.EmptyPoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructurePoolBasedGenerator {
    static final Logger LOGGER = LogManager.getLogger();

    public static void generate(DynamicRegistryManager dynamicRegistries, StructurePoolFeatureConfig config, PieceFactory pieceFactory, ChunkGenerator chunkGenerator, StructureManager structureManager, BlockPos pos, StructurePiecesHolder children, Random random, boolean modifyBoundingBox, boolean surface, HeightLimitView world) {
        StructureFeature.init();
        ArrayList<PoolStructurePiece> list = Lists.newArrayList();
        Registry<StructurePool> registry = dynamicRegistries.get(Registry.STRUCTURE_POOL_KEY);
        BlockRotation blockRotation = BlockRotation.random(random);
        StructurePool structurePool = config.getStartPool().get();
        StructurePoolElement structurePoolElement = structurePool.getRandomElement(random);
        if (structurePoolElement == EmptyPoolElement.INSTANCE) {
            return;
        }
        PoolStructurePiece poolStructurePiece = pieceFactory.create(structureManager, structurePoolElement, pos, structurePoolElement.getGroundLevelDelta(), blockRotation, structurePoolElement.getBoundingBox(structureManager, pos, blockRotation));
        BlockBox blockBox = poolStructurePiece.getBoundingBox();
        int i = (blockBox.getMaxX() + blockBox.getMinX()) / 2;
        int j = (blockBox.getMaxZ() + blockBox.getMinZ()) / 2;
        int k = surface ? pos.getY() + chunkGenerator.getHeightOnGround(i, j, Heightmap.Type.WORLD_SURFACE_WG, world) : pos.getY();
        int l = blockBox.getMinY() + poolStructurePiece.getGroundLevelDelta();
        poolStructurePiece.translate(0, k - l, 0);
        list.add(poolStructurePiece);
        if (config.getSize() <= 0) {
            return;
        }
        int m = 80;
        Box box = new Box(i - 80, k - 80, j - 80, i + 80 + 1, k + 80 + 1, j + 80 + 1);
        StructurePoolGenerator structurePoolGenerator = new StructurePoolGenerator(registry, config.getSize(), pieceFactory, chunkGenerator, structureManager, list, random);
        structurePoolGenerator.structurePieces.addLast(new ShapedPoolStructurePiece(poolStructurePiece, new MutableObject<VoxelShape>(VoxelShapes.combineAndSimplify(VoxelShapes.cuboid(box), VoxelShapes.cuboid(Box.from(blockBox)), BooleanBiFunction.ONLY_FIRST)), k + 80, 0));
        while (!structurePoolGenerator.structurePieces.isEmpty()) {
            ShapedPoolStructurePiece shapedPoolStructurePiece = structurePoolGenerator.structurePieces.removeFirst();
            structurePoolGenerator.generatePiece(shapedPoolStructurePiece.piece, shapedPoolStructurePiece.pieceShape, shapedPoolStructurePiece.minY, shapedPoolStructurePiece.currentSize, modifyBoundingBox, world);
        }
        list.forEach(children::addPiece);
    }

    public static void method_27230(DynamicRegistryManager dynamicRegistryManager, PoolStructurePiece poolStructurePiece, int i, PieceFactory pieceFactory, ChunkGenerator chunkGenerator, StructureManager structureManager, List<? super PoolStructurePiece> list, Random random, HeightLimitView heightLimitView) {
        Registry<StructurePool> registry = dynamicRegistryManager.get(Registry.STRUCTURE_POOL_KEY);
        StructurePoolGenerator structurePoolGenerator = new StructurePoolGenerator(registry, i, pieceFactory, chunkGenerator, structureManager, list, random);
        structurePoolGenerator.structurePieces.addLast(new ShapedPoolStructurePiece(poolStructurePiece, new MutableObject<VoxelShape>(VoxelShapes.UNBOUNDED), 0, 0));
        while (!structurePoolGenerator.structurePieces.isEmpty()) {
            ShapedPoolStructurePiece shapedPoolStructurePiece = structurePoolGenerator.structurePieces.removeFirst();
            structurePoolGenerator.generatePiece(shapedPoolStructurePiece.piece, shapedPoolStructurePiece.pieceShape, shapedPoolStructurePiece.minY, shapedPoolStructurePiece.currentSize, false, heightLimitView);
        }
    }

    public static interface PieceFactory {
        public PoolStructurePiece create(StructureManager var1, StructurePoolElement var2, BlockPos var3, int var4, BlockRotation var5, BlockBox var6);
    }

    static final class StructurePoolGenerator {
        private final Registry<StructurePool> registry;
        private final int maxSize;
        private final PieceFactory pieceFactory;
        private final ChunkGenerator chunkGenerator;
        private final StructureManager structureManager;
        private final List<? super PoolStructurePiece> children;
        private final Random random;
        final Deque<ShapedPoolStructurePiece> structurePieces = Queues.newArrayDeque();

        StructurePoolGenerator(Registry<StructurePool> registry, int maxSize, PieceFactory pieceFactory, ChunkGenerator chunkGenerator, StructureManager structureManager, List<? super PoolStructurePiece> children, Random random) {
            this.registry = registry;
            this.maxSize = maxSize;
            this.pieceFactory = pieceFactory;
            this.chunkGenerator = chunkGenerator;
            this.structureManager = structureManager;
            this.children = children;
            this.random = random;
        }

        void generatePiece(PoolStructurePiece piece, MutableObject<VoxelShape> pieceShape, int minY, int currentSize, boolean modifyBoundingBox, HeightLimitView world) {
            StructurePoolElement structurePoolElement = piece.getPoolElement();
            BlockPos blockPos = piece.getPos();
            BlockRotation blockRotation = piece.getRotation();
            StructurePool.Projection projection = structurePoolElement.getProjection();
            boolean bl = projection == StructurePool.Projection.RIGID;
            MutableObject<VoxelShape> mutableObject = new MutableObject<VoxelShape>();
            BlockBox blockBox = piece.getBoundingBox();
            int i = blockBox.getMinY();
            block0: for (Structure.StructureBlockInfo structureBlockInfo2 : structurePoolElement.getStructureBlockInfos(this.structureManager, blockPos, blockRotation, this.random)) {
                StructurePoolElement structurePoolElement2;
                int l;
                MutableObject<Object> mutableObject2;
                Direction direction = JigsawBlock.getFacing(structureBlockInfo2.state);
                BlockPos blockPos2 = structureBlockInfo2.pos;
                BlockPos blockPos3 = blockPos2.offset(direction);
                int j = blockPos2.getY() - i;
                int k = -1;
                Identifier identifier = new Identifier(structureBlockInfo2.nbt.getString("pool"));
                Optional<StructurePool> optional = this.registry.getOrEmpty(identifier);
                if (!optional.isPresent() || optional.get().getElementCount() == 0 && !Objects.equals(identifier, StructurePools.EMPTY.getValue())) {
                    LOGGER.warn("Empty or non-existent pool: {}", (Object)identifier);
                    continue;
                }
                Identifier identifier2 = optional.get().getTerminatorsId();
                Optional<StructurePool> optional2 = this.registry.getOrEmpty(identifier2);
                if (!optional2.isPresent() || optional2.get().getElementCount() == 0 && !Objects.equals(identifier2, StructurePools.EMPTY.getValue())) {
                    LOGGER.warn("Empty or non-existent fallback pool: {}", (Object)identifier2);
                    continue;
                }
                boolean bl2 = blockBox.contains(blockPos3);
                if (bl2) {
                    mutableObject2 = mutableObject;
                    l = i;
                    if (mutableObject.getValue() == null) {
                        mutableObject.setValue(VoxelShapes.cuboid(Box.from(blockBox)));
                    }
                } else {
                    mutableObject2 = pieceShape;
                    l = minY;
                }
                ArrayList<StructurePoolElement> list = Lists.newArrayList();
                if (currentSize != this.maxSize) {
                    list.addAll(optional.get().getElementIndicesInRandomOrder(this.random));
                }
                list.addAll(optional2.get().getElementIndicesInRandomOrder(this.random));
                Iterator iterator = list.iterator();
                while (iterator.hasNext() && (structurePoolElement2 = (StructurePoolElement)iterator.next()) != EmptyPoolElement.INSTANCE) {
                    for (BlockRotation blockRotation2 : BlockRotation.randomRotationOrder(this.random)) {
                        List<Structure.StructureBlockInfo> list2 = structurePoolElement2.getStructureBlockInfos(this.structureManager, BlockPos.ORIGIN, blockRotation2, this.random);
                        BlockBox blockBox2 = structurePoolElement2.getBoundingBox(this.structureManager, BlockPos.ORIGIN, blockRotation2);
                        int m = !modifyBoundingBox || blockBox2.getBlockCountY() > 16 ? 0 : list2.stream().mapToInt(structureBlockInfo -> {
                            if (!blockBox2.contains(structureBlockInfo.pos.offset(JigsawBlock.getFacing(structureBlockInfo.state)))) {
                                return 0;
                            }
                            Identifier identifier = new Identifier(structureBlockInfo.nbt.getString("pool"));
                            Optional<StructurePool> optional = this.registry.getOrEmpty(identifier);
                            Optional<Integer> optional2 = optional.flatMap(structurePool -> this.registry.getOrEmpty(structurePool.getTerminatorsId()));
                            int i = optional.map(structurePool -> structurePool.getHighestY(this.structureManager)).orElse(0);
                            int j = optional2.map(structurePool -> structurePool.getHighestY(this.structureManager)).orElse(0);
                            return Math.max(i, j);
                        }).max().orElse(0);
                        for (Structure.StructureBlockInfo structureBlockInfo22 : list2) {
                            int u;
                            int s;
                            int q;
                            if (!JigsawBlock.attachmentMatches(structureBlockInfo2, structureBlockInfo22)) continue;
                            BlockPos blockPos4 = structureBlockInfo22.pos;
                            BlockPos blockPos5 = blockPos3.subtract(blockPos4);
                            BlockBox blockBox3 = structurePoolElement2.getBoundingBox(this.structureManager, blockPos5, blockRotation2);
                            int n = blockBox3.getMinY();
                            StructurePool.Projection projection2 = structurePoolElement2.getProjection();
                            boolean bl3 = projection2 == StructurePool.Projection.RIGID;
                            int o = blockPos4.getY();
                            int p = j - o + JigsawBlock.getFacing(structureBlockInfo2.state).getOffsetY();
                            if (bl && bl3) {
                                q = i + p;
                            } else {
                                if (k == -1) {
                                    k = this.chunkGenerator.getHeightOnGround(blockPos2.getX(), blockPos2.getZ(), Heightmap.Type.WORLD_SURFACE_WG, world);
                                }
                                q = k - o;
                            }
                            int r = q - n;
                            BlockBox blockBox4 = blockBox3.offset(0, r, 0);
                            BlockPos blockPos6 = blockPos5.add(0, r, 0);
                            if (m > 0) {
                                s = Math.max(m + 1, blockBox4.getMaxY() - blockBox4.getMinY());
                                blockBox4.encompass(new BlockPos(blockBox4.getMinX(), blockBox4.getMinY() + s, blockBox4.getMinZ()));
                            }
                            if (VoxelShapes.matchesAnywhere((VoxelShape)mutableObject2.getValue(), VoxelShapes.cuboid(Box.from(blockBox4).contract(0.25)), BooleanBiFunction.ONLY_SECOND)) continue;
                            mutableObject2.setValue(VoxelShapes.combine((VoxelShape)mutableObject2.getValue(), VoxelShapes.cuboid(Box.from(blockBox4)), BooleanBiFunction.ONLY_FIRST));
                            s = piece.getGroundLevelDelta();
                            int t = bl3 ? s - p : structurePoolElement2.getGroundLevelDelta();
                            PoolStructurePiece poolStructurePiece = this.pieceFactory.create(this.structureManager, structurePoolElement2, blockPos6, t, blockRotation2, blockBox4);
                            if (bl) {
                                u = i + j;
                            } else if (bl3) {
                                u = q + o;
                            } else {
                                if (k == -1) {
                                    k = this.chunkGenerator.getHeightOnGround(blockPos2.getX(), blockPos2.getZ(), Heightmap.Type.WORLD_SURFACE_WG, world);
                                }
                                u = k + p / 2;
                            }
                            piece.addJunction(new JigsawJunction(blockPos3.getX(), u - j + s, blockPos3.getZ(), p, projection2));
                            poolStructurePiece.addJunction(new JigsawJunction(blockPos2.getX(), u - o + t, blockPos2.getZ(), -p, projection));
                            this.children.add(poolStructurePiece);
                            if (currentSize + 1 > this.maxSize) continue block0;
                            this.structurePieces.addLast(new ShapedPoolStructurePiece(poolStructurePiece, mutableObject2, l, currentSize + 1));
                            continue block0;
                        }
                    }
                }
            }
        }
    }

    static final class ShapedPoolStructurePiece {
        final PoolStructurePiece piece;
        final MutableObject<VoxelShape> pieceShape;
        final int minY;
        final int currentSize;

        ShapedPoolStructurePiece(PoolStructurePiece piece, MutableObject<VoxelShape> pieceShape, int minY, int currentSize) {
            this.piece = piece;
            this.pieceShape = pieceShape;
            this.minY = minY;
            this.currentSize = currentSize;
        }
    }
}

