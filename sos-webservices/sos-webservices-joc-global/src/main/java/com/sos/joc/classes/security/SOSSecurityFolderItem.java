package com.sos.joc.classes.security;

import com.sos.joc.model.security.permissions.SecurityConfigurationFolder;

public class SOSSecurityFolderItem {

    private boolean recursive;
    private String folder;
    private String normalizedFolder;

    public SOSSecurityFolderItem(String folder) {
        this.folder = folder.trim();
        this.normalizedFolder = normalizeFolder();
    }
    
    public SOSSecurityFolderItem(SecurityConfigurationFolder securityConfigurationFolder) {
        this.folder = securityConfigurationFolder.getPath();
        this.normalizedFolder = this.normalizeFolder();
        this.recursive = securityConfigurationFolder.getRecursive();
    }

    public boolean isRecursive() {
        return recursive;
    }

    public String getFolder() {
        return folder;
    }

    public String getNormalizedFolder() {
        return normalizedFolder;
    }

    private String normalizeFolder() {
        if (folder.endsWith("/*")) {
            folder = ("/" + folder.trim().replaceFirst("/\\*$", "")).replaceAll("//+", "/");
            recursive = true;
        } else {
            folder = ("/" + folder.trim().replaceFirst("/$", "")).replaceAll("//+", "/");
        }
        return folder;
    }

    public String getIniValue() {
        String s = folder;
        if (isRecursive()) {
            s = s + "/*";
        }
        return s;

    }

}
