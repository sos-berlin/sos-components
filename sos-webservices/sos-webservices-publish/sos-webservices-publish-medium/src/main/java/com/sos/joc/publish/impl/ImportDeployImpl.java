package com.sos.joc.publish.impl;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.publish.resource.IImportDeploy;

import jakarta.ws.rs.Path;

@Path("inventory/deployment")
public class ImportDeployImpl extends JOCResourceImpl implements IImportDeploy {

    @Override
    public JOCDefaultResponse postImportDeploy(String xAccessToken, FormDataBodyPart body, String controllerId, String signatureAlgorithm,
            String format, String timeSpent, String ticketLink, String comment) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            throw new JocNotImplementedException("The web service is not available for Security Level MEDIUM. Only Security Level HIGH is supported.");
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}
