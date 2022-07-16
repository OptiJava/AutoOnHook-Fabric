/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.option;

import java.util.Arrays;
import java.util.Comparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public enum NarratorMode {
    OFF(0, "options.narrator.off"),
    ALL(1, "options.narrator.all"),
    CHAT(2, "options.narrator.chat"),
    SYSTEM(3, "options.narrator.system");

    private static final NarratorMode[] VALUES;
    private final int id;
    private final Text name;

    private NarratorMode(int id, String name) {
        this.id = id;
        this.name = new TranslatableText(name);
    }

    public int getId() {
        return this.id;
    }

    public Text getName() {
        return this.name;
    }

    public static NarratorMode byId(int id) {
        return VALUES[MathHelper.floorMod(id, VALUES.length)];
    }

    static {
        VALUES = (NarratorMode[])Arrays.stream(NarratorMode.values()).sorted(Comparator.comparingInt(NarratorMode::getId)).toArray(NarratorMode[]::new);
    }
}

