/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.SharedConstants;
import org.apache.commons.io.FilenameUtils;

public class FileNameUtil {
    private static final Pattern FILE_NAME_WITH_COUNT = Pattern.compile("(<name>.*) \\((<count>\\d*)\\)", 66);
    private static final int MAX_NAME_LENGTH = 255;
    private static final Pattern RESERVED_WINDOWS_NAMES = Pattern.compile(".*\\.|(?:COM|CLOCK\\$|CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?", 2);

    public static String getNextUniqueName(Path path, String name, String extension) throws IOException {
        for (char c : SharedConstants.INVALID_CHARS_LEVEL_NAME) {
            name = ((String)name).replace(c, '_');
        }
        if (RESERVED_WINDOWS_NAMES.matcher((CharSequence)(name = ((String)name).replaceAll("[./\"]", "_"))).matches()) {
            name = "_" + (String)name + "_";
        }
        Matcher matcher = FILE_NAME_WITH_COUNT.matcher((CharSequence)name);
        int i = 0;
        if (matcher.matches()) {
            name = matcher.group("name");
            i = Integer.parseInt(matcher.group("count"));
        }
        if (((String)name).length() > 255 - extension.length()) {
            name = ((String)name).substring(0, 255 - extension.length());
        }
        while (true) {
            Object c;
            Object string = name;
            if (i != 0) {
                c = " (" + i + ")";
                int j = 255 - ((String)c).length();
                if (((String)string).length() > j) {
                    string = ((String)string).substring(0, j);
                }
                string = (String)string + (String)c;
            }
            string = (String)string + extension;
            c = path.resolve((String)string);
            try {
                Path j = Files.createDirectory((Path)c, new FileAttribute[0]);
                Files.deleteIfExists(j);
                return path.relativize(j).toString();
            }
            catch (FileAlreadyExistsException j) {
                ++i;
                continue;
            }
            break;
        }
    }

    public static boolean isNormal(Path path) {
        Path path2 = path.normalize();
        return path2.equals(path);
    }

    public static boolean isAllowedName(Path path) {
        for (Path path2 : path) {
            if (!RESERVED_WINDOWS_NAMES.matcher(path2.toString()).matches()) continue;
            return false;
        }
        return true;
    }

    public static Path getResourcePath(Path path, String resourceName, String extension) {
        String string = resourceName + extension;
        Path path2 = Paths.get(string, new String[0]);
        if (path2.endsWith(extension)) {
            throw new InvalidPathException(string, "empty resource name");
        }
        return path.resolve(path2);
    }

    public static String getPosixFullPath(String path) {
        return FilenameUtils.getFullPath(path).replace(File.separator, "/");
    }

    public static String normalizeToPosix(String path) {
        return FilenameUtils.normalize(path).replace(File.separator, "/");
    }
}

