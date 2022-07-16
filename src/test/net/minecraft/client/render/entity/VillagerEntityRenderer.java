/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerClothingFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class VillagerEntityRenderer
extends MobEntityRenderer<VillagerEntity, VillagerResemblingModel<VillagerEntity>> {
    private static final Identifier TEXTURE = new Identifier("textures/entity/villager/villager.png");

    public VillagerEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new VillagerResemblingModel(context.getPart(EntityModelLayers.VILLAGER)), 0.5f);
        this.addFeature(new HeadFeatureRenderer<VillagerEntity, VillagerResemblingModel<VillagerEntity>>(this, context.getModelLoader()));
        this.addFeature(new VillagerClothingFeatureRenderer<VillagerEntity, VillagerResemblingModel<VillagerEntity>>(this, context.getResourceManager(), "villager"));
        this.addFeature(new VillagerHeldItemFeatureRenderer<VillagerEntity, VillagerResemblingModel<VillagerEntity>>(this));
    }

    @Override
    public Identifier getTexture(VillagerEntity villagerEntity) {
        return TEXTURE;
    }

    @Override
    protected void scale(VillagerEntity villagerEntity, MatrixStack matrixStack, float f) {
        float g = 0.9375f;
        if (villagerEntity.isBaby()) {
            g = (float)((double)g * 0.5);
            this.shadowRadius = 0.25f;
        } else {
            this.shadowRadius = 0.5f;
        }
        matrixStack.scale(g, g, g);
    }
}

