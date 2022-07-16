/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.VerticalSurfaceType;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class VegetationPatchFeatureConfig
implements FeatureConfig {
    public static final Codec<VegetationPatchFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("replaceable")).forGetter(vegetationPatchFeatureConfig -> vegetationPatchFeatureConfig.replaceable), ((MapCodec)BlockStateProvider.TYPE_CODEC.fieldOf("ground_state")).forGetter(vegetationPatchFeatureConfig -> vegetationPatchFeatureConfig.groundState), ((MapCodec)ConfiguredFeature.REGISTRY_CODEC.fieldOf("vegetation_feature")).forGetter(vegetationPatchFeatureConfig -> vegetationPatchFeatureConfig.vegetationFeature), ((MapCodec)VerticalSurfaceType.CODEC.fieldOf("surface")).forGetter(vegetationPatchFeatureConfig -> vegetationPatchFeatureConfig.surface), ((MapCodec)IntProvider.createValidatingCodec(1, 128).fieldOf("depth")).forGetter(vegetationPatchFeatureConfig -> vegetationPatchFeatureConfig.depth), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("extra_bottom_block_chance")).forGetter(vegetationPatchFeatureConfig -> Float.valueOf(vegetationPatchFeatureConfig.extraBottomBlockChance)), ((MapCodec)Codec.intRange(1, 256).fieldOf("vertical_range")).forGetter(vegetationPatchFeatureConfig -> vegetationPatchFeatureConfig.verticalRange), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("vegetation_chance")).forGetter(vegetationPatchFeatureConfig -> Float.valueOf(vegetationPatchFeatureConfig.vegetationChance)), ((MapCodec)IntProvider.VALUE_CODEC.fieldOf("xz_radius")).forGetter(vegetationPatchFeatureConfig -> vegetationPatchFeatureConfig.horizontalRadius), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("extra_edge_column_chance")).forGetter(vegetationPatchFeatureConfig -> Float.valueOf(vegetationPatchFeatureConfig.extraEdgeColumnChance))).apply((Applicative<VegetationPatchFeatureConfig, ?>)instance, VegetationPatchFeatureConfig::new));
    public final Identifier replaceable;
    public final BlockStateProvider groundState;
    public final Supplier<ConfiguredFeature<?, ?>> vegetationFeature;
    public final VerticalSurfaceType surface;
    public final IntProvider depth;
    public final float extraBottomBlockChance;
    public final int verticalRange;
    public final float vegetationChance;
    public final IntProvider horizontalRadius;
    public final float extraEdgeColumnChance;

    public VegetationPatchFeatureConfig(Identifier replaceable, BlockStateProvider groundState, Supplier<ConfiguredFeature<?, ?>> vegetationFeature, VerticalSurfaceType surface, IntProvider depth, float extraBottomBlockChance, int verticalRange, float vegetationChance, IntProvider horizontalRadius, float extraEdgeColumnChance) {
        this.replaceable = replaceable;
        this.groundState = groundState;
        this.vegetationFeature = vegetationFeature;
        this.surface = surface;
        this.depth = depth;
        this.extraBottomBlockChance = extraBottomBlockChance;
        this.verticalRange = verticalRange;
        this.vegetationChance = vegetationChance;
        this.horizontalRadius = horizontalRadius;
        this.extraEdgeColumnChance = extraEdgeColumnChance;
    }
}

