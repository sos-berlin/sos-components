package com.sos.joc.classes.security;

import java.util.ArrayList;
import java.util.List;

public class SOSSecurityConfigurationRoleEntry {

    private String role;
    private List<String> listOWritePermissions;

    public SOSSecurityConfigurationRoleEntry(String role) {
        listOWritePermissions = new ArrayList<String>();
        this.role = role;
    }

    public void addPermission(String permission) {
        listOWritePermissions.add(permission);
    }

    public String getIniWriteString(){
        String delimiter = ", \\" + System.lineSeparator() + "                                                                    ".substring(0, role.length()+3);
        return String.join(delimiter, listOWritePermissions) + System.lineSeparator();
    }
}
