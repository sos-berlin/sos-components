package com.sos.auth.keycloak.classes;

public class SOSKeycloakStoreUser {

    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPassword() {
      return this.password;
    }


}
