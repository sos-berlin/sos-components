package com.sos.joc.classes.jobtemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.controller.model.jobtemplate.JobTemplate;
import com.sos.inventory.model.instruction.Cycle;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.ForkList;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.job.ExecutableJava;
import com.sos.inventory.model.job.ExecutableType;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.job.JobTemplateRef;
import com.sos.inventory.model.jobtemplate.Parameter;
import com.sos.inventory.model.jobtemplate.Parameters;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JsonConverter;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.jobtemplate.propagate.Actions;
import com.sos.joc.model.jobtemplate.propagate.JobReport;
import com.sos.joc.model.jobtemplate.propagate.JobReportState;
import com.sos.joc.model.jobtemplate.propagate.JobReportStateText;
import com.sos.joc.model.jobtemplate.propagate.JobReports;
import com.sos.joc.model.jobtemplate.propagate.JobTemplatesPropagateBaseFilter;
import com.sos.joc.model.jobtemplate.propagate.WorkflowReport;

public class JobTemplatesPropagate {
    
    private static final Map<ExecutableType, String> EXECUTABLE_STRING = Collections.unmodifiableMap(new HashMap<ExecutableType, String>() {

        private static final long serialVersionUID = 1L;

        {
            put(ExecutableType.InternalExecutable, "JITL");
            put(ExecutableType.ScriptExecutable, "Shell");
            put(ExecutableType.ShellScriptExecutable, "Shell");
        }
    });
    
    private static final Map<JobReportStateText, Integer> PROPAGATE_STATES = Collections.unmodifiableMap(new HashMap<JobReportStateText, Integer>() {

        private static final long serialVersionUID = 1L;

        {
            put(JobReportStateText.SKIPPED, 6);
            put(JobReportStateText.UPTODATE, 6);
            put(JobReportStateText.CHANGED, 5);
            put(JobReportStateText.CONFLICT, 2);
            put(JobReportStateText.PERMISSION_DENIED, 2);
        }
    });
    
    public static Predicate<JobReport> jobIsChanged = jr -> JobReportStateText.CHANGED.equals(jr.getState().get_text());
    public static Predicate<WorkflowReport> workflowIsChanged = wr -> wr.getJobs() != null && wr.getJobs().getAdditionalProperties() != null && !wr
            .getJobs().getAdditionalProperties().isEmpty() && wr.getJobs().getAdditionalProperties().values().stream().anyMatch(jobIsChanged);

    private boolean withAdmissionTime = false;
    private boolean withNotification = false;
    private Set<Folder> permittedFolders = null;
    //private WorkflowReport workflowReport;
    
    public JobTemplatesPropagate() {
        //
    }
    
    public JobTemplatesPropagate(JobTemplatesPropagateBaseFilter filter, Set<Folder> permittedFolders) {
        this.withAdmissionTime = filter.getOverwriteAdmissionTime() == Boolean.TRUE;
        this.withNotification = filter.getOverwriteNotification() == Boolean.TRUE;
        this.permittedFolders = permittedFolders;
    }
    
    public WorkflowReport template2Job(DBItemInventoryConfiguration dbWorkflow, Workflow workflow, Map<String, JobTemplate> jobTemplates,
            InventoryDBLayer dbLayer, Date now, DBItemJocAuditLog dbAuditLog) throws JsonParseException, JsonMappingException, IOException,
            SOSHibernateException {
        WorkflowReport report = template2Job(dbWorkflow.getPath(), workflow, jobTemplates);
        if (workflowIsChanged.test(report)) {
            validate(dbWorkflow, workflow, dbLayer);
            dbWorkflow.setDeployed(false);
            dbWorkflow.setModified(now);
            dbWorkflow.setAuditLogId(dbAuditLog.getId());
            JocInventory.updateConfiguration(dbLayer, dbWorkflow, workflow);
            JocAuditLog.storeAuditLogDetail(new AuditLogDetail(dbWorkflow.getPath(), dbWorkflow.getType()), dbLayer.getSession(), dbAuditLog);
        }
        return report;
    }

    public WorkflowReport template2Job(DBItemInventoryConfiguration dbWorkflow, Map<String, JobTemplate> jobTemplates, InventoryDBLayer dbLayer,
            Date now, DBItemJocAuditLog dbAuditLog) throws JsonParseException, JsonMappingException, IOException, SOSHibernateException {
        Workflow workflow = (Workflow) JocInventory.content2IJSObject(dbWorkflow.getContent(), ConfigurationType.WORKFLOW.intValue());
        return template2Job(dbWorkflow, workflow, jobTemplates, dbLayer, now, dbAuditLog);
    }
    
    private WorkflowReport template2Job(String workflowPath, Workflow w, Map<String, JobTemplate> jobTemplates)
            throws JsonParseException, JsonMappingException, IOException {
        WorkflowReport wReport = new WorkflowReport();
        wReport.setPath(workflowPath);
        wReport.setState(getState(JobReportStateText.SKIPPED));
        wReport.setJobs(new JobReports());
        
        if (w.getJobs() == null || w.getJobs().getAdditionalProperties() == null || w.getJobs().getAdditionalProperties().isEmpty()) {
            return wReport;
        }
        Set<JobReportStateText> jobStates = new HashSet<>();
        for (Map.Entry<String, Job> job : w.getJobs().getAdditionalProperties().entrySet()) {
            JobReport jReport = new JobReport();
            
            if (checkTemplateReference(jobTemplates, job.getKey(), job.getValue(), jReport)) {
                jReport.setState(getState(JobReportStateText.CHANGED));
                JobTemplate jt = jobTemplates.get(job.getValue().getJobTemplate().getName());
                jReport.setJobTemplatePath(jt.getPath());
                template2Job(jt, job.getValue(), jReport);
                switch (jt.getExecutable().getTYPE()) {
                case InternalExecutable:
                    break;
                case ScriptExecutable:
                case ShellScriptExecutable:
                    setNodeArguments(w.getInstructions(), job.getKey(), jReport, jt.getArguments());
                    break;
                }
            }
            
            jobStates.add(jReport.getState().get_text());
            wReport.getJobs().setAdditionalProperty(job.getKey(), jReport);
        }
        
        if (jobStates.contains(JobReportStateText.CONFLICT)) {
            wReport.setState(getState(JobReportStateText.CONFLICT));
        } else if (jobStates.contains(JobReportStateText.CHANGED)) {
            wReport.setState(getState(JobReportStateText.CHANGED));
        } else if (jobStates.contains(JobReportStateText.UPTODATE)) {
            wReport.setState(getState(JobReportStateText.UPTODATE));
        }
        return wReport;
    }
    
    private void template2Job(JobTemplate jt, Job j, JobReport jReport) {
        if (withAdmissionTime) {
            j.setAdmissionTimeScheme(jt.getAdmissionTimeScheme());
            j.setSkipIfNoAdmissionForOrderDay(jt.getSkipIfNoAdmissionForOrderDay());
        }
        if (withNotification) {
            j.setNotification(jt.getNotification());
        }
        j.setCriticality(jt.getCriticality());
        //j.setDefaultArguments(jt.getDefaultArguments());
        j.setDocumentationName(jt.getDocumentationName());
        j.setFailOnErrWritten(jt.getFailOnErrWritten());
        j.setGraceTimeout(jt.getGraceTimeout());
        j.setJobResourceNames(jt.getJobResourceNames());
        j.getJobTemplate().setHash(jt.getHash());
        j.setParallelism(jt.getParallelism());
        j.setTimeout(jt.getTimeout());
        j.setTitle(jt.getTitle());
        j.setWarnIfLonger(jt.getWarnIfLonger());
        j.setWarnIfShorter(jt.getWarnIfShorter());
        
        setExecutable(jReport, j, jt);
    }
    
    private boolean checkTemplateReference(Map<String, JobTemplate> jobTemplates, String jobName, Job job, JobReport jReport) {
        JobTemplateRef jtRef = job.getJobTemplate();
        if (jtRef == null || jtRef.getName() == null || jtRef.getName().isEmpty()) {
            job.setJobTemplate(null);
            jReport.setState(getState(JobReportStateText.SKIPPED, String.format("Job '%s' is not created from a job template", jobName)));
            return false;
        }
        Set<String> jobTemplateNames = jobTemplates.keySet();
        if (!jobTemplateNames.contains(jtRef.getName())) {
            if (jobTemplates.size() == 1) {
                jReport.setState(getState(JobReportStateText.SKIPPED, String.format("Job '%s' is not created from the job template '%s'", jobName,
                        jobTemplateNames.iterator().next())));
            } else {
                jReport.setState(getState(JobReportStateText.SKIPPED, String.format("Job '%s' is not created from the job templates %s", jobName,
                        jobTemplateNames.toString())));
            }
            return false;
        }
        JobTemplate jobTemplate = jobTemplates.get(jtRef.getName());
        if (jtRef.getHash() != null && jtRef.getHash().equals(jobTemplate.getHash())) {
            jReport.setJobTemplatePath(jobTemplate.getPath());
            jReport.setState(getState(JobReportStateText.UPTODATE, String.format(
                    "Job '%s' is created from the job template '%s'. Updating the job is not necessary because version is up to date.", jobName,
                    jobTemplate.getName())));
            return false;
        }
        if (!JOCResourceImpl.canAdd(jobTemplate.getPath(), permittedFolders)) {
            jReport.setState(getState(JobReportStateText.PERMISSION_DENIED, String.format("Job '%s' is created from the job template '%s'", jobName,
                    jobTemplate.getPath())));
        }
        if (!job.getExecutable().getTYPE().equals(jobTemplate.getExecutable().getTYPE())) {
            jReport.setJobTemplatePath(jobTemplate.getPath());
            jReport.setState(getState(JobReportStateText.CONFLICT, String.format("Job '%s' is a %s job and the job template '%s' specifies a %s job",
                    jobName, EXECUTABLE_STRING.get(job.getExecutable().getTYPE()), jobTemplate.getName(), EXECUTABLE_STRING.get(jobTemplate
                            .getExecutable().getTYPE()))));
            return false;
        }
        return true;
    }
    
    private static void setExecutable(JobReport jReport, Job j, JobTemplate jt) {
        switch (jt.getExecutable().getTYPE()) {
        case InternalExecutable:
            
            ExecutableJava e = j.getExecutable().cast();
            ExecutableJava jtE = jt.getExecutable().cast();
            
            e.setClassName(jtE.getClassName());
            e.setJobArguments(jtE.getJobArguments());
            e.setReturnCodeMeaning(jtE.getReturnCodeMeaning());
            
            e.setArguments(setArguments(jReport, e.getArguments(), jt.getArguments()));
            
            break;
        case ScriptExecutable:
        case ShellScriptExecutable:
            j.setExecutable(jt.getExecutable());
            break;
        }
    }
    
    private static void setNodeArguments(JobReport jReport, NamedJob j, Parameters args) {
        j.setDefaultArguments(setArguments(jReport, j.getDefaultArguments(), args));
    }
    
    private static Environment setArguments(JobReport jReport, Environment env, Parameters args) {
        Actions actions = new Actions();
        actions.setChanges(null); // TODO fill the changes
        
        boolean withAction = false;

        if (env == null || env.getAdditionalProperties() == null) {
            env = new Environment(); 
        }
        if (args == null || args.getAdditionalProperties() == null) {
            args = new Parameters();
        }
        Set<String> paramKeys = args.getAdditionalProperties().keySet();
        
        // delete unknown keys
        Set<String> keysToDelete = env.getAdditionalProperties().keySet().stream().filter(key -> !paramKeys.contains(key)).collect(
                Collectors.toSet());
        
        if (!keysToDelete.isEmpty()) {
            Environment deletedEnv = new Environment();
            for (String key : keysToDelete) {
                deletedEnv.setAdditionalProperty(key, env.getAdditionalProperties().get(key));
            }
            actions.setDeleteArguments(deletedEnv);
            withAction = true;
        }
        
        for (String key : keysToDelete) {
            env.getAdditionalProperties().remove(key);
        }
        
        // add required new keys with default value
        Environment addEnv = new Environment();
        for (Map.Entry<String, Parameter> entry : args.getAdditionalProperties().entrySet()) {
            if (entry.getValue().getRequired()) {
                // TODO conflict required without default value
                String _default = entry.getValue().getDefault() != null ? entry.getValue().getDefault().toString() : "";
                if (_default.isEmpty()) {
                  //TODO store this keys for report
                }
                addEnv.setAdditionalProperty(entry.getKey(), JsonConverter.quoteString(_default));
                env.setAdditionalProperty(entry.getKey(), JsonConverter.quoteString(_default));
            }
        }
        
        if (!addEnv.getAdditionalProperties().isEmpty()) {
            actions.setAddRequiredArguments(addEnv);
            withAction = true;
        }
        if (!withAction) {
            actions = null;
        }
        jReport.setActions(actions);
        
        if (env.getAdditionalProperties().isEmpty()) {
            env = null;
        }
        
        return env;
    }
    
    private static void setNodeArguments(List<Instruction> insts, String jobName, JobReport jReport, Parameters args) {
        if (insts != null) {
            for (Instruction inst : insts) {
                switch (inst.getTYPE()) {
                case FORK:
                    ForkJoin f = inst.cast();
                    if (f.getBranches() != null) {
                        for (Branch b : f.getBranches()) {
                            setNodeArguments(b.getWorkflow().getInstructions(), jobName, jReport, args);
                        }
                    }
                    break;
                case FORKLIST:
                    ForkList fl = inst.cast();
                    if (fl.getWorkflow() != null) {
                        setNodeArguments(fl.getWorkflow().getInstructions(), jobName, jReport, args);
                    }
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    if (ie.getThen() != null) {
                        setNodeArguments(ie.getThen().getInstructions(), jobName, jReport, args);
                    }
                    if (ie.getElse() != null) {
                        setNodeArguments(ie.getElse().getInstructions(), jobName, jReport, args);
                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    if (tc.getTry() != null) {
                        setNodeArguments(tc.getTry().getInstructions(), jobName, jReport, args);
                    }
                    if (tc.getCatch() != null) {
                        setNodeArguments(tc.getCatch().getInstructions(), jobName, jReport, args);
                    }
                    break;
                case LOCK:
                    Lock l = inst.cast();
                    if (l.getLockedWorkflow() != null) {
                        setNodeArguments(l.getLockedWorkflow().getInstructions(), jobName, jReport, args);
                    }
                    break;
                case CYCLE:
                    Cycle c = inst.cast();
                    if (c.getCycleWorkflow() != null) {
                        setNodeArguments(c.getCycleWorkflow().getInstructions(), jobName, jReport, args);
                    }
                    break;
                case EXECUTE_NAMED:
                    NamedJob j = inst.cast();
                    if (j.getJobName().equals(jobName)) {
                        setNodeArguments(jReport, j, args);
                    }
                default:
                    break;
                }
            }
        }
    }
    
    public static JobReportState getState(JobReportStateText state) {
        return getState(state, null);
    }
    
    public static JobReportState getState(JobReportStateText state, String message) {
        JobReportState s = new JobReportState();
        s.setMessage(message);
        s.set_text(state);
        s.setSeverity(PROPAGATE_STATES.get(state));
        return s;
    }
    
    private static void validate(DBItemInventoryConfiguration item, Workflow workflow, InventoryDBLayer dbLayer) {

        try {
            item.setContent(JocInventory.toString(workflow));
            Validator.validate(ConfigurationType.WORKFLOW, workflow, dbLayer, null);
            item.setValid(true);
        } catch (Throwable e) {
            item.setValid(false);
        }
    }
    
}
