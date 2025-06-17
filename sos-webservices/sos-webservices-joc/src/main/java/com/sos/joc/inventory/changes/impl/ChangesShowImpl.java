package com.sos.joc.inventory.changes.impl;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.inventory.changes.common.AShowChange;
import com.sos.joc.inventory.changes.resource.IChangesShow;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.changes.ShowChangesFilter;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("inventory/changes")
public class ChangesShowImpl extends AShowChange implements IChangesShow {

    private static final String API_CALL = "./inventory/changes";
    
    @Override
    public JOCDefaultResponse postShowChanges(String xAccessToken, byte[] filter) {
        SOSHibernateSession hibernateSession = null;
        try {
            filter = initLogging(API_CALL, filter, xAccessToken, CategoryType.INVENTORY);
            JsonValidator.validate(filter, ShowChangesFilter.class);
            ShowChangesFilter showFilter = Globals.objectMapper.readValue(filter, ShowChangesFilter.class);
            
            JOCDefaultResponse response = initPermissions(null, getBasicJocPermissions(xAccessToken).getInventory().getView());
            if (response == null) {
                response = showChange(showFilter, API_CALL);
            }
            return response;
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}
