/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.test;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.Bootstrap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.data.dev.NbtProvider;
import net.minecraft.data.validate.StructureValidatorProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class StructureTestUtil {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String field_33173 = "gameteststructures";
    public static String testStructuresDirectoryName = "gameteststructures";
    private static final int field_33174 = 4;

    public static BlockRotation getRotation(int steps) {
        switch (steps) {
            case 0: {
                return BlockRotation.NONE;
            }
            case 1: {
                return BlockRotation.CLOCKWISE_90;
            }
            case 2: {
                return BlockRotation.CLOCKWISE_180;
            }
            case 3: {
                return BlockRotation.COUNTERCLOCKWISE_90;
            }
        }
        throw new IllegalArgumentException("rotationSteps must be a value from 0-3. Got value " + steps);
    }

    public static int getRotationSteps(BlockRotation rotation) {
        switch (rotation) {
            case NONE: {
                return 0;
            }
            case CLOCKWISE_90: {
                return 1;
            }
            case CLOCKWISE_180: {
                return 2;
            }
            case COUNTERCLOCKWISE_90: {
                return 3;
            }
        }
        throw new IllegalArgumentException("Unknown rotation value, don't know how many steps it represents: " + rotation);
    }

    public static void main(String[] args) throws IOException {
        Bootstrap.initialize();
        Files.walk(Paths.get(testStructuresDirectoryName, new String[0]), new FileVisitOption[0]).filter(path -> path.toString().endsWith(".snbt")).forEach(path -> {
            try {
                String string = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                NbtCompound nbtCompound = NbtHelper.method_32260(string);
                NbtCompound nbtCompound2 = StructureValidatorProvider.update(path.toString(), nbtCompound);
                NbtProvider.writeTo(path, NbtHelper.toPrettyPrintedString(nbtCompound2));
            }
            catch (CommandSyntaxException | IOException string) {
                LOGGER.error("Something went wrong upgrading: {}", path, (Object)string);
            }
        });
    }

    public static Box getStructureBoundingBox(StructureBlockBlockEntity structureBlockEntity) {
        BlockPos blockPos = structureBlockEntity.getPos();
        BlockPos blockPos2 = blockPos.add(structureBlockEntity.getSize().add(-1, -1, -1));
        BlockPos blockPos3 = Structure.transformAround(blockPos2, BlockMirror.NONE, structureBlockEntity.getRotation(), blockPos);
        return new Box(blockPos, blockPos3);
    }

    public static BlockBox getStructureBlockBox(StructureBlockBlockEntity structureBlockEntity) {
        BlockPos blockPos = structureBlockEntity.getPos();
        BlockPos blockPos2 = blockPos.add(structureBlockEntity.getSize().add(-1, -1, -1));
        BlockPos blockPos3 = Structure.transformAround(blockPos2, BlockMirror.NONE, structureBlockEntity.getRotation(), blockPos);
        return BlockBox.create(blockPos, blockPos3);
    }

    public static void placeStartButton(BlockPos pos, BlockPos relativePos, BlockRotation rotation, ServerWorld world) {
        BlockPos blockPos = Structure.transformAround(pos.add(relativePos), BlockMirror.NONE, rotation, pos);
        world.setBlockState(blockPos, Blocks.COMMAND_BLOCK.getDefaultState());
        CommandBlockBlockEntity commandBlockBlockEntity = (CommandBlockBlockEntity)world.getBlockEntity(blockPos);
        commandBlockBlockEntity.getCommandExecutor().setCommand("test runthis");
        BlockPos blockPos2 = Structure.transformAround(blockPos.add(0, 0, -1), BlockMirror.NONE, rotation, blockPos);
        world.setBlockState(blockPos2, Blocks.STONE_BUTTON.getDefaultState().rotate(rotation));
    }

    public static void createTestArea(String structure, BlockPos pos, Vec3i relativePos, BlockRotation rotation, ServerWorld world) {
        BlockBox blockBox = StructureTestUtil.getStructureBlockBox(pos, relativePos, rotation);
        StructureTestUtil.clearArea(blockBox, pos.getY(), world);
        world.setBlockState(pos, Blocks.STRUCTURE_BLOCK.getDefaultState());
        StructureBlockBlockEntity structureBlockBlockEntity = (StructureBlockBlockEntity)world.getBlockEntity(pos);
        structureBlockBlockEntity.setIgnoreEntities(false);
        structureBlockBlockEntity.setStructureName(new Identifier(structure));
        structureBlockBlockEntity.setSize(relativePos);
        structureBlockBlockEntity.setMode(StructureBlockMode.SAVE);
        structureBlockBlockEntity.setShowBoundingBox(true);
    }

    public static StructureBlockBlockEntity createStructure(String structureName, BlockPos pos, BlockRotation rotation, int i, ServerWorld world, boolean bl) {
        BlockPos blockPos;
        Vec3i vec3i = StructureTestUtil.createStructure(structureName, world).getSize();
        BlockBox blockBox = StructureTestUtil.getStructureBlockBox(pos, vec3i, rotation);
        if (rotation == BlockRotation.NONE) {
            blockPos = pos;
        } else if (rotation == BlockRotation.CLOCKWISE_90) {
            blockPos = pos.add(vec3i.getZ() - 1, 0, 0);
        } else if (rotation == BlockRotation.CLOCKWISE_180) {
            blockPos = pos.add(vec3i.getX() - 1, 0, vec3i.getZ() - 1);
        } else if (rotation == BlockRotation.COUNTERCLOCKWISE_90) {
            blockPos = pos.add(0, 0, vec3i.getX() - 1);
        } else {
            throw new IllegalArgumentException("Invalid rotation: " + rotation);
        }
        StructureTestUtil.forceLoadNearbyChunks(pos, world);
        StructureTestUtil.clearArea(blockBox, pos.getY(), world);
        StructureBlockBlockEntity structureBlockBlockEntity = StructureTestUtil.placeStructure(structureName, blockPos, rotation, world, bl);
        ((ServerTickScheduler)world.getBlockTickScheduler()).getScheduledTicks(blockBox, true, false);
        world.clearUpdatesInArea(blockBox);
        return structureBlockBlockEntity;
    }

    private static void forceLoadNearbyChunks(BlockPos pos, ServerWorld world) {
        ChunkPos chunkPos = new ChunkPos(pos);
        for (int i = -1; i < 4; ++i) {
            for (int j = -1; j < 4; ++j) {
                int k = chunkPos.x + i;
                int l = chunkPos.z + j;
                world.setChunkForced(k, l, true);
            }
        }
    }

    public static void clearArea(BlockBox area, int altitude, ServerWorld world) {
        BlockBox blockBox = new BlockBox(area.getMinX() - 2, area.getMinY() - 3, area.getMinZ() - 3, area.getMaxX() + 3, area.getMaxY() + 20, area.getMaxZ() + 3);
        BlockPos.stream(blockBox).forEach(pos -> StructureTestUtil.resetBlock(altitude, pos, world));
        ((ServerTickScheduler)world.getBlockTickScheduler()).getScheduledTicks(blockBox, true, false);
        world.clearUpdatesInArea(blockBox);
        Box box = new Box(blockBox.getMinX(), blockBox.getMinY(), blockBox.getMinZ(), blockBox.getMaxX(), blockBox.getMaxY(), blockBox.getMaxZ());
        List<Entity> list = world.getEntitiesByClass(Entity.class, box, entity -> !(entity instanceof PlayerEntity));
        list.forEach(Entity::discard);
    }

    public static BlockBox getStructureBlockBox(BlockPos pos, Vec3i relativePos, BlockRotation rotation) {
        BlockPos blockPos = pos.add(relativePos).add(-1, -1, -1);
        BlockPos blockPos2 = Structure.transformAround(blockPos, BlockMirror.NONE, rotation, pos);
        BlockBox blockBox = BlockBox.create(pos, blockPos2);
        int i = Math.min(blockBox.getMinX(), blockBox.getMaxX());
        int j = Math.min(blockBox.getMinZ(), blockBox.getMaxZ());
        return blockBox.move(pos.getX() - i, 0, pos.getZ() - j);
    }

    public static Optional<BlockPos> findContainingStructureBlock(BlockPos pos, int radius, ServerWorld world) {
        return StructureTestUtil.findStructureBlocks(pos, radius, world).stream().filter(structureBlockPos -> StructureTestUtil.isInStructureBounds(structureBlockPos, pos, world)).findFirst();
    }

    @Nullable
    public static BlockPos findNearestStructureBlock(BlockPos pos2, int radius, ServerWorld world) {
        Comparator<BlockPos> comparator = Comparator.comparingInt(pos -> pos.getManhattanDistance(pos2));
        Collection<BlockPos> collection = StructureTestUtil.findStructureBlocks(pos2, radius, world);
        Optional<BlockPos> optional = collection.stream().min(comparator);
        return optional.orElse(null);
    }

    public static Collection<BlockPos> findStructureBlocks(BlockPos pos, int radius, ServerWorld world) {
        ArrayList<BlockPos> collection = Lists.newArrayList();
        Box box = new Box(pos);
        box = box.expand(radius);
        for (int i = (int)box.minX; i <= (int)box.maxX; ++i) {
            for (int j = (int)box.minY; j <= (int)box.maxY; ++j) {
                for (int k = (int)box.minZ; k <= (int)box.maxZ; ++k) {
                    BlockPos blockPos = new BlockPos(i, j, k);
                    BlockState blockState = world.getBlockState(blockPos);
                    if (!blockState.isOf(Blocks.STRUCTURE_BLOCK)) continue;
                    collection.add(blockPos);
                }
            }
        }
        return collection;
    }

    private static Structure createStructure(String structureId, ServerWorld world) {
        StructureManager structureManager = world.getStructureManager();
        Optional<Structure> optional = structureManager.getStructure(new Identifier(structureId));
        if (optional.isPresent()) {
            return optional.get();
        }
        String string = structureId + ".snbt";
        Path path = Paths.get(testStructuresDirectoryName, string);
        NbtCompound nbtCompound = StructureTestUtil.loadSnbt(path);
        if (nbtCompound == null) {
            throw new RuntimeException("Could not find structure file " + path + ", and the structure is not available in the world structures either.");
        }
        return structureManager.createStructure(nbtCompound);
    }

    private static StructureBlockBlockEntity placeStructure(String name, BlockPos pos, BlockRotation rotation, ServerWorld world, boolean bl) {
        world.setBlockState(pos, Blocks.STRUCTURE_BLOCK.getDefaultState());
        StructureBlockBlockEntity structureBlockBlockEntity = (StructureBlockBlockEntity)world.getBlockEntity(pos);
        structureBlockBlockEntity.setMode(StructureBlockMode.LOAD);
        structureBlockBlockEntity.setRotation(rotation);
        structureBlockBlockEntity.setIgnoreEntities(false);
        structureBlockBlockEntity.setStructureName(new Identifier(name));
        structureBlockBlockEntity.loadStructure(world, bl);
        if (structureBlockBlockEntity.getSize() != Vec3i.ZERO) {
            return structureBlockBlockEntity;
        }
        Structure structure = StructureTestUtil.createStructure(name, world);
        structureBlockBlockEntity.place(world, bl, structure);
        if (structureBlockBlockEntity.getSize() == Vec3i.ZERO) {
            throw new RuntimeException("Failed to load structure " + name);
        }
        return structureBlockBlockEntity;
    }

    @Nullable
    private static NbtCompound loadSnbt(Path path) {
        try {
            BufferedReader bufferedReader = Files.newBufferedReader(path);
            String string = IOUtils.toString(bufferedReader);
            return NbtHelper.method_32260(string);
        }
        catch (IOException bufferedReader) {
            return null;
        }
        catch (CommandSyntaxException bufferedReader) {
            throw new RuntimeException("Error while trying to load structure " + path, bufferedReader);
        }
    }

    private static void resetBlock(int altitude, BlockPos pos, ServerWorld world) {
        Object list;
        BlockState blockState = null;
        FlatChunkGeneratorConfig flatChunkGeneratorConfig = FlatChunkGeneratorConfig.getDefaultConfig(world.getRegistryManager().get(Registry.BIOME_KEY));
        if (flatChunkGeneratorConfig instanceof FlatChunkGeneratorConfig) {
            list = flatChunkGeneratorConfig.getLayerBlocks();
            int i = pos.getY() - world.getBottomY();
            if (pos.getY() < altitude && i > 0 && i <= list.size()) {
                blockState = list.get(i - 1);
            }
        } else if (pos.getY() == altitude - 1) {
            blockState = world.getBiome(pos).getGenerationSettings().getSurfaceConfig().getTopMaterial();
        } else if (pos.getY() < altitude - 1) {
            blockState = world.getBiome(pos).getGenerationSettings().getSurfaceConfig().getUnderMaterial();
        }
        if (blockState == null) {
            blockState = Blocks.AIR.getDefaultState();
        }
        list = new BlockStateArgument(blockState, Collections.emptySet(), null);
        ((BlockStateArgument)list).setBlockState(world, pos, Block.NOTIFY_LISTENERS);
        world.updateNeighbors(pos, blockState.getBlock());
    }

    private static boolean isInStructureBounds(BlockPos structureBlockPos, BlockPos pos, ServerWorld world) {
        StructureBlockBlockEntity structureBlockBlockEntity = (StructureBlockBlockEntity)world.getBlockEntity(structureBlockPos);
        Box box = StructureTestUtil.getStructureBoundingBox(structureBlockBlockEntity).expand(1.0);
        return box.contains(Vec3d.ofCenter(pos));
    }
}

