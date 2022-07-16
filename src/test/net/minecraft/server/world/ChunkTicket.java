/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.server.world;

import java.util.Objects;
import net.minecraft.server.world.ChunkTicketType;

/**
 * Represents a chunk ticket, which specifies the reason a chunk has been loaded.
 */
public final class ChunkTicket<T>
implements Comparable<ChunkTicket<?>> {
    private final ChunkTicketType<T> type;
    private final int level;
    private final T argument;
    private long tickCreated;

    protected ChunkTicket(ChunkTicketType<T> type, int level, T argument) {
        this.type = type;
        this.level = level;
        this.argument = argument;
    }

    @Override
    public int compareTo(ChunkTicket<?> chunkTicket) {
        int i = Integer.compare(this.level, chunkTicket.level);
        if (i != 0) {
            return i;
        }
        int j = Integer.compare(System.identityHashCode(this.type), System.identityHashCode(chunkTicket.type));
        if (j != 0) {
            return j;
        }
        return this.type.getArgumentComparator().compare(this.argument, chunkTicket.argument);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChunkTicket)) {
            return false;
        }
        ChunkTicket chunkTicket = (ChunkTicket)o;
        return this.level == chunkTicket.level && Objects.equals(this.type, chunkTicket.type) && Objects.equals(this.argument, chunkTicket.argument);
    }

    public int hashCode() {
        return Objects.hash(this.type, this.level, this.argument);
    }

    public String toString() {
        return "Ticket[" + this.type + " " + this.level + " (" + this.argument + ")] at " + this.tickCreated;
    }

    public ChunkTicketType<T> getType() {
        return this.type;
    }

    public int getLevel() {
        return this.level;
    }

    protected void setTickCreated(long tickCreated) {
        this.tickCreated = tickCreated;
    }

    protected boolean isExpired(long currentTick) {
        long l = this.type.getExpiryTicks();
        return l != 0L && currentTick - this.tickCreated > l;
    }

    @Override
    public /* synthetic */ int compareTo(Object that) {
        return this.compareTo((ChunkTicket)that);
    }
}

