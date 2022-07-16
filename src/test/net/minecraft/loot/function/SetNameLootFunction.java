/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Set;
import java.util.function.UnaryOperator;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.JsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class SetNameLootFunction
extends ConditionalLootFunction {
    private static final Logger LOGGER = LogManager.getLogger();
    final Text name;
    @Nullable
    final LootContext.EntityTarget entity;

    SetNameLootFunction(LootCondition[] lootConditions, @Nullable Text text, @Nullable LootContext.EntityTarget entityTarget) {
        super(lootConditions);
        this.name = text;
        this.entity = entityTarget;
    }

    @Override
    public LootFunctionType getType() {
        return LootFunctionTypes.SET_NAME;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return this.entity != null ? ImmutableSet.of(this.entity.getParameter()) : ImmutableSet.of();
    }

    public static UnaryOperator<Text> applySourceEntity(LootContext context, @Nullable LootContext.EntityTarget sourceEntity) {
        Entity entity;
        if (sourceEntity != null && (entity = context.get(sourceEntity.getParameter())) != null) {
            ServerCommandSource serverCommandSource = entity.getCommandSource().withLevel(2);
            return textComponent -> {
                try {
                    return Texts.parse(serverCommandSource, textComponent, entity, 0);
                }
                catch (CommandSyntaxException commandSyntaxException) {
                    LOGGER.warn("Failed to resolve text component", (Throwable)commandSyntaxException);
                    return textComponent;
                }
            };
        }
        return textComponent -> textComponent;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        if (this.name != null) {
            stack.setCustomName((Text)SetNameLootFunction.applySourceEntity(context, this.entity).apply(this.name));
        }
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder(Text name) {
        return SetNameLootFunction.builder((LootCondition[] conditions) -> new SetNameLootFunction((LootCondition[])conditions, name, null));
    }

    public static ConditionalLootFunction.Builder<?> builder(Text name, LootContext.EntityTarget target) {
        return SetNameLootFunction.builder((LootCondition[] conditions) -> new SetNameLootFunction((LootCondition[])conditions, name, target));
    }

    public static class Serializer
    extends ConditionalLootFunction.Serializer<SetNameLootFunction> {
        @Override
        public void toJson(JsonObject jsonObject, SetNameLootFunction setNameLootFunction, JsonSerializationContext jsonSerializationContext) {
            super.toJson(jsonObject, setNameLootFunction, jsonSerializationContext);
            if (setNameLootFunction.name != null) {
                jsonObject.add("name", Text.Serializer.toJsonTree(setNameLootFunction.name));
            }
            if (setNameLootFunction.entity != null) {
                jsonObject.add("entity", jsonSerializationContext.serialize((Object)setNameLootFunction.entity));
            }
        }

        @Override
        public SetNameLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {
            MutableText text = Text.Serializer.fromJson(jsonObject.get("name"));
            LootContext.EntityTarget entityTarget = JsonHelper.deserialize(jsonObject, "entity", null, jsonDeserializationContext, LootContext.EntityTarget.class);
            return new SetNameLootFunction(lootConditions, text, entityTarget);
        }

        @Override
        public /* synthetic */ ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return this.fromJson(json, context, conditions);
        }
    }
}

