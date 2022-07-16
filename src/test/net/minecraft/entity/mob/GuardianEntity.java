/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.mob;

import java.util.EnumSet;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.SwimNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class GuardianEntity
extends HostileEntity {
    protected static final int WARMUP_TIME = 80;
    private static final TrackedData<Boolean> SPIKES_RETRACTED = DataTracker.registerData(GuardianEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> BEAM_TARGET_ID = DataTracker.registerData(GuardianEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private float tailAngle;
    private float prevTailAngle;
    private float spikesExtensionRate;
    private float spikesExtension;
    private float prevSpikesExtension;
    private LivingEntity cachedBeamTarget;
    private int beamTicks;
    private boolean flopping;
    protected WanderAroundGoal wanderGoal;

    public GuardianEntity(EntityType<? extends GuardianEntity> entityType, World world) {
        super((EntityType<? extends HostileEntity>)entityType, world);
        this.experiencePoints = 10;
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0f);
        this.moveControl = new GuardianMoveControl(this);
        this.prevTailAngle = this.tailAngle = this.random.nextFloat();
    }

    @Override
    protected void initGoals() {
        GoToWalkTargetGoal goToWalkTargetGoal = new GoToWalkTargetGoal(this, 1.0);
        this.wanderGoal = new WanderAroundGoal(this, 1.0, 80);
        this.goalSelector.add(4, new FireBeamGoal(this));
        this.goalSelector.add(5, goToWalkTargetGoal);
        this.goalSelector.add(7, this.wanderGoal);
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(8, new LookAtEntityGoal(this, GuardianEntity.class, 12.0f, 0.01f));
        this.goalSelector.add(9, new LookAroundGoal(this));
        this.wanderGoal.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        goToWalkTargetGoal.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        this.targetSelector.add(1, new ActiveTargetGoal<LivingEntity>(this, LivingEntity.class, 10, true, false, new GuardianTargetPredicate(this)));
    }

    public static DefaultAttributeContainer.Builder createGuardianAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new SwimNavigation(this, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SPIKES_RETRACTED, false);
        this.dataTracker.startTracking(BEAM_TARGET_ID, 0);
    }

    @Override
    public boolean canBreatheInWater() {
        return true;
    }

    @Override
    public EntityGroup getGroup() {
        return EntityGroup.AQUATIC;
    }

    public boolean areSpikesRetracted() {
        return this.dataTracker.get(SPIKES_RETRACTED);
    }

    void setSpikesRetracted(boolean retracted) {
        this.dataTracker.set(SPIKES_RETRACTED, retracted);
    }

    public int getWarmupTime() {
        return 80;
    }

    void setBeamTarget(int entityId) {
        this.dataTracker.set(BEAM_TARGET_ID, entityId);
    }

    public boolean hasBeamTarget() {
        return this.dataTracker.get(BEAM_TARGET_ID) != 0;
    }

    @Nullable
    public LivingEntity getBeamTarget() {
        if (!this.hasBeamTarget()) {
            return null;
        }
        if (this.world.isClient) {
            if (this.cachedBeamTarget != null) {
                return this.cachedBeamTarget;
            }
            Entity entity = this.world.getEntityById(this.dataTracker.get(BEAM_TARGET_ID));
            if (entity instanceof LivingEntity) {
                this.cachedBeamTarget = (LivingEntity)entity;
                return this.cachedBeamTarget;
            }
            return null;
        }
        return this.getTarget();
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (BEAM_TARGET_ID.equals(data)) {
            this.beamTicks = 0;
            this.cachedBeamTarget = null;
        }
    }

    @Override
    public int getMinAmbientSoundDelay() {
        return 160;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isInsideWaterOrBubbleColumn() ? SoundEvents.ENTITY_GUARDIAN_AMBIENT : SoundEvents.ENTITY_GUARDIAN_AMBIENT_LAND;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.isInsideWaterOrBubbleColumn() ? SoundEvents.ENTITY_GUARDIAN_HURT : SoundEvents.ENTITY_GUARDIAN_HURT_LAND;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isInsideWaterOrBubbleColumn() ? SoundEvents.ENTITY_GUARDIAN_DEATH : SoundEvents.ENTITY_GUARDIAN_DEATH_LAND;
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.EVENTS;
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.5f;
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        if (world.getFluidState(pos).isIn(FluidTags.WATER)) {
            return 10.0f + world.getBrightness(pos) - 0.5f;
        }
        return super.getPathfindingFavor(pos, world);
    }

    @Override
    public void tickMovement() {
        if (this.isAlive()) {
            if (this.world.isClient) {
                Object vec3d;
                this.prevTailAngle = this.tailAngle;
                if (!this.isTouchingWater()) {
                    this.spikesExtensionRate = 2.0f;
                    vec3d = this.getVelocity();
                    if (((Vec3d)vec3d).y > 0.0 && this.flopping && !this.isSilent()) {
                        this.world.playSound(this.getX(), this.getY(), this.getZ(), this.getFlopSound(), this.getSoundCategory(), 1.0f, 1.0f, false);
                    }
                    this.flopping = ((Vec3d)vec3d).y < 0.0 && this.world.isTopSolid(this.getBlockPos().down(), this);
                } else {
                    this.spikesExtensionRate = this.areSpikesRetracted() ? (this.spikesExtensionRate < 0.5f ? 4.0f : (this.spikesExtensionRate += (0.5f - this.spikesExtensionRate) * 0.1f)) : (this.spikesExtensionRate += (0.125f - this.spikesExtensionRate) * 0.2f);
                }
                this.tailAngle += this.spikesExtensionRate;
                this.prevSpikesExtension = this.spikesExtension;
                this.spikesExtension = !this.isInsideWaterOrBubbleColumn() ? this.random.nextFloat() : (this.areSpikesRetracted() ? (this.spikesExtension += (0.0f - this.spikesExtension) * 0.25f) : (this.spikesExtension += (1.0f - this.spikesExtension) * 0.06f));
                if (this.areSpikesRetracted() && this.isTouchingWater()) {
                    vec3d = this.getRotationVec(0.0f);
                    for (int i = 0; i < 2; ++i) {
                        this.world.addParticle(ParticleTypes.BUBBLE, this.getParticleX(0.5) - ((Vec3d)vec3d).x * 1.5, this.getRandomBodyY() - ((Vec3d)vec3d).y * 1.5, this.getParticleZ(0.5) - ((Vec3d)vec3d).z * 1.5, 0.0, 0.0, 0.0);
                    }
                }
                if (this.hasBeamTarget()) {
                    if (this.beamTicks < this.getWarmupTime()) {
                        ++this.beamTicks;
                    }
                    if ((vec3d = this.getBeamTarget()) != null) {
                        this.getLookControl().lookAt((Entity)vec3d, 90.0f, 90.0f);
                        this.getLookControl().tick();
                        double i = this.getBeamProgress(0.0f);
                        double d = ((Entity)vec3d).getX() - this.getX();
                        double e = ((Entity)vec3d).getBodyY(0.5) - this.getEyeY();
                        double f = ((Entity)vec3d).getZ() - this.getZ();
                        double g = Math.sqrt(d * d + e * e + f * f);
                        d /= g;
                        e /= g;
                        f /= g;
                        double h = this.random.nextDouble();
                        while (h < g) {
                            this.world.addParticle(ParticleTypes.BUBBLE, this.getX() + d * (h += 1.8 - i + this.random.nextDouble() * (1.7 - i)), this.getEyeY() + e * h, this.getZ() + f * h, 0.0, 0.0, 0.0);
                        }
                    }
                }
            }
            if (this.isInsideWaterOrBubbleColumn()) {
                this.setAir(300);
            } else if (this.onGround) {
                this.setVelocity(this.getVelocity().add((this.random.nextFloat() * 2.0f - 1.0f) * 0.4f, 0.5, (this.random.nextFloat() * 2.0f - 1.0f) * 0.4f));
                this.setYaw(this.random.nextFloat() * 360.0f);
                this.onGround = false;
                this.velocityDirty = true;
            }
            if (this.hasBeamTarget()) {
                this.setYaw(this.headYaw);
            }
        }
        super.tickMovement();
    }

    protected SoundEvent getFlopSound() {
        return SoundEvents.ENTITY_GUARDIAN_FLOP;
    }

    public float getTailAngle(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevTailAngle, this.tailAngle);
    }

    public float getSpikesExtension(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevSpikesExtension, this.spikesExtension);
    }

    public float getBeamProgress(float tickDelta) {
        return ((float)this.beamTicks + tickDelta) / (float)this.getWarmupTime();
    }

    @Override
    public boolean canSpawn(WorldView world) {
        return world.intersectsEntities(this);
    }

    public static boolean canSpawn(EntityType<? extends GuardianEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return !(random.nextInt(20) != 0 && world.isSkyVisibleAllowingSea(pos) || world.getDifficulty() == Difficulty.PEACEFUL || spawnReason != SpawnReason.SPAWNER && !world.getFluidState(pos).isIn(FluidTags.WATER));
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!this.areSpikesRetracted() && !source.isMagic() && source.getSource() instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)source.getSource();
            if (!source.isExplosive()) {
                livingEntity.damage(DamageSource.thorns(this), 2.0f);
            }
        }
        if (this.wanderGoal != null) {
            this.wanderGoal.ignoreChanceOnce();
        }
        return super.damage(source, amount);
    }

    @Override
    public int getLookPitchSpeed() {
        return 180;
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (this.canMoveVoluntarily() && this.isTouchingWater()) {
            this.updateVelocity(0.1f, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.9));
            if (!this.areSpikesRetracted() && this.getTarget() == null) {
                this.setVelocity(this.getVelocity().add(0.0, -0.005, 0.0));
            }
        } else {
            super.travel(movementInput);
        }
    }

    static class GuardianMoveControl
    extends MoveControl {
        private final GuardianEntity guardian;

        public GuardianMoveControl(GuardianEntity guardian) {
            super(guardian);
            this.guardian = guardian;
        }

        @Override
        public void tick() {
            if (this.state != MoveControl.State.MOVE_TO || this.guardian.getNavigation().isIdle()) {
                this.guardian.setMovementSpeed(0.0f);
                this.guardian.setSpikesRetracted(false);
                return;
            }
            Vec3d vec3d = new Vec3d(this.targetX - this.guardian.getX(), this.targetY - this.guardian.getY(), this.targetZ - this.guardian.getZ());
            double d = vec3d.length();
            double e = vec3d.x / d;
            double f = vec3d.y / d;
            double g = vec3d.z / d;
            float h = (float)(MathHelper.atan2(vec3d.z, vec3d.x) * 57.2957763671875) - 90.0f;
            this.guardian.setYaw(this.wrapDegrees(this.guardian.getYaw(), h, 90.0f));
            this.guardian.bodyYaw = this.guardian.getYaw();
            float i = (float)(this.speed * this.guardian.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
            float j = MathHelper.lerp(0.125f, this.guardian.getMovementSpeed(), i);
            this.guardian.setMovementSpeed(j);
            double k = Math.sin((double)(this.guardian.age + this.guardian.getId()) * 0.5) * 0.05;
            double l = Math.cos(this.guardian.getYaw() * ((float)Math.PI / 180));
            double m = Math.sin(this.guardian.getYaw() * ((float)Math.PI / 180));
            double n = Math.sin((double)(this.guardian.age + this.guardian.getId()) * 0.75) * 0.05;
            this.guardian.setVelocity(this.guardian.getVelocity().add(k * l, n * (m + l) * 0.25 + (double)j * f * 0.1, k * m));
            LookControl lookControl = this.guardian.getLookControl();
            double o = this.guardian.getX() + e * 2.0;
            double p = this.guardian.getEyeY() + f / d;
            double q = this.guardian.getZ() + g * 2.0;
            double r = lookControl.getLookX();
            double s = lookControl.getLookY();
            double t = lookControl.getLookZ();
            if (!lookControl.isActive()) {
                r = o;
                s = p;
                t = q;
            }
            this.guardian.getLookControl().lookAt(MathHelper.lerp(0.125, r, o), MathHelper.lerp(0.125, s, p), MathHelper.lerp(0.125, t, q), 10.0f, 40.0f);
            this.guardian.setSpikesRetracted(true);
        }
    }

    static class FireBeamGoal
    extends Goal {
        private final GuardianEntity guardian;
        private int beamTicks;
        private final boolean elder;

        public FireBeamGoal(GuardianEntity guardian) {
            this.guardian = guardian;
            this.elder = guardian instanceof ElderGuardianEntity;
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity livingEntity = this.guardian.getTarget();
            return livingEntity != null && livingEntity.isAlive();
        }

        @Override
        public boolean shouldContinue() {
            return super.shouldContinue() && (this.elder || this.guardian.squaredDistanceTo(this.guardian.getTarget()) > 9.0);
        }

        @Override
        public void start() {
            this.beamTicks = -10;
            this.guardian.getNavigation().stop();
            this.guardian.getLookControl().lookAt(this.guardian.getTarget(), 90.0f, 90.0f);
            this.guardian.velocityDirty = true;
        }

        @Override
        public void stop() {
            this.guardian.setBeamTarget(0);
            this.guardian.setTarget(null);
            this.guardian.wanderGoal.ignoreChanceOnce();
        }

        @Override
        public void tick() {
            LivingEntity livingEntity = this.guardian.getTarget();
            this.guardian.getNavigation().stop();
            this.guardian.getLookControl().lookAt(livingEntity, 90.0f, 90.0f);
            if (!this.guardian.canSee(livingEntity)) {
                this.guardian.setTarget(null);
                return;
            }
            ++this.beamTicks;
            if (this.beamTicks == 0) {
                this.guardian.setBeamTarget(this.guardian.getTarget().getId());
                if (!this.guardian.isSilent()) {
                    this.guardian.world.sendEntityStatus(this.guardian, (byte)21);
                }
            } else if (this.beamTicks >= this.guardian.getWarmupTime()) {
                float f = 1.0f;
                if (this.guardian.world.getDifficulty() == Difficulty.HARD) {
                    f += 2.0f;
                }
                if (this.elder) {
                    f += 2.0f;
                }
                livingEntity.damage(DamageSource.magic(this.guardian, this.guardian), f);
                livingEntity.damage(DamageSource.mob(this.guardian), (float)this.guardian.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE));
                this.guardian.setTarget(null);
            }
            super.tick();
        }
    }

    static class GuardianTargetPredicate
    implements Predicate<LivingEntity> {
        private final GuardianEntity owner;

        public GuardianTargetPredicate(GuardianEntity owner) {
            this.owner = owner;
        }

        @Override
        public boolean test(@Nullable LivingEntity livingEntity) {
            return (livingEntity instanceof PlayerEntity || livingEntity instanceof SquidEntity || livingEntity instanceof AxolotlEntity) && livingEntity.squaredDistanceTo(this.owner) > 9.0;
        }

        @Override
        public /* synthetic */ boolean test(@Nullable Object context) {
            return this.test((LivingEntity)context);
        }
    }
}

