package com.sos.joc.inventory.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.impl.common.ADeleteConfiguration;
import com.sos.joc.inventory.resource.IDeleteConfigurationResource;
import com.sos.joc.model.inventory.delete.RequestFilters;
import com.sos.joc.model.inventory.delete.RequestFolder;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocInventory.APPLICATION_PATH)
public class DeleteConfigurationResourceImpl extends ADeleteConfiguration implements IDeleteConfigurationResource {

    @Override
    public JOCDefaultResponse remove(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH_DELETE, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilters.class, true);
            RequestFilters in = Globals.objectMapper.readValue(inBytes, RequestFilters.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                response = remove(accessToken, in, IMPL_PATH_DELETE);
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
    public JOCDefaultResponse removeFolder(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH_FOLDER_DELETE, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFolder.class, true);
            RequestFolder in = Globals.objectMapper.readValue(inBytes, RequestFolder.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                response = removeFolder(accessToken, in, IMPL_PATH_FOLDER_DELETE);
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
    public JOCDefaultResponse delete(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH_TRASH_DELETE, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilters.class, true);
            RequestFilters in = Globals.objectMapper.readValue(inBytes, RequestFilters.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                response = delete(accessToken, in, IMPL_PATH_TRASH_DELETE);
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
    public JOCDefaultResponse deleteFolder(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH_TRASH_DELETE, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFolder.class, true);
            RequestFolder in = Globals.objectMapper.readValue(inBytes, RequestFolder.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                response = deleteFolder(accessToken, in, IMPL_PATH_TRASH_DELETE);
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
