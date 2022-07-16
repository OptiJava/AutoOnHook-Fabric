/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.RotationArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TeleportCommand {
    private static final SimpleCommandExceptionType INVALID_POSITION_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.teleport.invalidPosition"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("teleport").requires(source -> source.hasPermissionLevel(2))).then(CommandManager.argument("location", Vec3ArgumentType.vec3()).executes(context -> TeleportCommand.execute((ServerCommandSource)context.getSource(), Collections.singleton(((ServerCommandSource)context.getSource()).getEntityOrThrow()), ((ServerCommandSource)context.getSource()).getWorld(), Vec3ArgumentType.getPosArgument(context, "location"), DefaultPosArgument.zero(), null)))).then(CommandManager.argument("destination", EntityArgumentType.entity()).executes(context -> TeleportCommand.execute((ServerCommandSource)context.getSource(), Collections.singleton(((ServerCommandSource)context.getSource()).getEntityOrThrow()), EntityArgumentType.getEntity(context, "destination"))))).then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.entities()).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("location", Vec3ArgumentType.vec3()).executes(context -> TeleportCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), ((ServerCommandSource)context.getSource()).getWorld(), Vec3ArgumentType.getPosArgument(context, "location"), null, null))).then(CommandManager.argument("rotation", RotationArgumentType.rotation()).executes(context -> TeleportCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), ((ServerCommandSource)context.getSource()).getWorld(), Vec3ArgumentType.getPosArgument(context, "location"), RotationArgumentType.getRotation(context, "rotation"), null)))).then(((LiteralArgumentBuilder)CommandManager.literal("facing").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("entity").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("facingEntity", EntityArgumentType.entity()).executes(context -> TeleportCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), ((ServerCommandSource)context.getSource()).getWorld(), Vec3ArgumentType.getPosArgument(context, "location"), null, new LookTarget(EntityArgumentType.getEntity(context, "facingEntity"), EntityAnchorArgumentType.EntityAnchor.FEET)))).then(CommandManager.argument("facingAnchor", EntityAnchorArgumentType.entityAnchor()).executes(context -> TeleportCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), ((ServerCommandSource)context.getSource()).getWorld(), Vec3ArgumentType.getPosArgument(context, "location"), null, new LookTarget(EntityArgumentType.getEntity(context, "facingEntity"), EntityAnchorArgumentType.getEntityAnchor(context, "facingAnchor")))))))).then(CommandManager.argument("facingLocation", Vec3ArgumentType.vec3()).executes(context -> TeleportCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), ((ServerCommandSource)context.getSource()).getWorld(), Vec3ArgumentType.getPosArgument(context, "location"), null, new LookTarget(Vec3ArgumentType.getVec3(context, "facingLocation")))))))).then(CommandManager.argument("destination", EntityArgumentType.entity()).executes(context -> TeleportCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), EntityArgumentType.getEntity(context, "destination"))))));
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("tp").requires(source -> source.hasPermissionLevel(2))).redirect(literalCommandNode));
    }

    private static int execute(ServerCommandSource source, Collection<? extends Entity> targets, Entity destination) throws CommandSyntaxException {
        for (Entity entity : targets) {
            TeleportCommand.teleport(source, entity, (ServerWorld)destination.world, destination.getX(), destination.getY(), destination.getZ(), EnumSet.noneOf(PlayerPositionLookS2CPacket.Flag.class), destination.getYaw(), destination.getPitch(), null);
        }
        if (targets.size() == 1) {
            source.sendFeedback(new TranslatableText("commands.teleport.success.entity.single", targets.iterator().next().getDisplayName(), destination.getDisplayName()), true);
        } else {
            source.sendFeedback(new TranslatableText("commands.teleport.success.entity.multiple", targets.size(), destination.getDisplayName()), true);
        }
        return targets.size();
    }

    private static int execute(ServerCommandSource source, Collection<? extends Entity> targets, ServerWorld world, PosArgument location, @Nullable PosArgument rotation, @Nullable LookTarget facingLocation) throws CommandSyntaxException {
        Vec3d vec3d = location.toAbsolutePos(source);
        Vec2f vec2f = rotation == null ? null : rotation.toAbsoluteRotation(source);
        EnumSet<PlayerPositionLookS2CPacket.Flag> set = EnumSet.noneOf(PlayerPositionLookS2CPacket.Flag.class);
        if (location.isXRelative()) {
            set.add(PlayerPositionLookS2CPacket.Flag.X);
        }
        if (location.isYRelative()) {
            set.add(PlayerPositionLookS2CPacket.Flag.Y);
        }
        if (location.isZRelative()) {
            set.add(PlayerPositionLookS2CPacket.Flag.Z);
        }
        if (rotation == null) {
            set.add(PlayerPositionLookS2CPacket.Flag.X_ROT);
            set.add(PlayerPositionLookS2CPacket.Flag.Y_ROT);
        } else {
            if (rotation.isXRelative()) {
                set.add(PlayerPositionLookS2CPacket.Flag.X_ROT);
            }
            if (rotation.isYRelative()) {
                set.add(PlayerPositionLookS2CPacket.Flag.Y_ROT);
            }
        }
        for (Entity entity : targets) {
            if (rotation == null) {
                TeleportCommand.teleport(source, entity, world, vec3d.x, vec3d.y, vec3d.z, set, entity.getYaw(), entity.getPitch(), facingLocation);
                continue;
            }
            TeleportCommand.teleport(source, entity, world, vec3d.x, vec3d.y, vec3d.z, set, vec2f.y, vec2f.x, facingLocation);
        }
        if (targets.size() == 1) {
            source.sendFeedback(new TranslatableText("commands.teleport.success.location.single", targets.iterator().next().getDisplayName(), TeleportCommand.formatFloat(vec3d.x), TeleportCommand.formatFloat(vec3d.y), TeleportCommand.formatFloat(vec3d.z)), true);
        } else {
            source.sendFeedback(new TranslatableText("commands.teleport.success.location.multiple", targets.size(), TeleportCommand.formatFloat(vec3d.x), TeleportCommand.formatFloat(vec3d.y), TeleportCommand.formatFloat(vec3d.z)), true);
        }
        return targets.size();
    }

    private static String formatFloat(double d) {
        return String.format(Locale.ROOT, "%f", d);
    }

    private static void teleport(ServerCommandSource source, Entity target, ServerWorld world, double x, double y, double z, Set<PlayerPositionLookS2CPacket.Flag> movementFlags, float yaw, float pitch, @Nullable LookTarget facingLocation) throws CommandSyntaxException {
        BlockPos blockPos = new BlockPos(x, y, z);
        if (!World.isValid(blockPos)) {
            throw INVALID_POSITION_EXCEPTION.create();
        }
        float f = MathHelper.wrapDegrees(yaw);
        float g = MathHelper.wrapDegrees(pitch);
        if (target instanceof ServerPlayerEntity) {
            ChunkPos chunkPos = new ChunkPos(new BlockPos(x, y, z));
            world.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, target.getId());
            target.stopRiding();
            if (((ServerPlayerEntity)target).isSleeping()) {
                ((ServerPlayerEntity)target).wakeUp(true, true);
            }
            if (world == target.world) {
                ((ServerPlayerEntity)target).networkHandler.requestTeleport(x, y, z, f, g, movementFlags);
            } else {
                ((ServerPlayerEntity)target).teleport(world, x, y, z, f, g);
            }
            target.setHeadYaw(f);
        } else {
            float chunkPos = MathHelper.clamp(g, -90.0f, 90.0f);
            if (world == target.world) {
                target.refreshPositionAndAngles(x, y, z, f, chunkPos);
                target.setHeadYaw(f);
            } else {
                target.detach();
                Entity entity = target;
                target = entity.getType().create(world);
                if (target != null) {
                    target.copyFrom(entity);
                    target.refreshPositionAndAngles(x, y, z, f, chunkPos);
                    target.setHeadYaw(f);
                    entity.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
                    world.onDimensionChanged(target);
                } else {
                    return;
                }
            }
        }
        if (facingLocation != null) {
            facingLocation.look(source, target);
        }
        if (!(target instanceof LivingEntity) || !((LivingEntity)target).isFallFlying()) {
            target.setVelocity(target.getVelocity().multiply(1.0, 0.0, 1.0));
            target.setOnGround(true);
        }
        if (target instanceof PathAwareEntity) {
            ((PathAwareEntity)target).getNavigation().stop();
        }
    }

    static class LookTarget {
        private final Vec3d targetPos;
        private final Entity target;
        private final EntityAnchorArgumentType.EntityAnchor targetAnchor;

        public LookTarget(Entity target, EntityAnchorArgumentType.EntityAnchor targetAnchor) {
            this.target = target;
            this.targetAnchor = targetAnchor;
            this.targetPos = targetAnchor.positionAt(target);
        }

        public LookTarget(Vec3d targetPos) {
            this.target = null;
            this.targetPos = targetPos;
            this.targetAnchor = null;
        }

        public void look(ServerCommandSource source, Entity entity) {
            if (this.target != null) {
                if (entity instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity)entity).lookAtEntity(source.getEntityAnchor(), this.target, this.targetAnchor);
                } else {
                    entity.lookAt(source.getEntityAnchor(), this.targetPos);
                }
            } else {
                entity.lookAt(source.getEntityAnchor(), this.targetPos);
            }
        }
    }
}

