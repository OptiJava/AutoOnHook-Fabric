/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.structure.rule;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.structure.rule.RuleTestType;

public class AlwaysTrueRuleTest
extends RuleTest {
    public static final Codec<AlwaysTrueRuleTest> CODEC = Codec.unit(() -> INSTANCE);
    public static final AlwaysTrueRuleTest INSTANCE = new AlwaysTrueRuleTest();

    private AlwaysTrueRuleTest() {
    }

    @Override
    public boolean test(BlockState state, Random random) {
        return true;
    }

    @Override
    protected RuleTestType<?> getType() {
        return RuleTestType.ALWAYS_TRUE;
    }
}

