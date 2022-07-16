/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.resource.metadata;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;

public class PackResourceMetadataReader
implements ResourceMetadataReader<PackResourceMetadata> {
    @Override
    public PackResourceMetadata fromJson(JsonObject jsonObject) {
        MutableText text = Text.Serializer.fromJson(jsonObject.get("description"));
        if (text == null) {
            throw new JsonParseException("Invalid/missing description!");
        }
        int i = JsonHelper.getInt(jsonObject, "pack_format");
        return new PackResourceMetadata(text, i);
    }

    @Override
    public String getKey() {
        return "pack";
    }

    @Override
    public /* synthetic */ Object fromJson(JsonObject json) {
        return this.fromJson(json);
    }
}

