package com.sos.auth.keycloak.classes;

public class SOSKeycloakRoleRepresentation {

    private String id;
    private String name;
    private String description;
    private Boolean composite;
    private Boolean clientRole;
    private String containerId;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getComposite() {
        return composite;
    }

    public Boolean getClientRole() {
        return clientRole;
    }

    public String getContainerId() {
        return containerId;
    }

}
