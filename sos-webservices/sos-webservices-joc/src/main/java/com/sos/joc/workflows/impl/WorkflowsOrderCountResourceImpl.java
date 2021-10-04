package com.sos.joc.workflows.impl;

import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.workflow.WorkflowIdsFilter;
import com.sos.joc.model.workflow.WorkflowOrderCount;
import com.sos.joc.model.workflow.WorkflowsOrderCount;
import com.sos.joc.workflows.resource.IWorkflowsOrderCountResource;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.data.item.VersionedItemId;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;

@Path("workflows")
public class WorkflowsOrderCountResourceImpl extends JOCResourceImpl implements IWorkflowsOrderCountResource {

    private static final String API_CALL = "./workflows/order_count";
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowsOrderCountResourceImpl.class);

    @Override
    public JOCDefaultResponse postOrderCount(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, WorkflowIdsFilter.class);
            WorkflowIdsFilter workflowsFilter = Globals.objectMapper.readValue(filterBytes, WorkflowIdsFilter.class);
            String controllerId = workflowsFilter.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getWorkflows()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();

            WorkflowsOrderCount workflows = new WorkflowsOrderCount();
            workflows.setSurveyDate(Date.from(Instant.now()));
            final JControllerState currentstate = getCurrentState(controllerId);
            if (currentstate != null) {
                workflows.setSurveyDate(Date.from(currentstate.instant()));
            }
            Set<VersionedItemId<WorkflowPath>> workflows2 = workflowsFilter.getWorkflowIds().parallelStream().filter(w -> canAdd(WorkflowPaths
                    .getPath(w), permittedFolders)).map(w -> {
                        if (w.getVersionId() == null || w.getVersionId().isEmpty()) {
                            return currentstate.repo().pathToWorkflow(WorkflowPath.of(JocInventory.pathToName(w.getPath())));
                        } else {
                            return currentstate.repo().idToWorkflow(JWorkflowId.of(JocInventory.pathToName(w.getPath()), w.getVersionId()));
                        }
                    }).filter(Either::isRight).map(Either::get).map(JWorkflow::id).map(JWorkflowId::asScala).collect(Collectors.toSet());
            workflows.setWorkflows(currentstate.ordersBy(o -> workflows2.contains(o.workflowId())).collect(Collectors.groupingByConcurrent(
                    JOrder::workflowId, Collectors.reducing(0, e -> 1, Integer::sum))).entrySet().stream().map(e -> {
                        WorkflowOrderCount w = new WorkflowOrderCount();
                        w.setPath(e.getKey().path().string());
                        w.setVersionId(e.getKey().versionId().string());
                        w.setNumOfOrders(e.getValue());
                        return w;
                    }).collect(Collectors.toList()));
            workflows.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(workflows);

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private JControllerState getCurrentState(String controllerId) {
        JControllerState currentstate = null;
        try {
            currentstate = Proxy.of(controllerId).currentState();
        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }
        return currentstate;
    }
}
