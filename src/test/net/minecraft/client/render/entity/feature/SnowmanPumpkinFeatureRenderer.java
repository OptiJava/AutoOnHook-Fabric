/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.SnowGolemEntityModel;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3f;

@Environment(value=EnvType.CLIENT)
public class SnowmanPumpkinFeatureRenderer
extends FeatureRenderer<SnowGolemEntity, SnowGolemEntityModel<SnowGolemEntity>> {
    public SnowmanPumpkinFeatureRenderer(FeatureRendererContext<SnowGolemEntity, SnowGolemEntityModel<SnowGolemEntity>> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, SnowGolemEntity snowGolemEntity, float f, float g, float h, float j, float k, float l) {
        boolean bl;
        if (!snowGolemEntity.hasPumpkin()) {
            return;
        }
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        boolean bl2 = bl = minecraftClient.hasOutline(snowGolemEntity) && snowGolemEntity.isInvisible();
        if (snowGolemEntity.isInvisible() && !bl) {
            return;
        }
        matrixStack.push();
        ((SnowGolemEntityModel)this.getContextModel()).getHead().rotate(matrixStack);
        float m = 0.625f;
        matrixStack.translate(0.0, -0.34375, 0.0);
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f));
        matrixStack.scale(0.625f, -0.625f, -0.625f);
        ItemStack itemStack = new ItemStack(Blocks.CARVED_PUMPKIN);
        if (bl) {
            BlockState blockState = Blocks.CARVED_PUMPKIN.getDefaultState();
            BlockRenderManager blockRenderManager = minecraftClient.getBlockRenderManager();
            BakedModel bakedModel = blockRenderManager.getModel(blockState);
            int n = LivingEntityRenderer.getOverlay(snowGolemEntity, 0.0f);
            matrixStack.translate(-0.5, -0.5, -0.5);
            blockRenderManager.getModelRenderer().render(matrixStack.peek(), vertexConsumerProvider.getBuffer(RenderLayer.getOutline(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)), blockState, bakedModel, 0.0f, 0.0f, 0.0f, i, n);
        } else {
            minecraftClient.getItemRenderer().renderItem(snowGolemEntity, itemStack, ModelTransformation.Mode.HEAD, false, matrixStack, vertexConsumerProvider, snowGolemEntity.world, i, LivingEntityRenderer.getOverlay(snowGolemEntity, 0.0f), snowGolemEntity.getId());
        }
        matrixStack.pop();
    }
}

