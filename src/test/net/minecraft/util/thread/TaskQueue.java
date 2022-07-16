/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.util.thread;

import com.google.common.collect.Queues;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jetbrains.annotations.Nullable;

public interface TaskQueue<T, F> {
    @Nullable
    public F poll();

    public boolean add(T var1);

    public boolean isEmpty();

    public int getSize();

    public static final class Prioritized
    implements TaskQueue<PrioritizedTask, Runnable> {
        private final List<Queue<Runnable>> queues;

        public Prioritized(int priorityCount) {
            this.queues = IntStream.range(0, priorityCount).mapToObj(i -> Queues.newConcurrentLinkedQueue()).collect(Collectors.toList());
        }

        @Override
        @Nullable
        public Runnable poll() {
            for (Queue<Runnable> queue : this.queues) {
                Runnable runnable = queue.poll();
                if (runnable == null) continue;
                return runnable;
            }
            return null;
        }

        @Override
        public boolean add(PrioritizedTask prioritizedTask) {
            int i = prioritizedTask.getPriority();
            this.queues.get(i).add(prioritizedTask);
            return true;
        }

        @Override
        public boolean isEmpty() {
            return this.queues.stream().allMatch(Collection::isEmpty);
        }

        @Override
        public int getSize() {
            int i = 0;
            for (Queue<Runnable> queue : this.queues) {
                i += queue.size();
            }
            return i;
        }

        @Override
        @Nullable
        public /* synthetic */ Object poll() {
            return this.poll();
        }
    }

    public static final class PrioritizedTask
    implements Runnable {
        private final int priority;
        private final Runnable runnable;

        public PrioritizedTask(int priority, Runnable runnable) {
            this.priority = priority;
            this.runnable = runnable;
        }

        @Override
        public void run() {
            this.runnable.run();
        }

        public int getPriority() {
            return this.priority;
        }
    }

    public static final class Simple<T>
    implements TaskQueue<T, T> {
        private final Queue<T> queue;

        public Simple(Queue<T> queue) {
            this.queue = queue;
        }

        @Override
        @Nullable
        public T poll() {
            return this.queue.poll();
        }

        @Override
        public boolean add(T message) {
            return this.queue.add(message);
        }

        @Override
        public boolean isEmpty() {
            return this.queue.isEmpty();
        }

        @Override
        public int getSize() {
            return this.queue.size();
        }
    }
}

