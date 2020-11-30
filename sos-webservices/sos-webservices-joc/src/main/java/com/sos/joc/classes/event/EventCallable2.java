package com.sos.joc.classes.event;

import java.util.concurrent.Callable;

import org.apache.shiro.session.Session;

import com.sos.joc.model.event.JobSchedulerEvent;

public class EventCallable2 implements Callable<JobSchedulerEvent> {

    public final Session session;
    public final String controllerId;
    public final Long eventId;

    public EventCallable2(Session session, Long eventId, String controllerId) {
        this.eventId = eventId;
        this.session = session;
        this.controllerId = controllerId;
    }

    @Override
    public JobSchedulerEvent call() throws Exception {
        return EventServiceFactory.getEvents(controllerId, eventId, session);
    }
}
