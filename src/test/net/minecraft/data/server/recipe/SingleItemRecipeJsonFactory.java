/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.data.server.recipe;

import com.google.gson.JsonObject;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class SingleItemRecipeJsonFactory
implements CraftingRecipeJsonFactory {
    private final Item output;
    private final Ingredient input;
    private final int count;
    private final Advancement.Task builder = Advancement.Task.create();
    @Nullable
    private String group;
    private final RecipeSerializer<?> serializer;

    public SingleItemRecipeJsonFactory(RecipeSerializer<?> serializer, Ingredient input, ItemConvertible output, int outputCount) {
        this.serializer = serializer;
        this.output = output.asItem();
        this.input = input;
        this.count = outputCount;
    }

    public static SingleItemRecipeJsonFactory createStonecutting(Ingredient input, ItemConvertible output) {
        return new SingleItemRecipeJsonFactory(RecipeSerializer.STONECUTTING, input, output, 1);
    }

    public static SingleItemRecipeJsonFactory createStonecutting(Ingredient input, ItemConvertible output, int outputCount) {
        return new SingleItemRecipeJsonFactory(RecipeSerializer.STONECUTTING, input, output, outputCount);
    }

    @Override
    public SingleItemRecipeJsonFactory criterion(String string, CriterionConditions criterionConditions) {
        this.builder.criterion(string, criterionConditions);
        return this;
    }

    @Override
    public SingleItemRecipeJsonFactory group(@Nullable String string) {
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
        exporter.accept(new SingleItemRecipeJsonProvider(recipeId, this.serializer, this.group == null ? "" : this.group, this.input, this.output, this.count, this.builder, new Identifier(recipeId.getNamespace(), "recipes/" + this.output.getGroup().getName() + "/" + recipeId.getPath())));
    }

    private void validate(Identifier recipeId) {
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

    public static class SingleItemRecipeJsonProvider
    implements RecipeJsonProvider {
        private final Identifier recipeId;
        private final String group;
        private final Ingredient input;
        private final Item output;
        private final int count;
        private final Advancement.Task builder;
        private final Identifier advancementId;
        private final RecipeSerializer<?> serializer;

        public SingleItemRecipeJsonProvider(Identifier recipeId, RecipeSerializer<?> serializer, String group, Ingredient input, Item output, int outputCount, Advancement.Task builder, Identifier advancementId) {
            this.recipeId = recipeId;
            this.serializer = serializer;
            this.group = group;
            this.input = input;
            this.output = output;
            this.count = outputCount;
            this.builder = builder;
            this.advancementId = advancementId;
        }

        @Override
        public void serialize(JsonObject json) {
            if (!this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }
            json.add("ingredient", this.input.toJson());
            json.addProperty("result", Registry.ITEM.getId(this.output).toString());
            json.addProperty("count", this.count);
        }

        @Override
        public Identifier getRecipeId() {
            return this.recipeId;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return this.serializer;
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

