/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.font;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.platform.TextureUtil;
import java.io.IOException;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontLoader;
import net.minecraft.client.font.TrueTypeFont;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Struct;

@Environment(value=EnvType.CLIENT)
public class TrueTypeFontLoader
implements FontLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Identifier filename;
    private final float size;
    private final float oversample;
    private final float shiftX;
    private final float shiftY;
    private final String excludedCharacters;

    public TrueTypeFontLoader(Identifier filename, float size, float oversample, float shiftX, float shiftY, String excludedCharacters) {
        this.filename = filename;
        this.size = size;
        this.oversample = oversample;
        this.shiftX = shiftX;
        this.shiftY = shiftY;
        this.excludedCharacters = excludedCharacters;
    }

    public static FontLoader fromJson(JsonObject json) {
        Object jsonArray;
        float f = 0.0f;
        float g = 0.0f;
        if (json.has("shift")) {
            jsonArray = json.getAsJsonArray("shift");
            if (((JsonArray)jsonArray).size() != 2) {
                throw new JsonParseException("Expected 2 elements in 'shift', found " + ((JsonArray)jsonArray).size());
            }
            f = JsonHelper.asFloat(((JsonArray)jsonArray).get(0), "shift[0]");
            g = JsonHelper.asFloat(((JsonArray)jsonArray).get(1), "shift[1]");
        }
        jsonArray = new StringBuilder();
        if (json.has("skip")) {
            JsonElement jsonElement = json.get("skip");
            if (jsonElement.isJsonArray()) {
                JsonArray jsonArray2 = JsonHelper.asArray(jsonElement, "skip");
                for (int i = 0; i < jsonArray2.size(); ++i) {
                    ((StringBuilder)jsonArray).append(JsonHelper.asString(jsonArray2.get(i), "skip[" + i + "]"));
                }
            } else {
                ((StringBuilder)jsonArray).append(JsonHelper.asString(jsonElement, "skip"));
            }
        }
        return new TrueTypeFontLoader(new Identifier(JsonHelper.getString(json, "file")), JsonHelper.getFloat(json, "size", 11.0f), JsonHelper.getFloat(json, "oversample", 1.0f), f, g, ((StringBuilder)jsonArray).toString());
    }

    @Override
    @Nullable
    public Font load(ResourceManager manager) {
        TrueTypeFont trueTypeFont;
        block10: {
            Struct sTBTTFontinfo = null;
            ByteBuffer byteBuffer = null;
            Resource resource = manager.getResource(new Identifier(this.filename.getNamespace(), "font/" + this.filename.getPath()));
            try {
                LOGGER.debug("Loading font {}", (Object)this.filename);
                sTBTTFontinfo = STBTTFontinfo.malloc();
                byteBuffer = TextureUtil.readResource(resource.getInputStream());
                byteBuffer.flip();
                LOGGER.debug("Reading font {}", (Object)this.filename);
                if (!STBTruetype.stbtt_InitFont((STBTTFontinfo)sTBTTFontinfo, byteBuffer)) {
                    throw new IOException("Invalid ttf");
                }
                trueTypeFont = new TrueTypeFont(byteBuffer, (STBTTFontinfo)sTBTTFontinfo, this.size, this.oversample, this.shiftX, this.shiftY, this.excludedCharacters);
                if (resource == null) break block10;
            }
            catch (Throwable throwable) {
                try {
                    if (resource != null) {
                        try {
                            resource.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception resource2) {
                    LOGGER.error("Couldn't load truetype font {}", (Object)this.filename, (Object)resource2);
                    if (sTBTTFontinfo != null) {
                        sTBTTFontinfo.free();
                    }
                    MemoryUtil.memFree(byteBuffer);
                    return null;
                }
            }
            resource.close();
        }
        return trueTypeFont;
    }
}

