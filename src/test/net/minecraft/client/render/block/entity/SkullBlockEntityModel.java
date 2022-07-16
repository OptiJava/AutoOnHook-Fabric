/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.RenderLayer;

@Environment(value=EnvType.CLIENT)
public abstract class SkullBlockEntityModel
extends Model {
    public SkullBlockEntityModel() {
        super(RenderLayer::getEntityTranslucent);
    }

    public abstract void setHeadRotation(float var1, float var2, float var3);
}

