package com.sos.joc.descriptor.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.descriptor.resource.ICopyDescriptor;
import com.sos.joc.inventory.impl.common.ACopyConfiguration;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.descriptor.copy.RequestFilter;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("descriptor")
public class CopyDescriptorImpl extends ACopyConfiguration implements ICopyDescriptor {

    @Override
    public JOCDefaultResponse copy(String accessToken, byte[] body) {
        try {
            body = initLogging(IMPL_PATH_COPY, body, accessToken, CategoryType.INVENTORY);
            JsonValidator.validate(body, RequestFilter.class, true);
            com.sos.joc.model.inventory.copy.RequestFilter in = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.copy.RequestFilter.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
            if (response == null) {
                response = copy(in, true, IMPL_PATH_COPY);
            }
            return response;
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}
