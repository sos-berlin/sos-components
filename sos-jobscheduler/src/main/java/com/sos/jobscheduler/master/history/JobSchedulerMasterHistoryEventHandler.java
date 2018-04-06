package com.sos.jobscheduler.master.history;

import javax.json.JsonArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.master.event.JobSchedulerEvent.EventType;
import com.sos.jobscheduler.master.event.JobSchedulerUnlimitedEventHandler;

public class JobSchedulerMasterHistoryEventHandler extends JobSchedulerUnlimitedEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerMasterHistoryEventHandler.class);

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

            EventType[] observedEventTypes = new EventType[] { EventType.TaskStarted, EventType.TaskClosed, EventType.OrderStepStarted,
                    EventType.OrderStepEnded, EventType.OrderFinished, EventType.OrderWaitingInTask };
            start(observedEventTypes);
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
            String method = "onEmptyEvent";
            LOGGER.debug(String.format("%s eventId=%s", method, eventId));

            execute(false, eventId, null);
        }
    }

    @Override
    public void onNonEmptyEvent(Long eventId, JsonArray events) {
        String method = "onNonEmptyEvent";
        LOGGER.debug(String.format("%s eventId=%s", method, eventId));

        rerun = false;
        execute(true, eventId, events);
    }

    private void execute(boolean onNonEmptyEvent, Long eventId, JsonArray events) {
        String method = "execute";
        LOGGER.debug(String.format("%s onNonEmptyEvent=%s, eventId=%s", method, onNonEmptyEvent, eventId));

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
