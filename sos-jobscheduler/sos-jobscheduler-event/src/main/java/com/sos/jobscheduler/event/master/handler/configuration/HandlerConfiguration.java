package com.sos.jobscheduler.event.master.handler.configuration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class HandlerConfiguration {

    private Path hibernateConfiguration;
    private List<IMasterConfiguration> masters;

    private String mailSmtpHost;
    private String mailSmtpPort;
    private String mailSmtpUser;
    private String mailSmtpPassword;
    private String mailFrom;
    private String mailTo;

    public HandlerConfiguration() {
        masters = new ArrayList<IMasterConfiguration>();
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

    public String getMailSmtpHost() {
        return mailSmtpHost;
    }

    public void setMailSmtpHost(String val) {
        mailSmtpHost = val;
    }

    public String getMailSmtpPort() {
        return mailSmtpPort;
    }

    public void setMailSmtpPort(String val) {
        mailSmtpPort = val;
    }

    public String getMailSmtpUser() {
        return mailSmtpUser;
    }

    public void setMailSmtpUser(String val) {
        mailSmtpUser = val;
    }

    public String getMailSmtpPassword() {
        return mailSmtpPassword;
    }

    public void setMailSmtpPassword(String val) {
        mailSmtpPassword = val;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public void setMailFrom(String val) {
        mailFrom = val;
    }

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(String val) {
        mailTo = val;
    }

}
