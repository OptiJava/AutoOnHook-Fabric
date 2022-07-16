/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.util.math.MathHelper;

/**
 * Represents the model of a {@linkplain RabbitEntity}.
 * 
 * <div class="fabric">
 * <table border=1>
 * <caption>Model parts of this model</caption>
 * <tr>
 *   <th>Part Name</th><th>Parent</th><th>Corresponding Field</th>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#LEFT_HIND_FOOT}</td><td>Root part</td><td>{@link #leftHindLeg}</td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#RIGHT_HIND_FOOT}</td><td>Root part</td><td>{@link #rightHindLeg}</td>
 * </tr>
 * <tr>
 *   <td>{@value #LEFT_HAUNCH}</td><td>Root part</td><td>{@link #leftHaunch}</td>
 * </tr>
 * <tr>
 *   <td>{@value #RIGHT_HAUNCH}</td><td>Root part</td><td>{@link #rightHaunch}</td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#BODY}</td><td>Root part</td><td>{@link #body}</td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#LEFT_FRONT_LEG}</td><td>Root part</td><td>{@link #leftFrontLeg}</td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#RIGHT_FRONT_LEG}</td><td>Root part</td><td>{@link #rightFrontLeg}</td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#HEAD}</td><td>Root part</td><td>{@link #head}</td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#RIGHT_EAR}</td><td>Root part</td><td>{@link #rightEar}</td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#LEFT_EAR}</td><td>Root part</td><td>{@link #leftEar}</td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#TAIL}</td><td>Root part</td><td>{@link #tail}</td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#NOSE}</td><td>Root part</td><td>{@link #nose}</td>
 * </tr>
 * </table>
 * </div>
 */
@Environment(value=EnvType.CLIENT)
public class RabbitEntityModel<T extends RabbitEntity>
extends EntityModel<T> {
    private static final float HAUNCH_JUMP_PITCH_MULTIPLIER = 50.0f;
    private static final float FRONT_LEGS_JUMP_PITCH_MULTIPLIER = -40.0f;
    /**
     * The key of the left haunch model part, whose value is {@value}.
     */
    private static final String LEFT_HAUNCH = "left_haunch";
    /**
     * The key of the right haunch model part, whose value is {@value}.
     */
    private static final String RIGHT_HAUNCH = "right_haunch";
    private final ModelPart leftHindLeg;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHaunch;
    private final ModelPart rightHaunch;
    private final ModelPart body;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart head;
    private final ModelPart rightEar;
    private final ModelPart leftEar;
    private final ModelPart tail;
    private final ModelPart nose;
    private float jumpProgress;
    private static final float SCALE = 0.6f;

    public RabbitEntityModel(ModelPart root) {
        this.leftHindLeg = root.getChild(EntityModelPartNames.LEFT_HIND_FOOT);
        this.rightHindLeg = root.getChild(EntityModelPartNames.RIGHT_HIND_FOOT);
        this.leftHaunch = root.getChild(LEFT_HAUNCH);
        this.rightHaunch = root.getChild(RIGHT_HAUNCH);
        this.body = root.getChild(EntityModelPartNames.BODY);
        this.leftFrontLeg = root.getChild(EntityModelPartNames.LEFT_FRONT_LEG);
        this.rightFrontLeg = root.getChild(EntityModelPartNames.RIGHT_FRONT_LEG);
        this.head = root.getChild(EntityModelPartNames.HEAD);
        this.rightEar = root.getChild(EntityModelPartNames.RIGHT_EAR);
        this.leftEar = root.getChild(EntityModelPartNames.LEFT_EAR);
        this.tail = root.getChild(EntityModelPartNames.TAIL);
        this.nose = root.getChild(EntityModelPartNames.NOSE);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild(EntityModelPartNames.LEFT_HIND_FOOT, ModelPartBuilder.create().uv(26, 24).cuboid(-1.0f, 5.5f, -3.7f, 2.0f, 1.0f, 7.0f), ModelTransform.pivot(3.0f, 17.5f, 3.7f));
        modelPartData.addChild(EntityModelPartNames.RIGHT_HIND_FOOT, ModelPartBuilder.create().uv(8, 24).cuboid(-1.0f, 5.5f, -3.7f, 2.0f, 1.0f, 7.0f), ModelTransform.pivot(-3.0f, 17.5f, 3.7f));
        modelPartData.addChild(LEFT_HAUNCH, ModelPartBuilder.create().uv(30, 15).cuboid(-1.0f, 0.0f, 0.0f, 2.0f, 4.0f, 5.0f), ModelTransform.of(3.0f, 17.5f, 3.7f, -0.34906584f, 0.0f, 0.0f));
        modelPartData.addChild(RIGHT_HAUNCH, ModelPartBuilder.create().uv(16, 15).cuboid(-1.0f, 0.0f, 0.0f, 2.0f, 4.0f, 5.0f), ModelTransform.of(-3.0f, 17.5f, 3.7f, -0.34906584f, 0.0f, 0.0f));
        modelPartData.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 0).cuboid(-3.0f, -2.0f, -10.0f, 6.0f, 5.0f, 10.0f), ModelTransform.of(0.0f, 19.0f, 8.0f, -0.34906584f, 0.0f, 0.0f));
        modelPartData.addChild(EntityModelPartNames.LEFT_FRONT_LEG, ModelPartBuilder.create().uv(8, 15).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f), ModelTransform.of(3.0f, 17.0f, -1.0f, -0.17453292f, 0.0f, 0.0f));
        modelPartData.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, ModelPartBuilder.create().uv(0, 15).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f), ModelTransform.of(-3.0f, 17.0f, -1.0f, -0.17453292f, 0.0f, 0.0f));
        modelPartData.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(32, 0).cuboid(-2.5f, -4.0f, -5.0f, 5.0f, 4.0f, 5.0f), ModelTransform.pivot(0.0f, 16.0f, -1.0f));
        modelPartData.addChild(EntityModelPartNames.RIGHT_EAR, ModelPartBuilder.create().uv(52, 0).cuboid(-2.5f, -9.0f, -1.0f, 2.0f, 5.0f, 1.0f), ModelTransform.of(0.0f, 16.0f, -1.0f, 0.0f, -0.2617994f, 0.0f));
        modelPartData.addChild(EntityModelPartNames.LEFT_EAR, ModelPartBuilder.create().uv(58, 0).cuboid(0.5f, -9.0f, -1.0f, 2.0f, 5.0f, 1.0f), ModelTransform.of(0.0f, 16.0f, -1.0f, 0.0f, 0.2617994f, 0.0f));
        modelPartData.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create().uv(52, 6).cuboid(-1.5f, -1.5f, 0.0f, 3.0f, 3.0f, 2.0f), ModelTransform.of(0.0f, 20.0f, 7.0f, -0.3490659f, 0.0f, 0.0f));
        modelPartData.addChild(EntityModelPartNames.NOSE, ModelPartBuilder.create().uv(32, 9).cuboid(-0.5f, -2.5f, -5.5f, 1.0f, 1.0f, 1.0f), ModelTransform.pivot(0.0f, 16.0f, -1.0f));
        return TexturedModelData.of(modelData, 64, 32);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        if (this.child) {
            float f = 1.5f;
            matrices.push();
            matrices.scale(0.56666666f, 0.56666666f, 0.56666666f);
            matrices.translate(0.0, 1.375, 0.125);
            ImmutableList.of(this.head, this.leftEar, this.rightEar, this.nose).forEach(part -> part.render(matrices, vertices, light, overlay, red, green, blue, alpha));
            matrices.pop();
            matrices.push();
            matrices.scale(0.4f, 0.4f, 0.4f);
            matrices.translate(0.0, 2.25, 0.0);
            ImmutableList.of(this.leftHindLeg, this.rightHindLeg, this.leftHaunch, this.rightHaunch, this.body, this.leftFrontLeg, this.rightFrontLeg, this.tail).forEach(part -> part.render(matrices, vertices, light, overlay, red, green, blue, alpha));
            matrices.pop();
        } else {
            matrices.push();
            matrices.scale(0.6f, 0.6f, 0.6f);
            matrices.translate(0.0, 1.0, 0.0);
            ImmutableList.of(this.leftHindLeg, this.rightHindLeg, this.leftHaunch, this.rightHaunch, this.body, this.leftFrontLeg, this.rightFrontLeg, this.head, this.rightEar, this.leftEar, this.tail, this.nose, new ModelPart[0]).forEach(part -> part.render(matrices, vertices, light, overlay, red, green, blue, alpha));
            matrices.pop();
        }
    }

    @Override
    public void setAngles(T rabbitEntity, float f, float g, float h, float i, float j) {
        float k = h - (float)((RabbitEntity)rabbitEntity).age;
        this.nose.pitch = j * ((float)Math.PI / 180);
        this.head.pitch = j * ((float)Math.PI / 180);
        this.rightEar.pitch = j * ((float)Math.PI / 180);
        this.leftEar.pitch = j * ((float)Math.PI / 180);
        this.nose.yaw = i * ((float)Math.PI / 180);
        this.head.yaw = i * ((float)Math.PI / 180);
        this.rightEar.yaw = this.nose.yaw - 0.2617994f;
        this.leftEar.yaw = this.nose.yaw + 0.2617994f;
        this.jumpProgress = MathHelper.sin(((RabbitEntity)rabbitEntity).getJumpProgress(k) * (float)Math.PI);
        this.leftHaunch.pitch = (this.jumpProgress * 50.0f - 21.0f) * ((float)Math.PI / 180);
        this.rightHaunch.pitch = (this.jumpProgress * 50.0f - 21.0f) * ((float)Math.PI / 180);
        this.leftHindLeg.pitch = this.jumpProgress * 50.0f * ((float)Math.PI / 180);
        this.rightHindLeg.pitch = this.jumpProgress * 50.0f * ((float)Math.PI / 180);
        this.leftFrontLeg.pitch = (this.jumpProgress * -40.0f - 11.0f) * ((float)Math.PI / 180);
        this.rightFrontLeg.pitch = (this.jumpProgress * -40.0f - 11.0f) * ((float)Math.PI / 180);
    }

    @Override
    public void animateModel(T rabbitEntity, float f, float g, float h) {
        super.animateModel(rabbitEntity, f, g, h);
        this.jumpProgress = MathHelper.sin(((RabbitEntity)rabbitEntity).getJumpProgress(h) * (float)Math.PI);
    }
}

