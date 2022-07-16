/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.network;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.Address;
import net.minecraft.client.network.ServerAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface AddressResolver {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final AddressResolver DEFAULT = address -> {
        try {
            InetAddress inetAddress = InetAddress.getByName(address.getAddress());
            return Optional.of(Address.create(new InetSocketAddress(inetAddress, address.getPort())));
        }
        catch (UnknownHostException inetAddress) {
            LOGGER.debug("Couldn't resolve server {} address", (Object)address.getAddress(), (Object)inetAddress);
            return Optional.empty();
        }
    };

    public Optional<Address> resolve(ServerAddress var1);
}

