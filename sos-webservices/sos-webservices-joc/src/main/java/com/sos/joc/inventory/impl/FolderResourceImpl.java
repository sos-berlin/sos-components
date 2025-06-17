package com.sos.joc.inventory.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.inventory.impl.common.AReadFolder;
import com.sos.joc.inventory.resource.IFolderResource;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.common.RequestFolder;
import com.sos.joc.model.inventory.common.ResponseFolder;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocInventory.APPLICATION_PATH)
public class FolderResourceImpl extends AReadFolder implements IFolderResource {

    @Override
    public JOCDefaultResponse readFolder(final String accessToken, byte[] inBytes) {
        try {
            inBytes = initLogging(IMPL_PATH, inBytes, accessToken, CategoryType.INVENTORY);
            JsonValidator.validateFailFast(inBytes, RequestFolder.class);
            RequestFolder in = Globals.objectMapper.readValue(inBytes, RequestFolder.class);

            in.setPath(normalizeFolder(in.getPath()));
            JOCDefaultResponse response = checkPermissions(accessToken, in, getBasicJocPermissions(accessToken).getInventory().getView());
            if (response == null) {
                ResponseFolder folder = readFolder(in, IMPL_PATH);
                folder.setDeploymentDescriptors(null);
                response = responseStatus200(Globals.objectMapper.writeValueAsBytes(folder));
            }
            return response;
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    @Override
    public JOCDefaultResponse readTrashFolder(final String accessToken, byte[] inBytes) {
        try {
            inBytes = initLogging(TRASH_IMPL_PATH, inBytes, accessToken, CategoryType.INVENTORY);
            JsonValidator.validateFailFast(inBytes, RequestFolder.class);
            RequestFolder in = Globals.objectMapper.readValue(inBytes, RequestFolder.class);

            in.setPath(normalizeFolder(in.getPath()));
            JOCDefaultResponse response = checkPermissions(accessToken, in, getBasicJocPermissions(accessToken).getInventory().getView());
            if (response == null) {
                ResponseFolder folder = readFolder(in, TRASH_IMPL_PATH);
                folder.setDeploymentDescriptors(null);
                response = responseStatus200(Globals.objectMapper.writeValueAsBytes(folder));
            }
            return response;
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}
