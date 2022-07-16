/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.data.client.model;

import com.google.gson.JsonElement;
import java.util.function.Supplier;
import net.minecraft.block.Block;

/**
 * A supplier of a block state JSON definition.
 */
public interface BlockStateSupplier
extends Supplier<JsonElement> {
    public Block getBlock();
}

