package com.sos.joc.descriptor.impl;

import java.util.Arrays;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.descriptor.resource.IReadFolderDescriptor;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.impl.common.AReadFolder;
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
            initLogging(IMPL_PATH_READ_FOLDER, body, accessToken);
            JsonValidator.validate(body, RequestFolder.class);
            com.sos.joc.model.inventory.common.RequestFolder filter = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.common.RequestFolder.class);
            JOCDefaultResponse response = checkPermissions(accessToken, filter, getBasicJocPermissions(accessToken).getInventory().getView());
            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(readFolder(accessToken, filter, false));
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
    public JOCDefaultResponse postReadTrashFolder(String accessToken, byte[] body) {
        try {
            initLogging(IMPL_PATH_TRASH_READ_FOLDER, body, accessToken);
            JsonValidator.validateFailFast(body, RequestFolder.class);
            com.sos.joc.model.inventory.common.RequestFolder filter = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.common.RequestFolder.class);

            filter.setPath(normalizeFolder(filter.getPath()));
            JOCDefaultResponse response = checkPermissions(accessToken, filter, getBasicJocPermissions(accessToken).getInventory().getView());
            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(readFolder(accessToken, filter, true));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
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
