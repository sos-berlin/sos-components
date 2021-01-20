package com.sos.joc.inventory.impl;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.model.instruction.ForkJoin;
import com.sos.jobscheduler.model.instruction.IfElse;
import com.sos.jobscheduler.model.instruction.Instruction;
import com.sos.jobscheduler.model.instruction.Lock;
import com.sos.jobscheduler.model.instruction.NamedJob;
import com.sos.jobscheduler.model.instruction.TryCatch;
import com.sos.jobscheduler.model.workflow.Branch;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.PredicateParser;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IValidateResource;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.Validate;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;
import com.sos.webservices.order.initiator.model.AssignedCalendars;
import com.sos.webservices.order.initiator.model.AssignedNonWorkingCalendars;
import com.sos.webservices.order.initiator.model.Schedule;

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

    public static void validate(ConfigurationType type, byte[] configBytes) throws SOSJsonSchemaException, IOException, SOSHibernateException {
        validate(type, configBytes, (IConfigurationObject) Globals.objectMapper.readValue(configBytes, JocInventory.CLASS_MAPPING.get(type)));
    }

    public static void validate(ConfigurationType type, IConfigurationObject config) throws SOSJsonSchemaException, IOException,
            SOSHibernateException {
        validate(type, Globals.objectMapper.writeValueAsBytes(config), config);
    }
    
    private static void validate(ConfigurationType type, byte[] configBytes, IConfigurationObject config) throws SOSJsonSchemaException, IOException,
            SOSHibernateException {
        JsonValidator.validate(configBytes, URI.create(JocInventory.SCHEMA_LOCATION.get(type)));
        if (ConfigurationType.WORKFLOW.equals(type)) {
            JsonValidator.validateStrict(configBytes, URI.create("classpath:/raml/controller/schemas/workflow/workflowJobs-schema.json"));
            validateInstructions(((Workflow) config).getInstructions(), "instructions", new HashMap<String, String>());
        } else if (ConfigurationType.SCHEDULE.equals(type)) {
            validateCalendarRefs((Schedule) config);
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

    private static void validateCalendarRefs(Schedule schedule) throws SOSHibernateException {
        Set<String> calendarNames = schedule.getCalendars().stream().map(AssignedCalendars::getCalendarPath).collect(Collectors.toSet());
        if (schedule.getNonWorkingCalendars() != null) {
            //temp.: map(JocInventory::pathToName)
            calendarNames.addAll(schedule.getNonWorkingCalendars().stream().map(AssignedNonWorkingCalendars::getCalendarPath).map(
                    JocInventory::pathToName).collect(Collectors.toSet()));
        }
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            List<DBItemInventoryConfiguration> dbCalendars = dbLayer.getCalendarsByNames(calendarNames.stream());
            if (dbCalendars == null || dbCalendars.isEmpty()) {
                throw new JocConfigurationException("Missing assigned Calendars: " + calendarNames.toString()); 
            } else if (dbCalendars.size() < calendarNames.size()) {
                calendarNames.removeAll(dbCalendars.stream().map(DBItemInventoryConfiguration::getPath).collect(Collectors.toSet()));
                throw new JocConfigurationException("Missing assigned Calendars: " + calendarNames.toString());
            }
        } finally {
            Globals.disconnect(session);
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
