/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class EntityRenderDispatcher
implements SynchronousResourceReloader {
    private static final RenderLayer SHADOW_LAYER = RenderLayer.getEntityShadow(new Identifier("textures/misc/shadow.png"));
    private Map<EntityType<?>, EntityRenderer<?>> renderers = ImmutableMap.of();
    private Map<String, EntityRenderer<? extends PlayerEntity>> modelRenderers = ImmutableMap.of();
    public final TextureManager textureManager;
    private World world;
    public Camera camera;
    private Quaternion rotation;
    public Entity targetedEntity;
    private final ItemRenderer itemRenderer;
    private final TextRenderer textRenderer;
    public final GameOptions gameOptions;
    private final EntityModelLoader modelLoader;
    private boolean renderShadows = true;
    private boolean renderHitboxes;

    public <E extends Entity> int getLight(E entity, float tickDelta) {
        return this.getRenderer(entity).getLight(entity, tickDelta);
    }

    public EntityRenderDispatcher(TextureManager textureManager, ItemRenderer itemRenderer, TextRenderer textRenderer, GameOptions gameOptions, EntityModelLoader modelLoader) {
        this.textureManager = textureManager;
        this.itemRenderer = itemRenderer;
        this.textRenderer = textRenderer;
        this.gameOptions = gameOptions;
        this.modelLoader = modelLoader;
    }

    public <T extends Entity> EntityRenderer<? super T> getRenderer(T entity) {
        if (entity instanceof AbstractClientPlayerEntity) {
            String string = ((AbstractClientPlayerEntity)entity).getModel();
            EntityRenderer<? extends PlayerEntity> entityRenderer = this.modelRenderers.get(string);
            if (entityRenderer != null) {
                return entityRenderer;
            }
            return this.modelRenderers.get("default");
        }
        return this.renderers.get(entity.getType());
    }

    public void configure(World world, Camera camera, Entity target) {
        this.world = world;
        this.camera = camera;
        this.rotation = camera.getRotation();
        this.targetedEntity = target;
    }

    public void setRotation(Quaternion rotation) {
        this.rotation = rotation;
    }

    public void setRenderShadows(boolean value) {
        this.renderShadows = value;
    }

    public void setRenderHitboxes(boolean value) {
        this.renderHitboxes = value;
    }

    public boolean shouldRenderHitboxes() {
        return this.renderHitboxes;
    }

    public <E extends Entity> boolean shouldRender(E entity, Frustum frustum, double x, double y, double z) {
        EntityRenderer<E> entityRenderer = this.getRenderer(entity);
        return entityRenderer.shouldRender(entity, frustum, x, y, z);
    }

    public <E extends Entity> void render(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        EntityRenderer<E> entityRenderer = this.getRenderer(entity);
        try {
            double g;
            float h;
            Vec3d vec3d = entityRenderer.getPositionOffset(entity, tickDelta);
            double d = x + vec3d.getX();
            double e = y + vec3d.getY();
            double f = z + vec3d.getZ();
            matrices.push();
            matrices.translate(d, e, f);
            entityRenderer.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
            if (entity.doesRenderOnFire()) {
                this.renderFire(matrices, vertexConsumers, entity);
            }
            matrices.translate(-vec3d.getX(), -vec3d.getY(), -vec3d.getZ());
            if (this.gameOptions.entityShadows && this.renderShadows && entityRenderer.shadowRadius > 0.0f && !entity.isInvisible() && (h = (float)((1.0 - (g = this.getSquaredDistanceToCamera(entity.getX(), entity.getY(), entity.getZ())) / 256.0) * (double)entityRenderer.shadowOpacity)) > 0.0f) {
                EntityRenderDispatcher.renderShadow(matrices, vertexConsumers, entity, h, tickDelta, this.world, entityRenderer.shadowRadius);
            }
            if (this.renderHitboxes && !entity.isInvisible() && !MinecraftClient.getInstance().hasReducedDebugInfo()) {
                EntityRenderDispatcher.renderHitbox(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), entity, tickDelta);
            }
            matrices.pop();
        }
        catch (Throwable vec3d) {
            CrashReport d = CrashReport.create(vec3d, "Rendering entity in world");
            CrashReportSection crashReportSection = d.addElement("Entity being rendered");
            entity.populateCrashReport(crashReportSection);
            CrashReportSection e = d.addElement("Renderer details");
            e.add("Assigned renderer", entityRenderer);
            e.add("Location", CrashReportSection.createPositionString((HeightLimitView)this.world, x, y, z));
            e.add("Rotation", Float.valueOf(yaw));
            e.add("Delta", Float.valueOf(tickDelta));
            throw new CrashException(d);
        }
    }

    private static void renderHitbox(MatrixStack matrices, VertexConsumer vertices, Entity entity, float tickDelta) {
        Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
        WorldRenderer.drawBox(matrices, vertices, box, 1.0f, 1.0f, 1.0f, 1.0f);
        if (entity instanceof EnderDragonEntity) {
            double d = -MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX());
            double e = -MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY());
            double f = -MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ());
            for (EnderDragonPart enderDragonPart : ((EnderDragonEntity)entity).getBodyParts()) {
                matrices.push();
                double g = d + MathHelper.lerp((double)tickDelta, enderDragonPart.lastRenderX, enderDragonPart.getX());
                double h = e + MathHelper.lerp((double)tickDelta, enderDragonPart.lastRenderY, enderDragonPart.getY());
                double i = f + MathHelper.lerp((double)tickDelta, enderDragonPart.lastRenderZ, enderDragonPart.getZ());
                matrices.translate(g, h, i);
                WorldRenderer.drawBox(matrices, vertices, enderDragonPart.getBoundingBox().offset(-enderDragonPart.getX(), -enderDragonPart.getY(), -enderDragonPart.getZ()), 0.25f, 1.0f, 0.0f, 1.0f);
                matrices.pop();
            }
        }
        if (entity instanceof LivingEntity) {
            float d = 0.01f;
            WorldRenderer.drawBox(matrices, vertices, box.minX, entity.getStandingEyeHeight() - 0.01f, box.minZ, box.maxX, entity.getStandingEyeHeight() + 0.01f, box.maxZ, 1.0f, 0.0f, 0.0f, 1.0f);
        }
        Vec3d d = entity.getRotationVec(tickDelta);
        Matrix4f matrix4f = matrices.peek().getModel();
        Matrix3f e = matrices.peek().getNormal();
        vertices.vertex(matrix4f, 0.0f, entity.getStandingEyeHeight(), 0.0f).color(0, 0, 255, 255).normal(e, (float)d.x, (float)d.y, (float)d.z).next();
        vertices.vertex(matrix4f, (float)(d.x * 2.0), (float)((double)entity.getStandingEyeHeight() + d.y * 2.0), (float)(d.z * 2.0)).color(0, 0, 255, 255).normal(e, (float)d.x, (float)d.y, (float)d.z).next();
    }

    private void renderFire(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity) {
        Sprite sprite = ModelLoader.FIRE_0.getSprite();
        Sprite sprite2 = ModelLoader.FIRE_1.getSprite();
        matrices.push();
        float f = entity.getWidth() * 1.4f;
        matrices.scale(f, f, f);
        float g = 0.5f;
        float h = 0.0f;
        float i = entity.getHeight() / f;
        float j = 0.0f;
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-this.camera.getYaw()));
        matrices.translate(0.0, 0.0, -0.3f + (float)((int)i) * 0.02f);
        float k = 0.0f;
        int l = 0;
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
        MatrixStack.Entry entry = matrices.peek();
        while (i > 0.0f) {
            Sprite sprite3 = l % 2 == 0 ? sprite : sprite2;
            float m = sprite3.getMinU();
            float n = sprite3.getMinV();
            float o = sprite3.getMaxU();
            float p = sprite3.getMaxV();
            if (l / 2 % 2 == 0) {
                float q = o;
                o = m;
                m = q;
            }
            EntityRenderDispatcher.drawFireVertex(entry, vertexConsumer, g - 0.0f, 0.0f - j, k, o, p);
            EntityRenderDispatcher.drawFireVertex(entry, vertexConsumer, -g - 0.0f, 0.0f - j, k, m, p);
            EntityRenderDispatcher.drawFireVertex(entry, vertexConsumer, -g - 0.0f, 1.4f - j, k, m, n);
            EntityRenderDispatcher.drawFireVertex(entry, vertexConsumer, g - 0.0f, 1.4f - j, k, o, n);
            i -= 0.45f;
            j -= 0.45f;
            g *= 0.9f;
            k += 0.03f;
            ++l;
        }
        matrices.pop();
    }

    private static void drawFireVertex(MatrixStack.Entry entry, VertexConsumer vertices, float x, float y, float z, float u, float v) {
        vertices.vertex(entry.getModel(), x, y, z).color(255, 255, 255, 255).texture(u, v).overlay(0, 10).light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE).normal(entry.getNormal(), 0.0f, 1.0f, 0.0f).next();
    }

    private static void renderShadow(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity, float opacity, float tickDelta, WorldView world, float radius) {
        MobEntity mobEntity;
        float f = radius;
        if (entity instanceof MobEntity && (mobEntity = (MobEntity)entity).isBaby()) {
            f *= 0.5f;
        }
        double mobEntity2 = MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX());
        double d = MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY());
        double e = MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ());
        int i = MathHelper.floor(mobEntity2 - (double)f);
        int j = MathHelper.floor(mobEntity2 + (double)f);
        int k = MathHelper.floor(d - (double)f);
        int l = MathHelper.floor(d);
        int m = MathHelper.floor(e - (double)f);
        int n = MathHelper.floor(e + (double)f);
        MatrixStack.Entry entry = matrices.peek();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(SHADOW_LAYER);
        for (BlockPos blockPos : BlockPos.iterate(new BlockPos(i, k, m), new BlockPos(j, l, n))) {
            EntityRenderDispatcher.renderShadowPart(entry, vertexConsumer, world, blockPos, mobEntity2, d, e, f, opacity);
        }
    }

    private static void renderShadowPart(MatrixStack.Entry entry, VertexConsumer vertices, WorldView world, BlockPos pos, double x, double y, double z, float radius, float opacity) {
        BlockPos blockPos = pos.down();
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.getRenderType() == BlockRenderType.INVISIBLE || world.getLightLevel(pos) <= 3) {
            return;
        }
        if (!blockState.isFullCube(world, blockPos)) {
            return;
        }
        VoxelShape voxelShape = blockState.getOutlineShape(world, pos.down());
        if (voxelShape.isEmpty()) {
            return;
        }
        float f = (float)(((double)opacity - (y - (double)pos.getY()) / 2.0) * 0.5 * (double)world.getBrightness(pos));
        if (f >= 0.0f) {
            if (f > 1.0f) {
                f = 1.0f;
            }
            Box box = voxelShape.getBoundingBox();
            double d = (double)pos.getX() + box.minX;
            double e = (double)pos.getX() + box.maxX;
            double g = (double)pos.getY() + box.minY;
            double h = (double)pos.getZ() + box.minZ;
            double i = (double)pos.getZ() + box.maxZ;
            float j = (float)(d - x);
            float k = (float)(e - x);
            float l = (float)(g - y);
            float m = (float)(h - z);
            float n = (float)(i - z);
            float o = -j / 2.0f / radius + 0.5f;
            float p = -k / 2.0f / radius + 0.5f;
            float q = -m / 2.0f / radius + 0.5f;
            float r = -n / 2.0f / radius + 0.5f;
            EntityRenderDispatcher.drawShadowVertex(entry, vertices, f, j, l, m, o, q);
            EntityRenderDispatcher.drawShadowVertex(entry, vertices, f, j, l, n, o, r);
            EntityRenderDispatcher.drawShadowVertex(entry, vertices, f, k, l, n, p, r);
            EntityRenderDispatcher.drawShadowVertex(entry, vertices, f, k, l, m, p, q);
        }
    }

    private static void drawShadowVertex(MatrixStack.Entry entry, VertexConsumer vertices, float alpha, float x, float y, float z, float u, float v) {
        vertices.vertex(entry.getModel(), x, y, z).color(1.0f, 1.0f, 1.0f, alpha).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(entry.getNormal(), 0.0f, 1.0f, 0.0f).next();
    }

    public void setWorld(@Nullable World world) {
        this.world = world;
        if (world == null) {
            this.camera = null;
        }
    }

    public double getSquaredDistanceToCamera(Entity entity) {
        return this.camera.getPos().squaredDistanceTo(entity.getPos());
    }

    public double getSquaredDistanceToCamera(double x, double y, double z) {
        return this.camera.getPos().squaredDistanceTo(x, y, z);
    }

    public Quaternion getRotation() {
        return this.rotation;
    }

    @Override
    public void reload(ResourceManager manager) {
        EntityRendererFactory.Context context = new EntityRendererFactory.Context(this, this.itemRenderer, manager, this.modelLoader, this.textRenderer);
        this.renderers = EntityRenderers.reloadEntityRenderers(context);
        this.modelRenderers = EntityRenderers.reloadPlayerRenderers(context);
    }
}

