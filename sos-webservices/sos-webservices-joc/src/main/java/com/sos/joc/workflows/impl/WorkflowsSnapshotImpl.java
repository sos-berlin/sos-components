package com.sos.joc.workflows.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.common.SyncState;
import com.sos.controller.model.common.SyncStateText;
import com.sos.controller.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.controller.ControllerIdReq;
import com.sos.joc.model.workflow.WorkflowsFilter;
import com.sos.joc.model.workflow.WorkflowsSnapshot;
import com.sos.joc.model.workflow.WorkflowsSummary;
import com.sos.joc.workflows.resource.IWorkflowsSnapshot;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.data_for_java.controller.JControllerState;

@Path("workflows")
public class WorkflowsSnapshotImpl extends JOCResourceImpl implements IWorkflowsSnapshot {

    private static final String API_CALL = "./workflows/overview/snapshot";
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowsSnapshotImpl.class);

    @Override
    public JOCDefaultResponse snapshot(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ControllerIdReq.class);
            ControllerIdReq in = Globals.objectMapper.readValue(filterBytes, ControllerIdReq.class);
            String controllerId = in.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getBasicControllerPermissions(controllerId, accessToken)
                    .getWorkflows().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            WorkflowsSnapshot workflows = new WorkflowsSnapshot();
            workflows.setSurveyDate(Date.from(Instant.now()));
            final JControllerState currentstate = getCurrentState(controllerId);
            if (currentstate != null) {
                workflows.setSurveyDate(Date.from(currentstate.instant()));
            }
            final Set<Folder> folders = folderPermissions.getListOfFolders();
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            
            Map<SyncStateText, Long> summary = getWorkflows(controllerId, new DeployedConfigurationDBLayer(connection), currentstate, folders);

            WorkflowsSummary wSummary = new WorkflowsSummary();
            wSummary.setNotSynchronized(summary.getOrDefault(SyncStateText.NOT_IN_SYNC, 0L).intValue());
            wSummary.setSynchronized(summary.getOrDefault(SyncStateText.IN_SYNC, 0L).intValue());
            wSummary.setOutstanding(summary.getOrDefault(SyncStateText.OUTSTANDING, 0L).intValue());
            wSummary.setSuspended(summary.getOrDefault(SyncStateText.SUSPENDED, 0L).intValue());
            workflows.setWorkflows(wSummary);
            workflows.setDeliveryDate(Date.from(Instant.now()));
            
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(workflows));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

    public static Map<SyncStateText, Long> getWorkflows(String controllerId, DeployedConfigurationDBLayer dbLayer, JControllerState currentstate,
            Set<Folder> permittedFolders) {

        WorkflowsFilter workflowsFilter = new WorkflowsFilter();
        workflowsFilter.setControllerId(controllerId);

        List<DeployedContent> contents = WorkflowsHelper.getDeployedContents(workflowsFilter, dbLayer, currentstate, permittedFolders).collect(
                Collectors.toList());

        return WorkflowsHelper.getDeployedContentsStream(workflowsFilter, dbLayer, currentstate, contents, permittedFolders).map(w -> {
            try {
                Workflow workflow = new Workflow();
                workflow.setPath(w.getPath());
                workflow.setVersionId(w.getCommitId());
                WorkflowsHelper.setStateAndSuspended(currentstate, workflow);

                return workflow;
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).map(Workflow::getState).collect(Collectors.groupingBy(SyncState::get_text, Collectors.counting()));
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
