/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.model;

import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface BakedModel {
    public List<BakedQuad> getQuads(@Nullable BlockState var1, @Nullable Direction var2, Random var3);

    public boolean useAmbientOcclusion();

    public boolean hasDepth();

    /**
     * Allows control of the lighting when rendering a model in a GUI.
     * <p>
     * True, the model will be lit from the side, like a block.
     * <p>
     * False, the model will be lit from the front, like an item.
     */
    public boolean isSideLit();

    public boolean isBuiltin();

    /**
     * {@return a texture that represents the model}
     * <p>
     * This is primarily used in particles. For example, block break particles use this sprite.
     */
    public Sprite getParticleSprite();

    public ModelTransformation getTransformation();

    public ModelOverrideList getOverrides();
}

