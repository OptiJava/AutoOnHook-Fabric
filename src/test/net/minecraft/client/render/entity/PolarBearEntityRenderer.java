/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PolarBearEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class PolarBearEntityRenderer
extends MobEntityRenderer<PolarBearEntity, PolarBearEntityModel<PolarBearEntity>> {
    private static final Identifier TEXTURE = new Identifier("textures/entity/bear/polarbear.png");

    public PolarBearEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new PolarBearEntityModel(context.getPart(EntityModelLayers.POLAR_BEAR)), 0.9f);
    }

    @Override
    public Identifier getTexture(PolarBearEntity polarBearEntity) {
        return TEXTURE;
    }

    @Override
    protected void scale(PolarBearEntity polarBearEntity, MatrixStack matrixStack, float f) {
        matrixStack.scale(1.2f, 1.2f, 1.2f);
        super.scale(polarBearEntity, matrixStack, f);
    }
}

