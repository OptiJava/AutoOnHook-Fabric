/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.scoreboard;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractTeam {
    public boolean isEqual(@Nullable AbstractTeam team) {
        if (team == null) {
            return false;
        }
        return this == team;
    }

    public abstract String getName();

    /**
     * Decorates the name of an entity with the prefix, suffix and color of this team.
     * 
     * @param name the name to be decorated
     */
    public abstract MutableText decorateName(Text var1);

    public abstract boolean shouldShowFriendlyInvisibles();

    public abstract boolean isFriendlyFireAllowed();

    public abstract VisibilityRule getNameTagVisibilityRule();

    public abstract Formatting getColor();

    public abstract Collection<String> getPlayerList();

    public abstract VisibilityRule getDeathMessageVisibilityRule();

    public abstract CollisionRule getCollisionRule();

    public static enum CollisionRule {
        ALWAYS("always", 0),
        NEVER("never", 1),
        PUSH_OTHER_TEAMS("pushOtherTeams", 2),
        PUSH_OWN_TEAM("pushOwnTeam", 3);

        private static final Map<String, CollisionRule> COLLISION_RULES;
        public final String name;
        public final int value;

        @Nullable
        public static CollisionRule getRule(String name) {
            return COLLISION_RULES.get(name);
        }

        private CollisionRule(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public Text getTranslationKey() {
            return new TranslatableText("team.collision." + this.name);
        }

        static {
            COLLISION_RULES = Arrays.stream(CollisionRule.values()).collect(Collectors.toMap(collisionRule -> collisionRule.name, collisionRule -> collisionRule));
        }
    }

    public static enum VisibilityRule {
        ALWAYS("always", 0),
        NEVER("never", 1),
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams", 2),
        HIDE_FOR_OWN_TEAM("hideForOwnTeam", 3);

        private static final Map<String, VisibilityRule> VISIBILITY_RULES;
        public final String name;
        public final int value;

        public static String[] method_35595() {
            return VISIBILITY_RULES.keySet().toArray(new String[VISIBILITY_RULES.size()]);
        }

        @Nullable
        public static VisibilityRule getRule(String name) {
            return VISIBILITY_RULES.get(name);
        }

        private VisibilityRule(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public Text getTranslationKey() {
            return new TranslatableText("team.visibility." + this.name);
        }

        static {
            VISIBILITY_RULES = Arrays.stream(VisibilityRule.values()).collect(Collectors.toMap(visibilityRule -> visibilityRule.name, visibilityRule -> visibilityRule));
        }
    }
}

