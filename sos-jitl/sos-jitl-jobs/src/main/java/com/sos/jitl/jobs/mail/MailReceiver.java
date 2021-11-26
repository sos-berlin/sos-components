package com.sos.jitl.jobs.mail;

import java.util.Map;

import javax.mail.MessagingException;

import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.mail.SOSMailReceiver;
import com.sos.jitl.jobs.common.JobLogger;


public class MailReceiver extends SOSMailReceiver {
    
    private final JobLogger jobLogger;

    public MailReceiver(Protocol protocol, Map<String, Object> mailProperties, JobLogger logger) throws SOSRequiredArgumentMissingException {
        super(protocol, mailProperties);
        this.jobLogger = logger;
    }

    public void connect() throws MessagingException {
        super.connect();
        if (jobLogger != null) {
            jobLogger.debug("..connection to host [" + getHostPort() + "] successfully established.");
        }
    }

}
