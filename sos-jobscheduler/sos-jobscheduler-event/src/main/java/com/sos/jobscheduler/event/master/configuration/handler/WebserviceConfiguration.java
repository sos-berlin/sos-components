package com.sos.jobscheduler.event.master.configuration.handler;

import java.util.Properties;

public class WebserviceConfiguration {

    private int timeout = 60;// seconds
    private int delay = 0; // seconds
    private int limit = 1000;

    public void load(Properties conf) {
        if (conf.getProperty("webservice_timeout") != null) {
            timeout = Integer.parseInt(conf.getProperty("webservice_timeout").trim());
        }
        if (conf.getProperty("webservice_limit") != null) {
            limit = Integer.parseInt(conf.getProperty("webservice_limit").trim());
        }
        if (conf.getProperty("webservice_delay") != null) {
            delay = Integer.parseInt(conf.getProperty("webservice_delay").trim());
        }
    }

    public int getTimeout() {
        return timeout;
    }

    public int getDelay() {
        return delay;
    }

    public int getLimit() {
        return limit;
    }

}
