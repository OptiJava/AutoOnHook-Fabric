/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.resource;

import net.minecraft.SharedConstants;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public enum ResourcePackCompatibility {
    TOO_OLD("old"),
    TOO_NEW("new"),
    COMPATIBLE("compatible");

    private final Text notification;
    private final Text confirmMessage;

    private ResourcePackCompatibility(String translationSuffix) {
        this.notification = new TranslatableText("pack.incompatible." + translationSuffix).formatted(Formatting.GRAY);
        this.confirmMessage = new TranslatableText("pack.incompatible.confirm." + translationSuffix);
    }

    public boolean isCompatible() {
        return this == COMPATIBLE;
    }

    public static ResourcePackCompatibility from(int packVersion, ResourceType type) {
        int i = type.getPackVersion(SharedConstants.getGameVersion());
        if (packVersion < i) {
            return TOO_OLD;
        }
        if (packVersion > i) {
            return TOO_NEW;
        }
        return COMPATIBLE;
    }

    public static ResourcePackCompatibility from(PackResourceMetadata metadata, ResourceType type) {
        return ResourcePackCompatibility.from(metadata.getPackFormat(), type);
    }

    public Text getNotification() {
        return this.notification;
    }

    public Text getConfirmMessage() {
        return this.confirmMessage;
    }
}

