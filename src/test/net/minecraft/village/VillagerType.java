/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.village;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public final class VillagerType {
    public static final VillagerType DESERT = VillagerType.create("desert");
    public static final VillagerType JUNGLE = VillagerType.create("jungle");
    public static final VillagerType PLAINS = VillagerType.create("plains");
    public static final VillagerType SAVANNA = VillagerType.create("savanna");
    public static final VillagerType SNOW = VillagerType.create("snow");
    public static final VillagerType SWAMP = VillagerType.create("swamp");
    public static final VillagerType TAIGA = VillagerType.create("taiga");
    private final String name;
    private static final Map<RegistryKey<Biome>, VillagerType> BIOME_TO_TYPE = Util.make(Maps.newHashMap(), map -> {
        map.put(BiomeKeys.BADLANDS, DESERT);
        map.put(BiomeKeys.BADLANDS_PLATEAU, DESERT);
        map.put(BiomeKeys.DESERT, DESERT);
        map.put(BiomeKeys.DESERT_HILLS, DESERT);
        map.put(BiomeKeys.DESERT_LAKES, DESERT);
        map.put(BiomeKeys.ERODED_BADLANDS, DESERT);
        map.put(BiomeKeys.MODIFIED_BADLANDS_PLATEAU, DESERT);
        map.put(BiomeKeys.MODIFIED_WOODED_BADLANDS_PLATEAU, DESERT);
        map.put(BiomeKeys.WOODED_BADLANDS_PLATEAU, DESERT);
        map.put(BiomeKeys.BAMBOO_JUNGLE, JUNGLE);
        map.put(BiomeKeys.BAMBOO_JUNGLE_HILLS, JUNGLE);
        map.put(BiomeKeys.JUNGLE, JUNGLE);
        map.put(BiomeKeys.JUNGLE_EDGE, JUNGLE);
        map.put(BiomeKeys.JUNGLE_HILLS, JUNGLE);
        map.put(BiomeKeys.MODIFIED_JUNGLE, JUNGLE);
        map.put(BiomeKeys.MODIFIED_JUNGLE_EDGE, JUNGLE);
        map.put(BiomeKeys.SAVANNA_PLATEAU, SAVANNA);
        map.put(BiomeKeys.SAVANNA, SAVANNA);
        map.put(BiomeKeys.SHATTERED_SAVANNA, SAVANNA);
        map.put(BiomeKeys.SHATTERED_SAVANNA_PLATEAU, SAVANNA);
        map.put(BiomeKeys.DEEP_FROZEN_OCEAN, SNOW);
        map.put(BiomeKeys.FROZEN_OCEAN, SNOW);
        map.put(BiomeKeys.FROZEN_RIVER, SNOW);
        map.put(BiomeKeys.ICE_SPIKES, SNOW);
        map.put(BiomeKeys.SNOWY_BEACH, SNOW);
        map.put(BiomeKeys.SNOWY_MOUNTAINS, SNOW);
        map.put(BiomeKeys.SNOWY_TAIGA, SNOW);
        map.put(BiomeKeys.SNOWY_TAIGA_HILLS, SNOW);
        map.put(BiomeKeys.SNOWY_TAIGA_MOUNTAINS, SNOW);
        map.put(BiomeKeys.SNOWY_TUNDRA, SNOW);
        map.put(BiomeKeys.SWAMP, SWAMP);
        map.put(BiomeKeys.SWAMP_HILLS, SWAMP);
        map.put(BiomeKeys.GIANT_SPRUCE_TAIGA, TAIGA);
        map.put(BiomeKeys.GIANT_SPRUCE_TAIGA_HILLS, TAIGA);
        map.put(BiomeKeys.GIANT_TREE_TAIGA, TAIGA);
        map.put(BiomeKeys.GIANT_TREE_TAIGA_HILLS, TAIGA);
        map.put(BiomeKeys.GRAVELLY_MOUNTAINS, TAIGA);
        map.put(BiomeKeys.MODIFIED_GRAVELLY_MOUNTAINS, TAIGA);
        map.put(BiomeKeys.MOUNTAIN_EDGE, TAIGA);
        map.put(BiomeKeys.MOUNTAINS, TAIGA);
        map.put(BiomeKeys.TAIGA, TAIGA);
        map.put(BiomeKeys.TAIGA_HILLS, TAIGA);
        map.put(BiomeKeys.TAIGA_MOUNTAINS, TAIGA);
        map.put(BiomeKeys.WOODED_MOUNTAINS, TAIGA);
    });

    private VillagerType(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    private static VillagerType create(String id) {
        return Registry.register(Registry.VILLAGER_TYPE, new Identifier(id), new VillagerType(id));
    }

    public static VillagerType forBiome(Optional<RegistryKey<Biome>> biomeKey) {
        return biomeKey.flatMap(registryKey -> Optional.ofNullable(BIOME_TO_TYPE.get(registryKey))).orElse(PLAINS);
    }
}

