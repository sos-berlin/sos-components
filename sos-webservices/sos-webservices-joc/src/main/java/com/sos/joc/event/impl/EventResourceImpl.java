package com.sos.joc.event.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.event.EventCallable;
import com.sos.joc.classes.event.EventCallableOfCurrentController;
import com.sos.joc.classes.event.EventServiceFactory;
import com.sos.joc.classes.event.EventServiceFactory.EventCondition;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.event.resource.IEventResource;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.event.Controller;
import com.sos.joc.model.event.Event;
import com.sos.joc.model.event.EventSnapshot;
import com.sos.joc.model.event.EventType;
import com.sos.joc.model.event.Events;
import com.sos.joc.model.event.RegisterEvent;
import com.sos.schema.JsonValidator;

@Path("events")
public class EventResourceImpl extends JOCResourceImpl implements IEventResource {

    private static final String API_CALL = "./events";
    private static final List<EventType> eventTypesforNameToPathMapping = Arrays.asList(EventType.LOCK, EventType.WORKFLOW);
    

    @Override
    public JOCDefaultResponse postEvent(String accessToken, byte[] inBytes) {

        Events entity = new Events();
        Map<String, Event> eventList = new HashMap<String, Event>();
        Session session = null;
        SOSHibernateSession connection = null;
        
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
            
            DeployedConfigurationDBLayer dbCLayer = new DeployedConfigurationDBLayer(connection);
            
            //Long defaultEventId = Instant.now().toEpochMilli() * 1000;
            long defaultEventId = Instant.now().getEpochSecond();
            List<EventCallable> tasks = new ArrayList<EventCallable>();
            
            String currentControllerId = in.getControllers().get(0).getControllerId();
            EventCondition eventArrived = EventServiceFactory.createCondition();
            for (Controller controller : in.getControllers()) {
                Event evt = initEvent(controller, defaultEventId);
                eventList.put(controller.getControllerId(), evt);
                if (currentControllerId.equals(controller.getControllerId())) {
                    tasks.add(new EventCallableOfCurrentController(session, evt.getEventId(), evt.getControllerId(), accessToken, eventArrived));
                } else {
                    tasks.add(new EventCallable(session, evt.getEventId(), evt.getControllerId(), accessToken, eventArrived));
                }
            }
            
            if (!tasks.isEmpty()) {
                ExecutorService executorService = Executors.newFixedThreadPool(tasks.size());
                try {
                    for (Future<Event> result : executorService.invokeAll(tasks)) {
                        try {
                            Event evt = processAfter(result.get(), currentControllerId, dbCLayer);
                            eventList.put(evt.getControllerId(), evt);
                        } catch (Exception e) {
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
        } finally {
            Globals.disconnect(connection);
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
    
    private Event processAfter(Event evt, String currentControllerId, DeployedConfigurationDBLayer dbCLayer) {
        if (!currentControllerId.equals(evt.getControllerId())) {
            evt.setEventSnapshots(Collections.emptyList());
        }
        // map workflow name to workflow path etc.
        Map<EventType, Set<String>> m = evt.getEventSnapshots().stream().filter(e -> eventTypesforNameToPathMapping.contains(e.getObjectType())).map(
                e -> {
                    if (e.getWorkflow() != null) {
                        e.setPath(e.getWorkflow().getPath());
                    }
                    return e;
                }).filter(e -> e.getPath() != null).collect(Collectors.groupingBy(EventSnapshot::getObjectType, Collectors.mapping(
                        EventSnapshot::getPath, Collectors.toSet())));
        Map<String, String> namePathWorkflowMap;
        Map<String, String> namePathLockMap;
        try {
            namePathWorkflowMap = dbCLayer.getNamePathMapping(evt.getControllerId(), m.get(EventType.WORKFLOW), DeployType.WORKFLOW
                    .intValue());
        } catch (SOSHibernateException e1) {
            namePathWorkflowMap = Collections.emptyMap();
        }
        try {
            namePathLockMap = dbCLayer.getNamePathMapping(evt.getControllerId(), m.get(EventType.LOCK), DeployType.LOCK.intValue());
        } catch (SOSHibernateException e1) {
            namePathLockMap = Collections.emptyMap();
        }
        for (EventSnapshot e : evt.getEventSnapshots()) {
            if (e.getWorkflow() != null) {
                String name = e.getWorkflow().getPath();
                if (name != null) {
                    e.getWorkflow().setPath(namePathWorkflowMap.getOrDefault(name, name));
                }
            } else if (EventType.WORKFLOW.equals(e.getObjectType()) && e.getPath() != null) {
                e.setPath(namePathWorkflowMap.getOrDefault(e.getPath(), e.getPath()));
            } else if (EventType.LOCK.equals(e.getObjectType()) && e.getPath() != null) {
                e.setPath(namePathLockMap.getOrDefault(e.getPath(), e.getPath()));
            }
        }
        return evt;
    }

}
