package com.sos.joc.inventory.impl;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.inventory.resource.IDeleteConfigurationResource;

@Path(JocInventory.APPLICATION_PATH)
public class DeleteConfigurationResourceImpl extends JOCResourceImpl implements IDeleteConfigurationResource {

    @Override
    public JOCDefaultResponse delete(final String accessToken, final byte[] inBytes) {
        return JOCDefaultResponse.responseNotYetImplemented();

    }

}
