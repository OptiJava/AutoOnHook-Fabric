/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.datafixer.fix.EntitySimpleTransformFix;

public class EntityCatSplitFix
extends EntitySimpleTransformFix {
    public EntityCatSplitFix(Schema outputSchema, boolean changesType) {
        super("EntityCatSplitFix", outputSchema, changesType);
    }

    @Override
    protected Pair<String, Dynamic<?>> transform(String choice, Dynamic<?> dynamic) {
        if (Objects.equals("minecraft:ocelot", choice)) {
            int i = dynamic.get("CatType").asInt(0);
            if (i == 0) {
                String string = dynamic.get("Owner").asString("");
                String string2 = dynamic.get("OwnerUUID").asString("");
                if (string.length() > 0 || string2.length() > 0) {
                    dynamic.set("Trusting", dynamic.createBoolean(true));
                }
            } else if (i > 0 && i < 4) {
                dynamic = dynamic.set("CatType", dynamic.createInt(i));
                dynamic = dynamic.set("OwnerUUID", dynamic.createString(dynamic.get("OwnerUUID").asString("")));
                return Pair.of("minecraft:cat", dynamic);
            }
        }
        return Pair.of(choice, dynamic);
    }
}

