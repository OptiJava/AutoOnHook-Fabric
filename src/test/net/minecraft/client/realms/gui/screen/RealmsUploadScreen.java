/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.realms.gui.screen;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPOutputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.FileUpload;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.SizeUnit;
import net.minecraft.client.realms.UploadStatus;
import net.minecraft.client.realms.dto.UploadInfo;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.exception.RetryCallException;
import net.minecraft.client.realms.gui.screen.RealmsResetWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.realms.util.UploadTokenCache;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Environment(value=EnvType.CLIENT)
public class RealmsUploadScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ReentrantLock UPLOAD_LOCK = new ReentrantLock();
    private static final String[] DOTS = new String[]{"", ".", ". .", ". . ."};
    private static final Text VERIFYING_TEXT = new TranslatableText("mco.upload.verifying");
    private final RealmsResetWorldScreen parent;
    private final LevelSummary selectedLevel;
    private final long worldId;
    private final int slotId;
    private final UploadStatus uploadStatus;
    private final RateLimiter narrationRateLimiter;
    private volatile Text[] statusTexts;
    private volatile Text status = new TranslatableText("mco.upload.preparing");
    private volatile String progress;
    private volatile boolean cancelled;
    private volatile boolean uploadFinished;
    private volatile boolean showDots = true;
    private volatile boolean uploadStarted;
    private ButtonWidget backButton;
    private ButtonWidget cancelButton;
    private int animTick;
    private Long previousWrittenBytes;
    private Long previousTimeSnapshot;
    private long bytesPerSecond;
    private final Runnable onBack;

    public RealmsUploadScreen(long worldId, int slotId, RealmsResetWorldScreen parent, LevelSummary selectedLevel, Runnable onBack) {
        super(NarratorManager.EMPTY);
        this.worldId = worldId;
        this.slotId = slotId;
        this.parent = parent;
        this.selectedLevel = selectedLevel;
        this.uploadStatus = new UploadStatus();
        this.narrationRateLimiter = RateLimiter.create(0.1f);
        this.onBack = onBack;
    }

    @Override
    public void init() {
        this.client.keyboard.setRepeatEvents(true);
        this.backButton = this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height - 42, 200, 20, ScreenTexts.BACK, button -> this.onBack()));
        this.backButton.visible = false;
        this.cancelButton = this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height - 42, 200, 20, ScreenTexts.CANCEL, button -> this.onCancel()));
        if (!this.uploadStarted) {
            if (this.parent.slot == -1) {
                this.upload();
            } else {
                this.parent.switchSlot(() -> {
                    if (!this.uploadStarted) {
                        this.uploadStarted = true;
                        this.client.setScreen(this);
                        this.upload();
                    }
                });
            }
        }
    }

    @Override
    public void removed() {
        this.client.keyboard.setRepeatEvents(false);
    }

    private void onBack() {
        this.onBack.run();
    }

    private void onCancel() {
        this.cancelled = true;
        this.client.setScreen(this.parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (this.showDots) {
                this.onCancel();
            } else {
                this.onBack();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        if (!this.uploadFinished && this.uploadStatus.bytesWritten != 0L && this.uploadStatus.bytesWritten == this.uploadStatus.totalBytes) {
            this.status = VERIFYING_TEXT;
            this.cancelButton.active = false;
        }
        RealmsUploadScreen.drawCenteredText(matrices, this.textRenderer, this.status, this.width / 2, 50, 0xFFFFFF);
        if (this.showDots) {
            this.drawDots(matrices);
        }
        if (this.uploadStatus.bytesWritten != 0L && !this.cancelled) {
            this.drawProgressBar(matrices);
            this.drawUploadSpeed(matrices);
        }
        if (this.statusTexts != null) {
            for (int i = 0; i < this.statusTexts.length; ++i) {
                RealmsUploadScreen.drawCenteredText(matrices, this.textRenderer, this.statusTexts[i], this.width / 2, 110 + 12 * i, 0xFF0000);
            }
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    private void drawDots(MatrixStack matrices) {
        int i = this.textRenderer.getWidth(this.status);
        this.textRenderer.draw(matrices, DOTS[this.animTick / 10 % DOTS.length], (float)(this.width / 2 + i / 2 + 5), 50.0f, 0xFFFFFF);
    }

    private void drawProgressBar(MatrixStack matrices) {
        double d = Math.min((double)this.uploadStatus.bytesWritten / (double)this.uploadStatus.totalBytes, 1.0);
        this.progress = String.format(Locale.ROOT, "%.1f", d * 100.0);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableTexture();
        double e = this.width / 2 - 100;
        double f = 0.5;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(e - 0.5, 95.5, 0.0).color(217, 210, 210, 255).next();
        bufferBuilder.vertex(e + 200.0 * d + 0.5, 95.5, 0.0).color(217, 210, 210, 255).next();
        bufferBuilder.vertex(e + 200.0 * d + 0.5, 79.5, 0.0).color(217, 210, 210, 255).next();
        bufferBuilder.vertex(e - 0.5, 79.5, 0.0).color(217, 210, 210, 255).next();
        bufferBuilder.vertex(e, 95.0, 0.0).color(128, 128, 128, 255).next();
        bufferBuilder.vertex(e + 200.0 * d, 95.0, 0.0).color(128, 128, 128, 255).next();
        bufferBuilder.vertex(e + 200.0 * d, 80.0, 0.0).color(128, 128, 128, 255).next();
        bufferBuilder.vertex(e, 80.0, 0.0).color(128, 128, 128, 255).next();
        tessellator.draw();
        RenderSystem.enableTexture();
        RealmsUploadScreen.drawCenteredText(matrices, this.textRenderer, this.progress + " %", this.width / 2, 84, 0xFFFFFF);
    }

    private void drawUploadSpeed(MatrixStack matrices) {
        if (this.animTick % 20 == 0) {
            if (this.previousWrittenBytes != null) {
                long l = Util.getMeasuringTimeMs() - this.previousTimeSnapshot;
                if (l == 0L) {
                    l = 1L;
                }
                this.bytesPerSecond = 1000L * (this.uploadStatus.bytesWritten - this.previousWrittenBytes) / l;
                this.drawUploadSpeed0(matrices, this.bytesPerSecond);
            }
            this.previousWrittenBytes = this.uploadStatus.bytesWritten;
            this.previousTimeSnapshot = Util.getMeasuringTimeMs();
        } else {
            this.drawUploadSpeed0(matrices, this.bytesPerSecond);
        }
    }

    private void drawUploadSpeed0(MatrixStack matrices, long bytesPerSecond) {
        if (bytesPerSecond > 0L) {
            int i = this.textRenderer.getWidth(this.progress);
            String string = "(" + SizeUnit.getUserFriendlyString(bytesPerSecond) + "/s)";
            this.textRenderer.draw(matrices, string, (float)(this.width / 2 + i / 2 + 15), 84.0f, 0xFFFFFF);
        }
    }

    @Override
    public void tick() {
        super.tick();
        ++this.animTick;
        if (this.status != null && this.narrationRateLimiter.tryAcquire(1)) {
            Text text = this.getNarration();
            NarratorManager.INSTANCE.narrate(text);
        }
    }

    private Text getNarration() {
        ArrayList<Text> list = Lists.newArrayList();
        list.add(this.status);
        if (this.progress != null) {
            list.add(new LiteralText(this.progress + "%"));
        }
        if (this.statusTexts != null) {
            list.addAll(Arrays.asList(this.statusTexts));
        }
        return ScreenTexts.joinLines(list);
    }

    private void upload() {
        this.uploadStarted = true;
        new Thread(() -> {
            File file = null;
            RealmsClient realmsClient = RealmsClient.createRealmsClient();
            long l = this.worldId;
            try {
                if (!UPLOAD_LOCK.tryLock(1L, TimeUnit.SECONDS)) {
                    this.status = new TranslatableText("mco.upload.close.failure");
                    return;
                }
                UploadInfo uploadInfo = null;
                for (int i = 0; i < 20; ++i) {
                    block35: {
                        if (!this.cancelled) break block35;
                        this.uploadCancelled();
                        return;
                    }
                    try {
                        uploadInfo = realmsClient.upload(l, UploadTokenCache.get(l));
                        if (uploadInfo == null) continue;
                        break;
                    }
                    catch (RetryCallException retryCallException) {
                        Thread.sleep(retryCallException.delaySeconds * 1000);
                    }
                }
                if (uploadInfo == null) {
                    this.status = new TranslatableText("mco.upload.close.failure");
                    return;
                }
                UploadTokenCache.put(l, uploadInfo.getToken());
                if (!uploadInfo.isWorldClosed()) {
                    this.status = new TranslatableText("mco.upload.close.failure");
                    return;
                }
                if (this.cancelled) {
                    this.uploadCancelled();
                    return;
                }
                File i = new File(this.client.runDirectory.getAbsolutePath(), "saves");
                file = this.tarGzipArchive(new File(i, this.selectedLevel.getName()));
                if (this.cancelled) {
                    this.uploadCancelled();
                    return;
                }
                if (!this.verify(file)) {
                    long retryCallException = file.length();
                    SizeUnit sizeUnit = SizeUnit.getLargestUnit(retryCallException);
                    SizeUnit sizeUnit2 = SizeUnit.getLargestUnit(0x140000000L);
                    if (SizeUnit.humanReadableSize(retryCallException, sizeUnit).equals(SizeUnit.humanReadableSize(0x140000000L, sizeUnit2)) && sizeUnit != SizeUnit.B) {
                        SizeUnit sizeUnit3 = SizeUnit.values()[sizeUnit.ordinal() - 1];
                        this.setStatusTexts(new TranslatableText("mco.upload.size.failure.line1", this.selectedLevel.getDisplayName()), new TranslatableText("mco.upload.size.failure.line2", SizeUnit.humanReadableSize(retryCallException, sizeUnit3), SizeUnit.humanReadableSize(0x140000000L, sizeUnit3)));
                        return;
                    }
                    this.setStatusTexts(new TranslatableText("mco.upload.size.failure.line1", this.selectedLevel.getDisplayName()), new TranslatableText("mco.upload.size.failure.line2", SizeUnit.humanReadableSize(retryCallException, sizeUnit), SizeUnit.humanReadableSize(0x140000000L, sizeUnit2)));
                    return;
                }
                this.status = new TranslatableText("mco.upload.uploading", this.selectedLevel.getDisplayName());
                FileUpload retryCallException = new FileUpload(file, this.worldId, this.slotId, uploadInfo, this.client.getSession(), SharedConstants.getGameVersion().getName(), this.uploadStatus);
                retryCallException.upload(result -> {
                    if (result.statusCode >= 200 && result.statusCode < 300) {
                        this.uploadFinished = true;
                        this.status = new TranslatableText("mco.upload.done");
                        this.backButton.setMessage(ScreenTexts.DONE);
                        UploadTokenCache.invalidate(l);
                    } else if (result.statusCode == 400 && result.errorMessage != null) {
                        this.setStatusTexts(new TranslatableText("mco.upload.failed", result.errorMessage));
                    } else {
                        this.setStatusTexts(new TranslatableText("mco.upload.failed", result.statusCode));
                    }
                });
                while (!retryCallException.isFinished()) {
                    if (this.cancelled) {
                        retryCallException.cancel();
                        this.uploadCancelled();
                        return;
                    }
                    try {
                        Thread.sleep(500L);
                    }
                    catch (InterruptedException interruptedException) {
                        LOGGER.error("Failed to check Realms file upload status");
                    }
                }
            }
            catch (IOException uploadInfo) {
                this.setStatusTexts(new TranslatableText("mco.upload.failed", uploadInfo.getMessage()));
            }
            catch (RealmsServiceException uploadInfo) {
                this.setStatusTexts(new TranslatableText("mco.upload.failed", uploadInfo.toString()));
            }
            catch (InterruptedException uploadInfo) {
                LOGGER.error("Could not acquire upload lock");
            }
            finally {
                this.uploadFinished = true;
                if (!UPLOAD_LOCK.isHeldByCurrentThread()) {
                    return;
                }
                UPLOAD_LOCK.unlock();
                this.showDots = false;
                this.backButton.visible = true;
                this.cancelButton.visible = false;
                if (file != null) {
                    LOGGER.debug("Deleting file {}", (Object)file.getAbsolutePath());
                    file.delete();
                }
            }
        }).start();
    }

    private void setStatusTexts(Text ... statusTexts) {
        this.statusTexts = statusTexts;
    }

    private void uploadCancelled() {
        this.status = new TranslatableText("mco.upload.cancelled");
        LOGGER.debug("Upload was cancelled");
    }

    private boolean verify(File archive) {
        return archive.length() < 0x140000000L;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private File tarGzipArchive(File pathToDirectoryFile) throws IOException {
        try (TarArchiveOutputStream tarArchiveOutputStream = null;){
            File file = File.createTempFile("realms-upload-file", ".tar.gz");
            tarArchiveOutputStream = new TarArchiveOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
            tarArchiveOutputStream.setLongFileMode(3);
            this.addFileToTarGz(tarArchiveOutputStream, pathToDirectoryFile.getAbsolutePath(), "world", true);
            tarArchiveOutputStream.finish();
            File file2 = file;
            return file2;
        }
    }

    private void addFileToTarGz(TarArchiveOutputStream tOut, String path, String base, boolean root) throws IOException {
        if (this.cancelled) {
            return;
        }
        File file = new File(path);
        String string = root ? base : base + file.getName();
        TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(file, string);
        tOut.putArchiveEntry(tarArchiveEntry);
        if (file.isFile()) {
            IOUtils.copy(new FileInputStream(file), tOut);
            tOut.closeArchiveEntry();
        } else {
            tOut.closeArchiveEntry();
            File[] files = file.listFiles();
            if (files != null) {
                for (File file2 : files) {
                    this.addFileToTarGz(tOut, file2.getAbsolutePath(), string + "/", false);
                }
            }
        }
    }
}

