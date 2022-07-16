/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

@Environment(value=EnvType.CLIENT)
public class ExplosionSmokeParticle
extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;

    protected ExplosionSmokeParticle(ClientWorld world, double x, double y, double z, double d, double e, double f, SpriteProvider spriteProvider) {
        super(world, x, y, z);
        float g;
        this.gravityStrength = -0.1f;
        this.field_28786 = 0.9f;
        this.spriteProvider = spriteProvider;
        this.velocityX = d + (Math.random() * 2.0 - 1.0) * (double)0.05f;
        this.velocityY = e + (Math.random() * 2.0 - 1.0) * (double)0.05f;
        this.velocityZ = f + (Math.random() * 2.0 - 1.0) * (double)0.05f;
        this.colorRed = g = this.random.nextFloat() * 0.3f + 0.7f;
        this.colorGreen = g;
        this.colorBlue = g;
        this.scale = 0.1f * (this.random.nextFloat() * this.random.nextFloat() * 6.0f + 1.0f);
        this.maxAge = (int)(16.0 / ((double)this.random.nextFloat() * 0.8 + 0.2)) + 2;
        this.setSpriteForAge(spriteProvider);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteForAge(this.spriteProvider);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Factory
    implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            return new ExplosionSmokeParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
        }
    }
}

