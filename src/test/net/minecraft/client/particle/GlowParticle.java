/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.particle;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class GlowParticle
extends SpriteBillboardParticle {
    static final Random RANDOM = new Random();
    private final SpriteProvider spriteProvider;

    GlowParticle(ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, SpriteProvider spriteProvider) {
        super(clientWorld, d, e, f, g, h, i);
        this.field_28786 = 0.96f;
        this.field_28787 = true;
        this.spriteProvider = spriteProvider;
        this.scale *= 0.75f;
        this.collidesWithWorld = false;
        this.setSpriteForAge(spriteProvider);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getBrightness(float tint) {
        float f = ((float)this.age + tint) / (float)this.maxAge;
        f = MathHelper.clamp(f, 0.0f, 1.0f);
        int i = super.getBrightness(tint);
        int j = i & 0xFF;
        int k = i >> 16 & 0xFF;
        if ((j += (int)(f * 15.0f * 16.0f)) > 240) {
            j = 240;
        }
        return j | k << 16;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteForAge(this.spriteProvider);
    }

    @Environment(value=EnvType.CLIENT)
    public static class ScrapeFactory
    implements ParticleFactory<DefaultParticleType> {
        private final double field_29573 = 0.01;
        private final SpriteProvider spriteProvider;

        public ScrapeFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            GlowParticle glowParticle = new GlowParticle(clientWorld, d, e, f, 0.0, 0.0, 0.0, this.spriteProvider);
            if (clientWorld.random.nextBoolean()) {
                glowParticle.setColor(0.29f, 0.58f, 0.51f);
            } else {
                glowParticle.setColor(0.43f, 0.77f, 0.62f);
            }
            glowParticle.setVelocity(g * 0.01, h * 0.01, i * 0.01);
            int j = 10;
            int k = 40;
            glowParticle.setMaxAge(clientWorld.random.nextInt(30) + 10);
            return glowParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class ElectricSparkFactory
    implements ParticleFactory<DefaultParticleType> {
        private final double field_29570 = 0.25;
        private final SpriteProvider spriteProvider;

        public ElectricSparkFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            GlowParticle glowParticle = new GlowParticle(clientWorld, d, e, f, 0.0, 0.0, 0.0, this.spriteProvider);
            glowParticle.setColor(1.0f, 0.9f, 1.0f);
            glowParticle.setVelocity(g * 0.25, h * 0.25, i * 0.25);
            int j = 2;
            int k = 4;
            glowParticle.setMaxAge(clientWorld.random.nextInt(2) + 2);
            return glowParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class WaxOffFactory
    implements ParticleFactory<DefaultParticleType> {
        private final double field_29575 = 0.01;
        private final SpriteProvider spriteProvider;

        public WaxOffFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            GlowParticle glowParticle = new GlowParticle(clientWorld, d, e, f, 0.0, 0.0, 0.0, this.spriteProvider);
            glowParticle.setColor(1.0f, 0.9f, 1.0f);
            glowParticle.setVelocity(g * 0.01 / 2.0, h * 0.01, i * 0.01 / 2.0);
            int j = 10;
            int k = 40;
            glowParticle.setMaxAge(clientWorld.random.nextInt(30) + 10);
            return glowParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class WaxOnFactory
    implements ParticleFactory<DefaultParticleType> {
        private final double field_29577 = 0.01;
        private final SpriteProvider spriteProvider;

        public WaxOnFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            GlowParticle glowParticle = new GlowParticle(clientWorld, d, e, f, 0.0, 0.0, 0.0, this.spriteProvider);
            glowParticle.setColor(0.91f, 0.55f, 0.08f);
            glowParticle.setVelocity(g * 0.01 / 2.0, h * 0.01, i * 0.01 / 2.0);
            int j = 10;
            int k = 40;
            glowParticle.setMaxAge(clientWorld.random.nextInt(30) + 10);
            return glowParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class GlowFactory
    implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public GlowFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            GlowParticle glowParticle = new GlowParticle(clientWorld, d, e, f, 0.5 - RANDOM.nextDouble(), h, 0.5 - RANDOM.nextDouble(), this.spriteProvider);
            if (clientWorld.random.nextBoolean()) {
                glowParticle.setColor(0.6f, 1.0f, 0.8f);
            } else {
                glowParticle.setColor(0.08f, 0.4f, 0.4f);
            }
            glowParticle.velocityY *= (double)0.2f;
            if (g == 0.0 && i == 0.0) {
                glowParticle.velocityX *= (double)0.1f;
                glowParticle.velocityZ *= (double)0.1f;
            }
            glowParticle.setMaxAge((int)(8.0 / (clientWorld.random.nextDouble() * 0.8 + 0.2)));
            return glowParticle;
        }
    }
}

