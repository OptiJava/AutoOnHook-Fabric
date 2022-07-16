/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.command.argument;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class BlockArgumentParser {
    public static final SimpleCommandExceptionType DISALLOWED_TAG_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("argument.block.tag.disallowed"));
    public static final DynamicCommandExceptionType INVALID_BLOCK_ID_EXCEPTION = new DynamicCommandExceptionType(object -> new TranslatableText("argument.block.id.invalid", object));
    public static final Dynamic2CommandExceptionType UNKNOWN_PROPERTY_EXCEPTION = new Dynamic2CommandExceptionType((object, object2) -> new TranslatableText("argument.block.property.unknown", object, object2));
    public static final Dynamic2CommandExceptionType DUPLICATE_PROPERTY_EXCEPTION = new Dynamic2CommandExceptionType((object, object2) -> new TranslatableText("argument.block.property.duplicate", object2, object));
    public static final Dynamic3CommandExceptionType INVALID_PROPERTY_EXCEPTION = new Dynamic3CommandExceptionType((object, object2, object3) -> new TranslatableText("argument.block.property.invalid", object, object3, object2));
    public static final Dynamic2CommandExceptionType EMPTY_PROPERTY_EXCEPTION = new Dynamic2CommandExceptionType((object, object2) -> new TranslatableText("argument.block.property.novalue", object, object2));
    public static final SimpleCommandExceptionType UNCLOSED_PROPERTIES_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("argument.block.property.unclosed"));
    private static final char field_32800 = '[';
    private static final char field_32801 = '{';
    private static final char field_32802 = ']';
    private static final char field_32803 = '=';
    private static final char field_32804 = ',';
    private static final char field_32805 = '#';
    private static final BiFunction<SuggestionsBuilder, TagGroup<Block>, CompletableFuture<Suggestions>> SUGGEST_DEFAULT = (suggestionsBuilder, tagGroup) -> suggestionsBuilder.buildFuture();
    private final StringReader reader;
    private final boolean allowTag;
    private final Map<Property<?>, Comparable<?>> blockProperties = Maps.newHashMap();
    private final Map<String, String> tagProperties = Maps.newHashMap();
    private Identifier blockId = new Identifier("");
    private StateManager<Block, BlockState> stateFactory;
    private BlockState blockState;
    @Nullable
    private NbtCompound data;
    private Identifier tagId = new Identifier("");
    private int cursorPos;
    private BiFunction<SuggestionsBuilder, TagGroup<Block>, CompletableFuture<Suggestions>> suggestions = SUGGEST_DEFAULT;

    public BlockArgumentParser(StringReader reader, boolean allowTag) {
        this.reader = reader;
        this.allowTag = allowTag;
    }

    public Map<Property<?>, Comparable<?>> getBlockProperties() {
        return this.blockProperties;
    }

    @Nullable
    public BlockState getBlockState() {
        return this.blockState;
    }

    @Nullable
    public NbtCompound getNbtData() {
        return this.data;
    }

    @Nullable
    public Identifier getTagId() {
        return this.tagId;
    }

    public BlockArgumentParser parse(boolean allowNbt) throws CommandSyntaxException {
        this.suggestions = this::suggestBlockOrTagId;
        if (this.reader.canRead() && this.reader.peek() == '#') {
            this.parseTagId();
            this.suggestions = this::suggestSnbtOrTagProperties;
            if (this.reader.canRead() && this.reader.peek() == '[') {
                this.parseTagProperties();
                this.suggestions = this::suggestSnbt;
            }
        } else {
            this.parseBlockId();
            this.suggestions = this::suggestSnbtOrBlockProperties;
            if (this.reader.canRead() && this.reader.peek() == '[') {
                this.parseBlockProperties();
                this.suggestions = this::suggestSnbt;
            }
        }
        if (allowNbt && this.reader.canRead() && this.reader.peek() == '{') {
            this.suggestions = SUGGEST_DEFAULT;
            this.parseSnbt();
        }
        return this;
    }

    private CompletableFuture<Suggestions> suggestBlockPropertiesOrEnd(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
        if (suggestionsBuilder.getRemaining().isEmpty()) {
            suggestionsBuilder.suggest(String.valueOf(']'));
        }
        return this.suggestBlockProperties(suggestionsBuilder, tagGroup);
    }

    private CompletableFuture<Suggestions> suggestTagPropertiesOrEnd(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
        if (suggestionsBuilder.getRemaining().isEmpty()) {
            suggestionsBuilder.suggest(String.valueOf(']'));
        }
        return this.suggestTagProperties(suggestionsBuilder, tagGroup);
    }

    private CompletableFuture<Suggestions> suggestBlockProperties(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
        String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        for (Property<?> property : this.blockState.getProperties()) {
            if (this.blockProperties.containsKey(property) || !property.getName().startsWith(string)) continue;
            suggestionsBuilder.suggest(property.getName() + "=");
        }
        return suggestionsBuilder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestTagProperties(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
        Tag<Block> tag;
        String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        if (this.tagId != null && !this.tagId.getPath().isEmpty() && (tag = tagGroup.getTag(this.tagId)) != null) {
            for (Block block : tag.values()) {
                for (Property<?> property : block.getStateManager().getProperties()) {
                    if (this.tagProperties.containsKey(property.getName()) || !property.getName().startsWith(string)) continue;
                    suggestionsBuilder.suggest(property.getName() + "=");
                }
            }
        }
        return suggestionsBuilder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestSnbt(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
        if (suggestionsBuilder.getRemaining().isEmpty() && this.hasBlockEntity(tagGroup)) {
            suggestionsBuilder.suggest(String.valueOf('{'));
        }
        return suggestionsBuilder.buildFuture();
    }

    private boolean hasBlockEntity(TagGroup<Block> tagGroup) {
        Tag<Block> tag;
        if (this.blockState != null) {
            return this.blockState.hasBlockEntity();
        }
        if (this.tagId != null && (tag = tagGroup.getTag(this.tagId)) != null) {
            for (Block block : tag.values()) {
                if (!block.getDefaultState().hasBlockEntity()) continue;
                return true;
            }
        }
        return false;
    }

    private CompletableFuture<Suggestions> suggestEqualsCharacter(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
        if (suggestionsBuilder.getRemaining().isEmpty()) {
            suggestionsBuilder.suggest(String.valueOf('='));
        }
        return suggestionsBuilder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestCommaOrEnd(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
        if (suggestionsBuilder.getRemaining().isEmpty()) {
            suggestionsBuilder.suggest(String.valueOf(']'));
        }
        if (suggestionsBuilder.getRemaining().isEmpty() && this.blockProperties.size() < this.blockState.getProperties().size()) {
            suggestionsBuilder.suggest(String.valueOf(','));
        }
        return suggestionsBuilder.buildFuture();
    }

    private static <T extends Comparable<T>> SuggestionsBuilder suggestPropertyValues(SuggestionsBuilder suggestionsBuilder, Property<T> property) {
        for (Comparable comparable : property.getValues()) {
            if (comparable instanceof Integer) {
                suggestionsBuilder.suggest((Integer)comparable);
                continue;
            }
            suggestionsBuilder.suggest(property.name(comparable));
        }
        return suggestionsBuilder;
    }

    private CompletableFuture<Suggestions> suggestTagPropertyValues(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup, String string) {
        Tag<Block> tag;
        boolean bl = false;
        if (this.tagId != null && !this.tagId.getPath().isEmpty() && (tag = tagGroup.getTag(this.tagId)) != null) {
            block0: for (Block block : tag.values()) {
                Property<?> property = block.getStateManager().getProperty(string);
                if (property != null) {
                    BlockArgumentParser.suggestPropertyValues(suggestionsBuilder, property);
                }
                if (bl) continue;
                for (Property<?> property2 : block.getStateManager().getProperties()) {
                    if (this.tagProperties.containsKey(property2.getName())) continue;
                    bl = true;
                    continue block0;
                }
            }
        }
        if (bl) {
            suggestionsBuilder.suggest(String.valueOf(','));
        }
        suggestionsBuilder.suggest(String.valueOf(']'));
        return suggestionsBuilder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestSnbtOrTagProperties(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
        Tag<Block> tag;
        if (suggestionsBuilder.getRemaining().isEmpty() && (tag = tagGroup.getTag(this.tagId)) != null) {
            Block block;
            boolean bl = false;
            boolean bl2 = false;
            Iterator<Block> iterator = tag.values().iterator();
            while (!(!iterator.hasNext() || (bl |= !(block = iterator.next()).getStateManager().getProperties().isEmpty()) && (bl2 |= block.getDefaultState().hasBlockEntity()))) {
            }
            if (bl) {
                suggestionsBuilder.suggest(String.valueOf('['));
            }
            if (bl2) {
                suggestionsBuilder.suggest(String.valueOf('{'));
            }
        }
        return this.suggestIdentifiers(suggestionsBuilder, tagGroup);
    }

    private CompletableFuture<Suggestions> suggestSnbtOrBlockProperties(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
        if (suggestionsBuilder.getRemaining().isEmpty()) {
            if (!this.blockState.getBlock().getStateManager().getProperties().isEmpty()) {
                suggestionsBuilder.suggest(String.valueOf('['));
            }
            if (this.blockState.hasBlockEntity()) {
                suggestionsBuilder.suggest(String.valueOf('{'));
            }
        }
        return suggestionsBuilder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestIdentifiers(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
        return CommandSource.suggestIdentifiers(tagGroup.getTagIds(), suggestionsBuilder.createOffset(this.cursorPos).add(suggestionsBuilder));
    }

    private CompletableFuture<Suggestions> suggestBlockOrTagId(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
        if (this.allowTag) {
            CommandSource.suggestIdentifiers(tagGroup.getTagIds(), suggestionsBuilder, String.valueOf('#'));
        }
        CommandSource.suggestIdentifiers(Registry.BLOCK.getIds(), suggestionsBuilder);
        return suggestionsBuilder.buildFuture();
    }

    public void parseBlockId() throws CommandSyntaxException {
        int i = this.reader.getCursor();
        this.blockId = Identifier.fromCommandInput(this.reader);
        Block block = Registry.BLOCK.getOrEmpty(this.blockId).orElseThrow(() -> {
            this.reader.setCursor(i);
            return INVALID_BLOCK_ID_EXCEPTION.createWithContext(this.reader, this.blockId.toString());
        });
        this.stateFactory = block.getStateManager();
        this.blockState = block.getDefaultState();
    }

    public void parseTagId() throws CommandSyntaxException {
        if (!this.allowTag) {
            throw DISALLOWED_TAG_EXCEPTION.create();
        }
        this.suggestions = this::suggestIdentifiers;
        this.reader.expect('#');
        this.cursorPos = this.reader.getCursor();
        this.tagId = Identifier.fromCommandInput(this.reader);
    }

    public void parseBlockProperties() throws CommandSyntaxException {
        this.reader.skip();
        this.suggestions = this::suggestBlockPropertiesOrEnd;
        this.reader.skipWhitespace();
        while (this.reader.canRead() && this.reader.peek() != ']') {
            this.reader.skipWhitespace();
            int i = this.reader.getCursor();
            String string = this.reader.readString();
            Property<?> property = this.stateFactory.getProperty(string);
            if (property == null) {
                this.reader.setCursor(i);
                throw UNKNOWN_PROPERTY_EXCEPTION.createWithContext(this.reader, this.blockId.toString(), string);
            }
            if (this.blockProperties.containsKey(property)) {
                this.reader.setCursor(i);
                throw DUPLICATE_PROPERTY_EXCEPTION.createWithContext(this.reader, this.blockId.toString(), string);
            }
            this.reader.skipWhitespace();
            this.suggestions = this::suggestEqualsCharacter;
            if (!this.reader.canRead() || this.reader.peek() != '=') {
                throw EMPTY_PROPERTY_EXCEPTION.createWithContext(this.reader, this.blockId.toString(), string);
            }
            this.reader.skip();
            this.reader.skipWhitespace();
            this.suggestions = (suggestionsBuilder, tagGroup) -> BlockArgumentParser.suggestPropertyValues(suggestionsBuilder, property).buildFuture();
            int j = this.reader.getCursor();
            this.parsePropertyValue(property, this.reader.readString(), j);
            this.suggestions = this::suggestCommaOrEnd;
            this.reader.skipWhitespace();
            if (!this.reader.canRead()) continue;
            if (this.reader.peek() == ',') {
                this.reader.skip();
                this.suggestions = this::suggestBlockProperties;
                continue;
            }
            if (this.reader.peek() == ']') break;
            throw UNCLOSED_PROPERTIES_EXCEPTION.createWithContext(this.reader);
        }
        if (!this.reader.canRead()) {
            throw UNCLOSED_PROPERTIES_EXCEPTION.createWithContext(this.reader);
        }
        this.reader.skip();
    }

    public void parseTagProperties() throws CommandSyntaxException {
        this.reader.skip();
        this.suggestions = this::suggestTagPropertiesOrEnd;
        int i = -1;
        this.reader.skipWhitespace();
        while (this.reader.canRead() && this.reader.peek() != ']') {
            this.reader.skipWhitespace();
            int j = this.reader.getCursor();
            String string = this.reader.readString();
            if (this.tagProperties.containsKey(string)) {
                this.reader.setCursor(j);
                throw DUPLICATE_PROPERTY_EXCEPTION.createWithContext(this.reader, this.blockId.toString(), string);
            }
            this.reader.skipWhitespace();
            if (!this.reader.canRead() || this.reader.peek() != '=') {
                this.reader.setCursor(j);
                throw EMPTY_PROPERTY_EXCEPTION.createWithContext(this.reader, this.blockId.toString(), string);
            }
            this.reader.skip();
            this.reader.skipWhitespace();
            this.suggestions = (suggestionsBuilder, tagGroup) -> this.suggestTagPropertyValues((SuggestionsBuilder)suggestionsBuilder, (TagGroup<Block>)tagGroup, string);
            i = this.reader.getCursor();
            String string2 = this.reader.readString();
            this.tagProperties.put(string, string2);
            this.reader.skipWhitespace();
            if (!this.reader.canRead()) continue;
            i = -1;
            if (this.reader.peek() == ',') {
                this.reader.skip();
                this.suggestions = this::suggestTagProperties;
                continue;
            }
            if (this.reader.peek() == ']') break;
            throw UNCLOSED_PROPERTIES_EXCEPTION.createWithContext(this.reader);
        }
        if (!this.reader.canRead()) {
            if (i >= 0) {
                this.reader.setCursor(i);
            }
            throw UNCLOSED_PROPERTIES_EXCEPTION.createWithContext(this.reader);
        }
        this.reader.skip();
    }

    public void parseSnbt() throws CommandSyntaxException {
        this.data = new StringNbtReader(this.reader).parseCompound();
    }

    private <T extends Comparable<T>> void parsePropertyValue(Property<T> property, String string, int i) throws CommandSyntaxException {
        Optional<T> optional = property.parse(string);
        if (!optional.isPresent()) {
            this.reader.setCursor(i);
            throw INVALID_PROPERTY_EXCEPTION.createWithContext(this.reader, this.blockId.toString(), property.getName(), string);
        }
        this.blockState = (BlockState)this.blockState.with(property, (Comparable)optional.get());
        this.blockProperties.put(property, (Comparable)optional.get());
    }

    public static String stringifyBlockState(BlockState blockState) {
        StringBuilder stringBuilder = new StringBuilder(Registry.BLOCK.getId(blockState.getBlock()).toString());
        if (!blockState.getProperties().isEmpty()) {
            stringBuilder.append('[');
            boolean bl = false;
            for (Map.Entry entry : blockState.getEntries().entrySet()) {
                if (bl) {
                    stringBuilder.append(',');
                }
                BlockArgumentParser.stringifyProperty(stringBuilder, (Property)entry.getKey(), (Comparable)entry.getValue());
                bl = true;
            }
            stringBuilder.append(']');
        }
        return stringBuilder.toString();
    }

    private static <T extends Comparable<T>> void stringifyProperty(StringBuilder stringBuilder, Property<T> property, Comparable<?> comparable) {
        stringBuilder.append(property.getName());
        stringBuilder.append('=');
        stringBuilder.append(property.name(comparable));
    }

    public CompletableFuture<Suggestions> getSuggestions(SuggestionsBuilder suggestionsBuilder, TagGroup<Block> tagGroup) {
        return this.suggestions.apply(suggestionsBuilder.createOffset(this.reader.getCursor()), tagGroup);
    }

    public Map<String, String> getProperties() {
        return this.tagProperties;
    }
}

