package com.sos.joc.event.impl;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Path;

import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.rest.SOSShiroCurrentUser;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.event.EventCallable;
import com.sos.joc.classes.event.EventCallableOfCurrentCluster;
import com.sos.joc.classes.event.EventCallableOfCurrentJobScheduler;
import com.sos.joc.classes.event.EventCallablePassiveJobSchedulerStateChanged;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.event.resource.IEventResource;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.ForcedClosingHttpClientException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.event.EventSnapshot;
import com.sos.joc.model.event.JobSchedulerEvent;
import com.sos.joc.model.event.JobSchedulerEvents;
import com.sos.joc.model.event.JobSchedulerObjects;
import com.sos.joc.model.event.RegisterEvent;

@Path("events")
public class EventResourceImpl extends JOCResourceImpl implements IEventResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventResourceImpl.class);
    private static final String API_CALL = "./events";
    private static final String SESSION_KEY = "EventsStarted";
    private String threadName = "";
    private String urlOfCurrentJs = null;
    public static final Integer EVENT_TIMEOUT = 60;

    @Override
    public JOCDefaultResponse postEvent(String accessToken, RegisterEvent eventBody) {

        JobSchedulerEvents entity = new JobSchedulerEvents();
        Map<String, JobSchedulerEvent> eventList = new HashMap<String, JobSchedulerEvent>();
        Session session = null;
        threadName = Thread.currentThread().getName();

        SOSHibernateSession connection = null;

        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, eventBody, accessToken, "", getPermissonsJocCockpit("", accessToken)
                    .getJS7Controller().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            SOSShiroCurrentUser shiroUser = null;
            connection = Globals.createSosHibernateStatelessConnection("postEvent");
            try {
                shiroUser = getJobschedulerUser().getSosShiroCurrentUser();
                session = shiroUser.getCurrentSubject().getSession(false);
                if (session != null) {
                    session.setAttribute(SESSION_KEY, threadName);
                    session.setAttribute(Globals.SESSION_KEY_FOR_SEND_EVENTS_IMMEDIATLY, false);
                }
            } catch (InvalidSessionException e1) {
                throw new SessionNotExistException(e1);
            }
            // Not a good idea: Same session in multiple tabs closed http
            // clients vice versa
            // Globals.forceClosingHttpClients(session);

            if (eventBody.getClose() != null && eventBody.getClose()) {
                Globals.forceClosingHttpClients(shiroUser, accessToken);
                entity.setEvents(null);
                entity.setDeliveryDate(Date.from(Instant.now()));
                return JOCDefaultResponse.responseStatus200(entity);
            }
            
//          TODO temporary with dummy answer
//            Long defaultEventId = Instant.now().toEpochMilli() * 1000;
//            List<JobSchedulerEvent> evts = new ArrayList<JobSchedulerEvent>();
//            for (JobSchedulerObjects jsObject : eventBody.getJobscheduler()) {
//            	evts.add(initEvent(jsObject, defaultEventId));
//            }
//            Thread.sleep(1000*60*6);  //6 Minutes delay like an empty answer
//            final String nextEventId = Instant.now().toEpochMilli() * 1000 + "";
//            evts.stream().map(e -> {
//            	e.setEventId(nextEventId);
//            	return e;
//            }).collect(Collectors.toList());
//            entity.setEvents(evts);
            

            if (eventBody.getControllers() == null && eventBody.getControllers().size() == 0) {
                throw new JocMissingRequiredParameterException("undefined 'jobscheduler'");
            }

            Long defaultEventId = Instant.now().toEpochMilli() * 1000;
            List<EventCallable> tasks = new ArrayList<EventCallable>();
            Set<JOCJsonCommand> jocJsonCommands = new HashSet<JOCJsonCommand>();

            InventoryInstancesDBLayer instanceLayer = new InventoryInstancesDBLayer(connection);

            Boolean isCurrentJobScheduler = true;
            for (JobSchedulerObjects jsObject : eventBody.getControllers()) {
                JobSchedulerEvent jsEvent = initEvent(jsObject, defaultEventId);
                eventList.put(jsObject.getControllerId(), jsEvent);
                DBItemInventoryJSInstance instance = getJobSchedulerInstance(jsObject, instanceLayer, accessToken);
                JOCJsonCommand command = initJocJsonCommand(jsEvent, instance);
                jocJsonCommands.add(command);
                List<JOCJsonCommand> jocJsonCommandsOfClusterMember = new ArrayList<JOCJsonCommand>();
                List<EventCallable> tasksOfClusterMember = new ArrayList<EventCallable>();

                if (isCurrentJobScheduler) {
                    tasksOfClusterMember.add(new EventCallableOfCurrentJobScheduler(command, jsEvent, session, instance.getId(),
                            shiroUser));
                } else {
                    tasksOfClusterMember.add(new EventCallable(command, jsEvent, session, instance.getId()));
                }
                
                List<DBItemInventoryJSInstance> jobSchedulerMembers = null;
                
                if (instance.getIsCluster()) {
                    jobSchedulerMembers = instanceLayer.getInventoryInstancesByControllerId(jsObject.getControllerId());
                    if (jobSchedulerMembers != null) {
                        for (DBItemInventoryJSInstance jobSchedulerMember : jobSchedulerMembers) {
                            if (jobSchedulerMember == null || jobSchedulerMember.equals(instance)) {
                                continue;
                            }
                            JobSchedulerEvent jsEventOfMember = initEvent(jsObject, session, jobSchedulerMember.getId(), defaultEventId);
                            JOCJsonCommand commandOfMember = initJocJsonCommand(jsEventOfMember, jobSchedulerMember, "ControllerEvent,ClusterEvent");
                            jocJsonCommandsOfClusterMember.add(commandOfMember);
                            tasksOfClusterMember.add(new EventCallablePassiveJobSchedulerStateChanged(commandOfMember, jsEventOfMember, session,
                                    jobSchedulerMember.getId()));
                        }
                    }
                }
                
                if (tasksOfClusterMember.size() == 1) {
                    tasks.add(tasksOfClusterMember.get(0));
                } else {
                    tasks.add(new EventCallableOfCurrentCluster(command, jsEvent, accessToken, session, instance.getId(), shiroUser,
                            tasksOfClusterMember, jocJsonCommandsOfClusterMember, isCurrentJobScheduler));
                }
                isCurrentJobScheduler = false;
                if (urlOfCurrentJs == null) {
                    urlOfCurrentJs = instance.getUri();
                }
                jocJsonCommands.addAll(jocJsonCommandsOfClusterMember);
            }
            Globals.disconnect(connection);
            try {
                jobschedulerUser.setJocJsonCommands(jocJsonCommands);
            } catch (Exception e1) {
            }
            if (!tasks.isEmpty()) {
                ExecutorService executorService = Executors.newFixedThreadPool(tasks.size());
                try {
                    JobSchedulerEvent evt = executorService.invokeAny(tasks);
                    eventList.put(evt.getControllerId(), evt);
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof JocException) {
                        throw (JocException) e.getCause();
                    } else {
                        throw (Exception) e.getCause();
                    }
                } finally {
                    // Globals.forceClosingHttpClients(session);
                    for (JOCJsonCommand command : jocJsonCommands) {
                        command.forcedClosingHttpClient();
                    }
                    executorService.shutdown();
                    if (!executorService.awaitTermination(300, TimeUnit.MILLISECONDS)) {
                        for (JOCJsonCommand command : jocJsonCommands) {
                            command.forcedClosingHttpClient();
                        }
                    }
                }
            }

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
            Globals.disconnect(connection);
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

    private JobSchedulerEvent initEvent(JobSchedulerObjects jsObject, Session session, Long instanceId, Long defaultEventId) {
        Long eventId = defaultEventId;
        String jsId = jsObject.getControllerId();

        if (jsObject.getEventId() != null && jsObject.getEventId() > 0L) {
            eventId = jsObject.getEventId();
        }
        if (session != null) {
            Long eventIdOfClusterMembers = (Long) session.getAttribute(jsId + "#eventIdOfClusterMembers");
            if (eventIdOfClusterMembers != null) {
                eventId = eventIdOfClusterMembers;
            }
        }
        if (instanceId != null) {
            jsId = "__instance__" + instanceId;
        }
        LOGGER.debug("EventId of " + jsId + ": " + eventId);
        JobSchedulerEvent jsEvent = new JobSchedulerEvent();
        jsEvent.setEventId(eventId);
        jsEvent.setControllerId(jsId);
        jsEvent.setEventSnapshots(new ArrayList<EventSnapshot>());
        return jsEvent;
    }

    private JobSchedulerEvent initEvent(JobSchedulerObjects jsObject, Long defaultEventId) {
        return initEvent(jsObject, null, null, defaultEventId);
    }

    private JOCJsonCommand initJocJsonCommand(JobSchedulerEvent jsEvent, DBItemInventoryJSInstance instance) {
        return initJocJsonCommand(jsEvent, instance, null);
    }

    private JOCJsonCommand initJocJsonCommand(JobSchedulerEvent jsEvent, DBItemInventoryJSInstance instance, String event) {
        JOCJsonCommand command = new JOCJsonCommand(instance, getAccessToken());
        command.setUriBuilderForEvents();
        //command.setBasicAuthorization(instance.getAuth());
        command.setSocketTimeout((EVENT_TIMEOUT + 5) * 1000);
        command.createHttpClient();
        command.setAutoCloseHttpClient(false);
        command.addEventQuery(jsEvent.getEventId(), EVENT_TIMEOUT, event);
        return command;
    }

    private DBItemInventoryJSInstance getJobSchedulerInstance(JobSchedulerObjects jsObject, InventoryInstancesDBLayer instanceLayer, String accessToken)
            throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {
        DBItemInventoryJSInstance instance = Globals.urlFromJobSchedulerId.get(jsObject.getControllerId());
        if (instance == null) {
            instance = instanceLayer.getInventoryInstanceByControllerId(jsObject.getControllerId(), accessToken);
            Globals.urlFromJobSchedulerId.put(jsObject.getControllerId(), instance);
        }
        return instance;
    }

}
