/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render.model.json;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.render.model.json.ModelRotation;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ModelElement {
    private static final boolean field_32785 = false;
    private static final float field_32786 = -16.0f;
    private static final float field_32787 = 32.0f;
    public final Vec3f from;
    public final Vec3f to;
    public final Map<Direction, ModelElementFace> faces;
    public final ModelRotation rotation;
    public final boolean shade;

    public ModelElement(Vec3f from, Vec3f to, Map<Direction, ModelElementFace> faces, @Nullable ModelRotation rotation, boolean shade) {
        this.from = from;
        this.to = to;
        this.faces = faces;
        this.rotation = rotation;
        this.shade = shade;
        this.initTextures();
    }

    private void initTextures() {
        for (Map.Entry<Direction, ModelElementFace> entry : this.faces.entrySet()) {
            float[] fs = this.getRotatedMatrix(entry.getKey());
            entry.getValue().textureData.setUvs(fs);
        }
    }

    private float[] getRotatedMatrix(Direction direction) {
        switch (direction) {
            case DOWN: {
                return new float[]{this.from.getX(), 16.0f - this.to.getZ(), this.to.getX(), 16.0f - this.from.getZ()};
            }
            case UP: {
                return new float[]{this.from.getX(), this.from.getZ(), this.to.getX(), this.to.getZ()};
            }
            default: {
                return new float[]{16.0f - this.to.getX(), 16.0f - this.to.getY(), 16.0f - this.from.getX(), 16.0f - this.from.getY()};
            }
            case SOUTH: {
                return new float[]{this.from.getX(), 16.0f - this.to.getY(), this.to.getX(), 16.0f - this.from.getY()};
            }
            case WEST: {
                return new float[]{this.from.getZ(), 16.0f - this.to.getY(), this.to.getZ(), 16.0f - this.from.getY()};
            }
            case EAST: 
        }
        return new float[]{16.0f - this.to.getZ(), 16.0f - this.to.getY(), 16.0f - this.from.getZ(), 16.0f - this.from.getY()};
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Deserializer
    implements JsonDeserializer<ModelElement> {
        private static final boolean DEFAULT_SHADE = true;

        protected Deserializer() {
        }

        @Override
        public ModelElement deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Vec3f vec3f = this.deserializeFrom(jsonObject);
            Vec3f vec3f2 = this.deserializeTo(jsonObject);
            ModelRotation modelRotation = this.deserializeRotation(jsonObject);
            Map<Direction, ModelElementFace> map = this.deserializeFacesValidating(jsonDeserializationContext, jsonObject);
            if (jsonObject.has("shade") && !JsonHelper.hasBoolean(jsonObject, "shade")) {
                throw new JsonParseException("Expected shade to be a Boolean");
            }
            boolean bl = JsonHelper.getBoolean(jsonObject, "shade", true);
            return new ModelElement(vec3f, vec3f2, map, modelRotation, bl);
        }

        @Nullable
        private ModelRotation deserializeRotation(JsonObject object) {
            ModelRotation modelRotation = null;
            if (object.has("rotation")) {
                JsonObject jsonObject = JsonHelper.getObject(object, "rotation");
                Vec3f vec3f = this.deserializeVec3f(jsonObject, "origin");
                vec3f.scale(0.0625f);
                Direction.Axis axis = this.deserializeAxis(jsonObject);
                float f = this.deserializeRotationAngle(jsonObject);
                boolean bl = JsonHelper.getBoolean(jsonObject, "rescale", false);
                modelRotation = new ModelRotation(vec3f, axis, f, bl);
            }
            return modelRotation;
        }

        private float deserializeRotationAngle(JsonObject object) {
            float f = JsonHelper.getFloat(object, "angle");
            if (f != 0.0f && MathHelper.abs(f) != 22.5f && MathHelper.abs(f) != 45.0f) {
                throw new JsonParseException("Invalid rotation " + f + " found, only -45/-22.5/0/22.5/45 allowed");
            }
            return f;
        }

        private Direction.Axis deserializeAxis(JsonObject object) {
            String string = JsonHelper.getString(object, "axis");
            Direction.Axis axis = Direction.Axis.fromName(string.toLowerCase(Locale.ROOT));
            if (axis == null) {
                throw new JsonParseException("Invalid rotation axis: " + string);
            }
            return axis;
        }

        private Map<Direction, ModelElementFace> deserializeFacesValidating(JsonDeserializationContext context, JsonObject object) {
            Map<Direction, ModelElementFace> map = this.deserializeFaces(context, object);
            if (map.isEmpty()) {
                throw new JsonParseException("Expected between 1 and 6 unique faces, got 0");
            }
            return map;
        }

        private Map<Direction, ModelElementFace> deserializeFaces(JsonDeserializationContext context, JsonObject object) {
            EnumMap<Direction, ModelElementFace> map = Maps.newEnumMap(Direction.class);
            JsonObject jsonObject = JsonHelper.getObject(object, "faces");
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                Direction direction = this.getDirection(entry.getKey());
                map.put(direction, (ModelElementFace)context.deserialize(entry.getValue(), (Type)((Object)ModelElementFace.class)));
            }
            return map;
        }

        private Direction getDirection(String name) {
            Direction direction = Direction.byName(name);
            if (direction == null) {
                throw new JsonParseException("Unknown facing: " + name);
            }
            return direction;
        }

        private Vec3f deserializeTo(JsonObject object) {
            Vec3f vec3f = this.deserializeVec3f(object, "to");
            if (vec3f.getX() < -16.0f || vec3f.getY() < -16.0f || vec3f.getZ() < -16.0f || vec3f.getX() > 32.0f || vec3f.getY() > 32.0f || vec3f.getZ() > 32.0f) {
                throw new JsonParseException("'to' specifier exceeds the allowed boundaries: " + vec3f);
            }
            return vec3f;
        }

        private Vec3f deserializeFrom(JsonObject object) {
            Vec3f vec3f = this.deserializeVec3f(object, "from");
            if (vec3f.getX() < -16.0f || vec3f.getY() < -16.0f || vec3f.getZ() < -16.0f || vec3f.getX() > 32.0f || vec3f.getY() > 32.0f || vec3f.getZ() > 32.0f) {
                throw new JsonParseException("'from' specifier exceeds the allowed boundaries: " + vec3f);
            }
            return vec3f;
        }

        private Vec3f deserializeVec3f(JsonObject object, String name) {
            JsonArray jsonArray = JsonHelper.getArray(object, name);
            if (jsonArray.size() != 3) {
                throw new JsonParseException("Expected 3 " + name + " values, found: " + jsonArray.size());
            }
            float[] fs = new float[3];
            for (int i = 0; i < fs.length; ++i) {
                fs[i] = JsonHelper.asFloat(jsonArray.get(i), name + "[" + i + "]");
            }
            return new Vec3f(fs[0], fs[1], fs[2]);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            return this.deserialize(json, type, context);
        }
    }
}

