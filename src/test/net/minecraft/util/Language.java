/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import net.minecraft.client.font.TextVisitFactory;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.util.JsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Language {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    private static final Pattern TOKEN_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
    public static final String DEFAULT_LANGUAGE = "en_us";
    private static volatile Language instance = Language.create();

    private static Language create() {
        Object inputStream;
        ImmutableMap.Builder builder = ImmutableMap.builder();
        BiConsumer<String, String> biConsumer = builder::put;
        String string = "/assets/minecraft/lang/en_us.json";
        try {
            inputStream = Language.class.getResourceAsStream("/assets/minecraft/lang/en_us.json");
            try {
                Language.load((InputStream)inputStream, biConsumer);
            }
            finally {
                if (inputStream != null) {
                    ((InputStream)inputStream).close();
                }
            }
        }
        catch (JsonParseException | IOException inputStream2) {
            LOGGER.error("Couldn't read strings from {}", (Object)"/assets/minecraft/lang/en_us.json", (Object)inputStream2);
        }
        inputStream = builder.build();
        return new Language((Map)inputStream){
            final /* synthetic */ Map field_25308;
            {
                this.field_25308 = map;
            }

            @Override
            public String get(String key) {
                return this.field_25308.getOrDefault(key, key);
            }

            @Override
            public boolean hasTranslation(String key) {
                return this.field_25308.containsKey(key);
            }

            @Override
            public boolean isRightToLeft() {
                return false;
            }

            @Override
            public OrderedText reorder(StringVisitable text) {
                return visitor -> text.visit((style, string) -> TextVisitFactory.visitFormatted(string, style, visitor) ? Optional.empty() : StringVisitable.TERMINATE_VISIT, Style.EMPTY).isPresent();
            }
        };
    }

    public static void load(InputStream inputStream, BiConsumer<String, String> entryConsumer) {
        JsonObject jsonObject = GSON.fromJson((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonObject.class);
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String string = TOKEN_PATTERN.matcher(JsonHelper.asString(entry.getValue(), entry.getKey())).replaceAll("%$1s");
            entryConsumer.accept(entry.getKey(), string);
        }
    }

    public static Language getInstance() {
        return instance;
    }

    public static void setInstance(Language language) {
        instance = language;
    }

    public abstract String get(String var1);

    public abstract boolean hasTranslation(String var1);

    public abstract boolean isRightToLeft();

    public abstract OrderedText reorder(StringVisitable var1);

    public List<OrderedText> reorder(List<StringVisitable> texts) {
        return texts.stream().map(this::reorder).collect(ImmutableList.toImmutableList());
    }
}

