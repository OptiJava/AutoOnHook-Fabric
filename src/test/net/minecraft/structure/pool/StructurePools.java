/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.structure.pool;

import com.google.common.collect.ImmutableList;
import net.minecraft.structure.BastionRemnantGenerator;
import net.minecraft.structure.PillagerOutpostGenerator;
import net.minecraft.structure.VillageGenerator;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public class StructurePools {
    public static final RegistryKey<StructurePool> EMPTY = RegistryKey.of(Registry.STRUCTURE_POOL_KEY, new Identifier("empty"));
    private static final StructurePool INVALID = StructurePools.register(new StructurePool(EMPTY.getValue(), EMPTY.getValue(), ImmutableList.of(), StructurePool.Projection.RIGID));

    public static StructurePool register(StructurePool templatePool) {
        return BuiltinRegistries.add(BuiltinRegistries.STRUCTURE_POOL, templatePool.getId(), templatePool);
    }

    public static StructurePool initDefaultPools() {
        BastionRemnantGenerator.init();
        PillagerOutpostGenerator.init();
        VillageGenerator.init();
        return INVALID;
    }

    static {
        StructurePools.initDefaultPools();
    }
}

