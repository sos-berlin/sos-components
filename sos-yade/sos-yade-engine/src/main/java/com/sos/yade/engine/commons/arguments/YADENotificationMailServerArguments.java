package com.sos.yade.engine.commons.arguments;

import java.util.Properties;

import com.sos.commons.mail.SOSMail;
import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgumentHelper;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;

public class YADENotificationMailServerArguments extends ASOSArguments {

    private final Properties mailSettings;

    private SOSArgument<String> hostname = new SOSArgument<>("Hostname", false);
    private SOSArgument<Integer> port = new SOSArgument<>("Port", false, Integer.valueOf(25));
    private SOSArgument<String> account = new SOSArgument<>("Account", false);
    private SOSArgument<String> password = new SOSArgument<>("Password", false, DisplayMode.MASKED);

    private SOSArgument<String> queueDirectory = new SOSArgument<>("QueueDirectory", false);
    private SOSArgument<String> connectTimeout = new SOSArgument<>("ConnectTimeout", false, "30s");

    public YADENotificationMailServerArguments() {
        mailSettings = new Properties();
        applyPort(String.valueOf(port.getDefaultValue()));
        applyConnectTimeout(connectTimeout.getDefaultValue());
    }

    public boolean isEnabled() {
        return !hostname.isEmpty();
    }

    public Properties getMailSettings() {
        return mailSettings;
    }

    public void addMailSetting(String name, String value) {
        if (name == null || value == null) {
            return;
        }
        mailSettings.put(name, value);
    }

    public void applyHostname(String val) {
        if (val == null) {
            return;
        }
        hostname.setValue(val);
        mailSettings.put(SOSMail.PROPERTY_NAME_SMTP_HOST, val);
    }

    public void applyPort(String val) {
        if (val == null) {
            return;
        }
        port.setValue(Integer.parseInt(val));
        mailSettings.put(SOSMail.PROPERTY_NAME_SMTP_PORT, val);
    }

    public void applyAccount(String val) {
        if (val == null) {
            return;
        }
        account.setValue(val);
        mailSettings.put(SOSMail.PROPERTY_NAME_SMTP_USER, val);
    }

    public void applyPassword(String val) {
        if (val == null) {
            return;
        }
        password.setValue(val);
        mailSettings.put(SOSMail.PROPERTY_NAME_SMTP_PASSWORD, val);
    }

    public void applyConnectTimeout(String val) {
        if (val == null) {
            return;
        }
        connectTimeout.setValue(val);
        mailSettings.put(SOSMail.PROPERTY_NAME_SMTP_CONNECTION_TIMEOUT, String.valueOf(SOSArgumentHelper.asMillis(connectTimeout)));
    }

    public SOSArgument<String> getQueueDirectory() {
        return queueDirectory;
    }

}
