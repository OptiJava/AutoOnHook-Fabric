/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.debug;

import java.util.Random;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class NameGenerator {
    private static final String[] PREFIX = new String[]{"Slim", "Far", "River", "Silly", "Fat", "Thin", "Fish", "Bat", "Dark", "Oak", "Sly", "Bush", "Zen", "Bark", "Cry", "Slack", "Soup", "Grim", "Hook", "Dirt", "Mud", "Sad", "Hard", "Crook", "Sneak", "Stink", "Weird", "Fire", "Soot", "Soft", "Rough", "Cling", "Scar"};
    private static final String[] SUFFIX = new String[]{"Fox", "Tail", "Jaw", "Whisper", "Twig", "Root", "Finder", "Nose", "Brow", "Blade", "Fry", "Seek", "Wart", "Tooth", "Foot", "Leaf", "Stone", "Fall", "Face", "Tongue", "Voice", "Lip", "Mouth", "Snail", "Toe", "Ear", "Hair", "Beard", "Shirt", "Fist"};

    public static String name(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return entity.getName().getString();
        }
        Text text = entity.getCustomName();
        if (text != null) {
            return text.getString();
        }
        return NameGenerator.name(entity.getUuid());
    }

    public static String name(UUID uuid) {
        Random random = NameGenerator.randomFromUuid(uuid);
        return NameGenerator.getRandom(random, PREFIX) + NameGenerator.getRandom(random, SUFFIX);
    }

    private static String getRandom(Random random, String[] options) {
        return Util.getRandom(options, random);
    }

    private static Random randomFromUuid(UUID uuid) {
        return new Random(uuid.hashCode() >> 2);
    }
}

