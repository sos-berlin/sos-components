package com.sos.joc.publish.util;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.sos.commons.hibernate.SOSHibernateSession;
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

public class ImportConfigurationUtils {

    
    public static ConfigurationObject updateImportNameAndPath(SOSHibernateSession session, DBItemInventoryConfiguration existingConfiguration, 
    		ConfigurationObject configuration) throws SOSHibernateException {
        String suffix = "";
        String prefix = "";
        ConfigurationGlobalsJoc clusterSettings = Globals.getConfigurationGlobalsJoc();
        SuffixPrefix suffixPrefix = JocInventory.getSuffixPrefix(suffix, prefix, ClusterSettings.getImportSuffixPrefix(clusterSettings),
                clusterSettings.getImportSuffix().getDefault(), existingConfiguration.getName(), configuration.getObjectType(),
                new InventoryDBLayer(session));
        final List<String> replace = JocInventory.getSearchReplace(suffixPrefix);
        configuration.setName(configuration.getName().replaceFirst(replace.get(0), replace.get(1)));
        configuration.setPath(Paths.get(configuration.getPath()).getParent().resolve(configuration.getName()).toString().replace('\\', '/'));
        return configuration;
    }
    

    public static void replaceReferences (DBItemInventoryConfiguration item, Map<ConfigurationType, Map<String, String>> oldToNewName) {
        String json = item.getContent();
        switch (item.getTypeAsEnum()) {
        case WORKFLOW:
            for (Map.Entry<String, String> oldNewName : oldToNewName.getOrDefault(ConfigurationType.LOCK, Collections.emptyMap()).entrySet()) {
                json = json.replaceAll("(\"lockId\"\\s*:\\s*\")" + oldNewName.getKey() + "\"", "$1" + oldNewName.getValue() + "\"");
            }
            break;
        case FILEORDERSOURCE:
            for (Map.Entry<String, String> oldNewName : oldToNewName.getOrDefault(ConfigurationType.WORKFLOW, Collections.emptyMap()).entrySet()) {
                json = json.replaceAll("(\"workflowPath\"\\s*:\\s*\")" + oldNewName.getKey() + "\"", "$1" + oldNewName.getValue() + "\"");
            }
            break;
        case SCHEDULE:
            for (Map.Entry<String, String> oldNewName : oldToNewName.getOrDefault(ConfigurationType.WORKFLOW, Collections.emptyMap()).entrySet()) {
                json = json.replaceAll("(\"workflowName\"\\s*:\\s*\")" + oldNewName.getKey() + "\"", "$1" + oldNewName.getValue() + "\"");
            }
            for (Map.Entry<String, String> oldNewName : oldToNewName.getOrDefault(ConfigurationType.WORKINGDAYSCALENDAR, Collections.emptyMap()).entrySet()) {
                json = json.replaceAll("(\"calendarName\"\\s*:\\s*\")" + oldNewName.getKey() + "\"", "$1" + oldNewName.getValue() + "\"");
            }
            for (Map.Entry<String, String> oldNewName : oldToNewName.getOrDefault(ConfigurationType.NONWORKINGDAYSCALENDAR,
                    Collections.emptyMap()).entrySet()) {
                json = json.replaceAll("(\"calendarName\"\\s*:\\s*\")" + oldNewName.getKey() + "\"", "$1" + oldNewName.getValue() + "\"");
            }
            break;
        default:
            break;
        }
        
    }
    
    private static DBItemInventoryConfiguration createItem(DBItemInventoryConfiguration oldItem, java.nio.file.Path newItem) {
        DBItemInventoryConfiguration item = new DBItemInventoryConfiguration();
        item.setId(null);
        item.setPath(newItem.toString().replace('\\', '/'));
        item.setFolder(newItem.getParent().toString().replace('\\', '/'));
        item.setName(newItem.getFileName().toString());
        item.setDeployed(false);
        item.setReleased(false);
        item.setModified(Date.from(Instant.now()));
        item.setCreated(item.getModified());
        item.setDeleted(false);
        item.setAuditLogId(0L);
        item.setDocumentationId(oldItem.getDocumentationId());
        item.setTitle(oldItem.getTitle());
        item.setType(oldItem.getType());
        item.setValid(oldItem.getValid());
        item.setContent(oldItem.getContent());
        return item;
    }

}
