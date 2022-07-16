/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.event.listener;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import org.jetbrains.annotations.Nullable;

/**
 * A game event listener listens to game events from {@link GameEventDispatcher}s.
 */
public interface GameEventListener {
    /**
     * Returns the position source of this listener.
     */
    public PositionSource getPositionSource();

    /**
     * Returns the range, in blocks, of the listener.
     */
    public int getRange();

    /**
     * Listens to an incoming game event.
     * 
     * @return {@code true} if the game event has been accepted by this listener
     */
    public boolean listen(World var1, GameEvent var2, @Nullable Entity var3, BlockPos var4);
}

