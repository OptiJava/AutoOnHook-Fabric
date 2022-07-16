/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.world.Vibration;

public class VibrationS2CPacket
implements Packet<ClientPlayPacketListener> {
    private final Vibration vibration;

    public VibrationS2CPacket(Vibration vibration) {
        this.vibration = vibration;
    }

    public VibrationS2CPacket(PacketByteBuf buf) {
        this.vibration = Vibration.readFromBuf(buf);
    }

    @Override
    public void write(PacketByteBuf buf) {
        Vibration.writeToBuf(buf, this.vibration);
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onVibration(this);
    }

    public Vibration getVibration() {
        return this.vibration;
    }
}

