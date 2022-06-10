package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.exceptions.ControllerInvalidResponseDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IValidateResource;
import com.sos.joc.model.inventory.Validate;
import com.sos.joc.model.inventory.common.ConfigurationType;

@Path(JocInventory.APPLICATION_PATH)
public class ValidateResourceImpl extends JOCResourceImpl implements IValidateResource {

    // private static final Logger LOGGER = LoggerFactory.getLogger(ValidateResourceImpl.class);

    @Override
    public JOCDefaultResponse validate(final String accessToken, String objectType, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
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
                    throw new ControllerInvalidResponseDataException("Unsupported objectType:" + objectType);
                }
                entity = getValidate(type, inBytes);
            } catch (IllegalArgumentException e) {
                throw new ControllerInvalidResponseDataException("Unsupported objectType:" + objectType);
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
            v.setInvalidMsg(e.getMessage());
        }
        return v;
    }

}
