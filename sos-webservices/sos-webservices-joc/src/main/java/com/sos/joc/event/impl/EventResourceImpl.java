package com.sos.joc.event.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;

import javax.ws.rs.Path;

import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.event.EventCallable;
import com.sos.joc.classes.event.EventCallableOfCurrentController;
import com.sos.joc.classes.event.EventServiceFactory;
import com.sos.joc.event.resource.IEventResource;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.event.Controller;
import com.sos.joc.model.event.Event;
import com.sos.joc.model.event.Events;
import com.sos.joc.model.event.RegisterEvent;
import com.sos.schema.JsonValidator;

@Path("events")
public class EventResourceImpl extends JOCResourceImpl implements IEventResource {

    private static final String API_CALL = "./events";

    @Override
    public JOCDefaultResponse postEvent(String accessToken, byte[] inBytes) {

        Events entity = new Events();
        Map<String, Event> eventList = new HashMap<String, Event>();
        Session session = null;
        
        try {
            initLogging(API_CALL, inBytes, accessToken);
            // TODO register RegisterEvent at validator?
            JsonValidator.validateFailFast(inBytes, RegisterEvent.class);
            RegisterEvent in = Globals.objectMapper.readValue(inBytes, RegisterEvent.class);
            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getJS7Controller().getView().isStatus());

            if (response != null) {
                return response;
            }
            
            try {
                session = getJobschedulerUser().getSosShiroCurrentUser().getCurrentSubject().getSession(false);
            } catch (InvalidSessionException e1) {
                throw new SessionNotExistException(e1);
            }
            
            if (in.getControllers() == null && in.getControllers().size() == 0) {
                throw new JocMissingRequiredParameterException("undefined 'controllers'");
            }
            
            //Long defaultEventId = Instant.now().toEpochMilli() * 1000;
            long defaultEventId = Instant.now().getEpochSecond();
            List<EventCallable> tasks = new ArrayList<EventCallable>();
            
            Boolean isCurrentJobScheduler = true;
            Condition eventArrived = EventServiceFactory.createCondition();
            for (Controller controller : in.getControllers()) {
                Event evt = initEvent(controller, defaultEventId);
                eventList.put(controller.getControllerId(), evt);
                if (isCurrentJobScheduler) {
                    tasks.add(new EventCallableOfCurrentController(session, evt.getEventId(), evt.getControllerId(), accessToken, eventArrived));
                    isCurrentJobScheduler = false;
                } else {
                    tasks.add(new EventCallable(session, evt.getEventId(), evt.getControllerId(), accessToken, eventArrived));
                }
            }
            
            if (!tasks.isEmpty()) {
                ExecutorService executorService = Executors.newFixedThreadPool(tasks.size());
                try {
//                    JobSchedulerEvent evt = executorService.invokeAll(tasks);
//                    eventList.put(evt.getControllerId(), evt);
                    for (Future<Event> result : executorService.invokeAll(tasks)) {
                        try {
                            Event evt = result.get();
//                            evt.setEventSnapshots(evt.getEventSnapshots().stream().map(e -> {
//                                e.setEventId(null);
//                                return e;
//                            }).distinct().collect(Collectors.toList()));
                            eventList.put(evt.getControllerId(), evt);
                        } catch (ExecutionException e) {
                            if (e.getCause() instanceof JocException) {
                                throw (JocException) e.getCause();
                            } else {
                                throw (Exception) e.getCause();
                            }
                        }
                    }
                } catch (ExecutionException e) {
                    throw (Exception) e.getCause();
                } finally {
                    executorService.shutdown();
                }
            }

            entity.setEvents(new ArrayList<>(eventList.values()));
            entity.setDeliveryDate(Date.from(Instant.now()));
            
        } catch (JobSchedulerConnectionRefusedException e) {
            entity.setEvents(new ArrayList<>(eventList.values()));
            entity.setDeliveryDate(Date.from(Instant.now()));
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatus434JSError(e);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (InvalidSessionException e) {
            entity.setEvents(new ArrayList<>(eventList.values()));
            entity.setDeliveryDate(Date.from(Instant.now()));
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
        return JOCDefaultResponse.responseStatus200(entity);
    }
    
    private Event initEvent(Controller controller, long defaultEventId) {
        long eventId = defaultEventId;
        if (controller.getEventId() != null && controller.getEventId() > 0L) {
            eventId = controller.getEventId();
        }
        Event jsEvent = new Event();
        jsEvent.setEventId(eventId);
        jsEvent.setControllerId(controller.getControllerId());
        jsEvent.setEventSnapshots(Collections.emptyList());
        return jsEvent;
    }

}
