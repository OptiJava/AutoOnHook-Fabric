/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui.screen.world;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.BackupPromptScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.world.OptimizeWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.dynamic.RegistryReadingOps;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class EditWorldScreen
extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final Text ENTER_NAME_TEXT = new TranslatableText("selectWorld.enterName");
    private ButtonWidget saveButton;
    private final BooleanConsumer callback;
    private TextFieldWidget levelNameTextField;
    private final LevelStorage.Session storageSession;

    public EditWorldScreen(BooleanConsumer callback, LevelStorage.Session storageSession) {
        super(new TranslatableText("selectWorld.edit.title"));
        this.callback = callback;
        this.storageSession = storageSession;
    }

    @Override
    public void tick() {
        this.levelNameTextField.tick();
    }

    @Override
    protected void init() {
        this.client.keyboard.setRepeatEvents(true);
        ButtonWidget buttonWidget = this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 0 + 5, 200, 20, new TranslatableText("selectWorld.edit.resetIcon"), button -> {
            this.storageSession.getIconFile().ifPresent(path -> FileUtils.deleteQuietly(path.toFile()));
            button.active = false;
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 24 + 5, 200, 20, new TranslatableText("selectWorld.edit.openFolder"), button -> Util.getOperatingSystem().open(this.storageSession.getDirectory(WorldSavePath.ROOT).toFile())));
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 48 + 5, 200, 20, new TranslatableText("selectWorld.edit.backup"), button -> {
            boolean bl = EditWorldScreen.backupLevel(this.storageSession);
            this.callback.accept(!bl);
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 72 + 5, 200, 20, new TranslatableText("selectWorld.edit.backupFolder"), button -> {
            LevelStorage levelStorage = this.client.getLevelStorage();
            Path path = levelStorage.getBackupsDirectory();
            try {
                Files.createDirectories(Files.exists(path, new LinkOption[0]) ? path.toRealPath(new LinkOption[0]) : path, new FileAttribute[0]);
            }
            catch (IOException iOException) {
                throw new RuntimeException(iOException);
            }
            Util.getOperatingSystem().open(path.toFile());
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 96 + 5, 200, 20, new TranslatableText("selectWorld.edit.optimize"), button -> this.client.setScreen(new BackupPromptScreen(this, (backup, eraseCache) -> {
            if (backup) {
                EditWorldScreen.backupLevel(this.storageSession);
            }
            this.client.setScreen(OptimizeWorldScreen.create(this.client, this.callback, this.client.getDataFixer(), this.storageSession, eraseCache));
        }, new TranslatableText("optimizeWorld.confirm.title"), new TranslatableText("optimizeWorld.confirm.description"), true))));
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 120 + 5, 200, 20, new TranslatableText("selectWorld.edit.export_worldgen_settings"), button -> {
            DataResult<Object> dataResult2;
            Object dynamicOps;
            Object integratedResourceManager;
            DynamicRegistryManager.Impl impl = DynamicRegistryManager.create();
            try {
                integratedResourceManager = this.client.createIntegratedResourceManager(impl, MinecraftClient::loadDataPackSettings, MinecraftClient::createSaveProperties, false, this.storageSession);
                try {
                    dynamicOps = RegistryReadingOps.of(JsonOps.INSTANCE, impl);
                    DataResult<JsonElement> dataResult = GeneratorOptions.CODEC.encodeStart(dynamicOps, ((MinecraftClient.IntegratedResourceManager)integratedResourceManager).getSaveProperties().getGeneratorOptions());
                    dataResult2 = dataResult.flatMap(json -> {
                        Path path = this.storageSession.getDirectory(WorldSavePath.ROOT).resolve("worldgen_settings_export.json");
                        try (JsonWriter jsonWriter = GSON.newJsonWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8, new OpenOption[0]));){
                            GSON.toJson((JsonElement)json, jsonWriter);
                        }
                        catch (JsonIOException | IOException jsonWriter2) {
                            return DataResult.error("Error writing file: " + jsonWriter2.getMessage());
                        }
                        return DataResult.success(path.toString());
                    });
                }
                finally {
                    if (integratedResourceManager != null) {
                        ((MinecraftClient.IntegratedResourceManager)integratedResourceManager).close();
                    }
                }
            }
            catch (Exception integratedResourceManager2) {
                LOGGER.warn("Could not parse level data", (Throwable)integratedResourceManager2);
                dataResult2 = DataResult.error("Could not parse level data: " + integratedResourceManager2.getMessage());
            }
            integratedResourceManager = new LiteralText(dataResult2.get().map(Function.identity(), DataResult.PartialResult::message));
            dynamicOps = new TranslatableText(dataResult2.result().isPresent() ? "selectWorld.edit.export_worldgen_settings.success" : "selectWorld.edit.export_worldgen_settings.failure");
            dataResult2.error().ifPresent(result -> LOGGER.error("Error exporting world settings: {}", result));
            this.client.getToastManager().add(SystemToast.create(this.client, SystemToast.Type.WORLD_GEN_SETTINGS_TRANSFER, (Text)dynamicOps, (Text)integratedResourceManager));
        }));
        this.saveButton = this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 144 + 5, 98, 20, new TranslatableText("selectWorld.edit.save"), button -> this.commit()));
        this.addDrawableChild(new ButtonWidget(this.width / 2 + 2, this.height / 4 + 144 + 5, 98, 20, ScreenTexts.CANCEL, button -> this.callback.accept(false)));
        buttonWidget.active = this.storageSession.getIconFile().filter(path -> Files.isRegularFile(path, new LinkOption[0])).isPresent();
        LevelSummary levelSummary = this.storageSession.getLevelSummary();
        String string = levelSummary == null ? "" : levelSummary.getDisplayName();
        this.levelNameTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 38, 200, 20, new TranslatableText("selectWorld.enterName"));
        this.levelNameTextField.setText(string);
        this.levelNameTextField.setChangedListener(levelName -> {
            this.saveButton.active = !levelName.trim().isEmpty();
        });
        this.addSelectableChild(this.levelNameTextField);
        this.setInitialFocus(this.levelNameTextField);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String string = this.levelNameTextField.getText();
        this.init(client, width, height);
        this.levelNameTextField.setText(string);
    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }

    @Override
    public void removed() {
        this.client.keyboard.setRepeatEvents(false);
    }

    private void commit() {
        try {
            this.storageSession.save(this.levelNameTextField.getText().trim());
            this.callback.accept(true);
        }
        catch (IOException iOException) {
            LOGGER.error("Failed to access world '{}'", (Object)this.storageSession.getDirectoryName(), (Object)iOException);
            SystemToast.addWorldAccessFailureToast(this.client, this.storageSession.getDirectoryName());
            this.callback.accept(true);
        }
    }

    public static void onBackupConfirm(LevelStorage storage, String levelName) {
        boolean bl = false;
        try (LevelStorage.Session session = storage.createSession(levelName);){
            bl = true;
            EditWorldScreen.backupLevel(session);
        }
        catch (IOException session2) {
            if (!bl) {
                SystemToast.addWorldAccessFailureToast(MinecraftClient.getInstance(), levelName);
            }
            LOGGER.warn("Failed to create backup of level {}", (Object)levelName, (Object)session2);
        }
    }

    public static boolean backupLevel(LevelStorage.Session storageSession) {
        long l = 0L;
        IOException iOException = null;
        try {
            l = storageSession.createBackup();
        }
        catch (IOException iOException2) {
            iOException = iOException2;
        }
        if (iOException != null) {
            TranslatableText iOException2 = new TranslatableText("selectWorld.edit.backupFailed");
            LiteralText text = new LiteralText(iOException.getMessage());
            MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.WORLD_BACKUP, iOException2, text));
            return false;
        }
        TranslatableText iOException2 = new TranslatableText("selectWorld.edit.backupCreated", storageSession.getDirectoryName());
        TranslatableText text = new TranslatableText("selectWorld.edit.backupSize", MathHelper.ceil((double)l / 1048576.0));
        MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.WORLD_BACKUP, iOException2, text));
        return true;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        EditWorldScreen.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        EditWorldScreen.drawTextWithShadow(matrices, this.textRenderer, ENTER_NAME_TEXT, this.width / 2 - 100, 24, 0xA0A0A0);
        this.levelNameTextField.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }
}

