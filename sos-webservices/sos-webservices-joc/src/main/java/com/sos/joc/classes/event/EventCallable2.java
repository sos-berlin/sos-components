package com.sos.joc.classes.event;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;

import org.apache.shiro.session.Session;

import com.sos.joc.model.event.JobSchedulerEvent;

public class EventCallable2 implements Callable<JobSchedulerEvent> {

    public final Session session;
    public final String controllerId;
    public final Long eventId;
    public final boolean isCurrentController;
    public final Condition eventArrived;
    public String accessToken;

    public EventCallable2(Session session, Long eventId, String controllerId, String accessToken, Condition eventArrived) {
        this.eventId = eventId;
        this.session = session;
        this.controllerId = controllerId;
        this.eventArrived = eventArrived;
        this.accessToken = accessToken;
        this.isCurrentController = false;
    }
    
    public EventCallable2(Session session, Long eventId, String controllerId, String accessToken, Condition eventArrived, boolean isCurrentController) {
        this.eventId = eventId;
        this.session = session;
        this.controllerId = controllerId;
        this.eventArrived = eventArrived;
        this.accessToken = accessToken;
        this.isCurrentController = isCurrentController;
    }

    @Override
    public JobSchedulerEvent call() throws Exception {
        return EventServiceFactory.getEvents(controllerId, eventId, accessToken, eventArrived, session, isCurrentController);
    }
}
