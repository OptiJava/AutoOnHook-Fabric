/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BlazeEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(value=EnvType.CLIENT)
public class BlazeEntityRenderer
extends MobEntityRenderer<BlazeEntity, BlazeEntityModel<BlazeEntity>> {
    private static final Identifier TEXTURE = new Identifier("textures/entity/blaze.png");

    public BlazeEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new BlazeEntityModel(context.getPart(EntityModelLayers.BLAZE)), 0.5f);
    }

    @Override
    protected int getBlockLight(BlazeEntity blazeEntity, BlockPos blockPos) {
        return 15;
    }

    @Override
    public Identifier getTexture(BlazeEntity blazeEntity) {
        return TEXTURE;
    }
}

