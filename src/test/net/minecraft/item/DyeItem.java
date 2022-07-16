/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.item;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;

public class DyeItem
extends Item {
    private static final Map<DyeColor, DyeItem> DYES = Maps.newEnumMap(DyeColor.class);
    private final DyeColor color;

    public DyeItem(DyeColor color, Item.Settings settings) {
        super(settings);
        this.color = color;
        DYES.put(color, this);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        SheepEntity sheepEntity;
        if (entity instanceof SheepEntity && (sheepEntity = (SheepEntity)entity).isAlive() && !sheepEntity.isSheared() && sheepEntity.getColor() != this.color) {
            sheepEntity.world.playSoundFromEntity(user, sheepEntity, SoundEvents.ITEM_DYE_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
            if (!user.world.isClient) {
                sheepEntity.setColor(this.color);
                stack.decrement(1);
            }
            return ActionResult.success(user.world.isClient);
        }
        return ActionResult.PASS;
    }

    public DyeColor getColor() {
        return this.color;
    }

    public static DyeItem byColor(DyeColor color) {
        return DYES.get(color);
    }
}

