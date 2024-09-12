package com.sos.joc.inventory.changes.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.changes.resource.IChangesDelete;
import com.sos.joc.model.inventory.changes.DeleteChangesRequest;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("inventory/changes")
public class ChangesDeleteImpl extends JOCResourceImpl implements IChangesDelete {

    private static final String API_CALL = "./inventory/changes/delete";
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangesDeleteImpl.class);
    
    @Override
    public JOCDefaultResponse postChangesDelete(String xAccessToken, byte[] filter) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, filter, xAccessToken);
            JsonValidator.validate(filter, DeleteChangesRequest.class);
            DeleteChangesRequest deletefilter = Globals.objectMapper.readValue(filter, DeleteChangesRequest.class);
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsString(Object.class));
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
