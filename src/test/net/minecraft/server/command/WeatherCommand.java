/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

public class WeatherCommand {
    private static final int DEFAULT_DURATION = 6000;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("weather").requires(source -> source.hasPermissionLevel(2))).then(((LiteralArgumentBuilder)CommandManager.literal("clear").executes(context -> WeatherCommand.executeClear((ServerCommandSource)context.getSource(), 6000))).then(CommandManager.argument("duration", IntegerArgumentType.integer(0, 1000000)).executes(context -> WeatherCommand.executeClear((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger(context, "duration") * 20))))).then(((LiteralArgumentBuilder)CommandManager.literal("rain").executes(context -> WeatherCommand.executeRain((ServerCommandSource)context.getSource(), 6000))).then(CommandManager.argument("duration", IntegerArgumentType.integer(0, 1000000)).executes(context -> WeatherCommand.executeRain((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger(context, "duration") * 20))))).then(((LiteralArgumentBuilder)CommandManager.literal("thunder").executes(context -> WeatherCommand.executeThunder((ServerCommandSource)context.getSource(), 6000))).then(CommandManager.argument("duration", IntegerArgumentType.integer(0, 1000000)).executes(context -> WeatherCommand.executeThunder((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger(context, "duration") * 20)))));
    }

    private static int executeClear(ServerCommandSource source, int duration) {
        source.getWorld().setWeather(duration, 0, false, false);
        source.sendFeedback(new TranslatableText("commands.weather.set.clear"), true);
        return duration;
    }

    private static int executeRain(ServerCommandSource source, int duration) {
        source.getWorld().setWeather(0, duration, true, false);
        source.sendFeedback(new TranslatableText("commands.weather.set.rain"), true);
        return duration;
    }

    private static int executeThunder(ServerCommandSource source, int duration) {
        source.getWorld().setWeather(0, duration, true, true);
        source.sendFeedback(new TranslatableText("commands.weather.set.thunder"), true);
        return duration;
    }
}

