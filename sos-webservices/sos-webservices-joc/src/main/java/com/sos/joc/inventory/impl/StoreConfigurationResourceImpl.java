package com.sos.joc.inventory.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.impl.common.AStoreConfiguration;
import com.sos.joc.inventory.resource.IStoreConfigurationResource;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocInventory.APPLICATION_PATH)
public class StoreConfigurationResourceImpl extends AStoreConfiguration implements IStoreConfigurationResource {

    @Override
    public JOCDefaultResponse store(final String accessToken, byte[] inBytes) {
        try {
            inBytes = initLogging(IMPL_PATH, inBytes, accessToken, CategoryType.INVENTORY);
            JsonValidator.validate(inBytes, ConfigurationObject.class, true);
            ConfigurationObject in = Globals.objectMapper.readValue(inBytes, ConfigurationObject.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
            if (response == null) {
                response = store(in, ConfigurationType.FOLDER, IMPL_PATH);
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
