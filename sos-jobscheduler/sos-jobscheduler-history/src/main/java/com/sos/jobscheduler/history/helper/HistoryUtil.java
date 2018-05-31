package com.sos.jobscheduler.history.helper;

import java.nio.charset.StandardCharsets;

import com.google.common.hash.Hashing;

public class HistoryUtil {

    public static String getFolderFromPath(String path) {
        int li = path.lastIndexOf("/");
        if (li == 0) {
            return path.substring(0, 1);
        }
        return li > -1 ? path.substring(0, li) : path;
    }

    public static String getBasenameFromPath(String path) {
        int li = path.lastIndexOf("/");
        return li > -1 ? path.substring(li + 1) : path;
    }

    public static String hashString(String val) {
        return Hashing.sha256().hashString(val, StandardCharsets.UTF_8).toString();
    }
}
