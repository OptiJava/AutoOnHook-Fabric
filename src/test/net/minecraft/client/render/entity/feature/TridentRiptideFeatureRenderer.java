/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

@Environment(value=EnvType.CLIENT)
public class TridentRiptideFeatureRenderer<T extends LivingEntity>
extends FeatureRenderer<T, PlayerEntityModel<T>> {
    public static final Identifier TEXTURE = new Identifier("textures/entity/trident_riptide.png");
    public static final String BOX = "box";
    private final ModelPart aura;

    public TridentRiptideFeatureRenderer(FeatureRendererContext<T, PlayerEntityModel<T>> context, EntityModelLoader loader) {
        super(context);
        ModelPart modelPart = loader.getModelPart(EntityModelLayers.SPIN_ATTACK);
        this.aura = modelPart.getChild(BOX);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild(BOX, ModelPartBuilder.create().uv(0, 0).cuboid(-8.0f, -16.0f, -8.0f, 16.0f, 32.0f, 16.0f), ModelTransform.NONE);
        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
        if (!((LivingEntity)livingEntity).isUsingRiptide()) {
            return;
        }
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE));
        for (int m = 0; m < 3; ++m) {
            matrixStack.push();
            float n = j * (float)(-(45 + m * 5));
            matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(n));
            float o = 0.75f * (float)m;
            matrixStack.scale(o, o, o);
            matrixStack.translate(0.0, -0.2f + 0.6f * (float)m, 0.0);
            this.aura.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV);
            matrixStack.pop();
        }
    }
}

