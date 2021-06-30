package com.sos.joc.classes.inventory;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.instruction.Fail;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.job.Executable;
import com.sos.inventory.model.job.ExecutableJava;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.ExecutableType;
import com.sos.inventory.model.job.JobReturnCode;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.ParameterType;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.joc.Globals;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data_for_java.value.JExpression;

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
        w.setOrderPreparation(emptyRequirementsToNull(w.getOrderPreparation()));
        w.setJobs(emptyJobsValuesToNull(w.getJobs()));
        cleanSignedInstructions(w.getInstructions());
        return w;
    }

    private static com.sos.inventory.model.workflow.Workflow emptyWorkflowValuesToNull(com.sos.inventory.model.workflow.Workflow w)
            throws JsonProcessingException {
        if (w == null) {
            return null;
        }
        emptyStringCollectionsToNull(w.getJobResourceNames());
        w.setOrderPreparation(emptyRequirementsToNull(w.getOrderPreparation()));
        w.setJobs(emptyJobsValuesToNull(w.getJobs()));
        cleanInventoryInstructions(w.getInstructions());
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
                    job.setDefaultArguments(emptyEnvToNullAndQuoteStrings(job.getDefaultArguments()));
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
                    job.setDefaultArguments(emptyEnvToNullAndQuoteStrings(job.getDefaultArguments()));
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
        r.getParameters().getAdditionalProperties().replaceAll((k, v) -> {
            if (ParameterType.String == v.getType()) {
                v.setDefault(quoteString((String) v.getDefault()));
            }
            v.setFinal(quoteString(v.getFinal()));
            return v;
        });
        r.setAllowUndeclared(defaultToNull(r.getAllowUndeclared(), Boolean.FALSE));
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
    
    
    // only temporary to convert Variables to Environment
    private static Environment emptyEnvToNullAndQuoteStrings(Environment env) {
        if (env != null) {
            if (env.getAdditionalProperties().isEmpty()) {
                return null;
            } else {
                env.getAdditionalProperties().replaceAll((k, v) -> quoteString(v));
            }
        }
        return env;
    }
    
    private static String quoteString(String str) {
        if (str == null) {
            return null;
        }
        Either<Problem, JExpression> e = JExpression.parse(str);
        if (e.isLeft()) {
            str = JExpression.quoteString(str);
        }
        return str;
    }
    
    private static void cleanInventoryInstructions(List<Instruction> instructions) {
        if (instructions != null) {
            for (Instruction inst : instructions) {
                switch (inst.getTYPE()) {
                case FAIL:
                    Fail f = inst.cast();
                    f.setMessage(quoteString(f.getMessage()));
                    if (f.getOutcome() == null) {
                        f.setOutcome(new Variables());
                    }
                    if (!f.getOutcome().getAdditionalProperties().containsKey("returnCode") ) {
                        f.getOutcome().setAdditionalProperty("returnCode", 1);
                    }
                    f.setUncatchable(defaultToNull(f.getUncatchable(), Boolean.FALSE));
                    break;
                case EXECUTE_NAMED:
                    NamedJob nj = inst.cast();
                    nj.setDefaultArguments(emptyEnvToNullAndQuoteStrings(nj.getDefaultArguments()));
                    break;
                case FORK:
                    ForkJoin fj = inst.cast();
                    for (Branch branch : fj.getBranches()) {
                        cleanInventoryInstructions(branch.getWorkflow().getInstructions());
                    }
                    break;
                case IF:
                    IfElse ifElse = inst.cast();
                    cleanInventoryInstructions(ifElse.getThen().getInstructions());
                    if (ifElse.getElse() != null) {
                        cleanInventoryInstructions(ifElse.getElse().getInstructions());
                    }
                    break;
                case TRY:
                    TryCatch tryCatch = inst.cast();
                    cleanInventoryInstructions(tryCatch.getTry().getInstructions());
                    cleanInventoryInstructions(tryCatch.getCatch().getInstructions());
                    break;
                case LOCK:
                    Lock lock = inst.cast();
                    cleanInventoryInstructions(lock.getLockedWorkflow().getInstructions());
                    break;
                default:
                    break;
                }
            }
        }
    }
    
    private static void cleanSignedInstructions(List<com.sos.sign.model.instruction.Instruction> instructions) {
        if (instructions != null) {
            for (com.sos.sign.model.instruction.Instruction inst : instructions) {
                switch (inst.getTYPE()) {
                case FAIL:
                    com.sos.sign.model.instruction.Fail f = inst.cast();
                    f.setMessage(quoteString(f.getMessage()));
                    if (f.getNamedValues() == null) {
                        f.setNamedValues(new Variables());
                    }
                    if (!f.getNamedValues().getAdditionalProperties().containsKey("returnCode") ) {
                        f.getNamedValues().setAdditionalProperty("returnCode", 1);
                    }
                    f.setUncatchable(defaultToNull(f.getUncatchable(), Boolean.FALSE));
                    break;
                case EXECUTE_NAMED:
                    com.sos.sign.model.instruction.NamedJob nj = inst.cast();
                    nj.setDefaultArguments(emptyEnvToNullAndQuoteStrings(nj.getDefaultArguments()));
                    break;
                case FORK:
                    com.sos.sign.model.instruction.ForkJoin fj = inst.cast();
                    for (com.sos.sign.model.workflow.Branch branch : fj.getBranches()) {
                        cleanSignedInstructions(branch.getWorkflow().getInstructions());
                    }
                    break;
                case IF:
                    com.sos.sign.model.instruction.IfElse ifElse = inst.cast();
                    cleanSignedInstructions(ifElse.getThen().getInstructions());
                    if (ifElse.getElse() != null) {
                        cleanSignedInstructions(ifElse.getElse().getInstructions());
                    }
                    break;
                case TRY:
                    com.sos.sign.model.instruction.TryCatch tryCatch = inst.cast();
                    cleanSignedInstructions(tryCatch.getTry().getInstructions());
                    cleanSignedInstructions(tryCatch.getCatch().getInstructions());
                    break;
                case LOCK:
                    com.sos.sign.model.instruction.Lock lock = inst.cast();
                    cleanSignedInstructions(lock.getLockedWorkflow().getInstructions());
                    break;
                default:
                    break;
                }
            }
        }
    }

}
