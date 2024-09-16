package com.sos.joc.inventory.changes.util;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.inventory.DBItemInventoryChange;
import com.sos.joc.db.inventory.changes.DBLayerChanges;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.model.inventory.changes.common.Change;
import com.sos.joc.model.inventory.changes.common.ChangeState;

public class ChangeUtil {


    public static Change convert(DBItemInventoryChange dbItem) {
        return convert(dbItem, false, null);
    }
        
    public static Change convert(DBItemInventoryChange dbItem, Boolean withDetails, SOSHibernateSession session){
        Change change = new Change();
        change.setName(dbItem.getName());
        change.setState(ChangeState.fromValue(dbItem.getState()));
        change.setTitle(dbItem.getTitle());
        change.setOwner(dbItem.getOwner());
        change.setLastPublishedBy(dbItem.getPublishedBy());
        change.setCreated(dbItem.getCreated());
        change.setModified(dbItem.getModified());
        change.setClosed(dbItem.getClosed());
        if(withDetails) {
            // get mappings and set configurations
            if(session != null) {
                DBLayerChanges dbLayer = new DBLayerChanges(session);
                try {
                    change.setConfigurations(dbLayer.getChangeItems(dbItem.getId()));
                } catch (SOSHibernateException e) {
                    throw new JocSosHibernateException(e);
                }
            }
        }
        return change;
    }
    
}