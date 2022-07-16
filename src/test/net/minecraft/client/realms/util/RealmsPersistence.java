/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.realms.util;

import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.realms.CheckedGson;
import net.minecraft.client.realms.RealmsSerializable;
import org.apache.commons.io.FileUtils;

@Environment(value=EnvType.CLIENT)
public class RealmsPersistence {
    private static final String FILE_NAME = "realms_persistence.json";
    private static final CheckedGson CHECKED_GSON = new CheckedGson();

    public RealmsPersistenceData load() {
        return RealmsPersistence.readFile();
    }

    public void save(RealmsPersistenceData data) {
        RealmsPersistence.writeFile(data);
    }

    public static RealmsPersistenceData readFile() {
        File file = RealmsPersistence.getFile();
        try {
            String string = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            RealmsPersistenceData realmsPersistenceData = CHECKED_GSON.fromJson(string, RealmsPersistenceData.class);
            return realmsPersistenceData != null ? realmsPersistenceData : new RealmsPersistenceData();
        }
        catch (IOException string) {
            return new RealmsPersistenceData();
        }
    }

    public static void writeFile(RealmsPersistenceData data) {
        File file = RealmsPersistence.getFile();
        try {
            FileUtils.writeStringToFile(file, CHECKED_GSON.toJson(data), StandardCharsets.UTF_8);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private static File getFile() {
        return new File(MinecraftClient.getInstance().runDirectory, FILE_NAME);
    }

    @Environment(value=EnvType.CLIENT)
    public static class RealmsPersistenceData
    implements RealmsSerializable {
        @SerializedName(value="newsLink")
        public String newsLink;
        @SerializedName(value="hasUnreadNews")
        public boolean hasUnreadNews;
    }
}

