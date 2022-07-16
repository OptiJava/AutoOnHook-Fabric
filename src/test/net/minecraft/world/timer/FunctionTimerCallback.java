/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.timer;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.timer.Timer;
import net.minecraft.world.timer.TimerCallback;

public class FunctionTimerCallback
implements TimerCallback<MinecraftServer> {
    final Identifier name;

    public FunctionTimerCallback(Identifier name) {
        this.name = name;
    }

    @Override
    public void call(MinecraftServer minecraftServer, Timer<MinecraftServer> timer, long l) {
        CommandFunctionManager commandFunctionManager = minecraftServer.getCommandFunctionManager();
        commandFunctionManager.getFunction(this.name).ifPresent(commandFunction -> commandFunctionManager.execute((CommandFunction)commandFunction, commandFunctionManager.getScheduledCommandSource()));
    }

    public static class Serializer
    extends TimerCallback.Serializer<MinecraftServer, FunctionTimerCallback> {
        public Serializer() {
            super(new Identifier("function"), FunctionTimerCallback.class);
        }

        @Override
        public void serialize(NbtCompound nbtCompound, FunctionTimerCallback functionTimerCallback) {
            nbtCompound.putString("Name", functionTimerCallback.name.toString());
        }

        @Override
        public FunctionTimerCallback deserialize(NbtCompound nbtCompound) {
            Identifier identifier = new Identifier(nbtCompound.getString("Name"));
            return new FunctionTimerCallback(identifier);
        }

        @Override
        public /* synthetic */ TimerCallback deserialize(NbtCompound nbt) {
            return this.deserialize(nbt);
        }
    }
}

