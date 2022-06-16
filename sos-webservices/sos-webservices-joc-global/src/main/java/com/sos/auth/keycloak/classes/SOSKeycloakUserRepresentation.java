package com.sos.auth.keycloak.classes;

import java.util.ArrayList;
import java.util.List;

public class SOSKeycloakUserRepresentation {

    private String id;
    private String createdTimestamp;
    private String username;
    private String enabled;
    private String totp;
    private String emailVerified;
    private String firstName;
    private String lastName;
    private String email;
    public ArrayList<String> disableableCredentialTypes;
    public ArrayList<String> requiredActions;
    private String notBefore;
    private SOSKeycloakUserAccessRepresentation access;

    public String getId() {
        return id;
    }

    public String getCreatedTimestamp() {
        return createdTimestamp;
    }

    public String getUsername() {
        return username;
    }

    public String getEnabled() {
        return enabled;
    }

    public String getTotp() {
        return totp;
    }

    public String getEmailVerified() {
        return emailVerified;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getDisableableCredentialTypes() {
        return disableableCredentialTypes;
    }

    public List<String> getRequiredActions() {
        return requiredActions;
    }

    public String getNotBefore() {
        return notBefore;
    }

    public SOSKeycloakUserAccessRepresentation getAccess() {
        return access;
    }

}
