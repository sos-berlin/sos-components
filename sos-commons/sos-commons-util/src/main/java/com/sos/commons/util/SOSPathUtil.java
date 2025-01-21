package com.sos.commons.util;

/** Only String operations should used */
public class SOSPathUtil {

    public static String toUnixStylePath(String path) {
        return SOSString.isEmpty(path) ? null : path.replace('\\', '/');
    }

    public static String toWindowsStylePath(String path) {
        return SOSString.isEmpty(path) ? null : path.replace('/', '\\');
    }

    public static boolean isAbsolutePathWindowsStyle(String path) {
        String np = toUnixStylePath(path);
        if (np == null) {
            return false;
        }
        return isAbsolutePathWindowsStandardStyle(np) || isAbsolutePathWindowsEnvStyle(np) || isAbsolutePathWindowsOpenSSHPathStyle(np)
                || isAbsolutePathWindowsUNCStyle(np);
    }

    /** 1) Unix/Linux absolute paths (starting with /)<br/>
     * 2) Unix/Linux environment variables (e.g., $HOME)<br/>
     * 3) Unix/Linux home directory paths (starting with ~/) */
    public static boolean isAbsolutePathUnixStyle(String path) {
        String np = toUnixStylePath(path);
        if (np == null) {
            return false;
        }
        return np.startsWith("/") || np.startsWith("$") || np.startsWith("~/");
    }

    public static boolean isAbsolutePathFileSystemStyle(String path) {
        return isAbsolutePathUnixStyle(path) || isAbsolutePathWindowsStyle(path);
    }

    /** URI paths (e.g., file:/, http:/, sftp:/...) */
    public static boolean isAbsolutePathURIStyle(String path) {
        String np = toUnixStylePath(path);
        if (np == null) {
            return false;
        }
        return np.matches("^[a-zA-Z][a-zA-Z0-9+.-]*:/.+");
    }

    public static String getFileName(String path) {
        String np = toUnixStylePath(path);
        if (np == null) {
            return null;
        }
        int li = np.lastIndexOf('/');
        return (li >= 0) ? np.substring(li + 1) : np;
    }

    public static String getUnixStyleParentPath(String path) {
        String np = toUnixStylePath(path);
        if (np == null) {
            return null;
        }
        int li = np.lastIndexOf('/');
        return (li > 0) ? np.substring(0, li) : null;
    }

    public static String getWindowsStyleParentPath(String path) {
        String np = toWindowsStylePath(path);
        if (np == null) {
            return null;
        }
        int li = np.lastIndexOf('\\');
        return (li > 0) ? np.substring(0, li) : null;
    }

    /** Windows OpenSSH-style paths (/C:/...) */
    private static boolean isAbsolutePathWindowsOpenSSHPathStyle(String unixStylePath) {
        return unixStylePath.matches("^/[a-zA-Z]:/.*");
    }

    /** Standard Windows-style paths (C:/... */
    private static boolean isAbsolutePathWindowsStandardStyle(String unixStylePath) {
        return unixStylePath.matches("^[a-zA-Z]:/.*");
    }

    /** Windows environment variables (e.g., %USERPROFILE%) */
    private static boolean isAbsolutePathWindowsEnvStyle(String unixStylePath) {
        // \\p{L}: Matches any alphabetic character (from any language, including Chinese, Japanese, Cyrillic, etc.).
        // \\p{N}: Matches any numeric character.
        // _ and .
        return unixStylePath.matches("^%[\\p{L}\\p{N}_.-]+%.*");
    }

    /** Original: \\server\folder<br/>
     * unixStylePath: //server/folder */
    private static boolean isAbsolutePathWindowsUNCStyle(String unixStylePath) {
        return unixStylePath.matches("^//[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+(/[a-zA-Z0-9._-]+)*");
    }

}
