/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.decorator;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.decorator.DecoratorConfig;

public class NopeDecoratorConfig
implements DecoratorConfig {
    public static final Codec<NopeDecoratorConfig> CODEC = Codec.unit(() -> INSTANCE);
    public static final NopeDecoratorConfig INSTANCE = new NopeDecoratorConfig();
}

