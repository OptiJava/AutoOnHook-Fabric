/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;

public class UnloadChunkS2CPacket
implements Packet<ClientPlayPacketListener> {
    private final int x;
    private final int z;

    public UnloadChunkS2CPacket(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public UnloadChunkS2CPacket(PacketByteBuf buf) {
        this.x = buf.readInt();
        this.z = buf.readInt();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.z);
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onUnloadChunk(this);
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }
}

