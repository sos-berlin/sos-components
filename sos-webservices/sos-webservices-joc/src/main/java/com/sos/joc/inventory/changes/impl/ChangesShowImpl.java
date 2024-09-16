package com.sos.joc.inventory.changes.impl;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.changes.common.AShowChange;
import com.sos.joc.inventory.changes.resource.IChangesShow;
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
            initLogging(API_CALL, filter, xAccessToken);
            JsonValidator.validate(filter, ShowChangesFilter.class);
            ShowChangesFilter showFilter = Globals.objectMapper.readValue(filter, ShowChangesFilter.class);
            
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(xAccessToken).getInventory().getManage());
            if (response == null) {
                response = showChange(showFilter, API_CALL);
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