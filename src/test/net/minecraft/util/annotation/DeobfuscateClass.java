/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.meta.TypeQualifierDefault;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * An annotation on classes. When a class is annotated, the class itself and its
 * fields and methods are not obfuscated. Mainly used by blaze3d.
 * 
 * @see net.minecraft.obfuscate.DontObfuscate
 */
@TypeQualifierDefault(value={ElementType.TYPE, ElementType.METHOD})
@Retention(value=RetentionPolicy.CLASS)
@Environment(value=EnvType.CLIENT)
public @interface DeobfuscateClass {
}

