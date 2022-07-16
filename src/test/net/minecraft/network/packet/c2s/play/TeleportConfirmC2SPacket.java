/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;

public class TeleportConfirmC2SPacket
implements Packet<ServerPlayPacketListener> {
    private final int teleportId;

    public TeleportConfirmC2SPacket(int teleportId) {
        this.teleportId = teleportId;
    }

    public TeleportConfirmC2SPacket(PacketByteBuf buf) {
        this.teleportId = buf.readVarInt();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.teleportId);
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onTeleportConfirm(this);
    }

    public int getTeleportId() {
        return this.teleportId;
    }
}

