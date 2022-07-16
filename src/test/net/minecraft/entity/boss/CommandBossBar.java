/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.boss;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class CommandBossBar
extends ServerBossBar {
    private final Identifier id;
    private final Set<UUID> playerUuids = Sets.newHashSet();
    private int value;
    private int maxValue = 100;

    public CommandBossBar(Identifier id, Text displayName) {
        super(displayName, BossBar.Color.WHITE, BossBar.Style.PROGRESS);
        this.id = id;
        this.setPercent(0.0f);
    }

    public Identifier getId() {
        return this.id;
    }

    @Override
    public void addPlayer(ServerPlayerEntity player) {
        super.addPlayer(player);
        this.playerUuids.add(player.getUuid());
    }

    public void addPlayer(UUID uuid) {
        this.playerUuids.add(uuid);
    }

    @Override
    public void removePlayer(ServerPlayerEntity player) {
        super.removePlayer(player);
        this.playerUuids.remove(player.getUuid());
    }

    @Override
    public void clearPlayers() {
        super.clearPlayers();
        this.playerUuids.clear();
    }

    public int getValue() {
        return this.value;
    }

    public int getMaxValue() {
        return this.maxValue;
    }

    public void setValue(int value) {
        this.value = value;
        this.setPercent(MathHelper.clamp((float)value / (float)this.maxValue, 0.0f, 1.0f));
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        this.setPercent(MathHelper.clamp((float)this.value / (float)maxValue, 0.0f, 1.0f));
    }

    public final Text toHoverableText() {
        return Texts.bracketed(this.getName()).styled(style -> style.withColor(this.getColor().getTextFormat()).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(this.getId().toString()))).withInsertion(this.getId().toString()));
    }

    public boolean addPlayers(Collection<ServerPlayerEntity> players) {
        boolean bl;
        HashSet<UUID> set = Sets.newHashSet();
        HashSet<ServerPlayerEntity> set2 = Sets.newHashSet();
        for (UUID uUID : this.playerUuids) {
            bl = false;
            for (ServerPlayerEntity serverPlayerEntity : players) {
                if (!serverPlayerEntity.getUuid().equals(uUID)) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            set.add(uUID);
        }
        for (ServerPlayerEntity serverPlayerEntity : players) {
            bl = false;
            for (UUID uUID : this.playerUuids) {
                if (!serverPlayerEntity.getUuid().equals(uUID)) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            set2.add(serverPlayerEntity);
        }
        for (UUID uUID : set) {
            for (ServerPlayerEntity serverPlayerEntity2 : this.getPlayers()) {
                if (!serverPlayerEntity2.getUuid().equals(uUID)) continue;
                this.removePlayer(serverPlayerEntity2);
                break;
            }
            this.playerUuids.remove(uUID);
        }
        for (ServerPlayerEntity serverPlayerEntity : set2) {
            this.addPlayer(serverPlayerEntity);
        }
        return !set.isEmpty() || !set2.isEmpty();
    }

    public NbtCompound toNbt() {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putString("Name", Text.Serializer.toJson(this.name));
        nbtCompound.putBoolean("Visible", this.isVisible());
        nbtCompound.putInt("Value", this.value);
        nbtCompound.putInt("Max", this.maxValue);
        nbtCompound.putString("Color", this.getColor().getName());
        nbtCompound.putString("Overlay", this.getStyle().getName());
        nbtCompound.putBoolean("DarkenScreen", this.shouldDarkenSky());
        nbtCompound.putBoolean("PlayBossMusic", this.hasDragonMusic());
        nbtCompound.putBoolean("CreateWorldFog", this.shouldThickenFog());
        NbtList nbtList = new NbtList();
        for (UUID uUID : this.playerUuids) {
            nbtList.add(NbtHelper.fromUuid(uUID));
        }
        nbtCompound.put("Players", nbtList);
        return nbtCompound;
    }

    public static CommandBossBar fromNbt(NbtCompound nbt, Identifier id) {
        CommandBossBar commandBossBar = new CommandBossBar(id, Text.Serializer.fromJson(nbt.getString("Name")));
        commandBossBar.setVisible(nbt.getBoolean("Visible"));
        commandBossBar.setValue(nbt.getInt("Value"));
        commandBossBar.setMaxValue(nbt.getInt("Max"));
        commandBossBar.setColor(BossBar.Color.byName(nbt.getString("Color")));
        commandBossBar.setStyle(BossBar.Style.byName(nbt.getString("Overlay")));
        commandBossBar.setDarkenSky(nbt.getBoolean("DarkenScreen"));
        commandBossBar.setDragonMusic(nbt.getBoolean("PlayBossMusic"));
        commandBossBar.setThickenFog(nbt.getBoolean("CreateWorldFog"));
        NbtList nbtList = nbt.getList("Players", 11);
        for (int i = 0; i < nbtList.size(); ++i) {
            commandBossBar.addPlayer(NbtHelper.toUuid(nbtList.get(i)));
        }
        return commandBossBar;
    }

    public void onPlayerConnect(ServerPlayerEntity player) {
        if (this.playerUuids.contains(player.getUuid())) {
            this.addPlayer(player);
        }
    }

    public void onPlayerDisconnect(ServerPlayerEntity player) {
        super.removePlayer(player);
    }
}

