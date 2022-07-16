/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.block.dispenser;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.CandleCakeBlock;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.TntBlock;
import net.minecraft.block.WitherSkullBlock;
import net.minecraft.block.dispenser.BlockPlacementDispenserBehavior;
import net.minecraft.block.dispenser.BoatDispenserBehavior;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.block.dispenser.ShearsDispenserBehavior;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface DispenserBehavior {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final DispenserBehavior NOOP = (blockPointer, itemStack) -> itemStack;

    public ItemStack dispense(BlockPointer var1, ItemStack var2);

    public static void registerDefaults() {
        DispenserBlock.registerBehavior(Items.ARROW, new ProjectileDispenserBehavior(){

            @Override
            protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
                ArrowEntity arrowEntity = new ArrowEntity(world, position.getX(), position.getY(), position.getZ());
                arrowEntity.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
                return arrowEntity;
            }
        });
        DispenserBlock.registerBehavior(Items.TIPPED_ARROW, new ProjectileDispenserBehavior(){

            @Override
            protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
                ArrowEntity arrowEntity = new ArrowEntity(world, position.getX(), position.getY(), position.getZ());
                arrowEntity.initFromStack(stack);
                arrowEntity.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
                return arrowEntity;
            }
        });
        DispenserBlock.registerBehavior(Items.SPECTRAL_ARROW, new ProjectileDispenserBehavior(){

            @Override
            protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
                SpectralArrowEntity persistentProjectileEntity = new SpectralArrowEntity(world, position.getX(), position.getY(), position.getZ());
                persistentProjectileEntity.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
                return persistentProjectileEntity;
            }
        });
        DispenserBlock.registerBehavior(Items.EGG, new ProjectileDispenserBehavior(){

            @Override
            protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
                return Util.make(new EggEntity(world, position.getX(), position.getY(), position.getZ()), eggEntity -> eggEntity.setItem(stack));
            }
        });
        DispenserBlock.registerBehavior(Items.SNOWBALL, new ProjectileDispenserBehavior(){

            @Override
            protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
                return Util.make(new SnowballEntity(world, position.getX(), position.getY(), position.getZ()), snowballEntity -> snowballEntity.setItem(stack));
            }
        });
        DispenserBlock.registerBehavior(Items.EXPERIENCE_BOTTLE, new ProjectileDispenserBehavior(){

            @Override
            protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
                return Util.make(new ExperienceBottleEntity(world, position.getX(), position.getY(), position.getZ()), experienceBottleEntity -> experienceBottleEntity.setItem(stack));
            }

            @Override
            protected float getVariation() {
                return super.getVariation() * 0.5f;
            }

            @Override
            protected float getForce() {
                return super.getForce() * 1.25f;
            }
        });
        DispenserBlock.registerBehavior(Items.SPLASH_POTION, new DispenserBehavior(){

            @Override
            public ItemStack dispense(BlockPointer blockPointer, ItemStack itemStack) {
                return new ProjectileDispenserBehavior(){

                    @Override
                    protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
                        return Util.make(new PotionEntity(world, position.getX(), position.getY(), position.getZ()), potionEntity -> potionEntity.setItem(stack));
                    }

                    @Override
                    protected float getVariation() {
                        return super.getVariation() * 0.5f;
                    }

                    @Override
                    protected float getForce() {
                        return super.getForce() * 1.25f;
                    }
                }.dispense(blockPointer, itemStack);
            }
        });
        DispenserBlock.registerBehavior(Items.LINGERING_POTION, new DispenserBehavior(){

            @Override
            public ItemStack dispense(BlockPointer blockPointer, ItemStack itemStack) {
                return new ProjectileDispenserBehavior(){

                    @Override
                    protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
                        return Util.make(new PotionEntity(world, position.getX(), position.getY(), position.getZ()), potionEntity -> potionEntity.setItem(stack));
                    }

                    @Override
                    protected float getVariation() {
                        return super.getVariation() * 0.5f;
                    }

                    @Override
                    protected float getForce() {
                        return super.getForce() * 1.25f;
                    }
                }.dispense(blockPointer, itemStack);
            }
        });
        ItemDispenserBehavior itemDispenserBehavior = new ItemDispenserBehavior(){

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
                EntityType<?> entityType = ((SpawnEggItem)stack.getItem()).getEntityType(stack.getNbt());
                try {
                    entityType.spawnFromItemStack(pointer.getWorld(), stack, null, pointer.getPos().offset(direction), SpawnReason.DISPENSER, direction != Direction.UP, false);
                }
                catch (Exception exception) {
                    LOGGER.error("Error while dispensing spawn egg from dispenser at {}", (Object)pointer.getPos(), (Object)exception);
                    return ItemStack.EMPTY;
                }
                stack.decrement(1);
                pointer.getWorld().emitGameEvent(GameEvent.ENTITY_PLACE, pointer.getPos());
                return stack;
            }
        };
        for (SpawnEggItem spawnEggItem : SpawnEggItem.getAll()) {
            DispenserBlock.registerBehavior(spawnEggItem, itemDispenserBehavior);
        }
        DispenserBlock.registerBehavior(Items.ARMOR_STAND, new ItemDispenserBehavior(){

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
                BlockPos blockPos = pointer.getPos().offset(direction);
                ServerWorld world = pointer.getWorld();
                ArmorStandEntity armorStandEntity = new ArmorStandEntity(world, (double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5);
                EntityType.loadFromEntityNbt(world, null, armorStandEntity, stack.getNbt());
                armorStandEntity.setYaw(direction.asRotation());
                world.spawnEntity(armorStandEntity);
                stack.decrement(1);
                return stack;
            }
        });
        DispenserBlock.registerBehavior(Items.SADDLE, new FallibleItemDispenserBehavior(){

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                BlockPos blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
                List<LivingEntity> list = pointer.getWorld().getEntitiesByClass(LivingEntity.class, new Box(blockPos), entity -> {
                    if (entity instanceof Saddleable) {
                        Saddleable saddleable = (Saddleable)((Object)entity);
                        return !saddleable.isSaddled() && saddleable.canBeSaddled();
                    }
                    return false;
                });
                if (!list.isEmpty()) {
                    ((Saddleable)((Object)list.get(0))).saddle(SoundCategory.BLOCKS);
                    stack.decrement(1);
                    this.setSuccess(true);
                    return stack;
                }
                return super.dispenseSilently(pointer, stack);
            }
        });
        FallibleItemDispenserBehavior itemDispenserBehavior2 = new FallibleItemDispenserBehavior(){

            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                BlockPos blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
                List<HorseBaseEntity> list = pointer.getWorld().getEntitiesByClass(HorseBaseEntity.class, new Box(blockPos), horseBaseEntity -> horseBaseEntity.isAlive() && horseBaseEntity.hasArmorSlot());
                for (HorseBaseEntity horseBaseEntity2 : list) {
                    if (!horseBaseEntity2.isHorseArmor(stack) || horseBaseEntity2.hasArmorInSlot() || !horseBaseEntity2.isTame()) continue;
                    horseBaseEntity2.getStackReference(401).set(stack.split(1));
                    this.setSuccess(true);
                    return stack;
                }
                return super.dispenseSilently(pointer, stack);
            }
        };
        DispenserBlock.registerBehavior(Items.LEATHER_HORSE_ARMOR, itemDispenserBehavior2);
        DispenserBlock.registerBehavior(Items.IRON_HORSE_ARMOR, itemDispenserBehavior2);
        DispenserBlock.registerBehavior(Items.GOLDEN_HORSE_ARMOR, itemDispenserBehavior2);
        DispenserBlock.registerBehavior(Items.DIAMOND_HORSE_ARMOR, itemDispenserBehavior2);
        DispenserBlock.registerBehavior(Items.WHITE_CARPET, itemDispenserBehavior2);
        DispenserBlock.registerBehavior(Items.ORANGE_CARPET, itemDispenserBehavior2);
        DispenserBlock.registerBehavior(Items.CYAN_CARPET, itemDispenserBehavior2);
        DispenserBlock.registerBehavior(Items.BLUE_CARPET, itemDispenserBehavior2);
        DispenserBlock.registerBehavior(Items.BROWN_CARPET, itemDispenserBehavior2);
        DispenserBlock.registerBehavior(Items.BLACK_CARPET, itemDispenserBehavior2);
        DispenserBlock.registerBehavior(Items.GRAY_CARPET, itemDispenserBehavior2);
        DispenserBlock.registerBehavior(Items.GREEN_CARPET, itemDispenserBehavior2);
        DispenserBlock.registerBehavior(Items.LIGHT_BLUE_CARPET, itemDispenserBehavior2);
        DispenserBlock.registerBehavior(Items.LIGHT_GRAY_CARPET, itemDispenserBehavior2);
        DispenserBlock.registerBehavior(Items.LIME_CARPET, itemDispenserBehavior2);
        DispenserBlock.registerBehavior(Items.MAGENTA_CARPET, itemDispenserBehavior2);
        DispenserBlock.registerBehavior(Items.PINK_CARPET, itemDispenserBehavior2);
        DispenserBlock.registerBehavior(Items.PURPLE_CARPET, itemDispenserBehavior2);
        DispenserBlock.registerBehavior(Items.RED_CARPET, itemDispenserBehavior2);
        DispenserBlock.registerBehavior(Items.YELLOW_CARPET, itemDispenserBehavior2);
        DispenserBlock.registerBehavior(Items.CHEST, new FallibleItemDispenserBehavior(){

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                BlockPos blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
                List<AbstractDonkeyEntity> list = pointer.getWorld().getEntitiesByClass(AbstractDonkeyEntity.class, new Box(blockPos), abstractDonkeyEntity -> abstractDonkeyEntity.isAlive() && !abstractDonkeyEntity.hasChest());
                for (AbstractDonkeyEntity abstractDonkeyEntity2 : list) {
                    if (!abstractDonkeyEntity2.isTame() || !abstractDonkeyEntity2.getStackReference(499).set(stack)) continue;
                    stack.decrement(1);
                    this.setSuccess(true);
                    return stack;
                }
                return super.dispenseSilently(pointer, stack);
            }
        });
        DispenserBlock.registerBehavior(Items.FIREWORK_ROCKET, new ItemDispenserBehavior(){

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
                FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity((World)pointer.getWorld(), stack, pointer.getX(), pointer.getY(), pointer.getX(), true);
                DispenserBehavior.setEntityPosition(pointer, fireworkRocketEntity, direction);
                fireworkRocketEntity.setVelocity(direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ(), 0.5f, 1.0f);
                pointer.getWorld().spawnEntity(fireworkRocketEntity);
                stack.decrement(1);
                return stack;
            }

            @Override
            protected void playSound(BlockPointer pointer) {
                pointer.getWorld().syncWorldEvent(WorldEvents.FIREWORK_ROCKET_SHOOTS, pointer.getPos(), 0);
            }
        });
        DispenserBlock.registerBehavior(Items.FIRE_CHARGE, new ItemDispenserBehavior(){

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
                Position position = DispenserBlock.getOutputLocation(pointer);
                double d = position.getX() + (double)((float)direction.getOffsetX() * 0.3f);
                double e = position.getY() + (double)((float)direction.getOffsetY() * 0.3f);
                double f = position.getZ() + (double)((float)direction.getOffsetZ() * 0.3f);
                ServerWorld world = pointer.getWorld();
                Random random = world.random;
                double g = random.nextGaussian() * 0.05 + (double)direction.getOffsetX();
                double h = random.nextGaussian() * 0.05 + (double)direction.getOffsetY();
                double i = random.nextGaussian() * 0.05 + (double)direction.getOffsetZ();
                SmallFireballEntity smallFireballEntity2 = new SmallFireballEntity(world, d, e, f, g, h, i);
                world.spawnEntity(Util.make(smallFireballEntity2, smallFireballEntity -> smallFireballEntity.setItem(stack)));
                stack.decrement(1);
                return stack;
            }

            @Override
            protected void playSound(BlockPointer pointer) {
                pointer.getWorld().syncWorldEvent(WorldEvents.BLAZE_SHOOTS, pointer.getPos(), 0);
            }
        });
        DispenserBlock.registerBehavior(Items.OAK_BOAT, new BoatDispenserBehavior(BoatEntity.Type.OAK));
        DispenserBlock.registerBehavior(Items.SPRUCE_BOAT, new BoatDispenserBehavior(BoatEntity.Type.SPRUCE));
        DispenserBlock.registerBehavior(Items.BIRCH_BOAT, new BoatDispenserBehavior(BoatEntity.Type.BIRCH));
        DispenserBlock.registerBehavior(Items.JUNGLE_BOAT, new BoatDispenserBehavior(BoatEntity.Type.JUNGLE));
        DispenserBlock.registerBehavior(Items.DARK_OAK_BOAT, new BoatDispenserBehavior(BoatEntity.Type.DARK_OAK));
        DispenserBlock.registerBehavior(Items.ACACIA_BOAT, new BoatDispenserBehavior(BoatEntity.Type.ACACIA));
        ItemDispenserBehavior itemDispenserBehavior3 = new ItemDispenserBehavior(){
            private final ItemDispenserBehavior fallbackBehavior = new ItemDispenserBehavior();

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                FluidModificationItem fluidModificationItem = (FluidModificationItem)((Object)stack.getItem());
                BlockPos blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
                ServerWorld world = pointer.getWorld();
                if (fluidModificationItem.placeFluid(null, world, blockPos, null)) {
                    fluidModificationItem.onEmptied(null, world, stack, blockPos);
                    return new ItemStack(Items.BUCKET);
                }
                return this.fallbackBehavior.dispense(pointer, stack);
            }
        };
        DispenserBlock.registerBehavior(Items.LAVA_BUCKET, itemDispenserBehavior3);
        DispenserBlock.registerBehavior(Items.WATER_BUCKET, itemDispenserBehavior3);
        DispenserBlock.registerBehavior(Items.POWDER_SNOW_BUCKET, itemDispenserBehavior3);
        DispenserBlock.registerBehavior(Items.SALMON_BUCKET, itemDispenserBehavior3);
        DispenserBlock.registerBehavior(Items.COD_BUCKET, itemDispenserBehavior3);
        DispenserBlock.registerBehavior(Items.PUFFERFISH_BUCKET, itemDispenserBehavior3);
        DispenserBlock.registerBehavior(Items.TROPICAL_FISH_BUCKET, itemDispenserBehavior3);
        DispenserBlock.registerBehavior(Items.AXOLOTL_BUCKET, itemDispenserBehavior3);
        DispenserBlock.registerBehavior(Items.BUCKET, new ItemDispenserBehavior(){
            private final ItemDispenserBehavior fallbackBehavior = new ItemDispenserBehavior();

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                ItemStack itemStack;
                BlockPos blockPos;
                ServerWorld worldAccess = pointer.getWorld();
                BlockState blockState = worldAccess.getBlockState(blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING)));
                Block block = blockState.getBlock();
                if (block instanceof FluidDrainable) {
                    itemStack = ((FluidDrainable)((Object)block)).tryDrainFluid(worldAccess, blockPos, blockState);
                    if (itemStack.isEmpty()) {
                        return super.dispenseSilently(pointer, stack);
                    }
                } else {
                    return super.dispenseSilently(pointer, stack);
                }
                worldAccess.emitGameEvent(null, GameEvent.FLUID_PICKUP, blockPos);
                Item item = itemStack.getItem();
                stack.decrement(1);
                if (stack.isEmpty()) {
                    return new ItemStack(item);
                }
                if (((DispenserBlockEntity)pointer.getBlockEntity()).addToFirstFreeSlot(new ItemStack(item)) < 0) {
                    this.fallbackBehavior.dispense(pointer, new ItemStack(item));
                }
                return stack;
            }
        });
        DispenserBlock.registerBehavior(Items.FLINT_AND_STEEL, new FallibleItemDispenserBehavior(){

            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                ServerWorld world = pointer.getWorld();
                this.setSuccess(true);
                Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
                BlockPos blockPos = pointer.getPos().offset(direction);
                BlockState blockState = world.getBlockState(blockPos);
                if (AbstractFireBlock.canPlaceAt(world, blockPos, direction)) {
                    world.setBlockState(blockPos, AbstractFireBlock.getState(world, blockPos));
                    world.emitGameEvent(null, GameEvent.BLOCK_PLACE, blockPos);
                } else if (CampfireBlock.canBeLit(blockState) || CandleBlock.canBeLit(blockState) || CandleCakeBlock.canBeLit(blockState)) {
                    world.setBlockState(blockPos, (BlockState)blockState.with(Properties.LIT, true));
                    world.emitGameEvent(null, GameEvent.BLOCK_CHANGE, blockPos);
                } else if (blockState.getBlock() instanceof TntBlock) {
                    TntBlock.primeTnt(world, blockPos);
                    world.removeBlock(blockPos, false);
                } else {
                    this.setSuccess(false);
                }
                if (this.isSuccess() && stack.damage(1, world.random, null)) {
                    stack.setCount(0);
                }
                return stack;
            }
        });
        DispenserBlock.registerBehavior(Items.BONE_MEAL, new FallibleItemDispenserBehavior(){

            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                this.setSuccess(true);
                ServerWorld world = pointer.getWorld();
                BlockPos blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
                if (BoneMealItem.useOnFertilizable(stack, world, blockPos) || BoneMealItem.useOnGround(stack, world, blockPos, null)) {
                    if (!world.isClient) {
                        world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos, 0);
                    }
                } else {
                    this.setSuccess(false);
                }
                return stack;
            }
        });
        DispenserBlock.registerBehavior(Blocks.TNT, new ItemDispenserBehavior(){

            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                ServerWorld world = pointer.getWorld();
                BlockPos blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
                TntEntity tntEntity = new TntEntity(world, (double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5, null);
                world.spawnEntity(tntEntity);
                ((World)world).playSound(null, tntEntity.getX(), tntEntity.getY(), tntEntity.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0f, 1.0f);
                world.emitGameEvent(null, GameEvent.ENTITY_PLACE, blockPos);
                stack.decrement(1);
                return stack;
            }
        });
        FallibleItemDispenserBehavior dispenserBehavior = new FallibleItemDispenserBehavior(){

            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                this.setSuccess(ArmorItem.dispenseArmor(pointer, stack));
                return stack;
            }
        };
        DispenserBlock.registerBehavior(Items.CREEPER_HEAD, dispenserBehavior);
        DispenserBlock.registerBehavior(Items.ZOMBIE_HEAD, dispenserBehavior);
        DispenserBlock.registerBehavior(Items.DRAGON_HEAD, dispenserBehavior);
        DispenserBlock.registerBehavior(Items.SKELETON_SKULL, dispenserBehavior);
        DispenserBlock.registerBehavior(Items.PLAYER_HEAD, dispenserBehavior);
        DispenserBlock.registerBehavior(Items.WITHER_SKELETON_SKULL, new FallibleItemDispenserBehavior(){

            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                ServerWorld world = pointer.getWorld();
                Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
                BlockPos blockPos = pointer.getPos().offset(direction);
                if (world.isAir(blockPos) && WitherSkullBlock.canDispense(world, blockPos, stack)) {
                    world.setBlockState(blockPos, (BlockState)Blocks.WITHER_SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, direction.getAxis() == Direction.Axis.Y ? 0 : direction.getOpposite().getHorizontal() * 4), Block.NOTIFY_ALL);
                    world.emitGameEvent(null, GameEvent.BLOCK_PLACE, blockPos);
                    BlockEntity blockEntity = world.getBlockEntity(blockPos);
                    if (blockEntity instanceof SkullBlockEntity) {
                        WitherSkullBlock.onPlaced(world, blockPos, (SkullBlockEntity)blockEntity);
                    }
                    stack.decrement(1);
                    this.setSuccess(true);
                } else {
                    this.setSuccess(ArmorItem.dispenseArmor(pointer, stack));
                }
                return stack;
            }
        });
        DispenserBlock.registerBehavior(Blocks.CARVED_PUMPKIN, new FallibleItemDispenserBehavior(){

            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                ServerWorld world = pointer.getWorld();
                BlockPos blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
                CarvedPumpkinBlock carvedPumpkinBlock = (CarvedPumpkinBlock)Blocks.CARVED_PUMPKIN;
                if (world.isAir(blockPos) && carvedPumpkinBlock.canDispense(world, blockPos)) {
                    if (!world.isClient) {
                        world.setBlockState(blockPos, carvedPumpkinBlock.getDefaultState(), Block.NOTIFY_ALL);
                        world.emitGameEvent(null, GameEvent.BLOCK_PLACE, blockPos);
                    }
                    stack.decrement(1);
                    this.setSuccess(true);
                } else {
                    this.setSuccess(ArmorItem.dispenseArmor(pointer, stack));
                }
                return stack;
            }
        });
        DispenserBlock.registerBehavior(Blocks.SHULKER_BOX.asItem(), new BlockPlacementDispenserBehavior());
        for (DyeColor dyeColor : DyeColor.values()) {
            DispenserBlock.registerBehavior(ShulkerBoxBlock.get(dyeColor).asItem(), new BlockPlacementDispenserBehavior());
        }
        DispenserBlock.registerBehavior(Items.GLASS_BOTTLE.asItem(), new FallibleItemDispenserBehavior(){
            private final ItemDispenserBehavior fallbackBehavior = new ItemDispenserBehavior();

            private ItemStack tryPutFilledBottle(BlockPointer pointer, ItemStack emptyBottleStack, ItemStack filledBottleStack) {
                emptyBottleStack.decrement(1);
                if (emptyBottleStack.isEmpty()) {
                    pointer.getWorld().emitGameEvent(null, GameEvent.FLUID_PICKUP, pointer.getPos());
                    return filledBottleStack.copy();
                }
                if (((DispenserBlockEntity)pointer.getBlockEntity()).addToFirstFreeSlot(filledBottleStack.copy()) < 0) {
                    this.fallbackBehavior.dispense(pointer, filledBottleStack.copy());
                }
                return emptyBottleStack;
            }

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                this.setSuccess(false);
                ServerWorld serverWorld = pointer.getWorld();
                BlockPos blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
                BlockState blockState = serverWorld.getBlockState(blockPos);
                if (blockState.isIn(BlockTags.BEEHIVES, state -> state.contains(BeehiveBlock.HONEY_LEVEL)) && blockState.get(BeehiveBlock.HONEY_LEVEL) >= 5) {
                    ((BeehiveBlock)blockState.getBlock()).takeHoney(serverWorld, blockState, blockPos, null, BeehiveBlockEntity.BeeState.BEE_RELEASED);
                    this.setSuccess(true);
                    return this.tryPutFilledBottle(pointer, stack, new ItemStack(Items.HONEY_BOTTLE));
                }
                if (serverWorld.getFluidState(blockPos).isIn(FluidTags.WATER)) {
                    this.setSuccess(true);
                    return this.tryPutFilledBottle(pointer, stack, PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER));
                }
                return super.dispenseSilently(pointer, stack);
            }
        });
        DispenserBlock.registerBehavior(Items.GLOWSTONE, new FallibleItemDispenserBehavior(){

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
                BlockPos blockPos = pointer.getPos().offset(direction);
                ServerWorld world = pointer.getWorld();
                BlockState blockState = world.getBlockState(blockPos);
                this.setSuccess(true);
                if (blockState.isOf(Blocks.RESPAWN_ANCHOR)) {
                    if (blockState.get(RespawnAnchorBlock.CHARGES) != 4) {
                        RespawnAnchorBlock.charge(world, blockPos, blockState);
                        stack.decrement(1);
                    } else {
                        this.setSuccess(false);
                    }
                    return stack;
                }
                return super.dispenseSilently(pointer, stack);
            }
        });
        DispenserBlock.registerBehavior(Items.SHEARS.asItem(), new ShearsDispenserBehavior());
        DispenserBlock.registerBehavior(Items.HONEYCOMB, new FallibleItemDispenserBehavior(){

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                BlockPos blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
                ServerWorld world = pointer.getWorld();
                BlockState blockState = world.getBlockState(blockPos);
                Optional<BlockState> optional = HoneycombItem.getWaxedState(blockState);
                if (optional.isPresent()) {
                    world.setBlockState(blockPos, optional.get());
                    world.syncWorldEvent(WorldEvents.BLOCK_WAXED, blockPos, 0);
                    stack.decrement(1);
                    this.setSuccess(true);
                    return stack;
                }
                return super.dispenseSilently(pointer, stack);
            }
        });
    }

    public static void setEntityPosition(BlockPointer pointer, Entity entity, Direction direction) {
        entity.setPosition(pointer.getX() + (double)direction.getOffsetX() * (0.5000099999997474 - (double)entity.getWidth() / 2.0), pointer.getY() + (double)direction.getOffsetY() * (0.5000099999997474 - (double)entity.getHeight() / 2.0) - (double)entity.getHeight() / 2.0, pointer.getZ() + (double)direction.getOffsetZ() * (0.5000099999997474 - (double)entity.getWidth() / 2.0));
    }
}

