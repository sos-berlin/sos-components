package com.sos.joc.classes.inventory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.board.Board;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingDayCalendars;
import com.sos.inventory.model.fileordersource.FileOrderSource;
import com.sos.inventory.model.instruction.AddOrder;
import com.sos.inventory.model.instruction.Cycle;
import com.sos.inventory.model.instruction.ExpectNotice;
import com.sos.inventory.model.instruction.ExpectNotices;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.ForkList;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.PostNotice;
import com.sos.inventory.model.instruction.PostNotices;
import com.sos.inventory.model.instruction.Prompt;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.job.ExecutableJava;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.ExecutableType;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.inventory.model.schedule.OrderParameterisation;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.BranchWorkflow;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.Parameter;
import com.sos.inventory.model.workflow.ParameterType;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.db.common.HistoryConstants;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data_for_java.value.JExpression;

public class Validator {

    private final static Predicate<String> checkKey = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$").asPredicate();
    private final static Predicate<String> firstCharOfKeyIsNumber = Pattern.compile("^[0-9]").asPredicate();
    private final static Pattern scriptIncludePattern = Pattern.compile("^" + JsonConverter.scriptIncludeComments + JsonConverter.scriptInclude
            + "[ \t]+(\\S+)[ \t]*(.*)$", Pattern.MULTILINE);
    private final static Pattern scriptIncludeWithoutScriptPattern = Pattern.compile("^" + JsonConverter.scriptIncludeComments
            + JsonConverter.scriptInclude + "[ \t]*$", Pattern.MULTILINE);
    private final static String noticesInstructions = String.join("|", InstructionType.POST_NOTICES.value(), InstructionType.POST_NOTICE.value(),
            InstructionType.EXPECT_NOTICE.value(), InstructionType.EXPECT_NOTICES.value());
    private final static Predicate<String> hasNoticesInstruction = Pattern.compile("\"TYPE\"\\s*:\\s*\"(" + noticesInstructions + ")\"")
            .asPredicate();

    /** @param type
     * @param configBytes
     * @param dbLayer
     * @param visibleAgentNames
     * @throws SOSJsonSchemaException
     * @throws IOException
     * @throws SOSHibernateException
     * @throws JocConfigurationException */
    public static void validate(ConfigurationType type, byte[] configBytes, InventoryDBLayer dbLayer, Set<String> visibleAgentNames)
            throws SOSJsonSchemaException, IOException, SOSHibernateException, JocConfigurationException {
        validate(type, configBytes, (IConfigurationObject) Globals.objectMapper.readValue(configBytes, JocInventory.CLASS_MAPPING.get(type)), dbLayer,
                visibleAgentNames);
    }

    /** @param type
     * @param config
     * @param dbLayer
     * @param visibleAgentNames
     * @throws SOSJsonSchemaException
     * @throws IOException
     * @throws SOSHibernateException
     * @throws JocConfigurationException */
    public static void validate(ConfigurationType type, IConfigurationObject config, InventoryDBLayer dbLayer, Set<String> visibleAgentNames)
            throws SOSJsonSchemaException, IOException, SOSHibernateException, JocConfigurationException {
        validate(type, Globals.objectMapper.writeValueAsBytes(config), config, dbLayer, visibleAgentNames);
    }

    /** @param type
     * @param configBytes
     * @throws SOSJsonSchemaException
     * @throws IOException
     * @throws SOSHibernateException
     * @throws JocConfigurationException */
    public static void validate(ConfigurationType type, byte[] configBytes) throws SOSJsonSchemaException, IOException, SOSHibernateException,
            JocConfigurationException {
        validate(type, configBytes, (IConfigurationObject) Globals.objectMapper.readValue(configBytes, JocInventory.CLASS_MAPPING.get(type)), null,
                null);
    }

    /** @param type
     * @param config
     * @throws SOSJsonSchemaException
     * @throws IOException
     * @throws SOSHibernateException
     * @throws JocConfigurationException */
    public static void validate(ConfigurationType type, IConfigurationObject config) throws SOSJsonSchemaException, IOException,
            SOSHibernateException, JocConfigurationException {
        validate(type, Globals.objectMapper.writeValueAsBytes(config), config, null, null);
    }

    private static void validate(ConfigurationType type, byte[] configBytes, IConfigurationObject config, InventoryDBLayer dbLayer,
            Set<String> visibleAgentNames) throws SOSJsonSchemaException, IOException, SOSHibernateException, JocConfigurationException {
        JsonValidator.validate(configBytes, URI.create(JocInventory.SCHEMA_LOCATION.get(type)));
        if (ConfigurationType.WORKFLOW.equals(type) || ConfigurationType.SCHEDULE.equals(type) || ConfigurationType.FILEORDERSOURCE.equals(type)) {
            SOSHibernateSession session = null;
            try {
                if (dbLayer == null) {
                    session = Globals.createSosHibernateStatelessConnection("validate");
                    dbLayer = new InventoryDBLayer(session);
                }
                if (ConfigurationType.WORKFLOW.equals(type)) {
                    String json = new String(configBytes, StandardCharsets.UTF_8);
                    InventoryAgentInstancesDBLayer agentDBLayer = null;
                    if (visibleAgentNames == null) {
                        agentDBLayer = new InventoryAgentInstancesDBLayer(dbLayer.getSession());
                    }
                    Workflow workflow = (Workflow) config;
                    List<String> jobResources = validateWorkflowJobs(workflow, dbLayer.getScriptNames());
                    if (workflow.getJobResourceNames() != null) {
                        jobResources.addAll(workflow.getJobResourceNames());
                    }
                    // JsonValidator.validateStrict(configBytes, URI.create("classpath:/raml/inventory/schemas/workflow/workflowJobs-schema.json"));
                    validateOrderPreparation(workflow.getOrderPreparation());
                    List<String> boardNames = hasNoticesInstruction.test(json) ? dbLayer.getBoardNames() : Collections.emptyList();
                    Set<String> jobNames = workflow.getJobs() == null ? Collections.emptySet() : workflow.getJobs().getAdditionalProperties()
                            .keySet();
                    validateInstructions(workflow.getInstructions(), "instructions", jobNames, workflow.getOrderPreparation(),
                            new HashMap<String, String>(), boardNames, dbLayer);
                    // validateJobArguments(workflow.getJobs(), workflow.getOrderPreparation());
                    validateLockRefs(json, dbLayer);
                    //validateBoardRefs(json, dbLayer);
                    validateJobResourceRefs(jobResources, dbLayer);
                    validateAgentRefs(json, agentDBLayer, visibleAgentNames);
                } else if (ConfigurationType.SCHEDULE.equals(type)) {
                    Schedule schedule = (Schedule) config;
                    validateCalendarRefs(schedule, dbLayer);

                    schedule = JocInventory.setWorkflowNames(schedule);
                    int namesSize = schedule.getWorkflowNames().size();
                    if (namesSize == 0) {
                        throw new JocConfigurationException("Missing assigned Workflows");
                    }
                    String position = "$.workflowNames";
                    for (String workflowName : schedule.getWorkflowNames()) {
                        String json = validateWorkflowRef(workflowName, dbLayer, position);
                        Workflow w = Globals.objectMapper.readValue(json, Workflow.class);
                        Requirements r = w.getOrderPreparation();
                        if (namesSize >= JocInventory.SCHEDULE_MIN_MULTIPLE_WORKFLOWS_SIZE) {// check only multiple workflows
                            if (r != null && r.getParameters() != null && r.getParameters().getAdditionalProperties() != null && r.getParameters()
                                    .getAdditionalProperties().size() > 0) {
                                throw new JocConfigurationException(String.format(
                                        "%s: Multiple workflows with order variables are not permitted: schedule=%s, workflowName=%s, %s order variables",
                                        position, schedule.getPath(), workflowName, r.getParameters().getAdditionalProperties().size()));
                            }
                        }
                        validateOrderParameterisations(schedule.getOrderParameterisations(), r, "$.variableSets");
                    }
                } else if (ConfigurationType.FILEORDERSOURCE.equals(type)) {
                    FileOrderSource fileOrderSource = (FileOrderSource) config;
                    validateWorkflowRef(fileOrderSource.getWorkflowName(), dbLayer, "$.workflowName");
                    if (fileOrderSource.getDirectoryExpr() != null) {
                        validateExpression("$.directoryExpr: ", fileOrderSource.getDirectoryExpr());
                    }
                }
            } finally {
                Globals.disconnect(session);
            }
        } else if (ConfigurationType.JOBRESOURCE.equals(type)) {
            JobResource jobResource = (JobResource) config;
            if (jobResource.getEnv() != null) {
                validateExpression("$.env", jobResource.getEnv().getAdditionalProperties());
            }
            if (jobResource.getArguments() != null) {
                validateExpression("$.arguments", jobResource.getArguments().getAdditionalProperties());
            }
        } else if (ConfigurationType.NOTICEBOARD.equals(type)) {
            Board board = (Board) config;
            if (board.getPostOrderToNoticeId() != null) {
                validateExpression("$.postOrderToNotice: ", board.getPostOrderToNoticeId());
            }
            if (board.getExpectOrderToNoticeId() != null) {
                validateExpression("$.expectOrderToNotice: ", board.getExpectOrderToNoticeId());
            }
            if (board.getEndOfLife() != null) {
                validateExpression("$.endOfLife: ", board.getEndOfLife());
            }
        }
    }

    private static void validateJobResourceRefs(List<String> jobResources, InventoryDBLayer dbLayer) throws SOSHibernateException {
        if (!jobResources.isEmpty()) {
            List<DBItemInventoryConfiguration> dbJobResources = dbLayer.getConfigurationByNames(jobResources, ConfigurationType.JOBRESOURCE
                    .intValue());
            if (dbJobResources == null || dbJobResources.isEmpty()) {
                throw new JocConfigurationException("Missing assigned JobResources: " + jobResources.toString());
            } else {
                jobResources.removeAll(dbJobResources.stream().map(DBItemInventoryConfiguration::getName).collect(Collectors.toSet()));
                if (!jobResources.isEmpty()) {
                    throw new JocConfigurationException("Missing assigned JobResources: " + jobResources.toString());
                }
            }
        }
    }

    private static String validateWorkflowRef(String workflowName, InventoryDBLayer dbLayer, String position) throws JocConfigurationException,
            SOSHibernateException {
        List<DBItemInventoryConfiguration> workflowPaths = dbLayer.getConfigurationByName(workflowName, ConfigurationType.WORKFLOW.intValue());
        if (workflowPaths == null || !workflowPaths.stream().anyMatch(w -> workflowName.equals(w.getName()))) {
            throw new JocConfigurationException(position + ": Missing assigned Workflow: " + workflowName);
        }
        return workflowPaths.get(0).getContent();
    }

    private static void validateCalendarRefs(Schedule schedule, InventoryDBLayer dbLayer) throws SOSHibernateException, JocConfigurationException {
        List<String> calendarNames = schedule.getCalendars().stream().map(AssignedCalendars::getCalendarName).distinct().collect(Collectors.toList());
        if (schedule.getNonWorkingDayCalendars() != null && !schedule.getNonWorkingDayCalendars().isEmpty()) {
            calendarNames.addAll(schedule.getNonWorkingDayCalendars().stream().map(AssignedNonWorkingDayCalendars::getCalendarName).collect(Collectors
                    .toSet()));
        }
        List<DBItemInventoryConfiguration> dbCalendars = dbLayer.getCalendarsByNames(calendarNames);
        if (dbCalendars == null || dbCalendars.isEmpty()) {
            throw new JocConfigurationException("Missing assigned Calendars: " + calendarNames.toString());
        } else if (dbCalendars.size() < calendarNames.size()) {
            calendarNames.removeAll(dbCalendars.stream().map(DBItemInventoryConfiguration::getName).collect(Collectors.toSet()));
            throw new JocConfigurationException("Missing assigned Calendars: " + calendarNames.toString());
        }

    }

    private static void validateAgentRefs(String json, InventoryAgentInstancesDBLayer dbLayer, Set<String> visibleAgentNames)
            throws SOSHibernateException, JocConfigurationException {
        Matcher m = Pattern.compile("\"agentName\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        Set<String> agents = new HashSet<>();
        while (m.find()) {
            if (m.group(1) != null && !m.group(1).isEmpty()) {
                agents.add(m.group(1));
            }
        }
        if (!agents.isEmpty()) {
            if (visibleAgentNames != null) {
                visibleAgentNames.forEach(a -> agents.remove(a));
            } else {
                dbLayer.getVisibleAgentNames().forEach(a -> agents.remove(a));
            }
            if (!agents.isEmpty()) {
                throw new JocConfigurationException("Missing assigned Agents: " + agents.toString());
            }
        }
    }

    private static void validateLockRefs(String json, InventoryDBLayer dbLayer) throws SOSHibernateException, JocConfigurationException {
        Matcher m = Pattern.compile("\"lockName\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        Set<String> locks = new HashSet<>();
        while (m.find()) {
            if (m.group(1) != null && !m.group(1).isEmpty()) {
                locks.add(m.group(1));
            }
        }
        if (!locks.isEmpty()) {
            List<DBItemInventoryConfiguration> dbLocks = dbLayer.getConfigurationByNames(locks.stream().collect(Collectors.toList()),
                    ConfigurationType.LOCK.intValue());
            if (dbLocks == null || dbLocks.isEmpty()) {
                throw new JocConfigurationException("Missing assigned Locks: " + locks.toString());
            } else {
                dbLocks.stream().map(DBItemInventoryConfiguration::getName).forEach(l -> locks.remove(l));
                if (!locks.isEmpty()) {
                    throw new JocConfigurationException("Missing assigned Locks: " + locks.toString());
                }
            }
        }
    }

//    private static void validateBoardRefs(String json, InventoryDBLayer dbLayer) throws SOSHibernateException, JocConfigurationException {
//        Matcher m = Pattern.compile("\"(?:noticeB|b)oardName\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
//        Set<String> boards = new HashSet<>();
//        while (m.find()) {
//            if (m.group(1) != null && !m.group(1).isEmpty()) {
//                boards.add(m.group(1));
//            }
//        }
//        if (!boards.isEmpty()) {
//            List<DBItemInventoryConfiguration> dbBoards = dbLayer.getConfigurationByNames(boards.stream().collect(Collectors.toList()),
//                    ConfigurationType.NOTICEBOARD.intValue());
//            if (dbBoards == null || dbBoards.isEmpty()) {
//                throw new JocConfigurationException("Missing assigned Notice Boards: " + boards.toString());
//            } else {
//                boards.removeAll(dbBoards.stream().map(DBItemInventoryConfiguration::getName).collect(Collectors.toSet()));
//                if (!boards.isEmpty()) {
//                    throw new JocConfigurationException("Missing assigned Notice Boards: " + boards.toString());
//                }
//            }
//        }
//    }

    private static List<String> validateWorkflowJobs(Workflow workflow, Set<String> releasedScripts) throws JsonProcessingException, IOException,
            SOSJsonSchemaException {
        List<String> jobResources = new ArrayList<>();
        if (workflow.getJobs() == null) {
            return jobResources;
        }
        for (Map.Entry<String, Job> entry : workflow.getJobs().getAdditionalProperties().entrySet()) {
            // TODO check JobResources references in Job
            try {
                Job job = entry.getValue();
                JsonValidator.validate(Globals.objectMapper.writeValueAsBytes(job), URI.create("classpath:/raml/inventory/schemas/job/job-schema.json"));
                if (job.getJobResourceNames() != null) {
                    jobResources.addAll(job.getJobResourceNames());
                }
            } catch (SOSJsonSchemaException e) {
                String msg = e.getMessage().replaceAll("(\\$\\.)", "$1jobs['" + entry.getKey() + "'].");
                throw new SOSJsonSchemaException(msg);
            }
            switch (entry.getValue().getExecutable().getTYPE()) {
            case InternalExecutable:
                ExecutableJava ej = entry.getValue().getExecutable().cast();
                if (ej.getArguments() != null) {
                    validateExpression("$.jobs['" + entry.getKey() + "'].executable.arguments", ej.getArguments().getAdditionalProperties());
                }
                break;
            case ScriptExecutable:
            case ShellScriptExecutable:
                ExecutableScript es = entry.getValue().getExecutable().cast();
                if (es.getEnv() != null) {
                    validateExpression("$.jobs['" + entry.getKey() + "'].executable.env", es.getEnv().getAdditionalProperties());
                }
                if (es.getScript() != null) {
                    Matcher m = scriptIncludePattern.matcher(es.getScript());
                    while (m.find()) {
                        String scriptName = m.group(2);
                        if (!releasedScripts.contains(scriptName)) {
                            throw new JocConfigurationException("$.jobs['" + entry.getKey() + "'].executable.script referenced an unknown script '"
                                    + scriptName + "'");
                        }
                        try {
                            JsonConverter.parseReplaceInclude(m.group(3)); // m.group(3) = "--replace="","" ...
                        } catch (Exception e) {
                            throw new JocConfigurationException("$.jobs['" + entry.getKey() + "'].executable.script: Invalid script include '" + m
                                    .group(0) + "'. Replace arguments must have the form: --replace=\"...\",\"...\"");
                        }
                    }
                    m = scriptIncludeWithoutScriptPattern.matcher(es.getScript());
                    while (m.find()) {
                        throw new JocConfigurationException("$.jobs['" + entry.getKey()
                                + "'].executable.script contains script include without script name");
                    }
                }
                break;
            }
            validateJobNotification(entry.getKey(), entry.getValue());
        }
        return jobResources;
    }

    private static void validateJobNotification(String jobName, Job job) throws JocConfigurationException {
        if (job == null || job.getNotification() == null || job.getNotification().getMail() == null) {
            return;
        }
        if (job.getNotification().getTypes() != null && job.getNotification().getTypes().size() > 0) {
            // to is required, when cc or bcc defined and the mail is not suppressed
            if (SOSString.isEmpty(job.getNotification().getMail().getTo())) {
                if (!SOSString.isEmpty(job.getNotification().getMail().getCc()) || !SOSString.isEmpty(job.getNotification().getMail().getBcc())) {
                    throw new JocConfigurationException(String.format("$.jobs['%s'].notification.mail: missing \"to\"", jobName));
                }
            }
        }
        try {
            String content = Globals.objectMapper.writeValueAsString(job.getNotification());
            if (content.length() > HistoryConstants.MAX_LEN_NOTIFICATION) {
                throw new JocConfigurationException(String.format(
                        "$.jobs['%s'].notification: reduce recipients, max json length %s (current length=%s) exceeded", jobName,
                        HistoryConstants.MAX_LEN_NOTIFICATION, content.length()));
            }

        } catch (JsonProcessingException e) {

        }
    }

    private static void validateInstructions(Collection<Instruction> instructions, String position, Set<String> jobNames,
            Requirements orderPreparation, Map<String, String> labels, List<String> boardNames, InventoryDBLayer dbLayer) throws SOSJsonSchemaException,
            JsonProcessingException, IOException, JocConfigurationException, SOSHibernateException {
        if (instructions != null) {
            int index = 0;
            for (Instruction inst : instructions) {
                String instPosition = position + "[" + index + "].";
                try {
                    JsonValidator.validateFailFast(Globals.objectMapper.writeValueAsBytes(inst), URI.create(JocInventory.INSTRUCTION_SCHEMA_LOCATION
                            .get(inst.getTYPE())));
                } catch (SOSJsonSchemaException e) {
                    String msg = e.getMessage().replaceFirst("(\\$\\.)", "$1" + instPosition);
                    throw new SOSJsonSchemaException(msg);
                }
                switch (inst.getTYPE()) {
                case EXECUTE_NAMED:
                    NamedJob nj = inst.cast();
                    if (!jobNames.contains(nj.getJobName())) {
                        throw new SOSJsonSchemaException("$." + instPosition + "jobName: job '" + nj.getJobName() + "' doesn't exist");
                    }
                    if (labels.containsKey(nj.getLabel())) {
                        throw new SOSJsonSchemaException("$." + instPosition + "label: duplicate label '" + nj.getLabel() + "' with " + labels.get(nj
                                .getLabel()));
                    } else {
                        labels.put(nj.getLabel(), "$." + instPosition + "label");
                    }
                    // validateArguments(nj.getDefaultArguments(), orderPreparation, "$." + instPosition + "defaultArguments");
                    // validateArgumentKeys(nj.getDefaultArguments(), "$." + instPosition + "defaultArguments");
                    break;
                case FORK:
                    ForkJoin fj = inst.cast();
                    int branchIndex = 0;
                    String branchPosition = instPosition + "branches";
                    Map<String, String> resultKeys = new HashMap<>();
                    for (Branch branch : fj.getBranches()) {
                        BranchWorkflow bw = branch.getWorkflow();
                        if (bw != null) {
                            if (bw.getResult() != null && bw.getResult().getAdditionalProperties() != null) {
                                String branchInstPosition = branchPosition + "[" + branchIndex + "].workflow";
                                for (Map.Entry<String, String> entry : bw.getResult().getAdditionalProperties().entrySet()) {
                                    validateExpression("$." + branchInstPosition + ".result", entry.getKey(), entry.getValue());
                                    if (resultKeys.containsKey(entry.getKey())) {
                                        throw new JocConfigurationException("$." + branchInstPosition + ".result: duplicate key '" + entry.getKey()
                                                + "': already used in " + resultKeys.get(entry.getKey()));
                                    } else {
                                        resultKeys.put(entry.getKey(), branchInstPosition);
                                    }
                                }
                            }
                        }
                        branchIndex++;
                    }
                    resultKeys = null;
                    branchIndex = 0;
                    for (Branch branch : fj.getBranches()) {
                        String branchInstPosition = branchPosition + "[" + branchIndex + "].";
                        if (branch.getWorkflow() != null) {
                            validateInstructions(branch.getWorkflow().getInstructions(), branchInstPosition + "instructions", jobNames,
                                    orderPreparation, labels, boardNames, dbLayer);
                        }
                        branchIndex++;
                    }
                    break;
                case FORKLIST:
                    ForkList fl = inst.cast();
                    if (fl.getWorkflow() != null) {
                        validateInstructions(fl.getWorkflow().getInstructions(), instPosition + "forklist.instructions", jobNames, orderPreparation,
                                labels, boardNames, dbLayer);
                    }
                    break;
                case IF:
                    IfElse ifElse = inst.cast();
                    try {
                        // PredicateParser.parse(ifElse.getPredicate());
                        validateExpression(ifElse.getPredicate());
                    } catch (Exception e) {
                        throw new SOSJsonSchemaException("$." + instPosition + "predicate:" + e.getMessage());
                    }
                    if (ifElse.getThen() != null) {
                        validateInstructions(ifElse.getThen().getInstructions(), instPosition + "then.instructions", jobNames, orderPreparation,
                                labels, boardNames, dbLayer);
                    }
                    if (ifElse.getElse() != null) {
                        validateInstructions(ifElse.getElse().getInstructions(), instPosition + "else.instructions", jobNames, orderPreparation,
                                labels, boardNames, dbLayer);
                    }
                    break;
                case TRY:
                    TryCatch tryCatch = inst.cast();
                    validateInstructions(tryCatch.getTry().getInstructions(), instPosition + "try.instructions", jobNames, orderPreparation, labels,
                            boardNames, dbLayer);
                    validateInstructions(tryCatch.getCatch().getInstructions(), instPosition + "catch.instructions", jobNames, orderPreparation,
                            labels, boardNames, dbLayer);
                    break;
                case LOCK:
                    Lock lock = inst.cast();
                    validateInstructions(lock.getLockedWorkflow().getInstructions(), instPosition + "lockedWorkflow.instructions", jobNames,
                            orderPreparation, labels, boardNames, dbLayer);
                    break;
                case PROMPT:
                    Prompt prompt = inst.cast();
                    validateExpression(prompt.getQuestion());
                    break;
                case ADD_ORDER:
                    AddOrder ao = inst.cast();
                    String json = validateWorkflowRef(ao.getWorkflowName(), dbLayer, "$." + instPosition + "workflowName");
                    if (json != null) {
                        Workflow workflowOfAddOrder = Globals.objectMapper.readValue(json, Workflow.class);
                        try {
                            OrdersHelper.checkArguments(ao.getArguments(), workflowOfAddOrder.getOrderPreparation());
                        } catch (Exception e) {
                            throw new JocConfigurationException("$." + instPosition + "arguments: " + e.getMessage());
                        }
                    }
                    break;
                case POST_NOTICE:
                    PostNotice pn = inst.cast();
                    String pnName = pn.getNoticeBoardName();
                    if (boardNames.isEmpty() || !boardNames.contains(pnName)) {
                        throw new JocConfigurationException("$." + instPosition + "noticeBoardName: Missing assigned Notice Board: " + pnName);
                    }
                    break;
                case POST_NOTICES:
                    PostNotices pns = inst.cast();
                    List<String> pnsNames = pns.getNoticeBoardNames();
                    pnsNames.removeAll(boardNames);
                    if (boardNames.isEmpty() || !pnsNames.isEmpty()) {
                        throw new JocConfigurationException("$." + instPosition + "noticeBoardNames: Missing assigned Notice Boards: " + pnsNames
                                .toString());
                    }
                    break;
                case EXPECT_NOTICE:
                    ExpectNotice en = inst.cast();
                    String enName = en.getNoticeBoardName();
                    if (boardNames.isEmpty() || !boardNames.contains(enName)) {
                        throw new JocConfigurationException("$." + instPosition + "noticeBoardName: Missing assigned Notice Board: " + enName);
                    }
                    break;
                case EXPECT_NOTICES:
                    ExpectNotices ens = inst.cast();
                    String ensNamesExpr = ens.getNoticeBoardNames();
                    String ensNamesExpr2 = ensNamesExpr.replaceAll("'[^']*'", "true").replaceAll("\"[^\"]*\"", "true");
                    Either<Problem, JExpression> e = JExpression.parse(ensNamesExpr2);
                    if (e.isLeft()) {
                        throw new JocConfigurationException("$." + instPosition + "noticeBoardNames: " + e.getLeft().message().replace("true", "'...'"));
                    }
                    List<String> ensNames = NoticeToNoticesConverter.expectNoticeBoardsToList(ensNamesExpr);
                    ensNames.removeAll(boardNames);
                    if (boardNames.isEmpty() || !ensNames.isEmpty()) {
                        throw new JocConfigurationException("$." + instPosition + "noticeBoardNames: Missing assigned Notice Boards: " + ensNames
                                .toString());
                    }
                    break;
                case CYCLE:
                    Cycle cycle = inst.cast();
                    validateInstructions(cycle.getCycleWorkflow().getInstructions(), instPosition + "cycleWorkflow.instructions", jobNames,
                            orderPreparation, labels, boardNames, dbLayer);
                    break;
                default:
                    break;
                }
                index++;
            }
        }
    }

    private static void validateOrderPreparation(Requirements orderRequirements) throws JocConfigurationException {
        final Map<String, Parameter> params = (orderRequirements != null && orderRequirements.getParameters() != null) ? orderRequirements
                .getParameters().getAdditionalProperties() : Collections.emptyMap();

        params.forEach((key, value) -> {
            // if ("returnCode".equals(key)) {
            // throw new JocConfigurationException(String.format(
            // "$.orderRequirements.parameters['%s']: 'returnCode' is a reserved word for a parameter.", key));
            // }
            // validateKey(key, "$.orderRequirements.parameters");
            boolean invalid = false;
            if (value.getDefault() != null) {
                Object _default = value.getDefault();
                switch (value.getType()) {
                case String:
                    invalid = (_default instanceof String) == false;
                    if (!invalid) {
                        validateExpression("$.orderPreparation.parameters['" + key + "'].default: ", (String) value.getDefault());
                    }
                    break;
                case Number:
                    invalid = (_default instanceof String) || (_default instanceof Boolean);
                    if (invalid && (_default instanceof String)) {
                        try {
                            new BigDecimal(_default.toString());
                            invalid = false;
                        } catch (Exception e) {
                            throw new JocConfigurationException(String.format(
                                    "$.orderPreparation.parameters['%s'].default: wrong number format: '%s'.", key, _default.toString()));
                        }
                    }
                    break;
                case Boolean:
                    invalid = (_default instanceof Boolean) == false;
                    if (invalid && (_default instanceof String)) {
                        invalid = !"true".equals(_default.toString()) && !"false".equals(_default.toString());
                    }
                    break;
                case List:
                    invalid = (_default instanceof List) == false;
                    // TODO check list params types
                    break;
                }
                if (invalid) {
                    throw new JocConfigurationException(String.format(
                            "$.orderPreparation.parameters['%s'].default: Wrong data type %s (%s is expected).", key, _default.getClass()
                                    .getSimpleName(), value.getType().value()));
                }
            }
            if (value.getFinal() != null) {
                validateExpression("$.orderPreparation.parameters['" + key + "'].final: ", value.getFinal());
            }
            if (ParameterType.List.equals(value.getType())) {
                if (value.getListParameters() == null || value.getListParameters().getAdditionalProperties() == null || value.getListParameters()
                        .getAdditionalProperties().isEmpty()) {
                    throw new JocConfigurationException(String.format(
                            "$.orderPreparation.parameters['%s'].listParameters: missing but required if the parameter is of type 'List'", key));
                }
            }
        });
    }

    private static void validateKey(String key, String position) {
        if (!checkKey.test(key)) {
            if (firstCharOfKeyIsNumber.test(key)) {
                throw new JocConfigurationException(String.format("%s['%s']: the variable name must not start with a number.", position, key));
            }
            throw new JocConfigurationException(String.format("%s['%s']: only characters 'a-zA-Z0-9_' are allowed in the variabe name.", position,
                    key));
        }
    }

    private static void validateArgumentKeys(Environment arguments, String position) throws JocConfigurationException {
        final Map<String, String> args = (arguments != null) ? arguments.getAdditionalProperties() : Collections.emptyMap();
        args.keySet().forEach(key -> validateKey(key, position));
    }

    private static void validateEnvironmentKeys(Environment arguments, String position) throws JocConfigurationException {
        final Map<String, String> args = (arguments != null) ? arguments.getAdditionalProperties() : Collections.emptyMap();
        args.keySet().forEach(key -> validateKey(key, position));
    }

    @SuppressWarnings("unused")
    private static void validateArguments(Environment arguments, Requirements orderPreparation, String position) throws JocConfigurationException {
        final Map<String, Parameter> params = (orderPreparation != null && orderPreparation.getParameters() != null) ? orderPreparation
                .getParameters().getAdditionalProperties() : Collections.emptyMap();
        final Map<String, String> args = (arguments != null) ? arguments.getAdditionalProperties() : Collections.emptyMap();

        // jobs and job instructions can have arguments which are not declared in orderRequrirements ??
        Set<String> keys = args.keySet().stream().filter(arg -> !params.containsKey(arg)).collect(Collectors.toSet());
        if (!keys.isEmpty()) {
            if (keys.size() == 1) {
                throw new JocConfigurationException("Variable " + keys.iterator().next() + " isn't declared in the workflow");
            }
            throw new JocConfigurationException("Variables " + keys.toString() + " aren't declared in the workflow");
        }
        if (!args.isEmpty()) {
            params.forEach((key, value) -> {
                // if (!checkKey.test(key)) {
                // if (firstCharOfKeyIsNumber.test(key)) {
                // throw new JocConfigurationException(String.format("%s['%s']: the variable name must not start with a number.", position, key));
                // }
                // throw new JocConfigurationException(String.format("%s['%s']: only characters 'a-zA-Z0-9_' are allowed in the variabe name.", position, key));
                // }
                boolean invalid = false;
                // arguments in jobs and job instruction are not required caused of orderPreparation ??
                if (value.getDefault() == null && !args.containsKey(key)) { // required
                    throw new JocConfigurationException("Variable '" + key + "' is missing but required");
                }
                if (args.containsKey(key)) {
                    Object curArg = args.get(key);
                    switch (value.getType()) {
                    case String:
                        invalid = (curArg instanceof String) == false;
                        break;
                    case Number:
                        invalid = (curArg instanceof String) || (curArg instanceof Boolean) || (curArg instanceof List);
                        break;
                    case Boolean:
                        invalid = (curArg instanceof Boolean) == false;
                        break;
                    case List:
                        invalid = (curArg instanceof List) == false;
                        // TODO check list params types
                        break;
                    }
                    if (invalid) {
                        throw new JocConfigurationException(String.format("%s['%s']: Wrong data type %s (%s is expected).", key, position, curArg
                                .getClass().getSimpleName(), value.getType().value()));
                    }
                }
            });
        }
    }

    @SuppressWarnings("unused")
    private static void validateOrderParameterisations(List<OrderParameterisation> variableSets, Requirements orderPreparation, String position)
            throws JocConfigurationException {
        if (variableSets != null) {
            if (variableSets.size() != variableSets.stream().map(OrderParameterisation::getOrderName).distinct().mapToInt(e -> 1).sum()) {
                throw new JocConfigurationException(position + ": Order names has to be unique");
            }
            variableSets.stream().map(OrderParameterisation::getVariables).filter(Objects::nonNull).forEach(v -> {
                try {
                    OrdersHelper.checkArguments(v, orderPreparation);
                } catch (Exception e1) {
                    throw new JocConfigurationException(position + ": " + e1.getMessage());
                }
            });
        }
    }

    @SuppressWarnings("unused")
    private static void validateJobArguments(Jobs jobs, Requirements orderPreparation) {
        if (jobs != null) {
            jobs.getAdditionalProperties().forEach((key, value) -> {
                // validateArguments(value.getDefaultArguments(), orderPreparation, "$.jobs['" + key + "'].defaultArguments");
                validateArgumentKeys(value.getDefaultArguments(), "$.jobs['" + key + "'].defaultArguments");
                if (ExecutableType.ScriptExecutable.equals(value.getExecutable().getTYPE())) {
                    ExecutableScript script = value.getExecutable().cast();
                    validateEnvironmentKeys(script.getEnv(), "$.jobs['" + key + "'].executable.env");
                }
            });
        }
    }

    private static void validateExpression(String prefix, Map<String, String> map) throws JocConfigurationException {
        if (map != null) {
            map.forEach((k, v) -> validateExpression(prefix, k, v));
        }
    }

    private static void validateExpression(String prefix, String key, String value) throws JocConfigurationException {
        Either<Problem, JExpression> e = JExpression.parse(value);
        if (e.isLeft()) {
            throw new JocConfigurationException(prefix + "[" + key + "]:" + e.getLeft().message());
        }
    }

    private static void validateExpression(String prefix, String value) throws JocConfigurationException {
        Either<Problem, JExpression> e = JExpression.parse(value);
        if (e.isLeft()) {
            throw new JocConfigurationException(prefix + e.getLeft().message());
        }
    }

    public static void validateExpression(String value) throws JocConfigurationException {
        if (value != null) {
            Either<Problem, JExpression> e = JExpression.parse(value);
            if (e.isLeft()) {
                throw new JocConfigurationException(e.getLeft().message());
            }
        }
    }
}
