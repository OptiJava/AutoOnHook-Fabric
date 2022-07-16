/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui.screen.narration;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.narration.Narration;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public interface NarrationMessageBuilder {
    default public void put(NarrationPart part, Text text) {
        this.put(part, Narration.string(text.getString()));
    }

    default public void put(NarrationPart part, String string) {
        this.put(part, Narration.string(string));
    }

    default public void put(NarrationPart part, Text ... texts) {
        this.put(part, Narration.texts(ImmutableList.copyOf(texts)));
    }

    public void put(NarrationPart var1, Narration<?> var2);

    public NarrationMessageBuilder nextMessage();
}
