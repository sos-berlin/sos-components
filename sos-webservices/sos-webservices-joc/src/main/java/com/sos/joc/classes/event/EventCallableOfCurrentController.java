package com.sos.joc.classes.event;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;

import org.apache.shiro.session.Session;

import com.sos.joc.model.event.Event;

public class EventCallableOfCurrentController extends EventCallable implements Callable<Event> {

    public EventCallableOfCurrentController(Session session, Long eventId, String controllerId, String accessToken, Condition eventArrived) {
        super(session, eventId, controllerId, accessToken, eventArrived, true);
    }
}
