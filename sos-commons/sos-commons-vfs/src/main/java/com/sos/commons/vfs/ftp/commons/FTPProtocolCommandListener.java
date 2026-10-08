package com.sos.commons.vfs.ftp.commons;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.loggers.base.ISOSLogger;

public class FTPProtocolCommandListener implements ProtocolCommandListener {

    private final ISOSLogger logger;
    private final String logPrefix;

    public FTPProtocolCommandListener(ISOSLogger logger, String logPrefix) {
        this.logger = logger;
        this.logPrefix = logPrefix;
    }

    @Override
    public void protocolCommandSent(ProtocolCommandEvent event) {
        if (!"PASS".equalsIgnoreCase(event.getCommand())) {
            if (logger.isDebugEnabled()) {
                logger.debug("%s[CommandListener][Sent][%s]%s", logPrefix, event.getCommand(), normalizeMessage(event.getMessage()));
            }
        }
    }

    @Override
    public void protocolReplyReceived(ProtocolCommandEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug("%s[CommandListener][ReplyReceived]%s", logPrefix, normalizeMessage(event.getMessage()));
        }
    }

    private String normalizeMessage(String msg) {
        if (SOSString.isEmpty(msg)) {
            return "";
        }
        return SOSString.replaceNewLines(msg.trim(), " ");
    }

}
