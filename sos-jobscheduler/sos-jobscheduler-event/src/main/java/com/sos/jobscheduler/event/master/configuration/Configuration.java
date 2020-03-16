package com.sos.jobscheduler.event.master.configuration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.sos.jobscheduler.event.master.configuration.handler.HandlerConfiguration;
import com.sos.jobscheduler.event.master.configuration.handler.HttpClientConfiguration;
import com.sos.jobscheduler.event.master.configuration.handler.MailerConfiguration;
import com.sos.jobscheduler.event.master.configuration.handler.WebserviceConfiguration;
import com.sos.jobscheduler.event.master.configuration.master.IMasterConfiguration;

public class Configuration {

    private Path hibernateConfiguration;
    private final List<IMasterConfiguration> masters;
    private final HttpClientConfiguration httpClient;
    private final WebserviceConfiguration webservice;
    private final HandlerConfiguration handler;
    private final MailerConfiguration mailer;
    private Object app;

    public Configuration() {
        masters = new ArrayList<IMasterConfiguration>();
        httpClient = new HttpClientConfiguration();
        webservice = new WebserviceConfiguration();
        handler = new HandlerConfiguration();
        mailer = new MailerConfiguration();
    }

    public Path getHibernateConfiguration() {
        return hibernateConfiguration;
    }

    public void setHibernateConfiguration(Path val) {
        hibernateConfiguration = val;
    }

    public List<IMasterConfiguration> getMasters() {
        return masters;
    }

    public void addMaster(IMasterConfiguration master) {
        masters.add(master);
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
