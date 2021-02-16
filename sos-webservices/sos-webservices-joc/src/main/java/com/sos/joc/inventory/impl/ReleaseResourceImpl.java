package com.sos.joc.inventory.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.InventoryAudit;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IReleaseResource;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.common.IReleaseObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.release.ReleaseFilter;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;

@Path(JocInventory.APPLICATION_PATH)
public class ReleaseResourceImpl extends JOCResourceImpl implements IReleaseResource {
    
    @Override
    public JOCDefaultResponse release(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, ReleaseFilter.class);
            ReleaseFilter in = Globals.objectMapper.readValue(inBytes, ReleaseFilter.class);

            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getInventory().getConfigurations().isEdit());

            if (response == null) {
                response = release(in, true);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private JOCDefaultResponse release(ReleaseFilter in, boolean withDeletionOfEmptyFolders) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            session.setAutoCommit(false);
            
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();
            List<Err419> errors = new ArrayList<>();
            
            if (in.getDelete() != null && !in.getDelete().isEmpty()) {
                errors.addAll(in.getDelete().stream().filter(Objects::nonNull).map(requestFilter -> {
                    Either<Err419, Void> either = null;
                    try {
                        DBItemInventoryConfiguration conf = JocInventory.getConfiguration(dbLayer, requestFilter, folderPermissions);
                        createAuditLog(conf, conf.getTypeAsEnum());
                        if (ConfigurationType.FOLDER.intValue() == conf.getType()) {
                            deleteReleasedFolder(conf, dbLayer, withDeletionOfEmptyFolders);
                            JocInventory.postEvent(conf.getFolder());
                        } else if (!JocInventory.isReleasable(conf.getTypeAsEnum())) {
                            throw new JobSchedulerInvalidResponseDataException(String.format("%s is not a 'Scheduling Object': %s", conf.getPath(),
                                    conf.getTypeAsEnum()));
                        } else {
                            deleteReleasedObject(conf, dbLayer);
                            JocInventory.postEvent(conf.getFolder());
                        }
                        either = Either.right(null);
                    } catch (DBMissingDataException ex) {
                        // ignore missing objects at deletion
                        either = Either.right(null);
                    } catch (Exception ex) {
                        if (requestFilter.getPath() != null) {
                            either = Either.left(new BulkError().get(ex, getJocError(), requestFilter.getPath()));
                        } else {
                            either = Either.left(new BulkError().get(ex, getJocError(), "Id: " + requestFilter.getId()));
                        }
                    }
                    return either;
                }).filter(e -> e.isLeft()).map(e -> e.getLeft()).collect(Collectors.toList()));
            }
            
            if (in.getUpdate() != null && !in.getUpdate().isEmpty()) {
                errors.addAll(in.getUpdate().stream().filter(Objects::nonNull).map(requestFilter -> {
                    Either<Err419, Void> either = null;
                    try {
                        DBItemInventoryConfiguration conf = JocInventory.getConfiguration(dbLayer, requestFilter, folderPermissions);
                        createAuditLog(conf, conf.getTypeAsEnum());
                        if (ConfigurationType.FOLDER.intValue() == conf.getType()) {
                            updateReleasedFolder(conf, dbLayer);
                            JocInventory.postEvent(conf.getFolder());
                        } else if (!JocInventory.isReleasable(conf.getTypeAsEnum())) {
                            throw new JobSchedulerInvalidResponseDataException(String.format("%s is not a 'Scheduling Object': %s", conf.getPath(),
                                    conf.getTypeAsEnum()));
                        } else {
                            updateReleasedObject(conf, dbLayer);
                            JocInventory.postEvent(conf.getFolder());
                        }
                        either = Either.right(null);
                    } catch (Exception ex) {
                        if (requestFilter.getPath() != null) {
                            either = Either.left(new BulkError().get(ex, getJocError(), requestFilter.getPath()));
                        } else {
                            either = Either.left(new BulkError().get(ex, getJocError(), "Id: " + requestFilter.getId()));
                        }
                    }
                    return either;
                    
                }).filter(e -> e.isLeft()).map(e -> e.getLeft()).collect(Collectors.toList()));
            }

            if (errors != null && !errors.isEmpty()) {
                Globals.rollback(session);
                return JOCDefaultResponse.responseStatus419(errors);
            }
            Globals.commit(session);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private void createAuditLog(DBItemInventoryConfiguration config, ConfigurationType objectType) throws Exception {
        InventoryAudit audit = new InventoryAudit(objectType, config.getPath(), config.getFolder());
        logAuditMessage(audit);
        DBItemJocAuditLog auditItem = storeAuditLogEntry(audit);
        if (auditItem != null) {
            config.setAuditLogId(auditItem.getId());
        }
    }
    
    private void updateReleasedFolder(DBItemInventoryConfiguration conf, InventoryDBLayer dbLayer) throws SOSHibernateException, JsonParseException,
            JsonMappingException, IOException {
        List<DBItemInventoryConfiguration> folderContent = dbLayer.getFolderContent(conf.getPath(), true, JocInventory.getReleasableTypes());
        Date now = Date.from(Instant.now());

        // quick and dirty TODO version with more performance
        if (folderContent != null && !folderContent.isEmpty()) {
            for (DBItemInventoryConfiguration item : folderContent) {
                if (item.getReleased() || !item.getValid()) {
                    continue;
                }
                updateReleasedObject(item, dbLayer, now);
            }
        }
    }
    
    private static void updateReleasedObject(DBItemInventoryConfiguration conf, InventoryDBLayer dbLayer, Date now)
            throws SOSHibernateException, JsonParseException, JsonMappingException, IOException {
        DBItemInventoryReleasedConfiguration releaseItem = dbLayer.getReleasedItemByConfigurationId(conf.getId());
        DBItemInventoryReleasedConfiguration contraintReleaseItem = dbLayer.getReleasedConfiguration(conf.getName(), conf.getType());
        
        if (releaseItem == null) {
            if (contraintReleaseItem != null) {
                dbLayer.getSession().delete(contraintReleaseItem);
            }
            DBItemInventoryReleasedConfiguration release = setReleaseItem(null, conf, now);
            dbLayer.getSession().save(release);
        } else {
            if (contraintReleaseItem != null && contraintReleaseItem.getId() != releaseItem.getId()) {
                dbLayer.getSession().delete(contraintReleaseItem);
            }
            DBItemInventoryReleasedConfiguration release = setReleaseItem(releaseItem.getId(), conf, now);
            dbLayer.getSession().update(release);
        }
        conf.setReleased(true);
        conf.setModified(now);
        dbLayer.getSession().update(conf);
    }

    private static void updateReleasedObject(DBItemInventoryConfiguration conf, InventoryDBLayer dbLayer)
            throws SOSHibernateException, JsonParseException, JsonMappingException, IOException {
        updateReleasedObject(conf, dbLayer, Date.from(Instant.now()));
    }
    
    private static DBItemInventoryReleasedConfiguration setReleaseItem(Long releaseId, DBItemInventoryConfiguration conf, Date now) throws JsonParseException, JsonMappingException, IOException {
        DBItemInventoryReleasedConfiguration release = new DBItemInventoryReleasedConfiguration();
        release.setId(releaseId);
        release.setAuditLogId(conf.getAuditLogId());
        release.setCid(conf.getId());
        IReleaseObject r = (IReleaseObject) Globals.objectMapper.readValue(conf.getContent(), JocInventory.CLASS_MAPPING.get(conf.getTypeAsEnum()));
        r.setPath(conf.getPath());
        release.setContent(Globals.objectMapper.writeValueAsString(r));
        release.setCreated(now);
        release.setDocumentationId(conf.getDocumentationId());
        release.setFolder(conf.getFolder());
        release.setModified(now);
        release.setName(conf.getName());
        release.setPath(conf.getPath());
        release.setTitle(conf.getTitle());
        release.setType(conf.getType());
        return release;
    }

    private static void deleteReleasedFolder(DBItemInventoryConfiguration conf, InventoryDBLayer dbLayer, boolean withDeletionOfEmptyFolders) throws SOSHibernateException {
        List<DBItemInventoryConfiguration> folderContent = dbLayer.getFolderContent(conf.getPath(), true, JocInventory.getReleasableTypes());
        
        if (folderContent != null && !folderContent.isEmpty()) {
            // delete in INV_RELEASED_CONFIGURATION
//            Globals.beginTransaction(dbLayer.getSession());
//            try {
                dbLayer.deleteReleasedItemsByConfigurationIds(folderContent.stream().map(DBItemInventoryConfiguration::getId).collect(Collectors.toSet()));
                Globals.commit(dbLayer.getSession());
//            } catch (Exception e) {
//                Globals.rollback(dbLayer.getSession());
//                throw e;
//            }
            for (DBItemInventoryConfiguration item : folderContent) {
                // delete releasable objects in INV_CONFIGURATION
                dbLayer.getSession().delete(item);
            }
        }
        if (withDeletionOfEmptyFolders) {
            JocInventory.deleteEmptyFolders(dbLayer, conf);
        }
        
    }

    private static void deleteReleasedObject(DBItemInventoryConfiguration conf, InventoryDBLayer dbLayer) throws SOSHibernateException {
//        Globals.beginTransaction(dbLayer.getSession());
//        try {
            dbLayer.deleteReleasedItemsByConfigurationIds(Arrays.asList(conf.getId()));
            Globals.commit(dbLayer.getSession());
//        } catch (Exception e) {
//            Globals.rollback(dbLayer.getSession());
//            throw e;
//        }
        dbLayer.getSession().delete(conf);
    }
}
