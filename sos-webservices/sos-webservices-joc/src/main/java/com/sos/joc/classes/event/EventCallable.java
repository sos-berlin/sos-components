package com.sos.joc.classes.event;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.json.JsonObject;

import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.event.impl.EventResourceImpl;
import com.sos.joc.exceptions.ForcedClosingHttpClientException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JobSchedulerNoResponseException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.common.JobSchedulerObjectType;
import com.sos.joc.model.event.EventSnapshot;
import com.sos.joc.model.event.JobSchedulerEvent;

public class EventCallable implements Callable<JobSchedulerEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventCallable.class);
    public final JobSchedulerEvent jobSchedulerEvent;
    public final JOCJsonCommand command;
    public final Session session;
    public final Long instanceId;
    private Long startTime = 0L;
    private SOSHibernateSession connection = null;

    public EventCallable() {
        this.command = null;
        this.jobSchedulerEvent = null;
        this.session = null;
        this.instanceId = null;
    }
    
    public EventCallable(JOCJsonCommand command, JobSchedulerEvent jobSchedulerEvent, Session session, Long instanceId) {
        this.command = command;
        this.jobSchedulerEvent = jobSchedulerEvent;
        this.session = session;
        this.instanceId = instanceId;
    }
    
    public EventCallable(JobSchedulerEvent jobSchedulerEvent, Session session) {
        this.command = null;
        this.jobSchedulerEvent = jobSchedulerEvent;
        this.session = session;
        this.instanceId = null;
    }
    
    public void setStartTime() {
        startTime = Instant.now().getEpochSecond(); 
    }

    @Override
    public JobSchedulerEvent call() throws Exception {
        try {
            setStartTime();
            List<EventSnapshot> evtSnapshots = getEventSnapshots(jobSchedulerEvent.getEventId());
            if (evtSnapshots.isEmpty()) {
                throw new ForcedClosingHttpClientException(command.getSchemeAndAuthority());
            }
            jobSchedulerEvent.getEventSnapshots().addAll(evtSnapshots);
            Globals.jobSchedulerIsRunning.put(command.getSchemeAndAuthority(), true);
        } catch (ForcedClosingHttpClientException e) {
            LOGGER.debug("Connection force close: " + command.getSchemeAndAuthority());
            jobSchedulerEvent.setEventSnapshots(null);
            handleError(e.getError().getCode(), e.getClass().getSimpleName());
            throw e;
        } catch (JobSchedulerNoResponseException | JobSchedulerConnectionRefusedException | JobSchedulerConnectionResetException e) {
            // TODO create JobScheduler unreachable event?
            jobSchedulerEvent.setEventSnapshots(null);
            handleError(e.getError().getCode(), e.getClass().getSimpleName());
            Boolean jobSchedulerIsRunning = Globals.jobSchedulerIsRunning.get(command.getSchemeAndAuthority());
            if (jobSchedulerIsRunning == null || jobSchedulerIsRunning) {
                LOGGER.warn(e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            Globals.jobSchedulerIsRunning.put(command.getSchemeAndAuthority(), false);
            throw e;
        } catch (SessionNotExistException e) {
            jobSchedulerEvent.setEventSnapshots(null);
            handleError("JOC-440", e.getClass().getSimpleName());
            throw e;
        } catch (JocException e) {
            jobSchedulerEvent.setEventSnapshots(null);
            handleError(e.getError().getCode(), e.getClass().getSimpleName(), e.getMessage());
            LOGGER.error(e.getClass().getSimpleName() + ": " + e.getMessage());
            throw e;
        } catch (Exception e) {
            jobSchedulerEvent.setEventSnapshots(null);
            handleError("JOC-420", e.getClass().getSimpleName(), e.getMessage());
            LOGGER.error("", e);
            throw e;
        } finally {
            if (command.getHttpClient() != null) {
                LOGGER.debug("Connection close: " + command.getSchemeAndAuthority());
            }
            command.closeHttpClient();
            Globals.disconnect(connection);
        }
        return jobSchedulerEvent;
    }

    private void handleError(String code, String simpleName) {
        handleError(code, simpleName, null);
    }

    private void handleError(String code, String simpleName, String msg) {
        Err err = new Err();
        err.setCode(code);
        String message = command.getSchemeAndAuthority();
        if (msg != null) {
            message += " - " + msg;
        }
        err.setMessage(simpleName + ": " + message);
        jobSchedulerEvent.setError(err);
    }

    protected JsonObject getJsonObject(Long eventId) throws JocException {
        return getJsonObject(eventId, EventResourceImpl.EVENT_TIMEOUT);
    }
    
    protected JsonObject getJsonObject(Long eventId, Integer evtTimeout) throws JocException {
        int timeout = Math.min(evtTimeout, getSessionTimeout() / 1000);
        command.replaceEventQuery(eventId, timeout);
//        JsonObjectBuilder builder = Json.createObjectBuilder();
//        builder.add("path", "/");
//        return command.getJsonObjectFromPost(builder.build().toString());
        return command.getJsonObjectFromGet();
    }
    
    protected void checkTimeout() throws ForcedClosingHttpClientException {
        Long curTime = Instant.now().getEpochSecond();
        if (curTime - startTime > 6 * 60) {  // general timeout 6min
            String msg = command != null ? command.getSchemeAndAuthority() : jobSchedulerEvent.getControllerId();
            throw new ForcedClosingHttpClientException(msg);
        }
    }

    protected List<EventSnapshot> getEventSnapshots(Long eventId) throws JocException {
        List<EventSnapshot> eventSnapshots = new ArrayList<EventSnapshot>();
        checkTimeout();
        try {
            JsonObject json = getJsonObject(eventId);
            Long newEventId = 0L;
            String type = json.getString("TYPE", "Empty");
            switch (type) {
            case "Empty":
                newEventId = json.getJsonNumber("lastEventId").longValue();
                jobSchedulerEvent.setEventId(newEventId);
                eventSnapshots.addAll(getEventSnapshots(newEventId));
                break;
            case "NonEmpty":
                eventSnapshots.addAll(nonEmptyEvent(json));
                newEventId = jobSchedulerEvent.getEventId();
                if (eventSnapshots.isEmpty()) {
                    eventSnapshots.addAll(getEventSnapshots(newEventId)); 
                } else {
                    try { //collect further events after 2sec to minimize the number of responses 
                        int delay = Math.min(2000, getSessionTimeout());
                        if (delay > 0) {
                            Thread.sleep(delay);
                        }
                    } catch (InterruptedException e1) {
                    }
                    eventSnapshots.addAll(getEventSnapshotsFromNextResponse(newEventId));
                }
                break;
            case "Torn":
                newEventId = json.getJsonNumber("after").longValue();
                jobSchedulerEvent.setEventId(newEventId);
                eventSnapshots.addAll(getEventSnapshots(newEventId));
                break;
            }
        } catch (JobSchedulerNoResponseException | JobSchedulerConnectionRefusedException e) {
            // if current Jobscheduler down then retry after 15sec
            try {
                int delay = Math.min(15000, getSessionTimeout());
                LOGGER.debug(command.getSchemeAndAuthority() + ": connection refused: retry after " + delay + "ms");
                while (delay > 0) {
                    Thread.sleep(1000);
                    delay = delay - 1000;
                    if (command.isForcedClosingHttpClient()) {
                        throw new ForcedClosingHttpClientException(command.getSchemeAndAuthority());
                    }
                }
                if (command.isForcedClosingHttpClient()) {
                    throw new ForcedClosingHttpClientException(command.getSchemeAndAuthority());
                }
            } catch (InterruptedException e1) {
            }
            eventSnapshots.addAll(getEventSnapshots(eventId));
        } catch (JobSchedulerConnectionResetException e) {
            EventSnapshot eventSnapshot = new EventSnapshot();
            eventSnapshot.setEventType("SchedulerStateChanged");
            eventSnapshot.setObjectType(JobSchedulerObjectType.CONTROLLER);
            eventSnapshot.setPath(command.getSchemeAndAuthority());
            eventSnapshots.add(eventSnapshot);
        }
        return eventSnapshots;
    }

    protected int getSessionTimeout() throws SessionNotExistException {
        try {
            if (session == null) {
                throw new SessionNotExistException("session is invalid");
            }
            long l = session.getTimeout()-1000;
            if (l < 0) {
                return 0;
            }
            if (l >= Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            return new Long(l).intValue();
        } catch (SessionNotExistException e) {
            throw e;
        } catch (InvalidSessionException e) {
            throw new SessionNotExistException(e);
        }
    }
    
    public boolean isOrderFinishedWithError(String jobChain, String node) {
    	return false;
//        InventoryJobChainsDBLayer jobChainsLayer = null;
//        try {
//            if (jobChain != null && node != null && instanceId != null) {
//                if (connection == null) {
//                    connection = Globals.createSosHibernateStatelessConnection("eventCallable-"+jobSchedulerEvent.getJobschedulerId());
//                    Globals.beginTransaction(connection);
//                }
//                jobChainsLayer = new InventoryJobChainsDBLayer(connection);
//                if (jobChainsLayer!= null) {
//                    return jobChainsLayer.isErrorNode(jobChain, node, instanceId);
//                }
//            }
//            return false;
//        } catch (Exception e) {
//            return false;
//        } finally {
//            Globals.disconnect(connection);
//            connection = null;
//        }
    }
    
    private List<EventSnapshot> getEventSnapshotsFromNextResponse(Long eventId) throws JocException {
        List<EventSnapshot> eventSnapshots = new ArrayList<EventSnapshot>();
        JsonObject json = getJsonObject(eventId, 0);
        String type = json.getString("TYPE", "Empty");
        switch (type) {
        case "Empty":
            break;
        case "NonEmpty":
            eventSnapshots.addAll(nonEmptyEvent(json));
            break;
        case "Torn":
            break;
        }
        return eventSnapshots;
    }

    private List<EventSnapshot> nonEmptyEvent(JsonObject json) {
        List<EventSnapshot> eventSnapshots = new ArrayList<EventSnapshot>();
        // boolean isOrderStarted = false;
        for (JsonObject event : json.getJsonArray("stamped").getValuesAs(JsonObject.class)) {
            EventSnapshot eventSnapshot = new EventSnapshot();
            Long eId = event.getJsonNumber("eventId").longValue();
            eventSnapshot.setEventId(eId);
            jobSchedulerEvent.setEventId(eId);
            String eventType = event.getString("TYPE", null);
            eventSnapshot.setEventType(eventType);
            if (eventType.startsWith("File")) {
                continue;
            }
            // TODO kann weg??
            if ("VariablesCustomEvent".equalsIgnoreCase(eventType)) {
                continue;
            }
            if (eventType.startsWith("Task")) {
                continue;
            }
            String eventKey = event.getString("key", null);
            eventSnapshot.setPath(eventKey);
            if (eventType.startsWith("JobState")) {
                eventSnapshot.setObjectType(JobSchedulerObjectType.JOB);
                eventSnapshot.setState(event.getString("state", null));
                if ("initialized,loaded,closed".contains(eventSnapshot.getState())) {
                    continue;
                }
                // TODO analogon for workflow??
                // } else if (eventType.startsWith("JobChainState")) {
                // eventSnapshot.setObjectType(JobSchedulerObjectType.JOBCHAIN);
                // eventSnapshot.setState(event.getString("state", null));
                // if ("initialized,loaded,closed".contains(eventSnapshot.getState())) {
                // continue;
                // }
            } else if (eventType.startsWith("Order")) {
                // TODO reflect new ORDER events
                eventSnapshot.setObjectType(JobSchedulerObjectType.ORDER);
//                switch (eventType) {
//                case "OrderNodeChanged":
//                    eventSnapshot.setNodeId(event.getString("nodeId", null));
//                    eventSnapshot.setFromNodeId(event.getString("fromNodeId", null));
//                    break;
//                case "OrderStepStarted":
//                    eventSnapshot.setNodeId(event.getString("nodeId", null));
//                    eventSnapshot.setTaskId(event.getJsonNumber("taskId").longValue());
//                    break;
//                case "OrderFinished":
//                    String node = event.getString("nodeId", null);
//                    eventSnapshot.setNodeId(node);
//                    eventSnapshot.setState("successful");
//                    String[] pathParts = eventSnapshot.getPath().split(",", 2);
//                    String jobChain = pathParts[0];
//                    if (isOrderFinishedWithError(jobChain, node)) {
//                        eventSnapshot.setState("failed");
//                    }
//                case "OrderStepEnded":
//                    JsonObject nodeTrans = event.getJsonObject("nodeTransition");
//                    if (nodeTrans != null) {
//                        NodeTransition nodeTransition = new NodeTransition();
//                        try {
//                            nodeTransition.setType(NodeTransitionType.fromValue(nodeTrans.getString("TYPE", "SUCCESS").toUpperCase()));
//                        } catch (IllegalArgumentException e) {
//                            // LOGGER.warn("unknown event transition type", e);
//                            nodeTransition.setType(NodeTransitionType.SUCCESS);
//                        }
//                        nodeTransition.setReturnCode(nodeTrans.getInt("returnCode", 0));
//                        eventSnapshot.setNodeTransition(nodeTransition);
//                    }
//                    break;
//                }

            } else if (eventType.startsWith("Controller")) {
                eventSnapshot.setEventType("SchedulerStateChanged");
                eventSnapshot.setObjectType(JobSchedulerObjectType.CONTROLLER);
                eventSnapshot.setPath(command.getSchemeAndAuthority());
            }

            eventSnapshots.add(eventSnapshot);
        }
        try {
            json.clear();
        } catch (Exception e) {
        } finally {
            json = null;
        }
        return eventSnapshots;
    }
}
