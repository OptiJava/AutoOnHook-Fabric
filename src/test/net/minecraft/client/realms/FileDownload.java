/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.realms;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.realms.dto.WorldDownload;
import net.minecraft.client.realms.exception.RealmsDefaultUncaughtExceptionHandler;
import net.minecraft.client.realms.gui.screen.RealmsDownloadLatestWorldScreen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class FileDownload {
    static final Logger LOGGER = LogManager.getLogger();
    volatile boolean cancelled;
    volatile boolean finished;
    volatile boolean error;
    volatile boolean extracting;
    private volatile File backupFile;
    volatile File resourcePackPath;
    private volatile HttpGet httpRequest;
    private Thread currentThread;
    private final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(120000).setConnectTimeout(120000).build();
    private static final String[] INVALID_FILE_NAMES = new String[]{"CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long contentLength(String downloadLink) {
        Closeable closeableHttpClient = null;
        HttpGet httpGet = null;
        try {
            httpGet = new HttpGet(downloadLink);
            closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
            CloseableHttpResponse closeableHttpResponse = ((CloseableHttpClient)closeableHttpClient).execute(httpGet);
            long l = Long.parseLong(closeableHttpResponse.getFirstHeader("Content-Length").getValue());
            return l;
        }
        catch (Throwable closeableHttpResponse) {
            LOGGER.error("Unable to get content length for download");
            long l = 0L;
            return l;
        }
        finally {
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
            if (closeableHttpClient != null) {
                try {
                    closeableHttpClient.close();
                }
                catch (IOException iOException) {
                    LOGGER.error("Could not close http client", (Throwable)iOException);
                }
            }
        }
    }

    public void downloadWorld(WorldDownload download, String message, RealmsDownloadLatestWorldScreen.DownloadStatus status, LevelStorage storage) {
        if (this.currentThread != null) {
            return;
        }
        this.currentThread = new Thread(() -> {
            Closeable closeableHttpClient = null;
            try {
                this.backupFile = File.createTempFile("backup", ".tar.gz");
                this.httpRequest = new HttpGet(worldDownload.downloadLink);
                closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
                CloseableHttpResponse httpResponse = ((CloseableHttpClient)closeableHttpClient).execute(this.httpRequest);
                downloadStatus.totalBytes = Long.parseLong(httpResponse.getFirstHeader("Content-Length").getValue());
                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    this.error = true;
                    this.httpRequest.abort();
                    return;
                }
                FileOutputStream httpResponse2 = new FileOutputStream(this.backupFile);
                ActionListener outputStream = new ProgressListener(message.trim(), this.backupFile, storage, status);
                DownloadCountingOutputStream resourcePackProgressListener = new DownloadCountingOutputStream(httpResponse2);
                resourcePackProgressListener.setListener(outputStream);
                IOUtils.copy(httpResponse.getEntity().getContent(), (OutputStream)resourcePackProgressListener);
                return;
            }
            catch (Exception httpResponse) {
                LOGGER.error("Caught exception while downloading: {}", (Object)httpResponse.getMessage());
                this.error = true;
                return;
            }
            finally {
                block40: {
                    block41: {
                        this.httpRequest.releaseConnection();
                        if (this.backupFile != null) {
                            this.backupFile.delete();
                        }
                        if (this.error) break block40;
                        if (worldDownload.resourcePackUrl.isEmpty() || worldDownload.resourcePackHash.isEmpty()) break block41;
                        try {
                            this.backupFile = File.createTempFile("resources", ".tar.gz");
                            this.httpRequest = new HttpGet(worldDownload.resourcePackUrl);
                            CloseableHttpResponse httpResponse = ((CloseableHttpClient)closeableHttpClient).execute(this.httpRequest);
                            downloadStatus.totalBytes = Long.parseLong(httpResponse.getFirstHeader("Content-Length").getValue());
                            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                                this.error = true;
                                this.httpRequest.abort();
                                return;
                            }
                        }
                        catch (Exception httpResponse) {
                            LOGGER.error("Caught exception while downloading: {}", (Object)httpResponse.getMessage());
                            this.error = true;
                        }
                        FileOutputStream httpResponse2 = new FileOutputStream(this.backupFile);
                        ResourcePackProgressListener outputStream = new ResourcePackProgressListener(this.backupFile, status, download);
                        DownloadCountingOutputStream resourcePackProgressListener = new DownloadCountingOutputStream(httpResponse2);
                        resourcePackProgressListener.setListener(outputStream);
                        IOUtils.copy(httpResponse.getEntity().getContent(), (OutputStream)resourcePackProgressListener);
                        break block40;
                        finally {
                            this.httpRequest.releaseConnection();
                            if (this.backupFile != null) {
                                this.backupFile.delete();
                            }
                        }
                    }
                    this.finished = true;
                }
                if (closeableHttpClient != null) {
                    try {
                        closeableHttpClient.close();
                    }
                    catch (IOException httpResponse) {
                        LOGGER.error("Failed to close Realms download client");
                    }
                }
            }
        });
        this.currentThread.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
        this.currentThread.start();
    }

    public void cancel() {
        if (this.httpRequest != null) {
            this.httpRequest.abort();
        }
        if (this.backupFile != null) {
            this.backupFile.delete();
        }
        this.cancelled = true;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public boolean isError() {
        return this.error;
    }

    public boolean isExtracting() {
        return this.extracting;
    }

    public static String findAvailableFolderName(String folder) {
        folder = ((String)folder).replaceAll("[\\./\"]", "_");
        for (String string : INVALID_FILE_NAMES) {
            if (!((String)folder).equalsIgnoreCase(string)) continue;
            folder = "_" + (String)folder + "_";
        }
        return folder;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void untarGzipArchive(String name, File archive, LevelStorage storage) throws IOException {
        Object string;
        Pattern pattern = Pattern.compile(".*-([0-9]+)$");
        int i = 1;
        for (char c : SharedConstants.INVALID_CHARS_LEVEL_NAME) {
            name = name.replace(c, '_');
        }
        if (StringUtils.isEmpty(name)) {
            name = "Realm";
        }
        name = FileDownload.findAvailableFolderName(name);
        try {
            Object object = storage.getLevelList().iterator();
            while (object.hasNext()) {
                LevelSummary levelSummary = (LevelSummary)object.next();
                if (!levelSummary.getName().toLowerCase(Locale.ROOT).startsWith(name.toLowerCase(Locale.ROOT))) continue;
                Matcher matcher = pattern.matcher(levelSummary.getName());
                if (matcher.matches()) {
                    if (Integer.valueOf(matcher.group(1)) <= i) continue;
                    i = Integer.valueOf(matcher.group(1));
                    continue;
                }
                ++i;
            }
        }
        catch (Exception exception) {
            LOGGER.error("Error getting level list", (Throwable)exception);
            this.error = true;
            return;
        }
        if (!storage.isLevelNameValid(name) || i > 1) {
            string = name + (String)(i == 1 ? "" : "-" + i);
            if (!storage.isLevelNameValid((String)string)) {
                boolean exception = false;
                while (!exception) {
                    if (!storage.isLevelNameValid((String)(string = name + (String)(++i == 1 ? "" : "-" + i)))) continue;
                    exception = true;
                }
            }
        } else {
            string = name;
        }
        TarArchiveInputStream exception = null;
        File levelSummary = new File(MinecraftClient.getInstance().runDirectory.getAbsolutePath(), "saves");
        try {
            levelSummary.mkdir();
            exception = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(archive))));
            Object matcher = exception.getNextTarEntry();
            while (matcher != null) {
                File c = new File(levelSummary, ((TarArchiveEntry)matcher).getName().replace("world", (CharSequence)string));
                if (((TarArchiveEntry)matcher).isDirectory()) {
                    c.mkdirs();
                } else {
                    c.createNewFile();
                    try (FileOutputStream fileOutputStream = new FileOutputStream(c);){
                        IOUtils.copy((InputStream)exception, (OutputStream)fileOutputStream);
                    }
                }
                matcher = exception.getNextTarEntry();
            }
        }
        catch (Exception matcher) {
            LOGGER.error("Error extracting world", (Throwable)matcher);
            this.error = true;
        }
        finally {
            if (exception != null) {
                exception.close();
            }
            if (archive != null) {
                archive.delete();
            }
            try (LevelStorage.Session matcher = storage.createSession((String)string);){
                matcher.save(((String)string).trim());
                Path c = matcher.getDirectory(WorldSavePath.LEVEL_DAT);
                FileDownload.readNbtFile(c.toFile());
            }
            catch (IOException matcher2) {
                LOGGER.error("Failed to rename unpacked realms level {}", string, (Object)matcher2);
            }
            this.resourcePackPath = new File(levelSummary, (String)string + File.separator + "resources.zip");
        }
    }

    private static void readNbtFile(File file) {
        if (file.exists()) {
            try {
                NbtCompound nbtCompound = NbtIo.readCompressed(file);
                NbtCompound nbtCompound2 = nbtCompound.getCompound("Data");
                nbtCompound2.remove("Player");
                NbtIo.writeCompressed(nbtCompound, file);
            }
            catch (Exception nbtCompound) {
                nbtCompound.printStackTrace();
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class ResourcePackProgressListener
    implements ActionListener {
        private final File tempFile;
        private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;
        private final WorldDownload worldDownload;

        ResourcePackProgressListener(File tempFile, RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus, WorldDownload worldDownload) {
            this.tempFile = tempFile;
            this.downloadStatus = downloadStatus;
            this.worldDownload = worldDownload;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.downloadStatus.bytesWritten = ((DownloadCountingOutputStream)e.getSource()).getByteCount();
            if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled) {
                try {
                    String string = Hashing.sha1().hashBytes(Files.toByteArray(this.tempFile)).toString();
                    if (string.equals(this.worldDownload.resourcePackHash)) {
                        FileUtils.copyFile(this.tempFile, FileDownload.this.resourcePackPath);
                        FileDownload.this.finished = true;
                    } else {
                        LOGGER.error("Resourcepack had wrong hash (expected {}, found {}). Deleting it.", (Object)this.worldDownload.resourcePackHash, (Object)string);
                        FileUtils.deleteQuietly(this.tempFile);
                        FileDownload.this.error = true;
                    }
                }
                catch (IOException string) {
                    LOGGER.error("Error copying resourcepack file: {}", (Object)string.getMessage());
                    FileDownload.this.error = true;
                }
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class DownloadCountingOutputStream
    extends CountingOutputStream {
        private ActionListener listener;

        public DownloadCountingOutputStream(OutputStream out) {
            super(out);
        }

        public void setListener(ActionListener listener) {
            this.listener = listener;
        }

        @Override
        protected void afterWrite(int n) throws IOException {
            super.afterWrite(n);
            if (this.listener != null) {
                this.listener.actionPerformed(new ActionEvent(this, 0, null));
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class ProgressListener
    implements ActionListener {
        private final String worldName;
        private final File tempFile;
        private final LevelStorage levelStorageSource;
        private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;

        ProgressListener(String worldName, File tempFile, LevelStorage levelStorageSource, RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus) {
            this.worldName = worldName;
            this.tempFile = tempFile;
            this.levelStorageSource = levelStorageSource;
            this.downloadStatus = downloadStatus;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.downloadStatus.bytesWritten = ((DownloadCountingOutputStream)e.getSource()).getByteCount();
            if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled && !FileDownload.this.error) {
                try {
                    FileDownload.this.extracting = true;
                    FileDownload.this.untarGzipArchive(this.worldName, this.tempFile, this.levelStorageSource);
                }
                catch (IOException iOException) {
                    LOGGER.error("Error extracting archive", (Throwable)iOException);
                    FileDownload.this.error = true;
                }
            }
        }
    }
}

