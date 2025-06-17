package com.sos.joc.descriptor.impl;

import java.util.Arrays;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.descriptor.resource.IReadFolderDescriptor;
import com.sos.joc.inventory.impl.common.AReadFolder;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.descriptor.common.RequestFolder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ResponseFolder;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("descriptor")
public class ReadFolderDescriptorImpl extends AReadFolder implements IReadFolderDescriptor {

    @Override
    public JOCDefaultResponse postReadFolder(String accessToken, byte[] body) {
        try {
            body = initLogging(IMPL_PATH_READ_FOLDER, body, accessToken, CategoryType.INVENTORY);
            JsonValidator.validate(body, RequestFolder.class);
            com.sos.joc.model.inventory.common.RequestFolder filter = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.common.RequestFolder.class);
            JOCDefaultResponse response = checkPermissions(accessToken, filter, getBasicJocPermissions(accessToken).getInventory().getView());
            if (response == null) {
                response = responseStatus200(Globals.objectMapper.writeValueAsBytes(readFolder(accessToken, filter, false)));
            }
            return response;
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
    
    @Override
    public JOCDefaultResponse postReadTrashFolder(String accessToken, byte[] body) {
        try {
            body = initLogging(IMPL_PATH_TRASH_READ_FOLDER, body, accessToken, CategoryType.INVENTORY);
            JsonValidator.validateFailFast(body, RequestFolder.class);
            com.sos.joc.model.inventory.common.RequestFolder filter = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.common.RequestFolder.class);

            filter.setPath(normalizeFolder(filter.getPath()));
            JOCDefaultResponse response = checkPermissions(accessToken, filter, getBasicJocPermissions(accessToken).getInventory().getView());
            if (response == null) {
                response = responseStatus200(Globals.objectMapper.writeValueAsBytes(readFolder(accessToken, filter, true)));
            }
            return response;
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    private ResponseFolder readFolder (String accessToken, com.sos.joc.model.inventory.common.RequestFolder filter, boolean forTrash)
            throws Exception {
        filter.setPath(normalizeFolder(filter.getPath()));
        filter.setObjectTypes(Arrays.asList(new ConfigurationType[]{ConfigurationType.DEPLOYMENTDESCRIPTOR}));
        filter.setControllerId(null);
        ResponseFolder folder = null;
        if(forTrash) {
            folder = readFolder(filter, IMPL_PATH_TRASH_READ_FOLDER);
        } else {
            folder = readFolder(filter, IMPL_PATH_READ_FOLDER);
        }
        folder.setCalendars(null);
        folder.setFileOrderSources(null);
        folder.setIncludeScripts(null);
        folder.setJobClasses(null);
        folder.setJobResources(null);
        folder.setJobTemplates(null);
        folder.setLocks(null);
        folder.setNoticeBoards(null);
        folder.setSchedules(null);
        folder.setWorkflows(null);
        return folder;
    }

}
