package com.sos.joc.inventory.changes.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.changes.resource.IAddToChange;
import com.sos.joc.model.inventory.changes.AddToChangeRequest;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("inventory/change")
public class AddToChangeImpl extends JOCResourceImpl implements IAddToChange {

    private static final String API_CALL = "./inventory/change/add";
    private static final Logger LOGGER = LoggerFactory.getLogger(AddToChangeImpl.class);
    
    @Override
    public JOCDefaultResponse postAddToChange(String xAccessToken, byte[] filter) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, filter, xAccessToken);
            JsonValidator.validate(filter, AddToChangeRequest.class);
            AddToChangeRequest addFilter = Globals.objectMapper.readValue(filter, AddToChangeRequest.class);
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
