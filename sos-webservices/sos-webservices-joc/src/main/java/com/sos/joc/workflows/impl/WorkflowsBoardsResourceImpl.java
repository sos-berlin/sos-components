package com.sos.joc.workflows.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.items.DeployedWorkflowWithBoards;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.workflow.WorkflowsBoards;
import com.sos.joc.model.workflow.WorkflowsFilter;
import com.sos.joc.workflows.resource.IWorkflowsBoardsResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.item.JRepo;

@Path("workflows")
public class WorkflowsBoardsResourceImpl extends JOCResourceImpl implements IWorkflowsBoardsResource {

    private static final String API_CALL = "./workflows/boards";
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowsBoardsResourceImpl.class);

    @Override
    public JOCDefaultResponse postWorkflowBoards(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, WorkflowsFilter.class);
            WorkflowsFilter workflowsFilter = Globals.objectMapper.readValue(filterBytes, WorkflowsFilter.class);
            String controllerId = workflowsFilter.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getWorkflows()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            final JRepo jRepo = getJRepo(controllerId);
            WorkflowsBoards entity = new WorkflowsBoards();
            List<com.sos.controller.model.workflow.WorkflowBoards> postingWorkflows = new ArrayList<>();
            List<com.sos.controller.model.workflow.WorkflowBoards> expectingWorkflows = new ArrayList<>();
            List<com.sos.controller.model.workflow.WorkflowBoards> consumingWorkflows = new ArrayList<>();

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
            Stream<DeployedWorkflowWithBoards> workflows = dbLayer.getWorkflowsWithBoards(controllerId).stream();
            
            if (jRepo != null) { // only synchronize Workflows if Proxy reachable
                workflows = workflows.filter(w -> jRepo.pathToCheckedWorkflow(WorkflowPath.of(w.getName())).isRight());
            }
            
            workflows.map(DeployedWorkflowWithBoards::mapToWorkflowBoards).filter(Objects::nonNull).forEach(w -> {
                w.setNoticeBoardNames(null);
                if (w.hasConsumeNotice() > 0) {
                    consumingWorkflows.add(w);
                }
                if (w.hasExpectNotice() > 0) {
                    expectingWorkflows.add(w);
                }
                if (w.hasPostNotice() > 0) {
                    postingWorkflows.add(w);
                }
            });

            entity.setPostingWorkflows(postingWorkflows);
            entity.setExpectingWorkflows(expectingWorkflows);
            entity.setConsumingWorkflows(consumingWorkflows);
            entity.setDeliveryDate(Date.from(Instant.now()));
            
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private JRepo getJRepo(String controllerId) {
        JRepo jRepo = null;
        try {
            jRepo = Proxy.of(controllerId).currentState().repo();
        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }
        return jRepo;
    }
    

}
