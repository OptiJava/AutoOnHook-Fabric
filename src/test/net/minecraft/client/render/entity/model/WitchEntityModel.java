/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * Represents the model of a witch resembling entity.
 * 
 * <div class="fabric">
 * <table border=1>
 * <caption>Model parts of this model</caption>
 * <tr>
 *   <th>Part Name</th><th>Parent</th><th>Corresponding Field</th>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#HEAD}</td><td>{@linkplain #root Root part}</td><td>{@link #head}</td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#HAT}</td><td>{@value EntityModelPartNames#HEAD}</td><td>{@link #hat}</td>
 * </tr>
 * <tr>
 *   <td>{@code hat2}</td><td>{@value EntityModelPartNames#HAT}</td><td></td>
 * </tr>
 * <tr>
 *   <td>{@code hat3}</td><td>{@code hat2}</td><td></td>
 * </tr>
 * <tr>
 *   <td>{@code hat4}</td><td>{@code hat3}</td><td></td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#NOSE}</td><td>{@value EntityModelPartNames#HEAD}</td><td>{@link #nose}</td>
 * </tr>
 * <tr>
 *   <td>{@code mole}</td><td>{@value EntityModelPartNames#NOSE}</td><td></td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#BODY}</td><td>{@linkplain #root Root part}</td><td></td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#JACKET}</td><td>{@value EntityModelPartNames#BODY}</td><td></td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#ARMS}</td><td>{@linkplain #root Root part}</td><td></td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#RIGHT_LEG}</td><td>{@linkplain #root Root part}</td><td>{@link #rightLeg}</td>
 * </tr>
 * <tr>
 *   <td>{@value EntityModelPartNames#LEFT_LEG}</td><td>{@linkplain #root Root part}</td><td>{@link #leftLeg}</td>
 * </tr>
 * </table>
 * </div>
 */
@Environment(value=EnvType.CLIENT)
public class WitchEntityModel<T extends Entity>
extends VillagerResemblingModel<T> {
    private boolean liftingNose;

    public WitchEntityModel(ModelPart modelPart) {
        super(modelPart);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = VillagerResemblingModel.getModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData modelPartData2 = modelPartData.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -10.0f, -4.0f, 8.0f, 10.0f, 8.0f), ModelTransform.NONE);
        ModelPartData modelPartData3 = modelPartData2.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create().uv(0, 64).cuboid(0.0f, 0.0f, 0.0f, 10.0f, 2.0f, 10.0f), ModelTransform.pivot(-5.0f, -10.03125f, -5.0f));
        ModelPartData modelPartData4 = modelPartData3.addChild("hat2", ModelPartBuilder.create().uv(0, 76).cuboid(0.0f, 0.0f, 0.0f, 7.0f, 4.0f, 7.0f), ModelTransform.of(1.75f, -4.0f, 2.0f, -0.05235988f, 0.0f, 0.02617994f));
        ModelPartData modelPartData5 = modelPartData4.addChild("hat3", ModelPartBuilder.create().uv(0, 87).cuboid(0.0f, 0.0f, 0.0f, 4.0f, 4.0f, 4.0f), ModelTransform.of(1.75f, -4.0f, 2.0f, -0.10471976f, 0.0f, 0.05235988f));
        modelPartData5.addChild("hat4", ModelPartBuilder.create().uv(0, 95).cuboid(0.0f, 0.0f, 0.0f, 1.0f, 2.0f, 1.0f, new Dilation(0.25f)), ModelTransform.of(1.75f, -2.0f, 2.0f, -0.20943952f, 0.0f, 0.10471976f));
        ModelPartData modelPartData6 = modelPartData2.getChild(EntityModelPartNames.NOSE);
        modelPartData6.addChild("mole", ModelPartBuilder.create().uv(0, 0).cuboid(0.0f, 3.0f, -6.75f, 1.0f, 1.0f, 1.0f, new Dilation(-0.25f)), ModelTransform.pivot(0.0f, -2.0f, 0.0f));
        return TexturedModelData.of(modelData, 64, 128);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        super.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
        this.nose.setPivot(0.0f, -2.0f, 0.0f);
        float f = 0.01f * (float)(((Entity)entity).getId() % 10);
        this.nose.pitch = MathHelper.sin((float)((Entity)entity).age * f) * 4.5f * ((float)Math.PI / 180);
        this.nose.yaw = 0.0f;
        this.nose.roll = MathHelper.cos((float)((Entity)entity).age * f) * 2.5f * ((float)Math.PI / 180);
        if (this.liftingNose) {
            this.nose.setPivot(0.0f, 1.0f, -1.5f);
            this.nose.pitch = -0.9f;
        }
    }

    public ModelPart getNose() {
        return this.nose;
    }

    public void setLiftingNose(boolean liftingNose) {
        this.liftingNose = liftingNose;
    }
}

