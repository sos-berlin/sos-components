package com.sos.commons.util;

/** Only String operations should used */
public class SOSPathUtil {

    public static String toUnixStylePath(String val) {
        return val.replace('\\', '/');
    }

    public static boolean isAbsolutePathWindowsStyle(String path) {
        if (SOSString.isEmpty(path)) {
            return false;
        }
        String np = toUnixStylePath(path);
        return isAbsolutePathWindowsStandardStyle(np) || isAbsolutePathWindowsEnvStyle(np) || isAbsolutePathWindowsOpenSSHPathStyle(np);
    }

    /** 1) Unix/Linux absolute paths (starting with /)<br/>
     * 2) Unix/Linux environment variables (e.g., $HOME)<br/>
     * 3) Unix/Linux home directory paths (starting with ~/) */
    public static boolean isAbsolutePathUnixStyle(String path) {
        if (SOSString.isEmpty(path)) {
            return false;
        }
        String np = toUnixStylePath(path);
        return np.startsWith("/") || np.startsWith("$") || np.startsWith("~/");
    }

    public static boolean isAbsolutePathFileSystemStyle(String path) {
        return isAbsolutePathUnixStyle(path) || isAbsolutePathWindowsStyle(path);
    }

    /** URI paths (e.g., file:/, http:/, sftp:/...) */
    public static boolean isAbsolutePathURIStyle(String path) {
        if (SOSString.isEmpty(path)) {
            return false;
        }
        String np = toUnixStylePath(path);
        return np.matches("^[a-zA-Z][a-zA-Z0-9+.-]*:/.+");
    }

    public static String getFileName(String path) {
        if (path == null) {
            return null;
        }
        String np = path.replace('\\', '/');
        int li = np.lastIndexOf('/');
        return (li >= 0) ? np.substring(li + 1) : np;
    }

    public static String getUnixStyleParentPath(String path) {
        if (SOSString.isEmpty(path)) {
            return null;
        }
        String np = path.replace('\\', '/');
        int li = np.lastIndexOf('/');
        return (li > 0) ? np.substring(0, li) : null;
    }

    /** Windows OpenSSH-style paths (/C:/...) */
    private static boolean isAbsolutePathWindowsOpenSSHPathStyle(String normalizedPath) {
        return normalizedPath.matches("^/[a-zA-Z]:/.*");
    }

    /** Standard Windows-style paths (C:/... */
    private static boolean isAbsolutePathWindowsStandardStyle(String normalizedPath) {
        return normalizedPath.matches("^[a-zA-Z]:/.*");
    }

    /** Windows environment variables (e.g., %USERPROFILE%) */
    private static boolean isAbsolutePathWindowsEnvStyle(String normalizedPath) {
        return normalizedPath.matches("^%[a-zA-Z_0-9.-]+%.*");
    }
}
