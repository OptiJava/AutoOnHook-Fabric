/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.command.argument.serialize;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.network.PacketByteBuf;

/**
 * Serializes an argument type to be sent to the client.
 */
public interface ArgumentSerializer<T extends ArgumentType<?>> {
    public void toPacket(T var1, PacketByteBuf var2);

    public T fromPacket(PacketByteBuf var1);

    public void toJson(T var1, JsonObject var2);
}

