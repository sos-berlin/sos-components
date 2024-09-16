package com.sos.joc.inventory.changes.impl;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.changes.common.AStoreChange;
import com.sos.joc.inventory.changes.resource.IChangesStore;
import com.sos.joc.model.inventory.changes.StoreChangeRequest;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("inventory/changes")
public class ChangesStoreImpl extends AStoreChange implements IChangesStore {

    private static final String API_CALL = "./inventory/changes/store";
    
    @Override
    public JOCDefaultResponse postChangesStore(String xAccessToken, byte[] filter) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, filter, xAccessToken);
            JsonValidator.validate(filter, StoreChangeRequest.class);
            StoreChangeRequest storeFilter = Globals.objectMapper.readValue(filter, StoreChangeRequest.class);
            
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(xAccessToken).getInventory().getManage());
            if (response == null) {
                response = storeChange(storeFilter, API_CALL);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }
    
}
