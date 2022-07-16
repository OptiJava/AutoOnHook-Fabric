/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.decorator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.gen.decorator.DecoratorConfig;

public class ChanceDecoratorConfig
implements DecoratorConfig {
    public static final Codec<ChanceDecoratorConfig> CODEC = ((MapCodec)Codec.INT.fieldOf("chance")).xmap(ChanceDecoratorConfig::new, chanceDecoratorConfig -> chanceDecoratorConfig.chance).codec();
    public final int chance;

    public ChanceDecoratorConfig(int chance) {
        this.chance = chance;
    }
}

