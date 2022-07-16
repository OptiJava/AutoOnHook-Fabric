/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.debug;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;

@Environment(value=EnvType.CLIENT)
public class NeighborUpdateDebugRenderer
implements DebugRenderer.Renderer {
    private final MinecraftClient client;
    private final Map<Long, Map<BlockPos, Integer>> neighborUpdates = Maps.newTreeMap(Ordering.natural().reverse());

    NeighborUpdateDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    public void addNeighborUpdate(long time, BlockPos pos) {
        Map map = this.neighborUpdates.computeIfAbsent(time, long_ -> Maps.newHashMap());
        int i = map.getOrDefault(pos, 0);
        map.put(pos, i + 1);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        Object map2;
        Comparable<Long> long_;
        long l = this.client.world.getTime();
        int i = 200;
        double d = 0.0025;
        HashSet<BlockPos> set = Sets.newHashSet();
        HashMap<BlockPos, Integer> map = Maps.newHashMap();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
        Iterator<Map.Entry<Long, Map<BlockPos, Integer>>> iterator = this.neighborUpdates.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Map<BlockPos, Integer>> entry = iterator.next();
            long_ = entry.getKey();
            map2 = entry.getValue();
            long m = l - (Long)long_;
            if (m > 200L) {
                iterator.remove();
                continue;
            }
            for (Map.Entry entry2 : map2.entrySet()) {
                BlockPos blockPos = (BlockPos)entry2.getKey();
                Integer integer = (Integer)entry2.getValue();
                if (!set.add(blockPos)) continue;
                Box box = new Box(BlockPos.ORIGIN).expand(0.002).contract(0.0025 * (double)m).offset(blockPos.getX(), blockPos.getY(), blockPos.getZ()).offset(-cameraX, -cameraY, -cameraZ);
                WorldRenderer.drawBox(matrices, vertexConsumer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, 1.0f, 1.0f, 1.0f, 1.0f);
                map.put(blockPos, integer);
            }
        }
        for (Map.Entry entry : map.entrySet()) {
            long_ = (BlockPos)entry.getKey();
            map2 = (Integer)entry.getValue();
            DebugRenderer.drawString(String.valueOf(map2), ((Vec3i)long_).getX(), ((Vec3i)long_).getY(), ((Vec3i)long_).getZ(), -1);
        }
    }
}

