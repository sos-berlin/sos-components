package com.sos.joc.keys.ca.impl;

import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.ca.CAUtils;
import com.sos.commons.sign.keys.certificate.CertificateUtils;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.ca.resource.IGenerateRootCa;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.publish.GenerateCaFilter;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.schema.JsonValidator;


@Path("profile/ca")
public class GenerateRootCaImpl extends JOCResourceImpl implements IGenerateRootCa {

    private static final String API_CALL = "./profile/ca/generate";

    @Override
    public JOCDefaultResponse postGenerateRootCa(String xAccessToken, byte[] generateCAFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, generateCAFilter, xAccessToken);
            JsonValidator.validateFailFast(generateCAFilter, GenerateCaFilter.class);
            GenerateCaFilter filter = Globals.objectMapper.readValue(generateCAFilter, GenerateCaFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getAdministration().getCertificates()
                    .getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            storeAuditLog(filter.getAuditLog(), CategoryType.CERTIFICATES);
            
            KeyPair keyPair = KeyUtil.createECDSAKeyPair();
            String subjectDN = CAUtils.createRootSubjectDN(filter.getCommonName(), filter.getOrganizationUnit(), filter.getOrganization(), filter.getCountryCode());
            Certificate cert = CAUtils.createSelfSignedRootCertificate(SOSKeyConstants.ECDSA_ALGORITHM_NAME, keyPair, subjectDN, true, false);
            JocKeyPair jocKeyPair = KeyUtil.createECDSAJOCKeyPair(keyPair);
            jocKeyPair.setCertificate(CertificateUtils.asPEMString((X509Certificate)cert));
            jocKeyPair.setKeyAlgorithm(SOSKeyConstants.ECDSA_ALGORITHM_NAME);
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            // store private key to the db
            dbLayerKeys.saveOrUpdateGeneratedKey(jocKeyPair, 
                    jobschedulerUser.getSosShiroCurrentUser().getUsername(),
                    JocSecurityLevel.LOW);
            return JOCDefaultResponse.responseStatus200(jocKeyPair);
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
