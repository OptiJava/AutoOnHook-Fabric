/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.font;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.BlankFont;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.FontType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class FontManager
implements AutoCloseable {
    static final Logger LOGGER = LogManager.getLogger();
    private static final String FONTS_JSON = "fonts.json";
    public static final Identifier MISSING_STORAGE_ID = new Identifier("minecraft", "missing");
    private final FontStorage missingStorage;
    final Map<Identifier, FontStorage> fontStorages = Maps.newHashMap();
    final TextureManager textureManager;
    private Map<Identifier, Identifier> idOverrides = ImmutableMap.of();
    private final ResourceReloader resourceReloadListener = new SinglePreparationResourceReloader<Map<Identifier, List<Font>>>(){

        @Override
        protected Map<Identifier, List<Font>> prepare(ResourceManager resourceManager, Profiler profiler) {
            profiler.startTick();
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            HashMap<Identifier, List<Font>> map = Maps.newHashMap();
            for (Identifier identifier : resourceManager.findResources("font", fileName -> fileName.endsWith(".json"))) {
                String string = identifier.getPath();
                Identifier identifier2 = new Identifier(identifier.getNamespace(), string.substring("font/".length(), string.length() - ".json".length()));
                List list = map.computeIfAbsent(identifier2, id -> Lists.newArrayList(new BlankFont()));
                profiler.push(identifier2::toString);
                try {
                    for (Resource resource : resourceManager.getAllResources(identifier)) {
                        profiler.push(resource::getResourcePackName);
                        try (Closeable inputStream = resource.getInputStream();
                             BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream)inputStream, StandardCharsets.UTF_8));){
                            profiler.push("reading");
                            JsonArray jsonArray = JsonHelper.getArray(JsonHelper.deserialize(gson, (Reader)reader, JsonObject.class), "providers");
                            profiler.swap("parsing");
                            for (int i = jsonArray.size() - 1; i >= 0; --i) {
                                JsonObject jsonObject = JsonHelper.asObject(jsonArray.get(i), "providers[" + i + "]");
                                try {
                                    String string2 = JsonHelper.getString(jsonObject, "type");
                                    FontType fontType = FontType.byId(string2);
                                    profiler.push(string2);
                                    Font font = fontType.createLoader(jsonObject).load(resourceManager);
                                    if (font != null) {
                                        list.add(font);
                                    }
                                    profiler.pop();
                                    continue;
                                }
                                catch (RuntimeException string2) {
                                    LOGGER.warn("Unable to read definition '{}' in {} in resourcepack: '{}': {}", (Object)identifier2, (Object)FontManager.FONTS_JSON, (Object)resource.getResourcePackName(), (Object)string2.getMessage());
                                }
                            }
                            profiler.pop();
                        }
                        catch (RuntimeException inputStream2) {
                            LOGGER.warn("Unable to load font '{}' in {} in resourcepack: '{}': {}", (Object)identifier2, (Object)FontManager.FONTS_JSON, (Object)resource.getResourcePackName(), (Object)inputStream2.getMessage());
                        }
                        profiler.pop();
                    }
                }
                catch (IOException iOException) {
                    LOGGER.warn("Unable to load font '{}' in {}: {}", (Object)identifier2, (Object)FontManager.FONTS_JSON, (Object)iOException.getMessage());
                }
                profiler.push("caching");
                IntOpenHashSet iOException = new IntOpenHashSet();
                for (Closeable inputStream : list) {
                    iOException.addAll(inputStream.getProvidedGlyphs());
                }
                iOException.forEach(codePoint -> {
                    Font font;
                    if (codePoint == 32) {
                        return;
                    }
                    Iterator iterator = Lists.reverse(list).iterator();
                    while (iterator.hasNext() && (font = (Font)iterator.next()).getGlyph(codePoint) == null) {
                    }
                });
                profiler.pop();
                profiler.pop();
            }
            profiler.endTick();
            return map;
        }

        @Override
        protected void apply(Map<Identifier, List<Font>> map, ResourceManager resourceManager, Profiler profiler) {
            profiler.startTick();
            profiler.push("closing");
            FontManager.this.fontStorages.values().forEach(FontStorage::close);
            FontManager.this.fontStorages.clear();
            profiler.swap("reloading");
            map.forEach((id, fonts) -> {
                FontStorage fontStorage = new FontStorage(FontManager.this.textureManager, (Identifier)id);
                fontStorage.setFonts(Lists.reverse(fonts));
                FontManager.this.fontStorages.put((Identifier)id, fontStorage);
            });
            profiler.pop();
            profiler.endTick();
        }

        @Override
        public String getName() {
            return "FontManager";
        }

        @Override
        protected /* synthetic */ Object prepare(ResourceManager manager, Profiler profiler) {
            return this.prepare(manager, profiler);
        }
    };

    public FontManager(TextureManager manager) {
        this.textureManager = manager;
        this.missingStorage = Util.make(new FontStorage(manager, MISSING_STORAGE_ID), fontStorage -> fontStorage.setFonts(Lists.newArrayList(new BlankFont())));
    }

    public void setIdOverrides(Map<Identifier, Identifier> overrides) {
        this.idOverrides = overrides;
    }

    public TextRenderer createTextRenderer() {
        return new TextRenderer(id -> this.fontStorages.getOrDefault(this.idOverrides.getOrDefault(id, (Identifier)id), this.missingStorage));
    }

    public ResourceReloader getResourceReloadListener() {
        return this.resourceReloadListener;
    }

    @Override
    public void close() {
        this.fontStorages.values().forEach(FontStorage::close);
        this.missingStorage.close();
    }
}

