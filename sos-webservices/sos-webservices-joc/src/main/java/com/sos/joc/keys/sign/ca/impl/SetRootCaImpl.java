package com.sos.joc.keys.sign.ca.impl;

import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocKeyNotValidException;
import com.sos.joc.keys.ca.resource.ISetRootCa;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.publish.SetRootCaFilter;
import com.sos.joc.model.publish.SetRootCaForSigningFilter;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.model.sign.JocKeyType;
import com.sos.schema.JsonValidator;


@Path("profile/key/ca")
public class SetRootCaImpl extends JOCResourceImpl implements ISetRootCa {

    private static final String API_CALL = "./profile/key/ca/store";

    @Override
    public JOCDefaultResponse postSetRootCa(String xAccessToken, byte[] filter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, filter, xAccessToken);
            JsonValidator.validateFailFast(filter, SetRootCaForSigningFilter.class);
            SetRootCaForSigningFilter setRootCaFilter = Globals.objectMapper.readValue(filter, SetRootCaForSigningFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(xAccessToken).getAdministration().getCertificates().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(setRootCaFilter.getAuditLog(), CategoryType.CERTIFICATES);
            X509Certificate cert;
            try {
                cert = KeyUtil.getX509Certificate(setRootCaFilter.getCertificate());
            } catch (Exception e) {
                throw new JocKeyNotValidException("Certificate data is not a known certificate type!");
            }
            JocKeyPair keyPair = new JocKeyPair();
            keyPair.setCertificate(setRootCaFilter.getCertificate());
            keyPair.setKeyID(cert.getSubjectDN().getName());
            keyPair.setValidUntil(cert.getNotAfter());
            keyPair.setKeyAlgorithm(SOSKeyConstants.ECDSA_ALGORITHM_NAME);
            keyPair.setKeyType(JocKeyType.CA.name());
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            if (keyPair.getCertificate() != null && !keyPair.getCertificate().isEmpty()) {
                DBLayerKeys dbLayer = new DBLayerKeys(hibernateSession);
                dbLayer.saveOrUpdateSigningRootCaCertificate(keyPair, jobschedulerUser.getSOSAuthCurrentAccount().getAccountname(), Globals.getJocSecurityLevel().intValue());
            } 
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
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
