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
import com.sos.inventory.model.board.Board;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingDayCalendars;
import com.sos.inventory.model.fileordersource.FileOrderSource;
import com.sos.inventory.model.instruction.AddOrder;
import com.sos.inventory.model.instruction.Cycle;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.ForkList;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.Prompt;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.job.ExecutableJava;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.ExecutableType;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.schedule.VariableSet;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.Parameter;
import com.sos.inventory.model.workflow.ParameterType;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.order.OrdersHelper;
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

    private static Predicate<String> checkKey = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$").asPredicate();
    private static Predicate<String> firstCharOfKeyIsNumber = Pattern.compile("^[0-9]").asPredicate();

    /** @param type
     * @param configBytes
     * @param dbLayer
     * @param enabledAgentNames
     * @throws SOSJsonSchemaException
     * @throws IOException
     * @throws SOSHibernateException
     * @throws JocConfigurationException */
    public static void validate(ConfigurationType type, byte[] configBytes, InventoryDBLayer dbLayer, Set<String> enabledAgentNames)
            throws SOSJsonSchemaException, IOException, SOSHibernateException, JocConfigurationException {
        validate(type, configBytes, (IConfigurationObject) Globals.objectMapper.readValue(configBytes, JocInventory.CLASS_MAPPING.get(type)), dbLayer,
                enabledAgentNames);
    }

    /** @param type
     * @param config
     * @param dbLayer
     * @param enabledAgentNames
     * @throws SOSJsonSchemaException
     * @throws IOException
     * @throws SOSHibernateException
     * @throws JocConfigurationException */
    public static void validate(ConfigurationType type, IConfigurationObject config, InventoryDBLayer dbLayer, Set<String> enabledAgentNames)
            throws SOSJsonSchemaException, IOException, SOSHibernateException, JocConfigurationException {
        validate(type, Globals.objectMapper.writeValueAsBytes(config), config, dbLayer, enabledAgentNames);
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
            Set<String> enabledAgentNames) throws SOSJsonSchemaException, IOException, SOSHibernateException, JocConfigurationException {
        JsonValidator.validate(configBytes, URI.create(JocInventory.SCHEMA_LOCATION.get(type)));
        if (ConfigurationType.WORKFLOW.equals(type) || ConfigurationType.SCHEDULE.equals(type) || ConfigurationType.FILEORDERSOURCE.equals(type)) {
            SOSHibernateSession session = null;
            try {
                if (dbLayer == null) {
                    session = Globals.createSosHibernateStatelessConnection("validate");
                    dbLayer = new InventoryDBLayer(session);
                }
                if (ConfigurationType.WORKFLOW.equals(type)) {
                    InventoryAgentInstancesDBLayer agentDBLayer = null;
                    if (enabledAgentNames == null) {
                        agentDBLayer = new InventoryAgentInstancesDBLayer(dbLayer.getSession());
                    }
                    Workflow workflow = (Workflow) config;
                    List<String> jobResources = validateWorkflowJobs(workflow);
                    if (workflow.getJobResourceNames() != null) {
                        jobResources.addAll(workflow.getJobResourceNames());
                    }
                    // JsonValidator.validateStrict(configBytes, URI.create("classpath:/raml/inventory/schemas/workflow/workflowJobs-schema.json"));
                    validateOrderPreparation(workflow.getOrderPreparation());
                    validateInstructions(workflow.getInstructions(), "instructions", workflow.getJobs().getAdditionalProperties().keySet(), workflow.getOrderPreparation(), new HashMap<String, String>(), dbLayer);
                    // validateJobArguments(workflow.getJobs(), workflow.getOrderPreparation());
                    validateLockRefs(new String(configBytes, StandardCharsets.UTF_8), dbLayer);
                    validateBoardRefs(new String(configBytes, StandardCharsets.UTF_8), dbLayer);
                    validateJobResourceRefs(jobResources, dbLayer);
                    validateAgentRefs(new String(configBytes, StandardCharsets.UTF_8), agentDBLayer, enabledAgentNames);
                } else if (ConfigurationType.SCHEDULE.equals(type)) {
                    Schedule schedule = (Schedule) config;
                    String json = validateWorkflowRef(schedule.getWorkflowName(), dbLayer, "$.workflowName");
                    validateCalendarRefs(schedule, dbLayer);
                    if (json != null) {
                        Workflow workflowOfSchedule = Globals.objectMapper.readValue(json, Workflow.class);
                        validateVariableSets(schedule.getVariableSets(), workflowOfSchedule.getOrderPreparation(), "$.variableSets");
                    }
                } else if (ConfigurationType.FILEORDERSOURCE.equals(type)) {
                    FileOrderSource fileOrderSource = (FileOrderSource) config;
                    validateWorkflowRef(fileOrderSource.getWorkflowName(), dbLayer, "$.workflowName");
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
            List<DBItemInventoryConfiguration> dbJobResources = dbLayer.getConfigurationByNames(jobResources.stream().distinct(),
                    ConfigurationType.JOBRESOURCE.intValue());
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

    private static String validateWorkflowRef(String workflowName, InventoryDBLayer dbLayer, String position) throws JocConfigurationException, SOSHibernateException {
        List<DBItemInventoryConfiguration> workflowPaths = dbLayer.getConfigurationByName(workflowName, ConfigurationType.WORKFLOW.intValue());
        if (workflowPaths == null || !workflowPaths.stream().anyMatch(w -> workflowName.equals(w.getName()))) {
            throw new JocConfigurationException(position + ": Missing assigned Workflow: " + workflowName);
        }
        return workflowPaths.get(0).getContent();
    }

    private static void validateCalendarRefs(Schedule schedule, InventoryDBLayer dbLayer) throws SOSHibernateException, JocConfigurationException {
        Set<String> calendarNames = schedule.getCalendars().stream().map(AssignedCalendars::getCalendarName).collect(Collectors.toSet());
        if (schedule.getNonWorkingDayCalendars() != null && !schedule.getNonWorkingDayCalendars().isEmpty()) {
            calendarNames.addAll(schedule.getNonWorkingDayCalendars().stream().map(AssignedNonWorkingDayCalendars::getCalendarName).collect(Collectors
                    .toSet()));
        }
        List<DBItemInventoryConfiguration> dbCalendars = dbLayer.getCalendarsByNames(calendarNames.stream());
        if (dbCalendars == null || dbCalendars.isEmpty()) {
            throw new JocConfigurationException("Missing assigned Calendars: " + calendarNames.toString());
        } else if (dbCalendars.size() < calendarNames.size()) {
            calendarNames.removeAll(dbCalendars.stream().map(DBItemInventoryConfiguration::getName).collect(Collectors.toSet()));
            throw new JocConfigurationException("Missing assigned Calendars: " + calendarNames.toString());
        }

    }

    private static void validateAgentRefs(String json, InventoryAgentInstancesDBLayer dbLayer, Set<String> enabledAgentNames)
            throws SOSHibernateException, JocConfigurationException {
        Matcher m = Pattern.compile("\"agentName\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        Set<String> agents = new HashSet<>();
        while (m.find()) {
            if (m.group(1) != null && !m.group(1).isEmpty()) {
                agents.add(m.group(1));
            }
        }
        if (!agents.isEmpty()) {
            if (enabledAgentNames != null) {
                agents.removeAll(enabledAgentNames);
            } else {
                agents.removeAll(dbLayer.getEnabledAgentNames());
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
            List<DBItemInventoryConfiguration> dbLocks = dbLayer.getConfigurationByNames(locks.stream(), ConfigurationType.LOCK.intValue());
            if (dbLocks == null || dbLocks.isEmpty()) {
                throw new JocConfigurationException("Missing assigned Locks: " + locks.toString());
            } else {
                locks.removeAll(dbLocks.stream().map(DBItemInventoryConfiguration::getName).collect(Collectors.toSet()));
                if (!locks.isEmpty()) {
                    throw new JocConfigurationException("Missing assigned Locks: " + locks.toString());
                }
            }
        }
    }
    
    private static void validateBoardRefs(String json, InventoryDBLayer dbLayer) throws SOSHibernateException, JocConfigurationException {
        Matcher m = Pattern.compile("\"(?:noticeB|b)oardName\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        Set<String> boards = new HashSet<>();
        while (m.find()) {
            if (m.group(1) != null && !m.group(1).isEmpty()) {
                boards.add(m.group(1));
            }
        }
        if (!boards.isEmpty()) {
            List<DBItemInventoryConfiguration> dbBoards = dbLayer.getConfigurationByNames(boards.stream(), ConfigurationType.NOTICEBOARD.intValue());
            if (dbBoards == null || dbBoards.isEmpty()) {
                throw new JocConfigurationException("Missing assigned Notice Boards: " + boards.toString());
            } else {
                boards.removeAll(dbBoards.stream().map(DBItemInventoryConfiguration::getName).collect(Collectors.toSet()));
                if (!boards.isEmpty()) {
                    throw new JocConfigurationException("Missing assigned Notice Boards: " + boards.toString());
                }
            }
        }
    }
    
    private static List<String> validateWorkflowJobs(Workflow workflow) throws JsonProcessingException, IOException, SOSJsonSchemaException {
        List<String> jobResources = new ArrayList<>();
        for (Map.Entry<String, Job> entry : workflow.getJobs().getAdditionalProperties().entrySet()) {
            // TODO check JobResources references in Job
            try {
                Job job = entry.getValue();
                JsonValidator.validate(Globals.objectMapper.writeValueAsBytes(job), URI.create(JocInventory.SCHEMA_LOCATION
                        .get(ConfigurationType.JOB)));
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
                break;
            }
        }
        return jobResources;
    }

    private static void validateInstructions(Collection<Instruction> instructions, String position, Set<String> jobNames,
            Requirements orderPreparation, Map<String, String> labels, InventoryDBLayer dbLayer) throws SOSJsonSchemaException,
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
                        if (branch.getResult() != null && branch.getResult().getAdditionalProperties() != null) {
                            String branchInstPosition = branchPosition + "[" + branchIndex + "]";
                            for (Map.Entry<String, String> entry : branch.getResult().getAdditionalProperties().entrySet()) {
                                validateExpression("$." + branchInstPosition + ".result", entry.getKey(), entry.getValue());
                                if (resultKeys.containsKey(entry.getKey())) {
                                    throw new JocConfigurationException("$." + branchInstPosition + ".result: duplicate key '" + entry.getKey()
                                            + "': already used in " + resultKeys.get(entry.getKey()));
                                } else {
                                    resultKeys.put(entry.getKey(), branchInstPosition);
                                }
                            }
                        }
                        branchIndex++;
                    }
                    resultKeys = null;
                    for (Branch branch : fj.getBranches()) {
                        String branchInstPosition = branchPosition + "[" + branchIndex + "].";
                        if (branch.getWorkflow() != null) {
                            validateInstructions(branch.getWorkflow().getInstructions(), branchInstPosition + "instructions", jobNames,
                                    orderPreparation, labels, dbLayer);
                        }
                        branchIndex++;
                    }
                    break;
                case FORKLIST:
                    ForkList fl = inst.cast();
                    if (fl.getWorkflow() != null) {
                        validateInstructions(fl.getWorkflow().getInstructions(), instPosition + "forklist.instructions", jobNames, orderPreparation,
                                labels, dbLayer);
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
                                labels, dbLayer);
                    }
                    if (ifElse.getElse() != null) {
                        validateInstructions(ifElse.getElse().getInstructions(), instPosition + "else.instructions", jobNames, orderPreparation,
                                labels, dbLayer);
                    }
                    break;
                case TRY:
                    TryCatch tryCatch = inst.cast();
                    validateInstructions(tryCatch.getTry().getInstructions(), instPosition + "try.instructions", jobNames, orderPreparation, labels,
                            dbLayer);
                    validateInstructions(tryCatch.getCatch().getInstructions(), instPosition + "catch.instructions", jobNames, orderPreparation,
                            labels, dbLayer);
                    break;
                case LOCK:
                    Lock lock = inst.cast();
                    validateInstructions(lock.getLockedWorkflow().getInstructions(), instPosition + "lockedWorkflow.instructions", jobNames,
                            orderPreparation, labels, dbLayer);
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
                case CYCLE:
                    Cycle cycle = inst.cast();
                    validateInstructions(cycle.getCycleWorkflow().getInstructions(), instPosition + "cycleWorkflow.instructions", jobNames,
                            orderPreparation, labels, dbLayer);
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
                            throw new JocConfigurationException(String.format("$.orderPreparation.parameters['%s'].default: wrong number format: '%s'.",
                                    key, _default.toString()));
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
    
    private static void validateVariableSets(List<VariableSet> variableSets, Requirements orderPreparation, String position) throws JocConfigurationException {
        if (variableSets != null) {
            if (variableSets.size() != variableSets.stream().map(VariableSet::getOrderName).distinct().mapToInt(e -> 1).sum()) {
                throw new JocConfigurationException(position + ": Order names has to be unique");
            }
            variableSets.stream().map(VariableSet::getVariables).filter(Objects::nonNull).forEach(v -> {
                try {
                    OrdersHelper.checkArguments(v, orderPreparation);
                } catch (Exception e1) {
                    throw new JocConfigurationException(position + ": " + e1.getMessage());
                }
            });
        }
    }

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
