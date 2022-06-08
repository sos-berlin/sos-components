package com.sos.joc.classes.security;

import java.util.ArrayList;
import java.util.List;

import com.sos.joc.model.security.configuration.SecurityConfigurationAccount;

public class SOSSecurityConfigurationAccountEntry {

    private String[] listOfRolesAndPassword;
    SecurityConfigurationAccount securityConfigurationAccount;

    public SOSSecurityConfigurationAccountEntry(String entry) {
        super();
        listOfRolesAndPassword = entry.split(",");
    }

    public String getPassword() {
        if (listOfRolesAndPassword.length > 0) {
            return listOfRolesAndPassword[0];
        } else {
            return "";
        }
    }

    public List<String> getRoles() {
        List<String> listOfRoles = new ArrayList<String>();
        for (int i = 1; i < listOfRolesAndPassword.length; i++) {
            listOfRoles.add(listOfRolesAndPassword[i].trim());
        }
        return listOfRoles;
    }

}
