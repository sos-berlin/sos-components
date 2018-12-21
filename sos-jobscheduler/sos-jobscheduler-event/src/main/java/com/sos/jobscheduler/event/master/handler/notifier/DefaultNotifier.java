package com.sos.jobscheduler.event.master.handler.notifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultNotifier implements INotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNotifier.class);

    @Override
    public void notifyOnError(String title, String msg, Throwable t) {
        LOGGER.error(String.format("[%s][%s]%s", title, msg, t == null ? "" : t.toString()));
    }

    @Override
    public void notifyOnError(String msg, Throwable t) {
        LOGGER.error(String.format("[%s]%s", msg, t == null ? "" : t.toString()));
    }

    @Override
    public void notifyOnError(Throwable t) {
        LOGGER.error(t.toString());
    }

    @Override
    public void notifyOnWarning(String title, String msg, Throwable t) {
        LOGGER.warn(String.format("[%s]%s", msg, t == null ? "" : t.toString()));
    }

    @Override
    public void notifyOnSuccess(String title, String msg) {
        LOGGER.info(String.format("[%s]%s", title, msg));
    }
}
