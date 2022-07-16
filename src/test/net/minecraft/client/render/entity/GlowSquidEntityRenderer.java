/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.SquidEntityRenderer;
import net.minecraft.client.render.entity.model.SquidEntityModel;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class GlowSquidEntityRenderer
extends SquidEntityRenderer<GlowSquidEntity> {
    private static final Identifier TEXTURE = new Identifier("textures/entity/squid/glow_squid.png");

    public GlowSquidEntityRenderer(EntityRendererFactory.Context context, SquidEntityModel<GlowSquidEntity> squidEntityModel) {
        super(context, squidEntityModel);
    }

    @Override
    public Identifier getTexture(GlowSquidEntity glowSquidEntity) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLight(GlowSquidEntity glowSquidEntity, BlockPos blockPos) {
        int i = (int)MathHelper.clampedLerp(0.0f, 15.0f, 1.0f - (float)glowSquidEntity.getDarkTicksRemaining() / 10.0f);
        if (i == 15) {
            return 15;
        }
        return Math.max(i, super.getBlockLight(glowSquidEntity, blockPos));
    }
}

