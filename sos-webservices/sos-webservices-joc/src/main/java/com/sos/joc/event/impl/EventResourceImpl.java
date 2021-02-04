package com.sos.joc.event.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.classes.event.EventServiceFactory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.event.resource.IEventResource;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.event.Controller;
import com.sos.joc.model.event.Event;
import com.sos.joc.model.event.EventSnapshot;
import com.sos.joc.model.event.EventType;
import com.sos.joc.model.order.OrderStateText;
import com.sos.schema.JsonValidator;

import js7.proxy.javaapi.data.workflow.JWorkflowId;

@Path("events")
public class EventResourceImpl extends JOCResourceImpl implements IEventResource {

    private static final String API_CALL = "./events";
    private static final Logger LOGGER = LoggerFactory.getLogger(EventResourceImpl.class);
    

    @Override
    public JOCDefaultResponse postEvent(String accessToken, byte[] inBytes) {

        Event entity = new Event();
        Session session = null;
        SOSHibernateSession connection = null;
        
        try {
            initLogging(API_CALL, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, Controller.class);
            Controller in = Globals.objectMapper.readValue(inBytes, Controller.class);
            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getJS7Controller().getView().isStatus());
            if (response != null) {
                return response;
            }
            
            try {
                session = getJobschedulerUser().getSosShiroCurrentUser().getCurrentSubject().getSession(false);
            } catch (InvalidSessionException e1) {
                throw new SessionNotExistException(e1);
            }
            
            String controllerId = in.getControllerId();
            boolean firstCall = in.getEventId() == null || in.getEventId() <= 0L;
            long eventId = firstCall ? Instant.now().getEpochSecond() : in.getEventId();
            entity.setEventId(eventId);
            entity.setControllerId(controllerId);
            entity.setEventSnapshots(Collections.emptyList());
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            final DeployedConfigurationDBLayer dbCLayer = new DeployedConfigurationDBLayer(connection);

            entity = processAfter(EventServiceFactory.getEvents(controllerId, eventId, accessToken, session, getTerminatedOrders(controllerId,
                    firstCall)), dbCLayer);

        } catch (JobSchedulerConnectionRefusedException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatus434JSError(e);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (InvalidSessionException e) {
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
        if (EventServiceFactory.isClosed.get()) {
            return null;
        }
        entity.setDeliveryDate(Date.from(Instant.now()));
        return JOCDefaultResponse.responseStatus200(entity);
    }
    
    public static Event processAfter(Event evt, DeployedConfigurationDBLayer dbCLayer) {
        if (evt.getEventSnapshots() == null || evt.getEventSnapshots().isEmpty()) {
            return evt;
        }
        
        Set<String> workflowNames = evt.getEventSnapshots().stream().filter(e -> EventType.WORKFLOW.equals(e.getObjectType())).map(e -> (e
                .getWorkflow() != null) ? e.getWorkflow().getPath() : e.getPath()).filter(Objects::nonNull).collect(Collectors.toSet());

        Set<String> lockNames = evt.getEventSnapshots().stream().filter(e -> EventType.LOCK.equals(e.getObjectType())).map(EventSnapshot::getPath)
                .filter(Objects::nonNull).collect(Collectors.toSet());

        Map<String, String> namePathWorkflowMap = null;
        Map<String, String> namePathLockMap = null;
        try {
            if (dbCLayer != null) {
                namePathWorkflowMap = dbCLayer.getNamePathMapping(evt.getControllerId(), workflowNames, DeployType.WORKFLOW.intValue());
            }
        } catch (SOSHibernateException e1) {
            LOGGER.warn(e1.toString());
        }
        try {
            if (dbCLayer != null) {
                namePathLockMap = dbCLayer.getNamePathMapping(evt.getControllerId(), lockNames, DeployType.LOCK.intValue());
            }
        } catch (SOSHibernateException e1) {
            LOGGER.warn(e1.toString());
        }
        if (namePathWorkflowMap == null) {
            namePathWorkflowMap = Collections.emptyMap();
        }
        if (namePathLockMap == null) {
            namePathLockMap = Collections.emptyMap();
        }
        for (EventSnapshot e : evt.getEventSnapshots()) {
            if (e.getWorkflow() != null) {
                String name = e.getWorkflow().getPath();
                if (name != null) {
                    e.getWorkflow().setPath(namePathWorkflowMap.getOrDefault(name, name));
                }
            }
            if (EventType.WORKFLOW.equals(e.getObjectType()) && e.getPath() != null) {
                e.setPath(namePathWorkflowMap.getOrDefault(e.getPath(), e.getPath()));
            } else if (EventType.LOCK.equals(e.getObjectType()) && e.getPath() != null) {
                e.setPath(namePathLockMap.getOrDefault(e.getPath(), e.getPath()));
            }
        }
        return evt;
    }
    
    private static WorkflowId mapWorkflowId(JWorkflowId workflowId) {
        WorkflowId w = new WorkflowId();
        w.setPath(workflowId.path().string());
        w.setVersionId(workflowId.versionId().string());
        return w;
    }
    
    private static Map<String, WorkflowId> getTerminatedOrders(String controllerId, boolean firstCall) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, ExecutionException {
        if (!firstCall) {
           return Collections.emptyMap(); 
        }
        final List<OrderStateText> states = Arrays.asList(OrderStateText.CANCELLED, OrderStateText.FINISHED);
        return Proxy.of(controllerId).currentState().ordersBy(o -> states.contains(OrdersHelper.getGroupedState(o.state().getClass())))
            .collect(Collectors.toMap(o -> o.id().string(), o -> mapWorkflowId(o.workflowId())));
    }

}
