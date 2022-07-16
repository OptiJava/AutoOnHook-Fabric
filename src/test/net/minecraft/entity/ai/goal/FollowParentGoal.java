/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.ai.goal;

import java.util.List;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.AnimalEntity;

public class FollowParentGoal
extends Goal {
    public static final int field_30209 = 8;
    public static final int field_30210 = 4;
    public static final int field_30211 = 3;
    private final AnimalEntity animal;
    private AnimalEntity parent;
    private final double speed;
    private int delay;

    public FollowParentGoal(AnimalEntity animal, double speed) {
        this.animal = animal;
        this.speed = speed;
    }

    @Override
    public boolean canStart() {
        if (this.animal.getBreedingAge() >= 0) {
            return false;
        }
        List<?> list = this.animal.world.getNonSpectatingEntities(this.animal.getClass(), this.animal.getBoundingBox().expand(8.0, 4.0, 8.0));
        AnimalEntity animalEntity = null;
        double d = Double.MAX_VALUE;
        for (AnimalEntity animalEntity2 : list) {
            double e;
            if (animalEntity2.getBreedingAge() < 0 || (e = this.animal.squaredDistanceTo(animalEntity2)) > d) continue;
            d = e;
            animalEntity = animalEntity2;
        }
        if (animalEntity == null) {
            return false;
        }
        if (d < 9.0) {
            return false;
        }
        this.parent = animalEntity;
        return true;
    }

    @Override
    public boolean shouldContinue() {
        if (this.animal.getBreedingAge() >= 0) {
            return false;
        }
        if (!this.parent.isAlive()) {
            return false;
        }
        double d = this.animal.squaredDistanceTo(this.parent);
        return !(d < 9.0) && !(d > 256.0);
    }

    @Override
    public void start() {
        this.delay = 0;
    }

    @Override
    public void stop() {
        this.parent = null;
    }

    @Override
    public void tick() {
        if (--this.delay > 0) {
            return;
        }
        this.delay = 10;
        this.animal.getNavigation().startMovingTo(this.parent, this.speed);
    }
}

