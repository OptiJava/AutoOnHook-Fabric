/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.util;

import com.mojang.text2speech.Narrator;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ClientChatListener;
import net.minecraft.client.option.NarratorMode;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class NarratorManager
implements ClientChatListener {
    public static final Text EMPTY = LiteralText.EMPTY;
    private static final Logger LOGGER = LogManager.getLogger();
    public static final NarratorManager INSTANCE = new NarratorManager();
    private final Narrator narrator = Narrator.getNarrator();

    @Override
    public void onChatMessage(MessageType messageType, Text message, UUID sender) {
        NarratorMode narratorMode = NarratorManager.getNarratorOption();
        if (narratorMode == NarratorMode.OFF) {
            return;
        }
        if (!this.narrator.active()) {
            this.debugPrintMessage(message.getString());
            return;
        }
        if (narratorMode == NarratorMode.ALL || narratorMode == NarratorMode.CHAT && messageType == MessageType.CHAT || narratorMode == NarratorMode.SYSTEM && messageType == MessageType.SYSTEM) {
            Text text = message instanceof TranslatableText && "chat.type.text".equals(((TranslatableText)message).getKey()) ? new TranslatableText("chat.type.text.narrate", ((TranslatableText)message).getArgs()) : message;
            String string = text.getString();
            this.debugPrintMessage(string);
            this.narrator.say(string, messageType.interruptsNarration());
        }
    }

    public void narrate(Text text) {
        this.narrate(text.getString());
    }

    public void narrate(String text) {
        NarratorMode narratorMode = NarratorManager.getNarratorOption();
        if (narratorMode != NarratorMode.OFF && narratorMode != NarratorMode.CHAT && !text.isEmpty()) {
            this.debugPrintMessage(text);
            if (this.narrator.active()) {
                this.narrator.clear();
                this.narrator.say(text, true);
            }
        }
    }

    private static NarratorMode getNarratorOption() {
        return MinecraftClient.getInstance().options.narrator;
    }

    private void debugPrintMessage(String message) {
        if (SharedConstants.isDevelopment) {
            LOGGER.debug("Narrating: {}", (Object)message.replaceAll("\n", "\\\\n"));
        }
    }

    public void addToast(NarratorMode option) {
        this.clear();
        this.narrator.say(new TranslatableText("options.narrator").append(" : ").append(option.getName()).getString(), true);
        ToastManager toastManager = MinecraftClient.getInstance().getToastManager();
        if (this.narrator.active()) {
            if (option == NarratorMode.OFF) {
                SystemToast.show(toastManager, SystemToast.Type.NARRATOR_TOGGLE, new TranslatableText("narrator.toast.disabled"), null);
            } else {
                SystemToast.show(toastManager, SystemToast.Type.NARRATOR_TOGGLE, new TranslatableText("narrator.toast.enabled"), option.getName());
            }
        } else {
            SystemToast.show(toastManager, SystemToast.Type.NARRATOR_TOGGLE, new TranslatableText("narrator.toast.disabled"), new TranslatableText("options.narrator.notavailable"));
        }
    }

    public boolean isActive() {
        return this.narrator.active();
    }

    public void clear() {
        if (NarratorManager.getNarratorOption() == NarratorMode.OFF || !this.narrator.active()) {
            return;
        }
        this.narrator.clear();
    }

    public void destroy() {
        this.narrator.destroy();
    }
}

