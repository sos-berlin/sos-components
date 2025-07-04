package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.jobtemplate.JobTemplate;
import com.sos.inventory.model.job.Job;
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
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.inventory.resource.IUpdateJobFromTemplates;
import com.sos.joc.jobtemplates.impl.JobTemplatesResourceImpl;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.jobtemplate.propagate.JobPropagateFilter;
import com.sos.joc.model.jobtemplate.propagate.JobReport;
import com.sos.joc.model.jobtemplate.propagate.JobReports;
import com.sos.joc.model.jobtemplate.propagate.WorkflowReport;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocInventory.APPLICATION_PATH)
public class UpdateJobFromTemplatesImpl extends JOCResourceImpl implements IUpdateJobFromTemplates {

    @Override
    public JOCDefaultResponse update(final String accessToken, byte[] inBytes) {
        try {
            inBytes = initLogging(IMPL_PATH, inBytes, accessToken, CategoryType.INVENTORY);
            JsonValidator.validateFailFast(inBytes, JobPropagateFilter.class);
            JobPropagateFilter in = Globals.objectMapper.readValue(inBytes, JobPropagateFilter.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
            if (response == null) {
                response = responseStatus200(Globals.objectMapper.writeValueAsBytes(update(in)));
            }
            return response;
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    private JobReport update(JobPropagateFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            session.setAutoCommit(false);
            Globals.beginTransaction(session);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, null, in.getWorkflowPath(), ConfigurationType.WORKFLOW,
                    folderPermissions);

            JobTemplatesPropagate propagate = new JobTemplatesPropagate(in, folderPermissions.getListOfFolders());

            DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());

            Date now = Date.from(Instant.now());

            JocError jocError = getJocError();
            WorkflowReport report = new WorkflowReport();
            report.setPath(config.getPath());
            report.setJobs(new JobReports());

            Map<String, JobTemplate> jobTemplates = Collections.emptyMap();
            Workflow workflow = JocInventory.workflowContent2Workflow(config.getContent());
            if (workflow != null) {
                if (workflow.getJobs() != null && workflow.getJobs().getAdditionalProperties() != null && !workflow.getJobs()
                        .getAdditionalProperties().isEmpty()) {
                    // determine job templates
                    Job job = workflow.getJobs().getAdditionalProperties().get(in.getJobName());
                    if (job == null) {
                        throw new JocBadRequestException(String.format("Workflow '%s' has no job '%s'.", config.getPath(), in.getJobName()));
                    }
                    String jobTemplateName = null;
                    if (job.getJobTemplate() != null && job.getJobTemplate().getName() != null) {
                        jobTemplateName = job.getJobTemplate().getName();
                    }
                    List<DBItemInventoryReleasedConfiguration> jts = dbLayer.getReleasedJobTemplatesByNames(Collections.singletonList(
                            jobTemplateName));
                    jobTemplates = jts.stream().map(item -> JobTemplatesResourceImpl.getJobTemplate(item, false, jocError)).filter(Objects::nonNull)
                            .collect(Collectors.toMap(JobTemplate::getName, Function.identity()));
                    if (jobTemplateName != null) {
                        jobTemplates.putIfAbsent(jobTemplateName, null);
                    }
                    report = propagate.template2Job(config, workflow, jobTemplates, Collections.singleton(in.getJobName()), dbLayer, now, dbAuditLog);
                }
            }
            
            Globals.commit(session);
            // post events
            if (JobTemplatesPropagate.workflowIsChanged.test(report)) {
                JocInventory.postEvent(getParent(report.getPath()));
                JocInventory.postObjectEvent(report.getPath(), ConfigurationType.WORKFLOW);
            }

            return report.getJobs().getAdditionalProperties().get(in.getJobName());
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
}
