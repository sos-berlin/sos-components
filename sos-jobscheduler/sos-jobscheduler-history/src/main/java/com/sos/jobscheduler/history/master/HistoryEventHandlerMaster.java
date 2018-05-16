package com.sos.jobscheduler.history.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.event.master.EventMeta.EventPath;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.bean.IEntry;
import com.sos.jobscheduler.event.master.handler.UnlimitedEventHandler;

public class HistoryEventHandlerMaster extends UnlimitedEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryEventHandlerMaster.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private SOSHibernateFactory factory;
    private boolean rerun = false;
    // wait iterval after db executions in seconds
    private int waitInterval = 2;

    public HistoryEventHandlerMaster(EventPath path, Class<? extends IEntry> clazz, SOSHibernateFactory hibernateFactory) {
        super(path, clazz);
        factory = hibernateFactory;
    }

    @Override
    public void run() {
        super.run();

        String method = "run";
        try {
            if (factory == null) {
                throw new Exception("factory is NULL");
            }
            setIdentifier(factory.getIdentifier() + "-" + getBaseUrl());

            start(new Long(0));
        } catch (Exception e) {
            LOGGER.error(String.format("%s %s", method, e.toString()), e);
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

    private Long execute(boolean onNonEmptyEvent, Long eventId, Event event) {
        String method = "execute";
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s onNonEmptyEvent=%s, eventId=%s", method, onNonEmptyEvent, eventId));
        }
        SOSHibernateSession session = null;
        Long newEventId = null;
        try {
            if (factory == null) {
                throw new Exception("factory is NULL");
            }
            session = factory.openStatelessSession();
            session.setIdentifier(getIdentifier());

            if (onNonEmptyEvent) {
                newEventId = event.getStampeds().get(event.getStampeds().size() - 1).getEventId();
            } else {
                newEventId = event.getLastEventId();
            }

        } catch (Throwable e) {
            rerun = true;
            LOGGER.error(String.format("%s %s", method, e.toString()), e);
        } finally {
            if (session != null) {
                session.close();
            }
            wait(waitInterval);
        }
        return newEventId;
    }

}
