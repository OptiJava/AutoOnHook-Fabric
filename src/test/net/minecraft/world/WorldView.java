/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world;

import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.ColorResolver;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a scoped, read-only view of a world like structure that contains biomes, chunks and is bound to a dimension.
 */
public interface WorldView
extends BlockRenderView,
CollisionView,
BiomeAccess.Storage {
    @Nullable
    public Chunk getChunk(int var1, int var2, ChunkStatus var3, boolean var4);

    @Deprecated
    public boolean isChunkLoaded(int var1, int var2);

    public int getTopY(Heightmap.Type var1, int var2, int var3);

    public int getAmbientDarkness();

    public BiomeAccess getBiomeAccess();

    default public Biome getBiome(BlockPos pos) {
        return this.getBiomeAccess().getBiome(pos);
    }

    default public Stream<BlockState> getStatesInBoxIfLoaded(Box box) {
        int n;
        int i = MathHelper.floor(box.minX);
        int j = MathHelper.floor(box.maxX);
        int k = MathHelper.floor(box.minY);
        int l = MathHelper.floor(box.maxY);
        int m = MathHelper.floor(box.minZ);
        if (this.isRegionLoaded(i, k, m, j, l, n = MathHelper.floor(box.maxZ))) {
            return this.getStatesInBox(box);
        }
        return Stream.empty();
    }

    @Override
    default public int getColor(BlockPos pos, ColorResolver colorResolver) {
        return colorResolver.getColor(this.getBiome(pos), pos.getX(), pos.getZ());
    }

    @Override
    default public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        Chunk chunk = this.getChunk(BiomeCoords.toChunk(biomeX), BiomeCoords.toChunk(biomeZ), ChunkStatus.BIOMES, false);
        if (chunk != null && chunk.getBiomeArray() != null) {
            return chunk.getBiomeArray().getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
        }
        return this.getGeneratorStoredBiome(biomeX, biomeY, biomeZ);
    }

    public Biome getGeneratorStoredBiome(int var1, int var2, int var3);

    /**
     * Checks if this world view is on the logical client.
     * 
     * <p>If the value returned is false, it is expected that this world is present on a logical server.
     */
    public boolean isClient();

    @Deprecated
    public int getSeaLevel();

    public DimensionType getDimension();

    @Override
    default public int getBottomY() {
        return this.getDimension().getMinimumY();
    }

    @Override
    default public int getHeight() {
        return this.getDimension().getHeight();
    }

    default public BlockPos getTopPosition(Heightmap.Type heightmap, BlockPos pos) {
        return new BlockPos(pos.getX(), this.getTopY(heightmap, pos.getX(), pos.getZ()), pos.getZ());
    }

    default public boolean isAir(BlockPos pos) {
        return this.getBlockState(pos).isAir();
    }

    default public boolean isSkyVisibleAllowingSea(BlockPos pos) {
        if (pos.getY() >= this.getSeaLevel()) {
            return this.isSkyVisible(pos);
        }
        BlockPos blockPos = new BlockPos(pos.getX(), this.getSeaLevel(), pos.getZ());
        if (!this.isSkyVisible(blockPos)) {
            return false;
        }
        blockPos = blockPos.down();
        while (blockPos.getY() > pos.getY()) {
            BlockState blockState = this.getBlockState(blockPos);
            if (blockState.getOpacity(this, blockPos) > 0 && !blockState.getMaterial().isLiquid()) {
                return false;
            }
            blockPos = blockPos.down();
        }
        return true;
    }

    @Deprecated
    default public float getBrightness(BlockPos pos) {
        return this.getDimension().getBrightness(this.getLightLevel(pos));
    }

    default public int getStrongRedstonePower(BlockPos pos, Direction direction) {
        return this.getBlockState(pos).getStrongRedstonePower(this, pos, direction);
    }

    default public Chunk getChunk(BlockPos pos) {
        return this.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
    }

    default public Chunk getChunk(int chunkX, int chunkZ) {
        return this.getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);
    }

    default public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus status) {
        return this.getChunk(chunkX, chunkZ, status, true);
    }

    @Override
    @Nullable
    default public BlockView getChunkAsView(int chunkX, int chunkZ) {
        return this.getChunk(chunkX, chunkZ, ChunkStatus.EMPTY, false);
    }

    default public boolean isWater(BlockPos pos) {
        return this.getFluidState(pos).isIn(FluidTags.WATER);
    }

    default public boolean containsFluid(Box box) {
        int i = MathHelper.floor(box.minX);
        int j = MathHelper.ceil(box.maxX);
        int k = MathHelper.floor(box.minY);
        int l = MathHelper.ceil(box.maxY);
        int m = MathHelper.floor(box.minZ);
        int n = MathHelper.ceil(box.maxZ);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int o = i; o < j; ++o) {
            for (int p = k; p < l; ++p) {
                for (int q = m; q < n; ++q) {
                    BlockState blockState = this.getBlockState(mutable.set(o, p, q));
                    if (blockState.getFluidState().isEmpty()) continue;
                    return true;
                }
            }
        }
        return false;
    }

    default public int getLightLevel(BlockPos pos) {
        return this.getLightLevel(pos, this.getAmbientDarkness());
    }

    default public int getLightLevel(BlockPos pos, int ambientDarkness) {
        if (pos.getX() < -30000000 || pos.getZ() < -30000000 || pos.getX() >= 30000000 || pos.getZ() >= 30000000) {
            return 15;
        }
        return this.getBaseLightLevel(pos, ambientDarkness);
    }

    @Deprecated
    default public boolean isPosLoaded(int x, int z) {
        return this.isChunkLoaded(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
    }

    @Deprecated
    default public boolean isChunkLoaded(BlockPos pos) {
        return this.isPosLoaded(pos.getX(), pos.getZ());
    }

    @Deprecated
    default public boolean isRegionLoaded(BlockPos min, BlockPos max) {
        return this.isRegionLoaded(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }

    @Deprecated
    default public boolean isRegionLoaded(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (maxY < this.getBottomY() || minY >= this.getTopY()) {
            return false;
        }
        return this.isRegionLoaded(minX, minZ, maxX, maxZ);
    }

    @Deprecated
    default public boolean isRegionLoaded(int minX, int minZ, int maxX, int maxZ) {
        int i = ChunkSectionPos.getSectionCoord(minX);
        int j = ChunkSectionPos.getSectionCoord(maxX);
        int k = ChunkSectionPos.getSectionCoord(minZ);
        int l = ChunkSectionPos.getSectionCoord(maxZ);
        for (int m = i; m <= j; ++m) {
            for (int n = k; n <= l; ++n) {
                if (this.isChunkLoaded(m, n)) continue;
                return false;
            }
        }
        return true;
    }
}

