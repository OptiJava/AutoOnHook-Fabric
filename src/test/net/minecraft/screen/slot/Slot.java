/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.screen.slot;

import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class Slot {
    private final int index;
    public final Inventory inventory;
    public int id;
    public final int x;
    public final int y;

    public Slot(Inventory inventory, int index, int x, int y) {
        this.inventory = inventory;
        this.index = index;
        this.x = x;
        this.y = y;
    }

    public void onQuickTransfer(ItemStack newItem, ItemStack original) {
        int i = original.getCount() - newItem.getCount();
        if (i > 0) {
            this.onCrafted(original, i);
        }
    }

    protected void onCrafted(ItemStack stack, int amount) {
    }

    protected void onTake(int amount) {
    }

    protected void onCrafted(ItemStack stack) {
    }

    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        this.markDirty();
    }

    public boolean canInsert(ItemStack stack) {
        return true;
    }

    public ItemStack getStack() {
        return this.inventory.getStack(this.index);
    }

    public boolean hasStack() {
        return !this.getStack().isEmpty();
    }

    public void setStack(ItemStack stack) {
        this.inventory.setStack(this.index, stack);
        this.markDirty();
    }

    public void markDirty() {
        this.inventory.markDirty();
    }

    public int getMaxItemCount() {
        return this.inventory.getMaxCountPerStack();
    }

    public int getMaxItemCount(ItemStack stack) {
        return Math.min(this.getMaxItemCount(), stack.getMaxCount());
    }

    @Nullable
    public Pair<Identifier, Identifier> getBackgroundSprite() {
        return null;
    }

    public ItemStack takeStack(int amount) {
        return this.inventory.removeStack(this.index, amount);
    }

    public boolean canTakeItems(PlayerEntity playerEntity) {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

    public Optional<ItemStack> tryTakeStackRange(int min, int max, PlayerEntity player) {
        if (!this.canTakeItems(player)) {
            return Optional.empty();
        }
        if (!this.canTakePartial(player) && max < this.getStack().getCount()) {
            return Optional.empty();
        }
        ItemStack itemStack = this.takeStack(min = Math.min(min, max));
        if (itemStack.isEmpty()) {
            return Optional.empty();
        }
        if (this.getStack().isEmpty()) {
            this.setStack(ItemStack.EMPTY);
        }
        return Optional.of(itemStack);
    }

    public ItemStack takeStackRange(int min, int max, PlayerEntity player) {
        Optional<ItemStack> optional = this.tryTakeStackRange(min, max, player);
        optional.ifPresent(stack -> this.onTakeItem(player, (ItemStack)stack));
        return optional.orElse(ItemStack.EMPTY);
    }

    public ItemStack insertStack(ItemStack stack) {
        return this.insertStack(stack, stack.getCount());
    }

    public ItemStack insertStack(ItemStack stack, int count) {
        if (stack.isEmpty() || !this.canInsert(stack)) {
            return stack;
        }
        ItemStack itemStack = this.getStack();
        int i = Math.min(Math.min(count, stack.getCount()), this.getMaxItemCount(stack) - itemStack.getCount());
        if (itemStack.isEmpty()) {
            this.setStack(stack.split(i));
        } else if (ItemStack.canCombine(itemStack, stack)) {
            stack.decrement(i);
            itemStack.increment(i);
            this.setStack(itemStack);
        }
        return stack;
    }

    public boolean canTakePartial(PlayerEntity player) {
        return this.canTakeItems(player) && this.canInsert(this.getStack());
    }

    public int getIndex() {
        return this.index;
    }
}

