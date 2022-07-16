/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTestBatch;
import net.minecraft.test.GameTestState;
import net.minecraft.test.StructureTestUtil;
import net.minecraft.test.TestFunction;
import net.minecraft.test.TestListener;
import net.minecraft.test.TestManager;
import net.minecraft.test.TestSet;
import net.minecraft.test.TestUtil;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestRunner {
    private static final Logger LOGGER = LogManager.getLogger();
    private final BlockPos pos;
    final ServerWorld world;
    private final TestManager testManager;
    private final int sizeZ;
    private final List<GameTestState> tests;
    private final List<Pair<GameTestBatch, Collection<GameTestState>>> batches;
    private final BlockPos.Mutable reusablePos;

    public TestRunner(Collection<GameTestBatch> batches, BlockPos pos, BlockRotation rotation, ServerWorld world, TestManager testManager, int sizeZ) {
        this.reusablePos = pos.mutableCopy();
        this.pos = pos;
        this.world = world;
        this.testManager = testManager;
        this.sizeZ = sizeZ;
        this.batches = batches.stream().map(batch -> {
            Collection collection = batch.getTestFunctions().stream().map(testFunction -> new GameTestState((TestFunction)testFunction, rotation, world)).collect(ImmutableList.toImmutableList());
            return Pair.of(batch, collection);
        }).collect(ImmutableList.toImmutableList());
        this.tests = this.batches.stream().flatMap(batch -> ((Collection)batch.getSecond()).stream()).collect(ImmutableList.toImmutableList());
    }

    public List<GameTestState> getTests() {
        return this.tests;
    }

    public void run() {
        this.runBatch(0);
    }

    void runBatch(final int index) {
        if (index >= this.batches.size()) {
            return;
        }
        Pair<GameTestBatch, Collection<GameTestState>> pair = this.batches.get(index);
        final GameTestBatch gameTestBatch = pair.getFirst();
        Collection<GameTestState> collection = pair.getSecond();
        Map<GameTestState, BlockPos> map = this.method_29401(collection);
        String string = gameTestBatch.getId();
        LOGGER.info("Running test batch '{}' ({} tests)...", (Object)string, (Object)collection.size());
        gameTestBatch.startBatch(this.world);
        final TestSet testSet = new TestSet();
        collection.forEach(testSet::add);
        testSet.addListener(new TestListener(){

            private void onFinished() {
                if (testSet.isDone()) {
                    gameTestBatch.finishBatch(TestRunner.this.world);
                    TestRunner.this.runBatch(index + 1);
                }
            }

            @Override
            public void onStarted(GameTestState test) {
            }

            @Override
            public void onPassed(GameTestState test) {
                this.onFinished();
            }

            @Override
            public void onFailed(GameTestState test) {
                this.onFinished();
            }
        });
        collection.forEach(gameTest -> {
            BlockPos blockPos = (BlockPos)map.get(gameTest);
            TestUtil.startTest(gameTest, blockPos, this.testManager);
        });
    }

    private Map<GameTestState, BlockPos> method_29401(Collection<GameTestState> gameTests) {
        HashMap<GameTestState, BlockPos> map = Maps.newHashMap();
        int i = 0;
        Box box = new Box(this.reusablePos);
        for (GameTestState gameTestState : gameTests) {
            BlockPos blockPos = new BlockPos(this.reusablePos);
            StructureBlockBlockEntity structureBlockBlockEntity = StructureTestUtil.createStructure(gameTestState.getStructureName(), blockPos, gameTestState.getRotation(), 2, this.world, true);
            Box box2 = StructureTestUtil.getStructureBoundingBox(structureBlockBlockEntity);
            gameTestState.setPos(structureBlockBlockEntity.getPos());
            map.put(gameTestState, new BlockPos(this.reusablePos));
            box = box.union(box2);
            this.reusablePos.move((int)box2.getXLength() + 5, 0, 0);
            if (i++ % this.sizeZ != this.sizeZ - 1) continue;
            this.reusablePos.move(0, 0, (int)box.getZLength() + 6);
            this.reusablePos.setX(this.pos.getX());
            box = new Box(this.reusablePos);
        }
        return map;
    }
}
