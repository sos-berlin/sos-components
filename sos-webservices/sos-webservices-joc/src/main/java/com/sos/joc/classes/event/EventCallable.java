package com.sos.joc.classes.event;

import java.util.concurrent.Callable;

import org.apache.shiro.session.Session;

import com.sos.joc.classes.event.EventServiceFactory.EventCondition;
import com.sos.joc.model.event.Event;

public class EventCallable implements Callable<Event> {

    public final Session session;
    public final String controllerId;
    public final Long eventId;
    public final boolean isCurrentController;
    public final EventCondition eventArrived;
    public String accessToken;

    public EventCallable(Session session, Long eventId, String controllerId, String accessToken, EventCondition eventArrived) {
        this.eventId = eventId;
        this.session = session;
        this.controllerId = controllerId;
        this.eventArrived = eventArrived;
        this.accessToken = accessToken;
        this.isCurrentController = false;
    }
    
    public EventCallable(Session session, Long eventId, String controllerId, String accessToken, EventCondition eventArrived, boolean isCurrentController) {
        this.eventId = eventId;
        this.session = session;
        this.controllerId = controllerId;
        this.eventArrived = eventArrived;
        this.accessToken = accessToken;
        this.isCurrentController = isCurrentController;
    }

    @Override
    public Event call() throws Exception {
        return EventServiceFactory.getEvents(controllerId, eventId, accessToken, eventArrived, session, isCurrentController);
    }
}
