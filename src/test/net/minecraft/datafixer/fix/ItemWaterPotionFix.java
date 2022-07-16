/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class ItemWaterPotionFix
extends DataFix {
    public ItemWaterPotionFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
        OpticFinder<Pair<String, String>> opticFinder = DSL.fieldFinder("id", DSL.named(TypeReferences.ITEM_NAME.typeName(), IdentifierNormalizingSchema.getIdentifierType()));
        OpticFinder<?> opticFinder2 = type.findField("tag");
        return this.fixTypeEverywhereTyped("ItemWaterPotionFix", type, typed -> {
            String string;
            Optional optional = typed.getOptional(opticFinder);
            if (optional.isPresent() && ("minecraft:potion".equals(string = (String)((Pair)optional.get()).getSecond()) || "minecraft:splash_potion".equals(string) || "minecraft:lingering_potion".equals(string) || "minecraft:tipped_arrow".equals(string))) {
                Typed<Dynamic<?>> typed2 = typed.getOrCreateTyped(opticFinder2);
                Dynamic dynamic = typed2.get(DSL.remainderFinder());
                if (!dynamic.get("Potion").asString().result().isPresent()) {
                    dynamic = dynamic.set("Potion", dynamic.createString("minecraft:water"));
                }
                return typed.set(opticFinder2, typed2.set(DSL.remainderFinder(), dynamic));
            }
            return typed;
        });
    }
}

