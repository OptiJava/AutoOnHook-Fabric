/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombieBaseEntityRenderer;
import net.minecraft.client.render.entity.feature.DrownedOverlayFeatureRenderer;
import net.minecraft.client.render.entity.model.DrownedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

@Environment(value=EnvType.CLIENT)
public class DrownedEntityRenderer
extends ZombieBaseEntityRenderer<DrownedEntity, DrownedEntityModel<DrownedEntity>> {
    private static final Identifier TEXTURE = new Identifier("textures/entity/zombie/drowned.png");

    public DrownedEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new DrownedEntityModel(context.getPart(EntityModelLayers.DROWNED)), new DrownedEntityModel(context.getPart(EntityModelLayers.DROWNED_INNER_ARMOR)), new DrownedEntityModel(context.getPart(EntityModelLayers.DROWNED_OUTER_ARMOR)));
        this.addFeature(new DrownedOverlayFeatureRenderer<DrownedEntity>(this, context.getModelLoader()));
    }

    @Override
    public Identifier getTexture(ZombieEntity zombieEntity) {
        return TEXTURE;
    }

    @Override
    protected void setupTransforms(DrownedEntity drownedEntity, MatrixStack matrixStack, float f, float g, float h) {
        super.setupTransforms(drownedEntity, matrixStack, f, g, h);
        float i = drownedEntity.getLeaningPitch(h);
        if (i > 0.0f) {
            matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(MathHelper.lerp(i, drownedEntity.getPitch(), -10.0f - drownedEntity.getPitch())));
        }
    }
}

