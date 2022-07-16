/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world;

import java.util.Comparator;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.Heightmap;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

public class PortalForcer {
    private static final int field_31810 = 3;
    private static final int field_31811 = 128;
    private static final int field_31812 = 16;
    private static final int field_31813 = 5;
    private static final int field_31814 = 4;
    private static final int field_31815 = 3;
    private static final int field_31816 = -1;
    private static final int field_31817 = 4;
    private static final int field_31818 = -1;
    private static final int field_31819 = 3;
    private static final int field_31820 = -1;
    private static final int field_31821 = 2;
    private static final int field_31822 = -1;
    private final ServerWorld world;

    public PortalForcer(ServerWorld world) {
        this.world = world;
    }

    public Optional<BlockLocating.Rectangle> getPortalRect(BlockPos destPos, boolean destIsNether) {
        PointOfInterestStorage pointOfInterestStorage = this.world.getPointOfInterestStorage();
        int i = destIsNether ? 16 : 128;
        pointOfInterestStorage.preloadChunks(this.world, destPos, i);
        Optional<PointOfInterest> optional = pointOfInterestStorage.getInSquare(pointOfInterestType -> pointOfInterestType == PointOfInterestType.NETHER_PORTAL, destPos, i, PointOfInterestStorage.OccupationStatus.ANY).sorted(Comparator.comparingDouble(pointOfInterest -> pointOfInterest.getPos().getSquaredDistance(destPos)).thenComparingInt(pointOfInterest -> pointOfInterest.getPos().getY())).filter(pointOfInterest -> this.world.getBlockState(pointOfInterest.getPos()).contains(Properties.HORIZONTAL_AXIS)).findFirst();
        return optional.map(pointOfInterest -> {
            BlockPos blockPos2 = pointOfInterest.getPos();
            this.world.getChunkManager().addTicket(ChunkTicketType.PORTAL, new ChunkPos(blockPos2), 3, blockPos2);
            BlockState blockState = this.world.getBlockState(blockPos2);
            return BlockLocating.getLargestRectangle(blockPos2, blockState.get(Properties.HORIZONTAL_AXIS), 21, Direction.Axis.Y, 21, blockPos -> this.world.getBlockState((BlockPos)blockPos) == blockState);
        });
    }

    public Optional<BlockLocating.Rectangle> createPortal(BlockPos blockPos, Direction.Axis axis) {
        int m;
        int l;
        int k;
        Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        double d = -1.0;
        BlockPos blockPos2 = null;
        double e = -1.0;
        BlockPos blockPos3 = null;
        WorldBorder worldBorder = this.world.getWorldBorder();
        int i = Math.min(this.world.getTopY(), this.world.getBottomY() + this.world.getLogicalHeight()) - 1;
        BlockPos.Mutable mutable = blockPos.mutableCopy();
        for (BlockPos.Mutable mutable2 : BlockPos.iterateInSquare(blockPos, 16, Direction.EAST, Direction.SOUTH)) {
            int j = Math.min(i, this.world.getTopY(Heightmap.Type.MOTION_BLOCKING, mutable2.getX(), mutable2.getZ()));
            k = 1;
            if (!worldBorder.contains(mutable2) || !worldBorder.contains(mutable2.move(direction, 1))) continue;
            mutable2.move(direction.getOpposite(), 1);
            for (l = j; l >= this.world.getBottomY(); --l) {
                int n;
                mutable2.setY(l);
                if (!this.world.isAir(mutable2)) continue;
                m = l;
                while (l > this.world.getBottomY() && this.world.isAir(mutable2.move(Direction.DOWN))) {
                    --l;
                }
                if (l + 4 > i || (n = m - l) > 0 && n < 3) continue;
                mutable2.setY(l);
                if (!this.isValidPortalPos(mutable2, mutable, direction, 0)) continue;
                double f = blockPos.getSquaredDistance(mutable2);
                if (this.isValidPortalPos(mutable2, mutable, direction, -1) && this.isValidPortalPos(mutable2, mutable, direction, 1) && (d == -1.0 || d > f)) {
                    d = f;
                    blockPos2 = mutable2.toImmutable();
                }
                if (d != -1.0 || e != -1.0 && !(e > f)) continue;
                e = f;
                blockPos3 = mutable2.toImmutable();
            }
        }
        if (d == -1.0 && e != -1.0) {
            blockPos2 = blockPos3;
            d = e;
        }
        if (d == -1.0) {
            int mutable2 = i - 9;
            int o = Math.max(this.world.getBottomY() - -1, 70);
            if (mutable2 < o) {
                return Optional.empty();
            }
            blockPos2 = new BlockPos(blockPos.getX(), MathHelper.clamp(blockPos.getY(), o, mutable2), blockPos.getZ()).toImmutable();
            Direction j = direction.rotateYClockwise();
            if (!worldBorder.contains(blockPos2)) {
                return Optional.empty();
            }
            for (k = -1; k < 2; ++k) {
                for (l = 0; l < 2; ++l) {
                    for (m = -1; m < 3; ++m) {
                        BlockState n = m < 0 ? Blocks.OBSIDIAN.getDefaultState() : Blocks.AIR.getDefaultState();
                        mutable.set(blockPos2, l * direction.getOffsetX() + k * j.getOffsetX(), m, l * direction.getOffsetZ() + k * j.getOffsetZ());
                        this.world.setBlockState(mutable, n);
                    }
                }
            }
        }
        for (int o = -1; o < 3; ++o) {
            for (int mutable2 = -1; mutable2 < 4; ++mutable2) {
                if (o != -1 && o != 2 && mutable2 != -1 && mutable2 != 3) continue;
                mutable.set(blockPos2, o * direction.getOffsetX(), mutable2, o * direction.getOffsetZ());
                this.world.setBlockState(mutable, Blocks.OBSIDIAN.getDefaultState(), Block.NOTIFY_ALL);
            }
        }
        BlockState o = (BlockState)Blocks.NETHER_PORTAL.getDefaultState().with(NetherPortalBlock.AXIS, axis);
        for (int mutable2 = 0; mutable2 < 2; ++mutable2) {
            for (int j = 0; j < 3; ++j) {
                mutable.set(blockPos2, mutable2 * direction.getOffsetX(), j, mutable2 * direction.getOffsetZ());
                this.world.setBlockState(mutable, o, Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
            }
        }
        return Optional.of(new BlockLocating.Rectangle(blockPos2.toImmutable(), 2, 3));
    }

    private boolean isValidPortalPos(BlockPos pos, BlockPos.Mutable temp, Direction portalDirection, int distanceOrthogonalToPortal) {
        Direction direction = portalDirection.rotateYClockwise();
        for (int i = -1; i < 3; ++i) {
            for (int j = -1; j < 4; ++j) {
                temp.set(pos, portalDirection.getOffsetX() * i + direction.getOffsetX() * distanceOrthogonalToPortal, j, portalDirection.getOffsetZ() * i + direction.getOffsetZ() * distanceOrthogonalToPortal);
                if (j < 0 && !this.world.getBlockState(temp).getMaterial().isSolid()) {
                    return false;
                }
                if (j < 0 || this.world.isAir(temp)) continue;
                return false;
            }
        }
        return true;
    }
}

