/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RemoveEntityStatusEffectS2CPacket
implements Packet<ClientPlayPacketListener> {
    private final int entityId;
    private final StatusEffect effectType;

    public RemoveEntityStatusEffectS2CPacket(int entityId, StatusEffect effectType) {
        this.entityId = entityId;
        this.effectType = effectType;
    }

    public RemoveEntityStatusEffectS2CPacket(PacketByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.effectType = StatusEffect.byRawId(buf.readUnsignedByte());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeByte(StatusEffect.getRawId(this.effectType));
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onRemoveEntityEffect(this);
    }

    @Nullable
    public Entity getEntity(World world) {
        return world.getEntityById(this.entityId);
    }

    @Nullable
    public StatusEffect getEffectType() {
        return this.effectType;
    }
}

