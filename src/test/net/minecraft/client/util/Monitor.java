/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.util;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.VideoMode;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

@Environment(value=EnvType.CLIENT)
public final class Monitor {
    private final long handle;
    private final List<VideoMode> videoModes;
    private VideoMode currentVideoMode;
    private int x;
    private int y;

    public Monitor(long handle) {
        this.handle = handle;
        this.videoModes = Lists.newArrayList();
        this.populateVideoModes();
    }

    public void populateVideoModes() {
        Object videoMode;
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        this.videoModes.clear();
        GLFWVidMode.Buffer buffer = GLFW.glfwGetVideoModes(this.handle);
        for (int i = buffer.limit() - 1; i >= 0; --i) {
            buffer.position(i);
            videoMode = new VideoMode(buffer);
            if (((VideoMode)videoMode).getRedBits() < 8 || ((VideoMode)videoMode).getGreenBits() < 8 || ((VideoMode)videoMode).getBlueBits() < 8) continue;
            this.videoModes.add((VideoMode)videoMode);
        }
        int[] i = new int[1];
        videoMode = new int[1];
        GLFW.glfwGetMonitorPos(this.handle, i, (int[])videoMode);
        this.x = i[0];
        this.y = (int)videoMode[0];
        GLFWVidMode gLFWVidMode = GLFW.glfwGetVideoMode(this.handle);
        this.currentVideoMode = new VideoMode(gLFWVidMode);
    }

    public VideoMode findClosestVideoMode(Optional<VideoMode> videoMode) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        if (videoMode.isPresent()) {
            VideoMode videoMode2 = videoMode.get();
            for (VideoMode videoMode3 : this.videoModes) {
                if (!videoMode3.equals(videoMode2)) continue;
                return videoMode3;
            }
        }
        return this.getCurrentVideoMode();
    }

    public int findClosestVideoModeIndex(VideoMode videoMode) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        return this.videoModes.indexOf(videoMode);
    }

    public VideoMode getCurrentVideoMode() {
        return this.currentVideoMode;
    }

    public int getViewportX() {
        return this.x;
    }

    public int getViewportY() {
        return this.y;
    }

    public VideoMode getVideoMode(int index) {
        return this.videoModes.get(index);
    }

    public int getVideoModeCount() {
        return this.videoModes.size();
    }

    public long getHandle() {
        return this.handle;
    }

    public String toString() {
        return String.format("Monitor[%s %sx%s %s]", this.handle, this.x, this.y, this.currentVideoMode);
    }
}

