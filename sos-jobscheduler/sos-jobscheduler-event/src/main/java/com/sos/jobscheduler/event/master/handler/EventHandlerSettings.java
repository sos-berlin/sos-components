package com.sos.jobscheduler.event.master.handler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class EventHandlerSettings {

    private Path hibernateConfiguration;
    private List<EventHandlerMasterSettings> masters;

    private String mailSmtpHost;
    private String mailSmtpPort;
    private String mailSmtpUser;
    private String mailSmtpPassword;
    private String mailFrom;
    private String mailTo;

    public EventHandlerSettings() {
        masters = new ArrayList<EventHandlerMasterSettings>();
    }

    public Path getHibernateConfiguration() {
        return hibernateConfiguration;
    }

    public void setHibernateConfiguration(Path val) {
        hibernateConfiguration = val;
    }

    public List<EventHandlerMasterSettings> getMasters() {
        return masters;
    }

    public void addMaster(EventHandlerMasterSettings master) {
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
