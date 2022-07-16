/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.predicate;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.CriterionProgress;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerRecipeBook;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

public class PlayerPredicate {
    public static final PlayerPredicate ANY = new Builder().build();
    public static final int field_33928 = 100;
    private final NumberRange.IntRange experienceLevel;
    @Nullable
    private final GameMode gameMode;
    private final Map<Stat<?>, NumberRange.IntRange> stats;
    private final Object2BooleanMap<Identifier> recipes;
    private final Map<Identifier, AdvancementPredicate> advancements;
    private final EntityPredicate lookingAt;

    private static AdvancementPredicate criterionFromJson(JsonElement json) {
        if (json.isJsonPrimitive()) {
            boolean bl = json.getAsBoolean();
            return new CompletedAdvancementPredicate(bl);
        }
        Object2BooleanOpenHashMap<String> bl = new Object2BooleanOpenHashMap<String>();
        JsonObject jsonObject = JsonHelper.asObject(json, "criterion data");
        jsonObject.entrySet().forEach(entry -> {
            boolean bl = JsonHelper.asBoolean((JsonElement)entry.getValue(), "criterion test");
            bl.put((String)entry.getKey(), bl);
        });
        return new AdvancementCriteriaPredicate(bl);
    }

    PlayerPredicate(NumberRange.IntRange experienceLevel, @Nullable GameMode gameMode, Map<Stat<?>, NumberRange.IntRange> stats, Object2BooleanMap<Identifier> recipes, Map<Identifier, AdvancementPredicate> advancements, EntityPredicate lookingAt) {
        this.experienceLevel = experienceLevel;
        this.gameMode = gameMode;
        this.stats = stats;
        this.recipes = recipes;
        this.advancements = advancements;
        this.lookingAt = lookingAt;
    }

    public boolean test(Entity entity2) {
        Object advancement;
        if (this == ANY) {
            return true;
        }
        if (!(entity2 instanceof ServerPlayerEntity)) {
            return false;
        }
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity2;
        if (!this.experienceLevel.test(serverPlayerEntity.experienceLevel)) {
            return false;
        }
        if (this.gameMode != null && this.gameMode != serverPlayerEntity.interactionManager.getGameMode()) {
            return false;
        }
        ServerStatHandler statHandler = serverPlayerEntity.getStatHandler();
        for (Map.Entry<Stat<?>, NumberRange.IntRange> entry : this.stats.entrySet()) {
            int n = statHandler.getStat(entry.getKey());
            if (entry.getValue().test(n)) continue;
            return false;
        }
        ServerRecipeBook recipeBook = serverPlayerEntity.getRecipeBook();
        for (Object2BooleanMap.Entry entry : this.recipes.object2BooleanEntrySet()) {
            if (recipeBook.contains((Identifier)entry.getKey()) == entry.getBooleanValue()) continue;
            return false;
        }
        if (!this.advancements.isEmpty()) {
            PlayerAdvancementTracker playerAdvancementTracker = serverPlayerEntity.getAdvancementTracker();
            ServerAdvancementLoader serverAdvancementLoader = serverPlayerEntity.getServer().getAdvancementLoader();
            for (Map.Entry<Identifier, AdvancementPredicate> entry : this.advancements.entrySet()) {
                advancement = serverAdvancementLoader.get(entry.getKey());
                if (advancement != null && entry.getValue().test(playerAdvancementTracker.getProgress((Advancement)advancement))) continue;
                return false;
            }
        }
        if (this.lookingAt != EntityPredicate.ANY) {
            Vec3d vec3d = serverPlayerEntity.getEyePos();
            Vec3d vec3d2 = serverPlayerEntity.getRotationVec(1.0f);
            Vec3d vec3d3 = vec3d.add(vec3d2.x * 100.0, vec3d2.y * 100.0, vec3d2.z * 100.0);
            EntityHitResult entityHitResult = ProjectileUtil.method_37226(serverPlayerEntity.world, serverPlayerEntity, vec3d, vec3d3, new Box(vec3d, vec3d3).expand(1.0), entity -> !entity.isSpectator(), 0.0f);
            if (entityHitResult == null || entityHitResult.getType() != HitResult.Type.ENTITY) {
                return false;
            }
            advancement = entityHitResult.getEntity();
            if (!this.lookingAt.test(serverPlayerEntity, (Entity)advancement) || !serverPlayerEntity.canSee((Entity)advancement)) {
                return false;
            }
        }
        return true;
    }

    public static PlayerPredicate fromJson(@Nullable JsonElement json) {
        Object intRange2;
        Object stat;
        StatType<?> statType;
        Object jsonObject2;
        if (json == null || json.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = JsonHelper.asObject(json, "player");
        NumberRange.IntRange intRange = NumberRange.IntRange.fromJson(jsonObject.get("level"));
        String string = JsonHelper.getString(jsonObject, "gamemode", "");
        GameMode gameMode = GameMode.byName(string, null);
        HashMap<Stat<?>, NumberRange.IntRange> map = Maps.newHashMap();
        JsonArray jsonArray = JsonHelper.getArray(jsonObject, "stats", null);
        if (jsonArray != null) {
            for (JsonElement jsonElement2 : jsonArray) {
                jsonObject2 = JsonHelper.asObject(jsonElement2, "stats entry");
                Identifier identifier = new Identifier(JsonHelper.getString((JsonObject)jsonObject2, "type"));
                statType = Registry.STAT_TYPE.get(identifier);
                if (statType == null) {
                    throw new JsonParseException("Invalid stat type: " + identifier);
                }
                Identifier identifier2 = new Identifier(JsonHelper.getString(jsonObject2, "stat"));
                stat = PlayerPredicate.getStat(statType, identifier2);
                intRange2 = NumberRange.IntRange.fromJson(((JsonObject)jsonObject2).get("value"));
                map.put((Stat<?>)stat, (NumberRange.IntRange)intRange2);
            }
        }
        Object2BooleanOpenHashMap<Identifier> object2BooleanMap = new Object2BooleanOpenHashMap<Identifier>();
        JsonObject jsonElement = JsonHelper.getObject(jsonObject, "recipes", new JsonObject());
        for (Map.Entry entry : jsonElement.entrySet()) {
            statType = new Identifier((String)entry.getKey());
            boolean identifier22 = JsonHelper.asBoolean((JsonElement)entry.getValue(), "recipe present");
            object2BooleanMap.put((Identifier)((Object)statType), identifier22);
        }
        jsonObject2 = Maps.newHashMap();
        JsonObject jsonObject3 = JsonHelper.getObject(jsonObject, "advancements", new JsonObject());
        for (Map.Entry<String, JsonElement> identifier2 : jsonObject3.entrySet()) {
            stat = new Identifier(identifier2.getKey());
            intRange2 = PlayerPredicate.criterionFromJson(identifier2.getValue());
            jsonObject2.put((Identifier)stat, (AdvancementPredicate)intRange2);
        }
        statType = EntityPredicate.fromJson(jsonObject.get("looking_at"));
        return new PlayerPredicate(intRange, gameMode, (Map<Stat<?>, NumberRange.IntRange>)map, (Object2BooleanMap<Identifier>)object2BooleanMap, (Map<Identifier, AdvancementPredicate>)jsonObject2, (EntityPredicate)((Object)statType));
    }

    private static <T> Stat<T> getStat(StatType<T> type, Identifier id) {
        Registry<T> registry = type.getRegistry();
        T object = registry.get(id);
        if (object == null) {
            throw new JsonParseException("Unknown object " + id + " for stat type " + Registry.STAT_TYPE.getId(type));
        }
        return type.getOrCreateStat(object);
    }

    private static <T> Identifier getStatId(Stat<T> stat) {
        return stat.getType().getRegistry().getId(stat.getValue());
    }

    public JsonElement toJson() {
        JsonElement jsonArray;
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("level", this.experienceLevel.toJson());
        if (this.gameMode != null) {
            jsonObject.addProperty("gamemode", this.gameMode.getName());
        }
        if (!this.stats.isEmpty()) {
            jsonArray = new JsonArray();
            this.stats.forEach((arg_0, arg_1) -> PlayerPredicate.method_22498((JsonArray)jsonArray, arg_0, arg_1));
            jsonObject.add("stats", jsonArray);
        }
        if (!this.recipes.isEmpty()) {
            jsonArray = new JsonObject();
            this.recipes.forEach((arg_0, arg_1) -> PlayerPredicate.method_22500((JsonObject)jsonArray, arg_0, arg_1));
            jsonObject.add("recipes", jsonArray);
        }
        if (!this.advancements.isEmpty()) {
            jsonArray = new JsonObject();
            this.advancements.forEach((arg_0, arg_1) -> PlayerPredicate.method_22501((JsonObject)jsonArray, arg_0, arg_1));
            jsonObject.add("advancements", jsonArray);
        }
        jsonObject.add("looking_at", this.lookingAt.toJson());
        return jsonObject;
    }

    private static /* synthetic */ void method_22501(JsonObject jsonObject, Identifier id, AdvancementPredicate advancementPredicate) {
        jsonObject.add(id.toString(), advancementPredicate.toJson());
    }

    private static /* synthetic */ void method_22500(JsonObject jsonObject, Identifier id, Boolean boolean_) {
        jsonObject.addProperty(id.toString(), boolean_);
    }

    private static /* synthetic */ void method_22498(JsonArray jsonArray, Stat stat, NumberRange.IntRange intRange) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", Registry.STAT_TYPE.getId(stat.getType()).toString());
        jsonObject.addProperty("stat", PlayerPredicate.getStatId(stat).toString());
        jsonObject.add("value", intRange.toJson());
        jsonArray.add(jsonObject);
    }

    static class CompletedAdvancementPredicate
    implements AdvancementPredicate {
        private final boolean done;

        public CompletedAdvancementPredicate(boolean done) {
            this.done = done;
        }

        @Override
        public JsonElement toJson() {
            return new JsonPrimitive(this.done);
        }

        @Override
        public boolean test(AdvancementProgress advancementProgress) {
            return advancementProgress.isDone() == this.done;
        }

        @Override
        public /* synthetic */ boolean test(Object progress) {
            return this.test((AdvancementProgress)progress);
        }
    }

    static class AdvancementCriteriaPredicate
    implements AdvancementPredicate {
        private final Object2BooleanMap<String> criteria;

        public AdvancementCriteriaPredicate(Object2BooleanMap<String> criteria) {
            this.criteria = criteria;
        }

        @Override
        public JsonElement toJson() {
            JsonObject jsonObject = new JsonObject();
            this.criteria.forEach(jsonObject::addProperty);
            return jsonObject;
        }

        @Override
        public boolean test(AdvancementProgress advancementProgress) {
            for (Object2BooleanMap.Entry entry : this.criteria.object2BooleanEntrySet()) {
                CriterionProgress criterionProgress = advancementProgress.getCriterionProgress((String)entry.getKey());
                if (criterionProgress != null && criterionProgress.isObtained() == entry.getBooleanValue()) continue;
                return false;
            }
            return true;
        }

        @Override
        public /* synthetic */ boolean test(Object progress) {
            return this.test((AdvancementProgress)progress);
        }
    }

    static interface AdvancementPredicate
    extends Predicate<AdvancementProgress> {
        public JsonElement toJson();
    }

    public static class Builder {
        private NumberRange.IntRange experienceLevel = NumberRange.IntRange.ANY;
        @Nullable
        private GameMode gameMode;
        private final Map<Stat<?>, NumberRange.IntRange> stats = Maps.newHashMap();
        private final Object2BooleanMap<Identifier> recipes = new Object2BooleanOpenHashMap<Identifier>();
        private final Map<Identifier, AdvancementPredicate> advancements = Maps.newHashMap();
        private EntityPredicate lookingAt = EntityPredicate.ANY;

        public static Builder create() {
            return new Builder();
        }

        public Builder experienceLevel(NumberRange.IntRange experienceLevel) {
            this.experienceLevel = experienceLevel;
            return this;
        }

        public Builder stat(Stat<?> stat, NumberRange.IntRange value) {
            this.stats.put(stat, value);
            return this;
        }

        public Builder recipe(Identifier id, boolean unlocked) {
            this.recipes.put(id, unlocked);
            return this;
        }

        public Builder gameMode(GameMode gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public Builder lookingAt(EntityPredicate lookingAt) {
            this.lookingAt = lookingAt;
            return this;
        }

        public Builder advancement(Identifier id, boolean done) {
            this.advancements.put(id, new CompletedAdvancementPredicate(done));
            return this;
        }

        public Builder advancement(Identifier id, Map<String, Boolean> criteria) {
            this.advancements.put(id, new AdvancementCriteriaPredicate(new Object2BooleanOpenHashMap<String>(criteria)));
            return this;
        }

        public PlayerPredicate build() {
            return new PlayerPredicate(this.experienceLevel, this.gameMode, this.stats, this.recipes, this.advancements, this.lookingAt);
        }
    }
}

