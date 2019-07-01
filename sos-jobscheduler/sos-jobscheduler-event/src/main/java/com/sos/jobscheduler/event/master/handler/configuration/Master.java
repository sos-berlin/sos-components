package com.sos.jobscheduler.event.master.handler.configuration;

public class Master {

    private String id;
    private String uri;
    private String user;
    private String password;
    private boolean useLogin;
    private boolean primary;

    public Master(String masterId, String masterUri) throws Exception {
        this(masterId, masterUri, null, null);
    }

    public Master(String masterId, String masterUri, String masterUser, String masterUserPassword) throws Exception {
        if (masterId == null) {
            throw new Exception("masterId is NULL");
        }
        if (masterUri == null) {
            throw new Exception("masterUri is NULL");
        }

        id = masterId.trim();
        uri = masterUri.trim();

        if (masterUser != null) {
            useLogin = true;
            user = masterUser.trim();
            if (masterUserPassword != null) {
                password = masterUserPassword.trim();
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

    @Override
    public String toString() {
        return String.format("%s, primary=%s", uri, primary);
    }

}
