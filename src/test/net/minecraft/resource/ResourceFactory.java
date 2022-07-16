/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.resource;

import java.io.IOException;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

/**
 * Provides resource access.
 */
public interface ResourceFactory {
    /**
     * Finds and returns the corresponding resource for a resource's identifier.
     * 
     * <p>Starts by scanning each resource pack from highest priority to lowest. If no resource packs were found
     * to contain the requested entry, will throw a {@code FileNotFoundException}.
     * 
     * <p>The returned resource must be closed to avoid resource leaks.
     * 
     * @throws java.io.FileNotFoundException if the identified resource could not be found, or could not be loaded.
     * @throws IOException if the identified resource was found but a stream to it could not be opened.
     * 
     * @param id the resource identifier to search for
     */
    public Resource getResource(Identifier var1) throws IOException;
}

