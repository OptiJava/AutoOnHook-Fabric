/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.ai.brain;

import java.util.List;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class EntityLookTarget
implements LookTarget {
    private final Entity entity;
    private final boolean useEyeHeight;

    public EntityLookTarget(Entity entity, boolean useEyeHeight) {
        this.entity = entity;
        this.useEyeHeight = useEyeHeight;
    }

    @Override
    public Vec3d getPos() {
        return this.useEyeHeight ? this.entity.getPos().add(0.0, this.entity.getStandingEyeHeight(), 0.0) : this.entity.getPos();
    }

    @Override
    public BlockPos getBlockPos() {
        return this.entity.getBlockPos();
    }

    @Override
    public boolean isSeenBy(LivingEntity entity) {
        if (this.entity instanceof LivingEntity) {
            Optional<List<LivingEntity>> optional = entity.getBrain().getOptionalMemory(MemoryModuleType.VISIBLE_MOBS);
            return this.entity.isAlive() && optional.isPresent() && optional.get().contains(this.entity);
        }
        return true;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public String toString() {
        return "EntityTracker for " + this.entity;
    }
}
