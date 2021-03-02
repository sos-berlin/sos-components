package com.sos.joc.classes.inventory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.controller.model.common.Variables;
import com.sos.inventory.model.Schedule;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingCalendars;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.ExecutableType;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.Parameter;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

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
        if (ConfigurationType.WORKFLOW.equals(type) || ConfigurationType.SCHEDULE.equals(type)) {
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
                    validateWorkflowJobs(workflow);
                    // JsonValidator.validateStrict(configBytes, URI.create("classpath:/raml/inventory/schemas/workflow/workflowJobs-schema.json"));
                    validateOrderRequirements(workflow.getOrderRequirements());
                    validateInstructions(workflow.getInstructions(), "instructions", workflow.getJobs().getAdditionalProperties().keySet(), workflow.getOrderRequirements(), new HashMap<String, String>());
                    // validateJobArguments(workflow.getJobs(), workflow.getOrderRequirements());
                    validateLockRefs(new String(configBytes, StandardCharsets.UTF_8), dbLayer);
                    validateAgentRefs(new String(configBytes, StandardCharsets.UTF_8), agentDBLayer, enabledAgentNames);
                } else if (ConfigurationType.SCHEDULE.equals(type)) {
                    Schedule schedule = (Schedule) config;
                    validateWorkflowRef(schedule.getWorkflowName(), dbLayer);
                    // String json = validateWorkflowRef(schedule.getWorkflowName(), dbLayer);
                    validateCalendarRefs(schedule, dbLayer);
                    // if (json != null) {
                    // Workflow workflowOfSchedule = (Workflow) Globals.objectMapper.readValue(json, Workflow.class);
                    // validateArguments(schedule.getVariables(), workflowOfSchedule.getOrderRequirements(), "$.variables");
                    // }
                }
            } finally {
                Globals.disconnect(session);
            }
        }
    }

    private static String validateWorkflowRef(String workflowName, InventoryDBLayer dbLayer) throws JocConfigurationException, SOSHibernateException {
        List<DBItemInventoryConfiguration> workflowPaths = dbLayer.getConfigurationByName(workflowName, ConfigurationType.WORKFLOW.intValue());
        if (workflowPaths == null || !workflowPaths.stream().anyMatch(w -> workflowName.equals(w.getName()))) {
            throw new JocConfigurationException("Missing assigned Workflow: " + workflowName);
        }
        return workflowPaths.get(0).getContent();
    }

    private static void validateCalendarRefs(Schedule schedule, InventoryDBLayer dbLayer) throws SOSHibernateException, JocConfigurationException {
        Set<String> calendarNames = schedule.getCalendars().stream().map(AssignedCalendars::getCalendarName).collect(Collectors.toSet());
        if (schedule.getNonWorkingCalendars() != null && !schedule.getNonWorkingCalendars().isEmpty()) {
            calendarNames.addAll(schedule.getNonWorkingCalendars().stream().map(AssignedNonWorkingCalendars::getCalendarName).collect(Collectors
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
        Matcher m = Pattern.compile("\"agentId\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
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
        Matcher m = Pattern.compile("\"lockId\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
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
    
    private static void validateWorkflowJobs(Workflow workflow) throws JsonProcessingException, IOException, SOSJsonSchemaException {
        for (Map.Entry<String, Job> entry : workflow.getJobs().getAdditionalProperties().entrySet()) {
            try {
                JsonValidator.validate(Globals.objectMapper.writeValueAsBytes(entry.getValue()), URI.create(JocInventory.SCHEMA_LOCATION
                        .get(ConfigurationType.JOB)));
            } catch (SOSJsonSchemaException e) {
                String msg = e.getMessage().replaceAll("(\\$\\.)", "$1jobs['" + entry.getKey() + "'].");
                throw new SOSJsonSchemaException(msg);
            }
        }
    }

    private static void validateInstructions(Collection<Instruction> instructions, String position, Set<String> jobNames, Requirements orderRequirements,
            Map<String, String> labels) throws SOSJsonSchemaException, JsonProcessingException, IOException, JocConfigurationException {
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
                case AWAIT:
                case FAIL:
                case FINISH:
                case PUBLISH:
                case RETRY:
                case IMPLICIT_END:
                    break;
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
                    // validateArguments(nj.getDefaultArguments(), orderRequirements, "$." + instPosition + "defaultArguments");
                    // validateArgumentKeys(nj.getDefaultArguments(), "$." + instPosition + "defaultArguments");
                    break;
                case FORK:
                    ForkJoin fj = inst.cast();
                    int branchIndex = 0;
                    String branchPosition = instPosition + "branches";
                    for (Branch branch : fj.getBranches()) {
                        String branchInstPosition = branchPosition + "[" + branchIndex + "].";
                        validateInstructions(branch.getWorkflow().getInstructions(), branchInstPosition + "instructions", jobNames, orderRequirements, labels);
                        branchIndex++;
                    }
                    break;
                case IF:
                    IfElse ifElse = inst.cast();
                    try {
                        PredicateParser.parse(ifElse.getPredicate());
                    } catch (Exception e) {
                        throw new SOSJsonSchemaException("$." + instPosition + "predicate:" + e.getMessage());
                    }
                    validateInstructions(ifElse.getThen().getInstructions(), instPosition + "then.instructions", jobNames, orderRequirements, labels);
                    if (ifElse.getElse() != null) {
                        validateInstructions(ifElse.getElse().getInstructions(), instPosition + "else.instructions", jobNames, orderRequirements, labels);
                    }
                    break;
                case TRY:
                    TryCatch tryCatch = inst.cast();
                    validateInstructions(tryCatch.getTry().getInstructions(), instPosition + "try.instructions", jobNames, orderRequirements, labels);
                    validateInstructions(tryCatch.getCatch().getInstructions(), instPosition + "catch.instructions", jobNames, orderRequirements, labels);
                    break;
                case LOCK:
                    Lock lock = inst.cast();
                    validateInstructions(lock.getLockedWorkflow().getInstructions(), instPosition + "lockedWorkflow.instructions", jobNames, orderRequirements,
                            labels);
                    break;
                }
                index++;
            }
        }
    }

    private static void validateOrderRequirements(Requirements orderRequirements) throws JocConfigurationException {
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
                    break;
                case Number:
                    invalid = (_default instanceof String) || (_default instanceof Boolean);
                    break;
                case Boolean:
                    invalid = (_default instanceof Boolean) == false;
                    break;
                }
                if (invalid) {
                    throw new JocConfigurationException(String.format(
                            "$.orderRequirements.parameters['%s'].default: Wrong data type %s (%s is expected).", key, _default.getClass()
                                    .getSimpleName(), value.getType().value()));
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

    private static void validateArgumentKeys(Variables arguments, String position) throws JocConfigurationException {
        final Map<String, Object> args = (arguments != null) ? arguments.getAdditionalProperties() : Collections.emptyMap();
        args.keySet().forEach(key -> validateKey(key, position));
    }

    private static void validateEnvironmentKeys(Environment arguments, String position) throws JocConfigurationException {
        final Map<String, String> args = (arguments != null) ? arguments.getAdditionalProperties() : Collections.emptyMap();
        args.keySet().forEach(key -> validateKey(key, position));
    }

    private static void validateArguments(Variables arguments, Requirements orderRequirements, String position) throws JocConfigurationException {
        final Map<String, Parameter> params = (orderRequirements != null && orderRequirements.getParameters() != null) ? orderRequirements
                .getParameters().getAdditionalProperties() : Collections.emptyMap();
        final Map<String, Object> args = (arguments != null) ? arguments.getAdditionalProperties() : Collections.emptyMap();

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
                // arguments in jobs and job instruction are not required caused of orderRequirements ??
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
                        invalid = (curArg instanceof String) || (curArg instanceof Boolean);
                        break;
                    case Boolean:
                        invalid = (curArg instanceof Boolean) == false;
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

    private static void validateJobArguments(Jobs jobs, Requirements orderRequirements) {
        if (jobs != null) {
            jobs.getAdditionalProperties().forEach((key, value) -> {
                // validateArguments(value.getDefaultArguments(), orderRequirements, "$.jobs['" + key + "'].defaultArguments");
                validateArgumentKeys(value.getDefaultArguments(), "$.jobs['" + key + "'].defaultArguments");
                if (ExecutableType.ScriptExecutable.equals(value.getExecutable().getTYPE())) {
                    ExecutableScript script = value.getExecutable().cast();
                    validateEnvironmentKeys(script.getEnv(), "$.jobs['" + key + "'].executable.env");
                }
            });
        }
    }
}
