/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.predicate.item;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.item.EnchantmentPredicate;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class ItemPredicate {
    public static final ItemPredicate ANY = new ItemPredicate();
    @Nullable
    private final Tag<Item> tag;
    @Nullable
    private final Set<Item> items;
    private final NumberRange.IntRange count;
    private final NumberRange.IntRange durability;
    private final EnchantmentPredicate[] enchantments;
    private final EnchantmentPredicate[] storedEnchantments;
    @Nullable
    private final Potion potion;
    private final NbtPredicate nbt;

    public ItemPredicate() {
        this.tag = null;
        this.items = null;
        this.potion = null;
        this.count = NumberRange.IntRange.ANY;
        this.durability = NumberRange.IntRange.ANY;
        this.enchantments = EnchantmentPredicate.ARRAY_OF_ANY;
        this.storedEnchantments = EnchantmentPredicate.ARRAY_OF_ANY;
        this.nbt = NbtPredicate.ANY;
    }

    public ItemPredicate(@Nullable Tag<Item> tag, @Nullable Set<Item> items, NumberRange.IntRange count, NumberRange.IntRange durability, EnchantmentPredicate[] enchantments, EnchantmentPredicate[] storedEnchantments, @Nullable Potion potion, NbtPredicate nbt) {
        this.tag = tag;
        this.items = items;
        this.count = count;
        this.durability = durability;
        this.enchantments = enchantments;
        this.storedEnchantments = storedEnchantments;
        this.potion = potion;
        this.nbt = nbt;
    }

    public boolean test(ItemStack stack) {
        Map<Enchantment, Integer> map;
        if (this == ANY) {
            return true;
        }
        if (this.tag != null && !stack.isIn(this.tag)) {
            return false;
        }
        if (this.items != null && !this.items.contains(stack.getItem())) {
            return false;
        }
        if (!this.count.test(stack.getCount())) {
            return false;
        }
        if (!this.durability.isDummy() && !stack.isDamageable()) {
            return false;
        }
        if (!this.durability.test(stack.getMaxDamage() - stack.getDamage())) {
            return false;
        }
        if (!this.nbt.test(stack)) {
            return false;
        }
        if (this.enchantments.length > 0) {
            map = EnchantmentHelper.fromNbt(stack.getEnchantments());
            for (EnchantmentPredicate enchantmentPredicate : this.enchantments) {
                if (enchantmentPredicate.test(map)) continue;
                return false;
            }
        }
        if (this.storedEnchantments.length > 0) {
            map = EnchantmentHelper.fromNbt(EnchantedBookItem.getEnchantmentNbt(stack));
            for (EnchantmentPredicate enchantmentPredicate : this.storedEnchantments) {
                if (enchantmentPredicate.test(map)) continue;
                return false;
            }
        }
        map = PotionUtil.getPotion(stack);
        return this.potion == null || this.potion == map;
    }

    public static ItemPredicate fromJson(@Nullable JsonElement el) {
        Object identifier2;
        EnchantmentPredicate[] identifier;
        EnchantmentPredicate[] jsonElement2;
        Object builder;
        if (el == null || el.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = JsonHelper.asObject(el, "item");
        NumberRange.IntRange intRange = NumberRange.IntRange.fromJson(jsonObject.get("count"));
        NumberRange.IntRange intRange2 = NumberRange.IntRange.fromJson(jsonObject.get("durability"));
        if (jsonObject.has("data")) {
            throw new JsonParseException("Disallowed data tag found");
        }
        NbtPredicate nbtPredicate = NbtPredicate.fromJson(jsonObject.get("nbt"));
        ImmutableCollection set = null;
        JsonArray jsonArray = JsonHelper.getArray(jsonObject, "items", null);
        if (jsonArray != null) {
            builder = ImmutableSet.builder();
            for (EnchantmentPredicate[] jsonElement2 : jsonArray) {
                identifier = new Identifier(JsonHelper.asString((JsonElement)jsonElement2, "item"));
                ((ImmutableSet.Builder)builder).add(Registry.ITEM.getOrEmpty((Identifier)identifier).orElseThrow(() -> ItemPredicate.method_17872((Identifier)identifier)));
            }
            set = ((ImmutableSet.Builder)builder).build();
        }
        builder = null;
        if (jsonObject.has("tag")) {
            identifier2 = new Identifier(JsonHelper.getString(jsonObject, "tag"));
            builder = ServerTagManagerHolder.getTagManager().getTag(Registry.ITEM_KEY, (Identifier)identifier2, id -> new JsonSyntaxException("Unknown item tag '" + id + "'"));
        }
        identifier2 = null;
        if (jsonObject.has("potion")) {
            jsonElement2 = new Identifier(JsonHelper.getString(jsonObject, "potion"));
            identifier2 = Registry.POTION.getOrEmpty((Identifier)jsonElement2).orElseThrow(() -> ItemPredicate.method_17871((Identifier)jsonElement2));
        }
        jsonElement2 = EnchantmentPredicate.deserializeAll(jsonObject.get("enchantments"));
        identifier = EnchantmentPredicate.deserializeAll(jsonObject.get("stored_enchantments"));
        return new ItemPredicate((Tag<Item>)builder, (Set<Item>)((Object)set), intRange, intRange2, jsonElement2, identifier, (Potion)identifier2, nbtPredicate);
    }

    public JsonElement toJson() {
        JsonArray jsonArray;
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        if (this.items != null) {
            jsonArray = new JsonArray();
            for (Item item : this.items) {
                jsonArray.add(Registry.ITEM.getId(item).toString());
            }
            jsonObject.add("items", jsonArray);
        }
        if (this.tag != null) {
            jsonObject.addProperty("tag", ServerTagManagerHolder.getTagManager().getTagId(Registry.ITEM_KEY, this.tag, () -> new IllegalStateException("Unknown item tag")).toString());
        }
        jsonObject.add("count", this.count.toJson());
        jsonObject.add("durability", this.durability.toJson());
        jsonObject.add("nbt", this.nbt.toJson());
        if (this.enchantments.length > 0) {
            jsonArray = new JsonArray();
            for (EnchantmentPredicate enchantmentPredicate : this.enchantments) {
                jsonArray.add(enchantmentPredicate.serialize());
            }
            jsonObject.add("enchantments", jsonArray);
        }
        if (this.storedEnchantments.length > 0) {
            jsonArray = new JsonArray();
            for (EnchantmentPredicate enchantmentPredicate : this.storedEnchantments) {
                jsonArray.add(enchantmentPredicate.serialize());
            }
            jsonObject.add("stored_enchantments", jsonArray);
        }
        if (this.potion != null) {
            jsonObject.addProperty("potion", Registry.POTION.getId(this.potion).toString());
        }
        return jsonObject;
    }

    public static ItemPredicate[] deserializeAll(@Nullable JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return new ItemPredicate[0];
        }
        JsonArray jsonArray = JsonHelper.asArray(el, "items");
        ItemPredicate[] itemPredicates = new ItemPredicate[jsonArray.size()];
        for (int i = 0; i < itemPredicates.length; ++i) {
            itemPredicates[i] = ItemPredicate.fromJson(jsonArray.get(i));
        }
        return itemPredicates;
    }

    private static /* synthetic */ JsonSyntaxException method_17871(Identifier identifier) {
        return new JsonSyntaxException("Unknown potion '" + identifier + "'");
    }

    private static /* synthetic */ JsonSyntaxException method_17872(Identifier identifier) {
        return new JsonSyntaxException("Unknown item id '" + identifier + "'");
    }

    public static class Builder {
        private final List<EnchantmentPredicate> enchantments = Lists.newArrayList();
        private final List<EnchantmentPredicate> storedEnchantments = Lists.newArrayList();
        @Nullable
        private Set<Item> item;
        @Nullable
        private Tag<Item> tag;
        private NumberRange.IntRange count = NumberRange.IntRange.ANY;
        private NumberRange.IntRange durability = NumberRange.IntRange.ANY;
        @Nullable
        private Potion potion;
        private NbtPredicate nbt = NbtPredicate.ANY;

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder items(ItemConvertible ... items) {
            this.item = Stream.of(items).map(ItemConvertible::asItem).collect(ImmutableSet.toImmutableSet());
            return this;
        }

        public Builder tag(Tag<Item> tag) {
            this.tag = tag;
            return this;
        }

        public Builder count(NumberRange.IntRange count) {
            this.count = count;
            return this;
        }

        public Builder durability(NumberRange.IntRange durability) {
            this.durability = durability;
            return this;
        }

        public Builder potion(Potion potion) {
            this.potion = potion;
            return this;
        }

        public Builder nbt(NbtCompound nbt) {
            this.nbt = new NbtPredicate(nbt);
            return this;
        }

        public Builder enchantment(EnchantmentPredicate enchantment) {
            this.enchantments.add(enchantment);
            return this;
        }

        public Builder storedEnchantment(EnchantmentPredicate enchantment) {
            this.storedEnchantments.add(enchantment);
            return this;
        }

        public ItemPredicate build() {
            return new ItemPredicate(this.tag, this.item, this.count, this.durability, this.enchantments.toArray(EnchantmentPredicate.ARRAY_OF_ANY), this.storedEnchantments.toArray(EnchantmentPredicate.ARRAY_OF_ANY), this.potion, this.nbt);
        }
    }
}

