/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * A resource pack, providing resources to resource managers.
 * 
 * <p>They are single-use in each reload cycle of a reloadable resource manager.
 * {@link ResourcePackProfile} is a persistent version of the resource packs.
 */
public interface ResourcePack
extends AutoCloseable {
    public static final String METADATA_PATH_SUFFIX = ".mcmeta";
    public static final String PACK_METADATA_NAME = "pack.mcmeta";

    @Nullable
    public InputStream openRoot(String var1) throws IOException;

    public InputStream open(ResourceType var1, Identifier var2) throws IOException;

    public Collection<Identifier> findResources(ResourceType var1, String var2, String var3, int var4, Predicate<String> var5);

    public boolean contains(ResourceType var1, Identifier var2);

    public Set<String> getNamespaces(ResourceType var1);

    @Nullable
    public <T> T parseMetadata(ResourceMetadataReader<T> var1) throws IOException;

    public String getName();

    @Override
    public void close();
}

