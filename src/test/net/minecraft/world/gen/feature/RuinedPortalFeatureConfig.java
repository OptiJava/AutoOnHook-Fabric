/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.RuinedPortalFeature;

public class RuinedPortalFeatureConfig
implements FeatureConfig {
    public static final Codec<RuinedPortalFeatureConfig> CODEC = ((MapCodec)RuinedPortalFeature.Type.CODEC.fieldOf("portal_type")).xmap(RuinedPortalFeatureConfig::new, ruinedPortalFeatureConfig -> ruinedPortalFeatureConfig.portalType).codec();
    public final RuinedPortalFeature.Type portalType;

    public RuinedPortalFeatureConfig(RuinedPortalFeature.Type portalType) {
        this.portalType = portalType;
    }
}

