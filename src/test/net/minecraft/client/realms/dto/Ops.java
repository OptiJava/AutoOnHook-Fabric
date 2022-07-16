/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.realms.dto;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.ValueObject;

@Environment(value=EnvType.CLIENT)
public class Ops
extends ValueObject {
    public Set<String> ops = Sets.newHashSet();

    public static Ops parse(String json) {
        Ops ops = new Ops();
        JsonParser jsonParser = new JsonParser();
        try {
            JsonElement jsonElement = jsonParser.parse(json);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonElement jsonElement2 = jsonObject.get("ops");
            if (jsonElement2.isJsonArray()) {
                for (JsonElement jsonElement3 : jsonElement2.getAsJsonArray()) {
                    ops.ops.add(jsonElement3.getAsString());
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return ops;
    }
}

