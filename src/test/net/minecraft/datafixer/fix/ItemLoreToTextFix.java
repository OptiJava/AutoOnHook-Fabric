/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class ItemLoreToTextFix
extends DataFix {
    public ItemLoreToTextFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
        OpticFinder<?> opticFinder = type.findField("tag");
        return this.fixTypeEverywhereTyped("Item Lore componentize", type, typed2 -> typed2.updateTyped(opticFinder, typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.update("display", dynamic2 -> dynamic2.update("Lore", dynamic -> DataFixUtils.orElse(dynamic.asStreamOpt().map(ItemLoreToTextFix::fixLoreNbt).map(dynamic::createList).result(), dynamic))))));
    }

    private static <T> Stream<Dynamic<T>> fixLoreNbt(Stream<Dynamic<T>> nbt) {
        return nbt.map(dynamic -> DataFixUtils.orElse(dynamic.asString().map(ItemLoreToTextFix::componentize).map(dynamic::createString).result(), dynamic));
    }

    private static String componentize(String string) {
        return Text.Serializer.toJson(new LiteralText(string));
    }
}
