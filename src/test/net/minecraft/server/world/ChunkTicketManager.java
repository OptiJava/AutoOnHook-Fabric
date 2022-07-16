/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.server.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTaskPrioritySystem;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.collection.SortedArraySet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.thread.MessageListener;
import net.minecraft.world.ChunkPosDistanceLevelPropagator;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public abstract class ChunkTicketManager {
    static final Logger LOGGER = LogManager.getLogger();
    private static final int field_29764 = 2;
    static final int NEARBY_PLAYER_TICKET_LEVEL = 33 + ChunkStatus.getDistanceFromFull(ChunkStatus.FULL) - 2;
    private static final int field_29765 = 4;
    final Long2ObjectMap<ObjectSet<ServerPlayerEntity>> playersByChunkPos = new Long2ObjectOpenHashMap<ObjectSet<ServerPlayerEntity>>();
    final Long2ObjectOpenHashMap<SortedArraySet<ChunkTicket<?>>> ticketsByPosition = new Long2ObjectOpenHashMap();
    private final TicketDistanceLevelPropagator distanceFromTicketTracker = new TicketDistanceLevelPropagator();
    private final DistanceFromNearestPlayerTracker distanceFromNearestPlayerTracker = new DistanceFromNearestPlayerTracker(8);
    private final NearbyChunkTicketUpdater nearbyChunkTicketUpdater = new NearbyChunkTicketUpdater(33);
    final Set<ChunkHolder> chunkHolders = Sets.newHashSet();
    final ChunkTaskPrioritySystem levelUpdateListener;
    final MessageListener<ChunkTaskPrioritySystem.Task<Runnable>> playerTicketThrottler;
    final MessageListener<ChunkTaskPrioritySystem.UnblockingMessage> playerTicketThrottlerUnblocker;
    final LongSet chunkPositions = new LongOpenHashSet();
    final Executor mainThreadExecutor;
    private long age;

    protected ChunkTicketManager(Executor workerExecutor, Executor mainThreadExecutor) {
        ChunkTaskPrioritySystem chunkTaskPrioritySystem;
        MessageListener<Runnable> messageListener = MessageListener.create("player ticket throttler", mainThreadExecutor::execute);
        this.levelUpdateListener = chunkTaskPrioritySystem = new ChunkTaskPrioritySystem(ImmutableList.of(messageListener), workerExecutor, 4);
        this.playerTicketThrottler = chunkTaskPrioritySystem.createExecutor(messageListener, true);
        this.playerTicketThrottlerUnblocker = chunkTaskPrioritySystem.createUnblockingExecutor(messageListener);
        this.mainThreadExecutor = mainThreadExecutor;
    }

    protected void purge() {
        ++this.age;
        ObjectIterator objectIterator = this.ticketsByPosition.long2ObjectEntrySet().fastIterator();
        while (objectIterator.hasNext()) {
            Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)objectIterator.next();
            if (((SortedArraySet)entry.getValue()).removeIf(chunkTicket -> chunkTicket.isExpired(this.age))) {
                this.distanceFromTicketTracker.updateLevel(entry.getLongKey(), ChunkTicketManager.getLevel((SortedArraySet)entry.getValue()), false);
            }
            if (!((SortedArraySet)entry.getValue()).isEmpty()) continue;
            objectIterator.remove();
        }
    }

    private static int getLevel(SortedArraySet<ChunkTicket<?>> sortedArraySet) {
        return !sortedArraySet.isEmpty() ? sortedArraySet.first().getLevel() : ThreadedAnvilChunkStorage.MAX_LEVEL + 1;
    }

    protected abstract boolean isUnloaded(long var1);

    @Nullable
    protected abstract ChunkHolder getChunkHolder(long var1);

    @Nullable
    protected abstract ChunkHolder setLevel(long var1, int var3, @Nullable ChunkHolder var4, int var5);

    public boolean tick(ThreadedAnvilChunkStorage threadedAnvilChunkStorage) {
        boolean bl;
        this.distanceFromNearestPlayerTracker.updateLevels();
        this.nearbyChunkTicketUpdater.updateLevels();
        int i = Integer.MAX_VALUE - this.distanceFromTicketTracker.update(Integer.MAX_VALUE);
        boolean bl2 = bl = i != 0;
        if (bl) {
            // empty if block
        }
        if (!this.chunkHolders.isEmpty()) {
            this.chunkHolders.forEach(chunkHolder -> chunkHolder.tick(threadedAnvilChunkStorage, this.mainThreadExecutor));
            this.chunkHolders.clear();
            return true;
        }
        if (!this.chunkPositions.isEmpty()) {
            LongIterator longIterator = this.chunkPositions.iterator();
            while (longIterator.hasNext()) {
                long l = longIterator.nextLong();
                if (!this.getTicketSet(l).stream().anyMatch(chunkTicket -> chunkTicket.getType() == ChunkTicketType.PLAYER)) continue;
                ChunkHolder chunkHolder2 = threadedAnvilChunkStorage.getCurrentChunkHolder(l);
                if (chunkHolder2 == null) {
                    throw new IllegalStateException();
                }
                CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> completableFuture = chunkHolder2.getEntityTickingFuture();
                completableFuture.thenAccept(either -> this.mainThreadExecutor.execute(() -> this.playerTicketThrottlerUnblocker.send(ChunkTaskPrioritySystem.createUnblockingMessage(() -> {}, l, false))));
            }
            this.chunkPositions.clear();
        }
        return bl;
    }

    void addTicket(long position, ChunkTicket<?> ticket) {
        SortedArraySet<ChunkTicket<?>> sortedArraySet = this.getTicketSet(position);
        int i = ChunkTicketManager.getLevel(sortedArraySet);
        ChunkTicket<?> chunkTicket = sortedArraySet.addAndGet(ticket);
        chunkTicket.setTickCreated(this.age);
        if (ticket.getLevel() < i) {
            this.distanceFromTicketTracker.updateLevel(position, ticket.getLevel(), true);
        }
    }

    void removeTicket(long pos, ChunkTicket<?> ticket) {
        SortedArraySet<ChunkTicket<?>> sortedArraySet = this.getTicketSet(pos);
        if (sortedArraySet.remove(ticket)) {
            // empty if block
        }
        if (sortedArraySet.isEmpty()) {
            this.ticketsByPosition.remove(pos);
        }
        this.distanceFromTicketTracker.updateLevel(pos, ChunkTicketManager.getLevel(sortedArraySet), false);
    }

    public <T> void addTicketWithLevel(ChunkTicketType<T> type, ChunkPos pos, int level, T argument) {
        this.addTicket(pos.toLong(), new ChunkTicket<T>(type, level, argument));
    }

    public <T> void removeTicketWithLevel(ChunkTicketType<T> type, ChunkPos pos, int level, T argument) {
        ChunkTicket<T> chunkTicket = new ChunkTicket<T>(type, level, argument);
        this.removeTicket(pos.toLong(), chunkTicket);
    }

    public <T> void addTicket(ChunkTicketType<T> type, ChunkPos pos, int radius, T argument) {
        this.addTicket(pos.toLong(), new ChunkTicket<T>(type, 33 - radius, argument));
    }

    public <T> void removeTicket(ChunkTicketType<T> type, ChunkPos pos, int radius, T argument) {
        ChunkTicket<T> chunkTicket = new ChunkTicket<T>(type, 33 - radius, argument);
        this.removeTicket(pos.toLong(), chunkTicket);
    }

    private SortedArraySet<ChunkTicket<?>> getTicketSet(long position) {
        return this.ticketsByPosition.computeIfAbsent(position, l -> SortedArraySet.create(4));
    }

    protected void setChunkForced(ChunkPos pos, boolean forced) {
        ChunkTicket<ChunkPos> chunkTicket = new ChunkTicket<ChunkPos>(ChunkTicketType.FORCED, 31, pos);
        if (forced) {
            this.addTicket(pos.toLong(), chunkTicket);
        } else {
            this.removeTicket(pos.toLong(), chunkTicket);
        }
    }

    public void handleChunkEnter(ChunkSectionPos pos, ServerPlayerEntity player) {
        long l2 = pos.toChunkPos().toLong();
        this.playersByChunkPos.computeIfAbsent(l2, l -> new ObjectOpenHashSet()).add(player);
        this.distanceFromNearestPlayerTracker.updateLevel(l2, 0, true);
        this.nearbyChunkTicketUpdater.updateLevel(l2, 0, true);
    }

    public void handleChunkLeave(ChunkSectionPos pos, ServerPlayerEntity player) {
        long l = pos.toChunkPos().toLong();
        ObjectSet objectSet = (ObjectSet)this.playersByChunkPos.get(l);
        objectSet.remove(player);
        if (objectSet.isEmpty()) {
            this.playersByChunkPos.remove(l);
            this.distanceFromNearestPlayerTracker.updateLevel(l, Integer.MAX_VALUE, false);
            this.nearbyChunkTicketUpdater.updateLevel(l, Integer.MAX_VALUE, false);
        }
    }

    protected String getTicket(long pos) {
        SortedArraySet<ChunkTicket<?>> sortedArraySet = this.ticketsByPosition.get(pos);
        String string = sortedArraySet == null || sortedArraySet.isEmpty() ? "no_ticket" : sortedArraySet.first().toString();
        return string;
    }

    protected void setWatchDistance(int viewDistance) {
        this.nearbyChunkTicketUpdater.setWatchDistance(viewDistance);
    }

    public int getSpawningChunkCount() {
        this.distanceFromNearestPlayerTracker.updateLevels();
        return this.distanceFromNearestPlayerTracker.distanceFromNearestPlayer.size();
    }

    public boolean method_20800(long l) {
        this.distanceFromNearestPlayerTracker.updateLevels();
        return this.distanceFromNearestPlayerTracker.distanceFromNearestPlayer.containsKey(l);
    }

    public String toDumpString() {
        return this.levelUpdateListener.getDebugString();
    }

    private void method_34876(String string) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(string));){
            for (Long2ObjectMap.Entry entry : this.ticketsByPosition.long2ObjectEntrySet()) {
                ChunkPos chunkPos = new ChunkPos(entry.getLongKey());
                for (ChunkTicket chunkTicket : (SortedArraySet)entry.getValue()) {
                    fileOutputStream.write((chunkPos.x + "\t" + chunkPos.z + "\t" + chunkTicket.getType() + "\t" + chunkTicket.getLevel() + "\t\n").getBytes(StandardCharsets.UTF_8));
                }
            }
        }
        catch (IOException fileOutputStream2) {
            LOGGER.error(fileOutputStream2);
        }
    }

    class TicketDistanceLevelPropagator
    extends ChunkPosDistanceLevelPropagator {
        public TicketDistanceLevelPropagator() {
            super(ThreadedAnvilChunkStorage.MAX_LEVEL + 2, 16, 256);
        }

        @Override
        protected int getInitialLevel(long id) {
            SortedArraySet<ChunkTicket<?>> sortedArraySet = ChunkTicketManager.this.ticketsByPosition.get(id);
            if (sortedArraySet == null) {
                return Integer.MAX_VALUE;
            }
            if (sortedArraySet.isEmpty()) {
                return Integer.MAX_VALUE;
            }
            return sortedArraySet.first().getLevel();
        }

        @Override
        protected int getLevel(long id) {
            ChunkHolder chunkHolder;
            if (!ChunkTicketManager.this.isUnloaded(id) && (chunkHolder = ChunkTicketManager.this.getChunkHolder(id)) != null) {
                return chunkHolder.getLevel();
            }
            return ThreadedAnvilChunkStorage.MAX_LEVEL + 1;
        }

        @Override
        protected void setLevel(long id, int level) {
            int i;
            ChunkHolder chunkHolder = ChunkTicketManager.this.getChunkHolder(id);
            int n = i = chunkHolder == null ? ThreadedAnvilChunkStorage.MAX_LEVEL + 1 : chunkHolder.getLevel();
            if (i == level) {
                return;
            }
            if ((chunkHolder = ChunkTicketManager.this.setLevel(id, level, chunkHolder, i)) != null) {
                ChunkTicketManager.this.chunkHolders.add(chunkHolder);
            }
        }

        public int update(int distance) {
            return this.applyPendingUpdates(distance);
        }
    }

    class DistanceFromNearestPlayerTracker
    extends ChunkPosDistanceLevelPropagator {
        protected final Long2ByteMap distanceFromNearestPlayer;
        protected final int maxDistance;

        protected DistanceFromNearestPlayerTracker(int maxDistance) {
            super(maxDistance + 2, 16, 256);
            this.distanceFromNearestPlayer = new Long2ByteOpenHashMap();
            this.maxDistance = maxDistance;
            this.distanceFromNearestPlayer.defaultReturnValue((byte)(maxDistance + 2));
        }

        @Override
        protected int getLevel(long id) {
            return this.distanceFromNearestPlayer.get(id);
        }

        @Override
        protected void setLevel(long id, int level) {
            byte b = level > this.maxDistance ? this.distanceFromNearestPlayer.remove(id) : this.distanceFromNearestPlayer.put(id, (byte)level);
            this.onDistanceChange(id, b, level);
        }

        protected void onDistanceChange(long pos, int oldDistance, int distance) {
        }

        @Override
        protected int getInitialLevel(long id) {
            return this.isPlayerInChunk(id) ? 0 : Integer.MAX_VALUE;
        }

        private boolean isPlayerInChunk(long chunkPos) {
            ObjectSet objectSet = (ObjectSet)ChunkTicketManager.this.playersByChunkPos.get(chunkPos);
            return objectSet != null && !objectSet.isEmpty();
        }

        public void updateLevels() {
            this.applyPendingUpdates(Integer.MAX_VALUE);
        }

        private void method_34878(String string) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(new File(string));){
                for (Long2ByteMap.Entry entry : this.distanceFromNearestPlayer.long2ByteEntrySet()) {
                    ChunkPos chunkPos = new ChunkPos(entry.getLongKey());
                    String string2 = Byte.toString(entry.getByteValue());
                    fileOutputStream.write((chunkPos.x + "\t" + chunkPos.z + "\t" + string2 + "\n").getBytes(StandardCharsets.UTF_8));
                }
            }
            catch (IOException fileOutputStream2) {
                LOGGER.error(fileOutputStream2);
            }
        }
    }

    class NearbyChunkTicketUpdater
    extends DistanceFromNearestPlayerTracker {
        private int watchDistance;
        private final Long2IntMap distances;
        private final LongSet positionsAffected;

        protected NearbyChunkTicketUpdater(int i) {
            super(i);
            this.distances = Long2IntMaps.synchronize(new Long2IntOpenHashMap());
            this.positionsAffected = new LongOpenHashSet();
            this.watchDistance = 0;
            this.distances.defaultReturnValue(i + 2);
        }

        @Override
        protected void onDistanceChange(long pos, int oldDistance, int distance) {
            this.positionsAffected.add(pos);
        }

        public void setWatchDistance(int watchDistance) {
            for (Long2ByteMap.Entry entry : this.distanceFromNearestPlayer.long2ByteEntrySet()) {
                byte b = entry.getByteValue();
                long l = entry.getLongKey();
                this.updateTicket(l, b, this.isWithinViewDistance(b), b <= watchDistance - 2);
            }
            this.watchDistance = watchDistance;
        }

        private void updateTicket(long pos, int distance, boolean oldWithinViewDistance, boolean withinViewDistance) {
            if (oldWithinViewDistance != withinViewDistance) {
                ChunkTicket<ChunkPos> chunkTicket = new ChunkTicket<ChunkPos>(ChunkTicketType.PLAYER, NEARBY_PLAYER_TICKET_LEVEL, new ChunkPos(pos));
                if (withinViewDistance) {
                    ChunkTicketManager.this.playerTicketThrottler.send(ChunkTaskPrioritySystem.createMessage(() -> ChunkTicketManager.this.mainThreadExecutor.execute(() -> {
                        if (this.isWithinViewDistance(this.getLevel(pos))) {
                            ChunkTicketManager.this.addTicket(pos, chunkTicket);
                            ChunkTicketManager.this.chunkPositions.add(pos);
                        } else {
                            ChunkTicketManager.this.playerTicketThrottlerUnblocker.send(ChunkTaskPrioritySystem.createUnblockingMessage(() -> {}, pos, false));
                        }
                    }), pos, () -> distance));
                } else {
                    ChunkTicketManager.this.playerTicketThrottlerUnblocker.send(ChunkTaskPrioritySystem.createUnblockingMessage(() -> ChunkTicketManager.this.mainThreadExecutor.execute(() -> ChunkTicketManager.this.removeTicket(pos, chunkTicket)), pos, true));
                }
            }
        }

        @Override
        public void updateLevels() {
            super.updateLevels();
            if (!this.positionsAffected.isEmpty()) {
                LongIterator longIterator = this.positionsAffected.iterator();
                while (longIterator.hasNext()) {
                    int j;
                    long l = longIterator.nextLong();
                    int i2 = this.distances.get(l);
                    if (i2 == (j = this.getLevel(l))) continue;
                    ChunkTicketManager.this.levelUpdateListener.updateLevel(new ChunkPos(l), () -> this.distances.get(l), j, i -> {
                        if (i >= this.distances.defaultReturnValue()) {
                            this.distances.remove(l);
                        } else {
                            this.distances.put(l, i);
                        }
                    });
                    this.updateTicket(l, j, this.isWithinViewDistance(i2), this.isWithinViewDistance(j));
                }
                this.positionsAffected.clear();
            }
        }

        private boolean isWithinViewDistance(int distance) {
            return distance <= this.watchDistance - 2;
        }
    }
}

