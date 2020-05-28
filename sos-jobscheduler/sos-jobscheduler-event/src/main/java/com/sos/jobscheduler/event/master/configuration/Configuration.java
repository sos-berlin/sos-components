package com.sos.jobscheduler.event.master.configuration;

import java.util.ArrayList;
import java.util.List;

import com.sos.jobscheduler.event.http.HttpClientConfiguration;
import com.sos.jobscheduler.event.master.configuration.handler.HandlerConfiguration;
import com.sos.jobscheduler.event.master.configuration.handler.MailerConfiguration;
import com.sos.jobscheduler.event.master.configuration.handler.WebserviceConfiguration;
import com.sos.jobscheduler.event.master.configuration.master.MasterConfiguration;

public class Configuration {

    private final HttpClientConfiguration httpClient;
    private final WebserviceConfiguration webservice;
    private final HandlerConfiguration handler;
    private final MailerConfiguration mailer;
    private List<MasterConfiguration> masters;
    private Object app;
   
    public Configuration() {
        httpClient = new HttpClientConfiguration();
        webservice = new WebserviceConfiguration();
        handler = new HandlerConfiguration();
        mailer = new MailerConfiguration();
        masters = new ArrayList<MasterConfiguration>();
    }
   
    public List<MasterConfiguration> getMasters() {
        return masters;
    }

    public void setMasters(List<MasterConfiguration> val) {
        masters = val;
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
