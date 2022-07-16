/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;

public class EntityWolfColorFix
extends ChoiceFix {
    public EntityWolfColorFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType, "EntityWolfColorFix", TypeReferences.ENTITY, "minecraft:wolf");
    }

    public Dynamic<?> fixCollarColor(Dynamic<?> dynamic2) {
        return dynamic2.update("CollarColor", dynamic -> dynamic.createByte((byte)(15 - dynamic.asInt(0))));
    }

    @Override
    protected Typed<?> transform(Typed<?> inputType) {
        return inputType.update(DSL.remainderFinder(), this::fixCollarColor);
    }
}

