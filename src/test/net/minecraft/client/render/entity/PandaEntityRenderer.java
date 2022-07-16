/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.PandaHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PandaEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

@Environment(value=EnvType.CLIENT)
public class PandaEntityRenderer
extends MobEntityRenderer<PandaEntity, PandaEntityModel<PandaEntity>> {
    private static final Map<PandaEntity.Gene, Identifier> TEXTURES = Util.make(Maps.newEnumMap(PandaEntity.Gene.class), enumMap -> {
        enumMap.put(PandaEntity.Gene.NORMAL, new Identifier("textures/entity/panda/panda.png"));
        enumMap.put(PandaEntity.Gene.LAZY, new Identifier("textures/entity/panda/lazy_panda.png"));
        enumMap.put(PandaEntity.Gene.WORRIED, new Identifier("textures/entity/panda/worried_panda.png"));
        enumMap.put(PandaEntity.Gene.PLAYFUL, new Identifier("textures/entity/panda/playful_panda.png"));
        enumMap.put(PandaEntity.Gene.BROWN, new Identifier("textures/entity/panda/brown_panda.png"));
        enumMap.put(PandaEntity.Gene.WEAK, new Identifier("textures/entity/panda/weak_panda.png"));
        enumMap.put(PandaEntity.Gene.AGGRESSIVE, new Identifier("textures/entity/panda/aggressive_panda.png"));
    });

    public PandaEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new PandaEntityModel(context.getPart(EntityModelLayers.PANDA)), 0.9f);
        this.addFeature(new PandaHeldItemFeatureRenderer(this));
    }

    @Override
    public Identifier getTexture(PandaEntity pandaEntity) {
        return TEXTURES.getOrDefault((Object)pandaEntity.getProductGene(), TEXTURES.get((Object)PandaEntity.Gene.NORMAL));
    }

    @Override
    protected void setupTransforms(PandaEntity pandaEntity, MatrixStack matrixStack, float f, float g, float h) {
        float j;
        float i;
        float k;
        super.setupTransforms(pandaEntity, matrixStack, f, g, h);
        if (pandaEntity.playingTicks > 0) {
            float l;
            int i2 = pandaEntity.playingTicks;
            int j2 = i2 + 1;
            k = 7.0f;
            float f2 = l = pandaEntity.isBaby() ? 0.3f : 0.8f;
            if (i2 < 8) {
                float m = (float)(90 * i2) / 7.0f;
                float n = (float)(90 * j2) / 7.0f;
                float o = this.method_4086(m, n, j2, h, 8.0f);
                matrixStack.translate(0.0, (l + 0.2f) * (o / 90.0f), 0.0);
                matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-o));
            } else if (i2 < 16) {
                float m = ((float)i2 - 8.0f) / 7.0f;
                float n = 90.0f + 90.0f * m;
                float p = 90.0f + 90.0f * ((float)j2 - 8.0f) / 7.0f;
                float o = this.method_4086(n, p, j2, h, 16.0f);
                matrixStack.translate(0.0, l + 0.2f + (l - 0.2f) * (o - 90.0f) / 90.0f, 0.0);
                matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-o));
            } else if ((float)i2 < 24.0f) {
                float m = ((float)i2 - 16.0f) / 7.0f;
                float n = 180.0f + 90.0f * m;
                float p = 180.0f + 90.0f * ((float)j2 - 16.0f) / 7.0f;
                float o = this.method_4086(n, p, j2, h, 24.0f);
                matrixStack.translate(0.0, l + l * (270.0f - o) / 90.0f, 0.0);
                matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-o));
            } else if (i2 < 32) {
                float m = ((float)i2 - 24.0f) / 7.0f;
                float n = 270.0f + 90.0f * m;
                float p = 270.0f + 90.0f * ((float)j2 - 24.0f) / 7.0f;
                float o = this.method_4086(n, p, j2, h, 32.0f);
                matrixStack.translate(0.0, l * ((360.0f - o) / 90.0f), 0.0);
                matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-o));
            }
        }
        if ((i = pandaEntity.getScaredAnimationProgress(h)) > 0.0f) {
            matrixStack.translate(0.0, 0.8f * i, 0.0);
            matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(MathHelper.lerp(i, pandaEntity.getPitch(), pandaEntity.getPitch() + 90.0f)));
            matrixStack.translate(0.0, -1.0f * i, 0.0);
            if (pandaEntity.isScaredByThunderstorm()) {
                float j3 = (float)(Math.cos((double)pandaEntity.age * 1.25) * Math.PI * (double)0.05f);
                matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(j3));
                if (pandaEntity.isBaby()) {
                    matrixStack.translate(0.0, 0.8f, 0.55f);
                }
            }
        }
        if ((j = pandaEntity.getLieOnBackAnimationProgress(h)) > 0.0f) {
            k = pandaEntity.isBaby() ? 0.5f : 1.3f;
            matrixStack.translate(0.0, k * j, 0.0);
            matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(MathHelper.lerp(j, pandaEntity.getPitch(), pandaEntity.getPitch() + 180.0f)));
        }
    }

    private float method_4086(float f, float g, int i, float h, float j) {
        if ((float)i < j) {
            return MathHelper.lerp(h, f, g);
        }
        return f;
    }
}

