package com.sos.joc.event.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.sos.auth.interfaces.ISOSSession;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.event.EventServiceFactory;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.event.resource.IEventResource;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.event.Controller;
import com.sos.joc.model.event.Event;
import com.sos.joc.model.event.EventSnapshot;
import com.sos.joc.model.event.EventType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("events")
public class EventResourceImpl extends JOCResourceImpl implements IEventResource {

    private static final String API_CALL = "./events";
    // private static final Logger LOGGER = LoggerFactory.getLogger(EventResourceImpl.class);

    @Override
    public JOCDefaultResponse postEvent(String accessToken, byte[] inBytes) {

        Event entity = new Event();
        ISOSSession session = null;
        try {
            inBytes = initLogging(API_CALL, inBytes, accessToken, CategoryType.OTHERS);
            JsonValidator.validateFailFast(inBytes, Controller.class);
            Controller in = Globals.objectMapper.readValue(inBytes, Controller.class);
            String controllerId = in.getControllerId();

            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            session = checkSession();
            boolean evtIdIsEmpty = in.getEventId() == null || in.getEventId() <= 0L;
            long eventId = evtIdIsEmpty ? Instant.now().getEpochSecond() - 1L : in.getEventId();
            entity.setEventId(eventId);
            entity.setControllerId(controllerId);
            entity.setEventSnapshots(Collections.emptyList());

            entity = processAfter(EventServiceFactory.getEvents(controllerId, evtIdIsEmpty, eventId, session, getJobschedulerUser()),
                    folderPermissions.getListOfFolders(), accessToken);
            
            entity.setDeliveryDate(Date.from(Instant.now()));
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));

        } catch (ControllerConnectionRefusedException e) {
            e.addErrorMetaInfo(getJocError());
            return responseStatus434JSError(e);
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
//        if (EventServiceFactory.isClosed.get()) {
//            return null;
//        }
    }

    private ISOSSession checkSession() throws SessionNotExistException {
        try {
            ISOSSession session = getJobschedulerUser().getSOSAuthCurrentAccount().getCurrentSubject().getSession();
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
            if (evt.getEventsFromSystemMonitoring() != null && evt.getEventsFromSystemMonitoring().size() > 10) {
                evt.getEventsFromSystemMonitoring().subList(10, evt.getEventsFromSystemMonitoring().size() - 1).clear();
            }
            // TODO same sublist for getEventsFromOrderMonitoring?
            if (evt.getEventSnapshots() != null && !evt.getEventSnapshots().isEmpty()) {

                connection = Globals.createSosHibernateStatelessConnection(API_CALL);
                final DeployedConfigurationDBLayer dbCLayer = new DeployedConfigurationDBLayer(connection);

                // List<EventType> eventsWithWorkflow = Arrays.asList(EventType.WORKFLOW, EventType.JOB, EventType.TASKHISTORY, EventType.ORDERHISTORY);
                // List<String> workflowNames = evt.getEventSnapshots().stream().filter(e -> eventsWithWorkflow.contains(e.getObjectType())).map(e -> (e
                // .getWorkflow() != null) ? e.getWorkflow().getPath() : e.getPath()).filter(Objects::nonNull).distinct().collect(Collectors.toList());

                List<String> lockNames = evt.getEventSnapshots().stream().filter(e -> EventType.LOCK.equals(e.getObjectType())).map(
                        EventSnapshot::getPath).filter(Objects::nonNull).distinct().collect(Collectors.toList());

                List<String> noticeBoardNames = evt.getEventSnapshots().stream().filter(e -> EventType.NOTICEBOARD.equals(e.getObjectType())).map(
                        EventSnapshot::getPath).filter(Objects::nonNull).distinct().collect(Collectors.toList());

                // List<String> fileOrderSourceNames = evt.getEventSnapshots().stream().filter(e -> EventType.FILEORDERSOURCE.equals(e.getObjectType())).map(
                // EventSnapshot::getPath).filter(Objects::nonNull).distinct().collect(Collectors.toList());

                // List<String> jobResourceNames = evt.getEventSnapshots().stream().filter(e -> EventType.JOBRESOURCE.equals(e.getObjectType())).map(
                // EventSnapshot::getPath).filter(Objects::nonNull).distinct().collect(Collectors.toList());

                // Map<String, String> namePathWorkflowMap = dbCLayer.getNamePathMapping(evt.getControllerId(), workflowNames, DeployType.WORKFLOW
                // .intValue());
                Map<String, String> namePathLockMap = dbCLayer.getNamePathMapping(evt.getControllerId(), lockNames, DeployType.LOCK.intValue());
                Map<String, String> namePathNoticeBoardMap = dbCLayer.getNamePathMapping(evt.getControllerId(), noticeBoardNames,
                        DeployType.NOTICEBOARD.intValue());
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

            }

            return evt;
        } catch(JocConfigurationException e) {
            // could be occur when JOC is shutting down while ./events API is called
            if (e.getCause() != null && e.getCause() instanceof SOSHibernateConfigurationException) {
                return evt;
            } else {
                throw e;
            }
            
        } finally {
            Globals.disconnect(connection);
        }
    }

}
