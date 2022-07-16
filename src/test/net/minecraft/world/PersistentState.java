/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world;

import java.io.File;
import java.io.IOException;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class PersistentState {
    private static final Logger LOGGER = LogManager.getLogger();
    private boolean dirty;

    public abstract NbtCompound writeNbt(NbtCompound var1);

    public void markDirty() {
        this.setDirty(true);
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void save(File file) {
        if (!this.isDirty()) {
            return;
        }
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.put("data", this.writeNbt(new NbtCompound()));
        nbtCompound.putInt("DataVersion", SharedConstants.getGameVersion().getWorldVersion());
        try {
            NbtIo.writeCompressed(nbtCompound, file);
        }
        catch (IOException iOException) {
            LOGGER.error("Could not save data {}", (Object)this, (Object)iOException);
        }
        this.setDirty(false);
    }
}

