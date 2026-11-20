package com.sos.joc.classes.inventory;

import java.net.URI;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Frequencies;
import com.sos.inventory.model.fileordersource.FileOrderSource;
import com.sos.inventory.model.schedule.OrderPositions;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.joc.Globals;
import com.sos.joc.classes.common.StringSizeSanitizer;
import com.sos.joc.classes.inventory.validators.BoardValidator;
import com.sos.joc.classes.inventory.validators.CalendarsValidator;
import com.sos.joc.classes.inventory.validators.FileOrderSourceValidator;
import com.sos.joc.classes.inventory.validators.FileTransferValidator;
import com.sos.joc.classes.inventory.validators.JobResourceValidator;
import com.sos.joc.classes.inventory.validators.JobTemplateValidator;
import com.sos.joc.classes.inventory.validators.ReportValidator;
import com.sos.joc.classes.inventory.validators.ScheduleValidator;
import com.sos.joc.classes.inventory.validators.WorkflowValidator;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.tag.GroupedTag;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.order.BlockPosition;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data_for_java.value.JExpression;

public class Validator {

    /** @param configPath
     * @param type
     * @param configBytes
     * @param dbLayer
     * @param visibleAgentNames
     * @throws Exception */
    public static void validate(ConfigurationType type, String configPath, byte[] configBytes, InventoryDBLayer dbLayer,
            Set<String> visibleAgentNames) throws Exception {
        validate(type, configPath, configBytes, (IConfigurationObject) Globals.objectMapper.readValue(configBytes, JocInventory.CLASS_MAPPING.get(
                type)), dbLayer, visibleAgentNames);
    }

    /** @param configPath
     * @param type
     * @param config
     * @param dbLayer
     * @param visibleAgentNames
     * @throws Exception */
    public static void validate(ConfigurationType type, String configPath, IConfigurationObject config, InventoryDBLayer dbLayer,
            Set<String> visibleAgentNames) throws Exception {
        validate(type, configPath, Globals.objectMapper.writeValueAsBytes(config), config, dbLayer, visibleAgentNames);
    }

    /** @param configPath
     * @param type
     * @param configBytes
     * @throws Exception */
    public static void validate(ConfigurationType type, String configPath, byte[] configBytes) throws Exception {
        validate(type, configPath, configBytes, (IConfigurationObject) Globals.objectMapper.readValue(configBytes, JocInventory.CLASS_MAPPING.get(
                type)), (InventoryDBLayer) null, null);
    }

    /** @param configPath
     * @param type
     * @param config
     * @throws Exception */
    public static void validate(ConfigurationType type, String configPath, IConfigurationObject config) throws Exception {
        validate(type, configPath, Globals.objectMapper.writeValueAsBytes(config), config, (InventoryDBLayer) null, null);
    }

    private static void validate(ConfigurationType type, String configPath, byte[] configBytes, IConfigurationObject config, InventoryDBLayer dbLayer,
            Set<String> visibleAgentNames) throws Exception {

        JsonValidator.validate(configBytes, URI.create(JocInventory.SCHEMA_LOCATION.get(type)), true);

        SOSHibernateSession session = null;
        try {
            switch (type) {
            case WORKFLOW:
                if (dbLayer == null) {
                    session = Globals.createSosHibernateStatelessConnection("validateWorkflow");
                    dbLayer = new InventoryDBLayer(session);
                }
                WorkflowValidator.validate(configBytes, config, dbLayer, visibleAgentNames);
                break;
            case SCHEDULE:
                if (dbLayer == null) {
                    session = Globals.createSosHibernateStatelessConnection("validateSchedule");
                    dbLayer = new InventoryDBLayer(session);
                }
                ScheduleValidator.validate(config, dbLayer);
                break;
            case FILEORDERSOURCE:
                if (dbLayer == null) {
                    session = Globals.createSosHibernateStatelessConnection("validateFileOrderSource");
                    dbLayer = new InventoryDBLayer(session);
                }
                FileOrderSourceValidator.validate(config, dbLayer);
                break;
            case JOBTEMPLATE:
                if (dbLayer == null) {
                    session = Globals.createSosHibernateStatelessConnection("validateJobTemplate");
                    dbLayer = new InventoryDBLayer(session);
                }
                JobTemplateValidator.validate(config, dbLayer);
                break;
            case WORKINGDAYSCALENDAR:
            case NONWORKINGDAYSCALENDAR:
                if (dbLayer == null) {
                    session = Globals.createSosHibernateStatelessConnection("validateCalendars");
                    dbLayer = new InventoryDBLayer(session);
                }
                CalendarsValidator.validate(config, dbLayer);
                break;

            case JOBRESOURCE:
                JobResourceValidator.validate(config);
                break;
            case NOTICEBOARD:
                BoardValidator.validate(config);
                break;
            case REPORT:
                ReportValidator.validate(config);
                break;

            // FileTransfer
            case FT_FRAGMENT_CREDENTIAL_STORE:
                FileTransferValidator.validateCredentialStoreFragment(configPath, config);
                break;
            case FT_FRAGMENT_DECRYPTION:
                FileTransferValidator.validateDecryptionFragment(configPath, config);
                break;
            case FT_FRAGMENT_MAIL:
                FileTransferValidator.validateMailFragment(configPath, config);
                break;
            case FT_FRAGMENT_HTTP_PROXY:
                FileTransferValidator.validateHttpProxyFragment(configPath, config);
                break;
            case FT_FRAGMENT_SOCKS_PROXY:
                FileTransferValidator.validateSocksProxyFragment(configPath, config);
                break;

            case FT_FRAGMENT_AZURE_BLOB_STORAGE:
                if (dbLayer == null) {
                    session = Globals.createSosHibernateStatelessConnection("validateAzureBlobStorageFragment");
                    dbLayer = new InventoryDBLayer(session);
                }
                FileTransferValidator.validateAzureBlobStorageFragment(configPath, config, dbLayer);
                break;
            case FT_FRAGMENT_FTP:
                if (dbLayer == null) {
                    session = Globals.createSosHibernateStatelessConnection("validateFtpFragment");
                    dbLayer = new InventoryDBLayer(session);
                }
                FileTransferValidator.validateFtpFragment(configPath, config, dbLayer);
                break;
            case FT_FRAGMENT_HTTP:
                if (dbLayer == null) {
                    session = Globals.createSosHibernateStatelessConnection("validateHttpFragment");
                    dbLayer = new InventoryDBLayer(session);
                }
                FileTransferValidator.validateHttpFragment(configPath, config, dbLayer);
                break;
            case FT_FRAGMENT_JUMP:
                if (dbLayer == null) {
                    session = Globals.createSosHibernateStatelessConnection("validateJumpFragment");
                    dbLayer = new InventoryDBLayer(session);
                }
                FileTransferValidator.validateJumpFragment(configPath, config, dbLayer);
                break;
            case FT_FRAGMENT_SFTP:
                if (dbLayer == null) {
                    session = Globals.createSosHibernateStatelessConnection("validateSftpFragment");
                    dbLayer = new InventoryDBLayer(session);
                }
                FileTransferValidator.validateSftpFragment(configPath, config, dbLayer);
                break;
            case FT_FRAGMENT_SMB:
                if (dbLayer == null) {
                    session = Globals.createSosHibernateStatelessConnection("validateSmbFragment");
                    dbLayer = new InventoryDBLayer(session);
                }
                FileTransferValidator.validateSmbFragment(configPath, config, dbLayer);
                break;
            case FT_FRAGMENT_WEBDAV:
                if (dbLayer == null) {
                    session = Globals.createSosHibernateStatelessConnection("validateWebdavFragment");
                    dbLayer = new InventoryDBLayer(session);
                }
                FileTransferValidator.validateWebdavFragment(configPath, config, dbLayer);
                break;
            case FT_PROFILE_JOBRESOURCE:
                if (dbLayer == null) {
                    session = Globals.createSosHibernateStatelessConnection("validateProfileJobResource");
                    dbLayer = new InventoryDBLayer(session);
                }
                FileTransferValidator.validateJobResource(configPath, config, dbLayer);
                break;

            case FOLDER:
            case DEPLOYMENTDESCRIPTOR:
            case DESCRIPTORFOLDER:
            case JOBCLASS:
            case INCLUDESCRIPT:
            case LOCK:
            default:
                break;

            }
        } finally {
            Globals.disconnect(session);
        }
    }

    public static void revalidate(ConfigurationType type, byte[] configBytes, IConfigurationObject config,
            Map<ConfigurationType, Set<String>> invObjNames, Map<String, String> workflowJsonsByName, Set<String> visibleAgentNames)
            throws Exception {

        JsonValidator.validate(configBytes, URI.create(JocInventory.SCHEMA_LOCATION.get(type)), true);

        switch (type) {
        case WORKFLOW:
            WorkflowValidator.revalidate(configBytes, config, invObjNames, workflowJsonsByName, visibleAgentNames);
            break;
        case SCHEDULE:
            ScheduleValidator.revalidate(config, invObjNames, workflowJsonsByName);
            break;
        case FILEORDERSOURCE:
            FileOrderSourceValidator.revalidate(config, workflowJsonsByName);
            break;
        case JOBTEMPLATE:
            JobTemplateValidator.revalidate(config, invObjNames);
            break;
        case WORKINGDAYSCALENDAR:
        case NONWORKINGDAYSCALENDAR:
            CalendarsValidator.revalidate(config, invObjNames);
            break;
        case JOBRESOURCE:
            JobResourceValidator.revalidate(config);
            break;
        case NOTICEBOARD:
            BoardValidator.revalidate(config);
            break;
        case REPORT:
            ReportValidator.revalidate(config);
            break;
        // FileTransfer
        case FT_FRAGMENT_AZURE_BLOB_STORAGE:
        case FT_FRAGMENT_CREDENTIAL_STORE:
        case FT_FRAGMENT_DECRYPTION:
        case FT_FRAGMENT_FTP:
        case FT_FRAGMENT_HTTP:
        case FT_FRAGMENT_HTTP_PROXY:
        case FT_FRAGMENT_JUMP:
        case FT_FRAGMENT_MAIL:
        case FT_FRAGMENT_SFTP:
        case FT_FRAGMENT_SMB:
        case FT_FRAGMENT_SOCKS_PROXY:
        case FT_FRAGMENT_WEBDAV:
        case FT_PROFILE_JOBRESOURCE:
            // Nothing to do for FileTransfer types - validated by schema.
            break;

        case FOLDER:
        case DEPLOYMENTDESCRIPTOR:
        case DESCRIPTORFOLDER:
        case JOBCLASS:
        case INCLUDESCRIPT:
        case LOCK:
        default:
            break;

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

    public static boolean orderPreparationHasRequiredParameters(Requirements orderPrep) {
        if (orderPrep != null && orderPrep.getParameters() != null && orderPrep.getParameters().getAdditionalProperties() != null) {
            // find required param in orderPreparation
            return orderPrep.getParameters().getAdditionalProperties().values().stream().filter(i -> i.getFinal() == null).anyMatch(i -> i
                    .getDefault() == null);
        }
        return false;
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

    public static void validateExpression(String prefix, Map<String, String> map) throws JocConfigurationException {
        if (map != null) {
            map.forEach((k, v) -> validateExpression(prefix, k, v));
        }
    }

    public static void validateExpression(String prefix, String key, String value) throws JocConfigurationException {
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

    public static void validateJobResourceRefs(List<String> jobResources, InventoryDBLayer dbLayer) throws SOSHibernateException {
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

    public static void validateJobResourceRefs(List<String> jobResources, Set<String> allJobResourceNames) throws JocConfigurationException {
        if (jobResources != null && !jobResources.isEmpty()) {
            Set<String> jobResourcesCopy = new HashSet<>(jobResources);
            jobResourcesCopy.removeAll(allJobResourceNames);
            if (!jobResourcesCopy.isEmpty()) {
                throw new JocConfigurationException("Missing assigned JobResources: " + jobResourcesCopy.toString());
            }
        }
    }

    public static String validateWorkflowRef(String workflowName, InventoryDBLayer dbLayer, String position) throws JocConfigurationException,
            SOSHibernateException {
        List<DBItemInventoryConfiguration> workflows = dbLayer.getConfigurationByName(workflowName, ConfigurationType.WORKFLOW.intValue());
        if (workflows == null || !workflows.stream().anyMatch(w -> workflowName.equals(w.getName()))) {
            throw new JocConfigurationException(position + ": Missing assigned Workflow: " + workflowName);
        }
        return workflows.get(0).getContent();
    }

    public static String validateWorkflowRef(String workflowName, Map<String, String> workflowJsons, String position)
            throws JocConfigurationException {
        String wJson = workflowJsons.get(workflowName);
        if (wJson == null) {
            throw new JocConfigurationException(position + ": Missing assigned Workflow: " + workflowName);
        }
        return wJson;
    }

    public static void validateCalendarRefs(Calendar calendar, Set<String> allWorkingDaysCalendarNames, Set<String> allNonWorkingDaysCalendarNames)
            throws JocConfigurationException {
        Set<String> calendarNames = getCalendarRefsInCalendar(calendar);

        calendarNames.removeAll(allWorkingDaysCalendarNames);
        calendarNames.removeAll(allNonWorkingDaysCalendarNames);
        if (!calendarNames.isEmpty()) {
            throw new JocConfigurationException("Missing assigned Calendars: " + calendarNames.toString());
        }
    }

    public static void validateCalendarRefs(List<String> calendarNames, InventoryDBLayer dbLayer) throws SOSHibernateException {
        if (!calendarNames.isEmpty()) {
            List<DBItemInventoryConfiguration> dbCalendars = dbLayer.getCalendarsByNames(calendarNames);
            if (dbCalendars == null || dbCalendars.isEmpty()) {
                throw new JocConfigurationException("Missing assigned Calendars: " + calendarNames.toString());
            } else if (dbCalendars.size() < calendarNames.size()) {
                calendarNames.removeAll(dbCalendars.stream().map(DBItemInventoryConfiguration::getName).collect(Collectors.toSet()));
                throw new JocConfigurationException("Missing assigned Calendars: " + calendarNames.toString());
            }
        }
    }

    public static void validateTimeZone(String timeZone, String position) {
        try {
            ZoneId.of(timeZone);
        } catch (Exception e) {
            throw new JocConfigurationException(position + ".timeZone: " + e.getMessage());
        }
    }

    public static void validateTimeZone(String timeZone) {
        validateTimeZone(timeZone, "$");
    }

    public static void checkAddOrderPositions(OrderPositions p, Set<BlockPosition> availableBlockPositions, String workflowName,
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

    public static void testJavaNameRules(String prefix, String key, String value) throws JocConfigurationException {
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
        Optional<String> duplicateTagname = tags.map(tag -> testJavaNameRulesAtTag(prefix, tag)).collect(Collectors.groupingBy(Function.identity(),
                Collectors.counting())).entrySet().stream().filter(e -> e.getValue() > 1L).findAny().map(Map.Entry::getKey);
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

    private static Set<String> getCalendarRefsInCalendar(Calendar calendar) {
        return Optional.ofNullable(calendar.getExcludes()).map(Frequencies::getNonWorkingDayCalendars).orElse(Collections.emptySet());
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

}
