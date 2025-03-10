package com.sos.commons.vfs.ftp.commons;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;

import com.sos.commons.util.loggers.base.ISOSLogger;

public class FTPProtocolCommandListener implements ProtocolCommandListener {

    private final ISOSLogger logger;

    public FTPProtocolCommandListener(ISOSLogger logger) {
        this.logger = logger;
    }

    @Override
    public void protocolCommandSent(ProtocolCommandEvent event) {
        if (!"PASS".equalsIgnoreCase(event.getCommand())) {
            if (logger.isDebugEnabled()) {
                logger.debug("[%s]%s", event.getCommand(), event.getMessage().trim());
            }
        }
    }

    @Override
    public void protocolReplyReceived(ProtocolCommandEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug("[%s]%s", event.getCommand(), event.getMessage().trim());
        }
    }

}
