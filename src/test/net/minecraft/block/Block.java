/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.block;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SideShapeType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a block that can be placed in a world.
 * 
 * <p>There is exactly one instance for every type of block. Every stone
 * block for example in a world shares the same block instance. Each block
 * instance is registered under {@link net.minecraft.util.registry.Registry#BLOCK}.
 * See {@link Blocks} for examples of block instances.
 * 
 * <p>An item corresponding to a block is not automatically created. You
 * may create your own {@link net.minecraft.item.BlockItem} and register it
 * under {@link net.minecraft.util.registry.Registry#ITEM}.
 * 
 * <p>The translation key for the block name is determined by {@link
 * #getTranslationKey}.
 * 
 * @see <a href="https://minecraft.fandom.com/wiki/Model">Model - Official Minecraft Wiki</a>
 */
public class Block
extends AbstractBlock
implements ItemConvertible {
    protected static final Logger LOGGER = LogManager.getLogger();
    public static final IdList<BlockState> STATE_IDS = new IdList();
    private static final LoadingCache<VoxelShape, Boolean> FULL_CUBE_SHAPE_CACHE = CacheBuilder.newBuilder().maximumSize(512L).weakKeys().build(new CacheLoader<VoxelShape, Boolean>(){

        @Override
        public Boolean load(VoxelShape voxelShape) {
            return !VoxelShapes.matchesAnywhere(VoxelShapes.fullCube(), voxelShape, BooleanBiFunction.NOT_SAME);
        }

        @Override
        public /* synthetic */ Object load(Object shape) throws Exception {
            return this.load((VoxelShape)shape);
        }
    });
    /**
     * Sends a neighbor update event to surrounding blocks.
     */
    public static final int NOTIFY_NEIGHBORS = 1;
    /**
     * Notifies listeners and clients who need to react when the block changes.
     */
    public static final int NOTIFY_LISTENERS = 2;
    /**
     * Used in conjunction with {@link #NOTIFY_LISTENERS} to suppress the render pass on clients.
     */
    public static final int NO_REDRAW = 4;
    /**
     * Forces a synchronous redraw on clients.
     */
    public static final int REDRAW_ON_MAIN_THREAD = 8;
    /**
     * Bypass virtual block state changes and forces the passed state to be stored as-is.
     */
    public static final int FORCE_STATE = 16;
    /**
     * Prevents the previous block (container) from dropping items when destroyed.
     */
    public static final int SKIP_DROPS = 32;
    /**
     * Signals that the current block is being moved to a different location, usually because of a piston.
     */
    public static final int MOVED = 64;
    /**
     * Signals that lighting updates should be skipped.
     */
    public static final int SKIP_LIGHTING_UPDATES = 128;
    public static final int field_31035 = 4;
    /**
     * The default setBlockState behavior. Same as {@code NOTIFY_NEIGHBORS | NOTIFY_LISTENERS}.
     */
    public static final int NOTIFY_ALL = 3;
    public static final int field_31022 = 11;
    public static final float field_31023 = -1.0f;
    public static final float field_31024 = 0.0f;
    public static final int field_31025 = 512;
    protected final StateManager<Block, BlockState> stateManager;
    private BlockState defaultState;
    @Nullable
    private String translationKey;
    @Nullable
    private Item cachedItem;
    private static final int field_31026 = 2048;
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<NeighborGroup>> FACE_CULL_MAP = ThreadLocal.withInitial(() -> {
        Object2ByteLinkedOpenHashMap<NeighborGroup> object2ByteLinkedOpenHashMap = new Object2ByteLinkedOpenHashMap<NeighborGroup>(2048, 0.25f){

            @Override
            protected void rehash(int i) {
            }
        };
        object2ByteLinkedOpenHashMap.defaultReturnValue((byte)127);
        return object2ByteLinkedOpenHashMap;
    });

    public static int getRawIdFromState(@Nullable BlockState state) {
        if (state == null) {
            return 0;
        }
        int i = STATE_IDS.getRawId(state);
        return i == -1 ? 0 : i;
    }

    public static BlockState getStateFromRawId(int stateId) {
        BlockState blockState = STATE_IDS.get(stateId);
        return blockState == null ? Blocks.AIR.getDefaultState() : blockState;
    }

    public static Block getBlockFromItem(@Nullable Item item) {
        if (item instanceof BlockItem) {
            return ((BlockItem)item).getBlock();
        }
        return Blocks.AIR;
    }

    public static BlockState pushEntitiesUpBeforeBlockChange(BlockState from, BlockState to, World world, BlockPos pos) {
        VoxelShape voxelShape = VoxelShapes.combine(from.getCollisionShape(world, pos), to.getCollisionShape(world, pos), BooleanBiFunction.ONLY_SECOND).offset(pos.getX(), pos.getY(), pos.getZ());
        if (voxelShape.isEmpty()) {
            return to;
        }
        List<Entity> list = world.getOtherEntities(null, voxelShape.getBoundingBox());
        for (Entity entity : list) {
            double d = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, entity.getBoundingBox().offset(0.0, 1.0, 0.0), Stream.of(voxelShape), -1.0);
            entity.requestTeleport(entity.getX(), entity.getY() + 1.0 + d, entity.getZ());
        }
        return to;
    }

    public static VoxelShape createCuboidShape(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return VoxelShapes.cuboid(minX / 16.0, minY / 16.0, minZ / 16.0, maxX / 16.0, maxY / 16.0, maxZ / 16.0);
    }

    public static BlockState postProcessState(BlockState state, WorldAccess world, BlockPos pos) {
        BlockState blockState = state;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (Direction direction : DIRECTIONS) {
            mutable.set((Vec3i)pos, direction);
            blockState = blockState.getStateForNeighborUpdate(direction, world.getBlockState(mutable), world, pos, mutable);
        }
        return blockState;
    }

    /**
     * Replaces the {@code state} with the {@code newState} at the {@code pos}.
     * 
     * <p>If the two state objects are identical, this method does nothing.
     * 
     * <p>If the new state {@linkplain BlockState#isAir() is air},
     * breaks the block at the position instead.
     * 
     * @param state the existing block state
     * @param newState the new block state
     * @param world the world
     * @param pos the position of the replaced block state
     * @param flags the bitwise flags for {@link net.minecraft.world.ModifiableWorld#setBlockState(BlockPos, BlockState, int, int)}
     */
    public static void replace(BlockState state, BlockState newState, WorldAccess world, BlockPos pos, int flags) {
        Block.replace(state, newState, world, pos, flags, 512);
    }

    /**
     * Replaces the {@code state} with the {@code newState} at the {@code pos}.
     * 
     * <p>If the two state objects are identical, this method does nothing.
     * 
     * <p>If the new state {@linkplain BlockState#isAir() is air},
     * breaks the block at the position instead.
     * 
     * @param state the existing block state
     * @param newState the new block state
     * @param world the world
     * @param pos the position of the replaced block state
     * @param flags the bitwise flags for {@link net.minecraft.world.ModifiableWorld#setBlockState(BlockPos, BlockState, int, int)}
     * @param maxUpdateDepth the limit for the cascading block updates
     */
    public static void replace(BlockState state, BlockState newState, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
        if (newState != state) {
            if (newState.isAir()) {
                if (!world.isClient()) {
                    world.breakBlock(pos, (flags & SKIP_DROPS) == 0, null, maxUpdateDepth);
                }
            } else {
                world.setBlockState(pos, newState, flags & ~SKIP_DROPS, maxUpdateDepth);
            }
        }
    }

    public Block(AbstractBlock.Settings settings) {
        super(settings);
        String string;
        StateManager.Builder<Block, BlockState> builder = new StateManager.Builder<Block, BlockState>(this);
        this.appendProperties(builder);
        this.stateManager = builder.build(Block::getDefaultState, BlockState::new);
        this.setDefaultState(this.stateManager.getDefaultState());
        if (SharedConstants.isDevelopment && !(string = this.getClass().getSimpleName()).endsWith("Block")) {
            LOGGER.error("Block classes should end with Block and {} doesn't.", (Object)string);
        }
    }

    public static boolean cannotConnect(BlockState state) {
        return state.getBlock() instanceof LeavesBlock || state.isOf(Blocks.BARRIER) || state.isOf(Blocks.CARVED_PUMPKIN) || state.isOf(Blocks.JACK_O_LANTERN) || state.isOf(Blocks.MELON) || state.isOf(Blocks.PUMPKIN) || state.isIn(BlockTags.SHULKER_BOXES);
    }

    public boolean hasRandomTicks(BlockState state) {
        return this.randomTicks;
    }

    public static boolean shouldDrawSide(BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos blockPos) {
        BlockState blockState = world.getBlockState(blockPos);
        if (state.isSideInvisible(blockState, side)) {
            return false;
        }
        if (blockState.isOpaque()) {
            NeighborGroup neighborGroup = new NeighborGroup(state, blockState, side);
            Object2ByteLinkedOpenHashMap<NeighborGroup> object2ByteLinkedOpenHashMap = FACE_CULL_MAP.get();
            byte b = object2ByteLinkedOpenHashMap.getAndMoveToFirst(neighborGroup);
            if (b != 127) {
                return b != 0;
            }
            VoxelShape voxelShape = state.getCullingFace(world, pos, side);
            if (voxelShape.isEmpty()) {
                return true;
            }
            VoxelShape voxelShape2 = blockState.getCullingFace(world, blockPos, side.getOpposite());
            boolean bl = VoxelShapes.matchesAnywhere(voxelShape, voxelShape2, BooleanBiFunction.ONLY_FIRST);
            if (object2ByteLinkedOpenHashMap.size() == 2048) {
                object2ByteLinkedOpenHashMap.removeLastByte();
            }
            object2ByteLinkedOpenHashMap.putAndMoveToFirst(neighborGroup, (byte)(bl ? 1 : 0));
            return bl;
        }
        return true;
    }

    public static boolean hasTopRim(BlockView world, BlockPos pos) {
        return world.getBlockState(pos).isSideSolid(world, pos, Direction.UP, SideShapeType.RIGID);
    }

    public static boolean sideCoversSmallSquare(WorldView world, BlockPos pos, Direction side) {
        BlockState blockState = world.getBlockState(pos);
        if (side == Direction.DOWN && blockState.isIn(BlockTags.UNSTABLE_BOTTOM_CENTER)) {
            return false;
        }
        return blockState.isSideSolid(world, pos, side, SideShapeType.CENTER);
    }

    public static boolean isFaceFullSquare(VoxelShape shape, Direction side) {
        VoxelShape voxelShape = shape.getFace(side);
        return Block.isShapeFullCube(voxelShape);
    }

    public static boolean isShapeFullCube(VoxelShape shape) {
        return FULL_CUBE_SHAPE_CACHE.getUnchecked(shape);
    }

    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return !Block.isShapeFullCube(state.getOutlineShape(world, pos)) && state.getFluidState().isEmpty();
    }

    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
    }

    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
    }

    public static List<ItemStack> getDroppedStacks(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity) {
        LootContext.Builder builder = new LootContext.Builder(world).random(world.random).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos)).parameter(LootContextParameters.TOOL, ItemStack.EMPTY).optionalParameter(LootContextParameters.BLOCK_ENTITY, blockEntity);
        return state.getDroppedStacks(builder);
    }

    public static List<ItemStack> getDroppedStacks(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack stack) {
        LootContext.Builder builder = new LootContext.Builder(world).random(world.random).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos)).parameter(LootContextParameters.TOOL, stack).optionalParameter(LootContextParameters.THIS_ENTITY, entity).optionalParameter(LootContextParameters.BLOCK_ENTITY, blockEntity);
        return state.getDroppedStacks(builder);
    }

    public static void dropStacks(BlockState state, LootContext.Builder lootContext) {
        ServerWorld serverWorld = lootContext.getWorld();
        BlockPos blockPos = new BlockPos(lootContext.get(LootContextParameters.ORIGIN));
        state.getDroppedStacks(lootContext).forEach(stack -> Block.dropStack((World)serverWorld, blockPos, stack));
        state.onStacksDropped(serverWorld, blockPos, ItemStack.EMPTY);
    }

    public static void dropStacks(BlockState state, World world, BlockPos pos) {
        if (world instanceof ServerWorld) {
            Block.getDroppedStacks(state, (ServerWorld)world, pos, null).forEach(stack -> Block.dropStack(world, pos, stack));
            state.onStacksDropped((ServerWorld)world, pos, ItemStack.EMPTY);
        }
    }

    public static void dropStacks(BlockState state, WorldAccess world, BlockPos pos, @Nullable BlockEntity blockEntity) {
        if (world instanceof ServerWorld) {
            Block.getDroppedStacks(state, (ServerWorld)world, pos, blockEntity).forEach(stack -> Block.dropStack((World)((ServerWorld)world), pos, stack));
            state.onStacksDropped((ServerWorld)world, pos, ItemStack.EMPTY);
        }
    }

    public static void dropStacks(BlockState state, World world, BlockPos pos, @Nullable BlockEntity blockEntity, Entity entity, ItemStack stack) {
        if (world instanceof ServerWorld) {
            Block.getDroppedStacks(state, (ServerWorld)world, pos, blockEntity, entity, stack).forEach(itemStack -> Block.dropStack(world, pos, itemStack));
            state.onStacksDropped((ServerWorld)world, pos, stack);
        }
    }

    public static void dropStack(World world, BlockPos pos, ItemStack stack) {
        float f = EntityType.ITEM.getHeight() / 2.0f;
        double d = (double)((float)pos.getX() + 0.5f) + MathHelper.nextDouble(world.random, -0.25, 0.25);
        double e = (double)((float)pos.getY() + 0.5f) + MathHelper.nextDouble(world.random, -0.25, 0.25) - (double)f;
        double g = (double)((float)pos.getZ() + 0.5f) + MathHelper.nextDouble(world.random, -0.25, 0.25);
        Block.dropStack(world, () -> new ItemEntity(world, d, e, g, stack), stack);
    }

    public static void dropStack(World world, BlockPos pos, Direction direction, ItemStack stack) {
        int i = direction.getOffsetX();
        int j = direction.getOffsetY();
        int k = direction.getOffsetZ();
        float f = EntityType.ITEM.getWidth() / 2.0f;
        float g = EntityType.ITEM.getHeight() / 2.0f;
        double d = (double)((float)pos.getX() + 0.5f) + (i == 0 ? MathHelper.nextDouble(world.random, -0.25, 0.25) : (double)((float)i * (0.5f + f)));
        double e = (double)((float)pos.getY() + 0.5f) + (j == 0 ? MathHelper.nextDouble(world.random, -0.25, 0.25) : (double)((float)j * (0.5f + g))) - (double)g;
        double h = (double)((float)pos.getZ() + 0.5f) + (k == 0 ? MathHelper.nextDouble(world.random, -0.25, 0.25) : (double)((float)k * (0.5f + f)));
        double l = i == 0 ? MathHelper.nextDouble(world.random, -0.1, 0.1) : (double)i * 0.1;
        double m = j == 0 ? MathHelper.nextDouble(world.random, 0.0, 0.1) : (double)j * 0.1 + 0.1;
        double n = k == 0 ? MathHelper.nextDouble(world.random, -0.1, 0.1) : (double)k * 0.1;
        Block.dropStack(world, () -> new ItemEntity(world, d, e, h, stack, l, m, n), stack);
    }

    private static void dropStack(World world, Supplier<ItemEntity> itemEntitySupplier, ItemStack stack) {
        if (world.isClient || stack.isEmpty() || !world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
            return;
        }
        ItemEntity itemEntity = itemEntitySupplier.get();
        itemEntity.setToDefaultPickupDelay();
        world.spawnEntity(itemEntity);
    }

    protected void dropExperience(ServerWorld world, BlockPos pos, int size) {
        if (world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
            ExperienceOrbEntity.spawn(world, Vec3d.ofCenter(pos), size);
        }
    }

    public float getBlastResistance() {
        return this.resistance;
    }

    /**
     * Called when this block is destroyed by an explosion.
     */
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
    }

    /**
     * Called when an entity steps on this block.
     */
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState();
    }

    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
        player.incrementStat(Stats.MINED.getOrCreateStat(this));
        player.addExhaustion(0.005f);
        Block.dropStacks(state, world, pos, blockEntity, player, stack);
    }

    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
    }

    public boolean canMobSpawnInside() {
        return !this.material.isSolid() && !this.material.isLiquid();
    }

    public MutableText getName() {
        return new TranslatableText(this.getTranslationKey());
    }

    /**
     * {@return the translation key for the name of this block}
     * 
     * <p>By default, it returns {@code block.namespace.path} where {@code
     * namespace} and {@code path} are of the identifier used for registering
     * this block, but {@code /} in {@code path} is replaced with {@code .}.
     * If the block is not registered, it returns {@code block.unregistered_sadface}.
     */
    public String getTranslationKey() {
        if (this.translationKey == null) {
            this.translationKey = Util.createTranslationKey("block", Registry.BLOCK.getId(this));
        }
        return this.translationKey;
    }

    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.handleFallDamage(fallDistance, 1.0f, DamageSource.FALL);
    }

    public void onEntityLand(BlockView world, Entity entity) {
        entity.setVelocity(entity.getVelocity().multiply(1.0, 0.0, 1.0));
    }

    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return new ItemStack(this);
    }

    /**
     * Appends the stacks of this block shown in the item group to the list.
     * 
     * @see net.minecraft.item.BlockItem#appendStacks(ItemGroup, DefaultedList)
     */
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        stacks.add(new ItemStack(this));
    }

    public float getSlipperiness() {
        return this.slipperiness;
    }

    public float getVelocityMultiplier() {
        return this.velocityMultiplier;
    }

    public float getJumpVelocityMultiplier() {
        return this.jumpVelocityMultiplier;
    }

    protected void spawnBreakParticles(World world, PlayerEntity player, BlockPos pos, BlockState state) {
        world.syncWorldEvent(player, WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
    }

    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        this.spawnBreakParticles(world, player, pos, state);
        if (state.isIn(BlockTags.GUARDED_BY_PIGLINS)) {
            PiglinBrain.onGuardedBlockInteracted(player, false);
        }
        world.emitGameEvent((Entity)player, GameEvent.BLOCK_DESTROY, pos);
    }

    public void precipitationTick(BlockState state, World world, BlockPos pos, Biome.Precipitation precipitation) {
    }

    public boolean shouldDropItemsOnExplosion(Explosion explosion) {
        return true;
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
    }

    public StateManager<Block, BlockState> getStateManager() {
        return this.stateManager;
    }

    protected final void setDefaultState(BlockState state) {
        this.defaultState = state;
    }

    public final BlockState getDefaultState() {
        return this.defaultState;
    }

    /**
     * Gets a block state with all properties that both this block and the source block state have.
     */
    public final BlockState getStateWithProperties(BlockState state) {
        BlockState blockState = this.getDefaultState();
        for (Property<?> property : state.getBlock().getStateManager().getProperties()) {
            if (!blockState.contains(property)) continue;
            blockState = Block.copyProperty(state, blockState, property);
        }
        return blockState;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState source, BlockState target, Property<T> property) {
        return (BlockState)target.with(property, source.get(property));
    }

    public BlockSoundGroup getSoundGroup(BlockState state) {
        return this.soundGroup;
    }

    @Override
    public Item asItem() {
        if (this.cachedItem == null) {
            this.cachedItem = Item.fromBlock(this);
        }
        return this.cachedItem;
    }

    public boolean hasDynamicBounds() {
        return this.dynamicBounds;
    }

    public String toString() {
        return "Block{" + Registry.BLOCK.getId(this) + "}";
    }

    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
    }

    @Override
    protected Block asBlock() {
        return this;
    }

    protected ImmutableMap<BlockState, VoxelShape> getShapesForStates(Function<BlockState, VoxelShape> function) {
        return this.stateManager.getStates().stream().collect(ImmutableMap.toImmutableMap(Function.identity(), function));
    }

    public static final class NeighborGroup {
        private final BlockState self;
        private final BlockState other;
        private final Direction facing;

        public NeighborGroup(BlockState self, BlockState other, Direction facing) {
            this.self = self;
            this.other = other;
            this.facing = facing;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof NeighborGroup)) {
                return false;
            }
            NeighborGroup neighborGroup = (NeighborGroup)o;
            return this.self == neighborGroup.self && this.other == neighborGroup.other && this.facing == neighborGroup.facing;
        }

        public int hashCode() {
            int i = this.self.hashCode();
            i = 31 * i + this.other.hashCode();
            i = 31 * i + this.facing.hashCode();
            return i;
        }
    }
}
