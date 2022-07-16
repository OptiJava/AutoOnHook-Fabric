/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.stateprovider;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.stateprovider.BlockStateProviderType;

public abstract class BlockStateProvider {
    public static final Codec<BlockStateProvider> TYPE_CODEC = Registry.BLOCK_STATE_PROVIDER_TYPE.dispatch(BlockStateProvider::getType, BlockStateProviderType::getCodec);

    protected abstract BlockStateProviderType<?> getType();

    public abstract BlockState getBlockState(Random var1, BlockPos var2);
}

