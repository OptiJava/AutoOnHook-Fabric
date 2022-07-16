/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.resource.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.resource.language.ReorderingUtil;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class TranslationStorage
extends Language {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<String, String> translations;
    private final boolean rightToLeft;

    private TranslationStorage(Map<String, String> translations, boolean rightToLeft) {
        this.translations = translations;
        this.rightToLeft = rightToLeft;
    }

    public static TranslationStorage load(ResourceManager resourceManager, List<LanguageDefinition> definitions) {
        HashMap<String, String> map = Maps.newHashMap();
        boolean bl = false;
        for (LanguageDefinition languageDefinition : definitions) {
            bl |= languageDefinition.isRightToLeft();
            String string = String.format("lang/%s.json", languageDefinition.getCode());
            for (String string2 : resourceManager.getAllNamespaces()) {
                try {
                    Identifier identifier = new Identifier(string2, string);
                    TranslationStorage.load(resourceManager.getAllResources(identifier), map);
                }
                catch (FileNotFoundException identifier) {
                }
                catch (Exception identifier) {
                    LOGGER.warn("Skipped language file: {}:{} ({})", (Object)string2, (Object)string, (Object)identifier.toString());
                }
            }
        }
        return new TranslationStorage(ImmutableMap.copyOf(map), bl);
    }

    private static void load(List<Resource> resources, Map<String, String> translationMap) {
        for (Resource resource : resources) {
            try {
                InputStream inputStream = resource.getInputStream();
                try {
                    Language.load(inputStream, translationMap::put);
                }
                finally {
                    if (inputStream == null) continue;
                    inputStream.close();
                }
            }
            catch (IOException inputStream) {
                LOGGER.warn("Failed to load translations from {}", (Object)resource, (Object)inputStream);
            }
        }
    }

    @Override
    public String get(String key) {
        return this.translations.getOrDefault(key, key);
    }

    @Override
    public boolean hasTranslation(String key) {
        return this.translations.containsKey(key);
    }

    @Override
    public boolean isRightToLeft() {
        return this.rightToLeft;
    }

    @Override
    public OrderedText reorder(StringVisitable text) {
        return ReorderingUtil.reorder(text, this.rightToLeft);
    }
}

