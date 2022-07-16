/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;

public abstract class AbstractDustParticleEffect
implements ParticleEffect {
    public static final float field_33114 = 0.01f;
    public static final float field_33115 = 4.0f;
    protected final Vec3f color;
    protected final float scale;

    public AbstractDustParticleEffect(Vec3f color, float scale) {
        this.color = color;
        this.scale = MathHelper.clamp(scale, 0.01f, 4.0f);
    }

    public static Vec3f readColor(StringReader stringReader) throws CommandSyntaxException {
        stringReader.expect(' ');
        float f = stringReader.readFloat();
        stringReader.expect(' ');
        float g = stringReader.readFloat();
        stringReader.expect(' ');
        float h = stringReader.readFloat();
        return new Vec3f(f, g, h);
    }

    public static Vec3f readColor(PacketByteBuf buf) {
        return new Vec3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeFloat(this.color.getX());
        buf.writeFloat(this.color.getY());
        buf.writeFloat(this.color.getZ());
        buf.writeFloat(this.scale);
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f", Registry.PARTICLE_TYPE.getId(this.getType()), Float.valueOf(this.color.getX()), Float.valueOf(this.color.getY()), Float.valueOf(this.color.getZ()), Float.valueOf(this.scale));
    }

    public Vec3f getColor() {
        return this.color;
    }

    public float getScale() {
        return this.scale;
    }
}

