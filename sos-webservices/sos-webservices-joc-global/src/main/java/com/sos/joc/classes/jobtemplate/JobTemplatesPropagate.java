package com.sos.joc.classes.jobtemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
            put(JobReportStateText.TEMPLATE_REFERENCE_DELETED, 5);
            put(JobReportStateText.CONFLICT, 2);
            put(JobReportStateText.PERMISSION_DENIED, 2);
        }
    });
    
    private static List<JobReportStateText> changedStates = Arrays.asList(JobReportStateText.CHANGED, JobReportStateText.TEMPLATE_REFERENCE_DELETED);
    public static Predicate<JobReport> jobIsChanged = jr -> changedStates.contains(jr.getState().get_text());
    public static Predicate<WorkflowReport> workflowIsChanged = wr -> wr.getJobs() != null && wr.getJobs().getAdditionalProperties() != null && !wr
            .getJobs().getAdditionalProperties().isEmpty() && wr.getJobs().getAdditionalProperties().values().stream().anyMatch(jobIsChanged);

    private boolean withAdmissionTime = false;
    private boolean withNotification = false;
    private Set<Folder> permittedFolders = null;
    
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
        return template2Job(dbWorkflow, workflow, jobTemplates, null, dbLayer, now, dbAuditLog);
    }
    
    public WorkflowReport template2Job(DBItemInventoryConfiguration dbWorkflow, Workflow workflow, Map<String, JobTemplate> jobTemplates,
            Set<String> jobNames, InventoryDBLayer dbLayer, Date now, DBItemJocAuditLog dbAuditLog) throws JsonParseException, JsonMappingException,
            IOException, SOSHibernateException {
        WorkflowReport report = template2Job(dbWorkflow.getPath(), workflow, jobTemplates, jobNames);
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
    
    private WorkflowReport template2Job(String workflowPath, Workflow w, Map<String, JobTemplate> jobTemplates, Set<String> jobNames)
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
            
            if (checkTemplateReference(jobTemplates, jobNames, job.getKey(), job.getValue(), jReport)) {
                JobTemplate jt = jobTemplates.get(job.getValue().getJobTemplate().getName());
                if (jt == null) {
                    jReport.setJobTemplatePath(job.getValue().getJobTemplate().getName());
                    jReport.setActions(new Actions());
                    job.getValue().setJobTemplate(null);
                    jReport.getActions().setChanges(Collections.singletonList("jobTemplate"));
                } else {
                    template2Job(jReport, jt, job.getKey(), job.getValue(), w);
                }
            }
            
            jobStates.add(jReport.getState().get_text());
            wReport.getJobs().setAdditionalProperty(job.getKey(), jReport);
        }
        
        if (jobStates.contains(JobReportStateText.CONFLICT)) {
            wReport.setState(getState(JobReportStateText.CONFLICT));
        } else if (jobStates.contains(JobReportStateText.CHANGED) || jobStates.contains(JobReportStateText.TEMPLATE_REFERENCE_DELETED)) {
            wReport.setState(getState(JobReportStateText.CHANGED));
        } else if (jobStates.contains(JobReportStateText.UPTODATE)) {
            wReport.setState(getState(JobReportStateText.UPTODATE));
        }
        return wReport;
    }
    
    private void template2Job(JobReport jReport, JobTemplate jt, String jobName, Job job, Workflow w) {
        Environment env = getArguments(jReport, jt, jobName, job, w);
        
        jReport.setState(getState(JobReportStateText.CHANGED));
        jReport.setJobTemplatePath(jt.getPath());
        jReport.setActions(new Actions());
        switch (jt.getExecutable().getTYPE()) {
        case InternalExecutable:
            if (env == null) {
                setNodeArguments(w.getInstructions(), jobName, jReport, jt.getArguments(), null, false);
            }
            template2Job(jt, job, jReport, env);
            break;
        case ScriptExecutable:
        case ShellScriptExecutable:
            template2Job(jt, job, jReport, env);
            setNodeArguments(w.getInstructions(), jobName, jReport, jt.getArguments(), env, true);
            break;
        }
        Actions actions = jReport.getActions();
        if (actions.getChanges() != null && actions.getChanges().isEmpty()) {
            actions.setChanges(null);
        }
        if (actions.getChanges() == null && actions.getAddRequiredArguments() == null && actions.getDeleteArguments() == null) {
            jReport.setActions(null);
        }
    }
    
    private static <T> boolean isNotEqual(T o1, T o2) {
        Optional<T> opt1 = o1 == null ? Optional.empty() : Optional.of(o1);
        Optional<T> opt2 = o2 == null ? Optional.empty() : Optional.of(o2);
        return !opt1.equals(opt2);
    }
    
    private void template2Job(JobTemplate jt, Job j, JobReport jReport, Environment arguments) {
        Actions actions = jReport.getActions();
        if (actions.getChanges() == null) {
            actions.setChanges(new ArrayList<String>());
        }
        if (withAdmissionTime) {
            if (isNotEqual(j.getAdmissionTimeScheme(), jt.getAdmissionTimeScheme())) {
                actions.getChanges().add("admissionTimeScheme");
            }
            j.setAdmissionTimeScheme(jt.getAdmissionTimeScheme());
            
//            if (isNotEqual(j.getSkipIfNoAdmissionForOrderDay(), jt.getSkipIfNoAdmissionForOrderDay())) {
//                actions.getChanges().add("skipIfNoAdmissionForOrderDay");
//            }
//            //never updated j.setSkipIfNoAdmissionForOrderDay(jt.getSkipIfNoAdmissionForOrderDay());
        }
        if (withNotification) {
            if (isNotEqual(j.getNotification(), jt.getNotification())) {
                actions.getChanges().add("notification");
            }
            j.setNotification(jt.getNotification());
        }
        
        if (isNotEqual(j.getCriticality(), jt.getCriticality())) {
            actions.getChanges().add("criticality");
        }
        j.setCriticality(jt.getCriticality());
        //j.setDefaultArguments(jt.getDefaultArguments());
        
        if (isNotEqual(j.getDocumentationName(), jt.getDocumentationName())) {
            actions.getChanges().add("documentationName");
        }
        j.setDocumentationName(jt.getDocumentationName());
        
        if (isNotEqual(j.getFailOnErrWritten(), jt.getFailOnErrWritten())) {
            actions.getChanges().add("failOnErrWritten");
        }
        j.setFailOnErrWritten(jt.getFailOnErrWritten());
        
        if (isNotEqual(j.getWarnOnErrWritten(), jt.getWarnOnErrWritten())) {
            actions.getChanges().add("warnOnErrWritten");
        }
        j.setWarnOnErrWritten(jt.getWarnOnErrWritten());
        
        if (isNotEqual(j.getGraceTimeout(), jt.getGraceTimeout())) {
            actions.getChanges().add("graceTimeout");
        }
        j.setGraceTimeout(jt.getGraceTimeout());
        
        if (isNotEqual(j.getJobResourceNames(), jt.getJobResourceNames())) {
            actions.getChanges().add("jobResourceNames");
        }
        j.setJobResourceNames(jt.getJobResourceNames());
        
        j.getJobTemplate().setHash(jt.getHash());
        
        if (isNotEqual(j.getParallelism(), jt.getParallelism())) {
            actions.getChanges().add("parallelism");
        }
        j.setParallelism(jt.getParallelism());
        
        if (isNotEqual(j.getTimeout(), jt.getTimeout())) {
            actions.getChanges().add("timeout");
        }
        j.setTimeout(jt.getTimeout());
        
        if (isNotEqual(j.getTitle(), jt.getTitle())) {
            actions.getChanges().add("title");
        }
        j.setTitle(jt.getTitle());
        
        if (isNotEqual(j.getWarnIfLonger(), jt.getWarnIfLonger())) {
            actions.getChanges().add("warnIfLonger");
        }
        j.setWarnIfLonger(jt.getWarnIfLonger());
        
        if (isNotEqual(j.getWarnIfShorter(), jt.getWarnIfShorter())) {
            actions.getChanges().add("warnIfShorter");
        }
        j.setWarnIfShorter(jt.getWarnIfShorter());
        
        setExecutable(jReport, j, jt, arguments);
    }
    
    private boolean checkTemplateReference(Map<String, JobTemplate> jobTemplates, Set<String> jobNames, String jobName, Job job, JobReport jReport) {
        if (jobNames != null && !jobNames.contains(jobName)) {
            jReport.setState(getState(JobReportStateText.SKIPPED, String.format("Updating Job '%s' is not requested", jobName)));
            return false;
        }
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
        if (jobTemplate == null) {
            jReport.setState(getState(JobReportStateText.TEMPLATE_REFERENCE_DELETED, String.format(
                    "Job '%s' has a job template reference '%s' but this job template doesn't exist", jobName, jtRef.getName())));
            return true;
        }
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
            return true;
        }
        return true;
    }
    
    private static void setExecutable(JobReport jReport, Job j, JobTemplate jt, Environment arguments) {
        Actions actions = jReport.getActions();
        if (actions.getChanges() == null) {
            actions.setChanges(new ArrayList<String>());
        }
        
        switch (jt.getExecutable().getTYPE()) {
        case InternalExecutable:
            
            ExecutableJava e = new ExecutableJava();
            e.setTYPE(ExecutableType.InternalExecutable);
            if (ExecutableType.InternalExecutable.equals(j.getExecutable().getTYPE())) {
                e = j.getExecutable().cast();
            }
            ExecutableJava jtE = jt.getExecutable().cast();
            
            if (isNotEqual(e.getClassName(), jtE.getClassName()) || isNotEqual(e.getJobArguments(), jtE.getJobArguments()) || isNotEqual(e
                    .getReturnCodeMeaning(), jtE.getReturnCodeMeaning())) {
                actions.getChanges().add("executable");
            }
            e.setClassName(jtE.getClassName());
            e.setJobArguments(jtE.getJobArguments());
            e.setReturnCodeMeaning(jtE.getReturnCodeMeaning());
            
            if (arguments != null && arguments.getAdditionalProperties() != null) {
                e.setArguments(arguments);
            }
            e.setArguments(setArguments(jReport, e.getArguments(), jt.getArguments()));
            
            if (!ExecutableType.InternalExecutable.equals(j.getExecutable().getTYPE())) {
                j.setExecutable(e);
            }
            
            break;
        case ScriptExecutable:
        case ShellScriptExecutable:
            if (isNotEqual(j.getExecutable(), jt.getExecutable())) {
                actions.getChanges().add("executable");
            }
            j.setExecutable(jt.getExecutable());
            break;
        }
    }
    
    private static Environment getArguments(JobReport jReport, JobTemplate jt, String jobName, Job job, Workflow w) {
        if (jReport.getState() == null || jReport.getState().get_text() == null) {
            return null;
        }
        if (!JobReportStateText.CONFLICT.equals(jReport.getState().get_text())) {
            return null;
        }
        Environment env = null;
        switch (jt.getExecutable().getTYPE()) {
        case InternalExecutable:
            env = getNodeArguments(w.getInstructions(), jobName);
            break;
        case ScriptExecutable:
        case ShellScriptExecutable:
            env = getExecutableArguments(job);
            break;
        }
        return env;
    }
    
    private static Environment getExecutableArguments(Job j) {
        Environment env = new Environment();
        switch (j.getExecutable().getTYPE()) {
        case InternalExecutable:
            
            ExecutableJava e = j.getExecutable().cast();
            // copy NodeArgument
            if (e.getArguments() != null && e.getArguments().getAdditionalProperties() != null) {
                e.getArguments().getAdditionalProperties().forEach((k, v) -> env.setAdditionalProperty(k, v));
            }
            // delete NodeArgument
            e.setArguments(null);
            
            break;
        case ScriptExecutable:
        case ShellScriptExecutable:
            break;
        }
        return env;
    }
    
    private static void setNodeArguments(JobReport jReport, NamedJob j, Parameters args, Environment defaultArgs, boolean withAddRequiredParams) {
        if (defaultArgs != null && defaultArgs.getAdditionalProperties() != null) {
            j.setDefaultArguments(defaultArgs);
        }
        j.setDefaultArguments(setArguments(jReport, j.getDefaultArguments(), args, withAddRequiredParams));
    }
    
    private static Environment readNodeArguments(NamedJob j, Environment env) {
        
        // copy NodeArgument
        if (j.getDefaultArguments() != null && j.getDefaultArguments().getAdditionalProperties() != null) {
            j.getDefaultArguments().getAdditionalProperties().forEach((k, v) -> env.setAdditionalProperty(k, v));
        }
        // delete NodeArgument
        j.setDefaultArguments(null);
        return env;
    }
    
    private static Environment setArguments(JobReport jReport, Environment env, Parameters args) {
        return setArguments(jReport, env, args, true);
    }
    
    private static Environment setArguments(JobReport jReport, Environment env, Parameters args, boolean withAddRequiredParams) {
        Actions actions = jReport.getActions();
        
        if (env == null || env.getAdditionalProperties() == null) {
            env = new Environment(); 
        }
        if (args == null || args.getAdditionalProperties() == null) {
            args = new Parameters();
        }
        Set<String> paramKeys = args.getAdditionalProperties().keySet();
        Set<String> envKeys = env.getAdditionalProperties().keySet();
        
        // delete unknown keys
        Set<String> keysToDelete = envKeys.stream().filter(key -> !paramKeys.contains(key)).collect(Collectors.toSet());

        if (!keysToDelete.isEmpty()) {
            if (actions.getDeleteArguments() == null) {
                actions.setDeleteArguments(new Environment());
            }
            Environment deletedEnv = actions.getDeleteArguments();
            for (String key : keysToDelete) {
                deletedEnv.setAdditionalProperty(key, env.getAdditionalProperties().get(key));
            }
        }
        
        for (String key : keysToDelete) {
            env.getAdditionalProperties().remove(key);
        }
        
        // add required new keys with default value
        if (actions.getAddRequiredArguments() == null) {
            actions.setAddRequiredArguments(new Environment());
        }
        Environment addEnv = actions.getAddRequiredArguments();
        List<String> foundRequiredParams = new ArrayList<>();
        for (Map.Entry<String, Parameter> entry : args.getAdditionalProperties().entrySet()) {
            if (entry.getValue().getRequired()) {
                if (withAddRequiredParams && !envKeys.contains(entry.getKey())) {

                    // TODO conflict required without default value
                    String _default = entry.getValue().getDefault() != null ? entry.getValue().getDefault().toString() : "";
                    if (_default.isEmpty()) {
                        // TODO store this keys for report
                    }
                    addEnv.setAdditionalProperty(entry.getKey(), JsonConverter.quoteString(_default));
                    env.setAdditionalProperty(entry.getKey(), JsonConverter.quoteString(_default));
                    
                } else if (envKeys.contains(entry.getKey())) {
                    foundRequiredParams.add(entry.getKey());
                }
            }
        }
        
        if (!foundRequiredParams.isEmpty()) {
            for (String key : foundRequiredParams) {
                args.getAdditionalProperties().remove(key);
            }
        }

        if (addEnv.getAdditionalProperties().isEmpty()) {
            actions.setAddRequiredArguments(null);
        }

        if (env.getAdditionalProperties().isEmpty()) {
            env = null;
        }
            
        return env;
    }
    
    private static void setNodeArguments(List<Instruction> insts, String jobName, JobReport jReport, Parameters args, Environment defaultArgs,
            boolean withAddRequiredParams) {
        if (insts != null) {
            for (Instruction inst : insts) {
                switch (inst.getTYPE()) {
                case FORK:
                    ForkJoin f = inst.cast();
                    if (f.getBranches() != null) {
                        for (Branch b : f.getBranches()) {
                            setNodeArguments(b.getWorkflow().getInstructions(), jobName, jReport, args, defaultArgs, withAddRequiredParams);
                        }
                    }
                    break;
                case FORKLIST:
                    ForkList fl = inst.cast();
                    if (fl.getWorkflow() != null) {
                        setNodeArguments(fl.getWorkflow().getInstructions(), jobName, jReport, args, defaultArgs, withAddRequiredParams);
                    }
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    if (ie.getThen() != null) {
                        setNodeArguments(ie.getThen().getInstructions(), jobName, jReport, args, defaultArgs, withAddRequiredParams);
                    }
                    if (ie.getElse() != null) {
                        setNodeArguments(ie.getElse().getInstructions(), jobName, jReport, args, defaultArgs, withAddRequiredParams);
                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    if (tc.getTry() != null) {
                        setNodeArguments(tc.getTry().getInstructions(), jobName, jReport, args, defaultArgs, withAddRequiredParams);
                    }
                    if (tc.getCatch() != null) {
                        setNodeArguments(tc.getCatch().getInstructions(), jobName, jReport, args, defaultArgs, withAddRequiredParams);
                    }
                    break;
                case LOCK:
                    Lock l = inst.cast();
                    if (l.getLockedWorkflow() != null) {
                        setNodeArguments(l.getLockedWorkflow().getInstructions(), jobName, jReport, args, defaultArgs, withAddRequiredParams);
                    }
                    break;
                case CYCLE:
                    Cycle c = inst.cast();
                    if (c.getCycleWorkflow() != null) {
                        setNodeArguments(c.getCycleWorkflow().getInstructions(), jobName, jReport, args, defaultArgs, withAddRequiredParams);
                    }
                    break;
                case EXECUTE_NAMED:
                    NamedJob j = inst.cast();
                    if (j.getJobName().equals(jobName)) {
                        setNodeArguments(jReport, j, args, defaultArgs, withAddRequiredParams);
                    }
                default:
                    break;
                }
            }
        }
    }
    
    private static Environment getNodeArguments(List<Instruction> insts, String jobName) {
        Environment args = new Environment();
        readNodeArguments(insts, jobName, args);
        return args;
    }
    
    private static void readNodeArguments(List<Instruction> insts, String jobName, Environment args) {
        if (insts != null) {
            for (Instruction inst : insts) {
                switch (inst.getTYPE()) {
                case FORK:
                    ForkJoin f = inst.cast();
                    if (f.getBranches() != null) {
                        for (Branch b : f.getBranches()) {
                            readNodeArguments(b.getWorkflow().getInstructions(), jobName, args);
                        }
                    }
                    break;
                case FORKLIST:
                    ForkList fl = inst.cast();
                    if (fl.getWorkflow() != null) {
                        readNodeArguments(fl.getWorkflow().getInstructions(), jobName, args);
                    }
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    if (ie.getThen() != null) {
                        readNodeArguments(ie.getThen().getInstructions(), jobName, args);
                    }
                    if (ie.getElse() != null) {
                        readNodeArguments(ie.getElse().getInstructions(), jobName, args);
                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    if (tc.getTry() != null) {
                        readNodeArguments(tc.getTry().getInstructions(), jobName, args);
                    }
                    if (tc.getCatch() != null) {
                        readNodeArguments(tc.getCatch().getInstructions(), jobName, args);
                    }
                    break;
                case LOCK:
                    Lock l = inst.cast();
                    if (l.getLockedWorkflow() != null) {
                        readNodeArguments(l.getLockedWorkflow().getInstructions(), jobName, args);
                    }
                    break;
                case CYCLE:
                    Cycle c = inst.cast();
                    if (c.getCycleWorkflow() != null) {
                        readNodeArguments(c.getCycleWorkflow().getInstructions(), jobName, args);
                    }
                    break;
                case EXECUTE_NAMED:
                    NamedJob j = inst.cast();
                    if (j.getJobName().equals(jobName)) {
                        readNodeArguments(j, args);
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
