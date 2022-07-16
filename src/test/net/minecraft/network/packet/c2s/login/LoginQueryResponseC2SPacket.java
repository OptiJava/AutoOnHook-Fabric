/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.network.packet.c2s.login;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerLoginPacketListener;
import org.jetbrains.annotations.Nullable;

public class LoginQueryResponseC2SPacket
implements Packet<ServerLoginPacketListener> {
    private static final int MAX_PAYLOAD_SIZE = 0x100000;
    private final int queryId;
    private final PacketByteBuf response;

    public LoginQueryResponseC2SPacket(int queryId, @Nullable PacketByteBuf response) {
        this.queryId = queryId;
        this.response = response;
    }

    public LoginQueryResponseC2SPacket(PacketByteBuf buf) {
        this.queryId = buf.readVarInt();
        if (buf.readBoolean()) {
            int i = buf.readableBytes();
            if (i < 0 || i > 0x100000) {
                throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
            }
            this.response = new PacketByteBuf(buf.readBytes(i));
        } else {
            this.response = null;
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.queryId);
        if (this.response != null) {
            buf.writeBoolean(true);
            buf.writeBytes(this.response.copy());
        } else {
            buf.writeBoolean(false);
        }
    }

    @Override
    public void apply(ServerLoginPacketListener serverLoginPacketListener) {
        serverLoginPacketListener.onQueryResponse(this);
    }

    public int getQueryId() {
        return this.queryId;
    }

    public PacketByteBuf getResponse() {
        return this.response;
    }
}

