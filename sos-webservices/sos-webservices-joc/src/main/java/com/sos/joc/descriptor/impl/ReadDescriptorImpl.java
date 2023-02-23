package com.sos.joc.descriptor.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.descriptor.resource.IReadDescriptor;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.impl.common.AReadConfiguration;
import com.sos.joc.model.descriptor.common.RequestFilter;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("descriptor")
public class ReadDescriptorImpl extends AReadConfiguration implements IReadDescriptor {

    @Override
    public JOCDefaultResponse read(String accessToken, byte[] body) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH_READ, body, accessToken);
            JsonValidator.validate(body, RequestFilter.class, true);
            com.sos.joc.model.inventory.read.RequestFilter filter = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.read.RequestFilter.class);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());
            if (response == null) {
                filter.setObjectType(ConfigurationType.DEPLOYMENTDESCRIPTOR);
                response = read(filter, IMPL_PATH_READ);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse readTrash(String accessToken, byte[] body) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH_TRASH_READ, body, accessToken);
            JsonValidator.validate(body, RequestFilter.class, true);
            com.sos.joc.model.inventory.read.RequestFilter filter = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.read.RequestFilter.class);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());
            if (response == null) {
                filter.setObjectType(ConfigurationType.DEPLOYMENTDESCRIPTOR);
                response = readTrash(filter, PATH_TRASH_READ);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
