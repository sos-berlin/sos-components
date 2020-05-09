package com.sos.jobscheduler.event.master.configuration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.sos.jobscheduler.event.master.configuration.handler.HandlerConfiguration;
import com.sos.jobscheduler.event.master.configuration.handler.HttpClientConfiguration;
import com.sos.jobscheduler.event.master.configuration.handler.MailerConfiguration;
import com.sos.jobscheduler.event.master.configuration.handler.WebserviceConfiguration;
import com.sos.jobscheduler.event.master.configuration.master.MasterConfiguration;

public class Configuration {

    private final List<MasterConfiguration> masters;
    private final HttpClientConfiguration httpClient;
    private final WebserviceConfiguration webservice;
    private final HandlerConfiguration handler;
    private final MailerConfiguration mailer;
    private Object app;
    private Path hibernateConfiguration;
    private boolean isPublic;

    public Configuration() {
        masters = new ArrayList<MasterConfiguration>();
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

    public boolean isPublic() {
        return isPublic;
    }

    public void isPublic(boolean val) {
        isPublic = val;
    }

    public List<MasterConfiguration> getMasters() {
        return masters;
    }

    public void addMaster(MasterConfiguration master) {
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
