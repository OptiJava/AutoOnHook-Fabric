/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.entity.model;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

/**
 * Represents the model of an {@linkplain Entity}.
 * 
 * <p>An instance of an entity model will not define the model data,
 * but will animate it.
 * Model data may be defined in a static method, most of the children classes will
 * have a {@code getTexturedModelData} method.
 * The model data is usually passed in the constructor of the entity model.
 * 
 * <p>Some children classes contain a model parts table, those tables contains
 * the model parts for the base model. In a modded context the tables may be
 * inaccurate. Model parts with an associated field are required.
 */
@Environment(value=EnvType.CLIENT)
public abstract class EntityModel<T extends Entity>
extends Model {
    public float handSwingProgress;
    public boolean riding;
    public boolean child = true;

    protected EntityModel() {
        this(RenderLayer::getEntityCutoutNoCull);
    }

    protected EntityModel(Function<Identifier, RenderLayer> function) {
        super(function);
    }

    public abstract void setAngles(T var1, float var2, float var3, float var4, float var5, float var6);

    public void animateModel(T entity, float limbAngle, float limbDistance, float tickDelta) {
    }

    public void copyStateTo(EntityModel<T> copy) {
        copy.handSwingProgress = this.handSwingProgress;
        copy.riding = this.riding;
        copy.child = this.child;
    }
}

