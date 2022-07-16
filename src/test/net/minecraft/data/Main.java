/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.data;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.SnbtProvider;
import net.minecraft.data.client.BlockStateDefinitionProvider;
import net.minecraft.data.dev.NbtProvider;
import net.minecraft.data.report.BiomeListProvider;
import net.minecraft.data.report.BlockListProvider;
import net.minecraft.data.report.CommandSyntaxProvider;
import net.minecraft.data.report.RegistryDumpProvider;
import net.minecraft.data.server.AdvancementsProvider;
import net.minecraft.data.server.BlockTagsProvider;
import net.minecraft.data.server.EntityTypeTagsProvider;
import net.minecraft.data.server.FluidTagsProvider;
import net.minecraft.data.server.GameEventTagsProvider;
import net.minecraft.data.server.ItemTagsProvider;
import net.minecraft.data.server.LootTablesProvider;
import net.minecraft.data.server.RecipesProvider;
import net.minecraft.data.validate.StructureValidatorProvider;
import net.minecraft.obfuscate.DontObfuscate;

public class Main {
    @DontObfuscate
    public static void main(String[] args) throws IOException {
        SharedConstants.createGameVersion();
        OptionParser optionParser = new OptionParser();
        AbstractOptionSpec optionSpec = optionParser.accepts("help", "Show the help menu").forHelp();
        OptionSpecBuilder optionSpec2 = optionParser.accepts("server", "Include server generators");
        OptionSpecBuilder optionSpec3 = optionParser.accepts("client", "Include client generators");
        OptionSpecBuilder optionSpec4 = optionParser.accepts("dev", "Include development tools");
        OptionSpecBuilder optionSpec5 = optionParser.accepts("reports", "Include data reports");
        OptionSpecBuilder optionSpec6 = optionParser.accepts("validate", "Validate inputs");
        OptionSpecBuilder optionSpec7 = optionParser.accepts("all", "Include all generators");
        ArgumentAcceptingOptionSpec<String> optionSpec8 = optionParser.accepts("output", "Output folder").withRequiredArg().defaultsTo("generated", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec9 = optionParser.accepts("input", "Input folder").withRequiredArg();
        OptionSet optionSet = optionParser.parse(args);
        if (optionSet.has(optionSpec) || !optionSet.hasOptions()) {
            optionParser.printHelpOn(System.out);
            return;
        }
        Path path = Paths.get((String)optionSpec8.value(optionSet), new String[0]);
        boolean bl = optionSet.has(optionSpec7);
        boolean bl2 = bl || optionSet.has(optionSpec3);
        boolean bl3 = bl || optionSet.has(optionSpec2);
        boolean bl4 = bl || optionSet.has(optionSpec4);
        boolean bl5 = bl || optionSet.has(optionSpec5);
        boolean bl6 = bl || optionSet.has(optionSpec6);
        DataGenerator dataGenerator = Main.create(path, optionSet.valuesOf(optionSpec9).stream().map(string -> Paths.get(string, new String[0])).collect(Collectors.toList()), bl2, bl3, bl4, bl5, bl6);
        dataGenerator.run();
    }

    public static DataGenerator create(Path output, Collection<Path> inputs, boolean includeClient, boolean includeServer, boolean includeDev, boolean includeReports, boolean validate) {
        DataGenerator dataGenerator = new DataGenerator(output, inputs);
        if (includeClient || includeServer) {
            dataGenerator.install(new SnbtProvider(dataGenerator).addWriter(new StructureValidatorProvider()));
        }
        if (includeClient) {
            dataGenerator.install(new BlockStateDefinitionProvider(dataGenerator));
        }
        if (includeServer) {
            dataGenerator.install(new FluidTagsProvider(dataGenerator));
            BlockTagsProvider blockTagsProvider = new BlockTagsProvider(dataGenerator);
            dataGenerator.install(blockTagsProvider);
            dataGenerator.install(new ItemTagsProvider(dataGenerator, blockTagsProvider));
            dataGenerator.install(new EntityTypeTagsProvider(dataGenerator));
            dataGenerator.install(new RecipesProvider(dataGenerator));
            dataGenerator.install(new AdvancementsProvider(dataGenerator));
            dataGenerator.install(new LootTablesProvider(dataGenerator));
            dataGenerator.install(new GameEventTagsProvider(dataGenerator));
        }
        if (includeDev) {
            dataGenerator.install(new NbtProvider(dataGenerator));
        }
        if (includeReports) {
            dataGenerator.install(new BlockListProvider(dataGenerator));
            dataGenerator.install(new RegistryDumpProvider(dataGenerator));
            dataGenerator.install(new CommandSyntaxProvider(dataGenerator));
            dataGenerator.install(new BiomeListProvider(dataGenerator));
        }
        return dataGenerator;
    }
}
