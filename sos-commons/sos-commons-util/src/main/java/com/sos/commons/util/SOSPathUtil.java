package com.sos.commons.util;

/** Only String operations should be used, without nio Path(see REGEXP_ABSOLUTE_WINDOWS_OPENSSH_PATH) */
public class SOSPathUtil {

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

    public static String toUnixPath(String path) {
        return SOSString.isEmpty(path) ? null : path.replace('\\', '/');
    }

    public static String toWindowsPath(String path) {
        return SOSString.isEmpty(path) ? null : path.replace('/', '\\');
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
        String np = toUnixPath(path);
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

    public static String getName(String path) {
        String np = toUnixPath(path);
        if (np == null) {
            return null;
        }
        int li = np.lastIndexOf('/');
        return (li >= 0) ? np.substring(li + 1) : np;
    }

    public static String getParentPath(String path) {
        if (path == null) {
            return null;
        }
        if (path.contains("\\")) {
            return getWindowsStyleParentPath(path);
        }
        return getUnixStyleParentPath(path);
    }

    public static String getUnixStyleParentPath(String path) {
        String np = toUnixPath(path);
        if (np == null) {
            return null;
        }
        int li = np.lastIndexOf('/');
        return (li > 0) ? np.substring(0, li) : null;
    }

    public static String getWindowsStyleParentPath(String path) {
        String np = toWindowsPath(path);
        if (np == null) {
            return null;
        }
        int li = np.lastIndexOf('\\');
        return (li > 0) ? np.substring(0, li) : null;
    }

    public static String getUnixStyleDirectoryWithoutTrailingSeparator(String path) {
        String p = SOSPathUtil.toUnixPath(path);
        if (p == null) {
            return null;
        }
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    public static String getUnixStyleDirectoryWithTrailingSeparator(String path) {
        String p = SOSPathUtil.toUnixPath(path);
        if (p == null) {
            return null;
        }
        return path.endsWith("/") ? path : path + "/";
    }

}
