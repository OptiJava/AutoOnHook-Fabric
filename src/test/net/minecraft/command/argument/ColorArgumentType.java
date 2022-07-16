/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class ColorArgumentType
implements ArgumentType<Formatting> {
    private static final Collection<String> EXAMPLES = Arrays.asList("red", "green");
    public static final DynamicCommandExceptionType INVALID_COLOR_EXCEPTION = new DynamicCommandExceptionType(color -> new TranslatableText("argument.color.invalid", color));

    private ColorArgumentType() {
    }

    public static ColorArgumentType color() {
        return new ColorArgumentType();
    }

    public static Formatting getColor(CommandContext<ServerCommandSource> context, String name) {
        return context.getArgument(name, Formatting.class);
    }

    @Override
    public Formatting parse(StringReader stringReader) throws CommandSyntaxException {
        String string = stringReader.readUnquotedString();
        Formatting formatting = Formatting.byName(string);
        if (formatting == null || formatting.isModifier()) {
            throw INVALID_COLOR_EXCEPTION.create(string);
        }
        return formatting;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(Formatting.getNames(true, false), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }
}

