/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.util;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class RawTextureDataLoader {
    @Deprecated
    public static int[] loadRawTextureData(ResourceManager resourceManager, Identifier id) throws IOException {
        try (Resource resource = resourceManager.getResource(id);){
            NativeImage nativeImage = NativeImage.read(resource.getInputStream());
            try {
                int[] nArray = nativeImage.makePixelArray();
                if (nativeImage != null) {
                    nativeImage.close();
                }
                return nArray;
            }
            catch (Throwable throwable) {
                if (nativeImage != null) {
                    try {
                        nativeImage.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
    }
}

