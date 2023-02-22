package com.sos.joc.descriptor.impl;

import java.util.Arrays;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.descriptor.resource.IReadDescriptor;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.impl.FolderResourceImpl;
import com.sos.joc.inventory.impl.ReadConfigurationResourceImpl;
import com.sos.joc.inventory.resource.IFolderResource;
import com.sos.joc.model.descriptor.common.RequestFilter;
import com.sos.joc.model.descriptor.common.RequestFolder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ResponseFolder;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path(JocInventory.APPLICATION_PATH)
public class ReadDescriptorImpl extends JOCResourceImpl implements IReadDescriptor {

    @Override
    public JOCDefaultResponse read(String accessToken, byte[] body) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH_READ, body, accessToken);
            JsonValidator.validate(body, RequestFilter.class, true);
            com.sos.joc.model.inventory.read.RequestFilter filter = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.read.RequestFilter.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());
            if (response == null) {
                ReadConfigurationResourceImpl readConfiguration = new ReadConfigurationResourceImpl();
                filter.setObjectType(ConfigurationType.DEPLOYMENTDESCRIPTOR);
                response = readConfiguration.read(filter);
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
    public JOCDefaultResponse readFolder(String accessToken, byte[] body) {
        try {
            initLogging(IMPL_PATH_READ_FOLDER, body, accessToken);
            JsonValidator.validate(body, RequestFolder.class);
            com.sos.joc.model.inventory.common.RequestFolder filter = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.common.RequestFolder.class);
            boolean permission = getJocPermissions(accessToken).getInventory().getView();
            FolderResourceImpl folderImpl = new  FolderResourceImpl();
            JOCDefaultResponse response = folderImpl.checkPermissions(accessToken, filter, permission);
            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(readFolder(accessToken, filter, folderImpl, false));
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
    public JOCDefaultResponse readTrash(String accessToken, byte[] body) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH_TRASH_READ, body, accessToken);
            JsonValidator.validate(body, RequestFilter.class, true);
            com.sos.joc.model.inventory.read.RequestFilter filter = Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.read.RequestFilter.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());
            if (response == null) {
                ReadConfigurationResourceImpl readConfiguration = new ReadConfigurationResourceImpl();
                filter.setObjectType(ConfigurationType.DEPLOYMENTDESCRIPTOR);
                response = readConfiguration.readTrash(filter);
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
    public JOCDefaultResponse readTrashFolder(String accessToken, byte[] body) {
        try {
            initLogging(IMPL_PATH_TRASH_READ_FOLDER, body, accessToken);
            JsonValidator.validateFailFast(body, RequestFolder.class);
            com.sos.joc.model.inventory.common.RequestFolder filter = 
                    Globals.objectMapper.readValue(body, com.sos.joc.model.inventory.common.RequestFolder.class);

            filter.setPath(normalizeFolder(filter.getPath()));
            boolean permission = getJocPermissions(accessToken).getInventory().getView();
            FolderResourceImpl folderImpl = new  FolderResourceImpl();
            JOCDefaultResponse response = folderImpl.checkPermissions(accessToken, filter, permission);
            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(readFolder(accessToken, filter, folderImpl, true));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private ResponseFolder readFolder (String accessToken, com.sos.joc.model.inventory.common.RequestFolder filter,
            FolderResourceImpl folderImpl, boolean forTrash) throws Exception {
        filter.setPath(normalizeFolder(filter.getPath()));
        filter.setObjectTypes(Arrays.asList(new ConfigurationType[]{ConfigurationType.DEPLOYMENTDESCRIPTOR}));
        filter.setControllerId(null);
        ResponseFolder folder = null;
        if(forTrash) {
            folder = folderImpl.readFolder(filter, IFolderResource.TRASH_IMPL_PATH);
        } else {
            folder = folderImpl.readFolder(filter, IFolderResource.IMPL_PATH);
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
