/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.data.server;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.condition.LocationCheckLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.loot.function.EnchantWithLevelsLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.function.SetDamageLootFunction;
import net.minecraft.loot.function.SetNbtLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.FishingHookPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.biome.BiomeKeys;

public class FishingLootTableGenerator
implements Consumer<BiConsumer<Identifier, LootTable.Builder>> {
    public static final LootCondition.Builder NEEDS_JUNGLE_BIOME = LocationCheckLootCondition.builder(LocationPredicate.Builder.create().biome(BiomeKeys.JUNGLE));
    public static final LootCondition.Builder NEEDS_JUNGLE_HILLS_BIOME = LocationCheckLootCondition.builder(LocationPredicate.Builder.create().biome(BiomeKeys.JUNGLE_HILLS));
    public static final LootCondition.Builder NEEDS_JUNGLE_EDGE_BIOME = LocationCheckLootCondition.builder(LocationPredicate.Builder.create().biome(BiomeKeys.JUNGLE_EDGE));
    public static final LootCondition.Builder NEEDS_BAMBOO_JUNGLE_BIOME = LocationCheckLootCondition.builder(LocationPredicate.Builder.create().biome(BiomeKeys.BAMBOO_JUNGLE));
    public static final LootCondition.Builder NEEDS_MODIFIED_JUNGLE_BIOME = LocationCheckLootCondition.builder(LocationPredicate.Builder.create().biome(BiomeKeys.MODIFIED_JUNGLE));
    public static final LootCondition.Builder NEEDS_MODIFIED_JUNGLE_EDGE_BIOME = LocationCheckLootCondition.builder(LocationPredicate.Builder.create().biome(BiomeKeys.MODIFIED_JUNGLE_EDGE));
    public static final LootCondition.Builder NEEDS_BAMBOO_JUNGLE_HILLS_BIOME = LocationCheckLootCondition.builder(LocationPredicate.Builder.create().biome(BiomeKeys.BAMBOO_JUNGLE_HILLS));

    @Override
    public void accept(BiConsumer<Identifier, LootTable.Builder> biConsumer) {
        biConsumer.accept(LootTables.FISHING_GAMEPLAY, LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)((LeafEntry.Builder)LootTableEntry.builder(LootTables.FISHING_JUNK_GAMEPLAY).weight(10)).quality(-2)).with((LootPoolEntry.Builder<?>)((LootPoolEntry.Builder)((LeafEntry.Builder)LootTableEntry.builder(LootTables.FISHING_TREASURE_GAMEPLAY).weight(5)).quality(2)).conditionally(EntityPropertiesLootCondition.builder(LootContext.EntityTarget.THIS, EntityPredicate.Builder.create().fishHook(FishingHookPredicate.of(true))))).with((LootPoolEntry.Builder<?>)((LeafEntry.Builder)LootTableEntry.builder(LootTables.FISHING_FISH_GAMEPLAY).weight(85)).quality(-1))));
        biConsumer.accept(LootTables.FISHING_FISH_GAMEPLAY, LootTable.builder().pool(LootPool.builder().with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.COD).weight(60)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.SALMON).weight(25)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.TROPICAL_FISH).weight(2)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.PUFFERFISH).weight(13))));
        biConsumer.accept(LootTables.FISHING_JUNK_GAMEPLAY, LootTable.builder().pool(LootPool.builder().with((LootPoolEntry.Builder<?>)ItemEntry.builder(Blocks.LILY_PAD).weight(17)).with((LootPoolEntry.Builder<?>)((LeafEntry.Builder)ItemEntry.builder(Items.LEATHER_BOOTS).weight(10)).apply(SetDamageLootFunction.builder(UniformLootNumberProvider.create(0.0f, 0.9f)))).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.LEATHER).weight(10)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.BONE).weight(10)).with((LootPoolEntry.Builder<?>)((LeafEntry.Builder)ItemEntry.builder(Items.POTION).weight(10)).apply(SetNbtLootFunction.builder(Util.make(new NbtCompound(), nbtCompound -> nbtCompound.putString("Potion", "minecraft:water"))))).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.STRING).weight(5)).with((LootPoolEntry.Builder<?>)((LeafEntry.Builder)ItemEntry.builder(Items.FISHING_ROD).weight(2)).apply(SetDamageLootFunction.builder(UniformLootNumberProvider.create(0.0f, 0.9f)))).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.BOWL).weight(10)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.STICK).weight(5)).with((LootPoolEntry.Builder<?>)((LeafEntry.Builder)ItemEntry.builder(Items.INK_SAC).weight(1)).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(10.0f)))).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Blocks.TRIPWIRE_HOOK).weight(10)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.ROTTEN_FLESH).weight(10)).with((LootPoolEntry.Builder<?>)((LeafEntry.Builder)ItemEntry.builder(Blocks.BAMBOO).conditionally(NEEDS_JUNGLE_BIOME.or(NEEDS_JUNGLE_HILLS_BIOME).or(NEEDS_JUNGLE_EDGE_BIOME).or(NEEDS_BAMBOO_JUNGLE_BIOME).or(NEEDS_MODIFIED_JUNGLE_BIOME).or(NEEDS_MODIFIED_JUNGLE_EDGE_BIOME).or(NEEDS_BAMBOO_JUNGLE_HILLS_BIOME))).weight(10))));
        biConsumer.accept(LootTables.FISHING_TREASURE_GAMEPLAY, LootTable.builder().pool(LootPool.builder().with(ItemEntry.builder(Items.NAME_TAG)).with(ItemEntry.builder(Items.SADDLE)).with((LootPoolEntry.Builder<?>)((LeafEntry.Builder)ItemEntry.builder(Items.BOW).apply(SetDamageLootFunction.builder(UniformLootNumberProvider.create(0.0f, 0.25f)))).apply(EnchantWithLevelsLootFunction.builder(ConstantLootNumberProvider.create(30.0f)).allowTreasureEnchantments())).with((LootPoolEntry.Builder<?>)((LeafEntry.Builder)ItemEntry.builder(Items.FISHING_ROD).apply(SetDamageLootFunction.builder(UniformLootNumberProvider.create(0.0f, 0.25f)))).apply(EnchantWithLevelsLootFunction.builder(ConstantLootNumberProvider.create(30.0f)).allowTreasureEnchantments())).with((LootPoolEntry.Builder<?>)ItemEntry.builder(Items.BOOK).apply(EnchantWithLevelsLootFunction.builder(ConstantLootNumberProvider.create(30.0f)).allowTreasureEnchantments())).with(ItemEntry.builder(Items.NAUTILUS_SHELL))));
    }

    @Override
    public /* synthetic */ void accept(Object exporter) {
        this.accept((BiConsumer)exporter);
    }
}

