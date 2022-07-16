/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.stream.Collectors;
import net.minecraft.datafixer.TypeReferences;

public class OptionsKeyTranslationFix
extends DataFix {
    public OptionsKeyTranslationFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("OptionsKeyTranslationFix", this.getInputSchema().getType(TypeReferences.OPTIONS), typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.getMapValues().map(map -> dynamic.createMap(map.entrySet().stream().map(entry -> {
            String string;
            if (((Dynamic)entry.getKey()).asString("").startsWith("key_") && !(string = ((Dynamic)entry.getValue()).asString("")).startsWith("key.mouse") && !string.startsWith("scancode.")) {
                return Pair.of((Dynamic)entry.getKey(), dynamic.createString("key.keyboard." + string.substring("key.".length())));
            }
            return Pair.of((Dynamic)entry.getKey(), (Dynamic)entry.getValue());
        }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)))).result().orElse((Dynamic)dynamic)));
    }
}
