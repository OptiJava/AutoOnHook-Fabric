/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AbstractZombieModel;
import net.minecraft.entity.mob.GiantEntity;

/**
 * Represents the model of a {@linkplain GiantEntity}.
 * 
 * <p>Inherits the model of {@link AbstractZombieModel}.
 */
@Environment(value=EnvType.CLIENT)
public class GiantEntityModel
extends AbstractZombieModel<GiantEntity> {
    public GiantEntityModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public boolean isAttacking(GiantEntity giantEntity) {
        return false;
    }
}

