/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

@Environment(value=EnvType.CLIENT)
public class WaterDebugRenderer
implements DebugRenderer.Renderer {
    private final MinecraftClient client;

    public WaterDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        FluidState fluidState;
        BlockPos blockPos = this.client.player.getBlockPos();
        World worldView = this.client.player.world;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(0.0f, 1.0f, 0.0f, 0.75f);
        RenderSystem.disableTexture();
        RenderSystem.lineWidth(6.0f);
        for (BlockPos blockPos2 : BlockPos.iterate(blockPos.add(-10, -10, -10), blockPos.add(10, 10, 10))) {
            fluidState = worldView.getFluidState(blockPos2);
            if (!fluidState.isIn(FluidTags.WATER)) continue;
            double d = (float)blockPos2.getY() + fluidState.getHeight(worldView, blockPos2);
            DebugRenderer.drawBox(new Box((float)blockPos2.getX() + 0.01f, (float)blockPos2.getY() + 0.01f, (float)blockPos2.getZ() + 0.01f, (float)blockPos2.getX() + 0.99f, d, (float)blockPos2.getZ() + 0.99f).offset(-cameraX, -cameraY, -cameraZ), 1.0f, 1.0f, 1.0f, 0.2f);
        }
        for (BlockPos blockPos2 : BlockPos.iterate(blockPos.add(-10, -10, -10), blockPos.add(10, 10, 10))) {
            fluidState = worldView.getFluidState(blockPos2);
            if (!fluidState.isIn(FluidTags.WATER)) continue;
            DebugRenderer.drawString(String.valueOf(fluidState.getLevel()), (double)blockPos2.getX() + 0.5, (float)blockPos2.getY() + fluidState.getHeight(worldView, blockPos2), (double)blockPos2.getZ() + 0.5, -16777216);
        }
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}

