/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EntityView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ModifiableTestableWorld;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

/**
 * A world view or {@link World}'s superinterface that exposes access to
 * a registry manager.
 * 
 * @see #getRegistryManager()
 */
public interface RegistryWorldView
extends EntityView,
WorldView,
ModifiableTestableWorld {
    @Override
    default public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos pos, BlockEntityType<T> type) {
        return WorldView.super.getBlockEntity(pos, type);
    }

    @Override
    default public Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, Box box, Predicate<Entity> predicate) {
        return EntityView.super.getEntityCollisions(entity, box, predicate);
    }

    @Override
    default public boolean intersectsEntities(@Nullable Entity entity, VoxelShape shape) {
        return EntityView.super.intersectsEntities(entity, shape);
    }

    @Override
    default public BlockPos getTopPosition(Heightmap.Type heightmap, BlockPos pos) {
        return WorldView.super.getTopPosition(heightmap, pos);
    }

    public DynamicRegistryManager getRegistryManager();

    default public Optional<RegistryKey<Biome>> getBiomeKey(BlockPos pos) {
        return this.getRegistryManager().get(Registry.BIOME_KEY).getKey(this.getBiome(pos));
    }
}

