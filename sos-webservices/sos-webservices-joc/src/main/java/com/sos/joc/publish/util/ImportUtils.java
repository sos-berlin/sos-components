package com.sos.joc.publish.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingDayCalendars;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.CalendarType;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.script.Script;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JsonSerializer;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocImportException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.model.SuffixPrefix;
import com.sos.joc.model.calendar.NonWorkingDaysCalendarEdit;
import com.sos.joc.model.calendar.WorkingDaysCalendarEdit;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.board.BoardEdit;
import com.sos.joc.model.inventory.board.BoardPublish;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.fileordersource.FileOrderSourceEdit;
import com.sos.joc.model.inventory.fileordersource.FileOrderSourcePublish;
import com.sos.joc.model.inventory.jobclass.JobClassEdit;
import com.sos.joc.model.inventory.jobclass.JobClassPublish;
import com.sos.joc.model.inventory.jobresource.JobResourceEdit;
import com.sos.joc.model.inventory.jobresource.JobResourcePublish;
import com.sos.joc.model.inventory.lock.LockEdit;
import com.sos.joc.model.inventory.lock.LockPublish;
import com.sos.joc.model.inventory.script.ScriptEdit;
import com.sos.joc.model.inventory.workflow.WorkflowEdit;
import com.sos.joc.model.inventory.workflow.WorkflowPublish;
import com.sos.joc.model.joc.JocMetaInfo;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.joc.model.sign.Signature;
import com.sos.joc.model.sign.SignaturePath;
import com.sos.joc.publish.common.ConfigurationObjectFileExtension;
import com.sos.joc.publish.common.ControllerObjectFileExtension;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpdateableConfigurationObject;
import com.sos.sign.model.board.Board;
import com.sos.sign.model.fileordersource.FileOrderSource;
import com.sos.sign.model.jobclass.JobClass;
import com.sos.sign.model.jobresource.JobResource;
import com.sos.sign.model.lock.Lock;
import com.sos.webservices.order.initiator.model.ScheduleEdit;

public class ImportUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportUtils.class);
    public static final String JOC_META_INFO_FILENAME = "meta_inf";
    
    public static UpdateableConfigurationObject createUpdateableConfiguration(DBItemInventoryConfiguration existingConfiguration, 
    		ConfigurationObject configuration, Set<ConfigurationObject> configurations, String prefix, String suffix, String targetFolder, DBLayerDeploy dbLayer)
    				throws SOSHibernateException {

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
	    		referencedBy.addAll(getUsedWorkflowsFromArchiveByLockId(oldName, configurations));
	    		break;
	    	case NOTICEBOARD:
                referencedBy.addAll(getUsedWorkflowsFromArchiveByBoardName(oldName, configurations));
	    	    break;
        	case WORKFLOW:
                referencedBy.addAll(getUsedFileOrderSourcesFromArchiveByWorkflowName(oldName, configurations));
                referencedBy.addAll(getUsedSchedulesFromArchiveByWorkflowName(oldName, configurations));
                referencedBy.addAll(getUsedWorkflowsFromArchiveByWorkflowName(oldName, configurations));
        		break;
        	case WORKINGDAYSCALENDAR:
        	case NONWORKINGDAYSCALENDAR:
        		referencedBy.addAll(getUsedSchedulesFromArchiveByCalendarName(oldName, configurations));
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
        	for (ConfigurationObject configurationWithReference : updateableItem.getReferencedBy()) {
                switch (configurationWithReference.getObjectType()) {
                case WORKFLOW:
                    if (updateableItem.getConfigurationObject().getObjectType().equals(ConfigurationType.LOCK)) {
                        try {
                            String json = Globals.objectMapper.writeValueAsString(configurationWithReference.getConfiguration());
                            json = json.replaceAll("(\"lockName\"\\s*:\\s*\")" + updateableItem.getOldName() + "\"", "$1" + updateableItem.getNewName() + "\"");
                            ((WorkflowEdit)configurationWithReference).setConfiguration(Globals.objectMapper.readValue(json, Workflow.class));
                        } catch (IOException e) {
                            throw new JocImportException(e);
                        }
                    } else if (updateableItem.getConfigurationObject().getObjectType().equals(ConfigurationType.NOTICEBOARD)) {
                        try {
                            String json = Globals.objectMapper.writeValueAsString(configurationWithReference.getConfiguration());
                            json = json.replaceAll("(\"(?:noticeB|b)oardName\"\\s*:\\s*\")" + updateableItem.getOldName() + "\"", "$1" + updateableItem.getNewName() + "\"");
                            ((WorkflowEdit)configurationWithReference).setConfiguration(Globals.objectMapper.readValue(json, Workflow.class));
                        } catch (IOException e) {
                            throw new JocImportException(e);
                        }
                    } else if (updateableItem.getConfigurationObject().getObjectType().equals(ConfigurationType.WORKFLOW)) {
                        try {
                            String json = Globals.objectMapper.writeValueAsString(configurationWithReference.getConfiguration());
                            json = json.replaceAll("(\"workflowName\"\\s*:\\s*\")" + updateableItem.getOldName() + "\"", "$1" + updateableItem.getNewName() + "\"");
                            ((WorkflowEdit)configurationWithReference).setConfiguration(Globals.objectMapper.readValue(json, Workflow.class));
                        } catch (IOException e) {
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
                    	if (((ScheduleEdit)configurationWithReference).getConfiguration().getWorkflowName().equals(updateableItem.getOldName()) ||
                    			((ScheduleEdit)configurationWithReference).getConfiguration().getWorkflowName()
                				.equals(updateableItem.getConfigurationObject().getName())) {
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
                default:
                    break;
                }
        	}
    	}
    }

    public static void updateConfigurationWithChangedReferences (DBLayerDeploy dbLayer, ConfigurationObject config) {
        DBItemInventoryConfiguration alreadyExist = dbLayer.getInventoryConfigurationByNameAndType(config.getName(), config.getObjectType().intValue());
        if (alreadyExist != null) {
            try {
                alreadyExist.setContent(Globals.objectMapper.writeValueAsString(config.getConfiguration()));
                alreadyExist.setModified(Date.from(Instant.now()));
                dbLayer.getSession().update(alreadyExist);
            } catch (JsonProcessingException e) {
                LOGGER.error(e.getMessage(),e);
            } catch (SOSHibernateException e) {
                throw new JocSosHibernateException(e);
            }
        }
    }
    
    private static Set<ConfigurationObject> getUsedWorkflowsFromArchiveByWorkflowName (String name, Set<ConfigurationObject> configurations) {
        return configurations.stream().filter(item -> ConfigurationType.WORKFLOW.equals(item.getObjectType()))
                .map(item -> {
                    Workflow wf = (Workflow)item.getConfiguration();
                    try {
                        String wfJson = Globals.objectMapper.writeValueAsString(wf);
                        Matcher matcher = Pattern.compile("(\"workflowName\"\\s*:\\s*\"" + name + "\")").matcher(wfJson); 
                        if (matcher.find()) {
                            return item;
                        }
                    } catch (JsonProcessingException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private static Set<ConfigurationObject> getUsedWorkflowsFromArchiveByLockId (String name, Set<ConfigurationObject> configurations) {
        return configurations.stream().filter(item -> ConfigurationType.WORKFLOW.equals(item.getObjectType()))
                .map(item -> {
                    Workflow wf = (Workflow)item.getConfiguration();
                    try {
                        String wfJson = Globals.objectMapper.writeValueAsString(wf);
                        Matcher matcher = Pattern.compile("(\"lockName\"\\s*:\\s*\"" + name + "\")").matcher(wfJson); 
                        if (matcher.find()) {
                            return item;
                        }
                    } catch (JsonProcessingException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private static Set<ConfigurationObject> getUsedWorkflowsFromArchiveByBoardName (String name, Set<ConfigurationObject> configurations) {
        return configurations.stream().filter(item -> ConfigurationType.WORKFLOW.equals(item.getObjectType()))
                .map(item -> {
                    Workflow wf = (Workflow)item.getConfiguration();
                    try {
                        String wfJson = Globals.objectMapper.writeValueAsString(wf);
                        Matcher matcher = Pattern.compile("(\"(?:noticeB|b)oardName\"\\s*:\\s*\"" + name + "\")").matcher(wfJson); 
                        if (matcher.find()) {
                            return item;
                        }
                    } catch (JsonProcessingException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private static Set<ConfigurationObject> getUsedFileOrderSourcesFromArchiveByWorkflowName (String name, Set<ConfigurationObject> configurations) {
    	return configurations.stream().filter(item -> ConfigurationType.FILEORDERSOURCE.equals(item.getObjectType())
    			&& ((FileOrderSourceEdit)item).getConfiguration().getWorkflowName().equals(name)).collect(Collectors.toSet());
    }

    private static Set<ConfigurationObject> getUsedSchedulesFromArchiveByWorkflowName (String name, Set<ConfigurationObject> configurations) {
    	return configurations.stream().filter(item -> ConfigurationType.SCHEDULE.equals(item.getObjectType())
    			&& ((ScheduleEdit)item).getConfiguration().getWorkflowName().equals(name)).collect(Collectors.toSet());
    }

    private static Set<ConfigurationObject> getUsedSchedulesFromArchiveByCalendarName (String name, Set<ConfigurationObject> configurations) {
    	return configurations.stream()
    			.filter(item -> ConfigurationType.SCHEDULE.equals(item.getObjectType())).map(item -> {
    	    		if (ConfigurationType.SCHEDULE.equals(item.getObjectType())) {
    	    			List<AssignedCalendars> assignedCalendars = ((ScheduleEdit)item).getConfiguration().getCalendars();
    	    			List<AssignedNonWorkingDayCalendars> assignedNonWorkingDaysCalendars = 
    	    					((ScheduleEdit)item).getConfiguration().getNonWorkingDayCalendars();
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
        return Arrays.asList(ConfigurationType.LOCK,  ConfigurationType.NOTICEBOARD, 
                ConfigurationType.JOBRESOURCE, ConfigurationType.INCLUDESCRIPT, ConfigurationType.NONWORKINGDAYSCALENDAR,
                ConfigurationType.WORKINGDAYSCALENDAR, ConfigurationType.WORKFLOW, ConfigurationType.FILEORDERSOURCE, ConfigurationType.SCHEDULE);
    }

    public static Map<ControllerObject, SignaturePath> readZipFileContentWithSignatures(InputStream inputStream, JocMetaInfo jocMetaInfo)
            throws DBConnectionRefusedException, DBInvalidDataException, SOSHibernateException, IOException, JocUnsupportedFileTypeException,
            JocConfigurationException, DBOpenSessionException {
        Set<ControllerObject> objects = new HashSet<ControllerObject>();
        Set<SignaturePath> signaturePaths = new HashSet<SignaturePath>();
        Map<ControllerObject, SignaturePath> objectsWithSignature = new HashMap<ControllerObject, SignaturePath>();
        ZipInputStream zipStream = null;
        try {
            zipStream = new ZipInputStream(inputStream);
            ZipEntry entry = null;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = entry.getName().replace('\\', '/');
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
            objects.stream().forEach(item -> {
                objectsWithSignature.put(item, signaturePaths.stream().filter(item2 -> item2.getObjectPath().equals(item.getPath())).findFirst()
                        .get());
            });
        } finally {
            if (zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {
                }
            }
        }
        return objectsWithSignature;
    }

    public static Set<ConfigurationObject> readZipFileContent(InputStream inputStream, JocMetaInfo jocMetaInfo) throws DBConnectionRefusedException,
            DBInvalidDataException, SOSHibernateException, IOException, JocUnsupportedFileTypeException, JocConfigurationException,
            DBOpenSessionException {
        Set<ConfigurationObject> objects = new HashSet<ConfigurationObject>();
        ZipInputStream zipStream = null;
        try {
            zipStream = new ZipInputStream(inputStream);
            ZipEntry entry = null;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = entry.getName().replace('\\', '/');
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
                    }
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
        return objects;
    }

    public static Map<ControllerObject, SignaturePath> readTarGzipFileContentWithSignatures(InputStream inputStream, JocMetaInfo jocMetaInfo)
            throws DBConnectionRefusedException, DBInvalidDataException, SOSHibernateException, IOException, JocUnsupportedFileTypeException,
            JocConfigurationException, DBOpenSessionException {
        Set<ControllerObject> objects = new HashSet<ControllerObject>();
        Set<SignaturePath> signaturePaths = new HashSet<SignaturePath>();
        Map<ControllerObject, SignaturePath> objectsWithSignature = new HashMap<ControllerObject, SignaturePath>();
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
            objects.stream().forEach(item -> {
                objectsWithSignature.put(item, signaturePaths.stream().filter(item2 -> item2.getObjectPath().equals(item.getPath())).findFirst()
                        .get());
            });
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
        return objectsWithSignature;
    }

    public static Set<ConfigurationObject> readTarGzipFileContent(InputStream inputStream, JocMetaInfo jocMetaInfo)
            throws DBConnectionRefusedException, DBInvalidDataException, SOSHibernateException, IOException, JocUnsupportedFileTypeException,
            JocConfigurationException, DBOpenSessionException {
        Set<ConfigurationObject> objects = new HashSet<ConfigurationObject>();
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
                    }
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
        return objects;
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
            com.sos.inventory.model.workflow.Workflow workflow = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8.displayName()),
                    com.sos.inventory.model.workflow.Workflow.class);
            workflow = JsonSerializer.emptyValuesToNull(workflow);
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
            com.sos.inventory.model.jobresource.JobResource jobResource = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8
                    .displayName()), com.sos.inventory.model.jobresource.JobResource.class);
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
            com.sos.inventory.model.lock.Lock lock = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8.displayName()),
                    com.sos.inventory.model.lock.Lock.class);
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
            com.sos.inventory.model.board.Board board = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8.displayName()),
                    com.sos.inventory.model.board.Board.class);
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
            com.sos.inventory.model.jobclass.JobClass jobClass = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8
                    .displayName()), com.sos.inventory.model.jobclass.JobClass.class);
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
            com.sos.inventory.model.fileordersource.FileOrderSource fileOrderSource = Globals.objectMapper.readValue(outBuffer.toString(
                    StandardCharsets.UTF_8.displayName()), com.sos.inventory.model.fileordersource.FileOrderSource.class);
            fileOrderSource = JsonSerializer.emptyValuesToNull(fileOrderSource);
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
            Schedule schedule = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8.displayName()), Schedule.class);
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
            Script script = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8.displayName()), Script.class);
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
        } else if (entryName.endsWith(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.value())) {
            String normalizedPath = Globals.normalizePath("/" + entryName.replace(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.value(),
                    ""));
            if (normalizedPath.startsWith("//")) {
                normalizedPath = normalizedPath.substring(1);
            }
            Calendar cal = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8.displayName()), Calendar.class);
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
        if (board != null && board.getEndOfLife() == null && board.getExpectOrderToNoticeId() == null && board.getPostOrderToNoticeId() == null
                && board.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(com.sos.inventory.model.board.Board board) {
        if (board != null && board.getDocumentationName() == null && board.getEndOfLife() == null && board.getExpectOrderToNoticeId() == null && board
                .getPostOrderToNoticeId() == null && board.getTYPE() == null) {
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
                        || jocMetaInfo.getApiVersion().isEmpty()))) {
            return true;
        } else {
            return false;
        }
    }
    
    public static void validateAndUpdate (List<DBItemInventoryConfiguration> storedConfigurations, Set<String> agentNames,
            SOSHibernateSession session) {
        Boolean autoCommit = true; 
        try {
            autoCommit = session.isAutoCommit();
            if(autoCommit) {
                session.setAutoCommit(false);
            }
            Globals.beginTransaction(session);
            for (DBItemInventoryConfiguration dbItem : storedConfigurations) {
                boolean valid = false;
                try {
                    Validator.validate(dbItem.getTypeAsEnum(), dbItem.getContent().getBytes(), new InventoryDBLayer(session), agentNames);
                    valid = true;
                } catch (Throwable e) {
                    valid = false;
                }
                StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
                hql.append("set valid = :valid where id = :id");
                Query<?> query = session.createQuery(hql.toString());
                query.setParameter("valid", valid);
                query.setParameter("id", dbItem.getId());
                session.executeUpdate(query);
            }
            Globals.commit(session);
        } catch (SOSHibernateException e) {
            Globals.rollback(session);
        } finally {
            try {
                session.setAutoCommit(autoCommit);
            } catch (SOSHibernateException e1) {}
        }
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

}
