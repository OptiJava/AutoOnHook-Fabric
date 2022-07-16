/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.passive;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.AttackGoal;
import net.minecraft.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.GoToBedAndSleepGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.UntamedActiveTargetGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.StructureFeature;
import org.jetbrains.annotations.Nullable;

/**
 * Meow.
 */
public class CatEntity
extends TameableEntity {
    public static final double CROUCHING_SPEED = 0.6;
    public static final double NORMAL_SPEED = 0.8;
    public static final double SPRINTING_SPEED = 1.33;
    private static final Ingredient TAMING_INGREDIENT = Ingredient.ofItems(Items.COD, Items.SALMON);
    private static final TrackedData<Integer> CAT_TYPE = DataTracker.registerData(CatEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> IN_SLEEPING_POSE = DataTracker.registerData(CatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> HEAD_DOWN = DataTracker.registerData(CatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> COLLAR_COLOR = DataTracker.registerData(CatEntity.class, TrackedDataHandlerRegistry.INTEGER);
    /**
     * The tabby cat type, whose value is {@value}.
     */
    public static final int TABBY_TYPE = 0;
    /**
     * The black cat type, whose value is {@value}.
     */
    public static final int BLACK_TYPE = 1;
    /**
     * The red cat type, whose value is {@value}.
     */
    public static final int RED_TYPE = 2;
    /**
     * The Siamese cat type, whose value is {@value}.
     */
    public static final int SIAMESE_TYPE = 3;
    /**
     * The British shorthair cat type, whose value is {@value}.
     */
    public static final int BRITISH_SHORTHAIR_TYPE = 4;
    /**
     * The calico cat type, whose value is {@value}.
     */
    public static final int CALICO_TYPE = 5;
    /**
     * The Persian cat type, whose value is {@value}.
     */
    public static final int PERSIAN_TYPE = 6;
    /**
     * The ragdoll cat type, whose value is {@value}.
     */
    public static final int RAGDOLL_TYPE = 7;
    /**
     * The white cat type, whose value is {@value}.
     */
    public static final int WHITE_TYPE = 8;
    /**
     * The Jellie cat type, whose value is {@value}.
     * <p>
     * This cat type was added in the 1.14 update after the community cat contest.
     * The winner was GoodTimesWithScar's cat named Jellie.
     */
    public static final int JELLIE_TYPE = 9;
    /**
     * The all black cat type, whose value is {@value}.
     */
    public static final int ALL_BLACK_TYPE = 10;
    private static final int field_30324 = 11;
    private static final int field_30325 = 10;
    public static final Map<Integer, Identifier> TEXTURES = Util.make(Maps.newHashMap(), map -> {
        map.put(0, new Identifier("textures/entity/cat/tabby.png"));
        map.put(1, new Identifier("textures/entity/cat/black.png"));
        map.put(2, new Identifier("textures/entity/cat/red.png"));
        map.put(3, new Identifier("textures/entity/cat/siamese.png"));
        map.put(4, new Identifier("textures/entity/cat/british_shorthair.png"));
        map.put(5, new Identifier("textures/entity/cat/calico.png"));
        map.put(6, new Identifier("textures/entity/cat/persian.png"));
        map.put(7, new Identifier("textures/entity/cat/ragdoll.png"));
        map.put(8, new Identifier("textures/entity/cat/white.png"));
        map.put(9, new Identifier("textures/entity/cat/jellie.png"));
        map.put(10, new Identifier("textures/entity/cat/all_black.png"));
    });
    private CatFleeGoal<PlayerEntity> fleeGoal;
    private net.minecraft.entity.ai.goal.TemptGoal temptGoal;
    private float sleepAnimation;
    private float prevSleepAnimation;
    private float tailCurlAnimation;
    private float prevTailCurlAnimation;
    private float headDownAnimation;
    private float prevHeadDownAnimation;

    public CatEntity(EntityType<? extends CatEntity> entityType, World world) {
        super((EntityType<? extends TameableEntity>)entityType, world);
    }

    public Identifier getTexture() {
        return TEXTURES.getOrDefault(this.getCatType(), TEXTURES.get(0));
    }

    @Override
    protected void initGoals() {
        this.temptGoal = new TemptGoal(this, 0.6, TAMING_INGREDIENT, true);
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(1, new SitGoal(this));
        this.goalSelector.add(2, new SleepWithOwnerGoal(this));
        this.goalSelector.add(3, this.temptGoal);
        this.goalSelector.add(5, new GoToBedAndSleepGoal(this, 1.1, 8));
        this.goalSelector.add(6, new FollowOwnerGoal(this, 1.0, 10.0f, 5.0f, false));
        this.goalSelector.add(7, new CatSitOnBlockGoal(this, 0.8));
        this.goalSelector.add(8, new PounceAtTargetGoal(this, 0.3f));
        this.goalSelector.add(9, new AttackGoal(this));
        this.goalSelector.add(10, new AnimalMateGoal(this, 0.8));
        this.goalSelector.add(11, new WanderAroundFarGoal((PathAwareEntity)this, 0.8, 1.0000001E-5f));
        this.goalSelector.add(12, new LookAtEntityGoal(this, PlayerEntity.class, 10.0f));
        this.targetSelector.add(1, new UntamedActiveTargetGoal<RabbitEntity>(this, RabbitEntity.class, false, null));
        this.targetSelector.add(1, new UntamedActiveTargetGoal<TurtleEntity>(this, TurtleEntity.class, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
    }

    public int getCatType() {
        return this.dataTracker.get(CAT_TYPE);
    }

    public void setCatType(int type) {
        if (type < 0 || type >= 11) {
            type = this.random.nextInt(10);
        }
        this.dataTracker.set(CAT_TYPE, type);
    }

    /**
     * Sets whether this cat is in a sleeping pose or not.
     * 
     * @param sleeping {@code true} if this cat is in a sleeping pose, otherwise {@code false}
     */
    public void setInSleepingPose(boolean sleeping) {
        this.dataTracker.set(IN_SLEEPING_POSE, sleeping);
    }

    /**
     * {@return whether this cat is in a sleeping pose}
     */
    public boolean isInSleepingPose() {
        return this.dataTracker.get(IN_SLEEPING_POSE);
    }

    public void setHeadDown(boolean headDown) {
        this.dataTracker.set(HEAD_DOWN, headDown);
    }

    public boolean isHeadDown() {
        return this.dataTracker.get(HEAD_DOWN);
    }

    public DyeColor getCollarColor() {
        return DyeColor.byId(this.dataTracker.get(COLLAR_COLOR));
    }

    public void setCollarColor(DyeColor color) {
        this.dataTracker.set(COLLAR_COLOR, color.getId());
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(CAT_TYPE, 1);
        this.dataTracker.startTracking(IN_SLEEPING_POSE, false);
        this.dataTracker.startTracking(HEAD_DOWN, false);
        this.dataTracker.startTracking(COLLAR_COLOR, DyeColor.RED.getId());
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("CatType", this.getCatType());
        nbt.putByte("CollarColor", (byte)this.getCollarColor().getId());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setCatType(nbt.getInt("CatType"));
        if (nbt.contains("CollarColor", 99)) {
            this.setCollarColor(DyeColor.byId(nbt.getInt("CollarColor")));
        }
    }

    @Override
    public void mobTick() {
        if (this.getMoveControl().isMoving()) {
            double d = this.getMoveControl().getSpeed();
            if (d == 0.6) {
                this.setPose(EntityPose.CROUCHING);
                this.setSprinting(false);
            } else if (d == 1.33) {
                this.setPose(EntityPose.STANDING);
                this.setSprinting(true);
            } else {
                this.setPose(EntityPose.STANDING);
                this.setSprinting(false);
            }
        } else {
            this.setPose(EntityPose.STANDING);
            this.setSprinting(false);
        }
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        if (this.isTamed()) {
            if (this.isInLove()) {
                return SoundEvents.ENTITY_CAT_PURR;
            }
            if (this.random.nextInt(4) == 0) {
                return SoundEvents.ENTITY_CAT_PURREOW;
            }
            return SoundEvents.ENTITY_CAT_AMBIENT;
        }
        return SoundEvents.ENTITY_CAT_STRAY_AMBIENT;
    }

    @Override
    public int getMinAmbientSoundDelay() {
        return 120;
    }

    public void hiss() {
        this.playSound(SoundEvents.ENTITY_CAT_HISS, this.getSoundVolume(), this.getSoundPitch());
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_CAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_CAT_DEATH;
    }

    public static DefaultAttributeContainer.Builder createCatAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3f).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0);
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void eat(PlayerEntity player, Hand hand, ItemStack stack) {
        if (this.isBreedingItem(stack)) {
            this.playSound(SoundEvents.ENTITY_CAT_EAT, 1.0f, 1.0f);
        }
        super.eat(player, hand, stack);
    }

    private float getAttackDamage() {
        return (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
    }

    @Override
    public boolean tryAttack(Entity target) {
        return target.damage(DamageSource.mob(this), this.getAttackDamage());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.temptGoal != null && this.temptGoal.isActive() && !this.isTamed() && this.age % 100 == 0) {
            this.playSound(SoundEvents.ENTITY_CAT_BEG_FOR_FOOD, 1.0f, 1.0f);
        }
        this.updateAnimations();
    }

    private void updateAnimations() {
        if ((this.isInSleepingPose() || this.isHeadDown()) && this.age % 5 == 0) {
            this.playSound(SoundEvents.ENTITY_CAT_PURR, 0.6f + 0.4f * (this.random.nextFloat() - this.random.nextFloat()), 1.0f);
        }
        this.updateSleepAnimation();
        this.updateHeadDownAnimation();
    }

    private void updateSleepAnimation() {
        this.prevSleepAnimation = this.sleepAnimation;
        this.prevTailCurlAnimation = this.tailCurlAnimation;
        if (this.isInSleepingPose()) {
            this.sleepAnimation = Math.min(1.0f, this.sleepAnimation + 0.15f);
            this.tailCurlAnimation = Math.min(1.0f, this.tailCurlAnimation + 0.08f);
        } else {
            this.sleepAnimation = Math.max(0.0f, this.sleepAnimation - 0.22f);
            this.tailCurlAnimation = Math.max(0.0f, this.tailCurlAnimation - 0.13f);
        }
    }

    private void updateHeadDownAnimation() {
        this.prevHeadDownAnimation = this.headDownAnimation;
        this.headDownAnimation = this.isHeadDown() ? Math.min(1.0f, this.headDownAnimation + 0.1f) : Math.max(0.0f, this.headDownAnimation - 0.13f);
    }

    public float getSleepAnimation(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevSleepAnimation, this.sleepAnimation);
    }

    public float getTailCurlAnimation(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevTailCurlAnimation, this.tailCurlAnimation);
    }

    public float getHeadDownAnimation(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevHeadDownAnimation, this.headDownAnimation);
    }

    @Override
    public CatEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        CatEntity catEntity = EntityType.CAT.create(serverWorld);
        if (passiveEntity instanceof CatEntity) {
            if (this.random.nextBoolean()) {
                catEntity.setCatType(this.getCatType());
            } else {
                catEntity.setCatType(((CatEntity)passiveEntity).getCatType());
            }
            if (this.isTamed()) {
                catEntity.setOwnerUuid(this.getOwnerUuid());
                catEntity.setTamed(true);
                if (this.random.nextBoolean()) {
                    catEntity.setCollarColor(this.getCollarColor());
                } else {
                    catEntity.setCollarColor(((CatEntity)passiveEntity).getCollarColor());
                }
            }
        }
        return catEntity;
    }

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        if (!this.isTamed()) {
            return false;
        }
        if (!(other instanceof CatEntity)) {
            return false;
        }
        CatEntity catEntity = (CatEntity)other;
        return catEntity.isTamed() && super.canBreedWith(other);
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        entityData = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
        if (world.getMoonSize() > 0.9f) {
            this.setCatType(this.random.nextInt(11));
        } else {
            this.setCatType(this.random.nextInt(10));
        }
        ServerWorld world2 = world.toServerWorld();
        if (world2 instanceof ServerWorld && world2.getStructureAccessor().getStructureAt(this.getBlockPos(), true, StructureFeature.SWAMP_HUT).hasChildren()) {
            this.setCatType(ALL_BLACK_TYPE);
            this.setPersistent();
        }
        return entityData;
    }

    /*
     * Enabled aggressive block sorting
     */
    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        Enum dyeColor;
        ItemStack itemStack = player.getStackInHand(hand);
        Item item = itemStack.getItem();
        if (this.world.isClient) {
            if (this.isTamed() && this.isOwner(player)) {
                return ActionResult.SUCCESS;
            }
            if (this.isBreedingItem(itemStack) && (this.getHealth() < this.getMaxHealth() || !this.isTamed())) {
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        }
        if (this.isTamed()) {
            if (this.isOwner(player)) {
                if (item instanceof DyeItem) {
                    dyeColor = ((DyeItem)item).getColor();
                    if (dyeColor != this.getCollarColor()) {
                        this.setCollarColor((DyeColor)dyeColor);
                        if (!player.getAbilities().creativeMode) {
                            itemStack.decrement(1);
                        }
                        this.setPersistent();
                        return ActionResult.CONSUME;
                    }
                } else {
                    if (item.isFood() && this.isBreedingItem(itemStack) && this.getHealth() < this.getMaxHealth()) {
                        this.eat(player, hand, itemStack);
                        this.heal(item.getFoodComponent().getHunger());
                        return ActionResult.CONSUME;
                    }
                    ActionResult dyeColor2 = super.interactMob(player, hand);
                    if (!dyeColor2.isAccepted() || this.isBaby()) {
                        this.setSitting(!this.isSitting());
                    }
                    return dyeColor2;
                }
            }
        } else if (this.isBreedingItem(itemStack)) {
            this.eat(player, hand, itemStack);
            if (this.random.nextInt(3) == 0) {
                this.setOwner(player);
                this.setSitting(true);
                this.world.sendEntityStatus(this, (byte)7);
            } else {
                this.world.sendEntityStatus(this, (byte)6);
            }
            this.setPersistent();
            return ActionResult.CONSUME;
        }
        if (((ActionResult)(dyeColor = super.interactMob(player, hand))).isAccepted()) {
            this.setPersistent();
        }
        return dyeColor;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return TAMING_INGREDIENT.test(stack);
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.5f;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return !this.isTamed() && this.age > 2400;
    }

    @Override
    protected void onTamedChanged() {
        if (this.fleeGoal == null) {
            this.fleeGoal = new CatFleeGoal<PlayerEntity>(this, PlayerEntity.class, 16.0f, 0.8, 1.33);
        }
        this.goalSelector.remove(this.fleeGoal);
        if (!this.isTamed()) {
            this.goalSelector.add(4, this.fleeGoal);
        }
    }

    @Override
    public boolean bypassesSteppingEffects() {
        return this.getPose() == EntityPose.CROUCHING || super.bypassesSteppingEffects();
    }

    @Override
    public /* synthetic */ PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }

    static class TemptGoal
    extends net.minecraft.entity.ai.goal.TemptGoal {
        @Nullable
        private PlayerEntity player;
        private final CatEntity cat;

        public TemptGoal(CatEntity cat, double speed, Ingredient food, boolean canBeScared) {
            super(cat, speed, food, canBeScared);
            this.cat = cat;
        }

        @Override
        public void tick() {
            super.tick();
            if (this.player == null && this.mob.getRandom().nextInt(600) == 0) {
                this.player = this.closestPlayer;
            } else if (this.mob.getRandom().nextInt(500) == 0) {
                this.player = null;
            }
        }

        @Override
        protected boolean canBeScared() {
            if (this.player != null && this.player.equals(this.closestPlayer)) {
                return false;
            }
            return super.canBeScared();
        }

        @Override
        public boolean canStart() {
            return super.canStart() && !this.cat.isTamed();
        }
    }

    static class SleepWithOwnerGoal
    extends Goal {
        private final CatEntity cat;
        private PlayerEntity owner;
        private BlockPos bedPos;
        private int ticksOnBed;

        public SleepWithOwnerGoal(CatEntity cat) {
            this.cat = cat;
        }

        @Override
        public boolean canStart() {
            if (!this.cat.isTamed()) {
                return false;
            }
            if (this.cat.isSitting()) {
                return false;
            }
            LivingEntity livingEntity = this.cat.getOwner();
            if (livingEntity instanceof PlayerEntity) {
                this.owner = (PlayerEntity)livingEntity;
                if (!livingEntity.isSleeping()) {
                    return false;
                }
                if (this.cat.squaredDistanceTo(this.owner) > 100.0) {
                    return false;
                }
                BlockPos blockPos = this.owner.getBlockPos();
                BlockState blockState = this.cat.world.getBlockState(blockPos);
                if (blockState.isIn(BlockTags.BEDS)) {
                    this.bedPos = blockState.getOrEmpty(BedBlock.FACING).map(direction -> blockPos.offset(direction.getOpposite())).orElseGet(() -> new BlockPos(blockPos));
                    return !this.cannotSleep();
                }
            }
            return false;
        }

        private boolean cannotSleep() {
            List<CatEntity> list = this.cat.world.getNonSpectatingEntities(CatEntity.class, new Box(this.bedPos).expand(2.0));
            for (CatEntity catEntity : list) {
                if (catEntity == this.cat || !catEntity.isInSleepingPose() && !catEntity.isHeadDown()) continue;
                return true;
            }
            return false;
        }

        @Override
        public boolean shouldContinue() {
            return this.cat.isTamed() && !this.cat.isSitting() && this.owner != null && this.owner.isSleeping() && this.bedPos != null && !this.cannotSleep();
        }

        @Override
        public void start() {
            if (this.bedPos != null) {
                this.cat.setInSittingPose(false);
                this.cat.getNavigation().startMovingTo(this.bedPos.getX(), this.bedPos.getY(), this.bedPos.getZ(), 1.1f);
            }
        }

        @Override
        public void stop() {
            this.cat.setInSleepingPose(false);
            float f = this.cat.world.getSkyAngle(1.0f);
            if (this.owner.getSleepTimer() >= 100 && (double)f > 0.77 && (double)f < 0.8 && (double)this.cat.world.getRandom().nextFloat() < 0.7) {
                this.dropMorningGifts();
            }
            this.ticksOnBed = 0;
            this.cat.setHeadDown(false);
            this.cat.getNavigation().stop();
        }

        private void dropMorningGifts() {
            Random random = this.cat.getRandom();
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            mutable.set(this.cat.getBlockPos());
            this.cat.teleport(mutable.getX() + random.nextInt(11) - 5, mutable.getY() + random.nextInt(5) - 2, mutable.getZ() + random.nextInt(11) - 5, false);
            mutable.set(this.cat.getBlockPos());
            LootTable lootTable = this.cat.world.getServer().getLootManager().getTable(LootTables.CAT_MORNING_GIFT_GAMEPLAY);
            LootContext.Builder builder = new LootContext.Builder((ServerWorld)this.cat.world).parameter(LootContextParameters.ORIGIN, this.cat.getPos()).parameter(LootContextParameters.THIS_ENTITY, this.cat).random(random);
            List<ItemStack> list = lootTable.generateLoot(builder.build(LootContextTypes.GIFT));
            for (ItemStack itemStack : list) {
                this.cat.world.spawnEntity(new ItemEntity(this.cat.world, (double)mutable.getX() - (double)MathHelper.sin(this.cat.bodyYaw * ((float)Math.PI / 180)), mutable.getY(), (double)mutable.getZ() + (double)MathHelper.cos(this.cat.bodyYaw * ((float)Math.PI / 180)), itemStack));
            }
        }

        @Override
        public void tick() {
            if (this.owner != null && this.bedPos != null) {
                this.cat.setInSittingPose(false);
                this.cat.getNavigation().startMovingTo(this.bedPos.getX(), this.bedPos.getY(), this.bedPos.getZ(), 1.1f);
                if (this.cat.squaredDistanceTo(this.owner) < 2.5) {
                    ++this.ticksOnBed;
                    if (this.ticksOnBed > 16) {
                        this.cat.setInSleepingPose(true);
                        this.cat.setHeadDown(false);
                    } else {
                        this.cat.lookAtEntity(this.owner, 45.0f, 45.0f);
                        this.cat.setHeadDown(true);
                    }
                } else {
                    this.cat.setInSleepingPose(false);
                }
            }
        }
    }

    static class CatFleeGoal<T extends LivingEntity>
    extends FleeEntityGoal<T> {
        private final CatEntity cat;

        public CatFleeGoal(CatEntity cat, Class<T> fleeFromType, float distance, double slowSpeed, double fastSpeed) {
            super(cat, fleeFromType, distance, slowSpeed, fastSpeed, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR::test);
            this.cat = cat;
        }

        @Override
        public boolean canStart() {
            return !this.cat.isTamed() && super.canStart();
        }

        @Override
        public boolean shouldContinue() {
            return !this.cat.isTamed() && super.shouldContinue();
        }
    }
}

