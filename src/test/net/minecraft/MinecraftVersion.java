/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.bridge.game.GameVersion;
import com.mojang.bridge.game.PackType;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.util.JsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MinecraftVersion
implements GameVersion {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final GameVersion GAME_VERSION = new MinecraftVersion();
    private final String id;
    private final String name;
    private final boolean stable;
    private final int worldVersion;
    private final int protocolVersion;
    private final int resourcePackVersion;
    private final int dataPackVersion;
    private final Date buildTime;
    private final String releaseTarget;

    private MinecraftVersion() {
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
        this.name = "1.17.1";
        this.stable = true;
        this.worldVersion = 2730;
        this.protocolVersion = SharedConstants.getProtocolVersion();
        this.resourcePackVersion = 7;
        this.dataPackVersion = 7;
        this.buildTime = new Date();
        this.releaseTarget = "1.17.1";
    }

    private MinecraftVersion(JsonObject json) {
        this.id = JsonHelper.getString(json, "id");
        this.name = JsonHelper.getString(json, "name");
        this.releaseTarget = JsonHelper.getString(json, "release_target");
        this.stable = JsonHelper.getBoolean(json, "stable");
        this.worldVersion = JsonHelper.getInt(json, "world_version");
        this.protocolVersion = JsonHelper.getInt(json, "protocol_version");
        JsonObject jsonObject = JsonHelper.getObject(json, "pack_version");
        this.resourcePackVersion = JsonHelper.getInt(jsonObject, "resource");
        this.dataPackVersion = JsonHelper.getInt(jsonObject, "data");
        this.buildTime = Date.from(ZonedDateTime.parse(JsonHelper.getString(json, "build_time")).toInstant());
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static GameVersion create() {
        try (InputStream inputStream = MinecraftVersion.class.getResourceAsStream("/version.json");){
            MinecraftVersion minecraftVersion;
            if (inputStream == null) {
                LOGGER.warn("Missing version information!");
                GameVersion gameVersion = GAME_VERSION;
                return gameVersion;
            }
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);){
                minecraftVersion = new MinecraftVersion(JsonHelper.deserialize(inputStreamReader));
            }
            return minecraftVersion;
        }
        catch (JsonParseException | IOException inputStream2) {
            throw new IllegalStateException("Game version information is corrupt", inputStream2);
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getReleaseTarget() {
        return this.releaseTarget;
    }

    @Override
    public int getWorldVersion() {
        return this.worldVersion;
    }

    @Override
    public int getProtocolVersion() {
        return this.protocolVersion;
    }

    @Override
    public int getPackVersion(PackType packType) {
        return packType == PackType.DATA ? this.dataPackVersion : this.resourcePackVersion;
    }

    @Override
    public Date getBuildTime() {
        return this.buildTime;
    }

    @Override
    public boolean isStable() {
        return this.stable;
    }
}

