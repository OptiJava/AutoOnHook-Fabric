/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;

public class UpdateSelectedSlotS2CPacket
implements Packet<ClientPlayPacketListener> {
    private final int selectedSlot;

    public UpdateSelectedSlotS2CPacket(int slot) {
        this.selectedSlot = slot;
    }

    public UpdateSelectedSlotS2CPacket(PacketByteBuf buf) {
        this.selectedSlot = buf.readByte();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeByte(this.selectedSlot);
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onHeldItemChange(this);
    }

    public int getSlot() {
        return this.selectedSlot;
    }
}

