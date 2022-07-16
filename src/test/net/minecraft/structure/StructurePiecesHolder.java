/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.structure;

import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import org.jetbrains.annotations.Nullable;

/**
 * A holder of generated structure pieces.
 * 
 * @see StructurePiece#fillOpenings
 */
public interface StructurePiecesHolder {
    /**
     * Adds a structure piece into this holder.
     * 
     * @param piece the piece to add
     */
    public void addPiece(StructurePiece var1);

    /**
     * Returns an arbitrary piece in this holder that intersects the given {@code box},
     * or {@code null} if there is no such piece.
     * 
     * @param box the box to check intersection against
     */
    @Nullable
    public StructurePiece getIntersecting(BlockBox var1);
}

