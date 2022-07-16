/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.realms.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.ValueObject;
import net.minecraft.client.realms.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsServerPlayerList
extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final JsonParser JSON_PARSER = new JsonParser();
    public long serverId;
    public List<String> players;

    public static RealmsServerPlayerList parse(JsonObject node) {
        RealmsServerPlayerList realmsServerPlayerList = new RealmsServerPlayerList();
        try {
            JsonElement jsonElement;
            realmsServerPlayerList.serverId = JsonUtils.getLongOr("serverId", node, -1L);
            String string = JsonUtils.getStringOr("playerList", node, null);
            realmsServerPlayerList.players = string != null ? ((jsonElement = JSON_PARSER.parse(string)).isJsonArray() ? RealmsServerPlayerList.parsePlayers(jsonElement.getAsJsonArray()) : Lists.newArrayList()) : Lists.newArrayList();
        }
        catch (Exception string) {
            LOGGER.error("Could not parse RealmsServerPlayerList: {}", (Object)string.getMessage());
        }
        return realmsServerPlayerList;
    }

    private static List<String> parsePlayers(JsonArray jsonArray) {
        ArrayList<String> list = Lists.newArrayList();
        for (JsonElement jsonElement : jsonArray) {
            try {
                list.add(jsonElement.getAsString());
            }
            catch (Exception exception) {}
        }
        return list;
    }
}

