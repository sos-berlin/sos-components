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
import com.sos.jobscheduler.model.instruction.ForkJoin;
import com.sos.jobscheduler.model.instruction.IfElse;
import com.sos.jobscheduler.model.instruction.Instruction;
import com.sos.jobscheduler.model.instruction.TryCatch;
import com.sos.jobscheduler.model.workflow.Branch;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.jobscheduler.model.workflow.WorkflowId;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.deploy.items.DeployedContent;
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
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, WorkflowsFilter.class);
            WorkflowsFilter workflowsFilter = Globals.objectMapper.readValue(filterBytes, WorkflowsFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(workflowsFilter.getControllerId(), getPermissonsJocCockpit(workflowsFilter
                    .getControllerId(), accessToken).getOrder().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            DeployedConfigurationFilter dbFilter = new DeployedConfigurationFilter();
            dbFilter.setControllerId(workflowsFilter.getControllerId());
            dbFilter.setObjectTypes(Arrays.asList(DeployType.WORKFLOW.intValue()));
            
            List<WorkflowId> workflowIds = workflowsFilter.getWorkflowIds();
            if (workflowIds != null && !workflowIds.isEmpty()) {
                workflowsFilter.setFolders(null);
            }
            boolean withFolderFilter = workflowsFilter.getFolders() != null && !workflowsFilter.getFolders().isEmpty();
            final Set<Folder> folders = addPermittedFolder(workflowsFilter.getFolders());
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
            List<DeployedContent> contents = null;
            
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
                Stream<DeployedContent> contentsStream = contents.stream();
                if (workflowsFilter.getRegex() != null && !workflowsFilter.getRegex().isEmpty()) {
                    Predicate<String> regex = Pattern.compile(workflowsFilter.getRegex().replaceAll("%", ".*")).asPredicate();
                    contentsStream = contentsStream.filter(w -> regex.test(w.getPath()));
                }
                Stream<com.sos.jobscheduler.model.workflow.Workflow> workflowsStream = contentsStream.map(c -> {
                    try {
                        com.sos.jobscheduler.model.workflow.Workflow workflow = Globals.objectMapper.readValue(c.getContent(),
                                com.sos.jobscheduler.model.workflow.Workflow.class);
                        workflow.setPath(c.getPath());
                        return addWorkflowPositions(workflow);
                    } catch (Exception e) {
                        // TODO
                        return null;
                    }
                }).filter(Objects::nonNull);
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
    
    private Workflow addWorkflowPositions(Workflow w) {
        if (w == null) {
            return null;
        }
        Object[] o = {};
        setWorkflowPositions(o, w.getInstructions());
        return w;
    }
    
    private void setWorkflowPositions(Object[] parentPosition, List<Instruction> insts) {
        if (insts != null) {
            for (int i = 0; i < insts.size(); i++) {
                Object[] pos = extendArray(parentPosition, i);
                Instruction inst = insts.get(i);
                inst.setPosition(Arrays.asList(pos));
                switch (inst.getTYPE()) {
                case FORK:
                    ForkJoin f = inst.cast();
                    for(Branch b : f.getBranches()) {
                        setWorkflowPositions(extendArray(pos, "fork+" + b.getId()), b.getWorkflow().getInstructions());
                    }
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    setWorkflowPositions(extendArray(pos, "then"), ie.getThen().getInstructions());
                    if (ie.getElse() != null) {
                        setWorkflowPositions(extendArray(pos, "else"), ie.getElse().getInstructions());
                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    setWorkflowPositions(extendArray(pos, "try+0"), tc.getTry().getInstructions());
                    if (tc.getCatch() != null) {
                        setWorkflowPositions(extendArray(pos, "catch+0"), tc.getCatch().getInstructions());
                    }
                    break;
                default:
                    break;
                }
            }
        }
    }
    
    private Object[] extendArray(Object[] position, Object extValue) {
        Object[] pos = Arrays.copyOf(position, position.length + 1);
        pos[position.length] = extValue;
        return pos;
    }

}
