/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.screen;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

public class CartographyTableScreenHandler
extends ScreenHandler {
    public static final int MAP_SLOT_INDEX = 0;
    public static final int MATERIAL_SLOT_INDEX = 1;
    public static final int RESULT_SLOT_INDEX = 2;
    private static final int field_30776 = 3;
    private static final int field_30777 = 30;
    private static final int field_30778 = 30;
    private static final int field_30779 = 39;
    private final ScreenHandlerContext context;
    long lastTakeResultTime;
    public final Inventory inventory = new SimpleInventory(2){

        @Override
        public void markDirty() {
            CartographyTableScreenHandler.this.onContentChanged(this);
            super.markDirty();
        }
    };
    private final CraftingResultInventory resultInventory = new CraftingResultInventory(){

        @Override
        public void markDirty() {
            CartographyTableScreenHandler.this.onContentChanged(this);
            super.markDirty();
        }
    };

    public CartographyTableScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, ScreenHandlerContext.EMPTY);
    }

    public CartographyTableScreenHandler(int syncId, PlayerInventory inventory, final ScreenHandlerContext context) {
        super(ScreenHandlerType.CARTOGRAPHY_TABLE, syncId);
        int i;
        this.context = context;
        this.addSlot(new Slot(this.inventory, 0, 15, 15){

            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.FILLED_MAP);
            }
        });
        this.addSlot(new Slot(this.inventory, 1, 15, 52){

            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.PAPER) || stack.isOf(Items.MAP) || stack.isOf(Items.GLASS_PANE);
            }
        });
        this.addSlot(new Slot(this.resultInventory, 2, 145, 39){

            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                ((Slot)CartographyTableScreenHandler.this.slots.get(0)).takeStack(1);
                ((Slot)CartographyTableScreenHandler.this.slots.get(1)).takeStack(1);
                stack.getItem().onCraft(stack, player.world, player);
                context.run((world, pos) -> {
                    long l = world.getTime();
                    if (CartographyTableScreenHandler.this.lastTakeResultTime != l) {
                        world.playSound(null, (BlockPos)pos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        CartographyTableScreenHandler.this.lastTakeResultTime = l;
                    }
                });
                super.onTakeItem(player, stack);
            }
        });
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return CartographyTableScreenHandler.canUse(this.context, player, Blocks.CARTOGRAPHY_TABLE);
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        ItemStack itemStack = this.inventory.getStack(0);
        ItemStack itemStack2 = this.inventory.getStack(1);
        ItemStack itemStack3 = this.resultInventory.getStack(2);
        if (!itemStack3.isEmpty() && (itemStack.isEmpty() || itemStack2.isEmpty())) {
            this.resultInventory.removeStack(2);
        } else if (!itemStack.isEmpty() && !itemStack2.isEmpty()) {
            this.updateResult(itemStack, itemStack2, itemStack3);
        }
    }

    private void updateResult(ItemStack map, ItemStack item, ItemStack oldResult) {
        this.context.run((world, pos) -> {
            ItemStack itemStack4;
            MapState mapState = FilledMapItem.getOrCreateMapState(map, world);
            if (mapState == null) {
                return;
            }
            if (item.isOf(Items.PAPER) && !mapState.locked && mapState.scale < 4) {
                itemStack4 = map.copy();
                itemStack4.setCount(1);
                itemStack4.getOrCreateNbt().putInt("map_scale_direction", 1);
                this.sendContentUpdates();
            } else if (item.isOf(Items.GLASS_PANE) && !mapState.locked) {
                itemStack4 = map.copy();
                itemStack4.setCount(1);
                itemStack4.getOrCreateNbt().putBoolean("map_to_lock", true);
                this.sendContentUpdates();
            } else if (item.isOf(Items.MAP)) {
                itemStack4 = map.copy();
                itemStack4.setCount(2);
                this.sendContentUpdates();
            } else {
                this.resultInventory.removeStack(2);
                this.sendContentUpdates();
                return;
            }
            if (!ItemStack.areEqual(itemStack4, oldResult)) {
                this.resultInventory.setStack(2, itemStack4);
                this.sendContentUpdates();
            }
        });
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.inventory != this.resultInventory && super.canInsertIntoSlot(stack, slot);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index == 2) {
                itemStack2.getItem().onCraft(itemStack2, player.world, player);
                if (!this.insertItem(itemStack2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickTransfer(itemStack2, itemStack);
            } else if (index == 1 || index == 0 ? !this.insertItem(itemStack2, 3, 39, false) : (itemStack2.isOf(Items.FILLED_MAP) ? !this.insertItem(itemStack2, 0, 1, false) : (itemStack2.isOf(Items.PAPER) || itemStack2.isOf(Items.MAP) || itemStack2.isOf(Items.GLASS_PANE) ? !this.insertItem(itemStack2, 1, 2, false) : (index >= 3 && index < 30 ? !this.insertItem(itemStack2, 30, 39, false) : index >= 30 && index < 39 && !this.insertItem(itemStack2, 3, 30, false))))) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            }
            slot.markDirty();
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTakeItem(player, itemStack2);
            this.sendContentUpdates();
        }
        return itemStack;
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.resultInventory.removeStack(2);
        this.context.run((world, pos) -> this.dropInventory(player, this.inventory));
    }
}

