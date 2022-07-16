/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.util;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public class LowercaseEnumTypeAdapterFactory
implements TypeAdapterFactory {
    @Override
    @Nullable
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Class<T> class_ = typeToken.getRawType();
        if (!class_.isEnum()) {
            return null;
        }
        final HashMap<String, T> map = Maps.newHashMap();
        for (T object : class_.getEnumConstants()) {
            map.put(this.getKey(object), object);
        }
        return new TypeAdapter<T>(){

            @Override
            public void write(JsonWriter jsonWriter, T object) throws IOException {
                if (object == null) {
                    jsonWriter.nullValue();
                } else {
                    jsonWriter.value(LowercaseEnumTypeAdapterFactory.this.getKey(object));
                }
            }

            @Override
            @Nullable
            public T read(JsonReader jsonReader) throws IOException {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.nextNull();
                    return null;
                }
                return map.get(jsonReader.nextString());
            }
        };
    }

    String getKey(Object o) {
        if (o instanceof Enum) {
            return ((Enum)o).name().toLowerCase(Locale.ROOT);
        }
        return o.toString().toLowerCase(Locale.ROOT);
    }
}

