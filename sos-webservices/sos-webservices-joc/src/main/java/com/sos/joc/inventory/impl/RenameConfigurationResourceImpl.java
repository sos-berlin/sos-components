package com.sos.joc.inventory.impl;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
            
            final java.nio.file.Path p = Paths.get(config.getFolder()).resolve(in.getNewPath()).normalize();
            String newFolder = p.getParent().toString().replace('\\', '/');
            String newPath = p.toString().replace('\\', '/');
            
            if (config.getPath().equals(newPath)) { // Nothing to do
                return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
            }
            
            if (!folderPermissions.isPermittedForFolder(newFolder)) {
                throw new JocFolderPermissionsException("Access denied for folder: " + newFolder);
            }
            
            // Check Java variable name rules
            for (int i = 0; i < p.getNameCount(); i++) {
                if (i == p.getNameCount() - 1) {
                    CheckJavaVariableName.test("name", p.getName(i).toString());
                } else {
                    CheckJavaVariableName.test("folder", p.getName(i).toString());
                }
            }

            if (config.getType() == ConfigurationType.FOLDER.intValue()) {
                List<DBItemInventoryConfiguration> oldDBFolderContent = dbLayer.getFolderContent(config.getPath(), true, null);
                final java.nio.file.Path oldPath = Paths.get(config.getPath()); 
                oldDBFolderContent = oldDBFolderContent.stream().map(oldItem -> {
                    setItem(oldItem, p.resolve(oldPath.relativize(Paths.get(oldItem.getPath()))));
                    return oldItem;
                }).collect(Collectors.toList());
                DBItemInventoryConfiguration newItem = dbLayer.getConfiguration(newPath, config.getType());
                List<DBItemInventoryConfiguration> newDBFolderContent = dbLayer.getFolderContent(newPath, true, null);

                if (newDBFolderContent != null && !newDBFolderContent.isEmpty()) {
                    newDBFolderContent.retainAll(oldDBFolderContent);
                    if (!newDBFolderContent.isEmpty()) {
                        if (in.getOverwrite()) {
                            for (DBItemInventoryConfiguration targetItem : newDBFolderContent) {
                                session.delete(targetItem);
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
                                session.delete(targetItem);
                            }
                        }
                    }
                }
                if (newItem != null) {
                    session.delete(newItem);
                }
                
                setItem(config, p);
                createAuditLog(config);
                session.update(config);
                JocInventory.makeParentDirs(dbLayer, p.getParent(), config.getAuditLogId());
                for (DBItemInventoryConfiguration item : oldDBFolderContent) {
                    session.update(item);
                }
                
            } else {
                DBItemInventoryConfiguration targetItem = dbLayer.getConfiguration(newPath, config.getType());
                
                if (targetItem != null) {
                    if (in.getOverwrite()) {
                        session.delete(targetItem);
                    } else {
                        throw new JocObjectAlreadyExistException(String.format("%s %s already exists", ConfigurationType.fromValue(config.getType())
                                .value().toLowerCase(), targetItem.getPath()));
                    }
                }
                setItem(config, p);
                createAuditLog(config);
                session.update(config);
                JocInventory.makeParentDirs(dbLayer, p.getParent(), config.getAuditLogId());
            }
            
            session.commit();

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
