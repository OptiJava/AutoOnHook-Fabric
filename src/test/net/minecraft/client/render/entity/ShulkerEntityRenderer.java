/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.ShulkerHeadFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ShulkerEntityRenderer
extends MobEntityRenderer<ShulkerEntity, ShulkerEntityModel<ShulkerEntity>> {
    private static final Identifier TEXTURE = new Identifier("textures/" + TexturedRenderLayers.SHULKER_TEXTURE_ID.getTextureId().getPath() + ".png");
    private static final Identifier[] COLORED_TEXTURES = (Identifier[])TexturedRenderLayers.COLORED_SHULKER_BOXES_TEXTURES.stream().map(spriteIdentifier -> new Identifier("textures/" + spriteIdentifier.getTextureId().getPath() + ".png")).toArray(Identifier[]::new);

    public ShulkerEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new ShulkerEntityModel(context.getPart(EntityModelLayers.SHULKER)), 0.0f);
        this.addFeature(new ShulkerHeadFeatureRenderer(this));
    }

    @Override
    public Vec3d getPositionOffset(ShulkerEntity shulkerEntity, float f) {
        return shulkerEntity.method_33352(f).orElse(super.getPositionOffset(shulkerEntity, f));
    }

    @Override
    public boolean shouldRender(ShulkerEntity shulkerEntity, Frustum frustum, double d, double e, double f) {
        if (super.shouldRender(shulkerEntity, frustum, d, e, f)) {
            return true;
        }
        return shulkerEntity.method_33352(0.0f).filter(vec3d -> {
            EntityType<?> entityType = shulkerEntity.getType();
            float f = entityType.getHeight() / 2.0f;
            float g = entityType.getWidth() / 2.0f;
            Vec3d vec3d2 = Vec3d.ofBottomCenter(shulkerEntity.getBlockPos());
            return frustum.isVisible(new Box(vec3d.x, vec3d.y + (double)f, vec3d.z, vec3d2.x, vec3d2.y + (double)f, vec3d2.z).expand(g, f, g));
        }).isPresent();
    }

    @Override
    public Identifier getTexture(ShulkerEntity shulkerEntity) {
        return ShulkerEntityRenderer.getTexture(shulkerEntity.getColor());
    }

    public static Identifier getTexture(@Nullable DyeColor shulkerColor) {
        if (shulkerColor == null) {
            return TEXTURE;
        }
        return COLORED_TEXTURES[shulkerColor.getId()];
    }

    @Override
    protected void setupTransforms(ShulkerEntity shulkerEntity, MatrixStack matrixStack, float f, float g, float h) {
        super.setupTransforms(shulkerEntity, matrixStack, f, g + 180.0f, h);
        matrixStack.translate(0.0, 0.5, 0.0);
        matrixStack.multiply(shulkerEntity.getAttachedFace().getOpposite().getRotationQuaternion());
        matrixStack.translate(0.0, -0.5, 0.0);
    }
}

