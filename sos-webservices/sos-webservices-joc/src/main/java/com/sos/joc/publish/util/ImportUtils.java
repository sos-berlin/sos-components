package com.sos.joc.publish.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.model.SuffixPrefix;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpdateableConfigurationObject;

public class ImportUtils {

    
    public static UpdateableConfigurationObject createUpdateableConfiguration(DBItemInventoryConfiguration existingConfiguration, 
    		ConfigurationObject configuration, String prefix, String suffix, String targetFolder, DBLayerDeploy dbLayer) throws SOSHibernateException {

    	ConfigurationGlobalsJoc clusterSettings = Globals.getConfigurationGlobalsJoc();
        SuffixPrefix suffixPrefix = JocInventory.getSuffixPrefix(suffix, prefix, ClusterSettings.getImportSuffixPrefix(clusterSettings),
                clusterSettings.getImportSuffix().getDefault(), existingConfiguration.getName(), configuration.getObjectType(),
                new InventoryDBLayer(dbLayer.getSession()));
        final List<String> replace = JocInventory.getSearchReplace(suffixPrefix);
        final String oldName = configuration.getName();
        final String newName = configuration.getName().replaceFirst(replace.get(0), replace.get(1));
        Set<DBItemInventoryConfiguration> referencedBy = new HashSet<DBItemInventoryConfiguration>();
        switch (configuration.getObjectType()) {
	    	case LOCK:
	    		referencedBy.addAll(dbLayer.getUsedWorkflowsByLockId(oldName));
	    		break;
        	case WORKFLOW:
                referencedBy.addAll(dbLayer.getUsedFileOrderSourcesByWorkflowName(oldName));
                referencedBy.addAll(dbLayer.getUsedSchedulesByWorkflowName(oldName));
        		break;
        	case WORKINGDAYSCALENDAR:
        	case NONWORKINGDAYSCALENDAR:
        		referencedBy.addAll(dbLayer.getUsedSchedulesByCalendarName(oldName));
        		break;
    		default:
    			break;
        }
        return new UpdateableConfigurationObject(configuration, existingConfiguration, oldName, newName, referencedBy, targetFolder);
    }
    
    public static void replaceReferences (UpdateableConfigurationObject updateableItem, DBLayerDeploy dbLayer) throws SOSHibernateException {
    	
    	// update existing configuration from DB
    	DBItemInventoryConfiguration currentItem = updateableItem.getConfigurationDbItem();
    	currentItem.setName(updateableItem.getNewName());
    	if (updateableItem.getTargetFolder() != null && !updateableItem.getTargetFolder().isEmpty()) {
    		Path folder = Paths.get(updateableItem.getTargetFolder() + currentItem.getFolder()); 
    		currentItem.setFolder(folder.toString().replace('\\', '/'));
    		currentItem.setPath(folder.resolve(currentItem.getName()).toString().replace('\\', '/'));
    	} else {
    		currentItem.setPath(Paths.get(currentItem.getPath()).getParent().resolve(currentItem.getName()).toString().replace('\\', '/'));
    	}
    	dbLayer.getSession().update(currentItem);
    	
    	// update configurations referenced by existing configuration from DB
    	if (updateableItem.getReferencedBy() != null && !updateableItem.getReferencedBy().isEmpty()) {
        	for (DBItemInventoryConfiguration configurationWithReference : updateableItem.getReferencedBy()) {
                String json = configurationWithReference.getContent();
                switch (configurationWithReference.getTypeAsEnum()) {
                case WORKFLOW:
                    json = json.replaceAll("(\"lockId\"\\s*:\\s*\")" + updateableItem.getOldName() + "\"", "$1" + updateableItem.getNewName() + "\"");
                    break;
                case FILEORDERSOURCE:
                    json = json.replaceAll("(\"workflowPath\"\\s*:\\s*\")" + updateableItem.getOldName() + "\"", "$1" + updateableItem.getNewName() + "\"");
                    break;
                case SCHEDULE:
                    if (updateableItem.getConfigurationDbItem().getTypeAsEnum().equals(ConfigurationType.WORKFLOW)) {
                        json = json.replaceAll("(\"workflowName\"\\s*:\\s*\")" + updateableItem.getOldName() + "\"", "$1" + updateableItem.getNewName() + "\"");
                    } else  if (updateableItem.getConfigurationDbItem().getTypeAsEnum().equals(ConfigurationType.WORKINGDAYSCALENDAR) 
                    		|| updateableItem.getConfigurationDbItem().getTypeAsEnum().equals(ConfigurationType.NONWORKINGDAYSCALENDAR)) {
                        json = json.replaceAll("(\"calendarName\"\\s*:\\s*\")" + updateableItem.getOldName() + "\"", "$1" + updateableItem.getNewName() + "\"");
                    }
                    break;
                default:
                    break;
                }
                configurationWithReference.setContent(json);
                dbLayer.getSession().update(configurationWithReference);
        	}
    	}
    		
    }
    
}
