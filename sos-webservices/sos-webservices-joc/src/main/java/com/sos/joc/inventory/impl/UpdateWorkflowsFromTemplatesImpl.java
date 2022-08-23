package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.jobtemplate.JobTemplate;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.jobtemplate.JobTemplatesPropagate;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IUpdateWorkflowsFromTemplates;
import com.sos.joc.jobtemplates.impl.JobTemplatesResourceImpl;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ResponseFolder;
import com.sos.joc.model.jobtemplate.propagate.JobReportStateText;
import com.sos.joc.model.jobtemplate.propagate.Report;
import com.sos.joc.model.jobtemplate.propagate.WorkflowPropagateFilter;
import com.sos.joc.model.jobtemplate.propagate.WorkflowReport;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class UpdateWorkflowsFromTemplatesImpl extends JOCResourceImpl implements IUpdateWorkflowsFromTemplates {

    @Override
    public JOCDefaultResponse update(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, WorkflowPropagateFilter.class);
            WorkflowPropagateFilter in = Globals.objectMapper.readValue(inBytes, WorkflowPropagateFilter.class);

            in.setFolder(normalizeFolder(in.getFolder()));
            boolean permission = getJocPermissions(accessToken).getInventory().getManage();
            JOCDefaultResponse response = checkPermissions(accessToken, in, permission);
            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(update(in)));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private Report update(WorkflowPropagateFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            
            List<DBItemInventoryConfiguration> dbWorkflows = null;
            if (in.getWorkflowPaths() != null && !in.getWorkflowPaths().isEmpty()) {
                List<String> workflowNames = in.getWorkflowPaths().stream().map(p -> JocInventory.pathToName(p)).distinct().collect(Collectors.toList());
                dbWorkflows = dbLayer.getConfigurationByNames(workflowNames, ConfigurationType.WORKFLOW.intValue());
            } else {
                dbWorkflows = dbLayer.getUsedWorkflowsByJobTemplateNames(in.getFolder(), in.getRecursive() == Boolean.TRUE, null);
            }
            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();

            JobTemplatesPropagate propagate = new JobTemplatesPropagate(in, permittedFolders);

            DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());

            Report report = new Report();
            Date now = Date.from(Instant.now());
            report.setDeliveryDate(now);

            JocError jocError = getJocError();
            if (dbWorkflows != null && !dbWorkflows.isEmpty()) {
                for (DBItemInventoryConfiguration dbWorkflow : dbWorkflows) {
                    if (!folderIsPermitted(dbWorkflow.getFolder(), permittedFolders)) {
                        WorkflowReport wr = new WorkflowReport();
                        wr.setPath(dbWorkflow.getPath());
                        wr.setState(JobTemplatesPropagate.getState(JobReportStateText.PERMISSION_DENIED));
                        continue;
                    }

                    Map<String, JobTemplate> jobTemplates = Collections.emptyMap();
                    Workflow workflow = (Workflow) JocInventory.content2IJSObject(dbWorkflow.getContent(), ConfigurationType.WORKFLOW.intValue());
                    if (workflow.getJobs() != null && workflow.getJobs().getAdditionalProperties() != null && !workflow.getJobs()
                            .getAdditionalProperties().isEmpty()) {
                        // determine job templates
                        List<DBItemInventoryReleasedConfiguration> jts = dbLayer.getReleasedJobTemplatesByNames(workflow.getJobs()
                                .getAdditionalProperties().values().stream().filter(j -> j.getJobTemplate() != null).map(j -> j.getJobTemplate()
                                        .getName()).filter(Objects::nonNull).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
                        jobTemplates = jts.stream().map(item -> JobTemplatesResourceImpl.getJobTemplate(item, false, jocError)).filter(
                                Objects::nonNull).collect(Collectors.toMap(JobTemplate::getName, Function.identity()));
                    }
                    report.getWorkflows().add(propagate.template2Job(dbWorkflow, workflow, jobTemplates, dbLayer, now, dbAuditLog));
                }
            }
            Globals.commit(session);
            if (dbWorkflows != null && !dbWorkflows.isEmpty()) {
                // post events
                if (!report.getWorkflows().isEmpty()) {
                    report.getWorkflows().stream().filter(JobTemplatesPropagate.workflowIsChanged).map(WorkflowReport::getPath).map(path -> getParent(
                            path)).distinct().forEach(folder -> JocInventory.postEvent(folder));
                }
            }
            
            return report;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final WorkflowPropagateFilter in, boolean permission) throws Exception {
        JOCDefaultResponse response = initPermissions(null, permission);
        if (response == null && in.getFolder() != null) {
            // for in.getRecursive() == TRUE: folder permissions are checked later
            if (JocInventory.ROOT_FOLDER.equals(in.getFolder())) {
                if (in.getRecursive() != Boolean.TRUE && !folderPermissions.isPermittedForFolder(in.getFolder())) {
                    ResponseFolder entity = new ResponseFolder();
                    entity.setDeliveryDate(Date.from(Instant.now()));
                    entity.setPath(in.getFolder());
                    response = JOCDefaultResponse.responseStatus200(entity);
                }

            } else {
                if (in.getRecursive() != Boolean.TRUE && !folderPermissions.isPermittedForFolder(in.getFolder())) {
                    response = accessDeniedResponse();
                }
            }
        }
        return response;
    }
}
