/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.ai.brain.sensor;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.NearestVisibleLivingEntitySensor;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.tag.EntityTypeTags;

public class AxolotlAttackablesSensor
extends NearestVisibleLivingEntitySensor {
    public static final float field_30248 = 8.0f;

    @Override
    protected boolean matches(LivingEntity entity, LivingEntity target) {
        if (Sensor.testAttackableTargetPredicate(entity, target) && (this.isAlwaysHostileTo(target) || this.canHunt(entity, target))) {
            return this.isInRange(entity, target) && target.isInsideWaterOrBubbleColumn();
        }
        return false;
    }

    private boolean canHunt(LivingEntity axolotl, LivingEntity target) {
        return !axolotl.getBrain().hasMemoryModule(MemoryModuleType.HAS_HUNTING_COOLDOWN) && EntityTypeTags.AXOLOTL_HUNT_TARGETS.contains(target.getType());
    }

    private boolean isAlwaysHostileTo(LivingEntity axolotl) {
        return EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES.contains(axolotl.getType());
    }

    private boolean isInRange(LivingEntity axolotl, LivingEntity target) {
        return target.squaredDistanceTo(axolotl) <= 64.0;
    }

    @Override
    protected MemoryModuleType<LivingEntity> getOutputMemoryModule() {
        return MemoryModuleType.NEAREST_ATTACKABLE;
    }
}

