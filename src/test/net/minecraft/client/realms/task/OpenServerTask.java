/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.realms.task;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.exception.RetryCallException;
import net.minecraft.client.realms.gui.screen.RealmsConfigureWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.realms.task.LongRunningTask;
import net.minecraft.text.TranslatableText;

@Environment(value=EnvType.CLIENT)
public class OpenServerTask
extends LongRunningTask {
    private final RealmsServer serverData;
    private final Screen returnScreen;
    private final boolean join;
    private final RealmsMainScreen mainScreen;
    private final MinecraftClient client;

    public OpenServerTask(RealmsServer realmsServer, Screen returnScreen, RealmsMainScreen mainScreen, boolean join, MinecraftClient client) {
        this.serverData = realmsServer;
        this.returnScreen = returnScreen;
        this.join = join;
        this.mainScreen = mainScreen;
        this.client = client;
    }

    @Override
    public void run() {
        this.setTitle(new TranslatableText("mco.configure.world.opening"));
        RealmsClient realmsClient = RealmsClient.createRealmsClient();
        for (int i = 0; i < 25; ++i) {
            if (this.aborted()) {
                return;
            }
            try {
                boolean bl = realmsClient.open(this.serverData.id);
                if (!bl) continue;
                this.client.execute(() -> {
                    if (this.returnScreen instanceof RealmsConfigureWorldScreen) {
                        ((RealmsConfigureWorldScreen)this.returnScreen).stateChanged();
                    }
                    this.serverData.state = RealmsServer.State.OPEN;
                    if (this.join) {
                        this.mainScreen.play(this.serverData, this.returnScreen);
                    } else {
                        this.client.setScreen(this.returnScreen);
                    }
                });
                break;
            }
            catch (RetryCallException bl) {
                if (this.aborted()) {
                    return;
                }
                OpenServerTask.pause(bl.delaySeconds);
                continue;
            }
            catch (Exception bl) {
                if (this.aborted()) {
                    return;
                }
                LOGGER.error("Failed to open server", (Throwable)bl);
                this.error("Failed to open the server");
            }
        }
    }
}

