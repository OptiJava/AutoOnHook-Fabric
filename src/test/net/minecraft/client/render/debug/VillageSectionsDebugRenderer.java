/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.debug;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

@Environment(value=EnvType.CLIENT)
public class VillageSectionsDebugRenderer
implements DebugRenderer.Renderer {
    private static final int RANGE = 60;
    private final Set<ChunkSectionPos> sections = Sets.newHashSet();

    VillageSectionsDebugRenderer() {
    }

    @Override
    public void clear() {
        this.sections.clear();
    }

    public void addSection(ChunkSectionPos pos) {
        this.sections.add(pos);
    }

    public void removeSection(ChunkSectionPos pos) {
        this.sections.remove(pos);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        this.drawSections(cameraX, cameraY, cameraZ);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private void drawSections(double cameraX, double cameraY, double cameraZ) {
        BlockPos blockPos = new BlockPos(cameraX, cameraY, cameraZ);
        this.sections.forEach(section -> {
            if (blockPos.isWithinDistance(section.getCenterPos(), 60.0)) {
                VillageSectionsDebugRenderer.drawBoxAtCenterOf(section);
            }
        });
    }

    private static void drawBoxAtCenterOf(ChunkSectionPos pos) {
        float f = 1.0f;
        BlockPos blockPos = pos.getCenterPos();
        BlockPos blockPos2 = blockPos.add(-1.0, -1.0, -1.0);
        BlockPos blockPos3 = blockPos.add(1.0, 1.0, 1.0);
        DebugRenderer.drawBox(blockPos2, blockPos3, 0.2f, 1.0f, 0.2f, 0.15f);
    }
}

