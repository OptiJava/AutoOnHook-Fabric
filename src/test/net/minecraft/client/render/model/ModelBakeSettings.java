/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.AffineTransformation;

@Environment(value=EnvType.CLIENT)
public interface ModelBakeSettings {
    default public AffineTransformation getRotation() {
        return AffineTransformation.identity();
    }

    default public boolean isUvLocked() {
        return false;
    }
}

