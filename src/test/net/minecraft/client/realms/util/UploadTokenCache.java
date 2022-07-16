/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.realms.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class UploadTokenCache {
    private static final Long2ObjectMap<String> TOKEN_CACHE = new Long2ObjectOpenHashMap<String>();

    public static String get(long worldId) {
        return (String)TOKEN_CACHE.get(worldId);
    }

    public static void invalidate(long world) {
        TOKEN_CACHE.remove(world);
    }

    public static void put(long wid, String token) {
        TOKEN_CACHE.put(wid, token);
    }
}

