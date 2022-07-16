/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.advancement;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class AdvancementRewards {
    public static final AdvancementRewards NONE = new AdvancementRewards(0, new Identifier[0], new Identifier[0], CommandFunction.LazyContainer.EMPTY);
    private final int experience;
    private final Identifier[] loot;
    private final Identifier[] recipes;
    private final CommandFunction.LazyContainer function;

    public AdvancementRewards(int experience, Identifier[] loot, Identifier[] recipes, CommandFunction.LazyContainer function) {
        this.experience = experience;
        this.loot = loot;
        this.recipes = recipes;
        this.function = function;
    }

    public Identifier[] getRecipes() {
        return this.recipes;
    }

    public void apply(ServerPlayerEntity player) {
        player.addExperience(this.experience);
        LootContext lootContext = new LootContext.Builder(player.getServerWorld()).parameter(LootContextParameters.THIS_ENTITY, player).parameter(LootContextParameters.ORIGIN, player.getPos()).random(player.getRandom()).build(LootContextTypes.ADVANCEMENT_REWARD);
        boolean bl = false;
        for (Identifier identifier : this.loot) {
            for (ItemStack itemStack : player.server.getLootManager().getTable(identifier).generateLoot(lootContext)) {
                if (player.giveItemStack(itemStack)) {
                    player.world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
                    bl = true;
                    continue;
                }
                ItemEntity itemEntity = player.dropItem(itemStack, false);
                if (itemEntity == null) continue;
                itemEntity.resetPickupDelay();
                itemEntity.setOwner(player.getUuid());
            }
        }
        if (bl) {
            player.currentScreenHandler.sendContentUpdates();
        }
        if (this.recipes.length > 0) {
            player.unlockRecipes(this.recipes);
        }
        MinecraftServer minecraftServer = player.server;
        this.function.get(minecraftServer.getCommandFunctionManager()).ifPresent(commandFunction -> minecraftServer.getCommandFunctionManager().execute((CommandFunction)commandFunction, player.getCommandSource().withSilent().withLevel(2)));
    }

    public String toString() {
        return "AdvancementRewards{experience=" + this.experience + ", loot=" + Arrays.toString(this.loot) + ", recipes=" + Arrays.toString(this.recipes) + ", function=" + this.function + "}";
    }

    public JsonElement toJson() {
        JsonArray jsonArray;
        if (this == NONE) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        if (this.experience != 0) {
            jsonObject.addProperty("experience", this.experience);
        }
        if (this.loot.length > 0) {
            jsonArray = new JsonArray();
            for (Identifier identifier : this.loot) {
                jsonArray.add(identifier.toString());
            }
            jsonObject.add("loot", jsonArray);
        }
        if (this.recipes.length > 0) {
            jsonArray = new JsonArray();
            for (Identifier identifier : this.recipes) {
                jsonArray.add(identifier.toString());
            }
            jsonObject.add("recipes", jsonArray);
        }
        if (this.function.getId() != null) {
            jsonObject.addProperty("function", this.function.getId().toString());
        }
        return jsonObject;
    }

    public static AdvancementRewards fromJson(JsonObject json) throws JsonParseException {
        int i = JsonHelper.getInt(json, "experience", 0);
        JsonArray jsonArray = JsonHelper.getArray(json, "loot", new JsonArray());
        Identifier[] identifiers = new Identifier[jsonArray.size()];
        for (int j = 0; j < identifiers.length; ++j) {
            identifiers[j] = new Identifier(JsonHelper.asString(jsonArray.get(j), "loot[" + j + "]"));
        }
        JsonArray j = JsonHelper.getArray(json, "recipes", new JsonArray());
        Identifier[] identifiers2 = new Identifier[j.size()];
        for (int k = 0; k < identifiers2.length; ++k) {
            identifiers2[k] = new Identifier(JsonHelper.asString(j.get(k), "recipes[" + k + "]"));
        }
        CommandFunction.LazyContainer k = json.has("function") ? new CommandFunction.LazyContainer(new Identifier(JsonHelper.getString(json, "function"))) : CommandFunction.LazyContainer.EMPTY;
        return new AdvancementRewards(i, identifiers, identifiers2, k);
    }

    public static class Builder {
        private int experience;
        private final List<Identifier> loot = Lists.newArrayList();
        private final List<Identifier> recipes = Lists.newArrayList();
        @Nullable
        private Identifier function;

        public static Builder experience(int experience) {
            return new Builder().setExperience(experience);
        }

        public Builder setExperience(int experience) {
            this.experience += experience;
            return this;
        }

        public static Builder loot(Identifier loot) {
            return new Builder().addLoot(loot);
        }

        public Builder addLoot(Identifier loot) {
            this.loot.add(loot);
            return this;
        }

        public static Builder recipe(Identifier recipe) {
            return new Builder().addRecipe(recipe);
        }

        public Builder addRecipe(Identifier recipe) {
            this.recipes.add(recipe);
            return this;
        }

        public static Builder function(Identifier function) {
            return new Builder().setFunction(function);
        }

        public Builder setFunction(Identifier function) {
            this.function = function;
            return this;
        }

        public AdvancementRewards build() {
            return new AdvancementRewards(this.experience, this.loot.toArray(new Identifier[0]), this.recipes.toArray(new Identifier[0]), this.function == null ? CommandFunction.LazyContainer.EMPTY : new CommandFunction.LazyContainer(this.function));
        }
    }
}

