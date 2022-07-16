/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class LookTargetUtil {
    public static void lookAtAndWalkTowardsEachOther(LivingEntity first, LivingEntity second, float speed) {
        LookTargetUtil.lookAtEachOther(first, second);
        LookTargetUtil.walkTowardsEachOther(first, second, speed);
    }

    public static boolean canSee(Brain<?> brain, LivingEntity target) {
        return brain.getOptionalMemory(MemoryModuleType.VISIBLE_MOBS).filter(list -> list.contains(target)).isPresent();
    }

    public static boolean canSee(Brain<?> brain, MemoryModuleType<? extends LivingEntity> memoryModuleType, EntityType<?> entityType) {
        return LookTargetUtil.canSee(brain, memoryModuleType, (LivingEntity livingEntity) -> livingEntity.getType() == entityType);
    }

    private static boolean canSee(Brain<?> brain, MemoryModuleType<? extends LivingEntity> memoryType, Predicate<LivingEntity> filter) {
        return brain.getOptionalMemory(memoryType).filter(filter).filter(LivingEntity::isAlive).filter(livingEntity -> LookTargetUtil.canSee(brain, livingEntity)).isPresent();
    }

    private static void lookAtEachOther(LivingEntity first, LivingEntity second) {
        LookTargetUtil.lookAt(first, second);
        LookTargetUtil.lookAt(second, first);
    }

    public static void lookAt(LivingEntity entity, LivingEntity target) {
        entity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(target, true));
    }

    private static void walkTowardsEachOther(LivingEntity first, LivingEntity second, float speed) {
        int i = 2;
        LookTargetUtil.walkTowards(first, second, speed, 2);
        LookTargetUtil.walkTowards(second, first, speed, 2);
    }

    public static void walkTowards(LivingEntity entity, Entity target, float speed, int completionRange) {
        WalkTarget walkTarget = new WalkTarget(new EntityLookTarget(target, false), speed, completionRange);
        entity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(target, true));
        entity.getBrain().remember(MemoryModuleType.WALK_TARGET, walkTarget);
    }

    public static void walkTowards(LivingEntity entity, BlockPos target, float speed, int completionRange) {
        WalkTarget walkTarget = new WalkTarget(new BlockPosLookTarget(target), speed, completionRange);
        entity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(target));
        entity.getBrain().remember(MemoryModuleType.WALK_TARGET, walkTarget);
    }

    public static void give(LivingEntity entity, ItemStack stack, Vec3d targetLocation) {
        double d = entity.getEyeY() - (double)0.3f;
        ItemEntity itemEntity = new ItemEntity(entity.world, entity.getX(), d, entity.getZ(), stack);
        float f = 0.3f;
        Vec3d vec3d = targetLocation.subtract(entity.getPos());
        vec3d = vec3d.normalize().multiply(0.3f);
        itemEntity.setVelocity(vec3d);
        itemEntity.setToDefaultPickupDelay();
        entity.world.spawnEntity(itemEntity);
    }

    public static ChunkSectionPos getPosClosestToOccupiedPointOfInterest(ServerWorld world, ChunkSectionPos center, int radius) {
        int i = world.getOccupiedPointOfInterestDistance(center);
        return ChunkSectionPos.stream(center, radius).filter(chunkSectionPos -> world.getOccupiedPointOfInterestDistance((ChunkSectionPos)chunkSectionPos) < i).min(Comparator.comparingInt(world::getOccupiedPointOfInterestDistance)).orElse(center);
    }

    public static boolean isTargetWithinAttackRange(MobEntity source, LivingEntity target, int rangedWeaponReachReduction) {
        Item item = source.getMainHandStack().getItem();
        if (item instanceof RangedWeaponItem && source.canUseRangedWeapon((RangedWeaponItem)item)) {
            int i = ((RangedWeaponItem)item).getRange() - rangedWeaponReachReduction;
            return source.isInRange(target, i);
        }
        return LookTargetUtil.isTargetWithinMeleeRange(source, target);
    }

    public static boolean isTargetWithinMeleeRange(MobEntity source, LivingEntity target) {
        double d = source.squaredDistanceTo(target.getX(), target.getY(), target.getZ());
        return d <= source.squaredAttackRange(target);
    }

    /**
     * Checks if an entity can be a new attack target for the source entity.
     * 
     * @param source the source entity
     * @param target the attack target candidate
     * @param extraDistance the max distance this new target can be farther compared to the existing target
     */
    public static boolean isNewTargetTooFar(LivingEntity source, LivingEntity target, double extraDistance) {
        Optional<LivingEntity> optional = source.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET);
        if (!optional.isPresent()) {
            return false;
        }
        double d = source.squaredDistanceTo(optional.get().getPos());
        double e = source.squaredDistanceTo(target.getPos());
        return e > d + extraDistance * extraDistance;
    }

    public static boolean isVisibleInMemory(LivingEntity source, LivingEntity target) {
        Brain<List<LivingEntity>> brain = source.getBrain();
        if (!brain.hasMemoryModule(MemoryModuleType.VISIBLE_MOBS)) {
            return false;
        }
        return brain.getOptionalMemory(MemoryModuleType.VISIBLE_MOBS).get().contains(target);
    }

    public static LivingEntity getCloserEntity(LivingEntity source, Optional<LivingEntity> first, LivingEntity second) {
        if (!first.isPresent()) {
            return second;
        }
        return LookTargetUtil.getCloserEntity(source, first.get(), second);
    }

    public static LivingEntity getCloserEntity(LivingEntity source, LivingEntity first, LivingEntity second) {
        Vec3d vec3d = first.getPos();
        Vec3d vec3d2 = second.getPos();
        return source.squaredDistanceTo(vec3d) < source.squaredDistanceTo(vec3d2) ? first : second;
    }

    public static Optional<LivingEntity> getEntity(LivingEntity entity2, MemoryModuleType<UUID> uuidMemoryModule) {
        Optional<UUID> optional = entity2.getBrain().getOptionalMemory(uuidMemoryModule);
        return optional.map(uUID -> ((ServerWorld)livingEntity.world).getEntity((UUID)uUID)).map(entity -> entity instanceof LivingEntity ? (LivingEntity)entity : null);
    }

    public static Stream<VillagerEntity> streamSeenVillagers(VillagerEntity villager, Predicate<VillagerEntity> filter) {
        return villager.getBrain().getOptionalMemory(MemoryModuleType.MOBS).map(list -> list.stream().filter(livingEntity -> livingEntity instanceof VillagerEntity && livingEntity != villager).map(livingEntity -> (VillagerEntity)livingEntity).filter(LivingEntity::isAlive).filter(filter)).orElseGet(Stream::empty);
    }

    @Nullable
    public static Vec3d find(PathAwareEntity entity, int horizontalRange, int verticalRange) {
        Vec3d vec3d = NoPenaltyTargeting.find(entity, horizontalRange, verticalRange);
        int i = 0;
        while (vec3d != null && !entity.world.getBlockState(new BlockPos(vec3d)).canPathfindThrough(entity.world, new BlockPos(vec3d), NavigationType.WATER) && i++ < 10) {
            vec3d = NoPenaltyTargeting.find(entity, horizontalRange, verticalRange);
        }
        return vec3d;
    }
}

