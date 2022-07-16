/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.option;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.AoMode;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.option.ChatVisibility;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.NarratorMode;
import net.minecraft.client.option.Option;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.option.StickyKeyBinding;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.tutorial.TutorialStep;
import net.minecraft.client.util.InputUtil;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Arm;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.world.Difficulty;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(value=EnvType.CLIENT)
public class GameOptions {
    static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    private static final TypeToken<List<String>> STRING_LIST_TYPE = new TypeToken<List<String>>(){};
    public static final int field_32149 = 2;
    public static final int field_32150 = 4;
    public static final int field_32152 = 8;
    public static final int field_32153 = 12;
    public static final int field_32154 = 16;
    public static final int field_32155 = 32;
    private static final Splitter COLON_SPLITTER = Splitter.on(':').limit(2);
    private static final float field_32151 = 1.0f;
    public boolean monochromeLogo;
    public double mouseSensitivity = 0.5;
    public int viewDistance;
    public float entityDistanceScaling = 1.0f;
    public int maxFps = 120;
    public CloudRenderMode cloudRenderMode = CloudRenderMode.FANCY;
    public GraphicsMode graphicsMode = GraphicsMode.FANCY;
    public AoMode ao = AoMode.MAX;
    public List<String> resourcePacks = Lists.newArrayList();
    public List<String> incompatibleResourcePacks = Lists.newArrayList();
    public ChatVisibility chatVisibility = ChatVisibility.FULL;
    public double chatOpacity = 1.0;
    public double chatLineSpacing;
    public double textBackgroundOpacity = 0.5;
    @Nullable
    public String fullscreenResolution;
    public boolean hideServerAddress;
    public boolean advancedItemTooltips;
    public boolean pauseOnLostFocus = true;
    private final Set<PlayerModelPart> enabledPlayerModelParts = EnumSet.allOf(PlayerModelPart.class);
    public Arm mainArm = Arm.RIGHT;
    public int overrideWidth;
    public int overrideHeight;
    public boolean heldItemTooltips = true;
    public double chatScale = 1.0;
    public double chatWidth = 1.0;
    public double chatHeightUnfocused = 0.44366195797920227;
    public double chatHeightFocused = 1.0;
    public double chatDelay;
    public int mipmapLevels = 4;
    private final Object2FloatMap<SoundCategory> soundVolumeLevels = Util.make(new Object2FloatOpenHashMap(), object2FloatOpenHashMap -> object2FloatOpenHashMap.defaultReturnValue(1.0f));
    public boolean useNativeTransport = true;
    public AttackIndicator attackIndicator = AttackIndicator.CROSSHAIR;
    public TutorialStep tutorialStep = TutorialStep.MOVEMENT;
    public boolean joinedFirstServer = false;
    public boolean hideBundleTutorial = false;
    public int biomeBlendRadius = 2;
    public double mouseWheelSensitivity = 1.0;
    public boolean rawMouseInput = true;
    public int glDebugVerbosity = 1;
    public boolean autoJump = true;
    public boolean autoSuggestions = true;
    public boolean chatColors = true;
    public boolean chatLinks = true;
    public boolean chatLinksPrompt = true;
    public boolean enableVsync = true;
    public boolean entityShadows = true;
    public boolean forceUnicodeFont;
    public boolean invertYMouse;
    public boolean discreteMouseScroll;
    public boolean realmsNotifications = true;
    public boolean reducedDebugInfo;
    public boolean snooperEnabled = true;
    public boolean showSubtitles;
    public boolean backgroundForChatOnly = true;
    public boolean touchscreen;
    public boolean fullscreen;
    public boolean bobView = true;
    public boolean sneakToggled;
    public boolean sprintToggled;
    public boolean skipMultiplayerWarning;
    public boolean hideMatchedNames = true;
    /**
     * A key binding for moving forward.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_W the W key} by default.
     */
    public final KeyBinding keyForward = new KeyBinding("key.forward", GLFW.GLFW_KEY_W, "key.categories.movement");
    /**
     * A key binding for moving left.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_A the A key} by default.
     */
    public final KeyBinding keyLeft = new KeyBinding("key.left", GLFW.GLFW_KEY_A, "key.categories.movement");
    /**
     * A key binding for moving backward.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_S the S key} by default.
     */
    public final KeyBinding keyBack = new KeyBinding("key.back", GLFW.GLFW_KEY_S, "key.categories.movement");
    /**
     * A key binding for moving right.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_D the D key} by default.
     */
    public final KeyBinding keyRight = new KeyBinding("key.right", GLFW.GLFW_KEY_D, "key.categories.movement");
    /**
     * A key binding for jumping.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_SPACE the space key} by default.
     */
    public final KeyBinding keyJump = new KeyBinding("key.jump", GLFW.GLFW_KEY_SPACE, "key.categories.movement");
    /**
     * A key binding for sneaking.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_LEFT_SHIFT the left shift key} by default.
     */
    public final KeyBinding keySneak = new StickyKeyBinding("key.sneak", GLFW.GLFW_KEY_LEFT_SHIFT, "key.categories.movement", () -> this.sneakToggled);
    /**
     * A key binding for sprinting.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_LEFT_CONTROL the left control key} by default.
     */
    public final KeyBinding keySprint = new StickyKeyBinding("key.sprint", GLFW.GLFW_KEY_LEFT_CONTROL, "key.categories.movement", () -> this.sprintToggled);
    /**
     * A key binding for opening {@linkplain net.minecraft.client.gui.screen.ingame.InventoryScreen the inventory screen}.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_E the E key} by default.
     */
    public final KeyBinding keyInventory = new KeyBinding("key.inventory", GLFW.GLFW_KEY_E, "key.categories.inventory");
    /**
     * A key binding for swapping the items in the selected slot and the off hand.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_F the F key} by default.
     * 
     * <p>The selected slot is the slot the mouse is over when in a screen.
     * Otherwise, it is the main hand.
     */
    public final KeyBinding keySwapHands = new KeyBinding("key.swapOffhand", GLFW.GLFW_KEY_F, "key.categories.inventory");
    /**
     * A key binding for dropping the item in the selected slot.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_Q the Q key} by default.
     * 
     * <p>The selected slot is the slot the mouse is over when in a screen.
     * Otherwise, it is the main hand.
     */
    public final KeyBinding keyDrop = new KeyBinding("key.drop", GLFW.GLFW_KEY_Q, "key.categories.inventory");
    /**
     * A key binding for using an item, such as placing a block.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_RIGHT the right mouse button} by default.
     */
    public final KeyBinding keyUse = new KeyBinding("key.use", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_RIGHT, "key.categories.gameplay");
    /**
     * A key binding for attacking an entity or breaking a block.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_LEFT the left mouse button} by default.
     */
    public final KeyBinding keyAttack = new KeyBinding("key.attack", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_LEFT, "key.categories.gameplay");
    /**
     * A key binding for holding an item corresponding to the {@linkplain net.minecraft.entity.Entity#getPickBlockStack() entity}
     * or {@linkplain net.minecraft.block.Block#getPickStack(net.minecraft.world.BlockView,
     * net.minecraft.util.math.BlockPos, net.minecraft.block.BlockState) block} the player is looking at.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_MIDDLE the middle mouse button} by default.
     */
    public final KeyBinding keyPickItem = new KeyBinding("key.pickItem", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, "key.categories.gameplay");
    /**
     * A key binding for opening {@linkplain net.minecraft.client.gui.screen.ChatScreen the chat screen}.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_T the T key} by default.
     */
    public final KeyBinding keyChat = new KeyBinding("key.chat", GLFW.GLFW_KEY_T, "key.categories.multiplayer");
    /**
     * A key binding for displaying {@linkplain net.minecraft.client.gui.hud.PlayerListHud the player list}.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_TAB the tab key} by default.
     */
    public final KeyBinding keyPlayerList = new KeyBinding("key.playerlist", GLFW.GLFW_KEY_TAB, "key.categories.multiplayer");
    /**
     * A key binding for opening {@linkplain net.minecraft.client.gui.screen.ChatScreen
     * the chat screen} with the {@code /} already typed.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_SLASH the slash key} by default.
     */
    public final KeyBinding keyCommand = new KeyBinding("key.command", GLFW.GLFW_KEY_SLASH, "key.categories.multiplayer");
    /**
     * A key binding for opening {@linkplain net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen the social interactions screen}.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_P the P key} by default.
     */
    public final KeyBinding keySocialInteractions = new KeyBinding("key.socialInteractions", GLFW.GLFW_KEY_P, "key.categories.multiplayer");
    /**
     * A key binding for taking a screenshot.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_F2 the F2 key} by default.
     */
    public final KeyBinding keyScreenshot = new KeyBinding("key.screenshot", GLFW.GLFW_KEY_F2, "key.categories.misc");
    /**
     * A key binding for toggling perspective.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_F5 the F5 key} by default.
     */
    public final KeyBinding keyTogglePerspective = new KeyBinding("key.togglePerspective", GLFW.GLFW_KEY_F5, "key.categories.misc");
    /**
     * A key binding for toggling smooth camera.
     * Not bound to any keys by default.
     */
    public final KeyBinding keySmoothCamera = new KeyBinding("key.smoothCamera", InputUtil.UNKNOWN_KEY.getCode(), "key.categories.misc");
    /**
     * A key binding for toggling fullscreen.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_F11 the F11 key} by default.
     */
    public final KeyBinding keyFullscreen = new KeyBinding("key.fullscreen", GLFW.GLFW_KEY_F11, "key.categories.misc");
    /**
     * A key binding for highlighting players in {@linkplain net.minecraft.world.GameMode#SPECTATOR spectator mode}.
     * Not bound to any keys by default.
     */
    public final KeyBinding keySpectatorOutlines = new KeyBinding("key.spectatorOutlines", InputUtil.UNKNOWN_KEY.getCode(), "key.categories.misc");
    /**
     * A key binding for opening {@linkplain net.minecraft.client.gui.screen.advancement.AdvancementsScreen the advancements screen}.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_L the L key} by default.
     */
    public final KeyBinding keyAdvancements = new KeyBinding("key.advancements", GLFW.GLFW_KEY_L, "key.categories.misc");
    /**
     * Key bindings for selecting hotbar slots.
     * Bound to the corresponding number keys (from {@linkplain
     * org.lwjgl.glfw.GLFW#GLFW_KEY_1 the 1 key} to {@linkplain
     * org.lwjgl.glfw.GLFW#GLFW_KEY_9 the 9 key}) by default.
     */
    public final KeyBinding[] keysHotbar = new KeyBinding[]{new KeyBinding("key.hotbar.1", GLFW.GLFW_KEY_1, "key.categories.inventory"), new KeyBinding("key.hotbar.2", GLFW.GLFW_KEY_2, "key.categories.inventory"), new KeyBinding("key.hotbar.3", GLFW.GLFW_KEY_3, "key.categories.inventory"), new KeyBinding("key.hotbar.4", GLFW.GLFW_KEY_4, "key.categories.inventory"), new KeyBinding("key.hotbar.5", GLFW.GLFW_KEY_5, "key.categories.inventory"), new KeyBinding("key.hotbar.6", GLFW.GLFW_KEY_6, "key.categories.inventory"), new KeyBinding("key.hotbar.7", GLFW.GLFW_KEY_7, "key.categories.inventory"), new KeyBinding("key.hotbar.8", GLFW.GLFW_KEY_8, "key.categories.inventory"), new KeyBinding("key.hotbar.9", GLFW.GLFW_KEY_9, "key.categories.inventory")};
    /**
     * A key binding for saving the hotbar items in {@linkplain net.minecraft.world.GameMode#CREATIVE creative mode}.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_C the C key} by default.
     */
    public final KeyBinding keySaveToolbarActivator = new KeyBinding("key.saveToolbarActivator", GLFW.GLFW_KEY_C, "key.categories.creative");
    /**
     * A key binding for loading the hotbar items in {@linkplain net.minecraft.world.GameMode#CREATIVE creative mode}.
     * Bound to {@linkplain org.lwjgl.glfw.GLFW#GLFW_KEY_X the X key} by default.
     */
    public final KeyBinding keyLoadToolbarActivator = new KeyBinding("key.loadToolbarActivator", GLFW.GLFW_KEY_X, "key.categories.creative");
    /**
     * An array of all key bindings.
     * 
     * <p>Key bindings in this array are shown and can be configured in
     * {@linkplain net.minecraft.client.gui.screen.option.ControlsOptionsScreen
     * the controls options screen}.
     */
    public final KeyBinding[] keysAll = ArrayUtils.addAll(new KeyBinding[]{this.keyAttack, this.keyUse, this.keyForward, this.keyLeft, this.keyBack, this.keyRight, this.keyJump, this.keySneak, this.keySprint, this.keyDrop, this.keyInventory, this.keyChat, this.keyPlayerList, this.keyPickItem, this.keyCommand, this.keySocialInteractions, this.keyScreenshot, this.keyTogglePerspective, this.keySmoothCamera, this.keyFullscreen, this.keySpectatorOutlines, this.keySwapHands, this.keySaveToolbarActivator, this.keyLoadToolbarActivator, this.keyAdvancements}, this.keysHotbar);
    protected MinecraftClient client;
    private final File optionsFile;
    public Difficulty difficulty = Difficulty.NORMAL;
    public boolean hudHidden;
    private Perspective perspective = Perspective.FIRST_PERSON;
    public boolean debugEnabled;
    public boolean debugProfilerEnabled;
    public boolean debugTpsEnabled;
    public String lastServer = "";
    public boolean smoothCameraEnabled;
    public double fov = 70.0;
    public float distortionEffectScale = 1.0f;
    public float fovEffectScale = 1.0f;
    public double gamma;
    public int guiScale;
    public ParticlesMode particles = ParticlesMode.ALL;
    public NarratorMode narrator = NarratorMode.OFF;
    public String language = "en_us";
    public boolean syncChunkWrites;

    public GameOptions(MinecraftClient client, File optionsFile) {
        this.client = client;
        this.optionsFile = new File(optionsFile, "options.txt");
        if (client.is64Bit() && Runtime.getRuntime().maxMemory() >= 1000000000L) {
            Option.RENDER_DISTANCE.setMax(32.0f);
        } else {
            Option.RENDER_DISTANCE.setMax(16.0f);
        }
        this.viewDistance = client.is64Bit() ? 12 : 8;
        this.syncChunkWrites = Util.getOperatingSystem() == Util.OperatingSystem.WINDOWS;
        this.load();
    }

    public float getTextBackgroundOpacity(float fallback) {
        return this.backgroundForChatOnly ? fallback : (float)this.textBackgroundOpacity;
    }

    public int getTextBackgroundColor(float fallbackOpacity) {
        return (int)(this.getTextBackgroundOpacity(fallbackOpacity) * 255.0f) << 24 & 0xFF000000;
    }

    public int getTextBackgroundColor(int fallbackColor) {
        return this.backgroundForChatOnly ? fallbackColor : (int)(this.textBackgroundOpacity * 255.0) << 24 & 0xFF000000;
    }

    public void setKeyCode(KeyBinding key, InputUtil.Key code) {
        key.setBoundKey(code);
        this.write();
    }

    private void accept(Visitor visitor) {
        this.autoJump = visitor.visitBoolean("autoJump", this.autoJump);
        this.autoSuggestions = visitor.visitBoolean("autoSuggestions", this.autoSuggestions);
        this.chatColors = visitor.visitBoolean("chatColors", this.chatColors);
        this.chatLinks = visitor.visitBoolean("chatLinks", this.chatLinks);
        this.chatLinksPrompt = visitor.visitBoolean("chatLinksPrompt", this.chatLinksPrompt);
        this.enableVsync = visitor.visitBoolean("enableVsync", this.enableVsync);
        this.entityShadows = visitor.visitBoolean("entityShadows", this.entityShadows);
        this.forceUnicodeFont = visitor.visitBoolean("forceUnicodeFont", this.forceUnicodeFont);
        this.discreteMouseScroll = visitor.visitBoolean("discrete_mouse_scroll", this.discreteMouseScroll);
        this.invertYMouse = visitor.visitBoolean("invertYMouse", this.invertYMouse);
        this.realmsNotifications = visitor.visitBoolean("realmsNotifications", this.realmsNotifications);
        this.reducedDebugInfo = visitor.visitBoolean("reducedDebugInfo", this.reducedDebugInfo);
        this.snooperEnabled = visitor.visitBoolean("snooperEnabled", this.snooperEnabled);
        this.showSubtitles = visitor.visitBoolean("showSubtitles", this.showSubtitles);
        this.touchscreen = visitor.visitBoolean("touchscreen", this.touchscreen);
        this.fullscreen = visitor.visitBoolean("fullscreen", this.fullscreen);
        this.bobView = visitor.visitBoolean("bobView", this.bobView);
        this.sneakToggled = visitor.visitBoolean("toggleCrouch", this.sneakToggled);
        this.sprintToggled = visitor.visitBoolean("toggleSprint", this.sprintToggled);
        this.monochromeLogo = visitor.visitBoolean("darkMojangStudiosBackground", this.monochromeLogo);
        this.mouseSensitivity = visitor.visitDouble("mouseSensitivity", this.mouseSensitivity);
        this.fov = visitor.visitDouble("fov", (this.fov - 70.0) / 40.0) * 40.0 + 70.0;
        this.distortionEffectScale = visitor.visitFloat("screenEffectScale", this.distortionEffectScale);
        this.fovEffectScale = visitor.visitFloat("fovEffectScale", this.fovEffectScale);
        this.gamma = visitor.visitDouble("gamma", this.gamma);
        this.viewDistance = visitor.visitInt("renderDistance", this.viewDistance);
        this.entityDistanceScaling = visitor.visitFloat("entityDistanceScaling", this.entityDistanceScaling);
        this.guiScale = visitor.visitInt("guiScale", this.guiScale);
        this.particles = visitor.visitObject("particles", this.particles, ParticlesMode::byId, ParticlesMode::getId);
        this.maxFps = visitor.visitInt("maxFps", this.maxFps);
        this.difficulty = visitor.visitObject("difficulty", this.difficulty, Difficulty::byOrdinal, Difficulty::getId);
        this.graphicsMode = visitor.visitObject("graphicsMode", this.graphicsMode, GraphicsMode::byId, GraphicsMode::getId);
        this.ao = visitor.visitObject("ao", this.ao, GameOptions::loadAo, ao -> Integer.toString(ao.getId()));
        this.biomeBlendRadius = visitor.visitInt("biomeBlendRadius", this.biomeBlendRadius);
        this.cloudRenderMode = visitor.visitObject("renderClouds", this.cloudRenderMode, GameOptions::loadCloudRenderMode, GameOptions::saveCloudRenderMode);
        this.resourcePacks = visitor.visitObject("resourcePacks", this.resourcePacks, GameOptions::parseList, GSON::toJson);
        this.incompatibleResourcePacks = visitor.visitObject("incompatibleResourcePacks", this.incompatibleResourcePacks, GameOptions::parseList, GSON::toJson);
        this.lastServer = visitor.visitString("lastServer", this.lastServer);
        this.language = visitor.visitString("lang", this.language);
        this.chatVisibility = visitor.visitObject("chatVisibility", this.chatVisibility, ChatVisibility::byId, ChatVisibility::getId);
        this.chatOpacity = visitor.visitDouble("chatOpacity", this.chatOpacity);
        this.chatLineSpacing = visitor.visitDouble("chatLineSpacing", this.chatLineSpacing);
        this.textBackgroundOpacity = visitor.visitDouble("textBackgroundOpacity", this.textBackgroundOpacity);
        this.backgroundForChatOnly = visitor.visitBoolean("backgroundForChatOnly", this.backgroundForChatOnly);
        this.hideServerAddress = visitor.visitBoolean("hideServerAddress", this.hideServerAddress);
        this.advancedItemTooltips = visitor.visitBoolean("advancedItemTooltips", this.advancedItemTooltips);
        this.pauseOnLostFocus = visitor.visitBoolean("pauseOnLostFocus", this.pauseOnLostFocus);
        this.overrideWidth = visitor.visitInt("overrideWidth", this.overrideWidth);
        this.overrideHeight = visitor.visitInt("overrideHeight", this.overrideHeight);
        this.heldItemTooltips = visitor.visitBoolean("heldItemTooltips", this.heldItemTooltips);
        this.chatHeightFocused = visitor.visitDouble("chatHeightFocused", this.chatHeightFocused);
        this.chatDelay = visitor.visitDouble("chatDelay", this.chatDelay);
        this.chatHeightUnfocused = visitor.visitDouble("chatHeightUnfocused", this.chatHeightUnfocused);
        this.chatScale = visitor.visitDouble("chatScale", this.chatScale);
        this.chatWidth = visitor.visitDouble("chatWidth", this.chatWidth);
        this.mipmapLevels = visitor.visitInt("mipmapLevels", this.mipmapLevels);
        this.useNativeTransport = visitor.visitBoolean("useNativeTransport", this.useNativeTransport);
        this.mainArm = visitor.visitObject("mainHand", this.mainArm, GameOptions::loadArm, GameOptions::saveArm);
        this.attackIndicator = visitor.visitObject("attackIndicator", this.attackIndicator, AttackIndicator::byId, AttackIndicator::getId);
        this.narrator = visitor.visitObject("narrator", this.narrator, NarratorMode::byId, NarratorMode::getId);
        this.tutorialStep = visitor.visitObject("tutorialStep", this.tutorialStep, TutorialStep::byName, TutorialStep::getName);
        this.mouseWheelSensitivity = visitor.visitDouble("mouseWheelSensitivity", this.mouseWheelSensitivity);
        this.rawMouseInput = visitor.visitBoolean("rawMouseInput", this.rawMouseInput);
        this.glDebugVerbosity = visitor.visitInt("glDebugVerbosity", this.glDebugVerbosity);
        this.skipMultiplayerWarning = visitor.visitBoolean("skipMultiplayerWarning", this.skipMultiplayerWarning);
        this.hideMatchedNames = visitor.visitBoolean("hideMatchedNames", this.hideMatchedNames);
        this.joinedFirstServer = visitor.visitBoolean("joinedFirstServer", this.joinedFirstServer);
        this.hideBundleTutorial = visitor.visitBoolean("hideBundleTutorial", this.hideBundleTutorial);
        this.syncChunkWrites = visitor.visitBoolean("syncChunkWrites", this.syncChunkWrites);
        for (KeyBinding keyBinding : this.keysAll) {
            String string2;
            String string = keyBinding.getBoundKeyTranslationKey();
            if (string.equals(string2 = visitor.visitString("key_" + keyBinding.getTranslationKey(), string))) continue;
            keyBinding.setBoundKey(InputUtil.fromTranslationKey(string2));
        }
        for (SoundCategory soundCategory : SoundCategory.values()) {
            this.soundVolumeLevels.computeFloat(soundCategory, (category, currentLevel) -> Float.valueOf(visitor.visitFloat("soundCategory_" + category.getName(), currentLevel != null ? currentLevel.floatValue() : 1.0f)));
        }
        for (PlayerModelPart playerModelPart : PlayerModelPart.values()) {
            boolean string = this.enabledPlayerModelParts.contains((Object)playerModelPart);
            boolean string2 = visitor.visitBoolean("modelPart_" + playerModelPart.getName(), string);
            if (string2 == string) continue;
            this.setPlayerModelPart(playerModelPart, string2);
        }
    }

    public void load() {
        try {
            if (!this.optionsFile.exists()) {
                return;
            }
            this.soundVolumeLevels.clear();
            NbtCompound nbtCompound = new NbtCompound();
            try (Object bufferedReader = Files.newReader(this.optionsFile, Charsets.UTF_8);){
                ((BufferedReader)bufferedReader).lines().forEach(line -> {
                    try {
                        Iterator<String> iterator = COLON_SPLITTER.split((CharSequence)line).iterator();
                        nbtCompound.putString(iterator.next(), iterator.next());
                    }
                    catch (Exception iterator) {
                        LOGGER.warn("Skipping bad option: {}", line);
                    }
                });
            }
            bufferedReader = this.update(nbtCompound);
            if (!((NbtCompound)bufferedReader).contains("graphicsMode") && ((NbtCompound)bufferedReader).contains("fancyGraphics")) {
                this.graphicsMode = GameOptions.isTrue(((NbtCompound)bufferedReader).getString("fancyGraphics")) ? GraphicsMode.FANCY : GraphicsMode.FAST;
            }
            this.accept(new Visitor(){
                final /* synthetic */ NbtCompound field_28778;
                {
                    this.field_28778 = nbtCompound;
                }

                @Nullable
                private String find(String key) {
                    return this.field_28778.contains(key) ? this.field_28778.getString(key) : null;
                }

                @Override
                public int visitInt(String key, int current) {
                    String string = this.find(key);
                    if (string != null) {
                        try {
                            return Integer.parseInt(string);
                        }
                        catch (NumberFormatException numberFormatException) {
                            LOGGER.warn("Invalid integer value for option {} = {}", (Object)key, (Object)string, (Object)numberFormatException);
                        }
                    }
                    return current;
                }

                @Override
                public boolean visitBoolean(String key, boolean current) {
                    String string = this.find(key);
                    return string != null ? GameOptions.isTrue(string) : current;
                }

                @Override
                public String visitString(String key, String current) {
                    return MoreObjects.firstNonNull(this.find(key), current);
                }

                @Override
                public double visitDouble(String key, double current) {
                    String string = this.find(key);
                    if (string != null) {
                        if (GameOptions.isTrue(string)) {
                            return 1.0;
                        }
                        if (GameOptions.isFalse(string)) {
                            return 0.0;
                        }
                        try {
                            return Double.parseDouble(string);
                        }
                        catch (NumberFormatException numberFormatException) {
                            LOGGER.warn("Invalid floating point value for option {} = {}", (Object)key, (Object)string, (Object)numberFormatException);
                        }
                    }
                    return current;
                }

                @Override
                public float visitFloat(String key, float current) {
                    String string = this.find(key);
                    if (string != null) {
                        if (GameOptions.isTrue(string)) {
                            return 1.0f;
                        }
                        if (GameOptions.isFalse(string)) {
                            return 0.0f;
                        }
                        try {
                            return Float.parseFloat(string);
                        }
                        catch (NumberFormatException numberFormatException) {
                            LOGGER.warn("Invalid floating point value for option {} = {}", (Object)key, (Object)string, (Object)numberFormatException);
                        }
                    }
                    return current;
                }

                @Override
                public <T> T visitObject(String key, T current, Function<String, T> decoder, Function<T, String> encoder) {
                    String string = this.find(key);
                    return string == null ? current : decoder.apply(string);
                }

                @Override
                public <T> T visitObject(String key, T current, IntFunction<T> decoder, ToIntFunction<T> encoder) {
                    String string = this.find(key);
                    if (string != null) {
                        try {
                            return decoder.apply(Integer.parseInt(string));
                        }
                        catch (Exception exception) {
                            LOGGER.warn("Invalid integer value for option {} = {}", (Object)key, (Object)string, (Object)exception);
                        }
                    }
                    return current;
                }
            });
            if (((NbtCompound)bufferedReader).contains("fullscreenResolution")) {
                this.fullscreenResolution = ((NbtCompound)bufferedReader).getString("fullscreenResolution");
            }
            if (this.client.getWindow() != null) {
                this.client.getWindow().setFramerateLimit(this.maxFps);
            }
            KeyBinding.updateKeysByCode();
        }
        catch (Exception nbtCompound) {
            LOGGER.error("Failed to load options", (Throwable)nbtCompound);
        }
    }

    static boolean isTrue(String value) {
        return "true".equals(value);
    }

    static boolean isFalse(String value) {
        return "false".equals(value);
    }

    private NbtCompound update(NbtCompound nbt) {
        int i = 0;
        try {
            i = Integer.parseInt(nbt.getString("version"));
        }
        catch (RuntimeException runtimeException) {
            // empty catch block
        }
        return NbtHelper.update(this.client.getDataFixer(), DataFixTypes.OPTIONS, nbt, i);
    }

    public void write() {
        try (final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8));){
            printWriter.println("version:" + SharedConstants.getGameVersion().getWorldVersion());
            this.accept(new Visitor(){

                public void print(String key) {
                    printWriter.print(key);
                    printWriter.print(':');
                }

                @Override
                public int visitInt(String key, int current) {
                    this.print(key);
                    printWriter.println(current);
                    return current;
                }

                @Override
                public boolean visitBoolean(String key, boolean current) {
                    this.print(key);
                    printWriter.println(current);
                    return current;
                }

                @Override
                public String visitString(String key, String current) {
                    this.print(key);
                    printWriter.println(current);
                    return current;
                }

                @Override
                public double visitDouble(String key, double current) {
                    this.print(key);
                    printWriter.println(current);
                    return current;
                }

                @Override
                public float visitFloat(String key, float current) {
                    this.print(key);
                    printWriter.println(current);
                    return current;
                }

                @Override
                public <T> T visitObject(String key, T current, Function<String, T> decoder, Function<T, String> encoder) {
                    this.print(key);
                    printWriter.println(encoder.apply(current));
                    return current;
                }

                @Override
                public <T> T visitObject(String key, T current, IntFunction<T> decoder, ToIntFunction<T> encoder) {
                    this.print(key);
                    printWriter.println(encoder.applyAsInt(current));
                    return current;
                }
            });
            if (this.client.getWindow().getVideoMode().isPresent()) {
                printWriter.println("fullscreenResolution:" + this.client.getWindow().getVideoMode().get().asString());
            }
        }
        catch (Exception printWriter2) {
            LOGGER.error("Failed to save options", (Throwable)printWriter2);
        }
        this.sendClientSettings();
    }

    public float getSoundVolume(SoundCategory category) {
        return this.soundVolumeLevels.getFloat((Object)category);
    }

    public void setSoundVolume(SoundCategory category, float volume) {
        this.soundVolumeLevels.put(category, volume);
        this.client.getSoundManager().updateSoundVolume(category, volume);
    }

    /**
     * Sends the current client settings to the server if the client is
     * connected to a server.
     * 
     * <p>Called when a player joins the game or when client settings are
     * changed.
     */
    public void sendClientSettings() {
        if (this.client.player != null) {
            int i = 0;
            for (PlayerModelPart playerModelPart : this.enabledPlayerModelParts) {
                i |= playerModelPart.getBitFlag();
            }
            this.client.player.networkHandler.sendPacket(new ClientSettingsC2SPacket(this.language, this.viewDistance, this.chatVisibility, this.chatColors, i, this.mainArm, this.client.shouldFilterText()));
        }
    }

    private void setPlayerModelPart(PlayerModelPart part, boolean enabled) {
        if (enabled) {
            this.enabledPlayerModelParts.add(part);
        } else {
            this.enabledPlayerModelParts.remove((Object)part);
        }
    }

    public boolean isPlayerModelPartEnabled(PlayerModelPart part) {
        return this.enabledPlayerModelParts.contains((Object)part);
    }

    public void togglePlayerModelPart(PlayerModelPart part, boolean enabled) {
        this.setPlayerModelPart(part, enabled);
        this.sendClientSettings();
    }

    public CloudRenderMode getCloudRenderMode() {
        if (this.viewDistance >= 4) {
            return this.cloudRenderMode;
        }
        return CloudRenderMode.OFF;
    }

    public boolean shouldUseNativeTransport() {
        return this.useNativeTransport;
    }

    public void addResourcePackProfilesToManager(ResourcePackManager manager) {
        LinkedHashSet<String> set = Sets.newLinkedHashSet();
        Iterator<String> iterator = this.resourcePacks.iterator();
        while (iterator.hasNext()) {
            String string = iterator.next();
            ResourcePackProfile resourcePackProfile = manager.getProfile(string);
            if (resourcePackProfile == null && !string.startsWith("file/")) {
                resourcePackProfile = manager.getProfile("file/" + string);
            }
            if (resourcePackProfile == null) {
                LOGGER.warn("Removed resource pack {} from options because it doesn't seem to exist anymore", (Object)string);
                iterator.remove();
                continue;
            }
            if (!resourcePackProfile.getCompatibility().isCompatible() && !this.incompatibleResourcePacks.contains(string)) {
                LOGGER.warn("Removed resource pack {} from options because it is no longer compatible", (Object)string);
                iterator.remove();
                continue;
            }
            if (resourcePackProfile.getCompatibility().isCompatible() && this.incompatibleResourcePacks.contains(string)) {
                LOGGER.info("Removed resource pack {} from incompatibility list because it's now compatible", (Object)string);
                this.incompatibleResourcePacks.remove(string);
                continue;
            }
            set.add(resourcePackProfile.getName());
        }
        manager.setEnabledProfiles(set);
    }

    public Perspective getPerspective() {
        return this.perspective;
    }

    public void setPerspective(Perspective perspective) {
        this.perspective = perspective;
    }

    private static List<String> parseList(String content) {
        List<String> list = JsonHelper.deserialize(GSON, content, STRING_LIST_TYPE);
        return list != null ? list : Lists.newArrayList();
    }

    private static CloudRenderMode loadCloudRenderMode(String literal) {
        switch (literal) {
            case "true": {
                return CloudRenderMode.FANCY;
            }
            case "fast": {
                return CloudRenderMode.FAST;
            }
        }
        return CloudRenderMode.OFF;
    }

    private static String saveCloudRenderMode(CloudRenderMode mode) {
        switch (mode) {
            case FANCY: {
                return "true";
            }
            case FAST: {
                return "fast";
            }
        }
        return "false";
    }

    private static AoMode loadAo(String value) {
        if (GameOptions.isTrue(value)) {
            return AoMode.MAX;
        }
        if (GameOptions.isFalse(value)) {
            return AoMode.OFF;
        }
        return AoMode.byId(Integer.parseInt(value));
    }

    private static Arm loadArm(String arm) {
        return "left".equals(arm) ? Arm.LEFT : Arm.RIGHT;
    }

    private static String saveArm(Arm arm) {
        return arm == Arm.LEFT ? "left" : "right";
    }

    public File getOptionsFile() {
        return this.optionsFile;
    }

    public String collectProfiledOptions() {
        ImmutableCollection immutableList = ((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)ImmutableList.builder().add(Pair.of("ao", String.valueOf((Object)this.ao)))).add(Pair.of("biomeBlendRadius", String.valueOf(this.biomeBlendRadius)))).add(Pair.of("enableVsync", String.valueOf(this.enableVsync)))).add(Pair.of("entityDistanceScaling", String.valueOf(this.entityDistanceScaling)))).add(Pair.of("entityShadows", String.valueOf(this.entityShadows)))).add(Pair.of("forceUnicodeFont", String.valueOf(this.forceUnicodeFont)))).add(Pair.of("fov", String.valueOf(this.fov)))).add(Pair.of("fovEffectScale", String.valueOf(this.fovEffectScale)))).add(Pair.of("fullscreen", String.valueOf(this.fullscreen)))).add(Pair.of("fullscreenResolution", String.valueOf(this.fullscreenResolution)))).add(Pair.of("gamma", String.valueOf(this.gamma)))).add(Pair.of("glDebugVerbosity", String.valueOf(this.glDebugVerbosity)))).add(Pair.of("graphicsMode", String.valueOf((Object)this.graphicsMode)))).add(Pair.of("guiScale", String.valueOf(this.guiScale)))).add(Pair.of("maxFps", String.valueOf(this.maxFps)))).add(Pair.of("mipmapLevels", String.valueOf(this.mipmapLevels)))).add(Pair.of("narrator", String.valueOf((Object)this.narrator)))).add(Pair.of("overrideHeight", String.valueOf(this.overrideHeight)))).add(Pair.of("overrideWidth", String.valueOf(this.overrideWidth)))).add(Pair.of("particles", String.valueOf((Object)this.particles)))).add(Pair.of("reducedDebugInfo", String.valueOf(this.reducedDebugInfo)))).add(Pair.of("renderClouds", String.valueOf((Object)this.cloudRenderMode)))).add(Pair.of("renderDistance", String.valueOf(this.viewDistance)))).add(Pair.of("resourcePacks", String.valueOf(this.resourcePacks)))).add(Pair.of("screenEffectScale", String.valueOf(this.distortionEffectScale)))).add(Pair.of("syncChunkWrites", String.valueOf(this.syncChunkWrites)))).add(Pair.of("useNativeTransport", String.valueOf(this.useNativeTransport)))).build();
        return immutableList.stream().map(option -> (String)option.getFirst() + ": " + (String)option.getSecond()).collect(Collectors.joining(System.lineSeparator()));
    }

    @Environment(value=EnvType.CLIENT)
    static interface Visitor {
        public int visitInt(String var1, int var2);

        public boolean visitBoolean(String var1, boolean var2);

        public String visitString(String var1, String var2);

        public double visitDouble(String var1, double var2);

        public float visitFloat(String var1, float var2);

        public <T> T visitObject(String var1, T var2, Function<String, T> var3, Function<T, String> var4);

        public <T> T visitObject(String var1, T var2, IntFunction<T> var3, ToIntFunction<T> var4);
    }
}
