/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.IllagerEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.IllagerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class EvokerEntityRenderer<T extends SpellcastingIllagerEntity>
extends IllagerEntityRenderer<T> {
    private static final Identifier TEXTURE = new Identifier("textures/entity/illager/evoker.png");

    public EvokerEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new IllagerEntityModel(context.getPart(EntityModelLayers.EVOKER)), 0.5f);
        this.addFeature(new HeldItemFeatureRenderer<T, IllagerEntityModel<T>>(this){

            @Override
            public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T spellcastingIllagerEntity, float f, float g, float h, float j, float k, float l) {
                if (((SpellcastingIllagerEntity)spellcastingIllagerEntity).isSpellcasting()) {
                    super.render(matrixStack, vertexConsumerProvider, i, spellcastingIllagerEntity, f, g, h, j, k, l);
                }
            }
        });
    }

    @Override
    public Identifier getTexture(T spellcastingIllagerEntity) {
        return TEXTURE;
    }
}

