package com.sos.joc.descriptor.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.descriptor.resource.IRestoreDescriptor;
import com.sos.joc.inventory.impl.common.ARestoreConfiguration;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.descriptor.restore.RequestFilter;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("descriptor")
public class RestoreDescriptorImpl extends ARestoreConfiguration implements IRestoreDescriptor {

    @Override
    public JOCDefaultResponse postRestoreFromTrash(String accessToken, byte[] body) {
        try {
            body = initLogging(IMPL_PATH_RESTORE, body, accessToken, CategoryType.INVENTORY);
            JsonValidator.validate(body, RequestFilter.class, true);
            com.sos.joc.model.inventory.restore.RequestFilter filter = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.restore.RequestFilter.class);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
            if (response == null) {
                response = restore(filter, IMPL_PATH_RESTORE, true);
            }
            return response;
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}
