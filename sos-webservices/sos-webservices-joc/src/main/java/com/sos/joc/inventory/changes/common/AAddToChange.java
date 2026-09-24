package com.sos.joc.inventory.changes.common;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.records.AuthFolders;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.DBItemInventoryChange;
import com.sos.joc.db.inventory.DBItemInventoryChangesMapping;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.changes.DBLayerChanges;
import com.sos.joc.model.inventory.changes.AddToChangeRequest;
import com.sos.joc.model.inventory.changes.common.ChangeItem;

public abstract class AAddToChange extends JOCResourceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(AAddToChange.class);
    
    public JOCDefaultResponse addToChange(AddToChangeRequest request, String apiCall, AuthFolders authFolders) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(apiCall);
            add(request, session, authFolders);
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private void add(AddToChangeRequest request, SOSHibernateSession session, AuthFolders authFolders) throws SOSHibernateException {
        DBLayerChanges dbLayer = new DBLayerChanges(session); 
        DBItemInventoryChange change = dbLayer.getChange(request.getChange().getName());
        List<ChangeItem> itemsToAdd = request.getAdd();
        List<DBItemInventoryChangesMapping> mappings = itemsToAdd.stream()
                .map(item -> convert(item, change.getId(), session, authFolders)).filter(Objects::nonNull).collect(Collectors.toList());
        for(DBItemInventoryChangesMapping mapping : mappings) {
            DBItemInventoryChangesMapping exisitingMapping = dbLayer.getMapping(mapping.getChangeId(), mapping.getInvId());
            if(exisitingMapping != null) {
                session.update(exisitingMapping);
            } else {
                session.save(mapping);
            }
        }
    }
    
    private DBItemInventoryChangesMapping convert(ChangeItem changeItem, Long changeId, SOSHibernateSession session, AuthFolders authFolders) {
        DBItemInventoryChangesMapping mapping = new DBItemInventoryChangesMapping();
        mapping.setChangeId(changeId);
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        List<DBItemInventoryConfiguration> inventoryItems = dbLayer.getConfigurationByName(changeItem.getName(), changeItem.getObjectType().intValue());
        if(!inventoryItems.isEmpty()) {
            DBItemInventoryConfiguration invItem = inventoryItems.get(0);
            if(!folderIsPermitted(invItem.getPath(), authFolders)) {
                LOGGER.warn("missing folder permissions for item: " + invItem.getPath());
            } else {
                mapping.setInvId(invItem.getId());
                mapping.setType(changeItem.getObjectType());
            }
        }
        if(mapping.getInvId() == null || mapping.getType() == null) {
            return null;
        } else {
            return mapping;
        }
    }
}
