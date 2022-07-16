/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.datafixer.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.Const;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.util.Identifier;

public class IdentifierNormalizingSchema
extends Schema {
    public static final PrimitiveCodec<String> CODEC = new PrimitiveCodec<String>(){

        @Override
        public <T> DataResult<String> read(DynamicOps<T> dynamicOps, T object) {
            return dynamicOps.getStringValue(object).map(IdentifierNormalizingSchema::normalize);
        }

        @Override
        public <T> T write(DynamicOps<T> dynamicOps, String string) {
            return dynamicOps.createString(string);
        }

        public String toString() {
            return "NamespacedString";
        }

        @Override
        public /* synthetic */ Object write(DynamicOps dynamicOps, Object object) {
            return this.write(dynamicOps, (String)object);
        }
    };
    private static final Type<String> IDENTIFIER_TYPE = new Const.PrimitiveType<String>(CODEC);

    public IdentifierNormalizingSchema(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    public static String normalize(String id) {
        Identifier identifier = Identifier.tryParse(id);
        if (identifier != null) {
            return identifier.toString();
        }
        return id;
    }

    public static Type<String> getIdentifierType() {
        return IDENTIFIER_TYPE;
    }

    @Override
    public Type<?> getChoiceType(DSL.TypeReference typeReference, String string) {
        return super.getChoiceType(typeReference, IdentifierNormalizingSchema.normalize(string));
    }
}

