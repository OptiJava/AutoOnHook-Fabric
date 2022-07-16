/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.block.entity;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BlockEntityRenderDispatcher
implements SynchronousResourceReloader {
    private Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers = ImmutableMap.of();
    private final TextRenderer textRenderer;
    private final EntityModelLoader entityModelLoader;
    public World world;
    public Camera camera;
    public HitResult crosshairTarget;
    private final Supplier<BlockRenderManager> blockRenderManager;

    public BlockEntityRenderDispatcher(TextRenderer textRenderer, EntityModelLoader entityModelLoader, Supplier<BlockRenderManager> blockRenderManager) {
        this.textRenderer = textRenderer;
        this.entityModelLoader = entityModelLoader;
        this.blockRenderManager = blockRenderManager;
    }

    @Nullable
    public <E extends BlockEntity> BlockEntityRenderer<E> get(E blockEntity) {
        return this.renderers.get(blockEntity.getType());
    }

    public void configure(World world, Camera camera, HitResult crosshairTarget) {
        if (this.world != world) {
            this.setWorld(world);
        }
        this.camera = camera;
        this.crosshairTarget = crosshairTarget;
    }

    public <E extends BlockEntity> void render(E blockEntity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertexConsumers) {
        BlockEntityRenderer blockEntityRenderer = this.get(blockEntity);
        if (blockEntityRenderer == null) {
            return;
        }
        if (!blockEntity.hasWorld() || !blockEntity.getType().supports(blockEntity.getCachedState())) {
            return;
        }
        if (!blockEntityRenderer.isInRenderDistance(blockEntity, this.camera.getPos())) {
            return;
        }
        BlockEntityRenderDispatcher.runReported(blockEntity, () -> BlockEntityRenderDispatcher.render(blockEntityRenderer, blockEntity, tickDelta, matrix, vertexConsumers));
    }

    private static <T extends BlockEntity> void render(BlockEntityRenderer<T> renderer, T blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        World world = blockEntity.getWorld();
        int i = world != null ? WorldRenderer.getLightmapCoordinates(world, blockEntity.getPos()) : LightmapTextureManager.MAX_LIGHT_COORDINATE;
        renderer.render(blockEntity, tickDelta, matrices, vertexConsumers, i, OverlayTexture.DEFAULT_UV);
    }

    public <E extends BlockEntity> boolean renderEntity(E entity, MatrixStack matrix, VertexConsumerProvider vertexConsumerProvider, int light, int overlay) {
        BlockEntityRenderer blockEntityRenderer = this.get(entity);
        if (blockEntityRenderer == null) {
            return true;
        }
        BlockEntityRenderDispatcher.runReported(entity, () -> blockEntityRenderer.render(entity, 0.0f, matrix, vertexConsumerProvider, light, overlay));
        return false;
    }

    private static void runReported(BlockEntity blockEntity, Runnable runnable) {
        try {
            runnable.run();
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.create(throwable, "Rendering Block Entity");
            CrashReportSection crashReportSection = crashReport.addElement("Block Entity Details");
            blockEntity.populateCrashReport(crashReportSection);
            throw new CrashException(crashReport);
        }
    }

    public void setWorld(@Nullable World world) {
        this.world = world;
        if (world == null) {
            this.camera = null;
        }
    }

    @Override
    public void reload(ResourceManager manager) {
        BlockEntityRendererFactory.Context context = new BlockEntityRendererFactory.Context(this, this.blockRenderManager.get(), this.entityModelLoader, this.textRenderer);
        this.renderers = BlockEntityRendererFactories.reload(context);
    }
}

