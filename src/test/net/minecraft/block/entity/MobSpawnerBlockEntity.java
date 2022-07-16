/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerEntry;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MobSpawnerBlockEntity
extends BlockEntity {
    private final MobSpawnerLogic logic = new MobSpawnerLogic(){

        @Override
        public void sendStatus(World world, BlockPos pos, int i) {
            world.addSyncedBlockEvent(pos, Blocks.SPAWNER, i, 0);
        }

        @Override
        public void setSpawnEntry(@Nullable World world, BlockPos pos, MobSpawnerEntry spawnEntry) {
            super.setSpawnEntry(world, pos, spawnEntry);
            if (world != null) {
                BlockState blockState = world.getBlockState(pos);
                world.updateListeners(pos, blockState, blockState, Block.NO_REDRAW);
            }
        }
    };

    public MobSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.MOB_SPAWNER, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.logic.readNbt(this.world, this.pos, nbt);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        this.logic.writeNbt(this.world, this.pos, nbt);
        return nbt;
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, MobSpawnerBlockEntity blockEntity) {
        blockEntity.logic.clientTick(world, pos);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, MobSpawnerBlockEntity blockEntity) {
        blockEntity.logic.serverTick((ServerWorld)world, pos);
    }

    @Override
    @Nullable
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return new BlockEntityUpdateS2CPacket(this.pos, BlockEntityUpdateS2CPacket.MOB_SPAWNER, this.toInitialChunkDataNbt());
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbtCompound = this.writeNbt(new NbtCompound());
        nbtCompound.remove("SpawnPotentials");
        return nbtCompound;
    }

    @Override
    public boolean onSyncedBlockEvent(int type, int data) {
        if (this.logic.method_8275(this.world, type)) {
            return true;
        }
        return super.onSyncedBlockEvent(type, data);
    }

    @Override
    public boolean copyItemDataRequiresOperator() {
        return true;
    }

    public MobSpawnerLogic getLogic() {
        return this.logic;
    }
}
