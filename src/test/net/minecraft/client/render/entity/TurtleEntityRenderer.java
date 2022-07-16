/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.TurtleEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class TurtleEntityRenderer
extends MobEntityRenderer<TurtleEntity, TurtleEntityModel<TurtleEntity>> {
    private static final Identifier TEXTURE = new Identifier("textures/entity/turtle/big_sea_turtle.png");

    public TurtleEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new TurtleEntityModel(context.getPart(EntityModelLayers.TURTLE)), 0.7f);
    }

    @Override
    public void render(TurtleEntity turtleEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        if (turtleEntity.isBaby()) {
            this.shadowRadius *= 0.5f;
        }
        super.render(turtleEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(TurtleEntity turtleEntity) {
        return TEXTURE;
    }
}

