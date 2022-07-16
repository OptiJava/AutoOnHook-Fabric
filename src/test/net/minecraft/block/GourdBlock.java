/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AttachedStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.StemBlock;

public abstract class GourdBlock
extends Block {
    public GourdBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    public abstract StemBlock getStem();

    public abstract AttachedStemBlock getAttachedStem();
}

