/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.resource.metadata;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.metadata.TextureResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.JsonHelper;

@Environment(value=EnvType.CLIENT)
public class TextureResourceMetadataReader
implements ResourceMetadataReader<TextureResourceMetadata> {
    @Override
    public TextureResourceMetadata fromJson(JsonObject jsonObject) {
        boolean bl = JsonHelper.getBoolean(jsonObject, "blur", false);
        boolean bl2 = JsonHelper.getBoolean(jsonObject, "clamp", false);
        return new TextureResourceMetadata(bl, bl2);
    }

    @Override
    public String getKey() {
        return "texture";
    }

    @Override
    public /* synthetic */ Object fromJson(JsonObject json) {
        return this.fromJson(json);
    }
}

