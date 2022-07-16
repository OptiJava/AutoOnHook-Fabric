/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;

public class ChunkToProtoChunkFix
extends DataFix {
    private static final int field_29881 = 16;

    public ChunkToProtoChunkFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.CHUNK);
        Type<?> type2 = this.getOutputSchema().getType(TypeReferences.CHUNK);
        Type<?> type3 = type.findFieldType("Level");
        Type<?> type4 = type2.findFieldType("Level");
        Type<?> type5 = type3.findFieldType("TileTicks");
        OpticFinder<?> opticFinder = DSL.fieldFinder("Level", type3);
        OpticFinder<?> opticFinder2 = DSL.fieldFinder("TileTicks", type5);
        return TypeRewriteRule.seq(this.fixTypeEverywhereTyped("ChunkToProtoChunkFix", type, this.getOutputSchema().getType(TypeReferences.CHUNK), (Typed<?> typed) -> typed.updateTyped(opticFinder, type4, typed2 -> {
            Dynamic<Object> dynamic2;
            Optional optional = typed2.getOptionalTyped(opticFinder2).flatMap(typed -> typed.write().result()).flatMap(dynamic -> dynamic.asStreamOpt().result());
            Dynamic<Object> dynamic3 = typed2.get(DSL.remainderFinder());
            boolean bl = dynamic3.get("TerrainPopulated").asBoolean(false) && (!dynamic3.get("LightPopulated").asNumber().result().isPresent() || dynamic3.get("LightPopulated").asBoolean(false));
            dynamic3 = dynamic3.set("Status", dynamic3.createString(bl ? "mobs_spawned" : "empty"));
            dynamic3 = dynamic3.set("hasLegacyStructureData", dynamic3.createBoolean(true));
            if (bl) {
                Object is;
                Object byteBuffer;
                Optional<ByteBuffer> optional2 = dynamic3.get("Biomes").asByteBufferOpt().result();
                if (optional2.isPresent()) {
                    byteBuffer = optional2.get();
                    is = new int[256];
                    for (int i2 = 0; i2 < ((int[])is).length; ++i2) {
                        if (i2 >= ((Buffer)byteBuffer).capacity()) continue;
                        is[i2] = ((ByteBuffer)byteBuffer).get(i2) & 0xFF;
                    }
                    dynamic3 = dynamic3.set("Biomes", dynamic3.createIntList(Arrays.stream((int[])is)));
                }
                byteBuffer = dynamic3;
                is = IntStream.range(0, 16).mapToObj(i -> new ShortArrayList()).collect(Collectors.toList());
                if (optional.isPresent()) {
                    ((Stream)optional.get()).forEach(arg_0 -> ChunkToProtoChunkFix.method_28186((List)is, arg_0));
                    dynamic3 = dynamic3.set("ToBeTicked", dynamic3.createList(is.stream().map(arg_0 -> ChunkToProtoChunkFix.method_28185((Dynamic)byteBuffer, arg_0))));
                }
                dynamic2 = DataFixUtils.orElse(typed2.set(DSL.remainderFinder(), dynamic3).write().result(), dynamic3);
            } else {
                dynamic2 = dynamic3;
            }
            return type4.readTyped(dynamic2).result().orElseThrow(() -> new IllegalStateException("Could not read the new chunk")).getFirst();
        })), this.writeAndRead("Structure biome inject", this.getInputSchema().getType(TypeReferences.STRUCTURE_FEATURE), this.getOutputSchema().getType(TypeReferences.STRUCTURE_FEATURE)));
    }

    private static short method_15675(int i, int j, int k) {
        return (short)(i & 0xF | (j & 0xF) << 4 | (k & 0xF) << 8);
    }

    private static /* synthetic */ Dynamic method_28185(Dynamic dynamic, ShortList shortList) {
        return dynamic.createList(shortList.stream().map(dynamic::createShort));
    }

    private static /* synthetic */ void method_28186(List list, Dynamic dynamic) {
        int i = dynamic.get("x").asInt(0);
        int j = dynamic.get("y").asInt(0);
        int k = dynamic.get("z").asInt(0);
        short s = ChunkToProtoChunkFix.method_15675(i, j, k);
        ((ShortList)list.get(j >> 4)).add(s);
    }
}

