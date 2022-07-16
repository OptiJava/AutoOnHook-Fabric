/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.TameableEntity;

public class SitGoal
extends Goal {
    private final TameableEntity tameable;

    public SitGoal(TameableEntity tameable) {
        this.tameable = tameable;
        this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
    }

    @Override
    public boolean shouldContinue() {
        return this.tameable.isSitting();
    }

    @Override
    public boolean canStart() {
        if (!this.tameable.isTamed()) {
            return false;
        }
        if (this.tameable.isInsideWaterOrBubbleColumn()) {
            return false;
        }
        if (!this.tameable.isOnGround()) {
            return false;
        }
        LivingEntity livingEntity = this.tameable.getOwner();
        if (livingEntity == null) {
            return true;
        }
        if (this.tameable.squaredDistanceTo(livingEntity) < 144.0 && livingEntity.getAttacker() != null) {
            return false;
        }
        return this.tameable.isSitting();
    }

    @Override
    public void start() {
        this.tameable.getNavigation().stop();
        this.tameable.setInSittingPose(true);
    }

    @Override
    public void stop() {
        this.tameable.setInSittingPose(false);
    }
}

