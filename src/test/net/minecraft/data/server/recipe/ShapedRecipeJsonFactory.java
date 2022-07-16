/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.data.server.recipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonFactory;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class ShapedRecipeJsonFactory
implements CraftingRecipeJsonFactory {
    private final Item output;
    private final int outputCount;
    private final List<String> pattern = Lists.newArrayList();
    private final Map<Character, Ingredient> inputs = Maps.newLinkedHashMap();
    private final Advancement.Task builder = Advancement.Task.create();
    @Nullable
    private String group;

    public ShapedRecipeJsonFactory(ItemConvertible output, int outputCount) {
        this.output = output.asItem();
        this.outputCount = outputCount;
    }

    public static ShapedRecipeJsonFactory create(ItemConvertible output) {
        return ShapedRecipeJsonFactory.create(output, 1);
    }

    public static ShapedRecipeJsonFactory create(ItemConvertible output, int outputCount) {
        return new ShapedRecipeJsonFactory(output, outputCount);
    }

    public ShapedRecipeJsonFactory input(Character c, Tag<Item> tag) {
        return this.input(c, Ingredient.fromTag(tag));
    }

    public ShapedRecipeJsonFactory input(Character c, ItemConvertible itemProvider) {
        return this.input(c, Ingredient.ofItems(itemProvider));
    }

    public ShapedRecipeJsonFactory input(Character c, Ingredient ingredient) {
        if (this.inputs.containsKey(c)) {
            throw new IllegalArgumentException("Symbol '" + c + "' is already defined!");
        }
        if (c.charValue() == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        }
        this.inputs.put(c, ingredient);
        return this;
    }

    public ShapedRecipeJsonFactory pattern(String patternStr) {
        if (!this.pattern.isEmpty() && patternStr.length() != this.pattern.get(0).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        }
        this.pattern.add(patternStr);
        return this;
    }

    @Override
    public ShapedRecipeJsonFactory criterion(String string, CriterionConditions criterionConditions) {
        this.builder.criterion(string, criterionConditions);
        return this;
    }

    @Override
    public ShapedRecipeJsonFactory group(@Nullable String string) {
        this.group = string;
        return this;
    }

    @Override
    public Item getOutputItem() {
        return this.output;
    }

    @Override
    public void offerTo(Consumer<RecipeJsonProvider> exporter, Identifier recipeId) {
        this.validate(recipeId);
        this.builder.parent(new Identifier("recipes/root")).criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).criteriaMerger(CriterionMerger.OR);
        exporter.accept(new ShapedRecipeJsonProvider(recipeId, this.output, this.outputCount, this.group == null ? "" : this.group, this.pattern, this.inputs, this.builder, new Identifier(recipeId.getNamespace(), "recipes/" + this.output.getGroup().getName() + "/" + recipeId.getPath())));
    }

    private void validate(Identifier recipeId) {
        if (this.pattern.isEmpty()) {
            throw new IllegalStateException("No pattern is defined for shaped recipe " + recipeId + "!");
        }
        HashSet<Character> set = Sets.newHashSet(this.inputs.keySet());
        set.remove(Character.valueOf(' '));
        for (String string : this.pattern) {
            for (int i = 0; i < string.length(); ++i) {
                char c = string.charAt(i);
                if (!this.inputs.containsKey(Character.valueOf(c)) && c != ' ') {
                    throw new IllegalStateException("Pattern in recipe " + recipeId + " uses undefined symbol '" + c + "'");
                }
                set.remove(Character.valueOf(c));
            }
        }
        if (!set.isEmpty()) {
            throw new IllegalStateException("Ingredients are defined but not used in pattern for recipe " + recipeId);
        }
        if (this.pattern.size() == 1 && this.pattern.get(0).length() == 1) {
            throw new IllegalStateException("Shaped recipe " + recipeId + " only takes in a single item - should it be a shapeless recipe instead?");
        }
        if (this.builder.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeId);
        }
    }

    @Override
    public /* synthetic */ CraftingRecipeJsonFactory group(@Nullable String group) {
        return this.group(group);
    }

    @Override
    public /* synthetic */ CraftingRecipeJsonFactory criterion(String name, CriterionConditions conditions) {
        return this.criterion(name, conditions);
    }

    static class ShapedRecipeJsonProvider
    implements RecipeJsonProvider {
        private final Identifier recipeId;
        private final Item output;
        private final int resultCount;
        private final String group;
        private final List<String> pattern;
        private final Map<Character, Ingredient> inputs;
        private final Advancement.Task builder;
        private final Identifier advancementId;

        public ShapedRecipeJsonProvider(Identifier recipeId, Item output, int resultCount, String group, List<String> pattern, Map<Character, Ingredient> inputs, Advancement.Task builder, Identifier advancementId) {
            this.recipeId = recipeId;
            this.output = output;
            this.resultCount = resultCount;
            this.group = group;
            this.pattern = pattern;
            this.inputs = inputs;
            this.builder = builder;
            this.advancementId = advancementId;
        }

        @Override
        public void serialize(JsonObject json) {
            if (!this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }
            JsonArray jsonArray = new JsonArray();
            for (String string : this.pattern) {
                jsonArray.add(string);
            }
            json.add("pattern", jsonArray);
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<Character, Ingredient> entry : this.inputs.entrySet()) {
                jsonObject.add(String.valueOf(entry.getKey()), entry.getValue().toJson());
            }
            json.add("key", jsonObject);
            JsonObject jsonObject2 = new JsonObject();
            jsonObject2.addProperty("item", Registry.ITEM.getId(this.output).toString());
            if (this.resultCount > 1) {
                jsonObject2.addProperty("count", this.resultCount);
            }
            json.add("result", jsonObject2);
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return RecipeSerializer.SHAPED;
        }

        @Override
        public Identifier getRecipeId() {
            return this.recipeId;
        }

        @Override
        @Nullable
        public JsonObject toAdvancementJson() {
            return this.builder.toJson();
        }

        @Override
        @Nullable
        public Identifier getAdvancementId() {
            return this.advancementId;
        }
    }
}

