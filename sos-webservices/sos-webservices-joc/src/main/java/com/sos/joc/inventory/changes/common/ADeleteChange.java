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
import com.sos.joc.db.inventory.changes.DBLayerChanges;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.model.inventory.changes.DeleteChangesRequest;
import com.sos.joc.model.inventory.changes.common.Change;
import com.sos.joc.model.inventory.changes.common.ChangeItem;

public abstract class ADeleteChange extends JOCResourceImpl {

    public JOCDefaultResponse deleteChange(DeleteChangesRequest request, String apiCall) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(apiCall);
            
            delete(request, session);
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    public void delete(DeleteChangesRequest request, SOSHibernateSession session) throws SOSHibernateException {
        DBLayerChanges dbLayer = new DBLayerChanges(session);
        for(Change change : request.getChanges()) {
            DBItemInventoryChange changeDbItem = dbLayer.getChange(change.getName());
            if(changeDbItem != null) {
                // delete cascading - delete mappings first, then delete change itself
                List<DBItemInventoryChangesMapping> mappings = dbLayer.getMappings(changeDbItem.getId());
                for(DBItemInventoryChangesMapping mapping : mappings) {
                    session.delete(mapping);
                }
                session.delete(changeDbItem);
            }
        }
    }
}
