/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.Monitor;
import net.minecraft.client.util.MonitorFactory;
import net.minecraft.client.util.Window;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMonitorCallback;

@Environment(value=EnvType.CLIENT)
public class MonitorTracker {
    private final Long2ObjectMap<Monitor> pointerToMonitorMap = new Long2ObjectOpenHashMap<Monitor>();
    private final MonitorFactory monitorFactory;

    public MonitorTracker(MonitorFactory monitorFactory) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        this.monitorFactory = monitorFactory;
        GLFW.glfwSetMonitorCallback(this::handleMonitorEvent);
        PointerBuffer pointerBuffer = GLFW.glfwGetMonitors();
        if (pointerBuffer != null) {
            for (int i = 0; i < pointerBuffer.limit(); ++i) {
                long l = pointerBuffer.get(i);
                this.pointerToMonitorMap.put(l, monitorFactory.createMonitor(l));
            }
        }
    }

    private void handleMonitorEvent(long monitor, int event) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (event == 262145) {
            this.pointerToMonitorMap.put(monitor, this.monitorFactory.createMonitor(monitor));
        } else if (event == 262146) {
            this.pointerToMonitorMap.remove(monitor);
        }
    }

    @Nullable
    public Monitor getMonitor(long l) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        return (Monitor)this.pointerToMonitorMap.get(l);
    }

    @Nullable
    public Monitor getMonitor(Window window) {
        long l = GLFW.glfwGetWindowMonitor(window.getHandle());
        if (l != 0L) {
            return this.getMonitor(l);
        }
        int i = window.getX();
        int j = i + window.getWidth();
        int k = window.getY();
        int m = k + window.getHeight();
        int n = -1;
        Monitor monitor = null;
        for (Monitor monitor2 : this.pointerToMonitorMap.values()) {
            int x;
            int o = monitor2.getViewportX();
            int p = o + monitor2.getCurrentVideoMode().getWidth();
            int q = monitor2.getViewportY();
            int r = q + monitor2.getCurrentVideoMode().getHeight();
            int s = MonitorTracker.clamp(i, o, p);
            int t = MonitorTracker.clamp(j, o, p);
            int u = MonitorTracker.clamp(k, q, r);
            int v = MonitorTracker.clamp(m, q, r);
            int w = Math.max(0, t - s);
            int y = w * (x = Math.max(0, v - u));
            if (y <= n) continue;
            monitor = monitor2;
            n = y;
        }
        return monitor;
    }

    public static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public void stop() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GLFWMonitorCallback gLFWMonitorCallback = GLFW.glfwSetMonitorCallback(null);
        if (gLFWMonitorCallback != null) {
            gLFWMonitorCallback.free();
        }
    }
}

