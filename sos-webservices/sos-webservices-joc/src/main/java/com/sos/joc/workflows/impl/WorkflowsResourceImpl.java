package com.sos.joc.workflows.impl;

import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.workflow.WorkflowId;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.workflow.Workflows;
import com.sos.joc.model.workflow.WorkflowsFilter;
import com.sos.joc.workflows.resource.IWorkflowsResource;
import com.sos.schema.JsonValidator;

@Path("workflows")
public class WorkflowsResourceImpl extends JOCResourceImpl implements IWorkflowsResource {

    private static final String API_CALL = "./workflows";
    
    
    @Override
    public JOCDefaultResponse postWorkflows(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            JsonValidator.validateFailFast(filterBytes, WorkflowsFilter.class);
            WorkflowsFilter workflowsFilter = Globals.objectMapper.readValue(filterBytes, WorkflowsFilter.class);
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, workflowsFilter, accessToken, workflowsFilter.getJobschedulerId(),
                    getPermissonsJocCockpit(workflowsFilter.getJobschedulerId(), accessToken).getOrder().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            DeployedConfigurationFilter dbFilter = new DeployedConfigurationFilter();
            dbFilter.setControllerId(workflowsFilter.getJobschedulerId());
            dbFilter.setObjectTypes(Arrays.asList(DeployType.WORKFLOW.ordinal()));
            
            List<WorkflowId> workflowIds = workflowsFilter.getWorkflowIds();
            if (workflowIds != null && !workflowIds.isEmpty()) {
                workflowsFilter.setFolders(null);
            }
            boolean withFolderFilter = workflowsFilter.getFolders() != null && !workflowsFilter.getFolders().isEmpty();
            final Set<Folder> folders = addPermittedFolder(workflowsFilter.getFolders());
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
            List<String> contents = null;
            
            if (workflowIds != null && !workflowIds.isEmpty()) {
                workflowsFilter.setRegex(null);
                Map<Boolean, Set<WorkflowId>> workflowMap = workflowIds.stream().filter(w -> canAdd(w.getPath(), folders)).collect(Collectors
                        .groupingBy(w -> w.getVersionId() != null, Collectors.toSet()));
                if (workflowMap.containsKey(true)) {
                    dbFilter.setWorkflowIds(workflowMap.get(true));
                    contents = dbLayer.getDeployedInventoryWithCommitIds(dbFilter);
                }
                if (workflowMap.containsKey(false)) {
                    dbFilter.setPaths(workflowMap.get(false).stream().map(WorkflowId::getPath).collect(Collectors.toSet()));
                    contents = dbLayer.getDeployedInventory(dbFilter);
                }
            } else if (withFolderFilter && (folders == null || folders.isEmpty())) {
                // no folder permissions
            } else if (folders != null && !folders.isEmpty()) {
                dbFilter.setFolders(folders);
                contents = dbLayer.getDeployedInventory(dbFilter);
            } else {
                contents = dbLayer.getDeployedInventory(dbFilter);
            }
            
            Workflows workflows = new Workflows();
            if (contents != null) {
                Stream<com.sos.jobscheduler.model.workflow.Workflow> workflowsStream = contents.stream().map(c -> {
                    try {
                        return Globals.objectMapper.readValue(c, com.sos.jobscheduler.model.workflow.Workflow.class);
                    } catch (Exception e) {
                        // TODO
                        return null;
                    }
                }).filter(Objects::nonNull);
                if (workflowsFilter.getRegex() != null && !workflowsFilter.getRegex().isEmpty()) {
                    Predicate<String> regex = Pattern.compile(workflowsFilter.getRegex().replaceAll("%", ".*")).asPredicate();
                    workflowsStream = workflowsStream.filter(w -> regex.test(w.getPath()));
                }
                workflows.setWorkflows(workflowsStream.collect(Collectors.toList()));
            }
            workflows.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsString(workflows));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

}
