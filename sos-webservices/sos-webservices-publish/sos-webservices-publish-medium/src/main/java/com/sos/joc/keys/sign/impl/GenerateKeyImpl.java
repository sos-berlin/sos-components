package com.sos.joc.keys.sign.impl;

import java.security.cert.X509Certificate;
import java.util.Date;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.ca.CAUtils;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.sign.resource.IGenerateKey;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.publish.CreateCSRFilter;
import com.sos.joc.model.publish.GenerateKeyFilter;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.publish.util.SigningCertificateUtil;
import com.sos.schema.JsonValidator;

@Path("profile/key")
public class GenerateKeyImpl extends JOCResourceImpl implements IGenerateKey {

    private static final String API_CALL = "./profile/key/generate";

    @Override
    public JOCDefaultResponse postGenerateKey(String xAccessToken, byte[] generateKeyFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, generateKeyFilter, xAccessToken);
            JsonValidator.validateFailFast(generateKeyFilter, GenerateKeyFilter.class);
            GenerateKeyFilter filter = Globals.objectMapper.readValue(generateKeyFilter, GenerateKeyFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getAdministration().getCertificates()
                    .getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(filter.getAuditLog(), CategoryType.CERTIFICATES);
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayer = new DBLayerKeys(hibernateSession);
            
            Date validUntil = filter.getValidUntil();
            if (filter.getKeyAlgorithm() == null) {
                // default
                filter.setKeyAlgorithm(SOSKeyConstants.RSA_ALGORITHM_NAME);
            }
            JocKeyPair keyPair = null;
            boolean rootCaAvailable = false;
            JocKeyPair rootKeyPair = dbLayer.getAuthRootCaKeyPair();
            X509Certificate rootCert = null;
            if (rootKeyPair != null) {
                rootCaAvailable = true;
                rootCert = KeyUtil.getX509Certificate(rootKeyPair.getCertificate());
            }
            String accountName = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname();
            if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(filter.getKeyAlgorithm())) {
                if (validUntil != null) {
                    Long secondsToExpire = validUntil.getTime() / 1000;
                    keyPair = KeyUtil.createKeyPair(jobschedulerUser.getSOSAuthCurrentAccount().getAccountname(), null, secondsToExpire);
                } else {
                    keyPair = KeyUtil.createKeyPair(jobschedulerUser.getSOSAuthCurrentAccount().getAccountname(), null, null);
                }
            } else {
                if (rootCaAvailable && rootCert != null) {
                    // first: get new PK and X509 certificate from stored CA
                    String newDN = CAUtils.createUserSubjectDN("CN=" + accountName, rootCert);
                    String san = accountName;
                    CreateCSRFilter csrFilter = new CreateCSRFilter();
                    csrFilter.setDn(newDN);
                    csrFilter.setSan(san);
                    keyPair = SigningCertificateUtil.createSigningKeyPair(hibernateSession, csrFilter, filter.getKeyAlgorithm(), validUntil);
                } else {
                    if (SOSKeyConstants.RSA_ALGORITHM_NAME.equals(filter.getKeyAlgorithm())) {
                        // default
                        keyPair = KeyUtil.createRSAJocKeyPair();
                    } else {
                        keyPair = KeyUtil.createECDSAJOCKeyPair();
                    }
                }
            }
            keyPair.setKeyAlgorithm(filter.getKeyAlgorithm());
            // store private key to the db
            dbLayer.saveOrUpdateGeneratedKey(keyPair, accountName, JocSecurityLevel.MEDIUM);
            // update CA info for signing key
            if(rootCaAvailable) {
                dbLayer.saveOrUpdateSigningRootCaCertificate(rootKeyPair, accountName, Globals.getJocSecurityLevel().intValue());
            }
            return JOCDefaultResponse.responseStatus200(keyPair);
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
