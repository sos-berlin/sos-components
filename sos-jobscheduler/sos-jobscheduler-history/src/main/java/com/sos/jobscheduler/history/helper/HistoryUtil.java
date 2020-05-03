package com.sos.jobscheduler.history.helper;

import java.nio.charset.StandardCharsets;

import com.google.common.hash.Hashing;
import com.sos.commons.util.SOSParameterSubstitutor;

public class HistoryUtil {

    public static final String NEW_LINE = "\r\n";

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

    /** An variable is referenced as "${VAR}" */
    public static String resolveVars(String cmd) {
        SOSParameterSubstitutor ps = new SOSParameterSubstitutor();
        String val = ps.replaceEnvVars(cmd);
        return ps.replaceSystemProperties(val);
    }

    public static String nl2sp(String value) {
        return value.replaceAll("\\r\\n|\\r|\\n", " ");
    }

}
