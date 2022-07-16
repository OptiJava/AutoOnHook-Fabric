/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.model.json;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;

@Environment(value=EnvType.CLIENT)
public class SimpleMultipartModelSelector
implements MultipartModelSelector {
    private static final Splitter VALUE_SPLITTER = Splitter.on('|').omitEmptyStrings();
    private final String key;
    private final String valueString;

    public SimpleMultipartModelSelector(String key, String valueString) {
        this.key = key;
        this.valueString = valueString;
    }

    @Override
    public Predicate<BlockState> getPredicate(StateManager<Block, BlockState> stateManager) {
        Predicate<BlockState> predicate;
        List<String> list;
        boolean bl;
        Property<?> property = stateManager.getProperty(this.key);
        if (property == null) {
            throw new RuntimeException(String.format("Unknown property '%s' on '%s'", this.key, stateManager.getOwner()));
        }
        String string2 = this.valueString;
        boolean bl2 = bl = !string2.isEmpty() && string2.charAt(0) == '!';
        if (bl) {
            string2 = string2.substring(1);
        }
        if ((list = VALUE_SPLITTER.splitToList(string2)).isEmpty()) {
            throw new RuntimeException(String.format("Empty value '%s' for property '%s' on '%s'", this.valueString, this.key, stateManager.getOwner()));
        }
        if (list.size() == 1) {
            predicate = this.createPredicate(stateManager, property, string2);
        } else {
            List list2 = list.stream().map(string -> this.createPredicate(stateManager, property, (String)string)).collect(Collectors.toList());
            predicate = blockState -> list2.stream().anyMatch(predicate -> predicate.test(blockState));
        }
        return bl ? predicate.negate() : predicate;
    }

    private Predicate<BlockState> createPredicate(StateManager<Block, BlockState> stateFactory, Property<?> property, String valueString) {
        Optional<?> optional = property.parse(valueString);
        if (!optional.isPresent()) {
            throw new RuntimeException(String.format("Unknown value '%s' for property '%s' on '%s' in '%s'", valueString, this.key, stateFactory.getOwner(), this.valueString));
        }
        return blockState -> blockState.get(property).equals(optional.get());
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("key", this.key).add("value", this.valueString).toString();
    }
}

