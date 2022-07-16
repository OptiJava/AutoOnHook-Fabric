/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.command.argument;

import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class TextArgumentType
implements ArgumentType<Text> {
    private static final Collection<String> EXAMPLES = Arrays.asList("\"hello world\"", "\"\"", "\"{\"text\":\"hello world\"}", "[\"\"]");
    public static final DynamicCommandExceptionType INVALID_COMPONENT_EXCEPTION = new DynamicCommandExceptionType(text -> new TranslatableText("argument.component.invalid", text));

    private TextArgumentType() {
    }

    public static Text getTextArgument(CommandContext<ServerCommandSource> context, String name) {
        return context.getArgument(name, Text.class);
    }

    public static TextArgumentType text() {
        return new TextArgumentType();
    }

    @Override
    public Text parse(StringReader stringReader) throws CommandSyntaxException {
        try {
            MutableText text = Text.Serializer.fromJson(stringReader);
            if (text == null) {
                throw INVALID_COMPONENT_EXCEPTION.createWithContext(stringReader, "empty");
            }
            return text;
        }
        catch (JsonParseException text) {
            String string = text.getCause() != null ? text.getCause().getMessage() : text.getMessage();
            throw INVALID_COMPONENT_EXCEPTION.createWithContext(stringReader, string);
        }
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

