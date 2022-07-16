/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;

public class ClientStatusC2SPacket
implements Packet<ServerPlayPacketListener> {
    private final Mode mode;

    public ClientStatusC2SPacket(Mode mode) {
        this.mode = mode;
    }

    public ClientStatusC2SPacket(PacketByteBuf buf) {
        this.mode = buf.readEnumConstant(Mode.class);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(this.mode);
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onClientStatus(this);
    }

    public Mode getMode() {
        return this.mode;
    }

    public static enum Mode {
        PERFORM_RESPAWN,
        REQUEST_STATS;

    }
}

