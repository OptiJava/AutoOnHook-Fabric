/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.data.client.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;

public interface When
extends Supplier<JsonElement> {
    public void validate(StateManager<?, ?> var1);

    public static PropertyCondition create() {
        return new PropertyCondition();
    }

    public static When allOf(When ... conditions) {
        return new LogicalCondition(LogicalOperator.AND, Arrays.asList(conditions));
    }

    public static When anyOf(When ... conditions) {
        return new LogicalCondition(LogicalOperator.OR, Arrays.asList(conditions));
    }

    public static class PropertyCondition
    implements When {
        private final Map<Property<?>, String> properties = Maps.newHashMap();

        private static <T extends Comparable<T>> String name(Property<T> property, Stream<T> valueStream) {
            return valueStream.map(property::name).collect(Collectors.joining("|"));
        }

        private static <T extends Comparable<T>> String name(Property<T> property, T value, T[] otherValues) {
            return PropertyCondition.name(property, Stream.concat(Stream.of(value), Stream.of(otherValues)));
        }

        private <T extends Comparable<T>> void set(Property<T> property, String value) {
            String string = this.properties.put(property, value);
            if (string != null) {
                throw new IllegalStateException("Tried to replace " + property + " value from " + string + " to " + value);
            }
        }

        public final <T extends Comparable<T>> PropertyCondition set(Property<T> property, T value) {
            this.set(property, property.name(value));
            return this;
        }

        @SafeVarargs
        public final <T extends Comparable<T>> PropertyCondition set(Property<T> property, T value, T ... otherValues) {
            this.set(property, PropertyCondition.name(property, value, otherValues));
            return this;
        }

        public final <T extends Comparable<T>> PropertyCondition method_35871(Property<T> property, T comparable) {
            this.set(property, "!" + property.name(comparable));
            return this;
        }

        @SafeVarargs
        public final <T extends Comparable<T>> PropertyCondition method_35872(Property<T> property, T comparable, T ... comparables) {
            this.set(property, "!" + PropertyCondition.name(property, comparable, comparables));
            return this;
        }

        @Override
        public JsonElement get() {
            JsonObject jsonObject = new JsonObject();
            this.properties.forEach((property, string) -> jsonObject.addProperty(property.getName(), (String)string));
            return jsonObject;
        }

        @Override
        public void validate(StateManager<?, ?> stateManager) {
            List list = this.properties.keySet().stream().filter(property -> stateManager.getProperty(property.getName()) != property).collect(Collectors.toList());
            if (!list.isEmpty()) {
                throw new IllegalStateException("Properties " + list + " are missing from " + stateManager);
            }
        }

        @Override
        public /* synthetic */ Object get() {
            return this.get();
        }
    }

    public static class LogicalCondition
    implements When {
        private final LogicalOperator operator;
        private final List<When> components;

        LogicalCondition(LogicalOperator logicalOperator, List<When> list) {
            this.operator = logicalOperator;
            this.components = list;
        }

        @Override
        public void validate(StateManager<?, ?> stateManager) {
            this.components.forEach(when -> when.validate(stateManager));
        }

        @Override
        public JsonElement get() {
            JsonArray jsonArray = new JsonArray();
            this.components.stream().map(Supplier::get).forEach(jsonArray::add);
            JsonObject jsonObject = new JsonObject();
            jsonObject.add(this.operator.name, jsonArray);
            return jsonObject;
        }

        @Override
        public /* synthetic */ Object get() {
            return this.get();
        }
    }

    public static enum LogicalOperator {
        AND("AND"),
        OR("OR");

        final String name;

        private LogicalOperator(String name) {
            this.name = name;
        }
    }
}

