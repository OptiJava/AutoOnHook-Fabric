/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.render;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GLImportProcessor;
import net.minecraft.client.gl.GlBlendState;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.Program;
import net.minecraft.client.gl.ShaderParseException;
import net.minecraft.client.gl.Uniform;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.FileNameUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class Shader
implements GlShader,
AutoCloseable {
    private static final String CORE_DIRECTORY = "shaders/core/";
    private static final String INCLUDE_DIRECTORY = "shaders/include/";
    static final Logger LOGGER = LogManager.getLogger();
    private static final Uniform DEFAULT_UNIFORM = new Uniform();
    private static final boolean field_32780 = true;
    private static Shader activeShader;
    private static int activeShaderId;
    private final Map<String, Object> samplers = Maps.newHashMap();
    private final List<String> samplerNames = Lists.newArrayList();
    private final List<Integer> loadedSamplerIds = Lists.newArrayList();
    private final List<GlUniform> uniforms = Lists.newArrayList();
    private final List<Integer> loadedUniformIds = Lists.newArrayList();
    private final Map<String, GlUniform> loadedUniforms = Maps.newHashMap();
    private final int programId;
    private final String name;
    private boolean dirty;
    private final GlBlendState blendState;
    private final List<Integer> loadedAttributeIds;
    private final List<String> attributeNames;
    private final Program vertexShader;
    private final Program fragmentShader;
    private final VertexFormat format;
    @Nullable
    public final GlUniform modelViewMat;
    @Nullable
    public final GlUniform projectionMat;
    @Nullable
    public final GlUniform textureMat;
    @Nullable
    public final GlUniform screenSize;
    @Nullable
    public final GlUniform colorModulator;
    @Nullable
    public final GlUniform light0Direction;
    @Nullable
    public final GlUniform light1Direction;
    @Nullable
    public final GlUniform fogStart;
    @Nullable
    public final GlUniform fogEnd;
    @Nullable
    public final GlUniform fogColor;
    @Nullable
    public final GlUniform lineWidth;
    @Nullable
    public final GlUniform gameTime;
    @Nullable
    public final GlUniform chunkOffset;

    public Shader(ResourceFactory factory, String name, VertexFormat format) throws IOException {
        this.name = name;
        this.format = format;
        Identifier identifier = new Identifier(CORE_DIRECTORY + name + ".json");
        Resource resource = null;
        try {
            JsonArray j;
            JsonArray i;
            resource = factory.getResource(identifier);
            JsonObject jsonObject = JsonHelper.deserialize(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
            String string = JsonHelper.getString(jsonObject, "vertex");
            String string2 = JsonHelper.getString(jsonObject, "fragment");
            JsonArray jsonArray = JsonHelper.getArray(jsonObject, "samplers", null);
            if (jsonArray != null) {
                int i2 = 0;
                for (Object jsonElement : jsonArray) {
                    try {
                        this.readSampler((JsonElement)jsonElement);
                    }
                    catch (Exception exception) {
                        ShaderParseException shaderParseException = ShaderParseException.wrap(exception);
                        shaderParseException.addFaultyElement("samplers[" + i2 + "]");
                        throw shaderParseException;
                    }
                    ++i2;
                }
            }
            if ((i = JsonHelper.getArray(jsonObject, "attributes", null)) != null) {
                int j2 = 0;
                this.loadedAttributeIds = Lists.newArrayListWithCapacity(i.size());
                this.attributeNames = Lists.newArrayListWithCapacity(i.size());
                for (JsonElement exception : i) {
                    try {
                        this.attributeNames.add(JsonHelper.asString(exception, "attribute"));
                    }
                    catch (Exception exception2) {
                        ShaderParseException shaderParseException2 = ShaderParseException.wrap(exception2);
                        shaderParseException2.addFaultyElement("attributes[" + j2 + "]");
                        throw shaderParseException2;
                    }
                    ++j2;
                }
            } else {
                this.loadedAttributeIds = null;
                this.attributeNames = null;
            }
            if ((j = JsonHelper.getArray(jsonObject, "uniforms", null)) != null) {
                int jsonElement = 0;
                for (JsonElement jsonElement2 : j) {
                    try {
                        this.addUniform(jsonElement2);
                    }
                    catch (Exception shaderParseException2) {
                        ShaderParseException shaderParseException3 = ShaderParseException.wrap(shaderParseException2);
                        shaderParseException3.addFaultyElement("uniforms[" + jsonElement + "]");
                        throw shaderParseException3;
                    }
                    ++jsonElement;
                }
            }
            this.blendState = Shader.readBlendState(JsonHelper.getObject(jsonObject, "blend", null));
            this.vertexShader = Shader.loadProgram(factory, Program.Type.VERTEX, string);
            this.fragmentShader = Shader.loadProgram(factory, Program.Type.FRAGMENT, string2);
            this.programId = GlProgramManager.createProgram();
            if (this.attributeNames != null) {
                int jsonElement = 0;
                for (String string3 : format.getShaderAttributes()) {
                    GlUniform.bindAttribLocation(this.programId, jsonElement, string3);
                    this.loadedAttributeIds.add(jsonElement);
                    ++jsonElement;
                }
            }
            GlProgramManager.linkProgram(this);
            this.loadReferences();
        }
        catch (Exception string) {
            ShaderParseException string2 = ShaderParseException.wrap(string);
            string2.addFaultyFile(identifier.getPath());
            throw string2;
        }
        finally {
            IOUtils.closeQuietly((Closeable)resource);
        }
        this.markUniformsDirty();
        this.modelViewMat = this.getUniform("ModelViewMat");
        this.projectionMat = this.getUniform("ProjMat");
        this.textureMat = this.getUniform("TextureMat");
        this.screenSize = this.getUniform("ScreenSize");
        this.colorModulator = this.getUniform("ColorModulator");
        this.light0Direction = this.getUniform("Light0_Direction");
        this.light1Direction = this.getUniform("Light1_Direction");
        this.fogStart = this.getUniform("FogStart");
        this.fogEnd = this.getUniform("FogEnd");
        this.fogColor = this.getUniform("FogColor");
        this.lineWidth = this.getUniform("LineWidth");
        this.gameTime = this.getUniform("GameTime");
        this.chunkOffset = this.getUniform("ChunkOffset");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Program loadProgram(final ResourceFactory factory, Program.Type type, String name) throws IOException {
        Program program2;
        Program program = type.getProgramCache().get(name);
        if (program == null) {
            String string = CORE_DIRECTORY + name + type.getFileExtension();
            Identifier identifier = new Identifier(string);
            Resource resource = factory.getResource(identifier);
            final String string2 = FileNameUtil.getPosixFullPath(string);
            try {
                program2 = Program.createFromResource(type, name, resource.getInputStream(), resource.getResourcePackName(), new GLImportProcessor(){
                    private final Set<String> visitedImports = Sets.newHashSet();

                    @Override
                    public String loadImport(boolean inline, String name) {
                        String string;
                        block9: {
                            name = FileNameUtil.normalizeToPosix((inline ? string2 : Shader.INCLUDE_DIRECTORY) + name);
                            if (!this.visitedImports.add(name)) {
                                return null;
                            }
                            Identifier identifier = new Identifier(name);
                            Resource resource = factory.getResource(identifier);
                            try {
                                string = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
                                if (resource == null) break block9;
                            }
                            catch (Throwable throwable) {
                                try {
                                    if (resource != null) {
                                        try {
                                            resource.close();
                                        }
                                        catch (Throwable throwable2) {
                                            throwable.addSuppressed(throwable2);
                                        }
                                    }
                                    throw throwable;
                                }
                                catch (IOException resource2) {
                                    LOGGER.error("Could not open GLSL import {}: {}", (Object)name, (Object)resource2.getMessage());
                                    return "#error " + resource2.getMessage();
                                }
                            }
                            resource.close();
                        }
                        return string;
                    }
                });
            }
            finally {
                IOUtils.closeQuietly((Closeable)resource);
            }
        } else {
            program2 = program;
        }
        return program2;
    }

    public static GlBlendState readBlendState(JsonObject json) {
        if (json == null) {
            return new GlBlendState();
        }
        int i = 32774;
        int j = 1;
        int k = 0;
        int l = 1;
        int m = 0;
        boolean bl = true;
        boolean bl2 = false;
        if (JsonHelper.hasString(json, "func") && (i = GlBlendState.getFuncFromString(json.get("func").getAsString())) != 32774) {
            bl = false;
        }
        if (JsonHelper.hasString(json, "srcrgb") && (j = GlBlendState.getComponentFromString(json.get("srcrgb").getAsString())) != 1) {
            bl = false;
        }
        if (JsonHelper.hasString(json, "dstrgb") && (k = GlBlendState.getComponentFromString(json.get("dstrgb").getAsString())) != 0) {
            bl = false;
        }
        if (JsonHelper.hasString(json, "srcalpha")) {
            l = GlBlendState.getComponentFromString(json.get("srcalpha").getAsString());
            if (l != 1) {
                bl = false;
            }
            bl2 = true;
        }
        if (JsonHelper.hasString(json, "dstalpha")) {
            m = GlBlendState.getComponentFromString(json.get("dstalpha").getAsString());
            if (m != 0) {
                bl = false;
            }
            bl2 = true;
        }
        if (bl) {
            return new GlBlendState();
        }
        if (bl2) {
            return new GlBlendState(j, k, l, m, i);
        }
        return new GlBlendState(j, k, i);
    }

    @Override
    public void close() {
        for (GlUniform glUniform : this.uniforms) {
            glUniform.close();
        }
        GlProgramManager.deleteProgram(this);
    }

    public void unbind() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlProgramManager.useProgram(0);
        activeShaderId = -1;
        activeShader = null;
        int i = GlStateManager._getActiveTexture();
        for (int j = 0; j < this.loadedSamplerIds.size(); ++j) {
            if (this.samplers.get(this.samplerNames.get(j)) == null) continue;
            GlStateManager._activeTexture(33984 + j);
            GlStateManager._bindTexture(0);
        }
        GlStateManager._activeTexture(i);
    }

    public void bind() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        this.dirty = false;
        activeShader = this;
        this.blendState.enable();
        if (this.programId != activeShaderId) {
            GlProgramManager.useProgram(this.programId);
            activeShaderId = this.programId;
        }
        int i = GlStateManager._getActiveTexture();
        for (int j = 0; j < this.loadedSamplerIds.size(); ++j) {
            String string = this.samplerNames.get(j);
            if (this.samplers.get(string) == null) continue;
            int k = GlUniform.getUniformLocation(this.programId, string);
            GlUniform.uniform1(k, j);
            RenderSystem.activeTexture(33984 + j);
            RenderSystem.enableTexture();
            Object object = this.samplers.get(string);
            int l = -1;
            if (object instanceof Framebuffer) {
                l = ((Framebuffer)object).getColorAttachment();
            } else if (object instanceof AbstractTexture) {
                l = ((AbstractTexture)object).getGlId();
            } else if (object instanceof Integer) {
                l = (Integer)object;
            }
            if (l == -1) continue;
            RenderSystem.bindTexture(l);
        }
        GlStateManager._activeTexture(i);
        for (GlUniform glUniform : this.uniforms) {
            glUniform.upload();
        }
    }

    @Override
    public void markUniformsDirty() {
        this.dirty = true;
    }

    @Nullable
    public GlUniform getUniform(String name) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return this.loadedUniforms.get(name);
    }

    public Uniform getUniformOrDefault(String name) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlUniform glUniform = this.getUniform(name);
        return glUniform == null ? DEFAULT_UNIFORM : glUniform;
    }

    private void loadReferences() {
        int i;
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        IntArrayList intList = new IntArrayList();
        for (i = 0; i < this.samplerNames.size(); ++i) {
            String string = this.samplerNames.get(i);
            int j = GlUniform.getUniformLocation(this.programId, string);
            if (j == -1) {
                LOGGER.warn("Shader {} could not find sampler named {} in the specified shader program.", (Object)this.name, (Object)string);
                this.samplers.remove(string);
                intList.add(i);
                continue;
            }
            this.loadedSamplerIds.add(j);
        }
        for (i = intList.size() - 1; i >= 0; --i) {
            int string = intList.getInt(i);
            this.samplerNames.remove(string);
        }
        for (GlUniform string : this.uniforms) {
            String j = string.getName();
            int k = GlUniform.getUniformLocation(this.programId, j);
            if (k == -1) {
                LOGGER.warn("Shader {} could not find uniform named {} in the specified shader program.", (Object)this.name, (Object)j);
                continue;
            }
            this.loadedUniformIds.add(k);
            string.setLoc(k);
            this.loadedUniforms.put(j, string);
        }
    }

    private void readSampler(JsonElement json) {
        JsonObject jsonObject = JsonHelper.asObject(json, "sampler");
        String string = JsonHelper.getString(jsonObject, "name");
        if (!JsonHelper.hasString(jsonObject, "file")) {
            this.samplers.put(string, null);
            this.samplerNames.add(string);
            return;
        }
        this.samplerNames.add(string);
    }

    public void addSampler(String name, Object sampler) {
        this.samplers.put(name, sampler);
        this.markUniformsDirty();
    }

    private void addUniform(JsonElement json) throws ShaderParseException {
        Object jsonElement2;
        JsonObject jsonObject = JsonHelper.asObject(json, "uniform");
        String string = JsonHelper.getString(jsonObject, "name");
        int i = GlUniform.getTypeIndex(JsonHelper.getString(jsonObject, "type"));
        int j = JsonHelper.getInt(jsonObject, "count");
        float[] fs = new float[Math.max(j, 16)];
        JsonArray jsonArray = JsonHelper.getArray(jsonObject, "values");
        if (jsonArray.size() != j && jsonArray.size() > 1) {
            throw new ShaderParseException("Invalid amount of values specified (expected " + j + ", found " + jsonArray.size() + ")");
        }
        int k = 0;
        for (Object jsonElement2 : jsonArray) {
            try {
                fs[k] = JsonHelper.asFloat((JsonElement)jsonElement2, "value");
            }
            catch (Exception exception) {
                ShaderParseException shaderParseException = ShaderParseException.wrap(exception);
                shaderParseException.addFaultyElement("values[" + k + "]");
                throw shaderParseException;
            }
            ++k;
        }
        if (j > 1 && jsonArray.size() == 1) {
            while (k < j) {
                fs[k] = fs[0];
                ++k;
            }
        }
        int l = j > 1 && j <= 4 && i < 8 ? j - 1 : 0;
        jsonElement2 = new GlUniform(string, i + l, j, this);
        if (i <= 3) {
            ((GlUniform)jsonElement2).setForDataType((int)fs[0], (int)fs[1], (int)fs[2], (int)fs[3]);
        } else if (i <= 7) {
            ((GlUniform)jsonElement2).setForDataType(fs[0], fs[1], fs[2], fs[3]);
        } else {
            ((GlUniform)jsonElement2).set(fs);
        }
        this.uniforms.add((GlUniform)jsonElement2);
    }

    @Override
    public Program getVertexShader() {
        return this.vertexShader;
    }

    @Override
    public Program getFragmentShader() {
        return this.fragmentShader;
    }

    @Override
    public void attachReferencedShaders() {
        this.fragmentShader.attachTo(this);
        this.vertexShader.attachTo(this);
    }

    public VertexFormat getFormat() {
        return this.format;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int getProgramRef() {
        return this.programId;
    }

    static {
        activeShaderId = -1;
    }
}

