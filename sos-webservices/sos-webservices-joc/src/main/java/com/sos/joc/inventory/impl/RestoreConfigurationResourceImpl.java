package com.sos.joc.inventory.impl;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
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
import com.sos.joc.db.inventory.DBItemInventoryConfigurationTrash;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.inventory.resource.IRestoreConfigurationResource;
import com.sos.joc.model.SuffixPrefix;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.restore.RequestFilter;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class RestoreConfigurationResourceImpl extends JOCResourceImpl implements IRestoreConfigurationResource {

    @Override
    public JOCDefaultResponse restore(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(TRASH_IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);

            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getInventory().getConfigurations().isEdit());
            if (response == null) {
                response = restore(in);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private JOCDefaultResponse restore(RequestFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(TRASH_IMPL_PATH);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            
            session.beginTransaction();
            DBItemInventoryConfigurationTrash config = JocInventory.getTrashConfiguration(dbLayer, in, folderPermissions);
            ConfigurationType type = config.getTypeAsEnum();
            
            final java.nio.file.Path pWithoutFix = Paths.get(config.getPath());
            // without any prefix/suffix
//            String trashFolder = pWithoutFix.getParent().toString().replace('\\', '/');
//            String trashPathWithoutFix = pWithoutFix.toString().replace('\\', '/');
            
            SuffixPrefix fix = Globals.restoreSuffixPrefix;
            String prefix = in.getPrefix() == null ? "" : in.getPrefix().trim().replaceFirst("-+$", "");
            String suffix = in.getSuffix() == null ? "" : in.getSuffix().trim().replaceFirst("^-+", "");
            String name = JocInventory.isFolder(type) ? null : config.getName();
            
            if (!suffix.isEmpty()) { // suffix beats prefix
                prefix = "";
            } else if (prefix.isEmpty()) {
                suffix = fix.getSuffix();
                if (suffix.isEmpty()) {
                    prefix = fix.getPrefix();
                }
            }
            if (suffix.isEmpty() && prefix.isEmpty()) {
                suffix = JocInventory.DEFAULT_RESTORE_SUFFIX;
            }
            
            if (!suffix.isEmpty()) {
                CheckJavaVariableName.test("suffix", suffix);
                // determine number of suffix "-suffix<number>"
                Integer num = dbLayer.getSuffixNumber(suffix, name, config.getType());
                if (num > 0) {
                    suffix += num;
                }
            } else if (!prefix.isEmpty()) {
                CheckJavaVariableName.test("prefix", prefix);
                // determine number of prefix "prefix<number>-"
                Integer num = dbLayer.getPrefixNumber(prefix, name, config.getType());
                if (num > 0) {
                    prefix += num;
                }
            }
            
            final List<String> replace = suffix.isEmpty() ? Arrays.asList("^(" + prefix + "[0-9]*-)?(.*)$", prefix + "-$2") : Arrays.asList("(.*?)(-"
                    + suffix + "[0-9]*)?$", "$1-" + suffix);
            Set<String> events = new HashSet<>();
            
            if (JocInventory.isFolder(type)) {
                
                List<DBItemInventoryConfigurationTrash> trashDBFolderContent = dbLayer.getTrashFolderContent(config.getPath(), true, null);
                
                List<DBItemInventoryConfiguration> newDBFolderContent = trashDBFolderContent.stream().map(trashItem -> {
                    java.nio.file.Path invItemPath = Paths.get(trashItem.getPath());
                    if (ConfigurationType.FOLDER.intValue() == trashItem.getType()) {
                        return createItem(trashItem, invItemPath);
                    }
                    return createItem(trashItem, invItemPath.getParent().resolve(trashItem.getName().replaceFirst(replace.get(0), replace.get(1))));
                }).collect(Collectors.toList());

                List<DBItemInventoryConfiguration> curDBFolderContent = dbLayer.getFolderContent(config.getPath(), true, null);
                
                if (curDBFolderContent != null && !curDBFolderContent.isEmpty()) {
                    curDBFolderContent.retainAll(newDBFolderContent);
                    if (!curDBFolderContent.isEmpty()) {
                        Map<Boolean, List<DBItemInventoryConfiguration>> map = curDBFolderContent.stream().collect(Collectors.groupingBy(
                                item -> ConfigurationType.FOLDER.intValue() == item.getType()));
                        if (!map.getOrDefault(false, Collections.emptyList()).isEmpty()) { // all not folder items
                            throw new JocObjectAlreadyExistException("Cannot restore to " + config.getPath() + ": common objects are " + map.get(false).stream()
                                    .map(DBItemInventoryConfiguration::getPath).collect(Collectors.joining("', '", "'", "'")));
                        }
                        
                        newDBFolderContent.removeAll(map.getOrDefault(true, Collections.emptyList()));
                    }
                }
                
                DBItemInventoryConfiguration newItem = dbLayer.getConfiguration(config.getPath(), ConfigurationType.FOLDER.intValue());
                
                Long auditLogId = createAuditLog(config);
                if (newItem == null) {
                    DBItemInventoryConfiguration newDbItem = createItem(config, pWithoutFix);
                    newDbItem.setAuditLogId(auditLogId);
                    JocInventory.insertConfiguration(dbLayer, newDbItem);
                    JocInventory.makeParentDirs(dbLayer, pWithoutFix.getParent(), newDbItem.getAuditLogId());
                }
                for (DBItemInventoryConfiguration item : newDBFolderContent) {
                    item.setAuditLogId(auditLogId);
                    JocInventory.insertConfiguration(dbLayer, item);
                }
                dbLayer.deleteTrashFolder(config.getPath());
                
                events.add(config.getFolder());
                
            } else {
                
                java.nio.file.Path p = pWithoutFix.getParent().resolve(pWithoutFix.getFileName().toString().replaceFirst(replace.get(0), replace.get(1)));
                
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
                
                Long auditLogId = createAuditLog(config);
                DBItemInventoryConfiguration newDbItem = createItem(config, p);
                newDbItem.setAuditLogId(auditLogId);
                JocInventory.insertConfiguration(dbLayer, newDbItem);
                JocInventory.makeParentDirs(dbLayer, p.getParent(), newDbItem.getAuditLogId());
                session.delete(config);
                events.add(newDbItem.getFolder());
            }

            session.commit();
            for (String event : events) {
                JocInventory.postEvent(event);
                JocInventory.postTrashEvent(event);
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private Long createAuditLog(DBItemInventoryConfigurationTrash config) throws Exception {
        InventoryAudit audit = new InventoryAudit(config.getTypeAsEnum(), config.getPath(), config.getFolder());
        logAuditMessage(audit);
        DBItemJocAuditLog auditItem = storeAuditLogEntry(audit);
        if (auditItem != null) {
            return auditItem.getId();
        }
        return 0L;
    }

    private static DBItemInventoryConfiguration createItem(DBItemInventoryConfigurationTrash oldItem, java.nio.file.Path newItem) {
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
        item.setValid(false); // TODO
        item.setContent(oldItem.getContent());
        return item;
    }

}
