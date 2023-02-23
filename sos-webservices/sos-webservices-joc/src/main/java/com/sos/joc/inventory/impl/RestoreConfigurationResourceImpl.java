package com.sos.joc.inventory.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.impl.common.ARestoreConfiguration;
import com.sos.joc.inventory.resource.IRestoreConfigurationResource;
import com.sos.joc.model.inventory.restore.RequestFilter;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocInventory.APPLICATION_PATH)
public class RestoreConfigurationResourceImpl extends ARestoreConfiguration implements IRestoreConfigurationResource {

    @Override
    public JOCDefaultResponse restore(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(TRASH_IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilter.class, true);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                response = restore(in, TRASH_IMPL_PATH);
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
