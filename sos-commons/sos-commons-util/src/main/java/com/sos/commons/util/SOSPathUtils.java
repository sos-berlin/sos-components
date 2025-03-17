package com.sos.commons.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** Only String operations should be used, without nio Path(see REGEXP_ABSOLUTE_WINDOWS_OPENSSH_PATH) */
public class SOSPathUtils {

    public static final String PATH_SEPARATOR_UNIX = "/";
    public static final String PATH_SEPARATOR_WINDOWS = "\\";

    /** ----------------- */

    /** 1) URI */
    /** URI paths (e.g., file:/, http:/, sftp:/...) */
    private static final String REGEXP_ABSOLUTE_URI_PATH = "^[a-zA-Z][a-zA-Z0-9+.-]*:/.+";

    /** 2) UNIX */
    /** - Unix absolute paths (starting with /)<br/>
     * - Unix environment variables (e.g., $HOME)<br/>
     * - Unix home directory paths (starting with ~/) */
    private static final String REGEXP_ABSOLUTE_UNIX_PATH = "^(\\/|\\$|~\\/).*";

    /** 3) WINDOWS */
    /** C:/tmp, C:\\tmp */
    private static final String REGEXP_ABSOLUTE_WINDOWS_STANDARD_PATH = "^[a-zA-Z]:[\\\\/].*";
    /** Windows environment variables (e.g., %USERPROFILE%)<br/>
     * - \\p{L}: Matches any alphabetic character (from any language, including Chinese, Japanese, Cyrillic, etc.)<br/>
     * - \\p{N}: Matches any numeric character<br/>
     * - _ and . */
    private static final String REGEXP_ABSOLUTE_WINDOWS_ENV_PATH = "^%[\\p{L}\\p{N}_.-]+%.*";
    /** Windows OpenSSH-Style paths: <br/>
     * - /C:/MyPath...<br/>
     * -- \\C:\MyPath is technically possible but very atypical - ignore this<br/>
     * -- /C:\\MyPath is technically possible but very atypical - ignore this */
    private static final String REGEXP_ABSOLUTE_WINDOWS_OPENSSH_PATH = "^/[a-zA-Z]:/.*";
    /** \\server\folder<br/>
     * -- //server/folder is technically possible but very atypical - ignore this */
    private static final String REGEXP_ABSOLUTE_WINDOWS_UNC_PATH = "^\\\\[a-zA-Z0-9._-]+\\\\[a-zA-Z0-9._-]+(\\\\[a-zA-Z0-9._-]+)*$";
    /** Combined Windows Path RegExp */
    private static final String REGEX_ABSOLUTE_WINDOWS_PATH = REGEXP_ABSOLUTE_WINDOWS_STANDARD_PATH + "|" + REGEXP_ABSOLUTE_WINDOWS_ENV_PATH + "|"
            + REGEXP_ABSOLUTE_WINDOWS_OPENSSH_PATH + "|" + REGEXP_ABSOLUTE_WINDOWS_UNC_PATH;

    /** 4) FILESYSTEM */
    /** Unix and Windows */
    private static final String REGEX_ABSOLUTE_FILESYSTEM_PATH = REGEXP_ABSOLUTE_UNIX_PATH + "|" + REGEX_ABSOLUTE_WINDOWS_PATH;

    public static String toUnixStyle(String path) {
        return SOSString.isEmpty(path) ? null : path.replace(PATH_SEPARATOR_WINDOWS, PATH_SEPARATOR_UNIX);
    }

    public static String toWindowsStyle(String path) {
        return SOSString.isEmpty(path) ? null : path.replace(PATH_SEPARATOR_UNIX, PATH_SEPARATOR_WINDOWS);
    }

    public static boolean isAbsoluteWindowsPath(String path) {
        if (SOSString.isEmpty(path)) {
            return false;
        }
        return path.matches(REGEX_ABSOLUTE_WINDOWS_PATH);
    }

    public static boolean isAbsoluteUnixPath(String path) {
        if (SOSString.isEmpty(path)) {
            return false;
        }
        return path.matches(REGEXP_ABSOLUTE_UNIX_PATH);
    }

    public static boolean isAbsoluteFileSystemPath(String path) {
        if (SOSString.isEmpty(path)) {
            return false;
        }
        return path.matches(REGEX_ABSOLUTE_FILESYSTEM_PATH);
    }

    public static boolean isAbsoluteURIPath(String path) {
        String np = toUnixStyle(path);
        if (np == null) {
            return false;
        }
        return np.matches(REGEXP_ABSOLUTE_URI_PATH);
    }

    public static boolean isAbsoluteWindowsOpenSSHPath(String path) {
        if (SOSString.isEmpty(path)) {
            return false;
        }
        return path.matches(REGEXP_ABSOLUTE_WINDOWS_OPENSSH_PATH);
    }

    public static boolean isAbsoluteWindowsStandardPath(String path) {
        if (SOSString.isEmpty(path)) {
            return false;
        }
        return path.matches(REGEXP_ABSOLUTE_WINDOWS_STANDARD_PATH);
    }

    public static boolean isAbsoluteWindowsEnvPath(String path) {
        if (SOSString.isEmpty(path)) {
            return false;
        }
        return path.matches(REGEXP_ABSOLUTE_WINDOWS_ENV_PATH);
    }

    public static boolean isAbsoluteWindowsUNCPath(String path) {
        if (SOSString.isEmpty(path)) {
            return false;
        }
        return path.matches(REGEXP_ABSOLUTE_WINDOWS_UNC_PATH);
    }

    /** Returns:<br/>
     * path=null: null<br/>
     * path="/": null<br/>
     * 
     * path="C:": null<br/>
     * path="C://": null<br/>
     * path="C://xyz": xyz<br/>
     * 
     * @param path
     * @return */
    public static String getName(String path) {
        String np = toUnixStyle(path);
        if (np == null) {
            return null;
        }

        int li = np.lastIndexOf(PATH_SEPARATOR_UNIX);
        if (li == 0 && np.equals(PATH_SEPARATOR_UNIX)) {
            return null;
        }
        String tmp = (li >= 0) ? np.substring(li + 1) : np;
        return tmp.isEmpty() || SOSString.trimEnd(tmp, PATH_SEPARATOR_UNIX).endsWith(":") ? null : tmp;
    }

    /** Returns parent path without trailing separator
     * 
     * @param path
     * @return */
    public static String getParentPath(String path) {
        return getParentPath(path, null);
    }

    /** Returns the parent path, or null if this path does not have a parent<br/>
     * The trailing path separator is only added if the parent path is a first(root)-level path,e.g.: / or C:/<br/>
     * 
     * path=null: null<br/>
     * path="/": null<br>
     * path="/xyz": "/"(pathSeparator)<br/>
     * 
     * path="C:": null<br/>
     * path="C://": null<br/>
     * path="C:/xyz": "C:/"<br/>
     * 
     * @param path
     * @param pathSeparator
     * @return */
    public static String getParentPath(final String path, final String pathSeparator) {
        if (path == null) {
            return null;
        }
        String name = getName(path);
        if (name == null) {
            return null;
        }
        String ps = getPathSeparator(path, pathSeparator);
        String tmp = SOSString.trimEnd(path.substring(0, path.lastIndexOf(name)), ps);
        if (tmp.isEmpty()) {
            return ps;
        }
        return tmp.endsWith(":") ? (tmp + ps) : tmp;
    }

    /** Appends an additional path to the base path.<br/>
     * Ignores whether additionalPath is absolute or not */
    public static String appendPath(final String basePath, final String additionalPath) {
        return appendPath(basePath, additionalPath, null);
    }

    /** Appends an additional path to the base path.<br/>
     * Ignores whether additionalPath is absolute or not
     * 
     * @param basePath
     * @param additionalPath
     * @param pathSeparator
     * @return */
    public static String appendPath(final String basePath, final String additionalPath, final String pathSeparator) {
        if (SOSString.isEmpty(basePath)) {
            return additionalPath;
        }
        if (SOSString.isEmpty(additionalPath)) {
            return basePath;
        }
        String ps1 = getPathSeparator(basePath, pathSeparator);
        String ps2 = getPathSeparator(additionalPath, pathSeparator);
        return SOSString.trimEnd(basePath, ps1) + ps2 + SOSString.trimStart(additionalPath, ps2);
    }

    /** Returns parent path without trailing separator
     * 
     * @param path
     * @return */
    public static String getUnixStyleParentPath(String path) {
        return getParentPath(toUnixStyle(path), PATH_SEPARATOR_UNIX);
    }

    /** Returns parent path without trailing separator
     * 
     * @param path
     * @return */
    public static String getWindowsStyleParentPath(String path) {
        return getParentPath(toWindowsStyle(path), PATH_SEPARATOR_WINDOWS);
    }

    public static String getUnixStyleDirectoryWithoutTrailingSeparator(String path) {
        return getDirectoryWithoutTrailingSeparator(toUnixStyle(path), PATH_SEPARATOR_UNIX);
    }

    public static String getWindowsStyleDirectoryWithoutTrailingSeparator(String path) {
        return getDirectoryWithoutTrailingSeparator(toWindowsStyle(path), PATH_SEPARATOR_WINDOWS);
    }

    public static String getUnixStyleDirectoryWithTrailingSeparator(String path) {
        return getDirectoryWithTrailingSeparator(toUnixStyle(path), PATH_SEPARATOR_UNIX);
    }

    public static String getWindowsStyleDirectoryWithTrailingSeparator(String path) {
        return getDirectoryWithTrailingSeparator(toWindowsStyle(path), PATH_SEPARATOR_WINDOWS);
    }

    /** Selects top-level paths from a collection of path-like elements.<br />
     * A path is considered "top-level" if it is not a sub-path of any other path in the collection.<br/>
     * For example, given the paths: /tmp/x/1, /tmp/x/1/1, /tmp/x/2, /var/1<br/>
     * The top-level paths are: /tmp/x and /var/1<br/>
     * 
     * @param paths the collection of directory paths as strings or objects that return a path on "toString"(e.g. Path)
     * @return a sorted set of top-level paths as directory path without a trailing separator */
    public static Set<String> selectTopLevelPaths(final Collection<?> paths) {
        return selectTopLevelPaths(paths, null);
    }

    /** Selects top-level paths from a collection of path-like elements.<br />
     * A path is considered "top-level" if it is not a sub-path of any other path in the collection.<br/>
     * For example, given the paths: /tmp/x/1, /tmp/x/1/1, /tmp/x/2, /var/1<br/>
     * The top-level paths are: /tmp/x and /var/1<br/>
     * 
     * @param paths the collection of directory paths as strings or objects that return a path on "toString"(e.g. Path)
     * @pathSeparator \ or / path separator
     * @return a sorted set of top-level paths as directory path without a trailing separator */
    public static Set<String> selectTopLevelPaths(final Collection<?> paths, final String pathSeparator) {
        if (SOSCollection.isEmpty(paths)) {
            return new TreeSet<>();
        }

        final String ps = getPathSeparator(paths, pathSeparator);

        Set<String> result = new HashSet<>();
        Set<String> analyzed = new HashSet<>();
        // removes: duplicate entries and trailing separator
        Set<String> normalized = paths.stream().map(e -> getDirectoryWithoutTrailingSeparator(e.toString(), ps)).collect(Collectors.toSet());

        // TreeSet reduces the number of iterations
        normalized = new TreeSet<>(normalized);
        m: for (String path : normalized) {
            if (analyzed.stream().anyMatch(path::startsWith)) {
                analyzed.add(path);
                continue m;
            }

            sub: for (String pathSub : normalized) {
                if (pathSub.equals(path) || analyzed.contains(pathSub)) {
                    continue sub;
                }
                String pathSubStripped = SOSString.removePrefix(pathSub, path);
                if (pathSub.equals(pathSubStripped)) {
                    String p1 = getParentPath(path, ps);
                    String p2 = getParentPath(pathSub, ps);
                    if (p1.equals(p2)) {
                        result.add(p1);
                        analyzed.add(path);
                        analyzed.add(pathSub);
                    } else {
                        result.add(pathSub);
                    }
                } else {
                    analyzed.add(pathSub);
                }
            }
            // result = result.stream().filter(e -> !analyzed.contains(e)).collect(Collectors.toSet());
            result = result.stream().filter(e -> !analyzed.stream().anyMatch(e::startsWith)).collect(Collectors.toSet());
        }
        return new TreeSet<>(result);
    }

    /** Selects the deepest level paths from a collection of path-like elements.<br/>
     * A path is considered "deepest level" if no other path in the collection starts with it.<br/>
     * For example, given the paths: /tmp/x, /tmp/x/1, /tmp/x/1/1, /tmp/x/2, /var/1<br/>
     * The deepest level paths are: /tmp/x/1/1, /tmp/x/2 and /var/1.
     * 
     * @param paths the collection of directory paths as strings or objects that return a path on "toString"(e.g. Path)
     * @return a sorted set of deepest level paths as directory path without a trailing separator */
    public static Set<String> selectDeepestLevelPaths(Collection<?> paths) {
        return selectDeepestLevelPaths(paths, null);
    }

    /** Selects the deepest level paths from a collection of path-like elements.<br/>
     * A path is considered "deepest level" if no other path in the collection starts with it.<br/>
     * For example, given the paths: /tmp/x, /tmp/x/1, /tmp/x/1/1, /tmp/x/2, /var/1<br/>
     * The deepest level paths are: /tmp/x/1/1, /tmp/x/2 and /var/1.
     * 
     * @param paths the collection of directory paths as strings or objects that return a path on "toString"(e.g. Path)
     * @pathSeparator \ or / path separator
     * @return a sorted set of deepest level paths as directory path without a trailing separator */
    public static Set<String> selectDeepestLevelPaths(Collection<?> paths, String pathSeparator) {
        if (SOSCollection.isEmpty(paths)) {
            return new TreeSet<>();
        }
        final String ps = getPathSeparator(paths, pathSeparator);
        // removes: duplicate entries and trailing separator
        Set<String> normalized = paths.stream().map(e -> getDirectoryWithoutTrailingSeparator(e.toString(), ps)).collect(Collectors.toSet());
        return normalized.stream().map(Object::toString).sorted(Comparator.comparingInt(String::length).reversed()).filter(path -> normalized.stream()
                .map(Object::toString).noneMatch(other -> other.startsWith(path + ps) && !other.equals(path))).collect(Collectors.toCollection(
                        TreeSet::new));
    }

    public static boolean isUnixStylePathSeparator(String pathSeparator) {
        return PATH_SEPARATOR_UNIX.equals(pathSeparator);
    }

    public static String getPathSeparator(String path) {
        if (SOSString.isEmpty(path)) {
            return PATH_SEPARATOR_UNIX;
        }
        if (path.contains(PATH_SEPARATOR_WINDOWS)) {
            return PATH_SEPARATOR_WINDOWS;
        }
        // default: e.g. if path without any path separator(file name )...
        return PATH_SEPARATOR_UNIX;
    }

    /** Examples:<br/>
     * - 'folder/./file.txt' -> 'folder/file.txt'<br/>
     * - '/home/user/./docs/../a' ->'/home/user/a'<br/>
     * - 'a/b/./c/../d' -> 'a/b/d'<br/>
     * - '/../' -> '/'<br/>
     * - 'a/b/c/../../../' -> ''<br/>
     * - '/a/../b/./c/.' ->'/b/c'<br/>
     * - 'folder/file.txt'->'folder/file.txt'<br/>
     * 
     * @param path
     * @return */
    public static String normalize(String path) {
        return normalize(path, null);
    }

    public static String normalize(String path, String pathSeparator) {
        if (SOSString.isEmpty(path) || (!path.contains("..") && !path.contains("."))) {
            return path;
        }
        final String ps = getPathSeparator(path, pathSeparator);
        // Windows/URI
        if (path.contains(":")) {
            if (isAbsoluteWindowsOpenSSHPath(path)) {
                String n = ps + Path.of(path.substring(1)).normalize().toString();
                return toUnixStyle(n);
            } else if (isAbsoluteURIPath(path)) {
                try {
                    return new URI(null, path, null).normalize().toString();
                } catch (URISyntaxException e) {
                    return path;
                }
            } else {
                String n = Path.of(path).normalize().toString();
                return isUnixStylePathSeparator(ps) ? toUnixStyle(n) : toWindowsStyle(n);
            }
        }
        String n = Path.of(path).normalize().toString();
        // Unix and UNC(\\server\share)
        return isUnixStylePathSeparator(ps) ? toUnixStyle(n) : toWindowsStyle(n);
    }

    private static String getPathSeparator(String path, String pathSeparator) {
        if (pathSeparator != null) {
            return pathSeparator;
        }
        return path.contains(PATH_SEPARATOR_UNIX) ? PATH_SEPARATOR_UNIX : PATH_SEPARATOR_WINDOWS;
    }

    private static String getDirectoryWithoutTrailingSeparator(String path, String pathSeparator) {
        if (path == null) {
            return null;
        }
        return SOSString.trimEnd(path, pathSeparator);
    }

    private static String getDirectoryWithTrailingSeparator(String path, String pathSeparator) {
        if (path == null) {
            return null;
        }
        return path.endsWith(pathSeparator) ? path : path + pathSeparator;
    }

    private static String getPathSeparator(Collection<?> paths, String pathSeparator) {
        if (pathSeparator == null) {
            return getPathSeparator(paths.iterator().next().toString());
        }
        return pathSeparator;
    }

}
