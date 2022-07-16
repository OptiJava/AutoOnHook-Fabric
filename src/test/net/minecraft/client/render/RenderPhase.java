/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import org.apache.commons.lang3.tuple.Triple;

@Environment(value=EnvType.CLIENT)
public abstract class RenderPhase {
    private static final float VIEW_OFFSET_Z_LAYERING_SCALE = 0.99975586f;
    protected final String name;
    private final Runnable beginAction;
    private final Runnable endAction;
    protected static final Transparency NO_TRANSPARENCY = new Transparency("no_transparency", () -> RenderSystem.disableBlend(), () -> {});
    protected static final Transparency ADDITIVE_TRANSPARENCY = new Transparency("additive_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    protected static final Transparency LIGHTNING_TRANSPARENCY = new Transparency("lightning_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    protected static final Transparency GLINT_TRANSPARENCY = new Transparency("glint_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_COLOR, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    protected static final Transparency CRUMBLING_TRANSPARENCY = new Transparency("crumbling_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.DST_COLOR, GlStateManager.DstFactor.SRC_COLOR, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    protected static final Transparency TRANSLUCENT_TRANSPARENCY = new Transparency("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    protected static final Shader NO_SHADER = new Shader();
    protected static final Shader BLOCK_SHADER = new Shader(GameRenderer::getBlockShader);
    protected static final Shader NEW_ENTITY_SHADER = new Shader(GameRenderer::getNewEntityShader);
    protected static final Shader POSITION_COLOR_LIGHTMAP_SHADER = new Shader(GameRenderer::getPositionColorLightmapShader);
    protected static final Shader POSITION_SHADER = new Shader(GameRenderer::getPositionShader);
    protected static final Shader POSITION_COLOR_TEXTURE_SHADER = new Shader(GameRenderer::getPositionColorTexShader);
    protected static final Shader POSITION_TEXTURE_SHADER = new Shader(GameRenderer::getPositionTexShader);
    protected static final Shader POSITION_COLOR_TEXTURE_LIGHTMAP_SHADER = new Shader(GameRenderer::getPositionColorTexLightmapShader);
    protected static final Shader COLOR_SHADER = new Shader(GameRenderer::getPositionColorShader);
    protected static final Shader SOLID_SHADER = new Shader(GameRenderer::getRenderTypeSolidShader);
    protected static final Shader CUTOUT_MIPPED_SHADER = new Shader(GameRenderer::getRenderTypeCutoutMippedShader);
    protected static final Shader CUTOUT_SHADER = new Shader(GameRenderer::getRenderTypeCutoutShader);
    protected static final Shader TRANSLUCENT_SHADER = new Shader(GameRenderer::getRenderTypeTranslucentShader);
    protected static final Shader TRANSLUCENT_MOVING_BLOCK_SHADER = new Shader(GameRenderer::getRenderTypeTranslucentMovingBlockShader);
    protected static final Shader TRANSLUCENT_NO_CRUMBLING_SHADER = new Shader(GameRenderer::getRenderTypeTranslucentNoCrumblingShader);
    protected static final Shader ARMOR_CUTOUT_NO_CULL_SHADER = new Shader(GameRenderer::getRenderTypeArmorCutoutNoCullShader);
    protected static final Shader ENTITY_SOLID_SHADER = new Shader(GameRenderer::getRenderTypeEntitySolidShader);
    protected static final Shader ENTITY_CUTOUT_SHADER = new Shader(GameRenderer::getRenderTypeEntityCutoutShader);
    protected static final Shader ENTITY_CUTOUT_NONULL_SHADER = new Shader(GameRenderer::getRenderTypeEntityCutoutNoNullShader);
    protected static final Shader ENTITY_CUTOUT_NONULL_OFFSET_Z_SHADER = new Shader(GameRenderer::getRenderTypeEntityCutoutNoNullZOffsetShader);
    protected static final Shader ITEM_ENTITY_TRANSLUCENT_CULL_SHADER = new Shader(GameRenderer::getRenderTypeItemEntityTranslucentCullShader);
    protected static final Shader ENTITY_TRANSLUCENT_CULL_SHADER = new Shader(GameRenderer::getRenderTypeEntityTranslucentCullShader);
    protected static final Shader ENTITY_TRANSLUCENT_SHADER = new Shader(GameRenderer::getRenderTypeEntityTranslucentShader);
    protected static final Shader ENTITY_SMOOTH_CUTOUT_SHADER = new Shader(GameRenderer::getRenderTypeEntitySmoothCutoutShader);
    protected static final Shader BEACON_BEAM_SHADER = new Shader(GameRenderer::getRenderTypeBeaconBeamShader);
    protected static final Shader ENTITY_DECAL_SHADER = new Shader(GameRenderer::getRenderTypeEntityDecalShader);
    protected static final Shader ENTITY_NO_OUTLINE_SHADER = new Shader(GameRenderer::getRenderTypeEntityNoOutlineShader);
    protected static final Shader ENTITY_SHADOW_SHADER = new Shader(GameRenderer::getRenderTypeEntityShadowShader);
    protected static final Shader ENTITY_ALPHA_SHADER = new Shader(GameRenderer::getRenderTypeEntityAlphaShader);
    protected static final Shader EYES_SHADER = new Shader(GameRenderer::getRenderTypeEyesShader);
    protected static final Shader ENERGY_SWIRL_SHADER = new Shader(GameRenderer::getRenderTypeEnergySwirlShader);
    protected static final Shader LEASH_SHADER = new Shader(GameRenderer::getRenderTypeLeashShader);
    protected static final Shader WATER_MASK_SHADER = new Shader(GameRenderer::getRenderTypeWaterMaskShader);
    protected static final Shader OUTLINE_SHADER = new Shader(GameRenderer::getRenderTypeOutlineShader);
    protected static final Shader ARMOR_GLINT_SHADER = new Shader(GameRenderer::getRenderTypeArmorGlintShader);
    protected static final Shader ARMOR_ENTITY_GLINT_SHADER = new Shader(GameRenderer::getRenderTypeArmorEntityGlintShader);
    protected static final Shader TRANSLUCENT_GLINT_SHADER = new Shader(GameRenderer::getRenderTypeGlintTranslucentShader);
    protected static final Shader GLINT_SHADER = new Shader(GameRenderer::getRenderTypeGlintShader);
    protected static final Shader DIRECT_GLINT_SHADER = new Shader(GameRenderer::getRenderTypeGlintDirectShader);
    protected static final Shader ENTITY_GLINT_SHADER = new Shader(GameRenderer::getRenderTypeEntityGlintShader);
    protected static final Shader DIRECT_ENTITY_GLINT_SHADER = new Shader(GameRenderer::getRenderTypeEntityGlintDirectShader);
    protected static final Shader CRUMBLING_SHADER = new Shader(GameRenderer::getRenderTypeCrumblingShader);
    protected static final Shader TEXT_SHADER = new Shader(GameRenderer::getRenderTypeTextShader);
    protected static final Shader TEXT_INTENSITY_SHADER = new Shader(GameRenderer::getRenderTypeTextIntensityShader);
    protected static final Shader TRANSPARENT_TEXT_SHADER = new Shader(GameRenderer::getRenderTypeTextSeeThroughShader);
    protected static final Shader TRANSPARENT_TEXT_INTENSITY_SHADER = new Shader(GameRenderer::getRenderTypeTextIntensitySeeThroughShader);
    protected static final Shader LIGHTNING_SHADER = new Shader(GameRenderer::getRenderTypeLightningShader);
    protected static final Shader TRIPWIRE_SHADER = new Shader(GameRenderer::getRenderTypeTripwireShader);
    protected static final Shader END_PORTAL_SHADER = new Shader(GameRenderer::getRenderTypeEndPortalShader);
    protected static final Shader END_GATEWAY_SHADER = new Shader(GameRenderer::getRenderTypeEndGatewayShader);
    protected static final Shader LINES_SHADER = new Shader(GameRenderer::getRenderTypeLinesShader);
    protected static final Texture MIPMAP_BLOCK_ATLAS_TEXTURE = new Texture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, false, true);
    protected static final Texture BLOCK_ATLAS_TEXTURE = new Texture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, false, false);
    protected static final TextureBase NO_TEXTURE = new TextureBase();
    protected static final Texturing DEFAULT_TEXTURING = new Texturing("default_texturing", () -> {}, () -> {});
    protected static final Texturing GLINT_TEXTURING = new Texturing("glint_texturing", () -> RenderPhase.setupGlintTexturing(8.0f), () -> RenderSystem.resetTextureMatrix());
    protected static final Texturing ENTITY_GLINT_TEXTURING = new Texturing("entity_glint_texturing", () -> RenderPhase.setupGlintTexturing(0.16f), () -> RenderSystem.resetTextureMatrix());
    protected static final Lightmap ENABLE_LIGHTMAP = new Lightmap(true);
    protected static final Lightmap DISABLE_LIGHTMAP = new Lightmap(false);
    protected static final Overlay ENABLE_OVERLAY_COLOR = new Overlay(true);
    protected static final Overlay DISABLE_OVERLAY_COLOR = new Overlay(false);
    protected static final Cull ENABLE_CULLING = new Cull(true);
    protected static final Cull DISABLE_CULLING = new Cull(false);
    protected static final DepthTest ALWAYS_DEPTH_TEST = new DepthTest("always", 519);
    protected static final DepthTest EQUAL_DEPTH_TEST = new DepthTest("==", 514);
    protected static final DepthTest LEQUAL_DEPTH_TEST = new DepthTest("<=", 515);
    protected static final WriteMaskState ALL_MASK = new WriteMaskState(true, true);
    protected static final WriteMaskState COLOR_MASK = new WriteMaskState(true, false);
    protected static final WriteMaskState DEPTH_MASK = new WriteMaskState(false, true);
    protected static final Layering NO_LAYERING = new Layering("no_layering", () -> {}, () -> {});
    protected static final Layering POLYGON_OFFSET_LAYERING = new Layering("polygon_offset_layering", () -> {
        RenderSystem.polygonOffset(-1.0f, -10.0f);
        RenderSystem.enablePolygonOffset();
    }, () -> {
        RenderSystem.polygonOffset(0.0f, 0.0f);
        RenderSystem.disablePolygonOffset();
    });
    protected static final Layering VIEW_OFFSET_Z_LAYERING = new Layering("view_offset_z_layering", () -> {
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.scale(0.99975586f, 0.99975586f, 0.99975586f);
        RenderSystem.applyModelViewMatrix();
    }, () -> {
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
    });
    protected static final Target MAIN_TARGET = new Target("main_target", () -> {}, () -> {});
    protected static final Target OUTLINE_TARGET = new Target("outline_target", () -> MinecraftClient.getInstance().worldRenderer.getEntityOutlinesFramebuffer().beginWrite(false), () -> MinecraftClient.getInstance().getFramebuffer().beginWrite(false));
    protected static final Target TRANSLUCENT_TARGET = new Target("translucent_target", () -> {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().worldRenderer.getTranslucentFramebuffer().beginWrite(false);
        }
    }, () -> {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
        }
    });
    protected static final Target PARTICLES_TARGET = new Target("particles_target", () -> {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().worldRenderer.getParticlesFramebuffer().beginWrite(false);
        }
    }, () -> {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
        }
    });
    protected static final Target WEATHER_TARGET = new Target("weather_target", () -> {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().worldRenderer.getWeatherFramebuffer().beginWrite(false);
        }
    }, () -> {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
        }
    });
    protected static final Target CLOUDS_TARGET = new Target("clouds_target", () -> {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().worldRenderer.getCloudsFramebuffer().beginWrite(false);
        }
    }, () -> {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
        }
    });
    protected static final Target ITEM_TARGET = new Target("item_entity_target", () -> {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().worldRenderer.getEntityFramebuffer().beginWrite(false);
        }
    }, () -> {
        if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
        }
    });
    protected static final LineWidth FULL_LINE_WIDTH = new LineWidth(OptionalDouble.of(1.0));

    public RenderPhase(String name, Runnable beginAction, Runnable endAction) {
        this.name = name;
        this.beginAction = beginAction;
        this.endAction = endAction;
    }

    public void startDrawing() {
        this.beginAction.run();
    }

    public void endDrawing() {
        this.endAction.run();
    }

    public String toString() {
        return this.name;
    }

    private static void setupGlintTexturing(float scale) {
        long l = Util.getMeasuringTimeMs() * 8L;
        float f = (float)(l % 110000L) / 110000.0f;
        float g = (float)(l % 30000L) / 30000.0f;
        Matrix4f matrix4f = Matrix4f.translate(-f, g, 0.0f);
        matrix4f.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(10.0f));
        matrix4f.multiply(Matrix4f.scale(scale, scale, scale));
        RenderSystem.setTextureMatrix(matrix4f);
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Transparency
    extends RenderPhase {
        public Transparency(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Shader
    extends RenderPhase {
        private final Optional<Supplier<net.minecraft.client.render.Shader>> supplier;

        public Shader(Supplier<net.minecraft.client.render.Shader> supplier) {
            super("shader", () -> RenderSystem.setShader(supplier), () -> {});
            this.supplier = Optional.of(supplier);
        }

        public Shader() {
            super("shader", () -> RenderSystem.setShader(() -> null), () -> {});
            this.supplier = Optional.empty();
        }

        @Override
        public String toString() {
            return this.name + "[" + this.supplier + "]";
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Texture
    extends TextureBase {
        private final Optional<Identifier> id;
        private final boolean blur;
        private final boolean mipmap;

        public Texture(Identifier id, boolean blur, boolean mipmap) {
            super(() -> {
                RenderSystem.enableTexture();
                TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
                textureManager.getTexture(id).setFilter(blur, mipmap);
                RenderSystem.setShaderTexture(0, id);
            }, () -> {});
            this.id = Optional.of(id);
            this.blur = blur;
            this.mipmap = mipmap;
        }

        @Override
        public String toString() {
            return this.name + "[" + this.id + "(blur=" + this.blur + ", mipmap=" + this.mipmap + ")]";
        }

        @Override
        protected Optional<Identifier> getId() {
            return this.id;
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class TextureBase
    extends RenderPhase {
        public TextureBase(Runnable apply, Runnable unapply) {
            super("texture", apply, unapply);
        }

        TextureBase() {
            super("texture", () -> {}, () -> {});
        }

        protected Optional<Identifier> getId() {
            return Optional.empty();
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Texturing
    extends RenderPhase {
        public Texturing(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Lightmap
    extends Toggleable {
        public Lightmap(boolean lightmap) {
            super("lightmap", () -> {
                if (lightmap) {
                    MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().enable();
                }
            }, () -> {
                if (lightmap) {
                    MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().disable();
                }
            }, lightmap);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Overlay
    extends Toggleable {
        public Overlay(boolean overlayColor) {
            super("overlay", () -> {
                if (overlayColor) {
                    MinecraftClient.getInstance().gameRenderer.getOverlayTexture().setupOverlayColor();
                }
            }, () -> {
                if (overlayColor) {
                    MinecraftClient.getInstance().gameRenderer.getOverlayTexture().teardownOverlayColor();
                }
            }, overlayColor);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Cull
    extends Toggleable {
        public Cull(boolean culling) {
            super("cull", () -> {
                if (!culling) {
                    RenderSystem.disableCull();
                }
            }, () -> {
                if (!culling) {
                    RenderSystem.enableCull();
                }
            }, culling);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class DepthTest
    extends RenderPhase {
        private final String depthFunctionName;

        public DepthTest(String depthFunctionName, int depthFunction) {
            super("depth_test", () -> {
                if (depthFunction != 519) {
                    RenderSystem.enableDepthTest();
                    RenderSystem.depthFunc(depthFunction);
                }
            }, () -> {
                if (depthFunction != 519) {
                    RenderSystem.disableDepthTest();
                    RenderSystem.depthFunc(515);
                }
            });
            this.depthFunctionName = depthFunctionName;
        }

        @Override
        public String toString() {
            return this.name + "[" + this.depthFunctionName + "]";
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class WriteMaskState
    extends RenderPhase {
        private final boolean color;
        private final boolean depth;

        public WriteMaskState(boolean color, boolean depth) {
            super("write_mask_state", () -> {
                if (!depth) {
                    RenderSystem.depthMask(depth);
                }
                if (!color) {
                    RenderSystem.colorMask(color, color, color, color);
                }
            }, () -> {
                if (!depth) {
                    RenderSystem.depthMask(true);
                }
                if (!color) {
                    RenderSystem.colorMask(true, true, true, true);
                }
            });
            this.color = color;
            this.depth = depth;
        }

        @Override
        public String toString() {
            return this.name + "[writeColor=" + this.color + ", writeDepth=" + this.depth + "]";
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Layering
    extends RenderPhase {
        public Layering(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Target
    extends RenderPhase {
        public Target(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class LineWidth
    extends RenderPhase {
        private final OptionalDouble width;

        public LineWidth(OptionalDouble width) {
            super("line_width", () -> {
                if (!Objects.equals(width, OptionalDouble.of(1.0))) {
                    if (width.isPresent()) {
                        RenderSystem.lineWidth((float)width.getAsDouble());
                    } else {
                        RenderSystem.lineWidth(Math.max(2.5f, (float)MinecraftClient.getInstance().getWindow().getFramebufferWidth() / 1920.0f * 2.5f));
                    }
                }
            }, () -> {
                if (!Objects.equals(width, OptionalDouble.of(1.0))) {
                    RenderSystem.lineWidth(1.0f);
                }
            });
            this.width = width;
        }

        @Override
        public String toString() {
            return this.name + "[" + (Serializable)(this.width.isPresent() ? Double.valueOf(this.width.getAsDouble()) : "window_scale") + "]";
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Toggleable
    extends RenderPhase {
        private final boolean enabled;

        public Toggleable(String name, Runnable apply, Runnable unapply, boolean enabled) {
            super(name, apply, unapply);
            this.enabled = enabled;
        }

        @Override
        public String toString() {
            return this.name + "[" + this.enabled + "]";
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static final class OffsetTexturing
    extends Texturing {
        public OffsetTexturing(float x, float y) {
            super("offset_texturing", () -> RenderSystem.setTextureMatrix(Matrix4f.translate(x, y, 0.0f)), () -> RenderSystem.resetTextureMatrix());
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Textures
    extends TextureBase {
        private final Optional<Identifier> id;

        Textures(ImmutableList<Triple<Identifier, Boolean, Boolean>> immutableList) {
            super(() -> {
                int i = 0;
                for (Triple triple : immutableList) {
                    TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
                    textureManager.getTexture((Identifier)triple.getLeft()).setFilter((Boolean)triple.getMiddle(), (Boolean)triple.getRight());
                    RenderSystem.setShaderTexture(i++, (Identifier)triple.getLeft());
                }
            }, () -> {});
            this.id = immutableList.stream().findFirst().map(Triple::getLeft);
        }

        @Override
        protected Optional<Identifier> getId() {
            return this.id;
        }

        public static Builder create() {
            return new Builder();
        }

        @Environment(value=EnvType.CLIENT)
        public static final class Textures.Builder {
            private final ImmutableList.Builder<Triple<Identifier, Boolean, Boolean>> textures = new ImmutableList.Builder();

            public Builder add(Identifier id, boolean blur, boolean mipmap) {
                this.textures.add((Object)Triple.of(id, blur, mipmap));
                return this;
            }

            public Textures build() {
                return new Textures((ImmutableList<Triple<Identifier, Boolean, Boolean>>)this.textures.build());
            }
        }
    }
}

