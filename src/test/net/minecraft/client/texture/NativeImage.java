/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.texture;

import com.google.common.base.Charsets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.Untracker;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@Environment(value=EnvType.CLIENT)
public final class NativeImage
implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * The bit offset of the alpha data in the {@linkplain Format#RGBA RGBA} format.
     * Is {@value}. Notice the alpha data in {@linkplain Format#LUMINANCE_ALPHA
     * luminance-alpha} format has a different offset of {@code 8}.
     */
    private static final int ALPHA_OFFSET = 24;
    /**
     * The bit offset of the blue data in the {@linkplain Format#RGBA RGBA} or the
     * {@linkplain Format#RGB RGB} formats. Is {@value}.
     */
    private static final int BLUE_OFFSET = 16;
    /**
     * The bit offset of the green data in the {@linkplain Format#RGBA RGBA} or the
     * {@linkplain Format#RGB RGB} formats. Is {@value}.
     */
    private static final int GREEN_OFFSET = 8;
    /**
     * The bit offset of the red data in the {@linkplain Format#RGBA RGBA} or the
     * {@linkplain Format#RGB RGB} formats. Is {@value}.
     */
    private static final int RED_OFFSET = 0;
    private static final Set<StandardOpenOption> WRITE_TO_FILE_OPEN_OPTIONS = EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    private final Format format;
    private final int width;
    private final int height;
    private final boolean isStbImage;
    private long pointer;
    private final long sizeBytes;

    public NativeImage(int width, int height, boolean useStb) {
        this(Format.RGBA, width, height, useStb);
    }

    public NativeImage(Format format, int width, int height, boolean useStb) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid texture size: " + width + "x" + height);
        }
        this.format = format;
        this.width = width;
        this.height = height;
        this.sizeBytes = (long)width * (long)height * (long)format.getChannelCount();
        this.isStbImage = false;
        this.pointer = useStb ? MemoryUtil.nmemCalloc(1L, this.sizeBytes) : MemoryUtil.nmemAlloc(this.sizeBytes);
    }

    private NativeImage(Format format, int width, int height, boolean useStb, long pointer) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid texture size: " + width + "x" + height);
        }
        this.format = format;
        this.width = width;
        this.height = height;
        this.isStbImage = useStb;
        this.pointer = pointer;
        this.sizeBytes = (long)width * (long)height * (long)format.getChannelCount();
    }

    public String toString() {
        return "NativeImage[" + this.format + " " + this.width + "x" + this.height + "@" + this.pointer + (this.isStbImage ? "S" : "N") + "]";
    }

    private boolean isOutOfBounds(int x, int y) {
        return x < 0 || x >= this.width || y < 0 || y >= this.height;
    }

    public static NativeImage read(InputStream stream) throws IOException {
        return NativeImage.read(Format.RGBA, stream);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static NativeImage read(@Nullable Format format, InputStream stream) throws IOException {
        ByteBuffer byteBuffer = null;
        try {
            byteBuffer = TextureUtil.readResource(stream);
            byteBuffer.rewind();
            NativeImage nativeImage = NativeImage.read(format, byteBuffer);
            return nativeImage;
        }
        finally {
            MemoryUtil.memFree(byteBuffer);
            IOUtils.closeQuietly(stream);
        }
    }

    public static NativeImage read(ByteBuffer buffer) throws IOException {
        return NativeImage.read(Format.RGBA, buffer);
    }

    public static NativeImage read(@Nullable Format format, ByteBuffer buffer) throws IOException {
        if (format != null && !format.isWriteable()) {
            throw new UnsupportedOperationException("Don't know how to read format " + format);
        }
        if (MemoryUtil.memAddress(buffer) == 0L) {
            throw new IllegalArgumentException("Invalid buffer");
        }
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            IntBuffer intBuffer = memoryStack.mallocInt(1);
            IntBuffer intBuffer2 = memoryStack.mallocInt(1);
            IntBuffer intBuffer3 = memoryStack.mallocInt(1);
            ByteBuffer byteBuffer = STBImage.stbi_load_from_memory(buffer, intBuffer, intBuffer2, intBuffer3, format == null ? 0 : format.channelCount);
            if (byteBuffer == null) {
                throw new IOException("Could not load image: " + STBImage.stbi_failure_reason());
            }
            NativeImage nativeImage = new NativeImage(format == null ? Format.fromGl(intBuffer3.get(0)) : format, intBuffer.get(0), intBuffer2.get(0), true, MemoryUtil.memAddress(byteBuffer));
            return nativeImage;
        }
    }

    private static void setTextureFilter(boolean blur, boolean mipmap) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (blur) {
            GlStateManager._texParameter(3553, 10241, mipmap ? 9987 : 9729);
            GlStateManager._texParameter(3553, 10240, 9729);
        } else {
            GlStateManager._texParameter(3553, 10241, mipmap ? 9986 : 9728);
            GlStateManager._texParameter(3553, 10240, 9728);
        }
    }

    private void checkAllocated() {
        if (this.pointer == 0L) {
            throw new IllegalStateException("Image is not allocated.");
        }
    }

    @Override
    public void close() {
        if (this.pointer != 0L) {
            if (this.isStbImage) {
                STBImage.nstbi_image_free(this.pointer);
            } else {
                MemoryUtil.nmemFree(this.pointer);
            }
        }
        this.pointer = 0L;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Format getFormat() {
        return this.format;
    }

    /**
     * Gets the color of a pixel on this native image.
     * The color returned by this method will be in a big-endian (from biggest
     * to smallest bits) ABGR format, or little-endian RGBA.
     * 
     * @throws IllegalArgumentException when this native image's format is not
     * {@linkplain Format#RGBA little-endian RGBA}, or the coordinate is out-of-bounds
     * @return the color, with red at smallest and alpha at biggest bits
     */
    public int getColor(int x, int y) {
        if (this.format != Format.RGBA) {
            throw new IllegalArgumentException(String.format("getPixelRGBA only works on RGBA images; have %s", new Object[]{this.format}));
        }
        if (this.isOutOfBounds(x, y)) {
            throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
        }
        this.checkAllocated();
        long l = ((long)x + (long)y * (long)this.width) * 4L;
        return MemoryUtil.memGetInt(this.pointer + l);
    }

    /**
     * Sets the color of a pixel on this native image.
     * The color to be set by this method will be in a big-endian (from biggest
     * to smallest bits) ABGR format, or little-endian RGBA.
     * 
     * @throws IllegalArgumentException when this native image's format is not
     * {@linkplain Format#RGBA little-endian RGBA}, or the coordinate is out-of-bounds
     * 
     * @param color the color, with red at smallest and alpha at biggest bits
     */
    public void setColor(int x, int y, int color) {
        if (this.format != Format.RGBA) {
            throw new IllegalArgumentException(String.format("getPixelRGBA only works on RGBA images; have %s", new Object[]{this.format}));
        }
        if (this.isOutOfBounds(x, y)) {
            throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
        }
        this.checkAllocated();
        long l = ((long)x + (long)y * (long)this.width) * 4L;
        MemoryUtil.memPutInt(this.pointer + l, color);
    }

    public void setLuminance(int x, int y, byte luminance) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (!this.format.hasLuminance()) {
            throw new IllegalArgumentException(String.format("setPixelLuminance only works on image with luminance; have %s", new Object[]{this.format}));
        }
        if (this.isOutOfBounds(x, y)) {
            throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
        }
        this.checkAllocated();
        long l = ((long)x + (long)y * (long)this.width) * (long)this.format.getChannelCount() + (long)(this.format.getLuminanceOffset() / 8);
        MemoryUtil.memPutByte(this.pointer + l, luminance);
    }

    public byte getRed(int x, int y) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (!this.format.hasRedChannel()) {
            throw new IllegalArgumentException(String.format("no red or luminance in %s", new Object[]{this.format}));
        }
        if (this.isOutOfBounds(x, y)) {
            throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
        }
        int i = (x + y * this.width) * this.format.getChannelCount() + this.format.getRedChannelOffset() / 8;
        return MemoryUtil.memGetByte(this.pointer + (long)i);
    }

    public byte getGreen(int x, int y) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (!this.format.hasGreenChannel()) {
            throw new IllegalArgumentException(String.format("no green or luminance in %s", new Object[]{this.format}));
        }
        if (this.isOutOfBounds(x, y)) {
            throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
        }
        int i = (x + y * this.width) * this.format.getChannelCount() + this.format.getGreenChannelOffset() / 8;
        return MemoryUtil.memGetByte(this.pointer + (long)i);
    }

    public byte getBlue(int x, int y) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (!this.format.hasBlueChannel()) {
            throw new IllegalArgumentException(String.format("no blue or luminance in %s", new Object[]{this.format}));
        }
        if (this.isOutOfBounds(x, y)) {
            throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
        }
        int i = (x + y * this.width) * this.format.getChannelCount() + this.format.getBlueChannelOffset() / 8;
        return MemoryUtil.memGetByte(this.pointer + (long)i);
    }

    public byte getOpacity(int x, int y) {
        if (!this.format.hasOpacityChannel()) {
            throw new IllegalArgumentException(String.format("no luminance or alpha in %s", new Object[]{this.format}));
        }
        if (this.isOutOfBounds(x, y)) {
            throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
        }
        int i = (x + y * this.width) * this.format.getChannelCount() + this.format.getOpacityChannelOffset() / 8;
        return MemoryUtil.memGetByte(this.pointer + (long)i);
    }

    public void blend(int x, int y, int color) {
        if (this.format != Format.RGBA) {
            throw new UnsupportedOperationException("Can only call blendPixel with RGBA format");
        }
        int i = this.getColor(x, y);
        float f = (float)NativeImage.getAlpha(color) / 255.0f;
        float g = (float)NativeImage.getBlue(color) / 255.0f;
        float h = (float)NativeImage.getGreen(color) / 255.0f;
        float j = (float)NativeImage.getRed(color) / 255.0f;
        float k = (float)NativeImage.getAlpha(i) / 255.0f;
        float l = (float)NativeImage.getBlue(i) / 255.0f;
        float m = (float)NativeImage.getGreen(i) / 255.0f;
        float n = (float)NativeImage.getRed(i) / 255.0f;
        float o = f;
        float p = 1.0f - f;
        float q = f * o + k * p;
        float r = g * o + l * p;
        float s = h * o + m * p;
        float t = j * o + n * p;
        if (q > 1.0f) {
            q = 1.0f;
        }
        if (r > 1.0f) {
            r = 1.0f;
        }
        if (s > 1.0f) {
            s = 1.0f;
        }
        if (t > 1.0f) {
            t = 1.0f;
        }
        int u = (int)(q * 255.0f);
        int v = (int)(r * 255.0f);
        int w = (int)(s * 255.0f);
        int z = (int)(t * 255.0f);
        this.setColor(x, y, NativeImage.packColor(u, v, w, z));
    }

    @Deprecated
    public int[] makePixelArray() {
        if (this.format != Format.RGBA) {
            throw new UnsupportedOperationException("can only call makePixelArray for RGBA images.");
        }
        this.checkAllocated();
        int[] is = new int[this.getWidth() * this.getHeight()];
        for (int i = 0; i < this.getHeight(); ++i) {
            for (int j = 0; j < this.getWidth(); ++j) {
                int p;
                int k = this.getColor(j, i);
                int l = NativeImage.getAlpha(k);
                int m = NativeImage.getBlue(k);
                int n = NativeImage.getGreen(k);
                int o = NativeImage.getRed(k);
                is[j + i * this.getWidth()] = p = l << 24 | o << 16 | n << 8 | m;
            }
        }
        return is;
    }

    public void upload(int level, int offsetX, int offsetY, boolean close) {
        this.upload(level, offsetX, offsetY, 0, 0, this.width, this.height, false, close);
    }

    public void upload(int level, int offsetX, int offsetY, int unpackSkipPixels, int unpackSkipRows, int width, int height, boolean mipmap, boolean close) {
        this.upload(level, offsetX, offsetY, unpackSkipPixels, unpackSkipRows, width, height, false, false, mipmap, close);
    }

    public void upload(int level, int offsetX, int offsetY, int unpackSkipPixels, int unpackSkipRows, int width, int height, boolean blur, boolean clamp, boolean mipmap, boolean close) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> this.uploadInternal(level, offsetX, offsetY, unpackSkipPixels, unpackSkipRows, width, height, blur, clamp, mipmap, close));
        } else {
            this.uploadInternal(level, offsetX, offsetY, unpackSkipPixels, unpackSkipRows, width, height, blur, clamp, mipmap, close);
        }
    }

    private void uploadInternal(int level, int offsetX, int offsetY, int unpackSkipPixels, int unpackSkipRows, int width, int height, boolean blur, boolean clamp, boolean mipmap, boolean close) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        this.checkAllocated();
        NativeImage.setTextureFilter(blur, mipmap);
        if (width == this.getWidth()) {
            GlStateManager._pixelStore(3314, 0);
        } else {
            GlStateManager._pixelStore(3314, this.getWidth());
        }
        GlStateManager._pixelStore(3316, unpackSkipPixels);
        GlStateManager._pixelStore(3315, unpackSkipRows);
        this.format.setUnpackAlignment();
        GlStateManager._texSubImage2D(3553, level, offsetX, offsetY, width, height, this.format.toGl(), 5121, this.pointer);
        if (clamp) {
            GlStateManager._texParameter(3553, 10242, 33071);
            GlStateManager._texParameter(3553, 10243, 33071);
        }
        if (close) {
            this.close();
        }
    }

    public void loadFromTextureImage(int level, boolean removeAlpha) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        this.checkAllocated();
        this.format.setPackAlignment();
        GlStateManager._getTexImage(3553, level, this.format.toGl(), 5121, this.pointer);
        if (removeAlpha && this.format.hasAlpha()) {
            for (int i = 0; i < this.getHeight(); ++i) {
                for (int j = 0; j < this.getWidth(); ++j) {
                    this.setColor(j, i, this.getColor(j, i) | 255 << this.format.getAlphaOffset());
                }
            }
        }
    }

    public void readDepthComponent(float unused) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (this.format.getChannelCount() != 1) {
            throw new IllegalStateException("Depth buffer must be stored in NativeImage with 1 component.");
        }
        this.checkAllocated();
        this.format.setPackAlignment();
        GlStateManager._readPixels(0, 0, this.width, this.height, 6402, 5121, this.pointer);
    }

    /**
     * Use {@code upload} to upload this image to GL so it can be used later. This
     * method is not used in vanilla, and its side effects are not yet known.
     */
    public void drawPixels() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        this.format.setUnpackAlignment();
        GlStateManager._glDrawPixels(this.width, this.height, this.format.toGl(), 5121, this.pointer);
    }

    public void writeTo(String path) throws IOException {
        this.writeTo(FileSystems.getDefault().getPath(path, new String[0]));
    }

    public void writeTo(File path) throws IOException {
        this.writeTo(path.toPath());
    }

    public void makeGlyphBitmapSubpixel(STBTTFontinfo fontInfo, int glyphIndex, int width, int height, float scaleX, float scaleY, float shiftX, float shiftY, int startX, int startY) {
        if (startX < 0 || startX + width > this.getWidth() || startY < 0 || startY + height > this.getHeight()) {
            throw new IllegalArgumentException(String.format("Out of bounds: start: (%s, %s) (size: %sx%s); size: %sx%s", startX, startY, width, height, this.getWidth(), this.getHeight()));
        }
        if (this.format.getChannelCount() != 1) {
            throw new IllegalArgumentException("Can only write fonts into 1-component images.");
        }
        STBTruetype.nstbtt_MakeGlyphBitmapSubpixel(fontInfo.address(), this.pointer + (long)startX + (long)(startY * this.getWidth()), width, height, this.getWidth(), scaleX, scaleY, shiftX, shiftY, glyphIndex);
    }

    public void writeTo(Path path) throws IOException {
        if (!this.format.isWriteable()) {
            throw new UnsupportedOperationException("Don't know how to write format " + this.format);
        }
        this.checkAllocated();
        try (SeekableByteChannel writableByteChannel = Files.newByteChannel(path, WRITE_TO_FILE_OPEN_OPTIONS, new FileAttribute[0]);){
            if (!this.write(writableByteChannel)) {
                throw new IOException("Could not write image to the PNG file \"" + path.toAbsolutePath() + "\": " + STBImage.stbi_failure_reason());
            }
        }
    }

    public byte[] getBytes() throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();){
            byte[] byArray;
            block12: {
                WritableByteChannel writableByteChannel = Channels.newChannel(byteArrayOutputStream);
                try {
                    if (!this.write(writableByteChannel)) {
                        throw new IOException("Could not write image to byte array: " + STBImage.stbi_failure_reason());
                    }
                    byArray = byteArrayOutputStream.toByteArray();
                    if (writableByteChannel == null) break block12;
                }
                catch (Throwable throwable) {
                    if (writableByteChannel != null) {
                        try {
                            writableByteChannel.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                writableByteChannel.close();
            }
            return byArray;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean write(WritableByteChannel channel) throws IOException {
        WriteCallback writeCallback = new WriteCallback(channel);
        try {
            int i = Math.min(this.getHeight(), Integer.MAX_VALUE / this.getWidth() / this.format.getChannelCount());
            if (i < this.getHeight()) {
                LOGGER.warn("Dropping image height from {} to {} to fit the size into 32-bit signed int", (Object)this.getHeight(), (Object)i);
            }
            if (STBImageWrite.nstbi_write_png_to_func(writeCallback.address(), 0L, this.getWidth(), i, this.format.getChannelCount(), this.pointer, 0) == 0) {
                boolean bl = false;
                return bl;
            }
            writeCallback.throwStoredException();
            boolean bl = true;
            return bl;
        }
        finally {
            writeCallback.free();
        }
    }

    public void copyFrom(NativeImage image) {
        if (image.getFormat() != this.format) {
            throw new UnsupportedOperationException("Image formats don't match.");
        }
        int i = this.format.getChannelCount();
        this.checkAllocated();
        image.checkAllocated();
        if (this.width == image.width) {
            MemoryUtil.memCopy(image.pointer, this.pointer, Math.min(this.sizeBytes, image.sizeBytes));
        } else {
            int j = Math.min(this.getWidth(), image.getWidth());
            int k = Math.min(this.getHeight(), image.getHeight());
            for (int l = 0; l < k; ++l) {
                int m = l * image.getWidth() * i;
                int n = l * this.getWidth() * i;
                MemoryUtil.memCopy(image.pointer + (long)m, this.pointer + (long)n, j);
            }
        }
    }

    public void fillRect(int x, int y, int width, int height, int color) {
        for (int i = y; i < y + height; ++i) {
            for (int j = x; j < x + width; ++j) {
                this.setColor(j, i, color);
            }
        }
    }

    public void copyRect(int x, int y, int translateX, int translateY, int width, int height, boolean flipX, boolean flipY) {
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                int k = flipX ? width - 1 - j : j;
                int l = flipY ? height - 1 - i : i;
                int m = this.getColor(x + j, y + i);
                this.setColor(x + translateX + k, y + translateY + l, m);
            }
        }
    }

    public void mirrorVertically() {
        this.checkAllocated();
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            int i = this.format.getChannelCount();
            int j = this.getWidth() * i;
            long l = memoryStack.nmalloc(j);
            for (int k = 0; k < this.getHeight() / 2; ++k) {
                int m = k * this.getWidth() * i;
                int n = (this.getHeight() - 1 - k) * this.getWidth() * i;
                MemoryUtil.memCopy(this.pointer + (long)m, l, j);
                MemoryUtil.memCopy(this.pointer + (long)n, this.pointer + (long)m, j);
                MemoryUtil.memCopy(l, this.pointer + (long)n, j);
            }
        }
    }

    public void resizeSubRectTo(int x, int y, int width, int height, NativeImage targetImage) {
        this.checkAllocated();
        if (targetImage.getFormat() != this.format) {
            throw new UnsupportedOperationException("resizeSubRectTo only works for images of the same format.");
        }
        int i = this.format.getChannelCount();
        STBImageResize.nstbir_resize_uint8(this.pointer + (long)((x + y * this.getWidth()) * i), width, height, this.getWidth() * i, targetImage.pointer, targetImage.getWidth(), targetImage.getHeight(), 0, i);
    }

    public void untrack() {
        Untracker.untrack(this.pointer);
    }

    public static NativeImage read(String dataUri) throws IOException {
        byte[] bs = Base64.getDecoder().decode(dataUri.replaceAll("\n", "").getBytes(Charsets.UTF_8));
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            ByteBuffer byteBuffer = memoryStack.malloc(bs.length);
            byteBuffer.put(bs);
            byteBuffer.rewind();
            NativeImage nativeImage = NativeImage.read(byteBuffer);
            return nativeImage;
        }
    }

    public static int getAlpha(int color) {
        return color >> 24 & 0xFF;
    }

    public static int getRed(int color) {
        return color >> 0 & 0xFF;
    }

    public static int getGreen(int color) {
        return color >> 8 & 0xFF;
    }

    public static int getBlue(int color) {
        return color >> 16 & 0xFF;
    }

    /**
     * The resulting color of this operation is stored as RGBA from least to most
     * significant bits, or from smallest to biggest bits.
     */
    public static int packColor(int alpha, int blue, int green, int red) {
        return (alpha & 0xFF) << 24 | (blue & 0xFF) << 16 | (green & 0xFF) << 8 | (red & 0xFF) << 0;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Format {
        RGBA(4, 6408, true, true, true, false, true, 0, 8, 16, 255, 24, true),
        RGB(3, 6407, true, true, true, false, false, 0, 8, 16, 255, 255, true),
        LUMINANCE_ALPHA(2, 33319, false, false, false, true, true, 255, 255, 255, 0, 8, true),
        LUMINANCE(1, 6403, false, false, false, true, false, 0, 0, 0, 0, 255, true);

        final int channelCount;
        private final int glFormat;
        private final boolean hasRed;
        private final boolean hasGreen;
        private final boolean hasBlue;
        private final boolean hasLuminance;
        private final boolean hasAlpha;
        private final int redOffset;
        private final int greenOffset;
        private final int blueOffset;
        private final int luminanceOffset;
        private final int alphaOffset;
        private final boolean writeable;

        private Format(int channelCount, int glFormat, boolean hasRed, boolean hasGreen, boolean hasBlue, boolean hasLuminance, boolean hasAlpha, int redOffset, int greenOffset, int blueOffset, int luminanceOffset, int alphaOffset, boolean writeable) {
            this.channelCount = channelCount;
            this.glFormat = glFormat;
            this.hasRed = hasRed;
            this.hasGreen = hasGreen;
            this.hasBlue = hasBlue;
            this.hasLuminance = hasLuminance;
            this.hasAlpha = hasAlpha;
            this.redOffset = redOffset;
            this.greenOffset = greenOffset;
            this.blueOffset = blueOffset;
            this.luminanceOffset = luminanceOffset;
            this.alphaOffset = alphaOffset;
            this.writeable = writeable;
        }

        public int getChannelCount() {
            return this.channelCount;
        }

        public void setPackAlignment() {
            RenderSystem.assertThread(RenderSystem::isOnRenderThread);
            GlStateManager._pixelStore(3333, this.getChannelCount());
        }

        public void setUnpackAlignment() {
            RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
            GlStateManager._pixelStore(3317, this.getChannelCount());
        }

        public int toGl() {
            return this.glFormat;
        }

        public boolean hasRed() {
            return this.hasRed;
        }

        public boolean hasGreen() {
            return this.hasGreen;
        }

        public boolean hasBlue() {
            return this.hasBlue;
        }

        public boolean hasLuminance() {
            return this.hasLuminance;
        }

        public boolean hasAlpha() {
            return this.hasAlpha;
        }

        public int getRedOffset() {
            return this.redOffset;
        }

        public int getGreenOffset() {
            return this.greenOffset;
        }

        public int getBlueOffset() {
            return this.blueOffset;
        }

        public int getLuminanceOffset() {
            return this.luminanceOffset;
        }

        public int getAlphaOffset() {
            return this.alphaOffset;
        }

        public boolean hasRedChannel() {
            return this.hasLuminance || this.hasRed;
        }

        public boolean hasGreenChannel() {
            return this.hasLuminance || this.hasGreen;
        }

        public boolean hasBlueChannel() {
            return this.hasLuminance || this.hasBlue;
        }

        public boolean hasOpacityChannel() {
            return this.hasLuminance || this.hasAlpha;
        }

        public int getRedChannelOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.redOffset;
        }

        public int getGreenChannelOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.greenOffset;
        }

        public int getBlueChannelOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.blueOffset;
        }

        public int getOpacityChannelOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.alphaOffset;
        }

        public boolean isWriteable() {
            return this.writeable;
        }

        static Format fromGl(int glFormat) {
            switch (glFormat) {
                case 1: {
                    return LUMINANCE;
                }
                case 2: {
                    return LUMINANCE_ALPHA;
                }
                case 3: {
                    return RGB;
                }
            }
            return RGBA;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class WriteCallback
    extends STBIWriteCallback {
        private final WritableByteChannel channel;
        @Nullable
        private IOException exception;

        WriteCallback(WritableByteChannel channel) {
            this.channel = channel;
        }

        @Override
        public void invoke(long context, long data, int size) {
            ByteBuffer byteBuffer = WriteCallback.getData(data, size);
            try {
                this.channel.write(byteBuffer);
            }
            catch (IOException iOException) {
                this.exception = iOException;
            }
        }

        public void throwStoredException() throws IOException {
            if (this.exception != null) {
                throw this.exception;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum InternalFormat {
        RGBA(6408),
        RGB(6407),
        RG(33319),
        RED(6403);

        private final int value;

        private InternalFormat(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}

