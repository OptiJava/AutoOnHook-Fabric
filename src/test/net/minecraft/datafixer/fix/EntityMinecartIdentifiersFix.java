/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Objects;
import net.minecraft.datafixer.TypeReferences;

public class EntityMinecartIdentifiersFix
extends DataFix {
    private static final List<String> MINECARTS = Lists.newArrayList("MinecartRideable", "MinecartChest", "MinecartFurnace");

    public EntityMinecartIdentifiersFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    public TypeRewriteRule makeRule() {
        TaggedChoice.TaggedChoiceType<?> taggedChoiceType = this.getInputSchema().findChoiceType(TypeReferences.ENTITY);
        TaggedChoice.TaggedChoiceType<?> taggedChoiceType2 = this.getOutputSchema().findChoiceType(TypeReferences.ENTITY);
        return this.fixTypeEverywhere("EntityMinecartIdentifiersFix", taggedChoiceType, taggedChoiceType2, dynamicOps -> pair -> {
            if (Objects.equals(pair.getFirst(), "Minecart")) {
                Typed<Pair<String, ?>> typed = taggedChoiceType.point((DynamicOps<?>)dynamicOps, "Minecart", pair.getSecond()).orElseThrow(IllegalStateException::new);
                Dynamic<?> dynamic2 = typed.getOrCreate(DSL.remainderFinder());
                int i = dynamic2.get("Type").asInt(0);
                String string = i > 0 && i < MINECARTS.size() ? MINECARTS.get(i) : "MinecartRideable";
                return Pair.of(string, typed.write().map(dynamic -> taggedChoiceType2.types().get(string).read(dynamic)).result().orElseThrow(() -> new IllegalStateException("Could not read the new minecart.")));
            }
            return pair;
        });
    }
}

