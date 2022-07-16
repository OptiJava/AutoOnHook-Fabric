/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.util.profiler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleFunction;
import net.minecraft.util.profiler.SampleType;
import org.jetbrains.annotations.Nullable;

public class Sampler {
    private final String name;
    private final SampleType type;
    private final DoubleSupplier retriever;
    private final ByteBuf ticksBuffer;
    private final ByteBuf valueBuffer;
    private volatile boolean active;
    @Nullable
    private final Runnable startAction;
    @Nullable
    final DeviationChecker deviationChecker;
    private double currentSample;

    protected Sampler(String name, SampleType type, DoubleSupplier retriever, @Nullable Runnable startAction, @Nullable DeviationChecker deviationChecker) {
        this.name = name;
        this.type = type;
        this.startAction = startAction;
        this.retriever = retriever;
        this.deviationChecker = deviationChecker;
        this.valueBuffer = ByteBufAllocator.DEFAULT.buffer();
        this.ticksBuffer = ByteBufAllocator.DEFAULT.buffer();
        this.active = true;
    }

    public static Sampler create(String name, SampleType type, DoubleSupplier retriever) {
        return new Sampler(name, type, retriever, null, null);
    }

    public static <T> Sampler create(String name, SampleType type, T context, ToDoubleFunction<T> retriever) {
        return Sampler.builder(name, type, retriever, context).build();
    }

    public static <T> Builder<T> builder(String name, SampleType type, ToDoubleFunction<T> retriever, T context) {
        return new Builder<T>(name, type, retriever, context);
    }

    public void start() {
        if (!this.active) {
            throw new IllegalStateException("Not running");
        }
        if (this.startAction != null) {
            this.startAction.run();
        }
    }

    public void sample(int tick) {
        this.ensureActive();
        this.currentSample = this.retriever.getAsDouble();
        this.valueBuffer.writeDouble(this.currentSample);
        this.ticksBuffer.writeInt(tick);
    }

    public void stop() {
        this.ensureActive();
        this.valueBuffer.release();
        this.ticksBuffer.release();
        this.active = false;
    }

    private void ensureActive() {
        if (!this.active) {
            throw new IllegalStateException(String.format("Sampler for metric %s not started!", this.name));
        }
    }

    DoubleSupplier getRetriever() {
        return this.retriever;
    }

    public String getName() {
        return this.name;
    }

    public SampleType getType() {
        return this.type;
    }

    public Data collectData() {
        Int2DoubleOpenHashMap int2DoubleMap = new Int2DoubleOpenHashMap();
        int i = Integer.MIN_VALUE;
        int j = Integer.MIN_VALUE;
        while (this.valueBuffer.isReadable(8)) {
            int k = this.ticksBuffer.readInt();
            if (i == Integer.MIN_VALUE) {
                i = k;
            }
            int2DoubleMap.put(k, this.valueBuffer.readDouble());
            j = k;
        }
        return new Data(i, j, int2DoubleMap);
    }

    public boolean hasDeviated() {
        return this.deviationChecker != null && this.deviationChecker.check(this.currentSample);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Sampler sampler = (Sampler)o;
        return this.name.equals(sampler.name) && this.type.equals((Object)sampler.type);
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public static interface DeviationChecker {
        public boolean check(double var1);
    }

    public static class Builder<T> {
        private final String name;
        private final SampleType type;
        private final DoubleSupplier timeGetter;
        private final T context;
        @Nullable
        private Runnable startAction;
        @Nullable
        private DeviationChecker deviationChecker;

        public Builder(String name, SampleType type, ToDoubleFunction<T> timeFunction, T context) {
            this.name = name;
            this.type = type;
            this.timeGetter = () -> timeFunction.applyAsDouble(context);
            this.context = context;
        }

        public Builder<T> startAction(Consumer<T> action) {
            this.startAction = () -> action.accept(this.context);
            return this;
        }

        public Builder<T> deviationChecker(DeviationChecker deviationChecker) {
            this.deviationChecker = deviationChecker;
            return this;
        }

        public Sampler build() {
            return new Sampler(this.name, this.type, this.timeGetter, this.startAction, this.deviationChecker);
        }
    }

    public static class Data {
        private final Int2DoubleMap values;
        private final int startTick;
        private final int endTick;

        public Data(int startTick, int endTick, Int2DoubleMap values) {
            this.startTick = startTick;
            this.endTick = endTick;
            this.values = values;
        }

        public double getValue(int tick) {
            return this.values.get(tick);
        }

        public int getStartTick() {
            return this.startTick;
        }

        public int getEndTick() {
            return this.endTick;
        }
    }

    public static class RatioDeviationChecker
    implements DeviationChecker {
        private final float threshold;
        private double lastValue = Double.MIN_VALUE;

        public RatioDeviationChecker(float threshold) {
            this.threshold = threshold;
        }

        @Override
        public boolean check(double value) {
            boolean bl = this.lastValue == Double.MIN_VALUE || value <= this.lastValue ? false : (value - this.lastValue) / this.lastValue >= (double)this.threshold;
            this.lastValue = value;
            return bl;
        }
    }
}

