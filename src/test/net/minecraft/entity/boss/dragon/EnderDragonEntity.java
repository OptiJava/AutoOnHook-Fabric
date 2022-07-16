/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.boss.dragon;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathMinHeap;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseManager;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class EnderDragonEntity
extends MobEntity
implements Monster {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final TrackedData<Integer> PHASE_TYPE = DataTracker.registerData(EnderDragonEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TargetPredicate CLOSE_PLAYER_PREDICATE = TargetPredicate.createAttackable().setBaseMaxDistance(64.0);
    private static final int MAX_HEALTH = 200;
    private static final int field_30429 = 400;
    /**
     * The damage the dragon can take before it takes off, represented as a ratio to the full health.
     */
    private static final float TAKEOFF_THRESHOLD = 0.25f;
    private static final String DRAGON_DEATH_TIME_KEY = "DragonDeathTime";
    private static final String DRAGON_PHASE_KEY = "DragonPhase";
    /**
     * (yaw, y, ?)
     */
    public final double[][] segmentCircularBuffer = new double[64][3];
    public int latestSegment = -1;
    private final EnderDragonPart[] parts;
    public final EnderDragonPart head;
    private final EnderDragonPart neck;
    private final EnderDragonPart body;
    private final EnderDragonPart tail1;
    private final EnderDragonPart tail2;
    private final EnderDragonPart tail3;
    private final EnderDragonPart rightWing;
    private final EnderDragonPart leftWing;
    public float prevWingPosition;
    public float wingPosition;
    public boolean slowedDownByBlock;
    public int ticksSinceDeath;
    public float yawAcceleration;
    @Nullable
    public EndCrystalEntity connectedCrystal;
    @Nullable
    private final EnderDragonFight fight;
    private final PhaseManager phaseManager;
    private int ticksUntilNextGrowl = 100;
    private int damageDuringSitting;
    /**
     * The first 12 path nodes are used for end crystals; the others are not tied to them.
     */
    private final PathNode[] pathNodes = new PathNode[24];
    /**
     * An array of 24 bitflags, where node #i leads to #j if and only if
     * {@code (pathNodeConnections[i] & (1 << j)) != 0}.
     */
    private final int[] pathNodeConnections = new int[24];
    private final PathMinHeap pathHeap = new PathMinHeap();

    public EnderDragonEntity(EntityType<? extends EnderDragonEntity> entityType, World world) {
        super((EntityType<? extends MobEntity>)EntityType.ENDER_DRAGON, world);
        this.head = new EnderDragonPart(this, "head", 1.0f, 1.0f);
        this.neck = new EnderDragonPart(this, "neck", 3.0f, 3.0f);
        this.body = new EnderDragonPart(this, "body", 5.0f, 3.0f);
        this.tail1 = new EnderDragonPart(this, "tail", 2.0f, 2.0f);
        this.tail2 = new EnderDragonPart(this, "tail", 2.0f, 2.0f);
        this.tail3 = new EnderDragonPart(this, "tail", 2.0f, 2.0f);
        this.rightWing = new EnderDragonPart(this, "wing", 4.0f, 2.0f);
        this.leftWing = new EnderDragonPart(this, "wing", 4.0f, 2.0f);
        this.parts = new EnderDragonPart[]{this.head, this.neck, this.body, this.tail1, this.tail2, this.tail3, this.rightWing, this.leftWing};
        this.setHealth(this.getMaxHealth());
        this.noClip = true;
        this.ignoreCameraFrustum = true;
        this.fight = world instanceof ServerWorld ? ((ServerWorld)world).getEnderDragonFight() : null;
        this.phaseManager = new PhaseManager(this);
    }

    public static DefaultAttributeContainer.Builder createEnderDragonAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0);
    }

    @Override
    public boolean hasWings() {
        float f = MathHelper.cos(this.wingPosition * ((float)Math.PI * 2));
        float g = MathHelper.cos(this.prevWingPosition * ((float)Math.PI * 2));
        return g <= -0.3f && f >= -0.3f;
    }

    @Override
    public void addFlapEffects() {
        if (this.world.isClient && !this.isSilent()) {
            this.world.playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP, this.getSoundCategory(), 5.0f, 0.8f + this.random.nextFloat() * 0.3f, false);
        }
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.getDataTracker().startTracking(PHASE_TYPE, PhaseType.HOVER.getTypeId());
    }

    public double[] getSegmentProperties(int segmentNumber, float tickDelta) {
        if (this.isDead()) {
            tickDelta = 0.0f;
        }
        tickDelta = 1.0f - tickDelta;
        int i = this.latestSegment - segmentNumber & 0x3F;
        int j = this.latestSegment - segmentNumber - 1 & 0x3F;
        double[] ds = new double[3];
        double d = this.segmentCircularBuffer[i][0];
        double e = MathHelper.wrapDegrees(this.segmentCircularBuffer[j][0] - d);
        ds[0] = d + e * (double)tickDelta;
        d = this.segmentCircularBuffer[i][1];
        e = this.segmentCircularBuffer[j][1] - d;
        ds[1] = d + e * (double)tickDelta;
        ds[2] = MathHelper.lerp((double)tickDelta, this.segmentCircularBuffer[i][2], this.segmentCircularBuffer[j][2]);
        return ds;
    }

    @Override
    public void tickMovement() {
        int l;
        float o;
        float n;
        float m;
        Object vec3d2;
        this.addAirTravelEffects();
        if (this.world.isClient) {
            this.setHealth(this.getHealth());
            if (!this.isSilent() && !this.phaseManager.getCurrent().isSittingOrHovering() && --this.ticksUntilNextGrowl < 0) {
                this.world.playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, this.getSoundCategory(), 2.5f, 0.8f + this.random.nextFloat() * 0.3f, false);
                this.ticksUntilNextGrowl = 200 + this.random.nextInt(200);
            }
        }
        this.prevWingPosition = this.wingPosition;
        if (this.isDead()) {
            float f = (this.random.nextFloat() - 0.5f) * 8.0f;
            float g = (this.random.nextFloat() - 0.5f) * 4.0f;
            float h = (this.random.nextFloat() - 0.5f) * 8.0f;
            this.world.addParticle(ParticleTypes.EXPLOSION, this.getX() + (double)f, this.getY() + 2.0 + (double)g, this.getZ() + (double)h, 0.0, 0.0, 0.0);
            return;
        }
        this.tickWithEndCrystals();
        Vec3d f = this.getVelocity();
        float g = 0.2f / ((float)f.horizontalLength() * 10.0f + 1.0f);
        this.wingPosition = this.phaseManager.getCurrent().isSittingOrHovering() ? (this.wingPosition += 0.1f) : (this.slowedDownByBlock ? (this.wingPosition += g * 0.5f) : (this.wingPosition += (g *= (float)Math.pow(2.0, f.y))));
        this.setYaw(MathHelper.wrapDegrees(this.getYaw()));
        if (this.isAiDisabled()) {
            this.wingPosition = 0.5f;
            return;
        }
        if (this.latestSegment < 0) {
            for (int h = 0; h < this.segmentCircularBuffer.length; ++h) {
                this.segmentCircularBuffer[h][0] = this.getYaw();
                this.segmentCircularBuffer[h][1] = this.getY();
            }
        }
        if (++this.latestSegment == this.segmentCircularBuffer.length) {
            this.latestSegment = 0;
        }
        this.segmentCircularBuffer[this.latestSegment][0] = this.getYaw();
        this.segmentCircularBuffer[this.latestSegment][1] = this.getY();
        if (this.world.isClient) {
            if (this.bodyTrackingIncrements > 0) {
                double h = this.getX() + (this.serverX - this.getX()) / (double)this.bodyTrackingIncrements;
                d = this.getY() + (this.serverY - this.getY()) / (double)this.bodyTrackingIncrements;
                e = this.getZ() + (this.serverZ - this.getZ()) / (double)this.bodyTrackingIncrements;
                i = MathHelper.wrapDegrees(this.serverYaw - (double)this.getYaw());
                this.setYaw(this.getYaw() + (float)i / (float)this.bodyTrackingIncrements);
                this.setPitch(this.getPitch() + (float)(this.serverPitch - (double)this.getPitch()) / (float)this.bodyTrackingIncrements);
                --this.bodyTrackingIncrements;
                this.setPosition(h, d, e);
                this.setRotation(this.getYaw(), this.getPitch());
            }
            this.phaseManager.getCurrent().clientTick();
        } else {
            Vec3d vec3d;
            Phase h = this.phaseManager.getCurrent();
            h.serverTick();
            if (this.phaseManager.getCurrent() != h) {
                h = this.phaseManager.getCurrent();
                h.serverTick();
            }
            if ((vec3d = h.getPathTarget()) != null) {
                d = vec3d.x - this.getX();
                e = vec3d.y - this.getY();
                i = vec3d.z - this.getZ();
                double j = d * d + e * e + i * i;
                float k = h.getMaxYAcceleration();
                double l2 = Math.sqrt(d * d + i * i);
                if (l2 > 0.0) {
                    e = MathHelper.clamp(e / l2, (double)(-k), (double)k);
                }
                this.setVelocity(this.getVelocity().add(0.0, e * 0.01, 0.0));
                this.setYaw(MathHelper.wrapDegrees(this.getYaw()));
                vec3d2 = vec3d.subtract(this.getX(), this.getY(), this.getZ()).normalize();
                Vec3d vec3d3 = new Vec3d(MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)), this.getVelocity().y, -MathHelper.cos(this.getYaw() * ((float)Math.PI / 180))).normalize();
                m = Math.max(((float)vec3d3.dotProduct((Vec3d)vec3d2) + 0.5f) / 1.5f, 0.0f);
                if (Math.abs(d) > (double)1.0E-5f || Math.abs(i) > (double)1.0E-5f) {
                    double n2 = MathHelper.clamp(MathHelper.wrapDegrees(180.0 - MathHelper.atan2(d, i) * 57.2957763671875 - (double)this.getYaw()), -50.0, 50.0);
                    this.yawAcceleration *= 0.8f;
                    this.yawAcceleration = (float)((double)this.yawAcceleration + n2 * (double)h.getYawAcceleration());
                    this.setYaw(this.getYaw() + this.yawAcceleration * 0.1f);
                }
                n = (float)(2.0 / (j + 1.0));
                o = 0.06f;
                this.updateVelocity(0.06f * (m * n + (1.0f - n)), new Vec3d(0.0, 0.0, -1.0));
                if (this.slowedDownByBlock) {
                    this.move(MovementType.SELF, this.getVelocity().multiply(0.8f));
                } else {
                    this.move(MovementType.SELF, this.getVelocity());
                }
                Vec3d vec3d4 = this.getVelocity().normalize();
                double p = 0.8 + 0.15 * (vec3d4.dotProduct(vec3d3) + 1.0) / 2.0;
                this.setVelocity(this.getVelocity().multiply(p, 0.91f, p));
            }
        }
        this.bodyYaw = this.getYaw();
        Vec3d[] h = new Vec3d[this.parts.length];
        for (int vec3d = 0; vec3d < this.parts.length; ++vec3d) {
            h[vec3d] = new Vec3d(this.parts[vec3d].getX(), this.parts[vec3d].getY(), this.parts[vec3d].getZ());
        }
        float vec3d = (float)(this.getSegmentProperties(5, 1.0f)[1] - this.getSegmentProperties(10, 1.0f)[1]) * 10.0f * ((float)Math.PI / 180);
        float d = MathHelper.cos(vec3d);
        float q = MathHelper.sin(vec3d);
        float e = this.getYaw() * ((float)Math.PI / 180);
        float r = MathHelper.sin(e);
        float i = MathHelper.cos(e);
        this.movePart(this.body, r * 0.5f, 0.0, -i * 0.5f);
        this.movePart(this.rightWing, i * 4.5f, 2.0, r * 4.5f);
        this.movePart(this.leftWing, i * -4.5f, 2.0, r * -4.5f);
        if (!this.world.isClient && this.hurtTime == 0) {
            this.launchLivingEntities(this.world.getOtherEntities(this, this.rightWing.getBoundingBox().expand(4.0, 2.0, 4.0).offset(0.0, -2.0, 0.0), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
            this.launchLivingEntities(this.world.getOtherEntities(this, this.leftWing.getBoundingBox().expand(4.0, 2.0, 4.0).offset(0.0, -2.0, 0.0), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
            this.damageLivingEntities(this.world.getOtherEntities(this, this.head.getBoundingBox().expand(1.0), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
            this.damageLivingEntities(this.world.getOtherEntities(this, this.neck.getBoundingBox().expand(1.0), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
        }
        float s = MathHelper.sin(this.getYaw() * ((float)Math.PI / 180) - this.yawAcceleration * 0.01f);
        float j = MathHelper.cos(this.getYaw() * ((float)Math.PI / 180) - this.yawAcceleration * 0.01f);
        float t = this.getHeadVerticalMovement();
        this.movePart(this.head, s * 6.5f * d, t + q * 6.5f, -j * 6.5f * d);
        this.movePart(this.neck, s * 5.5f * d, t + q * 5.5f, -j * 5.5f * d);
        double[] k = this.getSegmentProperties(5, 1.0f);
        for (l = 0; l < 3; ++l) {
            EnderDragonPart enderDragonPart = null;
            if (l == 0) {
                enderDragonPart = this.tail1;
            }
            if (l == 1) {
                enderDragonPart = this.tail2;
            }
            if (l == 2) {
                enderDragonPart = this.tail3;
            }
            vec3d2 = this.getSegmentProperties(12 + l * 2, 1.0f);
            float vec3d3 = this.getYaw() * ((float)Math.PI / 180) + this.wrapYawChange((double)(vec3d2[0] - k[0])) * ((float)Math.PI / 180);
            m = MathHelper.sin(vec3d3);
            n = MathHelper.cos(vec3d3);
            o = 1.5f;
            float vec3d4 = (float)(l + 1) * 2.0f;
            this.movePart(enderDragonPart, -(r * 1.5f + m * vec3d4) * d, (double)(vec3d2[1] - k[1] - (double)((vec3d4 + 1.5f) * q) + 1.5), (i * 1.5f + n * vec3d4) * d);
        }
        if (!this.world.isClient) {
            this.slowedDownByBlock = this.destroyBlocks(this.head.getBoundingBox()) | this.destroyBlocks(this.neck.getBoundingBox()) | this.destroyBlocks(this.body.getBoundingBox());
            if (this.fight != null) {
                this.fight.updateFight(this);
            }
        }
        for (l = 0; l < this.parts.length; ++l) {
            this.parts[l].prevX = h[l].x;
            this.parts[l].prevY = h[l].y;
            this.parts[l].prevZ = h[l].z;
            this.parts[l].lastRenderX = h[l].x;
            this.parts[l].lastRenderY = h[l].y;
            this.parts[l].lastRenderZ = h[l].z;
        }
    }

    private void movePart(EnderDragonPart enderDragonPart, double dx, double dy, double dz) {
        enderDragonPart.setPosition(this.getX() + dx, this.getY() + dy, this.getZ() + dz);
    }

    private float getHeadVerticalMovement() {
        if (this.phaseManager.getCurrent().isSittingOrHovering()) {
            return -1.0f;
        }
        double[] ds = this.getSegmentProperties(5, 1.0f);
        double[] es = this.getSegmentProperties(0, 1.0f);
        return (float)(ds[1] - es[1]);
    }

    /**
     * Things to do every tick related to end crystals. The Ender Dragon:
     * 
     * * Disconnects from its crystal if it is removed
     * * If it is connected to a crystal, then heals every 10 ticks
     * * With a 1 in 10 chance each tick, searches for the nearest crystal and connects to it if present
     */
    private void tickWithEndCrystals() {
        if (this.connectedCrystal != null) {
            if (this.connectedCrystal.isRemoved()) {
                this.connectedCrystal = null;
            } else if (this.age % 10 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.setHealth(this.getHealth() + 1.0f);
            }
        }
        if (this.random.nextInt(10) == 0) {
            List<EndCrystalEntity> list = this.world.getNonSpectatingEntities(EndCrystalEntity.class, this.getBoundingBox().expand(32.0));
            EndCrystalEntity endCrystalEntity = null;
            double d = Double.MAX_VALUE;
            for (EndCrystalEntity endCrystalEntity2 : list) {
                double e = endCrystalEntity2.squaredDistanceTo(this);
                if (!(e < d)) continue;
                d = e;
                endCrystalEntity = endCrystalEntity2;
            }
            this.connectedCrystal = endCrystalEntity;
        }
    }

    private void launchLivingEntities(List<Entity> entities) {
        double d = (this.body.getBoundingBox().minX + this.body.getBoundingBox().maxX) / 2.0;
        double e = (this.body.getBoundingBox().minZ + this.body.getBoundingBox().maxZ) / 2.0;
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity)) continue;
            double f = entity.getX() - d;
            double g = entity.getZ() - e;
            double h = Math.max(f * f + g * g, 0.1);
            entity.addVelocity(f / h * 4.0, 0.2f, g / h * 4.0);
            if (this.phaseManager.getCurrent().isSittingOrHovering() || ((LivingEntity)entity).getLastAttackedTime() >= entity.age - 2) continue;
            entity.damage(DamageSource.mob(this), 5.0f);
            this.applyDamageEffects(this, entity);
        }
    }

    private void damageLivingEntities(List<Entity> entities) {
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity)) continue;
            entity.damage(DamageSource.mob(this), 10.0f);
            this.applyDamageEffects(this, entity);
        }
    }

    private float wrapYawChange(double yawDegrees) {
        return (float)MathHelper.wrapDegrees(yawDegrees);
    }

    private boolean destroyBlocks(Box box) {
        int i = MathHelper.floor(box.minX);
        int j = MathHelper.floor(box.minY);
        int k = MathHelper.floor(box.minZ);
        int l = MathHelper.floor(box.maxX);
        int m = MathHelper.floor(box.maxY);
        int n = MathHelper.floor(box.maxZ);
        boolean bl = false;
        boolean bl2 = false;
        for (int o = i; o <= l; ++o) {
            for (int p = j; p <= m; ++p) {
                for (int q = k; q <= n; ++q) {
                    BlockPos blockPos = new BlockPos(o, p, q);
                    BlockState blockState = this.world.getBlockState(blockPos);
                    if (blockState.isAir() || blockState.getMaterial() == Material.FIRE) continue;
                    if (!this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) || blockState.isIn(BlockTags.DRAGON_IMMUNE)) {
                        bl = true;
                        continue;
                    }
                    bl2 = this.world.removeBlock(blockPos, false) || bl2;
                }
            }
        }
        if (bl2) {
            BlockPos o = new BlockPos(i + this.random.nextInt(l - i + 1), j + this.random.nextInt(m - j + 1), k + this.random.nextInt(n - k + 1));
            this.world.syncWorldEvent(WorldEvents.ENDER_DRAGON_BREAKS_BLOCK, o, 0);
        }
        return bl;
    }

    public boolean damagePart(EnderDragonPart part, DamageSource source, float amount) {
        if (this.phaseManager.getCurrent().getType() == PhaseType.DYING) {
            return false;
        }
        amount = this.phaseManager.getCurrent().modifyDamageTaken(source, amount);
        if (part != this.head) {
            amount = amount / 4.0f + Math.min(amount, 1.0f);
        }
        if (amount < 0.01f) {
            return false;
        }
        if (source.getAttacker() instanceof PlayerEntity || source.isExplosive()) {
            float f = this.getHealth();
            this.parentDamage(source, amount);
            if (this.isDead() && !this.phaseManager.getCurrent().isSittingOrHovering()) {
                this.setHealth(1.0f);
                this.phaseManager.setPhase(PhaseType.DYING);
            }
            if (this.phaseManager.getCurrent().isSittingOrHovering()) {
                this.damageDuringSitting = (int)((float)this.damageDuringSitting + (f - this.getHealth()));
                if ((float)this.damageDuringSitting > 0.25f * this.getMaxHealth()) {
                    this.damageDuringSitting = 0;
                    this.phaseManager.setPhase(PhaseType.TAKEOFF);
                }
            }
        }
        return true;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source instanceof EntityDamageSource && ((EntityDamageSource)source).isThorns()) {
            this.damagePart(this.body, source, amount);
        }
        return false;
    }

    protected boolean parentDamage(DamageSource source, float amount) {
        return super.damage(source, amount);
    }

    @Override
    public void kill() {
        this.remove(Entity.RemovalReason.KILLED);
        if (this.fight != null) {
            this.fight.updateFight(this);
            this.fight.dragonKilled(this);
        }
    }

    @Override
    protected void updatePostDeath() {
        if (this.fight != null) {
            this.fight.updateFight(this);
        }
        ++this.ticksSinceDeath;
        if (this.ticksSinceDeath >= 180 && this.ticksSinceDeath <= 200) {
            float f = (this.random.nextFloat() - 0.5f) * 8.0f;
            float g = (this.random.nextFloat() - 0.5f) * 4.0f;
            float h = (this.random.nextFloat() - 0.5f) * 8.0f;
            this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.getX() + (double)f, this.getY() + 2.0 + (double)g, this.getZ() + (double)h, 0.0, 0.0, 0.0);
        }
        boolean f = this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT);
        int g = 500;
        if (this.fight != null && !this.fight.hasPreviouslyKilled()) {
            g = 12000;
        }
        if (this.world instanceof ServerWorld) {
            if (this.ticksSinceDeath > 150 && this.ticksSinceDeath % 5 == 0 && f) {
                ExperienceOrbEntity.spawn((ServerWorld)this.world, this.getPos(), MathHelper.floor((float)g * 0.08f));
            }
            if (this.ticksSinceDeath == 1 && !this.isSilent()) {
                this.world.syncGlobalEvent(WorldEvents.ENDER_DRAGON_DIES, this.getBlockPos(), 0);
            }
        }
        this.move(MovementType.SELF, new Vec3d(0.0, 0.1f, 0.0));
        this.setYaw(this.getYaw() + 20.0f);
        this.bodyYaw = this.getYaw();
        if (this.ticksSinceDeath == 200 && this.world instanceof ServerWorld) {
            if (f) {
                ExperienceOrbEntity.spawn((ServerWorld)this.world, this.getPos(), MathHelper.floor((float)g * 0.2f));
            }
            if (this.fight != null) {
                this.fight.dragonKilled(this);
            }
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    public int getNearestPathNodeIndex() {
        if (this.pathNodes[0] == null) {
            for (int i = 0; i < 24; ++i) {
                int m;
                int l;
                int j = 5;
                int k = i;
                if (i < 12) {
                    l = MathHelper.floor(60.0f * MathHelper.cos(2.0f * ((float)(-Math.PI) + 0.2617994f * (float)k)));
                    m = MathHelper.floor(60.0f * MathHelper.sin(2.0f * ((float)(-Math.PI) + 0.2617994f * (float)k)));
                } else if (i < 20) {
                    l = MathHelper.floor(40.0f * MathHelper.cos(2.0f * ((float)(-Math.PI) + 0.3926991f * (float)(k -= 12))));
                    m = MathHelper.floor(40.0f * MathHelper.sin(2.0f * ((float)(-Math.PI) + 0.3926991f * (float)k)));
                    j += 10;
                } else {
                    l = MathHelper.floor(20.0f * MathHelper.cos(2.0f * ((float)(-Math.PI) + 0.7853982f * (float)(k -= 20))));
                    m = MathHelper.floor(20.0f * MathHelper.sin(2.0f * ((float)(-Math.PI) + 0.7853982f * (float)k)));
                }
                int n = Math.max(this.world.getSeaLevel() + 10, this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(l, 0, m)).getY() + j);
                this.pathNodes[i] = new PathNode(l, n, m);
            }
            this.pathNodeConnections[0] = 6146;
            this.pathNodeConnections[1] = 8197;
            this.pathNodeConnections[2] = 8202;
            this.pathNodeConnections[3] = 16404;
            this.pathNodeConnections[4] = 32808;
            this.pathNodeConnections[5] = 32848;
            this.pathNodeConnections[6] = 65696;
            this.pathNodeConnections[7] = 131392;
            this.pathNodeConnections[8] = 131712;
            this.pathNodeConnections[9] = 263424;
            this.pathNodeConnections[10] = 526848;
            this.pathNodeConnections[11] = 525313;
            this.pathNodeConnections[12] = 1581057;
            this.pathNodeConnections[13] = 3166214;
            this.pathNodeConnections[14] = 2138120;
            this.pathNodeConnections[15] = 6373424;
            this.pathNodeConnections[16] = 4358208;
            this.pathNodeConnections[17] = 12910976;
            this.pathNodeConnections[18] = 9044480;
            this.pathNodeConnections[19] = 9706496;
            this.pathNodeConnections[20] = 15216640;
            this.pathNodeConnections[21] = 0xD0E000;
            this.pathNodeConnections[22] = 11763712;
            this.pathNodeConnections[23] = 0x7E0000;
        }
        return this.getNearestPathNodeIndex(this.getX(), this.getY(), this.getZ());
    }

    public int getNearestPathNodeIndex(double x, double y, double z) {
        float f = 10000.0f;
        int i = 0;
        PathNode pathNode = new PathNode(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
        int j = 0;
        if (this.fight == null || this.fight.getAliveEndCrystals() == 0) {
            j = 12;
        }
        for (int k = j; k < 24; ++k) {
            float g;
            if (this.pathNodes[k] == null || !((g = this.pathNodes[k].getSquaredDistance(pathNode)) < f)) continue;
            f = g;
            i = k;
        }
        return i;
    }

    @Nullable
    public Path findPath(int from, int to, @Nullable PathNode pathNode) {
        PathNode pathNode2;
        for (int i = 0; i < 24; ++i) {
            pathNode2 = this.pathNodes[i];
            pathNode2.visited = false;
            pathNode2.heapWeight = 0.0f;
            pathNode2.penalizedPathLength = 0.0f;
            pathNode2.distanceToNearestTarget = 0.0f;
            pathNode2.previous = null;
            pathNode2.heapIndex = -1;
        }
        PathNode i = this.pathNodes[from];
        pathNode2 = this.pathNodes[to];
        i.penalizedPathLength = 0.0f;
        i.heapWeight = i.distanceToNearestTarget = i.getDistance(pathNode2);
        this.pathHeap.clear();
        this.pathHeap.push(i);
        PathNode pathNode3 = i;
        int j = 0;
        if (this.fight == null || this.fight.getAliveEndCrystals() == 0) {
            j = 12;
        }
        while (!this.pathHeap.isEmpty()) {
            int l;
            PathNode pathNode4 = this.pathHeap.pop();
            if (pathNode4.equals(pathNode2)) {
                if (pathNode != null) {
                    pathNode.previous = pathNode2;
                    pathNode2 = pathNode;
                }
                return this.getPathOfAllPredecessors(i, pathNode2);
            }
            if (pathNode4.getDistance(pathNode2) < pathNode3.getDistance(pathNode2)) {
                pathNode3 = pathNode4;
            }
            pathNode4.visited = true;
            int k = 0;
            for (l = 0; l < 24; ++l) {
                if (this.pathNodes[l] != pathNode4) continue;
                k = l;
                break;
            }
            for (l = j; l < 24; ++l) {
                if ((this.pathNodeConnections[k] & 1 << l) <= 0) continue;
                PathNode pathNode5 = this.pathNodes[l];
                if (pathNode5.visited) continue;
                float f = pathNode4.penalizedPathLength + pathNode4.getDistance(pathNode5);
                if (pathNode5.isInHeap() && !(f < pathNode5.penalizedPathLength)) continue;
                pathNode5.previous = pathNode4;
                pathNode5.penalizedPathLength = f;
                pathNode5.distanceToNearestTarget = pathNode5.getDistance(pathNode2);
                if (pathNode5.isInHeap()) {
                    this.pathHeap.setNodeWeight(pathNode5, pathNode5.penalizedPathLength + pathNode5.distanceToNearestTarget);
                    continue;
                }
                pathNode5.heapWeight = pathNode5.penalizedPathLength + pathNode5.distanceToNearestTarget;
                this.pathHeap.push(pathNode5);
            }
        }
        if (pathNode3 == i) {
            return null;
        }
        LOGGER.debug("Failed to find path from {} to {}", (Object)from, (Object)to);
        if (pathNode != null) {
            pathNode.previous = pathNode3;
            pathNode3 = pathNode;
        }
        return this.getPathOfAllPredecessors(i, pathNode3);
    }

    private Path getPathOfAllPredecessors(PathNode unused, PathNode node) {
        ArrayList<PathNode> list = Lists.newArrayList();
        PathNode pathNode = node;
        list.add(0, pathNode);
        while (pathNode.previous != null) {
            pathNode = pathNode.previous;
            list.add(0, pathNode);
        }
        return new Path(list, new BlockPos(node.x, node.y, node.z), true);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt(DRAGON_PHASE_KEY, this.phaseManager.getCurrent().getType().getTypeId());
        nbt.putInt(DRAGON_DEATH_TIME_KEY, this.ticksSinceDeath);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains(DRAGON_PHASE_KEY)) {
            this.phaseManager.setPhase(PhaseType.getFromId(nbt.getInt(DRAGON_PHASE_KEY)));
        }
        if (nbt.contains(DRAGON_DEATH_TIME_KEY)) {
            this.ticksSinceDeath = nbt.getInt(DRAGON_DEATH_TIME_KEY);
        }
    }

    @Override
    public void checkDespawn() {
    }

    public EnderDragonPart[] getBodyParts() {
        return this.parts;
    }

    @Override
    public boolean collides() {
        return false;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ENDER_DRAGON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ENDER_DRAGON_HURT;
    }

    @Override
    protected float getSoundVolume() {
        return 5.0f;
    }

    public float getChangeInNeckPitch(int segmentOffset, double[] segment1, double[] segment2) {
        double e;
        Phase phase = this.phaseManager.getCurrent();
        PhaseType<? extends Phase> phaseType = phase.getType();
        if (phaseType == PhaseType.LANDING || phaseType == PhaseType.TAKEOFF) {
            BlockPos blockPos = this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.ORIGIN);
            double d = Math.max(Math.sqrt(blockPos.getSquaredDistance(this.getPos(), true)) / 4.0, 1.0);
            e = (double)segmentOffset / d;
        } else {
            e = phase.isSittingOrHovering() ? (double)segmentOffset : (segmentOffset == 6 ? 0.0 : segment2[1] - segment1[1]);
        }
        return (float)e;
    }

    public Vec3d getRotationVectorFromPhase(float tickDelta) {
        Vec3d vec3d;
        Phase phase = this.phaseManager.getCurrent();
        PhaseType<? extends Phase> phaseType = phase.getType();
        if (phaseType == PhaseType.LANDING || phaseType == PhaseType.TAKEOFF) {
            BlockPos blockPos = this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.ORIGIN);
            float f = Math.max((float)Math.sqrt(blockPos.getSquaredDistance(this.getPos(), true)) / 4.0f, 1.0f);
            float g = 6.0f / f;
            float h = this.getPitch();
            float i = 1.5f;
            this.setPitch(-g * 1.5f * 5.0f);
            vec3d = this.getRotationVec(tickDelta);
            this.setPitch(h);
        } else if (phase.isSittingOrHovering()) {
            float blockPos = this.getPitch();
            float f = 1.5f;
            this.setPitch(-45.0f);
            vec3d = this.getRotationVec(tickDelta);
            this.setPitch(blockPos);
        } else {
            vec3d = this.getRotationVec(tickDelta);
        }
        return vec3d;
    }

    public void crystalDestroyed(EndCrystalEntity crystal, BlockPos pos, DamageSource source) {
        PlayerEntity playerEntity = source.getAttacker() instanceof PlayerEntity ? (PlayerEntity)source.getAttacker() : this.world.getClosestPlayer(CLOSE_PLAYER_PREDICATE, pos.getX(), pos.getY(), pos.getZ());
        if (crystal == this.connectedCrystal) {
            this.damagePart(this.head, DamageSource.explosion(playerEntity), 10.0f);
        }
        this.phaseManager.getCurrent().crystalDestroyed(crystal, pos, source, playerEntity);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (PHASE_TYPE.equals(data) && this.world.isClient) {
            this.phaseManager.setPhase(PhaseType.getFromId(this.getDataTracker().get(PHASE_TYPE)));
        }
        super.onTrackedDataSet(data);
    }

    public PhaseManager getPhaseManager() {
        return this.phaseManager;
    }

    @Nullable
    public EnderDragonFight getFight() {
        return this.fight;
    }

    @Override
    public boolean addStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
        return false;
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
    public void readFromPacket(MobSpawnS2CPacket packet) {
        super.readFromPacket(packet);
        EnderDragonPart[] enderDragonParts = this.getBodyParts();
        for (int i = 0; i < enderDragonParts.length; ++i) {
            enderDragonParts[i].setId(i + packet.getId());
        }
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        return target.canTakeDamage();
    }
}

