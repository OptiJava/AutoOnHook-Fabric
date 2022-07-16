/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui.hud;

import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class ClientBossBar
extends BossBar {
    private static final long field_32204 = 100L;
    protected float healthLatest;
    protected long timeHealthSet;

    public ClientBossBar(UUID uuid, Text name, float percent, BossBar.Color color, BossBar.Style style, boolean darkenSky, boolean dragonMusic, boolean thickenFog) {
        super(uuid, name, color, style);
        this.healthLatest = percent;
        this.percent = percent;
        this.timeHealthSet = Util.getMeasuringTimeMs();
        this.setDarkenSky(darkenSky);
        this.setDragonMusic(dragonMusic);
        this.setThickenFog(thickenFog);
    }

    @Override
    public void setPercent(float percentage) {
        this.percent = this.getPercent();
        this.healthLatest = percentage;
        this.timeHealthSet = Util.getMeasuringTimeMs();
    }

    @Override
    public float getPercent() {
        long l = Util.getMeasuringTimeMs() - this.timeHealthSet;
        float f = MathHelper.clamp((float)l / 100.0f, 0.0f, 1.0f);
        return MathHelper.lerp(f, this.percent, this.healthLatest);
    }
}
