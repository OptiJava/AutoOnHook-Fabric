/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client;

import com.google.common.base.MoreObjects;
import java.text.MessageFormat;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.GameModeSelectionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.screen.option.NarratorOptionsScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.NarratorMode;
import net.minecraft.client.option.Option;
import net.minecraft.client.util.Clipboard;
import net.minecraft.client.util.GlfwUtil;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(value=EnvType.CLIENT)
public class Keyboard {
    public static final int field_32143 = 10000;
    private final MinecraftClient client;
    private boolean repeatEvents;
    private final Clipboard clipboard = new Clipboard();
    private long debugCrashStartTime = -1L;
    private long debugCrashLastLogTime = -1L;
    private long debugCrashElapsedTime = -1L;
    private boolean switchF3State;

    public Keyboard(MinecraftClient client) {
        this.client = client;
    }

    private boolean processDebugKeys(int key) {
        switch (key) {
            case 69: {
                this.client.debugChunkInfo = !this.client.debugChunkInfo;
                this.debugFormattedLog("ChunkPath: {0}", this.client.debugChunkInfo ? "shown" : "hidden");
                return true;
            }
            case 76: {
                this.client.chunkCullingEnabled = !this.client.chunkCullingEnabled;
                this.debugFormattedLog("SmartCull: {0}", this.client.chunkCullingEnabled ? "enabled" : "disabled");
                return true;
            }
            case 85: {
                if (Screen.hasShiftDown()) {
                    this.client.worldRenderer.killFrustum();
                    this.debugFormattedLog("Killed frustum", new Object[0]);
                } else {
                    this.client.worldRenderer.captureFrustum();
                    this.debugFormattedLog("Captured frustum", new Object[0]);
                }
                return true;
            }
            case 86: {
                this.client.debugChunkOcclusion = !this.client.debugChunkOcclusion;
                this.debugFormattedLog("ChunkVisibility: {0}", this.client.debugChunkOcclusion ? "enabled" : "disabled");
                return true;
            }
            case 87: {
                this.client.wireFrame = !this.client.wireFrame;
                this.debugFormattedLog("WireFrame: {0}", this.client.wireFrame ? "enabled" : "disabled");
                return true;
            }
        }
        return false;
    }

    private void addDebugMessage(Formatting formatting, Text text) {
        this.client.inGameHud.getChatHud().addMessage(new LiteralText("").append(new TranslatableText("debug.prefix").formatted(formatting, Formatting.BOLD)).append(" ").append(text));
    }

    private void debugLog(Text text) {
        this.addDebugMessage(Formatting.YELLOW, text);
    }

    private void debugLog(String key, Object ... args) {
        this.debugLog(new TranslatableText(key, args));
    }

    private void debugError(String key, Object ... args) {
        this.addDebugMessage(Formatting.RED, new TranslatableText(key, args));
    }

    private void debugFormattedLog(String pattern, Object ... args) {
        this.debugLog(new LiteralText(MessageFormat.format(pattern, args)));
    }

    private boolean processF3(int key) {
        if (this.debugCrashStartTime > 0L && this.debugCrashStartTime < Util.getMeasuringTimeMs() - 100L) {
            return true;
        }
        switch (key) {
            case 65: {
                this.client.worldRenderer.reload();
                this.debugLog("debug.reload_chunks.message", new Object[0]);
                return true;
            }
            case 66: {
                boolean bl = !this.client.getEntityRenderDispatcher().shouldRenderHitboxes();
                this.client.getEntityRenderDispatcher().setRenderHitboxes(bl);
                this.debugLog(bl ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off", new Object[0]);
                return true;
            }
            case 68: {
                if (this.client.inGameHud != null) {
                    this.client.inGameHud.getChatHud().clear(false);
                }
                return true;
            }
            case 70: {
                Option.RENDER_DISTANCE.set(this.client.options, MathHelper.clamp((double)(this.client.options.viewDistance + (Screen.hasShiftDown() ? -1 : 1)), Option.RENDER_DISTANCE.getMin(), Option.RENDER_DISTANCE.getMax()));
                this.debugLog("debug.cycle_renderdistance.message", this.client.options.viewDistance);
                return true;
            }
            case 71: {
                boolean bl2 = this.client.debugRenderer.toggleShowChunkBorder();
                this.debugLog(bl2 ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off", new Object[0]);
                return true;
            }
            case 72: {
                this.client.options.advancedItemTooltips = !this.client.options.advancedItemTooltips;
                this.debugLog(this.client.options.advancedItemTooltips ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off", new Object[0]);
                this.client.options.write();
                return true;
            }
            case 73: {
                if (!this.client.player.hasReducedDebugInfo()) {
                    this.copyLookAt(this.client.player.hasPermissionLevel(2), !Screen.hasShiftDown());
                }
                return true;
            }
            case 78: {
                if (!this.client.player.hasPermissionLevel(2)) {
                    this.debugLog("debug.creative_spectator.error", new Object[0]);
                } else if (!this.client.player.isSpectator()) {
                    this.client.player.sendChatMessage("/gamemode spectator");
                } else {
                    this.client.player.sendChatMessage("/gamemode " + MoreObjects.firstNonNull(this.client.interactionManager.getPreviousGameMode(), GameMode.CREATIVE).getName());
                }
                return true;
            }
            case 293: {
                if (!this.client.player.hasPermissionLevel(2)) {
                    this.debugLog("debug.gamemodes.error", new Object[0]);
                } else {
                    this.client.setScreen(new GameModeSelectionScreen());
                }
                return true;
            }
            case 80: {
                this.client.options.pauseOnLostFocus = !this.client.options.pauseOnLostFocus;
                this.client.options.write();
                this.debugLog(this.client.options.pauseOnLostFocus ? "debug.pause_focus.on" : "debug.pause_focus.off", new Object[0]);
                return true;
            }
            case 81: {
                this.debugLog("debug.help.message", new Object[0]);
                ChatHud chatHud = this.client.inGameHud.getChatHud();
                chatHud.addMessage(new TranslatableText("debug.reload_chunks.help"));
                chatHud.addMessage(new TranslatableText("debug.show_hitboxes.help"));
                chatHud.addMessage(new TranslatableText("debug.copy_location.help"));
                chatHud.addMessage(new TranslatableText("debug.clear_chat.help"));
                chatHud.addMessage(new TranslatableText("debug.cycle_renderdistance.help"));
                chatHud.addMessage(new TranslatableText("debug.chunk_boundaries.help"));
                chatHud.addMessage(new TranslatableText("debug.advanced_tooltips.help"));
                chatHud.addMessage(new TranslatableText("debug.inspect.help"));
                chatHud.addMessage(new TranslatableText("debug.profiling.help"));
                chatHud.addMessage(new TranslatableText("debug.creative_spectator.help"));
                chatHud.addMessage(new TranslatableText("debug.pause_focus.help"));
                chatHud.addMessage(new TranslatableText("debug.help.help"));
                chatHud.addMessage(new TranslatableText("debug.reload_resourcepacks.help"));
                chatHud.addMessage(new TranslatableText("debug.pause.help"));
                chatHud.addMessage(new TranslatableText("debug.gamemodes.help"));
                return true;
            }
            case 84: {
                this.debugLog("debug.reload_resourcepacks.message", new Object[0]);
                this.client.reloadResources();
                return true;
            }
            case 76: {
                if (this.client.toggleDebugProfiler(this::debugLog)) {
                    this.debugLog("debug.profiling.start", 10);
                }
                return true;
            }
            case 67: {
                if (this.client.player.hasReducedDebugInfo()) {
                    return false;
                }
                ClientPlayNetworkHandler clientPlayNetworkHandler = this.client.player.networkHandler;
                if (clientPlayNetworkHandler == null) {
                    return false;
                }
                this.debugLog("debug.copy_location.message", new Object[0]);
                this.setClipboard(String.format(Locale.ROOT, "/execute in %s run tp @s %.2f %.2f %.2f %.2f %.2f", this.client.player.world.getRegistryKey().getValue(), this.client.player.getX(), this.client.player.getY(), this.client.player.getZ(), Float.valueOf(this.client.player.getYaw()), Float.valueOf(this.client.player.getPitch())));
                return true;
            }
        }
        return false;
    }

    private void copyLookAt(boolean hasQueryPermission, boolean queryServer) {
        HitResult hitResult = this.client.crosshairTarget;
        if (hitResult == null) {
            return;
        }
        switch (hitResult.getType()) {
            case BLOCK: {
                BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
                BlockState blockState = this.client.player.world.getBlockState(blockPos);
                if (hasQueryPermission) {
                    if (queryServer) {
                        this.client.player.networkHandler.getDataQueryHandler().queryBlockNbt(blockPos, nbt -> {
                            this.copyBlock(blockState, blockPos, (NbtCompound)nbt);
                            this.debugLog("debug.inspect.server.block", new Object[0]);
                        });
                        break;
                    }
                    BlockEntity blockEntity = this.client.player.world.getBlockEntity(blockPos);
                    NbtCompound nbtCompound = blockEntity != null ? blockEntity.writeNbt(new NbtCompound()) : null;
                    this.copyBlock(blockState, blockPos, nbtCompound);
                    this.debugLog("debug.inspect.client.block", new Object[0]);
                    break;
                }
                this.copyBlock(blockState, blockPos, null);
                this.debugLog("debug.inspect.client.block", new Object[0]);
                break;
            }
            case ENTITY: {
                Entity blockPos = ((EntityHitResult)hitResult).getEntity();
                Identifier blockState = Registry.ENTITY_TYPE.getId(blockPos.getType());
                if (hasQueryPermission) {
                    if (queryServer) {
                        this.client.player.networkHandler.getDataQueryHandler().queryEntityNbt(blockPos.getId(), nbt -> {
                            this.copyEntity(blockState, blockPos.getPos(), (NbtCompound)nbt);
                            this.debugLog("debug.inspect.server.entity", new Object[0]);
                        });
                        break;
                    }
                    NbtCompound blockEntity = blockPos.writeNbt(new NbtCompound());
                    this.copyEntity(blockState, blockPos.getPos(), blockEntity);
                    this.debugLog("debug.inspect.client.entity", new Object[0]);
                    break;
                }
                this.copyEntity(blockState, blockPos.getPos(), null);
                this.debugLog("debug.inspect.client.entity", new Object[0]);
                break;
            }
        }
    }

    private void copyBlock(BlockState state, BlockPos pos, @Nullable NbtCompound nbt) {
        if (nbt != null) {
            nbt.remove("x");
            nbt.remove("y");
            nbt.remove("z");
            nbt.remove("id");
        }
        StringBuilder stringBuilder = new StringBuilder(BlockArgumentParser.stringifyBlockState(state));
        if (nbt != null) {
            stringBuilder.append(nbt);
        }
        String string = String.format(Locale.ROOT, "/setblock %d %d %d %s", pos.getX(), pos.getY(), pos.getZ(), stringBuilder);
        this.setClipboard(string);
    }

    private void copyEntity(Identifier id, Vec3d pos, @Nullable NbtCompound nbt) {
        String string2;
        if (nbt != null) {
            nbt.remove("UUID");
            nbt.remove("Pos");
            nbt.remove("Dimension");
            String string = NbtHelper.toPrettyPrintedText(nbt).getString();
            string2 = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f %s", id.toString(), pos.x, pos.y, pos.z, string);
        } else {
            string2 = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f", id.toString(), pos.x, pos.y, pos.z);
        }
        this.setClipboard(string2);
    }

    public void onKey(long window, int key, int scancode, int action, int modifiers) {
        boolean bl2;
        if (window != this.client.getWindow().getHandle()) {
            return;
        }
        if (this.debugCrashStartTime > 0L) {
            if (!InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_C) || !InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_F3)) {
                this.debugCrashStartTime = -1L;
            }
        } else if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_C) && InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_F3)) {
            this.switchF3State = true;
            this.debugCrashStartTime = Util.getMeasuringTimeMs();
            this.debugCrashLastLogTime = Util.getMeasuringTimeMs();
            this.debugCrashElapsedTime = 0L;
        }
        Screen screen = this.client.currentScreen;
        if (!(action != 1 || this.client.currentScreen instanceof ControlsOptionsScreen && ((ControlsOptionsScreen)screen).time > Util.getMeasuringTimeMs() - 20L)) {
            if (this.client.options.keyFullscreen.matchesKey(key, scancode)) {
                this.client.getWindow().toggleFullscreen();
                this.client.options.fullscreen = this.client.getWindow().isFullscreen();
                this.client.options.write();
                return;
            }
            if (this.client.options.keyScreenshot.matchesKey(key, scancode)) {
                if (Screen.hasControlDown()) {
                    // empty if block
                }
                ScreenshotRecorder.saveScreenshot(this.client.runDirectory, this.client.getFramebuffer(), message -> this.client.execute(() -> this.client.inGameHud.getChatHud().addMessage((Text)message)));
                return;
            }
        }
        if (NarratorManager.INSTANCE.isActive()) {
            boolean bl;
            boolean bl3 = bl = screen == null || !(screen.getFocused() instanceof TextFieldWidget) || !((TextFieldWidget)screen.getFocused()).isActive();
            if (action != 0 && key == GLFW.GLFW_KEY_B && Screen.hasControlDown() && bl) {
                bl2 = this.client.options.narrator == NarratorMode.OFF;
                this.client.options.narrator = NarratorMode.byId(this.client.options.narrator.getId() + 1);
                NarratorManager.INSTANCE.addToast(this.client.options.narrator);
                if (screen instanceof NarratorOptionsScreen) {
                    ((NarratorOptionsScreen)screen).updateNarratorButtonText();
                }
                if (bl2 && screen != null) {
                    screen.applyNarratorModeChangeDelay();
                }
            }
        }
        if (screen != null) {
            boolean[] bl = new boolean[]{false};
            Screen.wrapScreenError(() -> {
                if (action == 1 || action == 2 && this.repeatEvents) {
                    screen.applyKeyPressNarratorDelay();
                    bls[0] = screen.keyPressed(key, scancode, modifiers);
                } else if (action == 0) {
                    bls[0] = screen.keyReleased(key, scancode, modifiers);
                }
            }, "keyPressed event handler", screen.getClass().getCanonicalName());
            if (bl[0]) {
                return;
            }
        }
        if (this.client.currentScreen == null || this.client.currentScreen.passEvents) {
            InputUtil.Key bl = InputUtil.fromKeyCode(key, scancode);
            if (action == 0) {
                KeyBinding.setKeyPressed(bl, false);
                if (key == GLFW.GLFW_KEY_F3) {
                    if (this.switchF3State) {
                        this.switchF3State = false;
                    } else {
                        this.client.options.debugEnabled = !this.client.options.debugEnabled;
                        this.client.options.debugProfilerEnabled = this.client.options.debugEnabled && Screen.hasShiftDown();
                        this.client.options.debugTpsEnabled = this.client.options.debugEnabled && Screen.hasAltDown();
                    }
                }
            } else {
                if (key == GLFW.GLFW_KEY_F4 && this.client.gameRenderer != null) {
                    this.client.gameRenderer.toggleShadersEnabled();
                }
                bl2 = false;
                if (this.client.currentScreen == null) {
                    if (key == GLFW.GLFW_KEY_ESCAPE) {
                        boolean bl3 = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_F3);
                        this.client.openPauseMenu(bl3);
                    }
                    bl2 = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_F3) && this.processF3(key);
                    this.switchF3State |= bl2;
                    if (key == GLFW.GLFW_KEY_F1) {
                        boolean bl4 = this.client.options.hudHidden = !this.client.options.hudHidden;
                    }
                }
                if (bl2) {
                    KeyBinding.setKeyPressed(bl, false);
                } else {
                    KeyBinding.setKeyPressed(bl, true);
                    KeyBinding.onKeyPressed(bl);
                }
                if (this.client.options.debugProfilerEnabled && key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) {
                    this.client.handleProfilerKeyPress(key - GLFW.GLFW_KEY_0);
                }
            }
        }
    }

    private void onChar(long window, int codePoint, int modifiers) {
        if (window != this.client.getWindow().getHandle()) {
            return;
        }
        Screen element = this.client.currentScreen;
        if (element == null || this.client.getOverlay() != null) {
            return;
        }
        if (Character.charCount(codePoint) == 1) {
            Screen.wrapScreenError(() -> element.charTyped((char)codePoint, modifiers), "charTyped event handler", element.getClass().getCanonicalName());
        } else {
            for (char c : Character.toChars(codePoint)) {
                Screen.wrapScreenError(() -> element.charTyped(c, modifiers), "charTyped event handler", element.getClass().getCanonicalName());
            }
        }
    }

    public void setRepeatEvents(boolean repeatEvents) {
        this.repeatEvents = repeatEvents;
    }

    public void setup(long window2) {
        InputUtil.setKeyboardCallbacks(window2, (window, key, scancode, action, modifiers) -> this.client.execute(() -> this.onKey(window, key, scancode, action, modifiers)), (window, codePoint, modifiers) -> this.client.execute(() -> this.onChar(window, codePoint, modifiers)));
    }

    public String getClipboard() {
        return this.clipboard.getClipboard(this.client.getWindow().getHandle(), (error, description) -> {
            if (error != 65545) {
                this.client.getWindow().logGlError(error, description);
            }
        });
    }

    public void setClipboard(String clipboard) {
        if (!clipboard.isEmpty()) {
            this.clipboard.setClipboard(this.client.getWindow().getHandle(), clipboard);
        }
    }

    public void pollDebugCrash() {
        if (this.debugCrashStartTime > 0L) {
            long l = Util.getMeasuringTimeMs();
            long m = 10000L - (l - this.debugCrashStartTime);
            long n = l - this.debugCrashLastLogTime;
            if (m < 0L) {
                if (Screen.hasControlDown()) {
                    GlfwUtil.makeJvmCrash();
                }
                throw new CrashException(new CrashReport("Manually triggered debug crash", new Throwable()));
            }
            if (n >= 1000L) {
                if (this.debugCrashElapsedTime == 0L) {
                    this.debugLog("debug.crash.message", new Object[0]);
                } else {
                    this.debugError("debug.crash.warning", MathHelper.ceil((float)m / 1000.0f));
                }
                this.debugCrashLastLogTime = l;
                ++this.debugCrashElapsedTime;
            }
        }
    }
}
