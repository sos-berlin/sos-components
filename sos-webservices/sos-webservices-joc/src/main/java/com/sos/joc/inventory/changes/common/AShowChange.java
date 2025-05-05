package com.sos.joc.inventory.changes.common;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.DBItemInventoryChange;
import com.sos.joc.db.inventory.changes.DBLayerChanges;
import com.sos.joc.inventory.changes.util.ChangeUtil;
import com.sos.joc.model.inventory.changes.ShowChangesFilter;
import com.sos.joc.model.inventory.changes.ShowChangesResponse;

public abstract class AShowChange extends JOCResourceImpl {

    public JOCDefaultResponse showChange(ShowChangesFilter filter, String apiCall) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(apiCall);
            DBLayerChanges dbLayer = new DBLayerChanges(session);
            
            List<DBItemInventoryChange> changes = dbLayer.getChanges(filter);
            
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(getResponse(changes, filter.getDetails(), session)));
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private ShowChangesResponse getResponse(List<DBItemInventoryChange> changes, Boolean withDetails, SOSHibernateSession session) {
        ShowChangesResponse response = new ShowChangesResponse();
        if(withDetails) {
            response.setChanges(changes.stream().map(item -> ChangeUtil.convert(item, withDetails, session)).collect(Collectors.toList()));
        } else {
            response.setChanges(changes.stream().map(item -> ChangeUtil.convert(item)).collect(Collectors.toList()));
        }
        response.setDeliveryDate(Date.from(Instant.now()));
        return response;
    }
}
