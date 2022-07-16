/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.command.argument.serialize;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.argument.BrigadierArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;

public class IntegerArgumentSerializer
implements ArgumentSerializer<IntegerArgumentType> {
    @Override
    public void toPacket(IntegerArgumentType integerArgumentType, PacketByteBuf packetByteBuf) {
        boolean bl = integerArgumentType.getMinimum() != Integer.MIN_VALUE;
        boolean bl2 = integerArgumentType.getMaximum() != Integer.MAX_VALUE;
        packetByteBuf.writeByte(BrigadierArgumentTypes.createFlag(bl, bl2));
        if (bl) {
            packetByteBuf.writeInt(integerArgumentType.getMinimum());
        }
        if (bl2) {
            packetByteBuf.writeInt(integerArgumentType.getMaximum());
        }
    }

    @Override
    public IntegerArgumentType fromPacket(PacketByteBuf packetByteBuf) {
        byte b = packetByteBuf.readByte();
        int i = BrigadierArgumentTypes.hasMin(b) ? packetByteBuf.readInt() : Integer.MIN_VALUE;
        int j = BrigadierArgumentTypes.hasMax(b) ? packetByteBuf.readInt() : Integer.MAX_VALUE;
        return IntegerArgumentType.integer(i, j);
    }

    @Override
    public void toJson(IntegerArgumentType integerArgumentType, JsonObject jsonObject) {
        if (integerArgumentType.getMinimum() != Integer.MIN_VALUE) {
            jsonObject.addProperty("min", integerArgumentType.getMinimum());
        }
        if (integerArgumentType.getMaximum() != Integer.MAX_VALUE) {
            jsonObject.addProperty("max", integerArgumentType.getMaximum());
        }
    }

    @Override
    public /* synthetic */ ArgumentType fromPacket(PacketByteBuf buf) {
        return this.fromPacket(buf);
    }
}

