/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.item;

import java.util.List;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class GlassBottleItem
extends Item {
    public GlassBottleItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        List<AreaEffectCloudEntity> list = world.getEntitiesByClass(AreaEffectCloudEntity.class, user.getBoundingBox().expand(2.0), entity -> entity != null && entity.isAlive() && entity.getOwner() instanceof EnderDragonEntity);
        ItemStack itemStack = user.getStackInHand(hand);
        if (!list.isEmpty()) {
            AreaEffectCloudEntity areaEffectCloudEntity = list.get(0);
            areaEffectCloudEntity.setRadius(areaEffectCloudEntity.getRadius() - 0.5f);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOTTLE_FILL_DRAGONBREATH, SoundCategory.NEUTRAL, 1.0f, 1.0f);
            world.emitGameEvent((Entity)user, GameEvent.FLUID_PICKUP, user.getBlockPos());
            return TypedActionResult.success(this.fill(itemStack, user, new ItemStack(Items.DRAGON_BREATH)), world.isClient());
        }
        BlockHitResult areaEffectCloudEntity = GlassBottleItem.raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (((HitResult)areaEffectCloudEntity).getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(itemStack);
        }
        if (((HitResult)areaEffectCloudEntity).getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = areaEffectCloudEntity.getBlockPos();
            if (!world.canPlayerModifyAt(user, blockPos)) {
                return TypedActionResult.pass(itemStack);
            }
            if (world.getFluidState(blockPos).isIn(FluidTags.WATER)) {
                world.playSound(user, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0f, 1.0f);
                world.emitGameEvent((Entity)user, GameEvent.FLUID_PICKUP, blockPos);
                return TypedActionResult.success(this.fill(itemStack, user, PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER)), world.isClient());
            }
        }
        return TypedActionResult.pass(itemStack);
    }

    protected ItemStack fill(ItemStack stack, PlayerEntity player, ItemStack outputStack) {
        player.incrementStat(Stats.USED.getOrCreateStat(this));
        return ItemUsage.exchangeStack(stack, player, outputStack);
    }
}

