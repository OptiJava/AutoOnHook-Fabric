/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.potion;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class PotionUtil {
    public static final String CUSTOM_POTION_EFFECTS_KEY = "CustomPotionEffects";
    public static final String CUSTOM_POTION_COLOR_KEY = "CustomPotionColor";
    public static final String POTION_KEY = "Potion";
    private static final int DEFAULT_COLOR = 0xF800F8;
    private static final Text NONE_TEXT = new TranslatableText("effect.none").formatted(Formatting.GRAY);

    public static List<StatusEffectInstance> getPotionEffects(ItemStack stack) {
        return PotionUtil.getPotionEffects(stack.getNbt());
    }

    public static List<StatusEffectInstance> getPotionEffects(Potion potion, Collection<StatusEffectInstance> custom) {
        ArrayList<StatusEffectInstance> list = Lists.newArrayList();
        list.addAll(potion.getEffects());
        list.addAll(custom);
        return list;
    }

    public static List<StatusEffectInstance> getPotionEffects(@Nullable NbtCompound nbt) {
        ArrayList<StatusEffectInstance> list = Lists.newArrayList();
        list.addAll(PotionUtil.getPotion(nbt).getEffects());
        PotionUtil.getCustomPotionEffects(nbt, list);
        return list;
    }

    public static List<StatusEffectInstance> getCustomPotionEffects(ItemStack stack) {
        return PotionUtil.getCustomPotionEffects(stack.getNbt());
    }

    public static List<StatusEffectInstance> getCustomPotionEffects(@Nullable NbtCompound nbt) {
        ArrayList<StatusEffectInstance> list = Lists.newArrayList();
        PotionUtil.getCustomPotionEffects(nbt, list);
        return list;
    }

    public static void getCustomPotionEffects(@Nullable NbtCompound nbt, List<StatusEffectInstance> list) {
        if (nbt != null && nbt.contains(CUSTOM_POTION_EFFECTS_KEY, 9)) {
            NbtList nbtList = nbt.getList(CUSTOM_POTION_EFFECTS_KEY, 10);
            for (int i = 0; i < nbtList.size(); ++i) {
                NbtCompound nbtCompound = nbtList.getCompound(i);
                StatusEffectInstance statusEffectInstance = StatusEffectInstance.fromNbt(nbtCompound);
                if (statusEffectInstance == null) continue;
                list.add(statusEffectInstance);
            }
        }
    }

    public static int getColor(ItemStack stack) {
        NbtCompound nbtCompound = stack.getNbt();
        if (nbtCompound != null && nbtCompound.contains(CUSTOM_POTION_COLOR_KEY, 99)) {
            return nbtCompound.getInt(CUSTOM_POTION_COLOR_KEY);
        }
        return PotionUtil.getPotion(stack) == Potions.EMPTY ? 0xF800F8 : PotionUtil.getColor(PotionUtil.getPotionEffects(stack));
    }

    public static int getColor(Potion potion) {
        return potion == Potions.EMPTY ? 0xF800F8 : PotionUtil.getColor(potion.getEffects());
    }

    public static int getColor(Collection<StatusEffectInstance> effects) {
        int i = 3694022;
        if (effects.isEmpty()) {
            return 3694022;
        }
        float f = 0.0f;
        float g = 0.0f;
        float h = 0.0f;
        int j = 0;
        for (StatusEffectInstance statusEffectInstance : effects) {
            if (!statusEffectInstance.shouldShowParticles()) continue;
            int k = statusEffectInstance.getEffectType().getColor();
            int l = statusEffectInstance.getAmplifier() + 1;
            f += (float)(l * (k >> 16 & 0xFF)) / 255.0f;
            g += (float)(l * (k >> 8 & 0xFF)) / 255.0f;
            h += (float)(l * (k >> 0 & 0xFF)) / 255.0f;
            j += l;
        }
        if (j == 0) {
            return 0;
        }
        f = f / (float)j * 255.0f;
        g = g / (float)j * 255.0f;
        h = h / (float)j * 255.0f;
        return (int)f << 16 | (int)g << 8 | (int)h;
    }

    public static Potion getPotion(ItemStack stack) {
        return PotionUtil.getPotion(stack.getNbt());
    }

    public static Potion getPotion(@Nullable NbtCompound compound) {
        if (compound == null) {
            return Potions.EMPTY;
        }
        return Potion.byId(compound.getString(POTION_KEY));
    }

    public static ItemStack setPotion(ItemStack stack, Potion potion) {
        Identifier identifier = Registry.POTION.getId(potion);
        if (potion == Potions.EMPTY) {
            stack.removeSubNbt(POTION_KEY);
        } else {
            stack.getOrCreateNbt().putString(POTION_KEY, identifier.toString());
        }
        return stack;
    }

    public static ItemStack setCustomPotionEffects(ItemStack stack, Collection<StatusEffectInstance> effects) {
        if (effects.isEmpty()) {
            return stack;
        }
        NbtCompound nbtCompound = stack.getOrCreateNbt();
        NbtList nbtList = nbtCompound.getList(CUSTOM_POTION_EFFECTS_KEY, 9);
        for (StatusEffectInstance statusEffectInstance : effects) {
            nbtList.add(statusEffectInstance.writeNbt(new NbtCompound()));
        }
        nbtCompound.put(CUSTOM_POTION_EFFECTS_KEY, nbtList);
        return stack;
    }

    public static void buildTooltip(ItemStack stack, List<Text> list, float f) {
        Object mutableText;
        List<StatusEffectInstance> list2 = PotionUtil.getPotionEffects(stack);
        ArrayList<Pair<EntityAttribute, EntityAttributeModifier>> list3 = Lists.newArrayList();
        if (list2.isEmpty()) {
            list.add(NONE_TEXT);
        } else {
            for (StatusEffectInstance statusEffectInstance : list2) {
                mutableText = new TranslatableText(statusEffectInstance.getTranslationKey());
                StatusEffect statusEffect = statusEffectInstance.getEffectType();
                Map<EntityAttribute, EntityAttributeModifier> map = statusEffect.getAttributeModifiers();
                if (!map.isEmpty()) {
                    for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : map.entrySet()) {
                        EntityAttributeModifier entityAttributeModifier = entry.getValue();
                        EntityAttributeModifier entityAttributeModifier2 = new EntityAttributeModifier(entityAttributeModifier.getName(), statusEffect.adjustModifierAmount(statusEffectInstance.getAmplifier(), entityAttributeModifier), entityAttributeModifier.getOperation());
                        list3.add(new Pair<EntityAttribute, EntityAttributeModifier>(entry.getKey(), entityAttributeModifier2));
                    }
                }
                if (statusEffectInstance.getAmplifier() > 0) {
                    mutableText = new TranslatableText("potion.withAmplifier", mutableText, new TranslatableText("potion.potency." + statusEffectInstance.getAmplifier()));
                }
                if (statusEffectInstance.getDuration() > 20) {
                    mutableText = new TranslatableText("potion.withDuration", mutableText, StatusEffectUtil.durationToString(statusEffectInstance, f));
                }
                list.add(mutableText.formatted(statusEffect.getCategory().getFormatting()));
            }
        }
        if (!list3.isEmpty()) {
            list.add(LiteralText.EMPTY);
            list.add(new TranslatableText("potion.whenDrank").formatted(Formatting.DARK_PURPLE));
            for (Pair pair : list3) {
                mutableText = (EntityAttributeModifier)pair.getSecond();
                double statusEffect = ((EntityAttributeModifier)mutableText).getValue();
                double d = ((EntityAttributeModifier)mutableText).getOperation() == EntityAttributeModifier.Operation.MULTIPLY_BASE || ((EntityAttributeModifier)mutableText).getOperation() == EntityAttributeModifier.Operation.MULTIPLY_TOTAL ? ((EntityAttributeModifier)mutableText).getValue() * 100.0 : ((EntityAttributeModifier)mutableText).getValue();
                if (statusEffect > 0.0) {
                    list.add(new TranslatableText("attribute.modifier.plus." + ((EntityAttributeModifier)mutableText).getOperation().getId(), ItemStack.MODIFIER_FORMAT.format(d), new TranslatableText(((EntityAttribute)pair.getFirst()).getTranslationKey())).formatted(Formatting.BLUE));
                    continue;
                }
                if (!(statusEffect < 0.0)) continue;
                list.add(new TranslatableText("attribute.modifier.take." + ((EntityAttributeModifier)mutableText).getOperation().getId(), ItemStack.MODIFIER_FORMAT.format(d *= -1.0), new TranslatableText(((EntityAttribute)pair.getFirst()).getTranslationKey())).formatted(Formatting.RED));
            }
        }
    }
}

