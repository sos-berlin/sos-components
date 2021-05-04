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
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.inventory.resource.IRenameConfigurationResource;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ResponseNewPath;
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

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
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
            
            if (JocInventory.isFolder(type) && JocInventory.ROOT_FOLDER.equals(config.getPath())) {
                throw new JocFolderPermissionsException("Root folder cannot be renamed");
            }
            
            final java.nio.file.Path oldPath = Paths.get(config.getPath());
            final java.nio.file.Path p = Paths.get(config.getFolder()).resolve(in.getNewPath()).normalize();
            String newFolder = p.getParent().toString().replace('\\', '/');
            String newPath = p.toString().replace('\\', '/');
            boolean isRename = !oldPath.getFileName().toString().equals(p.getFileName().toString());
            Set<String> events = new HashSet<>();
            
            ResponseNewPath response = new ResponseNewPath();
            response.setObjectType(type);
            
            if (config.getPath().equals(newPath)) { // Nothing to do
                response.setPath(newPath);
                response.setId(config.getId());
                response.setDeliveryDate(Date.from(Instant.now()));
                return JOCDefaultResponse.responseStatus200(response);
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
            Long auditLogId = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());

            if (JocInventory.isFolder(type)) {
                List<DBItemInventoryConfiguration> oldDBFolderContent = dbLayer.getFolderContent(config.getPath(), true, null);
                oldDBFolderContent = oldDBFolderContent.stream().map(oldItem -> {
                    setItem(oldItem, p.resolve(oldPath.relativize(Paths.get(oldItem.getPath()))), auditLogId);
                    return oldItem;
                }).collect(Collectors.toList());
                DBItemInventoryConfiguration newItem = dbLayer.getConfiguration(newPath, ConfigurationType.FOLDER.intValue());
                List<DBItemInventoryConfiguration> newDBFolderContent = dbLayer.getFolderContent(newPath, true, null);
                Set<Long> deletedIds = new HashSet<>();

                if (newDBFolderContent != null && !newDBFolderContent.isEmpty()) {
                    newDBFolderContent.retainAll(oldDBFolderContent);
                    if (!newDBFolderContent.isEmpty()) {
                        Map<Boolean, List<DBItemInventoryConfiguration>> map = newDBFolderContent.stream().collect(Collectors.groupingBy(
                                item -> ConfigurationType.FOLDER.intValue() == item.getType()));
                        if (!map.getOrDefault(false, Collections.emptyList()).isEmpty()) { // all not folder items
                            throw new JocObjectAlreadyExistException("Cannot move to " + newPath + ": common objects are " + map.get(false).stream()
                                    .map(DBItemInventoryConfiguration::getPath).collect(Collectors.joining("', '", "'", "'")));
                        }
                        // delete all folder items
                        for (DBItemInventoryConfiguration targetItem : map.getOrDefault(true, Collections.emptyList())) {
                            deletedIds.add(targetItem.getId());
                            JocInventory.deleteConfiguration(dbLayer, targetItem);
                        }
                    }
                }
                
                if (newItem != null) {
                    deletedIds.add(newItem.getId());
                    JocInventory.deleteConfiguration(dbLayer, newItem);
                }
                
                setItem(config, p, auditLogId);
                if (deletedIds.remove(config.getId())) {
                    config.setId(null);
                    JocInventory.insertConfiguration(dbLayer, config);
                } else {
                    JocInventory.updateConfiguration(dbLayer, config);
                }
                
                JocInventory.makeParentDirs(dbLayer, p.getParent(), config.getAuditLogId());
                for (DBItemInventoryConfiguration item : oldDBFolderContent) {
                    if (deletedIds.remove(item.getId())) {
                        config.setId(null);
                        JocInventory.insertConfiguration(dbLayer, item);
                    } else {
                        JocInventory.updateConfiguration(dbLayer, item);
                    }
                }
                response.setPath(config.getPath());
                response.setId(config.getId());
                events.add(config.getFolder());
                
            } else {
                if (!newPath.equalsIgnoreCase(config.getPath())) { //if not only upper-lower case is changed then check if target exists
                    DBItemInventoryConfiguration targetItem = dbLayer.getConfiguration(newPath, config.getType());
                    
                    if (targetItem != null) {
                        throw new JocObjectAlreadyExistException(String.format("%s %s already exists", ConfigurationType.fromValue(config.getType())
                                .value().toLowerCase(), targetItem.getPath()));
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
                
                events.addAll(JocInventory.deepCopy(config, p.getFileName().toString(), dbLayer));
                
                setItem(config, p, auditLogId);
                JocInventory.updateConfiguration(dbLayer, config);
                JocInventory.makeParentDirs(dbLayer, p.getParent(), config.getAuditLogId());
                response.setPath(config.getPath());
                response.setId(config.getId());
                events.add(config.getFolder());
            }
            
            session.commit();
            for (String event : events) {
                JocInventory.postEvent(event);
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
    
    private static void setItem(DBItemInventoryConfiguration oldItem, java.nio.file.Path newItem, Long auditLogId) {
        oldItem.setPath(newItem.toString().replace('\\', '/'));
        oldItem.setFolder(newItem.getParent().toString().replace('\\', '/'));
        oldItem.setName(newItem.getFileName().toString());
        oldItem.setDeployed(false);
        oldItem.setReleased(false);
        oldItem.setAuditLogId(auditLogId);
        oldItem.setModified(Date.from(Instant.now()));
    }

}
