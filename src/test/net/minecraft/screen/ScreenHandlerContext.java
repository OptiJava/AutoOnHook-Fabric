/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.screen;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Screen handler contexts allow screen handlers to interact with the
 * logical server's world safely.
 */
public interface ScreenHandlerContext {
    /**
     * The dummy screen handler context for clientside screen handlers.
     */
    public static final ScreenHandlerContext EMPTY = new ScreenHandlerContext(){

        @Override
        public <T> Optional<T> get(BiFunction<World, BlockPos, T> getter) {
            return Optional.empty();
        }
    };

    /**
     * Returns an active screen handler context. Used on the logical server.
     */
    public static ScreenHandlerContext create(final World world, final BlockPos pos) {
        return new ScreenHandlerContext(){

            @Override
            public <T> Optional<T> get(BiFunction<World, BlockPos, T> getter) {
                return Optional.of(getter.apply(world, pos));
            }
        };
    }

    /**
     * Gets an optional value from this context's world and position
     * with a {@link BiFunction} getter.
     * 
     * @return a present {@link Optional} with the getter's return value,
     *         or {@link Optional#empty()} if this context is empty
     * 
     * @param getter a function that gets a non-null value from this context's world and position
     */
    public <T> Optional<T> get(BiFunction<World, BlockPos, T> var1);

    /**
     * Gets a value from this context's world and position
     * with a {@link BiFunction} getter.
     * 
     * @return the getter's return value if this context is active,
     *         the default value otherwise
     * 
     * @param getter a function that gets a non-null value from this context's world and position
     * @param defaultValue a fallback default value, used if this context is empty
     */
    default public <T> T get(BiFunction<World, BlockPos, T> getter, T defaultValue) {
        return this.get(getter).orElse(defaultValue);
    }

    /**
     * Runs a {@link BiConsumer} with this context's world and position
     * if this context is active.
     */
    default public void run(BiConsumer<World, BlockPos> function) {
        this.get((world, pos) -> {
            function.accept((World)world, (BlockPos)pos);
            return Optional.empty();
        });
    }
}

