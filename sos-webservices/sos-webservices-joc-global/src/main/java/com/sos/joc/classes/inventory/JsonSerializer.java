package com.sos.joc.classes.inventory;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.inventory.model.board.BoardType;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.instruction.AddOrder;
import com.sos.inventory.model.instruction.CaseWhen;
import com.sos.inventory.model.instruction.ConsumeNotices;
import com.sos.inventory.model.instruction.Cycle;
import com.sos.inventory.model.instruction.Fail;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.ForkList;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.Options;
import com.sos.inventory.model.instruction.StickySubagent;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.instruction.When;
import com.sos.inventory.model.job.AdmissionTimeScheme;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.job.Executable;
import com.sos.inventory.model.job.ExecutableJava;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.ExecutableType;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.job.JobReturnCode;
import com.sos.inventory.model.job.JobReturnCodeWarning;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.joc.Globals;
import com.sos.joc.classes.agent.AgentHelper;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data_for_java.value.JExpression;

public class JsonSerializer {
    
    // TODO FileOrderSource: default for delay? 0
    
    
    public static <T> String serializeAsString(T config) throws JsonProcessingException {
        if (config == null) {
            return null;
        }
        return Globals.objectMapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true).writeValueAsString(emptyValuesToNull(config));
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
        } else if (com.sos.sign.model.board.Board.class.isInstance(config)) {
            return (T) emptyBoardValuesToNull((com.sos.sign.model.board.Board) config);
        } else if (com.sos.sign.model.fileordersource.FileOrderSource.class.isInstance(config)) {
            return (T) emptyFileOrderSourceValuesToNull((com.sos.sign.model.fileordersource.FileOrderSource) config);
        } else if (com.sos.inventory.model.workflow.Workflow.class.isInstance(config)) {
            return (T) emptyWorkflowValuesToNull((com.sos.inventory.model.workflow.Workflow) config);
        } else if (com.sos.inventory.model.jobresource.JobResource.class.isInstance(config)) {
            return (T) emptyJobResourceValuesToNull((com.sos.inventory.model.jobresource.JobResource) config);
        } else if (com.sos.inventory.model.board.Board.class.isInstance(config)) {
            return (T) emptyBoardValuesToNull((com.sos.inventory.model.board.Board) config);
        } else if (com.sos.inventory.model.fileordersource.FileOrderSource.class.isInstance(config)) {
            return (T) emptyFileOrderSourceValuesToNull((com.sos.inventory.model.fileordersource.FileOrderSource) config);
        } else if (com.sos.inventory.model.jobtemplate.JobTemplate.class.isInstance(config)) {
            return (T) emptyJobTemplateValuesToNull((com.sos.inventory.model.jobtemplate.JobTemplate) config);
        }
        return config;
    }

    private static com.sos.sign.model.workflow.Workflow emptyWorkflowValuesToNull(com.sos.sign.model.workflow.Workflow w)
            throws JsonProcessingException {
        if (w == null) {
            return null;
        }
        emptyStringCollectionsToNull(w.getJobResourcePaths());
        w.setOrderPreparation(emptyOrderPreparationToNull(w.getOrderPreparation()));
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
        w.setOrderPreparation(emptyOrderPreparationToNull(w.getOrderPreparation()));
        getForklistJobsAndCleanInventoryInstructions(w);
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
        jr.setVariables(emptyEnvToNull(jr.getVariables()));
        jr.setEnv(emptyEnvToNull(jr.getEnv()));
        return jr;
    }
    
    private static com.sos.inventory.model.board.Board emptyBoardValuesToNull(com.sos.inventory.model.board.Board b) throws JsonProcessingException {
        if (b == null) {
            return null;
        }
        if (b.getBoardType() != null && b.getBoardType().equals(BoardType.PLANNABLE)) {
            b.setEndOfLife(null);
            if (b.getPostOrderToNoticeId() != null && b.getExpectOrderToNoticeId() != null) {
                b.setPostOrderToNoticeId(null);
            }
            if (b.getExpectOrderToNoticeId() != null && b.getExpectOrderToNoticeId().replace("\"", "").isBlank()) {
                b.setExpectOrderToNoticeId(null);
            }
        } else {
            if (b.getEndOfLife() != null) {
                b.setEndOfLife(b.getEndOfLife().replaceAll("(\\$)epochMilli", "$1js7EpochMilli"));
            }
        }
        return b;
    }
    
    private static com.sos.sign.model.board.Board emptyBoardValuesToNull(com.sos.sign.model.board.Board b) throws JsonProcessingException {
        if (b == null) {
            return null;
        }
        if (b.getBoardType() != null && b.getBoardType().equals(BoardType.PLANNABLE)) {
            b.setEndOfLife(null);
            b.setExpectOrderToNoticeId(null);
            b.setPostOrderToNoticeId(null);
        } else {
            if (b.getEndOfLife() != null) {
                b.setEndOfLife(b.getEndOfLife().replaceAll("(\\$)epochMilli", "$1js7EpochMilli"));
            }
        }
        return b;
    }
    
    private static com.sos.inventory.model.fileordersource.FileOrderSource emptyFileOrderSourceValuesToNull(
            com.sos.inventory.model.fileordersource.FileOrderSource fos) {
        if (fos == null) {
            return null;
        }
        if (fos.getDirectoryExpr() == null || fos.getDirectoryExpr().isEmpty()) {
            if (fos.getDirectory() != null) {
                fos.setDirectoryExpr(quoteString(fos.getDirectory()));
                fos.setDirectory(null);
            }
        } else {
            fos.setDirectory(null);
        }
        return fos;
    }
    
    private static com.sos.sign.model.fileordersource.FileOrderSource emptyFileOrderSourceValuesToNull(
            com.sos.sign.model.fileordersource.FileOrderSource fos) {
        if (fos == null) {
            return null;
        }
        if (fos.getDirectoryExpr() == null || fos.getDirectoryExpr().isEmpty()) {
            if (fos.getDirectory() != null) {
                fos.setDirectoryExpr(quoteString(fos.getDirectory()));
                fos.setDirectory(null);
            }
        } else {
            fos.setDirectory(null);
        }
        return fos;
    }
    
    private static com.sos.sign.model.workflow.Jobs emptyJobsValuesToNull(com.sos.sign.model.workflow.Jobs j) {
        if (j != null) {
            if (j.getAdditionalProperties().isEmpty()) {
                return null;
            } else {
                j.getAdditionalProperties().forEach((key, job) -> {
                    job.setFailOnErrWritten(defaultToNull(job.getFailOnErrWritten(), Boolean.FALSE));
                    //job.setProcessLimit(defaultToNull(job.getProcessLimit(), 1));
                    job.setDefaultArguments(emptyEnvToNull(job.getDefaultArguments()));
                    job.setKillAtEndOfAdmissionPeriod(defaultToNull(job.getKillAtEndOfAdmissionPeriod(), Boolean.FALSE));
                    emptyStringCollectionsToNull(job.getJobResourcePaths());
                    emptyExecutableToNull(job.getExecutable(), job.getReturnCodeMeaning());
                    job.setReturnCodeMeaning(null);
                    job.setSigkillDelay(defaultToNull(job.getSigkillDelay(), 15));
                    job.setAdmissionTimeScheme(emptyAdmissionTimeSchemeToNull(job.getAdmissionTimeScheme()));
                    job.setSkipIfNoAdmissionStartForOrderDay(defaultToNull(job.getSkipIfNoAdmissionStartForOrderDay(), Boolean.FALSE));
                    job.setIsNotRestartable(defaultToNull(job.getIsNotRestartable(), Boolean.FALSE));
                    if (job.getSubagentBundleIdExpr() != null && !job.getSubagentBundleIdExpr().isEmpty()) {
                        job.setSubagentBundleId(null); 
                    }
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
                    job.setWarnOnErrWritten(defaultToNull(job.getWarnOnErrWritten(), Boolean.FALSE));
                    job.setWithSubagentClusterIdExpr(defaultToNull(job.getWithSubagentClusterIdExpr(), Boolean.FALSE));
                    job.setKillAtEndOfAdmissionPeriod(defaultToNull(job.getKillAtEndOfAdmissionPeriod(), Boolean.FALSE));
                    job.setParallelism(defaultToNull(job.getParallelism(), 1));
                    job.setDefaultArguments(emptyEnvToNull(job.getDefaultArguments()));
                    emptyStringCollectionsToNull(job.getJobResourceNames());
                    emptyExecutableToNull(job.getExecutable(), job.getReturnCodeMeaning());
                    job.setReturnCodeMeaning(null);
                    //job.setGraceTimeout(defaultToNull(job.getGraceTimeout(), 15));
                    job.setAdmissionTimeScheme(emptyAdmissionTimeSchemeToNull(job.getAdmissionTimeScheme()));
                    job.setSkipIfNoAdmissionForOrderDay(defaultToNull(job.getSkipIfNoAdmissionForOrderDay(), Boolean.FALSE));
                    job.setIsNotRestartable(defaultToNull(job.getIsNotRestartable(), Boolean.FALSE));
                    if (job.getSubagentClusterIdExpr() != null && !job.getSubagentClusterIdExpr().isEmpty()) {
                        job.setSubagentClusterId(null); 
                    }
                });
            }
        }
        return j;
    }
    
    private static com.sos.inventory.model.jobtemplate.JobTemplate emptyJobTemplateValuesToNull(com.sos.inventory.model.jobtemplate.JobTemplate jt) {
        if (jt != null) {
            jt.setFailOnErrWritten(defaultToNull(jt.getFailOnErrWritten(), Boolean.FALSE));
            jt.setWarnOnErrWritten(defaultToNull(jt.getWarnOnErrWritten(), Boolean.FALSE));
            jt.setParallelism(defaultToNull(jt.getParallelism(), 1));
            jt.setDefaultArguments(emptyEnvToNull(jt.getDefaultArguments()));
            emptyStringCollectionsToNull(jt.getJobResourceNames());
            emptyExecutableToNull(jt.getExecutable(), null);
            //jt.setGraceTimeout(defaultToNull(jt.getGraceTimeout(), 15));
            jt.setAdmissionTimeScheme(emptyAdmissionTimeSchemeToNull(jt.getAdmissionTimeScheme()));
            jt.setIsNotRestartable(defaultToNull(jt.getIsNotRestartable(), Boolean.FALSE));
            jt.setSkipIfNoAdmissionForOrderDay(defaultToNull(jt.getSkipIfNoAdmissionForOrderDay(), Boolean.FALSE));
            jt.setHash(null);
        }
        return jt;
    }
    
    private static AdmissionTimeScheme emptyAdmissionTimeSchemeToNull(AdmissionTimeScheme obj) {
        if (obj != null && obj.getPeriods() == null && obj.getRestrictedSchemes() == null) {
            return null;
        }
        return obj;
    }
    
    private static void emptyStringCollectionsToNull(Collection<String> coll) {
        if (coll != null) {
            coll.removeIf(Objects::isNull);
            coll.removeIf(i -> i.isEmpty());
            if (coll.isEmpty()) {
                coll = null;
            }
        }
    }
    
    private static void emptyCollectionsToNull(Collection<?> coll) {
        if (coll != null) {
            coll.removeIf(Objects::isNull);
            if (coll.isEmpty()) {
                coll = null;
            }
        }
    }
    
    private static Environment emptyEnvToNull(Environment env) {
        if (env != null) {
            env.getAdditionalProperties().values().removeIf(v -> v == null);
            if (env.getAdditionalProperties().isEmpty()) {
                return null;
            } else {
                env.getAdditionalProperties().replaceAll((k, v) -> handleEmptyExpression(v));
            }
        }
        return env;
    }
    
    private static String handleEmptyExpression(String str) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return "\"\"";
        }
        return str;
    }
    
    private static Variables emptyVarsToNull(Variables vars) {
        if (vars != null) {
            vars.getAdditionalProperties().values().removeIf(v -> v == null);
            if (vars.getAdditionalProperties().isEmpty()) {
                return null;
            }
        }
        return vars;
    }
    
    private static Requirements emptyOrderPreparationToNull(Requirements r) {
        if (r == null) {
            return null;
        }
        if (r != null && (r.getParameters() == null || (r.getParameters() != null && r.getParameters().getAdditionalProperties().isEmpty()))) {
            if (r.getAllowUndeclared() == Boolean.TRUE) {
                r.setParameters(null);
                return r; 
            }
            return null;
        }
        r.getParameters().getAdditionalProperties().replaceAll((k, v) -> {
            if (v.getDefault() != null) {
                v.setDefault(quoteString(v.getDefault().toString()));
            }
            v.setFinal(quoteString(v.getFinal()));
            return v;
        });
        r.setAllowUndeclared(defaultToNull(r.getAllowUndeclared(), Boolean.FALSE));
        return r;
    }
    
    private static com.sos.sign.model.workflow.OrderPreparation emptyOrderPreparationToNull(com.sos.sign.model.workflow.OrderPreparation r) {
        if (r == null) {
            return null;
        }
        if (r != null && (r.getParameters() == null || (r.getParameters() != null && r.getParameters().getAdditionalProperties().isEmpty()))) {
            if (r.getAllowUndeclared() == Boolean.TRUE) {
                r.setParameters(null);
                return r; 
            }
            return null;
        }
        r.getParameters().getAdditionalProperties().replaceAll((k, v) -> {
            if (v.getDefault() != null) {
                v.setDefault(quoteString(v.getDefault().toString()));
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
            if (j.getSuccess() != null && j.getSuccess().isEmpty()) {
                j.setSuccess((String) null);
            }
            if (j.getFailure() != null && j.getFailure().isEmpty()) {
                j.setFailure((String) null);
            }
            if (j.getWarning() != null && j.getWarning().isEmpty()) {
                j.setWarning((String) null);
            }
            if (j.getFailure() == null && (j.getSuccess() == null || j.getSuccess().equals("0")) && j.getWarning() == null) {
                return null;
            }
            return j;
        }
        return null;
    }
    
    private static JobReturnCodeWarning emptyReturnCodeWarningToNull(JobReturnCodeWarning j) {
        if (j != null) {
            if (j.getWarning() != null && j.getWarning().isEmpty()) {
                j.setWarning((String) null);
            }
            if (j.getWarning() == null) {
                return null;
            }
            return j;
        }
        return null;
    }
    
    private static com.sos.sign.model.job.JobReturnCode emptyReturnCodeToNull(com.sos.sign.model.job.JobReturnCode j) {
        if (j != null) {
            if (j.getSuccess() != null && j.getSuccess().isEmpty()) {
                j.setSuccess((String) null);
            }
//            if (j.getFailure() != null && j.getFailure().isEmpty()) {
//                j.setFailure((String) null);
//            }
            if (j.getFailure() == null && (j.getSuccess() == null || j.getSuccess().equals("0"))) {
                return null;
            }
            return j;
        }
        return null;
    }
    
    private static void emptyExecutableToNull(Executable e, JobReturnCode rc) {
        if (e != null) {
            switch (e.getTYPE()) {
            case InternalExecutable:
                ExecutableJava ej = e.cast();
                ej.setArguments(emptyEnvToNull(ej.getArguments()));
                ej.setJobArguments(emptyVarsToNull(ej.getJobArguments()));
                ej.setReturnCodeMeaning(emptyReturnCodeWarningToNull(ej.getReturnCodeMeaning()));
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
                    if (es.getLogin().getCredentialKey() == null || es.getLogin().getCredentialKey().isEmpty()) {
                        es.setLogin(null);
                    }
                }
                break;
            }
        }
    }
    
    private static void emptyExecutableToNull(com.sos.sign.model.job.Executable e, com.sos.sign.model.job.JobReturnCode rc) {
        if (e != null) {
            switch (e.getTYPE()) {
            case InternalExecutable:
                com.sos.sign.model.job.ExecutableJava ej = e.cast();
                ej.setArguments(emptyEnvToNull(ej.getArguments()));
                ej.setJobArguments(emptyVarsToNull(ej.getJobArguments()));
                break;
            case ShellScriptExecutable:
            case ScriptExecutable:
                com.sos.sign.model.job.ExecutableScript es = e.cast();
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
                    if (es.getLogin().getCredentialKey() == null || es.getLogin().getCredentialKey().isEmpty()) {
                        es.setLogin(null);
                    }
                }
                break;
            }
        }
    }
    
    public static String quoteString(String str) {
        str = handleEmptyExpression(str);
        if (str == null) {
            return null;
        }
        if (!str.contains("\"") && !str.contains("'") && !str.contains("$") && !isBool(str) && !isNumber(str)) {
            str = "\"" + str + "\""; 
        }
        Either<Problem, JExpression> e = JExpression.parse(str);
        if (e.isLeft()) {
            str = JExpression.quoteString(str);
        }
        return str;
    }
    
    private static boolean isBool(String str) {
        if (str == null) {
            return false;
        }
        if ("true".equals(str.toLowerCase()) || "false".equals(str.toLowerCase())) {
            return true;
        }
        return false;
    }
    
    private static boolean isNumber(String str) {
        if (str == null) {
            return false;
        }
        try {
            new BigDecimal(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static void getForklistJobsAndCleanInventoryInstructions(com.sos.inventory.model.workflow.Workflow w) {
        cleanInventoryInstructions(w.getInstructions(), w.getJobs(), null, null);
    }
    
    private static void cleanInventoryInstructions(List<Instruction> instructions, Jobs jobs, String forkListAgentName, String stickyAgentName) {
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
                case FINISH:
                    //Finish fi = inst.cast();
                    //fi.setMessage(quoteString(fi.getMessage()));
                    //fi.setUnsuccessful(defaultToNull(fi.getUnsuccessful(), Boolean.FALSE));
                    break;
                case EXECUTE_NAMED:
                    NamedJob nj = inst.cast();
                    nj.setDefaultArguments(emptyEnvToNull(nj.getDefaultArguments()));
                    if (jobs != null && jobs.getAdditionalProperties() != null) {
                        Job job = jobs.getAdditionalProperties().get(nj.getJobName());
                        if (job != null) {
                            if (stickyAgentName != null && !stickyAgentName.isEmpty()) {
                                if (job.getAgentName() == null || job.getAgentName().isEmpty()) {
                                    job.setAgentName(stickyAgentName);
                                    job.setSubagentClusterId(null);
                                    job.setSubagentClusterIdExpr(null);
                                    job.setWithSubagentClusterIdExpr(true);
                                } else {
                                    // propagate agentName from StickySubagent???
                                    // if (!stickyAgentName.equals(job.getAgentName()) && job.getSubagentClusterIdExpr() == null) {
                                    // job.setAgentName(stickyAgentName);
                                    // }
                                }
                            }
                            if (forkListAgentName != null && !forkListAgentName.isEmpty()) {
                                if (job.getAgentName() == null || job.getAgentName().isEmpty()) {
                                    job.setAgentName(forkListAgentName);
                                    job.setSubagentClusterId(null);
                                    job.setSubagentClusterIdExpr("$js7ForkListSubagentId");
                                    job.setWithSubagentClusterIdExpr(true);
                                } else {
                                    if (forkListAgentName.equals(job.getAgentName()) && (job.getSubagentClusterIdExpr() == null || job
                                            .getSubagentClusterIdExpr().isEmpty()) && (job.getSubagentClusterId() == null || job
                                            .getSubagentClusterId().isEmpty())) {
                                        job.setSubagentClusterIdExpr("$js7ForkListSubagentId");
                                        job.setSubagentClusterId(null);
                                        job.setWithSubagentClusterIdExpr(true);
                                    }
                                    // propagate agentName from Forklist
                                    if (!forkListAgentName.equals(job.getAgentName())) {
                                        if ("$js7ForkListSubagentId".equals(job.getSubagentClusterIdExpr())) {
                                            job.setAgentName(forkListAgentName);
                                            job.setSubagentClusterId(null);
                                            job.setWithSubagentClusterIdExpr(true);
//                                        } else if ((job.getSubagentClusterIdExpr() == null || job.getSubagentClusterIdExpr().isEmpty()) && (job
//                                                .getSubagentClusterId() == null || job.getSubagentClusterId().isEmpty())) {
//                                            // TODO it can be a standalone agent at the job
//                                            // only if not then
////                                            job.setAgentName(forkListAgentName);
////                                            job.setSubagentClusterId(null);
////                                            job.setSubagentClusterIdExpr("$js7ForkListSubagentId");
////                                            job.setWithSubagentClusterIdExpr(true);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                case FORKLIST:
                    ForkList fl = inst.cast();
                    fl.setJoinIfFailed(defaultToNull(fl.getJoinIfFailed(), Boolean.FALSE));
                    fl.setAgentName(defaultToNull(fl.getAgentName(), ""));
                    if (fl.getSubagentClusterIdExpr() != null && fl.getSubagentClusterIdExpr().replaceAll("['\"]", "").trim().isEmpty()) {
                        fl.setSubagentClusterIdExpr(null); 
                    }
                    if (fl.getSubagentClusterIdExpr() != null && !fl.getSubagentClusterIdExpr().isEmpty()) {
                        fl.setSubagentClusterId(null);
                    }
                    if (fl.getWorkflow() != null) {
                        if (forkListAgentName == null && AgentHelper.hasClusterLicense()) {
                            forkListAgentName = fl.getAgentName();
                        }
                        cleanInventoryInstructions(fl.getWorkflow().getInstructions(), jobs, forkListAgentName, stickyAgentName);
                    }
                    break;
                case FORK:
                    ForkJoin fj = inst.cast();
                    fj.setJoinIfFailed(defaultToNull(fj.getJoinIfFailed(), Boolean.FALSE));
                    if (fj.getBranches() != null) {
                        for (Branch branch : fj.getBranches()) {
                            if (branch.getWorkflow() != null) {
                                cleanInventoryInstructions(branch.getWorkflow().getInstructions(), jobs, forkListAgentName, stickyAgentName);
                            }
                        }
                    }
                    break;
                case IF:
                    IfElse ifElse = inst.cast();
                    if (ifElse.getThen() != null) {
                        cleanInventoryInstructions(ifElse.getThen().getInstructions(), jobs, forkListAgentName, stickyAgentName);
                    }
                    if (ifElse.getElse() != null) {
                        cleanInventoryInstructions(ifElse.getElse().getInstructions(), jobs, forkListAgentName, stickyAgentName);
                    }
                    break;
                case CASE_WHEN:
                    CaseWhen caseWhen = inst.cast();
                    if (caseWhen.getCases() != null) {
                        for (When when : caseWhen.getCases()) {
                            if (when.getThen() != null) {
                                cleanInventoryInstructions(when.getThen().getInstructions(), jobs, forkListAgentName, stickyAgentName);
                            }
                        }
                    }
                    if (caseWhen.getElse() != null) {
                        cleanInventoryInstructions(caseWhen.getElse().getInstructions(), jobs, forkListAgentName, stickyAgentName);
                    }
                    break;
                case TRY:
                    TryCatch tryCatch = inst.cast();
                    if (tryCatch.getTry() != null) {
                        cleanInventoryInstructions(tryCatch.getTry().getInstructions(), jobs, forkListAgentName, stickyAgentName);
                    }
                    if (tryCatch.getCatch() != null) {
                        cleanInventoryInstructions(tryCatch.getCatch().getInstructions(), jobs, forkListAgentName, stickyAgentName);
                    }
                    break;
                case LOCK:
                    Lock lock = inst.cast();
                    if (lock.getLockedWorkflow() != null) {
                        cleanInventoryInstructions(lock.getLockedWorkflow().getInstructions(), jobs, forkListAgentName, stickyAgentName);
                    }
                    break;
                case ADD_ORDER:
                    AddOrder ao = inst.cast();
                    ao.setArguments(emptyVarsToNull(ao.getArguments()));
                    ao.setForceJobAdmission(defaultToNull(ao.getForceJobAdmission(), Boolean.FALSE));
                    ao.setRemainWhenTerminated(defaultToNull(ao.getRemainWhenTerminated(), Boolean.FALSE));
                    break;
                case CYCLE:
                    Cycle cycle = inst.cast();
                    cycle.setOnlyOnePeriod(defaultToNull(cycle.getOnlyOnePeriod(), Boolean.FALSE));
                    if (cycle.getCycleWorkflow() != null) {
                        cleanInventoryInstructions(cycle.getCycleWorkflow().getInstructions(), jobs, forkListAgentName, stickyAgentName);
                    }
                    break;
                case CONSUME_NOTICES:
                    ConsumeNotices cn = inst.cast();
                    if (cn.getSubworkflow() != null && cn.getSubworkflow().getInstructions() != null) {
                        cleanInventoryInstructions(cn.getSubworkflow().getInstructions(), jobs, forkListAgentName, stickyAgentName);
                    }
                    break;
                case STICKY_SUBAGENT:
                    StickySubagent ss = inst.cast();
                    if (ss.getSubworkflow() != null) {
                        if (AgentHelper.hasClusterLicense()) {
                            stickyAgentName = ss.getAgentName();
                        }
                        cleanInventoryInstructions(ss.getSubworkflow().getInstructions(), jobs, forkListAgentName, stickyAgentName);
                    }
                    break;
                case OPTIONS:
                    Options opts = inst.cast();
                    if (opts.getBlock() != null) {
                        cleanInventoryInstructions(opts.getBlock().getInstructions(), jobs, forkListAgentName, stickyAgentName);
                    }
                    break;
                default:
                    break;
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void cleanSignedInstructions(List<com.sos.sign.model.instruction.Instruction> instructions) {
        if (instructions != null) {
            for (com.sos.sign.model.instruction.Instruction inst : instructions) {
                switch (inst.getTYPE()) {
                case FAIL:
                    com.sos.sign.model.instruction.Fail f = inst.cast();
                    //f.setMessage(quoteString(f.getMessage()));
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
                    nj.setDefaultArguments(emptyEnvToNull(nj.getDefaultArguments()));
                    break;
                case FORK:
                    com.sos.sign.model.instruction.ForkJoin fj = inst.cast();
                    fj.setJoinIfFailed(defaultToNull(fj.getJoinIfFailed(), Boolean.FALSE));
                    for (com.sos.sign.model.workflow.Branch branch : fj.getBranches()) {
                        if (branch.getWorkflow() != null) {
                            cleanSignedInstructions(branch.getWorkflow().getInstructions());
                        }
                    }
                    break;
                case FORKLIST:
                    com.sos.sign.model.instruction.ForkList fl = inst.cast();
                    fl.setAgentPath(defaultToNull(fl.getAgentPath(), ""));
                    fl.setJoinIfFailed(defaultToNull(fl.getJoinIfFailed(), Boolean.FALSE));
                    if (fl.getWorkflow() != null) {
                        cleanSignedInstructions(fl.getWorkflow().getInstructions());
                    }
                    break;
                case IF:
                    com.sos.sign.model.instruction.IfElse ifElse = inst.cast();
                    if (ifElse.getThen() != null) {
                        cleanSignedInstructions(ifElse.getThen().getInstructions());
                    }
                    if (ifElse.getIfThens() != null) {
                        for (com.sos.sign.model.instruction.When when : ifElse.getIfThens()) {
                            if (when != null) {
                                cleanSignedInstructions(when.getThen().getInstructions());
                            }
                        }
                    }
                    if (ifElse.getElse() != null) {
                        cleanSignedInstructions(ifElse.getElse().getInstructions());
                    }
                    break;
                case TRY:
                    com.sos.sign.model.instruction.TryCatch tryCatch = inst.cast();
                    if (tryCatch.getTry() != null) {
                        cleanSignedInstructions(tryCatch.getTry().getInstructions());
                    }
                    if (tryCatch.getCatch() != null) {
                        cleanSignedInstructions(tryCatch.getCatch().getInstructions());
                    }
                    break;
                case LOCK:
                    com.sos.sign.model.instruction.Lock lock = inst.cast();
                    if (lock.getLockedWorkflow() != null) {
                        cleanSignedInstructions(lock.getLockedWorkflow().getInstructions());
                    }
                    break;
                case ADD_ORDER:
                    com.sos.sign.model.instruction.AddOrder ao = inst.cast();
                    //Is not optional: ao.setArguments(emptyVarsToNull(ao.getArguments()));
                    if (ao.getArguments() == null) {
                        ao.setArguments(new Variables());
                    }
                    if (ao.getStartPosition() != null) {
                        if (ao.getStartPosition() instanceof List<?>) {
                            emptyCollectionsToNull((List<Object>) ao.getStartPosition());
                        } else if (ao.getStartPosition() instanceof String) {
                            defaultToNull((String) ao.getStartPosition(), "");
                        }
                    }
                    ao.setForceAdmission(defaultToNull(ao.getForceAdmission(), Boolean.FALSE));
                    //Is not optional: ao.setDeleteWhenTerminated(defaultToNull(ao.getDeleteWhenTerminated(), Boolean.TRUE));
                    break;
                case CYCLE:
                    com.sos.sign.model.instruction.Cycle cycle = inst.cast();
                    cycle.setOnlyOnePeriod(defaultToNull(cycle.getOnlyOnePeriod(), Boolean.FALSE));
                    if (cycle.getCycleWorkflow() != null) {
                        cleanSignedInstructions(cycle.getCycleWorkflow().getInstructions());
                    }
                    break;
                case CONSUME_NOTICES:
                    com.sos.sign.model.instruction.ConsumeNotices cn = inst.cast();
                    if (cn.getSubworkflow() == null || cn.getSubworkflow().getInstructions() == null) {
                        cn.setSubworkflow(new com.sos.sign.model.instruction.Instructions(Collections.emptyList()));
                    } else {
                        cleanSignedInstructions(cn.getSubworkflow().getInstructions());
                    }
                    break;
                case STICKY_SUBAGENT:
                    com.sos.sign.model.instruction.StickySubagent ss = inst.cast();
                    if (ss.getSubworkflow() != null) {
                        cleanSignedInstructions(ss.getSubworkflow().getInstructions());
                    }
                    break;
                case OPTIONS:
                    com.sos.sign.model.instruction.Options opts = inst.cast();
                    opts.setStopOnFailure(defaultToNull(opts.getStopOnFailure(), Boolean.FALSE));
                    if (opts.getBlock() != null) {
                        cleanSignedInstructions(opts.getBlock().getInstructions());
                    }
                    break;
                default:
                    break;
                }
            }
        }
    }

}
