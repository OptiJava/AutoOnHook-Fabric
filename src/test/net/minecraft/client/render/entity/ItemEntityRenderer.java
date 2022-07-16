/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

@Environment(value=EnvType.CLIENT)
public class ItemEntityRenderer
extends EntityRenderer<ItemEntity> {
    private static final float field_32924 = 0.15f;
    private static final int MAX_COUNT_FOR_4_ITEMS_RENDERED = 48;
    private static final int MAX_COUNT_FOR_3_ITEMS_RENDERED = 32;
    private static final int MAX_COUNT_FOR_2_ITEMS_RENDERED = 16;
    private static final int MAX_COUNT_FOR_1_ITEM_RENDERED = 1;
    private static final float field_32929 = 0.0f;
    private static final float field_32930 = 0.0f;
    private static final float field_32931 = 0.09375f;
    private final ItemRenderer itemRenderer;
    private final Random random = new Random();

    public ItemEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.15f;
        this.shadowOpacity = 0.75f;
    }

    private int getRenderedAmount(ItemStack stack) {
        int i = 1;
        if (stack.getCount() > 48) {
            i = 5;
        } else if (stack.getCount() > 32) {
            i = 4;
        } else if (stack.getCount() > 16) {
            i = 3;
        } else if (stack.getCount() > 1) {
            i = 2;
        }
        return i;
    }

    @Override
    public void render(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        float t;
        float s;
        matrixStack.push();
        ItemStack itemStack = itemEntity.getStack();
        int j = itemStack.isEmpty() ? 187 : Item.getRawId(itemStack.getItem()) + itemStack.getDamage();
        this.random.setSeed(j);
        BakedModel bakedModel = this.itemRenderer.getHeldItemModel(itemStack, itemEntity.world, null, itemEntity.getId());
        boolean bl = bakedModel.hasDepth();
        int k = this.getRenderedAmount(itemStack);
        float h = 0.25f;
        float l = MathHelper.sin(((float)itemEntity.getItemAge() + g) / 10.0f + itemEntity.uniqueOffset) * 0.1f + 0.1f;
        float m = bakedModel.getTransformation().getTransformation((ModelTransformation.Mode)ModelTransformation.Mode.GROUND).scale.getY();
        matrixStack.translate(0.0, l + 0.25f * m, 0.0);
        float n = itemEntity.getRotation(g);
        matrixStack.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(n));
        float o = bakedModel.getTransformation().ground.scale.getX();
        float p = bakedModel.getTransformation().ground.scale.getY();
        float q = bakedModel.getTransformation().ground.scale.getZ();
        if (!bl) {
            float r = -0.0f * (float)(k - 1) * 0.5f * o;
            s = -0.0f * (float)(k - 1) * 0.5f * p;
            t = -0.09375f * (float)(k - 1) * 0.5f * q;
            matrixStack.translate(r, s, t);
        }
        for (int r = 0; r < k; ++r) {
            matrixStack.push();
            if (r > 0) {
                if (bl) {
                    s = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    t = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    float u = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    matrixStack.translate(s, t, u);
                } else {
                    s = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                    t = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                    matrixStack.translate(s, t, 0.0);
                }
            }
            this.itemRenderer.renderItem(itemStack, ModelTransformation.Mode.GROUND, false, matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV, bakedModel);
            matrixStack.pop();
            if (bl) continue;
            matrixStack.translate(0.0f * o, 0.0f * p, 0.09375f * q);
        }
        matrixStack.pop();
        super.render(itemEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(ItemEntity itemEntity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}

