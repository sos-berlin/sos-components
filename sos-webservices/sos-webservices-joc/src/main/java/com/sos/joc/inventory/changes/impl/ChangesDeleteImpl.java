package com.sos.joc.inventory.changes.impl;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.changes.common.ADeleteChange;
import com.sos.joc.inventory.changes.resource.IChangesDelete;
import com.sos.joc.model.inventory.changes.DeleteChangesRequest;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("inventory/changes")
public class ChangesDeleteImpl extends ADeleteChange implements IChangesDelete {

    private static final String API_CALL = "./inventory/changes/delete";
    
    @Override
    public JOCDefaultResponse postChangesDelete(String xAccessToken, byte[] filter) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, filter, xAccessToken);
            JsonValidator.validate(filter, DeleteChangesRequest.class);
            DeleteChangesRequest deletefilter = Globals.objectMapper.readValue(filter, DeleteChangesRequest.class);
            
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(xAccessToken).getInventory().getManage());
            if (response == null) {
                response = deleteChange(deletefilter, API_CALL);
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