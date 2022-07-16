/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.network;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.screen.ingame.CommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.JigsawBlockScreen;
import net.minecraft.client.gui.screen.ingame.MinecartCommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.gui.screen.ingame.StructureBlockScreen;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.sound.AmbientSoundLoops;
import net.minecraft.client.sound.AmbientSoundPlayer;
import net.minecraft.client.sound.BiomeEffectSoundPlayer;
import net.minecraft.client.sound.BubbleColumnSoundPlayer;
import net.minecraft.client.sound.ElytraSoundInstance;
import net.minecraft.client.sound.MinecartInsideSoundInstance;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.ClientPlayerTickable;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeBookDataC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Recipe;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.StatHandler;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.CommandBlockExecutor;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the client's own player entity.
 */
@Environment(value=EnvType.CLIENT)
public class ClientPlayerEntity
extends AbstractClientPlayerEntity {
    private static final int field_32671 = 20;
    private static final int field_32672 = 600;
    private static final int field_32673 = 100;
    private static final float field_32674 = 0.6f;
    private static final double field_32675 = 0.35;
    public final ClientPlayNetworkHandler networkHandler;
    private final StatHandler statHandler;
    private final ClientRecipeBook recipeBook;
    private final List<ClientPlayerTickable> tickables = Lists.newArrayList();
    private int clientPermissionLevel = 0;
    private double lastX;
    private double lastBaseY;
    private double lastZ;
    private float lastYaw;
    private float lastPitch;
    private boolean lastOnGround;
    private boolean inSneakingPose;
    private boolean lastSneaking;
    private boolean lastSprinting;
    private int ticksSinceLastPositionPacketSent;
    private boolean healthInitialized;
    private String serverBrand;
    public Input input;
    protected final MinecraftClient client;
    protected int ticksLeftToDoubleTapSprint;
    public int ticksSinceSprintingChanged;
    public float renderYaw;
    public float renderPitch;
    public float lastRenderYaw;
    public float lastRenderPitch;
    private int field_3938;
    private float mountJumpStrength;
    public float nextNauseaStrength;
    public float lastNauseaStrength;
    private boolean usingItem;
    private Hand activeHand;
    private boolean riding;
    private boolean autoJumpEnabled = true;
    private int ticksToNextAutojump;
    private boolean falling;
    private int underwaterVisibilityTicks;
    private boolean showsDeathScreen = true;

    public ClientPlayerEntity(MinecraftClient client, ClientWorld world, ClientPlayNetworkHandler networkHandler, StatHandler stats, ClientRecipeBook recipeBook, boolean lastSneaking, boolean lastSprinting) {
        super(world, networkHandler.getProfile());
        this.client = client;
        this.networkHandler = networkHandler;
        this.statHandler = stats;
        this.recipeBook = recipeBook;
        this.lastSneaking = lastSneaking;
        this.lastSprinting = lastSprinting;
        this.tickables.add(new AmbientSoundPlayer(this, client.getSoundManager()));
        this.tickables.add(new BubbleColumnSoundPlayer(this));
        this.tickables.add(new BiomeEffectSoundPlayer(this, client.getSoundManager(), world.getBiomeAccess()));
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }

    @Override
    public void heal(float amount) {
    }

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        if (!super.startRiding(entity, force)) {
            return false;
        }
        if (entity instanceof AbstractMinecartEntity) {
            this.client.getSoundManager().play(new MinecartInsideSoundInstance(this, (AbstractMinecartEntity)entity, true));
            this.client.getSoundManager().play(new MinecartInsideSoundInstance(this, (AbstractMinecartEntity)entity, false));
        }
        if (entity instanceof BoatEntity) {
            this.prevYaw = entity.getYaw();
            this.setYaw(entity.getYaw());
            this.setHeadYaw(entity.getYaw());
        }
        return true;
    }

    @Override
    public void dismountVehicle() {
        super.dismountVehicle();
        this.riding = false;
    }

    @Override
    public float getPitch(float tickDelta) {
        return this.getPitch();
    }

    @Override
    public float getYaw(float tickDelta) {
        if (this.hasVehicle()) {
            return super.getYaw(tickDelta);
        }
        return this.getYaw();
    }

    @Override
    public void tick() {
        if (!this.world.isPosLoaded(this.getBlockX(), this.getBlockZ())) {
            return;
        }
        super.tick();
        if (this.hasVehicle()) {
            this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(this.getYaw(), this.getPitch(), this.onGround));
            this.networkHandler.sendPacket(new PlayerInputC2SPacket(this.sidewaysSpeed, this.forwardSpeed, this.input.jumping, this.input.sneaking));
            Entity entity = this.getRootVehicle();
            if (entity != this && entity.isLogicalSideForUpdatingMovement()) {
                this.networkHandler.sendPacket(new VehicleMoveC2SPacket(entity));
            }
        } else {
            this.sendMovementPackets();
        }
        for (ClientPlayerTickable clientPlayerTickable : this.tickables) {
            clientPlayerTickable.tick();
        }
    }

    /**
     * {@return the percentage for the biome mood sound for the debug HUD to
     * display}
     */
    public float getMoodPercentage() {
        for (ClientPlayerTickable clientPlayerTickable : this.tickables) {
            if (!(clientPlayerTickable instanceof BiomeEffectSoundPlayer)) continue;
            return ((BiomeEffectSoundPlayer)clientPlayerTickable).getMoodPercentage();
        }
        return 0.0f;
    }

    private void sendMovementPackets() {
        boolean mode;
        boolean bl = this.isSprinting();
        if (bl != this.lastSprinting) {
            ClientCommandC2SPacket.Mode mode2 = bl ? ClientCommandC2SPacket.Mode.START_SPRINTING : ClientCommandC2SPacket.Mode.STOP_SPRINTING;
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, mode2));
            this.lastSprinting = bl;
        }
        if ((mode = this.isSneaking()) != this.lastSneaking) {
            ClientCommandC2SPacket.Mode mode2 = mode ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, mode2));
            this.lastSneaking = mode;
        }
        if (this.isCamera()) {
            boolean bl3;
            double mode2 = this.getX() - this.lastX;
            double d = this.getY() - this.lastBaseY;
            double e = this.getZ() - this.lastZ;
            double f = this.getYaw() - this.lastYaw;
            double g = this.getPitch() - this.lastPitch;
            ++this.ticksSinceLastPositionPacketSent;
            boolean bl2 = mode2 * mode2 + d * d + e * e > 9.0E-4 || this.ticksSinceLastPositionPacketSent >= 20;
            boolean bl4 = bl3 = f != 0.0 || g != 0.0;
            if (this.hasVehicle()) {
                Vec3d vec3d = this.getVelocity();
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(vec3d.x, -999.0, vec3d.z, this.getYaw(), this.getPitch(), this.onGround));
                bl2 = false;
            } else if (bl2 && bl3) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch(), this.onGround));
            } else if (bl2) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.getX(), this.getY(), this.getZ(), this.onGround));
            } else if (bl3) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(this.getYaw(), this.getPitch(), this.onGround));
            } else if (this.lastOnGround != this.onGround) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(this.onGround));
            }
            if (bl2) {
                this.lastX = this.getX();
                this.lastBaseY = this.getY();
                this.lastZ = this.getZ();
                this.ticksSinceLastPositionPacketSent = 0;
            }
            if (bl3) {
                this.lastYaw = this.getYaw();
                this.lastPitch = this.getPitch();
            }
            this.lastOnGround = this.onGround;
            this.autoJumpEnabled = this.client.options.autoJump;
        }
    }

    public boolean dropSelectedItem(boolean entireStack) {
        PlayerActionC2SPacket.Action action = entireStack ? PlayerActionC2SPacket.Action.DROP_ALL_ITEMS : PlayerActionC2SPacket.Action.DROP_ITEM;
        ItemStack itemStack = this.getInventory().dropSelectedItem(entireStack);
        this.networkHandler.sendPacket(new PlayerActionC2SPacket(action, BlockPos.ORIGIN, Direction.DOWN));
        return !itemStack.isEmpty();
    }

    public void sendChatMessage(String message) {
        this.networkHandler.sendPacket(new ChatMessageC2SPacket(message));
    }

    @Override
    public void swingHand(Hand hand) {
        super.swingHand(hand);
        this.networkHandler.sendPacket(new HandSwingC2SPacket(hand));
    }

    @Override
    public void requestRespawn() {
        this.networkHandler.sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
    }

    @Override
    protected void applyDamage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return;
        }
        this.setHealth(this.getHealth() - amount);
    }

    @Override
    public void closeHandledScreen() {
        this.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(this.currentScreenHandler.syncId));
        this.closeScreen();
    }

    public void closeScreen() {
        super.closeHandledScreen();
        this.client.setScreen(null);
    }

    public void updateHealth(float health) {
        if (this.healthInitialized) {
            float f = this.getHealth() - health;
            if (f <= 0.0f) {
                this.setHealth(health);
                if (f < 0.0f) {
                    this.timeUntilRegen = 10;
                }
            } else {
                this.lastDamageTaken = f;
                this.timeUntilRegen = 20;
                this.setHealth(health);
                this.hurtTime = this.maxHurtTime = 10;
            }
        } else {
            this.setHealth(health);
            this.healthInitialized = true;
        }
    }

    @Override
    public void sendAbilitiesUpdate() {
        this.networkHandler.sendPacket(new UpdatePlayerAbilitiesC2SPacket(this.getAbilities()));
    }

    @Override
    public boolean isMainPlayer() {
        return true;
    }

    @Override
    public boolean isHoldingOntoLadder() {
        return !this.getAbilities().flying && super.isHoldingOntoLadder();
    }

    @Override
    public boolean shouldSpawnSprintingParticles() {
        return !this.getAbilities().flying && super.shouldSpawnSprintingParticles();
    }

    @Override
    public boolean shouldDisplaySoulSpeedEffects() {
        return !this.getAbilities().flying && super.shouldDisplaySoulSpeedEffects();
    }

    protected void startRidingJump() {
        this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.START_RIDING_JUMP, MathHelper.floor(this.getMountJumpStrength() * 100.0f)));
    }

    public void openRidingInventory() {
        this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.OPEN_INVENTORY));
    }

    public void setServerBrand(String serverBrand) {
        this.serverBrand = serverBrand;
    }

    public String getServerBrand() {
        return this.serverBrand;
    }

    public StatHandler getStatHandler() {
        return this.statHandler;
    }

    public ClientRecipeBook getRecipeBook() {
        return this.recipeBook;
    }

    public void onRecipeDisplayed(Recipe<?> recipe) {
        if (this.recipeBook.shouldDisplay(recipe)) {
            this.recipeBook.onRecipeDisplayed(recipe);
            this.networkHandler.sendPacket(new RecipeBookDataC2SPacket(recipe));
        }
    }

    @Override
    protected int getPermissionLevel() {
        return this.clientPermissionLevel;
    }

    public void setClientPermissionLevel(int clientPermissionLevel) {
        this.clientPermissionLevel = clientPermissionLevel;
    }

    @Override
    public void sendMessage(Text message, boolean actionBar) {
        if (actionBar) {
            this.client.inGameHud.setOverlayMessage(message, false);
        } else {
            this.client.inGameHud.getChatHud().addMessage(message);
        }
    }

    private void pushOutOfBlocks(double x, double z) {
        Direction[] directions;
        BlockPos blockPos = new BlockPos(x, this.getY(), z);
        if (!this.wouldCollideAt(blockPos)) {
            return;
        }
        double d = x - (double)blockPos.getX();
        double e = z - (double)blockPos.getZ();
        Direction direction = null;
        double f = Double.MAX_VALUE;
        for (Direction direction2 : directions = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}) {
            double h;
            double g = direction2.getAxis().choose(d, 0.0, e);
            double d2 = h = direction2.getDirection() == Direction.AxisDirection.POSITIVE ? 1.0 - g : g;
            if (!(h < f) || this.wouldCollideAt(blockPos.offset(direction2))) continue;
            f = h;
            direction = direction2;
        }
        if (direction != null) {
            Vec3d vec3d = this.getVelocity();
            if (direction.getAxis() == Direction.Axis.X) {
                this.setVelocity(0.1 * (double)direction.getOffsetX(), vec3d.y, vec3d.z);
            } else {
                this.setVelocity(vec3d.x, vec3d.y, 0.1 * (double)direction.getOffsetZ());
            }
        }
    }

    private boolean wouldCollideAt(BlockPos pos2) {
        Box box = this.getBoundingBox();
        Box box2 = new Box(pos2.getX(), box.minY, pos2.getZ(), (double)pos2.getX() + 1.0, box.maxY, (double)pos2.getZ() + 1.0).contract(1.0E-7);
        return this.world.hasBlockCollision(this, box2, (state, pos) -> state.shouldSuffocate(this.world, (BlockPos)pos));
    }

    @Override
    public void setSprinting(boolean sprinting) {
        super.setSprinting(sprinting);
        this.ticksSinceSprintingChanged = 0;
    }

    public void setExperience(float progress, int total, int level) {
        this.experienceProgress = progress;
        this.totalExperience = total;
        this.experienceLevel = level;
    }

    @Override
    public void sendSystemMessage(Text message, UUID sender) {
        this.client.inGameHud.getChatHud().addMessage(message);
    }

    @Override
    public void handleStatus(byte status) {
        if (status >= 24 && status <= 28) {
            this.setClientPermissionLevel(status - 24);
        } else {
            super.handleStatus(status);
        }
    }

    public void setShowsDeathScreen(boolean shouldShow) {
        this.showsDeathScreen = shouldShow;
    }

    public boolean showsDeathScreen() {
        return this.showsDeathScreen;
    }

    @Override
    public void playSound(SoundEvent sound, float volume, float pitch) {
        this.world.playSound(this.getX(), this.getY(), this.getZ(), sound, this.getSoundCategory(), volume, pitch, false);
    }

    @Override
    public void playSound(SoundEvent event, SoundCategory category, float volume, float pitch) {
        this.world.playSound(this.getX(), this.getY(), this.getZ(), event, category, volume, pitch, false);
    }

    @Override
    public boolean canMoveVoluntarily() {
        return true;
    }

    @Override
    public void setCurrentHand(Hand hand) {
        ItemStack itemStack = this.getStackInHand(hand);
        if (itemStack.isEmpty() || this.isUsingItem()) {
            return;
        }
        super.setCurrentHand(hand);
        this.usingItem = true;
        this.activeHand = hand;
    }

    @Override
    public boolean isUsingItem() {
        return this.usingItem;
    }

    @Override
    public void clearActiveItem() {
        super.clearActiveItem();
        this.usingItem = false;
    }

    @Override
    public Hand getActiveHand() {
        return this.activeHand;
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (LIVING_FLAGS.equals(data)) {
            Hand hand;
            boolean bl = ((Byte)this.dataTracker.get(LIVING_FLAGS) & 1) > 0;
            Hand hand2 = hand = ((Byte)this.dataTracker.get(LIVING_FLAGS) & 2) > 0 ? Hand.OFF_HAND : Hand.MAIN_HAND;
            if (bl && !this.usingItem) {
                this.setCurrentHand(hand);
            } else if (!bl && this.usingItem) {
                this.clearActiveItem();
            }
        }
        if (FLAGS.equals(data) && this.isFallFlying() && !this.falling) {
            this.client.getSoundManager().play(new ElytraSoundInstance(this));
        }
    }

    public boolean hasJumpingMount() {
        Entity entity = this.getVehicle();
        return this.hasVehicle() && entity instanceof JumpingMount && ((JumpingMount)((Object)entity)).canJump();
    }

    public float getMountJumpStrength() {
        return this.mountJumpStrength;
    }

    @Override
    public void openEditSignScreen(SignBlockEntity sign) {
        this.client.setScreen(new SignEditScreen(sign, this.client.shouldFilterText()));
    }

    @Override
    public void openCommandBlockMinecartScreen(CommandBlockExecutor commandBlockExecutor) {
        this.client.setScreen(new MinecartCommandBlockScreen(commandBlockExecutor));
    }

    @Override
    public void openCommandBlockScreen(CommandBlockBlockEntity commandBlock) {
        this.client.setScreen(new CommandBlockScreen(commandBlock));
    }

    @Override
    public void openStructureBlockScreen(StructureBlockBlockEntity structureBlock) {
        this.client.setScreen(new StructureBlockScreen(structureBlock));
    }

    @Override
    public void openJigsawScreen(JigsawBlockEntity jigsaw) {
        this.client.setScreen(new JigsawBlockScreen(jigsaw));
    }

    @Override
    public void useBook(ItemStack book, Hand hand) {
        if (book.isOf(Items.WRITABLE_BOOK)) {
            this.client.setScreen(new BookEditScreen(this, book, hand));
        }
    }

    @Override
    public void addCritParticles(Entity target) {
        this.client.particleManager.addEmitter(target, ParticleTypes.CRIT);
    }

    @Override
    public void addEnchantedHitParticles(Entity target) {
        this.client.particleManager.addEmitter(target, ParticleTypes.ENCHANTED_HIT);
    }

    @Override
    public boolean isSneaking() {
        return this.input != null && this.input.sneaking;
    }

    @Override
    public boolean isInSneakingPose() {
        return this.inSneakingPose;
    }

    public boolean shouldSlowDown() {
        return this.isInSneakingPose() || this.shouldLeaveSwimmingPose();
    }

    @Override
    public void tickNewAi() {
        super.tickNewAi();
        if (this.isCamera()) {
            this.sidewaysSpeed = this.input.movementSideways;
            this.forwardSpeed = this.input.movementForward;
            this.jumping = this.input.jumping;
            this.lastRenderYaw = this.renderYaw;
            this.lastRenderPitch = this.renderPitch;
            this.renderPitch = (float)((double)this.renderPitch + (double)(this.getPitch() - this.renderPitch) * 0.5);
            this.renderYaw = (float)((double)this.renderYaw + (double)(this.getYaw() - this.renderYaw) * 0.5);
        }
    }

    protected boolean isCamera() {
        return this.client.getCameraEntity() == this;
    }

    public void init() {
        this.setPose(EntityPose.STANDING);
        if (this.world != null) {
            for (double d = this.getY(); d > (double)this.world.getBottomY() && d < (double)this.world.getTopY(); d += 1.0) {
                this.setPosition(this.getX(), d, this.getZ());
                if (this.world.isSpaceEmpty(this)) break;
            }
            this.setVelocity(Vec3d.ZERO);
            this.setPitch(0.0f);
        }
        this.setHealth(this.getMaxHealth());
        this.deathTime = 0;
    }

    @Override
    public void tickMovement() {
        ItemStack bl7;
        int bl72;
        boolean bl6;
        boolean bl5;
        ++this.ticksSinceSprintingChanged;
        if (this.ticksLeftToDoubleTapSprint > 0) {
            --this.ticksLeftToDoubleTapSprint;
        }
        this.updateNausea();
        boolean bl = this.input.jumping;
        boolean bl2 = this.input.sneaking;
        boolean bl3 = this.isWalking();
        this.inSneakingPose = !this.getAbilities().flying && !this.isSwimming() && this.wouldPoseNotCollide(EntityPose.CROUCHING) && (this.isSneaking() || !this.isSleeping() && !this.wouldPoseNotCollide(EntityPose.STANDING));
        this.input.tick(this.shouldSlowDown());
        this.client.getTutorialManager().onMovement(this.input);
        if (this.isUsingItem() && !this.hasVehicle()) {
            this.input.movementSideways *= 0.2f;
            this.input.movementForward *= 0.2f;
            this.ticksLeftToDoubleTapSprint = 0;
        }
        boolean bl4 = false;
        if (this.ticksToNextAutojump > 0) {
            --this.ticksToNextAutojump;
            bl4 = true;
            this.input.jumping = true;
        }
        if (!this.noClip) {
            this.pushOutOfBlocks(this.getX() - (double)this.getWidth() * 0.35, this.getZ() + (double)this.getWidth() * 0.35);
            this.pushOutOfBlocks(this.getX() - (double)this.getWidth() * 0.35, this.getZ() - (double)this.getWidth() * 0.35);
            this.pushOutOfBlocks(this.getX() + (double)this.getWidth() * 0.35, this.getZ() - (double)this.getWidth() * 0.35);
            this.pushOutOfBlocks(this.getX() + (double)this.getWidth() * 0.35, this.getZ() + (double)this.getWidth() * 0.35);
        }
        if (bl2) {
            this.ticksLeftToDoubleTapSprint = 0;
        }
        boolean bl8 = bl5 = (float)this.getHungerManager().getFoodLevel() > 6.0f || this.getAbilities().allowFlying;
        if (!(!this.onGround && !this.isSubmergedInWater() || bl2 || bl3 || !this.isWalking() || this.isSprinting() || !bl5 || this.isUsingItem() || this.hasStatusEffect(StatusEffects.BLINDNESS))) {
            if (this.ticksLeftToDoubleTapSprint > 0 || this.client.options.keySprint.isPressed()) {
                this.setSprinting(true);
            } else {
                this.ticksLeftToDoubleTapSprint = 7;
            }
        }
        if (!this.isSprinting() && (!this.isTouchingWater() || this.isSubmergedInWater()) && this.isWalking() && bl5 && !this.isUsingItem() && !this.hasStatusEffect(StatusEffects.BLINDNESS) && this.client.options.keySprint.isPressed()) {
            this.setSprinting(true);
        }
        if (this.isSprinting()) {
            bl6 = !this.input.hasForwardMovement() || !bl5;
            int n = bl72 = bl6 || this.horizontalCollision || this.isTouchingWater() && !this.isSubmergedInWater() ? 1 : 0;
            if (this.isSwimming()) {
                if (!this.onGround && !this.input.sneaking && bl6 || !this.isTouchingWater()) {
                    this.setSprinting(false);
                }
            } else if (bl72 != 0) {
                this.setSprinting(false);
            }
        }
        bl6 = false;
        if (this.getAbilities().allowFlying) {
            if (this.client.interactionManager.isFlyingLocked()) {
                if (!this.getAbilities().flying) {
                    this.getAbilities().flying = true;
                    bl6 = true;
                    this.sendAbilitiesUpdate();
                }
            } else if (!bl && this.input.jumping && !bl4) {
                if (this.abilityResyncCountdown == 0) {
                    this.abilityResyncCountdown = 7;
                } else if (!this.isSwimming()) {
                    this.getAbilities().flying = !this.getAbilities().flying;
                    bl6 = true;
                    this.sendAbilitiesUpdate();
                    this.abilityResyncCountdown = 0;
                }
            }
        }
        if (this.input.jumping && !bl6 && !bl && !this.getAbilities().flying && !this.hasVehicle() && !this.isClimbing() && (bl7 = this.getEquippedStack(EquipmentSlot.CHEST)).isOf(Items.ELYTRA) && ElytraItem.isUsable(bl7) && this.checkFallFlying()) {
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
        this.falling = this.isFallFlying();
        if (this.isTouchingWater() && this.input.sneaking && this.shouldSwimInFluids()) {
            this.knockDownwards();
        }
        if (this.isSubmergedIn(FluidTags.WATER)) {
            bl72 = this.isSpectator() ? 10 : 1;
            this.underwaterVisibilityTicks = MathHelper.clamp(this.underwaterVisibilityTicks + bl72, 0, 600);
        } else if (this.underwaterVisibilityTicks > 0) {
            this.isSubmergedIn(FluidTags.WATER);
            this.underwaterVisibilityTicks = MathHelper.clamp(this.underwaterVisibilityTicks - 10, 0, 600);
        }
        if (this.getAbilities().flying && this.isCamera()) {
            bl72 = 0;
            if (this.input.sneaking) {
                --bl72;
            }
            if (this.input.jumping) {
                ++bl72;
            }
            if (bl72 != 0) {
                this.setVelocity(this.getVelocity().add(0.0, (float)bl72 * this.getAbilities().getFlySpeed() * 3.0f, 0.0));
            }
        }
        if (this.hasJumpingMount()) {
            JumpingMount bl73 = (JumpingMount)((Object)this.getVehicle());
            if (this.field_3938 < 0) {
                ++this.field_3938;
                if (this.field_3938 == 0) {
                    this.mountJumpStrength = 0.0f;
                }
            }
            if (bl && !this.input.jumping) {
                this.field_3938 = -10;
                bl73.setJumpStrength(MathHelper.floor(this.getMountJumpStrength() * 100.0f));
                this.startRidingJump();
            } else if (!bl && this.input.jumping) {
                this.field_3938 = 0;
                this.mountJumpStrength = 0.0f;
            } else if (bl) {
                ++this.field_3938;
                this.mountJumpStrength = this.field_3938 < 10 ? (float)this.field_3938 * 0.1f : 0.8f + 2.0f / (float)(this.field_3938 - 9) * 0.1f;
            }
        } else {
            this.mountJumpStrength = 0.0f;
        }
        super.tickMovement();
        if (this.onGround && this.getAbilities().flying && !this.client.interactionManager.isFlyingLocked()) {
            this.getAbilities().flying = false;
            this.sendAbilitiesUpdate();
        }
    }

    @Override
    protected void updatePostDeath() {
        ++this.deathTime;
        if (this.deathTime == 20) {
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    private void updateNausea() {
        this.lastNauseaStrength = this.nextNauseaStrength;
        if (this.inNetherPortal) {
            if (this.client.currentScreen != null && !this.client.currentScreen.isPauseScreen() && !(this.client.currentScreen instanceof DeathScreen)) {
                if (this.client.currentScreen instanceof HandledScreen) {
                    this.closeHandledScreen();
                }
                this.client.setScreen(null);
            }
            if (this.nextNauseaStrength == 0.0f) {
                this.client.getSoundManager().play(PositionedSoundInstance.ambient(SoundEvents.BLOCK_PORTAL_TRIGGER, this.random.nextFloat() * 0.4f + 0.8f, 0.25f));
            }
            this.nextNauseaStrength += 0.0125f;
            if (this.nextNauseaStrength >= 1.0f) {
                this.nextNauseaStrength = 1.0f;
            }
            this.inNetherPortal = false;
        } else if (this.hasStatusEffect(StatusEffects.NAUSEA) && this.getStatusEffect(StatusEffects.NAUSEA).getDuration() > 60) {
            this.nextNauseaStrength += 0.006666667f;
            if (this.nextNauseaStrength > 1.0f) {
                this.nextNauseaStrength = 1.0f;
            }
        } else {
            if (this.nextNauseaStrength > 0.0f) {
                this.nextNauseaStrength -= 0.05f;
            }
            if (this.nextNauseaStrength < 0.0f) {
                this.nextNauseaStrength = 0.0f;
            }
        }
        this.tickNetherPortalCooldown();
    }

    @Override
    public void tickRiding() {
        super.tickRiding();
        this.riding = false;
        if (this.getVehicle() instanceof BoatEntity) {
            BoatEntity boatEntity = (BoatEntity)this.getVehicle();
            boatEntity.setInputs(this.input.pressingLeft, this.input.pressingRight, this.input.pressingForward, this.input.pressingBack);
            this.riding |= this.input.pressingLeft || this.input.pressingRight || this.input.pressingForward || this.input.pressingBack;
        }
    }

    public boolean isRiding() {
        return this.riding;
    }

    @Override
    @Nullable
    public StatusEffectInstance removeStatusEffectInternal(@Nullable StatusEffect type) {
        if (type == StatusEffects.NAUSEA) {
            this.lastNauseaStrength = 0.0f;
            this.nextNauseaStrength = 0.0f;
        }
        return super.removeStatusEffectInternal(type);
    }

    @Override
    public void move(MovementType movementType, Vec3d movement) {
        double d = this.getX();
        double e = this.getZ();
        super.move(movementType, movement);
        this.autoJump((float)(this.getX() - d), (float)(this.getZ() - e));
    }

    public boolean isAutoJumpEnabled() {
        return this.autoJumpEnabled;
    }

    protected void autoJump(float dx, float dz) {
        float j;
        if (!this.shouldAutoJump()) {
            return;
        }
        Vec3d vec3d = this.getPos();
        Vec3d vec3d2 = vec3d.add(dx, 0.0, dz);
        Vec3d vec3d3 = new Vec3d(dx, 0.0, dz);
        float f = this.getMovementSpeed();
        float g = (float)vec3d3.lengthSquared();
        if (g <= 0.001f) {
            Vec2f vec2f = this.input.getMovementInput();
            float h = f * vec2f.x;
            float i = f * vec2f.y;
            j = MathHelper.sin(this.getYaw() * ((float)Math.PI / 180));
            float k = MathHelper.cos(this.getYaw() * ((float)Math.PI / 180));
            vec3d3 = new Vec3d(h * k - i * j, vec3d3.y, i * k + h * j);
            g = (float)vec3d3.lengthSquared();
            if (g <= 0.001f) {
                return;
            }
        }
        float vec2f = MathHelper.fastInverseSqrt(g);
        Vec3d h = vec3d3.multiply(vec2f);
        Vec3d i = this.getRotationVecClient();
        j = (float)(i.x * h.x + i.z * h.z);
        if (j < -0.15f) {
            return;
        }
        ShapeContext k = ShapeContext.of(this);
        BlockPos blockPos = new BlockPos(this.getX(), this.getBoundingBox().maxY, this.getZ());
        BlockState blockState = this.world.getBlockState(blockPos);
        if (!blockState.getCollisionShape(this.world, blockPos, k).isEmpty()) {
            return;
        }
        BlockState blockState2 = this.world.getBlockState(blockPos = blockPos.up());
        if (!blockState2.getCollisionShape(this.world, blockPos, k).isEmpty()) {
            return;
        }
        float l = 7.0f;
        float m = 1.2f;
        if (this.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            m += (float)(this.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1) * 0.75f;
        }
        float n = Math.max(f * 7.0f, 1.0f / vec2f);
        Vec3d vec3d4 = vec3d;
        Vec3d vec3d5 = vec3d2.add(h.multiply(n));
        float o = this.getWidth();
        float p = this.getHeight();
        Box box = new Box(vec3d4, vec3d5.add(0.0, p, 0.0)).expand(o, 0.0, o);
        vec3d4 = vec3d4.add(0.0, 0.51f, 0.0);
        vec3d5 = vec3d5.add(0.0, 0.51f, 0.0);
        Vec3d vec3d6 = h.crossProduct(new Vec3d(0.0, 1.0, 0.0));
        Vec3d vec3d7 = vec3d6.multiply(o * 0.5f);
        Vec3d vec3d8 = vec3d4.subtract(vec3d7);
        Vec3d vec3d9 = vec3d5.subtract(vec3d7);
        Vec3d vec3d10 = vec3d4.add(vec3d7);
        Vec3d vec3d11 = vec3d5.add(vec3d7);
        Iterator iterator = this.world.getCollisions(this, box, entity -> true).flatMap(voxelShape -> voxelShape.getBoundingBoxes().stream()).iterator();
        float q = Float.MIN_VALUE;
        while (iterator.hasNext()) {
            Box box2 = (Box)iterator.next();
            if (!box2.intersects(vec3d8, vec3d9) && !box2.intersects(vec3d10, vec3d11)) continue;
            q = (float)box2.maxY;
            Vec3d vec3d12 = box2.getCenter();
            BlockPos blockPos2 = new BlockPos(vec3d12);
            int r = 1;
            while ((float)r < m) {
                BlockState blockState4;
                BlockPos blockPos3 = blockPos2.up(r);
                BlockState blockState3 = this.world.getBlockState(blockPos3);
                VoxelShape voxelShape2 = blockState3.getCollisionShape(this.world, blockPos3, k);
                if (!voxelShape2.isEmpty() && (double)(q = (float)voxelShape2.getMax(Direction.Axis.Y) + (float)blockPos3.getY()) - this.getY() > (double)m) {
                    return;
                }
                if (r > 1 && !(blockState4 = this.world.getBlockState(blockPos = blockPos.up())).getCollisionShape(this.world, blockPos, k).isEmpty()) {
                    return;
                }
                ++r;
            }
            break block0;
        }
        if (q == Float.MIN_VALUE) {
            return;
        }
        float box2 = (float)((double)q - this.getY());
        if (box2 <= 0.5f || box2 > m) {
            return;
        }
        this.ticksToNextAutojump = 1;
    }

    private boolean shouldAutoJump() {
        return this.isAutoJumpEnabled() && this.ticksToNextAutojump <= 0 && this.onGround && !this.clipAtLedge() && !this.hasVehicle() && this.hasMovementInput() && (double)this.getJumpVelocityMultiplier() >= 1.0;
    }

    /**
     * {@return whether the player has movement input}
     */
    private boolean hasMovementInput() {
        Vec2f vec2f = this.input.getMovementInput();
        return vec2f.x != 0.0f || vec2f.y != 0.0f;
    }

    private boolean isWalking() {
        double d = 0.8;
        return this.isSubmergedInWater() ? this.input.hasForwardMovement() : (double)this.input.movementForward >= 0.8;
    }

    /**
     * {@return the color multiplier of vision in water} Visibility in
     * water is reduced when the player just entered water.
     */
    public float getUnderwaterVisibility() {
        if (!this.isSubmergedIn(FluidTags.WATER)) {
            return 0.0f;
        }
        float f = 600.0f;
        float g = 100.0f;
        if ((float)this.underwaterVisibilityTicks >= 600.0f) {
            return 1.0f;
        }
        float h = MathHelper.clamp((float)this.underwaterVisibilityTicks / 100.0f, 0.0f, 1.0f);
        float i = (float)this.underwaterVisibilityTicks < 100.0f ? 0.0f : MathHelper.clamp(((float)this.underwaterVisibilityTicks - 100.0f) / 500.0f, 0.0f, 1.0f);
        return h * 0.6f + i * 0.39999998f;
    }

    @Override
    public boolean isSubmergedInWater() {
        return this.isSubmergedInWater;
    }

    @Override
    protected boolean updateWaterSubmersionState() {
        boolean bl = this.isSubmergedInWater;
        boolean bl2 = super.updateWaterSubmersionState();
        if (this.isSpectator()) {
            return this.isSubmergedInWater;
        }
        if (!bl && bl2) {
            this.world.playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundCategory.AMBIENT, 1.0f, 1.0f, false);
            this.client.getSoundManager().play(new AmbientSoundLoops.Underwater(this));
        }
        if (bl && !bl2) {
            this.world.playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT, SoundCategory.AMBIENT, 1.0f, 1.0f, false);
        }
        return this.isSubmergedInWater;
    }

    @Override
    public Vec3d method_30951(float f) {
        if (this.client.options.getPerspective().isFirstPerson()) {
            float g = MathHelper.lerp(f * 0.5f, this.getYaw(), this.prevYaw) * ((float)Math.PI / 180);
            float h = MathHelper.lerp(f * 0.5f, this.getPitch(), this.prevPitch) * ((float)Math.PI / 180);
            double d = this.getMainArm() == Arm.RIGHT ? -1.0 : 1.0;
            Vec3d vec3d = new Vec3d(0.39 * d, -0.6, 0.3);
            return vec3d.rotateX(-h).rotateY(-g).add(this.getCameraPosVec(f));
        }
        return super.method_30951(f);
    }

    @Override
    public void onPickupSlotClick(ItemStack cursorStack, ItemStack slotStack, ClickType clickType) {
        this.client.getTutorialManager().onPickupSlotClick(cursorStack, slotStack, clickType);
    }
}

