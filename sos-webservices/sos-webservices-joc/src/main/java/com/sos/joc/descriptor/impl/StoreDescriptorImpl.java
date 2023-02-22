package com.sos.joc.descriptor.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.descriptor.resource.IStoreDescriptor;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.impl.StoreConfigurationResourceImpl;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path(JocInventory.APPLICATION_PATH)
public class StoreDescriptorImpl extends JOCResourceImpl implements IStoreDescriptor {

    @Override
    public JOCDefaultResponse store(String accessToken, byte[] body) {
        try {
            initLogging(IMPL_PATH_STORE, body, accessToken);
            JsonValidator.validate(body, ConfigurationObject.class, true);
            ConfigurationObject filter = Globals.objectMapper.readValue(body, ConfigurationObject.class);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if(!ConfigurationType.DEPLOYMENTDESCRIPTOR.equals(filter.getObjectType()) 
                    && !ConfigurationType.DESCRIPTORFOLDER.equals(filter.getObjectType())) {
                throw new JocBadRequestException("wrong object type, only DEPLOYMENTDESCRIPTOR or DESCRIPTORFOLDER are allowed.");
            }
            if (response == null) {
                StoreConfigurationResourceImpl storeConfiguration = new StoreConfigurationResourceImpl();
                response = storeConfiguration.store(filter, ConfigurationType.DESCRIPTORFOLDER);
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
