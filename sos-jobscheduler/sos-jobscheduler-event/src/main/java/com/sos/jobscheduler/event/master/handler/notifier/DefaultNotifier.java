package com.sos.jobscheduler.event.master.handler.notifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultNotifier implements INotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNotifier.class);

    @Override
    public void notifyOnError(String bodyPart, Throwable t) {
        LOGGER.error(t.toString());
    }

    @Override
    public void notifyOnError(Throwable t) {
        LOGGER.error(t.toString());
    }

}
