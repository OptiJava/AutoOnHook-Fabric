/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.structure;

import net.minecraft.structure.DesertVillageData;
import net.minecraft.structure.PlainsVillageData;
import net.minecraft.structure.SavannaVillageData;
import net.minecraft.structure.SnowyVillageData;
import net.minecraft.structure.TaigaVillageData;

public class VillageGenerator {
    public static void init() {
        PlainsVillageData.init();
        SnowyVillageData.init();
        SavannaVillageData.init();
        DesertVillageData.init();
        TaigaVillageData.init();
    }
}

