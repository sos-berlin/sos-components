package com.sos.joc.event.impl;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.ws.rs.Path;

import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.rest.SOSShiroCurrentUser;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.event.Events;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.event.EventServiceStart;
import com.sos.joc.event.bean.event.EventServiceStop;
import com.sos.joc.event.resource.IEventResource2;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.ForcedClosingHttpClientException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.common.JobSchedulerObjectType;
import com.sos.joc.model.event.EventSnapshot;
import com.sos.joc.model.event.JobSchedulerEvent;
import com.sos.joc.model.event.JobSchedulerEvents;
import com.sos.joc.model.event.JobSchedulerObjects;
import com.sos.joc.model.event.RegisterEvent;
import com.sos.schema.JsonValidator;

import js7.controller.data.events.ControllerEvent;
import js7.controller.data.events.ControllerEvent.ControllerReady;
import js7.data.cluster.ClusterEvent;
import js7.data.event.Event;
import js7.data.event.KeyedEvent;
import js7.data.event.Stamped;
import js7.proxy.javaapi.data.controller.JControllerState;

@Path("events2")
public class EventResourceImpl2 extends JOCResourceImpl implements IEventResource2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventResourceImpl2.class);
    private static final String API_CALL = "./events";
    private static final String SESSION_KEY = "EventsStarted";
    private String threadName = "";
    private String urlOfCurrentJs = null;
    public static final Integer EVENT_TIMEOUT = 60;
    private static List<Class<? extends Event>> eventsOfCurrentController = Arrays.asList(ControllerEvent.class, ClusterEvent.class);

    @Override
    public JOCDefaultResponse postEvent(String accessToken, byte[] inBytes) {

        JobSchedulerEvents entity = new JobSchedulerEvents();
        Map<String, JobSchedulerEvent> eventList = new HashMap<String, JobSchedulerEvent>();
        Session session = null;
        threadName = Thread.currentThread().getName();
        
        
        try {
            initLogging(API_CALL, inBytes, accessToken);
            // TODO register RegisterEvent at validator?
            JsonValidator.validateFailFast(inBytes, RegisterEvent.class);
            RegisterEvent in = Globals.objectMapper.readValue(inBytes, RegisterEvent.class);
            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getJS7Controller().getView().isStatus());

            if (response != null) {
                return response;
            }
            SOSShiroCurrentUser shiroUser = null;
            try {
                shiroUser = getJobschedulerUser().getSosShiroCurrentUser();
                session = shiroUser.getCurrentSubject().getSession(false);
                if (session != null) {
                    session.setAttribute(SESSION_KEY, threadName);
                    session.setAttribute(Globals.SESSION_KEY_FOR_SEND_EVENTS_IMMEDIATLY, false);
                }
            } catch (InvalidSessionException e1) {
                EventBus.getInstance().post(new EventServiceStop(accessToken, "", Collections.emptyMap()));
                throw new SessionNotExistException(e1);
            }
            
            EventBus.getInstance().post(new EventServiceStart(accessToken, "", Collections.emptyMap()));

            Long defaultEventId = Instant.now().toEpochMilli() * 1000;
            Events eventSnapshots = new Events();
            
            BiConsumer<Stamped<KeyedEvent<Event>>, JControllerState> callbackOfCurrentController = (stampedEvt, state) -> {
                KeyedEvent<Event> event = stampedEvt.value();
                Event evt = event.event();
                EventSnapshot eventSnapshot = new EventSnapshot();
                
                if (evt instanceof ControllerEvent || evt instanceof ClusterEvent) {
                    eventSnapshot.setEventType("SchedulerStateChanged");
                    eventSnapshot.setObjectType(JobSchedulerObjectType.CONTROLLER);
                    eventSnapshot.setPath(in.getControllers().get(0).getControllerId());
                    eventSnapshots.add(eventSnapshot);
                }
            };


            Boolean isCurrentJobScheduler = true;
            for (JobSchedulerObjects jsObject : in.getControllers()) {

                if (isCurrentJobScheduler) {
                    
                    Proxy.of(jsObject.getControllerId()).controllerEventBus().subscribe(eventsOfCurrentController, callbackOfCurrentController);
                    // first step:  only events of current Controller
                    break;
                } else {
                    
                }
                
                isCurrentJobScheduler = false;
//                if (urlOfCurrentJs == null) {
//                    urlOfCurrentJs = instance.getUri();
//                }
                
            }
//            try {
//                jobschedulerUser.setJocJsonCommands(jocJsonCommands);
//            } catch (Exception e1) {
//            }

            entity.setEvents(new ArrayList<JobSchedulerEvent>(eventList.values()));
            entity.setDeliveryDate(Date.from(Instant.now()));
            
            
        } catch (DBConnectionRefusedException e) {
        	LOGGER.info(e.getMessage());
        } catch (ForcedClosingHttpClientException e) {
            entity.setEvents(new ArrayList<JobSchedulerEvent>(eventList.values()));
            entity.setDeliveryDate(Date.from(Instant.now()));
        } catch (JobSchedulerConnectionRefusedException e) {
            entity.setEvents(new ArrayList<JobSchedulerEvent>(eventList.values()));
            if (!concurrentEventsCallIsStarted(session)) {
                try {
                    e = new JobSchedulerConnectionRefusedException(urlOfCurrentJs);
                } catch (Exception e1) {
                }
                e.addErrorMetaInfo(getJocError());
                return JOCDefaultResponse.responseStatus434JSError(e);
            } else {
                LOGGER.warn("./events concurrent call was started");
                entity.setDeliveryDate(Date.from(Instant.now()));
            }
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (InvalidSessionException e) {
            entity.setEvents(new ArrayList<JobSchedulerEvent>(eventList.values()));
            entity.setDeliveryDate(Date.from(Instant.now()));
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            LOGGER.debug("./events ended");
        }
        return JOCDefaultResponse.responseStatus200(entity);
    }
    
    

    private boolean concurrentEventsCallIsStarted(Session session) {
        boolean concurrentEventsCallIsStarted = false;
        String sessionUuid = null;
        if (session != null) {
            try {
                sessionUuid = (String) session.getAttribute(SESSION_KEY);
            } catch (Exception ee) {
            }
        }
        if (sessionUuid != null && !sessionUuid.equals(threadName)) {
            concurrentEventsCallIsStarted = true;
        }
        return concurrentEventsCallIsStarted;
    }

}
