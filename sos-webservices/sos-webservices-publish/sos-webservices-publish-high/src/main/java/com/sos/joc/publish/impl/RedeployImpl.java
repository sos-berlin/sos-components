package com.sos.joc.publish.impl;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.publish.resource.IRedeploy;

import jakarta.ws.rs.Path;

@Path("inventory/deployment")
public class RedeployImpl extends JOCResourceImpl implements IRedeploy {

    @Override
    public JOCDefaultResponse postRedeploy(String xAccessToken, byte[] filter) {
        try {
            //  Use ./inventory/export and ./inventory/deployment/import_deploy instead.
            throw new JocNotImplementedException("The web service is not available for Security Level HIGH.");
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
    
    @Override
    public JOCDefaultResponse postSync(String xAccessToken, byte[] filter) {
        try {
            //  Use ./inventory/export and ./inventory/deployment/import_deploy instead.
            throw new JocNotImplementedException("The web service is not available for Security Level HIGH.");
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}