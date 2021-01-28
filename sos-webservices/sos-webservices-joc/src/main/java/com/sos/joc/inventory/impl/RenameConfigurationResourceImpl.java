package com.sos.joc.inventory.impl;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.CheckJavaVariableName;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.InventoryAudit;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.inventory.resource.IRenameConfigurationResource;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.rename.RequestFilter;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class RenameConfigurationResourceImpl extends JOCResourceImpl implements IRenameConfigurationResource {

    @Override
    public JOCDefaultResponse rename(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);

            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getInventory().getConfigurations().isEdit());
            if (response == null) {
                response = rename(in);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private JOCDefaultResponse rename(RequestFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            
            session.beginTransaction();
            DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, in, folderPermissions);
            ConfigurationType type = config.getTypeAsEnum();
            
            final java.nio.file.Path oldPath = Paths.get(config.getPath());
            final java.nio.file.Path p = Paths.get(config.getFolder()).resolve(in.getNewPath()).normalize();
            String newFolder = p.getParent().toString().replace('\\', '/');
            String newPath = p.toString().replace('\\', '/');
            boolean isRename = !oldPath.getFileName().toString().equals(p.getFileName().toString());
            Set<String> events = new HashSet<>();
            
            if (config.getPath().equals(newPath)) { // Nothing to do
                return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
            }
            
            // Check folder permissions
            if (JocInventory.isFolder(type)) {
                if (!folderPermissions.isPermittedForFolder(newPath)) {
                    throw new JocFolderPermissionsException("Access denied for folder: " + newPath);
                }
            } else {
                if (!folderPermissions.isPermittedForFolder(newFolder)) {
                    throw new JocFolderPermissionsException("Access denied for folder: " + newFolder);
                }
            }
            
            // Check Java variable name rules
            for (int i = 0; i < p.getNameCount(); i++) {
                if (i == p.getNameCount() - 1) {
                    CheckJavaVariableName.test("name", p.getName(i).toString());
                } else {
                    CheckJavaVariableName.test("folder", p.getName(i).toString());
                }
            }

            if (JocInventory.isFolder(type)) {
                List<DBItemInventoryConfiguration> oldDBFolderContent = dbLayer.getFolderContent(config.getPath(), true, null);
                oldDBFolderContent = oldDBFolderContent.stream().map(oldItem -> {
                    setItem(oldItem, p.resolve(oldPath.relativize(Paths.get(oldItem.getPath()))));
                    return oldItem;
                }).collect(Collectors.toList());
                DBItemInventoryConfiguration newItem = dbLayer.getConfiguration(newPath, ConfigurationType.FOLDER.intValue());
                List<DBItemInventoryConfiguration> newDBFolderContent = dbLayer.getFolderContent(newPath, true, null);

                if (newDBFolderContent != null && !newDBFolderContent.isEmpty()) {
                    newDBFolderContent.retainAll(oldDBFolderContent);
                    if (!newDBFolderContent.isEmpty()) {
                        if (in.getOverwrite()) {
                            for (DBItemInventoryConfiguration targetItem : newDBFolderContent) {
                                JocInventory.deleteConfiguration(dbLayer, targetItem);
                            }
                        } else {
                            Map<Boolean, List<DBItemInventoryConfiguration>> map = newDBFolderContent.stream().collect(Collectors.groupingBy(
                                    item -> ConfigurationType.FOLDER.intValue() == item.getType()));
                            if (!map.getOrDefault(false, Collections.emptyList()).isEmpty()) { // all not folder items
                                throw new JocObjectAlreadyExistException("Cannot move to " + newPath + ": common objects are " + map.get(false)
                                        .stream().map(DBItemInventoryConfiguration::getPath).collect(Collectors.joining("', '", "'", "'")));
                            }
                            // delete all folder items
                            for (DBItemInventoryConfiguration targetItem : map.getOrDefault(true, Collections.emptyList())) {
                                JocInventory.deleteConfiguration(dbLayer, targetItem);
                            }
                        }
                    }
                }
                if (newItem != null) {
                    JocInventory.deleteConfiguration(dbLayer, config);
                }
                
                setItem(config, p);
                createAuditLog(config);
                JocInventory.updateConfiguration(dbLayer, config);
                JocInventory.makeParentDirs(dbLayer, p.getParent(), config.getAuditLogId());
                for (DBItemInventoryConfiguration item : oldDBFolderContent) {
                    JocInventory.updateConfiguration(dbLayer, item);
                }
                events.add(config.getFolder());
                
            } else {
                if (!newPath.equalsIgnoreCase(config.getPath())) { //if not only upper-lower case is changed then check if target exists
                    DBItemInventoryConfiguration targetItem = dbLayer.getConfiguration(newPath, config.getType());
                    
                    if (targetItem != null) {
                        if (in.getOverwrite()) {
                            JocInventory.deleteConfiguration(dbLayer, targetItem);
                        } else {
                            throw new JocObjectAlreadyExistException(String.format("%s %s already exists", ConfigurationType.fromValue(config.getType())
                                    .value().toLowerCase(), targetItem.getPath()));
                        }
                    } else {
                        // check unique name
                        List<DBItemInventoryConfiguration> namedItems = dbLayer.getConfigurationByName(p.getFileName().toString(), config.getType());
                        if (namedItems != null) {
                            namedItems.remove(config);
                            if (!namedItems.isEmpty()) {
                                throw new JocObjectAlreadyExistException(String.format("The name has to be unique: '%s' is already used in '%s'", p
                                        .getFileName().toString(), namedItems.get(0).getPath()));
                            }
                        }
                    }
                }
                
                
                if (isRename) {  // deep rename if necessary
                    switch (type) {
                    case LOCK: // determine Workflows with Lock instructions
                        List<DBItemInventoryConfiguration> workflows = dbLayer.getUsedWorkflowsByLockId(config.getName());
                        if (workflows != null && !workflows.isEmpty()) {
                            for (DBItemInventoryConfiguration workflow : workflows) {
                                workflow.setContent(workflow.getContent().replaceAll("(\"lockId\"\\s*:\\s*\")" + config.getName() + "\"", "$1" + p
                                        .getFileName() + "\""));
                                workflow.setDeployed(false);
                                JocInventory.updateConfiguration(dbLayer, workflow);
                                events.add(workflow.getFolder());
                            }
                        }
                        break;
                    case WORKFLOW: // determine Schedules with Workflow reference
                        List<DBItemInventoryConfiguration> schedules = dbLayer.getUsedSchedulesByWorkflowName(config.getName()); // TODO config.getName() if possible
                        if (schedules != null && !schedules.isEmpty()) {
                            for (DBItemInventoryConfiguration schedule : schedules) {
                                schedule.setContent(schedule.getContent().replaceAll("(\"workflowName\"\\s*:\\s*\")" + config.getName() + "\"", "$1" + p
                                        .getFileName() + "\""));
                                schedule.setReleased(false);
                                JocInventory.updateConfiguration(dbLayer, schedule);
                                events.add(schedule.getFolder());
                            }
                        }
                        break;
                    case WORKINGDAYSCALENDAR: // determine Schedules with Calendar reference
                    case NONWORKINGDAYSCALENDAR:
                        List<DBItemInventoryConfiguration> schedules1 = dbLayer.getUsedSchedulesByCalendarName(config.getName());
                        if (schedules1 != null && !schedules1.isEmpty()) {
                            for (DBItemInventoryConfiguration schedule : schedules1) {
                                schedule.setContent(schedule.getContent().replaceAll("(\"calendarName\"\\s*:\\s*\")" + config.getName() + "\"", "$1" + p
                                        .getFileName() + "\""));
                                schedule.setReleased(false);
                                JocInventory.updateConfiguration(dbLayer, schedule);
                                events.add(schedule.getFolder());
                            }
                        }
                        break;
                    default:
                        break;
                    }
                }
                
                setItem(config, p);
                createAuditLog(config);
                JocInventory.updateConfiguration(dbLayer, config);
                JocInventory.makeParentDirs(dbLayer, p.getParent(), config.getAuditLogId());
                events.add(config.getFolder());
            }
            
            session.commit();
            for (String event : events) {
                JocInventory.postEvent(event);
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private void createAuditLog(DBItemInventoryConfiguration config) throws Exception {
        InventoryAudit audit = new InventoryAudit(config.getTypeAsEnum(), config.getPath(), config.getFolder());
        logAuditMessage(audit);
        DBItemJocAuditLog auditItem = storeAuditLogEntry(audit);
        if (auditItem != null) {
            config.setAuditLogId(auditItem.getId());
        }
    }

    private static void setItem(DBItemInventoryConfiguration oldItem, java.nio.file.Path newItem) {
        oldItem.setPath(newItem.toString().replace('\\', '/'));
        oldItem.setFolder(newItem.getParent().toString().replace('\\', '/'));
        oldItem.setName(newItem.getFileName().toString());
        oldItem.setDeployed(false);
        oldItem.setReleased(false);
        oldItem.setModified(Date.from(Instant.now()));
//        if (!SOSString.isEmpty(item.getContent())) {
//            ConfigurationType type = ConfigurationType.fromValue(item.getType());
//            try {
//                switch (type) {
//                case JOB:
//                    //obsolete: don't have path in json to update
//                    break;
//                case FOLDER:
//                    //obsolete: don't have path in json to update
//                    // but all item recursive get a new path, folder, ...
//                    throw new JocNotImplementedException("renaming of a folder is not yet implemented!");
//                    //break;
//                default:
//                    IConfigurationObject obj = (IConfigurationObject) Globals.objectMapper.readValue(item.getContent(),
//                            JocInventory.CLASS_MAPPING.get(type));
//                    obj.setPath(item.getPath());
//                    oldItem.setContent(Globals.objectMapper.writeValueAsString(obj));
//                    break;
//                }
//            } catch (Throwable e) {
//                LOGGER.error(String.format("[%s]%s", config.getContent(), e.toString()), e);
//            }
//        }
    }

}
