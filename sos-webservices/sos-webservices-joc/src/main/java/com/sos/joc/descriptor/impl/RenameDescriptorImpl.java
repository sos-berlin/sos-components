package com.sos.joc.descriptor.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.descriptor.resource.IRenameDescriptor;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.impl.RenameConfigurationResourceImpl;
import com.sos.joc.model.descriptor.rename.RequestFilter;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path(JocInventory.APPLICATION_PATH)
public class RenameDescriptorImpl extends JOCResourceImpl implements IRenameDescriptor {

    @Override
    public JOCDefaultResponse rename(String accessToken, byte[] body) {
        try {
            initLogging(IMPL_PATH_RENAME, body, accessToken);
            JsonValidator.validate(body, RequestFilter.class, true);
            com.sos.joc.model.inventory.rename.RequestFilter filter = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.rename.RequestFilter.class);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                RenameConfigurationResourceImpl renameConfiguration = new RenameConfigurationResourceImpl();
                response = renameConfiguration.rename(filter);
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
