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
public class Subscription
extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    public long startDate;
    public int daysLeft;
    public SubscriptionType type = SubscriptionType.NORMAL;

    public static Subscription parse(String json) {
        Subscription subscription = new Subscription();
        try {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();
            subscription.startDate = JsonUtils.getLongOr("startDate", jsonObject, 0L);
            subscription.daysLeft = JsonUtils.getIntOr("daysLeft", jsonObject, 0);
            subscription.type = Subscription.typeFrom(JsonUtils.getStringOr("subscriptionType", jsonObject, SubscriptionType.NORMAL.name()));
        }
        catch (Exception jsonParser) {
            LOGGER.error("Could not parse Subscription: {}", (Object)jsonParser.getMessage());
        }
        return subscription;
    }

    private static SubscriptionType typeFrom(String subscriptionType) {
        try {
            return SubscriptionType.valueOf(subscriptionType);
        }
        catch (Exception exception) {
            return SubscriptionType.NORMAL;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum SubscriptionType {
        NORMAL,
        RECURRING;

    }
}

