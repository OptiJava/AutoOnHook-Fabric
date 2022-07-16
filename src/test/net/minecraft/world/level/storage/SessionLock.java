/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.level.storage;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;

public class SessionLock
implements AutoCloseable {
    public static final String SESSION_LOCK = "session.lock";
    private final FileChannel channel;
    private final FileLock lock;
    private static final ByteBuffer SNOWMAN;

    public static SessionLock create(Path path) throws IOException {
        Path path2 = path.resolve(SESSION_LOCK);
        if (!Files.isDirectory(path, new LinkOption[0])) {
            Files.createDirectories(path, new FileAttribute[0]);
        }
        FileChannel fileChannel = FileChannel.open(path2, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        try {
            fileChannel.write(SNOWMAN.duplicate());
            fileChannel.force(true);
            FileLock fileLock = fileChannel.tryLock();
            if (fileLock == null) {
                throw AlreadyLockedException.create(path2);
            }
            return new SessionLock(fileChannel, fileLock);
        }
        catch (IOException fileLock) {
            try {
                fileChannel.close();
            }
            catch (IOException iOException) {
                fileLock.addSuppressed(iOException);
            }
            throw fileLock;
        }
    }

    private SessionLock(FileChannel channel, FileLock lock) {
        this.channel = channel;
        this.lock = lock;
    }

    @Override
    public void close() throws IOException {
        try {
            if (this.lock.isValid()) {
                this.lock.release();
            }
        }
        finally {
            if (this.channel.isOpen()) {
                this.channel.close();
            }
        }
    }

    public boolean isValid() {
        return this.lock.isValid();
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static boolean isLocked(Path path) throws IOException {
        Path path2 = path.resolve(SESSION_LOCK);
        try (FileChannel fileChannel = FileChannel.open(path2, StandardOpenOption.WRITE);){
            boolean bl;
            block15: {
                FileLock fileLock = fileChannel.tryLock();
                try {
                    boolean bl2 = bl = fileLock == null;
                    if (fileLock == null) break block15;
                }
                catch (Throwable throwable) {
                    if (fileLock != null) {
                        try {
                            fileLock.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                fileLock.close();
            }
            return bl;
        }
        catch (AccessDeniedException fileChannel2) {
            return true;
        }
        catch (NoSuchFileException fileChannel3) {
            return false;
        }
    }

    static {
        byte[] bs = "\u2603".getBytes(Charsets.UTF_8);
        SNOWMAN = ByteBuffer.allocateDirect(bs.length);
        SNOWMAN.put(bs);
        SNOWMAN.flip();
    }

    public static class AlreadyLockedException
    extends IOException {
        private AlreadyLockedException(Path path, String message) {
            super(path.toAbsolutePath() + ": " + message);
        }

        public static AlreadyLockedException create(Path path) {
            return new AlreadyLockedException(path, "already locked (possibly by other Minecraft instance?)");
        }
    }
}

