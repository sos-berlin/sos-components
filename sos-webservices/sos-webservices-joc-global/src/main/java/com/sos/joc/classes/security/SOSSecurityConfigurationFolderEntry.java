package com.sos.joc.classes.security;

import java.util.ArrayList;
import java.util.List;

import com.sos.joc.model.security.permissions.SecurityConfigurationFolder;

public class SOSSecurityConfigurationFolderEntry {

    private String controllerId;
    private String role;
    private List<String> listOWriteFolders;


    public SOSSecurityConfigurationFolderEntry(String controllerId, String role) {
        listOWriteFolders = new ArrayList<>();
        this.role = role;
        this.controllerId = controllerId;
    }

    public String getFolderKey() {
        if (!controllerId.isEmpty()) {
            return controllerId + "|" + role;
        } else {
            return role;
        }
    }

    public void addFolder(SecurityConfigurationFolder folder) {
        if (folder.getPath() != null && !folder.getPath().isEmpty()) {
            String f = folder.getPath();
            if (folder.getRecursive() == Boolean.TRUE) {
                f += "/*";
            }
            listOWriteFolders.add(f.replaceAll("//+", "/"));
        }
    }

    public String getController() {
        return controllerId;
    }

    public String getIniWriteString() {
        String delimiter = ", \\" + System.lineSeparator() + "                                                                                           ".substring(0, getFolderKey().length()+3);
        return String.join(delimiter, listOWriteFolders) + System.lineSeparator();
    }

}
