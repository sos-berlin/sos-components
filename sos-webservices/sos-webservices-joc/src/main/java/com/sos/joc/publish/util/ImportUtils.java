package com.sos.joc.publish.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.jobtemplate.JobTemplate;
import com.sos.inventory.model.report.Report;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.script.Script;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JsonConverter;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.classes.inventory.WorkflowConverter;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.classes.tag.GroupedTag;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryTagGroup;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryJobTagDBLayer;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.db.inventory.InventoryTagGroupDBLayer;
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
import com.sos.joc.model.tag.ExportedJobTagItem;
import com.sos.joc.model.tag.ExportedJobTagItems;
import com.sos.joc.model.tag.ExportedTagItem;
import com.sos.joc.model.tag.ExportedTags;
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
                if(entryName.equals(ExportUtils.TAGS_ENTRY_NAME)) {
                  tagsFromArchive = Globals.objectMapper.readValue(outBuffer.toByteArray(), ExportedTags.class);
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
                    JocMetaInfo fromFile = Globals.prettyPrintObjectMapper.readValue(outBuffer.toString(), JocMetaInfo.class);
                    if (!isJocMetaInfoNullOrEmpty(fromFile)) {
                        jocMetaInfo.setJocVersion(fromFile.getJocVersion());
                        jocMetaInfo.setInventorySchemaVersion(fromFile.getInventorySchemaVersion());
                        jocMetaInfo.setApiVersion(fromFile.getApiVersion());
                        jocMetaInfo.setVersionId(fromFile.getVersionId());
                    }
                }
                if(entryName.equals(ExportUtils.TAGS_ENTRY_NAME)) {
                  tagsFromArchive = Globals.prettyPrintObjectMapper.readValue(outBuffer.toString(), ExportedTags.class);
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
    
    public static void importTags(List<DBItemInventoryConfiguration> cfgs, Map<String, String> oldNewNameMap, ExportedTags tagsFromArchive,
            SOSHibernateSession session) throws SOSHibernateException {
//        List<DBItemInventoryTag> newTags = new ArrayList<>();
//        List<DBItemInventoryTag> storedTags = new ArrayList<>();
   
        
        if (tagsFromArchive != null) {
            
            InventoryTagDBLayer dbLayer = new InventoryTagDBLayer(session);
            InventoryTagGroupDBLayer dbGroupLayer = new InventoryTagGroupDBLayer(session);
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
            
            if (tagsFromArchive.getTags() != null) {
                
                Map<DBItemInventoryConfiguration, Set<String>> tagsPerWorkflows = new HashMap<>();
                tagsFromArchive.getTags().stream().sorted(Comparator.comparingInt(ExportedTagItem::getOrdering)).forEachOrdered(tag -> {
                    tag.getUsedBy().forEach(o -> {
                        try {
                            DBItemInventoryConfiguration conf = cfgsMap.getOrDefault(ConfigurationType.fromValue(o.getType()), Collections.emptyMap())
                                    .get(oldNewNameMap.getOrDefault(o.getName(), o.getName()));
                            if (conf != null) {
                                tagsPerWorkflows.putIfAbsent(conf, new HashSet<>());
                                tagsPerWorkflows.get(conf).add(tag.getName());
                            }
                        } catch (IllegalArgumentException e1) {
                            //
                        }
                    });
                });
                
                if (!tagsPerWorkflows.isEmpty()) {
                    TaggingImpl.storeTaggings(tagsPerWorkflows, dbGroupsMap, dbLayer, true);
                }
            }
            
            if (tagsFromArchive.getJobTags() != null) {
                Map<DBItemInventoryConfiguration, Set<JobTags>> jobTagsPerWorkflows = new HashMap<>();
                tagsFromArchive.getJobTags().forEach(w -> {
                    if (w.getJobs() != null && w.getJobs().getAdditionalProperties() != null && !w.getJobs().getAdditionalProperties().isEmpty() && w
                            .getName() != null) {
                        DBItemInventoryConfiguration conf = cfgsMap.getOrDefault(ConfigurationType.WORKFLOW, Collections.emptyMap()).get(oldNewNameMap
                                .getOrDefault(w.getName(), w.getName()));
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
                if (!jobTagsPerWorkflows.isEmpty()) {
                    com.sos.joc.tags.job.impl.TaggingImpl.storeTaggings(jobTagsPerWorkflows, dbGroupsMap, new InventoryJobTagDBLayer(session), true);
                }
            }
            
            
//            if (!tagsFromArchive.getTags().isEmpty()) {
//                tagsFromArchive.getTags().stream().forEach(item -> {
//                    GroupedTag gt = new GroupedTag(item.getName());
//                    final DBItemInventoryTag tag = dbLayer.getTag(gt.getTag());
//                    List<ExportedTaggedObject> taggingItems = item.getUsedBy();
//                    if (tag != null) {
//                        tag.setModified(now);
//                        try {
//                            session.update(tag);
//                        } catch (SOSHibernateException e) {
//                            throw new JocSosHibernateException(e);
//                        }
//                        storedTags.add(tag);
//                        List<DBItemInventoryTagging> existingTaggings = dbLayer.getTaggingsByTagId(tag.getId());
//                        taggingItems.stream().forEach(tagging -> {
//                            List<DBItemInventoryTagging> taggingsByNameType = dbLayer.getTaggings(tagging.getName(), ConfigurationType.fromValue(
//                                    tagging.getType()).intValue());
//                            existingTaggings.addAll(taggingsByNameType.stream().filter(tagItem -> tagItem.getTagId().equals(tag.getId())).collect(
//                                    Collectors.toList()));
//                            taggingItems.stream().forEach(used -> {
//                                DBItemInventoryConfiguration config = cfgs.stream().filter(cfg -> cfg.getName().equals(used.getName()) && cfg
//                                        .getType().equals(ConfigurationType.fromValue(used.getType()).intValue())).findAny().orElse(null);
//                                if (config != null) {
//                                    DBItemInventoryTagging exTagging = existingTaggings.stream().filter(cfgTagging -> cfgTagging.getName().equals(
//                                            config.getName()) && cfgTagging.getType().equals(config.getType())).findAny().orElse(null);
//                                    if (exTagging != null) {
//                                        exTagging.setTagId(tag.getId());
//                                        exTagging.setModified(now);
//                                        try {
//                                            session.update(exTagging);
//                                        } catch (SOSHibernateException e) {
//                                            throw new JocSosHibernateException(e);
//                                        }
//                                    }
//                                }
//                            });
//                        });
//                    } else {
//                        DBItemInventoryTag newTag = new DBItemInventoryTag();
//                        newTag.setName(item.getName());
//                        newTag.setOrdering(item.getOrdering());
//                        newTag.setModified(now);
//                        newTags.add(newTag);
//                        try {
//                            session.save(newTag);
//                        } catch (SOSHibernateException e) {
//                            throw new JocSosHibernateException(e);
//                        }
//                        storedTags.add(newTag);
//                        taggingItems.stream().forEach(used -> {
//                            DBItemInventoryConfiguration config = cfgs.stream().filter(cfg -> cfg.getName().equals(used.getName()) && cfg.getType()
//                                    .equals(ConfigurationType.fromValue(used.getType()).intValue())).findAny().orElse(null);
//                            if (config != null) {
//                                DBItemInventoryTagging newTagging = new DBItemInventoryTagging();
//                                newTagging.setCid(config.getId());
//                                newTagging.setName(used.getName());
//                                newTagging.setType(ConfigurationType.fromValue(used.getType()).intValue());
//                                newTagging.setTagId(newTag.getId());
//                                newTagging.setModified(now);
//                                try {
//                                    session.save(newTagging);
//                                } catch (SOSHibernateException e) {
//                                    throw new JocSosHibernateException(e);
//                                }
//                            }
//                        });
//
//                    }
//                });
//            }
//            if (storedTags != null) {
//                AtomicInteger ordering = new AtomicInteger(1);
//                List<DBItemInventoryTag> allDbTags = dbLayer.getAllTags();
//                // new HashSet<DBItemInventoryTag>(storedTags);
//                // get all Tags from db and merge them to update ordering for all tags
//                // for each item from import file json -> DBItemInventoryTag -> items.add()
//                Stream.concat(allDbTags.stream(), storedTags.stream()).distinct().sorted(Comparator.comparing(DBItemInventoryTag::getOrdering)
//                        .thenComparing(DBItemInventoryTag::getName)).peek(item -> item.setOrdering(ordering.getAndIncrement())).forEach(tag -> {
//                            try {
//                                session.update(tag);
//                            } catch (SOSHibernateException e) {
//                                throw new JocSosHibernateException(e);
//                            }
//                        });
//            }
        }
    }
}
