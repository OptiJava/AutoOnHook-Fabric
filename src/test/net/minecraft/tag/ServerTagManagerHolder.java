/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.tag;

import net.minecraft.tag.RequiredTagListRegistry;
import net.minecraft.tag.TagManager;

/**
 * A class containing the single static instance of {@link TagManager} on the server.
 */
public class ServerTagManagerHolder {
    private static volatile TagManager tagManager = RequiredTagListRegistry.createBuiltinTagManager();

    public static TagManager getTagManager() {
        return tagManager;
    }

    public static void setTagManager(TagManager tagManager) {
        ServerTagManagerHolder.tagManager = tagManager;
    }
}

