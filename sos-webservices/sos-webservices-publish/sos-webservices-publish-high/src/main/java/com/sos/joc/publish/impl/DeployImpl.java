package com.sos.joc.publish.impl;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.publish.resource.IDeploy;

import jakarta.ws.rs.Path;

@Path("inventory/deployment")
public class DeployImpl extends ADeploy implements IDeploy {

    @Override
    public JOCDefaultResponse postDeploy(String xAccessToken, byte[] filter) {
        return postDeploy(xAccessToken, filter, false);
    }

    public JOCDefaultResponse postDeploy(String xAccessToken, byte[] filter, boolean withoutFolderDeletion) {
        SOSHibernateSession hibernateSession = null;
        try {
            //  Use ./inventory/export and ./inventory/deployment/import_deploy instead.
            throw new JocNotImplementedException("The web service is not available for Security Level HIGH.");
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}