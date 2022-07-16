/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class WolfCollarFeatureRenderer
extends FeatureRenderer<WolfEntity, WolfEntityModel<WolfEntity>> {
    private static final Identifier SKIN = new Identifier("textures/entity/wolf/wolf_collar.png");

    public WolfCollarFeatureRenderer(FeatureRendererContext<WolfEntity, WolfEntityModel<WolfEntity>> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, WolfEntity wolfEntity, float f, float g, float h, float j, float k, float l) {
        if (!wolfEntity.isTamed() || wolfEntity.isInvisible()) {
            return;
        }
        float[] fs = wolfEntity.getCollarColor().getColorComponents();
        WolfCollarFeatureRenderer.renderModel(this.getContextModel(), SKIN, matrixStack, vertexConsumerProvider, i, wolfEntity, fs[0], fs[1], fs[2]);
    }
}

