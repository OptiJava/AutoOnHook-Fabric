/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ParticleEffectArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

public class ParticleCommand {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.particle.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("particle").requires(source -> source.hasPermissionLevel(2))).then(((RequiredArgumentBuilder)CommandManager.argument("name", ParticleEffectArgumentType.particleEffect()).executes(context -> ParticleCommand.execute((ServerCommandSource)context.getSource(), ParticleEffectArgumentType.getParticle(context, "name"), ((ServerCommandSource)context.getSource()).getPosition(), Vec3d.ZERO, 0.0f, 0, false, ((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getPlayerList()))).then(((RequiredArgumentBuilder)CommandManager.argument("pos", Vec3ArgumentType.vec3()).executes(context -> ParticleCommand.execute((ServerCommandSource)context.getSource(), ParticleEffectArgumentType.getParticle(context, "name"), Vec3ArgumentType.getVec3(context, "pos"), Vec3d.ZERO, 0.0f, 0, false, ((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getPlayerList()))).then(CommandManager.argument("delta", Vec3ArgumentType.vec3(false)).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("speed", FloatArgumentType.floatArg(0.0f)).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("count", IntegerArgumentType.integer(0)).executes(context -> ParticleCommand.execute((ServerCommandSource)context.getSource(), ParticleEffectArgumentType.getParticle(context, "name"), Vec3ArgumentType.getVec3(context, "pos"), Vec3ArgumentType.getVec3(context, "delta"), FloatArgumentType.getFloat(context, "speed"), IntegerArgumentType.getInteger(context, "count"), false, ((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getPlayerList()))).then(((LiteralArgumentBuilder)CommandManager.literal("force").executes(context -> ParticleCommand.execute((ServerCommandSource)context.getSource(), ParticleEffectArgumentType.getParticle(context, "name"), Vec3ArgumentType.getVec3(context, "pos"), Vec3ArgumentType.getVec3(context, "delta"), FloatArgumentType.getFloat(context, "speed"), IntegerArgumentType.getInteger(context, "count"), true, ((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getPlayerList()))).then(CommandManager.argument("viewers", EntityArgumentType.players()).executes(context -> ParticleCommand.execute((ServerCommandSource)context.getSource(), ParticleEffectArgumentType.getParticle(context, "name"), Vec3ArgumentType.getVec3(context, "pos"), Vec3ArgumentType.getVec3(context, "delta"), FloatArgumentType.getFloat(context, "speed"), IntegerArgumentType.getInteger(context, "count"), true, EntityArgumentType.getPlayers(context, "viewers")))))).then(((LiteralArgumentBuilder)CommandManager.literal("normal").executes(context -> ParticleCommand.execute((ServerCommandSource)context.getSource(), ParticleEffectArgumentType.getParticle(context, "name"), Vec3ArgumentType.getVec3(context, "pos"), Vec3ArgumentType.getVec3(context, "delta"), FloatArgumentType.getFloat(context, "speed"), IntegerArgumentType.getInteger(context, "count"), false, ((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getPlayerList()))).then(CommandManager.argument("viewers", EntityArgumentType.players()).executes(context -> ParticleCommand.execute((ServerCommandSource)context.getSource(), ParticleEffectArgumentType.getParticle(context, "name"), Vec3ArgumentType.getVec3(context, "pos"), Vec3ArgumentType.getVec3(context, "delta"), FloatArgumentType.getFloat(context, "speed"), IntegerArgumentType.getInteger(context, "count"), false, EntityArgumentType.getPlayers(context, "viewers")))))))))));
    }

    private static int execute(ServerCommandSource source, ParticleEffect parameters, Vec3d pos, Vec3d delta, float speed, int count, boolean force, Collection<ServerPlayerEntity> viewers) throws CommandSyntaxException {
        int i = 0;
        for (ServerPlayerEntity serverPlayerEntity : viewers) {
            if (!source.getWorld().spawnParticles(serverPlayerEntity, parameters, force, pos.x, pos.y, pos.z, count, delta.x, delta.y, delta.z, speed)) continue;
            ++i;
        }
        if (i == 0) {
            throw FAILED_EXCEPTION.create();
        }
        source.sendFeedback(new TranslatableText("commands.particle.success", Registry.PARTICLE_TYPE.getId(parameters.getType()).toString()), true);
        return i;
    }
}

