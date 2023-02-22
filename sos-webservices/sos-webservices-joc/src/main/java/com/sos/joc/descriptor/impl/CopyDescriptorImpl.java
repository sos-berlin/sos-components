package com.sos.joc.descriptor.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.descriptor.resource.ICopyDescriptor;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.impl.CopyConfigurationResourceImpl;
import com.sos.joc.model.descriptor.copy.RequestFilter;
import com.sos.schema.JsonValidator;


public class CopyDescriptorImpl extends JOCResourceImpl implements ICopyDescriptor {

    @Override
    public JOCDefaultResponse copy(String accessToken, byte[] body) {
        try {
            initLogging(IMPL_PATH_COPY, body, accessToken);
            JsonValidator.validate(body, RequestFilter.class, true);
            com.sos.joc.model.inventory.copy.RequestFilter in = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.copy.RequestFilter.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                CopyConfigurationResourceImpl copyConfiguration = new CopyConfigurationResourceImpl();
                response = copyConfiguration.copy(in, true);
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
