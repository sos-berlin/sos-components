package com.sos.joc.publish.impl;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.publish.resource.IRedeploy;

@Path("inventory/deployment")
public class RedeployImpl extends JOCResourceImpl implements IRedeploy {

    @Override
    public JOCDefaultResponse postRedeploy(String xAccessToken, byte[] filter) throws Exception {
        try {
            //  Use ./inventory/export and ./inventory/deployment/import_deploy instead.
            throw new JocNotImplementedException("The web service is not available for Security Level HIGH.");
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
        }
    }

}