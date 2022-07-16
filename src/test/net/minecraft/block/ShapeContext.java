/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.block;

import net.minecraft.block.EntityShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

public interface ShapeContext {
    public static ShapeContext absent() {
        return EntityShapeContext.ABSENT;
    }

    public static ShapeContext of(Entity entity) {
        return new EntityShapeContext(entity);
    }

    public boolean isDescending();

    public boolean isAbove(VoxelShape var1, BlockPos var2, boolean var3);

    public boolean isWearingOnFeet(Item var1);

    public boolean isHolding(Item var1);

    public boolean canWalkOnFluid(FluidState var1, FlowableFluid var2);
}

