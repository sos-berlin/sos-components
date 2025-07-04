package com.sos.joc.inventory.changes.impl;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.inventory.changes.common.AAddToChange;
import com.sos.joc.inventory.changes.resource.IAddToChange;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.changes.AddToChangeRequest;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("inventory/change")
public class AddToChangeImpl extends AAddToChange implements IAddToChange {

    private static final String API_CALL = "./inventory/change/add";
    
    @Override
    public JOCDefaultResponse postAddToChange(String xAccessToken, byte[] filter) {
        SOSHibernateSession hibernateSession = null;
        try {
            filter = initLogging(API_CALL, filter, xAccessToken, CategoryType.INVENTORY);
            JsonValidator.validate(filter, AddToChangeRequest.class);
            AddToChangeRequest addFilter = Globals.objectMapper.readValue(filter, AddToChangeRequest.class);
            
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(xAccessToken).map(p -> p.getInventory().getManage()));
            if (response == null) {
                response = addToChange(addFilter, API_CALL);
            }
            return response;
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}
