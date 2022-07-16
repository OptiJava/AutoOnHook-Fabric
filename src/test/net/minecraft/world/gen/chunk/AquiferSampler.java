/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.chunk;

import java.util.Arrays;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.gen.BlockSource;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.NoiseColumnSampler;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;

public interface AquiferSampler {
    public static final int field_33571 = 9;
    public static final int field_33572 = 30;

    public static AquiferSampler aquifer(ChunkPos pos, DoublePerlinNoiseSampler edgeDensityNoise, DoublePerlinNoiseSampler fluidLevelNoise, DoublePerlinNoiseSampler fluidTypeNoise, ChunkGeneratorSettings settings, NoiseColumnSampler columnSampler, int startY, int deltaY) {
        return new Impl(pos, edgeDensityNoise, fluidLevelNoise, fluidTypeNoise, settings, columnSampler, startY, deltaY);
    }

    public static AquiferSampler seaLevel(final int seaLevel, final BlockState state) {
        return new AquiferSampler(){

            @Override
            public BlockState apply(BlockSource source, int x, int y, int z, double weight) {
                if (weight > 0.0) {
                    return source.sample(x, y, z);
                }
                if (y >= seaLevel) {
                    return Blocks.AIR.getDefaultState();
                }
                return state;
            }

            @Override
            public boolean needsFluidTick() {
                return false;
            }
        };
    }

    public BlockState apply(BlockSource var1, int var2, int var3, int var4, double var5);

    public boolean needsFluidTick();

    public static class Impl
    implements AquiferSampler {
        private static final int field_31451 = 10;
        private static final int field_31452 = 9;
        private static final int field_31453 = 10;
        private static final int field_31454 = 6;
        private static final int field_31455 = 3;
        private static final int field_31456 = 6;
        private static final int field_31457 = 16;
        private static final int field_31458 = 12;
        private static final int field_31459 = 16;
        private final DoublePerlinNoiseSampler edgeDensityNoise;
        private final DoublePerlinNoiseSampler fluidLevelNoise;
        private final DoublePerlinNoiseSampler fluidTypeNoise;
        private final ChunkGeneratorSettings settings;
        private final FluidLevel[] waterLevels;
        private final long[] blockPositions;
        private boolean needsFluidTick;
        private final NoiseColumnSampler columnSampler;
        private final int startX;
        private final int startY;
        private final int startZ;
        private final int sizeX;
        private final int sizeZ;

        Impl(ChunkPos pos, DoublePerlinNoiseSampler edgeDensityNoise, DoublePerlinNoiseSampler fluidLevelNoise, DoublePerlinNoiseSampler fluidTypeNoise, ChunkGeneratorSettings settings, NoiseColumnSampler columnSampler, int startY, int deltaY) {
            this.edgeDensityNoise = edgeDensityNoise;
            this.fluidLevelNoise = fluidLevelNoise;
            this.fluidTypeNoise = fluidTypeNoise;
            this.settings = settings;
            this.columnSampler = columnSampler;
            this.startX = this.getLocalX(pos.getStartX()) - 1;
            int i = this.getLocalX(pos.getEndX()) + 1;
            this.sizeX = i - this.startX + 1;
            this.startY = this.getLocalY(startY) - 1;
            int j = this.getLocalY(startY + deltaY) + 1;
            int k = j - this.startY + 1;
            this.startZ = this.getLocalZ(pos.getStartZ()) - 1;
            int l = this.getLocalZ(pos.getEndZ()) + 1;
            this.sizeZ = l - this.startZ + 1;
            int m = this.sizeX * k * this.sizeZ;
            this.waterLevels = new FluidLevel[m];
            this.blockPositions = new long[m];
            Arrays.fill(this.blockPositions, Long.MAX_VALUE);
        }

        private int index(int x, int y, int z) {
            int i = x - this.startX;
            int j = y - this.startY;
            int k = z - this.startZ;
            return (j * this.sizeZ + k) * this.sizeX + i;
        }

        @Override
        public BlockState apply(BlockSource source, int x, int y, int z, double weight) {
            if (weight <= 0.0) {
                boolean bl;
                double d;
                BlockState blockState;
                if (this.shouldBeLava(y)) {
                    blockState = Blocks.LAVA.getDefaultState();
                    d = 0.0;
                    bl = false;
                } else {
                    int i = Math.floorDiv(x - 5, 16);
                    int j = Math.floorDiv(y + 1, 12);
                    int k = Math.floorDiv(z - 5, 16);
                    int l = Integer.MAX_VALUE;
                    int m = Integer.MAX_VALUE;
                    int n = Integer.MAX_VALUE;
                    long o = 0L;
                    long p = 0L;
                    long q = 0L;
                    for (int r = 0; r <= 1; ++r) {
                        for (int s = -1; s <= 1; ++s) {
                            for (int t = 0; t <= 1; ++t) {
                                long ac;
                                int u = i + r;
                                int v = j + s;
                                int w = k + t;
                                int aa = this.index(u, v, w);
                                long ab = this.blockPositions[aa];
                                if (ab != Long.MAX_VALUE) {
                                    ac = ab;
                                } else {
                                    ChunkRandom chunkRandom = new ChunkRandom(MathHelper.hashCode(u, v * 3, w) + 1L);
                                    this.blockPositions[aa] = ac = BlockPos.asLong(u * 16 + chunkRandom.nextInt(10), v * 12 + chunkRandom.nextInt(9), w * 16 + chunkRandom.nextInt(10));
                                }
                                int chunkRandom = BlockPos.unpackLongX(ac) - x;
                                int ad = BlockPos.unpackLongY(ac) - y;
                                int ae = BlockPos.unpackLongZ(ac) - z;
                                int af = chunkRandom * chunkRandom + ad * ad + ae * ae;
                                if (l >= af) {
                                    q = p;
                                    p = o;
                                    o = ac;
                                    n = m;
                                    m = l;
                                    l = af;
                                    continue;
                                }
                                if (m >= af) {
                                    q = p;
                                    p = ac;
                                    n = m;
                                    m = af;
                                    continue;
                                }
                                if (n < af) continue;
                                q = ac;
                                n = af;
                            }
                        }
                    }
                    FluidLevel r = this.getWaterLevel(o);
                    FluidLevel s = this.getWaterLevel(p);
                    FluidLevel t = this.getWaterLevel(q);
                    double u = this.maxDistance(l, m);
                    double w = this.maxDistance(l, n);
                    double ac = this.maxDistance(m, n);
                    boolean bl2 = bl = u > 0.0;
                    if (r.y >= y && r.state.isOf(Blocks.WATER) && this.shouldBeLava(y - 1)) {
                        d = 1.0;
                    } else if (u > -1.0) {
                        double ab = 1.0 + (this.edgeDensityNoise.sample(x, y, z) + 0.05) / 4.0;
                        double chunkRandom = this.calculateDensity(y, ab, r, s);
                        double ae = this.calculateDensity(y, ab, r, t);
                        double e = this.calculateDensity(y, ab, s, t);
                        double f = Math.max(0.0, u);
                        double g = Math.max(0.0, w);
                        double h = Math.max(0.0, ac);
                        double ag = 2.0 * f * Math.max(chunkRandom, Math.max(ae * g, e * h));
                        d = Math.max(0.0, ag);
                    } else {
                        d = 0.0;
                    }
                    BlockState blockState2 = blockState = y >= r.y ? Blocks.AIR.getDefaultState() : r.state;
                }
                if (weight + d <= 0.0) {
                    this.needsFluidTick = bl;
                    return blockState;
                }
            }
            this.needsFluidTick = false;
            return source.sample(x, y, z);
        }

        @Override
        public boolean needsFluidTick() {
            return this.needsFluidTick;
        }

        private boolean shouldBeLava(int y) {
            return y - this.settings.getGenerationShapeConfig().getMinimumY() <= 9;
        }

        private double maxDistance(int a, int b) {
            double d = 25.0;
            return 1.0 - (double)Math.abs(b - a) / 25.0;
        }

        private double calculateDensity(int y, double noise, FluidLevel first, FluidLevel second) {
            if (y <= first.y && y <= second.y && first.state != second.state) {
                return 1.0;
            }
            int i = Math.abs(first.y - second.y);
            double d = 0.5 * (double)(first.y + second.y);
            double e = Math.abs(d - (double)y - 0.5);
            return 0.5 * (double)i * noise - e;
        }

        private int getLocalX(int x) {
            return Math.floorDiv(x, 16);
        }

        private int getLocalY(int y) {
            return Math.floorDiv(y, 12);
        }

        private int getLocalZ(int z) {
            return Math.floorDiv(z, 16);
        }

        private FluidLevel getWaterLevel(long pos) {
            FluidLevel fluidLevel2;
            int n;
            int m;
            int i = BlockPos.unpackLongX(pos);
            int j = BlockPos.unpackLongY(pos);
            int k = BlockPos.unpackLongZ(pos);
            int l = this.getLocalX(i);
            int o = this.index(l, m = this.getLocalY(j), n = this.getLocalZ(k));
            FluidLevel fluidLevel = this.waterLevels[o];
            if (fluidLevel != null) {
                return fluidLevel;
            }
            this.waterLevels[o] = fluidLevel2 = this.getFluidLevel(i, j, k);
            return fluidLevel2;
        }

        private FluidLevel getFluidLevel(int x, int y, int z) {
            int i = this.settings.getSeaLevel();
            if (y > 30) {
                return new FluidLevel(i, Blocks.WATER.getDefaultState());
            }
            int j = 64;
            int k = -10;
            int l = 40;
            double d = this.fluidLevelNoise.sample(Math.floorDiv(x, 64), (double)Math.floorDiv(y, 40) / 1.4, Math.floorDiv(z, 64)) * 30.0 + -10.0;
            boolean bl = false;
            if (Math.abs(d) > 8.0) {
                d *= 4.0;
            }
            int m = Math.floorDiv(y, 40) * 40 + 20;
            int n = m + MathHelper.floor(d);
            if (m == -20) {
                double e = this.fluidTypeNoise.sample(Math.floorDiv(x, 64), (double)Math.floorDiv(y, 40) / 1.4, Math.floorDiv(z, 64));
                bl = Math.abs(e) > (double)0.22f;
            }
            return new FluidLevel(Math.min(56, n), bl ? Blocks.LAVA.getDefaultState() : Blocks.WATER.getDefaultState());
        }

        static final class Impl.FluidLevel {
            final int y;
            final BlockState state;

            public FluidLevel(int y, BlockState state) {
                this.y = y;
                this.state = state;
            }
        }
    }
}

