/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class AsyncTexture
extends ResourceTexture {
    @Nullable
    private CompletableFuture<ResourceTexture.TextureData> future;

    public AsyncTexture(ResourceManager resourceManager, Identifier identifier, Executor executor) {
        super(identifier);
        this.future = CompletableFuture.supplyAsync(() -> ResourceTexture.TextureData.load(resourceManager, identifier), executor);
    }

    @Override
    protected ResourceTexture.TextureData loadTextureData(ResourceManager resourceManager) {
        if (this.future != null) {
            ResourceTexture.TextureData textureData = this.future.join();
            this.future = null;
            return textureData;
        }
        return ResourceTexture.TextureData.load(resourceManager, this.location);
    }

    public CompletableFuture<Void> getLoadCompleteFuture() {
        return this.future == null ? CompletableFuture.completedFuture(null) : this.future.thenApply(textureData -> null);
    }

    @Override
    public void registerTexture(TextureManager textureManager, ResourceManager resourceManager, Identifier identifier, Executor executor) {
        this.future = CompletableFuture.supplyAsync(() -> ResourceTexture.TextureData.load(resourceManager, this.location), Util.getMainWorkerExecutor());
        this.future.thenRunAsync(() -> textureManager.registerTexture(this.location, this), AsyncTexture.method_22808(executor));
    }

    private static Executor method_22808(Executor executor) {
        return runnable -> executor.execute(() -> RenderSystem.recordRenderCall(runnable::run));
    }
}

