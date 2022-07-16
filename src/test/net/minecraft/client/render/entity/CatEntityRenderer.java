/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.CatCollarFeatureRenderer;
import net.minecraft.client.render.entity.model.CatEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

@Environment(value=EnvType.CLIENT)
public class CatEntityRenderer
extends MobEntityRenderer<CatEntity, CatEntityModel<CatEntity>> {
    public CatEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new CatEntityModel(context.getPart(EntityModelLayers.CAT)), 0.4f);
        this.addFeature(new CatCollarFeatureRenderer(this, context.getModelLoader()));
    }

    @Override
    public Identifier getTexture(CatEntity catEntity) {
        return catEntity.getTexture();
    }

    @Override
    protected void scale(CatEntity catEntity, MatrixStack matrixStack, float f) {
        super.scale(catEntity, matrixStack, f);
        matrixStack.scale(0.8f, 0.8f, 0.8f);
    }

    @Override
    protected void setupTransforms(CatEntity catEntity, MatrixStack matrixStack, float f, float g, float h) {
        super.setupTransforms(catEntity, matrixStack, f, g, h);
        float i = catEntity.getSleepAnimation(h);
        if (i > 0.0f) {
            matrixStack.translate(0.4f * i, 0.15f * i, 0.1f * i);
            matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(MathHelper.lerpAngleDegrees(i, 0.0f, 90.0f)));
            BlockPos blockPos = catEntity.getBlockPos();
            List<PlayerEntity> list = catEntity.world.getNonSpectatingEntities(PlayerEntity.class, new Box(blockPos).expand(2.0, 2.0, 2.0));
            for (PlayerEntity playerEntity : list) {
                if (!playerEntity.isSleeping()) continue;
                matrixStack.translate(0.15f * i, 0.0, 0.0);
                break;
            }
        }
    }
}

