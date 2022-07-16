/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.FunctionCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.tag.Tag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.world.timer.FunctionTagTimerCallback;
import net.minecraft.world.timer.FunctionTimerCallback;
import net.minecraft.world.timer.Timer;

public class ScheduleCommand {
    private static final SimpleCommandExceptionType SAME_TICK_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.schedule.same_tick"));
    private static final DynamicCommandExceptionType CLEARED_FAILURE_EXCEPTION = new DynamicCommandExceptionType(eventName -> new TranslatableText("commands.schedule.cleared.failure", eventName));
    private static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder) -> CommandSource.suggestMatching(((ServerCommandSource)context.getSource()).getServer().getSaveProperties().getMainWorldProperties().getScheduledEvents().method_22592(), builder);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("schedule").requires(source -> source.hasPermissionLevel(2))).then(CommandManager.literal("function").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("function", CommandFunctionArgumentType.commandFunction()).suggests(FunctionCommand.SUGGESTION_PROVIDER).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("time", TimeArgumentType.time()).executes(context -> ScheduleCommand.execute((ServerCommandSource)context.getSource(), CommandFunctionArgumentType.getFunctionOrTag(context, "function"), IntegerArgumentType.getInteger(context, "time"), true))).then(CommandManager.literal("append").executes(context -> ScheduleCommand.execute((ServerCommandSource)context.getSource(), CommandFunctionArgumentType.getFunctionOrTag(context, "function"), IntegerArgumentType.getInteger(context, "time"), false)))).then(CommandManager.literal("replace").executes(context -> ScheduleCommand.execute((ServerCommandSource)context.getSource(), CommandFunctionArgumentType.getFunctionOrTag(context, "function"), IntegerArgumentType.getInteger(context, "time"), true))))))).then(CommandManager.literal("clear").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("function", StringArgumentType.greedyString()).suggests(SUGGESTION_PROVIDER).executes(context -> ScheduleCommand.clearEvent((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "function"))))));
    }

    private static int execute(ServerCommandSource source, Pair<Identifier, Either<CommandFunction, Tag<CommandFunction>>> function2, int time, boolean replace) throws CommandSyntaxException {
        if (time == 0) {
            throw SAME_TICK_EXCEPTION.create();
        }
        long l = source.getWorld().getTime() + (long)time;
        Identifier identifier = function2.getFirst();
        Timer<MinecraftServer> timer = source.getServer().getSaveProperties().getMainWorldProperties().getScheduledEvents();
        function2.getSecond().ifLeft(function -> {
            String string = identifier.toString();
            if (replace) {
                timer.method_22593(string);
            }
            timer.setEvent(string, l, new FunctionTimerCallback(identifier));
            source.sendFeedback(new TranslatableText("commands.schedule.created.function", identifier, time, l), true);
        }).ifRight(tag -> {
            String string = "#" + identifier;
            if (replace) {
                timer.method_22593(string);
            }
            timer.setEvent(string, l, new FunctionTagTimerCallback(identifier));
            source.sendFeedback(new TranslatableText("commands.schedule.created.tag", identifier, time, l), true);
        });
        return Math.floorMod(l, Integer.MAX_VALUE);
    }

    private static int clearEvent(ServerCommandSource source, String eventName) throws CommandSyntaxException {
        int i = source.getServer().getSaveProperties().getMainWorldProperties().getScheduledEvents().method_22593(eventName);
        if (i == 0) {
            throw CLEARED_FAILURE_EXCEPTION.create(eventName);
        }
        source.sendFeedback(new TranslatableText("commands.schedule.cleared.success", i, eventName), true);
        return i;
    }
}

