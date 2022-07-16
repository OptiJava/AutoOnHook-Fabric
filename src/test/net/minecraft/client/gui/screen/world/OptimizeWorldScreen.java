/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui.screen.world;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.updater.WorldUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class OptimizeWorldScreen
extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Object2IntMap<RegistryKey<World>> DIMENSION_COLORS = Util.make(new Object2IntOpenCustomHashMap(Util.identityHashStrategy()), colors -> {
        colors.put(World.OVERWORLD, -13408734);
        colors.put(World.NETHER, -10075085);
        colors.put(World.END, -8943531);
        colors.defaultReturnValue(-2236963);
    });
    private final BooleanConsumer callback;
    private final WorldUpdater updater;

    @Nullable
    public static OptimizeWorldScreen create(MinecraftClient client, BooleanConsumer callback, DataFixer dataFixer, LevelStorage.Session storageSession, boolean eraseCache) {
        DynamicRegistryManager.Impl impl = DynamicRegistryManager.create();
        MinecraftClient.IntegratedResourceManager integratedResourceManager = client.createIntegratedResourceManager(impl, MinecraftClient::loadDataPackSettings, MinecraftClient::createSaveProperties, false, storageSession);
        try {
            SaveProperties saveProperties = integratedResourceManager.getSaveProperties();
            storageSession.backupLevelDataFile(impl, saveProperties);
            ImmutableSet<RegistryKey<World>> immutableSet = saveProperties.getGeneratorOptions().getWorlds();
            OptimizeWorldScreen optimizeWorldScreen = new OptimizeWorldScreen(callback, dataFixer, storageSession, saveProperties.getLevelInfo(), eraseCache, immutableSet);
            if (integratedResourceManager != null) {
                integratedResourceManager.close();
            }
            return optimizeWorldScreen;
        }
        catch (Throwable throwable) {
            try {
                if (integratedResourceManager != null) {
                    try {
                        integratedResourceManager.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
            catch (Exception integratedResourceManager2) {
                LOGGER.warn("Failed to load datapacks, can't optimize world", (Throwable)integratedResourceManager2);
                return null;
            }
        }
    }

    private OptimizeWorldScreen(BooleanConsumer callback, DataFixer dataFixer, LevelStorage.Session storageSession, LevelInfo levelInfo, boolean eraseCache, ImmutableSet<RegistryKey<World>> worlds) {
        super(new TranslatableText("optimizeWorld.title", levelInfo.getLevelName()));
        this.callback = callback;
        this.updater = new WorldUpdater(storageSession, dataFixer, worlds, eraseCache);
    }

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 150, 200, 20, ScreenTexts.CANCEL, button -> {
            this.updater.cancel();
            this.callback.accept(false);
        }));
    }

    @Override
    public void tick() {
        if (this.updater.isDone()) {
            this.callback.accept(true);
        }
    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }

    @Override
    public void removed() {
        this.updater.cancel();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        OptimizeWorldScreen.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        int i = this.width / 2 - 150;
        int j = this.width / 2 + 150;
        int k = this.height / 4 + 100;
        int l = k + 10;
        OptimizeWorldScreen.drawCenteredText(matrices, this.textRenderer, this.updater.getStatus(), this.width / 2, k - this.textRenderer.fontHeight - 2, 0xA0A0A0);
        if (this.updater.getTotalChunkCount() > 0) {
            OptimizeWorldScreen.fill(matrices, i - 1, k - 1, j + 1, l + 1, -16777216);
            OptimizeWorldScreen.drawTextWithShadow(matrices, this.textRenderer, new TranslatableText("optimizeWorld.info.converted", this.updater.getUpgradedChunkCount()), i, 40, 0xA0A0A0);
            OptimizeWorldScreen.drawTextWithShadow(matrices, this.textRenderer, new TranslatableText("optimizeWorld.info.skipped", this.updater.getSkippedChunkCount()), i, 40 + this.textRenderer.fontHeight + 3, 0xA0A0A0);
            OptimizeWorldScreen.drawTextWithShadow(matrices, this.textRenderer, new TranslatableText("optimizeWorld.info.total", this.updater.getTotalChunkCount()), i, 40 + (this.textRenderer.fontHeight + 3) * 2, 0xA0A0A0);
            int m = 0;
            for (RegistryKey registryKey : this.updater.getWorlds()) {
                int n = MathHelper.floor(this.updater.getProgress(registryKey) * (float)(j - i));
                OptimizeWorldScreen.fill(matrices, i + m, k, i + m + n, l, DIMENSION_COLORS.getInt(registryKey));
                m += n;
            }
            int o = this.updater.getUpgradedChunkCount() + this.updater.getSkippedChunkCount();
            OptimizeWorldScreen.drawCenteredText(matrices, this.textRenderer, o + " / " + this.updater.getTotalChunkCount(), this.width / 2, k + 2 * this.textRenderer.fontHeight + 2, 0xA0A0A0);
            OptimizeWorldScreen.drawCenteredText(matrices, this.textRenderer, MathHelper.floor(this.updater.getProgress() * 100.0f) + "%", this.width / 2, k + (l - k) / 2 - this.textRenderer.fontHeight / 2, 0xA0A0A0);
        }
        super.render(matrices, mouseX, mouseY, delta);
    }
}

