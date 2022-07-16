/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.projectile;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ShulkerBulletEntity
extends ProjectileEntity {
    private static final double field_30666 = 0.15;
    @Nullable
    private Entity target;
    @Nullable
    private Direction direction;
    private int stepCount;
    private double targetX;
    private double targetY;
    private double targetZ;
    @Nullable
    private UUID targetUuid;

    public ShulkerBulletEntity(EntityType<? extends ShulkerBulletEntity> entityType, World world) {
        super((EntityType<? extends ProjectileEntity>)entityType, world);
        this.noClip = true;
    }

    public ShulkerBulletEntity(World world, LivingEntity owner, Entity target, Direction.Axis axis) {
        this((EntityType<? extends ShulkerBulletEntity>)EntityType.SHULKER_BULLET, world);
        this.setOwner(owner);
        BlockPos blockPos = owner.getBlockPos();
        double d = (double)blockPos.getX() + 0.5;
        double e = (double)blockPos.getY() + 0.5;
        double f = (double)blockPos.getZ() + 0.5;
        this.refreshPositionAndAngles(d, e, f, this.getYaw(), this.getPitch());
        this.target = target;
        this.direction = Direction.UP;
        this.method_7486(axis);
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.target != null) {
            nbt.putUuid("Target", this.target.getUuid());
        }
        if (this.direction != null) {
            nbt.putInt("Dir", this.direction.getId());
        }
        nbt.putInt("Steps", this.stepCount);
        nbt.putDouble("TXD", this.targetX);
        nbt.putDouble("TYD", this.targetY);
        nbt.putDouble("TZD", this.targetZ);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.stepCount = nbt.getInt("Steps");
        this.targetX = nbt.getDouble("TXD");
        this.targetY = nbt.getDouble("TYD");
        this.targetZ = nbt.getDouble("TZD");
        if (nbt.contains("Dir", 99)) {
            this.direction = Direction.byId(nbt.getInt("Dir"));
        }
        if (nbt.containsUuid("Target")) {
            this.targetUuid = nbt.getUuid("Target");
        }
    }

    @Override
    protected void initDataTracker() {
    }

    @Nullable
    private Direction getDirection() {
        return this.direction;
    }

    private void setDirection(@Nullable Direction direction) {
        this.direction = direction;
    }

    private void method_7486(@Nullable Direction.Axis axis) {
        BlockPos blockPos;
        double d = 0.5;
        if (this.target == null) {
            blockPos = this.getBlockPos().down();
        } else {
            d = (double)this.target.getHeight() * 0.5;
            blockPos = new BlockPos(this.target.getX(), this.target.getY() + d, this.target.getZ());
        }
        double e = (double)blockPos.getX() + 0.5;
        double f = (double)blockPos.getY() + d;
        double g = (double)blockPos.getZ() + 0.5;
        Direction direction = null;
        if (!blockPos.isWithinDistance(this.getPos(), 2.0)) {
            BlockPos blockPos2 = this.getBlockPos();
            ArrayList<Direction> list = Lists.newArrayList();
            if (axis != Direction.Axis.X) {
                if (blockPos2.getX() < blockPos.getX() && this.world.isAir(blockPos2.east())) {
                    list.add(Direction.EAST);
                } else if (blockPos2.getX() > blockPos.getX() && this.world.isAir(blockPos2.west())) {
                    list.add(Direction.WEST);
                }
            }
            if (axis != Direction.Axis.Y) {
                if (blockPos2.getY() < blockPos.getY() && this.world.isAir(blockPos2.up())) {
                    list.add(Direction.UP);
                } else if (blockPos2.getY() > blockPos.getY() && this.world.isAir(blockPos2.down())) {
                    list.add(Direction.DOWN);
                }
            }
            if (axis != Direction.Axis.Z) {
                if (blockPos2.getZ() < blockPos.getZ() && this.world.isAir(blockPos2.south())) {
                    list.add(Direction.SOUTH);
                } else if (blockPos2.getZ() > blockPos.getZ() && this.world.isAir(blockPos2.north())) {
                    list.add(Direction.NORTH);
                }
            }
            direction = Direction.random(this.random);
            if (list.isEmpty()) {
                for (int i = 5; !this.world.isAir(blockPos2.offset(direction)) && i > 0; --i) {
                    direction = Direction.random(this.random);
                }
            } else {
                direction = (Direction)list.get(this.random.nextInt(list.size()));
            }
            e = this.getX() + (double)direction.getOffsetX();
            f = this.getY() + (double)direction.getOffsetY();
            g = this.getZ() + (double)direction.getOffsetZ();
        }
        this.setDirection(direction);
        double blockPos2 = e - this.getX();
        double i = f - this.getY();
        double h = g - this.getZ();
        double j = Math.sqrt(blockPos2 * blockPos2 + i * i + h * h);
        if (j == 0.0) {
            this.targetX = 0.0;
            this.targetY = 0.0;
            this.targetZ = 0.0;
        } else {
            this.targetX = blockPos2 / j * 0.15;
            this.targetY = i / j * 0.15;
            this.targetZ = h / j * 0.15;
        }
        this.velocityDirty = true;
        this.stepCount = 10 + this.random.nextInt(5) * 10;
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
        }
    }

    @Override
    public void tick() {
        Object vec3d;
        super.tick();
        if (!this.world.isClient) {
            if (this.target == null && this.targetUuid != null) {
                this.target = ((ServerWorld)this.world).getEntity(this.targetUuid);
                if (this.target == null) {
                    this.targetUuid = null;
                }
            }
            if (!(this.target == null || !this.target.isAlive() || this.target instanceof PlayerEntity && this.target.isSpectator())) {
                this.targetX = MathHelper.clamp(this.targetX * 1.025, -1.0, 1.0);
                this.targetY = MathHelper.clamp(this.targetY * 1.025, -1.0, 1.0);
                this.targetZ = MathHelper.clamp(this.targetZ * 1.025, -1.0, 1.0);
                vec3d = this.getVelocity();
                this.setVelocity(((Vec3d)vec3d).add((this.targetX - ((Vec3d)vec3d).x) * 0.2, (this.targetY - ((Vec3d)vec3d).y) * 0.2, (this.targetZ - ((Vec3d)vec3d).z) * 0.2));
            } else if (!this.hasNoGravity()) {
                this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
            }
            vec3d = ProjectileUtil.getCollision(this, this::canHit);
            if (((HitResult)vec3d).getType() != HitResult.Type.MISS) {
                this.onCollision((HitResult)vec3d);
            }
        }
        this.checkBlockCollision();
        vec3d = this.getVelocity();
        this.setPosition(this.getX() + ((Vec3d)vec3d).x, this.getY() + ((Vec3d)vec3d).y, this.getZ() + ((Vec3d)vec3d).z);
        ProjectileUtil.method_7484(this, 0.5f);
        if (this.world.isClient) {
            this.world.addParticle(ParticleTypes.END_ROD, this.getX() - ((Vec3d)vec3d).x, this.getY() - ((Vec3d)vec3d).y + 0.15, this.getZ() - ((Vec3d)vec3d).z, 0.0, 0.0, 0.0);
        } else if (this.target != null && !this.target.isRemoved()) {
            if (this.stepCount > 0) {
                --this.stepCount;
                if (this.stepCount == 0) {
                    this.method_7486(this.direction == null ? null : this.direction.getAxis());
                }
            }
            if (this.direction != null) {
                BlockPos blockPos = this.getBlockPos();
                Direction.Axis axis = this.direction.getAxis();
                if (this.world.isTopSolid(blockPos.offset(this.direction), this)) {
                    this.method_7486(axis);
                } else {
                    BlockPos blockPos2 = this.target.getBlockPos();
                    if (axis == Direction.Axis.X && blockPos.getX() == blockPos2.getX() || axis == Direction.Axis.Z && blockPos.getZ() == blockPos2.getZ() || axis == Direction.Axis.Y && blockPos.getY() == blockPos2.getY()) {
                        this.method_7486(axis);
                    }
                }
            }
        }
    }

    @Override
    protected boolean canHit(Entity entity) {
        return super.canHit(entity) && !entity.noClip;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean shouldRender(double distance) {
        return distance < 16384.0;
    }

    @Override
    public float getBrightnessAtEyes() {
        return 1.0f;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        Entity entity2 = this.getOwner();
        LivingEntity livingEntity = entity2 instanceof LivingEntity ? (LivingEntity)entity2 : null;
        boolean bl = entity.damage(DamageSource.mobProjectile(this, livingEntity).setProjectile(), 4.0f);
        if (bl) {
            this.applyDamageEffects(livingEntity, entity);
            if (entity instanceof LivingEntity) {
                ((LivingEntity)entity).addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 200), MoreObjects.firstNonNull(entity2, this));
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        ((ServerWorld)this.world).spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
        this.playSound(SoundEvents.ENTITY_SHULKER_BULLET_HIT, 1.0f, 1.0f);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        this.discard();
    }

    @Override
    public boolean collides() {
        return true;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!this.world.isClient) {
            this.playSound(SoundEvents.ENTITY_SHULKER_BULLET_HURT, 1.0f, 1.0f);
            ((ServerWorld)this.world).spawnParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 15, 0.2, 0.2, 0.2, 0.0);
            this.discard();
        }
        return true;
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        double d = packet.getVelocityX();
        double e = packet.getVelocityY();
        double f = packet.getVelocityZ();
        this.setVelocity(d, e, f);
    }
}

