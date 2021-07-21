package com.sos.joc.inventory.impl;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.CheckJavaVariableName;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.inventory.resource.ICopyConfigurationResource;
import com.sos.joc.model.SuffixPrefix;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ResponseNewPath;
import com.sos.joc.model.inventory.copy.RequestFilter;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class CopyConfigurationResourceImpl extends JOCResourceImpl implements ICopyConfigurationResource {

    @Override
    public JOCDefaultResponse copy(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                response = copy(in);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private JOCDefaultResponse copy(RequestFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            
            session.beginTransaction();
            DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, in, folderPermissions);
            ConfigurationType type = config.getTypeAsEnum();
            
            final java.nio.file.Path oldPath = Paths.get(config.getPath());
            final String oldFolder = config.getFolder();
            // without any prefix/suffix
            java.nio.file.Path pWithoutFix = Paths.get(oldFolder).resolve(in.getNewPath()).normalize();
            boolean newFolderIsRootFolder = JocInventory.ROOT_FOLDER.equals(pWithoutFix.toString().replace('\\', '/'));
            String newFolder = newFolderIsRootFolder ? JocInventory.ROOT_FOLDER : pWithoutFix.getParent().toString().replace('\\', '/');
            String newPathWithoutFix = pWithoutFix.toString().replace('\\', '/');
            String newFilename = newFolderIsRootFolder ? "" : pWithoutFix.getFileName().toString();
            
            // folder copy or (object copy where target and source name are the same)
            boolean fixMustUsed = JocInventory.isFolder(type) || (!JocInventory.isFolder(type) && oldPath.getFileName().toString().equals(newFilename));
            
            ConfigurationGlobalsJoc clusterSettings = Globals.getConfigurationGlobalsJoc();
            SuffixPrefix suffixPrefix = new SuffixPrefix(); 
            if (fixMustUsed) {
                suffixPrefix = JocInventory.getSuffixPrefix(in.getSuffix(), in.getPrefix(), ClusterSettings.getCopyPasteSuffixPrefix(clusterSettings),
                        clusterSettings.getCopyPasteSuffix().getDefault(), newFilename, type, dbLayer);
            } else {
                suffixPrefix.setPrefix("");
                suffixPrefix.setSuffix("");
            }
            
            final List<String> replace = JocInventory.getSearchReplace(suffixPrefix);
            Set<String> events = Collections.emptySet();
            Set<String> folderEvents = Collections.emptySet();
            ResponseNewPath response = new ResponseNewPath();
            response.setObjectType(type);
            
            // Check folder permissions
            if (JocInventory.isFolder(type)) {
                if (!folderPermissions.isPermittedForFolder(newPathWithoutFix)) {
                    throw new JocFolderPermissionsException("Access denied for folder: " + newPathWithoutFix);
                }
                // Check Java variable name rules
                for (int i = 0; i < pWithoutFix.getNameCount(); i++) {
                    if (i == pWithoutFix.getNameCount() - 1) {
                        CheckJavaVariableName.test("name", pWithoutFix.getName(i).toString());
                    } else {
                        CheckJavaVariableName.test("folder", pWithoutFix.getName(i).toString());
                    }
                }
                
                List<DBItemInventoryConfiguration> oldDBFolderContent = null;
                if (in.getShallowCopy()) {
                    oldDBFolderContent = dbLayer.getFolderContent(config.getPath(), true, JocInventory.getTypesFromObjectsWithReferencesAndFolders());
                } else {
                    oldDBFolderContent = dbLayer.getFolderContent(config.getPath(), true, null);
                }
                if (oldDBFolderContent == null) {
                    oldDBFolderContent = Collections.emptyList();
                }
                Map<ConfigurationType, Map<String, String>> oldToNewName = Collections.emptyMap();
                if (!in.getShallowCopy()) {
                    List<Integer> typesForReferences = Arrays.asList(ConfigurationType.WORKFLOW.intValue(), ConfigurationType.WORKINGDAYSCALENDAR
                            .intValue(), ConfigurationType.NONWORKINGDAYSCALENDAR.intValue(), ConfigurationType.LOCK.intValue(),
                            ConfigurationType.BOARD.intValue(), ConfigurationType.JOBRESOURCE.intValue());
                    oldToNewName = oldDBFolderContent.stream().filter(item -> typesForReferences.contains(item.getType())).collect(Collectors
                            .groupingBy(DBItemInventoryConfiguration::getTypeAsEnum, Collectors.toMap(DBItemInventoryConfiguration::getName,
                                    item -> item.getName().replaceFirst(replace.get(0), replace.get(1)))));
                }
                
                List<AuditLogDetail> auditLogDetails = new ArrayList<>();
                oldDBFolderContent = oldDBFolderContent.stream().map(oldItem -> {
                    java.nio.file.Path oldItemPath = Paths.get(oldItem.getPath());
                    if (ConfigurationType.FOLDER.intValue() == oldItem.getType()) {
                        return createItem(oldItem, pWithoutFix.resolve(oldPath.relativize(oldItemPath)));
                    }
                    auditLogDetails.add(new AuditLogDetail(oldItemPath, oldItem.getType()));
                    return createItem(oldItem, pWithoutFix.resolve(oldPath.relativize(oldItemPath.getParent().resolve(oldItem.getName()
                            .replaceFirst(replace.get(0), replace.get(1))))));
                }).collect(Collectors.toList());
                
                List<DBItemInventoryConfiguration> newDBFolderContent = null;
                if (in.getShallowCopy()) {
                    newDBFolderContent = dbLayer.getFolderContent(newPathWithoutFix, true, JocInventory.getTypesFromObjectsWithReferencesAndFolders());
                } else {
                    newDBFolderContent = dbLayer.getFolderContent(newPathWithoutFix, true, null);
                }

                if (newDBFolderContent != null && !newDBFolderContent.isEmpty()) {
                    newDBFolderContent.retainAll(oldDBFolderContent);
                    if (!newDBFolderContent.isEmpty()) {
                        Map<Boolean, List<DBItemInventoryConfiguration>> map = newDBFolderContent.stream().collect(Collectors.groupingBy(
                                item -> ConfigurationType.FOLDER.intValue() == item.getType()));
                        if (!map.getOrDefault(false, Collections.emptyList()).isEmpty()) { // all not folder items
                            throw new JocObjectAlreadyExistException("Cannot move to " + newPathWithoutFix + ": common objects are " + map.get(false).stream()
                                    .map(DBItemInventoryConfiguration::getPath).collect(Collectors.joining("', '", "'", "'")));
                        }
                        
                        oldDBFolderContent.removeAll(map.getOrDefault(true, Collections.emptyList()));
                    }
                }
                DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog(), auditLogDetails);
                
                if (!JocInventory.ROOT_FOLDER.equals(config.getPath())) {
                    DBItemInventoryConfiguration newItem = dbLayer.getConfiguration(newPathWithoutFix, ConfigurationType.FOLDER.intValue());
                    if (newItem == null) {
                        if (!newFolderIsRootFolder) {
                            DBItemInventoryConfiguration newDbItem = createItem(config, pWithoutFix);
                            newDbItem.setAuditLogId(dbAuditLog.getId());
                            JocInventory.insertConfiguration(dbLayer, newDbItem);
                            JocInventory.makeParentDirs(dbLayer, pWithoutFix.getParent(), newDbItem.getAuditLogId());
                            response.setId(newDbItem.getId());
                            response.setPath(newDbItem.getPath());
                        } else {
                            response.setId(0L);
                            response.setPath("/");
                        }
                    } else if (!oldDBFolderContent.isEmpty()) {
                        response.setId(newItem.getId());
                        response.setPath(newItem.getPath());
                    }
                } else {
                    response.setId(0L);
                    response.setPath("/");
                }
                if (in.getShallowCopy()) {
                    for (DBItemInventoryConfiguration item : oldDBFolderContent) {
                        item.setAuditLogId(dbAuditLog.getId());
                        JocInventory.insertConfiguration(dbLayer, item);
                    }
                } else {
                    for (DBItemInventoryConfiguration item : oldDBFolderContent) {
                        item.setAuditLogId(dbAuditLog.getId());
                        String json = item.getContent();
                        switch (item.getTypeAsEnum()) {
                        case WORKFLOW:
                            for (Map.Entry<String, String> oldNewName : oldToNewName.getOrDefault(ConfigurationType.LOCK, Collections.emptyMap())
                                    .entrySet()) {
                                json = json.replaceAll("(\"lockName\"\\s*:\\s*\")" + oldNewName.getKey() + "\"", "$1" + oldNewName.getValue() + "\"");
                            }
                            for (Map.Entry<String, String> oldNewName : oldToNewName.getOrDefault(ConfigurationType.BOARD, Collections.emptyMap())
                                    .entrySet()) {
                                json = json.replaceAll("(\"boardName\"\\s*:\\s*\")" + oldNewName.getKey() + "\"", "$1" + oldNewName.getValue() + "\"");
                            }
                            Map<String, String> oldNewJobResourceNames = oldToNewName.getOrDefault(ConfigurationType.JOBRESOURCE, Collections.emptyMap());
                            if (oldNewJobResourceNames.size() > 0) {
                                Workflow w = Globals.objectMapper.readValue(json, Workflow.class);
                                // JobResources on Workflow level
                                if (w.getJobResourceNames() != null) {
                                    w.setJobResourceNames(w.getJobResourceNames().stream().map(s -> oldNewJobResourceNames.getOrDefault(s, s))
                                            .collect(Collectors.toList()));
                                }
                                // JobResources on Job level
                                if (w.getJobs() != null) {
                                    w.getJobs().getAdditionalProperties().forEach((k, v) -> {
                                        if (v.getJobResourceNames() != null) {
                                            v.setJobResourceNames(v.getJobResourceNames().stream().map(s -> oldNewJobResourceNames.getOrDefault(s, s))
                                                    .collect(Collectors.toList()));
                                        }
                                    });
                                    json = Globals.objectMapper.writeValueAsString(w);
                                }
                            }
                            break;
                        case FILEORDERSOURCE:
                            for (Map.Entry<String, String> oldNewName : oldToNewName.getOrDefault(ConfigurationType.WORKFLOW, Collections.emptyMap())
                                    .entrySet()) {
                                json = json.replaceAll("(\"workflowName\"\\s*:\\s*\")" + oldNewName.getKey() + "\"", "$1" + oldNewName.getValue()
                                        + "\"");
                            }
                            break;
                        case SCHEDULE:
                            for (Map.Entry<String, String> oldNewName : oldToNewName.getOrDefault(ConfigurationType.WORKFLOW, Collections.emptyMap())
                                    .entrySet()) {
                                json = json.replaceAll("(\"workflowName\"\\s*:\\s*\")" + oldNewName.getKey() + "\"", "$1" + oldNewName.getValue()
                                        + "\"");
                            }
                            for (Map.Entry<String, String> oldNewName : oldToNewName.getOrDefault(ConfigurationType.WORKINGDAYSCALENDAR, Collections
                                    .emptyMap()).entrySet()) {
                                json = json.replaceAll("(\"calendarName\"\\s*:\\s*\")" + oldNewName.getKey() + "\"", "$1" + oldNewName.getValue()
                                        + "\"");
                            }
                            for (Map.Entry<String, String> oldNewName : oldToNewName.getOrDefault(ConfigurationType.NONWORKINGDAYSCALENDAR,
                                    Collections.emptyMap()).entrySet()) {
                                json = json.replaceAll("(\"calendarName\"\\s*:\\s*\")" + oldNewName.getKey() + "\"", "$1" + oldNewName.getValue()
                                        + "\"");
                            }
                            break;
                        default:
                            break;
                        }
                        item.setContent(json);
                        JocInventory.insertConfiguration(dbLayer, item);
                    }
                }
                
                events = Collections.singleton(newPathWithoutFix);
                folderEvents = Collections.singleton(newFolder);
                
            } else {
                if (!folderPermissions.isPermittedForFolder(newFolder)) {
                    throw new JocFolderPermissionsException("Access denied for folder: " + newFolder);
                }
                
                java.nio.file.Path p = pWithoutFix;
                if (!suffixPrefix.getSuffix().isEmpty() || !suffixPrefix.getPrefix().isEmpty()) {
                    p = pWithoutFix.getParent().resolve(pWithoutFix.getFileName().toString().replaceFirst(replace.get(0), replace.get(1)));
                }
                // Check Java variable name rules
                for (int i = 0; i < p.getNameCount(); i++) {
                    if (i == p.getNameCount() - 1) {
                        CheckJavaVariableName.test("name", p.getName(i).toString());
                    } else {
                        CheckJavaVariableName.test("folder", p.getName(i).toString());
                    }
                }
                String newPath = p.toString().replace('\\', '/');
                DBItemInventoryConfiguration targetItem = dbLayer.getConfiguration(newPath, config.getType());

                if (targetItem != null) { // this can occur if prefix/suffix is badly chosen
                    throw new JocObjectAlreadyExistException(String.format("%s %s already exists", ConfigurationType.fromValue(config.getType())
                            .value().toLowerCase(), targetItem.getPath()));
                } else {
                    // check unique name - this can occur if prefix/suffix is badly chosen
                    List<DBItemInventoryConfiguration> namedItems = dbLayer.getConfigurationByName(p.getFileName().toString(), config.getType());
                    if (namedItems != null) {
                        namedItems.remove(config);
                        if (!namedItems.isEmpty()) {
                            throw new JocObjectAlreadyExistException(String.format("The name has to be unique: '%s' is already used in '%s'", p
                                    .getFileName().toString(), namedItems.get(0).getPath()));
                        }
                    }
                }
                
                DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog(), Collections.singleton(new AuditLogDetail(oldPath,
                        config.getType())));
                DBItemInventoryConfiguration newDbItem = createItem(config, p);
                //createAuditLog(newDbItem, in.getAuditLog());
                newDbItem.setAuditLogId(dbAuditLog.getId());
                JocInventory.insertConfiguration(dbLayer, newDbItem);
                JocInventory.makeParentDirs(dbLayer, p.getParent(), newDbItem.getAuditLogId());
                response.setId(newDbItem.getId());
                response.setPath(newDbItem.getPath());
                events = Collections.singleton(newDbItem.getFolder());
            }

            session.commit();
            for (String event : events) {
                JocInventory.postEvent(event);
            }
            for (String event : folderEvents) {
                JocInventory.postFolderEvent(event);
            }

            response.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(response);
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
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
        item.setTitle(oldItem.getTitle());
        item.setType(oldItem.getType());
        item.setValid(oldItem.getValid());
        item.setContent(oldItem.getContent());
        return item;
    }

}
