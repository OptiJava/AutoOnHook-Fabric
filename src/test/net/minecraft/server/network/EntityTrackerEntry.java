/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityTrackerEntry {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int field_29767 = 1;
    private final ServerWorld world;
    private final Entity entity;
    private final int tickInterval;
    private final boolean alwaysUpdateVelocity;
    private final Consumer<Packet<?>> receiver;
    private long lastX;
    private long lastY;
    private long lastZ;
    private int lastYaw;
    private int lastPitch;
    private int lastHeadPitch;
    private Vec3d velocity = Vec3d.ZERO;
    private int trackingTick;
    private int updatesWithoutVehicle;
    private List<Entity> lastPassengers = Collections.emptyList();
    private boolean hadVehicle;
    private boolean lastOnGround;

    public EntityTrackerEntry(ServerWorld world, Entity entity, int tickInterval, boolean alwaysUpdateVelocity, Consumer<Packet<?>> receiver) {
        this.world = world;
        this.receiver = receiver;
        this.entity = entity;
        this.tickInterval = tickInterval;
        this.alwaysUpdateVelocity = alwaysUpdateVelocity;
        this.storeEncodedCoordinates();
        this.lastYaw = MathHelper.floor(entity.getYaw() * 256.0f / 360.0f);
        this.lastPitch = MathHelper.floor(entity.getPitch() * 256.0f / 360.0f);
        this.lastHeadPitch = MathHelper.floor(entity.getHeadYaw() * 256.0f / 360.0f);
        this.lastOnGround = entity.isOnGround();
    }

    public void tick() {
        Object integer;
        List<Entity> list = this.entity.getPassengerList();
        if (!list.equals(this.lastPassengers)) {
            this.lastPassengers = list;
            this.receiver.accept(new EntityPassengersSetS2CPacket(this.entity));
        }
        if (this.entity instanceof ItemFrameEntity && this.trackingTick % 10 == 0) {
            MapState mapState;
            ItemFrameEntity itemFrameEntity = (ItemFrameEntity)this.entity;
            ItemStack itemStack = itemFrameEntity.getHeldItemStack();
            if (itemStack.getItem() instanceof FilledMapItem && (mapState = FilledMapItem.getMapState((Integer)(integer = FilledMapItem.getMapId(itemStack)), this.world)) != null) {
                for (ServerPlayerEntity serverPlayerEntity : this.world.getPlayers()) {
                    mapState.update(serverPlayerEntity, itemStack);
                    Packet<?> packet = mapState.getPlayerMarkerPacket((Integer)integer, serverPlayerEntity);
                    if (packet == null) continue;
                    serverPlayerEntity.networkHandler.sendPacket(packet);
                }
            }
            this.syncEntityData();
        }
        if (this.trackingTick % this.tickInterval == 0 || this.entity.velocityDirty || this.entity.getDataTracker().isDirty()) {
            int itemFrameEntity;
            if (this.entity.hasVehicle()) {
                boolean integer2;
                itemFrameEntity = MathHelper.floor(this.entity.getYaw() * 256.0f / 360.0f);
                int itemStack = MathHelper.floor(this.entity.getPitch() * 256.0f / 360.0f);
                boolean bl = integer2 = Math.abs(itemFrameEntity - this.lastYaw) >= 1 || Math.abs(itemStack - this.lastPitch) >= 1;
                if (integer2) {
                    this.receiver.accept(new EntityS2CPacket.Rotate(this.entity.getId(), (byte)itemFrameEntity, (byte)itemStack, this.entity.isOnGround()));
                    this.lastYaw = itemFrameEntity;
                    this.lastPitch = itemStack;
                }
                this.storeEncodedCoordinates();
                this.syncEntityData();
                this.hadVehicle = true;
            } else {
                Vec3d l;
                double d;
                boolean packet;
                ++this.updatesWithoutVehicle;
                itemFrameEntity = MathHelper.floor(this.entity.getYaw() * 256.0f / 360.0f);
                int itemStack = MathHelper.floor(this.entity.getPitch() * 256.0f / 360.0f);
                integer = this.entity.getPos().subtract(EntityS2CPacket.decodePacketCoordinates(this.lastX, this.lastY, this.lastZ));
                boolean mapState = ((Vec3d)integer).lengthSquared() >= 7.62939453125E-6;
                Packet<ClientPlayPacketListener> packet2 = null;
                boolean serverPlayerEntity = mapState || this.trackingTick % 60 == 0;
                boolean bl = packet = Math.abs(itemFrameEntity - this.lastYaw) >= 1 || Math.abs(itemStack - this.lastPitch) >= 1;
                if (this.trackingTick > 0 || this.entity instanceof PersistentProjectileEntity) {
                    boolean bl2;
                    long l2 = EntityS2CPacket.encodePacketCoordinate(((Vec3d)integer).x);
                    long m = EntityS2CPacket.encodePacketCoordinate(((Vec3d)integer).y);
                    long n = EntityS2CPacket.encodePacketCoordinate(((Vec3d)integer).z);
                    boolean bl3 = bl2 = l2 < -32768L || l2 > 32767L || m < -32768L || m > 32767L || n < -32768L || n > 32767L;
                    if (bl2 || this.updatesWithoutVehicle > 400 || this.hadVehicle || this.lastOnGround != this.entity.isOnGround()) {
                        this.lastOnGround = this.entity.isOnGround();
                        this.updatesWithoutVehicle = 0;
                        packet2 = new EntityPositionS2CPacket(this.entity);
                    } else if (serverPlayerEntity && packet || this.entity instanceof PersistentProjectileEntity) {
                        packet2 = new EntityS2CPacket.RotateAndMoveRelative(this.entity.getId(), (short)l2, (short)m, (short)n, (byte)itemFrameEntity, (byte)itemStack, this.entity.isOnGround());
                    } else if (serverPlayerEntity) {
                        packet2 = new EntityS2CPacket.MoveRelative(this.entity.getId(), (short)l2, (short)m, (short)n, this.entity.isOnGround());
                    } else if (packet) {
                        packet2 = new EntityS2CPacket.Rotate(this.entity.getId(), (byte)itemFrameEntity, (byte)itemStack, this.entity.isOnGround());
                    }
                }
                if ((this.alwaysUpdateVelocity || this.entity.velocityDirty || this.entity instanceof LivingEntity && ((LivingEntity)this.entity).isFallFlying()) && this.trackingTick > 0 && ((d = (l = this.entity.getVelocity()).squaredDistanceTo(this.velocity)) > 1.0E-7 || d > 0.0 && l.lengthSquared() == 0.0)) {
                    this.velocity = l;
                    this.receiver.accept(new EntityVelocityUpdateS2CPacket(this.entity.getId(), this.velocity));
                }
                if (packet2 != null) {
                    this.receiver.accept(packet2);
                }
                this.syncEntityData();
                if (serverPlayerEntity) {
                    this.storeEncodedCoordinates();
                }
                if (packet) {
                    this.lastYaw = itemFrameEntity;
                    this.lastPitch = itemStack;
                }
                this.hadVehicle = false;
            }
            itemFrameEntity = MathHelper.floor(this.entity.getHeadYaw() * 256.0f / 360.0f);
            if (Math.abs(itemFrameEntity - this.lastHeadPitch) >= 1) {
                this.receiver.accept(new EntitySetHeadYawS2CPacket(this.entity, (byte)itemFrameEntity));
                this.lastHeadPitch = itemFrameEntity;
            }
            this.entity.velocityDirty = false;
        }
        ++this.trackingTick;
        if (this.entity.velocityModified) {
            this.sendSyncPacket(new EntityVelocityUpdateS2CPacket(this.entity));
            this.entity.velocityModified = false;
        }
    }

    public void stopTracking(ServerPlayerEntity player) {
        this.entity.onStoppedTrackingBy(player);
        player.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(this.entity.getId()));
    }

    public void startTracking(ServerPlayerEntity player) {
        this.sendPackets(player.networkHandler::sendPacket);
        this.entity.onStartedTrackingBy(player);
    }

    public void sendPackets(Consumer<Packet<?>> sender) {
        Collection<EntityAttributeInstance> collection;
        if (this.entity.isRemoved()) {
            LOGGER.warn("Fetching packet for removed entity {}", (Object)this.entity);
        }
        Packet<?> packet = this.entity.createSpawnPacket();
        this.lastHeadPitch = MathHelper.floor(this.entity.getHeadYaw() * 256.0f / 360.0f);
        sender.accept(packet);
        if (!this.entity.getDataTracker().isEmpty()) {
            sender.accept(new EntityTrackerUpdateS2CPacket(this.entity.getId(), this.entity.getDataTracker(), true));
        }
        boolean bl = this.alwaysUpdateVelocity;
        if (this.entity instanceof LivingEntity) {
            collection = ((LivingEntity)this.entity).getAttributes().getAttributesToSend();
            if (!collection.isEmpty()) {
                sender.accept(new EntityAttributesS2CPacket(this.entity.getId(), collection));
            }
            if (((LivingEntity)this.entity).isFallFlying()) {
                bl = true;
            }
        }
        this.velocity = this.entity.getVelocity();
        if (bl && !(packet instanceof MobSpawnS2CPacket)) {
            sender.accept(new EntityVelocityUpdateS2CPacket(this.entity.getId(), this.velocity));
        }
        if (this.entity instanceof LivingEntity) {
            collection = Lists.newArrayList();
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                ItemStack itemStack = ((LivingEntity)this.entity).getEquippedStack(equipmentSlot);
                if (itemStack.isEmpty()) continue;
                collection.add((EntityAttributeInstance)((Object)Pair.of(equipmentSlot, itemStack.copy())));
            }
            if (!collection.isEmpty()) {
                sender.accept(new EntityEquipmentUpdateS2CPacket(this.entity.getId(), (List<Pair<EquipmentSlot, ItemStack>>)collection));
            }
        }
        if (this.entity instanceof LivingEntity) {
            collection = (LivingEntity)this.entity;
            for (StatusEffectInstance statusEffectInstance : ((LivingEntity)((Object)collection)).getStatusEffects()) {
                sender.accept(new EntityStatusEffectS2CPacket(this.entity.getId(), statusEffectInstance));
            }
        }
        if (!this.entity.getPassengerList().isEmpty()) {
            sender.accept(new EntityPassengersSetS2CPacket(this.entity));
        }
        if (this.entity.hasVehicle()) {
            sender.accept(new EntityPassengersSetS2CPacket(this.entity.getVehicle()));
        }
        if (this.entity instanceof MobEntity && ((MobEntity)((Object)(collection = (MobEntity)this.entity))).isLeashed()) {
            sender.accept(new EntityAttachS2CPacket((Entity)((Object)collection), ((MobEntity)((Object)collection)).getHoldingEntity()));
        }
    }

    /**
     * Synchronizes tracked data and attributes
     */
    private void syncEntityData() {
        DataTracker dataTracker = this.entity.getDataTracker();
        if (dataTracker.isDirty()) {
            this.sendSyncPacket(new EntityTrackerUpdateS2CPacket(this.entity.getId(), dataTracker, false));
        }
        if (this.entity instanceof LivingEntity) {
            Set<EntityAttributeInstance> set = ((LivingEntity)this.entity).getAttributes().getTracked();
            if (!set.isEmpty()) {
                this.sendSyncPacket(new EntityAttributesS2CPacket(this.entity.getId(), set));
            }
            set.clear();
        }
    }

    /**
     * Stores the tracked entity's current coordinates encoded as lastX/Y/Z
     */
    private void storeEncodedCoordinates() {
        this.lastX = EntityS2CPacket.encodePacketCoordinate(this.entity.getX());
        this.lastY = EntityS2CPacket.encodePacketCoordinate(this.entity.getY());
        this.lastZ = EntityS2CPacket.encodePacketCoordinate(this.entity.getZ());
    }

    /**
     * Decodes lastX/Y/Z into a position vector
     */
    public Vec3d getLastPos() {
        return EntityS2CPacket.decodePacketCoordinates(this.lastX, this.lastY, this.lastZ);
    }

    /**
     * Sends a packet for synchronization with watcher and tracked player (if applicable)
     */
    private void sendSyncPacket(Packet<?> packet) {
        this.receiver.accept(packet);
        if (this.entity instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity)this.entity).networkHandler.sendPacket(packet);
        }
    }
}

