/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.server.world;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class BlockEvent {
    private final BlockPos pos;
    private final Block block;
    private final int type;
    private final int data;

    public BlockEvent(BlockPos pos, Block block, int type, int data) {
        this.pos = pos;
        this.block = block;
        this.type = type;
        this.data = data;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Block getBlock() {
        return this.block;
    }

    public int getType() {
        return this.type;
    }

    public int getData() {
        return this.data;
    }

    public boolean equals(Object o) {
        if (o instanceof BlockEvent) {
            BlockEvent blockEvent = (BlockEvent)o;
            return this.pos.equals(blockEvent.pos) && this.type == blockEvent.type && this.data == blockEvent.data && this.block == blockEvent.block;
        }
        return false;
    }

    public int hashCode() {
        int i = this.pos.hashCode();
        i = 31 * i + this.block.hashCode();
        i = 31 * i + this.type;
        i = 31 * i + this.data;
        return i;
    }

    public String toString() {
        return "TE(" + this.pos + ")," + this.type + "," + this.data + "," + this.block;
    }
}

