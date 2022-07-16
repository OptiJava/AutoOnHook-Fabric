/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.FeatureConfig;

public class SpringFeatureConfig
implements FeatureConfig {
    public static final Codec<SpringFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)FluidState.CODEC.fieldOf("state")).forGetter(springFeatureConfig -> springFeatureConfig.state), ((MapCodec)Codec.BOOL.fieldOf("requires_block_below")).orElse(true).forGetter(springFeatureConfig -> springFeatureConfig.requiresBlockBelow), ((MapCodec)Codec.INT.fieldOf("rock_count")).orElse(4).forGetter(springFeatureConfig -> springFeatureConfig.rockCount), ((MapCodec)Codec.INT.fieldOf("hole_count")).orElse(1).forGetter(springFeatureConfig -> springFeatureConfig.holeCount), ((MapCodec)Registry.BLOCK.listOf().fieldOf("valid_blocks")).xmap(ImmutableSet::copyOf, ImmutableList::copyOf).forGetter(springFeatureConfig -> springFeatureConfig.validBlocks)).apply((Applicative<SpringFeatureConfig, ?>)instance, SpringFeatureConfig::new));
    public final FluidState state;
    public final boolean requiresBlockBelow;
    public final int rockCount;
    public final int holeCount;
    public final Set<Block> validBlocks;

    public SpringFeatureConfig(FluidState state, boolean requiresBlockBelow, int rockCount, int holeCount, Set<Block> validBlocks) {
        this.state = state;
        this.requiresBlockBelow = requiresBlockBelow;
        this.rockCount = rockCount;
        this.holeCount = holeCount;
        this.validBlocks = validBlocks;
    }
}

