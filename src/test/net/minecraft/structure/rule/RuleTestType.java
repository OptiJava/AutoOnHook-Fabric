/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.structure.rule;

import com.mojang.serialization.Codec;
import net.minecraft.structure.rule.AlwaysTrueRuleTest;
import net.minecraft.structure.rule.BlockMatchRuleTest;
import net.minecraft.structure.rule.BlockStateMatchRuleTest;
import net.minecraft.structure.rule.RandomBlockMatchRuleTest;
import net.minecraft.structure.rule.RandomBlockStateMatchRuleTest;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.structure.rule.TagMatchRuleTest;
import net.minecraft.util.registry.Registry;

public interface RuleTestType<P extends RuleTest> {
    public static final RuleTestType<AlwaysTrueRuleTest> ALWAYS_TRUE = RuleTestType.register("always_true", AlwaysTrueRuleTest.CODEC);
    public static final RuleTestType<BlockMatchRuleTest> BLOCK_MATCH = RuleTestType.register("block_match", BlockMatchRuleTest.CODEC);
    public static final RuleTestType<BlockStateMatchRuleTest> BLOCKSTATE_MATCH = RuleTestType.register("blockstate_match", BlockStateMatchRuleTest.CODEC);
    public static final RuleTestType<TagMatchRuleTest> TAG_MATCH = RuleTestType.register("tag_match", TagMatchRuleTest.CODEC);
    public static final RuleTestType<RandomBlockMatchRuleTest> RANDOM_BLOCK_MATCH = RuleTestType.register("random_block_match", RandomBlockMatchRuleTest.CODEC);
    public static final RuleTestType<RandomBlockStateMatchRuleTest> RANDOM_BLOCKSTATE_MATCH = RuleTestType.register("random_blockstate_match", RandomBlockStateMatchRuleTest.CODEC);

    public Codec<P> codec();

    public static <P extends RuleTest> RuleTestType<P> register(String id, Codec<P> codec) {
        return Registry.register(Registry.RULE_TEST, id, () -> codec);
    }
}

