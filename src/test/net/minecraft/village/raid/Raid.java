/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.village.raid;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class Raid {
    private static final int field_30676 = 2;
    private static final int field_30677 = 0;
    private static final int field_30678 = 1;
    private static final int field_30679 = 2;
    private static final int field_30680 = 32;
    private static final int field_30681 = 48000;
    private static final int field_30682 = 3;
    private static final String OMINOUS_BANNER_TRANSLATION_KEY = "block.minecraft.ominous_banner";
    private static final String RAIDERS_REMAINING_TRANSLATION_KEY = "event.minecraft.raid.raiders_remaining";
    public static final int field_30669 = 16;
    private static final int field_30685 = 40;
    private static final int DEFAULT_PRE_RAID_TICKS = 300;
    public static final int MAX_DESPAWN_COUNTER = 2400;
    public static final int field_30671 = 600;
    private static final int field_30687 = 30;
    public static final int field_30672 = 24000;
    public static final int field_30673 = 5;
    private static final int field_30688 = 2;
    private static final Text EVENT_TEXT = new TranslatableText("event.minecraft.raid");
    private static final Text VICTORY_SUFFIX_TEXT = new TranslatableText("event.minecraft.raid.victory");
    private static final Text DEFEAT_SUFFIX_TEXT = new TranslatableText("event.minecraft.raid.defeat");
    private static final Text VICTORY_TITLE = EVENT_TEXT.shallowCopy().append(" - ").append(VICTORY_SUFFIX_TEXT);
    private static final Text DEFEAT_TITLE = EVENT_TEXT.shallowCopy().append(" - ").append(DEFEAT_SUFFIX_TEXT);
    private static final int MAX_ACTIVE_TICKS = 48000;
    public static final int field_30674 = 9216;
    public static final int field_30675 = 12544;
    private final Map<Integer, RaiderEntity> waveToCaptain = Maps.newHashMap();
    private final Map<Integer, Set<RaiderEntity>> waveToRaiders = Maps.newHashMap();
    private final Set<UUID> heroesOfTheVillage = Sets.newHashSet();
    private long ticksActive;
    private BlockPos center;
    private final ServerWorld world;
    private boolean started;
    private final int id;
    private float totalHealth;
    private int badOmenLevel;
    private boolean active;
    private int wavesSpawned;
    private final ServerBossBar bar = new ServerBossBar(EVENT_TEXT, BossBar.Color.RED, BossBar.Style.NOTCHED_10);
    private int postRaidTicks;
    private int preRaidTicks;
    private final Random random = new Random();
    private final int waveCount;
    private Status status;
    private int finishCooldown;
    private Optional<BlockPos> preCalculatedRavagerSpawnLocation = Optional.empty();

    public Raid(int id, ServerWorld world, BlockPos pos) {
        this.id = id;
        this.world = world;
        this.active = true;
        this.preRaidTicks = 300;
        this.bar.setPercent(0.0f);
        this.center = pos;
        this.waveCount = this.getMaxWaves(world.getDifficulty());
        this.status = Status.ONGOING;
    }

    public Raid(ServerWorld world, NbtCompound nbt) {
        this.world = world;
        this.id = nbt.getInt("Id");
        this.started = nbt.getBoolean("Started");
        this.active = nbt.getBoolean("Active");
        this.ticksActive = nbt.getLong("TicksActive");
        this.badOmenLevel = nbt.getInt("BadOmenLevel");
        this.wavesSpawned = nbt.getInt("GroupsSpawned");
        this.preRaidTicks = nbt.getInt("PreRaidTicks");
        this.postRaidTicks = nbt.getInt("PostRaidTicks");
        this.totalHealth = nbt.getFloat("TotalHealth");
        this.center = new BlockPos(nbt.getInt("CX"), nbt.getInt("CY"), nbt.getInt("CZ"));
        this.waveCount = nbt.getInt("NumGroups");
        this.status = Status.fromName(nbt.getString("Status"));
        this.heroesOfTheVillage.clear();
        if (nbt.contains("HeroesOfTheVillage", 9)) {
            NbtList nbtList = nbt.getList("HeroesOfTheVillage", 11);
            for (int i = 0; i < nbtList.size(); ++i) {
                this.heroesOfTheVillage.add(NbtHelper.toUuid(nbtList.get(i)));
            }
        }
    }

    public boolean isFinished() {
        return this.hasWon() || this.hasLost();
    }

    public boolean isPreRaid() {
        return this.hasSpawned() && this.getRaiderCount() == 0 && this.preRaidTicks > 0;
    }

    public boolean hasSpawned() {
        return this.wavesSpawned > 0;
    }

    public boolean hasStopped() {
        return this.status == Status.STOPPED;
    }

    public boolean hasWon() {
        return this.status == Status.VICTORY;
    }

    public boolean hasLost() {
        return this.status == Status.LOSS;
    }

    public float getTotalHealth() {
        return this.totalHealth;
    }

    public Set<RaiderEntity> getAllRaiders() {
        HashSet<RaiderEntity> set = Sets.newHashSet();
        for (Set<RaiderEntity> set2 : this.waveToRaiders.values()) {
            set.addAll(set2);
        }
        return set;
    }

    public World getWorld() {
        return this.world;
    }

    public boolean hasStarted() {
        return this.started;
    }

    public int getGroupsSpawned() {
        return this.wavesSpawned;
    }

    private Predicate<ServerPlayerEntity> isInRaidDistance() {
        return player -> {
            BlockPos blockPos = player.getBlockPos();
            return player.isAlive() && this.world.getRaidAt(blockPos) == this;
        };
    }

    private void updateBarToPlayers() {
        HashSet<ServerPlayerEntity> set = Sets.newHashSet(this.bar.getPlayers());
        List<ServerPlayerEntity> list = this.world.getPlayers(this.isInRaidDistance());
        for (ServerPlayerEntity serverPlayerEntity : list) {
            if (set.contains(serverPlayerEntity)) continue;
            this.bar.addPlayer(serverPlayerEntity);
        }
        for (ServerPlayerEntity serverPlayerEntity : set) {
            if (list.contains(serverPlayerEntity)) continue;
            this.bar.removePlayer(serverPlayerEntity);
        }
    }

    public int getMaxAcceptableBadOmenLevel() {
        return 5;
    }

    public int getBadOmenLevel() {
        return this.badOmenLevel;
    }

    public void setBadOmenLevel(int level) {
        this.badOmenLevel = level;
    }

    public void start(PlayerEntity player) {
        if (player.hasStatusEffect(StatusEffects.BAD_OMEN)) {
            this.badOmenLevel += player.getStatusEffect(StatusEffects.BAD_OMEN).getAmplifier() + 1;
            this.badOmenLevel = MathHelper.clamp(this.badOmenLevel, 0, this.getMaxAcceptableBadOmenLevel());
        }
        player.removeStatusEffect(StatusEffects.BAD_OMEN);
    }

    public void invalidate() {
        this.active = false;
        this.bar.clearPlayers();
        this.status = Status.STOPPED;
    }

    public void tick() {
        if (this.hasStopped()) {
            return;
        }
        if (this.status == Status.ONGOING) {
            int bl3;
            boolean bl2;
            boolean bl = this.active;
            this.active = this.world.isChunkLoaded(this.center);
            if (this.world.getDifficulty() == Difficulty.PEACEFUL) {
                this.invalidate();
                return;
            }
            if (bl != this.active) {
                this.bar.setVisible(this.active);
            }
            if (!this.active) {
                return;
            }
            if (!this.world.isNearOccupiedPointOfInterest(this.center)) {
                this.moveRaidCenter();
            }
            if (!this.world.isNearOccupiedPointOfInterest(this.center)) {
                if (this.wavesSpawned > 0) {
                    this.status = Status.LOSS;
                } else {
                    this.invalidate();
                }
            }
            ++this.ticksActive;
            if (this.ticksActive >= 48000L) {
                this.invalidate();
                return;
            }
            int i = this.getRaiderCount();
            if (i == 0 && this.shouldSpawnMoreGroups()) {
                if (this.preRaidTicks > 0) {
                    bl2 = this.preCalculatedRavagerSpawnLocation.isPresent();
                    int n = bl3 = !bl2 && this.preRaidTicks % 5 == 0 ? 1 : 0;
                    if (bl2 && !this.world.method_37118(this.preCalculatedRavagerSpawnLocation.get())) {
                        bl3 = 1;
                    }
                    if (bl3 != 0) {
                        int j = 0;
                        if (this.preRaidTicks < 100) {
                            j = 1;
                        } else if (this.preRaidTicks < 40) {
                            j = 2;
                        }
                        this.preCalculatedRavagerSpawnLocation = this.preCalculateRavagerSpawnLocation(j);
                    }
                    if (this.preRaidTicks == 300 || this.preRaidTicks % 20 == 0) {
                        this.updateBarToPlayers();
                    }
                    --this.preRaidTicks;
                    this.bar.setPercent(MathHelper.clamp((float)(300 - this.preRaidTicks) / 300.0f, 0.0f, 1.0f));
                } else if (this.preRaidTicks == 0 && this.wavesSpawned > 0) {
                    this.preRaidTicks = 300;
                    this.bar.setName(EVENT_TEXT);
                    return;
                }
            }
            if (this.ticksActive % 20L == 0L) {
                this.updateBarToPlayers();
                this.removeObsoleteRaiders();
                if (i > 0) {
                    if (i <= 2) {
                        this.bar.setName(EVENT_TEXT.shallowCopy().append(" - ").append(new TranslatableText(RAIDERS_REMAINING_TRANSLATION_KEY, i)));
                    } else {
                        this.bar.setName(EVENT_TEXT);
                    }
                } else {
                    this.bar.setName(EVENT_TEXT);
                }
            }
            bl2 = false;
            bl3 = 0;
            while (this.canSpawnRaiders()) {
                BlockPos j;
                BlockPos blockPos = j = this.preCalculatedRavagerSpawnLocation.isPresent() ? this.preCalculatedRavagerSpawnLocation.get() : this.getRavagerSpawnLocation(bl3, 20);
                if (j != null) {
                    this.started = true;
                    this.spawnNextWave(j);
                    if (!bl2) {
                        this.playRaidHorn(j);
                        bl2 = true;
                    }
                } else {
                    ++bl3;
                }
                if (bl3 <= 3) continue;
                this.invalidate();
                break;
            }
            if (this.hasStarted() && !this.shouldSpawnMoreGroups() && i == 0) {
                if (this.postRaidTicks < 40) {
                    ++this.postRaidTicks;
                } else {
                    this.status = Status.VICTORY;
                    for (UUID uUID : this.heroesOfTheVillage) {
                        Entity entity = this.world.getEntity(uUID);
                        if (!(entity instanceof LivingEntity) || entity.isSpectator()) continue;
                        LivingEntity livingEntity = (LivingEntity)entity;
                        livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 48000, this.badOmenLevel - 1, false, false, true));
                        if (!(livingEntity instanceof ServerPlayerEntity)) continue;
                        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)livingEntity;
                        serverPlayerEntity.incrementStat(Stats.RAID_WIN);
                        Criteria.HERO_OF_THE_VILLAGE.trigger(serverPlayerEntity);
                    }
                }
            }
            this.markDirty();
        } else if (this.isFinished()) {
            ++this.finishCooldown;
            if (this.finishCooldown >= 600) {
                this.invalidate();
                return;
            }
            if (this.finishCooldown % 20 == 0) {
                this.updateBarToPlayers();
                this.bar.setVisible(true);
                if (this.hasWon()) {
                    this.bar.setPercent(0.0f);
                    this.bar.setName(VICTORY_TITLE);
                } else {
                    this.bar.setName(DEFEAT_TITLE);
                }
            }
        }
    }

    private void moveRaidCenter() {
        Stream<ChunkSectionPos> stream = ChunkSectionPos.stream(ChunkSectionPos.from(this.center), 2);
        stream.filter(this.world::isNearOccupiedPointOfInterest).map(ChunkSectionPos::getCenterPos).min(Comparator.comparingDouble(blockPos -> blockPos.getSquaredDistance(this.center))).ifPresent(this::setCenter);
    }

    private Optional<BlockPos> preCalculateRavagerSpawnLocation(int proximity) {
        for (int i = 0; i < 3; ++i) {
            BlockPos blockPos = this.getRavagerSpawnLocation(proximity, 1);
            if (blockPos == null) continue;
            return Optional.of(blockPos);
        }
        return Optional.empty();
    }

    private boolean shouldSpawnMoreGroups() {
        if (this.hasExtraWave()) {
            return !this.hasSpawnedExtraWave();
        }
        return !this.hasSpawnedFinalWave();
    }

    private boolean hasSpawnedFinalWave() {
        return this.getGroupsSpawned() == this.waveCount;
    }

    private boolean hasExtraWave() {
        return this.badOmenLevel > 1;
    }

    private boolean hasSpawnedExtraWave() {
        return this.getGroupsSpawned() > this.waveCount;
    }

    private boolean isSpawningExtraWave() {
        return this.hasSpawnedFinalWave() && this.getRaiderCount() == 0 && this.hasExtraWave();
    }

    private void removeObsoleteRaiders() {
        Iterator<Set<RaiderEntity>> iterator = this.waveToRaiders.values().iterator();
        HashSet<RaiderEntity> set = Sets.newHashSet();
        while (iterator.hasNext()) {
            Set<RaiderEntity> set2 = iterator.next();
            for (RaiderEntity raiderEntity : set2) {
                BlockPos blockPos = raiderEntity.getBlockPos();
                if (raiderEntity.isRemoved() || raiderEntity.world.getRegistryKey() != this.world.getRegistryKey() || this.center.getSquaredDistance(blockPos) >= 12544.0) {
                    set.add(raiderEntity);
                    continue;
                }
                if (raiderEntity.age <= 600) continue;
                if (this.world.getEntity(raiderEntity.getUuid()) == null) {
                    set.add(raiderEntity);
                }
                if (!this.world.isNearOccupiedPointOfInterest(blockPos) && raiderEntity.getDespawnCounter() > 2400) {
                    raiderEntity.setOutOfRaidCounter(raiderEntity.getOutOfRaidCounter() + 1);
                }
                if (raiderEntity.getOutOfRaidCounter() < 30) continue;
                set.add(raiderEntity);
            }
        }
        for (RaiderEntity raiderEntity2 : set) {
            this.removeFromWave(raiderEntity2, true);
        }
    }

    private void playRaidHorn(BlockPos pos) {
        float f = 13.0f;
        int i = 64;
        Collection<ServerPlayerEntity> collection = this.bar.getPlayers();
        for (ServerPlayerEntity serverPlayerEntity : this.world.getPlayers()) {
            Vec3d vec3d = serverPlayerEntity.getPos();
            Vec3d vec3d2 = Vec3d.ofCenter(pos);
            double d = Math.sqrt((vec3d2.x - vec3d.x) * (vec3d2.x - vec3d.x) + (vec3d2.z - vec3d.z) * (vec3d2.z - vec3d.z));
            double e = vec3d.x + 13.0 / d * (vec3d2.x - vec3d.x);
            double g = vec3d.z + 13.0 / d * (vec3d2.z - vec3d.z);
            if (!(d <= 64.0) && !collection.contains(serverPlayerEntity)) continue;
            serverPlayerEntity.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.EVENT_RAID_HORN, SoundCategory.NEUTRAL, e, serverPlayerEntity.getY(), g, 64.0f, 1.0f));
        }
    }

    private void spawnNextWave(BlockPos pos) {
        boolean bl = false;
        int i = this.wavesSpawned + 1;
        this.totalHealth = 0.0f;
        LocalDifficulty localDifficulty = this.world.getLocalDifficulty(pos);
        boolean bl2 = this.isSpawningExtraWave();
        for (Member member : Member.VALUES) {
            int j = this.getCount(member, i, bl2) + this.getBonusCount(member, this.random, i, localDifficulty, bl2);
            int k = 0;
            for (int l = 0; l < j; ++l) {
                RaiderEntity raiderEntity = member.type.create(this.world);
                if (!bl && raiderEntity.canLead()) {
                    raiderEntity.setPatrolLeader(true);
                    this.setWaveCaptain(i, raiderEntity);
                    bl = true;
                }
                this.addRaider(i, raiderEntity, pos, false);
                if (member.type != EntityType.RAVAGER) continue;
                RaiderEntity raiderEntity2 = null;
                if (i == this.getMaxWaves(Difficulty.NORMAL)) {
                    raiderEntity2 = EntityType.PILLAGER.create(this.world);
                } else if (i >= this.getMaxWaves(Difficulty.HARD)) {
                    raiderEntity2 = k == 0 ? (RaiderEntity)EntityType.EVOKER.create(this.world) : (RaiderEntity)EntityType.VINDICATOR.create(this.world);
                }
                ++k;
                if (raiderEntity2 == null) continue;
                this.addRaider(i, raiderEntity2, pos, false);
                raiderEntity2.refreshPositionAndAngles(pos, 0.0f, 0.0f);
                raiderEntity2.startRiding(raiderEntity);
            }
        }
        this.preCalculatedRavagerSpawnLocation = Optional.empty();
        ++this.wavesSpawned;
        this.updateBar();
        this.markDirty();
    }

    public void addRaider(int wave, RaiderEntity raider, @Nullable BlockPos pos, boolean existing) {
        boolean bl = this.addToWave(wave, raider);
        if (bl) {
            raider.setRaid(this);
            raider.setWave(wave);
            raider.setAbleToJoinRaid(true);
            raider.setOutOfRaidCounter(0);
            if (!existing && pos != null) {
                raider.setPosition((double)pos.getX() + 0.5, (double)pos.getY() + 1.0, (double)pos.getZ() + 0.5);
                raider.initialize(this.world, this.world.getLocalDifficulty(pos), SpawnReason.EVENT, null, null);
                raider.addBonusForWave(wave, false);
                raider.setOnGround(true);
                this.world.spawnEntityAndPassengers(raider);
            }
        }
    }

    public void updateBar() {
        this.bar.setPercent(MathHelper.clamp(this.getCurrentRaiderHealth() / this.totalHealth, 0.0f, 1.0f));
    }

    public float getCurrentRaiderHealth() {
        float f = 0.0f;
        for (Set<RaiderEntity> set : this.waveToRaiders.values()) {
            for (RaiderEntity raiderEntity : set) {
                f += raiderEntity.getHealth();
            }
        }
        return f;
    }

    private boolean canSpawnRaiders() {
        return this.preRaidTicks == 0 && (this.wavesSpawned < this.waveCount || this.isSpawningExtraWave()) && this.getRaiderCount() == 0;
    }

    public int getRaiderCount() {
        return this.waveToRaiders.values().stream().mapToInt(Set::size).sum();
    }

    public void removeFromWave(RaiderEntity entity, boolean countHealth) {
        boolean bl;
        Set<RaiderEntity> set = this.waveToRaiders.get(entity.getWave());
        if (set != null && (bl = set.remove(entity))) {
            if (countHealth) {
                this.totalHealth -= entity.getHealth();
            }
            entity.setRaid(null);
            this.updateBar();
            this.markDirty();
        }
    }

    private void markDirty() {
        this.world.getRaidManager().markDirty();
    }

    public static ItemStack getOminousBanner() {
        ItemStack itemStack = new ItemStack(Items.WHITE_BANNER);
        NbtCompound nbtCompound = itemStack.getOrCreateSubNbt("BlockEntityTag");
        NbtList nbtList = new BannerPattern.Patterns().add(BannerPattern.RHOMBUS_MIDDLE, DyeColor.CYAN).add(BannerPattern.STRIPE_BOTTOM, DyeColor.LIGHT_GRAY).add(BannerPattern.STRIPE_CENTER, DyeColor.GRAY).add(BannerPattern.BORDER, DyeColor.LIGHT_GRAY).add(BannerPattern.STRIPE_MIDDLE, DyeColor.BLACK).add(BannerPattern.HALF_HORIZONTAL, DyeColor.LIGHT_GRAY).add(BannerPattern.CIRCLE_MIDDLE, DyeColor.LIGHT_GRAY).add(BannerPattern.BORDER, DyeColor.BLACK).toNbt();
        nbtCompound.put("Patterns", nbtList);
        itemStack.addHideFlag(ItemStack.TooltipSection.ADDITIONAL);
        itemStack.setCustomName(new TranslatableText(OMINOUS_BANNER_TRANSLATION_KEY).formatted(Formatting.GOLD));
        return itemStack;
    }

    @Nullable
    public RaiderEntity getCaptain(int wave) {
        return this.waveToCaptain.get(wave);
    }

    @Nullable
    private BlockPos getRavagerSpawnLocation(int proximity, int tries) {
        int i = proximity == 0 ? 2 : 2 - proximity;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int j = 0; j < tries; ++j) {
            float f = this.world.random.nextFloat() * ((float)Math.PI * 2);
            int k = this.center.getX() + MathHelper.floor(MathHelper.cos(f) * 32.0f * (float)i) + this.world.random.nextInt(5);
            int l = this.center.getZ() + MathHelper.floor(MathHelper.sin(f) * 32.0f * (float)i) + this.world.random.nextInt(5);
            int m = this.world.getTopY(Heightmap.Type.WORLD_SURFACE, k, l);
            mutable.set(k, m, l);
            if (this.world.isNearOccupiedPointOfInterest(mutable) && proximity < 2) continue;
            int n = 10;
            if (!this.world.isRegionLoaded(mutable.getX() - 10, mutable.getZ() - 10, mutable.getX() + 10, mutable.getZ() + 10) || !this.world.method_37118(mutable) || !SpawnHelper.canSpawn(SpawnRestriction.Location.ON_GROUND, this.world, mutable, EntityType.RAVAGER) && (!this.world.getBlockState((BlockPos)mutable.down()).isOf(Blocks.SNOW) || !this.world.getBlockState(mutable).isAir())) continue;
            return mutable;
        }
        return null;
    }

    private boolean addToWave(int wave, RaiderEntity entity) {
        return this.addToWave(wave, entity, true);
    }

    public boolean addToWave(int wave2, RaiderEntity entity, boolean countHealth) {
        this.waveToRaiders.computeIfAbsent(wave2, wave -> Sets.newHashSet());
        Set<RaiderEntity> set = this.waveToRaiders.get(wave2);
        RaiderEntity raiderEntity = null;
        for (RaiderEntity raiderEntity2 : set) {
            if (!raiderEntity2.getUuid().equals(entity.getUuid())) continue;
            raiderEntity = raiderEntity2;
            break;
        }
        if (raiderEntity != null) {
            set.remove(raiderEntity);
            set.add(entity);
        }
        set.add(entity);
        if (countHealth) {
            this.totalHealth += entity.getHealth();
        }
        this.updateBar();
        this.markDirty();
        return true;
    }

    public void setWaveCaptain(int wave, RaiderEntity entity) {
        this.waveToCaptain.put(wave, entity);
        entity.equipStack(EquipmentSlot.HEAD, Raid.getOminousBanner());
        entity.setEquipmentDropChance(EquipmentSlot.HEAD, 2.0f);
    }

    public void removeLeader(int wave) {
        this.waveToCaptain.remove(wave);
    }

    public BlockPos getCenter() {
        return this.center;
    }

    private void setCenter(BlockPos center) {
        this.center = center;
    }

    public int getRaidId() {
        return this.id;
    }

    private int getCount(Member member, int wave, boolean extra) {
        return extra ? member.countInWave[this.waveCount] : member.countInWave[wave];
    }

    private int getBonusCount(Member member, Random random, int wave, LocalDifficulty localDifficulty, boolean extra) {
        int i;
        Difficulty difficulty = localDifficulty.getGlobalDifficulty();
        boolean bl = difficulty == Difficulty.EASY;
        boolean bl2 = difficulty == Difficulty.NORMAL;
        switch (member) {
            case WITCH: {
                if (!bl && wave > 2 && wave != 4) {
                    i = 1;
                    break;
                }
                return 0;
            }
            case PILLAGER: 
            case VINDICATOR: {
                if (bl) {
                    i = random.nextInt(2);
                    break;
                }
                if (bl2) {
                    i = 1;
                    break;
                }
                i = 2;
                break;
            }
            case RAVAGER: {
                i = !bl && extra ? 1 : 0;
                break;
            }
            default: {
                return 0;
            }
        }
        return i > 0 ? random.nextInt(i + 1) : 0;
    }

    public boolean isActive() {
        return this.active;
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("Id", this.id);
        nbt.putBoolean("Started", this.started);
        nbt.putBoolean("Active", this.active);
        nbt.putLong("TicksActive", this.ticksActive);
        nbt.putInt("BadOmenLevel", this.badOmenLevel);
        nbt.putInt("GroupsSpawned", this.wavesSpawned);
        nbt.putInt("PreRaidTicks", this.preRaidTicks);
        nbt.putInt("PostRaidTicks", this.postRaidTicks);
        nbt.putFloat("TotalHealth", this.totalHealth);
        nbt.putInt("NumGroups", this.waveCount);
        nbt.putString("Status", this.status.getName());
        nbt.putInt("CX", this.center.getX());
        nbt.putInt("CY", this.center.getY());
        nbt.putInt("CZ", this.center.getZ());
        NbtList nbtList = new NbtList();
        for (UUID uUID : this.heroesOfTheVillage) {
            nbtList.add(NbtHelper.fromUuid(uUID));
        }
        nbt.put("HeroesOfTheVillage", nbtList);
        return nbt;
    }

    public int getMaxWaves(Difficulty difficulty) {
        switch (difficulty) {
            case EASY: {
                return 3;
            }
            case NORMAL: {
                return 5;
            }
            case HARD: {
                return 7;
            }
        }
        return 0;
    }

    public float getEnchantmentChance() {
        int i = this.getBadOmenLevel();
        if (i == 2) {
            return 0.1f;
        }
        if (i == 3) {
            return 0.25f;
        }
        if (i == 4) {
            return 0.5f;
        }
        if (i == 5) {
            return 0.75f;
        }
        return 0.0f;
    }

    public void addHero(Entity entity) {
        this.heroesOfTheVillage.add(entity.getUuid());
    }

    static enum Status {
        ONGOING,
        VICTORY,
        LOSS,
        STOPPED;

        private static final Status[] VALUES;

        static Status fromName(String name) {
            for (Status status : VALUES) {
                if (!name.equalsIgnoreCase(status.name())) continue;
                return status;
            }
            return ONGOING;
        }

        public String getName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        static {
            VALUES = Status.values();
        }
    }

    static enum Member {
        VINDICATOR(EntityType.VINDICATOR, new int[]{0, 0, 2, 0, 1, 4, 2, 5}),
        EVOKER(EntityType.EVOKER, new int[]{0, 0, 0, 0, 0, 1, 1, 2}),
        PILLAGER(EntityType.PILLAGER, new int[]{0, 4, 3, 3, 4, 4, 4, 2}),
        WITCH(EntityType.WITCH, new int[]{0, 0, 0, 0, 3, 0, 0, 1}),
        RAVAGER(EntityType.RAVAGER, new int[]{0, 0, 0, 1, 0, 1, 0, 2});

        static final Member[] VALUES;
        final EntityType<? extends RaiderEntity> type;
        final int[] countInWave;

        private Member(EntityType<? extends RaiderEntity> type, int[] countInWave) {
            this.type = type;
            this.countInWave = countInWave;
        }

        static {
            VALUES = Member.values();
        }
    }
}

