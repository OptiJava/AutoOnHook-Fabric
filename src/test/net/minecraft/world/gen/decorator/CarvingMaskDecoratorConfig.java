/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.decorator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.decorator.DecoratorConfig;

public class CarvingMaskDecoratorConfig
implements DecoratorConfig {
    public static final Codec<CarvingMaskDecoratorConfig> CODEC = ((MapCodec)GenerationStep.Carver.CODEC.fieldOf("step")).xmap(CarvingMaskDecoratorConfig::new, config -> config.carver).codec();
    protected final GenerationStep.Carver carver;

    public CarvingMaskDecoratorConfig(GenerationStep.Carver carver) {
        this.carver = carver;
    }
}

