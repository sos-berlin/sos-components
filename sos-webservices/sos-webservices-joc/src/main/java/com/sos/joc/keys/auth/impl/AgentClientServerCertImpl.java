package com.sos.joc.keys.auth.impl;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.auth.resource.IAgentClientServerCert;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.publish.CreateCSRFilter;
import com.sos.joc.publish.util.ClientServerCertificateUtil;
import com.sos.schema.JsonValidator;

@Path("agent")
public class AgentClientServerCertImpl extends JOCResourceImpl implements IAgentClientServerCert {

    private static String API_CALL = "./agent/certificate";

    @Override
    public JOCDefaultResponse postCreateAgentClientServerCert(String xAccessToken, byte[] filter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, filter, xAccessToken);
            JsonValidator.validateFailFast(filter, CreateCSRFilter.class);
            CreateCSRFilter createCsrFilter = Globals.objectMapper.readValue(filter, CreateCSRFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getAdministration().getCertificates().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            storeAuditLog(createCsrFilter.getAuditLog(), CategoryType.CERTIFICATES);

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
