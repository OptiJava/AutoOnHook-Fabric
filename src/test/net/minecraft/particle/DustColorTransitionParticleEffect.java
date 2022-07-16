/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.AbstractDustParticleEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;

public class DustColorTransitionParticleEffect
extends AbstractDustParticleEffect {
    public static final Vec3f SCULK_BLUE = new Vec3f(Vec3d.unpackRgb(3790560));
    public static final DustColorTransitionParticleEffect DEFAULT = new DustColorTransitionParticleEffect(SCULK_BLUE, DustParticleEffect.RED, 1.0f);
    public static final Codec<DustColorTransitionParticleEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Vec3f.CODEC.fieldOf("fromColor")).forGetter(dustColorTransitionParticleEffect -> dustColorTransitionParticleEffect.color), ((MapCodec)Vec3f.CODEC.fieldOf("toColor")).forGetter(dustColorTransitionParticleEffect -> dustColorTransitionParticleEffect.toColor), ((MapCodec)Codec.FLOAT.fieldOf("scale")).forGetter(dustColorTransitionParticleEffect -> Float.valueOf(dustColorTransitionParticleEffect.scale))).apply((Applicative<DustColorTransitionParticleEffect, ?>)instance, DustColorTransitionParticleEffect::new));
    public static final ParticleEffect.Factory<DustColorTransitionParticleEffect> FACTORY = new ParticleEffect.Factory<DustColorTransitionParticleEffect>(){

        @Override
        public DustColorTransitionParticleEffect read(ParticleType<DustColorTransitionParticleEffect> particleType, StringReader stringReader) throws CommandSyntaxException {
            Vec3f vec3f = AbstractDustParticleEffect.readColor(stringReader);
            stringReader.expect(' ');
            float f = stringReader.readFloat();
            Vec3f vec3f2 = AbstractDustParticleEffect.readColor(stringReader);
            return new DustColorTransitionParticleEffect(vec3f, vec3f2, f);
        }

        @Override
        public DustColorTransitionParticleEffect read(ParticleType<DustColorTransitionParticleEffect> particleType, PacketByteBuf packetByteBuf) {
            Vec3f vec3f = AbstractDustParticleEffect.readColor(packetByteBuf);
            float f = packetByteBuf.readFloat();
            Vec3f vec3f2 = AbstractDustParticleEffect.readColor(packetByteBuf);
            return new DustColorTransitionParticleEffect(vec3f, vec3f2, f);
        }

        @Override
        public /* synthetic */ ParticleEffect read(ParticleType type, PacketByteBuf buf) {
            return this.read(type, buf);
        }

        @Override
        public /* synthetic */ ParticleEffect read(ParticleType type, StringReader reader) throws CommandSyntaxException {
            return this.read(type, reader);
        }
    };
    private final Vec3f toColor;

    public DustColorTransitionParticleEffect(Vec3f fromColor, Vec3f toColor, float scale) {
        super(fromColor, scale);
        this.toColor = toColor;
    }

    public Vec3f getFromColor() {
        return this.color;
    }

    public Vec3f getToColor() {
        return this.toColor;
    }

    @Override
    public void write(PacketByteBuf buf) {
        super.write(buf);
        buf.writeFloat(this.toColor.getX());
        buf.writeFloat(this.toColor.getY());
        buf.writeFloat(this.toColor.getZ());
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %.2f %.2f %.2f", Registry.PARTICLE_TYPE.getId(this.getType()), Float.valueOf(this.color.getX()), Float.valueOf(this.color.getY()), Float.valueOf(this.color.getZ()), Float.valueOf(this.scale), Float.valueOf(this.toColor.getX()), Float.valueOf(this.toColor.getY()), Float.valueOf(this.toColor.getZ()));
    }

    public ParticleType<DustColorTransitionParticleEffect> getType() {
        return ParticleTypes.DUST_COLOR_TRANSITION;
    }
}

