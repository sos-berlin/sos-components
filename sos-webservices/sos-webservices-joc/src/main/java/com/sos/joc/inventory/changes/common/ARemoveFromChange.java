package com.sos.joc.inventory.changes.common;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.model.inventory.changes.RemoveFromChangeRequest;

public abstract class ARemoveFromChange extends JOCResourceImpl {

    public JOCDefaultResponse removeFromChange(RemoveFromChangeRequest request, String apiCall) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(apiCall);
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(apiCall));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
}
