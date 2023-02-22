package com.sos.joc.descriptor.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.descriptor.resource.IRemoveDescriptor;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.impl.DeleteConfigurationResourceImpl;
import com.sos.joc.model.descriptor.common.RequestFolder;
import com.sos.joc.model.descriptor.remove.RequestFilters;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path(JocInventory.APPLICATION_PATH)
public class RemoveDescriptorImpl extends JOCResourceImpl implements IRemoveDescriptor {

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
                DeleteConfigurationResourceImpl deleteConfiguration = new DeleteConfigurationResourceImpl();
                response = deleteConfiguration.remove(accessToken, in);
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
                DeleteConfigurationResourceImpl deleteConfiguration = new DeleteConfigurationResourceImpl();
                response = deleteConfiguration.removeFolder(accessToken, in, true);
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
            initLogging(IMPL_PATH_TRASH_DELETE, body, accessToken);
            JsonValidator.validate(body, RequestFilters.class, true);
            com.sos.joc.model.inventory.delete.RequestFilters in = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.delete.RequestFilters.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                DeleteConfigurationResourceImpl deleteConfiguration = new DeleteConfigurationResourceImpl();
                response = deleteConfiguration.delete(accessToken, in);
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
            initLogging(IMPL_PATH_TRASH_DELETE, body, accessToken);
            JsonValidator.validate(body, RequestFolder.class, true);
            com.sos.joc.model.inventory.delete.RequestFolder in = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.delete.RequestFolder.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                DeleteConfigurationResourceImpl deleteConfiguration = new DeleteConfigurationResourceImpl();
                response = deleteConfiguration.deleteFolder(accessToken, in, true);
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
