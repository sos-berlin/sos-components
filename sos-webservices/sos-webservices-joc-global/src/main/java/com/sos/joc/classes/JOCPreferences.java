package com.sos.joc.classes;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class JOCPreferences {
    private Preferences prefs;
    private String userName;

    public JOCPreferences(String userName) {
        prefs = Preferences.userNodeForPackage(this.getClass());
        this.userName = userName;
        Logger l = Logger.getLogger("java.util.prefs");
        l.setLevel(Level.OFF);
    }

    public void put(String key, String value){
        try {
            prefs.node(WebserviceConstants.JOC_COCKPIT).put(getKey(key),value);
            prefs.node(WebserviceConstants.JOC_COCKPIT).flush();
        } catch (Exception e) {
        }
    }
    public String get(String key, String defaultValue){
        return prefs.node(WebserviceConstants.JOC_COCKPIT).get(getKey(key),defaultValue);
    }
    
    private String getKey(String key){
        return key + ":" + userName;
    }
}
