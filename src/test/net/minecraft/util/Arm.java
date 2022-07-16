/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.util;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public enum Arm {
    LEFT(new TranslatableText("options.mainHand.left")),
    RIGHT(new TranslatableText("options.mainHand.right"));

    private final Text optionName;

    private Arm(Text optionName) {
        this.optionName = optionName;
    }

    public Arm getOpposite() {
        if (this == LEFT) {
            return RIGHT;
        }
        return LEFT;
    }

    public String toString() {
        return this.optionName.getString();
    }

    public Text getOptionName() {
        return this.optionName;
    }
}

