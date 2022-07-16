/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.block;

import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BlockModelRenderer {
    private static final int field_32782 = 0;
    private static final int field_32783 = 1;
    static final Direction[] DIRECTIONS = Direction.values();
    private final BlockColors colors;
    private static final int BRIGHTNESS_CACHE_MAX_SIZE = 100;
    static final ThreadLocal<BrightnessCache> BRIGHTNESS_CACHE = ThreadLocal.withInitial(BrightnessCache::new);

    public BlockModelRenderer(BlockColors colors) {
        this.colors = colors;
    }

    public boolean render(BlockRenderView world, BakedModel model, BlockState state, BlockPos pos, MatrixStack matrix, VertexConsumer vertexConsumer, boolean cull, Random random, long seed, int overlay) {
        boolean bl = MinecraftClient.isAmbientOcclusionEnabled() && state.getLuminance() == 0 && model.useAmbientOcclusion();
        Vec3d vec3d = state.getModelOffset(world, pos);
        matrix.translate(vec3d.x, vec3d.y, vec3d.z);
        try {
            if (bl) {
                return this.renderSmooth(world, model, state, pos, matrix, vertexConsumer, cull, random, seed, overlay);
            }
            return this.renderFlat(world, model, state, pos, matrix, vertexConsumer, cull, random, seed, overlay);
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.create(throwable, "Tesselating block model");
            CrashReportSection crashReportSection = crashReport.addElement("Block model being tesselated");
            CrashReportSection.addBlockInfo(crashReportSection, world, pos, state);
            crashReportSection.add("Using AO", bl);
            throw new CrashException(crashReport);
        }
    }

    public boolean renderSmooth(BlockRenderView world, BakedModel model, BlockState state, BlockPos pos, MatrixStack buffer, VertexConsumer vertexConsumer, boolean cull, Random random, long seed, int overlay) {
        boolean bl = false;
        float[] fs = new float[DIRECTIONS.length * 2];
        BitSet bitSet = new BitSet(3);
        AmbientOcclusionCalculator ambientOcclusionCalculator = new AmbientOcclusionCalculator();
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (Direction direction : DIRECTIONS) {
            random.setSeed(seed);
            List<BakedQuad> list = model.getQuads(state, direction, random);
            if (list.isEmpty()) continue;
            mutable.set((Vec3i)pos, direction);
            if (cull && !Block.shouldDrawSide(state, world, pos, direction, mutable)) continue;
            this.renderQuadsSmooth(world, state, pos, buffer, vertexConsumer, list, fs, bitSet, ambientOcclusionCalculator, overlay);
            bl = true;
        }
        random.setSeed(seed);
        List<BakedQuad> list2 = model.getQuads(state, null, random);
        if (!list2.isEmpty()) {
            this.renderQuadsSmooth(world, state, pos, buffer, vertexConsumer, list2, fs, bitSet, ambientOcclusionCalculator, overlay);
            bl = true;
        }
        return bl;
    }

    public boolean renderFlat(BlockRenderView world, BakedModel model, BlockState state, BlockPos pos, MatrixStack buffer, VertexConsumer vertexConsumer, boolean cull, Random random, long l, int i) {
        boolean bl = false;
        BitSet bitSet = new BitSet(3);
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (Direction direction : DIRECTIONS) {
            random.setSeed(l);
            List<BakedQuad> list = model.getQuads(state, direction, random);
            if (list.isEmpty()) continue;
            mutable.set((Vec3i)pos, direction);
            if (cull && !Block.shouldDrawSide(state, world, pos, direction, mutable)) continue;
            int j = WorldRenderer.getLightmapCoordinates(world, state, mutable);
            this.renderQuadsFlat(world, state, pos, j, i, false, buffer, vertexConsumer, list, bitSet);
            bl = true;
        }
        random.setSeed(l);
        List<BakedQuad> list2 = model.getQuads(state, null, random);
        if (!list2.isEmpty()) {
            this.renderQuadsFlat(world, state, pos, -1, i, true, buffer, vertexConsumer, list2, bitSet);
            bl = true;
        }
        return bl;
    }

    private void renderQuadsSmooth(BlockRenderView world, BlockState state, BlockPos pos, MatrixStack matrix, VertexConsumer vertexConsumer, List<BakedQuad> quads, float[] box, BitSet flags, AmbientOcclusionCalculator ambientOcclusionCalculator, int overlay) {
        for (BakedQuad bakedQuad : quads) {
            this.getQuadDimensions(world, state, pos, bakedQuad.getVertexData(), bakedQuad.getFace(), box, flags);
            ambientOcclusionCalculator.apply(world, state, pos, bakedQuad.getFace(), box, flags, bakedQuad.hasShade());
            this.renderQuad(world, state, pos, vertexConsumer, matrix.peek(), bakedQuad, ambientOcclusionCalculator.brightness[0], ambientOcclusionCalculator.brightness[1], ambientOcclusionCalculator.brightness[2], ambientOcclusionCalculator.brightness[3], ambientOcclusionCalculator.light[0], ambientOcclusionCalculator.light[1], ambientOcclusionCalculator.light[2], ambientOcclusionCalculator.light[3], overlay);
        }
    }

    private void renderQuad(BlockRenderView world, BlockState state, BlockPos pos, VertexConsumer vertexConsumer, MatrixStack.Entry matrixEntry, BakedQuad quad, float brightness0, float brightness1, float brightness2, float brightness3, int light0, int light1, int light2, int light3, int overlay) {
        float h;
        float g;
        float f;
        if (quad.hasColor()) {
            int i = this.colors.getColor(state, world, pos, quad.getColorIndex());
            f = (float)(i >> 16 & 0xFF) / 255.0f;
            g = (float)(i >> 8 & 0xFF) / 255.0f;
            h = (float)(i & 0xFF) / 255.0f;
        } else {
            f = 1.0f;
            g = 1.0f;
            h = 1.0f;
        }
        vertexConsumer.quad(matrixEntry, quad, new float[]{brightness0, brightness1, brightness2, brightness3}, f, g, h, new int[]{light0, light1, light2, light3}, overlay, true);
    }

    private void getQuadDimensions(BlockRenderView world, BlockState state, BlockPos pos, int[] vertexData, Direction face, @Nullable float[] box, BitSet flags) {
        float m;
        int l;
        float f = 32.0f;
        float g = 32.0f;
        float h = 32.0f;
        float i = -32.0f;
        float j = -32.0f;
        float k = -32.0f;
        for (l = 0; l < 4; ++l) {
            m = Float.intBitsToFloat(vertexData[l * 8]);
            float n = Float.intBitsToFloat(vertexData[l * 8 + 1]);
            float o = Float.intBitsToFloat(vertexData[l * 8 + 2]);
            f = Math.min(f, m);
            g = Math.min(g, n);
            h = Math.min(h, o);
            i = Math.max(i, m);
            j = Math.max(j, n);
            k = Math.max(k, o);
        }
        if (box != null) {
            box[Direction.WEST.getId()] = f;
            box[Direction.EAST.getId()] = i;
            box[Direction.DOWN.getId()] = g;
            box[Direction.UP.getId()] = j;
            box[Direction.NORTH.getId()] = h;
            box[Direction.SOUTH.getId()] = k;
            l = DIRECTIONS.length;
            box[Direction.WEST.getId() + l] = 1.0f - f;
            box[Direction.EAST.getId() + l] = 1.0f - i;
            box[Direction.DOWN.getId() + l] = 1.0f - g;
            box[Direction.UP.getId() + l] = 1.0f - j;
            box[Direction.NORTH.getId() + l] = 1.0f - h;
            box[Direction.SOUTH.getId() + l] = 1.0f - k;
        }
        float l2 = 1.0E-4f;
        m = 0.9999f;
        switch (face) {
            case DOWN: {
                flags.set(1, f >= 1.0E-4f || h >= 1.0E-4f || i <= 0.9999f || k <= 0.9999f);
                flags.set(0, g == j && (g < 1.0E-4f || state.isFullCube(world, pos)));
                break;
            }
            case UP: {
                flags.set(1, f >= 1.0E-4f || h >= 1.0E-4f || i <= 0.9999f || k <= 0.9999f);
                flags.set(0, g == j && (j > 0.9999f || state.isFullCube(world, pos)));
                break;
            }
            case NORTH: {
                flags.set(1, f >= 1.0E-4f || g >= 1.0E-4f || i <= 0.9999f || j <= 0.9999f);
                flags.set(0, h == k && (h < 1.0E-4f || state.isFullCube(world, pos)));
                break;
            }
            case SOUTH: {
                flags.set(1, f >= 1.0E-4f || g >= 1.0E-4f || i <= 0.9999f || j <= 0.9999f);
                flags.set(0, h == k && (k > 0.9999f || state.isFullCube(world, pos)));
                break;
            }
            case WEST: {
                flags.set(1, g >= 1.0E-4f || h >= 1.0E-4f || j <= 0.9999f || k <= 0.9999f);
                flags.set(0, f == i && (f < 1.0E-4f || state.isFullCube(world, pos)));
                break;
            }
            case EAST: {
                flags.set(1, g >= 1.0E-4f || h >= 1.0E-4f || j <= 0.9999f || k <= 0.9999f);
                flags.set(0, f == i && (i > 0.9999f || state.isFullCube(world, pos)));
            }
        }
    }

    private void renderQuadsFlat(BlockRenderView world, BlockState state, BlockPos pos, int light, int overlay, boolean useWorldLight, MatrixStack matrices, VertexConsumer vertexConsumer, List<BakedQuad> quads, BitSet flags) {
        for (BakedQuad bakedQuad : quads) {
            if (useWorldLight) {
                this.getQuadDimensions(world, state, pos, bakedQuad.getVertexData(), bakedQuad.getFace(), null, flags);
                BlockPos blockPos = flags.get(0) ? pos.offset(bakedQuad.getFace()) : pos;
                light = WorldRenderer.getLightmapCoordinates(world, state, blockPos);
            }
            float blockPos = world.getBrightness(bakedQuad.getFace(), bakedQuad.hasShade());
            this.renderQuad(world, state, pos, vertexConsumer, matrices.peek(), bakedQuad, blockPos, blockPos, blockPos, blockPos, light, light, light, light, overlay);
        }
    }

    public void render(MatrixStack.Entry entry, VertexConsumer vertexConsumer, @Nullable BlockState blockState, BakedModel bakedModel, float f, float g, float h, int i, int j) {
        Random random = new Random();
        long l = 42L;
        for (Direction direction : DIRECTIONS) {
            random.setSeed(42L);
            BlockModelRenderer.renderQuad(entry, vertexConsumer, f, g, h, bakedModel.getQuads(blockState, direction, random), i, j);
        }
        random.setSeed(42L);
        BlockModelRenderer.renderQuad(entry, vertexConsumer, f, g, h, bakedModel.getQuads(blockState, null, random), i, j);
    }

    private static void renderQuad(MatrixStack.Entry entry, VertexConsumer vertexConsumer, float f, float g, float h, List<BakedQuad> list, int i, int j) {
        for (BakedQuad bakedQuad : list) {
            float m;
            float l;
            float k;
            if (bakedQuad.hasColor()) {
                k = MathHelper.clamp(f, 0.0f, 1.0f);
                l = MathHelper.clamp(g, 0.0f, 1.0f);
                m = MathHelper.clamp(h, 0.0f, 1.0f);
            } else {
                k = 1.0f;
                l = 1.0f;
                m = 1.0f;
            }
            vertexConsumer.quad(entry, bakedQuad, k, l, m, i, j);
        }
    }

    public static void enableBrightnessCache() {
        BRIGHTNESS_CACHE.get().enable();
    }

    public static void disableBrightnessCache() {
        BRIGHTNESS_CACHE.get().disable();
    }

    @Environment(value=EnvType.CLIENT)
    class AmbientOcclusionCalculator {
        final float[] brightness = new float[4];
        final int[] light = new int[4];

        public void apply(BlockRenderView world, BlockState state, BlockPos pos, Direction direction, float[] box, BitSet flags, boolean bl) {
            float w;
            int u;
            float t;
            int s;
            float r;
            int q;
            float p;
            int o;
            float n;
            BlockState blockState9;
            boolean bl5;
            BlockPos blockPos = flags.get(0) ? pos.offset(direction) : pos;
            NeighborData neighborData = NeighborData.getData(direction);
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            BrightnessCache brightnessCache = BRIGHTNESS_CACHE.get();
            mutable.set((Vec3i)blockPos, neighborData.faces[0]);
            BlockState blockState = world.getBlockState(mutable);
            int i = brightnessCache.getInt(blockState, world, mutable);
            float f = brightnessCache.getFloat(blockState, world, mutable);
            mutable.set((Vec3i)blockPos, neighborData.faces[1]);
            BlockState blockState2 = world.getBlockState(mutable);
            int j = brightnessCache.getInt(blockState2, world, mutable);
            float g = brightnessCache.getFloat(blockState2, world, mutable);
            mutable.set((Vec3i)blockPos, neighborData.faces[2]);
            BlockState blockState3 = world.getBlockState(mutable);
            int k = brightnessCache.getInt(blockState3, world, mutable);
            float h = brightnessCache.getFloat(blockState3, world, mutable);
            mutable.set((Vec3i)blockPos, neighborData.faces[3]);
            BlockState blockState4 = world.getBlockState(mutable);
            int l = brightnessCache.getInt(blockState4, world, mutable);
            float m = brightnessCache.getFloat(blockState4, world, mutable);
            BlockState blockState5 = world.getBlockState(mutable.set((Vec3i)blockPos, neighborData.faces[0]).move(direction));
            boolean bl2 = !blockState5.shouldBlockVision(world, mutable) || blockState5.getOpacity(world, mutable) == 0;
            BlockState blockState6 = world.getBlockState(mutable.set((Vec3i)blockPos, neighborData.faces[1]).move(direction));
            boolean bl3 = !blockState6.shouldBlockVision(world, mutable) || blockState6.getOpacity(world, mutable) == 0;
            BlockState blockState7 = world.getBlockState(mutable.set((Vec3i)blockPos, neighborData.faces[2]).move(direction));
            boolean bl4 = !blockState7.shouldBlockVision(world, mutable) || blockState7.getOpacity(world, mutable) == 0;
            BlockState blockState8 = world.getBlockState(mutable.set((Vec3i)blockPos, neighborData.faces[3]).move(direction));
            boolean bl6 = bl5 = !blockState8.shouldBlockVision(world, mutable) || blockState8.getOpacity(world, mutable) == 0;
            if (bl4 || bl2) {
                mutable.set((Vec3i)blockPos, neighborData.faces[0]).move(neighborData.faces[2]);
                blockState9 = world.getBlockState(mutable);
                n = brightnessCache.getFloat(blockState9, world, mutable);
                o = brightnessCache.getInt(blockState9, world, mutable);
            } else {
                n = f;
                o = i;
            }
            if (bl5 || bl2) {
                mutable.set((Vec3i)blockPos, neighborData.faces[0]).move(neighborData.faces[3]);
                blockState9 = world.getBlockState(mutable);
                p = brightnessCache.getFloat(blockState9, world, mutable);
                q = brightnessCache.getInt(blockState9, world, mutable);
            } else {
                p = f;
                q = i;
            }
            if (bl4 || bl3) {
                mutable.set((Vec3i)blockPos, neighborData.faces[1]).move(neighborData.faces[2]);
                blockState9 = world.getBlockState(mutable);
                r = brightnessCache.getFloat(blockState9, world, mutable);
                s = brightnessCache.getInt(blockState9, world, mutable);
            } else {
                r = f;
                s = i;
            }
            if (bl5 || bl3) {
                mutable.set((Vec3i)blockPos, neighborData.faces[1]).move(neighborData.faces[3]);
                blockState9 = world.getBlockState(mutable);
                t = brightnessCache.getFloat(blockState9, world, mutable);
                u = brightnessCache.getInt(blockState9, world, mutable);
            } else {
                t = f;
                u = i;
            }
            int blockState92 = brightnessCache.getInt(state, world, pos);
            mutable.set((Vec3i)pos, direction);
            BlockState blockState10 = world.getBlockState(mutable);
            if (flags.get(0) || !blockState10.isOpaqueFullCube(world, mutable)) {
                blockState92 = brightnessCache.getInt(blockState10, world, mutable);
            }
            float v = flags.get(0) ? brightnessCache.getFloat(world.getBlockState(blockPos), world, blockPos) : brightnessCache.getFloat(world.getBlockState(pos), world, pos);
            Translation translation = Translation.getTranslations(direction);
            if (!flags.get(1) || !neighborData.nonCubicWeight) {
                w = (m + f + p + v) * 0.25f;
                x = (h + f + n + v) * 0.25f;
                y = (h + g + r + v) * 0.25f;
                z = (m + g + t + v) * 0.25f;
                this.light[translation.firstCorner] = this.getAmbientOcclusionBrightness(l, i, q, blockState92);
                this.light[translation.secondCorner] = this.getAmbientOcclusionBrightness(k, i, o, blockState92);
                this.light[translation.thirdCorner] = this.getAmbientOcclusionBrightness(k, j, s, blockState92);
                this.light[translation.fourthCorner] = this.getAmbientOcclusionBrightness(l, j, u, blockState92);
                this.brightness[translation.firstCorner] = w;
                this.brightness[translation.secondCorner] = x;
                this.brightness[translation.thirdCorner] = y;
                this.brightness[translation.fourthCorner] = z;
            } else {
                w = (m + f + p + v) * 0.25f;
                x = (h + f + n + v) * 0.25f;
                y = (h + g + r + v) * 0.25f;
                z = (m + g + t + v) * 0.25f;
                float aa = box[neighborData.field_4192[0].shape] * box[neighborData.field_4192[1].shape];
                float ab = box[neighborData.field_4192[2].shape] * box[neighborData.field_4192[3].shape];
                float ac = box[neighborData.field_4192[4].shape] * box[neighborData.field_4192[5].shape];
                float ad = box[neighborData.field_4192[6].shape] * box[neighborData.field_4192[7].shape];
                float ae = box[neighborData.field_4185[0].shape] * box[neighborData.field_4185[1].shape];
                float af = box[neighborData.field_4185[2].shape] * box[neighborData.field_4185[3].shape];
                float ag = box[neighborData.field_4185[4].shape] * box[neighborData.field_4185[5].shape];
                float ah = box[neighborData.field_4185[6].shape] * box[neighborData.field_4185[7].shape];
                float ai = box[neighborData.field_4180[0].shape] * box[neighborData.field_4180[1].shape];
                float aj = box[neighborData.field_4180[2].shape] * box[neighborData.field_4180[3].shape];
                float ak = box[neighborData.field_4180[4].shape] * box[neighborData.field_4180[5].shape];
                float al = box[neighborData.field_4180[6].shape] * box[neighborData.field_4180[7].shape];
                float am = box[neighborData.field_4188[0].shape] * box[neighborData.field_4188[1].shape];
                float an = box[neighborData.field_4188[2].shape] * box[neighborData.field_4188[3].shape];
                float ao = box[neighborData.field_4188[4].shape] * box[neighborData.field_4188[5].shape];
                float ap = box[neighborData.field_4188[6].shape] * box[neighborData.field_4188[7].shape];
                this.brightness[translation.firstCorner] = w * aa + x * ab + y * ac + z * ad;
                this.brightness[translation.secondCorner] = w * ae + x * af + y * ag + z * ah;
                this.brightness[translation.thirdCorner] = w * ai + x * aj + y * ak + z * al;
                this.brightness[translation.fourthCorner] = w * am + x * an + y * ao + z * ap;
                int aq = this.getAmbientOcclusionBrightness(l, i, q, blockState92);
                int ar = this.getAmbientOcclusionBrightness(k, i, o, blockState92);
                int as = this.getAmbientOcclusionBrightness(k, j, s, blockState92);
                int at = this.getAmbientOcclusionBrightness(l, j, u, blockState92);
                this.light[translation.firstCorner] = this.getBrightness(aq, ar, as, at, aa, ab, ac, ad);
                this.light[translation.secondCorner] = this.getBrightness(aq, ar, as, at, ae, af, ag, ah);
                this.light[translation.thirdCorner] = this.getBrightness(aq, ar, as, at, ai, aj, ak, al);
                this.light[translation.fourthCorner] = this.getBrightness(aq, ar, as, at, am, an, ao, ap);
            }
            w = world.getBrightness(direction, bl);
            int x = 0;
            while (x < this.brightness.length) {
                int n2 = x++;
                this.brightness[n2] = this.brightness[n2] * w;
            }
        }

        private int getAmbientOcclusionBrightness(int i, int j, int k, int l) {
            if (i == 0) {
                i = l;
            }
            if (j == 0) {
                j = l;
            }
            if (k == 0) {
                k = l;
            }
            return i + j + k + l >> 2 & 0xFF00FF;
        }

        private int getBrightness(int i, int j, int k, int l, float f, float g, float h, float m) {
            int n = (int)((float)(i >> 16 & 0xFF) * f + (float)(j >> 16 & 0xFF) * g + (float)(k >> 16 & 0xFF) * h + (float)(l >> 16 & 0xFF) * m) & 0xFF;
            int o = (int)((float)(i & 0xFF) * f + (float)(j & 0xFF) * g + (float)(k & 0xFF) * h + (float)(l & 0xFF) * m) & 0xFF;
            return n << 16 | o;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class BrightnessCache {
        private boolean enabled;
        private final Long2IntLinkedOpenHashMap intCache = Util.make(() -> {
            Long2IntLinkedOpenHashMap long2IntLinkedOpenHashMap = new Long2IntLinkedOpenHashMap(100, 0.25f){

                @Override
                protected void rehash(int i) {
                }
            };
            long2IntLinkedOpenHashMap.defaultReturnValue(Integer.MAX_VALUE);
            return long2IntLinkedOpenHashMap;
        });
        private final Long2FloatLinkedOpenHashMap floatCache = Util.make(() -> {
            Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(100, 0.25f){

                @Override
                protected void rehash(int i) {
                }
            };
            long2FloatLinkedOpenHashMap.defaultReturnValue(Float.NaN);
            return long2FloatLinkedOpenHashMap;
        });

        private BrightnessCache() {
        }

        public void enable() {
            this.enabled = true;
        }

        public void disable() {
            this.enabled = false;
            this.intCache.clear();
            this.floatCache.clear();
        }

        public int getInt(BlockState state, BlockRenderView world, BlockPos pos) {
            int i;
            long l = pos.asLong();
            if (this.enabled && (i = this.intCache.get(l)) != Integer.MAX_VALUE) {
                return i;
            }
            i = WorldRenderer.getLightmapCoordinates(world, state, pos);
            if (this.enabled) {
                if (this.intCache.size() == 100) {
                    this.intCache.removeFirstInt();
                }
                this.intCache.put(l, i);
            }
            return i;
        }

        public float getFloat(BlockState state, BlockRenderView blockView, BlockPos pos) {
            float f;
            long l = pos.asLong();
            if (this.enabled && !Float.isNaN(f = this.floatCache.get(l))) {
                return f;
            }
            f = state.getAmbientOcclusionLightLevel(blockView, pos);
            if (this.enabled) {
                if (this.floatCache.size() == 100) {
                    this.floatCache.removeFirstFloat();
                }
                this.floatCache.put(l, f);
            }
            return f;
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static enum NeighborData {
        DOWN(new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}, 0.5f, true, new NeighborOrientation[]{NeighborOrientation.FLIP_WEST, NeighborOrientation.SOUTH, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.WEST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.WEST, NeighborOrientation.SOUTH}, new NeighborOrientation[]{NeighborOrientation.FLIP_WEST, NeighborOrientation.NORTH, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.WEST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.WEST, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.FLIP_EAST, NeighborOrientation.NORTH, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.EAST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.EAST, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.FLIP_EAST, NeighborOrientation.SOUTH, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.EAST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.EAST, NeighborOrientation.SOUTH}),
        UP(new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}, 1.0f, true, new NeighborOrientation[]{NeighborOrientation.EAST, NeighborOrientation.SOUTH, NeighborOrientation.EAST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_EAST, NeighborOrientation.SOUTH}, new NeighborOrientation[]{NeighborOrientation.EAST, NeighborOrientation.NORTH, NeighborOrientation.EAST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_EAST, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.WEST, NeighborOrientation.NORTH, NeighborOrientation.WEST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_WEST, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.WEST, NeighborOrientation.SOUTH, NeighborOrientation.WEST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_WEST, NeighborOrientation.SOUTH}),
        NORTH(new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST}, 0.8f, true, new NeighborOrientation[]{NeighborOrientation.UP, NeighborOrientation.FLIP_WEST, NeighborOrientation.UP, NeighborOrientation.WEST, NeighborOrientation.FLIP_UP, NeighborOrientation.WEST, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_WEST}, new NeighborOrientation[]{NeighborOrientation.UP, NeighborOrientation.FLIP_EAST, NeighborOrientation.UP, NeighborOrientation.EAST, NeighborOrientation.FLIP_UP, NeighborOrientation.EAST, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_EAST}, new NeighborOrientation[]{NeighborOrientation.DOWN, NeighborOrientation.FLIP_EAST, NeighborOrientation.DOWN, NeighborOrientation.EAST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.EAST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_EAST}, new NeighborOrientation[]{NeighborOrientation.DOWN, NeighborOrientation.FLIP_WEST, NeighborOrientation.DOWN, NeighborOrientation.WEST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.WEST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_WEST}),
        SOUTH(new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP}, 0.8f, true, new NeighborOrientation[]{NeighborOrientation.UP, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_UP, NeighborOrientation.WEST, NeighborOrientation.UP, NeighborOrientation.WEST}, new NeighborOrientation[]{NeighborOrientation.DOWN, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.WEST, NeighborOrientation.DOWN, NeighborOrientation.WEST}, new NeighborOrientation[]{NeighborOrientation.DOWN, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.EAST, NeighborOrientation.DOWN, NeighborOrientation.EAST}, new NeighborOrientation[]{NeighborOrientation.UP, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_UP, NeighborOrientation.EAST, NeighborOrientation.UP, NeighborOrientation.EAST}),
        WEST(new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH}, 0.6f, true, new NeighborOrientation[]{NeighborOrientation.UP, NeighborOrientation.SOUTH, NeighborOrientation.UP, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_UP, NeighborOrientation.SOUTH}, new NeighborOrientation[]{NeighborOrientation.UP, NeighborOrientation.NORTH, NeighborOrientation.UP, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_UP, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.DOWN, NeighborOrientation.NORTH, NeighborOrientation.DOWN, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_DOWN, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.DOWN, NeighborOrientation.SOUTH, NeighborOrientation.DOWN, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_DOWN, NeighborOrientation.SOUTH}),
        EAST(new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH}, 0.6f, true, new NeighborOrientation[]{NeighborOrientation.FLIP_DOWN, NeighborOrientation.SOUTH, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.DOWN, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.DOWN, NeighborOrientation.SOUTH}, new NeighborOrientation[]{NeighborOrientation.FLIP_DOWN, NeighborOrientation.NORTH, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_NORTH, NeighborOrientation.DOWN, NeighborOrientation.FLIP_NORTH, NeighborOrientation.DOWN, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.FLIP_UP, NeighborOrientation.NORTH, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_NORTH, NeighborOrientation.UP, NeighborOrientation.FLIP_NORTH, NeighborOrientation.UP, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.FLIP_UP, NeighborOrientation.SOUTH, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.UP, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.UP, NeighborOrientation.SOUTH});

        final Direction[] faces;
        final boolean nonCubicWeight;
        final NeighborOrientation[] field_4192;
        final NeighborOrientation[] field_4185;
        final NeighborOrientation[] field_4180;
        final NeighborOrientation[] field_4188;
        private static final NeighborData[] field_4190;

        private NeighborData(Direction[] faces, float f, boolean nonCubicWeight, NeighborOrientation[] neighborOrientations, NeighborOrientation[] neighborOrientations2, NeighborOrientation[] neighborOrientations3, NeighborOrientation[] neighborOrientations4) {
            this.faces = faces;
            this.nonCubicWeight = nonCubicWeight;
            this.field_4192 = neighborOrientations;
            this.field_4185 = neighborOrientations2;
            this.field_4180 = neighborOrientations3;
            this.field_4188 = neighborOrientations4;
        }

        public static NeighborData getData(Direction direction) {
            return field_4190[direction.getId()];
        }

        static {
            field_4190 = Util.make(new NeighborData[6], neighborDatas -> {
                neighborDatas[Direction.DOWN.getId()] = DOWN;
                neighborDatas[Direction.UP.getId()] = UP;
                neighborDatas[Direction.NORTH.getId()] = NORTH;
                neighborDatas[Direction.SOUTH.getId()] = SOUTH;
                neighborDatas[Direction.WEST.getId()] = WEST;
                neighborDatas[Direction.EAST.getId()] = EAST;
            });
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static enum NeighborOrientation {
        DOWN(Direction.DOWN, false),
        UP(Direction.UP, false),
        NORTH(Direction.NORTH, false),
        SOUTH(Direction.SOUTH, false),
        WEST(Direction.WEST, false),
        EAST(Direction.EAST, false),
        FLIP_DOWN(Direction.DOWN, true),
        FLIP_UP(Direction.UP, true),
        FLIP_NORTH(Direction.NORTH, true),
        FLIP_SOUTH(Direction.SOUTH, true),
        FLIP_WEST(Direction.WEST, true),
        FLIP_EAST(Direction.EAST, true);

        final int shape;

        private NeighborOrientation(Direction direction, boolean flip) {
            this.shape = direction.getId() + (flip ? DIRECTIONS.length : 0);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum Translation {
        DOWN(0, 1, 2, 3),
        UP(2, 3, 0, 1),
        NORTH(3, 0, 1, 2),
        SOUTH(0, 1, 2, 3),
        WEST(3, 0, 1, 2),
        EAST(1, 2, 3, 0);

        final int firstCorner;
        final int secondCorner;
        final int thirdCorner;
        final int fourthCorner;
        private static final Translation[] VALUES;

        private Translation(int firstCorner, int secondCorner, int thirdCorner, int fourthCorner) {
            this.firstCorner = firstCorner;
            this.secondCorner = secondCorner;
            this.thirdCorner = thirdCorner;
            this.fourthCorner = fourthCorner;
        }

        public static Translation getTranslations(Direction direction) {
            return VALUES[direction.getId()];
        }

        static {
            VALUES = Util.make(new Translation[6], translations -> {
                translations[Direction.DOWN.getId()] = DOWN;
                translations[Direction.UP.getId()] = UP;
                translations[Direction.NORTH.getId()] = NORTH;
                translations[Direction.SOUTH.getId()] = SOUTH;
                translations[Direction.WEST.getId()] = WEST;
                translations[Direction.EAST.getId()] = EAST;
            });
        }
    }
}

