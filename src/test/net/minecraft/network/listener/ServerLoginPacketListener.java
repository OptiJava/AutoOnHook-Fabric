/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.network.listener;

import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;

public interface ServerLoginPacketListener
extends PacketListener {
    public void onHello(LoginHelloC2SPacket var1);

    public void onKey(LoginKeyC2SPacket var1);

    public void onQueryResponse(LoginQueryResponseC2SPacket var1);
}

