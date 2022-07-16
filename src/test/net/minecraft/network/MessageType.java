/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.network;

public enum MessageType {
    CHAT(0, false),
    SYSTEM(1, true),
    GAME_INFO(2, true);

    private final byte id;
    private final boolean interruptsNarration;

    private MessageType(byte id, boolean interruptsNarration) {
        this.id = id;
        this.interruptsNarration = interruptsNarration;
    }

    public byte getId() {
        return this.id;
    }

    public static MessageType byId(byte id) {
        for (MessageType messageType : MessageType.values()) {
            if (id != messageType.id) continue;
            return messageType;
        }
        return CHAT;
    }

    public boolean interruptsNarration() {
        return this.interruptsNarration;
    }
}

