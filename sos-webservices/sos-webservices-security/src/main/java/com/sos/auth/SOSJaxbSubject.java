package com.sos.auth;

import com.sos.auth.rest.permission.model.SOSPermissionShiro;

public class SOSJaxbSubject {

    private SOSPermissionShiro sosPermissionShiro;

    public SOSJaxbSubject(SOSPermissionShiro sosPermissionShiro) {
        super();
        this.sosPermissionShiro = sosPermissionShiro;
    }

    public boolean hasRole(String role) {
        return (sosPermissionShiro != null && sosPermissionShiro.getSOSPermissionRoles().getSOSPermissionRole().contains(role));
    }

    public boolean isPermitted(String permission) {

        if (permission.startsWith("sos:products:")) {
            return (sosPermissionShiro != null && sosPermissionShiro.getSOSPermissions().getSOSPermission().contains(permission));
        }
        return false;
    }

    public boolean isAuthenticated() {
        return sosPermissionShiro != null && sosPermissionShiro.isSetAuthenticated() && sosPermissionShiro.isAuthenticated();
    }

    public String getSessionId() {
        if (sosPermissionShiro == null) {
            return "";
        } else {
            return sosPermissionShiro.getAccessToken();
        }
    }

}