/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.HoglinEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class HoglinEntityRenderer
extends MobEntityRenderer<HoglinEntity, HoglinEntityModel<HoglinEntity>> {
    private static final Identifier TEXTURE = new Identifier("textures/entity/hoglin/hoglin.png");

    public HoglinEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new HoglinEntityModel(context.getPart(EntityModelLayers.HOGLIN)), 0.7f);
    }

    @Override
    public Identifier getTexture(HoglinEntity hoglinEntity) {
        return TEXTURE;
    }

    @Override
    protected boolean isShaking(HoglinEntity hoglinEntity) {
        return super.isShaking(hoglinEntity) || hoglinEntity.canConvert();
    }

    @Override
    protected /* synthetic */ boolean isShaking(LivingEntity entity) {
        return this.isShaking((HoglinEntity)entity);
    }
}

