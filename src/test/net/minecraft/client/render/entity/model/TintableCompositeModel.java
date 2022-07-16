/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

@Environment(value=EnvType.CLIENT)
public abstract class TintableCompositeModel<E extends Entity>
extends SinglePartEntityModel<E> {
    private float redMultiplier = 1.0f;
    private float greenMultiplier = 1.0f;
    private float blueMultiplier = 1.0f;

    public void setColorMultiplier(float red, float green, float blue) {
        this.redMultiplier = red;
        this.greenMultiplier = green;
        this.blueMultiplier = blue;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        super.render(matrices, vertices, light, overlay, this.redMultiplier * red, this.greenMultiplier * green, this.blueMultiplier * blue, alpha);
    }
}

