package com.sos.joc.publish.impl;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.publish.resource.IDeploy;

import jakarta.ws.rs.Path;

@Path("inventory/deployment")
public class DeployImpl extends ADeploy implements IDeploy {
    
    public static final JocSecurityLevel SEC_LVL = JocSecurityLevel.HIGH; 

    @Override
    public JOCDefaultResponse postDeploy(String xAccessToken, byte[] filter) {
        return postDeploy(xAccessToken, filter, false);
    }

    public JOCDefaultResponse postDeploy(String xAccessToken, byte[] filter, boolean withoutFolderDeletion) {
        try {
            //  Use ./inventory/export and ./inventory/deployment/import_deploy instead.
            throw new JocNotImplementedException("The web service is not available for Security Level HIGH.");
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}