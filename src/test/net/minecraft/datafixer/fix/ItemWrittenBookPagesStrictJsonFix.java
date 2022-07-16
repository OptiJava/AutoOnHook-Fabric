/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.datafixer.fix;

import com.google.gson.JsonParseException;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.BlockEntitySignTextStrictJsonFix;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import org.apache.commons.lang3.StringUtils;

public class ItemWrittenBookPagesStrictJsonFix
extends DataFix {
    public ItemWrittenBookPagesStrictJsonFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    public Dynamic<?> fixBookPages(Dynamic<?> dynamic) {
        return dynamic.update("pages", dynamic2 -> DataFixUtils.orElse(dynamic2.asStreamOpt().map(stream -> stream.map(dynamic -> {
            if (!dynamic.asString().result().isPresent()) {
                return dynamic;
            }
            String string = dynamic.asString("");
            Text text = null;
            if ("null".equals(string) || StringUtils.isEmpty(string)) {
                text = LiteralText.EMPTY;
            } else if (string.charAt(0) == '\"' && string.charAt(string.length() - 1) == '\"' || string.charAt(0) == '{' && string.charAt(string.length() - 1) == '}') {
                try {
                    text = JsonHelper.deserialize(BlockEntitySignTextStrictJsonFix.GSON, string, Text.class, true);
                    if (text == null) {
                        text = LiteralText.EMPTY;
                    }
                }
                catch (JsonParseException jsonParseException) {
                    // empty catch block
                }
                if (text == null) {
                    try {
                        text = Text.Serializer.fromJson(string);
                    }
                    catch (JsonParseException jsonParseException) {
                        // empty catch block
                    }
                }
                if (text == null) {
                    try {
                        text = Text.Serializer.fromLenientJson(string);
                    }
                    catch (JsonParseException jsonParseException) {
                        // empty catch block
                    }
                }
                if (text == null) {
                    text = new LiteralText(string);
                }
            } else {
                text = new LiteralText(string);
            }
            return dynamic.createString(Text.Serializer.toJson(text));
        })).map(dynamic::createList).result(), dynamic.emptyList()));
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
        OpticFinder<?> opticFinder = type.findField("tag");
        return this.fixTypeEverywhereTyped("ItemWrittenBookPagesStrictJsonFix", type, typed2 -> typed2.updateTyped(opticFinder, typed -> typed.update(DSL.remainderFinder(), this::fixBookPages)));
    }
}

