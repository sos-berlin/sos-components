package com.sos.jobscheduler.history.master;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.util.SOSDate;
import com.sos.jobscheduler.event.master.EventMeta.EventPath;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.bean.IEntry;
import com.sos.jobscheduler.event.master.handler.LoopEventHandler;

public class HistoryEventHandlerMaster extends LoopEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryEventHandlerMaster.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private final SOSHibernateFactory factory;
    private HistoryModel model;
    private Date lastKeepEvents;

    // private boolean rerun = false;

    public HistoryEventHandlerMaster(SOSHibernateFactory hibernateFactory, HistoryMailer hm, EventPath path, Class<? extends IEntry> clazz) {
        super(hm, path, clazz);
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
            setIdentifier(Thread.currentThread().getName() + "-" + getSettings().getMasterId());

            model = new HistoryModel(factory, getSettings(), getIdentifier());
            model.setMaxTransactions(getSettings().getMaxTransactions());
            executeGetEventId();
            start(model.getStoredEventId());
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][%s]%s", getIdentifier(), method, e.toString()), e);
            getSender().sendOnError(method, e);
            wait(getWaitIntervalOnError());
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
    public Long onEmptyEvent(Long eventId, Event event) {
        // if (rerun) {
        return execute(false, eventId, event);
        // }
    }

    @Override
    public Long onNonEmptyEvent(Long eventId, Event event) {
        // rerun = false;
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
                LOGGER.info(String.format("[%s][%s]start storedEventId=%s", getIdentifier(), method, model.getStoredEventId()));
                lastKeepEvents = new Date();
            } catch (Throwable e) {
                LOGGER.error(String.format("[%s][%s][%s]%s", getIdentifier(), method, count, e.toString()), e);
                getSender().sendOnError(String.format("[%s][%s]", method, count), e);
                wait(getWaitIntervalOnError());
            }
        }
    }

    private Long execute(boolean onNonEmptyEvent, Long eventId, Event event) {
        String method = "execute";
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s]%s, eventId=%s", getIdentifier(), method, onNonEmptyEvent ? "onNonEmptyEvent" : "onEmptyEvent",
                    eventId));
        }
        Long newEventId = null;
        try {
            if (onNonEmptyEvent) {
                newEventId = model.process(event, getLastRestServiceDuration());
                // TODO sendKeepEvents for all events: empty and notEmpty
                sendKeepEvents(newEventId);
            } else {
                newEventId = event.getLastEventId();
            }

        } catch (Throwable e) {
            // rerun = true;
            LOGGER.error(String.format("[%s][%s]%s", getIdentifier(), method, e.toString()), e);
            getSender().sendOnError(method, e);
            wait(getWaitIntervalOnError());
            // TODO endless loop
            newEventId = eventId;
        }
        return newEventId;
    }

    private void sendKeepEvents(Long eventId) {
        String method = "sendKeepEvents";
        if (eventId != null && eventId > 0 && lastKeepEvents != null) {
            if (SOSDate.getMinutes(new Date()) - SOSDate.getMinutes(lastKeepEvents) >= getSettings().getKeepEventsInterval()) {
                LOGGER.info(String.format("[%s][%s]eventId=%s", getIdentifier(), method, eventId));
                try {
                    String answer = keepEvents(eventId);
                    if (answer != null && !answer.equals("Accepted")) {
                        LOGGER.error(String.format("[%s][%s][%s]%s", getIdentifier(), method, eventId, answer));
                    }
                } catch (Throwable t) {
                    LOGGER.error(String.format("[%s][%s][%s]%s", getIdentifier(), method, eventId, t.toString()), t);
                } finally {
                    lastKeepEvents = new Date();
                }
            }
        }

    }
}
