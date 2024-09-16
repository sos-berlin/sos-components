package com.sos.joc.inventory.changes.common;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
import com.sos.joc.model.inventory.common.ConfigurationType;

public abstract class AAddToChange extends JOCResourceImpl {

    public JOCDefaultResponse addToChange(AddToChangeRequest request, String apiCall) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(apiCall);
            
            add(request, session);
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private void add(AddToChangeRequest request, SOSHibernateSession session) throws SOSHibernateException {
        DBLayerChanges dbLayer = new DBLayerChanges(session); 
        DBItemInventoryChange change = dbLayer.getChange(request.getChange().getName());
        List<ChangeItem> itemsToAdd = request.getAdd();
        List<DBItemInventoryChangesMapping> mappings = itemsToAdd.stream()
                .map(item -> convert(item, change.getId(), session)).collect(Collectors.toList());
        for(DBItemInventoryChangesMapping mapping : mappings) {
            session.save(mapping);
        }
    }
    
    private DBItemInventoryChangesMapping convert(ChangeItem changeItem, Long changeId, SOSHibernateSession session) {
        DBItemInventoryChangesMapping mapping = new DBItemInventoryChangesMapping();
        mapping.setChangeId(changeId);
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        List<DBItemInventoryConfiguration> inventoryItems = dbLayer.getConfigurationByName(changeItem.getName(), changeItem.getObjectType().intValue());
        if(!inventoryItems.isEmpty()) {
            mapping.setInvId(inventoryItems.get(0).getId());
            mapping.setType(changeItem.getObjectType());
        }
        return mapping;
    }
}
