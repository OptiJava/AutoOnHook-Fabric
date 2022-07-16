/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.gen.feature.FeatureConfig;

public class ShipwreckFeatureConfig
implements FeatureConfig {
    public static final Codec<ShipwreckFeatureConfig> CODEC = ((MapCodec)Codec.BOOL.fieldOf("is_beached")).orElse(false).xmap(ShipwreckFeatureConfig::new, shipwreckFeatureConfig -> shipwreckFeatureConfig.isBeached).codec();
    public final boolean isBeached;

    public ShipwreckFeatureConfig(boolean isBeached) {
        this.isBeached = isBeached;
    }
}

