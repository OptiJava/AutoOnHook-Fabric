/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui.screen.world;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.resource.FileResourcePackProvider;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.dynamic.RegistryReadingOps;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.gen.GeneratorOptions;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

@Environment(value=EnvType.CLIENT)
public class MoreOptionsDialog
implements Drawable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Text CUSTOM_TEXT = new TranslatableText("generator.custom");
    private static final Text AMPLIFIED_INFO_TEXT = new TranslatableText("generator.amplified.info");
    private static final Text MAP_FEATURES_INFO_TEXT = new TranslatableText("selectWorld.mapFeatures.info");
    private static final Text SELECT_SETTINGS_FILE_TEXT = new TranslatableText("selectWorld.import_worldgen_settings.select_file");
    private MultilineText generatorInfoText = MultilineText.EMPTY;
    private TextRenderer textRenderer;
    private int parentWidth;
    private TextFieldWidget seedTextField;
    private CyclingButtonWidget<Boolean> mapFeaturesButton;
    private CyclingButtonWidget<Boolean> bonusItemsButton;
    private CyclingButtonWidget<GeneratorType> mapTypeButton;
    private ButtonWidget field_28001;
    private ButtonWidget customizeTypeButton;
    private ButtonWidget importSettingsButton;
    private DynamicRegistryManager.Impl registryManager;
    private GeneratorOptions generatorOptions;
    private Optional<GeneratorType> generatorType;
    private OptionalLong seed;

    public MoreOptionsDialog(DynamicRegistryManager.Impl registryManager, GeneratorOptions generatorOptions, Optional<GeneratorType> generatorType, OptionalLong seed) {
        this.registryManager = registryManager;
        this.generatorOptions = generatorOptions;
        this.generatorType = generatorType;
        this.seed = seed;
    }

    public void init(CreateWorldScreen parent, MinecraftClient client, TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
        this.parentWidth = parent.width;
        this.seedTextField = new TextFieldWidget(this.textRenderer, this.parentWidth / 2 - 100, 60, 200, 20, new TranslatableText("selectWorld.enterSeed"));
        this.seedTextField.setText(MoreOptionsDialog.seedToString(this.seed));
        this.seedTextField.setChangedListener(seedText -> {
            this.seed = this.getSeed();
        });
        parent.addSelectableChild(this.seedTextField);
        int i = this.parentWidth / 2 - 155;
        int j = this.parentWidth / 2 + 5;
        this.mapFeaturesButton = parent.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.generatorOptions.shouldGenerateStructures()).narration(button -> ScreenTexts.joinSentences(button.getGenericNarrationMessage(), new TranslatableText("selectWorld.mapFeatures.info"))).build(i, 100, 150, 20, new TranslatableText("selectWorld.mapFeatures"), (button, generateStructures) -> {
            this.generatorOptions = this.generatorOptions.toggleGenerateStructures();
        }));
        this.mapFeaturesButton.visible = false;
        this.mapTypeButton = parent.addDrawableChild(CyclingButtonWidget.builder(GeneratorType::getTranslationKey).values(GeneratorType.VALUES.stream().filter(GeneratorType::isNotDebug).collect(Collectors.toList()), GeneratorType.VALUES).narration(button -> {
            if (button.getValue() == GeneratorType.AMPLIFIED) {
                return ScreenTexts.joinSentences(button.getGenericNarrationMessage(), AMPLIFIED_INFO_TEXT);
            }
            return button.getGenericNarrationMessage();
        }).build(j, 100, 150, 20, new TranslatableText("selectWorld.mapType"), (button, generatorType) -> {
            this.generatorType = Optional.of(generatorType);
            this.generatorOptions = generatorType.createDefaultOptions(this.registryManager, this.generatorOptions.getSeed(), this.generatorOptions.shouldGenerateStructures(), this.generatorOptions.hasBonusChest());
            parent.setMoreOptionsOpen();
        }));
        this.generatorType.ifPresent(this.mapTypeButton::setValue);
        this.mapTypeButton.visible = false;
        this.field_28001 = parent.addDrawableChild(new ButtonWidget(j, 100, 150, 20, ScreenTexts.composeGenericOptionText(new TranslatableText("selectWorld.mapType"), CUSTOM_TEXT), button -> {}));
        this.field_28001.active = false;
        this.field_28001.visible = false;
        this.customizeTypeButton = parent.addDrawableChild(new ButtonWidget(j, 120, 150, 20, new TranslatableText("selectWorld.customizeType"), button -> {
            GeneratorType.ScreenProvider screenProvider = GeneratorType.SCREEN_PROVIDERS.get(this.generatorType);
            if (screenProvider != null) {
                client.setScreen(screenProvider.createEditScreen(parent, this.generatorOptions));
            }
        }));
        this.customizeTypeButton.visible = false;
        this.bonusItemsButton = parent.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.generatorOptions.hasBonusChest() && !parent.hardcore).build(i, 151, 150, 20, new TranslatableText("selectWorld.bonusItems"), (button, bonusChest) -> {
            this.generatorOptions = this.generatorOptions.toggleBonusChest();
        }));
        this.bonusItemsButton.visible = false;
        this.importSettingsButton = parent.addDrawableChild(new ButtonWidget(i, 185, 150, 20, new TranslatableText("selectWorld.import_worldgen_settings"), button -> {
            DataResult<Object> text2;
            Object jsonElement;
            Object bufferedReader;
            ServerResourceManager serverResourceManager;
            Object completableFuture;
            String string = TinyFileDialogs.tinyfd_openFileDialog(SELECT_SETTINGS_FILE_TEXT.getString(), null, null, null, false);
            if (string == null) {
                return;
            }
            DynamicRegistryManager.Impl impl = DynamicRegistryManager.create();
            ResourcePackManager resourcePackManager = new ResourcePackManager(ResourceType.SERVER_DATA, new VanillaDataPackProvider(), new FileResourcePackProvider(parent.getDataPackTempDir().toFile(), ResourcePackSource.PACK_SOURCE_WORLD));
            try {
                MinecraftServer.loadDataPacks(resourcePackManager, createWorldScreen.dataPackSettings, false);
                completableFuture = ServerResourceManager.reload(resourcePackManager.createResourcePacks(), impl, CommandManager.RegistrationEnvironment.INTEGRATED, 2, Util.getMainWorkerExecutor(), client);
                client.runTasks(() -> completableFuture.isDone());
                serverResourceManager = ((CompletableFuture)completableFuture).get();
            }
            catch (InterruptedException | ExecutionException completableFuture2) {
                LOGGER.error("Error loading data packs when importing world settings", (Throwable)completableFuture2);
                TranslatableText text = new TranslatableText("selectWorld.import_worldgen_settings.failure");
                LiteralText text22 = new LiteralText(completableFuture2.getMessage());
                client.getToastManager().add(SystemToast.create(client, SystemToast.Type.WORLD_GEN_SETTINGS_TRANSFER, text, text22));
                resourcePackManager.close();
                return;
            }
            completableFuture = RegistryOps.ofLoaded(JsonOps.INSTANCE, serverResourceManager.getResourceManager(), (DynamicRegistryManager)impl);
            JsonParser text = new JsonParser();
            try {
                bufferedReader = Files.newBufferedReader(Paths.get(string, new String[0]));
                try {
                    jsonElement = text.parse((Reader)bufferedReader);
                    text2 = GeneratorOptions.CODEC.parse(completableFuture, jsonElement);
                }
                finally {
                    if (bufferedReader != null) {
                        ((BufferedReader)bufferedReader).close();
                    }
                }
            }
            catch (JsonIOException | JsonSyntaxException | IOException bufferedReader2) {
                text2 = DataResult.error("Failed to parse file: " + bufferedReader2.getMessage());
            }
            if (text2.error().isPresent()) {
                bufferedReader = new TranslatableText("selectWorld.import_worldgen_settings.failure");
                jsonElement = text2.error().get().message();
                LOGGER.error("Error parsing world settings: {}", jsonElement);
                LiteralText text3 = new LiteralText((String)jsonElement);
                client.getToastManager().add(SystemToast.create(client, SystemToast.Type.WORLD_GEN_SETTINGS_TRANSFER, (Text)bufferedReader, text3));
            }
            serverResourceManager.close();
            bufferedReader = text2.lifecycle();
            text2.resultOrPartial(LOGGER::error).ifPresent(arg_0 -> this.method_29070(client, parent, impl, (Lifecycle)bufferedReader, arg_0));
        }));
        this.importSettingsButton.visible = false;
        this.generatorInfoText = MultilineText.create(textRenderer, (StringVisitable)AMPLIFIED_INFO_TEXT, this.mapTypeButton.getWidth());
    }

    private void importOptions(DynamicRegistryManager.Impl registryManager, GeneratorOptions generatorOptions) {
        this.registryManager = registryManager;
        this.generatorOptions = generatorOptions;
        this.generatorType = GeneratorType.fromGeneratorOptions(generatorOptions);
        this.setMapTypeButtonVisible(true);
        this.seed = OptionalLong.of(generatorOptions.getSeed());
        this.seedTextField.setText(MoreOptionsDialog.seedToString(this.seed));
    }

    public void tickSeedTextField() {
        this.seedTextField.tick();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.mapFeaturesButton.visible) {
            this.textRenderer.drawWithShadow(matrices, MAP_FEATURES_INFO_TEXT, (float)(this.parentWidth / 2 - 150), 122.0f, -6250336);
        }
        this.seedTextField.render(matrices, mouseX, mouseY, delta);
        if (this.generatorType.equals(Optional.of(GeneratorType.AMPLIFIED))) {
            this.generatorInfoText.drawWithShadow(matrices, this.mapTypeButton.x + 2, this.mapTypeButton.y + 22, this.textRenderer.fontHeight, 0xA0A0A0);
        }
    }

    protected void setGeneratorOptions(GeneratorOptions generatorOptions) {
        this.generatorOptions = generatorOptions;
    }

    private static String seedToString(OptionalLong seed) {
        if (seed.isPresent()) {
            return Long.toString(seed.getAsLong());
        }
        return "";
    }

    private static OptionalLong tryParseLong(String string) {
        try {
            return OptionalLong.of(Long.parseLong(string));
        }
        catch (NumberFormatException numberFormatException) {
            return OptionalLong.empty();
        }
    }

    public GeneratorOptions getGeneratorOptions(boolean hardcore) {
        OptionalLong optionalLong = this.getSeed();
        return this.generatorOptions.withHardcore(hardcore, optionalLong);
    }

    private OptionalLong getSeed() {
        OptionalLong optionalLong2;
        String string = this.seedTextField.getText();
        OptionalLong optionalLong = StringUtils.isEmpty(string) ? OptionalLong.empty() : ((optionalLong2 = MoreOptionsDialog.tryParseLong(string)).isPresent() && optionalLong2.getAsLong() != 0L ? optionalLong2 : OptionalLong.of(string.hashCode()));
        return optionalLong;
    }

    public boolean isDebugWorld() {
        return this.generatorOptions.isDebugWorld();
    }

    public void setVisible(boolean visible) {
        this.setMapTypeButtonVisible(visible);
        if (this.generatorOptions.isDebugWorld()) {
            this.mapFeaturesButton.visible = false;
            this.bonusItemsButton.visible = false;
            this.customizeTypeButton.visible = false;
            this.importSettingsButton.visible = false;
        } else {
            this.mapFeaturesButton.visible = visible;
            this.bonusItemsButton.visible = visible;
            this.customizeTypeButton.visible = visible && GeneratorType.SCREEN_PROVIDERS.containsKey(this.generatorType);
            this.importSettingsButton.visible = visible;
        }
        this.seedTextField.setVisible(visible);
    }

    private void setMapTypeButtonVisible(boolean visible) {
        if (this.generatorType.isPresent()) {
            this.mapTypeButton.visible = visible;
            this.field_28001.visible = false;
        } else {
            this.mapTypeButton.visible = false;
            this.field_28001.visible = visible;
        }
    }

    public DynamicRegistryManager.Impl getRegistryManager() {
        return this.registryManager;
    }

    void loadDatapacks(ServerResourceManager serverResourceManager) {
        DynamicRegistryManager.Impl impl = DynamicRegistryManager.create();
        RegistryReadingOps<JsonElement> registryReadingOps = RegistryReadingOps.of(JsonOps.INSTANCE, this.registryManager);
        RegistryOps<JsonElement> registryOps = RegistryOps.ofLoaded(JsonOps.INSTANCE, serverResourceManager.getResourceManager(), (DynamicRegistryManager)impl);
        DataResult dataResult = GeneratorOptions.CODEC.encodeStart(registryReadingOps, this.generatorOptions).flatMap(jsonElement -> GeneratorOptions.CODEC.parse(registryOps, jsonElement));
        dataResult.resultOrPartial(Util.addPrefix("Error parsing worldgen settings after loading data packs: ", LOGGER::error)).ifPresent(generatorOptions -> {
            this.generatorOptions = generatorOptions;
            this.registryManager = impl;
        });
    }

    public void disableBonusItems() {
        this.bonusItemsButton.active = false;
        this.bonusItemsButton.setValue(false);
    }

    public void enableBonusItems() {
        this.bonusItemsButton.active = true;
        this.bonusItemsButton.setValue(this.generatorOptions.hasBonusChest());
    }

    private /* synthetic */ void method_29070(MinecraftClient minecraftClient, CreateWorldScreen createWorldScreen, DynamicRegistryManager.Impl impl, Lifecycle lifecycle, GeneratorOptions generatorOptions) {
        BooleanConsumer booleanConsumer = confirmed -> {
            minecraftClient.setScreen(createWorldScreen);
            if (confirmed) {
                this.importOptions(impl, generatorOptions);
            }
        };
        if (lifecycle == Lifecycle.stable()) {
            this.importOptions(impl, generatorOptions);
        } else if (lifecycle == Lifecycle.experimental()) {
            minecraftClient.setScreen(new ConfirmScreen(booleanConsumer, new TranslatableText("selectWorld.import_worldgen_settings.experimental.title"), new TranslatableText("selectWorld.import_worldgen_settings.experimental.question")));
        } else {
            minecraftClient.setScreen(new ConfirmScreen(booleanConsumer, new TranslatableText("selectWorld.import_worldgen_settings.deprecated.title"), new TranslatableText("selectWorld.import_worldgen_settings.deprecated.question")));
        }
    }
}

