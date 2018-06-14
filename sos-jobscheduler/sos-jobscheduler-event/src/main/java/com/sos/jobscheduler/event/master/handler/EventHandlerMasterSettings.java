package com.sos.jobscheduler.event.master.handler;

public class EventHandlerMasterSettings {

    private String httpHost;
    private String httpPort;
    private String schedulerId;
    private String user;
    private String password;
    private boolean useLogin;

    public void setHttpHost(String val) {
        httpHost = val;
    }

    public String getHttpHost() {
        return httpHost;
    }

    public void setHttpPort(String val) {
        httpPort = val;
    }

    public String getHttpPort() {
        return httpPort;
    }

    public void setSchedulerId(String val) {
        schedulerId = val;
    }

    public String getSchedulerId() {
        return schedulerId;
    }

    public void setUser(String val) {
        user = val;
    }

    public String getUser() {
        return user;
    }

    public void setPassword(String val) {
        password = val;
    }

    public String getPassword() {
        return password;
    }

    public void useLogin(boolean val) {
        useLogin = val;
    }

    public boolean useLogin() {
        return useLogin;
    }
}
