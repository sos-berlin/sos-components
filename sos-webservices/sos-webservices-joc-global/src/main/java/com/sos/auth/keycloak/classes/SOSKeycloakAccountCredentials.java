package com.sos.auth.keycloak.classes;

import java.util.List;

public class SOSKeycloakAccountCredentials {

    private String accountName;
    private String password;
    private List<String> keycloackRoles;

    public String getUsername() {
        return accountName;
    }

    public void setUsername(String username) {
        this.accountName = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public List<String> getKeycloackRoles() {
        return keycloackRoles;
    }

    public void setKeycloackRoles(List<String> keycloackRoles) {
        this.keycloackRoles = keycloackRoles;
    }

}
