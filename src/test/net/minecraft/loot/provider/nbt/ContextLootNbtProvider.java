/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.loot.provider.nbt;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.provider.nbt.LootNbtProvider;
import net.minecraft.loot.provider.nbt.LootNbtProviderType;
import net.minecraft.loot.provider.nbt.LootNbtProviderTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.JsonSerializing;
import org.jetbrains.annotations.Nullable;

public class ContextLootNbtProvider
implements LootNbtProvider {
    private static final String BLOCK_ENTITY_TARGET_NAME = "block_entity";
    private static final Target BLOCK_ENTITY_TARGET = new Target(){

        @Override
        public NbtElement getNbt(LootContext context) {
            BlockEntity blockEntity = context.get(LootContextParameters.BLOCK_ENTITY);
            return blockEntity != null ? blockEntity.writeNbt(new NbtCompound()) : null;
        }

        @Override
        public String getName() {
            return ContextLootNbtProvider.BLOCK_ENTITY_TARGET_NAME;
        }

        @Override
        public Set<LootContextParameter<?>> getRequiredParameters() {
            return ImmutableSet.of(LootContextParameters.BLOCK_ENTITY);
        }
    };
    public static final ContextLootNbtProvider BLOCK_ENTITY = new ContextLootNbtProvider(BLOCK_ENTITY_TARGET);
    final Target target;

    private static Target getTarget(final LootContext.EntityTarget entityTarget) {
        return new Target(){

            @Override
            @Nullable
            public NbtElement getNbt(LootContext context) {
                Entity entity = context.get(entityTarget.getParameter());
                return entity != null ? NbtPredicate.entityToNbt(entity) : null;
            }

            @Override
            public String getName() {
                return entityTarget.name();
            }

            @Override
            public Set<LootContextParameter<?>> getRequiredParameters() {
                return ImmutableSet.of(entityTarget.getParameter());
            }
        };
    }

    private ContextLootNbtProvider(Target target) {
        this.target = target;
    }

    @Override
    public LootNbtProviderType getType() {
        return LootNbtProviderTypes.CONTEXT;
    }

    @Override
    @Nullable
    public NbtElement getNbt(LootContext context) {
        return this.target.getNbt(context);
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return this.target.getRequiredParameters();
    }

    public static LootNbtProvider fromTarget(LootContext.EntityTarget target) {
        return new ContextLootNbtProvider(ContextLootNbtProvider.getTarget(target));
    }

    static ContextLootNbtProvider fromTarget(String target) {
        if (target.equals(BLOCK_ENTITY_TARGET_NAME)) {
            return new ContextLootNbtProvider(BLOCK_ENTITY_TARGET);
        }
        LootContext.EntityTarget entityTarget = LootContext.EntityTarget.fromString(target);
        return new ContextLootNbtProvider(ContextLootNbtProvider.getTarget(entityTarget));
    }

    static interface Target {
        @Nullable
        public NbtElement getNbt(LootContext var1);

        public String getName();

        public Set<LootContextParameter<?>> getRequiredParameters();
    }

    public static class CustomSerializer
    implements JsonSerializing.ElementSerializer<ContextLootNbtProvider> {
        @Override
        public JsonElement toJson(ContextLootNbtProvider contextLootNbtProvider, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(contextLootNbtProvider.target.getName());
        }

        @Override
        public ContextLootNbtProvider fromJson(JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext) {
            String string = jsonElement.getAsString();
            return ContextLootNbtProvider.fromTarget(string);
        }

        @Override
        public /* synthetic */ Object fromJson(JsonElement json, JsonDeserializationContext context) {
            return this.fromJson(json, context);
        }
    }

    public static class Serializer
    implements JsonSerializer<ContextLootNbtProvider> {
        @Override
        public void toJson(JsonObject jsonObject, ContextLootNbtProvider contextLootNbtProvider, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("target", contextLootNbtProvider.target.getName());
        }

        @Override
        public ContextLootNbtProvider fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            String string = JsonHelper.getString(jsonObject, "target");
            return ContextLootNbtProvider.fromTarget(string);
        }

        @Override
        public /* synthetic */ Object fromJson(JsonObject json, JsonDeserializationContext context) {
            return this.fromJson(json, context);
        }
    }
}

