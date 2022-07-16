/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen;

public interface WorldGenRandom {
    public void setSeed(long var1);

    public int nextInt();

    public int nextInt(int var1);

    public long nextLong();

    public boolean nextBoolean();

    public float nextFloat();

    public double nextDouble();

    public double nextGaussian();

    default public void skip(int count) {
        for (int i = 0; i < count; ++i) {
            this.nextInt();
        }
    }
}

