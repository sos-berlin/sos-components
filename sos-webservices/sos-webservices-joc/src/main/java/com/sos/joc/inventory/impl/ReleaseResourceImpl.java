package com.sos.joc.inventory.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.rest.SOSShiroFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.ControllerInvalidResponseDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IReleaseResource;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.common.IReleaseObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestFilter;
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

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());

            if (response == null) {
                response = getReleaseResponse(in, true);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private JOCDefaultResponse getReleaseResponse(ReleaseFilter in, boolean withDeletionOfEmptyFolders) throws Exception {
        List<Err419> errors = release(in, getJocError(), withDeletionOfEmptyFolders);
        if (errors != null && !errors.isEmpty()) {
            return JOCDefaultResponse.responseStatus419(errors);
        }
        return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
    }

    private List<Err419> release(ReleaseFilter in, JocError jocError, boolean withDeletionOfEmptyFolders) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            session.setAutoCommit(false);
            
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            Globals.beginTransaction(session);
            List<Err419> errors = new ArrayList<>();
            
            DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());
            
            if (in.getDelete() != null && !in.getDelete().isEmpty()) {
                errors.addAll(delete(in.getDelete(), dbLayer, folderPermissions, getJocError(), dbAuditLog, withDeletionOfEmptyFolders));
            }

            if (in.getUpdate() != null && !in.getUpdate().isEmpty()) {
                errors.addAll(update(in.getUpdate(), dbLayer, folderPermissions, getJocError(), dbAuditLog, withDeletionOfEmptyFolders));
            }

            if (errors != null && !errors.isEmpty()) {
                Globals.rollback(session);
                return errors;
            }
            Globals.commit(session);
            return Collections.emptyList();
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private static List<Err419> delete(List<RequestFilter> toDelete, InventoryDBLayer dbLayer, SOSShiroFolderPermissions folderPermissions,
            JocError jocError, DBItemJocAuditLog dbAuditLog, boolean withDeletionOfEmptyFolders) {
        return toDelete.stream().filter(Objects::nonNull).map(requestFilter -> {
            Either<Err419, Void> either = null;
            try {
                DBItemInventoryConfiguration conf = JocInventory.getConfiguration(dbLayer, requestFilter, folderPermissions);
                delete(conf, dbLayer, dbAuditLog, withDeletionOfEmptyFolders, true);
                either = Either.right(null);
            } catch (DBMissingDataException ex) {
                // ignore missing objects at deletion
                either = Either.right(null);
            } catch (Exception ex) {
                if (requestFilter.getPath() != null) {
                    either = Either.left(new BulkError().get(ex, jocError, requestFilter.getPath()));
                } else {
                    either = Either.left(new BulkError().get(ex, jocError, "Id: " + requestFilter.getId()));
                }
            }
            return either;
        }).filter(e -> e.isLeft()).map(e -> e.getLeft()).collect(Collectors.toList());
    }
    
    public static void delete(DBItemInventoryConfiguration conf, InventoryDBLayer dbLayer, DBItemJocAuditLog dbAuditLog, boolean withDeletionOfEmptyFolders,
            boolean withEvent) throws SOSHibernateException {
    
        if (ConfigurationType.FOLDER.intValue() == conf.getType()) {
            deleteReleasedFolder(conf, dbLayer, dbAuditLog, withDeletionOfEmptyFolders);
            if (withEvent) {
                JocInventory.postEvent(conf.getFolder());
            }
        } else if (!JocInventory.isReleasable(conf.getTypeAsEnum())) {
            throw new ControllerInvalidResponseDataException(String.format("%s is not a 'Scheduling Object': %s", conf.getPath(), conf
                    .getTypeAsEnum()));
        } else {
            deleteReleasedObject(conf, dbAuditLog, dbLayer);
            if (withEvent) {
                JocInventory.postEvent(conf.getFolder());
            }
        }
    }
    
    public static List<Err419> update(List<RequestFilter> toUpdate, InventoryDBLayer dbLayer, SOSShiroFolderPermissions folderPermissions,
            JocError jocError, DBItemJocAuditLog dbAuditLog, boolean withDeletionOfEmptyFolders) {
        return toUpdate.stream().filter(Objects::nonNull).map(requestFilter -> {
            Either<Err419, Void> either = null;
            try {
                DBItemInventoryConfiguration conf = JocInventory.getConfiguration(dbLayer, requestFilter, folderPermissions);
                if (ConfigurationType.FOLDER.intValue() == conf.getType()) {
                    updateReleasedFolder(conf, dbLayer, dbAuditLog);
                    JocInventory.postEvent(conf.getFolder());
                } else if (!JocInventory.isReleasable(conf.getTypeAsEnum())) {
                    throw new ControllerInvalidResponseDataException(String.format("%s is not a 'Scheduling Object': %s", conf.getPath(), conf
                            .getTypeAsEnum()));
                } else {
                    updateReleasedObject(conf, dbLayer, dbAuditLog);
                    JocInventory.postEvent(conf.getFolder());
                }
                either = Either.right(null);
            } catch (Exception ex) {
                if (requestFilter.getPath() != null) {
                    either = Either.left(new BulkError().get(ex, jocError, requestFilter.getPath()));
                } else {
                    either = Either.left(new BulkError().get(ex, jocError, "Id: " + requestFilter.getId()));
                }
            }
            return either;

        }).filter(e -> e.isLeft()).map(e -> e.getLeft()).collect(Collectors.toList());
    }
    
    private static void updateReleasedFolder(DBItemInventoryConfiguration conf, InventoryDBLayer dbLayer, DBItemJocAuditLog dbAuditLog)
            throws SOSHibernateException, JsonParseException, JsonMappingException, IOException {
        List<DBItemInventoryConfiguration> folderContent = dbLayer.getFolderContent(conf.getPath(), true, JocInventory.getReleasableTypes());

        // quick and dirty TODO version with more performance
        if (folderContent != null && !folderContent.isEmpty()) {
            //createAuditLog(conf, conf.getTypeAsEnum(), auditLogger, auditParams);
            conf.setAuditLogId(dbAuditLog.getId());
            for (DBItemInventoryConfiguration item : folderContent) {
                if (item.getReleased() || !item.getValid()) {
                    continue;
                }
                updateReleasedObject(item, dbLayer, dbAuditLog);
            }
        }
    }
    
    private static void updateReleasedObject(DBItemInventoryConfiguration conf, InventoryDBLayer dbLayer, DBItemJocAuditLog dbAuditLog)
            throws SOSHibernateException, JsonParseException, JsonMappingException, IOException {
        conf.setAuditLogId(dbAuditLog.getId());
        DBItemInventoryReleasedConfiguration releaseItem = dbLayer.getReleasedItemByConfigurationId(conf.getId());
        DBItemInventoryReleasedConfiguration contraintReleaseItem = dbLayer.getReleasedConfiguration(conf.getName(), conf.getType());
        
        if (releaseItem == null) {
            if (contraintReleaseItem != null) {
                dbLayer.getSession().delete(contraintReleaseItem);
            }
            DBItemInventoryReleasedConfiguration release = setReleaseItem(null, conf, dbAuditLog.getCreated());
            dbLayer.getSession().save(release);
        } else {
            if (contraintReleaseItem != null && contraintReleaseItem.getId() != releaseItem.getId()) {
                dbLayer.getSession().delete(contraintReleaseItem);
            }
            DBItemInventoryReleasedConfiguration release = setReleaseItem(releaseItem.getId(), conf, dbAuditLog.getCreated());
            dbLayer.getSession().update(release);
        }
        conf.setReleased(true);
        conf.setModified(dbAuditLog.getCreated());
        dbLayer.getSession().update(conf);
        JocAuditLog.storeAuditLogDetail(new AuditLogDetail(conf.getPath(), conf.getType()), dbLayer.getSession(), dbAuditLog);
    }

//    private static void updateReleasedObject(DBItemInventoryConfiguration conf, InventoryDBLayer dbLayer, DBItemJocAuditLog dbAuditLog)
//            throws SOSHibernateException, JsonParseException, JsonMappingException, IOException {
//        conf.setAuditLogId(dbAuditLog.getId());
//        updateReleasedObject(conf, dbLayer, dbAuditLog);
//    }
    
    private static DBItemInventoryReleasedConfiguration setReleaseItem(Long releaseId, DBItemInventoryConfiguration conf, Date now)
            throws JsonParseException, JsonMappingException, IOException {
        DBItemInventoryReleasedConfiguration release = new DBItemInventoryReleasedConfiguration();
        release.setId(releaseId);
        release.setAuditLogId(conf.getAuditLogId());
        release.setCid(conf.getId());
        IReleaseObject r = (IReleaseObject) Globals.objectMapper.readValue(conf.getContent(), JocInventory.CLASS_MAPPING.get(conf.getTypeAsEnum()));
        r.setPath(conf.getPath());
        release.setContent(Globals.objectMapper.writeValueAsString(r));
        release.setCreated(now);
        release.setFolder(conf.getFolder());
        release.setModified(now);
        release.setName(conf.getName());
        release.setPath(conf.getPath());
        release.setTitle(conf.getTitle());
        release.setType(conf.getType());
        return release;
    }

    private static void deleteReleasedFolder(DBItemInventoryConfiguration conf, InventoryDBLayer dbLayer, DBItemJocAuditLog dbAuditLog,
            boolean withDeletionOfEmptyFolders) throws SOSHibernateException {
        List<DBItemInventoryConfiguration> folderContent = dbLayer.getFolderContent(conf.getPath(), true, JocInventory.getReleasableTypes());

        if (folderContent != null && !folderContent.isEmpty()) {
            dbLayer.deleteReleasedItemsByConfigurationIds(folderContent.stream().map(DBItemInventoryConfiguration::getId).collect(Collectors
                    .toSet()));
            for (DBItemInventoryConfiguration item : folderContent) {
                JocAuditLog.storeAuditLogDetail(new AuditLogDetail(item.getPath(), item.getType()), dbLayer.getSession(), dbAuditLog);
                // delete releasable objects in INV_CONFIGURATION
                JocInventory.deleteInventoryConfigurationAndPutToTrash(item, dbLayer);
            }
        }
        if (withDeletionOfEmptyFolders) {
            JocInventory.deleteEmptyFolders(dbLayer, conf);
        }

    }

    private static void deleteReleasedObject(DBItemInventoryConfiguration conf, DBItemJocAuditLog dbAuditLog, InventoryDBLayer dbLayer)
            throws SOSHibernateException {
        conf.setAuditLogId(dbAuditLog.getId());
        dbLayer.deleteReleasedItemsByConfigurationIds(Arrays.asList(conf.getId()));
        JocInventory.deleteInventoryConfigurationAndPutToTrash(conf, dbLayer);
        JocAuditLog.storeAuditLogDetail(new AuditLogDetail(conf.getPath(), conf.getType()), dbLayer.getSession(), dbAuditLog);
    }
}
