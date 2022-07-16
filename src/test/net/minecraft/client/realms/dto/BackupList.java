/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.realms.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.Backup;
import net.minecraft.client.realms.dto.ValueObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class BackupList
extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    public List<Backup> backups;

    public static BackupList parse(String json) {
        JsonParser jsonParser = new JsonParser();
        BackupList backupList = new BackupList();
        backupList.backups = Lists.newArrayList();
        try {
            JsonElement jsonElement = jsonParser.parse(json).getAsJsonObject().get("backups");
            if (jsonElement.isJsonArray()) {
                Iterator<JsonElement> iterator = jsonElement.getAsJsonArray().iterator();
                while (iterator.hasNext()) {
                    backupList.backups.add(Backup.parse(iterator.next()));
                }
            }
        }
        catch (Exception jsonElement) {
            LOGGER.error("Could not parse BackupList: {}", (Object)jsonElement.getMessage());
        }
        return backupList;
    }
}

