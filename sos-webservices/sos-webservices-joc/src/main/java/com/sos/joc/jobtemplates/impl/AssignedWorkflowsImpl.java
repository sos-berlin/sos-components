package com.sos.joc.jobtemplates.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.jobtemplate.JobTemplate;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobtemplates.resource.IAssignedWorkflows;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.jobtemplate.JobTemplateState;
import com.sos.joc.model.jobtemplate.JobTemplateStateText;
import com.sos.joc.model.jobtemplate.JobTemplateUsedBy;
import com.sos.joc.model.jobtemplate.JobTemplateUsedByJobs;
import com.sos.joc.model.jobtemplate.JobTemplateUsedByWorkflow;
import com.sos.joc.model.jobtemplate.JobTemplateWorkflowState;
import com.sos.joc.model.jobtemplate.JobTemplateWorkflowStateText;
import com.sos.joc.model.jobtemplate.JobTemplatesFilter;
import com.sos.joc.model.jobtemplate.JobTemplatesUsedBy;
import com.sos.schema.JsonValidator;

@Path("job_templates")
public class AssignedWorkflowsImpl extends JOCResourceImpl implements IAssignedWorkflows {

    private static final String API_CALL = "./job_templates/used";
    private static final Map<JobTemplateStateText, Integer> TEMPLATE_STATES = Collections.unmodifiableMap(new HashMap<JobTemplateStateText, Integer>() {

        private static final long serialVersionUID = 1L;

        {
            put(JobTemplateStateText.IN_SYNC, 6);
            put(JobTemplateStateText.NOT_IN_SYNC, 5);
            put(JobTemplateStateText.DELETED, 11);
            put(JobTemplateStateText.UNKNOWN, 2);
        }
    });
    
    private static final Map<JobTemplateWorkflowStateText, Integer> WORKFLOW_STATES = Collections.unmodifiableMap(new HashMap<JobTemplateWorkflowStateText, Integer>() {

        private static final long serialVersionUID = 1L;

        {
            put(JobTemplateWorkflowStateText.IN_SYNC, 6);
            put(JobTemplateWorkflowStateText.NOT_IN_SYNC, 5);
        }
    });

    @Override
    public JOCDefaultResponse postWorkflows(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, JobTemplatesFilter.class);
            JobTemplatesFilter jobTemplatesFilter = Globals.objectMapper.readValue(filterBytes, JobTemplatesFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getInventory().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            final Set<Folder> folders = folderPermissions.getPermittedFolders(jobTemplatesFilter.getFolders());
            
            List<DBItemInventoryReleasedConfiguration> dbJobTemplates = JobTemplatesResourceImpl.getDbJobTemplates(jobTemplatesFilter, folders,
                    dbLayer);
            JobTemplatesUsedBy entity = new JobTemplatesUsedBy();

            if (dbJobTemplates != null && !dbJobTemplates.isEmpty()) {
                
                
                List<JobTemplateUsedBy> jobTemplates = new ArrayList<>();
                
                for (DBItemInventoryReleasedConfiguration dbJobTemplate : dbJobTemplates) {
                    if (!folderIsPermitted(dbJobTemplate.getFolder(), folders)) {
                        continue;
                    }
                        
                    JobTemplateUsedBy jtUsedBy = new JobTemplateUsedBy();
                    jtUsedBy.setName(dbJobTemplate.getName());
                    jtUsedBy.setPath(dbJobTemplate.getPath());
                    JobTemplate jt = (JobTemplate) JocInventory.content2IJSObject(dbJobTemplate.getContent(), ConfigurationType.JOBTEMPLATE.intValue());
                    jtUsedBy.setHash(jt.getHash());
                    
                    List<JobTemplateUsedByWorkflow> jtWorkflows = new ArrayList<>();
                    List<DBItemInventoryConfiguration> workflows = dbLayer.getUsedWorkflowsByJobTemplateName(dbJobTemplate.getName());
                    for (DBItemInventoryConfiguration workflow : workflows) {
                        
                        JobTemplateUsedByWorkflow jtW = new JobTemplateUsedByWorkflow();
                        jtW.setName(workflow.getName());
                        jtW.setPath(workflow.getPath());
                        jtW.setDeployed(workflow.getDeployed());
                        
                        boolean workflowIsInSync = true;
                        Workflow w = (Workflow) JocInventory.content2IJSObject(workflow.getContent(), ConfigurationType.WORKFLOW.intValue());
                        JobTemplateUsedByJobs jtJobs = new JobTemplateUsedByJobs();
                        for (Map.Entry<String, Job> wJob : w.getJobs().getAdditionalProperties().entrySet()) {
                            Job j = wJob.getValue();
                            if (j.getJobTemplate() != null && j.getJobTemplate().getName() != null && j.getJobTemplate().getName().equals(
                                    dbJobTemplate.getName())) {
                                if (j.getJobTemplate().getHash() != null && j.getJobTemplate().getHash().equals(jt.getHash())) {
                                    jtJobs.setAdditionalProperty(wJob.getKey(), getState(JobTemplateStateText.IN_SYNC));
                                } else {
                                    jtJobs.setAdditionalProperty(wJob.getKey(), getState(JobTemplateStateText.NOT_IN_SYNC));
                                    workflowIsInSync = false;
                                }
                            }
                        }
                        jtW.setState(getWorkflowState(workflowIsInSync));
                        if (!jtJobs.getAdditionalProperties().isEmpty()) {
                            jtW.setJobs(jtJobs);
                            jtWorkflows.add(jtW);
                        }
                    }
                    if (!jtWorkflows.isEmpty()) {
                        jtUsedBy.setWorkflows(jtWorkflows);
                        jobTemplates.add(jtUsedBy);
                    }
                }

                entity.setJobTemplates(jobTemplates);
            }

            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }
    
    public static JobTemplateState getState(JobTemplateStateText state) {
        JobTemplateState s = new JobTemplateState();
        s.set_text(state);
        s.setSeverity(TEMPLATE_STATES.get(state));
        return s;
    }
    
    private static JobTemplateWorkflowState getWorkflowState(boolean workflowIsInSync) {
        JobTemplateWorkflowState s = new JobTemplateWorkflowState();
        JobTemplateWorkflowStateText state = workflowIsInSync ? JobTemplateWorkflowStateText.IN_SYNC
                : JobTemplateWorkflowStateText.NOT_IN_SYNC;
        s.set_text(state);
        s.setSeverity(WORKFLOW_STATES.get(state));
        return s;
    }

}