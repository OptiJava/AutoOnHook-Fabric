/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.command.argument.serialize;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;

public class StringArgumentSerializer
implements ArgumentSerializer<StringArgumentType> {
    @Override
    public void toPacket(StringArgumentType stringArgumentType, PacketByteBuf packetByteBuf) {
        packetByteBuf.writeEnumConstant(stringArgumentType.getType());
    }

    @Override
    public StringArgumentType fromPacket(PacketByteBuf packetByteBuf) {
        StringArgumentType.StringType stringType = packetByteBuf.readEnumConstant(StringArgumentType.StringType.class);
        switch (stringType) {
            case SINGLE_WORD: {
                return StringArgumentType.word();
            }
            case QUOTABLE_PHRASE: {
                return StringArgumentType.string();
            }
        }
        return StringArgumentType.greedyString();
    }

    @Override
    public void toJson(StringArgumentType stringArgumentType, JsonObject jsonObject) {
        switch (stringArgumentType.getType()) {
            case SINGLE_WORD: {
                jsonObject.addProperty("type", "word");
                break;
            }
            case QUOTABLE_PHRASE: {
                jsonObject.addProperty("type", "phrase");
                break;
            }
            default: {
                jsonObject.addProperty("type", "greedy");
            }
        }
    }

    @Override
    public /* synthetic */ ArgumentType fromPacket(PacketByteBuf buf) {
        return this.fromPacket(buf);
    }
}

