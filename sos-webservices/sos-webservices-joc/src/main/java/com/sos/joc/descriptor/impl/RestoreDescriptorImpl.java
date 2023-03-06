package com.sos.joc.descriptor.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.descriptor.resource.IRestoreDescriptor;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.impl.common.ARestoreConfiguration;
import com.sos.joc.model.descriptor.restore.RequestFilter;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("descriptor")
public class RestoreDescriptorImpl extends ARestoreConfiguration implements IRestoreDescriptor {

    @Override
    public JOCDefaultResponse postRestoreFromTrash(String accessToken, byte[] body) {
        try {
            initLogging(IMPL_PATH_RESTORE, body, accessToken);
            JsonValidator.validate(body, RequestFilter.class, true);
            com.sos.joc.model.inventory.restore.RequestFilter filter = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.restore.RequestFilter.class);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                response = restore(filter, IMPL_PATH_RESTORE, true);
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
