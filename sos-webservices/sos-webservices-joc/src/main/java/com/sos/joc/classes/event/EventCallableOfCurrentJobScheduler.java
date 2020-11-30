package com.sos.joc.classes.event;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.json.JsonObject;

import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.rest.SOSShiroCurrentUser;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.db.audit.AuditLogDBFilter;
import com.sos.joc.db.audit.AuditLogDBLayer;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.ForcedClosingHttpClientException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JobSchedulerNoResponseException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.JobSchedulerObjectType;
import com.sos.joc.model.event.EventSnapshot;
import com.sos.joc.model.event.EventType;
import com.sos.joc.model.event.JobSchedulerEvent;

public class EventCallableOfCurrentJobScheduler extends EventCallable implements Callable<JobSchedulerEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventCallableOfCurrentJobScheduler.class);
    private final SOSShiroCurrentUser shiroUser;
    private Long eventId = null;
    private String accessToken;
    private SOSHibernateSession connection = null;
    private Set<String> removedObjects = new HashSet<String>();
//    private Set<String> jobChainEventsOfJob = new HashSet<String>();

    public EventCallableOfCurrentJobScheduler(JOCJsonCommand command, JobSchedulerEvent jobSchedulerEvent, Session session,
            Long instanceId, SOSShiroCurrentUser shiroUser) {
        super(command, jobSchedulerEvent, session, instanceId);
        this.eventId = jobSchedulerEvent.getEventId();
        this.accessToken = command.getCsrfToken();
        this.shiroUser = shiroUser;
    }

    @Override
    public JobSchedulerEvent call() throws Exception {
        return super.call();
    }

    private Events updateSavedInventoryInstance() {
        Events events = new Events();
        if (Globals.urlFromJobSchedulerId.containsKey(jobSchedulerEvent.getControllerId())) {
            DBItemInventoryJSInstance instance = Globals.urlFromJobSchedulerId.get(jobSchedulerEvent.getControllerId());
            if (instance != null && instance.getIsCluster()) {

                try {
                    if (connection == null) {
                        connection = Globals.createSosHibernateStatelessConnection("eventCallable-" + jobSchedulerEvent.getControllerId());
                    }
                    InventoryInstancesDBLayer dbLayer = new InventoryInstancesDBLayer(connection);
                    Globals.beginTransaction(connection);
                    DBItemInventoryJSInstance inst = dbLayer.getInventoryInstanceByControllerId(jobSchedulerEvent.getControllerId(), accessToken);
                    shiroUser.addSchedulerInstanceDBItem(jobSchedulerEvent.getControllerId(), inst);
                    Globals.rollback(connection);
                    Globals.urlFromJobSchedulerId.put(jobSchedulerEvent.getControllerId(), inst);
                    if (!instance.equals(inst)) {
                        EventSnapshot masterChangedEventSnapshot = new EventSnapshot();
                        masterChangedEventSnapshot.setEventType("CurrentJobSchedulerChanged");
                        masterChangedEventSnapshot.setObjectType(EventType.CONTROLLER);
                        masterChangedEventSnapshot.setPath(inst.getUri());
                        events.put(masterChangedEventSnapshot);
                    }
                } catch (Exception e) {
                } finally {
                    Globals.disconnect(connection);
                    connection = null;
                }
            }
        }
        return events;
    }

    @Override
    protected List<EventSnapshot> getEventSnapshots(Long eventId) throws JocException {
        return getEventSnapshotsMap(eventId).values(removedObjects);
    }

    private Events nonEmptyEvent(JsonObject json) {
        Events eventSnapshots = new Events();
        for (JsonObject event : json.getJsonArray("stamped").getValuesAs(JsonObject.class)) {
            EventSnapshot eventSnapshot = new EventSnapshot();
            EventSnapshot eventNotification = new EventSnapshot();

            String eventType = event.getString("TYPE", null);
            eventSnapshot.setEventType(eventType);
            eventNotification.setEventType(eventType);

            Long eId = event.getJsonNumber("eventId").longValue();
            jobSchedulerEvent.setEventId(eId);
            eventNotification.setEventId(eId);

            if (eventType.startsWith("Controller") || eventType.startsWith("Cluster")) {
                eventNotification = null;
                eventSnapshot.setEventType("SchedulerStateChanged");
                eventSnapshot.setObjectType(EventType.CONTROLLER);
                // String state = event.getString("state", null);
                eventSnapshot.setPath(command.getSchemeAndAuthority());
                // if (state!= null && (state.contains("stop") || state.contains("waiting"))) {
                eventSnapshots.putAll(updateSavedInventoryInstance());
                // }
            } else {
                String eventKey = event.getString("key", null);
                if (eventKey == null) {
                    continue;
                }
                if (eventType.startsWith("File")) {
                    if ("FileBasedRemoved".equals(eventType)) {
                        String[] eventKeyParts = eventKey.split(":", 2);
                        removedObjects.add(eventKeyParts[1] + "." + JobSchedulerObjectType.fromValue(eventKeyParts[0].toUpperCase().replaceAll("_",
                                "")).name());
                    }
                    continue;
                } else {
                    eventSnapshot.setPath(eventKey);
                    eventNotification.setPath(eventKey);
                    // if (eventSnapshots.getEvents().containsKey(eventKey)) {
                    // continue;
                    // }
                    if (eventType.startsWith("Order")) {
                        if ("OrderAdded".equals(eventType)) {
                            eventSnapshot.setEventType(eventType);
                        } else {
                            eventSnapshot.setEventType("OrderStateChanged");
                        }
                        eventSnapshot.setObjectType(EventType.ORDER);
                        //obsolete in JS2? eventSnapshots.put(createJobChainEventOfOrder(eventSnapshot));
                        // add event for outerJobChain if exist
                        eventNotification.setObjectType(EventType.ORDER);
                    } else {
                        continue;
                    }
                }
            }
            if (eventNotification != null) {
                eventSnapshots.add(eventNotification);
            }
            if (eventSnapshot != null) {
                eventSnapshots.put(eventSnapshot);
            }
        }
        try {
            json.clear();
        } catch (Exception e) {
        } finally {
            json = null;
        }
        return eventSnapshots;
    }

    private Events getEventSnapshotsMap(Long eventId) throws JocException {
        Events eventSnapshots = new Events();
        checkTimeout();
        try {
            JsonObject json = getJsonObject(eventId);
            Long newEventId = 0L;
            String type = json.getString("TYPE", "Empty");
            switch (type) {
            case "Empty":
                newEventId = json.getJsonNumber("lastEventId").longValue();
                jobSchedulerEvent.setEventId(newEventId);
                eventSnapshots.putAll(getEventSnapshotsMap(newEventId));
                break;
            case "NonEmpty":
                eventSnapshots.putAll(nonEmptyEvent(json));
                newEventId = jobSchedulerEvent.getEventId();
                if (eventSnapshots.isEmpty()) {
                    eventSnapshots.putAll(getEventSnapshotsMap(newEventId));
                } else {
                    for (int i = 0; i < 6; i++) {
                        if (!(Boolean) session.getAttribute(Globals.SESSION_KEY_FOR_SEND_EVENTS_IMMEDIATLY)) {
                            try { // collect further events after 2sec to minimize the number of responses
                                int delay = Math.min(250, getSessionTimeout());
                                if (delay > 0) {
                                    Thread.sleep(delay);
                                }
                            } catch (InterruptedException e1) {
                            }
                        }
                    }
                    eventSnapshots.putAll(getEventSnapshotsMapFromNextResponse(newEventId));
                    // JOC-242
                    // obsolete in JS2? eventSnapshots.putAll(addEventforJobAndOrderWhichUseSchedule());
                    // add auditLogEvent
                    eventSnapshots.putAll(addAuditLogEvent());
                    try { // a small delay because events comes earlier then the JobScheduler has update its objects in some requests
                        int delay = Math.min(500, getSessionTimeout());
                        if (delay > 0) {
                            Thread.sleep(delay);
                        }
                    } catch (InterruptedException e1) {
                    }
                }
                break;
            case "Torn":
                newEventId = json.getJsonNumber("after").longValue();
                jobSchedulerEvent.setEventId(newEventId);
                eventSnapshots.putAll(getEventSnapshotsMap(newEventId));
                break;
            }
        } catch (JobSchedulerNoResponseException | JobSchedulerConnectionRefusedException e) {
            // if current Jobscheduler down then retry after 15sec
            eventSnapshots.putAll(updateSavedInventoryInstance());
            try {
                int delay = Math.min(15000, getSessionTimeout());
                LOGGER.debug(command.getSchemeAndAuthority() + ": connection refused; retry after " + delay + "ms");
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
            eventSnapshots.putAll(getEventSnapshotsMap(eventId));
        } catch (JobSchedulerConnectionResetException e) {
            EventSnapshot eventSnapshot = new EventSnapshot();
            eventSnapshot.setEventType("SchedulerStateChanged");
            eventSnapshot.setObjectType(EventType.CONTROLLER);
            eventSnapshot.setPath(command.getSchemeAndAuthority());
            eventSnapshots.put(eventSnapshot);
            eventSnapshots.putAll(updateSavedInventoryInstance());
        }
        return eventSnapshots;
    }

    private Events getEventSnapshotsMapFromNextResponse(Long eventId) throws JocException {
        Events eventSnapshots = new Events();
        JsonObject json = getJsonObject(eventId, 0);
        String type = json.getString("TYPE", "Empty");
        switch (type) {
        case "Empty":
            break;
        case "NonEmpty":
            eventSnapshots.putAll(nonEmptyEvent(json));
            break;
        case "Torn":
            break;
        }
        return eventSnapshots;
    }

    private Events addAuditLogEvent() {
        Events eventSnapshots = new Events();
        try {
            if (connection == null) {
                connection = Globals.createSosHibernateStatelessConnection("eventCallable-" + jobSchedulerEvent.getControllerId());
            }
            
            Date from = new Date();
            from.setTime(eventId / 1000);
           
            AuditLogDBLayer dbLayer = new AuditLogDBLayer(connection);
            Globals.beginTransaction(connection);
            AuditLogDBFilter auditLogDBFilter = new AuditLogDBFilter();
            auditLogDBFilter.setControllerId(jobSchedulerEvent.getControllerId());
            auditLogDBFilter.setCreatedFrom(from);
            List<DBItemJocAuditLog> auditLogs = dbLayer.getAuditLogs(auditLogDBFilter);
            Globals.rollback(connection);
            if (auditLogs != null && !auditLogs.isEmpty()) {
                for (DBItemJocAuditLog auditLogItem : auditLogs) {
                    EventSnapshot auditLogEvent = new EventSnapshot();
                    auditLogEvent.setEventType("AuditLogChanged");
                    if (auditLogItem.getWorkflow() != null && !auditLogItem.getWorkflow().isEmpty()) {
                        String path = auditLogItem.getWorkflow();
                        auditLogEvent.setObjectType(EventType.WORKFLOW);
                        if (auditLogItem.getOrderId() != null && !auditLogItem.getOrderId().isEmpty()) {
                            path += "," + auditLogItem.getOrderId();
                            auditLogEvent.setObjectType(EventType.ORDER);
                        }
                        auditLogEvent.setPath(path);
                    }
                    eventSnapshots.put(auditLogEvent);
                }

            }
        } catch (Exception e) {
        } finally {
            Globals.disconnect(connection);
            connection = null;
        }
        return eventSnapshots;
    }
}
