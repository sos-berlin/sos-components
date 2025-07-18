package com.sos.joc.keys.ca.impl;

import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.ca.CAUtils;
import com.sos.commons.sign.keys.certificate.CertificateUtils;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.keys.ca.resource.IGenerateRootCa;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.publish.GenerateCaFilter;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.model.sign.JocKeyType;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("profile/ca")
public class GenerateRootCaImpl extends JOCResourceImpl implements IGenerateRootCa {

    private static final String API_CALL = "./profile/ca/generate";

    @Override
    public JOCDefaultResponse postGenerateRootCa(String xAccessToken, byte[] generateCAFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            generateCAFilter = initLogging(API_CALL, generateCAFilter, xAccessToken, CategoryType.CERTIFICATES);
            JsonValidator.validateFailFast(generateCAFilter, GenerateCaFilter.class);
            GenerateCaFilter filter = Globals.objectMapper.readValue(generateCAFilter, GenerateCaFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).map(p -> p.getAdministration()
                    .getCertificates().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            storeAuditLog(filter.getAuditLog());
            
            KeyPair keyPair = KeyUtil.createECDSAKeyPair();
            String subjectDN = filter.getDn();
            Certificate cert = CAUtils.createSelfSignedRootCertificate(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, keyPair, subjectDN, true, true);
            JocKeyPair jocKeyPair = KeyUtil.createECDSAJOCKeyPair(keyPair);
            jocKeyPair.setCertificate(CertificateUtils.asPEMString((X509Certificate)cert));
            jocKeyPair.setKeyAlgorithm(SOSKeyConstants.ECDSA_ALGORITHM_NAME);
            jocKeyPair.setKeyType(JocKeyType.CA.name());
            X509Certificate x509Cert = (X509Certificate)cert;
            jocKeyPair.setKeyID(x509Cert.getSubjectX500Principal().getName());
            jocKeyPair.setValidUntil(x509Cert.getNotAfter());
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            // store private key to the db
            PublishUtils.storeAuthCA(jocKeyPair, hibernateSession);
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(jocKeyPair));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}
