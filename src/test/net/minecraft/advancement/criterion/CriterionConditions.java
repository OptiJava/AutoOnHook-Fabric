/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.util.Identifier;

public interface CriterionConditions {
    public Identifier getId();

    public JsonObject toJson(AdvancementEntityPredicateSerializer var1);
}

