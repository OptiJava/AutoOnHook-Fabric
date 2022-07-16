/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.realms.dto;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.ValueObject;
import net.minecraft.client.realms.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class UploadInfo
extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String HTTP_PROTOCOL = "http://";
    private static final int PORT = 8080;
    private static final Pattern PROTOCOL_PATTERN = Pattern.compile("^[a-zA-Z][-a-zA-Z0-9+.]+:");
    private final boolean worldClosed;
    @Nullable
    private final String token;
    private final URI uploadEndpoint;

    private UploadInfo(boolean worldClosed, @Nullable String token, URI uploadEndpoint) {
        this.worldClosed = worldClosed;
        this.token = token;
        this.uploadEndpoint = uploadEndpoint;
    }

    @Nullable
    public static UploadInfo parse(String json) {
        try {
            int i;
            URI uRI;
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();
            String string = JsonUtils.getStringOr("uploadEndpoint", jsonObject, null);
            if (string != null && (uRI = UploadInfo.getUrl(string, i = JsonUtils.getIntOr("port", jsonObject, -1))) != null) {
                boolean bl = JsonUtils.getBooleanOr("worldClosed", jsonObject, false);
                String string2 = JsonUtils.getStringOr("token", jsonObject, null);
                return new UploadInfo(bl, string2, uRI);
            }
        }
        catch (Exception jsonParser) {
            LOGGER.error("Could not parse UploadInfo: {}", (Object)jsonParser.getMessage());
        }
        return null;
    }

    @Nullable
    @VisibleForTesting
    public static URI getUrl(String url, int port) {
        Matcher matcher = PROTOCOL_PATTERN.matcher(url);
        String string = UploadInfo.getUrlWithProtocol(url, matcher);
        try {
            URI uRI = new URI(string);
            int i = UploadInfo.getPort(port, uRI.getPort());
            if (i != uRI.getPort()) {
                return new URI(uRI.getScheme(), uRI.getUserInfo(), uRI.getHost(), i, uRI.getPath(), uRI.getQuery(), uRI.getFragment());
            }
            return uRI;
        }
        catch (URISyntaxException uRI) {
            LOGGER.warn("Failed to parse URI {}", (Object)string, (Object)uRI);
            return null;
        }
    }

    private static int getPort(int port, int urlPort) {
        if (port != -1) {
            return port;
        }
        if (urlPort != -1) {
            return urlPort;
        }
        return 8080;
    }

    private static String getUrlWithProtocol(String url, Matcher matcher) {
        if (matcher.find()) {
            return url;
        }
        return HTTP_PROTOCOL + url;
    }

    public static String createRequestContent(@Nullable String token) {
        JsonObject jsonObject = new JsonObject();
        if (token != null) {
            jsonObject.addProperty("token", token);
        }
        return jsonObject.toString();
    }

    @Nullable
    public String getToken() {
        return this.token;
    }

    public URI getUploadEndpoint() {
        return this.uploadEndpoint;
    }

    public boolean isWorldClosed() {
        return this.worldClosed;
    }
}

