/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.obfuscate.DontObfuscate;

@Environment(value=EnvType.CLIENT)
public class ClientBrandRetriever {
    public static final String VANILLA = "vanilla";

    @DontObfuscate
    public static String getClientModName() {
        return VANILLA;
    }
}

