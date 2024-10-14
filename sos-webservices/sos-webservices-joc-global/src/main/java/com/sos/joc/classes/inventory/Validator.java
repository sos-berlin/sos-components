package com.sos.joc.classes.inventory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.board.Board;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingDayCalendars;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.fileordersource.FileOrderSource;
import com.sos.inventory.model.instruction.AddOrder;
import com.sos.inventory.model.instruction.ConsumeNotices;
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
import com.sos.inventory.model.instruction.Options;
import com.sos.inventory.model.instruction.PostNotice;
import com.sos.inventory.model.instruction.PostNotices;
import com.sos.inventory.model.instruction.Prompt;
import com.sos.inventory.model.instruction.StickySubagent;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.job.ExecutableJava;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.InternalExecutableType;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.inventory.model.jobtemplate.JobTemplate;
import com.sos.inventory.model.report.Report;
import com.sos.inventory.model.schedule.OrderParameterisation;
import com.sos.inventory.model.schedule.OrderPositions;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.BranchWorkflow;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.ListParameters;
import com.sos.inventory.model.workflow.Parameter;
import com.sos.inventory.model.workflow.ParameterType;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.common.StringSizeSanitizer;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.reporting.RunReport;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.classes.tag.GroupedTag;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.common.HistoryConstants;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.order.BlockPosition;
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
            InstructionType.EXPECT_NOTICE.value(), InstructionType.EXPECT_NOTICES.value(), InstructionType.CONSUME_NOTICES.value());
    private final static Predicate<String> hasNoticesInstruction = Pattern.compile("\"TYPE\"\\s*:\\s*\"(" + noticesInstructions + ")\"")
            .asPredicate();
    private static boolean hasLicense = false;

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
        validate(type, configBytes, (IConfigurationObject) Globals.objectMapper.readValue(configBytes, JocInventory.CLASS_MAPPING.get(type)),
                (InventoryDBLayer) null, null);
    }

    /** @param type
     * @param config
     * @throws SOSJsonSchemaException
     * @throws IOException
     * @throws SOSHibernateException
     * @throws JocConfigurationException */
    public static void validate(ConfigurationType type, IConfigurationObject config) throws SOSJsonSchemaException, IOException,
            SOSHibernateException, JocConfigurationException {
        validate(type, Globals.objectMapper.writeValueAsBytes(config), config, (InventoryDBLayer) null, null);
    }

    private static void validate(ConfigurationType type, byte[] configBytes, IConfigurationObject config, InventoryDBLayer dbLayer,
            Set<String> visibleAgentNames) throws SOSJsonSchemaException, IOException, SOSHibernateException, JocConfigurationException {
        JsonValidator.validate(configBytes, URI.create(JocInventory.SCHEMA_LOCATION.get(type)));
        if (ConfigurationType.WORKFLOW.equals(type) || ConfigurationType.SCHEDULE.equals(type) || ConfigurationType.FILEORDERSOURCE.equals(type)
                || ConfigurationType.JOBTEMPLATE.equals(type)) {
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
                    //TODO should know the workflow name to check if it is referenced in a file order source 
                    //     with dbLayer.getNumOfUsedFileOrderSourcesByWorkflowName(workflowName).intValue()
                    
                    // JsonValidator.validateStrict(configBytes, URI.create("classpath:/raml/inventory/schemas/workflow/workflowJobs-schema.json"));
                    validateOrderPreparation(workflow.getOrderPreparation());
                    List<String> boardNames = hasNoticesInstruction.test(json) ? dbLayer.getBoardNames() : Collections.emptyList();
                    Jobs jobs = workflow.getJobs() == null ? new Jobs() : workflow.getJobs();
                    Set<String> invalidAgentRefs = getInvalidAgentRefs(json, agentDBLayer, visibleAgentNames);
                    hasLicense = AgentHelper.hasClusterLicense();
                    validateInstructions(workflow.getInstructions(), "instructions", jobs, workflow.getOrderPreparation(),
                            new HashMap<String, String>(), invalidAgentRefs, boardNames, false, dbLayer);
                    //validateJobArguments(workflow.getJobs(), workflow.getOrderPreparation());
                    //validateJobTags(jobs);
                    validateLockRefs(json, dbLayer);
                    //validateBoardRefs(json, dbLayer);
                    validateJobResourceRefs(jobResources, dbLayer);
                    //validateAgentRefs(json, agentDBLayer, visibleAgentNames);
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
                            if (orderPreparationHasRequiredParameters(w.getOrderPreparation())) {
                                throw new JocConfigurationException(String.format(
                                        "%s: Multiple workflows with required order variables are not permitted: schedule=%s, workflowName=%s",
                                        position, schedule.getPath(), workflowName));
                            }
                        }
                        validateOrderParameterisations(schedule.getOrderParameterisations(), r, workflowName, w, "$.orderParameterisations");
                    }
                } else if (ConfigurationType.FILEORDERSOURCE.equals(type)) {
                    FileOrderSource fileOrderSource = (FileOrderSource) config;
                    String position = "$.workflowName";
                    String json = validateWorkflowRef(fileOrderSource.getWorkflowName(), dbLayer, position);
                    Workflow w = Globals.objectMapper.readValue(json, Workflow.class);
                    validateFileVariable(w.getOrderPreparation(), fileOrderSource.getWorkflowName(), position);
                    validateFileOrderSourceDirAndPattern(fileOrderSource);
                } else if (ConfigurationType.JOBTEMPLATE.equals(type)) {
                    JobTemplate jobTemplate = (JobTemplate) config;
                    validateJobTemplateJob(jobTemplate, dbLayer.getScriptNames());
                    // TODO something like validateOrderPreparation(workflow.getOrderPreparation());
                    validateJobResourceRefs(jobTemplate.getJobResourceNames(), dbLayer);
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
                validateExpression("$.postOrderToNoticeId: ", board.getPostOrderToNoticeId());
            }
            if (board.getExpectOrderToNoticeId() != null) {
                validateExpression("$.expectOrderToNoticeId: ", board.getExpectOrderToNoticeId());
            }
            if (board.getEndOfLife() != null) {
                validateExpression("$.endOfLife: ", board.getEndOfLife());
            }
        } else if (ConfigurationType.REPORT.equals(type)) {
            validateReport((Report) config);
        }
    }
    
    public static void revalidate(ConfigurationType type, byte[] configBytes, IConfigurationObject config,
            Map<ConfigurationType, Set<String>> invObjNames, Map<String, String> workflowJsonsByName, Set<String> visibleAgentNames)
            throws SOSJsonSchemaException, IOException, JocConfigurationException {
        JsonValidator.validate(configBytes, URI.create(JocInventory.SCHEMA_LOCATION.get(type)));
        if (ConfigurationType.WORKFLOW.equals(type) || ConfigurationType.SCHEDULE.equals(type) || ConfigurationType.FILEORDERSOURCE.equals(type)
                || ConfigurationType.JOBTEMPLATE.equals(type)) {
            if (ConfigurationType.WORKFLOW.equals(type)) {
                String json = new String(configBytes, StandardCharsets.UTF_8);
                InventoryAgentInstancesDBLayer agentDBLayer = null;
                Workflow workflow = (Workflow) config;
                List<String> jobResources = validateWorkflowJobs(workflow, invObjNames.getOrDefault(ConfigurationType.INCLUDESCRIPT, Collections
                        .emptySet()));
                if (workflow.getJobResourceNames() != null) {
                    jobResources.addAll(workflow.getJobResourceNames());
                }
                validateOrderPreparation(workflow.getOrderPreparation());
                Jobs jobs = workflow.getJobs() == null ? new Jobs() : workflow.getJobs();
                Set<String> invalidAgentRefs = getInvalidAgentRefs(json, agentDBLayer, visibleAgentNames);
                hasLicense = AgentHelper.hasClusterLicense();
                validateInstructions(workflow.getInstructions(), "instructions", jobs, workflow.getOrderPreparation(), new HashMap<String, String>(),
                        invalidAgentRefs, invObjNames.getOrDefault(ConfigurationType.NOTICEBOARD, Collections.emptySet()), false, workflowJsonsByName);
                //validateJobTags(jobs);
                validateLockRefs(json, invObjNames.getOrDefault(ConfigurationType.LOCK, Collections.emptySet()));
                validateJobResourceRefs(jobResources, invObjNames.getOrDefault(ConfigurationType.JOBRESOURCE, Collections.emptySet()));
            } else if (ConfigurationType.SCHEDULE.equals(type)) {
                Schedule schedule = (Schedule) config;
                validateCalendarRefs(schedule, invObjNames.getOrDefault(ConfigurationType.WORKINGDAYSCALENDAR, Collections.emptySet()), invObjNames
                        .getOrDefault(ConfigurationType.NONWORKINGDAYSCALENDAR, Collections.emptySet()));

                schedule = JocInventory.setWorkflowNames(schedule);
                int namesSize = schedule.getWorkflowNames().size();
                if (namesSize == 0) {
                    throw new JocConfigurationException("Missing assigned Workflows");
                }
                String position = "$.workflowNames";
                for (String workflowName : schedule.getWorkflowNames()) {
                    String json = validateWorkflowRef(workflowName, workflowJsonsByName, position);
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
                    validateOrderParameterisations(schedule.getOrderParameterisations(), r, workflowName, w, "$.orderParameterisations");
                }
            } else if (ConfigurationType.FILEORDERSOURCE.equals(type)) {
                String position = "$.workflowName";
                FileOrderSource fileOrderSource = (FileOrderSource) config;
                String json = validateWorkflowRef(fileOrderSource.getWorkflowName(), workflowJsonsByName, position);
                Workflow w = Globals.objectMapper.readValue(json, Workflow.class);
                validateFileVariable(w.getOrderPreparation(), fileOrderSource.getWorkflowName(), position);
                validateFileOrderSourceDirAndPattern(fileOrderSource);
            } else if (ConfigurationType.JOBTEMPLATE.equals(type)) {
                JobTemplate jobTemplate = (JobTemplate) config;
                validateJobTemplateJob(jobTemplate, invObjNames.getOrDefault(ConfigurationType.INCLUDESCRIPT, Collections.emptySet()));
                // TODO something like validateOrderPreparation(workflow.getOrderPreparation());
                validateJobResourceRefs(jobTemplate.getJobResourceNames(), invObjNames.getOrDefault(ConfigurationType.JOBRESOURCE, Collections
                        .emptySet()));
            } else if (ConfigurationType.REPORT.equals(type)) {
                validateReport((Report) config);
            }
        }
    }
    
    private static void validateReport(Report report) {
        if (!report.getTemplateName().isSupported()) {
            throw new JocConfigurationException(String.format( "Template '%s' is not longer supported", report.getTemplateName().value()));
        }
        if (report.getMonthTo() != null) {
            try {
                LocalDate mTo = RunReport.relativeDateToSpecificLocalDate(report.getMonthTo(), true);
                LocalDate mFrom = RunReport.relativeDateToSpecificLocalDate(report.getMonthFrom(), false);
                if (mTo != null && mFrom.isAfter(mTo)) {
                    throw new JocConfigurationException("$.monthFrom has to be older than $.monthTo");
                }
            } catch (IllegalArgumentException e) {
                throw new JocConfigurationException("$." + e.getMessage());
            }
        }
    }

    private static void validateJobResourceRefs(List<String> jobResources, InventoryDBLayer dbLayer) throws SOSHibernateException {
        if (jobResources != null && !jobResources.isEmpty()) {
            List<DBItemInventoryConfiguration> dbJobResources = dbLayer.getConfigurationByNames(jobResources, ConfigurationType.JOBRESOURCE
                    .intValue());
            if (dbJobResources == null || dbJobResources.isEmpty()) {
                throw new JocConfigurationException("Missing assigned JobResources: " + jobResources.toString());
            } else {
                Set<String> jobResourcesCopy = new HashSet<>(jobResources);
                jobResourcesCopy.removeAll(dbJobResources.stream().map(DBItemInventoryConfiguration::getName).collect(Collectors.toSet()));
                if (!jobResourcesCopy.isEmpty()) {
                    throw new JocConfigurationException("Missing assigned JobResources: " + jobResourcesCopy.toString());
                }
            }
        }
    }
    
    private static void validateJobResourceRefs(List<String> jobResources, Set<String> allJobResourceNames) throws JocConfigurationException {
        if (jobResources != null && !jobResources.isEmpty()) {
            Set<String> jobResourcesCopy = new HashSet<>(jobResources);
            jobResourcesCopy.removeAll(allJobResourceNames);
            if (!jobResourcesCopy.isEmpty()) {
                throw new JocConfigurationException("Missing assigned JobResources: " + jobResourcesCopy.toString());
            }
        }
    }

    private static String validateWorkflowRef(String workflowName, InventoryDBLayer dbLayer, String position) throws JocConfigurationException,
            SOSHibernateException {
        List<DBItemInventoryConfiguration> workflows = dbLayer.getConfigurationByName(workflowName, ConfigurationType.WORKFLOW.intValue());
        if (workflows == null || !workflows.stream().anyMatch(w -> workflowName.equals(w.getName()))) {
            throw new JocConfigurationException(position + ": Missing assigned Workflow: " + workflowName);
        }
        return workflows.get(0).getContent();
    }
    
    private static String validateWorkflowRef(String workflowName, Map<String, String> workflowJsons, String position)
            throws JocConfigurationException {
        String wJson = workflowJsons.get(workflowName);
        if (wJson == null) {
            throw new JocConfigurationException(position + ": Missing assigned Workflow: " + workflowName);
        }
        return wJson;
    }
    
    private static void validateFileVariable(Requirements r, String workflowName, String position) throws JocConfigurationException {
        if (r == null || r.getParameters() == null || r.getParameters().getAdditionalProperties() == null || !r.getParameters()
                .getAdditionalProperties().containsKey("file")) {
            throw new JocConfigurationException(String.format("%s: the 'file' variable is missing in the referenced workflow '%s'", position,
                    workflowName));
        }
    }
    
    public static void validateFileOrderSourceDirAndPattern(FileOrderSource fileOrderSource) throws JocConfigurationException {
        if (fileOrderSource.getDirectoryExpr() != null) {
            validateExpression("$.directoryExpr: ", fileOrderSource.getDirectoryExpr());
        }
        if (fileOrderSource.getPattern() != null) {
            try {
                Pattern.compile(fileOrderSource.getPattern());
            } catch (PatternSyntaxException e) {
                throw new JocConfigurationException("$.pattern: " + e.getMessage());
            }
        }
        testJavaNameRulesAtTags("$.tags", fileOrderSource.getTags());
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
    
    private static void validateCalendarRefs(Schedule schedule, Set<String> allWorkingDaysCalendarNames, Set<String> allNonWorkingDaysCalendarNames) throws JocConfigurationException {
        List<String> calendarNames = schedule.getCalendars().stream().map(AssignedCalendars::getCalendarName).distinct().collect(Collectors.toList());
        if (schedule.getNonWorkingDayCalendars() != null && !schedule.getNonWorkingDayCalendars().isEmpty()) {
            calendarNames.addAll(schedule.getNonWorkingDayCalendars().stream().map(AssignedNonWorkingDayCalendars::getCalendarName).collect(Collectors
                    .toSet()));
        }
        calendarNames.removeAll(allWorkingDaysCalendarNames);
        calendarNames.removeAll(allNonWorkingDaysCalendarNames);
        if (!calendarNames.isEmpty()) {
            throw new JocConfigurationException("Missing assigned Calendars: " + calendarNames.toString());
        }
    }

//    private static void validateAgentRefs(String json, InventoryAgentInstancesDBLayer dbLayer, Set<String> visibleAgentNames)
//            throws SOSHibernateException, JocConfigurationException {
//        Matcher m = Pattern.compile("\"agentName\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
//        Set<String> agents = new HashSet<>();
//        while (m.find()) {
//            if (m.group(1) != null && !m.group(1).isEmpty()) {
//                agents.add(m.group(1));
//            }
//        }
//        if (!agents.isEmpty()) {
//            if (visibleAgentNames != null) {
//                visibleAgentNames.forEach(a -> agents.remove(a));
//            } else {
//                dbLayer.getVisibleAgentNames().forEach(a -> agents.remove(a));
//            }
//            if (!agents.isEmpty()) {
//                throw new JocConfigurationException("Missing assigned Agents: " + agents.toString());
//            }
//        }
//    }
    
    private static Set<String> getInvalidAgentRefs(String json, InventoryAgentInstancesDBLayer dbLayer, Set<String> visibleAgentNames) {
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
//            if (!agents.isEmpty()) {
//                throw new JocConfigurationException("Missing assigned Agents: " + agents.toString());
//            }
        }
        return agents;
    }
    
    // return map agentName -> List of jobnames
//    private static Map<String, Set<String>> getInvalidAgentRefs(Jobs jobs, InventoryAgentInstancesDBLayer dbLayer, Set<String> visibleAgentNames)
//            throws SOSHibernateException, JocConfigurationException {
//        
//        Map<String, Set<String>> agentsMap = Collections.emptyMap();
//        if (jobs != null) {
//            agentsMap = jobs.getAdditionalProperties().entrySet().stream().collect(Collectors.groupingBy(e -> e.getValue().getAgentName(), Collectors
//                    .mapping(e -> e.getKey(), Collectors.toSet())));
//        }
//        if (!agentsMap.isEmpty()) {
//            if (visibleAgentNames != null) {
//                for(String visibleAgentName : visibleAgentNames) {
//                    agentsMap.remove(visibleAgentName);
//                }
//            } else {
//                for(String visibleAgentName : dbLayer.getVisibleAgentNames()) {
//                    agentsMap.remove(visibleAgentName);
//                }
//            }
//        }
//        return agentsMap;
//    }

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
    
    private static void validateLockRefs(String json, Set<String> allLockNames) throws JocConfigurationException {
        Matcher m = Pattern.compile("\"lockName\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        Set<String> locks = new HashSet<>();
        while (m.find()) {
            if (m.group(1) != null && !m.group(1).isEmpty()) {
                locks.add(m.group(1));
            }
        }
        if (!locks.isEmpty()) {
            locks.removeAll(allLockNames);
            if (!locks.isEmpty()) {
                throw new JocConfigurationException("Missing assigned Locks: " + locks.toString());
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
    
    private static void validateJobTemplateJob(JobTemplate jobTemplate, Set<String> releasedScripts) throws JsonProcessingException, IOException,
            SOSJsonSchemaException {

        switch (jobTemplate.getExecutable().getTYPE()) {
        case InternalExecutable:
            ExecutableJava ej = jobTemplate.getExecutable().cast();
            if (ej.getArguments() != null) {
                validateExpression("$.executable.arguments", ej.getArguments().getAdditionalProperties());
            }
            checkScriptIncludes(ej.getScript(), "$.executable.script", releasedScripts);
            break;
        case ScriptExecutable:
        case ShellScriptExecutable:
            ExecutableScript es = jobTemplate.getExecutable().cast();
            if (es.getEnv() != null) {
                validateExpression("$.executable.env", es.getEnv().getAdditionalProperties());
            }
            validateEnvironmentKeys(es.getEnv(), "$.executable.env");
            checkReturnCodeMeaning(es, "$.");
            checkScriptIncludes(es.getScript(), "$.executable.script", releasedScripts);
            break;
        }
        validateJobNotification(jobTemplate);
        validateOnErrWritten(jobTemplate.getFailOnErrWritten(), jobTemplate.getWarnOnErrWritten());
    }
    
    private static List<String> validateWorkflowJobs(Workflow workflow, Set<String> releasedScripts) throws JsonProcessingException, IOException,
            SOSJsonSchemaException {
        List<String> jobResources = new ArrayList<>();
        if (workflow.getJobs() == null) {
            return jobResources;
        }
        boolean allowEmptyArguments = ClusterSettings.getAllowEmptyArguments(Globals.getConfigurationGlobalsJoc());
        for (Map.Entry<String, Job> entry : workflow.getJobs().getAdditionalProperties().entrySet()) {
            // TODO check JobResources references in Job
            Job job = entry.getValue();
            try {
                JsonValidator.validate(Globals.objectMapper.writeValueAsBytes(job), URI.create(
                        "classpath:/raml/inventory/schemas/job/job-schema.json"), true);
                if (job.getJobResourceNames() != null) {
                    jobResources.addAll(job.getJobResourceNames());
                }
            } catch (SOSJsonSchemaException e) {
                String msg = e.getMessage().replaceAll("(\\$\\.)", "$1jobs['" + entry.getKey() + "'].");
                throw new SOSJsonSchemaException(msg);
            }
            validateExpression("$.jobs['" + entry.getKey() + "'].subagentClusterIdExpr: ", entry.getValue().getSubagentClusterIdExpr());
            switch (job.getExecutable().getTYPE()) {
            case InternalExecutable:
                ExecutableJava ej = job.getExecutable().cast();
                if (ej.getInternalType() != null && (InternalExecutableType.JavaScript_Graal.equals(ej.getInternalType())
                        || InternalExecutableType.JavaScript_Node.equals(ej.getInternalType()))) {
                    // script is required
                    if (ej.getScript() == null || ej.getScript().isEmpty()) {
                        throw new JocConfigurationException("$.jobs['" + entry.getKey() + "'].executable.script is required but missing");
                    }
                    checkScriptIncludes(ej.getScript(), "$.jobs['" + entry.getKey() + "'].executable.script", releasedScripts);
                } else {
                    // classname is required
                    if (ej.getClassName() == null || ej.getClassName().isEmpty()) {
                        throw new JocConfigurationException("$.jobs['" + entry.getKey() + "'].executable.className is required but missing");
                    }
                }
                if (ej.getArguments() != null) {
                    validateEmptyExpressions(ej.getArguments(), "$.jobs['" + entry.getKey() + "'].executable.arguments", allowEmptyArguments);
                    validateExpression("$.jobs['" + entry.getKey() + "'].executable.arguments", ej.getArguments().getAdditionalProperties());
                }
                break;
            case ScriptExecutable:
            case ShellScriptExecutable:
                ExecutableScript es = job.getExecutable().cast();
                if (es.getEnv() != null) {
                    validateEmptyExpressions(es.getEnv(), "$.jobs['" + entry.getKey() + "'].executable.env", allowEmptyArguments);
                    validateExpression("$.jobs['" + entry.getKey() + "'].executable.env", es.getEnv().getAdditionalProperties());
                }
                validateEnvironmentKeys(es.getEnv(), "$.jobs['" + entry.getKey() + "'].executable.env");
                checkReturnCodeMeaning(es, "$.jobs['" + entry.getKey() + "']");
                checkScriptIncludes(es.getScript(), "$.jobs['" + entry.getKey() + "'].executable.script", releasedScripts);
                break;
            }
            validateJobNotification(entry.getKey(), job);
            validateOnErrWritten(job.getFailOnErrWritten(), job.getWarnOnErrWritten());
        }
        return jobResources;
    }
    
    private static void checkScriptIncludes(String script, String position, Set<String> releasedScripts) {
        if (script != null) {
            Matcher m = scriptIncludePattern.matcher(script);
            while (m.find()) {
                String scriptName = m.group(2);
                if (!releasedScripts.contains(scriptName)) {
                    throw new JocConfigurationException(position + " referenced an unknown script '" + scriptName + "'");
                }
                try {
                    JsonConverter.parseReplaceInclude(m.group(3)); // m.group(3) = "--replace="","" ...
                } catch (Exception e) {
                    throw new JocConfigurationException(position + ": Invalid script include '" + m.group(0)
                            + "'. Replace arguments must have the form: --replace=\"...\",\"...\"");
                }
            }
            m = scriptIncludeWithoutScriptPattern.matcher(script);
            while (m.find()) {
                throw new JocConfigurationException(position + " contains script include without script name");
            }
        }
    }
    
    private static void checkReturnCodeMeaning(ExecutableScript es, String position) throws JsonProcessingException, IOException, SOSJsonSchemaException {
        if (es.getReturnCodeMeaning() != null) {
            if (es.getReturnCodeMeaning().getSuccess() != null && es.getReturnCodeMeaning().getFailure() != null) {
                throw new JocConfigurationException(position + ".executable.returnCodeMeaning: only one of 'success' or 'failure' may be specified.");
            }
            if (es.getReturnCodeMeaning().getWarning() != null) {
                try {
                    JsonValidator.validate(Globals.objectMapper.writeValueAsBytes(es.getReturnCodeMeaning()), URI.create(
                            "classpath:/raml/inventory/schemas/job/jobReturnCodeWarning-schema.json"), true);
                } catch (SOSJsonSchemaException e) {
                    throw new SOSJsonSchemaException(position + e.getMessage().substring(1));
                }
            }
        }
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
    
    private static void validateJobNotification(JobTemplate jobTemplate) throws JocConfigurationException {
        if (jobTemplate == null || jobTemplate.getNotification() == null || jobTemplate.getNotification().getMail() == null) {
            return;
        }
        if (jobTemplate.getNotification().getTypes() != null && jobTemplate.getNotification().getTypes().size() > 0) {
            // to is required, when cc or bcc defined and the mail is not suppressed
            if (SOSString.isEmpty(jobTemplate.getNotification().getMail().getTo())) {
                if (!SOSString.isEmpty(jobTemplate.getNotification().getMail().getCc()) || !SOSString.isEmpty(jobTemplate.getNotification().getMail()
                        .getBcc())) {
                    throw new JocConfigurationException("$.notification.mail: missing \"to\"");
                }
            }
        }
        try {
            String content = Globals.objectMapper.writeValueAsString(jobTemplate.getNotification());
            if (content.length() > HistoryConstants.MAX_LEN_NOTIFICATION) {
                throw new JocConfigurationException(String.format(
                        "$.notification: reduce recipients, max json length %s (current length=%s) exceeded", HistoryConstants.MAX_LEN_NOTIFICATION,
                        content.length()));
            }

        } catch (JsonProcessingException e) {
            //
        }
    }
    
    private static void validateOnErrWritten(Boolean failOnErrWritten, Boolean warnOnErrWritten) throws JocConfigurationException {
        if (failOnErrWritten == Boolean.TRUE && warnOnErrWritten == Boolean.TRUE) {
            throw new JocConfigurationException("Only one of 'failOnErrWritten' or 'warnOnErrWritten' may be activated");
        }
    }

    private static void validateInstructions(Collection<Instruction> instructions, String position, Jobs jobs, Requirements orderPreparation,
            Map<String, String> labels, Set<String> invalidAgentRefs, Collection<String> boardNames, boolean forkListExist, InventoryDBLayer dbLayer)
            throws SOSJsonSchemaException, JsonProcessingException, IOException, JocConfigurationException, SOSHibernateException {
        if (instructions != null) {
            int index = 0;
            for (Instruction inst : instructions) {
                String instPosition = position + "[" + index + "].";
                if (inst.getLabel() != null) {
                    if (labels.containsKey(inst.getLabel())) {
                        throw new SOSJsonSchemaException("$." + instPosition + "label: duplicate label '" + inst.getLabel() + "' with " + labels.get(
                                inst.getLabel()));
                    } else {
                        labels.put(inst.getLabel(), "$." + instPosition + "label");
                    }
                }
                boolean unLicensedForkList = inst.getTYPE().equals(InstructionType.FORKLIST) && !hasLicense;
                try {
                    if (unLicensedForkList) {
                        JsonValidator.validate(Globals.objectMapper.writeValueAsBytes(inst), URI.create(
                                JocInventory.FORKLIST_SCHEMA_WITHOUT_LICENSE), true);
                    } else {
                        JsonValidator.validate(Globals.objectMapper.writeValueAsBytes(inst), URI.create(JocInventory.INSTRUCTION_SCHEMA_LOCATION.get(
                                inst.getTYPE())), true);
                    }
                } catch (SOSJsonSchemaException e) {
                    String msg = e.getMessage();
                    //improve message: [$.children: is missing but it is required, $.childToId: is missing but it is required, $.subagentClusterId: is missing but it is required, $.subagentClusterIdExpr: is missing but it is required]
                    if (inst.getTYPE().equals(InstructionType.FORKLIST) && hasLicense && msg.contains("$.children")) {
                        msg = "[($.children and $.childToId) or ($.agentName and $.subagentClusterId) or ($.agentName and $.subagentClusterIdExpr) are missing but required]";
                    }
                    throw new SOSJsonSchemaException(msg.replaceAll("(\\$\\.)", "$1" + instPosition));
                }
                switch (inst.getTYPE()) {
                case EXECUTE_NAMED:
                    NamedJob nj = inst.cast();
                    testJavaNameRules("$." + instPosition, "jobName", nj.getJobName());
                    Job j = jobs.getAdditionalProperties().get(nj.getJobName());
                    if (j == null) {
                        throw new SOSJsonSchemaException("$." + instPosition + "jobName: job '" + nj.getJobName()
                                + "' doesn't exist. Found jobs are: " + jobs.getAdditionalProperties().keySet().toString());
                    }
                    if (invalidAgentRefs.contains(j.getAgentName())) {
                        throw new JocConfigurationException("$." + instPosition + "agentName: Missing assigned Agent: " + j.getAgentName());
                    }
                    testJavaNameRules("$." + instPosition, "label", nj.getLabel());
//                    if (labels.containsKey(nj.getLabel())) {
//                        throw new SOSJsonSchemaException("$." + instPosition + "label: duplicate label '" + nj.getLabel() + "' with " + labels.get(nj
//                                .getLabel()));
//                    } else {
//                        labels.put(nj.getLabel(), "$." + instPosition + "label");
//                    }
                    // validateArguments(nj.getDefaultArguments(), orderPreparation, "$." + instPosition + "defaultArguments");
                    // validateArgumentKeys(nj.getDefaultArguments(), "$." + instPosition + "defaultArguments");
                    validateEmptyExpressions(nj.getDefaultArguments(), "$." + instPosition + "defaultArguments", ClusterSettings
                            .getAllowEmptyArguments(Globals.getConfigurationGlobalsJoc()));
                    break;
                case FORK:
                    ForkJoin fj = inst.cast();
                    int branchIndex = 0;
                    String branchPosition = instPosition + "branches";
                    
                    Map<String, String> resultKeys = new HashMap<>();
                    List<String> branchIds = new ArrayList<>();
                    
                    for (Branch branch : fj.getBranches()) {
                        String branchId = branch.getId();
                        if (branchId != null) {
                            int otherBranchIndex = branchIds.indexOf(branchId);
                            if (otherBranchIndex > -1) {
                                throw new JocConfigurationException("$." + branchPosition + "[" + branchIndex + "].id: duplicate branch id with $."
                                        + branchPosition + "[" + otherBranchIndex + "].id");
                            } else {
                                branchIds.add(branchId);
                            }
                        }
                        BranchWorkflow bw = branch.getWorkflow();
                        if (bw != null) {
                            if (bw.getResult() != null && bw.getResult().getAdditionalProperties() != null) {
                                String branchInstPosition = branchPosition + "[" + branchIndex + "].workflow";
                                for (Map.Entry<String, String> entry : bw.getResult().getAdditionalProperties().entrySet()) {
                                    validateExpression("$." + branchInstPosition + ".result", entry.getKey(), entry.getValue());
                                    if (entry.getKey() != null) {
                                        if (resultKeys.containsKey(entry.getKey())) {
                                            throw new JocConfigurationException("$." + branchInstPosition + ".result: duplicate key '" + entry
                                                    .getKey() + "': already used in " + resultKeys.get(entry.getKey()));
                                        } else {
                                            resultKeys.put(entry.getKey(), branchInstPosition);
                                        }
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
                            validateInstructions(branch.getWorkflow().getInstructions(), branchInstPosition + "instructions", jobs,
                                    orderPreparation, labels, invalidAgentRefs, boardNames, forkListExist, dbLayer);
                        }
                        branchIndex++;
                    }
                    break;
                case FORKLIST:
                    if (forkListExist) {
                        throw new JocConfigurationException("$." + instPosition + "ForkList instructions can not be nested.");
                    }
                    ForkList fl = inst.cast();
                    if (!unLicensedForkList && fl.getAgentName() != null) {
                        testJavaNameRules("$." + instPosition, "subagentIdVariable", fl.getSubagentIdVariable());
                        validateExpression("$." + instPosition + "subagentClusterIdExpr: ", fl.getSubagentClusterIdExpr());
                        if (invalidAgentRefs.contains(fl.getAgentName())) {
                            throw new JocConfigurationException("$." + instPosition + "agentName: Missing assigned Agent: " + fl.getAgentName());
                        }
                    }
                    if (fl.getWorkflow() != null) {
                        firstChildIsForkInstruction(fl.getWorkflow().getInstructions(), instPosition + "workflow.instructions", "ForkList");
                        validateInstructions(fl.getWorkflow().getInstructions(), instPosition + "workflow.instructions", jobs, orderPreparation,
                                labels, invalidAgentRefs, boardNames, true, dbLayer);
                    }
                    break;
                case IF:
                    IfElse ifElse = inst.cast();
                    validateExpression("$." + instPosition + "predicate: ", ifElse.getPredicate());
                    if (ifElse.getThen() != null) {
                        validateInstructions(ifElse.getThen().getInstructions(), instPosition + "then.instructions", jobs, orderPreparation,
                                labels, invalidAgentRefs, boardNames, forkListExist, dbLayer);
                    }
                    if (ifElse.getElse() != null) {
                        validateInstructions(ifElse.getElse().getInstructions(), instPosition + "else.instructions", jobs, orderPreparation,
                                labels, invalidAgentRefs, boardNames, forkListExist, dbLayer);
                    }
                    break;
                case TRY:
                    TryCatch tryCatch = inst.cast();
                    if (tryCatch.getTry() != null) {
                        validateInstructions(tryCatch.getTry().getInstructions(), instPosition + "try.instructions", jobs, orderPreparation, labels,
                                invalidAgentRefs, boardNames, forkListExist, dbLayer);
                    }
                    if (tryCatch.getCatch() != null) {
                        validateInstructions(tryCatch.getCatch().getInstructions(), instPosition + "catch.instructions", jobs, orderPreparation,
                                labels, invalidAgentRefs, boardNames, forkListExist, dbLayer);
                    }
                    break;
                case LOCK:
                    Lock lock = inst.cast();
                    if (lock.getLockedWorkflow() != null) {
                    validateInstructions(lock.getLockedWorkflow().getInstructions(), instPosition + "lockedWorkflow.instructions", jobs,
                            orderPreparation, labels, invalidAgentRefs, boardNames, forkListExist, dbLayer);
                    }
                    break;
                case PROMPT:
                    Prompt prompt = inst.cast();
                    validateExpression("$." + instPosition + "question:", prompt.getQuestion());
                    break;
                case ADD_ORDER:
                    AddOrder ao = inst.cast();
                    String json = validateWorkflowRef(ao.getWorkflowName(), dbLayer, "$." + instPosition + "workflowName");
                    if (json != null) {
                        Workflow workflowOfAddOrder = Globals.objectMapper.readValue(json, Workflow.class);
                        try {
                            OrdersHelper.checkArgumentsWithAllowedDollarInValues(ao.getArguments(), workflowOfAddOrder.getOrderPreparation());
                        } catch (Exception e) {
                            throw new JocConfigurationException("$." + instPosition + "arguments: " + e.getMessage());
                        }
                        
                        Predicate<OrderPositions> hasPositionSetting = p -> p.getStartPosition() != null || (p.getEndPositions() != null && !p
                                .getEndPositions().isEmpty()) || p.getBlockPosition() != null;
                        OrderPositions op = new OrderPositions();
                        op.setBlockPosition(ao.getBlockPosition());
                        op.setStartPosition(ao.getStartPosition());
                        op.setEndPositions(ao.getEndPositions());
                        if (hasPositionSetting.test(op)) {
                            Set<BlockPosition> availableBlockPositions = WorkflowsHelper.getWorkflowBlockPositions(workflowOfAddOrder.getInstructions());
                            Map<String, List<Object>> labelMap = WorkflowsHelper.getLabelToPositionsMap(workflowOfAddOrder, true);
                            checkAddOrderPositions(op, availableBlockPositions, ao.getWorkflowName(), labelMap, "$." + instPosition);
                        }
                    }
                    testJavaNameRulesAtTags("$." + instPosition + "tags", ao.getTags());
                    break;
                case CONSUME_NOTICES:
                    ConsumeNotices cns = inst.cast();
                    String cnsNamesExpr = cns.getNoticeBoardNames();
                    String cnsNamesExpr2 = cnsNamesExpr.replaceAll("'[^']*'", "true").replaceAll("\"[^\"]*\"", "true");
                    Either<Problem, JExpression> cnsE = JExpression.parse(cnsNamesExpr2);
                    if (cnsE.isLeft()) {
                        throw new JocConfigurationException("$." + instPosition + "noticeBoardNames: " + cnsE.getLeft().message().replace("true", "'...'"));
                    }
                    List<String> cnsNames = NoticeToNoticesConverter.expectNoticeBoardsToList(cnsNamesExpr);
                    cnsNames.removeAll(boardNames);
                    if (boardNames.isEmpty() || !cnsNames.isEmpty()) {
                        throw new JocConfigurationException("$." + instPosition + "noticeBoardNames: Missing assigned Notice Boards: " + cnsNames
                                .toString());
                    }
                    if (cns.getSubworkflow() != null && cns.getSubworkflow().getInstructions() != null) {
                        validateInstructions(cns.getSubworkflow().getInstructions(), instPosition + "subworkflow.instructions", jobs,
                                orderPreparation, labels, invalidAgentRefs, boardNames, forkListExist, dbLayer);
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
                    List<String> pnsNames = new ArrayList<>(pns.getNoticeBoardNames());
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
                    Either<Problem, JExpression> enE = JExpression.parse(ensNamesExpr2);
                    if (enE.isLeft()) {
                        throw new JocConfigurationException("$." + instPosition + "noticeBoardNames: " + enE.getLeft().message().replace("true", "'...'"));
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
                    if (cycle.getCycleWorkflow() != null) {
                        validateInstructions(cycle.getCycleWorkflow().getInstructions(), instPosition + "cycleWorkflow.instructions", jobs,
                                orderPreparation, labels, invalidAgentRefs, boardNames, forkListExist, dbLayer);
                    }
                    break;
                case STICKY_SUBAGENT:
                    if (!hasLicense) {
                        throw new JocConfigurationException("$." + instPosition + "StickySubagent instruction needs license");
                    }
                    StickySubagent sticky = inst.cast();
                    validateExpression("$." + instPosition + "subagentClusterIdExpr: ", sticky.getSubagentClusterIdExpr());
                    if (invalidAgentRefs.contains(sticky.getAgentName())) {
                        throw new JocConfigurationException("$." + instPosition + "agentName: Missing assigned Agent: " + sticky.getAgentName());
                    }
                    if (sticky.getSubworkflow() != null) {
                        firstChildIsForkInstruction(sticky.getSubworkflow().getInstructions(), instPosition + "subworkflow.instructions", "StickySubagent");
                        validateInstructions(sticky.getSubworkflow().getInstructions(), instPosition + "subworkflow.instructions", jobs,
                                orderPreparation, labels, invalidAgentRefs, boardNames, forkListExist, dbLayer);
                    }
                    break;
                case OPTIONS:
                    Options opts = inst.cast();
                    if (opts.getBlock() != null) {
                        validateInstructions(opts.getBlock().getInstructions(), instPosition + "block.instructions", jobs, orderPreparation, labels,
                                invalidAgentRefs, boardNames, forkListExist, dbLayer);
                    }
                    break;
                default:
                    break;
                }
                index++;
            }
        }
    }
    
    private static void validateInstructions(Collection<Instruction> instructions, String position, Jobs jobs, Requirements orderPreparation,
            Map<String, String> labels, Set<String> invalidAgentRefs, Collection<String> boardNames, boolean forkListExist,
            Map<String, String> allWorkflowJsonsByName) throws SOSJsonSchemaException, JsonProcessingException, IOException,
            JocConfigurationException {
        if (instructions != null) {
            int index = 0;
            for (Instruction inst : instructions) {
                String instPosition = position + "[" + index + "].";
                if (inst.getLabel() != null) {
                    if (labels.containsKey(inst.getLabel())) {
                        throw new SOSJsonSchemaException("$." + instPosition + "label: duplicate label '" + inst.getLabel() + "' with " + labels.get(
                                inst.getLabel()));
                    } else {
                        labels.put(inst.getLabel(), "$." + instPosition + "label");
                    }
                }
                boolean unLicensedForkList = inst.getTYPE().equals(InstructionType.FORKLIST) && !hasLicense;
                try {
                    if (unLicensedForkList) {
                        JsonValidator.validate(Globals.objectMapper.writeValueAsBytes(inst), URI.create(
                                JocInventory.FORKLIST_SCHEMA_WITHOUT_LICENSE), true);
                    } else {
                        JsonValidator.validate(Globals.objectMapper.writeValueAsBytes(inst), URI.create(JocInventory.INSTRUCTION_SCHEMA_LOCATION.get(
                                inst.getTYPE())), true);
                    }
                } catch (SOSJsonSchemaException e) {
                    String msg = e.getMessage();
                    //improve message: [$.children: is missing but it is required, $.childToId: is missing but it is required, $.subagentClusterId: is missing but it is required, $.subagentClusterIdExpr: is missing but it is required]
                    if (inst.getTYPE().equals(InstructionType.FORKLIST) && hasLicense && msg.contains("$.children")) {
                        msg = "[($.children and $.childToId) or ($.agentName and $.subagentClusterId) or ($.agentName and $.subagentClusterIdExpr) are missing but required]";
                    }
                    throw new SOSJsonSchemaException(msg.replaceAll("(\\$\\.)", "$1" + instPosition));
                }
                switch (inst.getTYPE()) {
                case EXECUTE_NAMED:
                    NamedJob nj = inst.cast();
                    testJavaNameRules("$." + instPosition, "jobName", nj.getJobName());
                    Job j = jobs.getAdditionalProperties().get(nj.getJobName());
                    if (j == null) {
                        throw new SOSJsonSchemaException("$." + instPosition + "jobName: job '" + nj.getJobName()
                                + "' doesn't exist. Found jobs are: " + jobs.getAdditionalProperties().keySet().toString());
                    }
                    if (invalidAgentRefs.contains(j.getAgentName())) {
                        throw new JocConfigurationException("$." + instPosition + "agentName: Missing assigned Agent: " + j.getAgentName());
                    }
                    testJavaNameRules("$." + instPosition, "label", nj.getLabel());
//                    if (labels.containsKey(nj.getLabel())) {
//                        throw new SOSJsonSchemaException("$." + instPosition + "label: duplicate label '" + nj.getLabel() + "' with " + labels.get(nj
//                                .getLabel()));
//                    } else {
//                        labels.put(nj.getLabel(), "$." + instPosition + "label");
//                    }
                    // validateArguments(nj.getDefaultArguments(), orderPreparation, "$." + instPosition + "defaultArguments");
                    // validateArgumentKeys(nj.getDefaultArguments(), "$." + instPosition + "defaultArguments");
                    validateEmptyExpressions(nj.getDefaultArguments(), "$." + instPosition + "defaultArguments", ClusterSettings
                            .getAllowEmptyArguments(Globals.getConfigurationGlobalsJoc()));
                    break;
                case FORK:
                    ForkJoin fj = inst.cast();
                    int branchIndex = 0;
                    String branchPosition = instPosition + "branches";
                    
                    Map<String, String> resultKeys = new HashMap<>();
                    List<String> branchIds = new ArrayList<>();
                    
                    for (Branch branch : fj.getBranches()) {
                        String branchId = branch.getId();
                        if (branchId != null) {
                            int otherBranchIndex = branchIds.indexOf(branchId);
                            if (otherBranchIndex > -1) {
                                throw new JocConfigurationException("$." + branchPosition + "[" + branchIndex + "].id: duplicate branch id with $."
                                        + branchPosition + "[" + otherBranchIndex + "].id");
                            } else {
                                branchIds.add(branchId);
                            }
                        }
                        BranchWorkflow bw = branch.getWorkflow();
                        if (bw != null) {
                            if (bw.getResult() != null && bw.getResult().getAdditionalProperties() != null) {
                                String branchInstPosition = branchPosition + "[" + branchIndex + "].workflow";
                                for (Map.Entry<String, String> entry : bw.getResult().getAdditionalProperties().entrySet()) {
                                    validateExpression("$." + branchInstPosition + ".result", entry.getKey(), entry.getValue());
                                    if (entry.getKey() != null) {
                                        if (resultKeys.containsKey(entry.getKey())) {
                                            throw new JocConfigurationException("$." + branchInstPosition + ".result: duplicate key '" + entry
                                                    .getKey() + "': already used in " + resultKeys.get(entry.getKey()));
                                        } else {
                                            resultKeys.put(entry.getKey(), branchInstPosition);
                                        }
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
                            validateInstructions(branch.getWorkflow().getInstructions(), branchInstPosition + "instructions", jobs,
                                    orderPreparation, labels, invalidAgentRefs, boardNames, forkListExist, allWorkflowJsonsByName);
                        }
                        branchIndex++;
                    }
                    break;
                case FORKLIST:
                    if (forkListExist) {
                        throw new JocConfigurationException("$." + instPosition + "ForkList instructions can not be nested.");
                    }
                    ForkList fl = inst.cast();
                    if (!unLicensedForkList && fl.getAgentName() != null) {
                        testJavaNameRules("$." + instPosition, "subagentIdVariable", fl.getSubagentIdVariable());
                        validateExpression("$." + instPosition + "subagentClusterIdExpr: ", fl.getSubagentClusterIdExpr());
                        if (invalidAgentRefs.contains(fl.getAgentName())) {
                            throw new JocConfigurationException("$." + instPosition + "agentName: Missing assigned Agent: " + fl.getAgentName());
                        }
                    }
                    if (fl.getWorkflow() != null) {
                        firstChildIsForkInstruction(fl.getWorkflow().getInstructions(), instPosition + "workflow.instructions", "ForkList");
                        validateInstructions(fl.getWorkflow().getInstructions(), instPosition + "workflow.instructions", jobs, orderPreparation,
                                labels, invalidAgentRefs, boardNames, true, allWorkflowJsonsByName);
                    }
                    break;
                case IF:
                    IfElse ifElse = inst.cast();
                    validateExpression("$." + instPosition + "predicate: ", ifElse.getPredicate());
                    if (ifElse.getThen() != null) {
                        validateInstructions(ifElse.getThen().getInstructions(), instPosition + "then.instructions", jobs, orderPreparation,
                                labels, invalidAgentRefs, boardNames, forkListExist, allWorkflowJsonsByName);
                    }
                    if (ifElse.getElse() != null) {
                        validateInstructions(ifElse.getElse().getInstructions(), instPosition + "else.instructions", jobs, orderPreparation,
                                labels, invalidAgentRefs, boardNames, forkListExist, allWorkflowJsonsByName);
                    }
                    break;
                case TRY:
                    TryCatch tryCatch = inst.cast();
                    if (tryCatch.getTry() != null) {
                        validateInstructions(tryCatch.getTry().getInstructions(), instPosition + "try.instructions", jobs, orderPreparation, labels,
                                invalidAgentRefs, boardNames, forkListExist, allWorkflowJsonsByName);
                    }
                    if (tryCatch.getCatch() != null) {
                        validateInstructions(tryCatch.getCatch().getInstructions(), instPosition + "catch.instructions", jobs, orderPreparation,
                                labels, invalidAgentRefs, boardNames, forkListExist, allWorkflowJsonsByName);
                    }
                    break;
                case LOCK:
                    Lock lock = inst.cast();
                    if (lock.getLockedWorkflow() != null) {
                    validateInstructions(lock.getLockedWorkflow().getInstructions(), instPosition + "lockedWorkflow.instructions", jobs,
                            orderPreparation, labels, invalidAgentRefs, boardNames, forkListExist, allWorkflowJsonsByName);
                    }
                    break;
                case PROMPT:
                    Prompt prompt = inst.cast();
                    validateExpression("$." + instPosition + "question:", prompt.getQuestion());
                    break;
                case ADD_ORDER:
                    AddOrder ao = inst.cast();
                    String json = validateWorkflowRef(ao.getWorkflowName(), allWorkflowJsonsByName, "$." + instPosition + "workflowName");
                    if (json != null) {
                        Workflow workflowOfAddOrder = Globals.objectMapper.readValue(json, Workflow.class);
                        try {
                            OrdersHelper.checkArgumentsWithAllowedDollarInValues(ao.getArguments(), workflowOfAddOrder.getOrderPreparation());
                        } catch (Exception e) {
                            throw new JocConfigurationException("$." + instPosition + "arguments: " + e.getMessage());
                        }
                        
                        Predicate<OrderPositions> hasPositionSetting = p -> p.getStartPosition() != null || (p.getEndPositions() != null && !p
                                .getEndPositions().isEmpty()) || p.getBlockPosition() != null;
                        OrderPositions op = new OrderPositions();
                        op.setBlockPosition(ao.getBlockPosition());
                        op.setStartPosition(ao.getStartPosition());
                        op.setEndPositions(ao.getEndPositions());
                        if (hasPositionSetting.test(op)) {
                            Set<BlockPosition> availableBlockPositions = WorkflowsHelper.getWorkflowBlockPositions(workflowOfAddOrder.getInstructions());
                            Map<String, List<Object>> labelMap = WorkflowsHelper.getLabelToPositionsMap(workflowOfAddOrder, true);
                            checkAddOrderPositions(op, availableBlockPositions, ao.getWorkflowName(), labelMap, "$." + instPosition);
                        }
                    }
                    testJavaNameRulesAtTags("$." + instPosition + "tags", ao.getTags());
                    break;
                case CONSUME_NOTICES:
                    ConsumeNotices cns = inst.cast();
                    String cnsNamesExpr = cns.getNoticeBoardNames();
                    String cnsNamesExpr2 = cnsNamesExpr.replaceAll("'[^']*'", "true").replaceAll("\"[^\"]*\"", "true");
                    Either<Problem, JExpression> cnsE = JExpression.parse(cnsNamesExpr2);
                    if (cnsE.isLeft()) {
                        throw new JocConfigurationException("$." + instPosition + "noticeBoardNames: " + cnsE.getLeft().message().replace("true", "'...'"));
                    }
                    List<String> cnsNames = NoticeToNoticesConverter.expectNoticeBoardsToList(cnsNamesExpr);
                    cnsNames.removeAll(boardNames);
                    if (boardNames.isEmpty() || !cnsNames.isEmpty()) {
                        throw new JocConfigurationException("$." + instPosition + "noticeBoardNames: Missing assigned Notice Boards: " + cnsNames
                                .toString());
                    }
                    if (cns.getSubworkflow() != null && cns.getSubworkflow().getInstructions() != null) {
                        validateInstructions(cns.getSubworkflow().getInstructions(), instPosition + "subworkflow.instructions", jobs,
                                orderPreparation, labels, invalidAgentRefs, boardNames, forkListExist, allWorkflowJsonsByName);
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
                    List<String> pnsNames = new ArrayList<>(pns.getNoticeBoardNames());
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
                    Either<Problem, JExpression> enE = JExpression.parse(ensNamesExpr2);
                    if (enE.isLeft()) {
                        throw new JocConfigurationException("$." + instPosition + "noticeBoardNames: " + enE.getLeft().message().replace("true", "'...'"));
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
                    if (cycle.getCycleWorkflow() != null) {
                        validateInstructions(cycle.getCycleWorkflow().getInstructions(), instPosition + "cycleWorkflow.instructions", jobs,
                                orderPreparation, labels, invalidAgentRefs, boardNames, forkListExist, allWorkflowJsonsByName);
                    }
                    break;
                case STICKY_SUBAGENT:
                    if (!hasLicense) {
                        throw new JocConfigurationException("$." + instPosition + "StickySubagent instruction needs license");
                    }
                    StickySubagent sticky = inst.cast();
                    validateExpression("$." + instPosition + "subagentClusterIdExpr: ", sticky.getSubagentClusterIdExpr());
                    if (invalidAgentRefs.contains(sticky.getAgentName())) {
                        throw new JocConfigurationException("$." + instPosition + "agentName: Missing assigned Agent: " + sticky.getAgentName());
                    }
                    if (sticky.getSubworkflow() != null) {
                        firstChildIsForkInstruction(sticky.getSubworkflow().getInstructions(), instPosition + "subworkflow.instructions", "StickySubagent");
                        validateInstructions(sticky.getSubworkflow().getInstructions(), instPosition + "subworkflow.instructions", jobs,
                                orderPreparation, labels, invalidAgentRefs, boardNames, forkListExist, allWorkflowJsonsByName);
                    }
                    break;
                case OPTIONS:
                    Options opts = inst.cast();
                    if (opts.getBlock() != null) {
                        validateInstructions(opts.getBlock().getInstructions(), instPosition + "block.instructions", jobs, orderPreparation, labels,
                                invalidAgentRefs, boardNames, forkListExist, allWorkflowJsonsByName);
                    }
                    break;
                default:
                    break;
                }
                index++;
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void checkAddOrderPositions(Object pos, Map<String, List<Object>> labelPosMap, String position) {
        if (pos != null) {
            if (pos instanceof String && !labelPosMap.containsKey((String) pos)) {
                throw new JocConfigurationException(position + ": invalid label '" + (String) pos + "'");
            } else if (pos instanceof List<?> && !labelPosMap.containsKey(WorkflowsHelper.getJPositionString((List<Object>) pos))) {
                throw new JocConfigurationException(position + ": invalid position '" + ((List<Object>) pos).toString() + "'");
            }
        }
    }
    
    private static void checkAddOrderPositions(OrderPositions p, Set<BlockPosition> availableBlockPositions, String workflowName,
            Map<String, List<Object>> labelMap, String position) {
        if (p.getBlockPosition() != null) {
            try {
                BlockPosition blockPosition = OrdersHelper.getBlockPosition(p.getBlockPosition(), workflowName, availableBlockPositions);
                // check start-/endpositions inside block
                OrdersHelper.getStartPositionInBlock(p.getStartPosition(), labelMap, blockPosition);
                OrdersHelper.getEndPositionInBlock(p.getEndPositions(), labelMap, blockPosition);
            } catch (Exception e1) {
                throw new JocConfigurationException(position + "blockPosition:" + e1.getMessage());
            }
        } else if (p.getStartPosition() != null || (p.getEndPositions() != null && !p.getEndPositions().isEmpty())) {
            checkAddOrderPositions(p.getStartPosition(), labelMap, position + "startPosition");
            if (p.getEndPositions() != null) {
                p.getEndPositions().forEach(endP -> checkAddOrderPositions(endP, labelMap, position + "endPosition"));
            }
        }
    }
    
    //TODO this check is only temporary for 2.5.0 and can be deleted for 2.5.1 
    private static void firstChildIsForkInstruction(List<Instruction> insts, String pos, String parent) {
        if (insts != null && !insts.isEmpty()) {
            if (InstructionType.FORK.equals(insts.get(0).getTYPE())) {
                throw new JocConfigurationException(pos + ".[0]: A Fork instruction must not be the first instruction within a " + parent
                        + " instruction.");
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
                    //invalid = (_default instanceof List) == false;
                    // TODO check list params: see validateListParameters below
                    break;
                case Map:
                    //invalid = (_default instanceof Map) == false;
                    // TODO check list params: see validateListParameters below
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
            if (ParameterType.List.equals(value.getType()) || ParameterType.Map.equals(value.getType())) {
                validateListParameters(value.getType(), value.getListParameters(), key);
            }
        });
    }
    
    private static void validateListParameters(ParameterType type, ListParameters listParameters, String key) throws JocConfigurationException {
        if (listParameters == null || listParameters.getAdditionalProperties() == null || listParameters.getAdditionalProperties().isEmpty()) {
            throw new JocConfigurationException(String.format(
                    "$.orderPreparation.parameters['%s'].listParameters: missing but required if the parameter is of type '%s'", key, type.value()));
        }
        listParameters.getAdditionalProperties().forEach((listKey, listParam) -> {
            if (listParam.getDefault() != null) {
                Object _default = listParam.getDefault();
                boolean invalid = false;
                switch (listParam.getType()) {
                case String:
                    invalid = (_default instanceof String) == false;
                    break;
                case Boolean:
                    invalid = (_default instanceof Boolean) == false;
                    break;
                case Number:
                    invalid = (_default instanceof String) || (_default instanceof Boolean);
                    break;
                }
                if (invalid) {
                    throw new JocConfigurationException(String.format(
                            "$.orderPreparation.parameters['%s'].listParameters['%s'].default: Wrong data type %s (%s is expected).", key, listKey,
                            _default.getClass().getSimpleName(), listParam.getType().value()));
                }
            }
        });
    }

    private static void validateKey(String key, String position, String argName) {
        if (!checkKey.test(key)) {
            if (firstCharOfKeyIsNumber.test(key)) {
                throw new JocConfigurationException(String.format("%s['%s']: the %s name must not start with a number.", position, key, argName));
            }
            throw new JocConfigurationException(String.format("%s['%s']: only characters 'a-zA-Z0-9_' are allowed in the %s name.", position,
                    key, argName));
        }
    }

    @SuppressWarnings("unused")
    private static void validateArgumentKeys(Environment arguments, String position) throws JocConfigurationException {
        final Map<String, String> args = (arguments != null) ? arguments.getAdditionalProperties() : Collections.emptyMap();
        args.keySet().forEach(key -> validateKey(key, position, "argument"));
    }

    private static void validateEnvironmentKeys(Environment arguments, String position) throws JocConfigurationException {
        final Map<String, String> args = (arguments != null) ? arguments.getAdditionalProperties() : Collections.emptyMap();
        args.keySet().forEach(key -> validateKey(key, position, "variable"));
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
                    case Map:
                        //invalid = (curArg instanceof List) == false;
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
    
    private static void validateEmptyExpressions(Environment arguments, String position, boolean allowEmptyArguments) throws JocConfigurationException {
        if (!allowEmptyArguments) {
            final Map<String, String> args = (arguments != null) ? arguments.getAdditionalProperties() : Collections.emptyMap();
            Optional<JocConfigurationException> optException = args.entrySet().stream().filter(e -> e.getValue().isEmpty() || e.getValue()
                    .replaceFirst("^\"(.*)\"$", "$1").isEmpty() || e.getValue().replaceFirst("^'(.*)'$", "$1").isEmpty()).findAny().map(
                            e -> new JocConfigurationException(String.format("%s['%s']: disallowed empty value", position, e.getKey())));
            if (optException.isPresent()) {
                throw optException.get();
            }
        }
    }

    private static void validateOrderParameterisations(List<OrderParameterisation> variableSets, Requirements orderPreparation, String workflowName,
            Workflow workflow, String position) throws JocConfigurationException {
        boolean allowEmptyArguments = ClusterSettings.getAllowEmptyArguments(Globals.getConfigurationGlobalsJoc());
        if (variableSets != null) {
            if (variableSets.size() != variableSets.stream().map(OrderParameterisation::getOrderName).distinct().mapToInt(e -> 1).sum()) {
                throw new JocConfigurationException(position + ": Order names has to be unique");
            }
            variableSets.stream().map(OrderParameterisation::getVariables).forEach(v -> {
                if (v == null) {
                    v = new Variables();
                }
                try {
                    OrdersHelper.checkArguments(v, orderPreparation, allowEmptyArguments);
                } catch (Exception e1) {
                    throw new JocConfigurationException(position + ": " + e1.getMessage());
                }
            });
            Predicate<OrderPositions> hasPositionSetting = p -> p.getStartPosition() != null || (p.getEndPositions() != null && !p.getEndPositions()
                    .isEmpty()) || p.getBlockPosition() != null;
            if (variableSets.stream().map(OrderParameterisation::getPositions).filter(Objects::nonNull).anyMatch(hasPositionSetting)) {
                Set<BlockPosition> availableBlockPositions = WorkflowsHelper.getWorkflowBlockPositions(workflow.getInstructions());
                Map<String, List<Object>> labelMap = WorkflowsHelper.getLabelToPositionsMap(workflow, true);

                variableSets.stream().map(OrderParameterisation::getPositions).filter(Objects::nonNull).filter(hasPositionSetting).forEach(
                        p -> checkAddOrderPositions(p, availableBlockPositions, workflowName, labelMap, position + ".positions."));
            }
            testJavaNameRulesAtTags(position, variableSets.stream().map(OrderParameterisation::getTags).filter(Objects::nonNull).flatMap(Set::stream)
                    .collect(Collectors.toSet()));
        } else {
            try {
                OrdersHelper.checkArguments(new Variables(), orderPreparation, allowEmptyArguments);
            } catch (Exception e1) {
                throw new JocConfigurationException(position + ": " + e1.getMessage());
            }
        }
    }

//    private static void validateJobArguments(Jobs jobs, Requirements orderPreparation) {
//        if (jobs != null) {
//            jobs.getAdditionalProperties().forEach((key, value) -> {
//                // validateArguments(value.getDefaultArguments(), orderPreparation, "$.jobs['" + key + "'].defaultArguments");
//                // validateArgumentKeys(value.getDefaultArguments(), "$.jobs['" + key + "'].defaultArguments");
//            });
//        }
//    }
    
//    private static void validateJobTags(Jobs jobs) {
//        if (jobs != null && jobs.getAdditionalProperties() != null) {
//            jobs.getAdditionalProperties().entrySet().stream().filter(e -> e.getValue().getJobTags() != null).forEach(e -> testJavaNameRulesAtTags(
//                    "$.jobs[" + e.getKey() + "].jobTags", e.getValue().getJobTags()));
//        }
//    }

    private static void validateExpression(String prefix, Map<String, String> map) throws JocConfigurationException {
        if (map != null) {
            map.forEach((k, v) -> validateExpression(prefix, k, v));
        }
    }

    private static void validateExpression(String prefix, String key, String value) throws JocConfigurationException {
        if (value != null) {
            Either<Problem, JExpression> e = JExpression.parse(value);
            if (e.isLeft()) {
                throw new JocConfigurationException(prefix + "[" + key + "]: " + e.getLeft().message());
            } else {
                try {
                    StringSizeSanitizer.test(null, value);
                } catch (IllegalArgumentException e1) {
                    throw new JocConfigurationException(prefix + "[" + key + "]: " + e1.getMessage());
                }
            }
        }
    }

    public static void validateExpression(String prefix, String value) throws JocConfigurationException {
        if (value != null) {
            Either<Problem, JExpression> e = JExpression.parse(value);
            if (e.isLeft()) {
                throw new JocConfigurationException(prefix + e.getLeft().message());
            } else {
                try {
                    StringSizeSanitizer.test(null, value);
                } catch (IllegalArgumentException e1) {
                    throw new JocConfigurationException(prefix + e1.getMessage());
                }
            }
        }
    }

    public static void validateExpression(String value) throws JocConfigurationException {
        if (value != null) {
            Either<Problem, JExpression> e = JExpression.parse(value);
            if (e.isLeft()) {
                throw new JocConfigurationException(e.getLeft().message());
            } else {
                try {
                    StringSizeSanitizer.test(null, value);
                } catch (IllegalArgumentException e1) {
                    throw new JocConfigurationException(e1.getMessage());
                }
            }
        }
    }
    
    private static void testJavaNameRules(String prefix, String key, String value) throws JocConfigurationException {
        String errorMessage = SOSCheckJavaVariableName.check(value);
        if (errorMessage != null) {
            throw new JocConfigurationException(prefix + key + ": " + String.format(errorMessage, key, value));
        }
    }
    
    public static void testJavaNameRulesAtTags(String prefix, Collection<String> tags) {
        if (tags != null) {
            testJavaNameRulesAtTags(prefix, tags.stream());
        }
    }
    
    private static void testJavaNameRulesAtTags(String prefix, Stream<String> tags) {
            Optional<String> duplicateTagname = tags.map(tag -> testJavaNameRulesAtTag(prefix, tag)).collect(Collectors.groupingBy(Function
                    .identity(), Collectors.counting())).entrySet().stream().filter(e -> e.getValue() > 1L).findAny().map(Map.Entry::getKey);
            if (duplicateTagname.isPresent()) {
                throw new JocConfigurationException(prefix + ": duplicate tag name '" + duplicateTagname.get() + "' with different groups");
            }
    }
    
    private static String testJavaNameRulesAtTag(String prefix, String value) throws JocConfigurationException {
        GroupedTag gt = new GroupedTag(value);
        testJavaNameRules(prefix + ": ", "tag", gt.getTag());
        gt.getGroup().ifPresent(g -> testJavaNameRules(prefix + ": ", "group", g));
        return gt.getTag();
    }
    
    public static boolean orderPreparationHasRequiredParameters(Requirements orderPrep) {
        if (orderPrep != null && orderPrep.getParameters() != null && orderPrep.getParameters().getAdditionalProperties() != null) {
            // find required param in orderPreparation
            return orderPrep.getParameters().getAdditionalProperties().values().stream().filter(i -> i.getFinal() == null)
                    .anyMatch(i -> i.getDefault() == null);
        }
        return false;
    }
}
