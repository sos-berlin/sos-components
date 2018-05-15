package com.sos.jobscheduler.event.master.handler;

import java.nio.file.Path;

public class EventHandlerMasterSettings {

    private Path configDirectory;
    private String host;
    private String httpHost;
    private String httpPort;
    private String httpsHost;
    private String httpsPort;
    private Path liveDirectory;
    private String schedulerId;

    public Path getConfigDirectory() {
        return configDirectory;
    }

    public void setConfigDirectory(Path val) {
        this.configDirectory = val;
    }

    public String getHost() {
        return host;
    }

    public String getHttpPort() {
        return httpPort;
    }

    public String getHttpsPort() {
        return httpsPort;
    }

    public Path getLiveDirectory() {
        return liveDirectory;
    }

    public String getSchedulerId() {
        return schedulerId;
    }

    public void setHost(String val) {
        host = val;
    }

    public String getHttpHost() {
        return httpHost;
    }

    public void setHttpHost(String val) {
        httpHost = val;
    }

    public String getHttpsHost() {
        return httpsHost;
    }

    public void setHttpsHost(String val) {
        httpsHost = val;
    }

    public void setHttpPort(String val) {
        httpPort = val;
    }

    public void setHttpsPort(String val) {
        httpsPort = val;
    }

    public void setLiveDirectory(Path val) {
        liveDirectory = val;
    }

    public void setSchedulerId(String val) {
        schedulerId = val;
    }
}
