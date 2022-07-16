/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.predicate.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.predicate.NumberRange;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class EnchantmentPredicate {
    public static final EnchantmentPredicate ANY = new EnchantmentPredicate();
    public static final EnchantmentPredicate[] ARRAY_OF_ANY = new EnchantmentPredicate[0];
    private final Enchantment enchantment;
    private final NumberRange.IntRange levels;

    public EnchantmentPredicate() {
        this.enchantment = null;
        this.levels = NumberRange.IntRange.ANY;
    }

    public EnchantmentPredicate(@Nullable Enchantment enchantment, NumberRange.IntRange levels) {
        this.enchantment = enchantment;
        this.levels = levels;
    }

    public boolean test(Map<Enchantment, Integer> enchantments) {
        if (this.enchantment != null) {
            if (!enchantments.containsKey(this.enchantment)) {
                return false;
            }
            int i = enchantments.get(this.enchantment);
            if (this.levels != null && !this.levels.test(i)) {
                return false;
            }
        } else if (this.levels != null) {
            for (Integer integer : enchantments.values()) {
                if (!this.levels.test(integer)) continue;
                return true;
            }
            return false;
        }
        return true;
    }

    public JsonElement serialize() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        if (this.enchantment != null) {
            jsonObject.addProperty("enchantment", Registry.ENCHANTMENT.getId(this.enchantment).toString());
        }
        jsonObject.add("levels", this.levels.toJson());
        return jsonObject;
    }

    public static EnchantmentPredicate deserialize(@Nullable JsonElement el) {
        Object identifier;
        if (el == null || el.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = JsonHelper.asObject(el, "enchantment");
        Enchantment enchantment = null;
        if (jsonObject.has("enchantment")) {
            identifier = new Identifier(JsonHelper.getString(jsonObject, "enchantment"));
            enchantment = Registry.ENCHANTMENT.getOrEmpty((Identifier)identifier).orElseThrow(() -> EnchantmentPredicate.method_17849((Identifier)identifier));
        }
        identifier = NumberRange.IntRange.fromJson(jsonObject.get("levels"));
        return new EnchantmentPredicate(enchantment, (NumberRange.IntRange)identifier);
    }

    public static EnchantmentPredicate[] deserializeAll(@Nullable JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return ARRAY_OF_ANY;
        }
        JsonArray jsonArray = JsonHelper.asArray(el, "enchantments");
        EnchantmentPredicate[] enchantmentPredicates = new EnchantmentPredicate[jsonArray.size()];
        for (int i = 0; i < enchantmentPredicates.length; ++i) {
            enchantmentPredicates[i] = EnchantmentPredicate.deserialize(jsonArray.get(i));
        }
        return enchantmentPredicates;
    }

    private static /* synthetic */ JsonSyntaxException method_17849(Identifier identifier) {
        return new JsonSyntaxException("Unknown enchantment '" + identifier + "'");
    }
}

