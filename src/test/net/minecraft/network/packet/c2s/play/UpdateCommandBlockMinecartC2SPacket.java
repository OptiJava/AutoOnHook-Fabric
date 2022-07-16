/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.network.packet.c2s.play;

import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class UpdateCommandBlockMinecartC2SPacket
implements Packet<ServerPlayPacketListener> {
    private final int entityId;
    private final String command;
    private final boolean trackOutput;

    public UpdateCommandBlockMinecartC2SPacket(int entityId, String command, boolean trackOutput) {
        this.entityId = entityId;
        this.command = command;
        this.trackOutput = trackOutput;
    }

    public UpdateCommandBlockMinecartC2SPacket(PacketByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.command = buf.readString();
        this.trackOutput = buf.readBoolean();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeString(this.command);
        buf.writeBoolean(this.trackOutput);
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onUpdateCommandBlockMinecart(this);
    }

    @Nullable
    public CommandBlockExecutor getMinecartCommandExecutor(World world) {
        Entity entity = world.getEntityById(this.entityId);
        if (entity instanceof CommandBlockMinecartEntity) {
            return ((CommandBlockMinecartEntity)entity).getCommandExecutor();
        }
        return null;
    }

    public String getCommand() {
        return this.command;
    }

    public boolean shouldTrackOutput() {
        return this.trackOutput;
    }
}

