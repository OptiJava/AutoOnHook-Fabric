/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.network.packet.s2c.play;

import java.util.Objects;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.jetbrains.annotations.Nullable;

public class ScoreboardDisplayS2CPacket
implements Packet<ClientPlayPacketListener> {
    private final int slot;
    private final String name;

    public ScoreboardDisplayS2CPacket(int slot, @Nullable ScoreboardObjective objective) {
        this.slot = slot;
        this.name = objective == null ? "" : objective.getName();
    }

    public ScoreboardDisplayS2CPacket(PacketByteBuf buf) {
        this.slot = buf.readByte();
        this.name = buf.readString(16);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeByte(this.slot);
        buf.writeString(this.name);
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onScoreboardDisplay(this);
    }

    public int getSlot() {
        return this.slot;
    }

    @Nullable
    public String getName() {
        return Objects.equals(this.name, "") ? null : this.name;
    }
}

