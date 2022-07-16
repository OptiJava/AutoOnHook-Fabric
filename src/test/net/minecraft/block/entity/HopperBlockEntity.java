/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.block.entity;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HopperBlockEntity
extends LootableContainerBlockEntity
implements Hopper {
    public static final int field_31341 = 8;
    public static final int field_31342 = 5;
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(5, ItemStack.EMPTY);
    private int transferCooldown = -1;
    private long lastTickTime;

    public HopperBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.HOPPER, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.deserializeLootTable(nbt)) {
            Inventories.readNbt(nbt, this.inventory);
        }
        this.transferCooldown = nbt.getInt("TransferCooldown");
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (!this.serializeLootTable(nbt)) {
            Inventories.writeNbt(nbt, this.inventory);
        }
        nbt.putInt("TransferCooldown", this.transferCooldown);
        return nbt;
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        this.checkLootInteraction(null);
        return Inventories.splitStack(this.getInvStackList(), slot, amount);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.checkLootInteraction(null);
        this.getInvStackList().set(slot, stack);
        if (stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }
    }

    @Override
    protected Text getContainerName() {
        return new TranslatableText("container.hopper");
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity) {
        --blockEntity.transferCooldown;
        blockEntity.lastTickTime = world.getTime();
        if (!blockEntity.needsCooldown()) {
            blockEntity.setCooldown(0);
            HopperBlockEntity.insertAndExtract(world, pos, state, blockEntity, () -> HopperBlockEntity.extract(world, blockEntity));
        }
    }

    private static boolean insertAndExtract(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, BooleanSupplier booleanSupplier) {
        if (world.isClient) {
            return false;
        }
        if (!blockEntity.needsCooldown() && state.get(HopperBlock.ENABLED).booleanValue()) {
            boolean bl = false;
            if (!blockEntity.isEmpty()) {
                bl = HopperBlockEntity.insert(world, pos, state, blockEntity);
            }
            if (!blockEntity.isFull()) {
                bl |= booleanSupplier.getAsBoolean();
            }
            if (bl) {
                blockEntity.setCooldown(8);
                HopperBlockEntity.markDirty(world, pos, state);
                return true;
            }
        }
        return false;
    }

    private boolean isFull() {
        for (ItemStack itemStack : this.inventory) {
            if (!itemStack.isEmpty() && itemStack.getCount() == itemStack.getMaxCount()) continue;
            return false;
        }
        return true;
    }

    private static boolean insert(World world, BlockPos pos, BlockState state, Inventory inventory) {
        Inventory inventory2 = HopperBlockEntity.getOutputInventory(world, pos, state);
        if (inventory2 == null) {
            return false;
        }
        Direction direction = state.get(HopperBlock.FACING).getOpposite();
        if (HopperBlockEntity.isInventoryFull(inventory2, direction)) {
            return false;
        }
        for (int i = 0; i < inventory.size(); ++i) {
            if (inventory.getStack(i).isEmpty()) continue;
            ItemStack itemStack = inventory.getStack(i).copy();
            ItemStack itemStack2 = HopperBlockEntity.transfer(inventory, inventory2, inventory.removeStack(i, 1), direction);
            if (itemStack2.isEmpty()) {
                inventory2.markDirty();
                return true;
            }
            inventory.setStack(i, itemStack);
        }
        return false;
    }

    private static IntStream getAvailableSlots(Inventory inventory, Direction side) {
        if (inventory instanceof SidedInventory) {
            return IntStream.of(((SidedInventory)inventory).getAvailableSlots(side));
        }
        return IntStream.range(0, inventory.size());
    }

    private static boolean isInventoryFull(Inventory inventory, Direction direction) {
        return HopperBlockEntity.getAvailableSlots(inventory, direction).allMatch(i -> {
            ItemStack itemStack = inventory.getStack(i);
            return itemStack.getCount() >= itemStack.getMaxCount();
        });
    }

    private static boolean isInventoryEmpty(Inventory inv, Direction facing) {
        return HopperBlockEntity.getAvailableSlots(inv, facing).allMatch(i -> inv.getStack(i).isEmpty());
    }

    public static boolean extract(World world, Hopper hopper) {
        Inventory inventory = HopperBlockEntity.getInputInventory(world, hopper);
        if (inventory != null) {
            Direction direction = Direction.DOWN;
            if (HopperBlockEntity.isInventoryEmpty(inventory, direction)) {
                return false;
            }
            return HopperBlockEntity.getAvailableSlots(inventory, direction).anyMatch(i -> HopperBlockEntity.extract(hopper, inventory, i, direction));
        }
        for (ItemEntity itemEntity : HopperBlockEntity.getInputItemEntities(world, hopper)) {
            if (!HopperBlockEntity.extract(hopper, itemEntity)) continue;
            return true;
        }
        return false;
    }

    private static boolean extract(Hopper hopper, Inventory inventory, int slot, Direction side) {
        ItemStack itemStack = inventory.getStack(slot);
        if (!itemStack.isEmpty() && HopperBlockEntity.canExtract(inventory, itemStack, slot, side)) {
            ItemStack itemStack2 = itemStack.copy();
            ItemStack itemStack3 = HopperBlockEntity.transfer(inventory, hopper, inventory.removeStack(slot, 1), null);
            if (itemStack3.isEmpty()) {
                inventory.markDirty();
                return true;
            }
            inventory.setStack(slot, itemStack2);
        }
        return false;
    }

    public static boolean extract(Inventory inventory, ItemEntity itemEntity) {
        boolean bl = false;
        ItemStack itemStack = itemEntity.getStack().copy();
        ItemStack itemStack2 = HopperBlockEntity.transfer(null, inventory, itemStack, null);
        if (itemStack2.isEmpty()) {
            bl = true;
            itemEntity.discard();
        } else {
            itemEntity.setStack(itemStack2);
        }
        return bl;
    }

    public static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, @Nullable Direction side) {
        if (to instanceof SidedInventory && side != null) {
            SidedInventory sidedInventory = (SidedInventory)to;
            int[] is = sidedInventory.getAvailableSlots(side);
            for (int i = 0; i < is.length && !stack.isEmpty(); ++i) {
                stack = HopperBlockEntity.transfer(from, to, stack, is[i], side);
            }
        } else {
            int sidedInventory = to.size();
            for (int is = 0; is < sidedInventory && !stack.isEmpty(); ++is) {
                stack = HopperBlockEntity.transfer(from, to, stack, is, side);
            }
        }
        return stack;
    }

    private static boolean canInsert(Inventory inventory, ItemStack stack, int slot, @Nullable Direction side) {
        if (!inventory.isValid(slot, stack)) {
            return false;
        }
        return !(inventory instanceof SidedInventory) || ((SidedInventory)inventory).canInsert(slot, stack, side);
    }

    private static boolean canExtract(Inventory inv, ItemStack stack, int slot, Direction facing) {
        return !(inv instanceof SidedInventory) || ((SidedInventory)inv).canExtract(slot, stack, facing);
    }

    private static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, int slot, @Nullable Direction direction) {
        ItemStack itemStack = to.getStack(slot);
        if (HopperBlockEntity.canInsert(to, stack, slot, direction)) {
            int j;
            boolean bl = false;
            boolean bl2 = to.isEmpty();
            if (itemStack.isEmpty()) {
                to.setStack(slot, stack);
                stack = ItemStack.EMPTY;
                bl = true;
            } else if (HopperBlockEntity.canMergeItems(itemStack, stack)) {
                int i = stack.getMaxCount() - itemStack.getCount();
                j = Math.min(stack.getCount(), i);
                stack.decrement(j);
                itemStack.increment(j);
                boolean bl3 = bl = j > 0;
            }
            if (bl) {
                HopperBlockEntity i;
                if (bl2 && to instanceof HopperBlockEntity && !(i = (HopperBlockEntity)to).isDisabled()) {
                    j = 0;
                    if (from instanceof HopperBlockEntity) {
                        HopperBlockEntity hopperBlockEntity = (HopperBlockEntity)from;
                        if (i.lastTickTime >= hopperBlockEntity.lastTickTime) {
                            j = 1;
                        }
                    }
                    i.setCooldown(8 - j);
                }
                to.markDirty();
            }
        }
        return stack;
    }

    @Nullable
    private static Inventory getOutputInventory(World world, BlockPos pos, BlockState state) {
        Direction direction = state.get(HopperBlock.FACING);
        return HopperBlockEntity.getInventoryAt(world, pos.offset(direction));
    }

    @Nullable
    private static Inventory getInputInventory(World world, Hopper hopper) {
        return HopperBlockEntity.getInventoryAt(world, hopper.getHopperX(), hopper.getHopperY() + 1.0, hopper.getHopperZ());
    }

    public static List<ItemEntity> getInputItemEntities(World world, Hopper hopper) {
        return hopper.getInputAreaShape().getBoundingBoxes().stream().flatMap(box -> world.getEntitiesByClass(ItemEntity.class, box.offset(hopper.getHopperX() - 0.5, hopper.getHopperY() - 0.5, hopper.getHopperZ() - 0.5), EntityPredicates.VALID_ENTITY).stream()).collect(Collectors.toList());
    }

    @Nullable
    public static Inventory getInventoryAt(World world, BlockPos pos) {
        return HopperBlockEntity.getInventoryAt(world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
    }

    @Nullable
    private static Inventory getInventoryAt(World world, double x, double y, double z) {
        Object blockEntity;
        Inventory inventory = null;
        BlockPos blockPos = new BlockPos(x, y, z);
        BlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof InventoryProvider) {
            inventory = ((InventoryProvider)((Object)block)).getInventory(blockState, world, blockPos);
        } else if (blockState.hasBlockEntity() && (blockEntity = world.getBlockEntity(blockPos)) instanceof Inventory && (inventory = (Inventory)blockEntity) instanceof ChestBlockEntity && block instanceof ChestBlock) {
            inventory = ChestBlock.getInventory((ChestBlock)block, blockState, world, blockPos, true);
        }
        if (inventory == null && !(blockEntity = world.getOtherEntities(null, new Box(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5), EntityPredicates.VALID_INVENTORIES)).isEmpty()) {
            inventory = (Inventory)blockEntity.get(world.random.nextInt(blockEntity.size()));
        }
        return inventory;
    }

    private static boolean canMergeItems(ItemStack first, ItemStack second) {
        if (!first.isOf(second.getItem())) {
            return false;
        }
        if (first.getDamage() != second.getDamage()) {
            return false;
        }
        if (first.getCount() > first.getMaxCount()) {
            return false;
        }
        return ItemStack.areNbtEqual(first, second);
    }

    @Override
    public double getHopperX() {
        return (double)this.pos.getX() + 0.5;
    }

    @Override
    public double getHopperY() {
        return (double)this.pos.getY() + 0.5;
    }

    @Override
    public double getHopperZ() {
        return (double)this.pos.getZ() + 0.5;
    }

    private void setCooldown(int cooldown) {
        this.transferCooldown = cooldown;
    }

    private boolean needsCooldown() {
        return this.transferCooldown > 0;
    }

    private boolean isDisabled() {
        return this.transferCooldown > 8;
    }

    @Override
    protected DefaultedList<ItemStack> getInvStackList() {
        return this.inventory;
    }

    @Override
    protected void setInvStackList(DefaultedList<ItemStack> list) {
        this.inventory = list;
    }

    public static void onEntityCollided(World world, BlockPos pos, BlockState state, Entity entity, HopperBlockEntity blockEntity) {
        if (entity instanceof ItemEntity && VoxelShapes.matchesAnywhere(VoxelShapes.cuboid(entity.getBoundingBox().offset(-pos.getX(), -pos.getY(), -pos.getZ())), blockEntity.getInputAreaShape(), BooleanBiFunction.AND)) {
            HopperBlockEntity.insertAndExtract(world, pos, state, blockEntity, () -> HopperBlockEntity.extract(blockEntity, (ItemEntity)entity));
        }
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new HopperScreenHandler(syncId, playerInventory, this);
    }
}

