/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.chunk;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class StrongholdConfig {
    public static final Codec<StrongholdConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.intRange(0, 1023).fieldOf("distance")).forGetter(StrongholdConfig::getDistance), ((MapCodec)Codec.intRange(0, 1023).fieldOf("spread")).forGetter(StrongholdConfig::getSpread), ((MapCodec)Codec.intRange(1, 4095).fieldOf("count")).forGetter(StrongholdConfig::getCount)).apply((Applicative<StrongholdConfig, ?>)instance, StrongholdConfig::new));
    private final int distance;
    private final int spread;
    private final int count;

    public StrongholdConfig(int distance, int spread, int count) {
        this.distance = distance;
        this.spread = spread;
        this.count = count;
    }

    public int getDistance() {
        return this.distance;
    }

    public int getSpread() {
        return this.spread;
    }

    public int getCount() {
        return this.count;
    }
}

