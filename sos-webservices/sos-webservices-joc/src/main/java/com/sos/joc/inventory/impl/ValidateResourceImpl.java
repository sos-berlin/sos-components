package com.sos.joc.inventory.impl;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.Schedule;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingCalendars;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.PredicateParser;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IValidateResource;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.Validate;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

@Path(JocInventory.APPLICATION_PATH)
public class ValidateResourceImpl extends JOCResourceImpl implements IValidateResource {

    // private static final Logger LOGGER = LoggerFactory.getLogger(ValidateResourceImpl.class);

    @Override
    public JOCDefaultResponse validate(final String accessToken, String objectType, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getInventory().getConfigurations().isEdit());
            if (response != null) {
                return response;
            }
            checkRequiredParameter("objectType", objectType);
            Validate entity = new Validate();
            try {
                ConfigurationType type = ConfigurationType.fromValue(objectType.toUpperCase());
                if (ConfigurationType.FOLDER.equals(type)) {
                    throw new JobSchedulerInvalidResponseDataException("Unsupprted objectType:" + objectType);
                }
                entity = getValidate(type, inBytes);
            } catch (IllegalArgumentException e) {
                throw new JobSchedulerInvalidResponseDataException("Unsupprted objectType:" + objectType);
            }
            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    public static void validate(ConfigurationType type, byte[] configBytes) throws SOSJsonSchemaException, IOException, SOSHibernateException,
            JocConfigurationException {
        validate(type, configBytes, (IConfigurationObject) Globals.objectMapper.readValue(configBytes, JocInventory.CLASS_MAPPING.get(type)));
    }

    public static void validate(ConfigurationType type, IConfigurationObject config) throws SOSJsonSchemaException, IOException,
            SOSHibernateException, JocConfigurationException {
        validate(type, Globals.objectMapper.writeValueAsBytes(config), config);
    }
    
    private static void validate(ConfigurationType type, byte[] configBytes, IConfigurationObject config) throws SOSJsonSchemaException, IOException,
            SOSHibernateException, JocConfigurationException {
        JsonValidator.validate(configBytes, URI.create(JocInventory.SCHEMA_LOCATION.get(type)));
        if (ConfigurationType.WORKFLOW.equals(type) || ConfigurationType.SCHEDULE.equals(type)) {
            SOSHibernateSession session = null;
            try {
                session = Globals.createSosHibernateStatelessConnection("validate");
                InventoryDBLayer dbLayer = new InventoryDBLayer(session);
                if (ConfigurationType.WORKFLOW.equals(type)) {
                    InventoryAgentInstancesDBLayer agentDBLayer = new InventoryAgentInstancesDBLayer(session);
                    JsonValidator.validateStrict(configBytes, URI.create("classpath:/raml/inventory/schemas/workflow/workflowJobs-schema.json"));
                    validateInstructions(((Workflow) config).getInstructions(), "instructions", new HashMap<String, String>());
                    validateLockRefs(new String(configBytes, StandardCharsets.UTF_8), dbLayer);
                    validateAgentRefs(new String(configBytes, StandardCharsets.UTF_8), agentDBLayer);
                } else if (ConfigurationType.SCHEDULE.equals(type)) {
                    validateWorkflowRef(((Schedule) config).getWorkflowName(), dbLayer);
                    validateCalendarRefs((Schedule) config, dbLayer);
                }
            } finally {
                Globals.disconnect(session);
            }
        }
    }

    private static Validate getValidate(ConfigurationType objectType, byte[] inBytes) {
        Validate v = new Validate();
        try {
            validate(objectType, inBytes);
            v.setValid(true);
        } catch (Throwable e) {
            v.setValid(false);
            v.setInvalidMsg(e.getMessage());
        }
        return v;
    }
    
    private static void validateWorkflowRef(String workflowName, InventoryDBLayer dbLayer) throws JocConfigurationException, SOSHibernateException {
        List<DBItemInventoryConfiguration> workflowPaths = dbLayer.getConfigurationByName(workflowName, ConfigurationType.WORKFLOW.intValue());
        if (workflowPaths == null || !workflowPaths.stream().anyMatch(w -> workflowName.equals(w.getName()))) {
            throw new JocConfigurationException("Missing assigned Workflow: " + workflowName); 
        }
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
    
    private static void validateAgentRefs(String json, InventoryAgentInstancesDBLayer dbLayer) throws SOSHibernateException, JocConfigurationException {
        Matcher m = Pattern.compile("\"agentId\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        Set<String> agents = new HashSet<>();
        while (m.find()) {
            if (m.group(1) != null && !m.group(1).isEmpty()) {
                agents.add(m.group(1));
            }
        }
        if (!agents.isEmpty()) {
            agents.removeAll(dbLayer.getEnabledAgentNames());
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

    private static void validateInstructions(Collection<Instruction> instructions, String position, Map<String, String> labels)
            throws SOSJsonSchemaException, JsonProcessingException, IOException {
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
                    break;
                case EXECUTE_NAMED:
                    NamedJob nj = inst.cast();
                    if (labels.containsKey(nj.getLabel())) {
                        throw new SOSJsonSchemaException("$." + instPosition + "label: duplicate label '" + nj.getLabel() + "' with " + labels.get(nj
                                .getLabel()));
                    } else {
                        labels.put(nj.getLabel(), "$." + instPosition + "label");
                    }
                    break;
                case FORK:
                    ForkJoin fj = inst.cast();
                    int branchIndex = 0;
                    String branchPosition = instPosition + "branches";
                    for (Branch branch : fj.getBranches()) {
                        String branchInstPosition = branchPosition + "[" + branchIndex + "].";
                        validateInstructions(branch.getWorkflow().getInstructions(), branchInstPosition + "instructions", labels);
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
                    validateInstructions(ifElse.getThen().getInstructions(), instPosition + "then.instructions", labels);
                    if (ifElse.getElse() != null) {
                        validateInstructions(ifElse.getElse().getInstructions(), instPosition + "else.instructions", labels);
                    }
                    break;
                case TRY:
                    TryCatch tryCatch = inst.cast();
                    validateInstructions(tryCatch.getTry().getInstructions(), instPosition + "try.instructions", labels);
                    validateInstructions(tryCatch.getCatch().getInstructions(), instPosition + "catch.instructions", labels);
                    break;
                case LOCK:
                    Lock lock = inst.cast();
                    validateInstructions(lock.getLockedWorkflow().getInstructions(), instPosition + "lockedWorkflow.instructions", labels);
                    break;
                }
                index++;
            }
        }
    }

}
