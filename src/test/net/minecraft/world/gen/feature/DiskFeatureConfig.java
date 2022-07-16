/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.feature.FeatureConfig;

public class DiskFeatureConfig
implements FeatureConfig {
    public static final Codec<DiskFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockState.CODEC.fieldOf("state")).forGetter(diskFeatureConfig -> diskFeatureConfig.state), ((MapCodec)IntProvider.createValidatingCodec(0, 8).fieldOf("radius")).forGetter(diskFeatureConfig -> diskFeatureConfig.radius), ((MapCodec)Codec.intRange(0, 4).fieldOf("half_height")).forGetter(diskFeatureConfig -> diskFeatureConfig.halfHeight), ((MapCodec)BlockState.CODEC.listOf().fieldOf("targets")).forGetter(diskFeatureConfig -> diskFeatureConfig.targets)).apply((Applicative<DiskFeatureConfig, ?>)instance, DiskFeatureConfig::new));
    public final BlockState state;
    public final IntProvider radius;
    public final int halfHeight;
    public final List<BlockState> targets;

    public DiskFeatureConfig(BlockState state, IntProvider radius, int halfHeight, List<BlockState> targets) {
        this.state = state;
        this.radius = radius;
        this.halfHeight = halfHeight;
        this.targets = targets;
    }
}
