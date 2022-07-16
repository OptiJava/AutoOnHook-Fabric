/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.predicate.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class FishingHookPredicate {
    public static final FishingHookPredicate ANY = new FishingHookPredicate(false);
    private static final String IN_OPEN_WATER = "in_open_water";
    private final boolean inOpenWater;

    private FishingHookPredicate(boolean inOpenWater) {
        this.inOpenWater = inOpenWater;
    }

    public static FishingHookPredicate of(boolean inOpenWater) {
        return new FishingHookPredicate(inOpenWater);
    }

    public static FishingHookPredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = JsonHelper.asObject(json, "fishing_hook");
        JsonElement jsonElement = jsonObject.get(IN_OPEN_WATER);
        if (jsonElement != null) {
            return new FishingHookPredicate(JsonHelper.asBoolean(jsonElement, IN_OPEN_WATER));
        }
        return ANY;
    }

    public JsonElement toJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(IN_OPEN_WATER, new JsonPrimitive(this.inOpenWater));
        return jsonObject;
    }

    public boolean test(Entity entity) {
        if (this == ANY) {
            return true;
        }
        if (!(entity instanceof FishingBobberEntity)) {
            return false;
        }
        FishingBobberEntity fishingBobberEntity = (FishingBobberEntity)entity;
        return this.inOpenWater == fishingBobberEntity.isInOpenWater();
    }
}

