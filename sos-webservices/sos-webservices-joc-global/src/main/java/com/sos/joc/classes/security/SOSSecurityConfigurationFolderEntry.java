package com.sos.joc.classes.security;

import java.util.HashSet;
import java.util.Set;

import com.sos.joc.model.common.Folder;

public class SOSSecurityConfigurationFolderEntry {

    private String controllerId;
    private String role;
    private Set<String> listOWriteFolders;


    public SOSSecurityConfigurationFolderEntry(String controllerId, String role) {
        listOWriteFolders = new HashSet<>();
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

    public void addFolder(Folder folder) {
        if (folder.getFolder() != null && !folder.getFolder().isEmpty()) {
            String f = folder.getFolder();
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
