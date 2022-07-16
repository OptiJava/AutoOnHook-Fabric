/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class OnAStickItem<T extends Entity>
extends Item {
    private final EntityType<T> target;
    private final int damagePerUse;

    public OnAStickItem(Item.Settings settings, EntityType<T> target, int damagePerUse) {
        super(settings);
        this.target = target;
        this.damagePerUse = damagePerUse;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemSteerable itemSteerable;
        ItemStack itemStack = user.getStackInHand(hand);
        if (world.isClient) {
            return TypedActionResult.pass(itemStack);
        }
        Entity entity = user.getVehicle();
        if (user.hasVehicle() && entity instanceof ItemSteerable && entity.getType() == this.target && (itemSteerable = (ItemSteerable)((Object)entity)).consumeOnAStickItem()) {
            itemStack.damage(this.damagePerUse, user, p -> p.sendToolBreakStatus(hand));
            if (itemStack.isEmpty()) {
                ItemStack itemStack2 = new ItemStack(Items.FISHING_ROD);
                itemStack2.setNbt(itemStack.getNbt());
                return TypedActionResult.success(itemStack2);
            }
            return TypedActionResult.success(itemStack);
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        return TypedActionResult.pass(itemStack);
    }
}

