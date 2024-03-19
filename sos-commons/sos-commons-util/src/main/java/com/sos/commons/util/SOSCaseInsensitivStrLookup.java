package com.sos.commons.util;

import java.util.Map;

import org.apache.commons.text.lookup.StringLookup;

public class SOSCaseInsensitivStrLookup<V> implements StringLookup {

    private final Map<String, V> map;

    SOSCaseInsensitivStrLookup(final Map<String, V> map) {
        this.map = map;
    }

    @Override
    public String lookup(final String key) {
        String lowercaseKey = key.toLowerCase();
        if (map == null) {
            return null;
        }
        final V obj = map.get(lowercaseKey);
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }
}