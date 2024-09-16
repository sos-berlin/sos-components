package com.sos.joc.inventory.changes.common;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.DBItemInventoryChange;
import com.sos.joc.db.inventory.changes.DBLayerChanges;
import com.sos.joc.model.inventory.changes.StoreChangeRequest;
import com.sos.joc.model.inventory.changes.common.ChangeState;

public abstract class AStoreChange extends JOCResourceImpl {

    public JOCDefaultResponse storeChange(StoreChangeRequest request, String apiCall) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(apiCall);
            
            store(request, session);
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    public void store(StoreChangeRequest request, SOSHibernateSession session) throws SOSHibernateException {
        DBLayerChanges dbLayer = new DBLayerChanges(session);
        DBItemInventoryChange existingChange = dbLayer.getChange(request.getStore().getName());
        if(existingChange != null) {
            existingChange.setState(request.getStore().getState().value());
            if(ChangeState.PUBLISHED.equals(request.getStore().getState())) {
                existingChange.setPublishedBy(jobschedulerUser.getSOSAuthCurrentAccount().getAccountname());
            }
            existingChange.setTitle(request.getStore().getTitle());
            Date now = Date.from(Instant.now());
            existingChange.setModified(now);
            if(ChangeState.CLOSED.equals(request.getStore().getState())) {
                existingChange.setClosed(now);
            }
            session.update(existingChange);
        } else {
            DBItemInventoryChange newChange = new DBItemInventoryChange();
            newChange.setName(request.getStore().getName());
            newChange.setState(ChangeState.OPEN.value());
            newChange.setOwner(jobschedulerUser.getSOSAuthCurrentAccount().getAccountname());
            newChange.setTitle(request.getStore().getTitle());
            Date now = Date.from(Instant.now());
            newChange.setCreated(now);
            newChange.setModified(now);
            session.save(newChange);
        }
    }
}
