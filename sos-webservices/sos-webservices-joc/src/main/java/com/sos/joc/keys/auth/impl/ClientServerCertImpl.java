package com.sos.joc.keys.auth.impl;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocAuthenticationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.auth.resource.ICreateClientServerCert;
import com.sos.joc.keys.auth.token.OnetimeTokens;
import com.sos.joc.model.auth.token.OnetimeToken;
import com.sos.joc.model.publish.CreateCSRFilter;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.publish.util.ClientServerCertificateUtil;
import com.sos.schema.JsonValidator;

@Path("authentication")
public class ClientServerCertImpl extends JOCResourceImpl implements ICreateClientServerCert {

    private static String API_CALL = "./authentication/certificate/create";

    @Override
    public JOCDefaultResponse postCreateClientServerCert(HttpServletRequest request, String token, byte[] filter) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, filter);
            JsonValidator.validateFailFast(filter, CreateCSRFilter.class);
            CreateCSRFilter createCsrFilter = Globals.objectMapper.readValue(filter, CreateCSRFilter.class);
            OnetimeTokens onetimeTokens = OnetimeTokens.getInstance();
            OnetimeToken onetimeToken = null;
            if (onetimeTokens != null && !onetimeTokens.getTokens().isEmpty()) {
                onetimeToken = onetimeTokens.getTokens().stream().filter(item -> item.getUUID().equals(token)).filter(Objects::nonNull).findFirst().get();
                if (onetimeToken != null) {
                    if (onetimeToken.getAgentId() != null) {
                        
                    } else if (onetimeToken.getControllerId() != null) {
                        
                    }
                } else {
                    throw new JocAuthenticationException(String.format("One-time token %1$s not found or has expired and was removed from the system.", token));
                }
            } else {
                throw new JocAuthenticationException("No valid one-time token(s) found!");
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            return JOCDefaultResponse.responseStatus200(ClientServerCertificateUtil.createClientServerAuthKeyPair(hibernateSession, createCsrFilter));
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
