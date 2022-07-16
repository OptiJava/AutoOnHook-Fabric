/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.block.dispenser;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class ItemDispenserBehavior
implements DispenserBehavior {
    @Override
    public final ItemStack dispense(BlockPointer blockPointer, ItemStack itemStack) {
        ItemStack itemStack2 = this.dispenseSilently(blockPointer, itemStack);
        this.playSound(blockPointer);
        this.spawnParticles(blockPointer, blockPointer.getBlockState().get(DispenserBlock.FACING));
        return itemStack2;
    }

    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
        Position position = DispenserBlock.getOutputLocation(pointer);
        ItemStack itemStack = stack.split(1);
        ItemDispenserBehavior.spawnItem(pointer.getWorld(), itemStack, 6, direction, position);
        return stack;
    }

    public static void spawnItem(World world, ItemStack stack, int offset, Direction side, Position pos) {
        double d = pos.getX();
        double e = pos.getY();
        double f = pos.getZ();
        e = side.getAxis() == Direction.Axis.Y ? (e -= 0.125) : (e -= 0.15625);
        ItemEntity itemEntity = new ItemEntity(world, d, e, f, stack);
        double g = world.random.nextDouble() * 0.1 + 0.2;
        itemEntity.setVelocity(world.random.nextGaussian() * (double)0.0075f * (double)offset + (double)side.getOffsetX() * g, world.random.nextGaussian() * (double)0.0075f * (double)offset + (double)0.2f, world.random.nextGaussian() * (double)0.0075f * (double)offset + (double)side.getOffsetZ() * g);
        world.spawnEntity(itemEntity);
    }

    protected void playSound(BlockPointer pointer) {
        pointer.getWorld().syncWorldEvent(WorldEvents.DISPENSER_DISPENSES, pointer.getPos(), 0);
    }

    protected void spawnParticles(BlockPointer pointer, Direction side) {
        pointer.getWorld().syncWorldEvent(WorldEvents.DISPENSER_ACTIVATED, pointer.getPos(), side.getId());
    }
}

