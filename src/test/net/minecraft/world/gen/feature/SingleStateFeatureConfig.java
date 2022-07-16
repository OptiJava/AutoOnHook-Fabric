/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.feature.FeatureConfig;

public class SingleStateFeatureConfig
implements FeatureConfig {
    public static final Codec<SingleStateFeatureConfig> CODEC = ((MapCodec)BlockState.CODEC.fieldOf("state")).xmap(SingleStateFeatureConfig::new, singleStateFeatureConfig -> singleStateFeatureConfig.state).codec();
    public final BlockState state;

    public SingleStateFeatureConfig(BlockState state) {
        this.state = state;
    }
}

