package com.sos.jobscheduler.event.master.handler;

public class EventHandlerMasterSettings {

    private String httpHost;
    private String httpPort;
    private String schedulerId;
    private String user;
    private String password;
    private boolean useLogin;

    private int webserviceTimeout = 60;// seconds
    private int webserviceDelay = 0; // seconds
    private int webserviceLimit = 1000;

    // milliseconds
    private int httpClientConnectTimeout = 30_000;
    private int httpClientConnectionRequestTimeout = 30_000;
    private int httpClientSocketTimeout = 75_000;

    // milliseconds
    private int waitIntervalOnError = 30_000;
    private int waitIntervalOnEmptyEvent = 1_000;
    private int maxWaitIntervalOnEnd = 30_000;

    private int maxTransactions = 100;
    private boolean logRequestExecutionTime = false;

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

    public int getWebserviceTimeout() {
        return webserviceTimeout;
    }

    public void setWebserviceTimeout(int val) {
        webserviceTimeout = val;
    }

    public int getWebserviceDelay() {
        return webserviceDelay;
    }

    public void setWebserviceDelay(int val) {
        webserviceDelay = val;
    }

    public int getWebserviceLimit() {
        return webserviceLimit;
    }

    public void setWebserviceLimit(int val) {
        webserviceLimit = val;
    }

    public int getHttpClientConnectTimeout() {
        return httpClientConnectTimeout;
    }

    public void setHttpClientConnectTimeout(int val) {
        httpClientConnectTimeout = val;
    }

    public int getHttpClientConnectionRequestTimeout() {
        return httpClientConnectionRequestTimeout;
    }

    public void setHttpClientConnectionRequestTimeout(int val) {
        httpClientConnectionRequestTimeout = val;
    }

    public int getHttpClientSocketTimeout() {
        return httpClientSocketTimeout;
    }

    public void setHttpClientSocketTimeout(int val) {
        httpClientSocketTimeout = val;
    }

    public int getWaitIntervalOnError() {
        return waitIntervalOnError;
    }

    public void setWaitIntervalOnError(int val) {
        waitIntervalOnError = val;
    }

    public int getMaxWaitIntervalOnEnd() {
        return maxWaitIntervalOnEnd;
    }

    public void setMaxWaitIntervalOnEnd(int val) {
        maxWaitIntervalOnEnd = val;
    }

    public int getWaitIntervalOnEmptyEvent() {
        return waitIntervalOnEmptyEvent;
    }

    public void setWaitIntervalOnEmptyEvent(int val) {
        waitIntervalOnEmptyEvent = val;
    }

    public int getMaxTransactions() {
        return maxTransactions;
    }

    public void setMaxTransactions(int val) {
        maxTransactions = val;
    }

    public boolean getLogRequestExecutionTime() {
        return logRequestExecutionTime;
    }

    public void setLogRequestExecutionTime(boolean val) {
        logRequestExecutionTime = val;
    }
}
