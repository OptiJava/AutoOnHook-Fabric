/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleType;

public interface ParticleEffect {
    public ParticleType<?> getType();

    public void write(PacketByteBuf var1);

    public String asString();

    @Deprecated
    public static interface Factory<T extends ParticleEffect> {
        public T read(ParticleType<T> var1, StringReader var2) throws CommandSyntaxException;

        public T read(ParticleType<T> var1, PacketByteBuf var2);
    }
}

