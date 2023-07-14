package com.sos.joc.classes.inventory;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.instruction.AddOrder;
import com.sos.inventory.model.instruction.ConsumeNotices;
import com.sos.inventory.model.instruction.Cycle;
import com.sos.inventory.model.instruction.Finish;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.ForkList;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.Options;
import com.sos.inventory.model.instruction.StickySubagent;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.job.InternalExecutableType;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.job.JobReturnCode;
import com.sos.inventory.model.script.Script;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.ListParameterType;
import com.sos.inventory.model.workflow.ParameterType;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.calendar.DailyPlanCalendar;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.common.IDeployObject;
import com.sos.sign.model.job.ExecutableJava;
import com.sos.sign.model.job.ExecutableScript;
import com.sos.sign.model.workflow.ListParameters;
import com.sos.sign.model.workflow.OrderPreparation;
import com.sos.sign.model.workflow.Parameter;
import com.sos.sign.model.workflow.ParameterListType;
import com.sos.sign.model.workflow.Parameters;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data_for_java.value.JExpression;

public class JsonConverter {
    
    private final static String instructionsToConvert = String.join("|", InstructionType.FORKLIST.value(), InstructionType.ADD_ORDER.value(),
            InstructionType.POST_NOTICE.value(), InstructionType.EXPECT_NOTICE.value(), InstructionType.CONSUME_NOTICES.value(), InstructionType.LOCK
                    .value(), InstructionType.FINISH.value(), InstructionType.STICKY_SUBAGENT.value());
    private final static Predicate<String> hasInstructionToConvert = Pattern.compile("\"TYPE\"\\s*:\\s*\"(" + instructionsToConvert + ")\"")
            .asPredicate();
    private final static Predicate<String> hasCycleInstruction = Pattern.compile("\"TYPE\"\\s*:\\s*\"(" + InstructionType.CYCLE.value() + ")\"")
            .asPredicate();
    public final static String scriptIncludeComments = "(##|::|//)";
    public final static String scriptInclude = "!include";
    public final static Pattern scriptIncludePattern = Pattern.compile("^" + scriptIncludeComments + scriptInclude + "[ \t]+(\\S+)[ \t]*(.*)$",
            Pattern.DOTALL);
    public final static Predicate<String> hasScriptIncludes = Pattern.compile(scriptIncludeComments + scriptInclude + "[ \t]+").asPredicate();
    private final static String includeScriptErrorMsg = "Script include '%s' of job '%s[%s]' has wrong format, expected format: "
            + scriptIncludeComments + scriptInclude + " scriptname [--replace=\"search literal\",\"replacement literal\" [--replace=...]]";

    private final static Logger LOGGER = LoggerFactory.getLogger(JsonConverter.class);
    
    private static final Map<InternalExecutableType, String> type2classname = Collections.unmodifiableMap(new HashMap<InternalExecutableType, String>() {

        private static final long serialVersionUID = 1L;

        {
            put(InternalExecutableType.JavaScript_Graal, "com.sos.scriptengine.jobs.JavaScriptJob");
            put(InternalExecutableType.JavaScript_Node, "com.sos.scriptengine.jobs.JavaScriptJob"); //TODO different className
        }
    });

    @SuppressWarnings("unchecked")
    public static <T extends IDeployObject> T readAsConvertedDeployObject(String controllerId, String objectName, String json, Class<T> clazz,
            String commitId, Map<String, String> releasedScripts) throws JsonParseException, JsonMappingException, IOException {
        if (commitId != null && !commitId.isEmpty()) {
            json = json.replaceAll("(\"versionId\"\\s*:\\s*\")[^\"]*\"", "$1" + commitId + "\"");
        }
        if (clazz.getName().equals("com.sos.sign.model.workflow.Workflow")) {
            return (T) readAsConvertedWorkflow(controllerId, objectName, json, releasedScripts);
        } else {
            return Globals.objectMapper.readValue(json.replaceAll("(\\$)epoch(Second|Milli)", "$1js7Epoch$2"), clazz);
        }
    }
    
    public static com.sos.sign.model.workflow.Workflow readAsConvertedWorkflow(String controllerId, String workflowName, String json,
            Map<String, String> releasedScripts) throws JsonParseException, JsonMappingException, IOException {

        com.sos.sign.model.workflow.Workflow signWorkflow = Globals.objectMapper.readValue(json.replaceAll("(\\$)epoch(Second|Milli)",
                "$1js7Epoch$2"), com.sos.sign.model.workflow.Workflow.class);
        Workflow invWorkflow = Globals.objectMapper.readValue(json, Workflow.class);

        signWorkflow.setOrderPreparation(invOrderPreparationToSignOrderPreparation(invWorkflow.getOrderPreparation()));
        
        if (signWorkflow.getInstructions() != null) {
            // at the moment the converter is only necessary to modify instructions for ForkList, AddOrder instructions
            if (hasInstructionToConvert.test(json)) {
                convertInstructions(controllerId, workflowName, invWorkflow.getInstructions(), signWorkflow.getInstructions(), new AtomicInteger(0), OrdersHelper
                        .getDailyPlanTimeZone());
            }
            if (hasCycleInstruction.test(json)) {
                signWorkflow.setCalendarPath(DailyPlanCalendar.dailyPlanCalendarName); 
            }
        }
        if (signWorkflow.getJobs() != null) {
            if (hasScriptIncludes.test(json)) {
                includeScripts(workflowName, signWorkflow.getJobs(), releasedScripts);
            }
            considerReturnCodeWarningsAndSubagentClusterId(invWorkflow.getJobs(), signWorkflow.getJobs());
        }
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(Globals.objectMapper.writeValueAsString(signWorkflow));
        }
        
        return signWorkflow;
    }
    
    // not private because of unit test
    protected static void considerReturnCodeWarningsAndSubagentClusterId(Jobs invJobs, com.sos.sign.model.workflow.Jobs signJobs) {
        if (signJobs.getAdditionalProperties() != null && invJobs.getAdditionalProperties() != null) {
            invJobs.getAdditionalProperties().forEach((jobName, invJob) -> {
                
                com.sos.sign.model.job.Job signJob = signJobs.getAdditionalProperties().get(jobName);
                considerSubagentClusterId(invJob, signJob);
                
                switch (invJob.getExecutable().getTYPE()) {
                case InternalExecutable:
                    com.sos.inventory.model.job.ExecutableJava invEj = invJob.getExecutable().cast();
                    if (signJob != null && invEj.getInternalType() != null && invEj.getInternalType().equals(InternalExecutableType.JavaScript_Graal)) {
                        ExecutableJava signEj = signJob.getExecutable().cast();
                        if (signEj != null) {
                            signEj.setClassName(type2classname.get(InternalExecutableType.JavaScript_Graal));
                        }
                    }
                    break;
                case ShellScriptExecutable:
                case ScriptExecutable:
                    com.sos.inventory.model.job.ExecutableScript invEs = invJob.getExecutable().cast();
                    JobReturnCode rc = invEs.getReturnCodeMeaning();
                    if (rc != null && rc.getWarning() != null && !rc.getWarning().isEmpty()) {
                        if (signJob != null) {
                            ExecutableScript signEs = signJob.getExecutable().cast();
                            if (signEs != null) {
                                if (rc.getSuccess() != null) {
                                    signEs.getReturnCodeMeaning().setSuccessNormalized(rc.getSuccess() + "," + rc.getWarning());
//                                    signEs.getReturnCodeMeaning().setSuccess(Stream.of(rc.getSuccess(), rc.getWarning()).flatMap(List::stream).filter(
//                                            Objects::nonNull).distinct().sorted().collect(Collectors.toList()));
                                } else if (rc.getFailure() != null) {
//                                    signEs.getReturnCodeMeaning().getFailure().removeAll(rc.getWarning());
                                    signEs.getReturnCodeMeaning().setFailureNormalized(rc.getWarning());
                                } else {
                                    if (signEs.getReturnCodeMeaning() == null) {
                                        signEs.setReturnCodeMeaning(new com.sos.sign.model.job.JobReturnCode());
                                    }
                                    signEs.getReturnCodeMeaning().setSuccessNormalized("0," + rc.getWarning());
//                                    signEs.getReturnCodeMeaning().setSuccess(Stream.of(Collections.singletonList(0), rc.getWarning()).flatMap(
//                                            List::stream).filter(Objects::nonNull).distinct().sorted().collect(Collectors.toList()));
                                }
                            }
                        }
                    }
                    break;
                }
            });
        }
    }
    
    private static void considerSubagentClusterId(Job invJob, com.sos.sign.model.job.Job signJob) {
        if (signJob != null) {
            if (signJob.getSubagentSelectionIdExpr() == null || signJob.getSubagentSelectionIdExpr().isEmpty()) {
                if (invJob.getSubagentClusterId() != null && !invJob.getSubagentClusterId().isEmpty()) {
                    signJob.setSubagentSelectionIdExpr(quoteString(invJob.getSubagentClusterId()));
                }
            }
            signJob.setSubagentSelectionId(null);
        }
    }
    
    private static void includeScripts(String workflowName, com.sos.sign.model.workflow.Jobs signJobs, Map<String, String> releasedScripts) {
        Map<String, com.sos.sign.model.job.Job> replacedJobs = new HashMap<>();
        if (signJobs.getAdditionalProperties() != null) {
            signJobs.getAdditionalProperties().forEach((jobName, job) -> {
                if (job.getExecutable() != null) {
                    switch (job.getExecutable().getTYPE()) {
                    case InternalExecutable:
                        break;
                    case ShellScriptExecutable:
                    case ScriptExecutable:
                        ExecutableScript es = job.getExecutable().cast();
                        if (es.getScript() != null && hasScriptIncludes.test(es.getScript())) {
                            es.setScript(replaceIncludes(workflowName, es.getScript(), jobName, releasedScripts));
                            replacedJobs.put(jobName, job);
                        }
                        break;
                    }
                }
            });
            replacedJobs.forEach((jobName, job) -> signJobs.setAdditionalProperty(jobName, job));
        }
        
    }

    private static String replaceIncludes(String workflowName, String script, String jobName, Map<String, String> releasedScripts) {
        String[] scriptLines = script.split("\n");
        for (int i = 0; i < scriptLines.length; i++) {
            String line = scriptLines[i];
            if (hasScriptIncludes.test(line)) {
                Matcher m = scriptIncludePattern.matcher(line);
                if (m.find()) {
                    String scriptName = m.group(2);
                    if (releasedScripts == null || !releasedScripts.containsKey(scriptName)) {
                        throw new IllegalArgumentException(String.format("Script include '%s' of job '%s[%s]' referenced an unreleased script '%s'", line,
                                workflowName, jobName, scriptName));
                    }
                    try {
                        line = Globals.objectMapper.readValue(releasedScripts.get(scriptName), Script.class).getScript();
                    } catch (Exception e) {
                        throw new IllegalArgumentException(String.format("Script '%s' of job '%s[%s]' cannot be read: %s", scriptName, workflowName,
                                jobName, e.toString()));
                    }
                    try {
                        Map<String, String> replacements = parseReplaceInclude(m.group(3));
                        for (Map.Entry<String, String> entry : replacements.entrySet()) {
                            line = line.replaceAll(Pattern.quote(entry.getKey()), entry.getValue());
                        }
                    } catch (Exception e) {
                        throw new IllegalArgumentException(String.format(includeScriptErrorMsg, line, workflowName, jobName));
                    }
                    scriptLines[i] = line;
                } else {
                    throw new IllegalArgumentException(String.format(includeScriptErrorMsg, line, workflowName, jobName));
                }
            }
        }
        return String.join("\n", scriptLines);
    }
    
    public static Map<String, String> parseReplaceInclude(String str) throws IOException, IllegalArgumentException {
        if (str == null || str.trim().isEmpty()) {
           return Collections.emptyMap(); 
        }
        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(str.trim()));
        tokenizer.resetSyntax();
        tokenizer.quoteChar('"');
        tokenizer.quoteChar('\'');
        tokenizer.slashSlashComments(false);
        tokenizer.slashStarComments(false);
        //wordChars all except above quote chars and ',', i.e. 34, 39, 44
        tokenizer.wordChars(0, 33);
        tokenizer.wordChars(35, 38);
        tokenizer.wordChars(40, 43);
        tokenizer.wordChars(45, 255);
        
        Map<String, String> replaceTokens = new HashMap<>();
        
        int currentToken = tokenizer.nextToken();
        boolean searchValExpected = false;
        boolean replacementValExpected = false;
        boolean replaceStringExpected = true;
        boolean commaExpected = false;
        String searchVal = "";
        String msg = "wrong format";
        
        while (currentToken != StreamTokenizer.TT_EOF) {
            switch (tokenizer.ttype) {
            case StreamTokenizer.TT_WORD:
                if (tokenizer.sval.trim().isEmpty()) {
                    // ignore spaces
                } else {
                    if (tokenizer.sval.trim().matches("--replace[ \t]*=")) {
                        if (replaceStringExpected) {
                            searchValExpected = true;
                            replaceStringExpected = false;
                        } else {
                            throw new IllegalArgumentException(msg);
                        }
                    } else {
                        throw new IllegalArgumentException(msg);
                    }
                }
                break;
            case '\'':
            case '"':
                if (replacementValExpected) {
                    replaceTokens.put(searchVal, tokenizer.sval);
                    searchVal = "";
                    replacementValExpected = false;
                    replaceStringExpected = true;
                } else if (searchValExpected) {
                    searchVal = tokenizer.sval;
                    if (searchVal.isEmpty()) {
                        throw new IllegalArgumentException(msg);
                    }
                    searchValExpected = false;
                    commaExpected = true;
                } else {
                    throw new IllegalArgumentException(msg);
                }
                break;
            case ',':
                if (commaExpected) {
                    replacementValExpected = true;
                    commaExpected = false;
                } else {
                    throw new IllegalArgumentException(msg);
                }
                break;
            default:
                throw new IllegalArgumentException(msg);
            }
            currentToken = tokenizer.nextToken();
        }
        return replaceTokens;
    }

    private static void convertInstructions(String controllerId, String workflowName, List<Instruction> invInstructions,
            List<com.sos.sign.model.instruction.Instruction> signInstructions, AtomicInteger addOrderIndex, ZoneId zoneId) {
        if (invInstructions != null) {
            for (int i = 0; i < invInstructions.size(); i++) {
                Instruction invInstruction = invInstructions.get(i);
                com.sos.sign.model.instruction.Instruction signInstruction = signInstructions.get(i);
                switch (invInstruction.getTYPE()) {
                case FORKLIST:
                    ForkList fl = invInstruction.cast();
                    com.sos.sign.model.instruction.ForkList sfl = signInstruction.cast();
                    convertForkList(fl, sfl);
                    if (fl.getWorkflow() != null) {
                        convertInstructions(controllerId, workflowName, fl.getWorkflow().getInstructions(), sfl.getWorkflow().getInstructions(),
                                addOrderIndex, zoneId);
                    }
                    break;
                case FORK:
                    ForkJoin fj = invInstruction.cast();
                    com.sos.sign.model.instruction.ForkJoin sfj = signInstruction.cast();
                    for (int j = 0; j < fj.getBranches().size(); j++) {
                        Branch invBranch = fj.getBranches().get(j);
                        if (invBranch.getWorkflow() != null) {
                            convertInstructions(controllerId, workflowName, invBranch.getWorkflow().getInstructions(), sfj.getBranches().get(j)
                                    .getWorkflow().getInstructions(), addOrderIndex, zoneId);
                        }
                    }
                    break;
                case IF:
                    IfElse ifElse = invInstruction.cast();
                    com.sos.sign.model.instruction.IfElse sIfElse = signInstruction.cast();
                    if (ifElse.getThen() != null) {
                        convertInstructions(controllerId, workflowName, ifElse.getThen().getInstructions(), sIfElse.getThen().getInstructions(),
                                addOrderIndex, zoneId);
                    }
                    if (ifElse.getElse() != null) {
                        convertInstructions(controllerId, workflowName, ifElse.getElse().getInstructions(), sIfElse.getElse().getInstructions(),
                                addOrderIndex, zoneId);
                    }
                    break;
                case TRY:
                    TryCatch tryCatch = invInstruction.cast();
                    com.sos.sign.model.instruction.TryCatch sTryCatch = signInstruction.cast();
                    if (tryCatch.getTry() != null) {
                        convertInstructions(controllerId, workflowName, tryCatch.getTry().getInstructions(), sTryCatch.getTry().getInstructions(),
                                addOrderIndex, zoneId);
                    }
                    if (tryCatch.getCatch() != null) {
                        convertInstructions(controllerId, workflowName, tryCatch.getCatch().getInstructions(), sTryCatch.getCatch().getInstructions(),
                                addOrderIndex, zoneId);
                    }
                    break;
                case LOCK:
                    Lock lock = invInstruction.cast();
                    com.sos.sign.model.instruction.Lock sLock = LockToLockDemandsConverter.lockToSignLockDemands(lock, signInstruction.cast());
                    if (lock.getLockedWorkflow() != null) {
                        convertInstructions(controllerId, workflowName, lock.getLockedWorkflow().getInstructions(), sLock.getLockedWorkflow()
                                .getInstructions(), addOrderIndex, zoneId);
                    }
                    break;
                case ADD_ORDER:
                    convertAddOrder(controllerId, workflowName, invInstruction.cast(), signInstruction.cast(), addOrderIndex, zoneId);
                    break;
                case CYCLE:
                    Cycle cycle = invInstruction.cast();
                    if (cycle.getCycleWorkflow() != null) {
                        com.sos.sign.model.instruction.Cycle sCycle = signInstruction.cast();
                        convertInstructions(controllerId, workflowName, cycle.getCycleWorkflow().getInstructions(), sCycle.getCycleWorkflow()
                                .getInstructions(), addOrderIndex, zoneId);
                    }
                    break;
                case POST_NOTICE:
                    signInstructions.set(i, NoticeToNoticesConverter.postNoticeToSignPostNotices(signInstruction.cast()));
                    break;
                case EXPECT_NOTICE:
                    signInstructions.set(i, NoticeToNoticesConverter.expectNoticeToSignExpectNotices(signInstruction.cast()));
                    break;
                case CONSUME_NOTICES:
                    ConsumeNotices cn = invInstruction.cast();
                    com.sos.sign.model.instruction.ConsumeNotices sCn = signInstruction.cast();
                    if (sCn.getSubworkflow() == null || sCn.getSubworkflow().getInstructions() == null) {
                        sCn.setSubworkflow(new com.sos.sign.model.instruction.Instructions(Collections.emptyList()));
                    } else if (cn.getSubworkflow() != null && cn.getSubworkflow().getInstructions() != null) {
                        convertInstructions(controllerId, workflowName, cn.getSubworkflow().getInstructions(), sCn.getSubworkflow().getInstructions(),
                                addOrderIndex, zoneId);
                    }
                    break;
                case FINISH:
                    Finish finish = invInstruction.cast();
                    com.sos.sign.model.instruction.Finish sFinish = signInstruction.cast();
                    if (finish.getUnsuccessful() != null) {
                        Variables var = new Variables();
                        if (finish.getUnsuccessful()) {
                            var.setAdditionalProperty("returnCode", 1);
                            sFinish.setOutcome(new com.sos.sign.model.common.Outcome("Failed", finish.getMessage(), var));
                        } else {
//                            if (finish.getMessage() != null && !finish.getMessage().isEmpty()) {
//                                var.setAdditionalProperty("returnMessage", finish.getMessage());
//                            }
                            var.setAdditionalProperty("returnCode", 0);
                            sFinish.setOutcome(new com.sos.sign.model.common.Outcome("Succeeded", null, var));
                        }
                    }
                    break;
                case STICKY_SUBAGENT:
                    StickySubagent sticky = invInstruction.cast();
                    com.sos.sign.model.instruction.StickySubagent sSticky = signInstruction.cast();
                    convertStickySubagent(sticky, sSticky);
                    if (sticky.getSubworkflow() != null) {
                        convertInstructions(controllerId, workflowName, sticky.getSubworkflow().getInstructions(), sSticky.getSubworkflow()
                                .getInstructions(), addOrderIndex, zoneId);
                    }
                    break;
                case OPTIONS:
                    Options opts = invInstruction.cast();
                    com.sos.sign.model.instruction.Options sOpts = signInstruction.cast();
                    if (opts.getBlock() != null) {
                        convertInstructions(controllerId, workflowName, opts.getBlock().getInstructions(), sOpts.getBlock().getInstructions(),
                                addOrderIndex, zoneId);
                    }
                    break;
                default:
                    break;
                }
            }
        }
    }

    private static void convertForkList(ForkList fl, com.sos.sign.model.instruction.ForkList sfl) {
        if (fl.getChildren() != null) {
            sfl.setChildren("$" + fl.getChildren());
            sfl.setChildToArguments("(x) => $x");
            sfl.setChildToId("(x, i) => ($i + 1) ++ \".\" ++ $x." + fl.getChildToId());
        } else if (fl.getSubagentClusterIdExpr() != null || fl.getSubagentClusterId() != null) {
            AgentHelper.throwJocMissingLicenseException();
            if (fl.getSubagentClusterIdExpr() != null) {
                sfl.setChildren("subagentIds(" + quoteString(fl.getSubagentClusterIdExpr()) + ")");
            } else {
                sfl.setChildren("subagentIds(" + quoteString(fl.getSubagentClusterId()) + ")");
            }
            sfl.setChildToArguments("(x) => { " + fl.getSubagentIdVariable() + ": $x }");
            sfl.setChildToId("(x, i) => ($i + 1) ++ \".\" ++ $x");
        }
        //sfl.setChildToId("(x) => $x." + fl.getChildToId());
    }
    
    private static void convertStickySubagent(StickySubagent ss, com.sos.sign.model.instruction.StickySubagent sss) {
        if (sss.getSubagentSelectionIdExpr() == null) {
            if (ss.getSubagentClusterId() != null) {
                sss.setSubagentSelectionIdExpr(quoteString(ss.getSubagentClusterId()));
            }
        }
    }
    
    private static void convertAddOrder(String controllerId, String workflowName, AddOrder ao, com.sos.sign.model.instruction.AddOrder sao,
            AtomicInteger addOrderIndex, ZoneId zoneId) {
        sao.setDeleteWhenTerminated(ao.getRemainWhenTerminated() != Boolean.TRUE);
        String timeZone = zoneId.getId();
        int n = addOrderIndex.getAndUpdate(x -> x == Integer.MAX_VALUE ? 0 : x + 1);
        String sAddOrderIndex = ("" + (100 + (n % 100))).substring(1);
        String datetimePattern = "#[0-9]{4}-[0-9]{2}-[0-9]{2}#[^-]+";
        // first replaceAll(replaceAll...):
        //      #2022-01-01#T12345678901-test|branchname of parent orderId -> 2022010112345678901
        // second replaceAll(replaceAll...) 
        //      #2022-01-01#T12345678901-test|branchname of parent orderId -> test|branchname -> test
        // resp.
        //      #2022-01-01#T12345678901-2022010112345678901!-test|branchname of parent orderId -> test|branchname -> test
        String idPattern = "'#' ++ now(format='yyyy-MM-dd', timezone='%s') ++ '#D' ++ " + OrdersHelper.mainOrderIdControllerPattern + " ++ '"
                + sAddOrderIndex + "-' ++ replaceAll(replaceAll($js7OrderId, '^(" + datetimePattern
                + ")-.*$', '$1'), '\\D', \"\") ++ replaceAll(replaceAll($js7OrderId, '^" + datetimePattern
                + "-([^!]+!-)?(.*)$', '$2'), '^([^|]+).*', '!-$1')";
        sao.setOrderId(String.format(idPattern, timeZone));

        if (sao.getArguments() != null && sao.getArguments().getAdditionalProperties() != null) {
            sao.getArguments().getAdditionalProperties().replaceAll((k, v) -> quoteVariable(v));
        }
        if (sao.getStopPositions() == null) {
            sao.setStopPositions(Collections.emptyList());
        }
        
        boolean withStartLabel = (sao.getStartPosition() != null && sao.getStartPosition() instanceof String);
        boolean withEndLabels = sao.getStopPositions().stream().anyMatch(pos -> pos instanceof String);

        if (withStartLabel || withEndLabels) {
            Map<String, List<Object>> labelMap = getLabelToPositionsMap(controllerId, sao.getWorkflowPath());
            int numOfEndPositions = sao.getStopPositions().size();
            if (withStartLabel) {
                sao.setStartPosition(labelMap.get((String) sao.getStartPosition()));
            }
            if (withEndLabels) {
                sao.setStopPositions(sao.getStopPositions().stream().map(pos -> pos instanceof String ? labelMap.get((String) pos) : pos).filter(
                        Objects::nonNull).collect(Collectors.toList()));
            }
            if (sao.getStartPosition() == null || numOfEndPositions > sao.getStopPositions().size()) {
                throw new DBInvalidDataException("Workflow '" + sao.getWorkflowPath() + "' of AddOrder instruction in Workflow '" + workflowName
                        + "' doesn't all specified labels.");
            }
        }
    }
    
    private static Map<String, List<Object>> getLabelToPositionsMap(String controllerId, String workflowName) {
        Map<String, List<Object>> labelMap = Collections.emptyMap();
        // TODO it needs all other workflows of the same deploy call 
//        if (controllerId != null) {
//            try {
//                labelMap = WorkflowsHelper.getLabelToPositionsMapFromDepHistory(controllerId, workflowName);
//            } catch (DBMissingDataException e) {
//                labelMap = WorkflowsHelper.getLabelToPositionsMapFromInventory(workflowName);
//            }
//        } else {
            labelMap = WorkflowsHelper.getLabelToPositionsMapFromInventory(workflowName);
            if (labelMap == null) {
                return Collections.emptyMap();
            }
//        }
        return labelMap;
    }
    
    private static String quoteVariable(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> listVal = (List<Map<String, Object>>) val;
            return listVal.stream().map(mp -> {
                mp.replaceAll((k, v) -> quoteString(v.toString()).replace('=', '\0'));
                return mp;
            }).collect(Collectors.toList()).toString().replace('=', ':').replace('\0', '=');
        }
        return quoteString(val.toString());
    }
    
    public static String quoteString(String str) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            str = "\"" + str + "\"";
        }
        Either<Problem, JExpression> e = JExpression.parse(str);
        if (e.isLeft()) {
            str = JExpression.quoteString(str);
        }
        return str;
    }
    
    private static OrderPreparation invOrderPreparationToSignOrderPreparation(Requirements orderPreparation) {
        if (orderPreparation == null) {
            return null;
        }
        Parameters params = new Parameters();
        if (orderPreparation.getParameters() != null && orderPreparation.getParameters().getAdditionalProperties() != null) {
            orderPreparation.getParameters().getAdditionalProperties().forEach((k, v) -> {
                Parameter p = new Parameter();
                p.setDefault(v.getDefault());
                p.setFinal(v.getFinal());
                if (ParameterType.List.equals(v.getType())) {
                    ListParameters lps = new ListParameters();
                    if (v.getListParameters() != null && v.getListParameters().getAdditionalProperties() != null) {
                        v.getListParameters().getAdditionalProperties().forEach((k1, v1) -> {
                            lps.setAdditionalProperty(k1, v1.getType());
                        });
                    }
                    p.setType(new ParameterListType("List", lps));
                } else {
                    p.setType(v.getType()); // wrong type enum
                }
                params.setAdditionalProperty(k, p);
            });
        }
        return new OrderPreparation(params, orderPreparation.getAllowUndeclared());
    }
    
    public static Requirements signOrderPreparationToInvOrderPreparation(OrderPreparation orderPreparation) {
        return signOrderPreparationToInvOrderPreparation(orderPreparation, true);
    }
    
    @SuppressWarnings("unchecked")
    public static Requirements signOrderPreparationToInvOrderPreparation(OrderPreparation orderPreparation, boolean withFinals) {
        if (orderPreparation == null) {
            return null;
        }
        com.sos.inventory.model.workflow.Parameters params = new com.sos.inventory.model.workflow.Parameters();
        if (orderPreparation.getParameters() != null && orderPreparation.getParameters().getAdditionalProperties() != null) {
            orderPreparation.getParameters().getAdditionalProperties().forEach((k, v) -> {
                if (v.getFinal() == null || (v.getFinal() != null && withFinals)) {
                    com.sos.inventory.model.workflow.Parameter p = new com.sos.inventory.model.workflow.Parameter();
                    p.setDefault(v.getDefault());
                    p.setFinal(v.getFinal());
                    if (v.getType() != null) {
                        if (v.getType() instanceof String) {
                            try {
                                p.setType(ParameterType.fromValue((String) v.getType()));
                            } catch (Exception e) {
                            }
                        } else if (v.getType() instanceof ParameterListType) {
                            p.setType(ParameterType.List);
                            ParameterListType plt = (ParameterListType) v.getType();
                            if (plt.getElementType() != null && plt.getElementType().getAdditionalProperties() != null) {
                                com.sos.inventory.model.workflow.ListParameters lp = new com.sos.inventory.model.workflow.ListParameters();
                                plt.getElementType().getAdditionalProperties().forEach((k1, v1) -> {
                                    lp.setAdditionalProperty(k1, new com.sos.inventory.model.workflow.ListParameter(v1));
                                    p.setListParameters(lp);
                                });
                            }
                        } else if (v.getType() instanceof Map) {
                            p.setType(ParameterType.List);
                            Map<String, String> slp = (Map<String, String>) ((Map<String, Object>) v.getType()).get("elementType");
                            if (slp != null) {
                                com.sos.inventory.model.workflow.ListParameters lp = new com.sos.inventory.model.workflow.ListParameters();
                                slp.forEach((k1, v1) -> {
                                    if (!"TYPE".equals(k1)) {
                                        try {
                                            lp.setAdditionalProperty(k1, new com.sos.inventory.model.workflow.ListParameter(ListParameterType
                                                    .fromValue(v1)));
                                            p.setListParameters(lp);
                                        } catch (Exception e) {
                                        }
                                    }
                                });
                            }
                        }
                    }
                    params.setAdditionalProperty(k, p);
                }
            });
        }
        return new Requirements(params, orderPreparation.getAllowUndeclared()); 
    }

}
