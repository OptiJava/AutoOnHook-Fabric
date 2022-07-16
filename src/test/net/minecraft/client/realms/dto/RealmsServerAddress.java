/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.realms.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.ValueObject;
import net.minecraft.client.realms.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsServerAddress
extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    public String address;
    public String resourcePackUrl;
    public String resourcePackHash;

    public static RealmsServerAddress parse(String json) {
        JsonParser jsonParser = new JsonParser();
        RealmsServerAddress realmsServerAddress = new RealmsServerAddress();
        try {
            JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();
            realmsServerAddress.address = JsonUtils.getStringOr("address", jsonObject, null);
            realmsServerAddress.resourcePackUrl = JsonUtils.getStringOr("resourcePackUrl", jsonObject, null);
            realmsServerAddress.resourcePackHash = JsonUtils.getStringOr("resourcePackHash", jsonObject, null);
        }
        catch (Exception jsonObject) {
            LOGGER.error("Could not parse RealmsServerAddress: {}", (Object)jsonObject.getMessage());
        }
        return realmsServerAddress;
    }
}

