/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.server;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Objects;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.ServerConfigEntry;
import net.minecraft.server.ServerConfigList;

public class OperatorList
extends ServerConfigList<GameProfile, OperatorEntry> {
    public OperatorList(File file) {
        super(file);
    }

    @Override
    protected ServerConfigEntry<GameProfile> fromJson(JsonObject json) {
        return new OperatorEntry(json);
    }

    @Override
    public String[] getNames() {
        return (String[])this.values().stream().map(ServerConfigEntry::getKey).filter(Objects::nonNull).map(GameProfile::getName).toArray(String[]::new);
    }

    public boolean canBypassPlayerLimit(GameProfile profile) {
        OperatorEntry operatorEntry = (OperatorEntry)this.get(profile);
        if (operatorEntry != null) {
            return operatorEntry.canBypassPlayerLimit();
        }
        return false;
    }

    @Override
    protected String toString(GameProfile gameProfile) {
        return gameProfile.getId().toString();
    }

    @Override
    protected /* synthetic */ String toString(Object profile) {
        return this.toString((GameProfile)profile);
    }
}

