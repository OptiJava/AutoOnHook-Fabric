/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.fix.PointOfInterestRenameFix;

public class BeehiveRenameFix
extends PointOfInterestRenameFix {
    public BeehiveRenameFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected String rename(String input) {
        return input.equals("minecraft:bee_hive") ? "minecraft:beehive" : input;
    }
}

