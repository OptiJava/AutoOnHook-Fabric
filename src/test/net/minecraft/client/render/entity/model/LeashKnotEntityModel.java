/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.Entity;

/**
 * Represents the model of a leash-knot-like entity.
 * 
 * <div class="fabric">
 * <table border=1>
 * <caption>Model parts of this model</caption>
 * <tr>
 *   <th>Part Name</th><th>Parent</th><th>Corresponding Field</th>
 * </tr>
 * <tr>
 *   <td>{@value KNOT}</td><td>{@linkplain #root Root part}</td><td>{@link #knot}</td>
 * </tr>
 * </table>
 * </div>
 */
@Environment(value=EnvType.CLIENT)
public class LeashKnotEntityModel<T extends Entity>
extends SinglePartEntityModel<T> {
    /**
     * The key of the knot model part, whose value is {@value}.
     */
    private static final String KNOT = "knot";
    private final ModelPart root;
    private final ModelPart knot;

    public LeashKnotEntityModel(ModelPart root) {
        this.root = root;
        this.knot = root.getChild(KNOT);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild(KNOT, ModelPartBuilder.create().uv(0, 0).cuboid(-3.0f, -8.0f, -3.0f, 6.0f, 8.0f, 6.0f), ModelTransform.NONE);
        return TexturedModelData.of(modelData, 32, 32);
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.knot.yaw = headYaw * ((float)Math.PI / 180);
        this.knot.pitch = headPitch * ((float)Math.PI / 180);
    }
}

