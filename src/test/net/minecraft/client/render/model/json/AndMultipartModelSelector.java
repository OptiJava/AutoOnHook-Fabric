/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.model.json;

import com.google.common.collect.Streams;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.state.StateManager;

@Environment(value=EnvType.CLIENT)
public class AndMultipartModelSelector
implements MultipartModelSelector {
    public static final String KEY = "AND";
    private final Iterable<? extends MultipartModelSelector> selectors;

    public AndMultipartModelSelector(Iterable<? extends MultipartModelSelector> selectors) {
        this.selectors = selectors;
    }

    @Override
    public Predicate<BlockState> getPredicate(StateManager<Block, BlockState> stateManager) {
        List list = Streams.stream(this.selectors).map(multipartModelSelector -> multipartModelSelector.getPredicate(stateManager)).collect(Collectors.toList());
        return blockState -> list.stream().allMatch(predicate -> predicate.test(blockState));
    }
}

