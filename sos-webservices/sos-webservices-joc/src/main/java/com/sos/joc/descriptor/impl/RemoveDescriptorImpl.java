package com.sos.joc.descriptor.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.descriptor.resource.IRemoveDescriptor;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.impl.common.ADeleteConfiguration;
import com.sos.joc.model.descriptor.common.RequestFolder;
import com.sos.joc.model.descriptor.remove.RequestFilters;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("descriptor")
public class RemoveDescriptorImpl extends ADeleteConfiguration implements IRemoveDescriptor {

    @Override
    public JOCDefaultResponse remove(String accessToken, byte[] body) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH_REMOVE, body, accessToken);
            JsonValidator.validate(body, RequestFilters.class, true);
            com.sos.joc.model.inventory.delete.RequestFilters in = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.delete.RequestFilters.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                response = remove(accessToken, in, IMPL_PATH_REMOVE);
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
    public JOCDefaultResponse removeFolder(String accessToken, byte[] body) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH_REMOVE_FOLDER, body, accessToken);
            JsonValidator.validate(body, RequestFolder.class, true);
            com.sos.joc.model.inventory.delete.RequestFolder in = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.delete.RequestFolder.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                response = removeFolder(accessToken, in, true, IMPL_PATH_REMOVE_FOLDER);
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
    public JOCDefaultResponse deleteFromTrash(String accessToken, byte[] body) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IRemoveDescriptor.IMPL_PATH_TRASH_DELETE, body, accessToken);
            JsonValidator.validate(body, RequestFilters.class, true);
            com.sos.joc.model.inventory.delete.RequestFilters in = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.delete.RequestFilters.class);

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
    public JOCDefaultResponse deleteFolderFromTrash(String accessToken, byte[] body) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IRemoveDescriptor.IMPL_PATH_TRASH_DELETE, body, accessToken);
            JsonValidator.validate(body, RequestFolder.class, true);
            com.sos.joc.model.inventory.delete.RequestFolder in = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.delete.RequestFolder.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                response = deleteFolder(accessToken, in, true, PATH_TRASH_DELETE_FOLDER);
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
