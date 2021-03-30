package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.PredicateParser;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IValidatePredicateResource;
import com.sos.joc.model.inventory.Validate;

@Path(JocInventory.APPLICATION_PATH)
public class ValidatePredicateResourceImpl extends JOCResourceImpl implements IValidatePredicateResource {

    @Override
    public JOCDefaultResponse parse(final String accessToken, byte[] body) {
        try {
            initLogging(IMPL_PATH, body, accessToken);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response != null) {
                return response;
            }
            Validate entity = new Validate();
            try {
                PredicateParser.parse(body);
                entity.setValid(true);
            } catch (Throwable e) {
                entity.setValid(false);
                entity.setInvalidMsg(e.getMessage());
            }
            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
