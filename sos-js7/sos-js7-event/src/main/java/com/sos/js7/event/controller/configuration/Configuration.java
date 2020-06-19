package com.sos.js7.event.controller.configuration;

import java.util.ArrayList;
import java.util.List;

import com.sos.js7.event.http.HttpClientConfiguration;
import com.sos.js7.event.controller.configuration.handler.HandlerConfiguration;
import com.sos.js7.event.controller.configuration.handler.MailerConfiguration;
import com.sos.js7.event.controller.configuration.handler.WebserviceConfiguration;
import com.sos.js7.event.controller.configuration.controller.ControllerConfiguration;

public class Configuration {

    private final HttpClientConfiguration httpClient;
    private final WebserviceConfiguration webservice;
    private final HandlerConfiguration handler;
    private final MailerConfiguration mailer;
    private List<ControllerConfiguration> controllers;
    private Object app;

    public Configuration() {
        httpClient = new HttpClientConfiguration();
        webservice = new WebserviceConfiguration();
        handler = new HandlerConfiguration();
        mailer = new MailerConfiguration();
        controllers = new ArrayList<ControllerConfiguration>();
    }

    public List<ControllerConfiguration> getControllers() {
        return controllers;
    }

    public void setControllers(List<ControllerConfiguration> val) {
        controllers = val;
    }

    public HttpClientConfiguration getHttpClient() {
        return httpClient;
    }

    public WebserviceConfiguration getWebservice() {
        return webservice;
    }

    public HandlerConfiguration getHandler() {
        return handler;
    }

    public MailerConfiguration getMailer() {
        return mailer;
    }

    public Object getApp() {
        return app;
    }

    public void setApp(Object val) {
        app = val;
    }
}
