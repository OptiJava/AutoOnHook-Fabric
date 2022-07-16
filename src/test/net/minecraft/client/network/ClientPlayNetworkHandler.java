/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.DemoScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.StatsListener;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.ingame.CommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.DataQueryHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.realms.gui.screen.DisconnectedRealmsScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.debug.BeeDebugRenderer;
import net.minecraft.client.render.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.render.debug.NeighborUpdateDebugRenderer;
import net.minecraft.client.render.debug.VillageDebugRenderer;
import net.minecraft.client.render.debug.WorldGenAttemptDebugRenderer;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.search.SearchableContainer;
import net.minecraft.client.sound.AbstractBeeSoundInstance;
import net.minecraft.client.sound.AggressiveBeeSoundInstance;
import net.minecraft.client.sound.GuardianAttackSoundInstance;
import net.minecraft.client.sound.MovingMinecartSoundInstance;
import net.minecraft.client.sound.PassiveBeeSoundInstance;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.toast.RecipeToast;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayPongC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkRenderDistanceCenterS2CPacket;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CraftFailedResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.EndCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceOrbSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.LookAtS2CPacket;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.NbtQueryResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenHorseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenWrittenBookS2CPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PaintingSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayPingS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerActionResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SelectAdvancementTabS2CPacket;
import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.network.packet.s2c.play.UnlockRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.network.packet.s2c.play.VibrationS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderCenterChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInitializeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInterpolateSizeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderSizeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningBlocksChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningTimeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.VibrationParticleEffect;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.tag.RequiredTagListRegistry;
import net.minecraft.tag.TagManager;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.PositionImpl;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.LightType;
import net.minecraft.world.Vibration;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.explosion.Explosion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ClientPlayNetworkHandler
implements ClientPlayPacketListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Text DISCONNECT_LOST_TEXT = new TranslatableText("disconnect.lost");
    private final ClientConnection connection;
    private final GameProfile profile;
    private final Screen loginScreen;
    private final MinecraftClient client;
    private ClientWorld world;
    private ClientWorld.Properties worldProperties;
    private boolean positionLookSetup;
    private final Map<UUID, PlayerListEntry> playerListEntries = Maps.newHashMap();
    private final ClientAdvancementManager advancementHandler;
    private final ClientCommandSource commandSource;
    private TagManager tagManager = TagManager.EMPTY;
    private final DataQueryHandler dataQueryHandler = new DataQueryHandler(this);
    private int chunkLoadDistance = 3;
    private final Random random = new Random();
    private CommandDispatcher<CommandSource> commandDispatcher = new CommandDispatcher();
    private final RecipeManager recipeManager = new RecipeManager();
    private final UUID sessionId = UUID.randomUUID();
    private Set<RegistryKey<World>> worldKeys;
    private DynamicRegistryManager registryManager = DynamicRegistryManager.create();

    public ClientPlayNetworkHandler(MinecraftClient client, Screen screen, ClientConnection connection, GameProfile profile) {
        this.client = client;
        this.loginScreen = screen;
        this.connection = connection;
        this.profile = profile;
        this.advancementHandler = new ClientAdvancementManager(client);
        this.commandSource = new ClientCommandSource(this, client);
    }

    public ClientCommandSource getCommandSource() {
        return this.commandSource;
    }

    public void clearWorld() {
        this.world = null;
    }

    public RecipeManager getRecipeManager() {
        return this.recipeManager;
    }

    @Override
    public void onGameJoin(GameJoinS2CPacket packet) {
        ClientWorld.Properties properties;
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.interactionManager = new ClientPlayerInteractionManager(this.client, this);
        if (!this.connection.isLocal()) {
            RequiredTagListRegistry.clearAllTags();
        }
        ArrayList<RegistryKey<World>> list = Lists.newArrayList(packet.getDimensionIds());
        Collections.shuffle(list);
        this.worldKeys = Sets.newLinkedHashSet(list);
        this.registryManager = packet.getRegistryManager();
        RegistryKey<World> registryKey = packet.getDimensionId();
        DimensionType dimensionType = packet.getDimensionType();
        this.chunkLoadDistance = packet.getViewDistance();
        boolean bl = packet.isDebugWorld();
        boolean bl2 = packet.isFlatWorld();
        this.worldProperties = properties = new ClientWorld.Properties(Difficulty.NORMAL, packet.isHardcore(), bl2);
        this.world = new ClientWorld(this, properties, registryKey, dimensionType, this.chunkLoadDistance, this.client::getProfiler, this.client.worldRenderer, bl, packet.getSha256Seed());
        this.client.joinWorld(this.world);
        if (this.client.player == null) {
            this.client.player = this.client.interactionManager.createPlayer(this.world, new StatHandler(), new ClientRecipeBook());
            this.client.player.setYaw(-180.0f);
            if (this.client.getServer() != null) {
                this.client.getServer().setLocalPlayerUuid(this.client.player.getUuid());
            }
        }
        this.client.debugRenderer.reset();
        this.client.player.init();
        int i = packet.getEntityId();
        this.client.player.setId(i);
        this.world.addPlayer(i, this.client.player);
        this.client.player.input = new KeyboardInput(this.client.options);
        this.client.interactionManager.copyAbilities(this.client.player);
        this.client.cameraEntity = this.client.player;
        this.client.setScreen(new DownloadingTerrainScreen());
        this.client.player.setReducedDebugInfo(packet.hasReducedDebugInfo());
        this.client.player.setShowsDeathScreen(packet.showsDeathScreen());
        this.client.interactionManager.setGameModes(packet.getGameMode(), packet.getPreviousGameMode());
        this.client.options.sendClientSettings();
        this.connection.send(new CustomPayloadC2SPacket(CustomPayloadC2SPacket.BRAND, new PacketByteBuf(Unpooled.buffer()).writeString(ClientBrandRetriever.getClientModName())));
        this.client.getGame().onStartGameSession();
    }

    @Override
    public void onEntitySpawn(EntitySpawnS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        EntityType<?> entityType = packet.getEntityTypeId();
        Object entity = entityType.create(this.world);
        if (entity != null) {
            ((Entity)entity).onSpawnPacket(packet);
            int i = packet.getId();
            this.world.addEntity(i, (Entity)entity);
            if (entity instanceof AbstractMinecartEntity) {
                this.client.getSoundManager().play(new MovingMinecartSoundInstance((AbstractMinecartEntity)entity));
            }
        }
    }

    @Override
    public void onExperienceOrbSpawn(ExperienceOrbSpawnS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        double d = packet.getX();
        double e = packet.getY();
        double f = packet.getZ();
        ExperienceOrbEntity entity = new ExperienceOrbEntity(this.world, d, e, f, packet.getExperience());
        entity.updateTrackedPosition(d, e, f);
        entity.setYaw(0.0f);
        entity.setPitch(0.0f);
        entity.setId(packet.getId());
        this.world.addEntity(packet.getId(), entity);
    }

    @Override
    public void onVibration(VibrationS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Vibration vibration = packet.getVibration();
        BlockPos blockPos = vibration.getOrigin();
        this.world.addImportantParticle(new VibrationParticleEffect(vibration), true, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 0.0, 0.0, 0.0);
    }

    @Override
    public void onPaintingSpawn(PaintingSpawnS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        PaintingEntity paintingEntity = new PaintingEntity(this.world, packet.getPos(), packet.getFacing(), packet.getMotive());
        paintingEntity.setId(packet.getId());
        paintingEntity.setUuid(packet.getPaintingUuid());
        this.world.addEntity(packet.getId(), paintingEntity);
    }

    @Override
    public void onVelocityUpdate(EntityVelocityUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity entity = this.world.getEntityById(packet.getId());
        if (entity == null) {
            return;
        }
        entity.setVelocityClient((double)packet.getVelocityX() / 8000.0, (double)packet.getVelocityY() / 8000.0, (double)packet.getVelocityZ() / 8000.0);
    }

    @Override
    public void onEntityTrackerUpdate(EntityTrackerUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity entity = this.world.getEntityById(packet.id());
        if (entity != null && packet.getTrackedValues() != null) {
            entity.getDataTracker().writeUpdatedEntries(packet.getTrackedValues());
        }
    }

    @Override
    public void onPlayerSpawn(PlayerSpawnS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        double d = packet.getX();
        double e = packet.getY();
        double f = packet.getZ();
        float g = (float)(packet.getYaw() * 360) / 256.0f;
        float h = (float)(packet.getPitch() * 360) / 256.0f;
        int i = packet.getId();
        OtherClientPlayerEntity otherClientPlayerEntity = new OtherClientPlayerEntity(this.client.world, this.getPlayerListEntry(packet.getPlayerUuid()).getProfile());
        otherClientPlayerEntity.setId(i);
        otherClientPlayerEntity.updateTrackedPosition(d, e, f);
        otherClientPlayerEntity.updatePositionAndAngles(d, e, f, g, h);
        otherClientPlayerEntity.resetPosition();
        this.world.addPlayer(i, otherClientPlayerEntity);
    }

    @Override
    public void onEntityPosition(EntityPositionS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity entity = this.world.getEntityById(packet.getId());
        if (entity == null) {
            return;
        }
        double d = packet.getX();
        double e = packet.getY();
        double f = packet.getZ();
        entity.updateTrackedPosition(d, e, f);
        if (!entity.isLogicalSideForUpdatingMovement()) {
            float g = (float)(packet.getYaw() * 360) / 256.0f;
            float h = (float)(packet.getPitch() * 360) / 256.0f;
            entity.updateTrackedPositionAndAngles(d, e, f, g, h, 3, true);
            entity.setOnGround(packet.isOnGround());
        }
    }

    @Override
    public void onHeldItemChange(UpdateSelectedSlotS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        if (PlayerInventory.isValidHotbarIndex(packet.getSlot())) {
            this.client.player.getInventory().selectedSlot = packet.getSlot();
        }
    }

    @Override
    public void onEntityUpdate(EntityS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity entity = packet.getEntity(this.world);
        if (entity == null) {
            return;
        }
        if (!entity.isLogicalSideForUpdatingMovement()) {
            if (packet.isPositionChanged()) {
                Vec3d vec3d = packet.calculateDeltaPosition(entity.getTrackedPosition());
                entity.updateTrackedPosition(vec3d);
                float f = packet.hasRotation() ? (float)(packet.getYaw() * 360) / 256.0f : entity.getYaw();
                float g = packet.hasRotation() ? (float)(packet.getPitch() * 360) / 256.0f : entity.getPitch();
                entity.updateTrackedPositionAndAngles(vec3d.getX(), vec3d.getY(), vec3d.getZ(), f, g, 3, false);
            } else if (packet.hasRotation()) {
                float vec3d = (float)(packet.getYaw() * 360) / 256.0f;
                float f = (float)(packet.getPitch() * 360) / 256.0f;
                entity.updateTrackedPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), vec3d, f, 3, false);
            }
            entity.setOnGround(packet.isOnGround());
        }
    }

    @Override
    public void onEntitySetHeadYaw(EntitySetHeadYawS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity entity = packet.getEntity(this.world);
        if (entity == null) {
            return;
        }
        float f = (float)(packet.getHeadYaw() * 360) / 256.0f;
        entity.updateTrackedHeadRotation(f, 3);
    }

    @Override
    public void onEntitiesDestroy(EntitiesDestroyS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        packet.getEntityIds().forEach(entityId -> this.world.removeEntity(entityId, Entity.RemovalReason.DISCARDED));
    }

    @Override
    public void onPlayerPositionLook(PlayerPositionLookS2CPacket packet) {
        double i;
        double h;
        double g;
        double f;
        double e;
        double d;
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ClientPlayerEntity playerEntity = this.client.player;
        if (packet.shouldDismount()) {
            ((PlayerEntity)playerEntity).dismountVehicle();
        }
        Vec3d vec3d = playerEntity.getVelocity();
        boolean bl = packet.getFlags().contains((Object)PlayerPositionLookS2CPacket.Flag.X);
        boolean bl2 = packet.getFlags().contains((Object)PlayerPositionLookS2CPacket.Flag.Y);
        boolean bl3 = packet.getFlags().contains((Object)PlayerPositionLookS2CPacket.Flag.Z);
        if (bl) {
            d = vec3d.getX();
            e = playerEntity.getX() + packet.getX();
            playerEntity.lastRenderX += packet.getX();
        } else {
            d = 0.0;
            playerEntity.lastRenderX = e = packet.getX();
        }
        if (bl2) {
            f = vec3d.getY();
            g = playerEntity.getY() + packet.getY();
            playerEntity.lastRenderY += packet.getY();
        } else {
            f = 0.0;
            playerEntity.lastRenderY = g = packet.getY();
        }
        if (bl3) {
            h = vec3d.getZ();
            i = playerEntity.getZ() + packet.getZ();
            playerEntity.lastRenderZ += packet.getZ();
        } else {
            h = 0.0;
            playerEntity.lastRenderZ = i = packet.getZ();
        }
        playerEntity.setPos(e, g, i);
        playerEntity.prevX = e;
        playerEntity.prevY = g;
        playerEntity.prevZ = i;
        playerEntity.setVelocity(d, f, h);
        float j = packet.getYaw();
        float k = packet.getPitch();
        if (packet.getFlags().contains((Object)PlayerPositionLookS2CPacket.Flag.X_ROT)) {
            k += playerEntity.getPitch();
        }
        if (packet.getFlags().contains((Object)PlayerPositionLookS2CPacket.Flag.Y_ROT)) {
            j += playerEntity.getYaw();
        }
        playerEntity.updatePositionAndAngles(e, g, i, j, k);
        this.connection.send(new TeleportConfirmC2SPacket(packet.getTeleportId()));
        this.connection.send(new PlayerMoveC2SPacket.Full(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), playerEntity.getYaw(), playerEntity.getPitch(), false));
        if (!this.positionLookSetup) {
            this.positionLookSetup = true;
            this.client.setScreen(null);
        }
    }

    @Override
    public void onChunkDeltaUpdate(ChunkDeltaUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        int i = Block.NOTIFY_ALL | Block.FORCE_STATE | (packet.shouldSkipLightingUpdates() ? Block.SKIP_LIGHTING_UPDATES : 0);
        packet.visitUpdates((blockPos, blockState) -> this.world.setBlockState((BlockPos)blockPos, (BlockState)blockState, i));
    }

    @Override
    public void onChunkData(ChunkDataS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        int i = packet.getX();
        int j = packet.getZ();
        BiomeArray biomeArray = new BiomeArray(this.registryManager.get(Registry.BIOME_KEY), (HeightLimitView)this.world, packet.getBiomeArray());
        WorldChunk worldChunk = this.world.getChunkManager().loadChunkFromPacket(i, j, biomeArray, packet.getReadBuffer(), packet.getHeightmaps(), packet.getVerticalStripBitmask());
        for (int k = this.world.getBottomSectionCoord(); k < this.world.getTopSectionCoord(); ++k) {
            this.world.scheduleBlockRenders(i, k, j);
        }
        if (worldChunk != null) {
            for (NbtCompound nbtCompound : packet.getBlockEntityTagList()) {
                BlockPos blockPos = new BlockPos(nbtCompound.getInt("x"), nbtCompound.getInt("y"), nbtCompound.getInt("z"));
                BlockEntity blockEntity = worldChunk.getBlockEntity(blockPos, WorldChunk.CreationType.IMMEDIATE);
                if (blockEntity == null) continue;
                blockEntity.readNbt(nbtCompound);
            }
        }
    }

    @Override
    public void onUnloadChunk(UnloadChunkS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        int i = packet.getX();
        int j = packet.getZ();
        ClientChunkManager clientChunkManager = this.world.getChunkManager();
        clientChunkManager.unload(i, j);
        LightingProvider lightingProvider = clientChunkManager.getLightingProvider();
        for (int k = this.world.getBottomSectionCoord(); k < this.world.getTopSectionCoord(); ++k) {
            this.world.scheduleBlockRenders(i, k, j);
            lightingProvider.setSectionStatus(ChunkSectionPos.from(i, k, j), true);
        }
        lightingProvider.setColumnEnabled(new ChunkPos(i, j), false);
    }

    @Override
    public void onBlockUpdate(BlockUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.world.setBlockStateWithoutNeighborUpdates(packet.getPos(), packet.getState());
    }

    @Override
    public void onDisconnect(DisconnectS2CPacket packet) {
        this.connection.disconnect(packet.getReason());
    }

    @Override
    public void onDisconnected(Text reason) {
        this.client.disconnect();
        if (this.loginScreen != null) {
            if (this.loginScreen instanceof RealmsScreen) {
                this.client.setScreen(new DisconnectedRealmsScreen(this.loginScreen, DISCONNECT_LOST_TEXT, reason));
            } else {
                this.client.setScreen(new DisconnectedScreen(this.loginScreen, DISCONNECT_LOST_TEXT, reason));
            }
        } else {
            this.client.setScreen(new DisconnectedScreen(new MultiplayerScreen(new TitleScreen()), DISCONNECT_LOST_TEXT, reason));
        }
    }

    /**
     * Sends a packet to the server.
     * 
     * @param packet the packet to send
     */
    public void sendPacket(Packet<?> packet) {
        this.connection.send(packet);
    }

    @Override
    public void onItemPickupAnimation(ItemPickupAnimationS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity entity = this.world.getEntityById(packet.getEntityId());
        LivingEntity livingEntity = (LivingEntity)this.world.getEntityById(packet.getCollectorEntityId());
        if (livingEntity == null) {
            livingEntity = this.client.player;
        }
        if (entity != null) {
            if (entity instanceof ExperienceOrbEntity) {
                this.world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.1f, (this.random.nextFloat() - this.random.nextFloat()) * 0.35f + 0.9f, false);
            } else {
                this.world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f, (this.random.nextFloat() - this.random.nextFloat()) * 1.4f + 2.0f, false);
            }
            this.client.particleManager.addParticle(new ItemPickupParticle(this.client.getEntityRenderDispatcher(), this.client.getBufferBuilders(), this.world, entity, livingEntity));
            if (entity instanceof ItemEntity) {
                ItemEntity itemEntity = (ItemEntity)entity;
                ItemStack itemStack = itemEntity.getStack();
                itemStack.decrement(packet.getStackAmount());
                if (itemStack.isEmpty()) {
                    this.world.removeEntity(packet.getEntityId(), Entity.RemovalReason.DISCARDED);
                }
            } else if (!(entity instanceof ExperienceOrbEntity)) {
                this.world.removeEntity(packet.getEntityId(), Entity.RemovalReason.DISCARDED);
            }
        }
    }

    @Override
    public void onGameMessage(GameMessageS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.inGameHud.addChatMessage(packet.getLocation(), packet.getMessage(), packet.getSender());
    }

    @Override
    public void onEntityAnimation(EntityAnimationS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity entity = this.world.getEntityById(packet.getId());
        if (entity == null) {
            return;
        }
        if (packet.getAnimationId() == 0) {
            LivingEntity livingEntity = (LivingEntity)entity;
            livingEntity.swingHand(Hand.MAIN_HAND);
        } else if (packet.getAnimationId() == EntityAnimationS2CPacket.SWING_OFF_HAND) {
            LivingEntity livingEntity = (LivingEntity)entity;
            livingEntity.swingHand(Hand.OFF_HAND);
        } else if (packet.getAnimationId() == EntityAnimationS2CPacket.DAMAGE) {
            entity.animateDamage();
        } else if (packet.getAnimationId() == EntityAnimationS2CPacket.WAKE_UP) {
            PlayerEntity livingEntity = (PlayerEntity)entity;
            livingEntity.wakeUp(false, false);
        } else if (packet.getAnimationId() == EntityAnimationS2CPacket.CRIT) {
            this.client.particleManager.addEmitter(entity, ParticleTypes.CRIT);
        } else if (packet.getAnimationId() == EntityAnimationS2CPacket.ENCHANTED_HIT) {
            this.client.particleManager.addEmitter(entity, ParticleTypes.ENCHANTED_HIT);
        }
    }

    @Override
    public void onMobSpawn(MobSpawnS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        LivingEntity livingEntity = (LivingEntity)EntityType.createInstanceFromId(packet.getEntityTypeId(), this.world);
        if (livingEntity != null) {
            livingEntity.readFromPacket(packet);
            this.world.addEntity(packet.getId(), livingEntity);
            if (livingEntity instanceof BeeEntity) {
                boolean bl = ((BeeEntity)livingEntity).hasAngerTime();
                AbstractBeeSoundInstance abstractBeeSoundInstance = bl ? new AggressiveBeeSoundInstance((BeeEntity)livingEntity) : new PassiveBeeSoundInstance((BeeEntity)livingEntity);
                this.client.getSoundManager().playNextTick(abstractBeeSoundInstance);
            }
        } else {
            LOGGER.warn("Skipping Entity with id {}", (Object)packet.getEntityTypeId());
        }
    }

    @Override
    public void onWorldTimeUpdate(WorldTimeUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.world.setTime(packet.getTime());
        this.client.world.setTimeOfDay(packet.getTimeOfDay());
    }

    @Override
    public void onPlayerSpawnPosition(PlayerSpawnPositionS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.world.setSpawnPos(packet.getPos(), packet.getAngle());
    }

    @Override
    public void onEntityPassengersSet(EntityPassengersSetS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity entity = this.world.getEntityById(packet.getId());
        if (entity == null) {
            LOGGER.warn("Received passengers for unknown entity");
            return;
        }
        boolean bl = entity.hasPassengerDeep(this.client.player);
        entity.removeAllPassengers();
        for (int i : packet.getPassengerIds()) {
            Entity entity2 = this.world.getEntityById(i);
            if (entity2 == null) continue;
            entity2.startRiding(entity, true);
            if (entity2 != this.client.player || bl) continue;
            this.client.inGameHud.setOverlayMessage(new TranslatableText("mount.onboard", this.client.options.keySneak.getBoundKeyLocalizedText()), false);
        }
    }

    @Override
    public void onEntityAttach(EntityAttachS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity entity = this.world.getEntityById(packet.getAttachedEntityId());
        if (entity instanceof MobEntity) {
            ((MobEntity)entity).setHoldingEntityId(packet.getHoldingEntityId());
        }
    }

    private static ItemStack getActiveTotemOfUndying(PlayerEntity player) {
        for (Hand hand : Hand.values()) {
            ItemStack itemStack = player.getStackInHand(hand);
            if (!itemStack.isOf(Items.TOTEM_OF_UNDYING)) continue;
            return itemStack;
        }
        return new ItemStack(Items.TOTEM_OF_UNDYING);
    }

    @Override
    public void onEntityStatus(EntityStatusS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity entity = packet.getEntity(this.world);
        if (entity != null) {
            if (packet.getStatus() == 21) {
                this.client.getSoundManager().play(new GuardianAttackSoundInstance((GuardianEntity)entity));
            } else if (packet.getStatus() == 35) {
                int i = 40;
                this.client.particleManager.addEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
                this.world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_TOTEM_USE, entity.getSoundCategory(), 1.0f, 1.0f, false);
                if (entity == this.client.player) {
                    this.client.gameRenderer.showFloatingItem(ClientPlayNetworkHandler.getActiveTotemOfUndying(this.client.player));
                }
            } else {
                entity.handleStatus(packet.getStatus());
            }
        }
    }

    @Override
    public void onHealthUpdate(HealthUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.player.updateHealth(packet.getHealth());
        this.client.player.getHungerManager().setFoodLevel(packet.getFood());
        this.client.player.getHungerManager().setSaturationLevel(packet.getSaturation());
    }

    @Override
    public void onExperienceBarUpdate(ExperienceBarUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.player.setExperience(packet.getBarProgress(), packet.getExperienceLevel(), packet.getExperience());
    }

    @Override
    public void onPlayerRespawn(PlayerRespawnS2CPacket packet) {
        Object map;
        Object scoreboard;
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        RegistryKey<World> registryKey = packet.getDimension();
        DimensionType dimensionType = packet.getDimensionType();
        ClientPlayerEntity clientPlayerEntity = this.client.player;
        int i = clientPlayerEntity.getId();
        this.positionLookSetup = false;
        if (registryKey != clientPlayerEntity.world.getRegistryKey()) {
            ClientWorld.Properties properties;
            scoreboard = this.world.getScoreboard();
            map = this.world.getMapStates();
            boolean bl = packet.isDebugWorld();
            boolean bl2 = packet.isFlatWorld();
            this.worldProperties = properties = new ClientWorld.Properties(this.worldProperties.getDifficulty(), this.worldProperties.isHardcore(), bl2);
            this.world = new ClientWorld(this, properties, registryKey, dimensionType, this.chunkLoadDistance, this.client::getProfiler, this.client.worldRenderer, bl, packet.getSha256Seed());
            this.world.setScoreboard((Scoreboard)scoreboard);
            this.world.putMapStates((Map<String, MapState>)map);
            this.client.joinWorld(this.world);
            this.client.setScreen(new DownloadingTerrainScreen());
        }
        scoreboard = clientPlayerEntity.getServerBrand();
        this.client.cameraEntity = null;
        map = this.client.interactionManager.createPlayer(this.world, clientPlayerEntity.getStatHandler(), clientPlayerEntity.getRecipeBook(), clientPlayerEntity.isSneaking(), clientPlayerEntity.isSprinting());
        ((Entity)map).setId(i);
        this.client.player = map;
        if (registryKey != clientPlayerEntity.world.getRegistryKey()) {
            this.client.getMusicTracker().stop();
        }
        this.client.cameraEntity = map;
        ((Entity)map).getDataTracker().writeUpdatedEntries(clientPlayerEntity.getDataTracker().getAllEntries());
        if (packet.shouldKeepPlayerAttributes()) {
            ((LivingEntity)map).getAttributes().setFrom(clientPlayerEntity.getAttributes());
        }
        ((ClientPlayerEntity)map).init();
        ((ClientPlayerEntity)map).setServerBrand((String)scoreboard);
        this.world.addPlayer(i, (AbstractClientPlayerEntity)map);
        ((Entity)map).setYaw(-180.0f);
        ((ClientPlayerEntity)map).input = new KeyboardInput(this.client.options);
        this.client.interactionManager.copyAbilities((PlayerEntity)map);
        ((PlayerEntity)map).setReducedDebugInfo(clientPlayerEntity.hasReducedDebugInfo());
        ((ClientPlayerEntity)map).setShowsDeathScreen(clientPlayerEntity.showsDeathScreen());
        if (this.client.currentScreen instanceof DeathScreen) {
            this.client.setScreen(null);
        }
        this.client.interactionManager.setGameModes(packet.getGameMode(), packet.getPreviousGameMode());
    }

    @Override
    public void onExplosion(ExplosionS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Explosion explosion = new Explosion(this.client.world, null, packet.getX(), packet.getY(), packet.getZ(), packet.getRadius(), packet.getAffectedBlocks());
        explosion.affectWorld(true);
        this.client.player.setVelocity(this.client.player.getVelocity().add(packet.getPlayerVelocityX(), packet.getPlayerVelocityY(), packet.getPlayerVelocityZ()));
    }

    @Override
    public void onOpenHorseScreen(OpenHorseScreenS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity entity = this.world.getEntityById(packet.getHorseId());
        if (entity instanceof HorseBaseEntity) {
            ClientPlayerEntity clientPlayerEntity = this.client.player;
            HorseBaseEntity horseBaseEntity = (HorseBaseEntity)entity;
            SimpleInventory simpleInventory = new SimpleInventory(packet.getSlotCount());
            HorseScreenHandler horseScreenHandler = new HorseScreenHandler(packet.getSyncId(), clientPlayerEntity.getInventory(), simpleInventory, horseBaseEntity);
            clientPlayerEntity.currentScreenHandler = horseScreenHandler;
            this.client.setScreen(new HorseScreen(horseScreenHandler, clientPlayerEntity.getInventory(), horseBaseEntity));
        }
    }

    @Override
    public void onOpenScreen(OpenScreenS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        HandledScreens.open(packet.getScreenHandlerType(), this.client, packet.getSyncId(), packet.getName());
    }

    @Override
    public void onScreenHandlerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ClientPlayerEntity playerEntity = this.client.player;
        ItemStack itemStack = packet.getItemStack();
        int i = packet.getSlot();
        this.client.getTutorialManager().onSlotUpdate(itemStack);
        if (packet.getSyncId() == ScreenHandlerSlotUpdateS2CPacket.UPDATE_CURSOR_SYNC_ID) {
            if (!(this.client.currentScreen instanceof CreativeInventoryScreen)) {
                playerEntity.currentScreenHandler.setCursorStack(itemStack);
            }
        } else if (packet.getSyncId() == ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID) {
            playerEntity.getInventory().setStack(i, itemStack);
        } else {
            Object creativeInventoryScreen;
            boolean bl = false;
            if (this.client.currentScreen instanceof CreativeInventoryScreen) {
                creativeInventoryScreen = (CreativeInventoryScreen)this.client.currentScreen;
                boolean bl2 = bl = ((CreativeInventoryScreen)creativeInventoryScreen).getSelectedTab() != ItemGroup.INVENTORY.getIndex();
            }
            if (packet.getSyncId() == 0 && PlayerScreenHandler.method_36211(i)) {
                if (!itemStack.isEmpty() && (((ItemStack)(creativeInventoryScreen = playerEntity.playerScreenHandler.getSlot(i).getStack())).isEmpty() || ((ItemStack)creativeInventoryScreen).getCount() < itemStack.getCount())) {
                    itemStack.setCooldown(5);
                }
                playerEntity.playerScreenHandler.setStackInSlot(i, packet.getRevision(), itemStack);
            } else if (!(packet.getSyncId() != playerEntity.currentScreenHandler.syncId || packet.getSyncId() == 0 && bl)) {
                playerEntity.currentScreenHandler.setStackInSlot(i, packet.getRevision(), itemStack);
            }
        }
    }

    @Override
    public void onInventory(InventoryS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ClientPlayerEntity playerEntity = this.client.player;
        if (packet.getSyncId() == 0) {
            playerEntity.playerScreenHandler.updateSlotStacks(packet.getRevision(), packet.getContents(), packet.getCursorStack());
        } else if (packet.getSyncId() == playerEntity.currentScreenHandler.syncId) {
            playerEntity.currentScreenHandler.updateSlotStacks(packet.getRevision(), packet.getContents(), packet.getCursorStack());
        }
    }

    @Override
    public void onSignEditorOpen(SignEditorOpenS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        BlockPos blockPos = packet.getPos();
        BlockEntity blockEntity = this.world.getBlockEntity(blockPos);
        if (!(blockEntity instanceof SignBlockEntity)) {
            BlockState blockState = this.world.getBlockState(blockPos);
            blockEntity = new SignBlockEntity(blockPos, blockState);
            blockEntity.setWorld(this.world);
        }
        this.client.player.openEditSignScreen((SignBlockEntity)blockEntity);
    }

    @Override
    public void onBlockEntityUpdate(BlockEntityUpdateS2CPacket packet) {
        boolean bl;
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        BlockPos blockPos = packet.getPos();
        BlockEntity blockEntity = this.client.world.getBlockEntity(blockPos);
        int i = packet.getBlockEntityType();
        boolean bl2 = bl = i == BlockEntityUpdateS2CPacket.COMMAND_BLOCK && blockEntity instanceof CommandBlockBlockEntity;
        if (i == BlockEntityUpdateS2CPacket.MOB_SPAWNER && blockEntity instanceof MobSpawnerBlockEntity || bl || i == BlockEntityUpdateS2CPacket.BEACON && blockEntity instanceof BeaconBlockEntity || i == BlockEntityUpdateS2CPacket.SKULL && blockEntity instanceof SkullBlockEntity || i == BlockEntityUpdateS2CPacket.BANNER && blockEntity instanceof BannerBlockEntity || i == BlockEntityUpdateS2CPacket.STRUCTURE && blockEntity instanceof StructureBlockBlockEntity || i == BlockEntityUpdateS2CPacket.END_GATEWAY && blockEntity instanceof EndGatewayBlockEntity || i == BlockEntityUpdateS2CPacket.SIGN && blockEntity instanceof SignBlockEntity || i == BlockEntityUpdateS2CPacket.BED && blockEntity instanceof BedBlockEntity || i == BlockEntityUpdateS2CPacket.CONDUIT && blockEntity instanceof ConduitBlockEntity || i == BlockEntityUpdateS2CPacket.JIGSAW && blockEntity instanceof JigsawBlockEntity || i == BlockEntityUpdateS2CPacket.CAMPFIRE && blockEntity instanceof CampfireBlockEntity || i == BlockEntityUpdateS2CPacket.BEEHIVE && blockEntity instanceof BeehiveBlockEntity) {
            blockEntity.readNbt(packet.getNbt());
        }
        if (bl && this.client.currentScreen instanceof CommandBlockScreen) {
            ((CommandBlockScreen)this.client.currentScreen).updateCommandBlock();
        }
    }

    @Override
    public void onScreenHandlerPropertyUpdate(ScreenHandlerPropertyUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ClientPlayerEntity playerEntity = this.client.player;
        if (playerEntity.currentScreenHandler != null && playerEntity.currentScreenHandler.syncId == packet.getSyncId()) {
            playerEntity.currentScreenHandler.setProperty(packet.getPropertyId(), packet.getValue());
        }
    }

    @Override
    public void onEquipmentUpdate(EntityEquipmentUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity entity = this.world.getEntityById(packet.getId());
        if (entity != null) {
            packet.getEquipmentList().forEach(pair -> entity.equipStack((EquipmentSlot)((Object)((Object)pair.getFirst())), (ItemStack)pair.getSecond()));
        }
    }

    @Override
    public void onCloseScreen(CloseScreenS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.player.closeScreen();
    }

    @Override
    public void onBlockEvent(BlockEventS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.world.addSyncedBlockEvent(packet.getPos(), packet.getBlock(), packet.getType(), packet.getData());
    }

    @Override
    public void onBlockDestroyProgress(BlockBreakingProgressS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.world.setBlockBreakingInfo(packet.getEntityId(), packet.getPos(), packet.getProgress());
    }

    @Override
    public void onGameStateChange(GameStateChangeS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ClientPlayerEntity playerEntity = this.client.player;
        GameStateChangeS2CPacket.Reason reason = packet.getReason();
        float f = packet.getValue();
        int i = MathHelper.floor(f + 0.5f);
        if (reason == GameStateChangeS2CPacket.NO_RESPAWN_BLOCK) {
            ((PlayerEntity)playerEntity).sendMessage(new TranslatableText("block.minecraft.spawn.not_valid"), false);
        } else if (reason == GameStateChangeS2CPacket.RAIN_STARTED) {
            this.world.getLevelProperties().setRaining(true);
            this.world.setRainGradient(0.0f);
        } else if (reason == GameStateChangeS2CPacket.RAIN_STOPPED) {
            this.world.getLevelProperties().setRaining(false);
            this.world.setRainGradient(1.0f);
        } else if (reason == GameStateChangeS2CPacket.GAME_MODE_CHANGED) {
            this.client.interactionManager.setGameMode(GameMode.byId(i));
        } else if (reason == GameStateChangeS2CPacket.GAME_WON) {
            if (i == 0) {
                this.client.player.networkHandler.sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
                this.client.setScreen(new DownloadingTerrainScreen());
            } else if (i == 1) {
                this.client.setScreen(new CreditsScreen(true, () -> this.client.player.networkHandler.sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN))));
            }
        } else if (reason == GameStateChangeS2CPacket.DEMO_MESSAGE_SHOWN) {
            GameOptions gameOptions = this.client.options;
            if (f == GameStateChangeS2CPacket.DEMO_OPEN_SCREEN) {
                this.client.setScreen(new DemoScreen());
            } else if (f == GameStateChangeS2CPacket.DEMO_MOVEMENT_HELP) {
                this.client.inGameHud.getChatHud().addMessage(new TranslatableText("demo.help.movement", gameOptions.keyForward.getBoundKeyLocalizedText(), gameOptions.keyLeft.getBoundKeyLocalizedText(), gameOptions.keyBack.getBoundKeyLocalizedText(), gameOptions.keyRight.getBoundKeyLocalizedText()));
            } else if (f == GameStateChangeS2CPacket.DEMO_JUMP_HELP) {
                this.client.inGameHud.getChatHud().addMessage(new TranslatableText("demo.help.jump", gameOptions.keyJump.getBoundKeyLocalizedText()));
            } else if (f == GameStateChangeS2CPacket.DEMO_INVENTORY_HELP) {
                this.client.inGameHud.getChatHud().addMessage(new TranslatableText("demo.help.inventory", gameOptions.keyInventory.getBoundKeyLocalizedText()));
            } else if (f == GameStateChangeS2CPacket.DEMO_EXPIRY_NOTICE) {
                this.client.inGameHud.getChatHud().addMessage(new TranslatableText("demo.day.6", gameOptions.keyScreenshot.getBoundKeyLocalizedText()));
            }
        } else if (reason == GameStateChangeS2CPacket.PROJECTILE_HIT_PLAYER) {
            this.world.playSound(playerEntity, playerEntity.getX(), playerEntity.getEyeY(), playerEntity.getZ(), SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.18f, 0.45f);
        } else if (reason == GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED) {
            this.world.setRainGradient(f);
        } else if (reason == GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED) {
            this.world.setThunderGradient(f);
        } else if (reason == GameStateChangeS2CPacket.PUFFERFISH_STING) {
            this.world.playSound(playerEntity, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_PUFFER_FISH_STING, SoundCategory.NEUTRAL, 1.0f, 1.0f);
        } else if (reason == GameStateChangeS2CPacket.ELDER_GUARDIAN_EFFECT) {
            this.world.addParticle(ParticleTypes.ELDER_GUARDIAN, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), 0.0, 0.0, 0.0);
            if (i == 1) {
                this.world.playSound(playerEntity, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 1.0f, 1.0f);
            }
        } else if (reason == GameStateChangeS2CPacket.IMMEDIATE_RESPAWN) {
            this.client.player.setShowsDeathScreen(f == GameStateChangeS2CPacket.DEMO_OPEN_SCREEN);
        }
    }

    @Override
    public void onMapUpdate(MapUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        MapRenderer mapRenderer = this.client.gameRenderer.getMapRenderer();
        int i = packet.getId();
        String string = FilledMapItem.getMapName(i);
        MapState mapState = this.client.world.getMapState(string);
        if (mapState == null) {
            mapState = MapState.of(packet.getScale(), packet.isLocked(), this.client.world.getRegistryKey());
            this.client.world.putMapState(string, mapState);
        }
        packet.apply(mapState);
        mapRenderer.updateTexture(i, mapState);
    }

    @Override
    public void onWorldEvent(WorldEventS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        if (packet.isGlobal()) {
            this.client.world.syncGlobalEvent(packet.getEventId(), packet.getPos(), packet.getData());
        } else {
            this.client.world.syncWorldEvent(packet.getEventId(), packet.getPos(), packet.getData());
        }
    }

    @Override
    public void onAdvancements(AdvancementUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.advancementHandler.onAdvancements(packet);
    }

    @Override
    public void onSelectAdvancementTab(SelectAdvancementTabS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Identifier identifier = packet.getTabId();
        if (identifier == null) {
            this.advancementHandler.selectTab(null, false);
        } else {
            Advancement advancement = this.advancementHandler.getManager().get(identifier);
            this.advancementHandler.selectTab(advancement, false);
        }
    }

    @Override
    public void onCommandTree(CommandTreeS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.commandDispatcher = new CommandDispatcher<CommandSource>(packet.getCommandTree());
    }

    @Override
    public void onStopSound(StopSoundS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.getSoundManager().stopSounds(packet.getSoundId(), packet.getCategory());
    }

    @Override
    public void onCommandSuggestions(CommandSuggestionsS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.commandSource.onCommandSuggestions(packet.getCompletionId(), packet.getSuggestions());
    }

    @Override
    public void onSynchronizeRecipes(SynchronizeRecipesS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.recipeManager.setRecipes(packet.getRecipes());
        SearchableContainer<RecipeResultCollection> searchableContainer = this.client.getSearchableContainer(SearchManager.RECIPE_OUTPUT);
        searchableContainer.clear();
        ClientRecipeBook clientRecipeBook = this.client.player.getRecipeBook();
        clientRecipeBook.reload(this.recipeManager.values());
        clientRecipeBook.getOrderedResults().forEach(searchableContainer::add);
        searchableContainer.reload();
    }

    @Override
    public void onLookAt(LookAtS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Vec3d vec3d = packet.getTargetPosition(this.world);
        if (vec3d != null) {
            this.client.player.lookAt(packet.getSelfAnchor(), vec3d);
        }
    }

    @Override
    public void onTagQuery(NbtQueryResponseS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        if (!this.dataQueryHandler.handleQueryResponse(packet.getTransactionId(), packet.getNbt())) {
            LOGGER.debug("Got unhandled response to tag query {}", (Object)packet.getTransactionId());
        }
    }

    @Override
    public void onStatistics(StatisticsS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        for (Map.Entry<Stat<?>, Integer> entry : packet.getStatMap().entrySet()) {
            Stat<?> stat = entry.getKey();
            int i = entry.getValue();
            this.client.player.getStatHandler().setStat(this.client.player, stat, i);
        }
        if (this.client.currentScreen instanceof StatsListener) {
            ((StatsListener)((Object)this.client.currentScreen)).onStatsReady();
        }
    }

    @Override
    public void onUnlockRecipes(UnlockRecipesS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ClientRecipeBook clientRecipeBook = this.client.player.getRecipeBook();
        clientRecipeBook.setOptions(packet.getOptions());
        UnlockRecipesS2CPacket.Action action = packet.getAction();
        switch (action) {
            case REMOVE: {
                for (Identifier identifier : packet.getRecipeIdsToChange()) {
                    this.recipeManager.get(identifier).ifPresent(clientRecipeBook::remove);
                }
                break;
            }
            case INIT: {
                for (Identifier identifier : packet.getRecipeIdsToChange()) {
                    this.recipeManager.get(identifier).ifPresent(clientRecipeBook::add);
                }
                for (Identifier identifier : packet.getRecipeIdsToInit()) {
                    this.recipeManager.get(identifier).ifPresent(clientRecipeBook::display);
                }
                break;
            }
            case ADD: {
                for (Identifier identifier : packet.getRecipeIdsToChange()) {
                    this.recipeManager.get(identifier).ifPresent(recipe -> {
                        clientRecipeBook.add((Recipe<?>)recipe);
                        clientRecipeBook.display((Recipe<?>)recipe);
                        RecipeToast.show(this.client.getToastManager(), recipe);
                    });
                }
                break;
            }
        }
        clientRecipeBook.getOrderedResults().forEach(recipeResultCollection -> recipeResultCollection.initialize(clientRecipeBook));
        if (this.client.currentScreen instanceof RecipeBookProvider) {
            ((RecipeBookProvider)((Object)this.client.currentScreen)).refreshRecipeBook();
        }
    }

    @Override
    public void onEntityStatusEffect(EntityStatusEffectS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity entity = this.world.getEntityById(packet.getEntityId());
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        StatusEffect statusEffect = StatusEffect.byRawId(packet.getEffectId());
        if (statusEffect == null) {
            return;
        }
        StatusEffectInstance statusEffectInstance = new StatusEffectInstance(statusEffect, packet.getDuration(), packet.getAmplifier(), packet.isAmbient(), packet.shouldShowParticles(), packet.shouldShowIcon());
        statusEffectInstance.setPermanent(packet.isPermanent());
        ((LivingEntity)entity).setStatusEffect(statusEffectInstance, null);
    }

    @Override
    public void onSynchronizeTags(SynchronizeTagsS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        TagManager tagManager = TagManager.fromPacket(this.registryManager, packet.getGroups());
        Multimap<RegistryKey<Registry<?>>, Identifier> multimap = RequiredTagListRegistry.getMissingTags(tagManager);
        if (!multimap.isEmpty()) {
            LOGGER.warn("Incomplete server tags, disconnecting. Missing: {}", (Object)multimap);
            this.connection.disconnect(new TranslatableText("multiplayer.disconnect.missing_tags"));
            return;
        }
        this.tagManager = tagManager;
        if (!this.connection.isLocal()) {
            tagManager.apply();
        }
        this.client.getSearchableContainer(SearchManager.ITEM_TAG).reload();
    }

    @Override
    public void onEndCombat(EndCombatS2CPacket packet) {
    }

    @Override
    public void onEnterCombat(EnterCombatS2CPacket packet) {
    }

    @Override
    public void onDeathMessage(DeathMessageS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity entity = this.world.getEntityById(packet.getEntityId());
        if (entity == this.client.player) {
            if (this.client.player.showsDeathScreen()) {
                this.client.setScreen(new DeathScreen(packet.getMessage(), this.world.getLevelProperties().isHardcore()));
            } else {
                this.client.player.requestRespawn();
            }
        }
    }

    @Override
    public void onDifficulty(DifficultyS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.worldProperties.setDifficulty(packet.getDifficulty());
        this.worldProperties.setDifficultyLocked(packet.isDifficultyLocked());
    }

    @Override
    public void onSetCameraEntity(SetCameraEntityS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity entity = packet.getEntity(this.world);
        if (entity != null) {
            this.client.setCameraEntity(entity);
        }
    }

    @Override
    public void onWorldBorderInitialize(WorldBorderInitializeS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        WorldBorder worldBorder = this.world.getWorldBorder();
        worldBorder.setCenter(packet.getCenterX(), packet.getCenterZ());
        long l = packet.getSizeLerpTime();
        if (l > 0L) {
            worldBorder.interpolateSize(packet.getSize(), packet.getSizeLerpTarget(), l);
        } else {
            worldBorder.setSize(packet.getSizeLerpTarget());
        }
        worldBorder.setMaxRadius(packet.getMaxRadius());
        worldBorder.setWarningBlocks(packet.getWarningBlocks());
        worldBorder.setWarningTime(packet.getWarningTime());
    }

    @Override
    public void onWorldBorderCenterChanged(WorldBorderCenterChangedS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.world.getWorldBorder().setCenter(packet.getCenterX(), packet.getCenterZ());
    }

    @Override
    public void onWorldBorderInterpolateSize(WorldBorderInterpolateSizeS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.world.getWorldBorder().interpolateSize(packet.getSize(), packet.getSizeLerpTarget(), packet.getSizeLerpTime());
    }

    @Override
    public void onWorldBorderSizeChanged(WorldBorderSizeChangedS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.world.getWorldBorder().setSize(packet.getSizeLerpTarget());
    }

    @Override
    public void onWorldBorderWarningBlocksChanged(WorldBorderWarningBlocksChangedS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.world.getWorldBorder().setWarningBlocks(packet.getWarningBlocks());
    }

    @Override
    public void onWorldBorderWarningTimeChanged(WorldBorderWarningTimeChangedS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.world.getWorldBorder().setWarningTime(packet.getWarningTime());
    }

    @Override
    public void onTitleClear(ClearTitleS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.inGameHud.clearTitle();
        if (packet.shouldReset()) {
            this.client.inGameHud.setDefaultTitleFade();
        }
    }

    @Override
    public void onOverlayMessage(OverlayMessageS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.inGameHud.setOverlayMessage(packet.getMessage(), false);
    }

    @Override
    public void onTitle(TitleS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.inGameHud.setTitle(packet.getTitle());
    }

    @Override
    public void onSubtitle(SubtitleS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.inGameHud.setSubtitle(packet.getSubtitle());
    }

    @Override
    public void onTitleFade(TitleFadeS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.inGameHud.setTitleTicks(packet.getFadeInTicks(), packet.getRemainTicks(), packet.getFadeOutTicks());
    }

    @Override
    public void onPlayerListHeader(PlayerListHeaderS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.inGameHud.getPlayerListHud().setHeader(packet.getHeader().getString().isEmpty() ? null : packet.getHeader());
        this.client.inGameHud.getPlayerListHud().setFooter(packet.getFooter().getString().isEmpty() ? null : packet.getFooter());
    }

    @Override
    public void onRemoveEntityEffect(RemoveEntityStatusEffectS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity entity = packet.getEntity(this.world);
        if (entity instanceof LivingEntity) {
            ((LivingEntity)entity).removeStatusEffectInternal(packet.getEffectType());
        }
    }

    @Override
    public void onPlayerList(PlayerListS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
            if (packet.getAction() == PlayerListS2CPacket.Action.REMOVE_PLAYER) {
                this.client.getSocialInteractionsManager().setPlayerOffline(entry.getProfile().getId());
                this.playerListEntries.remove(entry.getProfile().getId());
                continue;
            }
            PlayerListEntry playerListEntry = this.playerListEntries.get(entry.getProfile().getId());
            if (packet.getAction() == PlayerListS2CPacket.Action.ADD_PLAYER) {
                playerListEntry = new PlayerListEntry(entry);
                this.playerListEntries.put(playerListEntry.getProfile().getId(), playerListEntry);
                this.client.getSocialInteractionsManager().setPlayerOnline(playerListEntry);
            }
            if (playerListEntry == null) continue;
            switch (packet.getAction()) {
                case ADD_PLAYER: {
                    playerListEntry.setGameMode(entry.getGameMode());
                    playerListEntry.setLatency(entry.getLatency());
                    playerListEntry.setDisplayName(entry.getDisplayName());
                    break;
                }
                case UPDATE_GAME_MODE: {
                    playerListEntry.setGameMode(entry.getGameMode());
                    break;
                }
                case UPDATE_LATENCY: {
                    playerListEntry.setLatency(entry.getLatency());
                    break;
                }
                case UPDATE_DISPLAY_NAME: {
                    playerListEntry.setDisplayName(entry.getDisplayName());
                }
            }
        }
    }

    @Override
    public void onKeepAlive(KeepAliveS2CPacket packet) {
        this.sendPacket(new KeepAliveC2SPacket(packet.getId()));
    }

    @Override
    public void onPlayerAbilities(PlayerAbilitiesS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ClientPlayerEntity playerEntity = this.client.player;
        playerEntity.getAbilities().flying = packet.isFlying();
        playerEntity.getAbilities().creativeMode = packet.isCreativeMode();
        playerEntity.getAbilities().invulnerable = packet.isInvulnerable();
        playerEntity.getAbilities().allowFlying = packet.allowFlying();
        playerEntity.getAbilities().setFlySpeed(packet.getFlySpeed());
        playerEntity.getAbilities().setWalkSpeed(packet.getWalkSpeed());
    }

    @Override
    public void onPlaySound(PlaySoundS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.world.playSound(this.client.player, packet.getX(), packet.getY(), packet.getZ(), packet.getSound(), packet.getCategory(), packet.getVolume(), packet.getPitch());
    }

    @Override
    public void onPlaySoundFromEntity(PlaySoundFromEntityS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity entity = this.world.getEntityById(packet.getEntityId());
        if (entity == null) {
            return;
        }
        this.client.world.playSoundFromEntity(this.client.player, entity, packet.getSound(), packet.getCategory(), packet.getVolume(), packet.getPitch());
    }

    @Override
    public void onPlaySoundId(PlaySoundIdS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.getSoundManager().play(new PositionedSoundInstance(packet.getSoundId(), packet.getCategory(), packet.getVolume(), packet.getPitch(), false, 0, SoundInstance.AttenuationType.LINEAR, packet.getX(), packet.getY(), packet.getZ(), false));
    }

    @Override
    public void onResourcePackSend(ResourcePackSendS2CPacket packet) {
        String string = packet.getURL();
        String string2 = packet.getSHA1();
        boolean bl = packet.isRequired();
        if (!this.validateResourcePackUrl(string)) {
            return;
        }
        if (string.startsWith("level://")) {
            try {
                String string3 = URLDecoder.decode(string.substring("level://".length()), StandardCharsets.UTF_8.toString());
                File file = new File(this.client.runDirectory, "saves");
                File file2 = new File(file, string3);
                if (file2.isFile()) {
                    this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.ACCEPTED);
                    CompletableFuture<Void> completableFuture = this.client.getResourcePackProvider().loadServerPack(file2, ResourcePackSource.PACK_SOURCE_WORLD);
                    this.feedbackAfterDownload(completableFuture);
                    return;
                }
            }
            catch (UnsupportedEncodingException string3) {
                // empty catch block
            }
            this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.FAILED_DOWNLOAD);
            return;
        }
        ServerInfo string3 = this.client.getCurrentServerEntry();
        if (string3 != null && string3.getResourcePackPolicy() == ServerInfo.ResourcePackPolicy.ENABLED) {
            this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.ACCEPTED);
            this.feedbackAfterDownload(this.client.getResourcePackProvider().download(string, string2, true));
        } else if (string3 == null || string3.getResourcePackPolicy() == ServerInfo.ResourcePackPolicy.PROMPT || bl && string3.getResourcePackPolicy() == ServerInfo.ResourcePackPolicy.DISABLED) {
            this.client.execute(() -> this.client.setScreen(new ConfirmScreen(enabled -> {
                this.client.setScreen(null);
                ServerInfo serverInfo = this.client.getCurrentServerEntry();
                if (enabled) {
                    if (serverInfo != null) {
                        serverInfo.setResourcePackPolicy(ServerInfo.ResourcePackPolicy.ENABLED);
                    }
                    this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.ACCEPTED);
                    this.feedbackAfterDownload(this.client.getResourcePackProvider().download(string, string2, true));
                } else {
                    this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.DECLINED);
                    if (bl) {
                        this.connection.disconnect(new TranslatableText("multiplayer.requiredTexturePrompt.disconnect"));
                    } else if (serverInfo != null) {
                        serverInfo.setResourcePackPolicy(ServerInfo.ResourcePackPolicy.DISABLED);
                    }
                }
                if (serverInfo != null) {
                    ServerList.updateServerListEntry(serverInfo);
                }
            }, bl ? new TranslatableText("multiplayer.requiredTexturePrompt.line1") : new TranslatableText("multiplayer.texturePrompt.line1"), ClientPlayNetworkHandler.getServerResourcePackPrompt(bl ? new TranslatableText("multiplayer.requiredTexturePrompt.line2").formatted(Formatting.YELLOW, Formatting.BOLD) : new TranslatableText("multiplayer.texturePrompt.line2"), packet.getPrompt()), bl ? ScreenTexts.PROCEED : ScreenTexts.YES, bl ? new TranslatableText("menu.disconnect") : ScreenTexts.NO)));
        } else {
            this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.DECLINED);
            if (bl) {
                this.connection.disconnect(new TranslatableText("multiplayer.requiredTexturePrompt.disconnect"));
            }
        }
    }

    private static Text getServerResourcePackPrompt(Text defaultPrompt, @Nullable Text customPrompt) {
        if (customPrompt == null) {
            return defaultPrompt;
        }
        return new TranslatableText("multiplayer.texturePrompt.serverPrompt", defaultPrompt, customPrompt);
    }

    private boolean validateResourcePackUrl(String url) {
        try {
            URI uRI = new URI(url);
            String string = uRI.getScheme();
            boolean bl = "level".equals(string);
            if (!("http".equals(string) || "https".equals(string) || bl)) {
                throw new URISyntaxException(url, "Wrong protocol");
            }
            if (bl && (url.contains("..") || !url.endsWith("/resources.zip"))) {
                throw new URISyntaxException(url, "Invalid levelstorage resourcepack path");
            }
        }
        catch (URISyntaxException uRI) {
            this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.FAILED_DOWNLOAD);
            return false;
        }
        return true;
    }

    private void feedbackAfterDownload(CompletableFuture<?> downloadFuture) {
        ((CompletableFuture)downloadFuture.thenRun(() -> this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED))).exceptionally(throwable -> {
            this.sendResourcePackStatus(ResourcePackStatusC2SPacket.Status.FAILED_DOWNLOAD);
            return null;
        });
    }

    private void sendResourcePackStatus(ResourcePackStatusC2SPacket.Status packStatus) {
        this.connection.send(new ResourcePackStatusC2SPacket(packStatus));
    }

    @Override
    public void onBossBar(BossBarS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.inGameHud.getBossBarHud().handlePacket(packet);
    }

    @Override
    public void onCooldownUpdate(CooldownUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        if (packet.getCooldown() == 0) {
            this.client.player.getItemCooldownManager().remove(packet.getItem());
        } else {
            this.client.player.getItemCooldownManager().set(packet.getItem(), packet.getCooldown());
        }
    }

    @Override
    public void onVehicleMove(VehicleMoveS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity entity = this.client.player.getRootVehicle();
        if (entity != this.client.player && entity.isLogicalSideForUpdatingMovement()) {
            entity.updatePositionAndAngles(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch());
            this.connection.send(new VehicleMoveC2SPacket(entity));
        }
    }

    @Override
    public void onOpenWrittenBook(OpenWrittenBookS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ItemStack itemStack = this.client.player.getStackInHand(packet.getHand());
        if (itemStack.isOf(Items.WRITTEN_BOOK)) {
            this.client.setScreen(new BookScreen(new BookScreen.WrittenBookContents(itemStack)));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void onCustomPayload(CustomPayloadS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Identifier identifier = packet.getChannel();
        PacketByteBuf packetByteBuf = null;
        try {
            packetByteBuf = packet.getData();
            if (CustomPayloadS2CPacket.BRAND.equals(identifier)) {
                this.client.player.setServerBrand(packetByteBuf.readString());
            } else if (CustomPayloadS2CPacket.DEBUG_PATH.equals(identifier)) {
                int i = packetByteBuf.readInt();
                float f = packetByteBuf.readFloat();
                Path path = Path.fromBuffer(packetByteBuf);
                this.client.debugRenderer.pathfindingDebugRenderer.addPath(i, path, f);
            } else if (CustomPayloadS2CPacket.DEBUG_NEIGHBORS_UPDATE.equals(identifier)) {
                long i = packetByteBuf.readVarLong();
                BlockPos path = packetByteBuf.readBlockPos();
                ((NeighborUpdateDebugRenderer)this.client.debugRenderer.neighborUpdateDebugRenderer).addNeighborUpdate(i, path);
            } else if (CustomPayloadS2CPacket.DEBUG_STRUCTURES.equals(identifier)) {
                DimensionType i = this.registryManager.get(Registry.DIMENSION_TYPE_KEY).get(packetByteBuf.readIdentifier());
                BlockBox f = new BlockBox(packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt());
                int path = packetByteBuf.readInt();
                ArrayList<BlockBox> list = Lists.newArrayList();
                ArrayList<Boolean> list2 = Lists.newArrayList();
                for (int j = 0; j < path; ++j) {
                    list.add(new BlockBox(packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt()));
                    list2.add(packetByteBuf.readBoolean());
                }
                this.client.debugRenderer.structureDebugRenderer.addStructure(f, list, list2, i);
            } else if (CustomPayloadS2CPacket.DEBUG_WORLDGEN_ATTEMPT.equals(identifier)) {
                ((WorldGenAttemptDebugRenderer)this.client.debugRenderer.worldGenAttemptDebugRenderer).method_3872(packetByteBuf.readBlockPos(), packetByteBuf.readFloat(), packetByteBuf.readFloat(), packetByteBuf.readFloat(), packetByteBuf.readFloat(), packetByteBuf.readFloat());
            } else if (CustomPayloadS2CPacket.DEBUG_VILLAGE_SECTIONS.equals(identifier)) {
                int f;
                int i = packetByteBuf.readInt();
                for (f = 0; f < i; ++f) {
                    this.client.debugRenderer.villageSectionsDebugRenderer.addSection(packetByteBuf.readChunkSectionPos());
                }
                f = packetByteBuf.readInt();
                for (int path = 0; path < f; ++path) {
                    this.client.debugRenderer.villageSectionsDebugRenderer.removeSection(packetByteBuf.readChunkSectionPos());
                }
            } else if (CustomPayloadS2CPacket.DEBUG_POI_ADDED.equals(identifier)) {
                BlockPos i = packetByteBuf.readBlockPos();
                String f = packetByteBuf.readString();
                int path = packetByteBuf.readInt();
                VillageDebugRenderer.PointOfInterest list = new VillageDebugRenderer.PointOfInterest(i, f, path);
                this.client.debugRenderer.villageDebugRenderer.addPointOfInterest(list);
            } else if (CustomPayloadS2CPacket.DEBUG_POI_REMOVED.equals(identifier)) {
                BlockPos i = packetByteBuf.readBlockPos();
                this.client.debugRenderer.villageDebugRenderer.removePointOfInterest(i);
            } else if (CustomPayloadS2CPacket.DEBUG_POI_TICKET_COUNT.equals(identifier)) {
                BlockPos i = packetByteBuf.readBlockPos();
                int f = packetByteBuf.readInt();
                this.client.debugRenderer.villageDebugRenderer.setFreeTicketCount(i, f);
            } else if (CustomPayloadS2CPacket.DEBUG_GOAL_SELECTOR.equals(identifier)) {
                BlockPos i = packetByteBuf.readBlockPos();
                int f = packetByteBuf.readInt();
                int path = packetByteBuf.readInt();
                ArrayList<GoalSelectorDebugRenderer.GoalSelector> list = Lists.newArrayList();
                for (int list2 = 0; list2 < path; ++list2) {
                    int j = packetByteBuf.readInt();
                    boolean bl = packetByteBuf.readBoolean();
                    String string = packetByteBuf.readString(255);
                    list.add(new GoalSelectorDebugRenderer.GoalSelector(i, j, string, bl));
                }
                this.client.debugRenderer.goalSelectorDebugRenderer.setGoalSelectorList(f, list);
            } else if (CustomPayloadS2CPacket.DEBUG_RAIDS.equals(identifier)) {
                int i = packetByteBuf.readInt();
                ArrayList<BlockPos> f = Lists.newArrayList();
                for (int path = 0; path < i; ++path) {
                    f.add(packetByteBuf.readBlockPos());
                }
                this.client.debugRenderer.raidCenterDebugRenderer.setRaidCenters(f);
            } else if (CustomPayloadS2CPacket.DEBUG_BRAIN.equals(identifier)) {
                int blockPos;
                int string7;
                int string6;
                int string5;
                int n;
                double i = packetByteBuf.readDouble();
                double path = packetByteBuf.readDouble();
                double list2 = packetByteBuf.readDouble();
                PositionImpl bl = new PositionImpl(i, path, list2);
                UUID string = packetByteBuf.readUuid();
                int k = packetByteBuf.readInt();
                String string2 = packetByteBuf.readString();
                String string3 = packetByteBuf.readString();
                int l = packetByteBuf.readInt();
                float g = packetByteBuf.readFloat();
                float h = packetByteBuf.readFloat();
                String string4 = packetByteBuf.readString();
                boolean bl2 = packetByteBuf.readBoolean();
                Path path2 = bl2 ? Path.fromBuffer(packetByteBuf) : null;
                boolean bl3 = packetByteBuf.readBoolean();
                VillageDebugRenderer.Brain brain = new VillageDebugRenderer.Brain(string, k, string2, string3, l, g, h, bl, string4, path2, bl3);
                int m = packetByteBuf.readVarInt();
                for (n = 0; n < m; ++n) {
                    String string52 = packetByteBuf.readString();
                    brain.possibleActivities.add(string52);
                }
                n = packetByteBuf.readVarInt();
                for (string5 = 0; string5 < n; ++string5) {
                    String string62 = packetByteBuf.readString();
                    brain.runningTasks.add(string62);
                }
                string5 = packetByteBuf.readVarInt();
                for (string6 = 0; string6 < string5; ++string6) {
                    String string72 = packetByteBuf.readString();
                    brain.memories.add(string72);
                }
                string6 = packetByteBuf.readVarInt();
                for (string7 = 0; string7 < string6; ++string7) {
                    BlockPos blockPos2 = packetByteBuf.readBlockPos();
                    brain.pointsOfInterest.add(blockPos2);
                }
                string7 = packetByteBuf.readVarInt();
                for (blockPos = 0; blockPos < string7; ++blockPos) {
                    BlockPos blockPos2 = packetByteBuf.readBlockPos();
                    brain.potentialJobSites.add(blockPos2);
                }
                blockPos = packetByteBuf.readVarInt();
                for (int blockPos2 = 0; blockPos2 < blockPos; ++blockPos2) {
                    String string8 = packetByteBuf.readString();
                    brain.gossips.add(string8);
                }
                this.client.debugRenderer.villageDebugRenderer.addBrain(brain);
            } else if (CustomPayloadS2CPacket.DEBUG_BEE.equals(identifier)) {
                int brain;
                double i = packetByteBuf.readDouble();
                double path = packetByteBuf.readDouble();
                double list2 = packetByteBuf.readDouble();
                PositionImpl bl = new PositionImpl(i, path, list2);
                UUID string = packetByteBuf.readUuid();
                int k = packetByteBuf.readInt();
                boolean string2 = packetByteBuf.readBoolean();
                BlockPos string3 = null;
                if (string2) {
                    string3 = packetByteBuf.readBlockPos();
                }
                boolean l = packetByteBuf.readBoolean();
                BlockPos g = null;
                if (l) {
                    g = packetByteBuf.readBlockPos();
                }
                int h = packetByteBuf.readInt();
                boolean string4 = packetByteBuf.readBoolean();
                Path bl2 = null;
                if (string4) {
                    bl2 = Path.fromBuffer(packetByteBuf);
                }
                BeeDebugRenderer.Bee path2 = new BeeDebugRenderer.Bee(string, k, bl, bl2, string3, g, h);
                int bl3 = packetByteBuf.readVarInt();
                for (brain = 0; brain < bl3; ++brain) {
                    String m = packetByteBuf.readString();
                    path2.labels.add(m);
                }
                brain = packetByteBuf.readVarInt();
                for (int m = 0; m < brain; ++m) {
                    BlockPos n = packetByteBuf.readBlockPos();
                    path2.blacklist.add(n);
                }
                this.client.debugRenderer.beeDebugRenderer.addBee(path2);
            } else if (CustomPayloadS2CPacket.DEBUG_HIVE.equals(identifier)) {
                BlockPos i = packetByteBuf.readBlockPos();
                String f = packetByteBuf.readString();
                int path = packetByteBuf.readInt();
                int list = packetByteBuf.readInt();
                boolean list2 = packetByteBuf.readBoolean();
                BeeDebugRenderer.Hive j = new BeeDebugRenderer.Hive(i, f, path, list, list2, this.world.getTime());
                this.client.debugRenderer.beeDebugRenderer.addHive(j);
            } else if (CustomPayloadS2CPacket.DEBUG_GAME_TEST_CLEAR.equals(identifier)) {
                this.client.debugRenderer.gameTestDebugRenderer.clear();
            } else if (CustomPayloadS2CPacket.DEBUG_GAME_TEST_ADD_MARKER.equals(identifier)) {
                BlockPos i = packetByteBuf.readBlockPos();
                int f = packetByteBuf.readInt();
                String path = packetByteBuf.readString();
                int list = packetByteBuf.readInt();
                this.client.debugRenderer.gameTestDebugRenderer.addMarker(i, f, path, list);
            } else if (CustomPayloadS2CPacket.DEBUG_GAME_EVENT.equals(identifier)) {
                GameEvent i = Registry.GAME_EVENT.get(new Identifier(packetByteBuf.readString()));
                BlockPos f = packetByteBuf.readBlockPos();
                this.client.debugRenderer.gameEventDebugRenderer.addEvent(i, f);
            } else if (CustomPayloadS2CPacket.DEBUG_GAME_EVENT_LISTENERS.equals(identifier)) {
                Identifier i = packetByteBuf.readIdentifier();
                Object f = Registry.POSITION_SOURCE_TYPE.getOrEmpty(i).orElseThrow(() -> new IllegalArgumentException("Unknown position source type " + i)).readFromBuf(packetByteBuf);
                int path = packetByteBuf.readVarInt();
                this.client.debugRenderer.gameEventDebugRenderer.addListener((PositionSource)f, path);
            } else {
                LOGGER.warn("Unknown custom packed identifier: {}", (Object)identifier);
            }
        }
        finally {
            if (packetByteBuf != null) {
                packetByteBuf.release();
            }
        }
    }

    @Override
    public void onScoreboardObjectiveUpdate(ScoreboardObjectiveUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Scoreboard scoreboard = this.world.getScoreboard();
        String string = packet.getName();
        if (packet.getMode() == 0) {
            scoreboard.addObjective(string, ScoreboardCriterion.DUMMY, packet.getDisplayName(), packet.getType());
        } else if (scoreboard.containsObjective(string)) {
            ScoreboardObjective scoreboardObjective = scoreboard.getNullableObjective(string);
            if (packet.getMode() == ScoreboardObjectiveUpdateS2CPacket.REMOVE_MODE) {
                scoreboard.removeObjective(scoreboardObjective);
            } else if (packet.getMode() == ScoreboardObjectiveUpdateS2CPacket.UPDATE_MODE) {
                scoreboardObjective.setRenderType(packet.getType());
                scoreboardObjective.setDisplayName(packet.getDisplayName());
            }
        }
    }

    @Override
    public void onScoreboardPlayerUpdate(ScoreboardPlayerUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Scoreboard scoreboard = this.world.getScoreboard();
        String string = packet.getObjectiveName();
        switch (packet.getUpdateMode()) {
            case CHANGE: {
                ScoreboardObjective scoreboardObjective = scoreboard.getObjective(string);
                ScoreboardPlayerScore scoreboardPlayerScore = scoreboard.getPlayerScore(packet.getPlayerName(), scoreboardObjective);
                scoreboardPlayerScore.setScore(packet.getScore());
                break;
            }
            case REMOVE: {
                scoreboard.resetPlayerScore(packet.getPlayerName(), scoreboard.getNullableObjective(string));
            }
        }
    }

    @Override
    public void onScoreboardDisplay(ScoreboardDisplayS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Scoreboard scoreboard = this.world.getScoreboard();
        String string = packet.getName();
        ScoreboardObjective scoreboardObjective = string == null ? null : scoreboard.getObjective(string);
        scoreboard.setObjectiveSlot(packet.getSlot(), scoreboardObjective);
    }

    @Override
    public void onTeam(TeamS2CPacket packet) {
        Team team2;
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Scoreboard scoreboard = this.world.getScoreboard();
        TeamS2CPacket.Operation operation = packet.getTeamOperation();
        if (operation == TeamS2CPacket.Operation.ADD) {
            team2 = scoreboard.addTeam(packet.getTeamName());
        } else {
            team2 = scoreboard.getTeam(packet.getTeamName());
            if (team2 == null) {
                LOGGER.warn("Received packet for unknown team {}: team action: {}, player action: {}", (Object)packet.getTeamName(), (Object)packet.getTeamOperation(), (Object)packet.getPlayerListOperation());
                return;
            }
        }
        Optional<TeamS2CPacket.SerializableTeam> optional = packet.getTeam();
        optional.ifPresent(team -> {
            AbstractTeam.CollisionRule collisionRule;
            team2.setDisplayName(team.getDisplayName());
            team2.setColor(team.getColor());
            team2.setFriendlyFlagsBitwise(team.getFriendlyFlagsBitwise());
            AbstractTeam.VisibilityRule visibilityRule = AbstractTeam.VisibilityRule.getRule(team.getNameTagVisibilityRule());
            if (visibilityRule != null) {
                team2.setNameTagVisibilityRule(visibilityRule);
            }
            if ((collisionRule = AbstractTeam.CollisionRule.getRule(team.getCollisionRule())) != null) {
                team2.setCollisionRule(collisionRule);
            }
            team2.setPrefix(team.getPrefix());
            team2.setSuffix(team.getSuffix());
        });
        TeamS2CPacket.Operation operation2 = packet.getPlayerListOperation();
        if (operation2 == TeamS2CPacket.Operation.ADD) {
            for (String string : packet.getPlayerNames()) {
                scoreboard.addPlayerToTeam(string, team2);
            }
        } else if (operation2 == TeamS2CPacket.Operation.REMOVE) {
            for (String string : packet.getPlayerNames()) {
                scoreboard.removePlayerFromTeam(string, team2);
            }
        }
        if (operation == TeamS2CPacket.Operation.REMOVE) {
            scoreboard.removeTeam(team2);
        }
    }

    @Override
    public void onParticle(ParticleS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        if (packet.getCount() == 0) {
            double d = packet.getSpeed() * packet.getOffsetX();
            double e = packet.getSpeed() * packet.getOffsetY();
            double f = packet.getSpeed() * packet.getOffsetZ();
            try {
                this.world.addParticle(packet.getParameters(), packet.isLongDistance(), packet.getX(), packet.getY(), packet.getZ(), d, e, f);
            }
            catch (Throwable throwable) {
                LOGGER.warn("Could not spawn particle effect {}", (Object)packet.getParameters());
            }
        } else {
            for (int d = 0; d < packet.getCount(); ++d) {
                double g = this.random.nextGaussian() * (double)packet.getOffsetX();
                double h = this.random.nextGaussian() * (double)packet.getOffsetY();
                double i = this.random.nextGaussian() * (double)packet.getOffsetZ();
                double j = this.random.nextGaussian() * (double)packet.getSpeed();
                double k = this.random.nextGaussian() * (double)packet.getSpeed();
                double l = this.random.nextGaussian() * (double)packet.getSpeed();
                try {
                    this.world.addParticle(packet.getParameters(), packet.isLongDistance(), packet.getX() + g, packet.getY() + h, packet.getZ() + i, j, k, l);
                    continue;
                }
                catch (Throwable throwable2) {
                    LOGGER.warn("Could not spawn particle effect {}", (Object)packet.getParameters());
                    return;
                }
            }
        }
    }

    @Override
    public void onPing(PlayPingS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.sendPacket(new PlayPongC2SPacket(packet.getParameter()));
    }

    @Override
    public void onEntityAttributes(EntityAttributesS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        Entity entity = this.world.getEntityById(packet.getEntityId());
        if (entity == null) {
            return;
        }
        if (!(entity instanceof LivingEntity)) {
            throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + entity + ")");
        }
        AttributeContainer attributeContainer = ((LivingEntity)entity).getAttributes();
        for (EntityAttributesS2CPacket.Entry entry : packet.getEntries()) {
            EntityAttributeInstance entityAttributeInstance = attributeContainer.getCustomInstance(entry.getId());
            if (entityAttributeInstance == null) {
                LOGGER.warn("Entity {} does not have attribute {}", (Object)entity, (Object)Registry.ATTRIBUTE.getId(entry.getId()));
                continue;
            }
            entityAttributeInstance.setBaseValue(entry.getBaseValue());
            entityAttributeInstance.clearModifiers();
            for (EntityAttributeModifier entityAttributeModifier : entry.getModifiers()) {
                entityAttributeInstance.addTemporaryModifier(entityAttributeModifier);
            }
        }
    }

    @Override
    public void onCraftFailedResponse(CraftFailedResponseS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ScreenHandler screenHandler = this.client.player.currentScreenHandler;
        if (screenHandler.syncId != packet.getSyncId()) {
            return;
        }
        this.recipeManager.get(packet.getRecipeId()).ifPresent(recipe -> {
            if (this.client.currentScreen instanceof RecipeBookProvider) {
                RecipeBookWidget recipeBookWidget = ((RecipeBookProvider)((Object)this.client.currentScreen)).getRecipeBookWidget();
                recipeBookWidget.showGhostRecipe((Recipe<?>)recipe, (List<Slot>)screenHandler.slots);
            }
        });
    }

    @Override
    public void onLightUpdate(LightUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        int i = packet.getChunkX();
        int j = packet.getChunkZ();
        LightingProvider lightingProvider = this.world.getChunkManager().getLightingProvider();
        BitSet bitSet = packet.getSkyLightMask();
        BitSet bitSet2 = packet.getFilledSkyLightMask();
        Iterator<byte[]> iterator = packet.getSkyLightUpdates().iterator();
        this.updateLighting(i, j, lightingProvider, LightType.SKY, bitSet, bitSet2, iterator, packet.isNotEdge());
        BitSet bitSet3 = packet.getBlockLightMask();
        BitSet bitSet4 = packet.getFilledBlockLightMask();
        Iterator<byte[]> iterator2 = packet.getBlockLightUpdates().iterator();
        this.updateLighting(i, j, lightingProvider, LightType.BLOCK, bitSet3, bitSet4, iterator2, packet.isNotEdge());
    }

    @Override
    public void onSetTradeOffers(SetTradeOffersS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        ScreenHandler screenHandler = this.client.player.currentScreenHandler;
        if (packet.getSyncId() == screenHandler.syncId && screenHandler instanceof MerchantScreenHandler) {
            MerchantScreenHandler merchantScreenHandler = (MerchantScreenHandler)screenHandler;
            merchantScreenHandler.setOffers(new TradeOfferList(packet.getOffers().toNbt()));
            merchantScreenHandler.setExperienceFromServer(packet.getExperience());
            merchantScreenHandler.setLevelProgress(packet.getLevelProgress());
            merchantScreenHandler.setCanLevel(packet.isLeveled());
            merchantScreenHandler.setRefreshTrades(packet.isRefreshable());
        }
    }

    @Override
    public void onChunkLoadDistance(ChunkLoadDistanceS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.chunkLoadDistance = packet.getDistance();
        this.world.getChunkManager().updateLoadDistance(packet.getDistance());
    }

    @Override
    public void onChunkRenderDistanceCenter(ChunkRenderDistanceCenterS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.world.getChunkManager().setChunkMapCenter(packet.getChunkX(), packet.getChunkZ());
    }

    @Override
    public void onPlayerActionResponse(PlayerActionResponseS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.client.interactionManager.processPlayerActionResponse(this.world, packet.getBlockPos(), packet.getBlockState(), packet.getAction(), packet.isApproved());
    }

    private void updateLighting(int chunkX, int chunkZ, LightingProvider provider, LightType type, BitSet bitSet, BitSet bitSet2, Iterator<byte[]> iterator, boolean bl) {
        for (int i = 0; i < provider.getHeight(); ++i) {
            int j = provider.getBottomY() + i;
            boolean bl2 = bitSet.get(i);
            boolean bl3 = bitSet2.get(i);
            if (!bl2 && !bl3) continue;
            provider.enqueueSectionData(type, ChunkSectionPos.from(chunkX, j, chunkZ), bl2 ? new ChunkNibbleArray((byte[])iterator.next().clone()) : new ChunkNibbleArray(), bl);
            this.world.scheduleBlockRenders(chunkX, j, chunkZ);
        }
    }

    @Override
    public ClientConnection getConnection() {
        return this.connection;
    }

    public Collection<PlayerListEntry> getPlayerList() {
        return this.playerListEntries.values();
    }

    public Collection<UUID> getPlayerUuids() {
        return this.playerListEntries.keySet();
    }

    @Nullable
    public PlayerListEntry getPlayerListEntry(UUID uuid) {
        return this.playerListEntries.get(uuid);
    }

    @Nullable
    public PlayerListEntry getPlayerListEntry(String profileName) {
        for (PlayerListEntry playerListEntry : this.playerListEntries.values()) {
            if (!playerListEntry.getProfile().getName().equals(profileName)) continue;
            return playerListEntry;
        }
        return null;
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    public ClientAdvancementManager getAdvancementHandler() {
        return this.advancementHandler;
    }

    public CommandDispatcher<CommandSource> getCommandDispatcher() {
        return this.commandDispatcher;
    }

    public ClientWorld getWorld() {
        return this.world;
    }

    public TagManager getTagManager() {
        return this.tagManager;
    }

    public DataQueryHandler getDataQueryHandler() {
        return this.dataQueryHandler;
    }

    public UUID getSessionId() {
        return this.sessionId;
    }

    public Set<RegistryKey<World>> getWorldKeys() {
        return this.worldKeys;
    }

    public DynamicRegistryManager getRegistryManager() {
        return this.registryManager;
    }
}

