package com.sos.joc.keys.auth.impl;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.keys.auth.resource.ICreateClientServerCert;
import com.sos.joc.keys.auth.token.OnetimeTokens;
import com.sos.joc.model.auth.token.OnetimeToken;
import com.sos.joc.model.publish.CreateCSRFilter;
import com.sos.joc.publish.util.ClientServerCertificateUtil;
import com.sos.schema.JsonValidator;

@Path("authentication")
public class ClientServerCertImpl implements ICreateClientServerCert {

    private static String API_CALL = "./authentication/certificate/create";

    @Override
    @Consumes(MediaType.APPLICATION_JSON)
    public JOCDefaultResponse postCreateClientServerCert(HttpServletRequest request, String token, byte[] filter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            JsonValidator.validateFailFast(filter, CreateCSRFilter.class);
            CreateCSRFilter createCsrFilter = Globals.objectMapper.readValue(filter, CreateCSRFilter.class);
            OnetimeTokens onetimeTokens = OnetimeTokens.getInstance();
            OnetimeToken onetimeToken = onetimeTokens.getTokens().stream().filter(item -> item.getUUID().equals(token)).findFirst().get();
            if (onetimeToken.getAgentId() != null) {
                
            } else if (onetimeToken.getControllerId() != null) {
                
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            return JOCDefaultResponse.responseStatus200(ClientServerCertificateUtil.createClientServerAuthKeyPair(hibernateSession, createCsrFilter));
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }


}
