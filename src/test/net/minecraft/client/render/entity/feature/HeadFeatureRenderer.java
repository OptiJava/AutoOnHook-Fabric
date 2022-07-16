/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity.feature;

import com.mojang.authlib.GameProfile;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.Vec3f;

@Environment(value=EnvType.CLIENT)
public class HeadFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>>
extends FeatureRenderer<T, M> {
    private final float scaleX;
    private final float scaleY;
    private final float scaleZ;
    private final Map<SkullBlock.SkullType, SkullBlockEntityModel> headModels;

    public HeadFeatureRenderer(FeatureRendererContext<T, M> context, EntityModelLoader loader) {
        this(context, loader, 1.0f, 1.0f, 1.0f);
    }

    public HeadFeatureRenderer(FeatureRendererContext<T, M> context, EntityModelLoader loader, float scaleX, float scaleY, float scaleZ) {
        super(context);
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        this.headModels = SkullBlockEntityRenderer.getModels(loader);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
        float m;
        boolean bl;
        ItemStack itemStack = ((LivingEntity)livingEntity).getEquippedStack(EquipmentSlot.HEAD);
        if (itemStack.isEmpty()) {
            return;
        }
        Item item = itemStack.getItem();
        matrixStack.push();
        matrixStack.scale(this.scaleX, this.scaleY, this.scaleZ);
        boolean bl2 = bl = livingEntity instanceof VillagerEntity || livingEntity instanceof ZombieVillagerEntity;
        if (((LivingEntity)livingEntity).isBaby() && !(livingEntity instanceof VillagerEntity)) {
            m = 2.0f;
            float n = 1.4f;
            matrixStack.translate(0.0, 0.03125, 0.0);
            matrixStack.scale(0.7f, 0.7f, 0.7f);
            matrixStack.translate(0.0, 1.0, 0.0);
        }
        ((ModelWithHead)this.getContextModel()).getHead().rotate(matrixStack);
        if (item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof AbstractSkullBlock) {
            Object nbtCompound;
            m = 1.1875f;
            matrixStack.scale(1.1875f, -1.1875f, -1.1875f);
            if (bl) {
                matrixStack.translate(0.0, 0.0625, 0.0);
            }
            GameProfile n = null;
            if (itemStack.hasNbt() && ((NbtCompound)(nbtCompound = itemStack.getNbt())).contains("SkullOwner", 10)) {
                n = NbtHelper.toGameProfile(((NbtCompound)nbtCompound).getCompound("SkullOwner"));
            }
            matrixStack.translate(-0.5, 0.0, -0.5);
            nbtCompound = ((AbstractSkullBlock)((BlockItem)item).getBlock()).getSkullType();
            SkullBlockEntityModel skullBlockEntityModel = this.headModels.get(nbtCompound);
            RenderLayer renderLayer = SkullBlockEntityRenderer.getRenderLayer((SkullBlock.SkullType)nbtCompound, n);
            SkullBlockEntityRenderer.renderSkull(null, 180.0f, f, matrixStack, vertexConsumerProvider, i, skullBlockEntityModel, renderLayer);
        } else if (!(item instanceof ArmorItem) || ((ArmorItem)item).getSlotType() != EquipmentSlot.HEAD) {
            HeadFeatureRenderer.translate(matrixStack, bl);
            MinecraftClient.getInstance().getHeldItemRenderer().renderItem((LivingEntity)livingEntity, itemStack, ModelTransformation.Mode.HEAD, false, matrixStack, vertexConsumerProvider, i);
        }
        matrixStack.pop();
    }

    public static void translate(MatrixStack matrices, boolean villager) {
        float f = 0.625f;
        matrices.translate(0.0, -0.25, 0.0);
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f));
        matrices.scale(0.625f, -0.625f, -0.625f);
        if (villager) {
            matrices.translate(0.0, 0.1875, 0.0);
        }
    }
}

