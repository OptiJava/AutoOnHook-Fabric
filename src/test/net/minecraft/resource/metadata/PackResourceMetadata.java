/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.resource.metadata;

import net.minecraft.resource.metadata.PackResourceMetadataReader;
import net.minecraft.text.Text;

public class PackResourceMetadata {
    public static final PackResourceMetadataReader READER = new PackResourceMetadataReader();
    private final Text description;
    private final int packFormat;

    public PackResourceMetadata(Text description, int format) {
        this.description = description;
        this.packFormat = format;
    }

    public Text getDescription() {
        return this.description;
    }

    public int getPackFormat() {
        return this.packFormat;
    }
}

