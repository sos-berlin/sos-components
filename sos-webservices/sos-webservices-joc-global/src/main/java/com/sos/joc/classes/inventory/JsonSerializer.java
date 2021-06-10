package com.sos.joc.classes.inventory;

import java.util.Collection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.job.Executable;
import com.sos.inventory.model.job.ExecutableJava;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.ExecutableType;
import com.sos.inventory.model.job.JobReturnCode;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.joc.Globals;

public class JsonSerializer {
    
    // TODO FileOrderSource: default for delay?
    // TODO Job: default for logLevel?
    // TODO Job: default for criticality?
    // TODO Job: default for timeout?
    // TODO Job: default for graceTimeout?
    
    
    public static <T> String serializeAsString(T config) throws JsonProcessingException {
        if (config == null) {
            return null;
        }
        return Globals.objectMapper.writeValueAsString(emptyValuesToNull(config));
    }
    
    public static <T> byte[] serializeAsBytes(T config) throws JsonProcessingException {
        if (config == null) {
            return null;
        }
        return Globals.objectMapper.writeValueAsBytes(emptyValuesToNull(config));
    }
    
    public static <T> String serializeAsPrettyPrintString(T config) throws JsonProcessingException {
        if (config == null) {
            return null;
        }
        return Globals.prettyPrintObjectMapper.writeValueAsString(emptyValuesToNull(config));
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T emptyValuesToNull(T config) throws JsonProcessingException {
        if (config == null) {
            return null;
        }
        if (com.sos.sign.model.workflow.Workflow.class.isInstance(config)) {
            return (T) emptyWorkflowValuesToNull((com.sos.sign.model.workflow.Workflow) config);
        } else if (com.sos.sign.model.jobresource.JobResource.class.isInstance(config)) {
            return (T) emptyJobResourceValuesToNull((com.sos.sign.model.jobresource.JobResource) config);
        } else if (com.sos.inventory.model.workflow.Workflow.class.isInstance(config)) {
            return (T) emptyWorkflowValuesToNull((com.sos.inventory.model.workflow.Workflow) config);
        } else if (com.sos.inventory.model.jobresource.JobResource.class.isInstance(config)) {
            return (T) emptyJobResourceValuesToNull((com.sos.inventory.model.jobresource.JobResource) config);
        }
        return config;
    }
    
    private static com.sos.sign.model.workflow.Workflow emptyWorkflowValuesToNull(com.sos.sign.model.workflow.Workflow w)
            throws JsonProcessingException {
        if (w == null) {
            return null;
        }
        emptyStringCollectionsToNull(w.getJobResourcePaths());
        w.setOrderRequirements(emptyRequirementsToNull(w.getOrderRequirements()));
        w.setJobs(emptyJobsValuesToNull(w.getJobs()));
        return w;
    }

    private static com.sos.inventory.model.workflow.Workflow emptyWorkflowValuesToNull(com.sos.inventory.model.workflow.Workflow w)
            throws JsonProcessingException {
        if (w == null) {
            return null;
        }
        emptyStringCollectionsToNull(w.getJobResourceNames());
        w.setOrderRequirements(emptyRequirementsToNull(w.getOrderRequirements()));
        w.setJobs(emptyJobsValuesToNull(w.getJobs()));
        return w;
    }
    
    private static com.sos.inventory.model.jobresource.JobResource emptyJobResourceValuesToNull(com.sos.inventory.model.jobresource.JobResource jr)
            throws JsonProcessingException {
        if (jr == null) {
            return null;
        }
        jr.setArguments(emptyEnvToNull(jr.getArguments()));
        jr.setEnv(emptyEnvToNull(jr.getEnv()));
        return jr;
    }

    private static com.sos.sign.model.jobresource.JobResource emptyJobResourceValuesToNull(com.sos.sign.model.jobresource.JobResource jr)
            throws JsonProcessingException {
        if (jr == null) {
            return null;
        }
        jr.setSettings(emptyEnvToNull(jr.getSettings()));
        jr.setEnv(emptyEnvToNull(jr.getEnv()));
        return jr;
    }
    
    private static com.sos.sign.model.workflow.Jobs emptyJobsValuesToNull(com.sos.sign.model.workflow.Jobs j) {
        if (j != null) {
            if (j.getAdditionalProperties().isEmpty()) {
                return null;
            } else {
                j.getAdditionalProperties().forEach((key, job) -> {
                    job.setFailOnErrWritten(defaultToNull(job.getFailOnErrWritten(), Boolean.FALSE));
                    job.setParallelism(defaultToNull(job.getParallelism(), 1));
                    job.setDefaultArguments(emptyVarsToNull(job.getDefaultArguments()));
                    emptyStringCollectionsToNull(job.getJobResourcePaths());
                    emptyExecutableToNull(job.getExecutable(), job.getReturnCodeMeaning());
                    job.setReturnCodeMeaning(null);
                });
            }
        }
        return j;
    }
    
    private static com.sos.inventory.model.workflow.Jobs emptyJobsValuesToNull(com.sos.inventory.model.workflow.Jobs j) {
        if (j != null) {
            if (j.getAdditionalProperties().isEmpty()) {
                return null;
            } else {
                j.getAdditionalProperties().forEach((key, job) -> {
                    job.setFailOnErrWritten(defaultToNull(job.getFailOnErrWritten(), Boolean.FALSE));
                    job.setParallelism(defaultToNull(job.getParallelism(), 1));
                    job.setDefaultArguments(emptyVarsToNull(job.getDefaultArguments()));
                    emptyStringCollectionsToNull(job.getJobResourceNames());
                    emptyExecutableToNull(job.getExecutable(), job.getReturnCodeMeaning());
                    job.setReturnCodeMeaning(null);
                });
            }
        }
        return j;
    }
    
    private static void emptyStringCollectionsToNull(Collection<String> coll) {
        if (coll != null && coll.removeIf(i -> i.isEmpty()) && coll.isEmpty()) {
            coll = null;
        }
    }
    
    private static void emptyCollectionsToNull(Collection<?> coll) {
        if (coll != null  && coll.isEmpty()) {
            coll = null;
        }
    }
    
    private static Environment emptyEnvToNull(Environment env) {
        if (env != null && env.getAdditionalProperties().isEmpty()) {
            return null;
        }
        return env;
    }
    
    private static Variables emptyVarsToNull(Variables vars) {
        if (vars != null && vars.getAdditionalProperties().isEmpty()) {
            return null;
        }
        return vars;
    }
    
    private static Requirements emptyRequirementsToNull(Requirements r) {
        if (r != null && (r.getParameters() == null || (r.getParameters() != null && r.getParameters().getAdditionalProperties().isEmpty()))) {
            return null;
        }
        return r;
    }
    
    private static <T> T defaultToNull(T val, T _default) {
        if (val != null && val.equals(_default)) {
            return null;
        }
        return val;
    }
    
    private static JobReturnCode emptyReturnCodeToNull(JobReturnCode j) {
        if (j != null) {
            emptyCollectionsToNull(j.getFailure());
            emptyCollectionsToNull(j.getSuccess());
            if (j.getFailure() == null && j.getSuccess() == null) {
                return null;
            }
            return j;
        }
        return null;
    }
    
    private static void emptyExecutableToNull(Executable e, JobReturnCode rc) {
        switch (e.getTYPE()) {
        case InternalExecutable:
            ExecutableJava ej = e.cast();
            ej.setArguments(emptyEnvToNull(ej.getArguments()));
            ej.setJobArguments(emptyVarsToNull(ej.getJobArguments()));
            break;
        case ShellScriptExecutable:
        case ScriptExecutable:
            ExecutableScript es = e.cast();
            es.setTYPE(ExecutableType.ShellScriptExecutable);
            if (es.getReturnCodeMeaning() == null) {
                es.setReturnCodeMeaning(emptyReturnCodeToNull(rc));
            } else {
                es.setReturnCodeMeaning(emptyReturnCodeToNull(es.getReturnCodeMeaning()));
            }
            es.setEnv(emptyEnvToNull(es.getEnv()));
            if (es.getV1Compatible() == Boolean.FALSE) {
                es.setV1Compatible(null);
            }
            if (es.getLogin() != null) {
                if (es.getLogin().getWithUserProfile() == Boolean.FALSE) {
                    es.getLogin().setWithUserProfile(null);
                }
                if (es.getLogin().getWithUserProfile() == null && (es.getLogin().getCredentialKey() == null || es.getLogin().getCredentialKey()
                        .isEmpty())) {
                    es.setLogin(null);
                }
            }
            break;
        }
    }

}
