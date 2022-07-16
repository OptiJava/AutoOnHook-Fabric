/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.poi;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.poi.PointOfInterestType;

public class PointOfInterest {
    private final BlockPos pos;
    private final PointOfInterestType type;
    private int freeTickets;
    private final Runnable updateListener;

    public static Codec<PointOfInterest> createCodec(Runnable updateListener) {
        return RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockPos.CODEC.fieldOf("pos")).forGetter(poi -> poi.pos), ((MapCodec)Registry.POINT_OF_INTEREST_TYPE.fieldOf("type")).forGetter(poi -> poi.type), ((MapCodec)Codec.INT.fieldOf("free_tickets")).orElse(0).forGetter(poi -> poi.freeTickets), RecordCodecBuilder.point(updateListener)).apply((Applicative<PointOfInterest, ?>)instance, PointOfInterest::new));
    }

    private PointOfInterest(BlockPos pos, PointOfInterestType type, int freeTickets, Runnable updateListener) {
        this.pos = pos.toImmutable();
        this.type = type;
        this.freeTickets = freeTickets;
        this.updateListener = updateListener;
    }

    public PointOfInterest(BlockPos pos, PointOfInterestType type, Runnable updateListener) {
        this(pos, type, type.getTicketCount(), updateListener);
    }

    @Deprecated
    @Debug
    public int getFreeTickets() {
        return this.freeTickets;
    }

    protected boolean reserveTicket() {
        if (this.freeTickets <= 0) {
            return false;
        }
        --this.freeTickets;
        this.updateListener.run();
        return true;
    }

    protected boolean releaseTicket() {
        if (this.freeTickets >= this.type.getTicketCount()) {
            return false;
        }
        ++this.freeTickets;
        this.updateListener.run();
        return true;
    }

    public boolean hasSpace() {
        return this.freeTickets > 0;
    }

    public boolean isOccupied() {
        return this.freeTickets != this.type.getTicketCount();
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public PointOfInterestType getType() {
        return this.type;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.pos, ((PointOfInterest)o).pos);
    }

    public int hashCode() {
        return this.pos.hashCode();
    }
}

