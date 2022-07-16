/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.TransparentBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;

@Environment(value=EnvType.CLIENT)
public class FluidRenderer {
    private static final float field_32781 = 0.8888889f;
    private final Sprite[] lavaSprites = new Sprite[2];
    private final Sprite[] waterSprites = new Sprite[2];
    private Sprite waterOverlaySprite;

    protected void onResourceReload() {
        this.lavaSprites[0] = MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(Blocks.LAVA.getDefaultState()).getParticleSprite();
        this.lavaSprites[1] = ModelLoader.LAVA_FLOW.getSprite();
        this.waterSprites[0] = MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(Blocks.WATER.getDefaultState()).getParticleSprite();
        this.waterSprites[1] = ModelLoader.WATER_FLOW.getSprite();
        this.waterOverlaySprite = ModelLoader.WATER_OVERLAY.getSprite();
    }

    private static boolean isSameFluid(BlockView world, BlockPos pos, Direction side, FluidState state) {
        BlockPos blockPos = pos.offset(side);
        FluidState fluidState = world.getFluidState(blockPos);
        return fluidState.getFluid().matchesType(state.getFluid());
    }

    private static boolean isSideCovered(BlockView world, Direction direction, float f, BlockPos pos, BlockState state) {
        if (state.isOpaque()) {
            VoxelShape voxelShape = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, f, 1.0);
            VoxelShape voxelShape2 = state.getCullingShape(world, pos);
            return VoxelShapes.isSideCovered(voxelShape, voxelShape2, direction);
        }
        return false;
    }

    private static boolean isSideCovered(BlockView world, BlockPos pos, Direction direction, float maxDeviation) {
        BlockPos blockPos = pos.offset(direction);
        BlockState blockState = world.getBlockState(blockPos);
        return FluidRenderer.isSideCovered(world, direction, maxDeviation, blockPos, blockState);
    }

    private static boolean isOppositeSideCovered(BlockView world, BlockPos pos, BlockState state, Direction direction) {
        return FluidRenderer.isSideCovered(world, direction.getOpposite(), 1.0f, pos, state);
    }

    public static boolean method_29708(BlockRenderView world, BlockPos pos, FluidState state, BlockState blockState, Direction direction) {
        return !FluidRenderer.isOppositeSideCovered(world, pos, blockState, direction) && !FluidRenderer.isSameFluid(world, pos, direction, state);
    }

    public boolean render(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, FluidState state) {
        float aj;
        float ai;
        float ab;
        float aa;
        float z;
        float y;
        float x;
        float w;
        float u;
        float t;
        boolean bl = state.isIn(FluidTags.LAVA);
        Sprite[] sprites = bl ? this.lavaSprites : this.waterSprites;
        BlockState blockState = world.getBlockState(pos);
        int i = bl ? 0xFFFFFF : BiomeColors.getWaterColor(world, pos);
        float f = (float)(i >> 16 & 0xFF) / 255.0f;
        float g = (float)(i >> 8 & 0xFF) / 255.0f;
        float h = (float)(i & 0xFF) / 255.0f;
        boolean bl2 = !FluidRenderer.isSameFluid(world, pos, Direction.UP, state);
        boolean bl3 = FluidRenderer.method_29708(world, pos, state, blockState, Direction.DOWN) && !FluidRenderer.isSideCovered(world, pos, Direction.DOWN, 0.8888889f);
        boolean bl4 = FluidRenderer.method_29708(world, pos, state, blockState, Direction.NORTH);
        boolean bl5 = FluidRenderer.method_29708(world, pos, state, blockState, Direction.SOUTH);
        boolean bl6 = FluidRenderer.method_29708(world, pos, state, blockState, Direction.WEST);
        boolean bl7 = FluidRenderer.method_29708(world, pos, state, blockState, Direction.EAST);
        if (!(bl2 || bl3 || bl7 || bl6 || bl4 || bl5)) {
            return false;
        }
        boolean bl8 = false;
        float j = world.getBrightness(Direction.DOWN, true);
        float k = world.getBrightness(Direction.UP, true);
        float l = world.getBrightness(Direction.NORTH, true);
        float m = world.getBrightness(Direction.WEST, true);
        float n = this.getNorthWestCornerFluidHeight(world, pos, state.getFluid());
        float o = this.getNorthWestCornerFluidHeight(world, pos.south(), state.getFluid());
        float p = this.getNorthWestCornerFluidHeight(world, pos.east().south(), state.getFluid());
        float q = this.getNorthWestCornerFluidHeight(world, pos.east(), state.getFluid());
        double d = pos.getX() & 0xF;
        double e = pos.getY() & 0xF;
        double r = pos.getZ() & 0xF;
        float s = 0.001f;
        float f2 = t = bl3 ? 0.001f : 0.0f;
        if (bl2 && !FluidRenderer.isSideCovered(world, pos, Direction.UP, Math.min(Math.min(n, o), Math.min(p, q)))) {
            float af;
            float ae;
            float ad;
            float ac;
            float v;
            bl8 = true;
            n -= 0.001f;
            o -= 0.001f;
            p -= 0.001f;
            q -= 0.001f;
            Vec3d vec3d = state.getVelocity(world, pos);
            if (vec3d.x == 0.0 && vec3d.z == 0.0) {
                sprite = sprites[0];
                u = sprite.getFrameU(0.0);
                v = sprite.getFrameV(0.0);
                w = u;
                x = sprite.getFrameV(16.0);
                y = sprite.getFrameU(16.0);
                z = x;
                aa = y;
                ab = v;
            } else {
                sprite = sprites[1];
                ac = (float)MathHelper.atan2(vec3d.z, vec3d.x) - 1.5707964f;
                ad = MathHelper.sin(ac) * 0.25f;
                ae = MathHelper.cos(ac) * 0.25f;
                af = 8.0f;
                u = sprite.getFrameU(8.0f + (-ae - ad) * 16.0f);
                v = sprite.getFrameV(8.0f + (-ae + ad) * 16.0f);
                w = sprite.getFrameU(8.0f + (-ae + ad) * 16.0f);
                x = sprite.getFrameV(8.0f + (ae + ad) * 16.0f);
                y = sprite.getFrameU(8.0f + (ae + ad) * 16.0f);
                z = sprite.getFrameV(8.0f + (ae - ad) * 16.0f);
                aa = sprite.getFrameU(8.0f + (ae - ad) * 16.0f);
                ab = sprite.getFrameV(8.0f + (-ae - ad) * 16.0f);
            }
            float sprite = (u + w + y + aa) / 4.0f;
            ac = (v + x + z + ab) / 4.0f;
            ad = (float)sprites[0].getWidth() / (sprites[0].getMaxU() - sprites[0].getMinU());
            ae = (float)sprites[0].getHeight() / (sprites[0].getMaxV() - sprites[0].getMinV());
            af = 4.0f / Math.max(ae, ad);
            u = MathHelper.lerp(af, u, sprite);
            w = MathHelper.lerp(af, w, sprite);
            y = MathHelper.lerp(af, y, sprite);
            aa = MathHelper.lerp(af, aa, sprite);
            v = MathHelper.lerp(af, v, ac);
            x = MathHelper.lerp(af, x, ac);
            z = MathHelper.lerp(af, z, ac);
            ab = MathHelper.lerp(af, ab, ac);
            int ag = this.getLight(world, pos);
            float ah = k * f;
            ai = k * g;
            aj = k * h;
            this.vertex(vertexConsumer, d + 0.0, e + (double)n, r + 0.0, ah, ai, aj, u, v, ag);
            this.vertex(vertexConsumer, d + 0.0, e + (double)o, r + 1.0, ah, ai, aj, w, x, ag);
            this.vertex(vertexConsumer, d + 1.0, e + (double)p, r + 1.0, ah, ai, aj, y, z, ag);
            this.vertex(vertexConsumer, d + 1.0, e + (double)q, r + 0.0, ah, ai, aj, aa, ab, ag);
            if (state.method_15756(world, pos.up())) {
                this.vertex(vertexConsumer, d + 0.0, e + (double)n, r + 0.0, ah, ai, aj, u, v, ag);
                this.vertex(vertexConsumer, d + 1.0, e + (double)q, r + 0.0, ah, ai, aj, aa, ab, ag);
                this.vertex(vertexConsumer, d + 1.0, e + (double)p, r + 1.0, ah, ai, aj, y, z, ag);
                this.vertex(vertexConsumer, d + 0.0, e + (double)o, r + 1.0, ah, ai, aj, w, x, ag);
            }
        }
        if (bl3) {
            u = sprites[0].getMinU();
            w = sprites[0].getMaxU();
            y = sprites[0].getMinV();
            aa = sprites[0].getMaxV();
            int v = this.getLight(world, pos.down());
            x = j * f;
            z = j * g;
            ab = j * h;
            this.vertex(vertexConsumer, d, e + (double)t, r + 1.0, x, z, ab, u, aa, v);
            this.vertex(vertexConsumer, d, e + (double)t, r, x, z, ab, u, y, v);
            this.vertex(vertexConsumer, d + 1.0, e + (double)t, r, x, z, ab, w, y, v);
            this.vertex(vertexConsumer, d + 1.0, e + (double)t, r + 1.0, x, z, ab, w, aa, v);
            bl8 = true;
        }
        int u2 = this.getLight(world, pos);
        for (int w2 = 0; w2 < 4; ++w2) {
            Block ai2;
            boolean af;
            Direction ae;
            double ac;
            double z2;
            double vec3d;
            double v;
            if (w2 == 0) {
                y = n;
                aa = q;
                v = d;
                vec3d = d + 1.0;
                z2 = r + (double)0.001f;
                ac = r + (double)0.001f;
                ae = Direction.NORTH;
                af = bl4;
            } else if (w2 == 1) {
                y = p;
                aa = o;
                v = d + 1.0;
                vec3d = d;
                z2 = r + 1.0 - (double)0.001f;
                ac = r + 1.0 - (double)0.001f;
                ae = Direction.SOUTH;
                af = bl5;
            } else if (w2 == 2) {
                y = o;
                aa = n;
                v = d + (double)0.001f;
                vec3d = d + (double)0.001f;
                z2 = r + 1.0;
                ac = r;
                ae = Direction.WEST;
                af = bl6;
            } else {
                y = q;
                aa = p;
                v = d + 1.0 - (double)0.001f;
                vec3d = d + 1.0 - (double)0.001f;
                z2 = r;
                ac = r + 1.0;
                ae = Direction.EAST;
                af = bl7;
            }
            if (!af || FluidRenderer.isSideCovered(world, pos, ae, Math.max(y, aa))) continue;
            bl8 = true;
            BlockPos ag = pos.offset(ae);
            Sprite ah = sprites[1];
            if (!bl && ((ai2 = world.getBlockState(ag).getBlock()) instanceof TransparentBlock || ai2 instanceof LeavesBlock)) {
                ah = this.waterOverlaySprite;
            }
            ai = ah.getFrameU(0.0);
            aj = ah.getFrameU(8.0);
            float ak = ah.getFrameV((1.0f - y) * 16.0f * 0.5f);
            float al = ah.getFrameV((1.0f - aa) * 16.0f * 0.5f);
            float am = ah.getFrameV(8.0);
            float an = w2 < 2 ? l : m;
            float ao = k * an * f;
            float ap = k * an * g;
            float aq = k * an * h;
            this.vertex(vertexConsumer, v, e + (double)y, z2, ao, ap, aq, ai, ak, u2);
            this.vertex(vertexConsumer, vec3d, e + (double)aa, ac, ao, ap, aq, aj, al, u2);
            this.vertex(vertexConsumer, vec3d, e + (double)t, ac, ao, ap, aq, aj, am, u2);
            this.vertex(vertexConsumer, v, e + (double)t, z2, ao, ap, aq, ai, am, u2);
            if (ah == this.waterOverlaySprite) continue;
            this.vertex(vertexConsumer, v, e + (double)t, z2, ao, ap, aq, ai, am, u2);
            this.vertex(vertexConsumer, vec3d, e + (double)t, ac, ao, ap, aq, aj, am, u2);
            this.vertex(vertexConsumer, vec3d, e + (double)aa, ac, ao, ap, aq, aj, al, u2);
            this.vertex(vertexConsumer, v, e + (double)y, z2, ao, ap, aq, ai, ak, u2);
        }
        return bl8;
    }

    private void vertex(VertexConsumer vertexConsumer, double x, double y, double z, float red, float green, float blue, float u, float v, int light) {
        vertexConsumer.vertex(x, y, z).color(red, green, blue, 1.0f).texture(u, v).light(light).normal(0.0f, 1.0f, 0.0f).next();
    }

    private int getLight(BlockRenderView world, BlockPos pos) {
        int i = WorldRenderer.getLightmapCoordinates(world, pos);
        int j = WorldRenderer.getLightmapCoordinates(world, pos.up());
        int k = i & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xF);
        int l = j & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xF);
        int m = i >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xF);
        int n = j >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xF);
        return (k > l ? k : l) | (m > n ? m : n) << 16;
    }

    private float getNorthWestCornerFluidHeight(BlockView world, BlockPos pos, Fluid fluid) {
        int i = 0;
        float f = 0.0f;
        for (int j = 0; j < 4; ++j) {
            BlockPos blockPos = pos.add(-(j & 1), 0, -(j >> 1 & 1));
            if (world.getFluidState(blockPos.up()).getFluid().matchesType(fluid)) {
                return 1.0f;
            }
            FluidState fluidState = world.getFluidState(blockPos);
            if (fluidState.getFluid().matchesType(fluid)) {
                float g = fluidState.getHeight(world, blockPos);
                if (g >= 0.8f) {
                    f += g * 10.0f;
                    i += 10;
                    continue;
                }
                f += g;
                ++i;
                continue;
            }
            if (world.getBlockState(blockPos).getMaterial().isSolid()) continue;
            ++i;
        }
        return f / (float)i;
    }
}

