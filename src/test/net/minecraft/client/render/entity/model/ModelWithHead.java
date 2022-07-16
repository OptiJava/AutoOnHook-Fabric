/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;

/**
 * Represents a model with a head.
 */
@Environment(value=EnvType.CLIENT)
public interface ModelWithHead {
    /**
     * Gets the head model part.
     * 
     * @return the head
     */
    public ModelPart getHead();
}

