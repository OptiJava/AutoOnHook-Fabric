/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.SaddleFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PigEntityModel;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class PigEntityRenderer
extends MobEntityRenderer<PigEntity, PigEntityModel<PigEntity>> {
    private static final Identifier TEXTURE = new Identifier("textures/entity/pig/pig.png");

    public PigEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new PigEntityModel(context.getPart(EntityModelLayers.PIG)), 0.7f);
        this.addFeature(new SaddleFeatureRenderer(this, new PigEntityModel(context.getPart(EntityModelLayers.PIG_SADDLE)), new Identifier("textures/entity/pig/pig_saddle.png")));
    }

    @Override
    public Identifier getTexture(PigEntity pigEntity) {
        return TEXTURE;
    }
}

