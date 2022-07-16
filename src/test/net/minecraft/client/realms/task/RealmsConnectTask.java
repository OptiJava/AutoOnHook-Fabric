/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.realms.task;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.realms.RealmsConnection;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsServerAddress;
import net.minecraft.client.realms.task.LongRunningTask;
import net.minecraft.text.TranslatableText;

@Environment(value=EnvType.CLIENT)
public class RealmsConnectTask
extends LongRunningTask {
    private final RealmsConnection realmsConnect;
    private final RealmsServer server;
    private final RealmsServerAddress address;

    public RealmsConnectTask(Screen lastScreen, RealmsServer server, RealmsServerAddress address) {
        this.server = server;
        this.address = address;
        this.realmsConnect = new RealmsConnection(lastScreen);
    }

    @Override
    public void run() {
        this.setTitle(new TranslatableText("mco.connect.connecting"));
        this.realmsConnect.connect(this.server, ServerAddress.parse(this.address.address));
    }

    @Override
    public void abortTask() {
        this.realmsConnect.abort();
        MinecraftClient.getInstance().getResourcePackProvider().clear();
    }

    @Override
    public void tick() {
        this.realmsConnect.tick();
    }
}

