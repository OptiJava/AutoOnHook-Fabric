/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.state.property;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.state.State;

public abstract class Property<T extends Comparable<T>> {
    private final Class<T> type;
    /**
     * The name of this property.
     * 
     * <p>Note that the name is required to match the {@linkplain
     * net.minecraft.state.StateManager#VALID_NAME_PATTERN valid name pattern}.
     * Otherwise, {@link IllegalArgumentException} will be thrown during the
     * {@linkplain net.minecraft.state.StateManager.Builder#validate(Property)
     * validation of a property}.
     */
    private final String name;
    private Integer hashCodeCache;
    private final Codec<T> codec = Codec.STRING.comapFlatMap(value -> this.parse((String)value).map(DataResult::success).orElseGet(() -> DataResult.error("Unable to read property: " + this + " with value: " + value)), this::name);
    private final Codec<Value<T>> valueCodec = this.codec.xmap(this::createValue, Value::getValue);

    protected Property(String name, Class<T> type) {
        this.type = type;
        this.name = name;
    }

    public Value<T> createValue(T value) {
        return new Value<T>(this, value);
    }

    public Value<T> createValue(State<?, ?> state) {
        return new Value(this, state.get(this));
    }

    public Stream<Value<T>> stream() {
        return this.getValues().stream().map(this::createValue);
    }

    public Codec<T> getCodec() {
        return this.codec;
    }

    public Codec<Value<T>> getValueCodec() {
        return this.valueCodec;
    }

    /**
     * Returns the name of this property.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the type of the values of this property.
     */
    public Class<T> getType() {
        return this.type;
    }

    /**
     * Returns all possible values of this property.
     */
    public abstract Collection<T> getValues();

    /**
     * Returns the name of the given value of this property.
     */
    public abstract String name(T var1);

    public abstract Optional<T> parse(String var1);

    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", this.name).add("clazz", this.type).add("values", this.getValues()).toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Property) {
            Property property = (Property)o;
            return this.type.equals(property.type) && this.name.equals(property.name);
        }
        return false;
    }

    public final int hashCode() {
        if (this.hashCodeCache == null) {
            this.hashCodeCache = this.computeHashCode();
        }
        return this.hashCodeCache;
    }

    public int computeHashCode() {
        return 31 * this.type.hashCode() + this.name.hashCode();
    }

    public <U, S extends State<?, S>> DataResult<S> method_35307(DynamicOps<U> dynamicOps, S state, U object) {
        DataResult dataResult = this.codec.parse(dynamicOps, object);
        return dataResult.map(comparable -> (State)state.with(this, comparable)).setPartial(state);
    }

    public static final class Value<T extends Comparable<T>> {
        private final Property<T> property;
        private final T value;

        Value(Property<T> property, T value) {
            if (!property.getValues().contains(value)) {
                throw new IllegalArgumentException("Value " + value + " does not belong to property " + property);
            }
            this.property = property;
            this.value = value;
        }

        public Property<T> getProperty() {
            return this.property;
        }

        public T getValue() {
            return this.value;
        }

        public String toString() {
            return this.property.getName() + "=" + this.property.name(this.value);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Value)) {
                return false;
            }
            Value value = (Value)o;
            return this.property == value.property && this.value.equals(value.value);
        }

        public int hashCode() {
            int i = this.property.hashCode();
            i = 31 * i + this.value.hashCode();
            return i;
        }
    }
}

