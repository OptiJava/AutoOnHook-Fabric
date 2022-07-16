/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

public class RecipeCommand {
    private static final SimpleCommandExceptionType GIVE_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.recipe.give.failed"));
    private static final SimpleCommandExceptionType TAKE_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.recipe.take.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("recipe").requires(source -> source.hasPermissionLevel(2))).then(CommandManager.literal("give").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("recipe", IdentifierArgumentType.identifier()).suggests(SuggestionProviders.ALL_RECIPES).executes(context -> RecipeCommand.executeGive((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), Collections.singleton(IdentifierArgumentType.getRecipeArgument(context, "recipe")))))).then(CommandManager.literal("*").executes(context -> RecipeCommand.executeGive((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), ((ServerCommandSource)context.getSource()).getServer().getRecipeManager().values())))))).then(CommandManager.literal("take").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("recipe", IdentifierArgumentType.identifier()).suggests(SuggestionProviders.ALL_RECIPES).executes(context -> RecipeCommand.executeTake((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), Collections.singleton(IdentifierArgumentType.getRecipeArgument(context, "recipe")))))).then(CommandManager.literal("*").executes(context -> RecipeCommand.executeTake((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), ((ServerCommandSource)context.getSource()).getServer().getRecipeManager().values()))))));
    }

    private static int executeGive(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Collection<Recipe<?>> recipes) throws CommandSyntaxException {
        int i = 0;
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            i += serverPlayerEntity.unlockRecipes(recipes);
        }
        if (i == 0) {
            throw GIVE_FAILED_EXCEPTION.create();
        }
        if (targets.size() == 1) {
            source.sendFeedback(new TranslatableText("commands.recipe.give.success.single", recipes.size(), targets.iterator().next().getDisplayName()), true);
        } else {
            source.sendFeedback(new TranslatableText("commands.recipe.give.success.multiple", recipes.size(), targets.size()), true);
        }
        return i;
    }

    private static int executeTake(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Collection<Recipe<?>> recipes) throws CommandSyntaxException {
        int i = 0;
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            i += serverPlayerEntity.lockRecipes(recipes);
        }
        if (i == 0) {
            throw TAKE_FAILED_EXCEPTION.create();
        }
        if (targets.size() == 1) {
            source.sendFeedback(new TranslatableText("commands.recipe.take.success.single", recipes.size(), targets.iterator().next().getDisplayName()), true);
        } else {
            source.sendFeedback(new TranslatableText("commands.recipe.take.success.multiple", recipes.size(), targets.size()), true);
        }
        return i;
    }
}

