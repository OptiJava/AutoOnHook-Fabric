/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.world.gen.feature;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.placer.BlockPlacer;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class RandomPatchFeatureConfig
implements FeatureConfig {
    public static final Codec<RandomPatchFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockStateProvider.TYPE_CODEC.fieldOf("state_provider")).forGetter(randomPatchFeatureConfig -> randomPatchFeatureConfig.stateProvider), ((MapCodec)BlockPlacer.TYPE_CODEC.fieldOf("block_placer")).forGetter(randomPatchFeatureConfig -> randomPatchFeatureConfig.blockPlacer), ((MapCodec)BlockState.CODEC.listOf().fieldOf("whitelist")).forGetter(randomPatchFeatureConfig -> randomPatchFeatureConfig.whitelist.stream().map(Block::getDefaultState).collect(Collectors.toList())), ((MapCodec)BlockState.CODEC.listOf().fieldOf("blacklist")).forGetter(randomPatchFeatureConfig -> ImmutableList.copyOf(randomPatchFeatureConfig.blacklist)), ((MapCodec)Codecs.POSITIVE_INT.fieldOf("tries")).orElse(128).forGetter(randomPatchFeatureConfig -> randomPatchFeatureConfig.tries), ((MapCodec)Codecs.NONNEGATIVE_INT.fieldOf("xspread")).orElse(7).forGetter(randomPatchFeatureConfig -> randomPatchFeatureConfig.spreadX), ((MapCodec)Codecs.NONNEGATIVE_INT.fieldOf("yspread")).orElse(3).forGetter(randomPatchFeatureConfig -> randomPatchFeatureConfig.spreadY), ((MapCodec)Codecs.NONNEGATIVE_INT.fieldOf("zspread")).orElse(7).forGetter(randomPatchFeatureConfig -> randomPatchFeatureConfig.spreadZ), ((MapCodec)Codec.BOOL.fieldOf("can_replace")).orElse(false).forGetter(randomPatchFeatureConfig -> randomPatchFeatureConfig.canReplace), ((MapCodec)Codec.BOOL.fieldOf("project")).orElse(true).forGetter(randomPatchFeatureConfig -> randomPatchFeatureConfig.project), ((MapCodec)Codec.BOOL.fieldOf("need_water")).orElse(false).forGetter(randomPatchFeatureConfig -> randomPatchFeatureConfig.needsWater)).apply((Applicative<RandomPatchFeatureConfig, ?>)instance, RandomPatchFeatureConfig::new));
    public final BlockStateProvider stateProvider;
    public final BlockPlacer blockPlacer;
    public final Set<Block> whitelist;
    public final Set<BlockState> blacklist;
    public final int tries;
    public final int spreadX;
    public final int spreadY;
    public final int spreadZ;
    public final boolean canReplace;
    public final boolean project;
    public final boolean needsWater;

    private RandomPatchFeatureConfig(BlockStateProvider stateProvider, BlockPlacer blockPlacer, List<BlockState> whitelist, List<BlockState> blacklist, int tries, int spreadX, int spreadY, int spreadZ, boolean canReplace, boolean project, boolean needsWater) {
        this(stateProvider, blockPlacer, whitelist.stream().map(AbstractBlock.AbstractBlockState::getBlock).collect(Collectors.toSet()), ImmutableSet.copyOf(blacklist), tries, spreadX, spreadY, spreadZ, canReplace, project, needsWater);
    }

    RandomPatchFeatureConfig(BlockStateProvider stateProvider, BlockPlacer blockPlacer, Set<Block> whitelist, Set<BlockState> blacklist, int tries, int spreadX, int spreadY, int spreadZ, boolean canReplace, boolean project, boolean needsWater) {
        this.stateProvider = stateProvider;
        this.blockPlacer = blockPlacer;
        this.whitelist = whitelist;
        this.blacklist = blacklist;
        this.tries = tries;
        this.spreadX = spreadX;
        this.spreadY = spreadY;
        this.spreadZ = spreadZ;
        this.canReplace = canReplace;
        this.project = project;
        this.needsWater = needsWater;
    }

    public static class Builder {
        private final BlockStateProvider stateProvider;
        private final BlockPlacer blockPlacer;
        private Set<Block> whitelist = ImmutableSet.of();
        private Set<BlockState> blacklist = ImmutableSet.of();
        private int tries = 64;
        private int spreadX = 7;
        private int spreadY = 3;
        private int spreadZ = 7;
        private boolean canReplace;
        private boolean project = true;
        private boolean needsWater;

        public Builder(BlockStateProvider stateProvider, BlockPlacer blockPlacer) {
            this.stateProvider = stateProvider;
            this.blockPlacer = blockPlacer;
        }

        public Builder whitelist(Set<Block> whitelist) {
            this.whitelist = whitelist;
            return this;
        }

        public Builder blacklist(Set<BlockState> blacklist) {
            this.blacklist = blacklist;
            return this;
        }

        public Builder tries(int tries) {
            this.tries = tries;
            return this;
        }

        public Builder spreadX(int spreadX) {
            this.spreadX = spreadX;
            return this;
        }

        public Builder spreadY(int spreadY) {
            this.spreadY = spreadY;
            return this;
        }

        public Builder spreadZ(int spreadZ) {
            this.spreadZ = spreadZ;
            return this;
        }

        public Builder canReplace() {
            this.canReplace = true;
            return this;
        }

        public Builder cannotProject() {
            this.project = false;
            return this;
        }

        public Builder needsWater() {
            this.needsWater = true;
            return this;
        }

        public RandomPatchFeatureConfig build() {
            return new RandomPatchFeatureConfig(this.stateProvider, this.blockPlacer, this.whitelist, this.blacklist, this.tries, this.spreadX, this.spreadY, this.spreadZ, this.canReplace, this.project, this.needsWater);
        }
    }
}

