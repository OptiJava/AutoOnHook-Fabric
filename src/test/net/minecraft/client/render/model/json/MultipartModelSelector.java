/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.model.json;

import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface MultipartModelSelector {
    public static final MultipartModelSelector TRUE = stateManager -> blockState -> true;
    public static final MultipartModelSelector FALSE = stateManager -> blockState -> false;

    public Predicate<BlockState> getPredicate(StateManager<Block, BlockState> var1);
}

