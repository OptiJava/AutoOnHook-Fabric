/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.data;

import net.minecraft.entity.data.TrackedDataHandler;

public class TrackedData<T> {
    private final int id;
    private final TrackedDataHandler<T> dataType;

    public TrackedData(int id, TrackedDataHandler<T> dataType) {
        this.id = id;
        this.dataType = dataType;
    }

    public int getId() {
        return this.id;
    }

    public TrackedDataHandler<T> getType() {
        return this.dataType;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        TrackedData trackedData = (TrackedData)o;
        return this.id == trackedData.id;
    }

    public int hashCode() {
        return this.id;
    }

    public String toString() {
        return "<entity data: " + this.id + ">";
    }
}

