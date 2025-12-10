package com.sos.joc.publish.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.networknt.schema.ValidatorTypeCode;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.inventory.model.board.BoardType;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingDayCalendars;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.CalendarType;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.instruction.AddOrder;
import com.sos.inventory.model.instruction.AdmissionTime;
import com.sos.inventory.model.instruction.CaseWhen;
import com.sos.inventory.model.instruction.ConsumeNotices;
import com.sos.inventory.model.instruction.Cycle;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.ForkList;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Options;
import com.sos.inventory.model.instruction.StickySubagent;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.instruction.When;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.jobtemplate.JobTemplate;
import com.sos.inventory.model.report.Report;
import com.sos.inventory.model.schedule.OrderParameterisation;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.script.Script;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JsonConverter;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.classes.inventory.WorkflowConverter;
import com.sos.joc.classes.order.OrderTags;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.classes.tag.GroupedTag;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.db.history.DBItemHistoryOrderTag;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJobTag;
import com.sos.joc.db.inventory.DBItemInventoryJobTagging;
import com.sos.joc.db.inventory.DBItemInventoryOrderTag;
import com.sos.joc.db.inventory.DBItemInventoryTag;
import com.sos.joc.db.inventory.DBItemInventoryTagGroup;
import com.sos.joc.db.inventory.DBItemInventoryTagging;
import com.sos.joc.db.inventory.IDBItemTag;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryJobTagDBLayer;
import com.sos.joc.db.inventory.InventoryOrderTagDBLayer;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.db.inventory.InventoryTagGroupDBLayer;
import com.sos.joc.db.inventory.common.ATagDBLayer;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocImportException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.model.SuffixPrefix;
import com.sos.joc.model.agent.transfer.Agent;
import com.sos.joc.model.calendar.NonWorkingDaysCalendarEdit;
import com.sos.joc.model.calendar.WorkingDaysCalendarEdit;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.board.BoardEdit;
import com.sos.joc.model.inventory.board.BoardPublish;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.fileordersource.FileOrderSourceEdit;
import com.sos.joc.model.inventory.fileordersource.FileOrderSourcePublish;
import com.sos.joc.model.inventory.job.JobEdit;
import com.sos.joc.model.inventory.jobclass.JobClassEdit;
import com.sos.joc.model.inventory.jobclass.JobClassPublish;
import com.sos.joc.model.inventory.jobresource.JobResourceEdit;
import com.sos.joc.model.inventory.jobresource.JobResourcePublish;
import com.sos.joc.model.inventory.lock.LockEdit;
import com.sos.joc.model.inventory.lock.LockPublish;
import com.sos.joc.model.inventory.report.ReportEdit;
import com.sos.joc.model.inventory.script.ScriptEdit;
import com.sos.joc.model.inventory.workflow.WorkflowEdit;
import com.sos.joc.model.inventory.workflow.WorkflowPublish;
import com.sos.joc.model.joc.JocMetaInfo;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.joc.model.sign.Signature;
import com.sos.joc.model.sign.SignaturePath;
import com.sos.joc.model.tag.AddOrderOrderTags;
import com.sos.joc.model.tag.AddOrdersOrderTags;
import com.sos.joc.model.tag.ExportedJobTagItem;
import com.sos.joc.model.tag.ExportedJobTagItems;
import com.sos.joc.model.tag.ExportedOrderTags;
import com.sos.joc.model.tag.ExportedTagItem;
import com.sos.joc.model.tag.ExportedTags;
import com.sos.joc.model.tag.FileOrderSourceOrderTags;
import com.sos.joc.model.tag.ScheduleOrderTags;
import com.sos.joc.model.tag.common.JobTags;
import com.sos.joc.publish.common.ConfigurationObjectFileExtension;
import com.sos.joc.publish.common.ControllerObjectFileExtension;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.ArchiveValues;
import com.sos.joc.publish.mapper.UpdateableConfigurationObject;
import com.sos.joc.tags.impl.TaggingImpl;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;
import com.sos.sign.model.board.Board;
import com.sos.sign.model.fileordersource.FileOrderSource;
import com.sos.sign.model.jobclass.JobClass;
import com.sos.sign.model.jobresource.JobResource;
import com.sos.sign.model.lock.Lock;
import com.sos.webservices.order.initiator.model.ScheduleEdit;

public class ImportUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportUtils.class);
    private static final String AGENT_FILE_EXTENSION = ".agent.json";
    private static final Predicate<String> HAS_NOTICE_BOARDS = Pattern.compile("\"(?:noticeB|b)oardNames\"\\s*:\\s*").asPredicate();
    private static final String jsonSchema = "classpath:/raml/api/schemas/agent/transfer/agent-schema.json";

    public static final String JOC_META_INFO_FILENAME = "meta_inf";

    
    public static UpdateableConfigurationObject createUpdateableConfiguration(DBItemInventoryConfiguration existingConfiguration,
            ConfigurationObject configuration, Map<ConfigurationType, List<ConfigurationObject>> configurations, String prefix, String suffix,
            String targetFolder, DBLayerDeploy dbLayer) throws SOSHibernateException {

        ConfigurationGlobalsJoc clusterSettings = Globals.getConfigurationGlobalsJoc();
        SuffixPrefix suffixPrefix = null;
        // prefix/suffix will always be added to the configurations name if not empty, even if no previous configuration exist
        if (existingConfiguration != null) {
            suffixPrefix = JocInventory.getSuffixPrefix(suffix, prefix, ClusterSettings.getImportSuffixPrefix(clusterSettings),
                    clusterSettings.getImportSuffix().getDefault(), existingConfiguration.getName(), configuration.getObjectType(), 
                    new InventoryDBLayer(dbLayer.getSession()));
        } else {
            suffixPrefix = JocInventory.getSuffixPrefix(suffix, prefix, ClusterSettings.getImportSuffixPrefix(clusterSettings), 
                    clusterSettings.getImportSuffix().getDefault(), configuration.getName(), configuration.getObjectType(), 
                    new InventoryDBLayer(dbLayer.getSession()));
        }
        final List<String> replace = JocInventory.getSearchReplace(suffixPrefix);
        final String oldName = configuration.getName();
        final String newName = configuration.getName().replaceFirst(replace.get(0), replace.get(1));
        Set<ConfigurationObject> referencedBy = new HashSet<ConfigurationObject>();

        switch (configuration.getObjectType()) {
        case LOCK:
            referencedBy.addAll(getUsedWorkflowsFromArchiveByLockId(oldName, configurations.get(ConfigurationType.WORKFLOW)));
            break;
        case NOTICEBOARD:
            referencedBy.addAll(getUsedWorkflowsFromArchiveByBoardName(oldName, configurations.get(ConfigurationType.WORKFLOW)));
            break;
        case WORKFLOW:
            referencedBy.addAll(getUsedFileOrderSourcesFromArchiveByWorkflowName(oldName, configurations.get(ConfigurationType.FILEORDERSOURCE)));
            referencedBy.addAll(getUsedSchedulesFromArchiveByWorkflowName(oldName, configurations.get(ConfigurationType.SCHEDULE)));
            referencedBy.addAll(getUsedWorkflowsFromArchiveByWorkflowName(oldName, configurations.get(ConfigurationType.WORKFLOW)));
            break;
        case WORKINGDAYSCALENDAR:
        case NONWORKINGDAYSCALENDAR:
            referencedBy.addAll(getUsedSchedulesFromArchiveByCalendarName(oldName, configurations.get(ConfigurationType.SCHEDULE)));
            break;
        case JOBRESOURCE:
            referencedBy.addAll(getUsedWorkflowsFromArchiveByJobResourceName(oldName, configurations.get(ConfigurationType.WORKFLOW)));
            referencedBy.addAll(getUsedJobTemplatesFromArchiveByJobResourcesName(oldName, configurations.get(ConfigurationType.JOBTEMPLATE)));
            break;
        case JOBTEMPLATE:
            referencedBy.addAll(getUsedWorkflowsFromArchiveByJobTemplateName(oldName, configurations.get(ConfigurationType.WORKFLOW)));
            break;
        case INCLUDESCRIPT:
            referencedBy.addAll(getUsedWorkflowsFromArchiveByIncludeScriptName(oldName, configurations.get(ConfigurationType.WORKFLOW)));
            break;
        default:
            break;
        }
        return new UpdateableConfigurationObject(configuration, existingConfiguration, oldName, newName, referencedBy, targetFolder);
    }

    public static void replaceReferences (UpdateableConfigurationObject updateableItem) {
    	
//    	Date now = Date.from(Instant.now());
    	// update existing configuration from archive
    	updateableItem.getConfigurationObject().setName(updateableItem.getNewName());
    	if (updateableItem.getTargetFolder() != null && !updateableItem.getTargetFolder().isEmpty()) {
    		Path folder = Paths.get(updateableItem.getTargetFolder() + updateableItem.getConfigurationObject().getPath()).getParent();
    		updateableItem.getConfigurationObject().setPath(folder.resolve(updateableItem.getNewName()).toString().replace('\\', '/'));
    	} else {
    		updateableItem.getConfigurationObject().setPath(
    				Paths.get(updateableItem.getConfigurationObject().getPath()).getParent().resolve(updateableItem.getNewName()).toString().replace('\\', '/'));
    	}
    	// update configurations referenced by existing configuration from DB
    	if (updateableItem.getReferencedBy() != null && !updateableItem.getReferencedBy().isEmpty()) {
    	    Map<String, String> oldNewNames =  Collections.singletonMap(updateableItem.getOldName(), updateableItem .getNewName());
            
        	for (ConfigurationObject configurationWithReference : updateableItem.getReferencedBy()) {
                switch (configurationWithReference.getObjectType()) {
                case WORKFLOW:
                    if (updateableItem.getConfigurationObject().getObjectType().equals(ConfigurationType.LOCK)) {
                        try {
                            String json = Globals.objectMapper.writeValueAsString(configurationWithReference.getConfiguration());
                            json = json.replaceAll("(\"lockName\"\\s*:\\s*\")" + updateableItem.getOldName() + "\"", "$1" + updateableItem.getNewName() + "\"");
                            ((WorkflowEdit)configurationWithReference).setConfiguration(WorkflowConverter.convertInventoryWorkflow(json));
                        } catch (IOException e) {
                            throw new JocImportException(e);
                        }
                    } else if (updateableItem.getConfigurationObject().getObjectType().equals(ConfigurationType.NOTICEBOARD)) {
                        try {
                            WorkflowsHelper.updateWorkflowBoardname(oldNewNames, ((Workflow) configurationWithReference.getConfiguration())
                                    .getInstructions());
                        } catch (Exception e) {
                            throw new JocImportException(e);
                        }
                    } else if (updateableItem.getConfigurationObject().getObjectType().equals(ConfigurationType.WORKFLOW)) {
                        try {
                            String json = Globals.objectMapper.writeValueAsString(configurationWithReference.getConfiguration());
                            json = json.replaceAll("(\"workflowName\"\\s*:\\s*\")" + updateableItem.getOldName() + "\"", "$1" + updateableItem.getNewName() + "\"");
                            ((WorkflowEdit)configurationWithReference).setConfiguration(WorkflowConverter.convertInventoryWorkflow(json));
                        } catch (IOException e) {
                            throw new JocImportException(e);
                        }
                    } else if (updateableItem.getConfigurationObject().getObjectType().equals(ConfigurationType.JOBRESOURCE)) {
                        try {
                            Workflow w = (Workflow) configurationWithReference.getConfiguration();
                            if (w.getJobResourceNames() != null) {
                                w.setJobResourceNames(w.getJobResourceNames().stream().map(s -> oldNewNames.getOrDefault(s, s)).collect(Collectors
                                        .toList()));
                            }
                            if (w.getJobs() != null && w.getJobs().getAdditionalProperties() != null) {
                                w.getJobs().getAdditionalProperties().forEach((k, v) -> {
                                    if (v.getJobResourceNames() != null) {
                                        v.setJobResourceNames(v.getJobResourceNames().stream().map(s -> oldNewNames.getOrDefault(s, s)).collect(
                                                Collectors.toList()));
                                    }
                                });
                            }
                        } catch (Exception e) {
                            throw new JocImportException(e);
                        }
                    } else if (updateableItem.getConfigurationObject().getObjectType().equals(ConfigurationType.JOBTEMPLATE)) {
                        try {
                            Workflow w = (Workflow) configurationWithReference.getConfiguration();
                            if (w.getJobs() != null && w.getJobs().getAdditionalProperties() != null) {
                                w.getJobs().getAdditionalProperties().forEach((k, v) -> {
                                    if (v.getJobTemplate() != null) {
                                        String oldName = v.getJobTemplate().getName();
                                        if (oldName != null) {
                                            v.getJobTemplate().setName(oldNewNames.getOrDefault(oldName, oldName));
                                        }
                                    }
                                });
                            }
                        } catch (Exception e) {
                            throw new JocImportException(e);
                        }
                    } else if (updateableItem.getConfigurationObject().getObjectType().equals(ConfigurationType.INCLUDESCRIPT)) {
                        try {
                            String json = Globals.objectMapper.writeValueAsString(configurationWithReference.getConfiguration());
                            json = JsonConverter.replaceNameOfIncludeScript(json, updateableItem.getOldName(), updateableItem.getNewName());
                            
                            ((WorkflowEdit)configurationWithReference).setConfiguration(WorkflowConverter.convertInventoryWorkflow(json));
                        } catch (Exception e) {
                            throw new JocImportException(e);
                        }
                    }
                    break;
                case FILEORDERSOURCE:
                	if (((FileOrderSourceEdit)configurationWithReference).getConfiguration().getWorkflowName().equals(updateableItem.getOldName())
                			|| ((FileOrderSourceEdit)configurationWithReference).getConfiguration().getWorkflowName()
                				.equals(updateableItem.getConfigurationObject().getName())) {
                		((FileOrderSourceEdit)configurationWithReference).getConfiguration().setWorkflowName(updateableItem.getNewName());
                	}
                    break;
                case SCHEDULE:
                    if (updateableItem.getConfigurationObject().getObjectType().equals(ConfigurationType.WORKFLOW)) {
                        if (((ScheduleEdit)configurationWithReference).getConfiguration().getWorkflowNames() != null && 
                                ((ScheduleEdit)configurationWithReference).getConfiguration().getWorkflowNames().contains(updateableItem.getOldName())) {
                            ((ScheduleEdit)configurationWithReference).getConfiguration().setWorkflowNames(
                                    ((ScheduleEdit)configurationWithReference).getConfiguration().getWorkflowNames().stream()
                                    .map(item -> item.equals(updateableItem.getOldName()) ? updateableItem.getNewName() : item).collect(Collectors.toList()));
                        } else if (updateableItem.getOldName().equals(((ScheduleEdit)configurationWithReference).getConfiguration().getWorkflowName())) {
                            ((ScheduleEdit)configurationWithReference).getConfiguration().setWorkflowName(updateableItem.getNewName());
                        }
                    } else  if (updateableItem.getConfigurationObject().getObjectType().equals(ConfigurationType.WORKINGDAYSCALENDAR)) {
                    	List<AssignedCalendars> assignedCalendars = ((ScheduleEdit)configurationWithReference).getConfiguration().getCalendars();
                    	assignedCalendars.stream().forEach(item -> {
                        	if (item.getCalendarName().equals(updateableItem.getOldName()) || 
                        			item.getCalendarName().equals(updateableItem.getConfigurationObject().getName())) {
                        		item.setCalendarName(updateableItem.getNewName());
                        	}
                    	});
                    } else  if (updateableItem.getConfigurationObject().getObjectType().equals(ConfigurationType.NONWORKINGDAYSCALENDAR)) {
                    	List<AssignedNonWorkingDayCalendars> assignedNWDCalendars = 
                    			((ScheduleEdit)configurationWithReference).getConfiguration().getNonWorkingDayCalendars();
                    	assignedNWDCalendars.stream().forEach(item -> {
                        	if (item.getCalendarName().equals(updateableItem.getOldName()) 
                        			|| item.getCalendarName().equals(updateableItem.getConfigurationObject().getName())) {
                        		item.setCalendarName(updateableItem.getNewName());
                        	}
                    	});
                    }
                    break;
                case JOBTEMPLATE:
                    if (updateableItem.getConfigurationObject().getObjectType().equals(ConfigurationType.INCLUDESCRIPT)) {
                        try {
                            String json = Globals.objectMapper.writeValueAsString(configurationWithReference.getConfiguration());
                            json = JsonConverter.replaceNameOfIncludeScript(json, updateableItem.getOldName(), updateableItem.getNewName());
                            ((JobEdit)configurationWithReference).setConfiguration(Globals.objectMapper.readValue(json, JobTemplate.class));
                        } catch (Exception e) {
                            throw new JocImportException(e);
                        }
                    } else if (updateableItem.getConfigurationObject().getObjectType().equals(ConfigurationType.JOBRESOURCE)) {
                        try {
                            JobTemplate jt = (JobTemplate) configurationWithReference.getConfiguration();
                            if (jt.getJobResourceNames() != null) {
                                jt.setJobResourceNames(jt.getJobResourceNames().stream().map(s -> oldNewNames.getOrDefault(s, s)).collect(
                                        Collectors.toList()));
                            }
                        } catch (Exception e) {
                            throw new JocImportException(e);
                        }
                    } 
                    break;
                default:
                    break;
                }
        	}
    	}
    }

    public static DBItemInventoryConfiguration updateConfigurationWithChangedReferences (DBLayerDeploy dbLayer, ConfigurationObject config) {
        DBItemInventoryConfiguration alreadyExist = dbLayer.getInventoryConfigurationByNameAndType(config.getName(), config.getObjectType().intValue());
        if (alreadyExist != null) {
            try {
                alreadyExist.setContent(JocInventory.toString(config.getConfiguration()));
                alreadyExist.setModified(Date.from(Instant.now()));
                try {
                    Validator.validate(alreadyExist.getTypeAsEnum(), alreadyExist.getContent().getBytes(), new InventoryDBLayer(dbLayer.getSession()), null);
                    alreadyExist.setValid(true);
                } catch (Throwable e) {
                    alreadyExist.setValid(false);
                }
                JocInventory.updateConfiguration(new InventoryDBLayer(dbLayer.getSession()), alreadyExist);
            } catch (JsonProcessingException e) {
                LOGGER.error(e.getMessage(),e);
            } catch (SOSHibernateException e) {
                throw new JocSosHibernateException(e);
            } catch (IOException e) {
                throw new JocImportException(e);
            }
        }
        return alreadyExist;
    }
    
    private static Set<ConfigurationObject> getUsedWorkflowsFromArchiveByWorkflowName (String name, List<ConfigurationObject> configurations) {
        if(configurations == null) {
            return Collections.emptySet();
        }
        Predicate<String> hasWorkflow = Pattern.compile("\"workflowName\"\\s*:\\s*\"" + name + "\"").asPredicate();
        return configurations.stream().filter(item -> ConfigurationType.WORKFLOW.equals(item.getObjectType()))
                .map(item -> {
                    try {
                        if (hasWorkflow.test(Globals.objectMapper.writeValueAsString(item.getConfiguration()))) {
                            return item;
                        }
                    } catch (JsonProcessingException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private static Set<ConfigurationObject> getUsedWorkflowsFromArchiveByLockId (String name, List<ConfigurationObject> configurations) {
        if(configurations == null) {
            return Collections.emptySet();
        }
        Predicate<String> hasLock = Pattern.compile("\"lockName\"\\s*:\\s*\"" + name + "\"").asPredicate();
        return configurations.stream().map(item -> {
                    try {
                        if (hasLock.test(Globals.objectMapper.writeValueAsString(item.getConfiguration()))) {
                            return item;
                        }
                    } catch (JsonProcessingException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private static Set<ConfigurationObject> getUsedWorkflowsFromArchiveByBoardName (String name, List<ConfigurationObject> configurations) {
        Predicate<String> hasNoticeBoard = Pattern.compile("\"(?:noticeB|b)oardName\"\\s*:\\s*\"" + name + "\"").asPredicate();
        if(configurations == null) {
            return Collections.emptySet();
        }
        return configurations.stream().map(item -> {
                    try {
                        Workflow wf = (Workflow)item.getConfiguration();
                        String wfJson = Globals.objectMapper.writeValueAsString(wf);
                        if (hasNoticeBoard.test(wfJson)) {
                            return item;
                        }
                        if (HAS_NOTICE_BOARDS.test(wfJson)) {
                            if(WorkflowsHelper.hasBoard(name, wf.getInstructions())) {
                                return item;
                            }
                        }
                    } catch (JsonProcessingException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toSet());
    }
    
    private static Set<ConfigurationObject> getUsedWorkflowsFromArchiveByJobResourceName (String name, List<ConfigurationObject> configurations) {
        if(configurations == null) {
            return Collections.emptySet();
        }
        return configurations.stream().map(item -> {
                    Workflow wf = (Workflow) item.getConfiguration();
                    if (wf.getJobResourceNames() != null && wf.getJobResourceNames().contains(name)) {
                        return item;
                    }
                    if (wf.getJobs() != null && wf.getJobs().getAdditionalProperties() != null) {
                        for (Job job : wf.getJobs().getAdditionalProperties().values()) {
                            if (job.getJobResourceNames() != null && job.getJobResourceNames().contains(name)) {
                                return item;
                            }
                        }
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private static Set<ConfigurationObject> getUsedJobTemplatesFromArchiveByJobResourcesName (String name, List<ConfigurationObject> configurations) {
        if(configurations == null) {
            return Collections.emptySet();
        }
        return configurations.stream().map(item -> {
            JobTemplate jt = (JobTemplate)item.getConfiguration();
            if (jt.getJobResourceNames() != null && jt.getJobResourceNames().contains(name)) {
                for (int i = 0; i < jt.getJobResourceNames().size(); i++) {
                    if (jt.getJobResourceNames().get(i).equals(name)) {
                        return item;
                    }
                }
            }
            return null;
            
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    
    private static Set<ConfigurationObject> getUsedWorkflowsFromArchiveByJobTemplateName (String name, List<ConfigurationObject> configurations) {
        if(configurations == null) {
            return Collections.emptySet();
        }
        return configurations.stream().map(item -> {
                    Workflow wf = (Workflow) item.getConfiguration();
                    if (wf.getJobs() != null && wf.getJobs().getAdditionalProperties() != null) {
                        for (Job job : wf.getJobs().getAdditionalProperties().values()) {
                            if (job.getJobTemplate() != null && job.getJobTemplate().getName() != null && job.getJobTemplate().getName().equals(
                                    name)) {
                                return item;
                            }
                        }
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toSet());
    }
    
    private static Set<ConfigurationObject> getUsedWorkflowsFromArchiveByIncludeScriptName (String name, List<ConfigurationObject> configurations) {
        if(configurations == null) {
            return Collections.emptySet();
        }
        Predicate<String> hasScriptInclude = Pattern.compile(JsonConverter.scriptIncludeComments + JsonConverter.scriptInclude + "[ \t]+" + name
                + "\\s*").asPredicate();
        return configurations.stream().map(item -> {
                    try {
                        if (hasScriptInclude.test(Globals.objectMapper.writeValueAsString(item.getConfiguration()))) {
                            return item;
                        }
                    } catch (JsonProcessingException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private static Set<ConfigurationObject> getUsedFileOrderSourcesFromArchiveByWorkflowName (String name, List<ConfigurationObject> configurations) {
//        Set<ConfigurationObject> returnValues = new HashSet<ConfigurationObject>();
//        for(ConfigurationObject item : configurations) {
//            if (ConfigurationType.FILEORDERSOURCE.equals(item.getObjectType()) && ((FileOrderSourceEdit)item).getConfiguration().getWorkflowName().equals(name)) {
//                returnValues.add(item);
//            }
//        }
//        return returnValues;
        if(configurations == null) {
            return Collections.emptySet();
        }
        return configurations.stream().filter(item -> ConfigurationType.FILEORDERSOURCE.equals(item.getObjectType()) 
                && ((FileOrderSourceEdit) item).getConfiguration().getWorkflowName().equals(name)).collect(Collectors.toSet());
    }

    private static Set<ConfigurationObject> getUsedSchedulesFromArchiveByWorkflowName (String name, List<ConfigurationObject> configurations) {
        if(configurations == null) {
            return Collections.emptySet();
        }
        return configurations.stream()
                .filter(item -> (((ScheduleEdit)item).getConfiguration().getWorkflowNames() != null 
                    && ((ScheduleEdit)item).getConfiguration().getWorkflowNames().contains(name))
                        || name.equals(((ScheduleEdit)item).getConfiguration().getWorkflowName()))
                .collect(Collectors.toSet());
    }

    private static Set<ConfigurationObject> getUsedSchedulesFromArchiveByCalendarName(String name, List<ConfigurationObject> configurations) {
        if (configurations == null) {
            return Collections.emptySet();
        }
        return configurations.stream().map(item -> {
            if (ConfigurationType.SCHEDULE.equals(item.getObjectType())) {
                List<AssignedCalendars> assignedCalendars = ((ScheduleEdit) item).getConfiguration().getCalendars();
                List<AssignedNonWorkingDayCalendars> assignedNonWorkingDaysCalendars = ((ScheduleEdit) item).getConfiguration()
                        .getNonWorkingDayCalendars();
                if (assignedCalendars != null) {
                    for (AssignedCalendars calendar : assignedCalendars) {
                        if (calendar.getCalendarName().equals(name)) {
                            return item;
                        }
                    }
                }
                if (assignedNonWorkingDaysCalendars != null) {
                    for (AssignedNonWorkingDayCalendars calendar : assignedNonWorkingDaysCalendars) {
                        if (calendar.getCalendarName().equals(name)) {
                            return item;
                        }
                    }
                }
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }
    
    public static List<ConfigurationType> getImportOrder() {
        return Arrays.asList(ConfigurationType.REPORT, ConfigurationType.LOCK, ConfigurationType.NOTICEBOARD, ConfigurationType.JOBRESOURCE,
                ConfigurationType.INCLUDESCRIPT, ConfigurationType.NONWORKINGDAYSCALENDAR, ConfigurationType.WORKINGDAYSCALENDAR,
                ConfigurationType.JOBTEMPLATE, ConfigurationType.WORKFLOW, ConfigurationType.FILEORDERSOURCE, ConfigurationType.SCHEDULE);
    }

    public static Map<ControllerObject, SignaturePath> readZipFileContentWithSignatures(InputStream inputStream, JocMetaInfo jocMetaInfo)
            throws DBConnectionRefusedException, DBInvalidDataException, SOSHibernateException, IOException, JocUnsupportedFileTypeException,
            JocConfigurationException, DBOpenSessionException {
        Set<ControllerObject> objects = new HashSet<ControllerObject>();
        Set<SignaturePath> signaturePaths = new HashSet<SignaturePath>();
        Map<ControllerObject, SignaturePath> objectsWithSignature = new HashMap<ControllerObject, SignaturePath>();
        Set<String> notImported = new HashSet<String>();
        ZipInputStream zipStream = null;
        try {
            zipStream = new ZipInputStream(inputStream);
            ZipEntry entry = null;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = entry.getName().replace('\\', '/');
                String filename = Paths.get(entryName).getFileName().toString();
                if(!SOSCheckJavaVariableName.test(filename)) {
                    notImported.add(filename);
                    continue;
                }
                ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
                byte[] binBuffer = new byte[8192];
                int binRead = 0;
                while ((binRead = zipStream.read(binBuffer, 0, 8192)) >= 0) {
                    outBuffer.write(binBuffer, 0, binRead);
                }
                // process JOC meta info file
                if (entryName.equals(JOC_META_INFO_FILENAME)) {
                    JocMetaInfo fromFile = Globals.prettyPrintObjectMapper.readValue(outBuffer.toString(), JocMetaInfo.class);
                    if (!isJocMetaInfoNullOrEmpty(fromFile)) {
                        jocMetaInfo.setJocVersion(fromFile.getJocVersion());
                        jocMetaInfo.setInventorySchemaVersion(fromFile.getInventorySchemaVersion());
                        jocMetaInfo.setApiVersion(fromFile.getApiVersion());
                        jocMetaInfo.setVersionId(fromFile.getVersionId());
                    }
                }
                // process deployables only
                ControllerObject fromArchive = createControllerObjectFromArchiveFileEntry(outBuffer, entryName);
                if (fromArchive != null) {
                    objects.add(fromArchive);
                }
                SignaturePath signaturePath = createSignatureFromArchiveFileEntry(outBuffer, entryName);
                if (signaturePath != null) {
                    signaturePaths.add(signaturePath);
                }
            }
            objects.stream().forEach(item -> objectsWithSignature.put(item, null));
//            objects.stream().forEach(item -> {
//                objectsWithSignature.put(item, signaturePaths.stream().filter(item2 -> item2.getObjectPath().equals(item.getPath())).findFirst()
//                        .get());
//            });
            for(ControllerObject item : objects) {
                for (SignaturePath item2 : signaturePaths) {
                    if(item2.getObjectPath().equals(item.getPath())) {
                        objectsWithSignature.put(item, item2);
                        break;
                    }
                }
            }
        } finally {
            if (zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {
                }
            }
            if(!notImported.isEmpty()) {
                LOGGER.warn("The following files were not imported, as the filenames do not comply to the JS7 naming rules.");
                LOGGER.warn(String.format("%1$s", notImported.toString()));
            }
        }
        return objectsWithSignature;
    }

    public static ArchiveValues readZipFileContent(InputStream inputStream, JocMetaInfo jocMetaInfo) throws DBConnectionRefusedException,
            DBInvalidDataException, SOSHibernateException, IOException, JocUnsupportedFileTypeException, JocConfigurationException,
            DBOpenSessionException {
        ArchiveValues values = new ArchiveValues(); 
        ExportedTags tagsFromArchive = null;
        Set<ConfigurationObject> objects = new HashSet<ConfigurationObject>();
        ZipInputStream zipStream = null;
        try {
            zipStream = new ZipInputStream(inputStream);
            ZipEntry entry = null;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    try {
                        SOSCheckJavaVariableName.test("folder", Paths.get(entry.getName()).getFileName().toString());
                    } catch (IllegalArgumentException e) {
                        throw new JocImportException("import rejected: " + e.getMessage());
                    }
                    continue;
                }
                String entryName = entry.getName().replace('\\', '/');
                String filename = Paths.get(entryName).getFileName().toString();
                filename = filename.replaceFirst("\\.[^.]+\\.json$", "");
                try {
                    SOSCheckJavaVariableName.test("filename", filename);
                } catch (IllegalArgumentException e) {
                    throw new JocImportException("import rejected: " + e.getMessage());
                }
                ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
                byte[] binBuffer = new byte[8192];
                int binRead = 0;
                while ((binRead = zipStream.read(binBuffer, 0, 8192)) >= 0) {
                    outBuffer.write(binBuffer, 0, binRead);
                }
                // process JOC meta info file
                if (entryName.equals(JOC_META_INFO_FILENAME)) {
                    JocMetaInfo fromFile = Globals.objectMapper.readValue(outBuffer.toByteArray(), JocMetaInfo.class);
                    if (!isJocMetaInfoNullOrEmpty(fromFile)) {
                        jocMetaInfo.setJocVersion(fromFile.getJocVersion());
                        jocMetaInfo.setInventorySchemaVersion(fromFile.getInventorySchemaVersion());
                        jocMetaInfo.setApiVersion(fromFile.getApiVersion());
                        jocMetaInfo.setVersionId(fromFile.getVersionId());
                    }
                }
                if (entryName.equals(ExportUtils.TAGS_ENTRY_NAME)) {
                    tagsFromArchive = Globals.objectMapper.readValue(outBuffer.toString(), ExportedTags.class);
                } else if (entryName.equals(ExportUtils.TAGS_ENTRY_OLD_NAME)) {
                    tagsFromArchive = Globals.objectMapper.readValue(outBuffer.toString(), ExportedTags.class);
                }
                ConfigurationObject fromArchive = createConfigurationObjectFromArchiveFileEntry(outBuffer, entryName);
                if (fromArchive != null) {
                    objects.add(fromArchive);
                }
            }
        } finally {
            if (zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {
                }
            }
        }
        values.setConfigurations(objects);
        values.setTags(tagsFromArchive);
        return values;
    }

    public static Map<ControllerObject, SignaturePath> readTarGzipFileContentWithSignatures(InputStream inputStream, JocMetaInfo jocMetaInfo)
            throws DBConnectionRefusedException, DBInvalidDataException, SOSHibernateException, IOException, JocUnsupportedFileTypeException,
            JocConfigurationException, DBOpenSessionException {
        Set<ControllerObject> objects = new HashSet<ControllerObject>();
        Set<SignaturePath> signaturePaths = new HashSet<SignaturePath>();
        Map<ControllerObject, SignaturePath> objectsWithSignature = new HashMap<ControllerObject, SignaturePath>();
        GZIPInputStream gzipInputStream = null;
        TarArchiveInputStream tarArchiveInputStream = null;
        Set<String> notImported = new HashSet<String>();
        try {
            gzipInputStream = new GZIPInputStream(inputStream);
            tarArchiveInputStream = new TarArchiveInputStream(gzipInputStream);
            ArchiveEntry entry = null;
            while ((entry = tarArchiveInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = entry.getName().replace('\\', '/');
                String filename = Paths.get(entryName).getFileName().toString();
                if(!SOSCheckJavaVariableName.test(filename)) {
                    notImported.add(filename);
                    continue;
                }
                ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
                byte[] binBuffer = new byte[8192];
                int binRead = 0;
                while ((binRead = tarArchiveInputStream.read(binBuffer, 0, 8192)) >= 0) {
                    outBuffer.write(binBuffer, 0, binRead);
                }
                // process JOC meta info file
                if (entryName.equals(JOC_META_INFO_FILENAME)) {
                    JocMetaInfo fromFile = Globals.prettyPrintObjectMapper.readValue(outBuffer.toString(), JocMetaInfo.class);
                    if (!isJocMetaInfoNullOrEmpty(fromFile)) {
                        jocMetaInfo.setJocVersion(fromFile.getJocVersion());
                        jocMetaInfo.setInventorySchemaVersion(fromFile.getInventorySchemaVersion());
                        jocMetaInfo.setApiVersion(fromFile.getApiVersion());
                        jocMetaInfo.setVersionId(fromFile.getVersionId());
                    }
                }
                // process deployables only
                ControllerObject fromArchive = createControllerObjectFromArchiveFileEntry(outBuffer, entryName);
                if (fromArchive != null) {
                    objects.add(fromArchive);
                }
                SignaturePath signaturePath = createSignatureFromArchiveFileEntry(outBuffer, entryName);
                if (signaturePath != null) {
                    signaturePaths.add(signaturePath);
                }
            }
            objects.stream().forEach(item -> objectsWithSignature.put(item, null));
            for(ControllerObject item : objects) {
                for (SignaturePath item2 : signaturePaths) {
                    if(item2.getObjectPath().equals(item.getPath())) {
                        objectsWithSignature.put(item, item2);
                        break;
                    }
                }
            }
//            objects.stream().forEach(item -> {
//                objectsWithSignature.put(item, signaturePaths.stream().filter(item2 -> item2.getObjectPath().equals(item.getPath())).findFirst()
//                        .get());
//            });
        } finally {
            try {
                if (tarArchiveInputStream != null) {
                    tarArchiveInputStream.close();
                }
                if (gzipInputStream != null) {
                    gzipInputStream.close();
                }
            } catch (Exception e) {
            }
            if(!notImported.isEmpty()) {
                LOGGER.warn("The following files were not imported, as the filenames do not comply to the JS7 naming rules.");
                LOGGER.warn(String.format("%1$s", notImported.toString()));
            }
        }
        return objectsWithSignature;
    }

    public static ArchiveValues readTarGzipFileContent(InputStream inputStream, JocMetaInfo jocMetaInfo)
            throws DBConnectionRefusedException, DBInvalidDataException, SOSHibernateException, IOException, JocUnsupportedFileTypeException,
            JocConfigurationException, DBOpenSessionException {
        Set<ConfigurationObject> objects = new HashSet<ConfigurationObject>();
        GZIPInputStream gzipInputStream = null;
        TarArchiveInputStream tarArchiveInputStream = null;
        ArchiveValues values = new ArchiveValues(); 
        ExportedTags tagsFromArchive = null;
        try {
            gzipInputStream = new GZIPInputStream(inputStream);
            tarArchiveInputStream = new TarArchiveInputStream(gzipInputStream);
            ArchiveEntry entry = null;
            while ((entry = tarArchiveInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    try {
                        SOSCheckJavaVariableName.test("folder", Paths.get(entry.getName()).getFileName().toString());
                    } catch (IllegalArgumentException e) {
                        throw new JocImportException("import rejected.", e);
                    }
                    continue;
                }
                String entryName = entry.getName().replace('\\', '/');
                String filename = Paths.get(entryName).getFileName().toString();
                try {
                    SOSCheckJavaVariableName.test("filename", filename);
                } catch (IllegalArgumentException e) {
                    throw new JocImportException("import rejected.", e);
                }
                ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
                byte[] binBuffer = new byte[8192];
                int binRead = 0;
                while ((binRead = tarArchiveInputStream.read(binBuffer, 0, 8192)) >= 0) {
                    outBuffer.write(binBuffer, 0, binRead);
                }
                // process JOC meta info file
                if (entryName.equals(JOC_META_INFO_FILENAME)) {
                    JocMetaInfo fromFile = Globals.objectMapper.readValue(outBuffer.toString(), JocMetaInfo.class);
                    if (!isJocMetaInfoNullOrEmpty(fromFile)) {
                        jocMetaInfo.setJocVersion(fromFile.getJocVersion());
                        jocMetaInfo.setInventorySchemaVersion(fromFile.getInventorySchemaVersion());
                        jocMetaInfo.setApiVersion(fromFile.getApiVersion());
                        jocMetaInfo.setVersionId(fromFile.getVersionId());
                    }
                }
                if (entryName.equals(ExportUtils.TAGS_ENTRY_NAME)) {
                    tagsFromArchive = Globals.objectMapper.readValue(outBuffer.toString(), ExportedTags.class);
                } else if (entryName.equals(ExportUtils.TAGS_ENTRY_OLD_NAME)) {
                    tagsFromArchive = Globals.objectMapper.readValue(outBuffer.toString(), ExportedTags.class);
                }
                ConfigurationObject fromArchive = createConfigurationObjectFromArchiveFileEntry(outBuffer, entryName);
                if (fromArchive != null) {
                    objects.add(fromArchive);
                }
            }
        } finally {
            try {
                if (tarArchiveInputStream != null) {
                    tarArchiveInputStream.close();
                }
                if (gzipInputStream != null) {
                    gzipInputStream.close();
                }
            } catch (Exception e) {
            }
        }
        values.setConfigurations(objects);
        values.setTags(tagsFromArchive);
        return values;
    }

    private static ConfigurationObject createConfigurationObjectFromArchiveFileEntry(ByteArrayOutputStream outBuffer, String entryName)
            throws JsonParseException, JsonMappingException, IOException {
        // process deployables and releaseables
        if (entryName.endsWith(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.value())) {
            String normalizedPath = Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.value(), ""));
            if (normalizedPath.startsWith("//")) {
                normalizedPath = normalizedPath.substring(1);
            }
            WorkflowEdit workflowEdit = new WorkflowEdit();
            com.sos.inventory.model.workflow.Workflow workflow = (com.sos.inventory.model.workflow.Workflow) JocInventory.content2IJSObject(outBuffer
                    .toString(StandardCharsets.UTF_8), ConfigurationType.WORKFLOW);
            //workflow = JsonSerializer.emptyValuesToNull(workflow);
            if (checkObjectNotEmpty(workflow)) {
                workflowEdit.setConfiguration(workflow);
            } else {
                throw new JocImportException(String.format("Workflow with path %1$s not imported. Object values could not be mapped.",
                        normalizedPath));
            }
            workflowEdit.setName(Paths.get(normalizedPath).getFileName().toString());
            workflowEdit.setPath(normalizedPath);
            workflowEdit.setObjectType(ConfigurationType.WORKFLOW);
            return workflowEdit;
        } else if (entryName.endsWith(ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.value())) {
            String normalizedPath = Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.value(),
                    ""));
            if (normalizedPath.startsWith("//")) {
                normalizedPath = normalizedPath.substring(1);
            }
            JobResourceEdit jobResourceEdit = new JobResourceEdit();
            com.sos.inventory.model.jobresource.JobResource jobResource = (com.sos.inventory.model.jobresource.JobResource) JocInventory
                    .content2IJSObject(outBuffer.toString(StandardCharsets.UTF_8), ConfigurationType.JOBRESOURCE);
            if (checkObjectNotEmpty(jobResource)) {
                jobResourceEdit.setConfiguration(jobResource);
            } else {
                throw new JocImportException(String.format("JobResource with path %1$s not imported. Object values could not be mapped.",
                        normalizedPath));
            }
            jobResourceEdit.setName(Paths.get(normalizedPath).getFileName().toString());
            jobResourceEdit.setPath(normalizedPath);
            jobResourceEdit.setObjectType(ConfigurationType.JOBRESOURCE);
            return jobResourceEdit;
        } else if (entryName.endsWith(ControllerObjectFileExtension.LOCK_FILE_EXTENSION.value())) {
            String normalizedPath = Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.LOCK_FILE_EXTENSION.value(), ""));
            if (normalizedPath.startsWith("//")) {
                normalizedPath = normalizedPath.substring(1);
            }
            LockEdit lockEdit = new LockEdit();
            com.sos.inventory.model.lock.Lock lock = (com.sos.inventory.model.lock.Lock) JocInventory.content2IJSObject(outBuffer.toString(
                    StandardCharsets.UTF_8), ConfigurationType.LOCK);
            if (checkObjectNotEmpty(lock)) {
                lockEdit.setConfiguration(lock);
            } else {
                throw new JocImportException(String.format("Lock with path %1$s not imported. Object values could not be mapped.", normalizedPath));
            }
            lockEdit.setName(Paths.get(normalizedPath).getFileName().toString());
            lockEdit.setPath(normalizedPath);
            lockEdit.setObjectType(ConfigurationType.LOCK);
            return lockEdit;
        } else if (entryName.endsWith(ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.value())) {
            String normalizedPath = Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.value(), ""));
            if (normalizedPath.startsWith("//")) {
                normalizedPath = normalizedPath.substring(1);
            }
            BoardEdit boardEdit = new BoardEdit();
            com.sos.inventory.model.board.Board board = (com.sos.inventory.model.board.Board) JocInventory.content2IJSObject(outBuffer.toString(
                    StandardCharsets.UTF_8), ConfigurationType.NOTICEBOARD);
            if (checkObjectNotEmpty(board)) {
                boardEdit.setConfiguration(board);
            } else {
                throw new JocImportException(String.format("Board with path %1$s not imported. Object values could not be mapped.", normalizedPath));
            }
            boardEdit.setName(Paths.get(normalizedPath).getFileName().toString());
            boardEdit.setPath(normalizedPath);
            boardEdit.setObjectType(ConfigurationType.NOTICEBOARD);
            return boardEdit;
        } else if (entryName.endsWith(ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.value())) {
            String normalizedPath = Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.value(), ""));
            if (normalizedPath.startsWith("//")) {
                normalizedPath = normalizedPath.substring(1);
            }
            JobClassEdit jobClassEdit = new JobClassEdit();
            com.sos.inventory.model.jobclass.JobClass jobClass = (com.sos.inventory.model.jobclass.JobClass) JocInventory.content2IJSObject(outBuffer
                    .toString(StandardCharsets.UTF_8), ConfigurationType.JOBCLASS);
            if (checkObjectNotEmpty(jobClass)) {
                jobClassEdit.setConfiguration(jobClass);
            } else {
                throw new JocImportException(String.format("JobClass with path %1$s not imported. Object values could not be mapped.",
                        normalizedPath));
            }
            jobClassEdit.setName(Paths.get(normalizedPath).getFileName().toString());
            jobClassEdit.setPath(normalizedPath);
            jobClassEdit.setObjectType(ConfigurationType.JOBCLASS);
            return jobClassEdit;
        } else if (entryName.endsWith(ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.value())) {
            String normalizedPath = Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION
                    .value(), ""));
            if (normalizedPath.startsWith("//")) {
                normalizedPath = normalizedPath.substring(1);
            }
            FileOrderSourceEdit fileOrderSourceEdit = new FileOrderSourceEdit();
            com.sos.inventory.model.fileordersource.FileOrderSource fileOrderSource =
                    (com.sos.inventory.model.fileordersource.FileOrderSource) JocInventory.content2IJSObject(outBuffer
                            .toString(StandardCharsets.UTF_8), ConfigurationType.FILEORDERSOURCE);
            //fileOrderSource = JsonSerializer.emptyValuesToNull(fileOrderSource);
            if (checkObjectNotEmpty(fileOrderSource)) {
                fileOrderSourceEdit.setConfiguration(fileOrderSource);
            } else {
                throw new JocImportException(String.format("FileOrderSource with path %1$s not imported. Object values could not be mapped.",
                        normalizedPath));
            }
            fileOrderSourceEdit.setName(Paths.get(normalizedPath).getFileName().toString());
            fileOrderSourceEdit.setPath(normalizedPath);
            fileOrderSourceEdit.setObjectType(ConfigurationType.FILEORDERSOURCE);
            return fileOrderSourceEdit;
        } else if (entryName.endsWith(ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.value())) {
            String normalizedPath = Globals.normalizePath("/" + entryName.replace(ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.value(),
                    ""));
            if (normalizedPath.startsWith("//")) {
                normalizedPath = normalizedPath.substring(1);
            }
            ScheduleEdit scheduleEdit = new ScheduleEdit();
            Schedule schedule = (Schedule) JocInventory.content2IJSObject(outBuffer.toString(StandardCharsets.UTF_8), ConfigurationType.SCHEDULE);
            if (checkObjectNotEmpty(schedule)) {
                scheduleEdit.setConfiguration(schedule);
            } else {
                throw new JocImportException(String.format("Schedule with path %1$s not imported. Object values could not be mapped.",
                        normalizedPath));
            }
            scheduleEdit.setName(Paths.get(normalizedPath).getFileName().toString());
            scheduleEdit.setPath(normalizedPath);
            scheduleEdit.setObjectType(ConfigurationType.SCHEDULE);
            return scheduleEdit;
        } else if (entryName.endsWith(ConfigurationObjectFileExtension.SCRIPT_FILE_EXTENSION.value())) {
            String normalizedPath = Globals.normalizePath("/" + entryName.replace(ConfigurationObjectFileExtension.SCRIPT_FILE_EXTENSION.value(),
                    ""));
            if (normalizedPath.startsWith("//")) {
                normalizedPath = normalizedPath.substring(1);
            }
            ScriptEdit scriptEdit = new ScriptEdit();
            Script script = (Script) JocInventory.content2IJSObject(outBuffer.toString(StandardCharsets.UTF_8), ConfigurationType.INCLUDESCRIPT);
            if (checkObjectNotEmpty(script)) {
                scriptEdit.setConfiguration(script);
            } else {
                throw new JocImportException(String.format("Script with path %1$s not imported. Object values could not be mapped.",
                        normalizedPath));
            }
            scriptEdit.setName(Paths.get(normalizedPath).getFileName().toString());
            scriptEdit.setPath(normalizedPath);
            scriptEdit.setObjectType(ConfigurationType.INCLUDESCRIPT);
            return scriptEdit;
        } else if (entryName.endsWith(ConfigurationObjectFileExtension.JOBTEMPLATE_FILE_EXTENSION.value())) {
            String normalizedPath = Globals.normalizePath("/" + entryName.replace(ConfigurationObjectFileExtension.JOBTEMPLATE_FILE_EXTENSION.value(),
                    ""));
            if (normalizedPath.startsWith("//")) {
                normalizedPath = normalizedPath.substring(1);
            }
            JobEdit jobEdit = new JobEdit();
            JobTemplate jobTemplate = (JobTemplate) JocInventory.content2IJSObject(outBuffer.toString(StandardCharsets.UTF_8),
                    ConfigurationType.JOBTEMPLATE);
            if (checkObjectNotEmpty(jobTemplate)) {
                jobEdit.setConfiguration(jobTemplate);
            } else {
                throw new JocImportException(String.format("Job template with path %1$s not imported. Object values could not be mapped.",
                        normalizedPath));
            }
            jobEdit.setName(Paths.get(normalizedPath).getFileName().toString());
            jobEdit.setPath(normalizedPath);
            jobEdit.setObjectType(ConfigurationType.JOBTEMPLATE);
            return jobEdit;
        } else if (entryName.endsWith(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.value())) {
            String normalizedPath = Globals.normalizePath("/" + entryName.replace(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.value(),
                    ""));
            if (normalizedPath.startsWith("//")) {
                normalizedPath = normalizedPath.substring(1);
            }
            Calendar cal = (Calendar) JocInventory.content2IJSObject(outBuffer.toString(StandardCharsets.UTF_8),
                    ConfigurationType.WORKINGDAYSCALENDAR);
            if (checkObjectNotEmpty(cal)) {
                if (CalendarType.WORKINGDAYSCALENDAR.equals(cal.getType())) {
                    WorkingDaysCalendarEdit wdcEdit = new WorkingDaysCalendarEdit();
                    wdcEdit.setConfiguration(cal);
                    wdcEdit.setName(Paths.get(normalizedPath).getFileName().toString());
                    wdcEdit.setPath(normalizedPath);
                    wdcEdit.setObjectType(ConfigurationType.WORKINGDAYSCALENDAR);
                    return wdcEdit;
                } else if (CalendarType.NONWORKINGDAYSCALENDAR.equals(cal.getType())) {
                    NonWorkingDaysCalendarEdit nwdcEdit = new NonWorkingDaysCalendarEdit();
                    nwdcEdit.setConfiguration(cal);
                    nwdcEdit.setName(Paths.get(normalizedPath).getFileName().toString());
                    nwdcEdit.setPath(normalizedPath);
                    nwdcEdit.setObjectType(ConfigurationType.NONWORKINGDAYSCALENDAR);
                    return nwdcEdit;
                }
            } else {
                throw new JocImportException(String.format("Calendar with path %1$s not imported. Object values could not be mapped.", ("/"
                        + entryName).replace(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.value(), "")));
            }
        } else if (entryName.endsWith(ConfigurationObjectFileExtension.REPORT_FILE_EXTENSION.value())) {
            String normalizedPath = Globals.normalizePath("/" + entryName.replace(ConfigurationObjectFileExtension.REPORT_FILE_EXTENSION.value(),
                    ""));
            if (normalizedPath.startsWith("//")) {
                normalizedPath = normalizedPath.substring(1);
            }
            ReportEdit reportEdit = new ReportEdit();
            Report report = (Report) JocInventory.content2IJSObject(outBuffer.toString(StandardCharsets.UTF_8), ConfigurationType.REPORT);
            if (checkObjectNotEmpty(report)) {
                reportEdit.setConfiguration(report);
            } else {
                throw new JocImportException(String.format("Report with path %1$s not imported. Object values could not be mapped.",
                        normalizedPath));
            }
            reportEdit.setName(Paths.get(normalizedPath).getFileName().toString());
            reportEdit.setPath(normalizedPath);
            reportEdit.setObjectType(ConfigurationType.REPORT);
            return reportEdit;
        }
        return null;
    }

    private static boolean checkObjectNotEmpty(Workflow workflow) {
        if (workflow != null && workflow.getInstructions() == null && workflow.getJobs() == null && workflow.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(Script script) {
        if (script != null && script.getScript() == null) {
            return false;
        } else {
            return true;
        }
    }
    
    private static boolean checkObjectNotEmpty(JobTemplate job) {
        if (job != null && job.getExecutable() == null) {
            return false;
        } else {
            return true;
        }
    }
    
    private static boolean checkObjectNotEmpty(Report report) {
        if (report != null && report.getFrequencies() == null && report.getTemplateName() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(com.sos.sign.model.workflow.Workflow workflow) {
        if (workflow != null && workflow.getInstructions() == null && workflow.getJobs() == null && workflow.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(JobResource jobResource) {
        if (jobResource != null && jobResource.getEnv() == null && jobResource.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(com.sos.inventory.model.jobresource.JobResource jobResource) {
        if (jobResource != null && jobResource.getDocumentationName() == null && jobResource.getEnv() == null && jobResource.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(Board board) {
        if (board == null) {
            return false;
        }
        if (board.getTYPE() == null) {
            return false;
        }
        if (board.getTYPE().equals(DeployType.PLANNABLEBOARD)) {
            return true;
        } else if (board.getEndOfLife() == null && (board.getExpectOrderToNoticeId() == null || board.getPostOrderToNoticeId() == null)) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(com.sos.inventory.model.board.Board board) {
        if (board == null) {
            return false;
        }
        if (board.getBoardType() == null) {
            return false;
        }
        if (board.getBoardType().equals(BoardType.PLANNABLE)) {
            return true;
        } else if (board.getEndOfLife() == null && (board.getExpectOrderToNoticeId() == null || board.getPostOrderToNoticeId() == null)) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(JobClass jobClass) {
        if (jobClass != null && jobClass.getMaxProcesses() == null && jobClass.getPriority() == null && jobClass.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(com.sos.inventory.model.jobclass.JobClass jobClass) {
        if (jobClass != null && jobClass.getDocumentationName() == null && jobClass.getMaxProcesses() == null && jobClass.getPriority() == null
                && jobClass.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(FileOrderSource fileOrderSource) {
        if (fileOrderSource != null && fileOrderSource.getAgentPath() == null && fileOrderSource.getDelay() == null && fileOrderSource
                .getTYPE() == null && fileOrderSource.getPattern() == null && fileOrderSource.getWorkflowPath() == null && fileOrderSource
                        .getDirectory() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(com.sos.inventory.model.fileordersource.FileOrderSource fileOrderSource) {
        if (fileOrderSource != null && fileOrderSource.getDocumentationName() == null && fileOrderSource.getAgentName() == null && fileOrderSource
                .getDelay() == null && fileOrderSource.getTYPE() == null && fileOrderSource.getPattern() == null && fileOrderSource
                        .getWorkflowName() == null && fileOrderSource.getDirectory() == null && fileOrderSource.getDirectoryExpr() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(Lock lock) {
        if (lock != null && lock.getLimit() == null && lock.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(com.sos.inventory.model.lock.Lock lock) {
        if (lock != null && lock.getDocumentationName() == null && lock.getLimit() == null && lock.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(Schedule schedule) {
        if (schedule != null && schedule.getDocumentationName() == null && schedule.getPlanOrderAutomatically() == null && schedule.getPath() == null
                && schedule.getCalendars() == null && schedule.getWorkflowName() == null && schedule.getWorkflowNames() == null && schedule
                        .getSubmitOrderToControllerWhenPlanned() == null && schedule.getNonWorkingDayCalendars() == null && schedule
                                .getOrderParameterisations() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(Calendar calendar) {
        if (calendar != null && calendar.getDocumentationName() == null && calendar.getExcludes() == null && calendar.getPath() == null && calendar
                .getFrom() == null && calendar.getIncludes() == null && calendar.getName() == null && calendar.getTo() == null && calendar
                        .getType() == null) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isJocMetaInfoNullOrEmpty(JocMetaInfo jocMetaInfo) {
        if (jocMetaInfo == null || ((jocMetaInfo.getJocVersion() == null || jocMetaInfo.getJocVersion().isEmpty()) && (jocMetaInfo
                .getInventorySchemaVersion() == null || jocMetaInfo.getInventorySchemaVersion().isEmpty()) && (jocMetaInfo.getApiVersion() == null
                        || jocMetaInfo.getApiVersion().isEmpty() && (jocMetaInfo.getVersionId() == null || jocMetaInfo.getVersionId().isEmpty())))) {
            return true;
        } else {
            return false;
        }
    }
    
    public static void validateAndUpdate (List<DBItemInventoryConfiguration> storedConfigurations, Set<String> agentNames,
            SOSHibernateSession session) {
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        storedConfigurations.stream()
            .forEach(cfg -> {
                boolean wasValid = cfg.getValid();
                cfg.setValid(validateConfiguration(cfg, agentNames, dbLayer));
                if(wasValid != cfg.getValid()) {
                    try {
                        JocInventory.updateConfiguration(dbLayer, cfg);
//                        session.update(cfg);
                    } catch (Throwable e) {
                        throw new JocSosHibernateException(e);
                    }
                }});
    }
    
    private static ControllerObject createControllerObjectFromArchiveFileEntry(ByteArrayOutputStream outBuffer, String entryName)
            throws JsonParseException, JsonMappingException, IOException {
        String importedJson = outBuffer.toString(StandardCharsets.UTF_8.displayName());
        if (entryName.endsWith(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.value())) {
            WorkflowPublish workflowPublish = new WorkflowPublish();
            com.sos.sign.model.workflow.Workflow workflow = Globals.objectMapper.readValue(importedJson, com.sos.sign.model.workflow.Workflow.class);
            workflowPublish.setSignedContent(importedJson);
            if (checkObjectNotEmpty(workflow)) {
                workflowPublish.setContent(workflow);
            } else {
                throw new JocImportException(String.format("Workflow with path %1$s not imported. Object values could not be mapped.", Globals
                        .normalizePath("/" + entryName.replace(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.value(), ""))));
            }
            workflowPublish.setPath(Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.value(),
                    "")));
            workflowPublish.setObjectType(DeployType.WORKFLOW);
            return workflowPublish;
        } else if (entryName.endsWith(ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.value())) {
            JobResourcePublish jobResourcePublish = new JobResourcePublish();
            JobResource jobResource = Globals.objectMapper.readValue(importedJson, JobResource.class);
            jobResourcePublish.setSignedContent(importedJson);
            if (checkObjectNotEmpty(jobResource)) {
                jobResourcePublish.setContent(jobResource);
            } else {
                throw new JocImportException(String.format("JobResource with path %1$s not imported. Object values could not be mapped.", Globals
                        .normalizePath("/" + entryName.replace(ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.value(), ""))));
            }
            jobResourcePublish.setPath(Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.value(),
                    "")));
            jobResourcePublish.setObjectType(DeployType.JOBRESOURCE);
            return jobResourcePublish;
        } else if (entryName.endsWith(ControllerObjectFileExtension.LOCK_FILE_EXTENSION.value())) {
            LockPublish lockPublish = new LockPublish();
            Lock lock = Globals.objectMapper.readValue(importedJson, Lock.class);
            lockPublish.setSignedContent(importedJson);
            if (checkObjectNotEmpty(lock)) {
                lockPublish.setContent(lock);
            } else {
                throw new JocImportException(String.format("Lock with path %1$s not imported. Object values could not be mapped.", Globals
                        .normalizePath("/" + entryName.replace(ControllerObjectFileExtension.LOCK_FILE_EXTENSION.value(), ""))));
            }
            lockPublish.setPath(Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.LOCK_FILE_EXTENSION.value(), "")));
            lockPublish.setObjectType(DeployType.LOCK);
            return lockPublish;
        } else if (entryName.endsWith(ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.value())) {
            BoardPublish boardPublish = new BoardPublish();
            Board board = Globals.objectMapper.readValue(importedJson, Board.class);
            boardPublish.setSignedContent(importedJson);
            if (checkObjectNotEmpty(board)) {
                boardPublish.setContent(board);
            } else {
                throw new JocImportException(String.format("Board with path %1$s not imported. Object values could not be mapped.", Globals
                        .normalizePath("/" + entryName.replace(ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.value(), ""))));
            }
            boardPublish.setPath(Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.value(), "")));
            boardPublish.setObjectType(DeployType.NOTICEBOARD);
            return boardPublish;
        } else if (entryName.endsWith(ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.value())) {
            JobClassPublish jobClassPublish = new JobClassPublish();
            JobClass jobClass = Globals.objectMapper.readValue(importedJson, JobClass.class);
            jobClassPublish.setSignedContent(importedJson);
            if (checkObjectNotEmpty(jobClass)) {
                jobClassPublish.setContent(jobClass);
            } else {
                throw new JocImportException(String.format("JobClass with path %1$s not imported. Object values could not be mapped.", Globals
                        .normalizePath("/" + entryName.replace(ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.value(), ""))));
            }
            jobClassPublish.setPath(Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.value(),
                    "")));
            jobClassPublish.setObjectType(DeployType.JOBCLASS);
            return jobClassPublish;
        } else if (entryName.endsWith(ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.value())) {
            FileOrderSourcePublish fileOrderSourcePublish = new FileOrderSourcePublish();
            FileOrderSource fileOrderSource = Globals.objectMapper.readValue(importedJson, FileOrderSource.class);
            fileOrderSourcePublish.setSignedContent(importedJson);
            if (checkObjectNotEmpty(fileOrderSource)) {
                fileOrderSourcePublish.setContent(fileOrderSource);
            } else {
                throw new JocImportException(String.format("FileOrderSource with path %1$s not imported. Object values could not be mapped.", Globals
                        .normalizePath("/" + entryName.replace(ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.value(), ""))));
            }
            fileOrderSourcePublish.setPath(Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION
                    .value(), "")));
            fileOrderSourcePublish.setObjectType(DeployType.FILEORDERSOURCE);
            return fileOrderSourcePublish;
        }
        return null;
    }

    private static SignaturePath createSignatureFromArchiveFileEntry(ByteArrayOutputStream outBuffer, String entryName) throws JsonParseException,
            JsonMappingException, IOException {
        SignaturePath signaturePath = new SignaturePath();
        Signature signature = new Signature();
        String sig = outBuffer.toString(); //.replaceAll("[\\r\\n]","");
        signature.setSignatureString(sig);
        signaturePath.setSignature(signature);
        if (entryName.endsWith(ControllerObjectFileExtension.WORKFLOW_PGP_SIGNATURE_FILE_EXTENSION.value())) {
            signaturePath.setObjectPath(Globals.normalizePath("/" + entryName.replace(
                    ControllerObjectFileExtension.WORKFLOW_PGP_SIGNATURE_FILE_EXTENSION.value(), "")));
            return signaturePath;
        } else if (entryName.endsWith(ControllerObjectFileExtension.WORKFLOW_X509_SIGNATURE_FILE_EXTENSION.value())) {
            signaturePath.setObjectPath(Globals.normalizePath("/" + entryName.replace(
                    ControllerObjectFileExtension.WORKFLOW_X509_SIGNATURE_FILE_EXTENSION.value(), "")));
            return signaturePath;
        } else if (entryName.endsWith(ControllerObjectFileExtension.WORKFLOW_X509_SIGNATURE_PEM_FILE_EXTENSION.value())) {
            signaturePath.setObjectPath(Globals.normalizePath("/" + entryName.replace(
                    ControllerObjectFileExtension.WORKFLOW_X509_SIGNATURE_PEM_FILE_EXTENSION.value(), "")));
            return signaturePath;
        } else if (entryName.endsWith(ControllerObjectFileExtension.JOBRESOURCE_PGP_SIGNATURE_FILE_EXTENSION.value())) {
            signaturePath.setObjectPath(Globals.normalizePath("/" + entryName.replace(
                    ControllerObjectFileExtension.JOBRESOURCE_PGP_SIGNATURE_FILE_EXTENSION.value(), "")));
            return signaturePath;
        } else if (entryName.endsWith(ControllerObjectFileExtension.JOBRESOURCE_X509_SIGNATURE_FILE_EXTENSION.value())) {
            signaturePath.setObjectPath(Globals.normalizePath("/" + entryName.replace(
                    ControllerObjectFileExtension.JOBRESOURCE_X509_SIGNATURE_FILE_EXTENSION.value(), "")));
            return signaturePath;
        } else if (entryName.endsWith(ControllerObjectFileExtension.JOBRESOURCE_X509_SIGNATURE_PEM_FILE_EXTENSION.value())) {
            signaturePath.setObjectPath(Globals.normalizePath("/" + entryName.replace(
                    ControllerObjectFileExtension.JOBRESOURCE_X509_SIGNATURE_PEM_FILE_EXTENSION.value(), "")));
            return signaturePath;
        }
        return null;
    }

    public static Set<Agent> readAgentsFromZipFileContent(InputStream inputStream) throws IOException {
        Set<Agent> agents = new HashSet<Agent>();
        ZipInputStream zipStream = null;
        try {
            zipStream = new ZipInputStream(inputStream);
            ZipEntry entry = null;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = entry.getName().replace('\\', '/');
                String filename = Paths.get(entryName).getFileName().toString();
                SOSCheckJavaVariableName.test("filename of archive entry", filename);
                if (filename.endsWith(AGENT_FILE_EXTENSION)) {
                    ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
                    byte[] binBuffer = new byte[8192];
                    int binRead = 0;
                    while ((binRead = zipStream.read(binBuffer, 0, 8192)) >= 0) {
                        outBuffer.write(binBuffer, 0, binRead);
                    }
                    Agent fromArchive = createAgentFromArchiveFileEntry(outBuffer.toByteArray(), filename);
                    if (fromArchive != null) {
                        agents.add(fromArchive);
                    }
                } else {
                    continue;
                }
            }
        } finally {
            if (zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {
                }
            }
        }
        return agents;
    }
    
    public static Set<Agent> readAgentsFromTarGzipFileContent(InputStream inputStream) throws IOException {
        Set<Agent> agents = new HashSet<Agent>();
        GZIPInputStream gzipInputStream = null;
        TarArchiveInputStream tarArchiveInputStream = null;
        try {
            gzipInputStream = new GZIPInputStream(inputStream);
            tarArchiveInputStream = new TarArchiveInputStream(gzipInputStream);
            ArchiveEntry entry = null;
            while ((entry = tarArchiveInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = entry.getName().replace('\\', '/');
                String filename = Paths.get(entryName).getFileName().toString();
                SOSCheckJavaVariableName.test("filename of archive entry", filename);
                if (filename.endsWith(AGENT_FILE_EXTENSION)) {
                    ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
                    byte[] binBuffer = new byte[8192];
                    int binRead = 0;
                    while ((binRead = tarArchiveInputStream.read(binBuffer, 0, 8192)) >= 0) {
                        outBuffer.write(binBuffer, 0, binRead);
                    }
                    Agent fromArchive = createAgentFromArchiveFileEntry(outBuffer.toByteArray(), filename);
                    if (fromArchive != null) {
                        agents.add(fromArchive);
                    }
                } else {
                    continue;
                }
            }
        } finally {
            try {
                if (tarArchiveInputStream != null) {
                    tarArchiveInputStream.close();
                }
                if (gzipInputStream != null) {
                    gzipInputStream.close();
                }
            } catch (Exception e) {
            }
        }
        return agents;
    }

    private static Agent createAgentFromArchiveFileEntry(byte[] bytes, String filename) throws IOException {
        try {
            JsonValidator.validate(bytes, URI.create(jsonSchema));
        } catch (SOSJsonSchemaException e) {
            //JOC-1984 - ignore minimum error for ordering
            if (!e.getErrors().isEmpty()) {
                e.getErrors().removeIf(vm -> vm.getPath().endsWith("ordering") && vm.getCode().equals(ValidatorTypeCode.MINIMUM.getErrorCode()));
                if (!e.getErrors().isEmpty()) {
                    throw new JocBadRequestException("Invalid JSON in " + filename + ": " + e.getMessageFromErrors());
                }
            } else {
                throw new JocBadRequestException("Invalid JSON in " + filename + ": " + e.getMessage());
            }
        }
        Agent agent = Globals.objectMapper.readValue(bytes, Agent.class);
        String agentId = filename.replace(AGENT_FILE_EXTENSION, "");
        
        if (agent.getAgentCluster() != null) {
            agent.getAgentCluster().setAgentId(agentId);
            
            if (agent.getAgentCluster().getSubagents() != null) {
                agent.getAgentCluster().getSubagents().forEach(subagent -> subagent.setAgentId(agentId));
            }
        }
        
        if (agent.getSubagentClusters() != null) {
            agent.getSubagentClusters().forEach(sac -> sac.setAgentId(agentId));
        }

        if (agent.getStandaloneAgent() != null) {
            agent.getStandaloneAgent().setAgentId(agentId);
        }
        return agent;
        
    }
    
    public static void revalidateInvalidInvConfigurations (List<DBItemInventoryConfiguration> storedConfigurations) {
        SOSHibernateSession session = Globals.createSosHibernateStatelessConnection("./inventory/import");
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        InventoryAgentInstancesDBLayer agentDbLayer = new InventoryAgentInstancesDBLayer(session);
        Set<Path> folders = new HashSet<>();
        List<Long> workflowInvIds = new ArrayList<>();
        try {
            List<DBItemInventoryConfiguration> invalidDBItems = dbLayer.getAllInvalidConfigurations();
            if (storedConfigurations != null) {
                invalidDBItems.removeAll(storedConfigurations);
            }
            Set<String> visibleAgentNames = agentDbLayer.getVisibleAgentNames();
            invalidDBItems.stream().filter(cfg -> validateConfiguration(cfg, visibleAgentNames, dbLayer)
            ).peek(cfg -> cfg.setValid(true)).forEach(cfg -> {
                try {
                    folders.add(Paths.get(cfg.getPath()).getParent());
                    if (JocInventory.isWorkflow(cfg.getType())) {
                        workflowInvIds.add(cfg.getId());
                    }
                    dbLayer.getSession().update(cfg);
                } catch (Exception e) {
                    //
                }
            });
            folders.stream().map(folder -> folder.toString().replace('\\', '/')).forEach(JocInventory::postEvent);
            // Tagging events
            if (workflowInvIds != null && !workflowInvIds.isEmpty()) {
                InventoryTagDBLayer dbTagLayer = new InventoryTagDBLayer(session);
                dbTagLayer.getTags(workflowInvIds).stream().distinct().forEach(JocInventory::postTaggingEvent);
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        } finally {
            Globals.disconnect(session);
        }
    }

    private static boolean validateConfiguration (DBItemInventoryConfiguration cfg, Set<String> visibleAgentNames, InventoryDBLayer dbLayer) {
        try {
            Validator.validate(cfg.getTypeAsEnum(), cfg.getContent().getBytes(StandardCharsets.UTF_8), dbLayer, visibleAgentNames);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static void importTags(List<DBItemInventoryConfiguration> cfgs, Map<ConfigurationType, Map<String, String>> oldNewNameMap,
            ExportedTags tagsFromArchive, Boolean overwriteTags, SOSHibernateSession session) throws SOSHibernateException {
        if (Boolean.TRUE.equals(overwriteTags)) {
            importTagsForced(cfgs, oldNewNameMap, tagsFromArchive, session);
        } else {
            importTags(cfgs, oldNewNameMap, tagsFromArchive, session);
        }
    }

    public static void importTagsForced(List<DBItemInventoryConfiguration> cfgs, Map<ConfigurationType, Map<String, String>> oldNewNameMap,
            ExportedTags tagsFromArchive, SOSHibernateSession session) throws SOSHibernateException {
        // determine group/tag assigment over all tags

        BinaryOperator<GroupedTag> merge = (gt1, gt2) -> {
            if (gt1.getGroup().equals(gt2.getGroup())) {
                return gt1;
            } else if (gt1.hasGroup() && gt2.hasGroup()) {
                throw new IllegalArgumentException("The imported tag '" + gt1.getTag() + "' has more than one group");
            } else {
                throw new IllegalArgumentException("The imported tag '" + gt1.getTag() + "' is both ungrouped and grouped");
            }
        };
        
        Collector<GroupedTag, ?, Map<String, GroupedTag>> toMapCollector = Collectors.toMap(GroupedTag::getTag, Function.identity(), merge);

        List<GroupedTag> groupedWorkflowTags = Optional.ofNullable(tagsFromArchive.getTags()).orElse(Collections.emptyList()).stream().sorted(
                Comparator.nullsLast(Comparator.comparingInt(ExportedTagItem::getOrdering))).map(ExportedTagItem::getName).filter(Objects::nonNull)
                .distinct().map(GroupedTag::new).peek(GroupedTag::testJavaNameRules).collect(Collectors.toList());

        List<GroupedTag> groupedJobTags = Optional.ofNullable(tagsFromArchive.getJobTags()).orElse(Collections.emptyList()).stream().map(
                ExportedJobTagItem::getJobs).filter(Objects::nonNull).map(ExportedJobTagItems::getAdditionalProperties).filter(Objects::nonNull).map(
                        Map::values).flatMap(Collection::stream).flatMap(Collection::stream).distinct().map(GroupedTag::new).peek(
                                GroupedTag::testJavaNameRules).collect(Collectors.toList());

        Stream<String> groupedOrderTagsStream = Optional.ofNullable(tagsFromArchive.getOrderTags()).map(ExportedOrderTags::getFileOrderSources).map(
                t -> t.stream().map(FileOrderSourceOrderTags::getTags).filter(Objects::nonNull).flatMap(Collection::stream).distinct()).orElse(Stream
                        .empty());

        groupedOrderTagsStream = Stream.concat(groupedOrderTagsStream, Optional.ofNullable(tagsFromArchive.getOrderTags()).map(
                ExportedOrderTags::getWorkflows).map(t -> t.stream().map(AddOrdersOrderTags::getAddOrderTags).filter(Objects::nonNull).map(
                        AddOrderOrderTags::getAdditionalProperties).filter(Objects::nonNull).map(Map::values).flatMap(Collection::stream).flatMap(
                                Collection::stream).distinct()).orElse(Stream.empty()));

        groupedOrderTagsStream = Stream.concat(groupedOrderTagsStream, Optional.ofNullable(tagsFromArchive.getOrderTags()).map(
                ExportedOrderTags::getSchedules).map(t -> t.stream().map(ScheduleOrderTags::getOrderParameterisations).filter(Objects::nonNull)
                        .flatMap(Collection::stream).map(OrderParameterisation::getTags).filter(Objects::nonNull).flatMap(Collection::stream)
                        .distinct()).orElse(Stream.empty()));

        List<GroupedTag> groupedOrderTags = groupedOrderTagsStream.map(GroupedTag::new).peek(GroupedTag::testJavaNameRules).collect(Collectors
                .toList());

        // -> to provide java names rules and that every tag can have at least one group
        Map<String, GroupedTag> groupedTags = Stream.of(groupedWorkflowTags, groupedJobTags, groupedOrderTags).flatMap(Collection::stream).collect(
                toMapCollector);

        if (!groupedTags.isEmpty()) {

            InventoryTagGroupDBLayer dbGroupTagLayer = new InventoryTagGroupDBLayer(session);

            Set<String> groups = groupedTags.values().stream().map(GroupedTag::getGroup).filter(Optional::isPresent).map(Optional::get).collect(
                    Collectors.toSet());
            List<DBItemInventoryTagGroup> dbGroups = groups.isEmpty() ? Collections.emptyList() : dbGroupTagLayer.getGroups(groups);
            Map<String, Long> dbGroupsMap = dbGroups.stream().collect(Collectors.toMap(DBItemInventoryTagGroup::getName,
                    DBItemInventoryTagGroup::getId));

            Date now = Date.from(Instant.now());

            // insert new groups if necessary
            if (!groups.isEmpty()) {
                int maxGroupsOrdering = dbGroupTagLayer.getMaxGroupsOrdering();
                for (String group : groups) {
                    if (!dbGroupsMap.containsKey(group)) {
                        DBItemInventoryTagGroup item = new DBItemInventoryTagGroup();
                        item.setName(group);
                        item.setModified(now);
                        item.setOrdering(++maxGroupsOrdering);
                        session.save(item);
                        dbGroupsMap.put(group, item.getId());
                    }
                }
            }
            
            // workflow tags
            InventoryTagDBLayer tagDBLayer = new InventoryTagDBLayer(session);
            Map<String, DBItemInventoryTag> updatedTags = updateTags(tagDBLayer, groupedWorkflowTags, groupedTags, dbGroupsMap, now, null);

            // job tags
            InventoryJobTagDBLayer jobTagDBLayer = new InventoryJobTagDBLayer(session);
            Map<String, DBItemInventoryJobTag> updatedJobTags = updateTags(jobTagDBLayer, groupedJobTags, groupedTags, dbGroupsMap, now, null);

            // orderTags
            InventoryOrderTagDBLayer orderTagDBLayer = new InventoryOrderTagDBLayer(session);
            Map<String, DBItemInventoryOrderTag> orderTagsWithNewGroupId = new HashMap<>();
            Map<String, DBItemInventoryOrderTag> updatedOrderTags = updateTags(orderTagDBLayer, groupedOrderTags, groupedTags, dbGroupsMap,
                    now, orderTagsWithNewGroupId);

            //history orderTags
            List<DBItemHistoryOrderTag> historyOrderTags =  OrderTags.getTagsByTagNames(orderTagsWithNewGroupId.keySet(), session);
            for (DBItemHistoryOrderTag historyOrderTag : historyOrderTags) {
                Optional<Long> newGroupId = Optional.ofNullable(orderTagsWithNewGroupId.get(historyOrderTag.getTagName())).map(
                        DBItemInventoryOrderTag::getGroupId);
                if (newGroupId.isPresent()) {
                    historyOrderTag.setGroupId(newGroupId.get());
                    // TODO ordering ???
                    session.update(historyOrderTag);
                }
            }
            
            // tagging
            DBLayerDeploy dbDepLayer = new DBLayerDeploy(session);
            Map<ConfigurationType, Map<String, DBItemInventoryConfiguration>> cfgsMap = cfgs.stream().collect(Collectors.groupingBy(
                    DBItemInventoryConfiguration::getTypeAsEnum, Collectors.toMap(DBItemInventoryConfiguration::getName, Function.identity())));
            
            // workflow tagging
            Map<DBItemInventoryConfiguration, Set<String>> tagsPerWorkflows = getTagsPerWorkflows(tagsFromArchive.getTags(), cfgsMap, oldNewNameMap,
                    dbDepLayer);
            
            for (Map.Entry<DBItemInventoryConfiguration, Set<String>> tagsPerWorkflow : tagsPerWorkflows.entrySet()) {
                List<DBItemInventoryTagging> dbTagItems = tagDBLayer.getTaggings(tagsPerWorkflow.getKey().getId());
                List<Long> tagIds = tagsPerWorkflow.getValue().stream().map(GroupedTag::new).map(GroupedTag::getTag).map(updatedTags::get).filter(
                        Objects::nonNull).map(DBItemInventoryTag::getId).collect(Collectors.toList());

                for (DBItemInventoryTagging dbTagItem : dbTagItems) {
                    if (tagIds.contains(dbTagItem.getTagId())) {
                        tagIds.remove(dbTagItem.getTagId());
                        if (tagsPerWorkflow.getKey().getName().equals(dbTagItem.getName())) {
                            continue;
                        } else { // maybe new name because of suffix/prefix during import
                            dbTagItem.setModified(now);
                            dbTagItem.setName(tagsPerWorkflow.getKey().getName());
                            session.update(dbTagItems);
                        }
                    } else {
                        session.delete(dbTagItem);
                    }
                }
                for (Long tagId : tagIds) {
                    DBItemInventoryTagging newItem = new DBItemInventoryTagging();
                    newItem.setCid(tagsPerWorkflow.getKey().getId());
                    newItem.setName(tagsPerWorkflow.getKey().getName());
                    newItem.setType(tagsPerWorkflow.getKey().getType());
                    newItem.setModified(now);
                    newItem.setTagId(tagId);
                    session.save(newItem);
                }
            }
            
            // job tagging
            Map<DBItemInventoryConfiguration, Set<JobTags>> jobTagsPerWorkflows = getJobTagsPerWorkflows(tagsFromArchive.getJobTags(), cfgsMap,
                    oldNewNameMap, dbDepLayer);
            
            for (Map.Entry<DBItemInventoryConfiguration, Set<JobTags>> jobTagsPerWorkflow : jobTagsPerWorkflows.entrySet()) {
                Set<DBItemInventoryJobTagging> dbJobTagItems = jobTagDBLayer.getTaggings(jobTagsPerWorkflow.getKey().getId());

                for (DBItemInventoryJobTagging dbJobTagItem : dbJobTagItems) {
                    session.delete(dbJobTagItem);
                }
                
                for (JobTags jts : jobTagsPerWorkflow.getValue()) {
                    for (String jobTag : jts.getJobTags()) {
                        Optional<Long> tagId = Optional.ofNullable(updatedJobTags.get(new GroupedTag(jobTag).getTag())).map(
                                DBItemInventoryJobTag::getId);
                        if (tagId.isPresent()) {
                            DBItemInventoryJobTagging newItem = new DBItemInventoryJobTagging();
                            newItem.setCid(jobTagsPerWorkflow.getKey().getId());
                            newItem.setWorkflowName(jobTagsPerWorkflow.getKey().getName());
                            newItem.setJobName(jts.getJobName());
                            newItem.setModified(now);
                            newItem.setTagId(tagId.get());
                            session.save(newItem);
                        }
                    }
                }
            }
            
            // order tagging
            // fileordersources
            updateFileOrderSources(Optional.ofNullable(tagsFromArchive.getOrderTags()).map(ExportedOrderTags::getFileOrderSources), cfgsMap,
                    oldNewNameMap, dbDepLayer, now, true);
            // schedules
            updateSchedules(Optional.ofNullable(tagsFromArchive.getOrderTags()).map(ExportedOrderTags::getSchedules), cfgsMap, oldNewNameMap,
                    dbDepLayer, now, true);
            // workflows
            updateWorkflows(Optional.ofNullable(tagsFromArchive.getOrderTags()).map(ExportedOrderTags::getWorkflows), cfgsMap, oldNewNameMap,
                    dbDepLayer, now, true);

        }
    }
    
    private static <T extends IDBItemTag> Map<String, T> updateTags(ATagDBLayer<T> dbLayer, List<GroupedTag> groupedTagsFromArchive,
            Map<String, GroupedTag> groupedTags, Map<String, Long> dbGroupsMap, Date now, Map<String, T> tagsWithNewGroupId)
            throws SOSHibernateException {
        List<T> tags = dbLayer.getAllTags(); // ordered by ordering
        List<GroupedTag> specificGroupedTags = new ArrayList<>(groupedTagsFromArchive);
        Map<String, T> updatedTags = new HashMap<>();
        for (T item : tags) {
            GroupedTag gt = groupedTags.get(item.getName());
            if (gt != null) {
                specificGroupedTags.removeAll(Collections.singleton(gt));
                Long groupId = gt.getGroup().map(dbGroupsMap::get).orElse(0L);
                if (!groupId.equals(item.getGroupId())) {
                    item.setGroupId(groupId);
                    item.setModified(now);
                    dbLayer.getSession().update(item);
                    if (tagsWithNewGroupId != null) {
                        tagsWithNewGroupId.put(item.getName(), item);
                    }
                }
                updatedTags.put(item.getName(), item);
            }
        }
        if (!specificGroupedTags.isEmpty()) { // new workflow/job/order tags if necessary
            int maxOrdering = dbLayer.getMaxOrdering();
            for (GroupedTag gt : specificGroupedTags) {
                Long groupId = gt.getGroup().map(dbGroupsMap::get).orElse(0L);
                T newItem = dbLayer.newDBItem();
                newItem.setGroupId(groupId);
                newItem.setModified(now);
                newItem.setOrdering(++maxOrdering);
                newItem.setName(gt.getTag());
                dbLayer.getSession().save(newItem);
                updatedTags.put(newItem.getName(), newItem);
            }
        }
        return updatedTags;
    }
    
    private static Map<DBItemInventoryConfiguration, Set<String>> getTagsPerWorkflows(List<ExportedTagItem> tags,
            Map<ConfigurationType, Map<String, DBItemInventoryConfiguration>> cfgsMap, Map<ConfigurationType, Map<String, String>> oldNewNameMap,
            DBLayerDeploy invDBLayer) {
        Map<DBItemInventoryConfiguration, Set<String>> tagsPerWorkflows = new HashMap<>();
        if (tags != null) {

            tags.stream().sorted(Comparator.comparingInt(ExportedTagItem::getOrdering)).forEachOrdered(tag -> {
                tag.getUsedBy().forEach(o -> {
                    if (o.getName() != null) {
                        try {
                            ConfigurationType objectType = ConfigurationType.fromValue(o.getType());
                            DBItemInventoryConfiguration conf = cfgsMap.getOrDefault(objectType, Collections.emptyMap()).get(oldNewNameMap
                                    .getOrDefault(objectType, Collections.emptyMap()).getOrDefault(o.getName(), o.getName()));
                            if (conf == null) {
                                conf = invDBLayer.getConfigurationByName(o.getName(), objectType.intValue());
                                if (conf != null) {
                                    cfgsMap.putIfAbsent(objectType, new HashMap<>());
                                    cfgsMap.get(objectType).put(conf.getName(), conf);
                                }
                            }
                            if (conf != null) {
                                tagsPerWorkflows.putIfAbsent(conf, new HashSet<>());
                                tagsPerWorkflows.get(conf).add(tag.getName());
                            }
                        } catch (IllegalArgumentException e1) {
                            //
                        }
                    }
                });
            });
        }
        return tagsPerWorkflows;
    }
    
    private static Map<DBItemInventoryConfiguration, Set<JobTags>> getJobTagsPerWorkflows(List<ExportedJobTagItem> jobTags,
            Map<ConfigurationType, Map<String, DBItemInventoryConfiguration>> cfgsMap, Map<ConfigurationType, Map<String, String>> oldNewNameMap,
            DBLayerDeploy invDBLayer) {
        Map<DBItemInventoryConfiguration, Set<JobTags>> jobTagsPerWorkflows = new HashMap<>();
        ConfigurationType objectType = ConfigurationType.WORKFLOW;
        if (jobTags != null) {
            jobTags.forEach(w -> {
                if (w.getJobs() != null && w.getJobs().getAdditionalProperties() != null && !w.getJobs().getAdditionalProperties().isEmpty() && w
                        .getName() != null) {
                    DBItemInventoryConfiguration conf = cfgsMap.getOrDefault(objectType, Collections.emptyMap()).get(oldNewNameMap
                            .getOrDefault(objectType, Collections.emptyMap()).getOrDefault(w.getName(), w.getName()));
                    if (conf == null) {
                        conf = invDBLayer.getConfigurationByName(w.getName(), objectType.intValue());
                        if (conf != null) {
                            cfgsMap.putIfAbsent(objectType, new HashMap<>());
                            cfgsMap.get(objectType).put(conf.getName(), conf);
                        }
                    }
                    if (conf != null) {
                        jobTagsPerWorkflows.put(conf, w.getJobs().getAdditionalProperties().entrySet().stream().map(e -> {
                            JobTags jt = new JobTags();
                            jt.setJobName(e.getKey());
                            jt.setJobTags(e.getValue());
                            return jt;
                        }).collect(Collectors.toSet()));
                    }
                }
            });
        }
        return jobTagsPerWorkflows;
    }
    
    private static void updateFileOrderSources(Optional<List<FileOrderSourceOrderTags>> orderTags,
            Map<ConfigurationType, Map<String, DBItemInventoryConfiguration>> cfgsMap, Map<ConfigurationType, Map<String, String>> oldNewNameMap,
            DBLayerDeploy invDBLayer, Date now, boolean overwriteTags) {
        ConfigurationType objectType = ConfigurationType.FILEORDERSOURCE;
        InventoryDBLayer dbLayer = new InventoryDBLayer(invDBLayer.getSession());
        if (orderTags.isPresent()) {
            orderTags.get().forEach(f -> {
                if (f.getTags() != null) {
                    Set<String> tagsWithoutGroup = f.getTags().stream().map(GroupedTag::new).map(GroupedTag::getTag).collect(Collectors.toSet());
                    try {
                        DBItemInventoryConfiguration conf = cfgsMap.getOrDefault(objectType, Collections.emptyMap()).get(oldNewNameMap.getOrDefault(
                                objectType, Collections.emptyMap()).getOrDefault(f.getName(), f.getName()));
                        if (conf == null || overwriteTags) { // otherwise it is already updated by "normal" import"
                            if (conf == null) {
                                conf = invDBLayer.getConfigurationByName(f.getName(), objectType.intValue());
                            }
                            if (conf != null) {
                                com.sos.inventory.model.fileordersource.FileOrderSource fos =
                                        (com.sos.inventory.model.fileordersource.FileOrderSource) JocInventory.content2IJSObject(conf.getContent(),
                                                objectType);
                                if (!tagsWithoutGroup.equals(fos.getTags())) {
                                    fos.setTags(tagsWithoutGroup);
                                    conf.setContent(JocInventory.toString(fos));
                                    conf.setModified(now);
                                    if (overwriteTags) {
                                        invDBLayer.getSession().update(conf);
                                    } else {
                                        JocInventory.updateConfiguration(dbLayer, conf, fos);
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    } catch (SOSHibernateException e) {
                        throw new JocSosHibernateException(e);
                    }
                }
            });
        }
    }
    
    private static void updateSchedules(Optional<List<ScheduleOrderTags>> orderTags,
            Map<ConfigurationType, Map<String, DBItemInventoryConfiguration>> cfgsMap, Map<ConfigurationType, Map<String, String>> oldNewNameMap,
            DBLayerDeploy invDBLayer, Date now, boolean overwriteTags) {
        
        ConfigurationType objectType = ConfigurationType.SCHEDULE;
        InventoryDBLayer dbLayer = new InventoryDBLayer(invDBLayer.getSession());
        if (orderTags.isPresent()) {
            orderTags.get().forEach(s -> {
                if (s.getOrderParameterisations() != null) {
                    Map<String, Set<String>> tagsWithoutGroupPerOrderName = s.getOrderParameterisations().stream().collect(Collectors.toMap(
                            OrderParameterisation::getOrderName, o -> o.getTags().stream().map(GroupedTag::new).map(GroupedTag::getTag).collect(
                                    Collectors.toSet()), (k1, k2) -> k1));
                    try {
                        DBItemInventoryConfiguration conf = cfgsMap.getOrDefault(objectType, Collections.emptyMap()).get(oldNewNameMap.getOrDefault(
                                objectType, Collections.emptyMap()).getOrDefault(s.getName(), s.getName()));
                        if (conf == null || overwriteTags) { // otherwise it is already updated by "normal" import"
                            if (conf == null) {
                                conf = invDBLayer.getConfigurationByName(s.getName(), objectType.intValue());
                            }
                            if (conf != null) {
                                Schedule sch = (Schedule) JocInventory.content2IJSObject(conf.getContent(), objectType);
                                AtomicBoolean b = new AtomicBoolean(false);
                                sch.getOrderParameterisations().forEach(op -> {
                                    String oldTags = String.join(",", Optional.ofNullable(op.getTags()).orElse(Collections.emptySet()));
                                    op.setTags(tagsWithoutGroupPerOrderName.get(op.getOrderName()));
                                    String newTags = String.join(",", Optional.ofNullable(op.getTags()).orElse(Collections.emptySet()));
                                    if (!oldTags.equals(newTags)) {
                                        b.set(true);
                                    }
                                });
                                if (b.get()) {
                                    conf.setContent(JocInventory.toString(sch));
                                    conf.setModified(now);
                                    if (overwriteTags) {
                                        invDBLayer.getSession().update(conf);
                                    } else {
                                        JocInventory.updateConfiguration(dbLayer, conf, sch);
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    } catch (SOSHibernateException e) {
                        throw new JocSosHibernateException(e);
                    }
                }
            });
        }
    }
    
    private static void updateWorkflows(Optional<List<AddOrdersOrderTags>> orderTags,
            Map<ConfigurationType, Map<String, DBItemInventoryConfiguration>> cfgsMap, Map<ConfigurationType, Map<String, String>> oldNewNameMap,
            DBLayerDeploy invDBLayer, Date now, boolean overwriteTags) {

        ConfigurationType objectType = ConfigurationType.WORKFLOW;
        InventoryDBLayer dbLayer = new InventoryDBLayer(invDBLayer.getSession());
        if (orderTags.isPresent()) {
            orderTags.get().forEach(w -> {
                if (w.getAddOrderTags() != null && w.getAddOrderTags().getAdditionalProperties() != null && !w.getAddOrderTags()
                        .getAdditionalProperties().isEmpty()) {
                    try {
                        DBItemInventoryConfiguration conf = cfgsMap.getOrDefault(objectType, Collections.emptyMap()).get(oldNewNameMap.getOrDefault(
                                objectType, Collections.emptyMap()).getOrDefault(w.getName(), w.getName()));
                        if (conf == null || overwriteTags) { // otherwise it is already updated by "normal" import"
                            if (conf == null) {
                                conf = invDBLayer.getConfigurationByName(w.getName(), objectType.intValue());
                            }
                            if (conf != null) {
                                Workflow wf = (Workflow) JocInventory.content2IJSObject(conf.getContent(), objectType);
                                AtomicBoolean b = new AtomicBoolean(false);
                                AtomicInteger pos = new AtomicInteger(0);
                                updateWorkflowInstructions(wf.getInstructions(), new HashMap<>(w.getAddOrderTags().getAdditionalProperties()), pos,
                                        b);
                                if (b.get()) {
                                    conf.setContent(JocInventory.toString(wf));
                                    conf.setModified(now);
                                    if (overwriteTags) {
                                        invDBLayer.getSession().update(conf);
                                        JocInventory.handleWorkflowSearch(dbLayer, wf, conf.getId());
                                    } else {
                                        JocInventory.updateConfiguration(dbLayer, conf, wf);
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    } catch (SOSHibernateException e) {
                        throw new JocSosHibernateException(e);
                    }
                }
            });
        }
    }
    
    private static void updateWorkflowInstructions(List<Instruction> insts, Map<String, LinkedHashSet<String>> orderTags, AtomicInteger pos,
            AtomicBoolean b) {
        if (insts != null && !orderTags.isEmpty()) {
        
            for (int i = 0; i < insts.size(); i++) {
                Instruction inst = insts.get(i);
                switch (inst.getTYPE()) {
                case FORK:
                    ForkJoin fj = inst.cast();
                    if (fj.getBranches() != null) {
                        for (Branch branch : fj.getBranches()) {
                            if (branch.getWorkflow() != null) {
                                updateWorkflowInstructions(branch.getWorkflow().getInstructions(), orderTags, pos, b);
                            }
                        }
                    }
                    break;
                case FORKLIST:
                    ForkList fl = inst.cast();
                    if (fl.getWorkflow() != null) {
                        updateWorkflowInstructions(fl.getWorkflow().getInstructions(), orderTags, pos, b);
                    }
                    break;
                case CONSUME_NOTICES:
                    ConsumeNotices cn = inst.cast();
                    if (cn.getSubworkflow() == null) {
                        updateWorkflowInstructions(cn.getSubworkflow().getInstructions(), orderTags, pos, b);
                    }
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    if (ie.getThen() != null) {
                        updateWorkflowInstructions(ie.getThen().getInstructions(), orderTags, pos, b);
                    }
                    if (ie.getElse() != null) {
                        updateWorkflowInstructions(ie.getElse().getInstructions(), orderTags, pos, b);
                    }
                    break;
                case CASE_WHEN:
                    CaseWhen cw = inst.cast();
                    if (cw.getCases() != null) {
                        for (When when : cw.getCases()) {
                            if (when.getThen() != null) {
                                updateWorkflowInstructions(when.getThen().getInstructions(), orderTags, pos, b);
                            }
                        }
                    }
                    if (cw.getElse() != null) {
                        updateWorkflowInstructions(cw.getElse().getInstructions(), orderTags, pos, b);
                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    if (tc.getTry() != null) {
                        updateWorkflowInstructions(tc.getTry().getInstructions(), orderTags, pos, b);
                    }
                    if (tc.getCatch() != null) {
                        updateWorkflowInstructions(tc.getCatch().getInstructions(), orderTags, pos, b);
                    }
                    break;
                case LOCK:
                    com.sos.inventory.model.instruction.Lock l = inst.cast();
                    if (l.getLockedWorkflow() != null) {
                        updateWorkflowInstructions(l.getLockedWorkflow().getInstructions(), orderTags, pos, b);
                    }
                    break;
                case CYCLE:
                    Cycle c = inst.cast();
                    if (c.getCycleWorkflow() != null) {
                        updateWorkflowInstructions(c.getCycleWorkflow().getInstructions(), orderTags, pos, b);
                    }
                    break;
                case STICKY_SUBAGENT:
                    StickySubagent sticky = inst.cast();
                    if (sticky.getSubworkflow() != null) {
                        updateWorkflowInstructions(sticky.getSubworkflow().getInstructions(), orderTags, pos, b);
                    }
                    break;
                case OPTIONS:
                    Options opts = inst.cast();
                    if (opts.getBlock() != null) {
                        updateWorkflowInstructions(opts.getBlock().getInstructions(), orderTags, pos, b);
                    }
                    break;
                case ADMISSION_TIME:
                    AdmissionTime at = inst.cast();
                    if (at.getBlock() != null) {
                        updateWorkflowInstructions(at.getBlock().getInstructions(), orderTags, pos, b);
                    }
                    break;
                case ADD_ORDER:
                    AddOrder ao = inst.cast();
                    int index = pos.getAndIncrement();
                    String orderTagsKey = index < 10 ? "0" + index : "" + index;
                    String oldTags = String.join(",", Optional.ofNullable(ao.getTags()).orElse(Collections.emptySet()));
                    ao.setTags(Optional.ofNullable(orderTags.remove(orderTagsKey)).map(t -> t.stream().map(GroupedTag::new).map(GroupedTag::getTag)
                            .collect(Collectors.toSet())).orElse(null));
                    String newTags = String.join(",", Optional.ofNullable(ao.getTags()).orElse(Collections.emptySet()));
                    if (!oldTags.equals(newTags)) {
                       b.set(true); 
                    }
                    break;
                default:
                    break;
                }
            }
        }
    }

    public static void importTags(List<DBItemInventoryConfiguration> cfgs, Map<ConfigurationType, Map<String, String>> oldNewNameMap,
            ExportedTags tagsFromArchive, SOSHibernateSession session) throws SOSHibernateException {
//        List<DBItemInventoryTag> newTags = new ArrayList<>();
//        List<DBItemInventoryTag> storedTags = new ArrayList<>();
   
        
        if (tagsFromArchive != null) {
            
            InventoryTagDBLayer dbLayer = new InventoryTagDBLayer(session);
            InventoryTagGroupDBLayer dbGroupLayer = new InventoryTagGroupDBLayer(session);
            DBLayerDeploy dbDepLayer = new DBLayerDeploy(session);
            Date now = Date.from(Instant.now());
            Map<String, DBItemInventoryTagGroup> allGroups = dbGroupLayer.getAllGroups().stream().collect(Collectors.toMap(
                    DBItemInventoryTagGroup::getName, Function.identity()));
            Integer maxGroupOrdering2 = allGroups.values().stream().collect(Collectors.maxBy(Comparator
                    .comparingInt(DBItemInventoryTagGroup::getOrdering))).map(DBItemInventoryTagGroup::getOrdering).orElse(0);
            AtomicInteger maxGroupOrdering = new AtomicInteger(maxGroupOrdering2 + 1);
//            Map<String, DBItemInventoryTag> allTags = dbLayer.getAllTags().stream().collect(Collectors.toMap(
//                    DBItemInventoryTag::getName, Function.identity()));

            // add new groups
            Consumer<GroupedTag> addGroup = gt -> {
                gt.checkJavaNameRules();
                gt.getGroup().map(g -> {

                    DBItemInventoryTagGroup gItem = allGroups.get(g);
                    if (gItem == null) {
                        gItem = new DBItemInventoryTagGroup();
                        gItem.setName(g);
                        gItem.setModified(now);
                        gItem.setOrdering(maxGroupOrdering.getAndIncrement());
                    }
                    return gItem;

                }).filter(groupItem -> groupItem.getId() == null).ifPresent(groupItem -> {
                    try {
                        session.save(groupItem);
                        allGroups.put(groupItem.getName(), groupItem);
                    } catch (SOSHibernateException e) {
                        throw new JocSosHibernateException(e);
                    }
                });
            };
            
            if (tagsFromArchive.getTags() != null) {
                tagsFromArchive.getTags().stream().map(ExportedTagItem::getName).map(GroupedTag::new).forEach(addGroup);
            }
            if (tagsFromArchive.getJobTags() != null) {
                tagsFromArchive.getJobTags().stream().map(ExportedJobTagItem::getJobs).filter(Objects::nonNull).map(
                        ExportedJobTagItems::getAdditionalProperties).filter(Objects::nonNull).map(Map::values).flatMap(Collection::stream).flatMap(
                                Collection::stream).distinct().map(GroupedTag::new).forEach(addGroup);
            }
            
            Map<String, Long> dbGroupsMap = allGroups.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getId()));

            Map<ConfigurationType, Map<String, DBItemInventoryConfiguration>> cfgsMap = cfgs.stream().collect(Collectors.groupingBy(
                    DBItemInventoryConfiguration::getTypeAsEnum, Collectors.toMap(DBItemInventoryConfiguration::getName, Function.identity())));
            
            Map<DBItemInventoryConfiguration, Set<String>> tagsPerWorkflows = getTagsPerWorkflows(tagsFromArchive.getTags(), cfgsMap, oldNewNameMap,
                    dbDepLayer);

            if (!tagsPerWorkflows.isEmpty()) {
                TaggingImpl.storeTaggings(tagsPerWorkflows, dbGroupsMap, dbLayer, true);
            }
            
            Map<DBItemInventoryConfiguration, Set<JobTags>> jobTagsPerWorkflows = getJobTagsPerWorkflows(tagsFromArchive.getJobTags(), cfgsMap,
                    oldNewNameMap, dbDepLayer);

            if (!jobTagsPerWorkflows.isEmpty()) {
                com.sos.joc.tags.job.impl.TaggingImpl.storeTaggings(jobTagsPerWorkflows, dbGroupsMap, new InventoryJobTagDBLayer(session), true);
            }
            
            // order tagging
            // fileordersources
            updateFileOrderSources(Optional.ofNullable(tagsFromArchive.getOrderTags()).map(ExportedOrderTags::getFileOrderSources), cfgsMap,
                    oldNewNameMap, dbDepLayer, now, false);
            // schedules
            updateSchedules(Optional.ofNullable(tagsFromArchive.getOrderTags()).map(ExportedOrderTags::getSchedules), cfgsMap, oldNewNameMap,
                    dbDepLayer, now, false);
            // workflows
            updateWorkflows(Optional.ofNullable(tagsFromArchive.getOrderTags()).map(ExportedOrderTags::getWorkflows), cfgsMap, oldNewNameMap,
                    dbDepLayer, now, false);
            
        }
    }
}
