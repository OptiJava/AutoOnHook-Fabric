/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.chunk;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.Codecs;

public class SlideConfig {
    public static final Codec<SlideConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("target")).forGetter(SlideConfig::getTarget), ((MapCodec)Codecs.NONNEGATIVE_INT.fieldOf("size")).forGetter(SlideConfig::getSize), ((MapCodec)Codec.INT.fieldOf("offset")).forGetter(SlideConfig::getOffset)).apply((Applicative<SlideConfig, ?>)instance, SlideConfig::new));
    private final int target;
    private final int size;
    private final int offset;

    public SlideConfig(int target, int size, int offset) {
        this.target = target;
        this.size = size;
        this.offset = offset;
    }

    public int getTarget() {
        return this.target;
    }

    public int getSize() {
        return this.size;
    }

    public int getOffset() {
        return this.offset;
    }
}

