package com.sos.jitl.jobs.mail;

import java.util.Map;

import javax.mail.MessagingException;

import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.mail.SOSMailReceiver;
import com.sos.commons.util.loggers.base.ISOSLogger;

public class MailReceiver extends SOSMailReceiver {

    private final ISOSLogger logger;

    public MailReceiver(Protocol protocol, Map<String, Object> mailProperties, ISOSLogger logger) throws SOSRequiredArgumentMissingException {
        super(protocol, mailProperties);
        this.logger = logger;
    }

    public void connect() throws MessagingException {
        super.connect();

        if (logger.isDebugEnabled()) {
            logger.debug("..connection to host [" + getHostPort() + "] successfully established.");
        }

    }

}
