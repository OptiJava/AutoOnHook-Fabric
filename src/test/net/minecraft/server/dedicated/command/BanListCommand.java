/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.server.dedicated.command;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Collection;
import net.minecraft.server.BanEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

public class BanListCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("banlist").requires(source -> source.hasPermissionLevel(3))).executes(context -> {
            PlayerManager playerManager = ((ServerCommandSource)context.getSource()).getServer().getPlayerManager();
            return BanListCommand.execute((ServerCommandSource)context.getSource(), Lists.newArrayList(Iterables.concat(playerManager.getUserBanList().values(), playerManager.getIpBanList().values())));
        })).then(CommandManager.literal("ips").executes(context -> BanListCommand.execute((ServerCommandSource)context.getSource(), ((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getIpBanList().values())))).then(CommandManager.literal("players").executes(context -> BanListCommand.execute((ServerCommandSource)context.getSource(), ((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getUserBanList().values()))));
    }

    private static int execute(ServerCommandSource source, Collection<? extends BanEntry<?>> targets) {
        if (targets.isEmpty()) {
            source.sendFeedback(new TranslatableText("commands.banlist.none"), false);
        } else {
            source.sendFeedback(new TranslatableText("commands.banlist.list", targets.size()), false);
            for (BanEntry<?> banEntry : targets) {
                source.sendFeedback(new TranslatableText("commands.banlist.entry", banEntry.toText(), banEntry.getSource(), banEntry.getReason()), false);
            }
        }
        return targets.size();
    }
}

