package com.sos.joc.workflows.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowRefs;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.items.WorkflowBoards;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
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
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, WorkflowsFilter.class);
            WorkflowsFilter workflowsFilter = Globals.objectMapper.readValue(filterBytes, WorkflowsFilter.class);
            String controllerId = workflowsFilter.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getBasicControllerPermissions(controllerId, accessToken)
                    .getWorkflows().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            final JRepo jRepo = getJRepo(controllerId);
            WorkflowsBoards entity = new WorkflowsBoards();
            List<com.sos.controller.model.workflow.WorkflowBoards> postingWorkflows = new ArrayList<>();
            List<com.sos.controller.model.workflow.WorkflowBoards> expectingWorkflows = new ArrayList<>();
            List<com.sos.controller.model.workflow.WorkflowBoards> consumingWorkflows = new ArrayList<>();

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
//            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
//            List<DeployedWorkflowWithBoards> workflows = dbLayer.getWorkflowsWithBoards(controllerId);
            
//            Map<String, LinkedHashSet<String>> wTags = getMapOfTagsPerWorkflow(connection, workflows);
            
            // only synchronized Workflows if Proxy is reachable
//            Predicate<DeployedWorkflowWithBoards> onlySynchronized = w -> jRepo == null ? true : jRepo.pathToCheckedWorkflow(WorkflowPath.of(w
//                    .getName())).isRight();
            
            Predicate<WorkflowBoards> onlySynchronized2 = w -> jRepo == null ? true : jRepo.pathToCheckedWorkflow(WorkflowPath.of(JocInventory
                    .pathToName(w.getPath()))).isRight();

            //workflows.stream().filter(onlySynchronized).map(DeployedWorkflowWithBoards::mapToWorkflowBoards).filter(Objects::nonNull)
//            workflows.stream().filter(onlySynchronized).map(DeployedWorkflowWithBoards::mapToWorkflowBoardsWithPositions).filter(Objects::nonNull)
//                .peek(w -> w.setWorkflowTags(wTags.get(JocInventory.pathToName(w.getPath()))))
//                .peek(w -> w.setNoticeBoardNames(null))
//                .forEach(w -> {
//                        if (w.hasConsumeNotice() > 0) {
//                            consumingWorkflows.add(w);
//                        }
//                        if (w.hasExpectNotice() > 0) {
//                            expectingWorkflows.add(w);
//                        }
//                        if (w.hasPostNotice() > 0) {
//                            postingWorkflows.add(w);
//                        }
//                    });
//
//            entity.setPostingWorkflows(postingWorkflows);
//            entity.setExpectingWorkflows(expectingWorkflows);
//            entity.setConsumingWorkflows(consumingWorkflows);
//            entity.setDeliveryDate(Date.from(Instant.now()));
            
            Map<String, WorkflowBoards> wbs = WorkflowRefs.getWorkflowNamesWithBoards(controllerId);
            
            Map<String, LinkedHashSet<String>> wTags = getMapOfTagsPerWorkflow(connection, wbs.keySet());
            
            wbs.values().stream().filter(w -> canAdd(w.getPath(), permittedFolders)).filter(onlySynchronized2).peek(w -> w.setWorkflowTags(wTags.get(
                    JocInventory.pathToName(w.getPath())))).peek(w -> w.setNoticeBoardNames(null)).forEach(w -> {
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

//    private static Map<String, LinkedHashSet<String>> getMapOfTagsPerWorkflow(SOSHibernateSession connection, List<DeployedWorkflowWithBoards> ws) {
//        if (WorkflowsHelper.withWorkflowTagsDisplayed()) {
//            return WorkflowsHelper.getMapOfTagsPerWorkflow(connection, ws.stream().map(DeployedWorkflowWithBoards::getName));
//        }
//        return Collections.emptyMap();
//    }
    
    private static Map<String, LinkedHashSet<String>> getMapOfTagsPerWorkflow(SOSHibernateSession connection, Set<String> ws) {
        if (WorkflowsHelper.withWorkflowTagsDisplayed()) {
            return WorkflowsHelper.getMapOfTagsPerWorkflow(connection, ws.stream());
        }
        return Collections.emptyMap();
    }
    
}
