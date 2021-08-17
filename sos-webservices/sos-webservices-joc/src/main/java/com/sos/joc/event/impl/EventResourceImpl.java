package com.sos.joc.event.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.apache.shiro.session.Session;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.event.EventServiceFactory;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.event.resource.IEventResource;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.event.Controller;
import com.sos.joc.model.event.Event;
import com.sos.joc.model.event.EventSnapshot;
import com.sos.joc.model.event.EventType;
import com.sos.schema.JsonValidator;

@Path("events")
public class EventResourceImpl extends JOCResourceImpl implements IEventResource {

    private static final String API_CALL = "./events";
    // private static final Logger LOGGER = LoggerFactory.getLogger(EventResourceImpl.class);

    @Override
    public JOCDefaultResponse postEvent(String accessToken, byte[] inBytes) {

        Event entity = new Event();
        Session session = null;
        try {
            initLogging(API_CALL, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, Controller.class);
            Controller in = Globals.objectMapper.readValue(inBytes, Controller.class);
            String controllerId = in.getControllerId();

            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            session = checkSession();
            boolean evtIdIsEmpty = in.getEventId() == null || in.getEventId() <= 0L;
            long eventId = evtIdIsEmpty ? Instant.now().getEpochSecond() : in.getEventId();
            entity.setEventId(eventId);
            entity.setControllerId(controllerId);
            entity.setEventSnapshots(Collections.emptyList());

            entity = processAfter(EventServiceFactory.getEvents(controllerId, eventId, accessToken, session), folderPermissions.getListOfFolders(),
                    accessToken);

        } catch (ControllerConnectionRefusedException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatus434JSError(e);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Throwable e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
        if (EventServiceFactory.isClosed.get()) {
            return null;
        }
        entity.setDeliveryDate(Date.from(Instant.now()));
        return JOCDefaultResponse.responseStatus200(entity);
    }

    private Session checkSession() throws SessionNotExistException {
        try {
            Session session = getJobschedulerUser().getSosShiroCurrentUser().getCurrentSubject().getSession(false);
            long timeout = session.getTimeout();
            // LOGGER.info("Session timeout: " + timeout);
            if (timeout < 0L) {
                // unlimited session
            } else if (timeout == 0L) {
                throw new SessionNotExistException("Session has expired");
            } else if (timeout - 1000L < 0L) {
                // doesn't send events in the last second of a session before it expires
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (Exception e) {
                }
            }
            return session;
        } catch (SessionNotExistException e) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e1) {
            }
            throw e;
        } catch (Exception e) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e1) {
            }
            throw new SessionNotExistException(e);
        }
    }

    private static Event processAfter(Event evt, Set<Folder> permittedFolders, String accessToken) {
        SOSHibernateSession connection = null;
        try {
            if (EventServiceFactory.isClosed.get()) {
                return evt;
            }
            if (evt.getEventSnapshots() == null || evt.getEventSnapshots().isEmpty()) {
                return evt;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            final DeployedConfigurationDBLayer dbCLayer = new DeployedConfigurationDBLayer(connection);

            // List<EventType> eventsWithWorkflow = Arrays.asList(EventType.WORKFLOW, EventType.JOB, EventType.TASKHISTORY, EventType.ORDERHISTORY);
            // Set<String> workflowNames = evt.getEventSnapshots().stream().filter(e -> eventsWithWorkflow.contains(e.getObjectType())).map(e -> (e
            // .getWorkflow() != null) ? e.getWorkflow().getPath() : e.getPath()).filter(Objects::nonNull).collect(Collectors.toSet());

            Set<String> lockNames = evt.getEventSnapshots().stream().filter(e -> EventType.LOCK.equals(e.getObjectType())).map(EventSnapshot::getPath)
                    .filter(Objects::nonNull).collect(Collectors.toSet());

            Set<String> noticeBoardNames = evt.getEventSnapshots().stream().filter(e -> EventType.NOTICEBOARD.equals(e.getObjectType())).map(
                    EventSnapshot::getPath).filter(Objects::nonNull).collect(Collectors.toSet());

            // Set<String> fileOrderSourceNames = evt.getEventSnapshots().stream().filter(e -> EventType.FILEORDERSOURCE.equals(e.getObjectType())).map(
            // EventSnapshot::getPath).filter(Objects::nonNull).collect(Collectors.toSet());

            // Set<String> jobResourceNames = evt.getEventSnapshots().stream().filter(e -> EventType.JOBRESOURCE.equals(e.getObjectType())).map(
            // EventSnapshot::getPath).filter(Objects::nonNull).collect(Collectors.toSet());

            // Map<String, String> namePathWorkflowMap = dbCLayer.getNamePathMapping(evt.getControllerId(), workflowNames, DeployType.WORKFLOW
            // .intValue());
            Map<String, String> namePathLockMap = dbCLayer.getNamePathMapping(evt.getControllerId(), lockNames, DeployType.LOCK.intValue());
            Map<String, String> namePathNoticeBoardMap = dbCLayer.getNamePathMapping(evt.getControllerId(), noticeBoardNames, DeployType.NOTICEBOARD
                    .intValue());
            // Map<String, String> namePathFileOrderSourceMap = dbCLayer.getNamePathMapping(evt.getControllerId(), fileOrderSourceNames,
            // DeployType.FILEORDERSOURCE.intValue());
            // Map<String, String> namePathJobResourceMap = dbCLayer.getNamePathMapping(evt.getControllerId(), jobResourceNames,
            // DeployType.JOBRESOURCE.intValue());

            // Map<WorkflowId, String> namePathWorkflowMap = WorkflowPaths.getNamePathMap();

            evt.setEventSnapshots(evt.getEventSnapshots().stream().map(e -> {
                // LOGGER.info(e.toString());
                if (e.getWorkflow() != null) {
                    e.setWorkflow(WorkflowPaths.getWorkflowId(e.getWorkflow()));
                    // LOGGER.info("workflowPath: " + e.getWorkflow().getPath());
                    if (!canAdd(e.getWorkflow().getPath(), permittedFolders)) {
                        // LOGGER.info("event skipped");
                        return null;
                    }
                }
                String path = e.getPath();
                if (path != null) {
                    if (EventType.WORKFLOW.equals(e.getObjectType())) {
                        e.setPath(WorkflowPaths.getPath(path));
                        // LOGGER.info("workflowPath2: " + e.getPath());
                        if (!canAdd(e.getPath(), permittedFolders)) {
                            // LOGGER.info("event skipped");
                            return null;
                        }
                    } else if (EventType.LOCK.equals(e.getObjectType())) {
                        e.setPath(namePathLockMap.getOrDefault(path, path));
                        // LOGGER.info("lockPath: " + e.getPath());
                        if (!canAdd(e.getPath(), permittedFolders)) {
                            // LOGGER.info("event skipped");
                            return null;
                        }
                        // } else if (EventType.FILEORDERSOURCE.equals(e.getObjectType())) {
                        // e.setPath(namePathFileOrderSourceMap.getOrDefault(path, path));
                        // if (!canAdd(e.getPath(), permittedFolders)) {
                        // return null;
                        // }
                        // } else if (EventType.JOBRESOURCE.equals(e.getObjectType())) {
                        // e.setPath(namePathJobResourceMap.getOrDefault(path, path));
                        // if (!canAdd(e.getPath(), permittedFolders)) {
                        // return null;
                        // }
                    } else if (EventType.NOTICEBOARD.equals(e.getObjectType())) {
                        e.setPath(namePathNoticeBoardMap.getOrDefault(path, path));
                        if (!canAdd(e.getPath(), permittedFolders)) {
                            return null;
                        }
                    } else if (EventType.FOLDER.equals(e.getObjectType())) {
                        // LOGGER.info("folder: " + path);
                        if (!folderIsPermitted(path, permittedFolders)) {
                            // LOGGER.info("event skipped");
                            return null;
                        }
                    }
                }
                if (e.getAccessToken() != null && !accessToken.equals(e.getAccessToken())) {
                    return null;
                }
                return e;
            }).filter(Objects::nonNull).collect(Collectors.toList()));

            return evt;
        } finally {
            Globals.disconnect(connection);
        }
    }

}
