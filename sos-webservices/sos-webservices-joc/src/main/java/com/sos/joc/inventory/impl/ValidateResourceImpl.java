package com.sos.joc.inventory.impl;

import java.net.URI;
import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IValidateResource;
import com.sos.joc.model.inventory.Validate;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class ValidateResourceImpl extends JOCResourceImpl implements IValidateResource {

    @Override
    public JOCDefaultResponse validate(final String accessToken, String objectType, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getInventory().getConfigurations().isEdit());
            if (response != null) {
                return response;
            }
            checkRequiredParameter("objectType", objectType);
            Validate entity = new Validate();
            try {
                ConfigurationType type = ConfigurationType.fromValue(objectType.toUpperCase());
                if (ConfigurationType.FOLDER.equals(type)) {
                    throw new JobSchedulerInvalidResponseDataException("Unsupprted objectType:" + objectType);
                }
                entity = validate(type, inBytes);
            } catch (Exception e) {
                throw new JobSchedulerInvalidResponseDataException("Unsupprted objectType:" + objectType);
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

    private static Validate validate(ConfigurationType objectType, byte[] inBytes) {
        Validate v = new Validate();
        try {
            JsonValidator.validate(inBytes, URI.create(JocInventory.SCHEMA_LOCATION.get(objectType)));
            v.setValid(true);
        } catch (Throwable e) {
            v.setValid(false);
            v.setInvalidMsg(e.getMessage());
        }
        return v;
    }

}
