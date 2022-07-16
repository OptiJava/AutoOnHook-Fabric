/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.util;

import com.google.common.annotations.VisibleForTesting;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class ModelIdentifier
extends Identifier {
    @VisibleForTesting
    static final char SEPARATOR = '#';
    private final String variant;

    protected ModelIdentifier(String[] strings) {
        super(strings);
        this.variant = strings[2].toLowerCase(Locale.ROOT);
    }

    public ModelIdentifier(String string, String string2, String string3) {
        this(new String[]{string, string2, string3});
    }

    public ModelIdentifier(String string) {
        this(ModelIdentifier.split(string));
    }

    public ModelIdentifier(Identifier id, String variant) {
        this(id.toString(), variant);
    }

    public ModelIdentifier(String string, String string2) {
        this(ModelIdentifier.split(string + "#" + string2));
    }

    protected static String[] split(String id) {
        String[] strings = new String[]{null, id, ""};
        int i = id.indexOf(35);
        String string = id;
        if (i >= 0) {
            strings[2] = id.substring(i + 1, id.length());
            if (i > 1) {
                string = id.substring(0, i);
            }
        }
        System.arraycopy(Identifier.split(string, ':'), 0, strings, 0, 2);
        return strings;
    }

    public String getVariant() {
        return this.variant;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof ModelIdentifier && super.equals(object)) {
            ModelIdentifier modelIdentifier = (ModelIdentifier)object;
            return this.variant.equals(modelIdentifier.variant);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + this.variant.hashCode();
    }

    @Override
    public String toString() {
        return super.toString() + "#" + this.variant;
    }
}

