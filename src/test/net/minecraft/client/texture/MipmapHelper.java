/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.texture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class MipmapHelper {
    private static final int field_32949 = 96;
    private static final float[] COLOR_FRACTIONS = Util.make(new float[256], fs -> {
        for (int i = 0; i < ((float[])fs).length; ++i) {
            fs[i] = (float)Math.pow((float)i / 255.0f, 2.2);
        }
    });

    private MipmapHelper() {
    }

    public static NativeImage[] getMipmapLevelsImages(NativeImage image, int mipmap) {
        NativeImage[] nativeImages = new NativeImage[mipmap + 1];
        nativeImages[0] = image;
        if (mipmap > 0) {
            int i;
            boolean bl = false;
            block0: for (i = 0; i < image.getWidth(); ++i) {
                for (int j = 0; j < image.getHeight(); ++j) {
                    if (image.getColor(i, j) >> 24 != 0) continue;
                    bl = true;
                    break block0;
                }
            }
            for (i = 1; i <= mipmap; ++i) {
                NativeImage j = nativeImages[i - 1];
                NativeImage nativeImage = new NativeImage(j.getWidth() >> 1, j.getHeight() >> 1, false);
                int k = nativeImage.getWidth();
                int l = nativeImage.getHeight();
                for (int m = 0; m < k; ++m) {
                    for (int n = 0; n < l; ++n) {
                        nativeImage.setColor(m, n, MipmapHelper.blend(j.getColor(m * 2 + 0, n * 2 + 0), j.getColor(m * 2 + 1, n * 2 + 0), j.getColor(m * 2 + 0, n * 2 + 1), j.getColor(m * 2 + 1, n * 2 + 1), bl));
                    }
                }
                nativeImages[i] = nativeImage;
            }
        }
        return nativeImages;
    }

    private static int blend(int one, int two, int three, int four, boolean checkAlpha) {
        if (checkAlpha) {
            float f = 0.0f;
            float g = 0.0f;
            float h = 0.0f;
            float i = 0.0f;
            if (one >> 24 != 0) {
                f += MipmapHelper.getColorFraction(one >> 24);
                g += MipmapHelper.getColorFraction(one >> 16);
                h += MipmapHelper.getColorFraction(one >> 8);
                i += MipmapHelper.getColorFraction(one >> 0);
            }
            if (two >> 24 != 0) {
                f += MipmapHelper.getColorFraction(two >> 24);
                g += MipmapHelper.getColorFraction(two >> 16);
                h += MipmapHelper.getColorFraction(two >> 8);
                i += MipmapHelper.getColorFraction(two >> 0);
            }
            if (three >> 24 != 0) {
                f += MipmapHelper.getColorFraction(three >> 24);
                g += MipmapHelper.getColorFraction(three >> 16);
                h += MipmapHelper.getColorFraction(three >> 8);
                i += MipmapHelper.getColorFraction(three >> 0);
            }
            if (four >> 24 != 0) {
                f += MipmapHelper.getColorFraction(four >> 24);
                g += MipmapHelper.getColorFraction(four >> 16);
                h += MipmapHelper.getColorFraction(four >> 8);
                i += MipmapHelper.getColorFraction(four >> 0);
            }
            int j = (int)(Math.pow(f /= 4.0f, 0.45454545454545453) * 255.0);
            int k = (int)(Math.pow(g /= 4.0f, 0.45454545454545453) * 255.0);
            int l = (int)(Math.pow(h /= 4.0f, 0.45454545454545453) * 255.0);
            int m = (int)(Math.pow(i /= 4.0f, 0.45454545454545453) * 255.0);
            if (j < 96) {
                j = 0;
            }
            return j << 24 | k << 16 | l << 8 | m;
        }
        int f = MipmapHelper.getColorComponent(one, two, three, four, 24);
        int g = MipmapHelper.getColorComponent(one, two, three, four, 16);
        int h = MipmapHelper.getColorComponent(one, two, three, four, 8);
        int i = MipmapHelper.getColorComponent(one, two, three, four, 0);
        return f << 24 | g << 16 | h << 8 | i;
    }

    private static int getColorComponent(int one, int two, int three, int four, int bits) {
        float f = MipmapHelper.getColorFraction(one >> bits);
        float g = MipmapHelper.getColorFraction(two >> bits);
        float h = MipmapHelper.getColorFraction(three >> bits);
        float i = MipmapHelper.getColorFraction(four >> bits);
        float j = (float)((double)((float)Math.pow((double)(f + g + h + i) * 0.25, 0.45454545454545453)));
        return (int)((double)j * 255.0);
    }

    private static float getColorFraction(int value) {
        return COLOR_FRACTIONS[value & 0xFF];
    }
}

