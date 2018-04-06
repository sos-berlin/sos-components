package com.sos.commons.util;

import java.util.Map;

public class SOSString {

    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public boolean parseToBoolean(Object key) throws Exception {
        try {
            if (key != null) {
                return "true".equalsIgnoreCase(key.toString()) || "yes".equalsIgnoreCase(key.toString()) || "1".equals(key);
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new Exception(e.toString(), e);
        }
    }

    public String parseToString(Map<String, String> hash, String key) throws Exception {
        try {
            if (hash != null && hash.get(key) != null) {
                return hash.get(key);
            } else {
                return "";
            }
        } catch (Exception e) {
            throw new Exception(e.toString(), e);
        }

    }

    public String parseToString(Object key) throws Exception {
        try {
            if (key != null) {
                return key.toString();
            } else {
                return "";
            }
        } catch (Exception e) {
            throw new Exception(e.toString(), e);
        }
    }

    public String parseToString(java.util.Properties prop, String key) throws Exception {
        try {
            if (prop.get(key) != null) {
                return prop.get(key).toString();
            } else {
                return "";
            }
        } catch (Exception e) {
            throw new Exception(e.toString(), e);
        }
    }

}