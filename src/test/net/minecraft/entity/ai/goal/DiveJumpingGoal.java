/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;

public abstract class DiveJumpingGoal
extends Goal {
    public DiveJumpingGoal() {
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.JUMP));
    }
}

