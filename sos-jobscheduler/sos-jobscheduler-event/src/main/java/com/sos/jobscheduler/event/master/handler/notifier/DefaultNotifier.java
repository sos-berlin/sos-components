package com.sos.jobscheduler.event.master.handler.notifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultNotifier implements INotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNotifier.class);

    @Override
    public void notifyOnError(String msg, Throwable t) {
        notifyOnError(null, msg, t);
    }

    @Override
    public void notifyOnError(String title, String msg, Throwable t) {
        StringBuilder sb = new StringBuilder();
        if (title != null) {
            sb.append("[").append(title).append("]");
        }
        if (t == null) {
            sb.append(msg);
        } else {
            sb.append("[").append(msg).append("]").append(t.toString());
        }
        LOGGER.error(sb.toString());
    }

    @Override
    public void notifyOnWarning(String msg, Throwable t) {
        notifyOnWarning(null, msg, t);
    }

    @Override
    public void notifyOnWarning(String title, String msg, Throwable t) {
        StringBuilder sb = new StringBuilder();
        if (title != null) {
            sb.append("[").append(title).append("]");
        }
        if (t == null) {
            sb.append(msg);
        } else {
            sb.append("[").append(msg).append("]").append(t.toString());
        }
        LOGGER.warn(sb.toString());
    }

    @Override
    public void notifyOnRecovery(String title, String msg) {
        LOGGER.info(String.format("[%s]%s", title, msg));
    }
}
