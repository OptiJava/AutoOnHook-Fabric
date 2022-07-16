/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.realms.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.PlayerActivity;
import net.minecraft.client.realms.dto.ValueObject;
import net.minecraft.client.realms.util.JsonUtils;

@Environment(value=EnvType.CLIENT)
public class PlayerActivities
extends ValueObject {
    public long periodInMillis;
    public List<PlayerActivity> playerActivityDto = Lists.newArrayList();

    public static PlayerActivities parse(String json) {
        PlayerActivities playerActivities = new PlayerActivities();
        JsonParser jsonParser = new JsonParser();
        try {
            JsonElement jsonElement = jsonParser.parse(json);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            playerActivities.periodInMillis = JsonUtils.getLongOr("periodInMillis", jsonObject, -1L);
            JsonElement jsonElement2 = jsonObject.get("playerActivityDto");
            if (jsonElement2 != null && jsonElement2.isJsonArray()) {
                JsonArray jsonArray = jsonElement2.getAsJsonArray();
                for (JsonElement jsonElement3 : jsonArray) {
                    PlayerActivity playerActivity = PlayerActivity.parse(jsonElement3.getAsJsonObject());
                    playerActivities.playerActivityDto.add(playerActivity);
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return playerActivities;
    }
}

