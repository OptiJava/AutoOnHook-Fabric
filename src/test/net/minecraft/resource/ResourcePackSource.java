/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.resource;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public interface ResourcePackSource {
    public static final ResourcePackSource PACK_SOURCE_NONE = ResourcePackSource.onlyName();
    public static final ResourcePackSource PACK_SOURCE_BUILTIN = ResourcePackSource.nameAndSource("pack.source.builtin");
    public static final ResourcePackSource PACK_SOURCE_WORLD = ResourcePackSource.nameAndSource("pack.source.world");
    public static final ResourcePackSource PACK_SOURCE_SERVER = ResourcePackSource.nameAndSource("pack.source.server");

    public Text decorate(Text var1);

    public static ResourcePackSource onlyName() {
        return name -> name;
    }

    public static ResourcePackSource nameAndSource(String source) {
        TranslatableText text = new TranslatableText(source);
        return name -> new TranslatableText("pack.nameAndSource", name, text).formatted(Formatting.GRAY);
    }
}

