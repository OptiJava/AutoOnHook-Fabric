/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.PointedDripstoneBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.Option;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltChunkStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.FpsSmoother;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.ParticleUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vector4f;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class WorldRenderer
implements SynchronousResourceReloader,
AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int field_32759 = 16;
    public static final int field_32760 = 66;
    public static final int field_32761 = 4356;
    private static final float field_32762 = 512.0f;
    private static final int field_32763 = 32;
    private static final int field_32764 = 10;
    private static final int field_32765 = 21;
    private static final int field_32766 = 15;
    private static final Identifier MOON_PHASES = new Identifier("textures/environment/moon_phases.png");
    private static final Identifier SUN = new Identifier("textures/environment/sun.png");
    private static final Identifier CLOUDS = new Identifier("textures/environment/clouds.png");
    private static final Identifier END_SKY = new Identifier("textures/environment/end_sky.png");
    private static final Identifier FORCEFIELD = new Identifier("textures/misc/forcefield.png");
    private static final Identifier RAIN = new Identifier("textures/environment/rain.png");
    private static final Identifier SNOW = new Identifier("textures/environment/snow.png");
    public static final Direction[] DIRECTIONS = Direction.values();
    private final MinecraftClient client;
    private final TextureManager textureManager;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final BufferBuilderStorage bufferBuilders;
    private ClientWorld world;
    private Set<ChunkBuilder.BuiltChunk> chunksToRebuild = Sets.newLinkedHashSet();
    private final ObjectArrayList<ChunkInfo> visibleChunks = new ObjectArrayList();
    private final Set<BlockEntity> noCullingBlockEntities = Sets.newHashSet();
    private BuiltChunkStorage chunks;
    private ChunkInfoList chunkInfos;
    @Nullable
    private VertexBuffer starsBuffer;
    @Nullable
    private VertexBuffer lightSkyBuffer;
    @Nullable
    private VertexBuffer darkSkyBuffer;
    private boolean cloudsDirty = true;
    @Nullable
    private VertexBuffer cloudsBuffer;
    private final FpsSmoother chunkUpdateSmoother = new FpsSmoother(100);
    private int ticks;
    private final Int2ObjectMap<BlockBreakingInfo> blockBreakingInfos = new Int2ObjectOpenHashMap<BlockBreakingInfo>();
    private final Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions = new Long2ObjectOpenHashMap<SortedSet<BlockBreakingInfo>>();
    private final Map<BlockPos, SoundInstance> playingSongs = Maps.newHashMap();
    @Nullable
    private Framebuffer entityOutlinesFramebuffer;
    @Nullable
    private ShaderEffect entityOutlineShader;
    @Nullable
    private Framebuffer translucentFramebuffer;
    @Nullable
    private Framebuffer entityFramebuffer;
    @Nullable
    private Framebuffer particlesFramebuffer;
    @Nullable
    private Framebuffer weatherFramebuffer;
    @Nullable
    private Framebuffer cloudsFramebuffer;
    @Nullable
    private ShaderEffect transparencyShader;
    private double lastCameraChunkUpdateX = Double.MIN_VALUE;
    private double lastCameraChunkUpdateY = Double.MIN_VALUE;
    private double lastCameraChunkUpdateZ = Double.MIN_VALUE;
    private int cameraChunkX = Integer.MIN_VALUE;
    private int cameraChunkY = Integer.MIN_VALUE;
    private int cameraChunkZ = Integer.MIN_VALUE;
    private double lastCameraX = Double.MIN_VALUE;
    private double lastCameraY = Double.MIN_VALUE;
    private double lastCameraZ = Double.MIN_VALUE;
    private double lastCameraPitch = Double.MIN_VALUE;
    private double lastCameraYaw = Double.MIN_VALUE;
    private int lastCloudsBlockX = Integer.MIN_VALUE;
    private int lastCloudsBlockY = Integer.MIN_VALUE;
    private int lastCloudsBlockZ = Integer.MIN_VALUE;
    private Vec3d lastCloudsColor = Vec3d.ZERO;
    private CloudRenderMode lastCloudsRenderMode;
    private ChunkBuilder chunkBuilder;
    private int viewDistance = -1;
    private int regularEntityCount;
    private int blockEntityCount;
    private Frustum frustum;
    private boolean shouldCaptureFrustum;
    @Nullable
    private Frustum capturedFrustum;
    private final Vector4f[] capturedFrustumOrientation = new Vector4f[8];
    private final Vector3d capturedFrustumPosition = new Vector3d(0.0, 0.0, 0.0);
    private double lastTranslucentSortX;
    private double lastTranslucentSortY;
    private double lastTranslucentSortZ;
    private boolean needsTerrainUpdate = true;
    private int frame;
    private int field_20793;
    private final float[] field_20794 = new float[1024];
    private final float[] field_20795 = new float[1024];

    public WorldRenderer(MinecraftClient client, BufferBuilderStorage bufferBuilders) {
        this.client = client;
        this.entityRenderDispatcher = client.getEntityRenderDispatcher();
        this.blockEntityRenderDispatcher = client.getBlockEntityRenderDispatcher();
        this.bufferBuilders = bufferBuilders;
        this.textureManager = client.getTextureManager();
        for (int i = 0; i < 32; ++i) {
            for (int j = 0; j < 32; ++j) {
                float f = j - 16;
                float g = i - 16;
                float h = MathHelper.sqrt(f * f + g * g);
                this.field_20794[i << 5 | j] = -g / h;
                this.field_20795[i << 5 | j] = f / h;
            }
        }
        this.renderStars();
        this.renderLightSky();
        this.renderDarkSky();
    }

    private void renderWeather(LightmapTextureManager manager, float f, double d, double e, double g) {
        float h = this.client.world.getRainGradient(f);
        if (h <= 0.0f) {
            return;
        }
        manager.enable();
        ClientWorld world = this.client.world;
        int i = MathHelper.floor(d);
        int j = MathHelper.floor(e);
        int k = MathHelper.floor(g);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        int l = 5;
        if (MinecraftClient.isFancyGraphicsOrBetter()) {
            l = 10;
        }
        RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
        int m = -1;
        float n = (float)this.ticks + f;
        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int o = k - l; o <= k + l; ++o) {
            for (int p = i - l; p <= i + l; ++p) {
                float ad;
                float z;
                int w;
                int q = (o - k + 16) * 32 + p - i + 16;
                double r = (double)this.field_20794[q] * 0.5;
                double s = (double)this.field_20795[q] * 0.5;
                mutable.set(p, 0, o);
                Biome biome = world.getBiome(mutable);
                if (biome.getPrecipitation() == Biome.Precipitation.NONE) continue;
                int t = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, mutable).getY();
                int u = j - l;
                int v = j + l;
                if (u < t) {
                    u = t;
                }
                if (v < t) {
                    v = t;
                }
                if ((w = t) < j) {
                    w = j;
                }
                if (u == v) continue;
                Random random = new Random(p * p * 3121 + p * 45238971 ^ o * o * 418711 + o * 13761);
                mutable.set(p, u, o);
                float x = biome.getTemperature(mutable);
                if (x >= 0.15f) {
                    if (m != 0) {
                        if (m >= 0) {
                            tessellator.draw();
                        }
                        m = 0;
                        RenderSystem.setShaderTexture(0, RAIN);
                        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
                    }
                    int y = this.ticks + p * p * 3121 + p * 45238971 + o * o * 418711 + o * 13761 & 0x1F;
                    z = -((float)y + f) / 32.0f * (3.0f + random.nextFloat());
                    double aa = (double)p + 0.5 - d;
                    double ab = (double)o + 0.5 - g;
                    float ac = (float)Math.sqrt(aa * aa + ab * ab) / (float)l;
                    ad = ((1.0f - ac * ac) * 0.5f + 0.5f) * h;
                    mutable.set(p, w, o);
                    int ae = WorldRenderer.getLightmapCoordinates(world, mutable);
                    bufferBuilder.vertex((double)p - d - r + 0.5, (double)v - e, (double)o - g - s + 0.5).texture(0.0f, (float)u * 0.25f + z).color(1.0f, 1.0f, 1.0f, ad).light(ae).next();
                    bufferBuilder.vertex((double)p - d + r + 0.5, (double)v - e, (double)o - g + s + 0.5).texture(1.0f, (float)u * 0.25f + z).color(1.0f, 1.0f, 1.0f, ad).light(ae).next();
                    bufferBuilder.vertex((double)p - d + r + 0.5, (double)u - e, (double)o - g + s + 0.5).texture(1.0f, (float)v * 0.25f + z).color(1.0f, 1.0f, 1.0f, ad).light(ae).next();
                    bufferBuilder.vertex((double)p - d - r + 0.5, (double)u - e, (double)o - g - s + 0.5).texture(0.0f, (float)v * 0.25f + z).color(1.0f, 1.0f, 1.0f, ad).light(ae).next();
                    continue;
                }
                if (m != 1) {
                    if (m >= 0) {
                        tessellator.draw();
                    }
                    m = 1;
                    RenderSystem.setShaderTexture(0, SNOW);
                    bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
                }
                float y = -((float)(this.ticks & 0x1FF) + f) / 512.0f;
                z = (float)(random.nextDouble() + (double)n * 0.01 * (double)((float)random.nextGaussian()));
                float aa = (float)(random.nextDouble() + (double)(n * (float)random.nextGaussian()) * 0.001);
                double af = (double)p + 0.5 - d;
                double ag = (double)o + 0.5 - g;
                ad = (float)Math.sqrt(af * af + ag * ag) / (float)l;
                float ae = ((1.0f - ad * ad) * 0.3f + 0.5f) * h;
                mutable.set(p, w, o);
                int ah = WorldRenderer.getLightmapCoordinates(world, mutable);
                int ai = ah >> 16 & 0xFFFF;
                int aj = ah & 0xFFFF;
                int ak = (ai * 3 + 240) / 4;
                int al = (aj * 3 + 240) / 4;
                bufferBuilder.vertex((double)p - d - r + 0.5, (double)v - e, (double)o - g - s + 0.5).texture(0.0f + z, (float)u * 0.25f + y + aa).color(1.0f, 1.0f, 1.0f, ae).light(al, ak).next();
                bufferBuilder.vertex((double)p - d + r + 0.5, (double)v - e, (double)o - g + s + 0.5).texture(1.0f + z, (float)u * 0.25f + y + aa).color(1.0f, 1.0f, 1.0f, ae).light(al, ak).next();
                bufferBuilder.vertex((double)p - d + r + 0.5, (double)u - e, (double)o - g + s + 0.5).texture(1.0f + z, (float)v * 0.25f + y + aa).color(1.0f, 1.0f, 1.0f, ae).light(al, ak).next();
                bufferBuilder.vertex((double)p - d - r + 0.5, (double)u - e, (double)o - g - s + 0.5).texture(0.0f + z, (float)v * 0.25f + y + aa).color(1.0f, 1.0f, 1.0f, ae).light(al, ak).next();
            }
        }
        if (m >= 0) {
            tessellator.draw();
        }
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        manager.disable();
    }

    public void tickRainSplashing(Camera camera) {
        float f = this.client.world.getRainGradient(1.0f) / (MinecraftClient.isFancyGraphicsOrBetter() ? 1.0f : 2.0f);
        if (f <= 0.0f) {
            return;
        }
        Random random = new Random((long)this.ticks * 312987231L);
        ClientWorld worldView = this.client.world;
        BlockPos blockPos = new BlockPos(camera.getPos());
        Vec3i blockPos2 = null;
        int i = (int)(100.0f * f * f) / (this.client.options.particles == ParticlesMode.DECREASED ? 2 : 1);
        for (int j = 0; j < i; ++j) {
            int k = random.nextInt(21) - 10;
            int l = random.nextInt(21) - 10;
            BlockPos blockPos3 = worldView.getTopPosition(Heightmap.Type.MOTION_BLOCKING, blockPos.add(k, 0, l)).down();
            Biome biome = worldView.getBiome(blockPos3);
            if (blockPos3.getY() <= worldView.getBottomY() || blockPos3.getY() > blockPos.getY() + 10 || blockPos3.getY() < blockPos.getY() - 10 || biome.getPrecipitation() != Biome.Precipitation.RAIN || !(biome.getTemperature(blockPos3) >= 0.15f)) continue;
            blockPos2 = blockPos3;
            if (this.client.options.particles == ParticlesMode.MINIMAL) break;
            double d = random.nextDouble();
            double e = random.nextDouble();
            BlockState blockState = worldView.getBlockState((BlockPos)blockPos2);
            FluidState fluidState = worldView.getFluidState((BlockPos)blockPos2);
            VoxelShape voxelShape = blockState.getCollisionShape(worldView, (BlockPos)blockPos2);
            double g = voxelShape.getEndingCoord(Direction.Axis.Y, d, e);
            double h = fluidState.getHeight(worldView, (BlockPos)blockPos2);
            double m = Math.max(g, h);
            DefaultParticleType particleEffect = fluidState.isIn(FluidTags.LAVA) || blockState.isOf(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(blockState) ? ParticleTypes.SMOKE : ParticleTypes.RAIN;
            this.client.world.addParticle(particleEffect, (double)blockPos2.getX() + d, (double)blockPos2.getY() + m, (double)blockPos2.getZ() + e, 0.0, 0.0, 0.0);
        }
        if (blockPos2 != null && random.nextInt(3) < this.field_20793++) {
            this.field_20793 = 0;
            if (blockPos2.getY() > blockPos.getY() + 1 && worldView.getTopPosition(Heightmap.Type.MOTION_BLOCKING, blockPos).getY() > MathHelper.floor(blockPos.getY())) {
                this.client.world.playSound((BlockPos)blockPos2, SoundEvents.WEATHER_RAIN_ABOVE, SoundCategory.WEATHER, 0.1f, 0.5f, false);
            } else {
                this.client.world.playSound((BlockPos)blockPos2, SoundEvents.WEATHER_RAIN, SoundCategory.WEATHER, 0.2f, 1.0f, false);
            }
        }
    }

    @Override
    public void close() {
        if (this.entityOutlineShader != null) {
            this.entityOutlineShader.close();
        }
        if (this.transparencyShader != null) {
            this.transparencyShader.close();
        }
    }

    @Override
    public void reload(ResourceManager manager) {
        this.loadEntityOutlineShader();
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            this.loadTransparencyShader();
        }
    }

    public void loadEntityOutlineShader() {
        if (this.entityOutlineShader != null) {
            this.entityOutlineShader.close();
        }
        Identifier identifier = new Identifier("shaders/post/entity_outline.json");
        try {
            this.entityOutlineShader = new ShaderEffect(this.client.getTextureManager(), this.client.getResourceManager(), this.client.getFramebuffer(), identifier);
            this.entityOutlineShader.setupDimensions(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight());
            this.entityOutlinesFramebuffer = this.entityOutlineShader.getSecondaryTarget("final");
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to load shader: {}", (Object)identifier, (Object)iOException);
            this.entityOutlineShader = null;
            this.entityOutlinesFramebuffer = null;
        }
        catch (JsonSyntaxException iOException) {
            LOGGER.warn("Failed to parse shader: {}", (Object)identifier, (Object)iOException);
            this.entityOutlineShader = null;
            this.entityOutlinesFramebuffer = null;
        }
    }

    private void loadTransparencyShader() {
        this.resetTransparencyShader();
        Identifier identifier = new Identifier("shaders/post/transparency.json");
        try {
            ShaderEffect shaderEffect = new ShaderEffect(this.client.getTextureManager(), this.client.getResourceManager(), this.client.getFramebuffer(), identifier);
            shaderEffect.setupDimensions(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight());
            Framebuffer framebuffer = shaderEffect.getSecondaryTarget("translucent");
            Framebuffer framebuffer2 = shaderEffect.getSecondaryTarget("itemEntity");
            Framebuffer framebuffer3 = shaderEffect.getSecondaryTarget("particles");
            Framebuffer framebuffer4 = shaderEffect.getSecondaryTarget("weather");
            Framebuffer framebuffer5 = shaderEffect.getSecondaryTarget("clouds");
            this.transparencyShader = shaderEffect;
            this.translucentFramebuffer = framebuffer;
            this.entityFramebuffer = framebuffer2;
            this.particlesFramebuffer = framebuffer3;
            this.weatherFramebuffer = framebuffer4;
            this.cloudsFramebuffer = framebuffer5;
        }
        catch (Exception shaderEffect) {
            String framebuffer = shaderEffect instanceof JsonSyntaxException ? "parse" : "load";
            String framebuffer2 = "Failed to " + framebuffer + " shader: " + identifier;
            ShaderException framebuffer3 = new ShaderException(framebuffer2, shaderEffect);
            if (this.client.getResourcePackManager().getEnabledNames().size() > 1) {
                LiteralText framebuffer4;
                try {
                    framebuffer4 = new LiteralText(this.client.getResourceManager().getResource(identifier).getResourcePackName());
                }
                catch (IOException framebuffer5) {
                    framebuffer4 = null;
                }
                this.client.options.graphicsMode = GraphicsMode.FANCY;
                this.client.onResourceReloadFailure(framebuffer3, framebuffer4);
            }
            CrashReport framebuffer4 = this.client.addDetailsToCrashReport(new CrashReport(framebuffer2, framebuffer3));
            this.client.options.graphicsMode = GraphicsMode.FANCY;
            this.client.options.write();
            LOGGER.fatal(framebuffer2, (Throwable)framebuffer3);
            this.client.cleanUpAfterCrash();
            MinecraftClient.printCrashReport(framebuffer4);
        }
    }

    private void resetTransparencyShader() {
        if (this.transparencyShader != null) {
            this.transparencyShader.close();
            this.translucentFramebuffer.delete();
            this.entityFramebuffer.delete();
            this.particlesFramebuffer.delete();
            this.weatherFramebuffer.delete();
            this.cloudsFramebuffer.delete();
            this.transparencyShader = null;
            this.translucentFramebuffer = null;
            this.entityFramebuffer = null;
            this.particlesFramebuffer = null;
            this.weatherFramebuffer = null;
            this.cloudsFramebuffer = null;
        }
    }

    public void drawEntityOutlinesFramebuffer() {
        if (this.canDrawEntityOutlines()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
            this.entityOutlinesFramebuffer.draw(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight(), false);
            RenderSystem.disableBlend();
        }
    }

    protected boolean canDrawEntityOutlines() {
        return !this.client.gameRenderer.isRenderingPanorama() && this.entityOutlinesFramebuffer != null && this.entityOutlineShader != null && this.client.player != null;
    }

    private void renderDarkSky() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        if (this.darkSkyBuffer != null) {
            this.darkSkyBuffer.close();
        }
        this.darkSkyBuffer = new VertexBuffer();
        WorldRenderer.method_34550(bufferBuilder, -16.0f);
        this.darkSkyBuffer.upload(bufferBuilder);
    }

    private void renderLightSky() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        if (this.lightSkyBuffer != null) {
            this.lightSkyBuffer.close();
        }
        this.lightSkyBuffer = new VertexBuffer();
        WorldRenderer.method_34550(bufferBuilder, 16.0f);
        this.lightSkyBuffer.upload(bufferBuilder);
    }

    private static void method_34550(BufferBuilder bufferBuilder, float f) {
        float g = Math.signum(f) * 512.0f;
        float h = 512.0f;
        RenderSystem.setShader(GameRenderer::getPositionShader);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION);
        bufferBuilder.vertex(0.0, f, 0.0).next();
        for (int i = -180; i <= 180; i += 45) {
            bufferBuilder.vertex(g * MathHelper.cos((float)i * ((float)Math.PI / 180)), f, 512.0f * MathHelper.sin((float)i * ((float)Math.PI / 180))).next();
        }
        bufferBuilder.end();
    }

    private void renderStars() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        if (this.starsBuffer != null) {
            this.starsBuffer.close();
        }
        this.starsBuffer = new VertexBuffer();
        this.renderStars(bufferBuilder);
        bufferBuilder.end();
        this.starsBuffer.upload(bufferBuilder);
    }

    private void renderStars(BufferBuilder buffer) {
        Random random = new Random(10842L);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        for (int i = 0; i < 1500; ++i) {
            double d = random.nextFloat() * 2.0f - 1.0f;
            double e = random.nextFloat() * 2.0f - 1.0f;
            double f = random.nextFloat() * 2.0f - 1.0f;
            double g = 0.15f + random.nextFloat() * 0.1f;
            double h = d * d + e * e + f * f;
            if (!(h < 1.0) || !(h > 0.01)) continue;
            h = 1.0 / Math.sqrt(h);
            double j = (d *= h) * 100.0;
            double k = (e *= h) * 100.0;
            double l = (f *= h) * 100.0;
            double m = Math.atan2(d, f);
            double n = Math.sin(m);
            double o = Math.cos(m);
            double p = Math.atan2(Math.sqrt(d * d + f * f), e);
            double q = Math.sin(p);
            double r = Math.cos(p);
            double s = random.nextDouble() * Math.PI * 2.0;
            double t = Math.sin(s);
            double u = Math.cos(s);
            for (int v = 0; v < 4; ++v) {
                double ab;
                double w = 0.0;
                double x = (double)((v & 2) - 1) * g;
                double y = (double)((v + 1 & 2) - 1) * g;
                double z = 0.0;
                double aa = x * u - y * t;
                double ac = ab = y * u + x * t;
                double ad = aa * q + 0.0 * r;
                double ae = 0.0 * q - aa * r;
                double af = ae * n - ac * o;
                double ag = ad;
                double ah = ac * n + ae * o;
                buffer.vertex(j + af, k + ag, l + ah).next();
            }
        }
    }

    public void setWorld(@Nullable ClientWorld world) {
        this.lastCameraChunkUpdateX = Double.MIN_VALUE;
        this.lastCameraChunkUpdateY = Double.MIN_VALUE;
        this.lastCameraChunkUpdateZ = Double.MIN_VALUE;
        this.cameraChunkX = Integer.MIN_VALUE;
        this.cameraChunkY = Integer.MIN_VALUE;
        this.cameraChunkZ = Integer.MIN_VALUE;
        this.entityRenderDispatcher.setWorld(world);
        this.world = world;
        if (world != null) {
            this.visibleChunks.ensureCapacity(4356 * world.countVerticalSections());
            this.reload();
        } else {
            this.chunksToRebuild.clear();
            this.visibleChunks.clear();
            if (this.chunks != null) {
                this.chunks.clear();
                this.chunks = null;
            }
            if (this.chunkBuilder != null) {
                this.chunkBuilder.stop();
            }
            this.chunkBuilder = null;
            this.noCullingBlockEntities.clear();
        }
    }

    public void reloadTransparencyShader() {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            this.loadTransparencyShader();
        } else {
            this.resetTransparencyShader();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void reload() {
        Entity entity;
        if (this.world == null) {
            return;
        }
        this.reloadTransparencyShader();
        this.world.reloadColor();
        if (this.chunkBuilder == null) {
            this.chunkBuilder = new ChunkBuilder(this.world, this, Util.getMainWorkerExecutor(), this.client.is64Bit(), this.bufferBuilders.getBlockBufferBuilders());
        } else {
            this.chunkBuilder.setWorld(this.world);
        }
        this.needsTerrainUpdate = true;
        this.cloudsDirty = true;
        RenderLayers.setFancyGraphicsOrBetter(MinecraftClient.isFancyGraphicsOrBetter());
        this.viewDistance = this.client.options.viewDistance;
        if (this.chunks != null) {
            this.chunks.clear();
        }
        this.clearChunkRenderers();
        Set<BlockEntity> set = this.noCullingBlockEntities;
        synchronized (set) {
            this.noCullingBlockEntities.clear();
        }
        this.chunks = new BuiltChunkStorage(this.chunkBuilder, this.world, this.client.options.viewDistance, this);
        this.chunkInfos = new ChunkInfoList(this.chunks.chunks.length);
        if (this.world != null && (entity = this.client.getCameraEntity()) != null) {
            this.chunks.updateCameraPosition(entity.getX(), entity.getZ());
        }
    }

    protected void clearChunkRenderers() {
        this.chunksToRebuild.clear();
        this.chunkBuilder.reset();
    }

    public void onResized(int width, int height) {
        this.scheduleTerrainUpdate();
        if (this.entityOutlineShader != null) {
            this.entityOutlineShader.setupDimensions(width, height);
        }
        if (this.transparencyShader != null) {
            this.transparencyShader.setupDimensions(width, height);
        }
    }

    public String getChunksDebugString() {
        int i = this.chunks.chunks.length;
        int j = this.getCompletedChunkCount();
        return String.format("C: %d/%d %sD: %d, %s", j, i, this.client.chunkCullingEnabled ? "(s) " : "", this.viewDistance, this.chunkBuilder == null ? "null" : this.chunkBuilder.getDebugString());
    }

    public ChunkBuilder getChunkBuilder() {
        return this.chunkBuilder;
    }

    public double getChunkCount() {
        return this.chunks.chunks.length;
    }

    public double getViewDistance() {
        return this.viewDistance;
    }

    public int getCompletedChunkCount() {
        int i = 0;
        for (ChunkInfo chunkInfo : this.visibleChunks) {
            if (chunkInfo.chunk.getData().isEmpty()) continue;
            ++i;
        }
        return i;
    }

    public String getEntitiesDebugString() {
        return "E: " + this.regularEntityCount + "/" + this.world.getRegularEntityCount() + ", B: " + this.blockEntityCount;
    }

    private void setupTerrain(Camera camera, Frustum frustum, boolean hasForcedFrustum, int frame, boolean spectator) {
        Vec3d vec3d = camera.getPos();
        if (this.client.options.viewDistance != this.viewDistance) {
            this.reload();
        }
        this.world.getProfiler().push("camera");
        double d = this.client.player.getX();
        double e = this.client.player.getY();
        double f = this.client.player.getZ();
        double g = d - this.lastCameraChunkUpdateX;
        double h = e - this.lastCameraChunkUpdateY;
        double i = f - this.lastCameraChunkUpdateZ;
        int j = ChunkSectionPos.getSectionCoord(d);
        int k = ChunkSectionPos.getSectionCoord(e);
        int l = ChunkSectionPos.getSectionCoord(f);
        if (this.cameraChunkX != j || this.cameraChunkY != k || this.cameraChunkZ != l || g * g + h * h + i * i > 16.0) {
            this.lastCameraChunkUpdateX = d;
            this.lastCameraChunkUpdateY = e;
            this.lastCameraChunkUpdateZ = f;
            this.cameraChunkX = j;
            this.cameraChunkY = k;
            this.cameraChunkZ = l;
            this.chunks.updateCameraPosition(d, f);
        }
        this.chunkBuilder.setCameraPosition(vec3d);
        this.world.getProfiler().swap("cull");
        this.client.getProfiler().swap("culling");
        BlockPos blockPos = camera.getBlockPos();
        ChunkBuilder.BuiltChunk builtChunk = this.chunks.getRenderedChunk(blockPos);
        int m = 16;
        BlockPos blockPos2 = new BlockPos(MathHelper.floor(vec3d.x / 16.0) * 16, MathHelper.floor(vec3d.y / 16.0) * 16, MathHelper.floor(vec3d.z / 16.0) * 16);
        float n = camera.getPitch();
        float o = camera.getYaw();
        this.needsTerrainUpdate = this.needsTerrainUpdate || !this.chunksToRebuild.isEmpty() || vec3d.x != this.lastCameraX || vec3d.y != this.lastCameraY || vec3d.z != this.lastCameraZ || (double)n != this.lastCameraPitch || (double)o != this.lastCameraYaw;
        this.lastCameraX = vec3d.x;
        this.lastCameraY = vec3d.y;
        this.lastCameraZ = vec3d.z;
        this.lastCameraPitch = n;
        this.lastCameraYaw = o;
        this.client.getProfiler().swap("update");
        if (!hasForcedFrustum && this.needsTerrainUpdate) {
            this.needsTerrainUpdate = false;
            this.method_34808(frustum, frame, spectator, vec3d, blockPos, builtChunk, 16, blockPos2);
        }
        this.client.getProfiler().swap("rebuildNear");
        Set<ChunkBuilder.BuiltChunk> set = this.chunksToRebuild;
        this.chunksToRebuild = Sets.newLinkedHashSet();
        for (ChunkInfo chunkInfo : this.visibleChunks) {
            boolean bl;
            ChunkBuilder.BuiltChunk builtChunk2 = chunkInfo.chunk;
            if (!builtChunk2.needsRebuild() && !set.contains(builtChunk2)) continue;
            this.needsTerrainUpdate = true;
            BlockPos blockPos3 = builtChunk2.getOrigin().add(8, 8, 8);
            boolean bl2 = bl = blockPos3.getSquaredDistance(blockPos) < 768.0;
            if (builtChunk2.needsImportantRebuild() || bl) {
                this.client.getProfiler().push("build near");
                this.chunkBuilder.rebuild(builtChunk2);
                builtChunk2.cancelRebuild();
                this.client.getProfiler().pop();
                continue;
            }
            this.chunksToRebuild.add(builtChunk2);
        }
        this.chunksToRebuild.addAll(set);
        this.client.getProfiler().pop();
    }

    private void method_34808(Frustum frustum, int i, boolean bl, Vec3d vec3d, BlockPos blockPos, ChunkBuilder.BuiltChunk builtChunk, int j, BlockPos blockPos2) {
        int k;
        this.visibleChunks.clear();
        ArrayDeque<Object> queue = Queues.newArrayDeque();
        Entity.setRenderDistanceMultiplier(MathHelper.clamp((double)this.client.options.viewDistance / 8.0, 1.0, 2.5) * (double)this.client.options.entityDistanceScaling);
        boolean bl2 = this.client.chunkCullingEnabled;
        if (builtChunk == null) {
            k = blockPos.getY() > this.world.getBottomY() ? this.world.getTopY() - 8 : this.world.getBottomY() + 8;
            int l = MathHelper.floor(vec3d.x / (double)j) * j;
            int m = MathHelper.floor(vec3d.z / (double)j) * j;
            ArrayList<ChunkInfo> list = Lists.newArrayList();
            for (int n = -this.viewDistance; n <= this.viewDistance; ++n) {
                for (int o = -this.viewDistance; o <= this.viewDistance; ++o) {
                    ChunkBuilder.BuiltChunk builtChunk2 = this.chunks.getRenderedChunk(new BlockPos(l + ChunkSectionPos.getOffsetPos(n, 8), k, m + ChunkSectionPos.getOffsetPos(o, 8)));
                    if (builtChunk2 == null || !frustum.isVisible(builtChunk2.boundingBox)) continue;
                    builtChunk2.setRebuildFrame(i);
                    list.add(new ChunkInfo(builtChunk2, null, 0));
                }
            }
            list.sort(Comparator.comparingDouble(chunkInfo -> blockPos.getSquaredDistance(chunkInfo.chunk.getOrigin().add(8, 8, 8))));
            queue.addAll(list);
        } else {
            if (bl && this.world.getBlockState(blockPos).isOpaqueFullCube(this.world, blockPos)) {
                bl2 = false;
            }
            builtChunk.setRebuildFrame(i);
            queue.add(new ChunkInfo(builtChunk, null, 0));
        }
        this.client.getProfiler().push("iteration");
        k = this.client.options.viewDistance;
        this.chunkInfos.update();
        while (!queue.isEmpty()) {
            ChunkInfo l = (ChunkInfo)queue.poll();
            ChunkBuilder.BuiltChunk m = l.chunk;
            this.visibleChunks.add(l);
            for (Direction direction : DIRECTIONS) {
                Object chunkData;
                ChunkBuilder.BuiltChunk builtChunk3 = this.getAdjacentChunk(blockPos2, m, direction);
                if (bl2 && l.canCull(direction.getOpposite())) continue;
                if (bl2 && l.hasAnyDirection()) {
                    chunkData = m.getData();
                    boolean bl3 = false;
                    for (int p = 0; p < DIRECTIONS.length; ++p) {
                        if (!l.hasDirection(p) || !((ChunkBuilder.ChunkData)chunkData).isVisibleThrough(DIRECTIONS[p].getOpposite(), direction)) continue;
                        bl3 = true;
                        break;
                    }
                    if (!bl3) continue;
                }
                if (builtChunk3 == null || !builtChunk3.shouldBuild()) continue;
                if (!builtChunk3.setRebuildFrame(i)) {
                    chunkData = this.chunkInfos.getInfo(builtChunk3);
                    if (chunkData == null) continue;
                    ((ChunkInfo)chunkData).addDirection(direction);
                    continue;
                }
                if (!frustum.isVisible(builtChunk3.boundingBox)) continue;
                chunkData = new ChunkInfo(builtChunk3, direction, l.propagationLevel + 1);
                ((ChunkInfo)chunkData).updateCullingState(l.cullingState, direction);
                queue.add(chunkData);
                this.chunkInfos.setInfo(builtChunk3, (ChunkInfo)chunkData);
            }
        }
        this.client.getProfiler().pop();
    }

    @Nullable
    private ChunkBuilder.BuiltChunk getAdjacentChunk(BlockPos pos, ChunkBuilder.BuiltChunk chunk, Direction direction) {
        BlockPos blockPos = chunk.getNeighborPosition(direction);
        if (MathHelper.abs(pos.getX() - blockPos.getX()) > this.viewDistance * 16) {
            return null;
        }
        if (blockPos.getY() < this.world.getBottomY() || blockPos.getY() >= this.world.getTopY()) {
            return null;
        }
        if (MathHelper.abs(pos.getZ() - blockPos.getZ()) > this.viewDistance * 16) {
            return null;
        }
        return this.chunks.getRenderedChunk(blockPos);
    }

    private void captureFrustum(Matrix4f modelMatrix, Matrix4f matrix4f, double x, double y, double z, Frustum frustum) {
        this.capturedFrustum = frustum;
        Matrix4f matrix4f2 = matrix4f.copy();
        matrix4f2.multiply(modelMatrix);
        matrix4f2.invert();
        this.capturedFrustumPosition.x = x;
        this.capturedFrustumPosition.y = y;
        this.capturedFrustumPosition.z = z;
        this.capturedFrustumOrientation[0] = new Vector4f(-1.0f, -1.0f, -1.0f, 1.0f);
        this.capturedFrustumOrientation[1] = new Vector4f(1.0f, -1.0f, -1.0f, 1.0f);
        this.capturedFrustumOrientation[2] = new Vector4f(1.0f, 1.0f, -1.0f, 1.0f);
        this.capturedFrustumOrientation[3] = new Vector4f(-1.0f, 1.0f, -1.0f, 1.0f);
        this.capturedFrustumOrientation[4] = new Vector4f(-1.0f, -1.0f, 1.0f, 1.0f);
        this.capturedFrustumOrientation[5] = new Vector4f(1.0f, -1.0f, 1.0f, 1.0f);
        this.capturedFrustumOrientation[6] = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.capturedFrustumOrientation[7] = new Vector4f(-1.0f, 1.0f, 1.0f, 1.0f);
        for (int i = 0; i < 8; ++i) {
            this.capturedFrustumOrientation[i].transform(matrix4f2);
            this.capturedFrustumOrientation[i].normalizeProjectiveCoordinates();
        }
    }

    public void setupFrustum(MatrixStack matrices, Vec3d pos, Matrix4f projectionMatrix) {
        Matrix4f matrix4f = matrices.peek().getModel();
        double d = pos.getX();
        double e = pos.getY();
        double f = pos.getZ();
        this.frustum = new Frustum(matrix4f, projectionMatrix);
        this.frustum.setPosition(d, e, f);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * WARNING - void declaration
     */
    public void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f) {
        Object entry;
        int u;
        Object outlineVertexConsumerProvider;
        Frustum frustum;
        boolean bl;
        RenderSystem.setShaderGameTime(this.world.getTime(), tickDelta);
        this.blockEntityRenderDispatcher.configure(this.world, camera, this.client.crosshairTarget);
        this.entityRenderDispatcher.configure(this.world, camera, this.client.targetedEntity);
        Profiler profiler = this.world.getProfiler();
        profiler.swap("light_updates");
        this.client.world.getChunkManager().getLightingProvider().doLightUpdates(Integer.MAX_VALUE, true, true);
        Vec3d vec3d = camera.getPos();
        double d = vec3d.getX();
        double e = vec3d.getY();
        double f = vec3d.getZ();
        Matrix4f matrix4f2 = matrices.peek().getModel();
        profiler.swap("culling");
        boolean bl2 = bl = this.capturedFrustum != null;
        if (bl) {
            frustum = this.capturedFrustum;
            frustum.setPosition(this.capturedFrustumPosition.x, this.capturedFrustumPosition.y, this.capturedFrustumPosition.z);
        } else {
            frustum = this.frustum;
        }
        this.client.getProfiler().swap("captureFrustum");
        if (this.shouldCaptureFrustum) {
            this.captureFrustum(matrix4f2, matrix4f, vec3d.x, vec3d.y, vec3d.z, bl ? new Frustum(matrix4f2, matrix4f) : frustum);
            this.shouldCaptureFrustum = false;
        }
        profiler.swap("clear");
        BackgroundRenderer.render(camera, tickDelta, this.client.world, this.client.options.viewDistance, gameRenderer.getSkyDarkness(tickDelta));
        BackgroundRenderer.setFogBlack();
        RenderSystem.clear(16640, MinecraftClient.IS_SYSTEM_MAC);
        float g = gameRenderer.getViewDistance();
        boolean bl22 = this.client.world.getDimensionEffects().useThickFog(MathHelper.floor(d), MathHelper.floor(e)) || this.client.inGameHud.getBossBarHud().shouldThickenFog();
        profiler.swap("sky");
        RenderSystem.setShader(GameRenderer::getPositionShader);
        this.renderSky(matrices, matrix4f, tickDelta, () -> BackgroundRenderer.applyFog(camera, BackgroundRenderer.FogType.FOG_SKY, g, bl22));
        profiler.swap("fog");
        BackgroundRenderer.applyFog(camera, BackgroundRenderer.FogType.FOG_TERRAIN, Math.max(g - 16.0f, 32.0f), bl22);
        profiler.swap("terrain_setup");
        this.setupTerrain(camera, frustum, bl, this.frame++, this.client.player.isSpectator());
        profiler.swap("updatechunks");
        int i = 30;
        int j = this.client.options.maxFps;
        long l = 33333333L;
        long m = (double)j == Option.FRAMERATE_LIMIT.getMax() ? 0L : (long)(1000000000 / j);
        long n = Util.getMeasuringTimeNano() - limitTime;
        long o = this.chunkUpdateSmoother.getTargetUsedTime(n);
        long p = o * 3L / 2L;
        long q = MathHelper.clamp(p, m, 33333333L);
        this.updateChunks(limitTime + q);
        profiler.swap("terrain");
        this.renderLayer(RenderLayer.getSolid(), matrices, d, e, f, matrix4f);
        this.renderLayer(RenderLayer.getCutoutMipped(), matrices, d, e, f, matrix4f);
        this.renderLayer(RenderLayer.getCutout(), matrices, d, e, f, matrix4f);
        if (this.world.getDimensionEffects().isDarkened()) {
            DiffuseLighting.enableForLevel(matrices.peek().getModel());
        } else {
            DiffuseLighting.disableForLevel(matrices.peek().getModel());
        }
        profiler.swap("entities");
        this.regularEntityCount = 0;
        this.blockEntityCount = 0;
        if (this.entityFramebuffer != null) {
            this.entityFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
            this.entityFramebuffer.copyDepthFrom(this.client.getFramebuffer());
            this.client.getFramebuffer().beginWrite(false);
        }
        if (this.weatherFramebuffer != null) {
            this.weatherFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
        }
        if (this.canDrawEntityOutlines()) {
            this.entityOutlinesFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
            this.client.getFramebuffer().beginWrite(false);
        }
        boolean bl3 = false;
        VertexConsumerProvider.Immediate immediate = this.bufferBuilders.getEntityVertexConsumers();
        for (Entity entity : this.world.getEntities()) {
            void var41_39;
            if (!this.entityRenderDispatcher.shouldRender(entity, frustum, d, e, f) && !entity.hasPassengerDeep(this.client.player) || entity == camera.getFocusedEntity() && !camera.isThirdPerson() && (!(camera.getFocusedEntity() instanceof LivingEntity) || !((LivingEntity)camera.getFocusedEntity()).isSleeping()) || entity instanceof ClientPlayerEntity && camera.getFocusedEntity() != entity) continue;
            ++this.regularEntityCount;
            if (entity.age == 0) {
                entity.lastRenderX = entity.getX();
                entity.lastRenderY = entity.getY();
                entity.lastRenderZ = entity.getZ();
            }
            if (this.canDrawEntityOutlines() && this.client.hasOutline(entity)) {
                bl3 = true;
                outlineVertexConsumerProvider = this.bufferBuilders.getOutlineVertexConsumers();
                OutlineVertexConsumerProvider outlineVertexConsumerProvider2 = outlineVertexConsumerProvider;
                int k = entity.getTeamColorValue();
                int r = 255;
                int s = k >> 16 & 0xFF;
                int t = k >> 8 & 0xFF;
                u = k & 0xFF;
                ((OutlineVertexConsumerProvider)outlineVertexConsumerProvider).setColor(s, t, u, 255);
            } else {
                VertexConsumerProvider.Immediate immediate2 = immediate;
            }
            this.renderEntity(entity, d, e, f, tickDelta, matrices, (VertexConsumerProvider)var41_39);
        }
        immediate.drawCurrentLayer();
        this.checkEmpty(matrices);
        immediate.draw(RenderLayer.getEntitySolid(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
        immediate.draw(RenderLayer.getEntityCutout(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
        immediate.draw(RenderLayer.getEntityCutoutNoCull(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
        immediate.draw(RenderLayer.getEntitySmoothCutout(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
        profiler.swap("blockentities");
        for (ChunkInfo chunkInfo : this.visibleChunks) {
            List<BlockEntity> list = chunkInfo.chunk.getData().getBlockEntities();
            if (list.isEmpty()) continue;
            for (BlockEntity k : list) {
                BlockPos r = k.getPos();
                VertexConsumerProvider s = immediate;
                matrices.push();
                matrices.translate((double)r.getX() - d, (double)r.getY() - e, (double)r.getZ() - f);
                SortedSet t = (SortedSet)this.blockBreakingProgressions.get(r.asLong());
                if (t != null && !t.isEmpty() && (u = ((BlockBreakingInfo)t.last()).getStage()) >= 0) {
                    entry = matrices.peek();
                    OverlayVertexConsumer vertexConsumer = new OverlayVertexConsumer(this.bufferBuilders.getEffectVertexConsumers().getBuffer(ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.get(u)), ((MatrixStack.Entry)entry).getModel(), ((MatrixStack.Entry)entry).getNormal());
                    s = renderLayer -> {
                        VertexConsumer vertexConsumer2 = immediate.getBuffer(renderLayer);
                        if (renderLayer.hasCrumbling()) {
                            return VertexConsumers.union(vertexConsumer, vertexConsumer2);
                        }
                        return vertexConsumer2;
                    };
                }
                this.blockEntityRenderDispatcher.render(k, tickDelta, matrices, s);
                matrices.pop();
            }
        }
        Set<BlockEntity> set = this.noCullingBlockEntities;
        synchronized (set) {
            for (BlockEntity blockEntity : this.noCullingBlockEntities) {
                outlineVertexConsumerProvider = blockEntity.getPos();
                matrices.push();
                matrices.translate((double)((Vec3i)outlineVertexConsumerProvider).getX() - d, (double)((Vec3i)outlineVertexConsumerProvider).getY() - e, (double)((Vec3i)outlineVertexConsumerProvider).getZ() - f);
                this.blockEntityRenderDispatcher.render(blockEntity, tickDelta, matrices, immediate);
                matrices.pop();
            }
        }
        this.checkEmpty(matrices);
        immediate.draw(RenderLayer.getSolid());
        immediate.draw(RenderLayer.getEndPortal());
        immediate.draw(RenderLayer.getEndGateway());
        immediate.draw(TexturedRenderLayers.getEntitySolid());
        immediate.draw(TexturedRenderLayers.getEntityCutout());
        immediate.draw(TexturedRenderLayers.getBeds());
        immediate.draw(TexturedRenderLayers.getShulkerBoxes());
        immediate.draw(TexturedRenderLayers.getSign());
        immediate.draw(TexturedRenderLayers.getChest());
        this.bufferBuilders.getOutlineVertexConsumers().draw();
        if (bl3) {
            this.entityOutlineShader.render(tickDelta);
            this.client.getFramebuffer().beginWrite(false);
        }
        profiler.swap("destroyProgress");
        for (Long2ObjectMap.Entry entry2 : this.blockBreakingProgressions.long2ObjectEntrySet()) {
            double t;
            double r;
            BlockPos blockPos = BlockPos.fromLong(entry2.getLongKey());
            double outlineVertexConsumerProvider2 = (double)blockPos.getX() - d;
            if (outlineVertexConsumerProvider2 * outlineVertexConsumerProvider2 + (r = (double)blockPos.getY() - e) * r + (t = (double)blockPos.getZ() - f) * t > 1024.0 || (entry = (SortedSet)entry2.getValue()) == null || entry.isEmpty()) continue;
            int vertexConsumer = ((BlockBreakingInfo)entry.last()).getStage();
            matrices.push();
            matrices.translate((double)blockPos.getX() - d, (double)blockPos.getY() - e, (double)blockPos.getZ() - f);
            MatrixStack.Entry entry22 = matrices.peek();
            OverlayVertexConsumer vertexConsumer2 = new OverlayVertexConsumer(this.bufferBuilders.getEffectVertexConsumers().getBuffer(ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.get(vertexConsumer)), entry22.getModel(), entry22.getNormal());
            this.client.getBlockRenderManager().renderDamage(this.world.getBlockState(blockPos), blockPos, this.world, matrices, vertexConsumer2);
            matrices.pop();
        }
        this.checkEmpty(matrices);
        HitResult hitResult = this.client.crosshairTarget;
        if (renderBlockOutline && hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            profiler.swap("outline");
            BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
            BlockState blockState = this.world.getBlockState(blockPos);
            if (!blockState.isAir() && this.world.getWorldBorder().contains(blockPos)) {
                VertexConsumer outlineVertexConsumerProvider3 = immediate.getBuffer(RenderLayer.getLines());
                this.drawBlockOutline(matrices, outlineVertexConsumerProvider3, camera.getFocusedEntity(), d, e, f, blockPos, blockState);
            }
        }
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.method_34425(matrices.peek().getModel());
        RenderSystem.applyModelViewMatrix();
        this.client.debugRenderer.render(matrices, immediate, d, e, f);
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
        immediate.draw(TexturedRenderLayers.getEntityTranslucentCull());
        immediate.draw(TexturedRenderLayers.getBannerPatterns());
        immediate.draw(TexturedRenderLayers.getShieldPatterns());
        immediate.draw(RenderLayer.getArmorGlint());
        immediate.draw(RenderLayer.getArmorEntityGlint());
        immediate.draw(RenderLayer.getGlint());
        immediate.draw(RenderLayer.getDirectGlint());
        immediate.draw(RenderLayer.getGlintTranslucent());
        immediate.draw(RenderLayer.getEntityGlint());
        immediate.draw(RenderLayer.getDirectEntityGlint());
        immediate.draw(RenderLayer.getWaterMask());
        this.bufferBuilders.getEffectVertexConsumers().draw();
        if (this.transparencyShader != null) {
            immediate.draw(RenderLayer.getLines());
            immediate.draw();
            this.translucentFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
            this.translucentFramebuffer.copyDepthFrom(this.client.getFramebuffer());
            profiler.swap("translucent");
            this.renderLayer(RenderLayer.getTranslucent(), matrices, d, e, f, matrix4f);
            profiler.swap("string");
            this.renderLayer(RenderLayer.getTripwire(), matrices, d, e, f, matrix4f);
            this.particlesFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
            this.particlesFramebuffer.copyDepthFrom(this.client.getFramebuffer());
            RenderPhase.PARTICLES_TARGET.startDrawing();
            profiler.swap("particles");
            this.client.particleManager.renderParticles(matrices, immediate, lightmapTextureManager, camera, tickDelta);
            RenderPhase.PARTICLES_TARGET.endDrawing();
        } else {
            profiler.swap("translucent");
            if (this.translucentFramebuffer != null) {
                this.translucentFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
            }
            this.renderLayer(RenderLayer.getTranslucent(), matrices, d, e, f, matrix4f);
            immediate.draw(RenderLayer.getLines());
            immediate.draw();
            profiler.swap("string");
            this.renderLayer(RenderLayer.getTripwire(), matrices, d, e, f, matrix4f);
            profiler.swap("particles");
            this.client.particleManager.renderParticles(matrices, immediate, lightmapTextureManager, camera, tickDelta);
        }
        matrixStack.push();
        matrixStack.method_34425(matrices.peek().getModel());
        RenderSystem.applyModelViewMatrix();
        if (this.client.options.getCloudRenderMode() != CloudRenderMode.OFF) {
            if (this.transparencyShader != null) {
                this.cloudsFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
                RenderPhase.CLOUDS_TARGET.startDrawing();
                profiler.swap("clouds");
                this.renderClouds(matrices, matrix4f, tickDelta, d, e, f);
                RenderPhase.CLOUDS_TARGET.endDrawing();
            } else {
                profiler.swap("clouds");
                RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
                this.renderClouds(matrices, matrix4f, tickDelta, d, e, f);
            }
        }
        if (this.transparencyShader != null) {
            RenderPhase.WEATHER_TARGET.startDrawing();
            profiler.swap("weather");
            this.renderWeather(lightmapTextureManager, tickDelta, d, e, f);
            this.renderWorldBorder(camera);
            RenderPhase.WEATHER_TARGET.endDrawing();
            this.transparencyShader.render(tickDelta);
            this.client.getFramebuffer().beginWrite(false);
        } else {
            RenderSystem.depthMask(false);
            profiler.swap("weather");
            this.renderWeather(lightmapTextureManager, tickDelta, d, e, f);
            this.renderWorldBorder(camera);
            RenderSystem.depthMask(true);
        }
        this.renderChunkDebugInfo(camera);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
        BackgroundRenderer.method_23792();
    }

    private void checkEmpty(MatrixStack matrices) {
        if (!matrices.isEmpty()) {
            throw new IllegalStateException("Pose stack not empty");
        }
    }

    private void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        double d = MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX());
        double e = MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY());
        double f = MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ());
        float g = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw());
        this.entityRenderDispatcher.render(entity, d - cameraX, e - cameraY, f - cameraZ, g, tickDelta, matrices, vertexConsumers, this.entityRenderDispatcher.getLight(entity, tickDelta));
    }

    private void renderLayer(RenderLayer renderLayer, MatrixStack matrices, double d, double e, double f, Matrix4f matrix4f) {
        int k;
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        renderLayer.startDrawing();
        if (renderLayer == RenderLayer.getTranslucent()) {
            this.client.getProfiler().push("translucent_sort");
            double g = d - this.lastTranslucentSortX;
            double h = e - this.lastTranslucentSortY;
            double i = f - this.lastTranslucentSortZ;
            if (g * g + h * h + i * i > 1.0) {
                this.lastTranslucentSortX = d;
                this.lastTranslucentSortY = e;
                this.lastTranslucentSortZ = f;
                int j = 0;
                for (ChunkInfo chunkInfo : this.visibleChunks) {
                    if (j >= 15 || !chunkInfo.chunk.scheduleSort(renderLayer, this.chunkBuilder)) continue;
                    ++j;
                }
            }
            this.client.getProfiler().pop();
        }
        this.client.getProfiler().push("filterempty");
        this.client.getProfiler().swap(() -> "render_" + renderLayer);
        boolean g = renderLayer != RenderLayer.getTranslucent();
        ListIterator objectListIterator = this.visibleChunks.listIterator(g ? 0 : this.visibleChunks.size());
        VertexFormat h = renderLayer.getVertexFormat();
        Shader shader = RenderSystem.getShader();
        BufferRenderer.unbindAll();
        for (int i = 0; i < 12; ++i) {
            k = RenderSystem.getShaderTexture(i);
            shader.addSampler("Sampler" + i, k);
        }
        if (shader.modelViewMat != null) {
            shader.modelViewMat.set(matrices.peek().getModel());
        }
        if (shader.projectionMat != null) {
            shader.projectionMat.set(matrix4f);
        }
        if (shader.colorModulator != null) {
            shader.colorModulator.set(RenderSystem.getShaderColor());
        }
        if (shader.fogStart != null) {
            shader.fogStart.set(RenderSystem.getShaderFogStart());
        }
        if (shader.fogEnd != null) {
            shader.fogEnd.set(RenderSystem.getShaderFogEnd());
        }
        if (shader.fogColor != null) {
            shader.fogColor.set(RenderSystem.getShaderFogColor());
        }
        if (shader.textureMat != null) {
            shader.textureMat.set(RenderSystem.getTextureMatrix());
        }
        if (shader.gameTime != null) {
            shader.gameTime.set(RenderSystem.getShaderGameTime());
        }
        RenderSystem.setupShaderLights(shader);
        shader.bind();
        GlUniform i = shader.chunkOffset;
        k = 0;
        while (g ? objectListIterator.hasNext() : objectListIterator.hasPrevious()) {
            ChunkInfo j = g ? (ChunkInfo)objectListIterator.next() : (ChunkInfo)objectListIterator.previous();
            ChunkBuilder.BuiltChunk builtChunk = j.chunk;
            if (builtChunk.getData().isEmpty(renderLayer)) continue;
            VertexBuffer vertexBuffer = builtChunk.getBuffer(renderLayer);
            BlockPos blockPos = builtChunk.getOrigin();
            if (i != null) {
                i.set((float)((double)blockPos.getX() - d), (float)((double)blockPos.getY() - e), (float)((double)blockPos.getZ() - f));
                i.upload();
            }
            vertexBuffer.drawVertices();
            k = 1;
        }
        if (i != null) {
            i.set(Vec3f.ZERO);
        }
        shader.unbind();
        if (k != 0) {
            h.endDrawing();
        }
        VertexBuffer.unbind();
        VertexBuffer.unbindVertexArray();
        this.client.getProfiler().pop();
        renderLayer.endDrawing();
    }

    private void renderChunkDebugInfo(Camera camera) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        if (this.client.debugChunkInfo || this.client.debugChunkOcclusion) {
            double d = camera.getPos().getX();
            double e = camera.getPos().getY();
            double f = camera.getPos().getZ();
            RenderSystem.depthMask(true);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableTexture();
            for (ChunkInfo chunkInfo : this.visibleChunks) {
                int i;
                ChunkBuilder.BuiltChunk builtChunk = chunkInfo.chunk;
                BlockPos blockPos = builtChunk.getOrigin();
                MatrixStack matrixStack = RenderSystem.getModelViewStack();
                matrixStack.push();
                matrixStack.translate((double)blockPos.getX() - d, (double)blockPos.getY() - e, (double)blockPos.getZ() - f);
                RenderSystem.applyModelViewMatrix();
                if (this.client.debugChunkInfo) {
                    bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);
                    RenderSystem.lineWidth(10.0f);
                    i = chunkInfo.propagationLevel == 0 ? 0 : MathHelper.hsvToRgb((float)chunkInfo.propagationLevel / 50.0f, 0.9f, 0.9f);
                    int j = i >> 16 & 0xFF;
                    int k = i >> 8 & 0xFF;
                    int l = i & 0xFF;
                    for (int m = 0; m < DIRECTIONS.length; ++m) {
                        if (!chunkInfo.hasDirection(m)) continue;
                        Direction direction = DIRECTIONS[m];
                        bufferBuilder.vertex(8.0, 8.0, 8.0).color(j, k, l, 255).next();
                        bufferBuilder.vertex(8 - 16 * direction.getOffsetX(), 8 - 16 * direction.getOffsetY(), 8 - 16 * direction.getOffsetZ()).color(j, k, l, 255).next();
                    }
                    tessellator.draw();
                    RenderSystem.lineWidth(1.0f);
                }
                if (this.client.debugChunkOcclusion && !builtChunk.getData().isEmpty()) {
                    bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);
                    RenderSystem.lineWidth(10.0f);
                    i = 0;
                    for (Direction m : DIRECTIONS) {
                        for (Direction direction2 : DIRECTIONS) {
                            boolean bl = builtChunk.getData().isVisibleThrough(m, direction2);
                            if (bl) continue;
                            ++i;
                            bufferBuilder.vertex(8 + 8 * m.getOffsetX(), 8 + 8 * m.getOffsetY(), 8 + 8 * m.getOffsetZ()).color(1, 0, 0, 1).next();
                            bufferBuilder.vertex(8 + 8 * direction2.getOffsetX(), 8 + 8 * direction2.getOffsetY(), 8 + 8 * direction2.getOffsetZ()).color(1, 0, 0, 1).next();
                        }
                    }
                    tessellator.draw();
                    RenderSystem.lineWidth(1.0f);
                    if (i > 0) {
                        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                        float j = 0.5f;
                        float k = 0.2f;
                        bufferBuilder.vertex(0.5, 15.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(15.5, 15.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(15.5, 15.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(0.5, 15.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(0.5, 0.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(15.5, 0.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(15.5, 0.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(0.5, 0.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(0.5, 15.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(0.5, 15.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(0.5, 0.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(0.5, 0.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(15.5, 0.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(15.5, 0.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(15.5, 15.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(15.5, 15.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(0.5, 0.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(15.5, 0.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(15.5, 15.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(0.5, 15.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(0.5, 15.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(15.5, 15.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(15.5, 0.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        bufferBuilder.vertex(0.5, 0.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).next();
                        tessellator.draw();
                    }
                }
                matrixStack.pop();
                RenderSystem.applyModelViewMatrix();
            }
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableTexture();
        }
        if (this.capturedFrustum != null) {
            RenderSystem.disableCull();
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.lineWidth(10.0f);
            MatrixStack d = RenderSystem.getModelViewStack();
            d.push();
            d.translate((float)(this.capturedFrustumPosition.x - camera.getPos().x), (float)(this.capturedFrustumPosition.y - camera.getPos().y), (float)(this.capturedFrustumPosition.z - camera.getPos().z));
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthMask(true);
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            this.method_22985(bufferBuilder, 0, 1, 2, 3, 0, 1, 1);
            this.method_22985(bufferBuilder, 4, 5, 6, 7, 1, 0, 0);
            this.method_22985(bufferBuilder, 0, 1, 5, 4, 1, 1, 0);
            this.method_22985(bufferBuilder, 2, 3, 7, 6, 0, 0, 1);
            this.method_22985(bufferBuilder, 0, 4, 7, 3, 0, 1, 0);
            this.method_22985(bufferBuilder, 1, 5, 6, 2, 1, 0, 1);
            tessellator.draw();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getPositionShader);
            bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            this.method_22984(bufferBuilder, 0);
            this.method_22984(bufferBuilder, 1);
            this.method_22984(bufferBuilder, 1);
            this.method_22984(bufferBuilder, 2);
            this.method_22984(bufferBuilder, 2);
            this.method_22984(bufferBuilder, 3);
            this.method_22984(bufferBuilder, 3);
            this.method_22984(bufferBuilder, 0);
            this.method_22984(bufferBuilder, 4);
            this.method_22984(bufferBuilder, 5);
            this.method_22984(bufferBuilder, 5);
            this.method_22984(bufferBuilder, 6);
            this.method_22984(bufferBuilder, 6);
            this.method_22984(bufferBuilder, 7);
            this.method_22984(bufferBuilder, 7);
            this.method_22984(bufferBuilder, 4);
            this.method_22984(bufferBuilder, 0);
            this.method_22984(bufferBuilder, 4);
            this.method_22984(bufferBuilder, 1);
            this.method_22984(bufferBuilder, 5);
            this.method_22984(bufferBuilder, 2);
            this.method_22984(bufferBuilder, 6);
            this.method_22984(bufferBuilder, 3);
            this.method_22984(bufferBuilder, 7);
            tessellator.draw();
            d.pop();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableTexture();
            RenderSystem.lineWidth(1.0f);
        }
    }

    private void method_22984(VertexConsumer vertexConsumer, int i) {
        vertexConsumer.vertex(this.capturedFrustumOrientation[i].getX(), this.capturedFrustumOrientation[i].getY(), this.capturedFrustumOrientation[i].getZ()).next();
    }

    private void method_22985(VertexConsumer vertexConsumer, int i, int j, int k, int l, int m, int n, int o) {
        float f = 0.25f;
        vertexConsumer.vertex(this.capturedFrustumOrientation[i].getX(), this.capturedFrustumOrientation[i].getY(), this.capturedFrustumOrientation[i].getZ()).color((float)m, (float)n, (float)o, 0.25f).next();
        vertexConsumer.vertex(this.capturedFrustumOrientation[j].getX(), this.capturedFrustumOrientation[j].getY(), this.capturedFrustumOrientation[j].getZ()).color((float)m, (float)n, (float)o, 0.25f).next();
        vertexConsumer.vertex(this.capturedFrustumOrientation[k].getX(), this.capturedFrustumOrientation[k].getY(), this.capturedFrustumOrientation[k].getZ()).color((float)m, (float)n, (float)o, 0.25f).next();
        vertexConsumer.vertex(this.capturedFrustumOrientation[l].getX(), this.capturedFrustumOrientation[l].getY(), this.capturedFrustumOrientation[l].getZ()).color((float)m, (float)n, (float)o, 0.25f).next();
    }

    public void captureFrustum() {
        this.shouldCaptureFrustum = true;
    }

    public void killFrustum() {
        this.capturedFrustum = null;
    }

    public void tick() {
        ++this.ticks;
        if (this.ticks % 20 != 0) {
            return;
        }
        Iterator iterator = this.blockBreakingInfos.values().iterator();
        while (iterator.hasNext()) {
            BlockBreakingInfo blockBreakingInfo = (BlockBreakingInfo)iterator.next();
            int i = blockBreakingInfo.getLastUpdateTick();
            if (this.ticks - i <= 400) continue;
            iterator.remove();
            this.removeBlockBreakingInfo(blockBreakingInfo);
        }
    }

    private void removeBlockBreakingInfo(BlockBreakingInfo info) {
        long l = info.getPos().asLong();
        Set set = (Set)this.blockBreakingProgressions.get(l);
        set.remove(info);
        if (set.isEmpty()) {
            this.blockBreakingProgressions.remove(l);
        }
    }

    private void renderEndSky(MatrixStack matrices) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, END_SKY);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        for (int i = 0; i < 6; ++i) {
            matrices.push();
            if (i == 1) {
                matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90.0f));
            }
            if (i == 2) {
                matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-90.0f));
            }
            if (i == 3) {
                matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180.0f));
            }
            if (i == 4) {
                matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(90.0f));
            }
            if (i == 5) {
                matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(-90.0f));
            }
            Matrix4f matrix4f = matrices.peek().getModel();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(matrix4f, -100.0f, -100.0f, -100.0f).texture(0.0f, 0.0f).color(40, 40, 40, 255).next();
            bufferBuilder.vertex(matrix4f, -100.0f, -100.0f, 100.0f).texture(0.0f, 16.0f).color(40, 40, 40, 255).next();
            bufferBuilder.vertex(matrix4f, 100.0f, -100.0f, 100.0f).texture(16.0f, 16.0f).color(40, 40, 40, 255).next();
            bufferBuilder.vertex(matrix4f, 100.0f, -100.0f, -100.0f).texture(16.0f, 0.0f).color(40, 40, 40, 255).next();
            tessellator.draw();
            matrices.pop();
        }
        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void renderSky(MatrixStack matrices, Matrix4f matrix4f, float f, Runnable runnable) {
        float r;
        float q;
        float p;
        int n;
        float l;
        float j;
        runnable.run();
        if (this.client.world.getDimensionEffects().getSkyType() == DimensionEffects.SkyType.END) {
            this.renderEndSky(matrices);
            return;
        }
        if (this.client.world.getDimensionEffects().getSkyType() != DimensionEffects.SkyType.NORMAL) {
            return;
        }
        RenderSystem.disableTexture();
        Vec3d vec3d = this.world.method_23777(this.client.gameRenderer.getCamera().getPos(), f);
        float g = (float)vec3d.x;
        float h = (float)vec3d.y;
        float i = (float)vec3d.z;
        BackgroundRenderer.setFogBlack();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(g, h, i, 1.0f);
        Shader shader = RenderSystem.getShader();
        this.lightSkyBuffer.setShader(matrices.peek().getModel(), matrix4f, shader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        float[] fs = this.world.getDimensionEffects().getFogColorOverride(this.world.getSkyAngle(f), f);
        if (fs != null) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.disableTexture();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            matrices.push();
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90.0f));
            j = MathHelper.sin(this.world.getSkyAngleRadians(f)) < 0.0f ? 180.0f : 0.0f;
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(j));
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(90.0f));
            float k = fs[0];
            l = fs[1];
            float m = fs[2];
            Matrix4f matrix4f2 = matrices.peek().getModel();
            bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(matrix4f2, 0.0f, 100.0f, 0.0f).color(k, l, m, fs[3]).next();
            n = 16;
            for (int o = 0; o <= 16; ++o) {
                p = (float)o * ((float)Math.PI * 2) / 16.0f;
                q = MathHelper.sin(p);
                r = MathHelper.cos(p);
                bufferBuilder.vertex(matrix4f2, q * 120.0f, r * 120.0f, -r * 40.0f * fs[3]).color(fs[0], fs[1], fs[2], 0.0f).next();
            }
            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);
            matrices.pop();
        }
        RenderSystem.enableTexture();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        matrices.push();
        j = 1.0f - this.world.getRainGradient(f);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, j);
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-90.0f));
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(this.world.getSkyAngle(f) * 360.0f));
        Matrix4f k = matrices.peek().getModel();
        l = 30.0f;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SUN);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(k, -l, 100.0f, -l).texture(0.0f, 0.0f).next();
        bufferBuilder.vertex(k, l, 100.0f, -l).texture(1.0f, 0.0f).next();
        bufferBuilder.vertex(k, l, 100.0f, l).texture(1.0f, 1.0f).next();
        bufferBuilder.vertex(k, -l, 100.0f, l).texture(0.0f, 1.0f).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        l = 20.0f;
        RenderSystem.setShaderTexture(0, MOON_PHASES);
        int m = this.world.getMoonPhase();
        int matrix4f2 = m % 4;
        n = m / 4 % 2;
        float o = (float)(matrix4f2 + 0) / 4.0f;
        p = (float)(n + 0) / 2.0f;
        q = (float)(matrix4f2 + 1) / 4.0f;
        r = (float)(n + 1) / 2.0f;
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(k, -l, -100.0f, l).texture(q, r).next();
        bufferBuilder.vertex(k, l, -100.0f, l).texture(o, r).next();
        bufferBuilder.vertex(k, l, -100.0f, -l).texture(o, p).next();
        bufferBuilder.vertex(k, -l, -100.0f, -l).texture(q, p).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.disableTexture();
        float s = this.world.method_23787(f) * j;
        if (s > 0.0f) {
            RenderSystem.setShaderColor(s, s, s, s);
            BackgroundRenderer.method_23792();
            this.starsBuffer.setShader(matrices.peek().getModel(), matrix4f, GameRenderer.getPositionShader());
            runnable.run();
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        matrices.pop();
        RenderSystem.disableTexture();
        RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 1.0f);
        double j2 = this.client.player.getCameraPosVec((float)f).y - this.world.getLevelProperties().getSkyDarknessHeight(this.world);
        if (j2 < 0.0) {
            matrices.push();
            matrices.translate(0.0, 12.0, 0.0);
            this.darkSkyBuffer.setShader(matrices.peek().getModel(), matrix4f, shader);
            matrices.pop();
        }
        if (this.world.getDimensionEffects().isAlternateSkyColor()) {
            RenderSystem.setShaderColor(g * 0.2f + 0.04f, h * 0.2f + 0.04f, i * 0.6f + 0.1f, 1.0f);
        } else {
            RenderSystem.setShaderColor(g, h, i, 1.0f);
        }
        RenderSystem.enableTexture();
        RenderSystem.depthMask(true);
    }

    public void renderClouds(MatrixStack matrices, Matrix4f matrix4f, float f, double d, double e, double g) {
        float h = this.world.getDimensionEffects().getCloudsHeight();
        if (Float.isNaN(h)) {
            return;
        }
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(true);
        float i = 12.0f;
        float j = 4.0f;
        double k = 2.0E-4;
        double l = ((float)this.ticks + f) * 0.03f;
        double m = (d + l) / 12.0;
        double n = h - (float)e + 0.33f;
        double o = g / 12.0 + (double)0.33f;
        m -= (double)(MathHelper.floor(m / 2048.0) * 2048);
        o -= (double)(MathHelper.floor(o / 2048.0) * 2048);
        float p = (float)(m - (double)MathHelper.floor(m));
        float q = (float)(n / 4.0 - (double)MathHelper.floor(n / 4.0)) * 4.0f;
        float r = (float)(o - (double)MathHelper.floor(o));
        Vec3d vec3d = this.world.getCloudsColor(f);
        int s = (int)Math.floor(m);
        int t = (int)Math.floor(n / 4.0);
        int u = (int)Math.floor(o);
        if (s != this.lastCloudsBlockX || t != this.lastCloudsBlockY || u != this.lastCloudsBlockZ || this.client.options.getCloudRenderMode() != this.lastCloudsRenderMode || this.lastCloudsColor.squaredDistanceTo(vec3d) > 2.0E-4) {
            this.lastCloudsBlockX = s;
            this.lastCloudsBlockY = t;
            this.lastCloudsBlockZ = u;
            this.lastCloudsColor = vec3d;
            this.lastCloudsRenderMode = this.client.options.getCloudRenderMode();
            this.cloudsDirty = true;
        }
        if (this.cloudsDirty) {
            this.cloudsDirty = false;
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            if (this.cloudsBuffer != null) {
                this.cloudsBuffer.close();
            }
            this.cloudsBuffer = new VertexBuffer();
            this.renderClouds(bufferBuilder, m, n, o, vec3d);
            bufferBuilder.end();
            this.cloudsBuffer.upload(bufferBuilder);
        }
        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        RenderSystem.setShaderTexture(0, CLOUDS);
        BackgroundRenderer.setFogBlack();
        matrices.push();
        matrices.scale(12.0f, 1.0f, 12.0f);
        matrices.translate(-p, q, -r);
        if (this.cloudsBuffer != null) {
            int bufferBuilder;
            for (int v = bufferBuilder = this.lastCloudsRenderMode == CloudRenderMode.FANCY ? 0 : 1; v < 2; ++v) {
                if (v == 0) {
                    RenderSystem.colorMask(false, false, false, false);
                } else {
                    RenderSystem.colorMask(true, true, true, true);
                }
                Shader shader = RenderSystem.getShader();
                this.cloudsBuffer.setShader(matrices.peek().getModel(), matrix4f, shader);
            }
        }
        matrices.pop();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void renderClouds(BufferBuilder builder, double x, double y, double z, Vec3d color) {
        float f = 4.0f;
        float g = 0.00390625f;
        int i = 8;
        int j = 4;
        float h = 9.765625E-4f;
        float k = (float)MathHelper.floor(x) * 0.00390625f;
        float l = (float)MathHelper.floor(z) * 0.00390625f;
        float m = (float)color.x;
        float n = (float)color.y;
        float o = (float)color.z;
        float p = m * 0.9f;
        float q = n * 0.9f;
        float r = o * 0.9f;
        float s = m * 0.7f;
        float t = n * 0.7f;
        float u = o * 0.7f;
        float v = m * 0.8f;
        float w = n * 0.8f;
        float aa = o * 0.8f;
        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_NORMAL);
        float ab = (float)Math.floor(y / 4.0) * 4.0f;
        if (this.lastCloudsRenderMode == CloudRenderMode.FANCY) {
            for (int ac = -3; ac <= 4; ++ac) {
                for (int ad = -3; ad <= 4; ++ad) {
                    int ag;
                    float ae = ac * 8;
                    float af = ad * 8;
                    if (ab > -5.0f) {
                        builder.vertex(ae + 0.0f, ab + 0.0f, af + 8.0f).texture((ae + 0.0f) * 0.00390625f + k, (af + 8.0f) * 0.00390625f + l).color(s, t, u, 0.8f).normal(0.0f, -1.0f, 0.0f).next();
                        builder.vertex(ae + 8.0f, ab + 0.0f, af + 8.0f).texture((ae + 8.0f) * 0.00390625f + k, (af + 8.0f) * 0.00390625f + l).color(s, t, u, 0.8f).normal(0.0f, -1.0f, 0.0f).next();
                        builder.vertex(ae + 8.0f, ab + 0.0f, af + 0.0f).texture((ae + 8.0f) * 0.00390625f + k, (af + 0.0f) * 0.00390625f + l).color(s, t, u, 0.8f).normal(0.0f, -1.0f, 0.0f).next();
                        builder.vertex(ae + 0.0f, ab + 0.0f, af + 0.0f).texture((ae + 0.0f) * 0.00390625f + k, (af + 0.0f) * 0.00390625f + l).color(s, t, u, 0.8f).normal(0.0f, -1.0f, 0.0f).next();
                    }
                    if (ab <= 5.0f) {
                        builder.vertex(ae + 0.0f, ab + 4.0f - 9.765625E-4f, af + 8.0f).texture((ae + 0.0f) * 0.00390625f + k, (af + 8.0f) * 0.00390625f + l).color(m, n, o, 0.8f).normal(0.0f, 1.0f, 0.0f).next();
                        builder.vertex(ae + 8.0f, ab + 4.0f - 9.765625E-4f, af + 8.0f).texture((ae + 8.0f) * 0.00390625f + k, (af + 8.0f) * 0.00390625f + l).color(m, n, o, 0.8f).normal(0.0f, 1.0f, 0.0f).next();
                        builder.vertex(ae + 8.0f, ab + 4.0f - 9.765625E-4f, af + 0.0f).texture((ae + 8.0f) * 0.00390625f + k, (af + 0.0f) * 0.00390625f + l).color(m, n, o, 0.8f).normal(0.0f, 1.0f, 0.0f).next();
                        builder.vertex(ae + 0.0f, ab + 4.0f - 9.765625E-4f, af + 0.0f).texture((ae + 0.0f) * 0.00390625f + k, (af + 0.0f) * 0.00390625f + l).color(m, n, o, 0.8f).normal(0.0f, 1.0f, 0.0f).next();
                    }
                    if (ac > -1) {
                        for (ag = 0; ag < 8; ++ag) {
                            builder.vertex(ae + (float)ag + 0.0f, ab + 0.0f, af + 8.0f).texture((ae + (float)ag + 0.5f) * 0.00390625f + k, (af + 8.0f) * 0.00390625f + l).color(p, q, r, 0.8f).normal(-1.0f, 0.0f, 0.0f).next();
                            builder.vertex(ae + (float)ag + 0.0f, ab + 4.0f, af + 8.0f).texture((ae + (float)ag + 0.5f) * 0.00390625f + k, (af + 8.0f) * 0.00390625f + l).color(p, q, r, 0.8f).normal(-1.0f, 0.0f, 0.0f).next();
                            builder.vertex(ae + (float)ag + 0.0f, ab + 4.0f, af + 0.0f).texture((ae + (float)ag + 0.5f) * 0.00390625f + k, (af + 0.0f) * 0.00390625f + l).color(p, q, r, 0.8f).normal(-1.0f, 0.0f, 0.0f).next();
                            builder.vertex(ae + (float)ag + 0.0f, ab + 0.0f, af + 0.0f).texture((ae + (float)ag + 0.5f) * 0.00390625f + k, (af + 0.0f) * 0.00390625f + l).color(p, q, r, 0.8f).normal(-1.0f, 0.0f, 0.0f).next();
                        }
                    }
                    if (ac <= 1) {
                        for (ag = 0; ag < 8; ++ag) {
                            builder.vertex(ae + (float)ag + 1.0f - 9.765625E-4f, ab + 0.0f, af + 8.0f).texture((ae + (float)ag + 0.5f) * 0.00390625f + k, (af + 8.0f) * 0.00390625f + l).color(p, q, r, 0.8f).normal(1.0f, 0.0f, 0.0f).next();
                            builder.vertex(ae + (float)ag + 1.0f - 9.765625E-4f, ab + 4.0f, af + 8.0f).texture((ae + (float)ag + 0.5f) * 0.00390625f + k, (af + 8.0f) * 0.00390625f + l).color(p, q, r, 0.8f).normal(1.0f, 0.0f, 0.0f).next();
                            builder.vertex(ae + (float)ag + 1.0f - 9.765625E-4f, ab + 4.0f, af + 0.0f).texture((ae + (float)ag + 0.5f) * 0.00390625f + k, (af + 0.0f) * 0.00390625f + l).color(p, q, r, 0.8f).normal(1.0f, 0.0f, 0.0f).next();
                            builder.vertex(ae + (float)ag + 1.0f - 9.765625E-4f, ab + 0.0f, af + 0.0f).texture((ae + (float)ag + 0.5f) * 0.00390625f + k, (af + 0.0f) * 0.00390625f + l).color(p, q, r, 0.8f).normal(1.0f, 0.0f, 0.0f).next();
                        }
                    }
                    if (ad > -1) {
                        for (ag = 0; ag < 8; ++ag) {
                            builder.vertex(ae + 0.0f, ab + 4.0f, af + (float)ag + 0.0f).texture((ae + 0.0f) * 0.00390625f + k, (af + (float)ag + 0.5f) * 0.00390625f + l).color(v, w, aa, 0.8f).normal(0.0f, 0.0f, -1.0f).next();
                            builder.vertex(ae + 8.0f, ab + 4.0f, af + (float)ag + 0.0f).texture((ae + 8.0f) * 0.00390625f + k, (af + (float)ag + 0.5f) * 0.00390625f + l).color(v, w, aa, 0.8f).normal(0.0f, 0.0f, -1.0f).next();
                            builder.vertex(ae + 8.0f, ab + 0.0f, af + (float)ag + 0.0f).texture((ae + 8.0f) * 0.00390625f + k, (af + (float)ag + 0.5f) * 0.00390625f + l).color(v, w, aa, 0.8f).normal(0.0f, 0.0f, -1.0f).next();
                            builder.vertex(ae + 0.0f, ab + 0.0f, af + (float)ag + 0.0f).texture((ae + 0.0f) * 0.00390625f + k, (af + (float)ag + 0.5f) * 0.00390625f + l).color(v, w, aa, 0.8f).normal(0.0f, 0.0f, -1.0f).next();
                        }
                    }
                    if (ad > 1) continue;
                    for (ag = 0; ag < 8; ++ag) {
                        builder.vertex(ae + 0.0f, ab + 4.0f, af + (float)ag + 1.0f - 9.765625E-4f).texture((ae + 0.0f) * 0.00390625f + k, (af + (float)ag + 0.5f) * 0.00390625f + l).color(v, w, aa, 0.8f).normal(0.0f, 0.0f, 1.0f).next();
                        builder.vertex(ae + 8.0f, ab + 4.0f, af + (float)ag + 1.0f - 9.765625E-4f).texture((ae + 8.0f) * 0.00390625f + k, (af + (float)ag + 0.5f) * 0.00390625f + l).color(v, w, aa, 0.8f).normal(0.0f, 0.0f, 1.0f).next();
                        builder.vertex(ae + 8.0f, ab + 0.0f, af + (float)ag + 1.0f - 9.765625E-4f).texture((ae + 8.0f) * 0.00390625f + k, (af + (float)ag + 0.5f) * 0.00390625f + l).color(v, w, aa, 0.8f).normal(0.0f, 0.0f, 1.0f).next();
                        builder.vertex(ae + 0.0f, ab + 0.0f, af + (float)ag + 1.0f - 9.765625E-4f).texture((ae + 0.0f) * 0.00390625f + k, (af + (float)ag + 0.5f) * 0.00390625f + l).color(v, w, aa, 0.8f).normal(0.0f, 0.0f, 1.0f).next();
                    }
                }
            }
        } else {
            boolean ac = true;
            int ad = 32;
            for (int ae = -32; ae < 32; ae += 32) {
                for (int af = -32; af < 32; af += 32) {
                    builder.vertex(ae + 0, ab, af + 32).texture((float)(ae + 0) * 0.00390625f + k, (float)(af + 32) * 0.00390625f + l).color(m, n, o, 0.8f).normal(0.0f, -1.0f, 0.0f).next();
                    builder.vertex(ae + 32, ab, af + 32).texture((float)(ae + 32) * 0.00390625f + k, (float)(af + 32) * 0.00390625f + l).color(m, n, o, 0.8f).normal(0.0f, -1.0f, 0.0f).next();
                    builder.vertex(ae + 32, ab, af + 0).texture((float)(ae + 32) * 0.00390625f + k, (float)(af + 0) * 0.00390625f + l).color(m, n, o, 0.8f).normal(0.0f, -1.0f, 0.0f).next();
                    builder.vertex(ae + 0, ab, af + 0).texture((float)(ae + 0) * 0.00390625f + k, (float)(af + 0) * 0.00390625f + l).color(m, n, o, 0.8f).normal(0.0f, -1.0f, 0.0f).next();
                }
            }
        }
    }

    private void updateChunks(long limitTime) {
        this.needsTerrainUpdate |= this.chunkBuilder.upload();
        long l = Util.getMeasuringTimeNano();
        int i = 0;
        if (!this.chunksToRebuild.isEmpty()) {
            Iterator<ChunkBuilder.BuiltChunk> iterator = this.chunksToRebuild.iterator();
            while (iterator.hasNext()) {
                long n;
                long o;
                ChunkBuilder.BuiltChunk builtChunk = iterator.next();
                if (builtChunk.needsImportantRebuild()) {
                    this.chunkBuilder.rebuild(builtChunk);
                } else {
                    builtChunk.scheduleRebuild(this.chunkBuilder);
                }
                builtChunk.cancelRebuild();
                iterator.remove();
                long m = Util.getMeasuringTimeNano();
                long p = limitTime - m;
                if (p >= (o = (n = m - l) / (long)(++i))) continue;
                break;
            }
        }
    }

    private void renderWorldBorder(Camera camera) {
        float v;
        double u;
        double t;
        float s;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        WorldBorder worldBorder = this.world.getWorldBorder();
        double d = this.client.options.viewDistance * 16;
        if (camera.getPos().x < worldBorder.getBoundEast() - d && camera.getPos().x > worldBorder.getBoundWest() + d && camera.getPos().z < worldBorder.getBoundSouth() - d && camera.getPos().z > worldBorder.getBoundNorth() + d) {
            return;
        }
        double e = 1.0 - worldBorder.getDistanceInsideBorder(camera.getPos().x, camera.getPos().z) / d;
        e = Math.pow(e, 4.0);
        e = MathHelper.clamp(e, 0.0, 1.0);
        double f = camera.getPos().x;
        double g = camera.getPos().z;
        double h = this.client.gameRenderer.method_32796();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.setShaderTexture(0, FORCEFIELD);
        RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        RenderSystem.applyModelViewMatrix();
        int i = worldBorder.getStage().getColor();
        float j = (float)(i >> 16 & 0xFF) / 255.0f;
        float k = (float)(i >> 8 & 0xFF) / 255.0f;
        float l = (float)(i & 0xFF) / 255.0f;
        RenderSystem.setShaderColor(j, k, l, (float)e);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.polygonOffset(-3.0f, -3.0f);
        RenderSystem.enablePolygonOffset();
        RenderSystem.disableCull();
        float m = (float)(Util.getMeasuringTimeMs() % 3000L) / 3000.0f;
        float n = 0.0f;
        float o = 0.0f;
        float p = (float)(h - MathHelper.fractionalPart(camera.getPos().y));
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        double q = Math.max((double)MathHelper.floor(g - d), worldBorder.getBoundNorth());
        double r = Math.min((double)MathHelper.ceil(g + d), worldBorder.getBoundSouth());
        if (f > worldBorder.getBoundEast() - d) {
            s = 0.0f;
            t = q;
            while (t < r) {
                u = Math.min(1.0, r - t);
                v = (float)u * 0.5f;
                bufferBuilder.vertex(worldBorder.getBoundEast() - f, -h, t - g).texture(m - s, m + p).next();
                bufferBuilder.vertex(worldBorder.getBoundEast() - f, -h, t + u - g).texture(m - (v + s), m + p).next();
                bufferBuilder.vertex(worldBorder.getBoundEast() - f, h, t + u - g).texture(m - (v + s), m + 0.0f).next();
                bufferBuilder.vertex(worldBorder.getBoundEast() - f, h, t - g).texture(m - s, m + 0.0f).next();
                t += 1.0;
                s += 0.5f;
            }
        }
        if (f < worldBorder.getBoundWest() + d) {
            s = 0.0f;
            t = q;
            while (t < r) {
                u = Math.min(1.0, r - t);
                v = (float)u * 0.5f;
                bufferBuilder.vertex(worldBorder.getBoundWest() - f, -h, t - g).texture(m + s, m + p).next();
                bufferBuilder.vertex(worldBorder.getBoundWest() - f, -h, t + u - g).texture(m + v + s, m + p).next();
                bufferBuilder.vertex(worldBorder.getBoundWest() - f, h, t + u - g).texture(m + v + s, m + 0.0f).next();
                bufferBuilder.vertex(worldBorder.getBoundWest() - f, h, t - g).texture(m + s, m + 0.0f).next();
                t += 1.0;
                s += 0.5f;
            }
        }
        q = Math.max((double)MathHelper.floor(f - d), worldBorder.getBoundWest());
        r = Math.min((double)MathHelper.ceil(f + d), worldBorder.getBoundEast());
        if (g > worldBorder.getBoundSouth() - d) {
            s = 0.0f;
            t = q;
            while (t < r) {
                u = Math.min(1.0, r - t);
                v = (float)u * 0.5f;
                bufferBuilder.vertex(t - f, -h, worldBorder.getBoundSouth() - g).texture(m + s, m + p).next();
                bufferBuilder.vertex(t + u - f, -h, worldBorder.getBoundSouth() - g).texture(m + v + s, m + p).next();
                bufferBuilder.vertex(t + u - f, h, worldBorder.getBoundSouth() - g).texture(m + v + s, m + 0.0f).next();
                bufferBuilder.vertex(t - f, h, worldBorder.getBoundSouth() - g).texture(m + s, m + 0.0f).next();
                t += 1.0;
                s += 0.5f;
            }
        }
        if (g < worldBorder.getBoundNorth() + d) {
            s = 0.0f;
            t = q;
            while (t < r) {
                u = Math.min(1.0, r - t);
                v = (float)u * 0.5f;
                bufferBuilder.vertex(t - f, -h, worldBorder.getBoundNorth() - g).texture(m - s, m + p).next();
                bufferBuilder.vertex(t + u - f, -h, worldBorder.getBoundNorth() - g).texture(m - (v + s), m + p).next();
                bufferBuilder.vertex(t + u - f, h, worldBorder.getBoundNorth() - g).texture(m - (v + s), m + 0.0f).next();
                bufferBuilder.vertex(t - f, h, worldBorder.getBoundNorth() - g).texture(m - s, m + 0.0f).next();
                t += 1.0;
                s += 0.5f;
            }
        }
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.enableCull();
        RenderSystem.polygonOffset(0.0f, 0.0f);
        RenderSystem.disablePolygonOffset();
        RenderSystem.disableBlend();
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
    }

    private void drawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos blockPos, BlockState blockState) {
        WorldRenderer.drawShapeOutline(matrices, vertexConsumer, blockState.getOutlineShape(this.world, blockPos, ShapeContext.of(entity)), (double)blockPos.getX() - d, (double)blockPos.getY() - e, (double)blockPos.getZ() - f, 0.0f, 0.0f, 0.0f, 0.4f);
    }

    public static void method_22983(MatrixStack matrixStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
        List<Box> list = voxelShape.getBoundingBoxes();
        int k = MathHelper.ceil((double)list.size() / 3.0);
        for (int l = 0; l < list.size(); ++l) {
            Box box = list.get(l);
            float m = ((float)l % (float)k + 1.0f) / (float)k;
            float n = l / k;
            float o = m * (float)(n == 0.0f ? 1 : 0);
            float p = m * (float)(n == 1.0f ? 1 : 0);
            float q = m * (float)(n == 2.0f ? 1 : 0);
            WorldRenderer.drawShapeOutline(matrixStack, vertexConsumer, VoxelShapes.cuboid(box.offset(0.0, 0.0, 0.0)), d, e, f, o, p, q, 1.0f);
        }
    }

    private static void drawShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
        MatrixStack.Entry entry = matrices.peek();
        voxelShape.forEachEdge((k, l, m, n, o, p) -> {
            float q = (float)(n - k);
            float r = (float)(o - l);
            float s = (float)(p - m);
            float t = MathHelper.sqrt(q * q + r * r + s * s);
            vertexConsumer.vertex(entry.getModel(), (float)(k + d), (float)(l + e), (float)(m + f)).color(g, h, i, j).normal(entry.getNormal(), q /= t, r /= t, s /= t).next();
            vertexConsumer.vertex(entry.getModel(), (float)(n + d), (float)(o + e), (float)(p + f)).color(g, h, i, j).normal(entry.getNormal(), q, r, s).next();
        });
    }

    public static void method_35773(VertexConsumer vertexConsumer, double d, double e, double f, double g, double h, double i, float j, float k, float l, float m) {
        WorldRenderer.drawBox(new MatrixStack(), vertexConsumer, d, e, f, g, h, i, j, k, l, m, j, k, l);
    }

    /**
     * Draws a box.
     * 
     * <p>Note the coordinates the box spans are relative to current translation of the matrices.
     */
    public static void drawBox(MatrixStack matrices, VertexConsumer vertexConsumer, Box box, float red, float green, float blue, float alpha) {
        WorldRenderer.drawBox(matrices, vertexConsumer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha, red, green, blue);
    }

    /**
     * Draws a box spanning from [x1,y1,z1] to [x2,y2,z2].
     * 
     * <p>Note the coordinates the box spans are relative to current translation of the matrices.
     */
    public static void drawBox(MatrixStack matrices, VertexConsumer vertexConsumer, double x1, double y1, double z1, double x2, double y2, double z2, float red, float green, float blue, float alpha) {
        WorldRenderer.drawBox(matrices, vertexConsumer, x1, y1, z1, x2, y2, z2, red, green, blue, alpha, red, green, blue);
    }

    /**
     * Draws a box spanning from [x1,y1,z1] to [x2,y2,z2].
     * The 3 axes centered at [x1,y1,z1] may be colored differently using xAxisRed, yAxisGreen, and zAxisBlue.
     * 
     * <p>Note the coordinates the box spans are relative to current translation of the matrices.
     */
    public static void drawBox(MatrixStack matrices, VertexConsumer vertexConsumer, double x1, double y1, double z1, double x2, double y2, double z2, float red, float green, float blue, float alpha, float xAxisRed, float yAxisGreen, float zAxisBlue) {
        Matrix4f matrix4f = matrices.peek().getModel();
        Matrix3f matrix3f = matrices.peek().getNormal();
        float f = (float)x1;
        float g = (float)y1;
        float h = (float)z1;
        float i = (float)x2;
        float j = (float)y2;
        float k = (float)z2;
        vertexConsumer.vertex(matrix4f, f, g, h).color(red, yAxisGreen, zAxisBlue, alpha).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
        vertexConsumer.vertex(matrix4f, i, g, h).color(red, yAxisGreen, zAxisBlue, alpha).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
        vertexConsumer.vertex(matrix4f, f, g, h).color(xAxisRed, green, zAxisBlue, alpha).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
        vertexConsumer.vertex(matrix4f, f, j, h).color(xAxisRed, green, zAxisBlue, alpha).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
        vertexConsumer.vertex(matrix4f, f, g, h).color(xAxisRed, yAxisGreen, blue, alpha).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
        vertexConsumer.vertex(matrix4f, f, g, k).color(xAxisRed, yAxisGreen, blue, alpha).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
        vertexConsumer.vertex(matrix4f, i, g, h).color(red, green, blue, alpha).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
        vertexConsumer.vertex(matrix4f, i, j, h).color(red, green, blue, alpha).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
        vertexConsumer.vertex(matrix4f, i, j, h).color(red, green, blue, alpha).normal(matrix3f, -1.0f, 0.0f, 0.0f).next();
        vertexConsumer.vertex(matrix4f, f, j, h).color(red, green, blue, alpha).normal(matrix3f, -1.0f, 0.0f, 0.0f).next();
        vertexConsumer.vertex(matrix4f, f, j, h).color(red, green, blue, alpha).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
        vertexConsumer.vertex(matrix4f, f, j, k).color(red, green, blue, alpha).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
        vertexConsumer.vertex(matrix4f, f, j, k).color(red, green, blue, alpha).normal(matrix3f, 0.0f, -1.0f, 0.0f).next();
        vertexConsumer.vertex(matrix4f, f, g, k).color(red, green, blue, alpha).normal(matrix3f, 0.0f, -1.0f, 0.0f).next();
        vertexConsumer.vertex(matrix4f, f, g, k).color(red, green, blue, alpha).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
        vertexConsumer.vertex(matrix4f, i, g, k).color(red, green, blue, alpha).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
        vertexConsumer.vertex(matrix4f, i, g, k).color(red, green, blue, alpha).normal(matrix3f, 0.0f, 0.0f, -1.0f).next();
        vertexConsumer.vertex(matrix4f, i, g, h).color(red, green, blue, alpha).normal(matrix3f, 0.0f, 0.0f, -1.0f).next();
        vertexConsumer.vertex(matrix4f, f, j, k).color(red, green, blue, alpha).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
        vertexConsumer.vertex(matrix4f, i, j, k).color(red, green, blue, alpha).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
        vertexConsumer.vertex(matrix4f, i, g, k).color(red, green, blue, alpha).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
        vertexConsumer.vertex(matrix4f, i, j, k).color(red, green, blue, alpha).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
        vertexConsumer.vertex(matrix4f, i, j, h).color(red, green, blue, alpha).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
        vertexConsumer.vertex(matrix4f, i, j, k).color(red, green, blue, alpha).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
    }

    public static void drawBox(BufferBuilder buffer, double x1, double y1, double z1, double x2, double y2, double z2, float red, float green, float blue, float alpha) {
        buffer.vertex(x1, y1, z1).color(red, green, blue, alpha).next();
        buffer.vertex(x1, y1, z1).color(red, green, blue, alpha).next();
        buffer.vertex(x1, y1, z1).color(red, green, blue, alpha).next();
        buffer.vertex(x1, y1, z2).color(red, green, blue, alpha).next();
        buffer.vertex(x1, y2, z1).color(red, green, blue, alpha).next();
        buffer.vertex(x1, y2, z2).color(red, green, blue, alpha).next();
        buffer.vertex(x1, y2, z2).color(red, green, blue, alpha).next();
        buffer.vertex(x1, y1, z2).color(red, green, blue, alpha).next();
        buffer.vertex(x2, y2, z2).color(red, green, blue, alpha).next();
        buffer.vertex(x2, y1, z2).color(red, green, blue, alpha).next();
        buffer.vertex(x2, y1, z2).color(red, green, blue, alpha).next();
        buffer.vertex(x2, y1, z1).color(red, green, blue, alpha).next();
        buffer.vertex(x2, y2, z2).color(red, green, blue, alpha).next();
        buffer.vertex(x2, y2, z1).color(red, green, blue, alpha).next();
        buffer.vertex(x2, y2, z1).color(red, green, blue, alpha).next();
        buffer.vertex(x2, y1, z1).color(red, green, blue, alpha).next();
        buffer.vertex(x1, y2, z1).color(red, green, blue, alpha).next();
        buffer.vertex(x1, y1, z1).color(red, green, blue, alpha).next();
        buffer.vertex(x1, y1, z1).color(red, green, blue, alpha).next();
        buffer.vertex(x2, y1, z1).color(red, green, blue, alpha).next();
        buffer.vertex(x1, y1, z2).color(red, green, blue, alpha).next();
        buffer.vertex(x2, y1, z2).color(red, green, blue, alpha).next();
        buffer.vertex(x2, y1, z2).color(red, green, blue, alpha).next();
        buffer.vertex(x1, y2, z1).color(red, green, blue, alpha).next();
        buffer.vertex(x1, y2, z1).color(red, green, blue, alpha).next();
        buffer.vertex(x1, y2, z2).color(red, green, blue, alpha).next();
        buffer.vertex(x2, y2, z1).color(red, green, blue, alpha).next();
        buffer.vertex(x2, y2, z2).color(red, green, blue, alpha).next();
        buffer.vertex(x2, y2, z2).color(red, green, blue, alpha).next();
        buffer.vertex(x2, y2, z2).color(red, green, blue, alpha).next();
    }

    public void updateBlock(BlockView world, BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        this.scheduleSectionRender(pos, (flags & 8) != 0);
    }

    private void scheduleSectionRender(BlockPos pos, boolean important) {
        for (int i = pos.getZ() - 1; i <= pos.getZ() + 1; ++i) {
            for (int j = pos.getX() - 1; j <= pos.getX() + 1; ++j) {
                for (int k = pos.getY() - 1; k <= pos.getY() + 1; ++k) {
                    this.scheduleChunkRender(ChunkSectionPos.getSectionCoord(j), ChunkSectionPos.getSectionCoord(k), ChunkSectionPos.getSectionCoord(i), important);
                }
            }
        }
    }

    public void scheduleBlockRenders(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        for (int i = minZ - 1; i <= maxZ + 1; ++i) {
            for (int j = minX - 1; j <= maxX + 1; ++j) {
                for (int k = minY - 1; k <= maxY + 1; ++k) {
                    this.scheduleBlockRender(ChunkSectionPos.getSectionCoord(j), ChunkSectionPos.getSectionCoord(k), ChunkSectionPos.getSectionCoord(i));
                }
            }
        }
    }

    public void scheduleBlockRerenderIfNeeded(BlockPos pos, BlockState old, BlockState updated) {
        if (this.client.getBakedModelManager().shouldRerender(old, updated)) {
            this.scheduleBlockRenders(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
        }
    }

    public void scheduleBlockRenders(int x, int y, int z) {
        for (int i = z - 1; i <= z + 1; ++i) {
            for (int j = x - 1; j <= x + 1; ++j) {
                for (int k = y - 1; k <= y + 1; ++k) {
                    this.scheduleBlockRender(j, k, i);
                }
            }
        }
    }

    public void scheduleBlockRender(int x, int y, int z) {
        this.scheduleChunkRender(x, y, z, false);
    }

    private void scheduleChunkRender(int x, int y, int z, boolean important) {
        this.chunks.scheduleRebuild(x, y, z, important);
    }

    public void playSong(@Nullable SoundEvent song, BlockPos songPosition) {
        SoundInstance soundInstance = this.playingSongs.get(songPosition);
        if (soundInstance != null) {
            this.client.getSoundManager().stop(soundInstance);
            this.playingSongs.remove(songPosition);
        }
        if (song != null) {
            MusicDiscItem musicDiscItem = MusicDiscItem.bySound(song);
            if (musicDiscItem != null) {
                this.client.inGameHud.setRecordPlayingOverlay(musicDiscItem.getDescription());
            }
            soundInstance = PositionedSoundInstance.record(song, songPosition.getX(), songPosition.getY(), songPosition.getZ());
            this.playingSongs.put(songPosition, soundInstance);
            this.client.getSoundManager().play(soundInstance);
        }
        this.updateEntitiesForSong(this.world, songPosition, song != null);
    }

    private void updateEntitiesForSong(World world, BlockPos pos, boolean playing) {
        List<LivingEntity> list = world.getNonSpectatingEntities(LivingEntity.class, new Box(pos).expand(3.0));
        for (LivingEntity livingEntity : list) {
            livingEntity.setNearbySongPlaying(pos, playing);
        }
    }

    public void addParticle(ParticleEffect parameters, boolean shouldAlwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        this.addParticle(parameters, shouldAlwaysSpawn, false, x, y, z, velocityX, velocityY, velocityZ);
    }

    public void addParticle(ParticleEffect parameters, boolean shouldAlwaysSpawn, boolean important, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        try {
            this.spawnParticle(parameters, shouldAlwaysSpawn, important, x, y, z, velocityX, velocityY, velocityZ);
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.create(throwable, "Exception while adding particle");
            CrashReportSection crashReportSection = crashReport.addElement("Particle being added");
            crashReportSection.add("ID", Registry.PARTICLE_TYPE.getId(parameters.getType()));
            crashReportSection.add("Parameters", parameters.asString());
            crashReportSection.add("Position", () -> CrashReportSection.createPositionString((HeightLimitView)this.world, x, y, z));
            throw new CrashException(crashReport);
        }
    }

    private <T extends ParticleEffect> void addParticle(T parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        this.addParticle(parameters, parameters.getType().shouldAlwaysSpawn(), x, y, z, velocityX, velocityY, velocityZ);
    }

    @Nullable
    private Particle spawnParticle(ParticleEffect parameters, boolean alwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        return this.spawnParticle(parameters, alwaysSpawn, false, x, y, z, velocityX, velocityY, velocityZ);
    }

    @Nullable
    private Particle spawnParticle(ParticleEffect parameters, boolean alwaysSpawn, boolean canSpawnOnMinimal, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        Camera camera = this.client.gameRenderer.getCamera();
        if (this.client == null || !camera.isReady() || this.client.particleManager == null) {
            return null;
        }
        ParticlesMode particlesMode = this.getRandomParticleSpawnChance(canSpawnOnMinimal);
        if (alwaysSpawn) {
            return this.client.particleManager.addParticle(parameters, x, y, z, velocityX, velocityY, velocityZ);
        }
        if (camera.getPos().squaredDistanceTo(x, y, z) > 1024.0) {
            return null;
        }
        if (particlesMode == ParticlesMode.MINIMAL) {
            return null;
        }
        return this.client.particleManager.addParticle(parameters, x, y, z, velocityX, velocityY, velocityZ);
    }

    private ParticlesMode getRandomParticleSpawnChance(boolean canSpawnOnMinimal) {
        ParticlesMode particlesMode = this.client.options.particles;
        if (canSpawnOnMinimal && particlesMode == ParticlesMode.MINIMAL && this.world.random.nextInt(10) == 0) {
            particlesMode = ParticlesMode.DECREASED;
        }
        if (particlesMode == ParticlesMode.DECREASED && this.world.random.nextInt(3) == 0) {
            particlesMode = ParticlesMode.MINIMAL;
        }
        return particlesMode;
    }

    public void method_3267() {
    }

    public void processGlobalEvent(int eventId, BlockPos pos, int i) {
        switch (eventId) {
            case 1023: 
            case 1028: 
            case 1038: {
                Camera camera = this.client.gameRenderer.getCamera();
                if (!camera.isReady()) break;
                double d = (double)pos.getX() - camera.getPos().x;
                double e = (double)pos.getY() - camera.getPos().y;
                double f = (double)pos.getZ() - camera.getPos().z;
                double g = Math.sqrt(d * d + e * e + f * f);
                double h = camera.getPos().x;
                double j = camera.getPos().y;
                double k = camera.getPos().z;
                if (g > 0.0) {
                    h += d / g * 2.0;
                    j += e / g * 2.0;
                    k += f / g * 2.0;
                }
                if (eventId == WorldEvents.WITHER_SPAWNS) {
                    this.world.playSound(h, j, k, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.0f, 1.0f, false);
                    break;
                }
                if (eventId == WorldEvents.END_PORTAL_OPENED) {
                    this.world.playSound(h, j, k, SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.HOSTILE, 1.0f, 1.0f, false);
                    break;
                }
                this.world.playSound(h, j, k, SoundEvents.ENTITY_ENDER_DRAGON_DEATH, SoundCategory.HOSTILE, 5.0f, 1.0f, false);
            }
        }
    }

    public void processWorldEvent(PlayerEntity source, int eventId, BlockPos pos, int data) {
        Random random = this.world.random;
        switch (eventId) {
            case 1035: {
                this.world.playSound(pos, SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 1033: {
                this.world.playSound(pos, SoundEvents.BLOCK_CHORUS_FLOWER_GROW, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 1034: {
                this.world.playSound(pos, SoundEvents.BLOCK_CHORUS_FLOWER_DEATH, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 1032: {
                this.client.getSoundManager().play(PositionedSoundInstance.ambient(SoundEvents.BLOCK_PORTAL_TRAVEL, random.nextFloat() * 0.4f + 0.8f, 0.25f));
                break;
            }
            case 1001: {
                this.world.playSound(pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0f, 1.2f, false);
                break;
            }
            case 1000: {
                this.world.playSound(pos, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 1003: {
                this.world.playSound(pos, SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.NEUTRAL, 1.0f, 1.2f, false);
                break;
            }
            case 1004: {
                this.world.playSound(pos, SoundEvents.ENTITY_FIREWORK_ROCKET_SHOOT, SoundCategory.NEUTRAL, 1.0f, 1.2f, false);
                break;
            }
            case 1002: {
                this.world.playSound(pos, SoundEvents.BLOCK_DISPENSER_LAUNCH, SoundCategory.BLOCKS, 1.0f, 1.2f, false);
                break;
            }
            case 2000: {
                Direction direction = Direction.byId(data);
                int i = direction.getOffsetX();
                int j = direction.getOffsetY();
                int k = direction.getOffsetZ();
                double d = (double)pos.getX() + (double)i * 0.6 + 0.5;
                double e = (double)pos.getY() + (double)j * 0.6 + 0.5;
                double f = (double)pos.getZ() + (double)k * 0.6 + 0.5;
                for (int l = 0; l < 10; ++l) {
                    double g = random.nextDouble() * 0.2 + 0.01;
                    double h = d + (double)i * 0.01 + (random.nextDouble() - 0.5) * (double)k * 0.5;
                    double m = e + (double)j * 0.01 + (random.nextDouble() - 0.5) * (double)j * 0.5;
                    double n = f + (double)k * 0.01 + (random.nextDouble() - 0.5) * (double)i * 0.5;
                    double o = (double)i * g + random.nextGaussian() * 0.01;
                    double p = (double)j * g + random.nextGaussian() * 0.01;
                    double q = (double)k * g + random.nextGaussian() * 0.01;
                    this.addParticle(ParticleTypes.SMOKE, h, m, n, o, p, q);
                }
                break;
            }
            case 2003: {
                double direction = (double)pos.getX() + 0.5;
                double j = pos.getY();
                double d = (double)pos.getZ() + 0.5;
                for (int e = 0; e < 8; ++e) {
                    this.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.ENDER_EYE)), direction, j, d, random.nextGaussian() * 0.15, random.nextDouble() * 0.2, random.nextGaussian() * 0.15);
                }
                for (double e = 0.0; e < Math.PI * 2; e += 0.15707963267948966) {
                    this.addParticle(ParticleTypes.PORTAL, direction + Math.cos(e) * 5.0, j - 0.4, d + Math.sin(e) * 5.0, Math.cos(e) * -5.0, 0.0, Math.sin(e) * -5.0);
                    this.addParticle(ParticleTypes.PORTAL, direction + Math.cos(e) * 5.0, j - 0.4, d + Math.sin(e) * 5.0, Math.cos(e) * -7.0, 0.0, Math.sin(e) * -7.0);
                }
                break;
            }
            case 2002: 
            case 2007: {
                Vec3d direction = Vec3d.ofBottomCenter(pos);
                for (int i = 0; i < 8; ++i) {
                    this.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.SPLASH_POTION)), direction.x, direction.y, direction.z, random.nextGaussian() * 0.15, random.nextDouble() * 0.2, random.nextGaussian() * 0.15);
                }
                float i = (float)(data >> 16 & 0xFF) / 255.0f;
                float j = (float)(data >> 8 & 0xFF) / 255.0f;
                float k = (float)(data >> 0 & 0xFF) / 255.0f;
                DefaultParticleType d = eventId == WorldEvents.INSTANT_SPLASH_POTION_SPLASHED ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.EFFECT;
                for (int r = 0; r < 100; ++r) {
                    double e = random.nextDouble() * 4.0;
                    double f = random.nextDouble() * Math.PI * 2.0;
                    double l = Math.cos(f) * e;
                    double s = 0.01 + random.nextDouble() * 0.5;
                    double t = Math.sin(f) * e;
                    Particle particle = this.spawnParticle(d, d.getType().shouldAlwaysSpawn(), direction.x + l * 0.1, direction.y + 0.3, direction.z + t * 0.1, l, s, t);
                    if (particle == null) continue;
                    float n = 0.75f + random.nextFloat() * 0.25f;
                    particle.setColor(i * n, j * n, k * n);
                    particle.move((float)e);
                }
                this.world.playSound(pos, SoundEvents.ENTITY_SPLASH_POTION_BREAK, SoundCategory.NEUTRAL, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 2001: {
                BlockState direction = Block.getStateFromRawId(data);
                if (!direction.isAir()) {
                    BlockSoundGroup i = direction.getSoundGroup();
                    this.world.playSound(pos, i.getBreakSound(), SoundCategory.BLOCKS, (i.getVolume() + 1.0f) / 2.0f, i.getPitch() * 0.8f, false);
                }
                this.world.addBlockBreakParticles(pos, direction);
                break;
            }
            case 2004: {
                for (int i = 0; i < 20; ++i) {
                    double j = (double)pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
                    double d = (double)pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
                    double e = (double)pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
                    this.world.addParticle(ParticleTypes.SMOKE, j, d, e, 0.0, 0.0, 0.0);
                    this.world.addParticle(ParticleTypes.FLAME, j, d, e, 0.0, 0.0, 0.0);
                }
                break;
            }
            case 2005: {
                BoneMealItem.createParticles(this.world, pos, data);
                break;
            }
            case 1505: {
                BoneMealItem.createParticles(this.world, pos, data);
                this.world.playSound(pos, SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 3002: {
                if (data >= 0 && data < Direction.Axis.VALUES.length) {
                    ParticleUtil.spawnParticle(Direction.Axis.VALUES[data], this.world, pos, 0.125, ParticleTypes.ELECTRIC_SPARK, UniformIntProvider.create(10, 19));
                    break;
                }
                ParticleUtil.spawnParticle((World)this.world, pos, ParticleTypes.ELECTRIC_SPARK, UniformIntProvider.create(3, 5));
                break;
            }
            case 3003: {
                ParticleUtil.spawnParticle((World)this.world, pos, ParticleTypes.WAX_ON, UniformIntProvider.create(3, 5));
                this.world.playSound(pos, SoundEvents.ITEM_HONEYCOMB_WAX_ON, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 3004: {
                ParticleUtil.spawnParticle((World)this.world, pos, ParticleTypes.WAX_OFF, UniformIntProvider.create(3, 5));
                break;
            }
            case 3005: {
                ParticleUtil.spawnParticle((World)this.world, pos, ParticleTypes.SCRAPE, UniformIntProvider.create(3, 5));
                break;
            }
            case 2008: {
                this.world.addParticle(ParticleTypes.EXPLOSION, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 0.0, 0.0, 0.0);
                break;
            }
            case 1500: {
                ComposterBlock.playEffects(this.world, pos, data > 0);
                break;
            }
            case 1504: {
                PointedDripstoneBlock.createParticle(this.world, pos, this.world.getBlockState(pos));
                break;
            }
            case 1501: {
                this.world.playSound(pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 2.6f + (random.nextFloat() - random.nextFloat()) * 0.8f, false);
                for (int i = 0; i < 8; ++i) {
                    this.world.addParticle(ParticleTypes.LARGE_SMOKE, (double)pos.getX() + random.nextDouble(), (double)pos.getY() + 1.2, (double)pos.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
                }
                break;
            }
            case 1502: {
                this.world.playSound(pos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.5f, 2.6f + (random.nextFloat() - random.nextFloat()) * 0.8f, false);
                for (int i = 0; i < 5; ++i) {
                    double j = (double)pos.getX() + random.nextDouble() * 0.6 + 0.2;
                    double d = (double)pos.getY() + random.nextDouble() * 0.6 + 0.2;
                    double e = (double)pos.getZ() + random.nextDouble() * 0.6 + 0.2;
                    this.world.addParticle(ParticleTypes.SMOKE, j, d, e, 0.0, 0.0, 0.0);
                }
                break;
            }
            case 1503: {
                this.world.playSound(pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
                for (int i = 0; i < 16; ++i) {
                    double j = (double)pos.getX() + (5.0 + random.nextDouble() * 6.0) / 16.0;
                    double d = (double)pos.getY() + 0.8125;
                    double e = (double)pos.getZ() + (5.0 + random.nextDouble() * 6.0) / 16.0;
                    this.world.addParticle(ParticleTypes.SMOKE, j, d, e, 0.0, 0.0, 0.0);
                }
                break;
            }
            case 2006: {
                for (int i = 0; i < 200; ++i) {
                    float j = random.nextFloat() * 4.0f;
                    float k = random.nextFloat() * ((float)Math.PI * 2);
                    double d = MathHelper.cos(k) * j;
                    double e = 0.01 + random.nextDouble() * 0.5;
                    double f = MathHelper.sin(k) * j;
                    Particle l = this.spawnParticle(ParticleTypes.DRAGON_BREATH, false, (double)pos.getX() + d * 0.1, (double)pos.getY() + 0.3, (double)pos.getZ() + f * 0.1, d, e, f);
                    if (l == null) continue;
                    l.move(j);
                }
                if (data != 1) break;
                this.world.playSound(pos, SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.HOSTILE, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 2009: {
                for (int i = 0; i < 8; ++i) {
                    this.world.addParticle(ParticleTypes.CLOUD, (double)pos.getX() + random.nextDouble(), (double)pos.getY() + 1.2, (double)pos.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
                }
                break;
            }
            case 1012: {
                this.world.playSound(pos, SoundEvents.BLOCK_WOODEN_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1036: {
                this.world.playSound(pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1013: {
                this.world.playSound(pos, SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1014: {
                this.world.playSound(pos, SoundEvents.BLOCK_FENCE_GATE_CLOSE, SoundCategory.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1011: {
                this.world.playSound(pos, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1006: {
                this.world.playSound(pos, SoundEvents.BLOCK_WOODEN_DOOR_OPEN, SoundCategory.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1007: {
                this.world.playSound(pos, SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1037: {
                this.world.playSound(pos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1008: {
                this.world.playSound(pos, SoundEvents.BLOCK_FENCE_GATE_OPEN, SoundCategory.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1005: {
                this.world.playSound(pos, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1009: {
                if (data == 0) {
                    this.world.playSound(pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 2.6f + (random.nextFloat() - random.nextFloat()) * 0.8f, false);
                    break;
                }
                if (data != 1) break;
                this.world.playSound(pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 0.7f, 1.6f + (random.nextFloat() - random.nextFloat()) * 0.4f, false);
                break;
            }
            case 1029: {
                this.world.playSound(pos, SoundEvents.BLOCK_ANVIL_DESTROY, SoundCategory.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1030: {
                this.world.playSound(pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1044: {
                this.world.playSound(pos, SoundEvents.BLOCK_SMITHING_TABLE_USE, SoundCategory.BLOCKS, 1.0f, this.world.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1031: {
                this.world.playSound(pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 0.3f, this.world.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1039: {
                this.world.playSound(pos, SoundEvents.ENTITY_PHANTOM_BITE, SoundCategory.HOSTILE, 0.3f, this.world.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1010: {
                if (Item.byRawId(data) instanceof MusicDiscItem) {
                    this.playSong(((MusicDiscItem)Item.byRawId(data)).getSound(), pos);
                    break;
                }
                this.playSong(null, pos);
                break;
            }
            case 1015: {
                this.world.playSound(pos, SoundEvents.ENTITY_GHAST_WARN, SoundCategory.HOSTILE, 10.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1017: {
                this.world.playSound(pos, SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, SoundCategory.HOSTILE, 10.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1016: {
                this.world.playSound(pos, SoundEvents.ENTITY_GHAST_SHOOT, SoundCategory.HOSTILE, 10.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1019: {
                this.world.playSound(pos, SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.HOSTILE, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1022: {
                this.world.playSound(pos, SoundEvents.ENTITY_WITHER_BREAK_BLOCK, SoundCategory.HOSTILE, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1021: {
                this.world.playSound(pos, SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, SoundCategory.HOSTILE, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1020: {
                this.world.playSound(pos, SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.HOSTILE, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1018: {
                this.world.playSound(pos, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1024: {
                this.world.playSound(pos, SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.HOSTILE, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1026: {
                this.world.playSound(pos, SoundEvents.ENTITY_ZOMBIE_INFECT, SoundCategory.HOSTILE, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1027: {
                this.world.playSound(pos, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, SoundCategory.NEUTRAL, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1040: {
                this.world.playSound(pos, SoundEvents.ENTITY_ZOMBIE_CONVERTED_TO_DROWNED, SoundCategory.NEUTRAL, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1041: {
                this.world.playSound(pos, SoundEvents.ENTITY_HUSK_CONVERTED_TO_ZOMBIE, SoundCategory.NEUTRAL, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1025: {
                this.world.playSound(pos, SoundEvents.ENTITY_BAT_TAKEOFF, SoundCategory.NEUTRAL, 0.05f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1042: {
                this.world.playSound(pos, SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 1.0f, this.world.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1043: {
                this.world.playSound(pos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1.0f, this.world.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 3000: {
                this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, true, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 0.0, 0.0, 0.0);
                this.world.playSound(pos, SoundEvents.BLOCK_END_GATEWAY_SPAWN, SoundCategory.BLOCKS, 10.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f, false);
                break;
            }
            case 3001: {
                this.world.playSound(pos, SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 64.0f, 0.8f + this.world.random.nextFloat() * 0.3f, false);
                break;
            }
            case 1045: {
                this.world.playSound(pos, SoundEvents.BLOCK_POINTED_DRIPSTONE_LAND, SoundCategory.BLOCKS, 2.0f, this.world.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1046: {
                this.world.playSound(pos, SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON, SoundCategory.BLOCKS, 2.0f, this.world.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1047: {
                this.world.playSound(pos, SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundCategory.BLOCKS, 2.0f, this.world.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1048: {
                this.world.playSound(pos, SoundEvents.ENTITY_SKELETON_CONVERTED_TO_STRAY, SoundCategory.NEUTRAL, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
            }
        }
    }

    public void setBlockBreakingInfo(int entityId, BlockPos pos, int stage) {
        if (stage < 0 || stage >= 10) {
            BlockBreakingInfo blockBreakingInfo = (BlockBreakingInfo)this.blockBreakingInfos.remove(entityId);
            if (blockBreakingInfo != null) {
                this.removeBlockBreakingInfo(blockBreakingInfo);
            }
        } else {
            BlockBreakingInfo blockBreakingInfo = (BlockBreakingInfo)this.blockBreakingInfos.get(entityId);
            if (blockBreakingInfo != null) {
                this.removeBlockBreakingInfo(blockBreakingInfo);
            }
            if (blockBreakingInfo == null || blockBreakingInfo.getPos().getX() != pos.getX() || blockBreakingInfo.getPos().getY() != pos.getY() || blockBreakingInfo.getPos().getZ() != pos.getZ()) {
                blockBreakingInfo = new BlockBreakingInfo(entityId, pos);
                this.blockBreakingInfos.put(entityId, blockBreakingInfo);
            }
            blockBreakingInfo.setStage(stage);
            blockBreakingInfo.setLastUpdateTick(this.ticks);
            this.blockBreakingProgressions.computeIfAbsent(blockBreakingInfo.getPos().asLong(), l -> Sets.newTreeSet()).add(blockBreakingInfo);
        }
    }

    public boolean isTerrainRenderComplete() {
        return this.chunksToRebuild.isEmpty() && this.chunkBuilder.isEmpty();
    }

    public void scheduleTerrainUpdate() {
        this.needsTerrainUpdate = true;
        this.cloudsDirty = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void updateNoCullingBlockEntities(Collection<BlockEntity> removed, Collection<BlockEntity> added) {
        Set<BlockEntity> set = this.noCullingBlockEntities;
        synchronized (set) {
            this.noCullingBlockEntities.removeAll(removed);
            this.noCullingBlockEntities.addAll(added);
        }
    }

    public static int getLightmapCoordinates(BlockRenderView world, BlockPos pos) {
        return WorldRenderer.getLightmapCoordinates(world, world.getBlockState(pos), pos);
    }

    public static int getLightmapCoordinates(BlockRenderView world, BlockState state, BlockPos pos) {
        int k;
        if (state.hasEmissiveLighting(world, pos)) {
            return 0xF000F0;
        }
        int i = world.getLightLevel(LightType.SKY, pos);
        int j = world.getLightLevel(LightType.BLOCK, pos);
        if (j < (k = state.getLuminance())) {
            j = k;
        }
        return i << 20 | j << 4;
    }

    @Nullable
    public Framebuffer getEntityOutlinesFramebuffer() {
        return this.entityOutlinesFramebuffer;
    }

    @Nullable
    public Framebuffer getTranslucentFramebuffer() {
        return this.translucentFramebuffer;
    }

    @Nullable
    public Framebuffer getEntityFramebuffer() {
        return this.entityFramebuffer;
    }

    @Nullable
    public Framebuffer getParticlesFramebuffer() {
        return this.particlesFramebuffer;
    }

    @Nullable
    public Framebuffer getWeatherFramebuffer() {
        return this.weatherFramebuffer;
    }

    @Nullable
    public Framebuffer getCloudsFramebuffer() {
        return this.cloudsFramebuffer;
    }

    @Environment(value=EnvType.CLIENT)
    public static class ShaderException
    extends RuntimeException {
        public ShaderException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ChunkInfoList {
        private final ChunkInfo[] current;
        private final ChunkInfo[] pending;

        ChunkInfoList(int i) {
            this.current = new ChunkInfo[i];
            this.pending = new ChunkInfo[i];
        }

        void update() {
            System.arraycopy(this.pending, 0, this.current, 0, this.current.length);
        }

        public void setInfo(ChunkBuilder.BuiltChunk chunk, ChunkInfo info) {
            this.current[chunk.index] = info;
        }

        public ChunkInfo getInfo(ChunkBuilder.BuiltChunk chunk) {
            return this.current[chunk.index];
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ChunkInfo {
        final ChunkBuilder.BuiltChunk chunk;
        private byte direction;
        byte cullingState;
        final int propagationLevel;

        ChunkInfo(ChunkBuilder.BuiltChunk chunk, @Nullable Direction direction, int propagationLevel) {
            this.chunk = chunk;
            if (direction != null) {
                this.addDirection(direction);
            }
            this.propagationLevel = propagationLevel;
        }

        public void updateCullingState(byte parentCullingState, Direction from) {
            this.cullingState = (byte)(this.cullingState | (parentCullingState | 1 << from.ordinal()));
        }

        public boolean canCull(Direction from) {
            return (this.cullingState & 1 << from.ordinal()) > 0;
        }

        public void addDirection(Direction direction) {
            this.direction = (byte)(this.direction | (this.direction | 1 << direction.ordinal()));
        }

        public boolean hasDirection(int ordinal) {
            return (this.direction & 1 << ordinal) > 0;
        }

        public boolean hasAnyDirection() {
            return this.direction != 0;
        }
    }
}

