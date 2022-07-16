/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class BlockPileFeatureConfig
implements FeatureConfig {
    public static final Codec<BlockPileFeatureConfig> CODEC = ((MapCodec)BlockStateProvider.TYPE_CODEC.fieldOf("state_provider")).xmap(BlockPileFeatureConfig::new, blockPileFeatureConfig -> blockPileFeatureConfig.stateProvider).codec();
    public final BlockStateProvider stateProvider;

    public BlockPileFeatureConfig(BlockStateProvider stateProvider) {
        this.stateProvider = stateProvider;
    }
}

