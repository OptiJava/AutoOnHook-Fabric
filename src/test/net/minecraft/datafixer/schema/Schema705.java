/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.datafixer.schema;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import net.minecraft.datafixer.schema.Schema100;
import net.minecraft.datafixer.schema.Schema704;
import net.minecraft.datafixer.schema.Schema99;

public class Schema705
extends IdentifierNormalizingSchema {
    protected static final Hook.HookFunction field_5746 = new Hook.HookFunction(){

        @Override
        public <T> T apply(DynamicOps<T> dynamicOps, T object) {
            return Schema99.method_5359(new Dynamic<T>(dynamicOps, object), Schema704.BLOCK_RENAMES, "minecraft:armor_stand");
        }
    };

    public Schema705(int i, Schema schema) {
        super(i, schema);
    }

    protected static void targetEntityItems(Schema schema, Map<String, Supplier<TypeTemplate>> map, String entityId) {
        schema.register(map, entityId, () -> Schema100.targetItems(schema));
    }

    protected static void targetInTile(Schema schema, Map<String, Supplier<TypeTemplate>> map, String entityId) {
        schema.register(map, entityId, () -> DSL.optionalFields("inTile", TypeReferences.BLOCK_NAME.in(schema)));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        HashMap<String, Supplier<TypeTemplate>> map = Maps.newHashMap();
        schema.registerSimple(map, "minecraft:area_effect_cloud");
        Schema705.targetEntityItems(schema, map, "minecraft:armor_stand");
        schema.register(map, "minecraft:arrow", (String string) -> DSL.optionalFields("inTile", TypeReferences.BLOCK_NAME.in(schema)));
        Schema705.targetEntityItems(schema, map, "minecraft:bat");
        Schema705.targetEntityItems(schema, map, "minecraft:blaze");
        schema.registerSimple(map, "minecraft:boat");
        Schema705.targetEntityItems(schema, map, "minecraft:cave_spider");
        schema.register(map, "minecraft:chest_minecart", (String string) -> DSL.optionalFields("DisplayTile", TypeReferences.BLOCK_NAME.in(schema), "Items", DSL.list(TypeReferences.ITEM_STACK.in(schema))));
        Schema705.targetEntityItems(schema, map, "minecraft:chicken");
        schema.register(map, "minecraft:commandblock_minecart", (String string) -> DSL.optionalFields("DisplayTile", TypeReferences.BLOCK_NAME.in(schema)));
        Schema705.targetEntityItems(schema, map, "minecraft:cow");
        Schema705.targetEntityItems(schema, map, "minecraft:creeper");
        schema.register(map, "minecraft:donkey", (String string) -> DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "SaddleItem", TypeReferences.ITEM_STACK.in(schema), Schema100.targetItems(schema)));
        schema.registerSimple(map, "minecraft:dragon_fireball");
        Schema705.targetInTile(schema, map, "minecraft:egg");
        Schema705.targetEntityItems(schema, map, "minecraft:elder_guardian");
        schema.registerSimple(map, "minecraft:ender_crystal");
        Schema705.targetEntityItems(schema, map, "minecraft:ender_dragon");
        schema.register(map, "minecraft:enderman", (String string) -> DSL.optionalFields("carried", TypeReferences.BLOCK_NAME.in(schema), Schema100.targetItems(schema)));
        Schema705.targetEntityItems(schema, map, "minecraft:endermite");
        Schema705.targetInTile(schema, map, "minecraft:ender_pearl");
        schema.registerSimple(map, "minecraft:eye_of_ender_signal");
        schema.register(map, "minecraft:falling_block", (String string) -> DSL.optionalFields("Block", TypeReferences.BLOCK_NAME.in(schema), "TileEntityData", TypeReferences.BLOCK_ENTITY.in(schema)));
        Schema705.targetInTile(schema, map, "minecraft:fireball");
        schema.register(map, "minecraft:fireworks_rocket", (String string) -> DSL.optionalFields("FireworksItem", TypeReferences.ITEM_STACK.in(schema)));
        schema.register(map, "minecraft:furnace_minecart", (String string) -> DSL.optionalFields("DisplayTile", TypeReferences.BLOCK_NAME.in(schema)));
        Schema705.targetEntityItems(schema, map, "minecraft:ghast");
        Schema705.targetEntityItems(schema, map, "minecraft:giant");
        Schema705.targetEntityItems(schema, map, "minecraft:guardian");
        schema.register(map, "minecraft:hopper_minecart", (String string) -> DSL.optionalFields("DisplayTile", TypeReferences.BLOCK_NAME.in(schema), "Items", DSL.list(TypeReferences.ITEM_STACK.in(schema))));
        schema.register(map, "minecraft:horse", (String string) -> DSL.optionalFields("ArmorItem", TypeReferences.ITEM_STACK.in(schema), "SaddleItem", TypeReferences.ITEM_STACK.in(schema), Schema100.targetItems(schema)));
        Schema705.targetEntityItems(schema, map, "minecraft:husk");
        schema.register(map, "minecraft:item", (String string) -> DSL.optionalFields("Item", TypeReferences.ITEM_STACK.in(schema)));
        schema.register(map, "minecraft:item_frame", (String string) -> DSL.optionalFields("Item", TypeReferences.ITEM_STACK.in(schema)));
        schema.registerSimple(map, "minecraft:leash_knot");
        Schema705.targetEntityItems(schema, map, "minecraft:magma_cube");
        schema.register(map, "minecraft:minecart", (String string) -> DSL.optionalFields("DisplayTile", TypeReferences.BLOCK_NAME.in(schema)));
        Schema705.targetEntityItems(schema, map, "minecraft:mooshroom");
        schema.register(map, "minecraft:mule", (String string) -> DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "SaddleItem", TypeReferences.ITEM_STACK.in(schema), Schema100.targetItems(schema)));
        Schema705.targetEntityItems(schema, map, "minecraft:ocelot");
        schema.registerSimple(map, "minecraft:painting");
        schema.registerSimple(map, "minecraft:parrot");
        Schema705.targetEntityItems(schema, map, "minecraft:pig");
        Schema705.targetEntityItems(schema, map, "minecraft:polar_bear");
        schema.register(map, "minecraft:potion", (String string) -> DSL.optionalFields("Potion", TypeReferences.ITEM_STACK.in(schema), "inTile", TypeReferences.BLOCK_NAME.in(schema)));
        Schema705.targetEntityItems(schema, map, "minecraft:rabbit");
        Schema705.targetEntityItems(schema, map, "minecraft:sheep");
        Schema705.targetEntityItems(schema, map, "minecraft:shulker");
        schema.registerSimple(map, "minecraft:shulker_bullet");
        Schema705.targetEntityItems(schema, map, "minecraft:silverfish");
        Schema705.targetEntityItems(schema, map, "minecraft:skeleton");
        schema.register(map, "minecraft:skeleton_horse", (String string) -> DSL.optionalFields("SaddleItem", TypeReferences.ITEM_STACK.in(schema), Schema100.targetItems(schema)));
        Schema705.targetEntityItems(schema, map, "minecraft:slime");
        Schema705.targetInTile(schema, map, "minecraft:small_fireball");
        Schema705.targetInTile(schema, map, "minecraft:snowball");
        Schema705.targetEntityItems(schema, map, "minecraft:snowman");
        schema.register(map, "minecraft:spawner_minecart", (String string) -> DSL.optionalFields("DisplayTile", TypeReferences.BLOCK_NAME.in(schema), TypeReferences.UNTAGGED_SPAWNER.in(schema)));
        schema.register(map, "minecraft:spectral_arrow", (String string) -> DSL.optionalFields("inTile", TypeReferences.BLOCK_NAME.in(schema)));
        Schema705.targetEntityItems(schema, map, "minecraft:spider");
        Schema705.targetEntityItems(schema, map, "minecraft:squid");
        Schema705.targetEntityItems(schema, map, "minecraft:stray");
        schema.registerSimple(map, "minecraft:tnt");
        schema.register(map, "minecraft:tnt_minecart", (String string) -> DSL.optionalFields("DisplayTile", TypeReferences.BLOCK_NAME.in(schema)));
        schema.register(map, "minecraft:villager", (String string) -> DSL.optionalFields("Inventory", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "Offers", DSL.optionalFields("Recipes", DSL.list(DSL.optionalFields("buy", TypeReferences.ITEM_STACK.in(schema), "buyB", TypeReferences.ITEM_STACK.in(schema), "sell", TypeReferences.ITEM_STACK.in(schema)))), Schema100.targetItems(schema)));
        Schema705.targetEntityItems(schema, map, "minecraft:villager_golem");
        Schema705.targetEntityItems(schema, map, "minecraft:witch");
        Schema705.targetEntityItems(schema, map, "minecraft:wither");
        Schema705.targetEntityItems(schema, map, "minecraft:wither_skeleton");
        Schema705.targetInTile(schema, map, "minecraft:wither_skull");
        Schema705.targetEntityItems(schema, map, "minecraft:wolf");
        Schema705.targetInTile(schema, map, "minecraft:xp_bottle");
        schema.registerSimple(map, "minecraft:xp_orb");
        Schema705.targetEntityItems(schema, map, "minecraft:zombie");
        schema.register(map, "minecraft:zombie_horse", (String string) -> DSL.optionalFields("SaddleItem", TypeReferences.ITEM_STACK.in(schema), Schema100.targetItems(schema)));
        Schema705.targetEntityItems(schema, map, "minecraft:zombie_pigman");
        Schema705.targetEntityItems(schema, map, "minecraft:zombie_villager");
        schema.registerSimple(map, "minecraft:evocation_fangs");
        Schema705.targetEntityItems(schema, map, "minecraft:evocation_illager");
        schema.registerSimple(map, "minecraft:illusion_illager");
        schema.register(map, "minecraft:llama", (String string) -> DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "SaddleItem", TypeReferences.ITEM_STACK.in(schema), "DecorItem", TypeReferences.ITEM_STACK.in(schema), Schema100.targetItems(schema)));
        schema.registerSimple(map, "minecraft:llama_spit");
        Schema705.targetEntityItems(schema, map, "minecraft:vex");
        Schema705.targetEntityItems(schema, map, "minecraft:vindication_illager");
        return map;
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);
        schema.registerType(true, TypeReferences.ENTITY, () -> DSL.taggedChoiceLazy("id", Schema705.getIdentifierType(), entityTypes));
        schema.registerType(true, TypeReferences.ITEM_STACK, () -> DSL.hook(DSL.optionalFields("id", TypeReferences.ITEM_NAME.in(schema), "tag", DSL.optionalFields("EntityTag", TypeReferences.ENTITY_TREE.in(schema), "BlockEntityTag", TypeReferences.BLOCK_ENTITY.in(schema), "CanDestroy", DSL.list(TypeReferences.BLOCK_NAME.in(schema)), "CanPlaceOn", DSL.list(TypeReferences.BLOCK_NAME.in(schema)), "Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)))), field_5746, Hook.HookFunction.IDENTITY));
    }
}

