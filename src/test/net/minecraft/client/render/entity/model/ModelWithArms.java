/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;

@Environment(value=EnvType.CLIENT)
public interface ModelWithArms {
    public void setArmAngle(Arm var1, MatrixStack var2);
}

