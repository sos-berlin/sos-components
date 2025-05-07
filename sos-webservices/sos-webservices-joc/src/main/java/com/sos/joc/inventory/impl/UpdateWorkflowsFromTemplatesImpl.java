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
import java.util.stream.Stream;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.jobtemplate.JobTemplate;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.job.JobTemplateRef;
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

import jakarta.ws.rs.Path;

@Path(JocInventory.APPLICATION_PATH)
public class UpdateWorkflowsFromTemplatesImpl extends JOCResourceImpl implements IUpdateWorkflowsFromTemplates {

    @Override
    public JOCDefaultResponse update(final String accessToken, byte[] inBytes) {
        try {
            inBytes = initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, WorkflowPropagateFilter.class);
            WorkflowPropagateFilter in = Globals.objectMapper.readValue(inBytes, WorkflowPropagateFilter.class);

            in.setFolder(normalizeFolder(in.getFolder()));
            JOCDefaultResponse response = checkPermissions(accessToken, in, getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
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
            Globals.beginTransaction(session);
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
                        report.getWorkflows().add(wr);
                        continue;
                    }

                    Map<String, JobTemplate> jobTemplates = Collections.emptyMap();
                    Workflow workflow = JocInventory.workflowContent2Workflow(dbWorkflow.getContent());
                    if (workflow != null) {
                        if (workflow.getJobs() != null && workflow.getJobs().getAdditionalProperties() != null && !workflow.getJobs()
                                .getAdditionalProperties().isEmpty()) {
                            // determine job template names from Workflow
                            List<String> jobTemplateNamesAtWorkflow = workflow.getJobs().getAdditionalProperties().values().stream().map(
                                    Job::getJobTemplate).filter(Objects::nonNull).map(JobTemplateRef::getName).filter(Objects::nonNull).filter(s -> !s
                                            .isEmpty()).collect(Collectors.toList());
                            // read job template items from DB
                            List<DBItemInventoryReleasedConfiguration> jts = dbLayer.getReleasedJobTemplatesByNames(jobTemplateNamesAtWorkflow);

                            jobTemplates = jts.stream().map(item -> JobTemplatesResourceImpl.getJobTemplate(item, false, jocError)).filter(
                                    Objects::nonNull).collect(Collectors.toMap(JobTemplate::getName, Function.identity()));

                            for (String name : jobTemplateNamesAtWorkflow) {
                                jobTemplates.putIfAbsent(name, null);
                            }
                        }
                        report.getWorkflows().add(propagate.template2Job(dbWorkflow, workflow, jobTemplates, dbLayer, now, dbAuditLog));
                    }
                }
            }
            Globals.commit(session);
            if (dbWorkflows != null && !dbWorkflows.isEmpty()) {
                // post events
                if (!report.getWorkflows().isEmpty()) {
                    report.getWorkflows().stream().filter(JobTemplatesPropagate.workflowIsChanged).map(WorkflowReport::getPath).distinct().forEach(
                            path -> {
                                JocInventory.postEvent(getParent(path));
                                JocInventory.postObjectEvent(path, ConfigurationType.WORKFLOW);
                            });
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

    private JOCDefaultResponse checkPermissions(final String accessToken, final WorkflowPropagateFilter in, Stream<Boolean> permission) throws Exception {
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
