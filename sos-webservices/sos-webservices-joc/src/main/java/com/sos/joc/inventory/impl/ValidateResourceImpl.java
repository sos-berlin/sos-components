package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IValidateResource;
import com.sos.joc.model.inventory.Validate;
import com.sos.joc.model.inventory.common.ConfigurationType;

import jakarta.ws.rs.Path;

@Path(JocInventory.APPLICATION_PATH)
public class ValidateResourceImpl extends JOCResourceImpl implements IValidateResource {

    // private static final Logger LOGGER = LoggerFactory.getLogger(ValidateResourceImpl.class);

    @Override
    public JOCDefaultResponse validate(final String accessToken, String objectType, byte[] inBytes) {
        try {
            String apiCall = String.format("./%s/%s/validate", JocInventory.APPLICATION_PATH, objectType);
            inBytes = initLogging(apiCall, inBytes, accessToken);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
            if (response != null) {
                return response;
            }
            checkRequiredParameter("objectType", objectType);
            Validate entity = new Validate();
            try {
                if (objectType.toUpperCase().equals("CALENDAR")) {
                    objectType = ConfigurationType.WORKINGDAYSCALENDAR.value(); 
                }
                ConfigurationType type = ConfigurationType.fromValue(objectType.toUpperCase());
                if (ConfigurationType.FOLDER.equals(type)) {
                    throw new JocBadRequestException("Unsupported objectType:" + objectType);
                }
                entity = getValidate(type, inBytes);
            } catch (IllegalArgumentException e) {
                throw new JocBadRequestException("Unsupported objectType:" + objectType);
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
    
    private static Validate getValidate(ConfigurationType objectType, byte[] inBytes) {
        Validate v = new Validate();
        try {
            Validator.validate(objectType, inBytes);
            v.setValid(true);
        } catch (Throwable e) {
            v.setValid(false);
            v.setInvalidMsg(e.getMessage() != null ? e.getMessage() : e.toString());
        }
        return v;
    }

}
