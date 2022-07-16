/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class CloneCommand {
    private static final int MAX_BLOCKS = 32768;
    private static final SimpleCommandExceptionType OVERLAP_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.clone.overlap"));
    private static final Dynamic2CommandExceptionType TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType((maxCount, count) -> new TranslatableText("commands.clone.toobig", maxCount, count));
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.clone.failed"));
    public static final Predicate<CachedBlockPosition> IS_AIR_PREDICATE = pos -> !pos.getBlockState().isAir();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("clone").requires(source -> source.hasPermissionLevel(2))).then(CommandManager.argument("begin", BlockPosArgumentType.blockPos()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("end", BlockPosArgumentType.blockPos()).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("destination", BlockPosArgumentType.blockPos()).executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "begin"), BlockPosArgumentType.getLoadedBlockPos(context, "end"), BlockPosArgumentType.getLoadedBlockPos(context, "destination"), pos -> true, Mode.NORMAL))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("replace").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "begin"), BlockPosArgumentType.getLoadedBlockPos(context, "end"), BlockPosArgumentType.getLoadedBlockPos(context, "destination"), pos -> true, Mode.NORMAL))).then(CommandManager.literal("force").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "begin"), BlockPosArgumentType.getLoadedBlockPos(context, "end"), BlockPosArgumentType.getLoadedBlockPos(context, "destination"), pos -> true, Mode.FORCE)))).then(CommandManager.literal("move").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "begin"), BlockPosArgumentType.getLoadedBlockPos(context, "end"), BlockPosArgumentType.getLoadedBlockPos(context, "destination"), pos -> true, Mode.MOVE)))).then(CommandManager.literal("normal").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "begin"), BlockPosArgumentType.getLoadedBlockPos(context, "end"), BlockPosArgumentType.getLoadedBlockPos(context, "destination"), pos -> true, Mode.NORMAL))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("masked").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "begin"), BlockPosArgumentType.getLoadedBlockPos(context, "end"), BlockPosArgumentType.getLoadedBlockPos(context, "destination"), IS_AIR_PREDICATE, Mode.NORMAL))).then(CommandManager.literal("force").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "begin"), BlockPosArgumentType.getLoadedBlockPos(context, "end"), BlockPosArgumentType.getLoadedBlockPos(context, "destination"), IS_AIR_PREDICATE, Mode.FORCE)))).then(CommandManager.literal("move").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "begin"), BlockPosArgumentType.getLoadedBlockPos(context, "end"), BlockPosArgumentType.getLoadedBlockPos(context, "destination"), IS_AIR_PREDICATE, Mode.MOVE)))).then(CommandManager.literal("normal").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "begin"), BlockPosArgumentType.getLoadedBlockPos(context, "end"), BlockPosArgumentType.getLoadedBlockPos(context, "destination"), IS_AIR_PREDICATE, Mode.NORMAL))))).then(CommandManager.literal("filtered").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("filter", BlockPredicateArgumentType.blockPredicate()).executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "begin"), BlockPosArgumentType.getLoadedBlockPos(context, "end"), BlockPosArgumentType.getLoadedBlockPos(context, "destination"), BlockPredicateArgumentType.getBlockPredicate(context, "filter"), Mode.NORMAL))).then(CommandManager.literal("force").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "begin"), BlockPosArgumentType.getLoadedBlockPos(context, "end"), BlockPosArgumentType.getLoadedBlockPos(context, "destination"), BlockPredicateArgumentType.getBlockPredicate(context, "filter"), Mode.FORCE)))).then(CommandManager.literal("move").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "begin"), BlockPosArgumentType.getLoadedBlockPos(context, "end"), BlockPosArgumentType.getLoadedBlockPos(context, "destination"), BlockPredicateArgumentType.getBlockPredicate(context, "filter"), Mode.MOVE)))).then(CommandManager.literal("normal").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "begin"), BlockPosArgumentType.getLoadedBlockPos(context, "end"), BlockPosArgumentType.getLoadedBlockPos(context, "destination"), BlockPredicateArgumentType.getBlockPredicate(context, "filter"), Mode.NORMAL)))))))));
    }

    private static int execute(ServerCommandSource source, BlockPos begin, BlockPos end, BlockPos destination, Predicate<CachedBlockPosition> filter, Mode mode) throws CommandSyntaxException {
        Object cachedBlockPosition;
        BlockBox blockBox = BlockBox.create(begin, end);
        BlockPos blockPos = destination.add(blockBox.getDimensions());
        BlockBox blockBox2 = BlockBox.create(destination, blockPos);
        if (!mode.allowsOverlap() && blockBox2.intersects(blockBox)) {
            throw OVERLAP_EXCEPTION.create();
        }
        int i = blockBox.getBlockCountX() * blockBox.getBlockCountY() * blockBox.getBlockCountZ();
        if (i > 32768) {
            throw TOO_BIG_EXCEPTION.create(32768, i);
        }
        ServerWorld serverWorld = source.getWorld();
        if (!serverWorld.isRegionLoaded(begin, end) || !serverWorld.isRegionLoaded(destination, blockPos)) {
            throw BlockPosArgumentType.UNLOADED_EXCEPTION.create();
        }
        ArrayList<BlockInfo> list = Lists.newArrayList();
        ArrayList<BlockInfo> list2 = Lists.newArrayList();
        ArrayList<BlockInfo> list3 = Lists.newArrayList();
        LinkedList<Object> deque = Lists.newLinkedList();
        BlockPos blockPos2 = new BlockPos(blockBox2.getMinX() - blockBox.getMinX(), blockBox2.getMinY() - blockBox.getMinY(), blockBox2.getMinZ() - blockBox.getMinZ());
        for (int j = blockBox.getMinZ(); j <= blockBox.getMaxZ(); ++j) {
            for (int k = blockBox.getMinY(); k <= blockBox.getMaxY(); ++k) {
                for (int l = blockBox.getMinX(); l <= blockBox.getMaxX(); ++l) {
                    Object blockPos3 = new BlockPos(l, k, j);
                    BlockPos blockPos4 = ((BlockPos)blockPos3).add(blockPos2);
                    cachedBlockPosition = new CachedBlockPosition(serverWorld, (BlockPos)blockPos3, false);
                    BlockState blockState = ((CachedBlockPosition)cachedBlockPosition).getBlockState();
                    if (!filter.test((CachedBlockPosition)cachedBlockPosition)) continue;
                    BlockEntity blockEntity = serverWorld.getBlockEntity((BlockPos)blockPos3);
                    if (blockEntity != null) {
                        NbtCompound nbtCompound = blockEntity.writeNbt(new NbtCompound());
                        list2.add(new BlockInfo(blockPos4, blockState, nbtCompound));
                        deque.addLast(blockPos3);
                        continue;
                    }
                    if (blockState.isOpaqueFullCube(serverWorld, (BlockPos)blockPos3) || blockState.isFullCube(serverWorld, (BlockPos)blockPos3)) {
                        list.add(new BlockInfo(blockPos4, blockState, null));
                        deque.addLast(blockPos3);
                        continue;
                    }
                    list3.add(new BlockInfo(blockPos4, blockState, null));
                    deque.addFirst(blockPos3);
                }
            }
        }
        if (mode == Mode.MOVE) {
            for (BlockPos blockPos5 : deque) {
                BlockEntity l = serverWorld.getBlockEntity(blockPos5);
                Clearable.clear(l);
                serverWorld.setBlockState(blockPos5, Blocks.BARRIER.getDefaultState(), Block.NOTIFY_LISTENERS);
            }
            for (BlockPos blockPos6 : deque) {
                serverWorld.setBlockState(blockPos6, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            }
        }
        ArrayList<BlockInfo> j = Lists.newArrayList();
        j.addAll(list);
        j.addAll(list2);
        j.addAll(list3);
        List<Object> list4 = Lists.reverse(j);
        for (Object blockPos3 : list4) {
            BlockEntity blockEntity = serverWorld.getBlockEntity(((BlockInfo)blockPos3).pos);
            Clearable.clear(blockEntity);
            serverWorld.setBlockState(((BlockInfo)blockPos3).pos, Blocks.BARRIER.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
        int l = 0;
        for (BlockInfo blockInfo : j) {
            if (!serverWorld.setBlockState(blockInfo.pos, blockInfo.state, Block.NOTIFY_LISTENERS)) continue;
            ++l;
        }
        for (BlockInfo blockInfo : list2) {
            cachedBlockPosition = serverWorld.getBlockEntity(blockInfo.pos);
            if (blockInfo.blockEntityTag != null && cachedBlockPosition != null) {
                blockInfo.blockEntityTag.putInt("x", blockInfo.pos.getX());
                blockInfo.blockEntityTag.putInt("y", blockInfo.pos.getY());
                blockInfo.blockEntityTag.putInt("z", blockInfo.pos.getZ());
                ((BlockEntity)cachedBlockPosition).readNbt(blockInfo.blockEntityTag);
                ((BlockEntity)cachedBlockPosition).markDirty();
            }
            serverWorld.setBlockState(blockInfo.pos, blockInfo.state, Block.NOTIFY_LISTENERS);
        }
        for (BlockInfo blockInfo : list4) {
            serverWorld.updateNeighbors(blockInfo.pos, blockInfo.state.getBlock());
        }
        ((ServerTickScheduler)serverWorld.getBlockTickScheduler()).copyScheduledTicks(blockBox, blockPos2);
        if (l == 0) {
            throw FAILED_EXCEPTION.create();
        }
        source.sendFeedback(new TranslatableText("commands.clone.success", l), true);
        return l;
    }

    static enum Mode {
        FORCE(true),
        MOVE(true),
        NORMAL(false);

        private final boolean allowsOverlap;

        private Mode(boolean allowsOverlap) {
            this.allowsOverlap = allowsOverlap;
        }

        public boolean allowsOverlap() {
            return this.allowsOverlap;
        }
    }

    static class BlockInfo {
        public final BlockPos pos;
        public final BlockState state;
        @Nullable
        public final NbtCompound blockEntityTag;

        public BlockInfo(BlockPos pos, BlockState state, @Nullable NbtCompound blockEntityTag) {
            this.pos = pos;
            this.state = state;
            this.blockEntityTag = blockEntityTag;
        }
    }
}

