package com.sos.joc.inventory.changes.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.changes.common.ARemoveFromChange;
import com.sos.joc.inventory.changes.resource.IRemoveFromChange;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.changes.RemoveFromChangeRequest;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("inventory/change")
public class RemoveFromChangeImpl extends ARemoveFromChange implements IRemoveFromChange {

    private static final String API_CALL = "./inventory/change/remove";
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveFromChangeImpl.class);
    
    @Override
    public JOCDefaultResponse postRemoveFromChange(String xAccessToken, byte[] filter) {
        SOSHibernateSession session = null;
        try {
            filter = initLogging(API_CALL, filter, xAccessToken, CategoryType.INVENTORY);
            JsonValidator.validate(filter, RemoveFromChangeRequest.class);
            RemoveFromChangeRequest removeFilter = Globals.objectMapper.readValue(filter, RemoveFromChangeRequest.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(xAccessToken).map(p -> p.getInventory().getManage()));
            if (response == null) {
                response = removeFromChange(removeFilter, API_CALL);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

}
