package com.sos.auth.keycloak.classes;

public class SOSKeycloakUserAccessRepresentation {

    private Boolean manageGroupMembership;
    private Boolean view;
    private Boolean mapRoles;
    private Boolean impersonate;
    private Boolean manage;

    public Boolean getManageGroupMembership() {
        return manageGroupMembership;
    }

    public Boolean getView() {
        return view;
    }

    public Boolean getMapRoles() {
        return mapRoles;
    }

    public Boolean getImpersonate() {
        return impersonate;
    }

    public Boolean getManage() {
        return manage;
    }

}
