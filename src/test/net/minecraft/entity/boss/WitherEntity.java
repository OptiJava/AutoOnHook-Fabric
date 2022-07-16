/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.boss;

import com.google.common.collect.ImmutableList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.entity.feature.SkinOverlayOwner;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class WitherEntity
extends HostileEntity
implements SkinOverlayOwner,
RangedAttackMob {
    private static final TrackedData<Integer> TRACKED_ENTITY_ID_1 = DataTracker.registerData(WitherEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> TRACKED_ENTITY_ID_2 = DataTracker.registerData(WitherEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> TRACKED_ENTITY_ID_3 = DataTracker.registerData(WitherEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final List<TrackedData<Integer>> TRACKED_ENTITY_IDS = ImmutableList.of(TRACKED_ENTITY_ID_1, TRACKED_ENTITY_ID_2, TRACKED_ENTITY_ID_3);
    private static final TrackedData<Integer> INVUL_TIMER = DataTracker.registerData(WitherEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final int DEFAULT_INVUL_TIMER = 220;
    private final float[] sideHeadPitches = new float[2];
    private final float[] sideHeadYaws = new float[2];
    private final float[] prevSideHeadPitches = new float[2];
    private final float[] prevSideHeadYaws = new float[2];
    private final int[] skullCooldowns = new int[2];
    private final int[] chargedSkullCooldowns = new int[2];
    private int blockBreakingCooldown;
    private final ServerBossBar bossBar = (ServerBossBar)new ServerBossBar(this.getDisplayName(), BossBar.Color.PURPLE, BossBar.Style.PROGRESS).setDarkenSky(true);
    private static final Predicate<LivingEntity> CAN_ATTACK_PREDICATE = entity -> entity.getGroup() != EntityGroup.UNDEAD && entity.isMobOrPlayer();
    private static final TargetPredicate HEAD_TARGET_PREDICATE = TargetPredicate.createAttackable().setBaseMaxDistance(20.0).setPredicate(CAN_ATTACK_PREDICATE);

    public WitherEntity(EntityType<? extends WitherEntity> entityType, World world) {
        super((EntityType<? extends HostileEntity>)entityType, world);
        this.setHealth(this.getMaxHealth());
        this.getNavigation().setCanSwim(true);
        this.experiencePoints = 50;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new DescendAtHalfHealthGoal());
        this.goalSelector.add(2, new ProjectileAttackGoal(this, 1.0, 40, 20.0f));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this, new Class[0]));
        this.targetSelector.add(2, new ActiveTargetGoal<MobEntity>(this, MobEntity.class, 0, false, false, CAN_ATTACK_PREDICATE));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(TRACKED_ENTITY_ID_1, 0);
        this.dataTracker.startTracking(TRACKED_ENTITY_ID_2, 0);
        this.dataTracker.startTracking(TRACKED_ENTITY_ID_3, 0);
        this.dataTracker.startTracking(INVUL_TIMER, 0);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Invul", this.getInvulnerableTimer());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setInvulTimer(nbt.getInt("Invul"));
        if (this.hasCustomName()) {
            this.bossBar.setName(this.getDisplayName());
        }
    }

    @Override
    public void setCustomName(@Nullable Text name) {
        super.setCustomName(name);
        this.bossBar.setName(this.getDisplayName());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_WITHER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_WITHER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_WITHER_DEATH;
    }

    @Override
    public void tickMovement() {
        int d;
        int entity;
        Entity entity2;
        Vec3d vec3d = this.getVelocity().multiply(1.0, 0.6, 1.0);
        if (!this.world.isClient && this.getTrackedEntityId(0) > 0 && (entity2 = this.world.getEntityById(this.getTrackedEntityId(0))) != null) {
            double d2 = vec3d.y;
            if (this.getY() < entity2.getY() || !this.shouldRenderOverlay() && this.getY() < entity2.getY() + 5.0) {
                d2 = Math.max(0.0, d2);
                d2 += 0.3 - d2 * (double)0.6f;
            }
            vec3d = new Vec3d(vec3d.x, d2, vec3d.z);
            Vec3d vec3d2 = new Vec3d(entity2.getX() - this.getX(), 0.0, entity2.getZ() - this.getZ());
            if (vec3d2.horizontalLengthSquared() > 9.0) {
                Vec3d vec3d3 = vec3d2.normalize();
                vec3d = vec3d.add(vec3d3.x * 0.3 - vec3d.x * 0.6, 0.0, vec3d3.z * 0.3 - vec3d.z * 0.6);
            }
        }
        this.setVelocity(vec3d);
        if (vec3d.horizontalLengthSquared() > 0.05) {
            this.setYaw((float)MathHelper.atan2(vec3d.z, vec3d.x) * 57.295776f - 90.0f);
        }
        super.tickMovement();
        for (entity = 0; entity < 2; ++entity) {
            this.prevSideHeadYaws[entity] = this.sideHeadYaws[entity];
            this.prevSideHeadPitches[entity] = this.sideHeadPitches[entity];
        }
        for (entity = 0; entity < 2; ++entity) {
            int d3 = this.getTrackedEntityId(entity + 1);
            Entity entity22 = null;
            if (d3 > 0) {
                entity22 = this.world.getEntityById(d3);
            }
            if (entity22 != null) {
                double vec3d2 = this.getHeadX(entity + 1);
                double e = this.getHeadY(entity + 1);
                double f = this.getHeadZ(entity + 1);
                double g = entity22.getX() - vec3d2;
                double h = entity22.getEyeY() - e;
                double i = entity22.getZ() - f;
                double j = Math.sqrt(g * g + i * i);
                float k = (float)(MathHelper.atan2(i, g) * 57.2957763671875) - 90.0f;
                float l = (float)(-(MathHelper.atan2(h, j) * 57.2957763671875));
                this.sideHeadPitches[entity] = this.getNextAngle(this.sideHeadPitches[entity], l, 40.0f);
                this.sideHeadYaws[entity] = this.getNextAngle(this.sideHeadYaws[entity], k, 10.0f);
                continue;
            }
            this.sideHeadYaws[entity] = this.getNextAngle(this.sideHeadYaws[entity], this.bodyYaw, 10.0f);
        }
        entity = this.shouldRenderOverlay() ? 1 : 0;
        for (d = 0; d < 3; ++d) {
            double entity23 = this.getHeadX(d);
            double vec3d3 = this.getHeadY(d);
            double m = this.getHeadZ(d);
            this.world.addParticle(ParticleTypes.SMOKE, entity23 + this.random.nextGaussian() * (double)0.3f, vec3d3 + this.random.nextGaussian() * (double)0.3f, m + this.random.nextGaussian() * (double)0.3f, 0.0, 0.0, 0.0);
            if (entity == 0 || this.world.random.nextInt(4) != 0) continue;
            this.world.addParticle(ParticleTypes.ENTITY_EFFECT, entity23 + this.random.nextGaussian() * (double)0.3f, vec3d3 + this.random.nextGaussian() * (double)0.3f, m + this.random.nextGaussian() * (double)0.3f, 0.7f, 0.7f, 0.5);
        }
        if (this.getInvulnerableTimer() > 0) {
            for (d = 0; d < 3; ++d) {
                this.world.addParticle(ParticleTypes.ENTITY_EFFECT, this.getX() + this.random.nextGaussian(), this.getY() + (double)(this.random.nextFloat() * 3.3f), this.getZ() + this.random.nextGaussian(), 0.7f, 0.7f, 0.9f);
            }
        }
    }

    @Override
    protected void mobTick() {
        int destructionType;
        int i;
        if (this.getInvulnerableTimer() > 0) {
            int i2 = this.getInvulnerableTimer() - 1;
            this.bossBar.setPercent(1.0f - (float)i2 / 220.0f);
            if (i2 <= 0) {
                Explosion.DestructionType destructionType2 = this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) ? Explosion.DestructionType.DESTROY : Explosion.DestructionType.NONE;
                this.world.createExplosion(this, this.getX(), this.getEyeY(), this.getZ(), 7.0f, false, destructionType2);
                if (!this.isSilent()) {
                    this.world.syncGlobalEvent(WorldEvents.WITHER_SPAWNS, this.getBlockPos(), 0);
                }
            }
            this.setInvulTimer(i2);
            if (this.age % 10 == 0) {
                this.heal(10.0f);
            }
            return;
        }
        super.mobTick();
        for (i = 1; i < 3; ++i) {
            List<LivingEntity> f;
            if (this.age < this.skullCooldowns[i - 1]) continue;
            this.skullCooldowns[i - 1] = this.age + 10 + this.random.nextInt(10);
            if (this.world.getDifficulty() == Difficulty.NORMAL || this.world.getDifficulty() == Difficulty.HARD) {
                int n = i - 1;
                int n2 = this.chargedSkullCooldowns[n];
                this.chargedSkullCooldowns[n] = n2 + 1;
                if (n2 > 15) {
                    float destructionType3 = 10.0f;
                    float f2 = 5.0f;
                    double d = MathHelper.nextDouble(this.random, this.getX() - 10.0, this.getX() + 10.0);
                    double e = MathHelper.nextDouble(this.random, this.getY() - 5.0, this.getY() + 5.0);
                    double g = MathHelper.nextDouble(this.random, this.getZ() - 10.0, this.getZ() + 10.0);
                    this.shootSkullAt(i + 1, d, e, g, true);
                    this.chargedSkullCooldowns[i - 1] = 0;
                }
            }
            if ((destructionType = this.getTrackedEntityId(i)) > 0) {
                f = (LivingEntity)this.world.getEntityById(destructionType);
                if (f == null || !this.canTarget((LivingEntity)((Object)f)) || this.squaredDistanceTo((Entity)((Object)f)) > 900.0 || !this.canSee((Entity)((Object)f))) {
                    this.setTrackedEntityId(i, 0);
                    continue;
                }
                this.shootSkullAt(i + 1, (LivingEntity)((Object)f));
                this.skullCooldowns[i - 1] = this.age + 40 + this.random.nextInt(20);
                this.chargedSkullCooldowns[i - 1] = 0;
                continue;
            }
            f = this.world.getTargets(LivingEntity.class, HEAD_TARGET_PREDICATE, this, this.getBoundingBox().expand(20.0, 8.0, 20.0));
            if (f.isEmpty()) continue;
            LivingEntity d = (LivingEntity)f.get(this.random.nextInt(f.size()));
            this.setTrackedEntityId(i, d.getId());
        }
        if (this.getTarget() != null) {
            this.setTrackedEntityId(0, this.getTarget().getId());
        } else {
            this.setTrackedEntityId(0, 0);
        }
        if (this.blockBreakingCooldown > 0) {
            --this.blockBreakingCooldown;
            if (this.blockBreakingCooldown == 0 && this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                i = MathHelper.floor(this.getY());
                destructionType = MathHelper.floor(this.getX());
                int f = MathHelper.floor(this.getZ());
                boolean d = false;
                for (int j = -1; j <= 1; ++j) {
                    for (int e = -1; e <= 1; ++e) {
                        for (int k = 0; k <= 3; ++k) {
                            int g = destructionType + j;
                            int l = i + k;
                            int m = f + e;
                            BlockPos blockPos = new BlockPos(g, l, m);
                            BlockState blockState = this.world.getBlockState(blockPos);
                            if (!WitherEntity.canDestroy(blockState)) continue;
                            d = this.world.breakBlock(blockPos, true, this) || d;
                        }
                    }
                }
                if (d) {
                    this.world.syncWorldEvent(null, WorldEvents.WITHER_BREAKS_BLOCK, this.getBlockPos(), 0);
                }
            }
        }
        if (this.age % 20 == 0) {
            this.heal(1.0f);
        }
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
    }

    public static boolean canDestroy(BlockState block) {
        return !block.isAir() && !block.isIn(BlockTags.WITHER_IMMUNE);
    }

    public void onSummoned() {
        this.setInvulTimer(220);
        this.bossBar.setPercent(0.0f);
        this.setHealth(this.getMaxHealth() / 3.0f);
    }

    @Override
    public void slowMovement(BlockState state, Vec3d multiplier) {
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }

    private double getHeadX(int headIndex) {
        if (headIndex <= 0) {
            return this.getX();
        }
        float f = (this.bodyYaw + (float)(180 * (headIndex - 1))) * ((float)Math.PI / 180);
        float g = MathHelper.cos(f);
        return this.getX() + (double)g * 1.3;
    }

    private double getHeadY(int headIndex) {
        if (headIndex <= 0) {
            return this.getY() + 3.0;
        }
        return this.getY() + 2.2;
    }

    private double getHeadZ(int headIndex) {
        if (headIndex <= 0) {
            return this.getZ();
        }
        float f = (this.bodyYaw + (float)(180 * (headIndex - 1))) * ((float)Math.PI / 180);
        float g = MathHelper.sin(f);
        return this.getZ() + (double)g * 1.3;
    }

    private float getNextAngle(float prevAngle, float desiredAngle, float maxDifference) {
        float f = MathHelper.wrapDegrees(desiredAngle - prevAngle);
        if (f > maxDifference) {
            f = maxDifference;
        }
        if (f < -maxDifference) {
            f = -maxDifference;
        }
        return prevAngle + f;
    }

    private void shootSkullAt(int headIndex, LivingEntity target) {
        this.shootSkullAt(headIndex, target.getX(), target.getY() + (double)target.getStandingEyeHeight() * 0.5, target.getZ(), headIndex == 0 && this.random.nextFloat() < 0.001f);
    }

    private void shootSkullAt(int headIndex, double targetX, double targetY, double targetZ, boolean charged) {
        if (!this.isSilent()) {
            this.world.syncWorldEvent(null, WorldEvents.WITHER_SHOOTS, this.getBlockPos(), 0);
        }
        double d = this.getHeadX(headIndex);
        double e = this.getHeadY(headIndex);
        double f = this.getHeadZ(headIndex);
        double g = targetX - d;
        double h = targetY - e;
        double i = targetZ - f;
        WitherSkullEntity witherSkullEntity = new WitherSkullEntity(this.world, this, g, h, i);
        witherSkullEntity.setOwner(this);
        if (charged) {
            witherSkullEntity.setCharged(true);
        }
        witherSkullEntity.setPos(d, e, f);
        this.world.spawnEntity(witherSkullEntity);
    }

    @Override
    public void attack(LivingEntity target, float pullProgress) {
        this.shootSkullAt(0, target);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        Entity entity;
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (source == DamageSource.DROWN || source.getAttacker() instanceof WitherEntity) {
            return false;
        }
        if (this.getInvulnerableTimer() > 0 && source != DamageSource.OUT_OF_WORLD) {
            return false;
        }
        if (this.shouldRenderOverlay() && (entity = source.getSource()) instanceof PersistentProjectileEntity) {
            return false;
        }
        entity = source.getAttacker();
        if (entity != null && !(entity instanceof PlayerEntity) && entity instanceof LivingEntity && ((LivingEntity)entity).getGroup() == this.getGroup()) {
            return false;
        }
        if (this.blockBreakingCooldown <= 0) {
            this.blockBreakingCooldown = 20;
        }
        int i = 0;
        while (i < this.chargedSkullCooldowns.length) {
            int n = i++;
            this.chargedSkullCooldowns[n] = this.chargedSkullCooldowns[n] + 3;
        }
        return super.damage(source, amount);
    }

    @Override
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        super.dropEquipment(source, lootingMultiplier, allowDrops);
        ItemEntity itemEntity = this.dropItem(Items.NETHER_STAR);
        if (itemEntity != null) {
            itemEntity.setCovetedItem();
        }
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == Difficulty.PEACEFUL && this.isDisallowedInPeaceful()) {
            this.discard();
            return;
        }
        this.despawnCounter = 0;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public boolean addStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
        return false;
    }

    public static DefaultAttributeContainer.Builder createWitherAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 300.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.6f).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0).add(EntityAttributes.GENERIC_ARMOR, 4.0);
    }

    public float getHeadYaw(int headIndex) {
        return this.sideHeadYaws[headIndex];
    }

    public float getHeadPitch(int headIndex) {
        return this.sideHeadPitches[headIndex];
    }

    public int getInvulnerableTimer() {
        return this.dataTracker.get(INVUL_TIMER);
    }

    public void setInvulTimer(int ticks) {
        this.dataTracker.set(INVUL_TIMER, ticks);
    }

    public int getTrackedEntityId(int headIndex) {
        return this.dataTracker.get(TRACKED_ENTITY_IDS.get(headIndex));
    }

    public void setTrackedEntityId(int headIndex, int id) {
        this.dataTracker.set(TRACKED_ENTITY_IDS.get(headIndex), id);
    }

    @Override
    public boolean shouldRenderOverlay() {
        return this.getHealth() <= this.getMaxHealth() / 2.0f;
    }

    @Override
    public EntityGroup getGroup() {
        return EntityGroup.UNDEAD;
    }

    @Override
    protected boolean canStartRiding(Entity entity) {
        return false;
    }

    @Override
    public boolean canUsePortals() {
        return false;
    }

    @Override
    public boolean canHaveStatusEffect(StatusEffectInstance effect) {
        if (effect.getEffectType() == StatusEffects.WITHER) {
            return false;
        }
        return super.canHaveStatusEffect(effect);
    }

    class DescendAtHalfHealthGoal
    extends Goal {
        public DescendAtHalfHealthGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.JUMP, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return WitherEntity.this.getInvulnerableTimer() > 0;
        }
    }
}

