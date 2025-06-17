package com.sos.joc.inventory.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.inventory.impl.common.AReadConfiguration;
import com.sos.joc.inventory.resource.IReadConfigurationResource;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.read.RequestFilter;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocInventory.APPLICATION_PATH)
public class ReadConfigurationResourceImpl extends AReadConfiguration implements IReadConfigurationResource {

    @Override
    public JOCDefaultResponse read(final String accessToken, byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            inBytes = initLogging(IMPL_PATH, inBytes, accessToken, CategoryType.INVENTORY);
            JsonValidator.validate(inBytes, RequestFilter.class, true);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);

            JOCDefaultResponse response = initPermissions(null, getBasicJocPermissions(accessToken).getInventory().getView());
            if (response == null) {
                response = read(in, IMPL_PATH);
            }
            return response;

        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    @Override
    public JOCDefaultResponse readTrash(final String accessToken, byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            inBytes = initLogging(TRASH_IMPL_PATH, inBytes, accessToken, CategoryType.INVENTORY);
            JsonValidator.validate(inBytes, RequestFilter.class, true);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);

            JOCDefaultResponse response = initPermissions(null, getBasicJocPermissions(accessToken).getInventory().getView());
            if (response == null) {
                response = readTrash(in, TRASH_IMPL_PATH);
            }
            return response;

        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}
