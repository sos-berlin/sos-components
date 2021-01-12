package com.sos.joc.classes.event;

import java.util.concurrent.Callable;

import org.apache.shiro.session.Session;

import com.sos.joc.classes.event.EventServiceFactory.EventCondition;
import com.sos.joc.model.event.Event;

public class EventCallableOfCurrentController extends EventCallable implements Callable<Event> {

    public EventCallableOfCurrentController(Session session, Long eventId, String controllerId, String accessToken, EventCondition eventArrived) {
        super(session, eventId, controllerId, accessToken, eventArrived, true);
    }
}
