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
import java.util.Objects;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class OminousBannerItemRenameFix
extends DataFix {
    public OminousBannerItemRenameFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    private Dynamic<?> fixBannerName(Dynamic<?> dynamic) {
        Optional<Dynamic<?>> optional = dynamic.get("display").result();
        if (optional.isPresent()) {
            Dynamic dynamic2 = optional.get();
            Optional<String> optional2 = dynamic2.get("Name").asString().result();
            if (optional2.isPresent()) {
                String string = optional2.get();
                string = string.replace("\"translate\":\"block.minecraft.illager_banner\"", "\"translate\":\"block.minecraft.ominous_banner\"");
                dynamic2 = dynamic2.set("Name", dynamic2.createString(string));
            }
            return dynamic.set("display", dynamic2);
        }
        return dynamic;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
        OpticFinder<Pair<String, String>> opticFinder = DSL.fieldFinder("id", DSL.named(TypeReferences.ITEM_NAME.typeName(), IdentifierNormalizingSchema.getIdentifierType()));
        OpticFinder<?> opticFinder2 = type.findField("tag");
        return this.fixTypeEverywhereTyped("OminousBannerRenameFix", type, typed -> {
            Optional optional2;
            Optional optional = typed.getOptional(opticFinder);
            if (optional.isPresent() && Objects.equals(((Pair)optional.get()).getSecond(), "minecraft:white_banner") && (optional2 = typed.getOptionalTyped(opticFinder2)).isPresent()) {
                Typed<Dynamic<?>> typed2 = optional2.get();
                Dynamic<?> dynamic = typed2.get(DSL.remainderFinder());
                return typed.set(opticFinder2, typed2.set(DSL.remainderFinder(), this.fixBannerName(dynamic)));
            }
            return typed;
        });
    }
}

