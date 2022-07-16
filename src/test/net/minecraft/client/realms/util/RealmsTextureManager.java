/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.realms.util;

import com.google.common.collect.Maps;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.util.UUIDTypeAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.realms.util.RealmsUtil;
import net.minecraft.client.realms.util.SkinProcessor;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RealmsTextureManager {
    private static final Map<String, RealmsTexture> TEXTURES = Maps.newHashMap();
    static final Map<String, Boolean> SKIN_FETCH_STATUS = Maps.newHashMap();
    static final Map<String, String> FETCHED_SKINS = Maps.newHashMap();
    static final Logger LOGGER = LogManager.getLogger();
    private static final Identifier ISLES = new Identifier("textures/gui/presets/isles.png");

    public static void bindWorldTemplate(String id, @Nullable String image) {
        if (image == null) {
            RenderSystem.setShaderTexture(0, ISLES);
            return;
        }
        int i = RealmsTextureManager.getTextureId(id, image);
        RenderSystem.setShaderTexture(0, i);
    }

    public static void withBoundFace(String uuid, Runnable r) {
        RealmsTextureManager.bindFace(uuid);
        r.run();
    }

    private static void bindDefaultFace(UUID uuid) {
        RenderSystem.setShaderTexture(0, DefaultSkinHelper.getTexture(uuid));
    }

    private static void bindFace(final String uuid) {
        UUID uUID = UUIDTypeAdapter.fromString(uuid);
        if (TEXTURES.containsKey(uuid)) {
            int i = RealmsTextureManager.TEXTURES.get((Object)uuid).textureId;
            RenderSystem.setShaderTexture(0, i);
            return;
        }
        if (SKIN_FETCH_STATUS.containsKey(uuid)) {
            if (!SKIN_FETCH_STATUS.get(uuid).booleanValue()) {
                RealmsTextureManager.bindDefaultFace(uUID);
            } else if (FETCHED_SKINS.containsKey(uuid)) {
                int i = RealmsTextureManager.getTextureId(uuid, FETCHED_SKINS.get(uuid));
                RenderSystem.setShaderTexture(0, i);
            } else {
                RealmsTextureManager.bindDefaultFace(uUID);
            }
            return;
        }
        SKIN_FETCH_STATUS.put(uuid, false);
        RealmsTextureManager.bindDefaultFace(uUID);
        Thread i = new Thread("Realms Texture Downloader"){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                block17: {
                    block16: {
                        ByteArrayOutputStream exception;
                        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = RealmsUtil.getTextures(uuid);
                        if (!map.containsKey((Object)MinecraftProfileTexture.Type.SKIN)) break block16;
                        MinecraftProfileTexture minecraftProfileTexture = map.get((Object)MinecraftProfileTexture.Type.SKIN);
                        String string = minecraftProfileTexture.getUrl();
                        HttpURLConnection httpURLConnection = null;
                        LOGGER.debug("Downloading http texture from {}", (Object)string);
                        try {
                            BufferedImage bufferedImage;
                            httpURLConnection = (HttpURLConnection)new URL(string).openConnection(MinecraftClient.getInstance().getNetworkProxy());
                            httpURLConnection.setDoInput(true);
                            httpURLConnection.setDoOutput(false);
                            httpURLConnection.connect();
                            if (httpURLConnection.getResponseCode() / 100 != 2) {
                                SKIN_FETCH_STATUS.remove(uuid);
                                return;
                            }
                            try {
                                bufferedImage = ImageIO.read(httpURLConnection.getInputStream());
                            }
                            catch (Exception exception2) {
                                SKIN_FETCH_STATUS.remove(uuid);
                                if (httpURLConnection != null) {
                                    httpURLConnection.disconnect();
                                }
                                return;
                            }
                            finally {
                                IOUtils.closeQuietly(httpURLConnection.getInputStream());
                            }
                            bufferedImage = new SkinProcessor().process(bufferedImage);
                            exception = new ByteArrayOutputStream();
                        }
                        catch (Exception bufferedImage) {
                            LOGGER.error("Couldn't download http texture", (Throwable)bufferedImage);
                            SKIN_FETCH_STATUS.remove(uuid);
                        }
                        finally {
                            if (httpURLConnection != null) {
                                httpURLConnection.disconnect();
                            }
                        }
                        ImageIO.write((RenderedImage)bufferedImage, "png", exception);
                        FETCHED_SKINS.put(uuid, new Base64().encodeToString(exception.toByteArray()));
                        SKIN_FETCH_STATUS.put(uuid, true);
                        break block17;
                    }
                    SKIN_FETCH_STATUS.put(uuid, true);
                }
            }
        };
        i.setDaemon(true);
        i.start();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static int getTextureId(String id, String image) {
        RealmsTexture realmsTexture = TEXTURES.get(id);
        if (realmsTexture != null && realmsTexture.image.equals(image)) {
            return realmsTexture.textureId;
        }
        int i = realmsTexture != null ? realmsTexture.textureId : GlStateManager._genTexture();
        IntBuffer intBuffer = null;
        int j = 0;
        int k = 0;
        try {
            BufferedImage bufferedImage;
            ByteArrayInputStream inputStream = new ByteArrayInputStream(new Base64().decode(image));
            try {
                bufferedImage = ImageIO.read(inputStream);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
            }
            j = bufferedImage.getWidth();
            k = bufferedImage.getHeight();
            int[] is = new int[j * k];
            bufferedImage.getRGB(0, 0, j, k, is, 0, j);
            intBuffer = ByteBuffer.allocateDirect(4 * j * k).order(ByteOrder.nativeOrder()).asIntBuffer();
            intBuffer.put(is);
            intBuffer.flip();
        }
        catch (IOException bufferedImage) {
            bufferedImage.printStackTrace();
        }
        RenderSystem.activeTexture(33984);
        RenderSystem.bindTextureForSetup(i);
        TextureUtil.initTexture(intBuffer, j, k);
        TEXTURES.put(id, new RealmsTexture(image, i));
        return i;
    }

    @Environment(value=EnvType.CLIENT)
    public static class RealmsTexture {
        final String image;
        final int textureId;

        public RealmsTexture(String image, int textureId) {
            this.image = image;
            this.textureId = textureId;
        }
    }
}

