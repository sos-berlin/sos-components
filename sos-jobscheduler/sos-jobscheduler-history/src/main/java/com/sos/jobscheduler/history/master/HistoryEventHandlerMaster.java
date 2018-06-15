package com.sos.jobscheduler.history.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.jobscheduler.event.master.EventMeta.EventPath;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.bean.IEntry;
import com.sos.jobscheduler.event.master.handler.UnlimitedEventHandler;

public class HistoryEventHandlerMaster extends UnlimitedEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryEventHandlerMaster.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private final SOSHibernateFactory factory;
    private HistoryModel model;
    private boolean rerun = false;

    public HistoryEventHandlerMaster(SOSHibernateFactory hibernateFactory, EventPath path, Class<? extends IEntry> clazz) {
        super(path, clazz);
        factory = hibernateFactory;

    }

    @Override
    public void run() {
        super.run();

        String method = "run";
        try {
            setWebserviceTimeout(getSettings().getWebserviceTimeout());
            setWebserviceLimit(getSettings().getWebserviceLimit());
            setWebserviceDelay(getSettings().getWebserviceDelay());

            setHttpClientConnectTimeout(getSettings().getHttpClientConnectTimeout());
            setHttpClientConnectionRequestTimeout(getSettings().getHttpClientConnectionRequestTimeout());
            setHttpClientSocketTimeout(getSettings().getHttpClientSocketTimeout());

            setWaitIntervalOnEmptyEvent(getSettings().getWaitIntervalOnEmptyEvent());
            setWaitIntervalOnError(getSettings().getWaitIntervalOnError());
            setMaxWaitIntervalOnEnd(getSettings().getMaxWaitIntervalOnEnd());

            useLogin(getSettings().useLogin());
            setIdentifier(getSettings().getSchedulerId());

            model = new HistoryModel(factory, getSettings(), getIdentifier());
            executeGetEventId();
            start(model.getStoredEventId());
        } catch (Exception e) {
            LOGGER.error(String.format("%s %s", method, e.toString()), e);
        }
    }

    @Override
    public void close() {
        super.close();
        if (model != null) {
            model.close();
        }
    }

    @Override
    public void onEnded() {
        super.onEnded();
    }

    @Override
    public void onEmptyEvent(Long eventId) {
        if (rerun) {
            if (isDebugEnabled) {
                String method = "onEmptyEvent";
                LOGGER.debug(String.format("%s eventId=%s", method, eventId));
            }
            execute(false, eventId, null);
        }
    }

    @Override
    public Long onNonEmptyEvent(Long eventId, Event event) {
        if (isDebugEnabled) {
            String method = "onNonEmptyEvent";
            LOGGER.debug(String.format("%s eventId=%s", method, eventId));
        }
        rerun = false;
        return execute(true, eventId, event);
    }

    private void executeGetEventId() {
        String method = "executeGetEventId";
        int count = 0;
        boolean run = true;
        while (run) {
            count++;
            try {
                model.setStoredEventId(model.getEventId());
                run = false;
                LOGGER.info(String.format("[%s]start storedEventId=%s", method, model.getStoredEventId()));
            } catch (Exception e) {
                LOGGER.error(String.format("[%s][%s]%s", method, count, e.toString()), e);
                wait(getWaitIntervalOnError());
            }
        }
    }

    private Long execute(boolean onNonEmptyEvent, Long eventId, Event event) {
        String method = "execute";
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s onNonEmptyEvent=%s, eventId=%s", method, onNonEmptyEvent, eventId));
        }
        Long newEventId = null;
        try {
            if (onNonEmptyEvent) {
                newEventId = model.process(event);
            } else {
                newEventId = event.getLastEventId();
                wait(getWaitIntervalOnEmptyEvent());
            }

        } catch (Throwable e) {
            rerun = true;
            LOGGER.error(String.format("%s %s", method, e.toString()), e);
        }
        return newEventId;
    }
}
