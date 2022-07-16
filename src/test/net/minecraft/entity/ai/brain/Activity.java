/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.ai.brain;

import net.minecraft.util.registry.Registry;

public class Activity {
    public static final Activity CORE = Activity.register("core");
    public static final Activity IDLE = Activity.register("idle");
    public static final Activity WORK = Activity.register("work");
    public static final Activity PLAY = Activity.register("play");
    public static final Activity REST = Activity.register("rest");
    public static final Activity MEET = Activity.register("meet");
    public static final Activity PANIC = Activity.register("panic");
    public static final Activity RAID = Activity.register("raid");
    public static final Activity PRE_RAID = Activity.register("pre_raid");
    public static final Activity HIDE = Activity.register("hide");
    public static final Activity FIGHT = Activity.register("fight");
    public static final Activity CELEBRATE = Activity.register("celebrate");
    public static final Activity ADMIRE_ITEM = Activity.register("admire_item");
    public static final Activity AVOID = Activity.register("avoid");
    public static final Activity RIDE = Activity.register("ride");
    public static final Activity PLAY_DEAD = Activity.register("play_dead");
    public static final Activity LONG_JUMP = Activity.register("long_jump");
    public static final Activity RAM = Activity.register("ram");
    private final String id;
    private final int hashCode;

    private Activity(String id) {
        this.id = id;
        this.hashCode = id.hashCode();
    }

    public String getId() {
        return this.id;
    }

    private static Activity register(String id) {
        return Registry.register(Registry.ACTIVITY, id, new Activity(id));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Activity activity = (Activity)o;
        return this.id.equals(activity.id);
    }

    public int hashCode() {
        return this.hashCode;
    }

    public String toString() {
        return this.getId();
    }
}

