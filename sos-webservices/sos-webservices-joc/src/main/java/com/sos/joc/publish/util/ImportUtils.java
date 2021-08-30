package com.sos.joc.publish.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingDayCalendars;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.model.SuffixPrefix;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.fileordersource.FileOrderSourceEdit;
import com.sos.joc.model.inventory.workflow.WorkflowEdit;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpdateableConfigurationObject;
import com.sos.webservices.order.initiator.model.ScheduleEdit;

public class ImportUtils {

    
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
    	
    	Date now = Date.from(Instant.now());
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
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else if (updateableItem.getConfigurationObject().getObjectType().equals(ConfigurationType.NOTICEBOARD)) {
                        try {
                            String json = Globals.objectMapper.writeValueAsString(configurationWithReference.getConfiguration());
                            json = json.replaceAll("(\"(?:noticeB|b)oardName\"\\s*:\\s*\")" + updateableItem.getOldName() + "\"", "$1" + updateableItem.getNewName() + "\"");
                            ((WorkflowEdit)configurationWithReference).setConfiguration(Globals.objectMapper.readValue(json, Workflow.class));
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
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
						e.printStackTrace();
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
                        e.printStackTrace();
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
                ConfigurationType.JOBRESOURCE, ConfigurationType.NONWORKINGDAYSCALENDAR, ConfigurationType.WORKINGDAYSCALENDAR, 
                ConfigurationType.WORKFLOW, ConfigurationType.FILEORDERSOURCE, ConfigurationType.SCHEDULE);
    }
}
