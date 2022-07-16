/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

@Environment(value=EnvType.CLIENT)
public class Deadmau5FeatureRenderer
extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public Deadmau5FeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, float h, float j, float k, float l) {
        if (!"deadmau5".equals(abstractClientPlayerEntity.getName().getString()) || !abstractClientPlayerEntity.hasSkinTexture() || abstractClientPlayerEntity.isInvisible()) {
            return;
        }
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(abstractClientPlayerEntity.getSkinTexture()));
        int m = LivingEntityRenderer.getOverlay(abstractClientPlayerEntity, 0.0f);
        for (int n = 0; n < 2; ++n) {
            float o = MathHelper.lerp(h, abstractClientPlayerEntity.prevYaw, abstractClientPlayerEntity.getYaw()) - MathHelper.lerp(h, abstractClientPlayerEntity.prevBodyYaw, abstractClientPlayerEntity.bodyYaw);
            float p = MathHelper.lerp(h, abstractClientPlayerEntity.prevPitch, abstractClientPlayerEntity.getPitch());
            matrixStack.push();
            matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(o));
            matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(p));
            matrixStack.translate(0.375f * (float)(n * 2 - 1), 0.0, 0.0);
            matrixStack.translate(0.0, -0.375, 0.0);
            matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-p));
            matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-o));
            float q = 1.3333334f;
            matrixStack.scale(1.3333334f, 1.3333334f, 1.3333334f);
            ((PlayerEntityModel)this.getContextModel()).renderEars(matrixStack, vertexConsumer, i, m);
            matrixStack.pop();
        }
    }
}

