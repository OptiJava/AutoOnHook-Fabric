/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.MathHelper;

public class ProtectionEnchantment
extends Enchantment {
    public final Type protectionType;

    public ProtectionEnchantment(Enchantment.Rarity weight, Type protectionType, EquipmentSlot ... slotTypes) {
        super(weight, protectionType == Type.FALL ? EnchantmentTarget.ARMOR_FEET : EnchantmentTarget.ARMOR, slotTypes);
        this.protectionType = protectionType;
    }

    @Override
    public int getMinPower(int level) {
        return this.protectionType.getBasePower() + (level - 1) * this.protectionType.getPowerPerLevel();
    }

    @Override
    public int getMaxPower(int level) {
        return this.getMinPower(level) + this.protectionType.getPowerPerLevel();
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public int getProtectionAmount(int level, DamageSource source) {
        if (source.isOutOfWorld()) {
            return 0;
        }
        if (this.protectionType == Type.ALL) {
            return level;
        }
        if (this.protectionType == Type.FIRE && source.isFire()) {
            return level * 2;
        }
        if (this.protectionType == Type.FALL && source.isFromFalling()) {
            return level * 3;
        }
        if (this.protectionType == Type.EXPLOSION && source.isExplosive()) {
            return level * 2;
        }
        if (this.protectionType == Type.PROJECTILE && source.isProjectile()) {
            return level * 2;
        }
        return 0;
    }

    @Override
    public boolean canAccept(Enchantment other) {
        if (other instanceof ProtectionEnchantment) {
            ProtectionEnchantment protectionEnchantment = (ProtectionEnchantment)other;
            if (this.protectionType == protectionEnchantment.protectionType) {
                return false;
            }
            return this.protectionType == Type.FALL || protectionEnchantment.protectionType == Type.FALL;
        }
        return super.canAccept(other);
    }

    public static int transformFireDuration(LivingEntity entity, int duration) {
        int i = EnchantmentHelper.getEquipmentLevel(Enchantments.FIRE_PROTECTION, entity);
        if (i > 0) {
            duration -= MathHelper.floor((float)duration * ((float)i * 0.15f));
        }
        return duration;
    }

    public static double transformExplosionKnockback(LivingEntity entity, double velocity) {
        int i = EnchantmentHelper.getEquipmentLevel(Enchantments.BLAST_PROTECTION, entity);
        if (i > 0) {
            velocity -= (double)MathHelper.floor(velocity * (double)((float)i * 0.15f));
        }
        return velocity;
    }

    public static enum Type {
        ALL(1, 11),
        FIRE(10, 8),
        FALL(5, 6),
        EXPLOSION(5, 8),
        PROJECTILE(3, 6);

        private final int basePower;
        private final int powerPerLevel;

        private Type(int basePower, int powerPerLevel) {
            this.basePower = basePower;
            this.powerPerLevel = powerPerLevel;
        }

        public int getBasePower() {
            return this.basePower;
        }

        public int getPowerPerLevel() {
            return this.powerPerLevel;
        }
    }
}
