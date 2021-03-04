package com.sos.joc.cluster.configuration.controller;

import com.sos.commons.util.SOSString;

public class Controller {

    private String id;
    private String uri;// JOC
    private String clusterUri;// between controllers
    private String user;
    private String password;
    private boolean useLogin;
    private boolean primary;

    public Controller(String controllerId, String controllerUri, String clusterUri) throws Exception {
        this(controllerId, controllerUri, clusterUri, null, null);
    }

    public Controller(String controllerId, String controllerUri, String controllerClusterUri, String controllerUser, String controllerUserPassword)
            throws Exception {
        if (controllerId == null) {
            throw new Exception("controllerId is NULL");
        }
        if (controllerUri == null) {
            throw new Exception("controllerUri is NULL");
        }

        id = controllerId.trim();
        uri = controllerUri.trim();
        clusterUri = SOSString.isEmpty(controllerClusterUri) ? null : controllerClusterUri.trim();

        if (controllerUser != null) {
            useLogin = true;
            user = controllerUser.trim();
            if (controllerUserPassword != null) {
                password = controllerUserPassword.trim();
            }
        }
    }

    public String getId() {
        return id;
    }

    protected void setId(String val) {
        id = val;
    }

    public String getUri() {
        return uri;
    }

    public String getClusterUri() {
        return clusterUri;
    }

    public String getUri4Log() {
        if (clusterUri == null || clusterUri.equals(uri)) {
            return uri;
        }
        return new StringBuilder("uri=").append(uri).append(", clusterUri=").append(clusterUri).toString();
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public boolean useLogin() {
        return useLogin;
    }

    protected void setPrimary(boolean val) {
        primary = val;
    }

    public boolean isPrimary() {
        return primary;
    }

    public String getType() {
        return primary ? "Primary" : "Secondary";
    }

    @Override
    public String toString() {
        return String.format("%s, primary=%s", uri, primary);
    }

}
