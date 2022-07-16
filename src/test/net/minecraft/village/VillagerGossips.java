/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.village;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.DoublePredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.dynamic.DynamicSerializableUuid;
import net.minecraft.village.VillageGossipType;

public class VillagerGossips {
    public static final int field_30236 = 2;
    private final Map<UUID, Reputation> entityReputation = Maps.newHashMap();

    @Debug
    public Map<UUID, Object2IntMap<VillageGossipType>> getEntityReputationAssociatedGossips() {
        HashMap<UUID, Object2IntMap<VillageGossipType>> map = Maps.newHashMap();
        this.entityReputation.keySet().forEach(uuid -> {
            Reputation reputation = this.entityReputation.get(uuid);
            map.put((UUID)uuid, reputation.associatedGossip);
        });
        return map;
    }

    public void decay() {
        Iterator<Reputation> iterator = this.entityReputation.values().iterator();
        while (iterator.hasNext()) {
            Reputation reputation = iterator.next();
            reputation.decay();
            if (!reputation.isObsolete()) continue;
            iterator.remove();
        }
    }

    private Stream<GossipEntry> entries() {
        return this.entityReputation.entrySet().stream().flatMap(entry -> ((Reputation)entry.getValue()).entriesFor((UUID)entry.getKey()));
    }

    private Collection<GossipEntry> pickGossips(Random random, int count) {
        List list = this.entries().collect(Collectors.toList());
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        int[] is = new int[list.size()];
        int i = 0;
        for (int j = 0; j < list.size(); ++j) {
            GossipEntry gossipEntry = (GossipEntry)list.get(j);
            is[j] = (i += Math.abs(gossipEntry.getValue())) - 1;
        }
        Set<GossipEntry> j = Sets.newIdentityHashSet();
        for (int gossipEntry = 0; gossipEntry < count; ++gossipEntry) {
            int k = random.nextInt(i);
            int l = Arrays.binarySearch(is, k);
            j.add((GossipEntry)list.get(l < 0 ? -l - 1 : l));
        }
        return j;
    }

    private Reputation getReputationFor(UUID target) {
        return this.entityReputation.computeIfAbsent(target, uUID -> new Reputation());
    }

    public void shareGossipFrom(VillagerGossips from, Random random, int count) {
        Collection<GossipEntry> collection = from.pickGossips(random, count);
        collection.forEach(gossipEntry -> {
            int i = gossipEntry.value - gossipEntry.type.shareDecrement;
            if (i >= 2) {
                this.getReputationFor((UUID)gossipEntry.target).associatedGossip.mergeInt(gossipEntry.type, i, VillagerGossips::max);
            }
        });
    }

    public int getReputationFor(UUID target, Predicate<VillageGossipType> gossipTypeFilter) {
        Reputation reputation = this.entityReputation.get(target);
        return reputation != null ? reputation.getValueFor(gossipTypeFilter) : 0;
    }

    public long method_35122(VillageGossipType villageGossipType, DoublePredicate doublePredicate) {
        return this.entityReputation.values().stream().filter(reputation -> doublePredicate.test(reputation.associatedGossip.getOrDefault((Object)villageGossipType, 0) * villageGossipType.multiplier)).count();
    }

    public void startGossip(UUID target, VillageGossipType type, int value) {
        Reputation reputation = this.getReputationFor(target);
        reputation.associatedGossip.mergeInt(type, value, (integer, integer2) -> this.mergeReputation(type, (int)integer, (int)integer2));
        reputation.clamp(type);
        if (reputation.isObsolete()) {
            this.entityReputation.remove(target);
        }
    }

    public void method_35126(UUID uUID, VillageGossipType villageGossipType, int i) {
        this.startGossip(uUID, villageGossipType, -i);
    }

    public void method_35124(UUID uUID, VillageGossipType villageGossipType) {
        Reputation reputation = this.entityReputation.get(uUID);
        if (reputation != null) {
            reputation.remove(villageGossipType);
            if (reputation.isObsolete()) {
                this.entityReputation.remove(uUID);
            }
        }
    }

    public void method_35121(VillageGossipType villageGossipType) {
        Iterator<Reputation> iterator = this.entityReputation.values().iterator();
        while (iterator.hasNext()) {
            Reputation reputation = iterator.next();
            reputation.remove(villageGossipType);
            if (!reputation.isObsolete()) continue;
            iterator.remove();
        }
    }

    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<Object>(dynamicOps, dynamicOps.createList(this.entries().map(gossipEntry -> gossipEntry.serialize(dynamicOps)).map(Dynamic::getValue)));
    }

    public void deserialize(Dynamic<?> dynamic) {
        dynamic.asStream().map(GossipEntry::deserialize).flatMap(dataResult -> Util.stream(dataResult.result())).forEach(gossipEntry -> this.getReputationFor((UUID)gossipEntry.target).associatedGossip.put(gossipEntry.type, gossipEntry.value));
    }

    private static int max(int left, int right) {
        return Math.max(left, right);
    }

    private int mergeReputation(VillageGossipType type, int left, int right) {
        int i = left + right;
        return i > type.maxValue ? Math.max(type.maxValue, left) : i;
    }

    static class Reputation {
        final Object2IntMap<VillageGossipType> associatedGossip = new Object2IntOpenHashMap<VillageGossipType>();

        Reputation() {
        }

        public int getValueFor(Predicate<VillageGossipType> gossipTypeFilter) {
            return this.associatedGossip.object2IntEntrySet().stream().filter(entry -> gossipTypeFilter.test((VillageGossipType)((Object)((Object)entry.getKey())))).mapToInt(entry -> entry.getIntValue() * ((VillageGossipType)((Object)((Object)entry.getKey()))).multiplier).sum();
        }

        public Stream<GossipEntry> entriesFor(UUID target) {
            return this.associatedGossip.object2IntEntrySet().stream().map(entry -> new GossipEntry(target, (VillageGossipType)((Object)((Object)entry.getKey())), entry.getIntValue()));
        }

        public void decay() {
            Iterator objectIterator = this.associatedGossip.object2IntEntrySet().iterator();
            while (objectIterator.hasNext()) {
                Object2IntMap.Entry entry = (Object2IntMap.Entry)objectIterator.next();
                int i = entry.getIntValue() - ((VillageGossipType)((Object)entry.getKey())).decay;
                if (i < 2) {
                    objectIterator.remove();
                    continue;
                }
                entry.setValue(i);
            }
        }

        public boolean isObsolete() {
            return this.associatedGossip.isEmpty();
        }

        public void clamp(VillageGossipType gossipType) {
            int i = this.associatedGossip.getInt((Object)gossipType);
            if (i > gossipType.maxValue) {
                this.associatedGossip.put(gossipType, gossipType.maxValue);
            }
            if (i < 2) {
                this.remove(gossipType);
            }
        }

        public void remove(VillageGossipType gossipType) {
            this.associatedGossip.removeInt((Object)gossipType);
        }
    }

    static class GossipEntry {
        public static final String TARGET_KEY = "Target";
        public static final String TYPE_KEY = "Type";
        public static final String VALUE_KEY = "Value";
        public final UUID target;
        public final VillageGossipType type;
        public final int value;

        public GossipEntry(UUID target, VillageGossipType type, int value) {
            this.target = target;
            this.type = type;
            this.value = value;
        }

        public int getValue() {
            return this.value * this.type.multiplier;
        }

        public String toString() {
            return "GossipEntry{target=" + this.target + ", type=" + this.type + ", value=" + this.value + "}";
        }

        public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
            return new Dynamic<T>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString(TARGET_KEY), DynamicSerializableUuid.CODEC.encodeStart(dynamicOps, this.target).result().orElseThrow(RuntimeException::new), dynamicOps.createString(TYPE_KEY), dynamicOps.createString(this.type.key), dynamicOps.createString(VALUE_KEY), dynamicOps.createInt(this.value))));
        }

        public static DataResult<GossipEntry> deserialize(Dynamic<?> dynamic) {
            return DataResult.unbox(DataResult.instance().group(dynamic.get(TARGET_KEY).read(DynamicSerializableUuid.CODEC), dynamic.get(TYPE_KEY).asString().map(VillageGossipType::byKey), dynamic.get(VALUE_KEY).asNumber().map(Number::intValue)).apply(DataResult.instance(), GossipEntry::new));
        }
    }
}

