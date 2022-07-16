/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.argument.AngleArgumentType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;

public class SetWorldSpawnCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("setworldspawn").requires(source -> source.hasPermissionLevel(2))).executes(context -> SetWorldSpawnCommand.execute((ServerCommandSource)context.getSource(), new BlockPos(((ServerCommandSource)context.getSource()).getPosition()), 0.0f))).then(((RequiredArgumentBuilder)CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes(context -> SetWorldSpawnCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getBlockPos(context, "pos"), 0.0f))).then(CommandManager.argument("angle", AngleArgumentType.angle()).executes(context -> SetWorldSpawnCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getBlockPos(context, "pos"), AngleArgumentType.getAngle(context, "angle"))))));
    }

    private static int execute(ServerCommandSource source, BlockPos pos, float angle) {
        source.getWorld().setSpawnPos(pos, angle);
        source.sendFeedback(new TranslatableText("commands.setworldspawn.success", pos.getX(), pos.getY(), pos.getZ(), Float.valueOf(angle)), true);
        return 1;
    }
}

