package com.sos.joc.inventory.changes.common;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
import com.sos.joc.model.inventory.changes.RemoveFromChangeRequest;
import com.sos.joc.model.inventory.changes.common.ChangeItem;

public abstract class ARemoveFromChange extends JOCResourceImpl {

    public JOCDefaultResponse removeFromChange(RemoveFromChangeRequest request, String apiCall) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(apiCall);
            
            remove(request, session);
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    public void remove(RemoveFromChangeRequest filter, SOSHibernateSession session) throws SOSHibernateException {
        DBLayerChanges dbLayer = new DBLayerChanges(session);
        DBItemInventoryChange change = dbLayer.getChange(filter.getChange().getName());
        if(change != null && filter.getRemove() != null) {
            List<DBItemInventoryChangesMapping> mappingToRemove = filter.getRemove().stream()
                    .map(mapping -> {
                        try {
                            return dbLayer.getMapping(change.getId(), mapping);
                        } catch (SOSHibernateException e) {
                            throw new JocSosHibernateException(e);
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toList());
            mappingToRemove.forEach(mapping -> {
                try {
                    session.delete(mapping);
                } catch (SOSHibernateException e) {
                    throw new JocSosHibernateException(e);
                }
            });
        }
    }

}
