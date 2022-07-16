/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;

@Environment(value=EnvType.CLIENT)
public class BackgroundRenderer {
    private static final int field_32685 = 192;
    public static final float field_32684 = 5000.0f;
    private static float red;
    private static float green;
    private static float blue;
    private static int waterFogColor;
    private static int nextWaterFogColor;
    private static long lastWaterFogColorUpdateTime;

    public static void render(Camera camera, float tickDelta, ClientWorld world, int i2, float f) {
        int j2;
        CameraSubmersionType cameraSubmersionType = camera.getSubmersionType();
        Entity entity = camera.getFocusedEntity();
        if (cameraSubmersionType == CameraSubmersionType.WATER) {
            long l = Util.getMeasuringTimeMs();
            j2 = world.getBiome(new BlockPos(camera.getPos())).getWaterFogColor();
            if (lastWaterFogColorUpdateTime < 0L) {
                waterFogColor = j2;
                nextWaterFogColor = j2;
                lastWaterFogColorUpdateTime = l;
            }
            int k2 = waterFogColor >> 16 & 0xFF;
            int m = waterFogColor >> 8 & 0xFF;
            int n = waterFogColor & 0xFF;
            int o = nextWaterFogColor >> 16 & 0xFF;
            int p = nextWaterFogColor >> 8 & 0xFF;
            int q = nextWaterFogColor & 0xFF;
            float g = MathHelper.clamp((float)(l - lastWaterFogColorUpdateTime) / 5000.0f, 0.0f, 1.0f);
            float h = MathHelper.lerp(g, o, k2);
            float r = MathHelper.lerp(g, p, m);
            float s = MathHelper.lerp(g, q, n);
            red = h / 255.0f;
            green = r / 255.0f;
            blue = s / 255.0f;
            if (waterFogColor != j2) {
                waterFogColor = j2;
                nextWaterFogColor = MathHelper.floor(h) << 16 | MathHelper.floor(r) << 8 | MathHelper.floor(s);
                lastWaterFogColorUpdateTime = l;
            }
        } else if (cameraSubmersionType == CameraSubmersionType.LAVA) {
            red = 0.6f;
            green = 0.1f;
            blue = 0.0f;
            lastWaterFogColorUpdateTime = -1L;
        } else if (cameraSubmersionType == CameraSubmersionType.POWDER_SNOW) {
            red = 0.623f;
            green = 0.734f;
            blue = 0.785f;
            lastWaterFogColorUpdateTime = -1L;
            RenderSystem.clearColor(red, green, blue, 0.0f);
        } else {
            float h;
            float r;
            float g;
            float l = 0.25f + 0.75f * (float)i2 / 32.0f;
            l = 1.0f - (float)Math.pow(l, 0.25);
            Vec3d vec3d = world.method_23777(camera.getPos(), tickDelta);
            float j3 = (float)vec3d.x;
            float k3 = (float)vec3d.y;
            float m = (float)vec3d.z;
            float n = MathHelper.clamp(MathHelper.cos(world.getSkyAngle(tickDelta) * ((float)Math.PI * 2)) * 2.0f + 0.5f, 0.0f, 1.0f);
            BiomeAccess o = world.getBiomeAccess();
            Vec3d p = camera.getPos().subtract(2.0, 2.0, 2.0).multiply(0.25);
            Vec3d q = CubicSampler.sampleColor(p, (i, j, k) -> world.getDimensionEffects().adjustFogColor(Vec3d.unpackRgb(o.getBiomeForNoiseGen(i, j, k).getFogColor()), n));
            red = (float)q.getX();
            green = (float)q.getY();
            blue = (float)q.getZ();
            if (i2 >= 4) {
                float[] s;
                g = MathHelper.sin(world.getSkyAngleRadians(tickDelta)) > 0.0f ? -1.0f : 1.0f;
                Vec3f h2 = new Vec3f(g, 0.0f, 0.0f);
                r = camera.getHorizontalPlane().dot(h2);
                if (r < 0.0f) {
                    r = 0.0f;
                }
                if (r > 0.0f && (s = world.getDimensionEffects().getFogColorOverride(world.getSkyAngle(tickDelta), tickDelta)) != null) {
                    red = red * (1.0f - (r *= s[3])) + s[0] * r;
                    green = green * (1.0f - r) + s[1] * r;
                    blue = blue * (1.0f - r) + s[2] * r;
                }
            }
            red += (j3 - red) * l;
            green += (k3 - green) * l;
            blue += (m - blue) * l;
            g = world.getRainGradient(tickDelta);
            if (g > 0.0f) {
                float h3 = 1.0f - g * 0.5f;
                r = 1.0f - g * 0.4f;
                red *= h3;
                green *= h3;
                blue *= r;
            }
            if ((h = world.getThunderGradient(tickDelta)) > 0.0f) {
                r = 1.0f - h * 0.5f;
                red *= r;
                green *= r;
                blue *= r;
            }
            lastWaterFogColorUpdateTime = -1L;
        }
        double l = (camera.getPos().y - (double)world.getBottomY()) * world.getLevelProperties().getHorizonShadingRatio();
        if (camera.getFocusedEntity() instanceof LivingEntity && ((LivingEntity)camera.getFocusedEntity()).hasStatusEffect(StatusEffects.BLINDNESS)) {
            j2 = ((LivingEntity)camera.getFocusedEntity()).getStatusEffect(StatusEffects.BLINDNESS).getDuration();
            l = j2 < 20 ? (l *= (double)(1.0f - (float)j2 / 20.0f)) : 0.0;
        }
        if (l < 1.0 && cameraSubmersionType != CameraSubmersionType.LAVA) {
            if (l < 0.0) {
                l = 0.0;
            }
            l *= l;
            red = (float)((double)red * l);
            green = (float)((double)green * l);
            blue = (float)((double)blue * l);
        }
        if (f > 0.0f) {
            red = red * (1.0f - f) + red * 0.7f * f;
            green = green * (1.0f - f) + green * 0.6f * f;
            blue = blue * (1.0f - f) + blue * 0.6f * f;
        }
        float j4 = cameraSubmersionType == CameraSubmersionType.WATER ? (entity instanceof ClientPlayerEntity ? ((ClientPlayerEntity)entity).getUnderwaterVisibility() : 1.0f) : (entity instanceof LivingEntity && ((LivingEntity)entity).hasStatusEffect(StatusEffects.NIGHT_VISION) ? GameRenderer.getNightVisionStrength((LivingEntity)entity, tickDelta) : 0.0f);
        if (red != 0.0f && green != 0.0f && blue != 0.0f) {
            float k4 = Math.min(1.0f / red, Math.min(1.0f / green, 1.0f / blue));
            red = red * (1.0f - j4) + red * k4 * j4;
            green = green * (1.0f - j4) + green * k4 * j4;
            blue = blue * (1.0f - j4) + blue * k4 * j4;
        }
        RenderSystem.clearColor(red, green, blue, 0.0f);
    }

    public static void method_23792() {
        RenderSystem.setShaderFogStart(Float.MAX_VALUE);
    }

    public static void applyFog(Camera camera, FogType fogType, float viewDistance, boolean thickFog) {
        CameraSubmersionType cameraSubmersionType = camera.getSubmersionType();
        Entity entity = camera.getFocusedEntity();
        if (cameraSubmersionType == CameraSubmersionType.WATER) {
            float f = 192.0f;
            if (entity instanceof ClientPlayerEntity) {
                ClientPlayerEntity clientPlayerEntity = (ClientPlayerEntity)entity;
                f *= Math.max(0.25f, clientPlayerEntity.getUnderwaterVisibility());
                Biome biome = clientPlayerEntity.world.getBiome(clientPlayerEntity.getBlockPos());
                if (biome.getCategory() == Biome.Category.SWAMP) {
                    f *= 0.85f;
                }
            }
            RenderSystem.setShaderFogStart(-8.0f);
            RenderSystem.setShaderFogEnd(f * 0.5f);
        } else {
            float clientPlayerEntity;
            float f;
            if (cameraSubmersionType == CameraSubmersionType.LAVA) {
                if (entity.isSpectator()) {
                    f = -8.0f;
                    clientPlayerEntity = viewDistance * 0.5f;
                } else if (entity instanceof LivingEntity && ((LivingEntity)entity).hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
                    f = 0.0f;
                    clientPlayerEntity = 3.0f;
                } else {
                    f = 0.25f;
                    clientPlayerEntity = 1.0f;
                }
            } else if (entity instanceof LivingEntity && ((LivingEntity)entity).hasStatusEffect(StatusEffects.BLINDNESS)) {
                int biome = ((LivingEntity)entity).getStatusEffect(StatusEffects.BLINDNESS).getDuration();
                float g = MathHelper.lerp(Math.min(1.0f, (float)biome / 20.0f), viewDistance, 5.0f);
                if (fogType == FogType.FOG_SKY) {
                    f = 0.0f;
                    clientPlayerEntity = g * 0.8f;
                } else {
                    f = g * 0.25f;
                    clientPlayerEntity = g;
                }
            } else if (cameraSubmersionType == CameraSubmersionType.POWDER_SNOW) {
                if (entity.isSpectator()) {
                    f = -8.0f;
                    clientPlayerEntity = viewDistance * 0.5f;
                } else {
                    f = 0.0f;
                    clientPlayerEntity = 2.0f;
                }
            } else if (thickFog) {
                f = viewDistance * 0.05f;
                clientPlayerEntity = Math.min(viewDistance, 192.0f) * 0.5f;
            } else if (fogType == FogType.FOG_SKY) {
                f = 0.0f;
                clientPlayerEntity = viewDistance;
            } else {
                f = viewDistance * 0.75f;
                clientPlayerEntity = viewDistance;
            }
            RenderSystem.setShaderFogStart(f);
            RenderSystem.setShaderFogEnd(clientPlayerEntity);
        }
    }

    public static void setFogBlack() {
        RenderSystem.setShaderFogColor(red, green, blue);
    }

    static {
        waterFogColor = -1;
        nextWaterFogColor = -1;
        lastWaterFogColorUpdateTime = -1L;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum FogType {
        FOG_SKY,
        FOG_TERRAIN;

    }
}

