/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;

public class EntityEquipmentToArmorAndHandFix
extends DataFix {
    public EntityEquipmentToArmorAndHandFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixEquipment(this.getInputSchema().getTypeRaw(TypeReferences.ITEM_STACK));
    }

    private <IS> TypeRewriteRule fixEquipment(Type<IS> type) {
        Type<Pair<Either<IS, Unit>, Dynamic<?>>> type2 = DSL.and(DSL.optional(DSL.field("Equipment", DSL.list(type))), DSL.remainderType());
        Type type3 = DSL.and(DSL.optional(DSL.field("ArmorItems", DSL.list(type))), DSL.optional(DSL.field("HandItems", DSL.list(type))), DSL.remainderType());
        OpticFinder opticFinder = DSL.typeFinder(type2);
        OpticFinder opticFinder2 = DSL.fieldFinder("Equipment", DSL.list(type));
        return this.fixTypeEverywhereTyped("EntityEquipmentToArmorAndHandFix", this.getInputSchema().getType(TypeReferences.ENTITY), this.getOutputSchema().getType(TypeReferences.ENTITY), (Typed<?> typed) -> {
            Object list2;
            Optional<Stream<Dynamic<Object>>> object;
            Object list;
            Either<Object, Unit> either = Either.right(DSL.unit());
            Either<Object, Unit> either2 = Either.right(DSL.unit());
            Dynamic dynamic = typed.getOrCreate(DSL.remainderFinder());
            Optional optional = typed.getOptional(opticFinder2);
            if (optional.isPresent()) {
                list = (List)optional.get();
                object = type.read(dynamic.emptyMap()).result().orElseThrow(() -> new IllegalStateException("Could not parse newly created empty itemstack.")).getFirst();
                if (!list.isEmpty()) {
                    either = Either.left(Lists.newArrayList(list.get(0), object));
                }
                if (list.size() > 1) {
                    list2 = Lists.newArrayList(object, object, object, object);
                    for (int i = 1; i < Math.min(list.size(), 5); ++i) {
                        list2.set(i - 1, list.get(i));
                    }
                    either2 = Either.left(list2);
                }
            }
            list = dynamic;
            object = dynamic.get("DropChances").asStreamOpt().result();
            if (object.isPresent()) {
                Dynamic dynamic2;
                list2 = Stream.concat(object.get(), Stream.generate(() -> EntityEquipmentToArmorAndHandFix.method_15701((Dynamic)list))).iterator();
                float i = ((Dynamic)list2.next()).asFloat(0.0f);
                if (!dynamic.get("HandDropChances").result().isPresent()) {
                    dynamic2 = dynamic.createList(Stream.of(Float.valueOf(i), Float.valueOf(0.0f)).map(dynamic::createFloat));
                    dynamic = dynamic.set("HandDropChances", dynamic2);
                }
                if (!dynamic.get("ArmorDropChances").result().isPresent()) {
                    dynamic2 = dynamic.createList(Stream.of(Float.valueOf(((Dynamic)list2.next()).asFloat(0.0f)), Float.valueOf(((Dynamic)list2.next()).asFloat(0.0f)), Float.valueOf(((Dynamic)list2.next()).asFloat(0.0f)), Float.valueOf(((Dynamic)list2.next()).asFloat(0.0f))).map(dynamic::createFloat));
                    dynamic = dynamic.set("ArmorDropChances", dynamic2);
                }
                dynamic = dynamic.remove("DropChances");
            }
            return typed.set(opticFinder, type3, Pair.of(either, Pair.of(either2, dynamic)));
        });
    }

    private static /* synthetic */ Dynamic method_15701(Dynamic dynamic) {
        return dynamic.createInt(0);
    }
}

