package com.sos.joc.classes.event;

import java.util.concurrent.Callable;

import org.apache.shiro.session.Session;

import com.sos.joc.model.event.JobSchedulerEvent;

public class EventCallable2OfCurrentController extends EventCallable2 implements Callable<JobSchedulerEvent> {

    public EventCallable2OfCurrentController(Session session, Long eventId, String controllerId) {
        super(session, eventId, controllerId, true);
    }
}
