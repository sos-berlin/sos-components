package com.sos.joc.descriptor.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.descriptor.resource.IRenameDescriptor;
import com.sos.joc.inventory.impl.common.ARenameConfiguration;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.descriptor.rename.RequestFilter;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("descriptor")
public class RenameDescriptorImpl extends ARenameConfiguration implements IRenameDescriptor {

    @Override
    public JOCDefaultResponse postRename(String accessToken, byte[] body) {
        try {
            body = initLogging(IMPL_PATH_RENAME, body, accessToken, CategoryType.INVENTORY);
            JsonValidator.validate(body, RequestFilter.class, true);
            com.sos.joc.model.inventory.rename.RequestFilter filter = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.rename.RequestFilter.class);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
            if (response == null) {
                response = rename(filter, IMPL_PATH_RENAME);
            }
            return response;
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}
