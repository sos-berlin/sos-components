package com.sos.jobscheduler.master.event;

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
        this.host = val;
    }

    public String getHttpHost() {
        return httpHost;
    }

    public void setHttpHost(String httpHost) {
        this.httpHost = httpHost;
    }

    public String getHttpsHost() {
        return httpsHost;
    }

    public void setHttpsHost(String httpsHost) {
        this.httpsHost = httpsHost;
    }

    public void setHttpPort(String val) {
        this.httpPort = val;
    }

    public void setHttpsPort(String httpsPort) {
        this.httpsPort = httpsPort;
    }

    public void setLiveDirectory(Path val) {
        this.liveDirectory = val;
    }

    public void setSchedulerId(String val) {
        this.schedulerId = val;
    }
}
