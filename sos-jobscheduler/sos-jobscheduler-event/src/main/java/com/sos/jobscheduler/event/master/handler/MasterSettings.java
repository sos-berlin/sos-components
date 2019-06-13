package com.sos.jobscheduler.event.master.handler;

public class MasterSettings {

    private String id;
    private String hostname;
    private String port;
    private String user;
    private String password;
    private boolean useLogin;
    private boolean primary;

    public MasterSettings(String masterId, String masterHost, String masterPort) throws Exception {
        this(masterHost, masterHost, masterPort, null, null);
    }

    public MasterSettings(String masterId, String masterHost, String masterPort, String masterUser, String masterUserPassword) throws Exception {
        if (masterId == null) {
            throw new Exception("masterId is NULL");
        }
        if (masterHost == null) {
            throw new Exception("masterHost is NULL");
        }
        if (masterPort == null) {
            throw new Exception("masterPort is NULL");
        }

        id = masterId.trim();
        hostname = masterHost.trim();
        port = masterPort.trim();

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

    public String getHostname() {
        return hostname;
    }

    public String getPort() {
        return port;
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
        return String.format("%s:%s, primary=%s", hostname, port, primary);
    }

}
