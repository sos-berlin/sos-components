package com.sos.joc.publish.repository.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.repository.UpdateFromFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.repository.resource.IRepositoryUpdateFrom;
import com.sos.joc.publish.repository.util.RepositoryUtil;
import com.sos.schema.JsonValidator;

@javax.ws.rs.Path("inventory/repository")
public class RepositoryUpdateFromImpl extends JOCResourceImpl implements IRepositoryUpdateFrom{

    private static final String API_CALL = "./inventory/repository/update";
    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryUpdateFromImpl.class);

    @Override
    public JOCDefaultResponse postUpdate(String xAccessToken, byte[] updateFromFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            Date started = Date.from(Instant.now());
            LOGGER.trace("*** update from repository started ***" + started);
            initLogging(API_CALL, updateFromFilter, xAccessToken);
            JsonValidator.validate(updateFromFilter, UpdateFromFilter.class);
            UpdateFromFilter filter = Globals.objectMapper.readValue(updateFromFilter, UpdateFromFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getInventory().getDeploy());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            DBItemJocAuditLog dbAuditlog = storeAuditLog(filter.getAuditLog(), CategoryType.INVENTORY);

            Path repositoriesBase = Globals.sosCockpitProperties.resolvePath("repositories");
            
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            
            Set<DBItemInventoryConfiguration> dbItemsToUpdate = RepositoryUtil.getUpdatedDbItems(filter, repositoriesBase, dbLayer);
            Set<DBItemInventoryConfiguration> newDbItems = RepositoryUtil.getNewItemsToUpdate(filter, repositoriesBase, dbLayer);
            
            dbItemsToUpdate.stream().forEach(item -> {
                try {
                    item.setRepoControlled(true);
                    dbLayer.getSession().update(item);
                } catch (SOSHibernateException e) {
                    throw new JocSosHibernateException(e);
                }
            });
            updateTopLevelFolder(dbItemsToUpdate, dbLayer);
            newDbItems.stream().forEach(item -> {
                item.setRepoControlled(true);
                try {
                    if (item.getId() == null || item.getId() == 0L) {
                        dbLayer.getSession().save(item);
                    } else {
                        dbLayer.getSession().update(item);
                    }
                    if(item.getFolder() != null && !item.getFolder().isEmpty() && !"/".equals(item.getFolder())) {
                        JocInventory.makeParentDirs(new InventoryDBLayer(dbLayer.getSession()), Paths.get(item.getFolder()));
                    }
                } catch (SOSHibernateException e) {
                    throw new JocSosHibernateException(e);
                }
            });
            updateTopLevelFolder(newDbItems, dbLayer);
            CompletableFuture.runAsync(() -> JocAuditLog.storeAuditLogDetails(dbItemsToUpdate.stream().map(item -> new AuditLogDetail(item.getPath(), 
                    item.getType())), dbAuditlog.getId(), dbAuditlog.getCreated()));
            CompletableFuture.runAsync(() -> JocAuditLog.storeAuditLogDetails(newDbItems.stream().map(item -> new AuditLogDetail(item.getPath(), 
                    item.getType())), dbAuditlog.getId(), dbAuditlog.getCreated()));
            Date apiCallFinished = Date.from(Instant.now());
            LOGGER.trace("*** read from repository finished ***" + apiCallFinished);
            LOGGER.trace("complete WS time : " + (apiCallFinished.getTime() - started.getTime()) + " ms");
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

    private void updateTopLevelFolder(Set<DBItemInventoryConfiguration> items, DBLayerDeploy dbLayer) throws SOSHibernateException {
        String topLevelFolder = "";
        for(DBItemInventoryConfiguration cfg : items) {
            topLevelFolder = Paths.get(cfg.getPath()).subpath(0, 1).toString();
            DBItemInventoryConfiguration topLevelFolderDBitem = dbLayer.getConfigurationByPath("/" + topLevelFolder, ConfigurationType.FOLDER);
            if(topLevelFolderDBitem != null) {
                topLevelFolderDBitem.setRepoControlled(true);
                dbLayer.getSession().update(topLevelFolderDBitem);
                break;
            }
        }
    }
}
