package com.sos.joc.jobtemplates.impl;

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

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.controller.model.jobtemplate.JobTemplate;
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
import com.sos.joc.jobtemplates.resource.IJobTemplatesPropagate;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.jobtemplate.JobTemplatesFilter;
import com.sos.joc.model.jobtemplate.propagate.JobReportStateText;
import com.sos.joc.model.jobtemplate.propagate.JobTemplatePropagateFilter;
import com.sos.joc.model.jobtemplate.propagate.JobTemplatesPropagateFilter;
import com.sos.joc.model.jobtemplate.propagate.Report;
import com.sos.joc.model.jobtemplate.propagate.WorkflowReport;
import com.sos.schema.JsonValidator;

@Path("job_templates")
public class JobTemplatesPropagateImpl extends JOCResourceImpl implements IJobTemplatesPropagate {

    private static final String API_CALL = "./job_templates/propagate";
    private class PropagateFilter {
        
        private String jobTemplate;
        private String workflow;
        
        public PropagateFilter(String jobTemplate, String workflow) {
            this.jobTemplate = JocInventory.pathToName(jobTemplate);
            this.workflow = JocInventory.pathToName(workflow);
        }
        
        public String getJobTemplate() {
            return jobTemplate;
        }
        
        public String getWorkflow() {
            return workflow;
        }
    }

    @Override
    public JOCDefaultResponse propagateJobTemplates(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, JobTemplatesPropagateFilter.class);
            JobTemplatesPropagateFilter jobTemplatesFilter = Globals.objectMapper.readValue(filterBytes, JobTemplatesPropagateFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getInventory().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            
            List<String> jobTemplateNames = jobTemplatesFilter.getJobTemplates().stream().map(JobTemplatePropagateFilter::getPath).map(
                    JocInventory::pathToName).distinct().collect(Collectors.toList());
            List<DBItemInventoryReleasedConfiguration> dbJobTemplates = dbLayer.getReleasedJobTemplatesByNames(jobTemplateNames);

            // TODO maybe map for reporting access denied job templates?
            Stream<DBItemInventoryReleasedConfiguration> permittedDbJobTemplatesStream = dbJobTemplates.stream().filter(jt -> folderIsPermitted(jt
                    .getFolder(), permittedFolders));
            JocError jocError = getJocError();
            Map<String, JobTemplate> jobTemplatesMap = permittedDbJobTemplatesStream.map(item -> JobTemplatesResourceImpl.getJobTemplate(item, false,
                    jocError)).filter(Objects::nonNull).collect(Collectors.toMap(JobTemplate::getName, Function.identity()));
            Set<String> permittedJobTemplateNames = jobTemplatesMap.keySet();

            Map<String, Set<String>> jobTemplateNamesPerWorkflowName = Collections.emptyMap();
            if (!permittedJobTemplateNames.isEmpty()) {
                jobTemplateNamesPerWorkflowName = jobTemplatesFilter.getJobTemplates().stream().flatMap(jt -> jt.getWorkflows().stream().map(
                        w -> new PropagateFilter(jt.getPath(), w))).filter(p -> permittedJobTemplateNames.contains(p.getJobTemplate())).collect(
                                Collectors.groupingBy(PropagateFilter::getWorkflow, Collectors.mapping(PropagateFilter::getJobTemplate, Collectors
                                        .toSet())));
            }
            
            Report report = new Report();
            Date now = Date.from(Instant.now());
            report.setDeliveryDate(now);
            if (!jobTemplateNamesPerWorkflowName.isEmpty()) {

                JobTemplatesPropagate propagate = new JobTemplatesPropagate(jobTemplatesFilter, permittedFolders);

                DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), jobTemplatesFilter.getAuditLog());

                for (Map.Entry<String, Set<String>> entry : jobTemplateNamesPerWorkflowName.entrySet()) {
                    String workflowName = entry.getKey();
                    DBItemInventoryConfiguration dbWorkflow = dbLayer.getConfigurationByName(JocInventory.pathToName(workflowName),
                            ConfigurationType.WORKFLOW.intValue()).get(0);
                    if (dbWorkflow == null) {
                        continue; // TODO report?
                    }
                    if (!folderIsPermitted(dbWorkflow.getFolder(), permittedFolders)) {
                        WorkflowReport wr = new WorkflowReport();
                        wr.setWorkflowPath(dbWorkflow.getPath());
                        wr.setState(JobTemplatesPropagate.getState(JobReportStateText.PERMISSION_DENIED));
                        continue;
                    }
                    Map<String, JobTemplate> jobTemplates = entry.getValue().stream().map(jobTemplateName -> jobTemplatesMap.get(jobTemplateName))
                            .filter(Objects::nonNull).collect(Collectors.toMap(JobTemplate::getName, Function.identity()));
                    report.getWorkflows().add(propagate.template2Job(dbWorkflow, jobTemplates, dbLayer, now, dbAuditLog));
                }
                // post events
                if (!report.getWorkflows().isEmpty()) {
                    report.getWorkflows().stream().filter(JobTemplatesPropagate.workflowIsChanged).map(WorkflowReport::getWorkflowPath).map(
                            path -> getParent(path)).distinct().forEach(folder -> JocInventory.postEvent(folder));
                }
            }
            Globals.commit(session);

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(report));
        } catch (JocException e) {
            Globals.rollback(session);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(session);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }
    
    public static List<DBItemInventoryReleasedConfiguration> getDbJobTemplates(JobTemplatesFilter jobTemplatesFilter, Set<Folder> folders,
            InventoryDBLayer dbLayer) throws SOSHibernateException {

        List<DBItemInventoryReleasedConfiguration> dbJobTemplates = null;
        boolean withFolderFilter = jobTemplatesFilter.getFolders() != null && !jobTemplatesFilter.getFolders().isEmpty();

        if (jobTemplatesFilter.getJobTemplatePaths() != null && !jobTemplatesFilter.getJobTemplatePaths().isEmpty()) {
            dbJobTemplates = dbLayer.getReleasedJobTemplatesByNames(jobTemplatesFilter.getJobTemplatePaths().stream().map(p -> JocInventory
                    .pathToName(p)).distinct().collect(Collectors.toList()));

        } else if (withFolderFilter && (folders == null || folders.isEmpty())) {
            // no folder permission
        } else {
            dbJobTemplates = dbLayer.getConfigurationsByType(Collections.singletonList(ConfigurationType.JOBTEMPLATE.intValue()));
        }
        return dbJobTemplates;
    }

}