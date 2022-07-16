/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.realms.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.RealmsServerPlayerList;
import net.minecraft.client.realms.dto.ValueObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsServerPlayerLists
extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    public List<RealmsServerPlayerList> servers;

    public static RealmsServerPlayerLists parse(String json) {
        RealmsServerPlayerLists realmsServerPlayerLists = new RealmsServerPlayerLists();
        realmsServerPlayerLists.servers = Lists.newArrayList();
        try {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();
            if (jsonObject.get("lists").isJsonArray()) {
                JsonArray jsonArray = jsonObject.get("lists").getAsJsonArray();
                Iterator<JsonElement> iterator = jsonArray.iterator();
                while (iterator.hasNext()) {
                    realmsServerPlayerLists.servers.add(RealmsServerPlayerList.parse(iterator.next().getAsJsonObject()));
                }
            }
        }
        catch (Exception jsonParser) {
            LOGGER.error("Could not parse RealmsServerPlayerLists: {}", (Object)jsonParser.getMessage());
        }
        return realmsServerPlayerLists;
    }
}

