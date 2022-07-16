/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.chunk;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.chunk.ChunkOcclusionData;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Environment(value=EnvType.CLIENT)
public class ChunkOcclusionDataBuilder {
    private static final int field_32833 = 4;
    private static final int field_32834 = 16;
    private static final int field_32835 = 15;
    private static final int field_32836 = 4096;
    private static final int field_32837 = 0;
    private static final int field_32838 = 4;
    private static final int field_32839 = 8;
    private static final int STEP_X = (int)Math.pow(16.0, 0.0);
    private static final int STEP_Z = (int)Math.pow(16.0, 1.0);
    private static final int STEP_Y = (int)Math.pow(16.0, 2.0);
    private static final int field_32840 = -1;
    private static final Direction[] DIRECTIONS = Direction.values();
    private final BitSet closed = new BitSet(4096);
    private static final int[] EDGE_POINTS = Util.make(new int[1352], is -> {
        boolean i = false;
        int j = 15;
        int k = 0;
        for (int l = 0; l < 16; ++l) {
            for (int m = 0; m < 16; ++m) {
                for (int n = 0; n < 16; ++n) {
                    if (l != 0 && l != 15 && m != 0 && m != 15 && n != 0 && n != 15) continue;
                    is[k++] = ChunkOcclusionDataBuilder.pack(l, m, n);
                }
            }
        }
    });
    private int openCount = 4096;

    public void markClosed(BlockPos pos) {
        this.closed.set(ChunkOcclusionDataBuilder.pack(pos), true);
        --this.openCount;
    }

    private static int pack(BlockPos pos) {
        return ChunkOcclusionDataBuilder.pack(pos.getX() & 0xF, pos.getY() & 0xF, pos.getZ() & 0xF);
    }

    private static int pack(int x, int y, int z) {
        return x << 0 | y << 8 | z << 4;
    }

    public ChunkOcclusionData build() {
        ChunkOcclusionData chunkOcclusionData = new ChunkOcclusionData();
        if (4096 - this.openCount < 256) {
            chunkOcclusionData.fill(true);
        } else if (this.openCount == 0) {
            chunkOcclusionData.fill(false);
        } else {
            for (int i : EDGE_POINTS) {
                if (this.closed.get(i)) continue;
                chunkOcclusionData.addOpenEdgeFaces(this.getOpenFaces(i));
            }
        }
        return chunkOcclusionData;
    }

    private Set<Direction> getOpenFaces(int pos) {
        EnumSet<Direction> set = EnumSet.noneOf(Direction.class);
        IntArrayFIFOQueue intPriorityQueue = new IntArrayFIFOQueue();
        intPriorityQueue.enqueue(pos);
        this.closed.set(pos, true);
        while (!intPriorityQueue.isEmpty()) {
            int i = intPriorityQueue.dequeueInt();
            this.addEdgeFaces(i, set);
            for (Direction direction : DIRECTIONS) {
                int j = this.offset(i, direction);
                if (j < 0 || this.closed.get(j)) continue;
                this.closed.set(j, true);
                intPriorityQueue.enqueue(j);
            }
        }
        return set;
    }

    private void addEdgeFaces(int pos, Set<Direction> openFaces) {
        int i = pos >> 0 & 0xF;
        if (i == 0) {
            openFaces.add(Direction.WEST);
        } else if (i == 15) {
            openFaces.add(Direction.EAST);
        }
        int j = pos >> 8 & 0xF;
        if (j == 0) {
            openFaces.add(Direction.DOWN);
        } else if (j == 15) {
            openFaces.add(Direction.UP);
        }
        int k = pos >> 4 & 0xF;
        if (k == 0) {
            openFaces.add(Direction.NORTH);
        } else if (k == 15) {
            openFaces.add(Direction.SOUTH);
        }
    }

    private int offset(int pos, Direction direction) {
        switch (direction) {
            case DOWN: {
                if ((pos >> 8 & 0xF) == 0) {
                    return -1;
                }
                return pos - STEP_Y;
            }
            case UP: {
                if ((pos >> 8 & 0xF) == 15) {
                    return -1;
                }
                return pos + STEP_Y;
            }
            case NORTH: {
                if ((pos >> 4 & 0xF) == 0) {
                    return -1;
                }
                return pos - STEP_Z;
            }
            case SOUTH: {
                if ((pos >> 4 & 0xF) == 15) {
                    return -1;
                }
                return pos + STEP_Z;
            }
            case WEST: {
                if ((pos >> 0 & 0xF) == 0) {
                    return -1;
                }
                return pos - STEP_X;
            }
            case EAST: {
                if ((pos >> 0 & 0xF) == 15) {
                    return -1;
                }
                return pos + STEP_X;
            }
        }
        return -1;
    }
}

