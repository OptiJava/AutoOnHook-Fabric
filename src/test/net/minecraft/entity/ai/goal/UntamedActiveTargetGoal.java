/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.ai.goal;

import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.passive.TameableEntity;
import org.jetbrains.annotations.Nullable;

public class UntamedActiveTargetGoal<T extends LivingEntity>
extends ActiveTargetGoal<T> {
    private final TameableEntity tameable;

    public UntamedActiveTargetGoal(TameableEntity tameable, Class<T> targetClass, boolean checkVisibility, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(tameable, targetClass, 10, checkVisibility, false, targetPredicate);
        this.tameable = tameable;
    }

    @Override
    public boolean canStart() {
        return !this.tameable.isTamed() && super.canStart();
    }

    @Override
    public boolean shouldContinue() {
        if (this.targetPredicate != null) {
            return this.targetPredicate.test(this.mob, this.targetEntity);
        }
        return super.shouldContinue();
    }
}

