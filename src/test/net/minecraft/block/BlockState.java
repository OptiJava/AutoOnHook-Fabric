/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.state.property.Property;
import net.minecraft.util.registry.Registry;

public class BlockState
extends AbstractBlock.AbstractBlockState {
    public static final Codec<BlockState> CODEC = BlockState.createCodec(Registry.BLOCK, Block::getDefaultState).stable();

    public BlockState(Block block, ImmutableMap<Property<?>, Comparable<?>> immutableMap, MapCodec<BlockState> mapCodec) {
        super(block, immutableMap, mapCodec);
    }

    @Override
    protected BlockState asBlockState() {
        return this;
    }
}
