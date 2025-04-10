package com.sos.commons.util.beans;

import java.util.LinkedHashMap;
import java.util.Map;

import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;

public class SOSEnv {

    private Map<String, String> localEnvs;
    private Map<String, String> globalEnvs;

    public SOSEnv() {

    }

    public SOSEnv(Map<String, String> localEnvs) {
        this(localEnvs, null);
    }

    public SOSEnv(Map<String, String> localEnvs, Map<String, String> globalEnvs) {
        this.localEnvs = localEnvs;
        this.globalEnvs = globalEnvs;
    }

    public Map<String, String> getLocalEnvs() {
        return localEnvs;
    }

    public void setLocalEnvs(Map<String, String> val) {
        localEnvs = val;
    }

    public void putLocalEnv(String name, String value) {
        if (localEnvs == null) {
            localEnvs = new LinkedHashMap<>();
        }
        localEnvs.put(name, value);
    }

    public Map<String, String> getGlobalEnvs() {
        return globalEnvs;
    }

    public void setGlobalEnvs(Map<String, String> val) {
        globalEnvs = val;
    }

    public void putGlobalEnv(String name, String value) {
        if (globalEnvs == null) {
            globalEnvs = new LinkedHashMap<>();
        }
        globalEnvs.put(name, value);
    }

    public static String newLine2Space(String value) {
        return value.replaceAll("\\r\\n|\\r|\\n", " ");
    }

    public static String escapeValue(String val) {
        return escapeValue(val, SOSShell.IS_WINDOWS);
    }

    /** The entire escaped value should be an original string: without interpretation/replacement by the shell */
    public static String escapeValue(String val, boolean isWindows) {
        if (SOSString.isEmpty(val)) {
            return val;
        }
        val = SOSString.remove4ByteCharacters(val).trim();
        return isWindows ? escapeValue4Windows(val) : escapeValue4Unix(val);
    }

    /** The entire escaped value should be an original string: without interpretation/replacement by the shell
     *
     * Escape with ^ to prevent the shell from interpreting value:<br/>
     * < <br/>
     * > <br/>
     * & <br/>
     * % <br/>
     * ^ <br/>
     * | <br/>
     * " <br/>
     * ' <br/>
     * ! <br/>
     * ( <br/>
     * ) <br/>
     * [ <br/>
     * ] <br/>
     * 
     * @param val
     * @return escaped value */
    private static String escapeValue4Windows(String val) {
        // return s.replaceAll("<", "^<").replaceAll(">", "^>").replaceAll("%", "^%").replaceAll("&", "^&");
        return val.replaceAll("([<>&%^|\"'!()\\[\\]])", "^$1");
    }

    /** The entire value should be an original string: without interpretation/replacement by the shell
     * 
     * Escape with \ to prevent the shell from interpreting value:<br/>
     * \ <br/>
     * " <br/>
     * < <br/>
     * > <br/>
     * & <br/>
     * % <br/>
     * ' <br/>
     * ; <br/>
     * ` <br/>
     * $ <br/>
     * ( <br/>
     * ) <br/>
     * { <br/>
     * } <br/>
     * [ <br/>
     * ] <br/>
     * | <br/>
     * ^ <br/>
     * # <br/>
     * ~ <br/>
     * ? <br/>
     * * <br/>
     * 
     * @param val
     * @return escaped value */
    private static String escapeValue4Unix(String val) {
        // return s.replaceAll("\"", "\\\\\"").replaceAll("<", "\\\\<").replaceAll(">", "\\\\>").replaceAll("%", "\\\\%").replaceAll("&", "\\\\&")
        // .replaceAll(";", "\\\\;").replaceAll("'", "\\\\'");
        return val.replaceAll("([\"<>&%';`$(){}\\[\\]|^#~?*])", "\\\\$1");
    }
}
