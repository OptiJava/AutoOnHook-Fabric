/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SetCameraEntityS2CPacket
implements Packet<ClientPlayPacketListener> {
    private final int entityId;

    public SetCameraEntityS2CPacket(Entity entity) {
        this.entityId = entity.getId();
    }

    public SetCameraEntityS2CPacket(PacketByteBuf buf) {
        this.entityId = buf.readVarInt();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.entityId);
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onSetCameraEntity(this);
    }

    @Nullable
    public Entity getEntity(World world) {
        return world.getEntityById(this.entityId);
    }
}

