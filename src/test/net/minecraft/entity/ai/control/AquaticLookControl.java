/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.ai.control;

import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.MathHelper;

public class AquaticLookControl
extends LookControl {
    private final int maxYawDifference;
    private static final int ADDED_PITCH = 10;
    private static final int ADDED_YAW = 20;

    public AquaticLookControl(MobEntity entity, int maxYawDifference) {
        super(entity);
        this.maxYawDifference = maxYawDifference;
    }

    @Override
    public void tick() {
        if (this.active) {
            this.active = false;
            this.getTargetYaw().ifPresent(float_ -> {
                this.entity.headYaw = this.changeAngle(this.entity.headYaw, float_.floatValue() + 20.0f, this.yawSpeed);
            });
            this.getTargetPitch().ifPresent(float_ -> this.entity.setPitch(this.changeAngle(this.entity.getPitch(), float_.floatValue() + 10.0f, this.pitchSpeed)));
        } else {
            if (this.entity.getNavigation().isIdle()) {
                this.entity.setPitch(this.changeAngle(this.entity.getPitch(), 0.0f, 5.0f));
            }
            this.entity.headYaw = this.changeAngle(this.entity.headYaw, this.entity.bodyYaw, this.yawSpeed);
        }
        float f = MathHelper.wrapDegrees(this.entity.headYaw - this.entity.bodyYaw);
        if (f < (float)(-this.maxYawDifference)) {
            this.entity.bodyYaw -= 4.0f;
        } else if (f > (float)this.maxYawDifference) {
            this.entity.bodyYaw += 4.0f;
        }
    }
}
