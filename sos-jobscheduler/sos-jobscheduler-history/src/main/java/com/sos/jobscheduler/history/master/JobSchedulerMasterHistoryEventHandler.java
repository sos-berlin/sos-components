package com.sos.jobscheduler.history.master;

import javax.json.JsonArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.event.master.JobSchedulerEvent.EventPath;
import com.sos.jobscheduler.event.master.JobSchedulerUnlimitedEventHandler;

public class JobSchedulerMasterHistoryEventHandler extends JobSchedulerUnlimitedEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerMasterHistoryEventHandler.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private SOSHibernateFactory factory;
    private boolean rerun = false;
    // wait iterval after db executions in seconds
    private int waitInterval = 2;

    public JobSchedulerMasterHistoryEventHandler(SOSHibernateFactory hibernateFactory) {
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

            start(EventPath.fatEvent, new Long(0));
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
    public void onNonEmptyEvent(Long eventId, JsonArray events) {
        if (isDebugEnabled) {
            String method = "onNonEmptyEvent";
            LOGGER.debug(String.format("%s eventId=%s", method, eventId));
        }
        rerun = false;
        execute(true, eventId, events);
    }

    private void execute(boolean onNonEmptyEvent, Long eventId, JsonArray events) {
        String method = "execute";
        if (isDebugEnabled) {
        LOGGER.debug(String.format("%s onNonEmptyEvent=%s, eventId=%s", method, onNonEmptyEvent, eventId));
        }
        SOSHibernateSession session = null;
        try {
            if (factory == null) {
                throw new Exception("factory is NULL");
            }
            session = factory.openStatelessSession();
            session.setIdentifier(getIdentifier());

        } catch (Throwable e) {
            rerun = true;
            LOGGER.error(String.format("%s %s", method, e.toString()), e);
        } finally {
            if (session != null) {
                session.close();
            }
            wait(waitInterval);
        }
    }

}
