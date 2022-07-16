/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.world;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.particle.FireworksSparkParticle;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.sound.EntityTrackingSoundInstance;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.BiomeColorCache;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientEntityManager;
import net.minecraft.client.world.DummyClientTickScheduler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.TagManager;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.CuboidBlockIterator;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.Difficulty;
import net.minecraft.world.EntityList;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.entity.EntityHandler;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.level.ColorResolver;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ClientWorld
extends World {
    /**
     * A minor offset applied when spawning particles.
     */
    private static final double PARTICLE_Y_OFFSET = 0.05;
    final EntityList entityList = new EntityList();
    private final ClientEntityManager<Entity> entityManager = new ClientEntityManager<Entity>(Entity.class, new ClientEntityHandler());
    private final ClientPlayNetworkHandler netHandler;
    private final WorldRenderer worldRenderer;
    private final Properties clientWorldProperties;
    private final DimensionEffects dimensionEffects;
    private final MinecraftClient client = MinecraftClient.getInstance();
    final List<AbstractClientPlayerEntity> players = Lists.newArrayList();
    private Scoreboard scoreboard = new Scoreboard();
    private final Map<String, MapState> mapStates = Maps.newHashMap();
    private static final long field_32640 = 0xFFFFFFL;
    private int lightningTicksLeft;
    private final Object2ObjectArrayMap<ColorResolver, BiomeColorCache> colorCache = Util.make(new Object2ObjectArrayMap(3), cache -> {
        cache.put(BiomeColors.GRASS_COLOR, new BiomeColorCache());
        cache.put(BiomeColors.FOLIAGE_COLOR, new BiomeColorCache());
        cache.put(BiomeColors.WATER_COLOR, new BiomeColorCache());
    });
    private final ClientChunkManager chunkManager;

    public ClientWorld(ClientPlayNetworkHandler networkHandler, Properties properties, RegistryKey<World> registryRef, DimensionType dimensionType, int loadDistance, Supplier<Profiler> profiler, WorldRenderer worldRenderer, boolean debugWorld, long seed) {
        super(properties, registryRef, dimensionType, profiler, true, debugWorld, seed);
        this.netHandler = networkHandler;
        this.chunkManager = new ClientChunkManager(this, loadDistance);
        this.clientWorldProperties = properties;
        this.worldRenderer = worldRenderer;
        this.dimensionEffects = DimensionEffects.byDimensionType(dimensionType);
        this.setSpawnPos(new BlockPos(8, 64, 8), 0.0f);
        this.calculateAmbientDarkness();
        this.initWeatherGradients();
    }

    public DimensionEffects getDimensionEffects() {
        return this.dimensionEffects;
    }

    public void tick(BooleanSupplier shouldKeepTicking) {
        this.getWorldBorder().tick();
        this.tickTime();
        this.getProfiler().push("blocks");
        this.chunkManager.tick(shouldKeepTicking);
        this.getProfiler().pop();
    }

    private void tickTime() {
        this.setTime(this.properties.getTime() + 1L);
        if (this.properties.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
            this.setTimeOfDay(this.properties.getTimeOfDay() + 1L);
        }
    }

    public void setTime(long time) {
        this.clientWorldProperties.setTime(time);
    }

    public void setTimeOfDay(long timeOfDay) {
        if (timeOfDay < 0L) {
            timeOfDay = -timeOfDay;
            this.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, null);
        } else {
            this.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, null);
        }
        this.clientWorldProperties.setTimeOfDay(timeOfDay);
    }

    public Iterable<Entity> getEntities() {
        return this.getEntityLookup().iterate();
    }

    public void tickEntities() {
        Profiler profiler = this.getProfiler();
        profiler.push("entities");
        this.entityList.forEach(entity -> {
            if (entity.isRemoved() || entity.hasVehicle()) {
                return;
            }
            this.tickEntity(this::tickEntity, entity);
        });
        profiler.pop();
        this.tickBlockEntities();
    }

    public void tickEntity(Entity entity) {
        entity.resetPosition();
        ++entity.age;
        this.getProfiler().push(() -> Registry.ENTITY_TYPE.getId(entity.getType()).toString());
        entity.tick();
        this.getProfiler().pop();
        for (Entity entity2 : entity.getPassengerList()) {
            this.tickPassenger(entity, entity2);
        }
    }

    private void tickPassenger(Entity entity, Entity passenger) {
        if (passenger.isRemoved() || passenger.getVehicle() != entity) {
            passenger.stopRiding();
            return;
        }
        if (!(passenger instanceof PlayerEntity) && !this.entityList.has(passenger)) {
            return;
        }
        passenger.resetPosition();
        ++passenger.age;
        passenger.tickRiding();
        for (Entity entity2 : passenger.getPassengerList()) {
            this.tickPassenger(passenger, entity2);
        }
    }

    public void unloadBlockEntities(WorldChunk chunk) {
        chunk.removeAllBlockEntities();
        this.chunkManager.getLightingProvider().setColumnEnabled(chunk.getPos(), false);
        this.entityManager.stopTicking(chunk.getPos());
    }

    public void resetChunkColor(ChunkPos chunkPos) {
        this.colorCache.forEach((colorResolver, biomeColorCache) -> biomeColorCache.reset(chunkPos.x, chunkPos.z));
        this.entityManager.startTicking(chunkPos);
    }

    public void reloadColor() {
        this.colorCache.forEach((colorResolver, biomeColorCache) -> biomeColorCache.reset());
    }

    @Override
    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return true;
    }

    public int getRegularEntityCount() {
        return this.entityManager.getEntityCount();
    }

    public void addPlayer(int id, AbstractClientPlayerEntity player) {
        this.addEntityPrivate(id, player);
    }

    public void addEntity(int id, Entity entity) {
        this.addEntityPrivate(id, entity);
    }

    private void addEntityPrivate(int id, Entity entity) {
        this.removeEntity(id, Entity.RemovalReason.DISCARDED);
        this.entityManager.addEntity(entity);
    }

    public void removeEntity(int entityId, Entity.RemovalReason removalReason) {
        Entity entity = this.getEntityLookup().get(entityId);
        if (entity != null) {
            entity.setRemoved(removalReason);
            entity.onRemoved();
        }
    }

    @Override
    @Nullable
    public Entity getEntityById(int id) {
        return this.getEntityLookup().get(id);
    }

    public void setBlockStateWithoutNeighborUpdates(BlockPos pos, BlockState state) {
        this.setBlockState(pos, state, Block.NOTIFY_ALL | Block.FORCE_STATE);
    }

    @Override
    public void disconnect() {
        this.netHandler.getConnection().disconnect(new TranslatableText("multiplayer.status.quitting"));
    }

    public void doRandomBlockDisplayTicks(int centerX, int centerY, int centerZ) {
        int i = 32;
        Random random = new Random();
        BlockParticle blockParticle = this.getBlockParticle();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int j = 0; j < 667; ++j) {
            this.randomBlockDisplayTick(centerX, centerY, centerZ, 16, random, blockParticle, mutable);
            this.randomBlockDisplayTick(centerX, centerY, centerZ, 32, random, blockParticle, mutable);
        }
    }

    @Nullable
    private BlockParticle getBlockParticle() {
        if (this.client.interactionManager.getCurrentGameMode() == GameMode.CREATIVE) {
            ItemStack itemStack = this.client.player.getMainHandStack();
            if (itemStack.getItem() == Items.BARRIER) {
                return BlockParticle.BARRIER;
            }
            if (itemStack.getItem() == Items.LIGHT) {
                return BlockParticle.LIGHT;
            }
        }
        return null;
    }

    public void randomBlockDisplayTick(int centerX, int centerY, int centerZ, int radius, Random random, @Nullable BlockParticle blockParticle, BlockPos.Mutable pos) {
        int i = centerX + this.random.nextInt(radius) - this.random.nextInt(radius);
        int j = centerY + this.random.nextInt(radius) - this.random.nextInt(radius);
        int k = centerZ + this.random.nextInt(radius) - this.random.nextInt(radius);
        pos.set(i, j, k);
        BlockState blockState = this.getBlockState(pos);
        blockState.getBlock().randomDisplayTick(blockState, this, pos, random);
        FluidState fluidState = this.getFluidState(pos);
        if (!fluidState.isEmpty()) {
            fluidState.randomDisplayTick(this, pos, random);
            ParticleEffect particleEffect = fluidState.getParticle();
            if (particleEffect != null && this.random.nextInt(10) == 0) {
                boolean bl = blockState.isSideSolidFullSquare(this, pos, Direction.DOWN);
                Vec3i blockPos = pos.down();
                this.addParticle((BlockPos)blockPos, this.getBlockState((BlockPos)blockPos), particleEffect, bl);
            }
        }
        if (blockParticle != null && blockState.getBlock() == blockParticle.block) {
            this.addParticle(blockParticle.particle, (double)i + 0.5, (double)j + 0.5, (double)k + 0.5, 0.0, 0.0, 0.0);
        }
        if (!blockState.isFullCube(this, pos)) {
            this.getBiome(pos).getParticleConfig().ifPresent(config -> {
                if (config.shouldAddParticle(this.random)) {
                    this.addParticle(config.getParticle(), (double)pos.getX() + this.random.nextDouble(), (double)pos.getY() + this.random.nextDouble(), (double)pos.getZ() + this.random.nextDouble(), 0.0, 0.0, 0.0);
                }
            });
        }
    }

    private void addParticle(BlockPos pos, BlockState state, ParticleEffect parameters, boolean bl) {
        if (!state.getFluidState().isEmpty()) {
            return;
        }
        VoxelShape voxelShape = state.getCollisionShape(this, pos);
        double d = voxelShape.getMax(Direction.Axis.Y);
        if (d < 1.0) {
            if (bl) {
                this.addParticle(pos.getX(), pos.getX() + 1, pos.getZ(), pos.getZ() + 1, (double)(pos.getY() + 1) - 0.05, parameters);
            }
        } else if (!state.isIn(BlockTags.IMPERMEABLE)) {
            double e = voxelShape.getMin(Direction.Axis.Y);
            if (e > 0.0) {
                this.addParticle(pos, parameters, voxelShape, (double)pos.getY() + e - 0.05);
            } else {
                BlockPos blockPos = pos.down();
                BlockState blockState = this.getBlockState(blockPos);
                VoxelShape voxelShape2 = blockState.getCollisionShape(this, blockPos);
                double f = voxelShape2.getMax(Direction.Axis.Y);
                if (f < 1.0 && blockState.getFluidState().isEmpty()) {
                    this.addParticle(pos, parameters, voxelShape, (double)pos.getY() - 0.05);
                }
            }
        }
    }

    private void addParticle(BlockPos pos, ParticleEffect parameters, VoxelShape shape, double y) {
        this.addParticle((double)pos.getX() + shape.getMin(Direction.Axis.X), (double)pos.getX() + shape.getMax(Direction.Axis.X), (double)pos.getZ() + shape.getMin(Direction.Axis.Z), (double)pos.getZ() + shape.getMax(Direction.Axis.Z), y, parameters);
    }

    private void addParticle(double minX, double maxX, double minZ, double maxZ, double y, ParticleEffect parameters) {
        this.addParticle(parameters, MathHelper.lerp(this.random.nextDouble(), minX, maxX), y, MathHelper.lerp(this.random.nextDouble(), minZ, maxZ), 0.0, 0.0, 0.0);
    }

    @Override
    public CrashReportSection addDetailsToCrashReport(CrashReport report) {
        CrashReportSection crashReportSection = super.addDetailsToCrashReport(report);
        crashReportSection.add("Server brand", () -> this.client.player.getServerBrand());
        crashReportSection.add("Server type", () -> this.client.getServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server");
        return crashReportSection;
    }

    @Override
    public void playSound(@Nullable PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        if (except == this.client.player) {
            this.playSound(x, y, z, sound, category, volume, pitch, false);
        }
    }

    @Override
    public void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        if (except == this.client.player) {
            this.client.getSoundManager().play(new EntityTrackingSoundInstance(sound, category, volume, pitch, entity));
        }
    }

    public void playSound(BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance) {
        this.playSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, sound, category, volume, pitch, useDistance);
    }

    @Override
    public void playSound(double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance) {
        double d = this.client.gameRenderer.getCamera().getPos().squaredDistanceTo(x, y, z);
        PositionedSoundInstance positionedSoundInstance = new PositionedSoundInstance(sound, category, volume, pitch, x, y, z);
        if (useDistance && d > 100.0) {
            double e = Math.sqrt(d) / 40.0;
            this.client.getSoundManager().play(positionedSoundInstance, (int)(e * 20.0));
        } else {
            this.client.getSoundManager().play(positionedSoundInstance);
        }
    }

    @Override
    public void addFireworkParticle(double x, double y, double z, double velocityX, double velocityY, double velocityZ, @Nullable NbtCompound nbt) {
        this.client.particleManager.addParticle(new FireworksSparkParticle.FireworkParticle(this, x, y, z, velocityX, velocityY, velocityZ, this.client.particleManager, nbt));
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        this.netHandler.sendPacket(packet);
    }

    @Override
    public RecipeManager getRecipeManager() {
        return this.netHandler.getRecipeManager();
    }

    public void setScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    @Override
    public TickScheduler<Block> getBlockTickScheduler() {
        return DummyClientTickScheduler.get();
    }

    @Override
    public TickScheduler<Fluid> getFluidTickScheduler() {
        return DummyClientTickScheduler.get();
    }

    @Override
    public ClientChunkManager getChunkManager() {
        return this.chunkManager;
    }

    @Override
    @Nullable
    public MapState getMapState(String id) {
        return this.mapStates.get(id);
    }

    @Override
    public void putMapState(String id, MapState state) {
        this.mapStates.put(id, state);
    }

    @Override
    public int getNextMapId() {
        return 0;
    }

    @Override
    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    @Override
    public TagManager getTagManager() {
        return this.netHandler.getTagManager();
    }

    @Override
    public DynamicRegistryManager getRegistryManager() {
        return this.netHandler.getRegistryManager();
    }

    @Override
    public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        this.worldRenderer.updateBlock(this, pos, oldState, newState, flags);
    }

    @Override
    public void scheduleBlockRerenderIfNeeded(BlockPos pos, BlockState old, BlockState updated) {
        this.worldRenderer.scheduleBlockRerenderIfNeeded(pos, old, updated);
    }

    public void scheduleBlockRenders(int x, int y, int z) {
        this.worldRenderer.scheduleBlockRenders(x, y, z);
    }

    @Override
    public void setBlockBreakingInfo(int entityId, BlockPos pos, int progress) {
        this.worldRenderer.setBlockBreakingInfo(entityId, pos, progress);
    }

    @Override
    public void syncGlobalEvent(int eventId, BlockPos pos, int data) {
        this.worldRenderer.processGlobalEvent(eventId, pos, data);
    }

    @Override
    public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {
        try {
            this.worldRenderer.processWorldEvent(player, eventId, pos, data);
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.create(throwable, "Playing level event");
            CrashReportSection crashReportSection = crashReport.addElement("Level event being played");
            crashReportSection.add("Block coordinates", CrashReportSection.createPositionString(this, pos));
            crashReportSection.add("Event source", player);
            crashReportSection.add("Event type", eventId);
            crashReportSection.add("Event data", data);
            throw new CrashException(crashReport);
        }
    }

    @Override
    public void addParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        this.worldRenderer.addParticle(parameters, parameters.getType().shouldAlwaysSpawn(), x, y, z, velocityX, velocityY, velocityZ);
    }

    @Override
    public void addParticle(ParticleEffect parameters, boolean alwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        this.worldRenderer.addParticle(parameters, parameters.getType().shouldAlwaysSpawn() || alwaysSpawn, x, y, z, velocityX, velocityY, velocityZ);
    }

    @Override
    public void addImportantParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        this.worldRenderer.addParticle(parameters, false, true, x, y, z, velocityX, velocityY, velocityZ);
    }

    @Override
    public void addImportantParticle(ParticleEffect parameters, boolean alwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        this.worldRenderer.addParticle(parameters, parameters.getType().shouldAlwaysSpawn() || alwaysSpawn, true, x, y, z, velocityX, velocityY, velocityZ);
    }

    public List<AbstractClientPlayerEntity> getPlayers() {
        return this.players;
    }

    @Override
    public Biome getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
        return this.getRegistryManager().get(Registry.BIOME_KEY).getOrThrow(BiomeKeys.PLAINS);
    }

    public float method_23783(float f) {
        float g = this.getSkyAngle(f);
        float h = 1.0f - (MathHelper.cos(g * ((float)Math.PI * 2)) * 2.0f + 0.2f);
        h = MathHelper.clamp(h, 0.0f, 1.0f);
        h = 1.0f - h;
        h = (float)((double)h * (1.0 - (double)(this.getRainGradient(f) * 5.0f) / 16.0));
        h = (float)((double)h * (1.0 - (double)(this.getThunderGradient(f) * 5.0f) / 16.0));
        return h * 0.8f + 0.2f;
    }

    public Vec3d method_23777(Vec3d vec3d, float f) {
        float n;
        float m;
        float g = this.getSkyAngle(f);
        Vec3d vec3d2 = vec3d.subtract(2.0, 2.0, 2.0).multiply(0.25);
        BiomeAccess biomeAccess = this.getBiomeAccess();
        Vec3d vec3d3 = CubicSampler.sampleColor(vec3d2, (i, j, k) -> Vec3d.unpackRgb(biomeAccess.getBiomeForNoiseGen(i, j, k).getSkyColor()));
        float h = MathHelper.cos(g * ((float)Math.PI * 2)) * 2.0f + 0.5f;
        h = MathHelper.clamp(h, 0.0f, 1.0f);
        float i2 = (float)vec3d3.x * h;
        float j2 = (float)vec3d3.y * h;
        float k2 = (float)vec3d3.z * h;
        float l = this.getRainGradient(f);
        if (l > 0.0f) {
            m = (i2 * 0.3f + j2 * 0.59f + k2 * 0.11f) * 0.6f;
            n = 1.0f - l * 0.75f;
            i2 = i2 * n + m * (1.0f - n);
            j2 = j2 * n + m * (1.0f - n);
            k2 = k2 * n + m * (1.0f - n);
        }
        if ((m = this.getThunderGradient(f)) > 0.0f) {
            n = (i2 * 0.3f + j2 * 0.59f + k2 * 0.11f) * 0.2f;
            float o = 1.0f - m * 0.75f;
            i2 = i2 * o + n * (1.0f - o);
            j2 = j2 * o + n * (1.0f - o);
            k2 = k2 * o + n * (1.0f - o);
        }
        if (this.lightningTicksLeft > 0) {
            n = (float)this.lightningTicksLeft - f;
            if (n > 1.0f) {
                n = 1.0f;
            }
            i2 = i2 * (1.0f - (n *= 0.45f)) + 0.8f * n;
            j2 = j2 * (1.0f - n) + 0.8f * n;
            k2 = k2 * (1.0f - n) + 1.0f * n;
        }
        return new Vec3d(i2, j2, k2);
    }

    public Vec3d getCloudsColor(float tickDelta) {
        float m;
        float l;
        float f = this.getSkyAngle(tickDelta);
        float g = MathHelper.cos(f * ((float)Math.PI * 2)) * 2.0f + 0.5f;
        g = MathHelper.clamp(g, 0.0f, 1.0f);
        float h = 1.0f;
        float i = 1.0f;
        float j = 1.0f;
        float k = this.getRainGradient(tickDelta);
        if (k > 0.0f) {
            l = (h * 0.3f + i * 0.59f + j * 0.11f) * 0.6f;
            m = 1.0f - k * 0.95f;
            h = h * m + l * (1.0f - m);
            i = i * m + l * (1.0f - m);
            j = j * m + l * (1.0f - m);
        }
        h *= g * 0.9f + 0.1f;
        i *= g * 0.9f + 0.1f;
        j *= g * 0.85f + 0.15f;
        l = this.getThunderGradient(tickDelta);
        if (l > 0.0f) {
            m = (h * 0.3f + i * 0.59f + j * 0.11f) * 0.2f;
            float n = 1.0f - l * 0.95f;
            h = h * n + m * (1.0f - n);
            i = i * n + m * (1.0f - n);
            j = j * n + m * (1.0f - n);
        }
        return new Vec3d(h, i, j);
    }

    public float method_23787(float f) {
        float g = this.getSkyAngle(f);
        float h = 1.0f - (MathHelper.cos(g * ((float)Math.PI * 2)) * 2.0f + 0.25f);
        h = MathHelper.clamp(h, 0.0f, 1.0f);
        return h * h * 0.5f;
    }

    public int getLightningTicksLeft() {
        return this.lightningTicksLeft;
    }

    @Override
    public void setLightningTicksLeft(int lightningTicksLeft) {
        this.lightningTicksLeft = lightningTicksLeft;
    }

    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        boolean bl = this.getDimensionEffects().isDarkened();
        if (!shaded) {
            return bl ? 0.9f : 1.0f;
        }
        switch (direction) {
            case DOWN: {
                return bl ? 0.9f : 0.5f;
            }
            case UP: {
                return bl ? 0.9f : 1.0f;
            }
            case NORTH: 
            case SOUTH: {
                return 0.8f;
            }
            case WEST: 
            case EAST: {
                return 0.6f;
            }
        }
        return 1.0f;
    }

    @Override
    public int getColor(BlockPos pos, ColorResolver colorResolver) {
        BiomeColorCache biomeColorCache = this.colorCache.get(colorResolver);
        return biomeColorCache.getBiomeColor(pos, () -> this.calculateColor(pos, colorResolver));
    }

    public int calculateColor(BlockPos pos, ColorResolver colorResolver) {
        int i = MinecraftClient.getInstance().options.biomeBlendRadius;
        if (i == 0) {
            return colorResolver.getColor(this.getBiome(pos), pos.getX(), pos.getZ());
        }
        int j = (i * 2 + 1) * (i * 2 + 1);
        int k = 0;
        int l = 0;
        int m = 0;
        CuboidBlockIterator cuboidBlockIterator = new CuboidBlockIterator(pos.getX() - i, pos.getY(), pos.getZ() - i, pos.getX() + i, pos.getY(), pos.getZ() + i);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        while (cuboidBlockIterator.step()) {
            mutable.set(cuboidBlockIterator.getX(), cuboidBlockIterator.getY(), cuboidBlockIterator.getZ());
            int n = colorResolver.getColor(this.getBiome(mutable), mutable.getX(), mutable.getZ());
            k += (n & 0xFF0000) >> 16;
            l += (n & 0xFF00) >> 8;
            m += n & 0xFF;
        }
        return (k / j & 0xFF) << 16 | (l / j & 0xFF) << 8 | m / j & 0xFF;
    }

    public BlockPos getSpawnPos() {
        BlockPos blockPos = new BlockPos(this.properties.getSpawnX(), this.properties.getSpawnY(), this.properties.getSpawnZ());
        if (!this.getWorldBorder().contains(blockPos)) {
            blockPos = this.getTopPosition(Heightmap.Type.MOTION_BLOCKING, new BlockPos(this.getWorldBorder().getCenterX(), 0.0, this.getWorldBorder().getCenterZ()));
        }
        return blockPos;
    }

    public float getSpawnAngle() {
        return this.properties.getSpawnAngle();
    }

    public void setSpawnPos(BlockPos pos, float angle) {
        this.properties.setSpawnPos(pos, angle);
    }

    public String toString() {
        return "ClientLevel";
    }

    @Override
    public Properties getLevelProperties() {
        return this.clientWorldProperties;
    }

    @Override
    public void emitGameEvent(@Nullable Entity entity, GameEvent event, BlockPos pos) {
    }

    protected Map<String, MapState> getMapStates() {
        return ImmutableMap.copyOf(this.mapStates);
    }

    protected void putMapStates(Map<String, MapState> mapStates) {
        this.mapStates.putAll(mapStates);
    }

    @Override
    protected EntityLookup<Entity> getEntityLookup() {
        return this.entityManager.getLookup();
    }

    @Override
    public String asString() {
        return "Chunks[C] W: " + this.chunkManager.getDebugString() + " E: " + this.entityManager.getDebugString();
    }

    @Override
    public void addBlockBreakParticles(BlockPos pos, BlockState state) {
        this.client.particleManager.addBlockBreakParticles(pos, state);
    }

    @Override
    public /* synthetic */ WorldProperties getLevelProperties() {
        return this.getLevelProperties();
    }

    @Override
    public /* synthetic */ ChunkManager getChunkManager() {
        return this.getChunkManager();
    }

    @Environment(value=EnvType.CLIENT)
    final class ClientEntityHandler
    implements EntityHandler<Entity> {
        ClientEntityHandler() {
        }

        @Override
        public void create(Entity entity) {
        }

        @Override
        public void destroy(Entity entity) {
        }

        @Override
        public void startTicking(Entity entity) {
            ClientWorld.this.entityList.add(entity);
        }

        @Override
        public void stopTicking(Entity entity) {
            ClientWorld.this.entityList.remove(entity);
        }

        @Override
        public void startTracking(Entity entity) {
            if (entity instanceof AbstractClientPlayerEntity) {
                ClientWorld.this.players.add((AbstractClientPlayerEntity)entity);
            }
        }

        @Override
        public void stopTracking(Entity entity) {
            entity.detach();
            ClientWorld.this.players.remove(entity);
        }

        @Override
        public /* synthetic */ void stopTracking(Object entity) {
            this.stopTracking((Entity)entity);
        }

        @Override
        public /* synthetic */ void startTracking(Object entity) {
            this.startTracking((Entity)entity);
        }

        @Override
        public /* synthetic */ void stopTicking(Object entity) {
            this.stopTicking((Entity)entity);
        }

        @Override
        public /* synthetic */ void startTicking(Object entity) {
            this.startTicking((Entity)entity);
        }

        @Override
        public /* synthetic */ void destroy(Object entity) {
            this.destroy((Entity)entity);
        }

        @Override
        public /* synthetic */ void create(Object entity) {
            this.create((Entity)entity);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Properties
    implements MutableWorldProperties {
        private final boolean hardcore;
        private final GameRules gameRules;
        private final boolean flatWorld;
        private int spawnX;
        private int spawnY;
        private int spawnZ;
        private float spawnAngle;
        private long time;
        private long timeOfDay;
        private boolean raining;
        private Difficulty difficulty;
        private boolean difficultyLocked;

        public Properties(Difficulty difficulty, boolean hardcore, boolean flatWorld) {
            this.difficulty = difficulty;
            this.hardcore = hardcore;
            this.flatWorld = flatWorld;
            this.gameRules = new GameRules();
        }

        @Override
        public int getSpawnX() {
            return this.spawnX;
        }

        @Override
        public int getSpawnY() {
            return this.spawnY;
        }

        @Override
        public int getSpawnZ() {
            return this.spawnZ;
        }

        @Override
        public float getSpawnAngle() {
            return this.spawnAngle;
        }

        @Override
        public long getTime() {
            return this.time;
        }

        @Override
        public long getTimeOfDay() {
            return this.timeOfDay;
        }

        @Override
        public void setSpawnX(int spawnX) {
            this.spawnX = spawnX;
        }

        @Override
        public void setSpawnY(int spawnY) {
            this.spawnY = spawnY;
        }

        @Override
        public void setSpawnZ(int spawnZ) {
            this.spawnZ = spawnZ;
        }

        @Override
        public void setSpawnAngle(float angle) {
            this.spawnAngle = angle;
        }

        public void setTime(long difficulty) {
            this.time = difficulty;
        }

        public void setTimeOfDay(long time) {
            this.timeOfDay = time;
        }

        @Override
        public void setSpawnPos(BlockPos pos, float angle) {
            this.spawnX = pos.getX();
            this.spawnY = pos.getY();
            this.spawnZ = pos.getZ();
            this.spawnAngle = angle;
        }

        @Override
        public boolean isThundering() {
            return false;
        }

        @Override
        public boolean isRaining() {
            return this.raining;
        }

        @Override
        public void setRaining(boolean raining) {
            this.raining = raining;
        }

        @Override
        public boolean isHardcore() {
            return this.hardcore;
        }

        @Override
        public GameRules getGameRules() {
            return this.gameRules;
        }

        @Override
        public Difficulty getDifficulty() {
            return this.difficulty;
        }

        @Override
        public boolean isDifficultyLocked() {
            return this.difficultyLocked;
        }

        @Override
        public void populateCrashReport(CrashReportSection reportSection, HeightLimitView world) {
            MutableWorldProperties.super.populateCrashReport(reportSection, world);
        }

        public void setDifficulty(Difficulty difficulty) {
            this.difficulty = difficulty;
        }

        public void setDifficultyLocked(boolean difficultyLocked) {
            this.difficultyLocked = difficultyLocked;
        }

        public double getSkyDarknessHeight(HeightLimitView world) {
            if (this.flatWorld) {
                return world.getBottomY();
            }
            return 63.0;
        }

        public double getHorizonShadingRatio() {
            if (this.flatWorld) {
                return 1.0;
            }
            return 0.03125;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum BlockParticle {
        BARRIER(Blocks.BARRIER, ParticleTypes.BARRIER),
        LIGHT(Blocks.LIGHT, ParticleTypes.LIGHT);

        final Block block;
        final ParticleEffect particle;

        private BlockParticle(Block block, ParticleEffect particle) {
            this.block = block;
            this.particle = particle;
        }
    }
}
