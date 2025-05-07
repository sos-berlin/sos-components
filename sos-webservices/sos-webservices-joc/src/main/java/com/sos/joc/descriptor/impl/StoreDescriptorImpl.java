package com.sos.joc.descriptor.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.descriptor.resource.IStoreDescriptor;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.impl.common.AStoreConfiguration;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("descriptor")
public class StoreDescriptorImpl extends AStoreConfiguration implements IStoreDescriptor {

    @Override
    public JOCDefaultResponse store(String accessToken, byte[] body) {
        try {
            body = initLogging(IMPL_PATH_STORE, body, accessToken);
            JsonValidator.validate(body, ConfigurationObject.class, true);
            ConfigurationObject filter = Globals.objectMapper.readValue(body, ConfigurationObject.class);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
            if(!JocInventory.isDescriptor(filter.getObjectType())) {
                throw new JocBadRequestException("wrong object type, only DEPLOYMENTDESCRIPTOR or DESCRIPTORFOLDER are allowed.");
            }
            if (response == null) {
                response = store(filter, ConfigurationType.DESCRIPTORFOLDER, IMPL_PATH_STORE);
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
