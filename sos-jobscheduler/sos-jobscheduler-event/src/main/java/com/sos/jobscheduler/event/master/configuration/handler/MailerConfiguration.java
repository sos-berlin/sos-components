package com.sos.jobscheduler.event.master.configuration.handler;

import java.util.Properties;

import com.sos.commons.util.SOSString;

public class MailerConfiguration {

    private String smtpHost;
    private String smtpPort;
    private String smtpUser;
    private String smtpPassword;
    private String from;
    private String to;

    public void load(Properties conf) {
        if (!SOSString.isEmpty(conf.getProperty("mail_smtp_host"))) {
            smtpHost = conf.getProperty("mail_smtp_host").trim();
        }
        if (!SOSString.isEmpty(conf.getProperty("mail_smtp_port"))) {
            smtpPort = conf.getProperty("mail_smtp_port").trim();
        }
        if (!SOSString.isEmpty(conf.getProperty("mail_smtp_user"))) {
            smtpUser = conf.getProperty("mail_smtp_user").trim();
        }
        if (!SOSString.isEmpty(conf.getProperty("mail_smtp_password"))) {
            smtpPassword = conf.getProperty("mail_smtp_password").trim();
        }
        if (!SOSString.isEmpty(conf.getProperty("mail_from"))) {
            from = conf.getProperty("mail_from").trim();
        }
        if (!SOSString.isEmpty(conf.getProperty("mail_to"))) {
            to = conf.getProperty("mail_to").trim();
        }
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    public String getSmtpUser() {
        return smtpUser;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

}
