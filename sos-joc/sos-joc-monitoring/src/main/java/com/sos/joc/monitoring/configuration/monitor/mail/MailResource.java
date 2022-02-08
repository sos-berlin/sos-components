package com.sos.joc.monitoring.configuration.monitor.mail;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.joc.Globals;

public class MailResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailResource.class);

    private static final String ARG_FROM = "from";
    private static final String ARG_FROM_NAME = "from_name";
    private static final String ARG_REPLAY_TO = "replay_to";
    private static final String ARG_TO = "to";
    private static final String ARG_CC = "cc";
    private static final String ARG_BCC = "bcc";

    private SOSCredentialStoreArguments credentialStoreArgs;
    private Properties mailProperties;
    private String jobResourceName;
    private String from;
    private String fromName;
    private String replayTo;
    private String to;
    private String cc;
    private String bcc;

    /** Create a MailResource from a certain JobResource */
    public void parse(String name, String content) {
        jobResourceName = name;
        init();
        try {
            Environment env = Globals.objectMapper.readValue(content, JobResource.class).getArguments();
            Map<String, String> arguments = env.getAdditionalProperties();
            if (arguments != null) {
                arguments.entrySet().stream().forEach(e -> {
                    setCredentialStoreArgs(e.getKey(), strip(e.getValue()));
                    if (e.getKey().startsWith("mail.")) {
                        mailProperties.put(e.getKey(), strip(e.getValue()));
                    }
                });

                from = strip(arguments.get(ARG_FROM));
                fromName = strip(arguments.get(ARG_FROM_NAME));
                replayTo = strip(arguments.get(ARG_REPLAY_TO));
                to = strip(arguments.get(ARG_TO));
                cc = strip(arguments.get(ARG_CC));
                bcc = strip(arguments.get(ARG_BCC));
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[parse][%s][mail properties]%s", jobResourceName, getMaskedMailProperties()));
                LOGGER.debug(String.format("[parse][%s][credentialStoreArgs]%s", jobResourceName, SOSString.toString(credentialStoreArgs)));
            }

        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    /** Create a MailResource from the JobResource list.<br />
     * The sequence of assignment is relevant as variables with the same name are not overwritten in the order from the first to the last assignment. */
    public void parse(List<MailResource> list) {
        init();
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        for (MailResource mr : list) {
            mr.getMailProperties().entrySet().forEach(e -> {
                if (!mailProperties.containsKey(e.getKey())) {
                    mailProperties.put(e.getKey(), e.getValue());
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[parse][job_resource=%s][mail %s]added", mr.jobResourceName, e.getKey()));
                    }
                }
            });

            setCurrentCredentialStoreArgs(mr, isDebugEnabled, mr.jobResourceName);

            from = getCurrentValue("mail", "from", from, mr.getFrom(), isDebugEnabled, mr.jobResourceName);
            fromName = getCurrentValue("mail", "fromName", fromName, mr.getFromName(), isDebugEnabled, mr.jobResourceName);
            replayTo = getCurrentValue("mail", "replayTo", replayTo, mr.getReplayTo(), isDebugEnabled, mr.jobResourceName);
            to = getCurrentValue("mail", "to", to, mr.getTo(), isDebugEnabled, mr.jobResourceName);
            cc = getCurrentValue("mail", "cc", cc, mr.getCC(), isDebugEnabled, mr.jobResourceName);
            bcc = getCurrentValue("mail", "bcc", bcc, mr.getBCC(), isDebugEnabled, mr.jobResourceName);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[parse][result]credentialStoreArgs=" + SOSString.toString(credentialStoreArgs));
        }
    }

    private void setCredentialStoreArgs(String key, String value) {
        if (SOSCredentialStoreArguments.ARG_NAME_FILE.equals(key)) {
            credentialStoreArgs.setFile(value);
        } else if (SOSCredentialStoreArguments.ARG_NAME_KEY_FILE.equals(key)) {
            credentialStoreArgs.setKeyFile(value);
        } else if (SOSCredentialStoreArguments.ARG_NAME_PASSWORD.equals(key)) {
            credentialStoreArgs.setPassword(value);
        } else if (SOSCredentialStoreArguments.ARG_NAME_ENTRY_PATH.equals(key)) {
            credentialStoreArgs.setEntryPath(value);
        }
    }

    private String getCurrentValue(String range, String name, String currentValue, String value, boolean isDebugEnabled, String jobResourceName) {
        if (SOSString.isEmpty(currentValue) && !SOSString.isEmpty(value)) {
            currentValue = value;
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[parse][job_resource=%s][%s %s]added", jobResourceName, range, name));
            }
        }
        return currentValue;
    }

    private void setCurrentCredentialStoreArgs(MailResource mr, boolean isDebugEnabled, String jobResourceName) {
        if (mr.getCredentialStoreArgs() != null) {
            credentialStoreArgs.setFile(getCurrentValue("credentialStore", "file", credentialStoreArgs.getFile().getValue(), mr
                    .getCredentialStoreArgs().getFile().getValue(), isDebugEnabled, jobResourceName));

            credentialStoreArgs.setKeyFile(getCurrentValue("credentialStore", "keyFile", credentialStoreArgs.getKeyFile().getValue(), mr
                    .getCredentialStoreArgs().getKeyFile().getValue(), isDebugEnabled, jobResourceName));

            credentialStoreArgs.setPassword(getCurrentValue("credentialStore", "password", credentialStoreArgs.getPassword().getValue(), mr
                    .getCredentialStoreArgs().getPassword().getValue(), isDebugEnabled, jobResourceName));

            credentialStoreArgs.setEntryPath(getCurrentValue("credentialStore", "entryPath", credentialStoreArgs.getEntryPath().getValue(), mr
                    .getCredentialStoreArgs().getEntryPath().getValue(), isDebugEnabled, jobResourceName));
        }
    }

    private String strip(String s) {
        return StringUtils.strip(StringUtils.strip(s, "\""), "'");
    }

    private void init() {
        credentialStoreArgs = new SOSCredentialStoreArguments();
        mailProperties = new Properties();
        from = null;
        fromName = null;
        replayTo = null;
        to = null;
        cc = null;
        bcc = null;
    }

    public SOSCredentialStoreArguments getCredentialStoreArgs() {
        return credentialStoreArgs;
    }

    public Properties getMailProperties() {
        return mailProperties;
    }

    public Properties copyMailProperties() {
        Properties p = new Properties();
        if (mailProperties != null) {
            mailProperties.forEach((k, v) -> {
                p.put(k, v);
            });
        }
        return p;
    }

    public Properties getMaskedMailProperties() {
        Properties p = copyMailProperties();
        p.forEach((k, v) -> {
            if (k.toString().contains("password")) {
                p.replace(k, DisplayMode.MASKED.getValue());
            }
        });
        return p;
    }

    public String getFrom() {
        return from;
    }

    public String getFromName() {
        return fromName;
    }

    public String getReplayTo() {
        return replayTo;
    }

    public String getTo() {
        return to;
    }

    public String getCC() {
        return cc;
    }

    public String getBCC() {
        return bcc;
    }

}
