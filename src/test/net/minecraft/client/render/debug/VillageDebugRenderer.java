/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.debug;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.debug.NameGenerator;
import net.minecraft.client.render.debug.PathfindingDebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3i;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class VillageDebugRenderer
implements DebugRenderer.Renderer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean field_32874 = true;
    private static final boolean field_32875 = false;
    private static final boolean field_32876 = false;
    private static final boolean field_32877 = false;
    private static final boolean field_32878 = false;
    private static final boolean field_32879 = false;
    private static final boolean field_32880 = false;
    private static final boolean field_32881 = false;
    private static final boolean field_32882 = true;
    private static final boolean field_32883 = true;
    private static final boolean field_32884 = true;
    private static final boolean field_32885 = true;
    private static final boolean field_32886 = true;
    private static final boolean field_32887 = true;
    private static final boolean field_32888 = true;
    private static final boolean field_32889 = true;
    private static final boolean field_32890 = true;
    private static final boolean field_32891 = true;
    private static final boolean field_32892 = true;
    private static final boolean field_32893 = true;
    private static final int POI_RANGE = 30;
    private static final int BRAIN_RANGE = 30;
    private static final int TARGET_ENTITY_RANGE = 8;
    private static final float DEFAULT_DRAWN_STRING_SIZE = 0.02f;
    private static final int WHITE = -1;
    private static final int YELLOW = -256;
    private static final int AQUA = -16711681;
    private static final int GREEN = -16711936;
    private static final int GRAY = -3355444;
    private static final int PINK = -98404;
    private static final int RED = -65536;
    private static final int ORANGE = -23296;
    private final MinecraftClient client;
    private final Map<BlockPos, PointOfInterest> pointsOfInterest = Maps.newHashMap();
    private final Map<UUID, Brain> brains = Maps.newHashMap();
    @Nullable
    private UUID targetedEntity;

    public VillageDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void clear() {
        this.pointsOfInterest.clear();
        this.brains.clear();
        this.targetedEntity = null;
    }

    public void addPointOfInterest(PointOfInterest poi) {
        this.pointsOfInterest.put(poi.pos, poi);
    }

    public void removePointOfInterest(BlockPos pos) {
        this.pointsOfInterest.remove(pos);
    }

    public void setFreeTicketCount(BlockPos pos, int freeTicketCount) {
        PointOfInterest pointOfInterest = this.pointsOfInterest.get(pos);
        if (pointOfInterest == null) {
            LOGGER.warn("Strange, setFreeTicketCount was called for an unknown POI: {}", (Object)pos);
            return;
        }
        pointOfInterest.freeTicketCount = freeTicketCount;
    }

    public void addBrain(Brain brain) {
        this.brains.put(brain.uuid, brain);
    }

    public void removeBrain(int entityId) {
        this.brains.values().removeIf(brain -> brain.entityId == entityId);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        this.removeRemovedBrains();
        this.draw(cameraX, cameraY, cameraZ);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        if (!this.client.player.isSpectator()) {
            this.updateTargetedEntity();
        }
    }

    private void removeRemovedBrains() {
        this.brains.entrySet().removeIf(entry -> {
            Entity entity = this.client.world.getEntityById(((Brain)entry.getValue()).entityId);
            return entity == null || entity.isRemoved();
        });
    }

    private void draw(double x, double y, double z) {
        BlockPos blockPos = new BlockPos(x, y, z);
        this.brains.values().forEach(brain -> {
            if (this.isClose((Brain)brain)) {
                this.drawBrain((Brain)brain, x, y, z);
            }
        });
        for (BlockPos blockPos2 : this.pointsOfInterest.keySet()) {
            if (!blockPos.isWithinDistance(blockPos2, 30.0)) continue;
            VillageDebugRenderer.drawPointOfInterest(blockPos2);
        }
        this.pointsOfInterest.values().forEach(poi -> {
            if (blockPos.isWithinDistance(poi.pos, 30.0)) {
                this.drawPointOfInterestInfo((PointOfInterest)poi);
            }
        });
        this.getGhostPointsOfInterest().forEach((pos, brains) -> {
            if (blockPos.isWithinDistance((Vec3i)pos, 30.0)) {
                this.drawGhostPointOfInterest((BlockPos)pos, (List<String>)brains);
            }
        });
    }

    private static void drawPointOfInterest(BlockPos pos) {
        float f = 0.05f;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        DebugRenderer.drawBox(pos, 0.05f, 0.2f, 0.2f, 1.0f, 0.3f);
    }

    private void drawGhostPointOfInterest(BlockPos pos, List<String> brains) {
        float f = 0.05f;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        DebugRenderer.drawBox(pos, 0.05f, 0.2f, 0.2f, 1.0f, 0.3f);
        VillageDebugRenderer.drawString("" + brains, pos, 0, -256);
        VillageDebugRenderer.drawString("Ghost POI", pos, 1, -65536);
    }

    private void drawPointOfInterestInfo(PointOfInterest pointOfInterest) {
        int i = 0;
        Set<String> set = this.getNamesOfPointOfInterestTicketHolders(pointOfInterest);
        if (set.size() < 4) {
            VillageDebugRenderer.drawString("Owners: " + set, pointOfInterest, i, -256);
        } else {
            VillageDebugRenderer.drawString(set.size() + " ticket holders", pointOfInterest, i, -256);
        }
        ++i;
        Set<String> set2 = this.getNamesOfJobSitePotentialOwners(pointOfInterest);
        if (set2.size() < 4) {
            VillageDebugRenderer.drawString("Candidates: " + set2, pointOfInterest, i, -23296);
        } else {
            VillageDebugRenderer.drawString(set2.size() + " potential owners", pointOfInterest, i, -23296);
        }
        VillageDebugRenderer.drawString("Free tickets: " + pointOfInterest.freeTicketCount, pointOfInterest, ++i, -256);
        VillageDebugRenderer.drawString(pointOfInterest.field_18932, pointOfInterest, ++i, -1);
    }

    private void drawPath(Brain brain, double cameraX, double cameraY, double cameraZ) {
        if (brain.path != null) {
            PathfindingDebugRenderer.drawPath(brain.path, 0.5f, false, false, cameraX, cameraY, cameraZ);
        }
    }

    private void drawBrain(Brain brain, double cameraX, double cameraY, double cameraZ) {
        boolean bl = this.isTargeted(brain);
        int i = 0;
        VillageDebugRenderer.drawString(brain.pos, i, brain.name, -1, 0.03f);
        ++i;
        if (bl) {
            VillageDebugRenderer.drawString(brain.pos, i, brain.profession + " " + brain.xp + " xp", -1, 0.02f);
            ++i;
        }
        if (bl) {
            int j = brain.health < brain.maxHealth ? -23296 : -1;
            VillageDebugRenderer.drawString(brain.pos, i, "health: " + String.format("%.1f", Float.valueOf(brain.health)) + " / " + String.format("%.1f", Float.valueOf(brain.maxHealth)), j, 0.02f);
            ++i;
        }
        if (bl && !brain.inventory.equals("")) {
            VillageDebugRenderer.drawString(brain.pos, i, brain.inventory, -98404, 0.02f);
            ++i;
        }
        if (bl) {
            for (String string : brain.runningTasks) {
                VillageDebugRenderer.drawString(brain.pos, i, string, -16711681, 0.02f);
                ++i;
            }
        }
        if (bl) {
            for (String string : brain.possibleActivities) {
                VillageDebugRenderer.drawString(brain.pos, i, string, -16711936, 0.02f);
                ++i;
            }
        }
        if (brain.wantsGolem) {
            VillageDebugRenderer.drawString(brain.pos, i, "Wants Golem", -23296, 0.02f);
            ++i;
        }
        if (bl) {
            for (String string : brain.gossips) {
                if (string.startsWith(brain.name)) {
                    VillageDebugRenderer.drawString(brain.pos, i, string, -1, 0.02f);
                } else {
                    VillageDebugRenderer.drawString(brain.pos, i, string, -23296, 0.02f);
                }
                ++i;
            }
        }
        if (bl) {
            for (String string : Lists.reverse(brain.memories)) {
                VillageDebugRenderer.drawString(brain.pos, i, string, -3355444, 0.02f);
                ++i;
            }
        }
        if (bl) {
            this.drawPath(brain, cameraX, cameraY, cameraZ);
        }
    }

    private static void drawString(String string, PointOfInterest pointOfInterest, int offsetY, int color) {
        BlockPos blockPos = pointOfInterest.pos;
        VillageDebugRenderer.drawString(string, blockPos, offsetY, color);
    }

    private static void drawString(String string, BlockPos pos, int offsetY, int color) {
        double d = 1.3;
        double e = 0.2;
        double f = (double)pos.getX() + 0.5;
        double g = (double)pos.getY() + 1.3 + (double)offsetY * 0.2;
        double h = (double)pos.getZ() + 0.5;
        DebugRenderer.drawString(string, f, g, h, color, 0.02f, true, 0.0f, true);
    }

    private static void drawString(Position pos, int offsetY, String string, int color, float size) {
        double d = 2.4;
        double e = 0.25;
        BlockPos blockPos = new BlockPos(pos);
        double f = (double)blockPos.getX() + 0.5;
        double g = pos.getY() + 2.4 + (double)offsetY * 0.25;
        double h = (double)blockPos.getZ() + 0.5;
        float i = 0.5f;
        DebugRenderer.drawString(string, f, g, h, color, size, false, 0.5f, true);
    }

    private Set<String> getNamesOfPointOfInterestTicketHolders(PointOfInterest pointOfInterest) {
        return this.getBrainsContainingPointOfInterest(pointOfInterest.pos).stream().map(NameGenerator::name).collect(Collectors.toSet());
    }

    private Set<String> getNamesOfJobSitePotentialOwners(PointOfInterest potentialJobSite) {
        return this.getBrainsContainingPotentialJobSite(potentialJobSite.pos).stream().map(NameGenerator::name).collect(Collectors.toSet());
    }

    private boolean isTargeted(Brain brain) {
        return Objects.equals(this.targetedEntity, brain.uuid);
    }

    private boolean isClose(Brain brain) {
        ClientPlayerEntity playerEntity = this.client.player;
        BlockPos blockPos = new BlockPos(playerEntity.getX(), brain.pos.getY(), playerEntity.getZ());
        BlockPos blockPos2 = new BlockPos(brain.pos);
        return blockPos.isWithinDistance(blockPos2, 30.0);
    }

    private Collection<UUID> getBrainsContainingPointOfInterest(BlockPos pointOfInterest) {
        return this.brains.values().stream().filter(brain -> brain.isPointOfInterest(pointOfInterest)).map(Brain::getUuid).collect(Collectors.toSet());
    }

    private Collection<UUID> getBrainsContainingPotentialJobSite(BlockPos potentialJobSite) {
        return this.brains.values().stream().filter(brain -> brain.isPotentialJobSite(potentialJobSite)).map(Brain::getUuid).collect(Collectors.toSet());
    }

    private Map<BlockPos, List<String>> getGhostPointsOfInterest() {
        HashMap<BlockPos, List<String>> map = Maps.newHashMap();
        for (Brain brain : this.brains.values()) {
            for (BlockPos blockPos2 : Iterables.concat(brain.pointsOfInterest, brain.potentialJobSites)) {
                if (this.pointsOfInterest.containsKey(blockPos2)) continue;
                map.computeIfAbsent(blockPos2, blockPos -> Lists.newArrayList()).add(brain.name);
            }
        }
        return map;
    }

    private void updateTargetedEntity() {
        DebugRenderer.getTargetedEntity(this.client.getCameraEntity(), 8).ifPresent(entity -> {
            this.targetedEntity = entity.getUuid();
        });
    }

    @Environment(value=EnvType.CLIENT)
    public static class PointOfInterest {
        public final BlockPos pos;
        public String field_18932;
        public int freeTicketCount;

        public PointOfInterest(BlockPos pos, String string, int freeTicketCount) {
            this.pos = pos;
            this.field_18932 = string;
            this.freeTicketCount = freeTicketCount;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Brain {
        public final UUID uuid;
        public final int entityId;
        public final String name;
        public final String profession;
        public final int xp;
        public final float health;
        public final float maxHealth;
        public final Position pos;
        public final String inventory;
        public final Path path;
        public final boolean wantsGolem;
        public final List<String> possibleActivities = Lists.newArrayList();
        public final List<String> runningTasks = Lists.newArrayList();
        public final List<String> memories = Lists.newArrayList();
        public final List<String> gossips = Lists.newArrayList();
        public final Set<BlockPos> pointsOfInterest = Sets.newHashSet();
        public final Set<BlockPos> potentialJobSites = Sets.newHashSet();

        public Brain(UUID uuid, int entityId, String name, String profession, int xp, float health, float maxHealth, Position pos, String inventory, @Nullable Path path, boolean wantsGolem) {
            this.uuid = uuid;
            this.entityId = entityId;
            this.name = name;
            this.profession = profession;
            this.xp = xp;
            this.health = health;
            this.maxHealth = maxHealth;
            this.pos = pos;
            this.inventory = inventory;
            this.path = path;
            this.wantsGolem = wantsGolem;
        }

        boolean isPointOfInterest(BlockPos pos) {
            return this.pointsOfInterest.stream().anyMatch(pos::equals);
        }

        boolean isPotentialJobSite(BlockPos pos) {
            return this.potentialJobSites.contains(pos);
        }

        public UUID getUuid() {
            return this.uuid;
        }
    }
}

